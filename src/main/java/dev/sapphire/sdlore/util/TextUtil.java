package dev.sapphire.sdlore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final Pattern SHORT_HEX_PATTERN = Pattern.compile("(?i)[&\u00a7]#([0-9a-f]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)[&\u00a7]x([&\u00a7][0-9a-f]){6}");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)[&\u00a7]([0-9a-fk-orx])");

    private TextUtil() {
    }

    public static Component toComponent(final String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        return MINI_MESSAGE.deserialize(toMiniMessage(input))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Rewrites legacy colour codes into their MiniMessage equivalent, leaving any
     * MiniMessage tag already present in the input untouched.
     *
     * <p>Supported legacy input: {@code &a}, {@code &l}, {@code &#RRGGBB} and the
     * BungeeCord-style {@code &x&R&R&G&G&B&B}, with {@code §} accepted as well.</p>
     */
    public static String toMiniMessage(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder(input.length());
        final Matcher shortHexMatcher = SHORT_HEX_PATTERN.matcher(input);
        final Matcher legacyHexMatcher = LEGACY_HEX_PATTERN.matcher(input);
        final Matcher codeMatcher = LEGACY_CODE_PATTERN.matcher(input);

        final int length = input.length();
        int index = 0;

        while (index < length) {
            if (shortHexMatcher.region(index, length).lookingAt()) {
                appendColor(builder, "#" + shortHexMatcher.group(1).toLowerCase(Locale.ROOT));
                index = shortHexMatcher.end();
                continue;
            }

            if (legacyHexMatcher.region(index, length).lookingAt()) {
                appendColor(builder, extractHex(legacyHexMatcher.group()));
                index = legacyHexMatcher.end();
                continue;
            }

            if (codeMatcher.region(index, length).lookingAt()) {
                final String tag = toMiniMessageTag(Character.toLowerCase(codeMatcher.group(1).charAt(0)));

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

    private static String toMiniMessageTag(final char code) {
        final LegacyFormat format = LegacyComponentSerializer.parseChar(code);

        if (format == null) {
            return null;
        }

        if (format.reset()) {
            return "reset";
        }

        final TextColor color = format.color();

        if (color instanceof NamedTextColor namedColor) {
            return NamedTextColor.NAMES.key(namedColor);
        }

        final TextDecoration decoration = format.decoration();

        if (decoration != null) {
            return TextDecoration.NAMES.key(decoration);
        }

        return null;
    }

    private static void appendColor(final StringBuilder builder, final String hex) {
        builder.append("<reset><color:").append(hex).append('>');
    }

    private static String extractHex(final String legacyHex) {
        final StringBuilder hex = new StringBuilder("#");

        for (int index = 0; index < legacyHex.length(); index++) {
            final char character = legacyHex.charAt(index);

            if (Character.digit(character, 16) >= 0) {
                hex.append(Character.toLowerCase(character));
            }
        }

        return hex.toString();
    }
}
