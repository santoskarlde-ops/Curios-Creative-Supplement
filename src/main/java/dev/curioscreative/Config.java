package dev.curioscreative;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;

/** COMMON config: loaded at startup on both sides, so the dedicated server and the main-menu screen both work. */
public final class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<ModeSetting> MODE;
    public static final ForgeConfigSpec.BooleanValue BYPASS_CAN_EQUIP;
    public static final ForgeConfigSpec.BooleanValue BYPASS_BINDING_CURSE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("general");
        ENABLED = b.comment("Master switch for the feature.")
                .define("enabled", true);
        MODE = b.comment("Who may use it: CREATIVE_ONLY, CREATIVE_AND_OP (creative players + survival operators), ALL.")
                .defineEnum("gameMode", ModeSetting.CREATIVE_ONLY);
        b.pop();

        b.push("rules");
        BYPASS_CAN_EQUIP = b.comment("If true, ICurio#canEquip / canUnequip restrictions are ignored. Slot/tag validity is still enforced.")
                .define("bypassCanEquip", false);
        BYPASS_BINDING_CURSE = b.comment("If true, items with Curse of Binding can be replaced/removed.")
                .define("bypassBindingCurse", false);
        BLACKLIST = b.comment("Item ids that can never be equipped through this mod, e.g. [\"modid:some_item\"].")
                .defineListAllowEmpty("itemBlacklist", List.of(), o -> o instanceof String s && ResourceLocation.isValidResourceLocation(s));
        b.pop();

        SPEC = b.build();
    }

    private Config() {}

    public static boolean isBlacklisted(ResourceLocation id) {
        return BLACKLIST.get().contains(id.toString());
    }

    public static boolean isAdmin(ServerPlayer p) {
        return p.hasPermissions(2) || p.server.isSingleplayerOwner(p.getGameProfile());
    }

    /** Authoritative server-side gate. */
    public static boolean mayUse(ServerPlayer p) {
        if (!ENABLED.get() || p.isSpectator()) return false;
        return switch (MODE.get()) {
            case CREATIVE_ONLY -> p.isCreative();
            case CREATIVE_AND_OP -> p.isCreative() || isAdmin(p);
            case ALL -> true;
        };
    }
}
