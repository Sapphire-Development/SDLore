package dev.sapphire.sdlore;

import dev.sapphire.sdlore.command.SDLoreCommand;
import dev.sapphire.sdlore.service.LoreService;
import dev.sapphire.sdlore.service.SoundService;
import dev.sapphire.sdlore.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class SDLore extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        MessageUtil.init(getConfig().getConfigurationSection("messages"));

        final SoundService soundService = new SoundService(this);
        final LoreService loreService = new LoreService(this, soundService);
        final SDLoreCommand sdloreCommand = new SDLoreCommand(this, loreService);

        getCommand("sdlore").setExecutor(sdloreCommand);
        getCommand("sdlore").setTabCompleter(sdloreCommand);
    }

    @Override
    public void onDisable() {
    }
}
