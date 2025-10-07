package net.zelythia.tool_trinkets.fabric;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zelythia.tool_trinkets.ToolTrinkets;

import java.util.Optional;

public final class ToolTrinketsFabric implements ModInitializer {
    public static final ResourceLocation MOVE_TRINKET_PACKET = new ResourceLocation(ToolTrinkets.MOD_ID, "move_trinket");

    @Override
    public void onInitialize() {
        ToolTrinkets.init();

        ServerPlayNetworking.registerGlobalReceiver(MOVE_TRINKET_PACKET, (server, player, handler, buf, sender) -> {
            int sourceSlot = buf.readInt();
            int destSlot = buf.readInt();

            server.execute(() -> {
                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
                if(trinketComponent.isPresent()) {
                    ItemStack fromStack = trinketComponent.get().getInventory().get("hand").get("tool").getItem(sourceSlot).copy();
                    ItemStack toStack = player.getInventory().getItem(destSlot).copy();

                    player.getInventory().setItem(destSlot, fromStack);
                    trinketComponent.get().getInventory().get("hand").get("tool").setItem(sourceSlot, toStack);

                    trinketComponent.get().getInventory().get("hand").get("tool").markUpdate();
                    player.containerMenu.broadcastChanges();
                }
            });
        });

    }

}
