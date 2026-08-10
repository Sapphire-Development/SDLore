package dev.sapphire.sdlore.command;

import dev.sapphire.sdlore.SDLore;
import dev.sapphire.sdlore.service.LoreService;
import dev.sapphire.sdlore.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SDLoreCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("apply", "reload");
    private static final List<String> APPLY_FLAGS = List.of("--name", "--lore", "--enchantments", "--flags");

    private final SDLore plugin;
    private final LoreService loreService;

    public SDLoreCommand(final SDLore plugin, final LoreService loreService) {
        this.plugin = plugin;
        this.loreService = loreService;
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final String[] args
    ) {
        if (args.length < 1) {
            MessageUtil.sendError(sender, "usage");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (args[0].equalsIgnoreCase("apply")) {
            return handleApply(sender, args);
        }

        MessageUtil.sendError(sender, "usage");
        return true;
    }

    private boolean handleReload(final CommandSender sender) {
        if (!sender.hasPermission("sdlore.reload")) {
            MessageUtil.sendError(sender, "no-permission");
            return true;
        }

        plugin.reloadConfig();
        MessageUtil.init(plugin.getConfig().getConfigurationSection("messages"));
        MessageUtil.sendSuccess(sender, "config-reloaded");
        return true;
    }

    private boolean handleApply(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendError(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("sdlore.apply")) {
            MessageUtil.sendError(player, "no-permission");
            return true;
        }

        if (args.length < 2) {
            MessageUtil.sendError(player, "usage");
            return true;
        }

        final Set<String> flags = parseFlags(args);

        for (final String flag : flags) {
            if (!APPLY_FLAGS.contains("--" + flag)) {
                MessageUtil.sendRawError(player, "Unknown flag: --" + flag);
                return true;
            }
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            MessageUtil.sendError(player, "no-item");
            return true;
        }

        loreService.applyLore(player, args[1], flags);

        return true;
    }

    private Set<String> parseFlags(final String[] args) {
        final Set<String> flags = new HashSet<>();
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                flags.add(args[i].substring(2).toLowerCase(Locale.ROOT));
            }
        }
        return flags;
    }

    @Override
    public List<String> onTabComplete(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String alias,
            final String[] args
    ) {
        if (args.length == 1) {
            final List<String> result = new ArrayList<>();

            for (final String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    if ("apply".equals(subcommand) && !sender.hasPermission("sdlore.apply")) {
                        continue;
                    }
                    if ("reload".equals(subcommand) && !sender.hasPermission("sdlore.reload")) {
                        continue;
                    }
                    result.add(subcommand);
                }
            }

            return result;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("apply") && sender.hasPermission("sdlore.apply")) {
            final List<String> result = new ArrayList<>();
            final String last = args[args.length - 1].toLowerCase(Locale.ROOT);

            for (final String flag : APPLY_FLAGS) {
                if (flag.startsWith(last)) {
                    result.add(flag);
                }
            }

            return result;
        }

        return Collections.emptyList();
    }
}
