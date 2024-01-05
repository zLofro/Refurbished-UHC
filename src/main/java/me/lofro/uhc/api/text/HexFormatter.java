package me.lofro.uhc.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexFormatter {

    private static final Component name = hexFormat("&6&lRefurbished UHC");

    private static final Component prefix = hexFormat("&6&lRefurbished UHC >> ");

    public static Component hexFormat(Component text) {
        return hexFormat(deserialize(text));
    }

    public static Component hexFormat(String text) { //TODO MAKE DECORATION COMBINATIONS WORK.
        String newText = text;

        Matcher chatColorMatcher = Pattern.compile("&[0-9a-f]").matcher(newText);

        while (chatColorMatcher.find()) {
            char char1 = chatColorMatcher.group(0).charAt(1);
            newText = newText.replace(chatColorMatcher.group(0), getHex(char1));
        }

        Matcher hexMatcher = Pattern.compile("#([a-fA-F0-9]{6})((?:(?!#[a-fA-F0-9]{6})[\s\\S])*)").matcher(newText);

        TextComponent.@NotNull Builder component = Component.text();
        while (hexMatcher.find()) {
            var textGroup = hexMatcher.group(2);
            Matcher decorationMatcher = Pattern.compile("&([k-o]{1})((?:(?!&[k-o]{1})[\\s\\S])*)").matcher(textGroup);
            TextComponent.@NotNull Builder decorationComponent = Component.text();

            while (decorationMatcher.find()) {
                var decorationText = decorationMatcher.group(2);
                var decoration = getDecoration(decorationMatcher.group(1).charAt(0));

                decorationComponent.append(Component.text(decorationText).decorate(decoration));
            }
            if (Component.IS_NOT_EMPTY.test(decorationComponent.build())) {
                component.append(decorationComponent.color(TextColor.fromHexString("#"+hexMatcher.group(1))));
            } else {
                component.append(Component.text(textGroup).color(TextColor.fromHexString("#"+hexMatcher.group(1))));
            }
        }
        TextComponent fcomponent = component.build();
        return Component.IS_NOT_EMPTY.test(fcomponent) ? fcomponent : Component.text(text);
    }

    public static String deserialize(Component text) {
        String[] deserializedText = new String[1];
        deserializedText[0] = "";
        text.spliterator(ComponentIteratorType.DEPTH_FIRST).forEachRemaining(component -> {
            if (component instanceof TextComponent textComponent) {
                TextColor color = textComponent.color();

                if (color != null) {
                    deserializedText[0] += TextColor.fromHexString(color.asHexString()) + textComponent.content();
                } else if (Component.IS_NOT_EMPTY.test(textComponent)) {
                    deserializedText[0] += textComponent.content();
                }
            }
        });
        return deserializedText[0];
    }

    public static Component hexFormatWithPrefix(String text) {
        return prefix.append(hexFormat(text));
    }

    private static String getHex(char symbol) {
        return switch(symbol) {
            case '0' -> "#000000";
            case '1' -> "#0000AA";
            case '2' -> "#00AA00";
            case '3' -> "#00AAAA";
            case '4' -> "#AA0000";
            case '5' -> "#AA00AA";
            case '6' -> "#FFAA00";
            case '7' -> "#AAAAAA";
            case '8' -> "#555555";
            case '9' -> "#5555FF";
            case 'a' -> "#55FF55";
            case 'b' -> "#55FFFF";
            case 'c' -> "#FF5555";
            case 'd' -> "#FF55FF";
            case 'e' -> "#FFFF55";
            default -> "#FFFFFF";
        };
    }

    private static TextDecoration getDecoration(char symbol) {
        return switch (symbol) {
            case 'l' -> TextDecoration.BOLD;
            case 'k' -> TextDecoration.OBFUSCATED;
            case 'm' -> TextDecoration.STRIKETHROUGH;
            case 'n' -> TextDecoration.UNDERLINED;
            case 'o' -> TextDecoration.ITALIC;
            default -> null;
        };
    }

}
