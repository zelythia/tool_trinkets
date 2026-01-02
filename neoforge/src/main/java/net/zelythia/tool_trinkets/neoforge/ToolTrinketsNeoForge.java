package net.zelythia.tool_trinkets.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zelythia.tool_trinkets.ToolTrinkets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;

@Mod(ToolTrinkets.MOD_ID)
public class ToolTrinketsNeoForge {
    public static final ResourceLocation MOVE_TRINKET_PACKET = new ResourceLocation(ToolTrinkets.MOD_ID, "move_trinket");
    public static final TagKey<Item> TOOLS = ItemTags.create(new ResourceLocation("curios", "tools"));
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTrinketsNeoForge.class);


    public ToolTrinketsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Registering mod for game events
        modEventBus.register(this);

    }

    @SubscribeEvent
    public void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(MoveTrinketPayload.TYPE, MoveTrinketPayload.CODEC, (pkt, iPayloadContext) -> {
            iPayloadContext.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) iPayloadContext.player();

                // Bounds check
                if (pkt.curioSlot < 0 || pkt.invSlot < 0) return;
                if (pkt.invSlot >= player.containerMenu.slots.size()) return;

                CuriosApi.getCuriosInventory(player).ifPresent(curiosItemHandler -> {
                    int curioSlot = pkt.curioSlot;

                    ICurioStacksHandler stacksHandler = curiosItemHandler.getStacksHandler("tools").orElseGet(null);
                    if (stacksHandler == null) return;

                    if (curioSlot >= stacksHandler.getSlots()) {
                        for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                            if (stacksHandler.getStacks().getStackInSlot(i).isEmpty()) {
                                curioSlot = i;
                                break;
                            }
                        }
                    }

                    ItemStack fromStack = stacksHandler.getStacks().getStackInSlot(curioSlot).copy();
                    ItemStack toStack = player.getInventory().getItem(pkt.invSlot).copy();

                    player.getInventory().setItem(pkt.invSlot, fromStack);
                    stacksHandler.getStacks().setStackInSlot(curioSlot, toStack);

                    // mark changes to server sends updates
                    stacksHandler.update();
                    PacketDistributor.sendToPlayer(player, new SPacketSyncCurios(player.getId(), curiosItemHandler.getCurios()));
//                    player.containerMenu.broadcastChanges();
                });
            });
        });
    }

    public record MoveTrinketPayload(int curioSlot, int invSlot) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MoveTrinketPayload> TYPE = new CustomPacketPayload.Type<>(MOVE_TRINKET_PACKET);
        public static final StreamCodec<FriendlyByteBuf, MoveTrinketPayload> CODEC = new StreamCodec<>() {
            @Override
            public @NotNull MoveTrinketPayload decode(FriendlyByteBuf buf) {
                return new MoveTrinketPayload(buf.readInt(), buf.readInt());
            }

            @Override
            public void encode(FriendlyByteBuf buf, MoveTrinketPayload payload) {
                buf.writeInt(payload.curioSlot);
                buf.writeInt(payload.invSlot);
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

// Removed due to syncing issues
//    @SubscribeEvent
//    public void registerCapabilities(final RegisterCapabilitiesEvent evt) {
//        evt.registerItem(
//                CuriosCapability.ITEM,
//                (stack, context) -> new ICurio() {
//
//                    @Override
//                    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
//                        ICurio.super.onEquip(slotContext, prevStack);
//
//                        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
//                        curiosInventory.ifPresent(inv -> {
//
//                            IDynamicStackHandler tools = inv.getStacksHandler("tools").get().getStacks();
//                            int slots = tools.getSlots();
//
//                            int count = 0;
//                            for (int i = 0; i < slots; i++) {
//                                if (tools.getStackInSlot(i) != ItemStack.EMPTY) {
//                                    count++;
//                                }
//                            }
//
//                            if (count == slots) {
//                                UUID uuid = UUID.randomUUID();
//                                inv.addPermanentSlotModifier("tools", uuid, ATTRIBUTE_NAME, 1, AttributeModifier.Operation.ADD_VALUE);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
//                        ICurio.super.onUnequip(slotContext, newStack);
//
//                        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
//                        curiosInventory.ifPresent(inv -> {
//                            inv.getModifiers().get("tools").stream().filter(attributeModifier -> attributeModifier.name().equals(ATTRIBUTE_NAME)).findFirst().ifPresent(attributeModifier -> {
//                                inv.removeSlotModifier("tools", attributeModifier.id());
//                            });
//                        });
//                    }
//
//                    @Override
//                    public ItemStack getStack() {
//                        return stack;
//                    }
//
//                },
//                BuiltInRegistries.ITEM.stream().toArray(Item[]::new));
//    }


}
