package net.zelythia.tool_trinkets.fabric.mixins.client;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.zelythia.autotools.AutoTools;
import net.zelythia.tool_trinkets.fabric.ToolTrinketsFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(AutoTools.class)
public class AutoToolsMixin {

    @Redirect(method = "getCorrectTool", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getContainerSize()I"))
    private static int onGetContainerSize(Inventory inventory) {
        int i = 0;

        Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(Minecraft.getInstance().player);
        if(trinketComponent.isPresent()) {
            i = trinketComponent.get().getInventory().get("hand").get("tool").getContainerSize();
        }

        return inventory.getContainerSize() + i;
    }

    @Redirect(method = "getCorrectTool", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onGetItem(Inventory inventory, int slot) {
        if(slot >= inventory.getContainerSize()) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(Minecraft.getInstance().player);
            if(trinketComponent.isPresent()) {
                int size = inventory.getContainerSize();
                return trinketComponent.get().getInventory().get("hand").get("tool").getItem(slot - size);
            }
            return ItemStack.EMPTY;
        }
        else return inventory.getItem(slot);
    }


    @Redirect(method = "selectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"))
    private static void onSelectItem(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player){
        if(sourceSlot >= player.getInventory().getContainerSize()) {
            ClientPlayNetworking.send(new ToolTrinketsFabric.MoveTrinketPayload(sourceSlot - player.getInventory().getContainerSize(), destSlot));
        }
        else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }

    @Redirect(method = "switchBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", ordinal = 1))
    private static void onSwitchBack(MultiPlayerGameMode instance, int id, int sourceSlot, int destSlot, ClickType clickType, Player player){
        if(sourceSlot >= player.getInventory().getContainerSize()) {
            ClientPlayNetworking.send(new ToolTrinketsFabric.MoveTrinketPayload(sourceSlot - player.getInventory().getContainerSize(), destSlot));
        }
        else instance.handleInventoryMouseClick(id, sourceSlot, destSlot, ClickType.SWAP, player);
    }
}

