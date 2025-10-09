package net.zelythia.tool_trinkets.forge.mixins;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.tool_trinkets.forge.network.CMoveCurioPacket;
import net.zelythia.tool_trinkets.forge.network.Networking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

@Mixin(AutoTools.class)
public class AutoToolsMixin {

    @Unique
    private static int tool_trinkets$wrongItems = 0;

    @Redirect(method = "getCorrectTool", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getContainerSize()I"))
    private static int onGetContainerSize(Inventory inventory) {
        int i = 0;

        Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosInventory(Minecraft.getInstance().player).resolve();
        if(curiosItemHandler.isPresent()) {
            i = curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getSlots();
        }

        return inventory.getContainerSize() + i;
    }

    @Redirect(method = "getCorrectTool", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onGetItem(Inventory inventory, int slot) {
        if(slot >= inventory.getContainerSize()) {
            Optional<ICuriosItemHandler> curiosItemHandler = CuriosApi.getCuriosInventory(Minecraft.getInstance().player).resolve();
            if(curiosItemHandler.isPresent()) {
                int size = inventory.getContainerSize();
                return curiosItemHandler.get().getStacksHandler("tools").get().getStacks().getStackInSlot(slot - size);
            }
            return ItemStack.EMPTY;
        }
        else return inventory.getItem(slot);
    }


    @Redirect(method = "selectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"))
    private static void onSelectItem(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player){
        if(sourceSlot >= player.getInventory().getContainerSize()) {
            if(player.getInventory().getItem(destSlot) != ItemStack.EMPTY && AutoToolsConfig.get().switchBack) {
                tool_trinkets$wrongItems++;
            }
            Networking.CHANNEL.sendToServer(new CMoveCurioPacket(sourceSlot - player.getInventory().getContainerSize(), destSlot));
        }
        else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }

    @Redirect(method = "switchBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", ordinal = 1))
    private static void onSwitchBack(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player){
        if(sourceSlot >= player.getInventory().getContainerSize()) {
            if(tool_trinkets$wrongItems > 0){
                tool_trinkets$wrongItems--;
                Networking.CHANNEL.sendToServer(new CMoveCurioPacket(sourceSlot - player.getInventory().getContainerSize(), destSlot));
            }
            else {
                Networking.CHANNEL.sendToServer(new CMoveCurioPacket(Integer.MAX_VALUE, destSlot));
            }
        }
        else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }
}
