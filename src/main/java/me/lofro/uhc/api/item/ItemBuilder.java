package me.lofro.uhc.api.item;

import me.lofro.uhc.api.data.DataContainer;
import me.lofro.uhc.api.nbt.NBTEditor;
import me.lofro.uhc.api.text.HexFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemBuilder {

    protected ItemStack is;

    protected ItemMeta im;

    public ItemBuilder() {
        this(Material.AIR);
    }

    public static ItemBuilder nullItem() { return new ItemBuilder((ItemStack) null); }

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack itemStack) {
        this.is = itemStack;
    }

    public ItemBuilder setAmount(int amount) {
        this.is.setAmount(amount);
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        this.im = this.is.getItemMeta();
        this.im.setCustomModelData(data);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setID(String id) {
        this.im = this.is.getItemMeta();
        DataContainer.set(im, "id", PersistentDataType.STRING, id);
        this.is.setItemMeta(im);
        return this;
    }

    public <T> ItemBuilder addNBT(String key, T value) {
        is = NBTEditor.set(is, value, key);

        return this;
    }

    public @Nullable String getID() {
        if (this.is == null) return null;
        this.im = this.is.getItemMeta();
        if (this.im == null) return null;
        var id = DataContainer.get(this.im, "id", PersistentDataType.STRING);
        var displayName = this.im.displayName();
        return id != null ? id : displayName != null ? PlainTextComponentSerializer.plainText().serialize(displayName) : is.getType().name().toLowerCase().replace(" ", "_");
    }

    public ItemBuilder setRandomUniqueID() {
        return setUniqueID(UUID.randomUUID());
    }

    public ItemBuilder setUniqueID(UUID uuid) {
        this.im = this.is.getItemMeta();
        DataContainer.set(im, "uuid", PersistentDataType.STRING, uuid.toString());
        this.is.setItemMeta(im);
        return this;
    }

    public @Nullable UUID getUniqueID() {
        if (this.is == null) return null;
        this.im = this.is.getItemMeta();
        if (this.im == null) return null;
        var id = DataContainer.get(this.im, "uuid", PersistentDataType.STRING);
        return id != null ? UUID.fromString(id) : null;
    }

    public boolean hasID() {
        if (this.is == null) return false;
        this.im = this.is.getItemMeta();
        return DataContainer.has(this.im, "id", PersistentDataType.STRING);
    }

    public ItemBuilder setUnbreakable(boolean b) {
        this.im = this.is.getItemMeta();
        this.im.setUnbreakable(b);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setDisplayName(Component component) {
        this.im = this.is.getItemMeta();
        this.im.displayName(component);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        this.im = this.is.getItemMeta();
        if (this.im instanceof EnchantmentStorageMeta) {
            ((EnchantmentStorageMeta) this.im).addStoredEnchant(enchantment, level, true);
        } else {
            this.im.addEnchant(enchantment, level, true);
        }
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addEnchants(Map<Enchantment, Integer> enchantments) {
        this.im = this.is.getItemMeta();
        if (!enchantments.isEmpty())
            for (Enchantment enchantment : enchantments.keySet())
                this.im.addEnchant(enchantment, enchantments.get(enchantment), true);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setColor(Color color) {
        this.im = this.is.getItemMeta();
        if (this.im instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
            this.is.setItemMeta(leatherArmorMeta);
        }
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag... itemFlag) {
        this.im = this.is.getItemMeta();
        this.im.addItemFlags(itemFlag);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setLore(List<Component> lore) {
        this.im = this.is.getItemMeta();
        this.im.lore(lore);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        this.setLore(Arrays.stream(lore).map(HexFormatter::hexFormat).collect(Collectors.toList()));
        return this;
    }

    public <T, Z> ItemBuilder addData(String key, PersistentDataType<T, Z> type, Z value) {
        this.im = this.is.getItemMeta();
        im.getPersistentDataContainer().set(DataContainer.key(key), type, value);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addData(String key, String value) {
        this.addData(key, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder addData(String key, byte value) {
        this.addData(key, PersistentDataType.BYTE, value);
        return this;
    }

    public ItemBuilder addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        this.im = this.is.getItemMeta();
        this.im.addAttributeModifier(attribute, attributeModifier);
        this.is.setItemMeta(this.im);
        return this;
    }

    public void setItemMeta(ItemMeta im) {
        this.is.setItemMeta(im);
    }

    public ItemBuilder addAttributeModifier(Attribute attribute, double value, AttributeModifier.Operation operation, EquipmentSlot slot) {
        this.addAttributeModifier(attribute, new AttributeModifier(UUID.randomUUID(), attribute.getKey().getKey(),value, operation, slot));
        return this;
    }

    public ItemStack build() {
        return this.is;
    }
}
