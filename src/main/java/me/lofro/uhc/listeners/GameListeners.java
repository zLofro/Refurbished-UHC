package me.lofro.uhc.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.infinitepotion.InfinitePotionEffect;
import me.lofro.uhc.api.item.ItemBuilder;
import me.lofro.uhc.api.text.ChatColorFormatter;
import me.lofro.uhc.api.text.HexFormatter;
import me.lofro.uhc.data.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.*;
import org.bukkit.craftbukkit.v1_19_R3.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameListeners implements Listener {

    @EventHandler
    private void onRegenerate(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            switch(event.getRegainReason()) {
                case EATING,SATIATED -> event.setCancelled(true);
                default -> {}
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            var block = event.getClickedBlock();
            if (block != null && block.getType().equals(Material.CHEST)) {
                Chest chest = (Chest) block.getState();
                var lootTable = chest.getLootTable();

                if (lootTable != null) {
                    if (chest.hasLootTable() && !chest.hasBeenFilled()) {
                        var loot = lootTable.populateLoot(new Random(), new LootContext.Builder(chest.getLocation()).luck(getLuckLevel(player)).build());

                        loot.forEach(itemStack -> {
                            var type = itemStack.getType();
                            if (type.equals(Material.POTION) || type.equals(Material.SPLASH_POTION)) {
                                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                                if (potionMeta.getBasePotionData().getType().equals(PotionType.REGEN)) itemStack.setType(Material.GOLDEN_APPLE);
                            }
                        });

                        if (lootTable.getKey().equals(LootTables.PILLAGER_OUTPOST.getKey()) && ThreadLocalRandom.current().nextBoolean()) {
                            loot.add(new ItemStack(Material.DIAMOND, ThreadLocalRandom.current().nextInt(1, 3)));
                        }

                        chest.getInventory().clear();

                        loot.forEach(itemStack -> chest.getInventory().addItem(itemStack));
                    }
                }
            }
        }
    }

    @EventHandler
    private void onEntityDropItem(EntityDeathEvent event) {
        if (event.getEntity() instanceof Ghast) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.GUNPOWDER, ThreadLocalRandom.current().nextInt(0, 3)));
            if (ThreadLocalRandom.current().nextBoolean()) event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 1));
        }
    }

    @EventHandler
    private void onItemConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        if (Objects.equals(new ItemBuilder(event.getItem()).getID(), "golden_head") || Objects.equals(new ItemBuilder(event.getItem()).getID(), "golden_skull")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 220, 1));

            event.setCancelled(true);

            player.getActiveItem().setAmount(player.getActiveItem().getAmount() - 1);
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    private void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getPlayer();

        Block skullBlock = player.getLocation().clone().add(0, 1, 0).getBlock();
        skullBlock.setType(Material.PLAYER_HEAD);
        BlockState state = skullBlock.getState();
        Skull skull = (Skull) state;
        UUID uuid = player.getUniqueId();
        skull.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(uuid));
        skull.setRotation(getDirection(player));
        skull.update();

        player.getLocation().clone().getBlock().setType(Material.NETHER_BRICK_FENCE);
        player.getLocation().clone().add(0, -1, 0).getBlock().setType(Material.GOLD_BLOCK);

        player.setGameMode(GameMode.SPECTATOR);

        var gameManager = UHC.getInstance().getGameManager();
        var playerTeamList = gameManager.getTeam(player.getUniqueId());

        if (!playerTeamList.isEmpty()) {
            var playerTeam = playerTeamList.get(0);
            playerTeam.getMembers().remove(player.getUniqueId());
            var teamName = playerTeam.getName();
            if (playerTeam.getMembers().isEmpty()) {
                gameManager.getGameData().getTeams().remove(playerTeam);
                if (teamName != null) {
                    Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador " + player.getName() + " del team " + teamName +" &cha muerto. Su team ha sido eliminado.")));
                } else {
                    Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador " + player.getName() + " ha muerto. Su team ha sido eliminado.")));
                }
                return;
            } else {
                if (teamName != null) {
                    Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador " + player.getName() + " del team " + teamName +" &cha muerto.")));
                    return;
                }
            }
        }

        Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador " + player.getName() + " ha muerto.")));
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var gameManager = UHC.getInstance().getGameManager();
        if (gameManager.isInGame()) {
            gameManager.showScoreboard(player);
            if (gameManager.getChapter(gameManager.getGameData().getTime()) >= 9) {
                InfinitePotionEffect.create(player, PotionEffectType.DAMAGE_RESISTANCE, 0);

                var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(maxHealth.getBaseValue() + 20);
            }
        }
    }

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();

        var gameManager = UHC.getInstance().getGameManager();
        var teamList = gameManager.getTeam(player.getUniqueId());
        var messageString = HexFormatter.deserialize(event.message());

        if ((messageString.charAt(0) == '!') || teamList.isEmpty()) {
            var realMessageString = messageString.substring(1);
            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(HexFormatter.hexFormat("#FF5555[GLOBAL] #AAAAAA" + player.getName() + " #FFFFFF>> " + realMessageString)));
            event.setCancelled(true);
        } else {
            var teamMembers = teamList.get(0).getMembers();

            teamMembers.forEach(memberUUID -> {
                var member = Bukkit.getPlayer(memberUUID);
                if (member != null) member.sendMessage(HexFormatter.hexFormat("#55FF55[TEAM] #AAAAAA" + player.getName() + " #FFFFFF>> " + HexFormatter.deserialize(event.message())));
            });
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        if (!(event.hasChangedPosition() || event.hasChangedBlock()) || !player.getGameMode().equals(GameMode.SURVIVAL) || player.isDead()) return;
        var gameManager = UHC.getInstance().getGameManager();

        if (gameManager.isInGame()) {
            var playersNearby = player.getNearbyEntities(40, 40, 40);

            playersNearby = playersNearby.stream().filter(entity -> entity instanceof Player p && p.getGameMode().equals(GameMode.SURVIVAL) && !p.isDead()).collect(Collectors.toList());

            var teams = gameManager.getGameData().getTeams();

            playersNearby.forEach(nearbyPlayer -> {
                if (teams.stream().noneMatch(team -> team.getMembers().contains(nearbyPlayer.getUniqueId()))) { // NEARBY PLAYER DOES NOT HAVE TEAM
                    if (teams.stream().noneMatch(team -> team.getMembers().contains(player.getUniqueId()))) { // EVENT PLAYER DOES NOT HAVE TEAM
                        List<UUID> newTeamMembers = new ArrayList<>(Arrays.asList(nearbyPlayer.getUniqueId(), player.getUniqueId()));
                        var newTeam = new Team(newTeamMembers);
                        gameManager.getGameData().getTeams().add(newTeam);

                        nearbyPlayer.sendMessage(ChatColorFormatter.stringWithPrefix("&eSe ha creado un nuevo team al que te has unido junto con el jugador " + player.getName() + ". ⚠ Deben de ponerle un nombre con el comando /team rename <Nombre>"));
                        player.sendMessage(ChatColorFormatter.stringWithPrefix("&eSe ha creado un nuevo team al que te has unido junto con el jugador " + nearbyPlayer.getName() + ". ⚠ Deben de ponerle un nombre con el comando /team rename <Nombre>"));

                        Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&aSe ha creado un nuevo team.")));
                    } else { // EVENT PLAYER HAS TEAM
                        var playerTeam = gameManager.getTeam(player.getUniqueId()).get(0);
                        if (playerTeam.getMembers().size() >= 2) return;
                        playerTeam.getMembers().add(nearbyPlayer.getUniqueId());

                        playerTeam.getMembers().forEach(member -> {
                            var memberPlayer = Bukkit.getPlayer(member);
                            if (memberPlayer == null) return;
                            memberPlayer.sendMessage(ChatColorFormatter.stringWithPrefix("&aEl jugador " + nearbyPlayer.getName() + " se ha unido al team."));
                        });

                        var playerTeamName = playerTeam.getName();
                        StringBuilder text;
                        if (playerTeamName == null) {
                            text = new StringBuilder("&aTe has unido a un team sin nombre cuyos miembros son: ");
                            for (UUID member : playerTeam.getMembers()) {
                                text.append(Bukkit.getOfflinePlayer(member).getName()).append(" ");
                            }
                        } else {
                            text = new StringBuilder("&aTe has unido al team " + playerTeamName + " cuyos miembros son: ");
                            for (UUID member : playerTeam.getMembers()) {
                                text.append(Bukkit.getOfflinePlayer(member).getName()).append(" ");
                            }
                        }
                        text.deleteCharAt(text.chars().toArray().length - 1);
                        text.append(".");
                        nearbyPlayer.sendMessage(ChatColorFormatter.stringWithPrefix(text.toString()));

                        Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.componentWithPrefix("&aUn jugador se ha unido a un team.")));
                    }
                } else { // NEARBY PLAYER HAS TEAM
                    if (teams.stream().noneMatch(team -> team.getMembers().contains(player.getUniqueId()))) { // EVENT PLAYER DOES NOT HAVE TEAM
                        var nearbyPlayerTeam = gameManager.getTeam(nearbyPlayer.getUniqueId()).get(0);
                        if (nearbyPlayerTeam.getMembers().size() >= 2) return;
                        nearbyPlayerTeam.getMembers().add(player.getUniqueId());

                        nearbyPlayerTeam.getMembers().forEach(member -> {
                            var memberPlayer = Bukkit.getPlayer(member);
                            if (memberPlayer == null) return;
                            memberPlayer.sendMessage(ChatColorFormatter.stringWithPrefix("&aEl jugador " + player.getName() + " se ha unido al team."));
                        });

                        var nearbyPlayerTeamName = nearbyPlayerTeam.getName();
                        if (nearbyPlayerTeamName == null) {
                            StringBuilder text = new StringBuilder("&aTe has unido a un team sin nombre cuyos miembros son: ");
                            for (UUID member : nearbyPlayerTeam.getMembers()) {
                                text.append(Bukkit.getOfflinePlayer(member).getName()).append(" ");
                            }
                            text.deleteCharAt(text.chars().toArray().length - 1);
                            text.append(".");
                            player.sendMessage(ChatColorFormatter.stringWithPrefix(text.toString()));
                        } else {
                            StringBuilder text = new StringBuilder("&aTe has unido al team " + nearbyPlayerTeamName + " cuyos miembros son: ");
                            for (UUID member : nearbyPlayerTeam.getMembers()) {
                                text.append(Bukkit.getOfflinePlayer(member).getName()).append(" ");
                            }
                            text.deleteCharAt(text.chars().toArray().length - 1);
                            text.append(".");
                            player.sendMessage(ChatColorFormatter.stringWithPrefix(text.toString()));

                            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.componentWithPrefix("&aUn jugador se ha unido a un team.")));
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    private void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        var advancement = ((CraftAdvancement)event.getAdvancement()).getHandle();

        boolean announceToChat = advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat();

        var serverPlayer = ((CraftPlayer)event.getPlayer()).getHandle();
        var displayName = serverPlayer.getDisplayName();
        var modifiedDisplayName = displayName.plainCopy().setStyle(displayName.getStyle().withObfuscated(true));

        net.kyori.adventure.text.Component message = announceToChat ? io.papermc.paper.adventure.PaperAdventure.asAdventure(net.minecraft.network.chat.Component.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getName(), modifiedDisplayName, advancement.getChatComponent())) : null;

        event.message(message);
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var gameManager = UHC.getInstance().getGameManager();
        if (gameManager.isInGame()) {
            gameManager.hideScoreBoard(player);
            if (gameManager.getChapter(gameManager.getGameData().getTime()) >= 9) {
                InfinitePotionEffect.remove(player, PotionEffectType.DAMAGE_RESISTANCE);

                var maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(20);
            }
        }
    }

    private int getLuckLevel(LivingEntity entity) {
        var luckPotionEffect = entity.getPotionEffect(PotionEffectType.LUCK);
        if (luckPotionEffect == null) return 0;
        return luckPotionEffect.getAmplifier();
    }

    private BlockFace getDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
            if (0 <= rotation && rotation < 22.5) {
                return BlockFace.NORTH;
            }
            if (22.5 <= rotation && rotation < 67.5) {
                return BlockFace.NORTH_EAST;
            }
            if (67.5 <= rotation && rotation < 112.5) {
                return BlockFace.EAST;
            }
            if (112.5 <= rotation && rotation < 157.5) {
                return BlockFace.SOUTH_EAST;
            }
            if (157.5 <= rotation && rotation < 202.5) {
                return BlockFace.SOUTH;
            }
            if (202.5 <= rotation && rotation < 247.5) {
                return BlockFace.SOUTH_WEST;
            }
            if (247.5 <= rotation && rotation < 292.5) {
                return BlockFace.WEST;
            }
            if (292.5 <= rotation && rotation < 337.5) {
                return BlockFace.NORTH_EAST;
            }
            if (337.5 <= rotation && rotation < 359) {
                return BlockFace.NORTH;
            }
        }
        return BlockFace.NORTH;
    }

}
