package me.lofro.uhc.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.text.ChatColorFormatter;
import me.lofro.uhc.data.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&cSe ha creado un nuevo team por la administración llamado " + teamName + " con el jugador " + player.getName() + ".")));
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

    @Subcommand("removeTeamByPlayer")
    private void removeTeam(CommandSender sender, @Flags("other") Player playerFromTeam) {

    }

    @Subcommand("addPlayerToTeam")
    @CommandCompletion("teamName ")
    private void addPlayerToTeam(CommandSender sender, String teamName, @Flags("other") Player playerToAdd) {

    }

    @Subcommand("addPlayerToTeamByPlayer")
    private void addPlayerToTeam(CommandSender sender, @Flags("other") Player playerFromTeam, @Flags("other") Player playerToAdd) {

    }

    @Subcommand("removePlayerFromTeam")
    @CommandCompletion("teamName ")
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

    @Subcommand("removePlayerFromTeamByPlayer")
    private void removePlayerFromTeam(CommandSender sender, @Flags("other") Player playerFromTeam, @Flags("other") Player playerToRemove) {

    }

}
