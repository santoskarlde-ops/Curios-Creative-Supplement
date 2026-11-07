package dev.curioscreative;

import net.minecraft.network.chat.Component;

public enum ModeSetting {
    CREATIVE_ONLY("creative_only"),
    CREATIVE_AND_OP("creative_op"),
    ALL("all");

    private final String key;

    ModeSetting(String key) { this.key = key; }

    public Component label() { return Component.translatable("gui.curioscreative.mode." + key); }

    public ModeSetting next() {
        ModeSetting[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public static ModeSetting byOrdinal(int i) {
        ModeSetting[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
