package com.github.kaktushose.nplaybot.items;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemExpirationTask {

    public static final int PLAY_ACTIVITY_KARMA_THRESHOLD = 30;
    private final ScheduledExecutorService executor;

    public ItemExpirationTask() {
        executor = Executors.newScheduledThreadPool(4, runnable -> new Thread(runnable, "ItemExpiration"));
    }

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS)
    public void onCheckItems(Bot bot) {
        var transactions = bot.getDatabase().getItemService().getExpiringTransactions();

        for (var transaction : transactions) {
            executor.schedule(() -> {
                bot.getDatabase().getItemService().deleteTransaction(
                        UserSnowflake.fromId(transaction.userId()),
                        transaction.transactionId(),
                        bot.getGuild()
                );

                if (transaction.isPlayActivity()) {
                    var rankInfo = bot.getDatabase().getRankService().getUserInfo(UserSnowflake.fromId(transaction.userId()));
                    if (rankInfo.karma() - rankInfo.lastKarma() >= PLAY_ACTIVITY_KARMA_THRESHOLD) {
                        bot.getDatabase().getItemService().addPlayActivity(UserSnowflake.fromId(transaction.userId()), bot.getGuild());
                        bot.getDatabase().getItemService().updateLastKarma(UserSnowflake.fromId(transaction.userId()));

                        messageUser(transaction, bot.getEmbedCache().getEmbed("playActivityRenew").toEmbedBuilder(), bot);
                        return;
                    }
                }

                var item = bot.getDatabase().getItemService().getItem(transaction.itemId());
                var emoji = bot.getDatabase().getItemService().getTypeEmoji(item.typeId());
                EmbedBuilder embed = bot.getEmbedCache().getEmbed("itemExpired")
                        .injectValue("item", String.format("%s %s", emoji, item.name()))
                        .toEmbedBuilder();

                messageUser(transaction, embed, bot);

            }, transaction.expiresAt() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void messageUser(ItemService.Transaction transaction, EmbedBuilder embed, Bot bot) {
        var user = bot.getJda().getUserById(transaction.userId());
        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed.build()))
                .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> {
                    TextChannel channel = bot.getDatabase().getSettingsService().getBotChannel(bot.getGuild());
                    channel.sendMessage(user.getAsMention()).and(channel.sendMessageEmbeds(embed.build())).queue();
                }));
    }

}
