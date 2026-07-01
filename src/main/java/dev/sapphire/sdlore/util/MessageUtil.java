package dev.sapphire.sdlore.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static String PREFIX = "<#0069a8>SDLore <gray>\u00bb ";
    private static ConfigurationSection MESSAGES;

    private MessageUtil() {
    }

    public static void init(final ConfigurationSection messages) {
        if (messages != null) {
            PREFIX = messages.getString("prefix", PREFIX);
        }
        MESSAGES = messages;
    }

    public static void sendSuccess(final CommandSender sender, final String key) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + getMessage(key)));
    }

    public static void sendSuccess(final CommandSender sender, final String key, final Map<String, String> placeholders) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + applyPlaceholders(getMessage(key), placeholders)));
    }

    public static void sendError(final CommandSender sender, final String key) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + getMessage(key)));
    }

    public static void sendError(final CommandSender sender, final String key, final Map<String, String> placeholders) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + applyPlaceholders(getMessage(key), placeholders)));
    }

    public static void sendRawError(final CommandSender sender, final String message) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + message));
    }

    private static String getMessage(final String key) {
        if (MESSAGES != null && MESSAGES.contains(key)) {
            return MESSAGES.getString(key);
        }
        return key;
    }

    private static String applyPlaceholders(String message, final Map<String, String> placeholders) {
        if (placeholders != null) {
            for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }
}
