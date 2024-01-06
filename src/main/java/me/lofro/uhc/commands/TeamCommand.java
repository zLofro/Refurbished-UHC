package me.lofro.uhc.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Subcommand;
import me.lofro.uhc.UHC;
import me.lofro.uhc.api.text.ChatColorFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("uhcTeam")
public class TeamCommand extends BaseCommand {

    @Subcommand("rename")
    @CommandCompletion("teamName")
    private void renameTeam(CommandSender sender, String teamName) {
        if (sender instanceof Player player) {
            var gameManager = UHC.getInstance().getGameManager();
            var playerTeam = gameManager.getTeam(player.getUniqueId());
            if (playerTeam.isEmpty()) {
                player.sendMessage(ChatColorFormatter.component("&cNo estás en ningun team."));
                return;
            }

            playerTeam.get(0).setName(teamName);
            Bukkit.getOnlinePlayers().forEach(online -> online.sendMessage(ChatColorFormatter.stringWithPrefix("&aSe ha cambiado el nombre de un team a " + teamName + "&a.")));
        } else {
            sender.sendMessage(ChatColorFormatter.component("&cDebes de ser un jugador para ejecutar ese comando."));
        }
    }

}
