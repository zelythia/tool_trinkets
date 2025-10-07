package net.zelythia.tool_trinkets.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.tool_trinkets.ToolTrinkets;
import net.zelythia.tool_trinkets.forge.network.Networking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.UUID;

@Mod(ToolTrinkets.MOD_ID)
public final class ToolTrinketsForge {


    public static final TagKey<Item> TOOLS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "tools"));
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTrinketsForge.class);

    private static final String ATTRIBUTE_NAME = "tool_trinkets_add_slot";

    public ToolTrinketsForge(FMLJavaModLoadingContext context) {
        ToolTrinkets.init();

        context.getModEventBus().addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        Networking.register();
    }

    public void clientSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();
        Item item = stack.getItem();
        if (stack.is(TOOLS)) {
            evt.addCapability(CuriosCapability.ID_ITEM, CuriosApi.createCurioProvider(new ICurio() {

                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                    ICurio.super.onEquip(slotContext, prevStack);

                    LazyOptional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
                    curiosInventory.ifPresent(inv -> {
                        int slots = inv.getStacksHandler("tools").get().getSlots();

                        IDynamicStackHandler tools = inv.getStacksHandler("tools").get().getStacks();

                        int count = 0;
                        for (int i = 0; i < slots; i++) {
                            if (tools.getStackInSlot(i) != ItemStack.EMPTY) {
                                count++;
                            }
                        }

                        if (count == slots) {
                            UUID uuid = UUID.randomUUID();
                            inv.addPermanentSlotModifier("tools", uuid, ATTRIBUTE_NAME, 1, AttributeModifier.Operation.ADDITION);
                        }
                    });
                }


                @Override
                public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                    ICurio.super.onUnequip(slotContext, newStack);

                    LazyOptional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
                    curiosInventory.ifPresent(inv -> {
                        inv.getModifiers().get("tools").stream().filter(attributeModifier -> attributeModifier.getName().equals(ATTRIBUTE_NAME)).findFirst().ifPresent(attributeModifier -> {
                            inv.removeSlotModifier("tools", attributeModifier.getId());
                        });
                    });
                }
            }));
        }

    }

}
