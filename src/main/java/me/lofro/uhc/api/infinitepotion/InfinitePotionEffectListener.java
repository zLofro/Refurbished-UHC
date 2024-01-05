package me.lofro.uhc.api.infinitepotion;

import me.lofro.uhc.api.data.DataContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

public class InfinitePotionEffectListener implements Listener {
    @EventHandler
    private static void onPlayerRemovedPotion(EntityPotionEffectEvent e) {
        switch (e.getAction()) {
            case REMOVED, CLEARED -> {
            }
            default -> {
                return;
            }
        }

        final LivingEntity entity = (LivingEntity) e.getEntity(); // e.getEntity() is ALWAYS an instance of LivingEntity in EntityPotionEffectEvent
        final PersistentDataContainer data = entity.getPersistentDataContainer();
        final PotionEffectType removedPotionType = e.getModifiedType();
        final NamespacedKey key = DataContainer.key(InfinitePotionEffect.dataPrefix + removedPotionType.getName());

        switch (e.getCause()) {
            case PLUGIN, COMMAND -> {
                if (data.has(key, PersistentDataType.INTEGER)) data.remove(key);
                return;
            }
        }

        if (data.has(key, PersistentDataType.INTEGER)) {
            e.setCancelled(true);
            @SuppressWarnings("ConstantConditions") final int savedAmplifier = data.get(key, PersistentDataType.INTEGER);
            //noinspection ConstantConditions
            if (savedAmplifier != e.getOldEffect().getAmplifier()) entity.removePotionEffect(removedPotionType);
            InfinitePotionEffect.create(entity, e.getModifiedType(), savedAmplifier);
        }
    }
}
