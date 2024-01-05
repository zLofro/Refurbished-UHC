package me.lofro.uhc.api.crafting;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

/**
 * Class designed to remove recipes from Minecraft without affecting secret vanilla recipes.
 * Supports 1.19.4.
 * @author zLofro
 */
public class CraftingRemover {

    /**
     * Removes a vanilla recipe based on its ItemStack identifier (only lower case).
     * @param itemStackIdentifier the ItemStack identifier. For example: "wooden_shovel".
     */
    public static void removeRecipeByKey(String itemStackIdentifier) {
        removeRecipeByResource("minecraft", itemStackIdentifier);
    }

    /**
     * Removes a Minecraft recipe based on its ItemStack identifier (only lower cases) and the .
     * @param instanceName the name of the instance that stores the item. For vanilla items it is "minecraft". (only lower case)
     * @param itemStackIdentifier the ItemStack identifier. For example: "wooden_shovel".
     */
    public static void removeRecipeByResource(String instanceName, String itemStackIdentifier) {
        ResourceLocation resourceLocation = new ResourceLocation(instanceName, itemStackIdentifier);
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>> recipes : ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().recipes.values()) {
            recipes.remove(resourceLocation);
        }
    }

}
