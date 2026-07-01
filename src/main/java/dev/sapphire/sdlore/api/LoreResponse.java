package dev.sapphire.sdlore.api;

import java.util.List;

public final class LoreResponse {

    private String name;
    private List<String> lore;
    private List<EnchantmentEntry> enchantments;
    private List<FlagEntry> flags;
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
}
