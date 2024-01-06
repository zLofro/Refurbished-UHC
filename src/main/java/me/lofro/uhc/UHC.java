package me.lofro.uhc;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.lofro.uhc.api.CommandUtils;
import me.lofro.uhc.api.ListenerUtils;
import me.lofro.uhc.api.crafting.CraftingRemover;
import me.lofro.uhc.api.infinitepotion.InfinitePotionEffectListener;
import me.lofro.uhc.commands.StaffCommand;
import me.lofro.uhc.data.DataManager;
import me.lofro.uhc.listeners.GameListeners;
import me.lofro.uhc.managers.GameManager;
import me.lofro.uhc.managers.RecipesManager;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class UHC extends JavaPlugin {

    private static final @Getter Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static @Getter TabAPI tabAPI;

    private static @Getter UHC instance;

    private @Getter GameManager gameManager;

    private @Getter PaperCommandManager paperCommandManager;

    private @Getter DataManager dataManager;

    private @Getter ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;

        tabAPI = TabAPI.getInstance();

        this.protocolManager = ProtocolLibrary.getProtocolManager();

        ListenerUtils.registerListener(new InfinitePotionEffectListener());

        this.paperCommandManager = new PaperCommandManager(this);

        CommandUtils.registerCommands(paperCommandManager, new StaffCommand());

        CraftingRemover.removeRecipeByKey("glistering_melon_slice");
        CraftingRemover.removeRecipeByKey("golden_carrot");

        RecipesManager.createCustomRecipes();

        try {
            this.dataManager = new DataManager(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.gameManager = new GameManager(this, new GameListeners());

        if (gameManager.getGameData().isInGame()) gameManager.startGame();
    }

    @Override
    public void onDisable() {
        this.gameManager.stopGame(gameManager.getGameData().isInGame());
    }

}
