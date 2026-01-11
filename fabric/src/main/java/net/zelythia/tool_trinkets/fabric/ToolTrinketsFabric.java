package net.zelythia.tool_trinkets.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zelythia.tool_trinkets.ToolTrinkets;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeInfo;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public final class ToolTrinketsFabric implements ModInitializer {
    public static final ResourceLocation MOVE_TRINKET_PACKET = new ResourceLocation(ToolTrinkets.MOD_ID, "move_trinket");

    @Override
    public void onInitialize() {
        ToolTrinkets.init();

        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlasTexture, registry) -> {
            registry.register(new ResourceLocation(ToolTrinkets.MOD_ID, "slot/tool"));
        });

        CuriosApi.enqueueSlotType(SlotTypeInfo.BuildScheme.REGISTER, new SlotTypeInfo.Builder("tools").icon(new ResourceLocation(ToolTrinkets.MOD_ID, "slot/tool")).size(9).build());


        ServerPlayNetworking.registerGlobalReceiver(MOVE_TRINKET_PACKET, (server, player, handler, buf, sender) -> {
            int curioSlot = buf.readInt();  // CurioSlot
            int invSlot = buf.readInt();

            server.execute(() -> {
                // Bounds check
                if (curioSlot < 0 || invSlot < 0) return;
                if (invSlot >= player.containerMenu.slots.size()) return;

                CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(curiosItemHandler -> {

                    ICurioStacksHandler stacksHandler = curiosItemHandler.getStacksHandler("tools").orElseGet(null);
                    if (stacksHandler == null) return;


                    ItemStack fromStack = stacksHandler.getStacks().getItem(curioSlot).copy();
                    ItemStack toStack = player.inventory.getItem(invSlot).copy();

                    player.inventory.setItem(invSlot, fromStack);
                    stacksHandler.getStacks().setItem(curioSlot, toStack);

                    // mark changes so server sends updates
//                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SPacketSyncCurios(player.getId(), curiosItemHandler.getCurios()));
                    player.containerMenu.broadcastChanges();
                });
            });
        });
    }
}
