package dev.sapphire.sdlore.api;

import java.util.List;

public final class LoreResponse {

    private String name;
    private List<String> lore;
    private List<EnchantmentEntry> enchantments;
    private List<FlagEntry> flags;
    private List<AttributeEntry> attributes;
    private String error;

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<EnchantmentEntry> getEnchantments() {
        return enchantments;
    }

    public List<FlagEntry> getFlags() {
        return flags;
    }

    public List<AttributeEntry> getAttributes() {
        return attributes;
    }

    public String getError() {
        return error;
    }

    public static final class EnchantmentEntry {

        private String id;
        private int level;

        public String getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }
    }

    public static final class FlagEntry {

        private String key;
        private boolean value;

        public String getKey() {
            return key;
        }

        public boolean isValue() {
            return value;
        }
    }

    public static final class AttributeEntry {

        private String id;
        private double amount;
        private String operation;
        private String slot;

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public String getOperation() {
            return operation;
        }

        public String getSlot() {
            return slot;
        }
    }
}
