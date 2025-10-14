package net.zelythia.tool_trinkets.fabric;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.zelythia.tool_trinkets.ToolTrinkets;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ToolTrinketsFabric implements ModInitializer {
    public static final ResourceLocation MOVE_TRINKET_PACKET = new ResourceLocation(ToolTrinkets.MOD_ID, "move_trinket");


    public record MoveTrinketPayload(int sourceSlot, int destSlot) implements CustomPacketPayload{
        public static final CustomPacketPayload.Type<MoveTrinketPayload> ID = new CustomPacketPayload.Type<>(MOVE_TRINKET_PACKET);
        public static final StreamCodec<FriendlyByteBuf, MoveTrinketPayload> CODEC = new StreamCodec<FriendlyByteBuf, MoveTrinketPayload>() {
            @Override
            public @NotNull MoveTrinketPayload decode(FriendlyByteBuf buf) {
                return new MoveTrinketPayload(buf.readInt(), buf.readInt());
            }

            @Override
            public void encode(FriendlyByteBuf buf, MoveTrinketPayload payload) {
                buf.writeInt(payload.sourceSlot);
                buf.writeInt(payload.destSlot);
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    @Override
    public void onInitialize() {
        ToolTrinkets.init();

        PayloadTypeRegistry.playC2S().register(MoveTrinketPayload.ID, MoveTrinketPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MoveTrinketPayload.ID, (moveTrinketPayload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();

                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
                if(trinketComponent.isPresent()) {
                    ItemStack fromStack = trinketComponent.get().getInventory().get("hand").get("tool").getItem(moveTrinketPayload.sourceSlot).copy();
                    ItemStack toStack = player.getInventory().getItem(moveTrinketPayload.destSlot).copy();

                    player.getInventory().setItem(moveTrinketPayload.destSlot, fromStack);
                    trinketComponent.get().getInventory().get("hand").get("tool").setItem(moveTrinketPayload.sourceSlot, toStack);

                    trinketComponent.get().getInventory().get("hand").get("tool").markUpdate();
                    player.containerMenu.broadcastChanges();
                }
            });
        });
    }

}
