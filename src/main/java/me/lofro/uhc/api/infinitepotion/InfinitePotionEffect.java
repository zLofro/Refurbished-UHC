package me.lofro.uhc.api.infinitepotion;

import me.lofro.uhc.api.data.DataContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InfinitePotionEffect {
    static final String dataPrefix = "Infinite_Potion_Effect_of_";

    public static void create(LivingEntity entity, PotionEffectType type, int amplifier, boolean ambient, boolean particles, boolean icon) {
        // El máximo de duración que permite MC mediante comandos es 1_000_000 segundos / 20_000_000 ticks / 11.5+ días reales
        // Poner Integer.MAX no da ningún error a primera vista, pero no cuesta nada poner 20Millones de ticks, no solo porque
        // nadie va a permanecer en el servidor durante más de 11 días sin haber salido ni una vez, sino porque incluso si esa
        // persona lo hiciese, aun así se le volvería a aplicar el efecto en InfinitePlayerPotionListener.java
        if (new PotionEffect(type, 20_000_000, amplifier, ambient, particles, icon).apply(entity)) {
            DataContainer.set(entity, dataPrefix + type.getName(), PersistentDataType.INTEGER, amplifier);
        }
    }

    public static void create(LivingEntity entity, PotionEffectType type, int amplifier, boolean ambient, boolean particles) {
        create(entity, type, amplifier, ambient, particles, particles);
    }

    public static void create(LivingEntity entity, PotionEffectType type, int amplifier, boolean ambient) {
        create(entity, type, amplifier, ambient, true);
    }

    public static void create(LivingEntity entity, PotionEffectType type, int amplifier) {
        create(entity, type, amplifier, true);
    }

    public static void remove(LivingEntity entity, PotionEffectType type) {
        entity.removePotionEffect(type);
        DataContainer.remove(entity, dataPrefix + type.getName());
    }
}
