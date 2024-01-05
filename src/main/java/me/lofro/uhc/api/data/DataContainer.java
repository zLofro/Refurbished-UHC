package me.lofro.uhc.api.data;

import me.lofro.uhc.UHC;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;

/**
 * Class designed to make the process of working with {@link PersistentDataContainer} easier.
 * @author <a href="https://github.com/zLofro">Lofro</a>.
 */
public class DataContainer {

    public static NamespacedKey key(String value) {
        return new NamespacedKey(UHC.getInstance(), value);
    }

    public static void setEntityID(net.minecraft.world.entity.Entity entity, String ID) {
        var craftLivingEntity = entity.getBukkitEntity();
        set(craftLivingEntity, "custom_entity_ID", PersistentDataType.STRING, ID);
    }

    public static String getEntityID(net.minecraft.world.entity.Entity entity) {
        var craftLivingEntity = entity.getBukkitEntity();
        return get(craftLivingEntity, "custom_entity_ID", PersistentDataType.STRING);
    }

    public static void setEntityID(Entity entity, String ID) {
        set(entity, "custom_entity_ID", PersistentDataType.STRING, ID);
    }

    public static String getEntityID(Entity entity) {
        return get(entity, "custom_entity_ID", PersistentDataType.STRING);
    }

    public static <T> void set(PersistentDataHolder holder, String key, PersistentDataType<T, T> type, T value) {
        if(holder == null)return;
        holder.getPersistentDataContainer().set(DataContainer.key(key), type, value);
    }

    @Nullable
    public static <T> T get(PersistentDataHolder holder, String key, PersistentDataType<T, T> type) {
        if(!DataContainer.has(holder, key, type)) return null;
        return holder.getPersistentDataContainer().get(DataContainer.key(key), type);
    }

    public static <T> boolean has(PersistentDataHolder holder, String key, PersistentDataType<T, T> type) {
        if(holder == null) return false;
        return holder.getPersistentDataContainer().has(DataContainer.key(key), type);
    }

    public static PersistentDataAdapterContext getAdapterContext(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().getAdapterContext();
    }

    public static PersistentDataContainer create(PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
    }

    public static void remove(PersistentDataHolder holder, String key) {
        holder.getPersistentDataContainer().remove(DataContainer.key(key));
    }

}
