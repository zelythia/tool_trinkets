package net.zelythia.tool_trinkets.forge.mixins;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.zelythia.autotools.AutoTools;
import net.zelythia.tool_trinkets.forge.network.CMoveCurioPacket;
import net.zelythia.tool_trinkets.forge.network.Networking;
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

        Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosHelper().getCuriosHandler(Minecraft.getInstance().player).resolve();
        if (curiosItemHandler.isPresent()) {
            i = curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getSlots();
        }

        return inventory.getContainerSize() + i;
    }

    @Redirect(method = "getCorrectTool", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onGetItem(Inventory inventory, int slot) {
        if (slot >= inventory.getContainerSize()) {
            Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosHelper().getCuriosHandler(Minecraft.getInstance().player).resolve();
            if (curiosItemHandler.isPresent()) {
                int size = inventory.getContainerSize();
                return curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getStackInSlot(slot - size);
            }
            return ItemStack.EMPTY;
        } else return inventory.getItem(slot);
    }


    @Redirect(method = "selectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasShiftDown()Z"))
    private static boolean onHasShiftDown() {
        return false;
    }

    @Redirect(method = "selectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onSelectItem(MultiPlayerGameMode instance, int id, int destSlot, int sourceSlot, ClickType clickType, Player player) {
        if (sourceSlot >= player.inventory.getContainerSize()) {
            Networking.CHANNEL.sendToServer(new CMoveCurioPacket(sourceSlot - player.inventory.getContainerSize(), destSlot - 36));
        } else{
            // Default behaviour
            if (Screen.hasShiftDown()) {
                instance.handleInventoryMouseClick(id, destSlot - 18, sourceSlot, ClickType.SWAP, player);
                instance.handleInventoryMouseClick(id, destSlot - 9, sourceSlot, ClickType.SWAP, player);
                return instance.handleInventoryMouseClick(id, destSlot, sourceSlot, ClickType.SWAP, player);
            }
            else return instance.handleInventoryMouseClick(id, destSlot, sourceSlot, ClickType.SWAP, player);
        }
        return null;
    }


    @Redirect(method = "switchBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;", ordinal = 1))
    private static ItemStack onSwitchBack(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player) {
        if (sourceSlot >= player.inventory.getContainerSize()) {
                Networking.CHANNEL.sendToServer(new CMoveCurioPacket(sourceSlot - player.inventory.getContainerSize(), destSlot));
        } else return instance.handleInventoryMouseClick(id, destSlot, sourceSlot, ClickType.SWAP, player);
        return null;
    }
}