package me.lofro.uhc.managers;

import me.lofro.uhc.UHC;
import me.lofro.uhc.api.item.ItemBuilder;
import me.lofro.uhc.api.text.HexFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipesManager {

    public static void createCustomRecipes() {
        createGoldenCarrotRecipe();
        createGlisteringMelonRecipe();
        createGoldenHeadRecipe();
        createGoldenSkullRecipe();
    }

    private static void createGoldenCarrotRecipe() {
        NamespacedKey goldenCarrotKey = new NamespacedKey(UHC.getInstance(), "custom_golden_carrot");

        ShapedRecipe goldenCarrotRecipe = new ShapedRecipe(goldenCarrotKey, new ItemStack(Material.GOLDEN_CARROT));

        goldenCarrotRecipe.shape("GGG", "GCG", "GGG");
        goldenCarrotRecipe.setIngredient('G', Material.GOLD_INGOT);
        goldenCarrotRecipe.setIngredient('C', Material.CARROT);

        Bukkit.addRecipe(goldenCarrotRecipe);
    }

    private static void createGlisteringMelonRecipe() {
        NamespacedKey glisteringMelonKey = new NamespacedKey(UHC.getInstance(), "custom_glistering_melon");

        ShapelessRecipe glisteringMelonRecipe = new ShapelessRecipe(glisteringMelonKey, new ItemStack(Material.GLISTERING_MELON_SLICE));

        glisteringMelonRecipe.addIngredient(Material.GOLD_BLOCK);
        glisteringMelonRecipe.addIngredient(Material.MELON_SLICE);

        Bukkit.addRecipe(glisteringMelonRecipe);
    }

    private static void createGoldenHeadRecipe() {
        NamespacedKey goldenHeadKey = new NamespacedKey(UHC.getInstance(), "custom_golden_head");

        var goldenHead = new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).setDisplayName(HexFormatter.hexFormat("&6Cabeza de Oro")).setID("golden_head").build();

        ShapedRecipe goldenHeadRecipe = new ShapedRecipe(goldenHeadKey, goldenHead);

        goldenHeadRecipe.shape("GGG", "GHG", "GGG");
        goldenHeadRecipe.setIngredient('G', Material.GOLD_INGOT);
        goldenHeadRecipe.setIngredient('H', Material.PLAYER_HEAD);

        Bukkit.addRecipe(goldenHeadRecipe);
    }

    private static void createGoldenSkullRecipe() {
        NamespacedKey goldenHeadKey = new NamespacedKey(UHC.getInstance(), "custom_golden_skull");

        var goldenHead = new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).setDisplayName(HexFormatter.hexFormat("&6Calavera de Oro")).setID("golden_skull").build();

        ShapedRecipe goldenHeadRecipe = new ShapedRecipe(goldenHeadKey, goldenHead);

        goldenHeadRecipe.shape("GGG", "GHG", "GGG");
        goldenHeadRecipe.setIngredient('G', Material.GOLD_INGOT);
        goldenHeadRecipe.setIngredient('H', Material.WITHER_SKELETON_SKULL);

        Bukkit.addRecipe(goldenHeadRecipe);
    }

}
