package me.lofro.uhc.api;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;

public class CommandUtils {

    public static void registerCommands(PaperCommandManager manager, BaseCommand... commandExecutors) {
        for (BaseCommand commandExecutor : commandExecutors) {
            manager.registerCommand(commandExecutor);
        }
    }

}
