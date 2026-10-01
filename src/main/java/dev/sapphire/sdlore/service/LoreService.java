package dev.sapphire.sdlore.service;

import dev.sapphire.sdlore.SDLore;
import dev.sapphire.sdlore.api.LoreApiClient;
import dev.sapphire.sdlore.api.LoreResponse;
import dev.sapphire.sdlore.util.DebugLogger;
import dev.sapphire.sdlore.util.DurationUtil;
import dev.sapphire.sdlore.util.MessageUtil;
import dev.sapphire.sdlore.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class LoreService {

    private final SDLore plugin;
    private final LoreApiClient apiClient;
    private final SoundService soundService;
    private final AttributeService attributeService;
    private final DebugLogger debug;

    public LoreService(final SDLore plugin, final SoundService soundService) {
        this.plugin = plugin;
        this.debug = new DebugLogger(plugin);
        this.apiClient = new LoreApiClient(debug);
        this.soundService = soundService;
        this.attributeService = new AttributeService(plugin, debug);
    }

    public void applyLore(final Player player, final String loreId) {
        applyLore(player, loreId, Collections.emptySet());
    }

    public void applyLore(final Player player, final String loreId, final Set<String> flags) {
        final long startedAt = System.nanoTime();

        debug.log(() -> "Apply requested by " + player.getName() + ": id=" + loreId
                + ", sections=" + (flags.isEmpty() ? "all" : String.join(",", flags)));

        apiClient.fetchLore(loreId).thenAccept(result -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                debug.log(() -> player.getName() + " went offline before the lore could be applied");
                return;
            }

            final ItemStack currentItem = player.getInventory().getItemInMainHand();

            if (currentItem.getType() == Material.AIR) {
                debug.log(() -> player.getName() + " is no longer holding an item");
                MessageUtil.sendError(player, "no-item");
                return;
            }

            if (!result.isSuccess()) {
                handleFailure(player, loreId, result.getErrorMessage());
                return;
            }

            debug.log(() -> "Applying to " + currentItem.getType() + " x" + currentItem.getAmount());
            applyLoreToItem(player, currentItem, result.getResponse(), flags);

            final long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
            debug.log(() -> "Apply finished in " + elapsedMillis + "ms");
            MessageUtil.sendSuccess(player, "lore-applied", Map.of("time", DurationUtil.format(elapsedMillis)));
            soundService.playApplySound(player);
        }));
    }

    private void applyLoreToItem(final Player player, final ItemStack itemStack, final LoreResponse loreResponse, final Set<String> flags) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            debug.log(() -> itemStack.getType() + " has no item meta");
            MessageUtil.sendError(player, "no-metadata");
            return;
        }

        final boolean applyAll = flags.isEmpty();

        if (applyAll || flags.contains("name")) {
            debug.log(() -> "Name: " + loreResponse.getName());
            itemMeta.displayName(TextUtil.toComponent(loreResponse.getName()));
        }

        if (applyAll || flags.contains("lore")) {
            if (loreResponse.getLore() != null) {
                debug.log(() -> "Lore: " + loreResponse.getLore().size() + " line(s)");
                final List<Component> loreLines = loreResponse.getLore().stream()
                        .map(TextUtil::toComponent)
                        .toList();
                itemMeta.lore(loreLines);
            } else {
                debug.log(() -> "Lore: none, clearing");
                itemMeta.lore(null);
            }
        }

        if (applyAll || flags.contains("enchantments")) {
            for (final Enchantment enchantment : itemMeta.getEnchants().keySet()) {
                itemMeta.removeEnchant(enchantment);
            }

            if (loreResponse.getEnchantments() != null) {
                for (final LoreResponse.EnchantmentEntry entry : loreResponse.getEnchantments()) {
                    final Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.getId()));

                    if (enchantment == null) {
                        plugin.getLogger().log(Level.WARNING, "Unknown enchantment id: " + entry.getId());
                        MessageUtil.sendError(player, "unknown-enchantment", Map.of("enchantment", entry.getId()));
                        continue;
                    }

                    debug.log(() -> "Enchantment: " + entry.getId() + " " + entry.getLevel());
                    itemMeta.addEnchant(enchantment, entry.getLevel(), true);
                }
            }
        }

        if (applyAll || flags.contains("flags")) {
            if (loreResponse.getFlags() != null) {
                for (final LoreResponse.FlagEntry entry : loreResponse.getFlags()) {
                    try {
                        final ItemFlag flag = ItemFlag.valueOf(entry.getKey().toUpperCase());
                        debug.log(() -> "Flag: " + flag + "=" + entry.isValue());

                        if (entry.isValue()) {
                            itemMeta.addItemFlags(flag);
                        } else {
                            itemMeta.removeItemFlags(flag);
                        }
                    } catch (final IllegalArgumentException exception) {
                        plugin.getLogger().log(Level.WARNING, "Unknown item flag: " + entry.getKey());
                    }
                }
            }
        }

        if (applyAll || flags.contains("attributes")) {
            if (attributeService.isSupported()) {
                attributeService.applyAttributes(player, itemMeta, loreResponse.getAttributes());
            } else if (loreResponse.getAttributes() != null && !loreResponse.getAttributes().isEmpty()) {
                debug.log(() -> "Attributes: skipped, server does not support them");
                MessageUtil.sendError(player, "attributes-unsupported");
            }
        }

        itemStack.setItemMeta(itemMeta);
    }

    private void handleFailure(final Player player, final String loreId, final String message) {
        plugin.getLogger().log(Level.WARNING, "Failed to apply lore '" + loreId + "' for " + player.getName() + ": " + message);
        MessageUtil.sendRawError(player, message);
    }
}
