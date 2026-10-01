package dev.sapphire.sdlore.service;

import dev.sapphire.sdlore.SDLore;
import dev.sapphire.sdlore.api.LoreResponse;
import dev.sapphire.sdlore.util.DebugLogger;
import dev.sapphire.sdlore.util.MessageUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class AttributeService {

    // Attribute keys lost their "generic."/"player."/"zombie." prefixes in 1.21.3.
    private static final List<String> LEGACY_PREFIXES = List.of("", "generic.", "player.", "zombie.");

    private final SDLore plugin;
    private final DebugLogger debug;
    private final boolean supported;

    public AttributeService(final SDLore plugin, final DebugLogger debug) {
        this.plugin = plugin;
        this.debug = debug;
        this.supported = detectSupport();

        if (!supported) {
            plugin.getLogger().log(Level.WARNING, "Item attributes require Minecraft 1.21 or newer; attributes will be ignored.");
        }
    }

    public boolean isSupported() {
        return supported;
    }

    public void applyAttributes(final Player player, final ItemMeta itemMeta, final List<LoreResponse.AttributeEntry> attributes) {
        itemMeta.setAttributeModifiers(null);

        if (attributes == null || attributes.isEmpty()) {
            debug.log(() -> "Attributes: none, restoring vanilla modifiers");
            return;
        }

        for (int index = 0; index < attributes.size(); index++) {
            final LoreResponse.AttributeEntry entry = attributes.get(index);
            final Attribute attribute = resolveAttribute(entry.getId());

            if (attribute == null) {
                plugin.getLogger().log(Level.WARNING, "Unknown attribute id: " + entry.getId());
                MessageUtil.sendError(player, "unknown-attribute", Map.of("attribute", String.valueOf(entry.getId())));
                continue;
            }

            final AttributeModifier.Operation operation = resolveOperation(entry.getOperation());

            if (operation == null) {
                plugin.getLogger().log(Level.WARNING, "Unknown attribute operation: " + entry.getOperation());
                continue;
            }

            // The index keeps keys unique even when the same attribute and slot repeat.
            final NamespacedKey key = new NamespacedKey(plugin, "sdlore_attr_" + index);
            final EquipmentSlotGroup slot = resolveSlot(entry.getSlot());
            final AttributeModifier modifier = new AttributeModifier(key, entry.getAmount(), operation, slot);

            debug.log(() -> "Attribute: " + entry.getId() + " " + entry.getAmount() + " " + operation
                    + " slot=" + slot + " (raw: " + entry.getSlot() + ") key=" + key);

            itemMeta.addAttributeModifier(attribute, modifier);
        }
    }

    private Attribute resolveAttribute(final String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        final String normalized = id.toLowerCase(Locale.ROOT);

        for (final String prefix : LEGACY_PREFIXES) {
            final NamespacedKey key = NamespacedKey.fromString(prefix + normalized);

            if (key == null) {
                continue;
            }

            final Attribute attribute = Registry.ATTRIBUTE.get(key);

            if (attribute != null) {
                return attribute;
            }
        }

        return null;
    }

    private static AttributeModifier.Operation resolveOperation(final String operation) {
        if (operation == null) {
            return null;
        }

        return switch (operation.toLowerCase(Locale.ROOT)) {
            case "add_value" -> AttributeModifier.Operation.ADD_NUMBER;
            case "add_multiplied_base" -> AttributeModifier.Operation.ADD_SCALAR;
            case "add_multiplied_total" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> null;
        };
    }

    private static EquipmentSlotGroup resolveSlot(final String slot) {
        if (slot == null) {
            return EquipmentSlotGroup.ANY;
        }

        final EquipmentSlotGroup group = EquipmentSlotGroup.getByName(slot.toLowerCase(Locale.ROOT));
        return group != null ? group : EquipmentSlotGroup.ANY;
    }

    private static boolean detectSupport() {
        try {
            final Class<?> slotGroupClass = Class.forName("org.bukkit.inventory.EquipmentSlotGroup");
            AttributeModifier.class.getConstructor(NamespacedKey.class, double.class, AttributeModifier.Operation.class, slotGroupClass);
            return true;
        } catch (final ClassNotFoundException | NoSuchMethodException exception) {
            return false;
        }
    }
}
