package net.zelythia.tool_trinkets.neoforge.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zelythia.autotools.AutoTools;
import net.zelythia.tool_trinkets.neoforge.ToolTrinketsNeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

@Mixin(AutoTools.class)
public class AutoToolsMixin {

    @Redirect(method = "getCorrectTool", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getContainerSize()I"))
    private static int onGetContainerSize(Inventory inventory) {
        int i = 0;

        Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosInventory(Minecraft.getInstance().player);
        if (curiosItemHandler.isPresent()) {
            i = curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getSlots();
        }

        return inventory.getContainerSize() + i;
    }

    @Redirect(method = "getCorrectTool", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onGetItem(Inventory inventory, int slot) {
        if (slot >= inventory.getContainerSize()) {
            Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosInventory(Minecraft.getInstance().player);
            if (curiosItemHandler.isPresent()) {
                int size = inventory.getContainerSize();
                return curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getStackInSlot(slot - size);
            }
            return ItemStack.EMPTY;
        } else return inventory.getItem(slot);
    }


    @Redirect(method = "selectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"))
    private static void onSelectItem(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player) {
        if (sourceSlot >= player.getInventory().getContainerSize()) {
            PacketDistributor.sendToServer(new ToolTrinketsNeoForge.MoveTrinketPayload(sourceSlot - player.getInventory().getContainerSize(), destSlot));
        } else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }

    @Redirect(method = "switchBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", ordinal = 1))
    private static void onSwitchBack(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player) {
        if (sourceSlot >= player.getInventory().getContainerSize()) {
            PacketDistributor.sendToServer(new ToolTrinketsNeoForge.MoveTrinketPayload(sourceSlot - player.getInventory().getContainerSize(), destSlot));
        } else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }
}

