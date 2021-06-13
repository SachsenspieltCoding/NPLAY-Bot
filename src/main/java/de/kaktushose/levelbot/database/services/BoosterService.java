package de.kaktushose.levelbot.database.services;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.NitroBooster;
import de.kaktushose.levelbot.database.model.Reward;
import de.kaktushose.levelbot.database.repositories.NitroBoosterRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class BoosterService {

    private final NitroBoosterRepository nitroBoosterRepository;
    private final Levelbot levelbot;
    private final UserService userService;
    private final SettingsService settingsService;

    public BoosterService(Levelbot levelbot) {
        ApplicationContext context = ApplicationContextHolder.getContext();
        nitroBoosterRepository = context.getBean(NitroBoosterRepository.class);
        this.userService = levelbot.getUserService();
        this.settingsService = levelbot.getSettingsService();
        this.levelbot = levelbot;
    }

    public List<NitroBooster> getAllNitroBoosters() {
        List<NitroBooster> result = new ArrayList<>();
        nitroBoosterRepository.findAll().forEach(result::add);
        return result;
    }

    public List<NitroBooster> getActiveNitroBoosters() {
        return nitroBoosterRepository.getActiveNitroBoosters();
    }

    public boolean isNitroBooster(long userId) {
        return nitroBoosterRepository.findById(userId).isPresent();
    }

    public boolean isActiveNitroBooster(long userId) {
        return getActiveNitroBoosters().stream().map(NitroBooster::getUserId).anyMatch(((Long) userId)::equals);
    }

    public void createNewNitroBooster(long userId) {
        nitroBoosterRepository.save(new NitroBooster(userId, System.currentTimeMillis(), true));
    }

    public void changeNitroBoosterStatus(long userId, boolean active) {
        NitroBooster nitroBooster = nitroBoosterRepository.findById(userId).orElseThrow();
        nitroBooster.setActive(active);
        nitroBoosterRepository.save(nitroBooster);
    }

    public String addMonthlyReward(long userId) {
        Reward reward = settingsService.getMonthlyNitroBoosterReward();
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        if (reward.getItem() != null) {
            userService.addUpItem(userId, reward.getItem().getItemId(), levelbot);
        }
        return reward.getMessage();
    }

    public String addOneTimeReward(long userId) {
        Reward reward = settingsService.getOneTimeNitroBoosterReward();
        userService.addCoins(userId, reward.getCoins());
        userService.addXp(userId, reward.getXp());
        userService.addDiamonds(userId, reward.getDiamonds());
        if (reward.getItem() != null) {
            userService.addUpItem(userId, reward.getItem().getItemId(), levelbot);
        }
        return reward.getMessage();
    }
}
