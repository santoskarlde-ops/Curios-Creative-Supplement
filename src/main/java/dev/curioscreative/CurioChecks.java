package dev.curioscreative;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** Thin wrappers over Curios' own validity checks. Used by server (authoritative) and client (picker listing). */
public final class CurioChecks {
    private CurioChecks() {}

    public static boolean canEquip(SlotContext ctx, ItemStack stack) {
        return CuriosApi.getCurio(stack).map(c -> c.canEquip(ctx)).orElse(true);
    }

    public static boolean canUnequip(SlotContext ctx, ItemStack stack) {
        return CuriosApi.getCurio(stack).map(c -> c.canUnequip(ctx)).orElse(true);
    }

    /** Slot type / tag validity plus canEquip. */
    public static boolean isValid(SlotContext ctx, ItemStack stack, boolean bypassCanEquip) {
        return CuriosApi.isStackValid(ctx, stack) && (bypassCanEquip || canEquip(ctx, stack));
    }

    public static boolean canRemove(SlotContext ctx, ItemStack stack, boolean bypassCanEquip, boolean bypassCurse) {
        if (stack.isEmpty()) return true;
        if (!bypassCurse && EnchantmentHelper.hasBindingCurse(stack)) return false;
        return bypassCanEquip || canUnequip(ctx, stack);
    }
}
