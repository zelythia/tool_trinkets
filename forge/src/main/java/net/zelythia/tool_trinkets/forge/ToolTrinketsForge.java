package net.zelythia.tool_trinkets.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.tool_trinkets.ToolTrinkets;
import net.zelythia.tool_trinkets.forge.network.Networking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ToolTrinkets.MOD_ID)
public final class ToolTrinketsForge {


    public static final TagKey<Item> TOOLS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "tools"));
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTrinketsForge.class);


    public ToolTrinketsForge(FMLJavaModLoadingContext context) {
        ToolTrinkets.init();

        context.getModEventBus().addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        Networking.register();
    }

    public void clientSetup(final FMLCommonSetupEvent event) {

    }

}
