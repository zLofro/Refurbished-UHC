package me.lofro.uhc.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class designed to translate both {@link String} to {@link Component} and {@link Component} to {@link String} and
 * use the {@link ChatColor} format by adding the "&" char and its respective {@link ChatColor} ID before the text you want to
 * apply it to. Also works with HEX color code format.
 * @author <a href="https://github.com/zLofro">Lofro</a>.
 */
public class ChatColorFormatter {

    private static final String name = string("&6&lRefurbished UHC");
    private static final String prefix = string(name + " &r>> ");

    private static String transform(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Translates the given String into another String with ChatColor format.
     * @param text to translate.
     * @return String containing the ChatColor.COLOR_CHAR color code character replaced by '&'.
     */
    public static String string(String text) {
        return transform(text);
    }

    /**
     * Translates the given String into a Component with ChatColor format.
     * @param text to translate.
     * @return Component containing the ChatColor.COLOR_CHAR color code character replaced by '&'.
     */
    public static String string(TextComponent text) {
        return transform(text.content());
    }

    /**
     * Translates the given Component into a String with ChatColor format.
     * @param text to translate.
     * @return String containing the ChatColor.COLOR_CHAR color code character replaced by '&'.
     */
    public static Component component(String text) {
        return Component.text(transform(text));
    }

    /**
     * Translates the given Component into another Component with ChatColor format.
     * @param text to translate.
     * @return Component containing the ChatColor.COLOR_CHAR color code character replaced by '&'.
     */
    public static Component component(TextComponent text) {
        return Component.text(transform(text.content()));
    }

    /**
     * Translates the given String into another String with ChatColor format and the plugin prefix and the plugin prefix.
     * @param text to translate.
     * @return String containing the ChatColor.COLOR_CHAR color code character replaced by '&' with the plugin prefix.
     */
    public static String stringWithPrefix(String text) {
        return transform(prefix + text);
    }

    /**
     * Translates the given String into a Component with ChatColor format and the plugin prefix.
     * @param text to translate.
     * @return Component containing the ChatColor.COLOR_CHAR color code character replaced by '&' with the plugin prefix.
     */
    public static String stringWithPrefix(TextComponent text) {
        return transform(prefix + text.content());
    }

    /**
     * Translates the given Component into a String with ChatColor format and the plugin prefix.
     * @param text to translate.
     * @return String containing the ChatColor.COLOR_CHAR color code character replaced by '&' with the plugin prefix.
     */
    public static Component componentWithPrefix(String text) {
        return Component.text(transform(prefix + text));
    }

    /**
     * Translates the given Component into another Component with ChatColor format and the plugin prefix.
     * @param text to translate.
     * @return Component containing the ChatColor.COLOR_CHAR color code character replaced by '&' with the plugin prefix.
     */
    public static Component componentWithPrefix(TextComponent text) {
        return Component.text(transform(prefix + text.content()));
    }

    public static String hexFormat(String text) {
        text = text.replace("#&", "#");
        var pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            String cl = text.substring(m.start(), m.end());
            text = text.replace(cl, "" + ChatColor.getByChar(cl));
            m = pattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
