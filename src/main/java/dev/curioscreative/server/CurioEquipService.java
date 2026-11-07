package dev.curioscreative.server;

import dev.curioscreative.Config;
import dev.curioscreative.CurioChecks;
import dev.curioscreative.CuriosCreativeSupplement;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Authoritative equip/clear logic. The client only sends (slot, index, cosmetic, item id).
 * Nothing is ever taken from or given to the player's inventory.
 * Curios' own tick handler detects the changed stack, fires onEquip/onUnequip and syncs tracking clients.
 */
public final class CurioEquipService {
    private CurioEquipService() {}

    public static void apply(ServerPlayer player, String slotId, int index, boolean cosmetic, @Nullable ResourceLocation itemId) {
        if (!Config.mayUse(player)) {
            deny(player, "disabled");
            return;
        }
        String[] error = {"invalid_slot"};
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> error[0] = tryApply(player, inv, slotId, index, cosmetic, itemId));
        if (error[0] != null) deny(player, error[0]);
    }

    /** @return null on success, otherwise a lang key suffix. */
    @Nullable
    private static String tryApply(ServerPlayer player, ICuriosItemHandler inv, String slotId, int index,
                                   boolean cosmetic, @Nullable ResourceLocation itemId) {
        ICurioStacksHandler handler = inv.getStacksHandler(slotId).orElse(null);
        if (handler == null || !handler.isVisible()) return "invalid_slot";
        if (index < 0 || index >= handler.getSlots()) return "invalid_slot";          // never exceeds slot limits
        if (cosmetic && !handler.hasCosmetic()) return "invalid_slot";

        IDynamicStackHandler stacks = cosmetic ? handler.getCosmeticStacks() : handler.getStacks();
        SlotContext ctx = new SlotContext(slotId, player, index, cosmetic, true);
        boolean bypassEquip = Config.BYPASS_CAN_EQUIP.get();

        ItemStack current = stacks.getStackInSlot(index);
        if (!CurioChecks.canRemove(ctx, current, bypassEquip, Config.BYPASS_BINDING_CURSE.get())) return "locked";

        if (itemId == null) {                                  // clear: just delete the copy
            stacks.setStackInSlot(index, ItemStack.EMPTY);
            return null;
        }

        if (!ForgeRegistries.ITEMS.containsKey(itemId)) return "invalid_item";
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) return "invalid_item";
        if (Config.isBlacklisted(itemId)) return "blacklisted";

        ItemStack stack = new ItemStack(item);
        if (!CuriosApi.isStackValid(ctx, stack)) return "invalid_item";
        if (!bypassEquip && !CurioChecks.canEquip(ctx, stack)) return "cannot_equip";

        stacks.setStackInSlot(index, stack);
        return null;
    }

    private static void deny(ServerPlayer player, String key) {
        CuriosCreativeSupplement.LOGGER.debug("Denied curio change for {}: {}", player.getGameProfile().getName(), key);
        player.displayClientMessage(Component.translatable("message.curioscreative.denied." + key), true);
    }
}
