package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.database.model.GuildSettings;
import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.database.repositories.RewardRepository;
import de.kaktushose.levelbot.database.repositories.SettingsRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final RewardRepository rewardRepository;

    public SettingsService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        this.settingsRepository = context.getBean(SettingsRepository.class);
        this.rewardRepository = context.getBean(RewardRepository.class);
    }

    private GuildSettings getGuildSettings(long guildId) {
        return settingsRepository.getGuildSettings(guildId).orElseThrow();
    }

    public long getBotChannelId(long guildId) {
        return getGuildSettings(guildId).getBotChannelId();
    }

    public String getBotPrefix(long guildId) {
        return getGuildSettings(guildId).getBotPrefix();
    }

    public String getBotToken(long guildId) {
        return getGuildSettings(guildId).getBotToken();
    }

    public String getVersion(long guildId) {
        return getGuildSettings(guildId).getVersion();
    }

    public long getMessageCooldown(long guildId) {
        return getGuildSettings(guildId).getMessageCooldown();
    }

    public void setMessageCooldown(long guildId, long cooldown) {
        GuildSettings settings = getGuildSettings(guildId);
        settings.setMessageCooldown(cooldown);
        settingsRepository.save(settings);
    }

    public String getYoutubeApiKey(long guildId) {
        return getGuildSettings(guildId).getYoutubeApiKey();
    }

    public boolean isIgnoredChannel(long channelId) {
        return settingsRepository.getIgnoredChannels().contains(channelId);
    }

    public Reward getReward(int rewardLevel) {
        return rewardRepository.findById(15 + rewardLevel).orElseThrow();
    }

    public Reward getMonthlyNitroBoosterReward() {
        return rewardRepository.findById(12).orElseThrow();
    }

    public Reward getOneTimeNitroBoosterReward() {
        return rewardRepository.findById(11).orElseThrow();
    }

    public void setEventChannelId(long guildId, long channelId) {
        GuildSettings settings = getGuildSettings(guildId);
        settings.setEventChannelId(channelId);
        settingsRepository.save(settings);
    }

    public long getEventChannelId(long guildId) {
        GuildSettings settings = getGuildSettings(guildId);
        return settings.getEventChannelId();
    }

    public void setEventEmote(long guildId, String emote) {
        GuildSettings settings = getGuildSettings(guildId);
        settings.setEventEmote(emote);
        settingsRepository.save(settings);
    }

    public String getEventEmote(long guildId) {
        GuildSettings settings = getGuildSettings(guildId);
        return settings.getEventEmote();
    }
}
