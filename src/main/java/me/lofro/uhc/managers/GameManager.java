package me.lofro.uhc.managers;

import lombok.Getter;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.ListenerUtils;
import me.lofro.uhc.api.data.JsonConfig;
import me.lofro.uhc.api.data.interfaces.Restorable;
import me.lofro.uhc.api.infinitepotion.InfinitePotionEffect;
import me.lofro.uhc.api.item.ItemBuilder;
import me.lofro.uhc.api.text.ChatColorFormatter;
import me.lofro.uhc.api.text.HexFormatter;
import me.lofro.uhc.api.timer.GameTimer;
import me.lofro.uhc.data.GameData;
import me.lofro.uhc.data.Team;
import me.lofro.uhc.listeners.GameListeners;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameManager implements Restorable {

    private final UHC uhc;

    private final @Getter GameListeners gameListener;

    private @Getter GameData gameData;

    private final Scoreboard scoreboard;

    private final Objective objective;

    private int taskID;

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

        this.restore(UHC.getInstance().getDataManager().getGameDataJson());
    }

    public void startGame() {
        gameData.setInScatter(false);
        if (!gameData.isInGame()) {
            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&aHa dado comienzo la partida.")));
        }

        this.gameData.setInGame(true);

        var isOnLastSize = Bukkit.getWorlds().get(0).getWorldBorder().getSize() <= 400;

        if (!isOnLastSize) Bukkit.getWorlds().get(0).getWorldBorder().setSize(4002);
        if (getChapter(gameData.getTime()) >= 9 && !isOnLastSize) {
            Bukkit.getWorlds().get(0).getWorldBorder().setSize(400, TimeUnit.MINUTES, 10);
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl borde se cierra. Tienen 5 minutos para llegar a 200x200.")));
        }

        ListenerUtils.registerListener(gameListener);

        Bukkit.getOnlinePlayers().forEach(player -> {
            showScoreboard(player);
            resetEffects(player, true);
        });

        this.taskID = Bukkit.getScheduler().runTaskTimer(uhc, () -> {
            this.scores.forEach(score -> scoreboard.resetScores(score.getEntry()));

            var newTime = gameData.getTime() + 1;

            gameData.setTime(newTime);

            var chapter = getChapter(newTime);
            var newChapterTime = (chapter != 1) ? (((newTime / 1500F) + 1) - chapter) * 1500 : newTime;

            if ((double) (chapter + 1) == getUnRoundedChapter(gameData.getTime() + 6)) {
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55Cambio de episodio en 5"))), 20);
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55Cambio de episodio en 4"))), 40);
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55Cambio de episodio en 3"))), 60);
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55Cambio de episodio en 2"))), 80);
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55Cambio de episodio en 1"))), 100);
                Bukkit.getScheduler().runTaskLater(UHC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormatWithPrefix("#FFFF55¡Cambio de episodio!"))), 120);
            }

            if (chapter > getChapter(gameData.getTime() - 1)) {
                Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&7Ha dado comienzo el episodio " + chapter + ".")));
                switch (chapter) {
                    case 9 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.sendMessage(ChatColorFormatter.stringWithPrefix("&a¡Todos los jugadores han obtenido mejoras!"));

                            resetEffects(player, true);

                            player.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl borde se cierra. Tienen 5 minutos para llegar a 200x200."));
                        });
                        if (!(Bukkit.getWorlds().get(0).getWorldBorder().getSize() <= 400)) Bukkit.getWorlds().get(0).getWorldBorder().setSize(400, TimeUnit.MINUTES, 10);
                    }
                    case 4 -> Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cSe ha activado el PVP.")));
                    case 3 -> Bukkit.getOnlinePlayers().forEach(online -> {
                        online.sendMessage(ChatColorFormatter.stringWithPrefix("&a¡Se ha dado una brújula que localiza al jugador más cercano a todos los jugadores!"));
                        var compass = new ItemBuilder(Material.COMPASS).setID("player_finder").setDisplayName(HexFormatter.hexFormat("#55FF55Brujula de jugadores."));

                        online.getInventory().addItem(compass.build());
                    });
                    case 10 -> Bukkit.getOnlinePlayers().forEach(player -> {
                        var aliveTeams = gameData.getTeams();
                        if (!aliveTeams.isEmpty()) {
                            StringBuilder text = new StringBuilder("&a¡Los equipos ");
                            aliveTeams.forEach(team -> text.append(team.getName()).append(" "));
                            text.append("&ahan quedado en empate!");
                            player.sendMessage(ChatColorFormatter.stringWithPrefix(text.toString()));
                            player.showTitle(Title.title(HexFormatter.hexFormat("#FFFF55¡Empate!"), HexFormatter.hexFormat("")));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
                            endGame();
                        }
                    });
                }
            }

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
            String footer = formatString("", "#FFFFFF  #FFAA00☀ #E8B465Episodio actual: #EDC588" + chapter);

            Bukkit.getOnlinePlayers().forEach(online -> {
                var nearestPlayer = getNearestPlayer(online, 5000D);
                if (nearestPlayer != null) {
                    online.setCompassTarget(nearestPlayer.getLocation());
                } else {
                    online.setCompassTarget(online.getWorld().getSpawnLocation());
                }

                var tabPlayer = TabAPI.getInstance().getPlayer(online.getUniqueId());
                if (!(tabPlayer != null && headerFooterManager != null && tablistFormatManager != null)) return;
                headerFooterManager.setHeader(tabPlayer, topper);
                headerFooterManager.setFooter(tabPlayer, footer);

                var absorption = online.getPotionEffect(PotionEffectType.ABSORPTION);
                var health = absorption != null ? BigDecimal.valueOf((online.getHealth() + absorption.getAmplifier() + 4) / 2).setScale(1, RoundingMode.HALF_EVEN).doubleValue() : BigDecimal.valueOf(online.getHealth() / 2).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                tablistFormatManager.setSuffix(tabPlayer, " " + getHealthColor(health) + health + "#EF2A2A❤");

                var teamList = getTeam(online.getUniqueId());

                if (teamList != null && !teamList.isEmpty()) {
                    var team = teamList.get(0);

                    StringBuilder text = new StringBuilder("&aTeam: " + team.getName() + "&a. Miembros: ");

                    if (!team.getMembers().isEmpty()) {
                        team.getMembers().forEach(member -> {
                            var player = Bukkit.getPlayer(member);
                            if (player != null) text.append(player.getName()).append(" ");
                        });
                    }

                    online.sendActionBar(ChatColorFormatter.string(text.toString()));
                }
            });

        }, 0, 20).getTaskId();
    }

    public void stopGame(boolean isInGame) {
        if (!isInGame) this.gameData.setInGame(false);
        Bukkit.getScheduler().cancelTask(this.taskID);

        Bukkit.getOnlinePlayers().forEach(player -> {
            hideScoreBoard(player);
            resetEffects(player, false);
        });

        ListenerUtils.unregisterListener(gameListener);

        UHC.getInstance().getDataManager().save();
    }

    public void endGame() {
        this.gameData.setInGame(false);
        Bukkit.getScheduler().cancelTask(this.taskID);

        Bukkit.getOnlinePlayers().forEach(player -> {
            hideScoreBoard(player);
            resetEffects(player, false);
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

    public double getUnRoundedChapter(int time) {
        return (time / 1500D) + 1;
    }

    public int getChapter(int time) {
        return BigDecimal.valueOf(getUnRoundedChapter(time)).setScale(1, RoundingMode.DOWN).intValue();
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

    private String getHealthColor(double health) {
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

    private @Nullable Player getNearestPlayer(Player p, Double range) {
        double maxDistance = Double.POSITIVE_INFINITY;
        Player target = null;
        for (Player e : p.getLocation().getNearbyEntitiesByType(Player.class, range, range, range).stream().filter(player -> {
            var playerTeamList = getTeam(p.getUniqueId());

            return playerTeamList.isEmpty() || !playerTeamList.get(0).getMembers().contains(player.getUniqueId());
        }).toList()) {
            if (e == p) continue;
            double distanceTo = p.getLocation().distance(e.getLocation());
            if (distanceTo > maxDistance)
                continue;
            maxDistance = distanceTo;
            target = e;
        }
        return target;
    }

    public void resetEffects(Player player, boolean isInGame) {
        if (isInGame) {
            if (getChapter(gameData.getTime()) >= 9) {
                InfinitePotionEffect.create(player, PotionEffectType.DAMAGE_RESISTANCE, 0);

                var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(40);
            } else {
                InfinitePotionEffect.remove(player, PotionEffectType.DAMAGE_RESISTANCE);
                var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(20);
            }
        } else {
            InfinitePotionEffect.remove(player, PotionEffectType.DAMAGE_RESISTANCE);
            var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) maxHealth.setBaseValue(20);
        }
    }

    public void teleportToRandomLocations() {
        List<Location> teleportLocations = new ArrayList<>();
        var world = Bukkit.getWorlds().get(0);
        teleportLocations.add(new Location(world, -2000.5, world.getMaxHeight(), 2000.5));
        teleportLocations.add(new Location(world, -666.5, world.getMaxHeight(), 2000.5));
        teleportLocations.add(new Location(world, 667.5, world.getMaxHeight(), 2000.5));
        teleportLocations.add(new Location(world, 2000.5, world.getMaxHeight(), 2000.5));
        teleportLocations.add(new Location(world, -2000.5, world.getMaxHeight(), 667.5));
        teleportLocations.add(new Location(world, -2000.5, world.getMaxHeight(), -667.5));
        teleportLocations.add(new Location(world, -2000.5, world.getMaxHeight(), -2000.5));
        teleportLocations.add(new Location(world, -667.5, world.getMaxHeight(), -2000.5));
        teleportLocations.add(new Location(world, 667.5, world.getMaxHeight(), 2000.5));
        teleportLocations.add(new Location(world, 2000.5, world.getMaxHeight(), -2000.5));
        teleportLocations.add(new Location(world, 2000.5, world.getMaxHeight(), -667.5));
        teleportLocations.add(new Location(world, 2000.5, world.getMaxHeight(), 667.5));

        var survivalPlayers = Bukkit.getOnlinePlayers().stream().filter(player -> player.getGameMode().equals(GameMode.SURVIVAL)).toList();

        for (int i = 0; i < survivalPlayers.size(); i++) {
            var player = survivalPlayers.get(i);

            if (survivalPlayers.size() > teleportLocations.size()) continue;

            player.teleport(teleportLocations.get(i));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 2400, 0));
        }
    }

}
