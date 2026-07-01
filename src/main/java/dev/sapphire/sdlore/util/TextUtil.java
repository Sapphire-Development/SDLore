package dev.sapphire.sdlore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)[&§]x([&§][0-9a-f]){6}");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&§]([0-9a-fk-orx])", Pattern.CASE_INSENSITIVE);

    private static final Map<Character, String> LEGACY_TO_MINIMESSAGE = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"),
            Map.entry('k', "obfuscated"),
            Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"),
            Map.entry('o', "italic"),
            Map.entry('r', "reset")
    );

    private TextUtil() {
    }

    public static Component toComponent(final String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        return MINI_MESSAGE.deserialize(convertLegacyToMiniMessage(input))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static String convertLegacyToMiniMessage(final String input) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;

        while (index < input.length()) {
            final Matcher hexMatcher = LEGACY_HEX_PATTERN.matcher(input);
            hexMatcher.region(index, input.length());

            if (hexMatcher.lookingAt()) {
                final String hexMatch = hexMatcher.group();
                final String hex = extractHex(hexMatch);
                builder.append("<reset><color:").append(hex).append(">");
                index = hexMatcher.end();
                continue;
            }

            final Matcher codeMatcher = LEGACY_CODE_PATTERN.matcher(input);
            codeMatcher.region(index, input.length());

            if (codeMatcher.lookingAt()) {
                final char code = Character.toLowerCase(codeMatcher.group(1).charAt(0));
                final String tag = LEGACY_TO_MINIMESSAGE.get(code);

                if (tag != null) {
                    builder.append('<').append(tag).append('>');
                }

                index = codeMatcher.end();
                continue;
            }

            builder.append(input.charAt(index));
            index++;
        }

        return builder.toString();
    }

    private static String extractHex(final String legacyHex) {
        final StringBuilder hex = new StringBuilder("#");

        for (int index = 0; index < legacyHex.length(); index++) {
            final char character = legacyHex.charAt(index);

            if (Character.digit(character, 16) >= 0) {
                hex.append(character);
            }
        }

        return hex.toString();
    }
}
