package dev.sapphire.sdlore.service;

import dev.sapphire.sdlore.SDLore;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class SoundService {

    private final SDLore plugin;

    public SoundService(final SDLore plugin) {
        this.plugin = plugin;
    }

    public void playApplySound(final Player player) {
        final ConfigurationSection soundConfig = plugin.getConfig().getConfigurationSection("sound");
        if (soundConfig == null || !soundConfig.getBoolean("enabled", true)) {
            return;
        }

        final String soundName = soundConfig.getString("name", "entity.player.levelup");
        final float volume = (float) soundConfig.getDouble("volume", 1.0);
        final float pitch = (float) soundConfig.getDouble("pitch", 1.0);

        player.playSound(Sound.sound(
                NamespacedKey.minecraft(soundName.toLowerCase()),
                Sound.Source.PLAYER,
                volume,
                pitch
        ));
    }
}
