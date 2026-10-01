package dev.sapphire.sdlore.util;

import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

public final class DebugLogger {

    private final Plugin plugin;

    public DebugLogger(final Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    public void log(final Supplier<String> message) {
        if (isEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message.get());
        }
    }
}
