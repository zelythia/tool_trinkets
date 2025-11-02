package net.zelythia.tool_trinkets.forge;

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
