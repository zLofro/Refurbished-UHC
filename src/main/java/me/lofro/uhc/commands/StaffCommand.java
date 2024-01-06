package me.lofro.uhc.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.text.ChatColorFormatter;
import me.lofro.uhc.data.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@CommandPermission("admin.perm")
@CommandAlias("uhcStaff")
public class StaffCommand extends BaseCommand {

    @Subcommand("createTeam")
    @CommandCompletion("teamName")
    private void createTeam(CommandSender sender, String teamName) {
        if (sender instanceof Player player) {
            var gameManager = UHC.getInstance().getGameManager();
            var teams = gameManager.getGameData().getTeams();

            var newTeam = new Team(teamName, new ArrayList<>(List.of(player.getUniqueId())));

            teams.add(newTeam);

            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&aSe ha creado un nuevo team por la administración llamado " + teamName + " con el jugador " + player.getName() + ".")));
        } else {
            sender.sendMessage(ChatColorFormatter.string("&cDebes de ser un jugador para ejecutar este comando."));
        }
    }

    @Subcommand("removeTeamByName")
    @CommandCompletion("teamName")
    private void removeTeam(CommandSender sender, String teamName) {
        if (sender instanceof Player) {
            var gameManager = UHC.getInstance().getGameManager();
            var teams = gameManager.getGameData().getTeams();

            AtomicReference<Team> selectedTeam = new AtomicReference<>(null);

            teams.forEach(team -> {
                if (team.getName().equals(teamName)) selectedTeam.set(team);
            });

            var selectedValue = selectedTeam.get();
            if (selectedValue == null) {
                sender.sendMessage(ChatColorFormatter.string("&cEl team con ese nombre no existe."));
                return;
            }

            teams.remove(selectedValue);
            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl team " + selectedValue.getName() + " ha sido eliminado por la administración.")));
        } else {
            sender.sendMessage(ChatColorFormatter.string("&cDebes de ser un jugador para ejecutar este comando."));
        }
    }

    @Subcommand("addPlayerToTeam")
    @CommandCompletion("teamName")
    private void addPlayerToTeam(CommandSender sender, String teamName, @Flags("other") Player playerToAdd) {
        if (sender instanceof Player) {
            var gameManager = UHC.getInstance().getGameManager();
            var teams = gameManager.getGameData().getTeams();

            var teamList = teams.stream().filter(teamFilter -> teamName.equals(teamFilter.getName())).toList();

            if (teamList.isEmpty()) {
                sender.sendMessage(ChatColorFormatter.string("&cNo existe un team con ese nombre."));
                return;
            }

            var team = teamList.get(0);

            if (team != null) {
                if (!team.getMembers().contains(playerToAdd.getUniqueId())) {
                    team.getMembers().add(playerToAdd.getUniqueId());
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColorFormatter.stringWithPrefix("&aLa administración ha añadido al team " + teamName + " al jugador " + playerToAdd.getName() + ".")));
                } else {
                    sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador no pertenece a ese team."));
                }
            } else {
                sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cNo existe un team con ese nombre."));
            }
        } else {
            sender.sendMessage(ChatColorFormatter.string("&cDebes de ser un jugador para ejecutar este comando."));
        }
    }

    @Subcommand("removePlayerFromTeam")
    @CommandCompletion("teamName")
    private void removePlayerFromTeam(CommandSender sender, String teamName, @Flags("other") Player playerToRemove) {
        if (sender instanceof Player) {
            var gameManager = UHC.getInstance().getGameManager();
            var teams = gameManager.getGameData().getTeams();

            var teamList = teams.stream().filter(teamFilter -> teamName.equals(teamFilter.getName())).toList();

            if (teamList.isEmpty()) {
                sender.sendMessage(ChatColorFormatter.string("&cNo existe un team con ese nombre."));
                return;
            }

            var team = teamList.get(0);

            if (team != null) {
                if (team.getMembers().contains(playerToRemove.getUniqueId())) {
                    team.getMembers().remove(playerToRemove.getUniqueId());
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColorFormatter.stringWithPrefix("&cLa administración ha eliminado del team " + teamName + " al jugador " + playerToRemove.getName() + ".")));
                } else {
                    sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cEl jugador no pertenece a ese team."));
                }
            } else {
                sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cNo existe un team con ese nombre."));
            }
        } else {
            sender.sendMessage(ChatColorFormatter.string("&cDebes de ser un jugador para ejecutar este comando."));
        }
    }

    @Subcommand("setTime")
    @CommandCompletion("time")
    private void setTime(CommandSender sender, int time) {
        var gameManager = UHC.getInstance().getGameManager();

        gameManager.getGameData().setTime(time);
        sender.sendMessage(ChatColorFormatter.stringWithPrefix("&aHas actualizado el tiempo de la partida a " + time + " segundos."));

        Bukkit.getOnlinePlayers().forEach(online -> gameManager.resetEffects(online, true));
    }

    @Subcommand("startGame")
    private void startGame(CommandSender sender) {
        var gameManager = UHC.getInstance().getGameManager();

        if (gameManager.getGameData().isInGame()) {
            sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cYa esta el juego iniciado."));
            return;
        }

        sender.sendMessage(ChatColorFormatter.stringWithPrefix("&aHas iniciado la partida."));

        gameManager.startGame();
    }

    @Subcommand("stopGame")
    private void stopGame(CommandSender sender) {
        var gameManager = UHC.getInstance().getGameManager();

        if (!gameManager.getGameData().isInGame()) {
            sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cNo esta el juego iniciado."));
            return;
        }

        gameManager.stopGame(false);

        sender.sendMessage(ChatColorFormatter.stringWithPrefix("&aHas parado la partida."));
    }

    @Subcommand("resetGame")
    private void resetGame(CommandSender sender) {
        var gameManager = UHC.getInstance().getGameManager();

        if (!gameManager.getGameData().isInGame()) {
            sender.sendMessage(ChatColorFormatter.stringWithPrefix("&cNo esta el juego iniciado."));
            return;
        }

        gameManager.endGame();

        sender.sendMessage(ChatColorFormatter.stringWithPrefix("&aHas reseteado la partida."));
    }

}
