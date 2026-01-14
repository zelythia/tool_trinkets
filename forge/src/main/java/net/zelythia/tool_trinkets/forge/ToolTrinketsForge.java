package net.zelythia.tool_trinkets.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.tool_trinkets.ToolTrinkets;
import net.zelythia.tool_trinkets.forge.network.Networking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.SlotTypeMessage;

@Mod(ToolTrinkets.MOD_ID)
public final class ToolTrinketsForge {


    public static final TagKey<Item> TOOLS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "tools"));
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTrinketsForge.class);


    public ToolTrinketsForge() {
        ToolTrinkets.init();

        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();

        context.getModEventBus().addListener(this::onTextureStitch);

        MinecraftForge.EVENT_BUS.register(this);

        Networking.register();

        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> {
            return new SlotTypeMessage.Builder("tools")
                    .icon(new ResourceLocation(ToolTrinkets.MOD_ID, "slot/tool"))
                    .size(9)
                    .build();
        });
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.addSprite(new ResourceLocation(ToolTrinkets.MOD_ID, "slot/tool"));
    }

}
