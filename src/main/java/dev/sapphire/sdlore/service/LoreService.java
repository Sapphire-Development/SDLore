package dev.sapphire.sdlore.service;

import dev.sapphire.sdlore.SDLore;
import dev.sapphire.sdlore.api.LoreApiClient;
import dev.sapphire.sdlore.api.LoreResponse;
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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class LoreService {

    private final SDLore plugin;
    private final LoreApiClient apiClient;
    private final SoundService soundService;

    public LoreService(final SDLore plugin, final SoundService soundService) {
        this.plugin = plugin;
        this.apiClient = new LoreApiClient();
        this.soundService = soundService;
    }

    public void applyLore(final Player player, final String loreId) {
        final long startedAt = System.nanoTime();

        apiClient.fetchLore(loreId).thenAccept(result -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            final ItemStack currentItem = player.getInventory().getItemInMainHand();

            if (currentItem.getType() == Material.AIR) {
                MessageUtil.sendError(player, "no-item");
                return;
            }

            if (!result.isSuccess()) {
                handleFailure(player, loreId, result.getErrorMessage());
                return;
            }

            applyLoreToItem(player, currentItem, result.getResponse());

            final long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
            MessageUtil.sendSuccess(player, "lore-applied", Map.of("time", DurationUtil.format(elapsedMillis)));
            soundService.playApplySound(player);
        }));
    }

    private void applyLoreToItem(final Player player, final ItemStack itemStack, final LoreResponse loreResponse) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            MessageUtil.sendError(player, "no-metadata");
            return;
        }

        itemMeta.displayName(TextUtil.toComponent(loreResponse.getName()));

        if (loreResponse.getLore() != null) {
            final List<Component> loreLines = loreResponse.getLore().stream()
                    .map(TextUtil::toComponent)
                    .toList();
            itemMeta.lore(loreLines);
        } else {
            itemMeta.lore(null);
        }

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

                itemMeta.addEnchant(enchantment, entry.getLevel(), true);
            }
        }

        if (loreResponse.getFlags() != null) {
            for (final LoreResponse.FlagEntry entry : loreResponse.getFlags()) {
                try {
                    final ItemFlag flag = ItemFlag.valueOf(entry.getKey().toUpperCase());

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

        itemStack.setItemMeta(itemMeta);
    }

    private void handleFailure(final Player player, final String loreId, final String message) {
        plugin.getLogger().log(Level.WARNING, "Failed to apply lore '" + loreId + "' for " + player.getName() + ": " + message);
        MessageUtil.sendRawError(player, message);
    }
}
