package me.lofro.uhc.managers;

import lombok.Getter;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.CommandUtils;
import me.lofro.uhc.api.ListenerUtils;
import me.lofro.uhc.api.data.JsonConfig;
import me.lofro.uhc.api.data.interfaces.Restorable;
import me.lofro.uhc.api.infinitepotion.InfinitePotionEffect;
import me.lofro.uhc.api.text.ChatColorFormatter;
import me.lofro.uhc.api.text.HexFormatter;
import me.lofro.uhc.api.timer.GameTimer;
import me.lofro.uhc.commands.TeamCommand;
import me.lofro.uhc.data.GameData;
import me.lofro.uhc.data.Team;
import me.lofro.uhc.listeners.GameListeners;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager implements Restorable {

    private final UHC uhc;

    private final GameListeners gameListener;

    private @Getter GameData gameData;

    private final Scoreboard scoreboard;

    private final Objective objective;

    private int taskID;

    private @Getter boolean isInGame = false;

    private final List<Score> scores;

    private final TabListFormatManager tablistFormatManager = UHC.getTabAPI().getTabListFormatManager();
    private final HeaderFooterManager headerFooterManager = UHC.getTabAPI().getHeaderFooterManager();

    public GameManager(UHC uhc, GameListeners gameListener) {
        this.uhc = uhc;
        this.gameListener = gameListener;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("time", Criteria.create("count"), HexFormatter.hexFormat("&6&lRefurbished UHC"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.scores = new ArrayList<>();
    }

    public void startGame() {
        this.isInGame = true;
        UHC.getInstance().getDataManager().restore();

        ListenerUtils.registerListener(gameListener);
        CommandUtils.registerCommands(uhc.getPaperCommandManager(), new TeamCommand());

        Bukkit.getOnlinePlayers().forEach(player -> {
            showScoreboard(player);
            if (getChapter(gameData.getTime()) >= 9) {
                InfinitePotionEffect.create(player, PotionEffectType.DAMAGE_RESISTANCE, 0);
                var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(maxHealth.getBaseValue() + 20);
            }
        });

        this.taskID = Bukkit.getScheduler().runTaskTimer(uhc, () -> {
            this.scores.forEach(score -> scoreboard.resetScores(score.getEntry()));

            var newTime = gameData.getTime() + 1;
            var chapter = getChapter(newTime);
            var newChapterTime = (chapter != 1) ? (((newTime / 1500F) + 1) - chapter) * 1500 : newTime;

            if (chapter > getChapter(gameData.getTime())) {
                Bukkit.broadcast(ChatColorFormatter.componentWithPrefix("&7Ha dado comienzo el episodio " + chapter + "."));
                switch (chapter) {
                    case 9 -> {
                        Bukkit.broadcast(ChatColorFormatter.componentWithPrefix("&a¡Todos los jugadores han obtenido mejoras!"));
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (maxHealth != null) maxHealth.setBaseValue(maxHealth.getBaseValue() + 20);

                            InfinitePotionEffect.create(player, PotionEffectType.DAMAGE_RESISTANCE, 0);
                        });
                    }
                    case 4 -> Bukkit.broadcast(ChatColorFormatter.componentWithPrefix("&cSe ha activado el PVP."));
                }
            }

            gameData.setTime(newTime);

            var separatorScore0 = objective.getScore("       ");
            var chapterScoreTitle = objective.getScore(ChatColorFormatter.string("&a✰ Episodio: &r" + chapter));
            var separatorScore1 = objective.getScore(" ");
            var chapterScoreTimeTitle = objective.getScore(ChatColorFormatter.string("&b⌚ Tiempo del episodio:"));
            var chapterScoreTime = objective.getScore(ChatColorFormatter.string("&7"+timeConvert(Math.round(newChapterTime)) + " "));
            var separatorScore2 = objective.getScore("  ");
            var totalTimeScoreTitle = objective.getScore(ChatColorFormatter.string("&e⌛ Tiempo de juego:"));
            var totalTimeScore = objective.getScore(ChatColorFormatter.string("&7"+timeConvert(newTime)));

            separatorScore0.setScore(7);
            chapterScoreTitle.setScore(6);
            separatorScore1.setScore(5);
            chapterScoreTimeTitle.setScore(4);
            chapterScoreTime.setScore(3);
            separatorScore2.setScore(2);
            totalTimeScoreTitle.setScore(1);
            totalTimeScore.setScore(0);

            scores.add(chapterScoreTitle);
            scores.add(chapterScoreTime);
            scores.add(totalTimeScore);

            String topper = formatString("#FFFFFF                  #FFAA00Refurbished UHC #FFFFFF                 ", "");
            String footer = formatString("", "#FFFFFF     #FFAA00☀ #E8B465Capitulo actual: #EDC588" + chapter);

            Bukkit.getOnlinePlayers().forEach(online -> {
                var tabPlayer = TabAPI.getInstance().getPlayer(online.getUniqueId());
                if (!(tabPlayer != null && headerFooterManager != null && tablistFormatManager != null)) return;
                headerFooterManager.setHeader(tabPlayer, topper);
                headerFooterManager.setFooter(tabPlayer, footer);

                var absorption = online.getPotionEffect(PotionEffectType.ABSORPTION);
                var health = absorption != null ? (int)(online.getHealth() + absorption.getAmplifier() + 4) : (int)online.getHealth();
                tablistFormatManager.setSuffix(tabPlayer, " " + getHealthColor(health) + health + "#EF2A2A❤");

                var teamList = getTeam(online.getUniqueId());

                if (teamList != null && !teamList.isEmpty()) {
                    var team = teamList.get(0);

                    Bukkit.getOnlinePlayers().forEach(all -> {
                        var teamMembers = team.getMembers();
                        if (teamMembers.isEmpty()) return;
                        if (!teamMembers.contains(all.getUniqueId())) {
                            //modifyDisplayName(online, all, ChatColorFormatter.string("&c" + all.getName()));
                        } else {
                            //modifyDisplayName(online, all, ChatColorFormatter.string("&a" + all.getName()));
                        }
                    });
                }
            });

        }, 0, 20).getTaskId();
    }

    public void stopGame() {
        this.isInGame = false;
        Bukkit.getScheduler().cancelTask(this.taskID);

        Bukkit.getOnlinePlayers().forEach(player -> {
            hideScoreBoard(player);
            if (getChapter(gameData.getTime()) >= 9) InfinitePotionEffect.remove(player, PotionEffectType.DAMAGE_RESISTANCE);

            var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) maxHealth.setBaseValue(20);
        });

        ListenerUtils.unregisterListener(gameListener);

        UHC.getInstance().getDataManager().save();
    }

    public void endGame() {
        this.isInGame = false;
        Bukkit.getScheduler().cancelTask(this.taskID);

        Bukkit.getOnlinePlayers().forEach(player -> {
            hideScoreBoard(player);
            if (getChapter(gameData.getTime()) >= 9) InfinitePotionEffect.remove(player, PotionEffectType.DAMAGE_RESISTANCE);

            var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) maxHealth.setBaseValue(20);
        });

        gameData.setTime(0);
        gameData.getTeams().clear();

        ListenerUtils.unregisterListener(gameListener);

        UHC.getInstance().getDataManager().save();
    }

    public void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void hideScoreBoard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @Override
    public void save(JsonConfig jsonConfig) {
        jsonConfig.setJsonObject(UHC.getGson().toJsonTree(gameData).getAsJsonObject());
        try {
            jsonConfig.save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void restore(JsonConfig jsonConfig) {
        if (jsonConfig.getJsonObject().entrySet().isEmpty()) {
            this.gameData = new GameData();
        } else {
            this.gameData = UHC.getGson().fromJson(jsonConfig.getJsonObject(), GameData.class);
        }
    }

    private String timeConvert(int t) {
        return GameTimer.getTimeString(t);
    }

    public int getChapter(int time) {
        return BigDecimal.valueOf((time / 1500) + 1).setScale(1, RoundingMode.DOWN).intValue();
    }

    public List<Team> getTeam(UUID teamMember) {
        var teams = gameData.getTeams();

        return teams.stream().filter(team -> team.getMembers().contains(teamMember)).collect(Collectors.toList());
    }

    private String formatString(String... message) {
        StringBuilder builder = new StringBuilder();
        for (String s : message) builder.append(s).append("\n");
        return builder.toString();
    }

    private String getHealthColor(int health) {
        if (health >= 18) {
            return "#70E370";
        } else if (health >= 16) {
            return "#8CE370";
        } else if (health >= 14) {
            return "#A3E370";
        } else if (health >= 12) {
            return "#DAE370";
        } else if (health >= 10) {
            return "#E3D270";
        } else if (health >= 8) {
            return "#E3BB70";
        } else if (health >= 6) {
            return "#E3A170";
        } else if (health >= 4) {
            return "#E38770";
        } else if (health >= 2) {
            return "#E37070";
        }
        return "#F05555";
    }
}
