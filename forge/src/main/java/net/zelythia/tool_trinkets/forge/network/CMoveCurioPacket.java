package net.zelythia.tool_trinkets.forge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;

import java.util.function.Supplier;

public class CMoveCurioPacket {

    private final int curioSlot;
    private final int invSlot;

    public CMoveCurioPacket(int curioSlot, int invSlot) {
//        System.out.println("New Packet: Curio: " + curioSlot + ", InvSlot: " + invSlot);
        this.curioSlot = curioSlot;
        this.invSlot = invSlot;
    }

    public static void encode(CMoveCurioPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.curioSlot);
        buf.writeInt(pkt.invSlot);
    }

    public static CMoveCurioPacket decode(FriendlyByteBuf buf) {
        int from = buf.readInt();
        int to = buf.readInt();
        return new CMoveCurioPacket(from, to);
    }

    public static void handle(CMoveCurioPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;
            ServerPlayer player = ctx.getSender();

            // Bounds check
            if (pkt.curioSlot < 0 || pkt.invSlot < 0) return;
            if (pkt.invSlot >= player.containerMenu.slots.size()) return;

            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(curiosItemHandler -> {
                int curioSlot = pkt.curioSlot;

                ICurioStacksHandler stacksHandler = curiosItemHandler.getStacksHandler("tools").orElseGet(null);
                if(stacksHandler == null) return;


                ItemStack fromStack = stacksHandler.getStacks().getStackInSlot(curioSlot).copy();
                ItemStack toStack = player.getInventory().getItem(pkt.invSlot).copy();

                player.getInventory().setItem(pkt.invSlot, fromStack);
                stacksHandler.getStacks().setStackInSlot(curioSlot, toStack);

                // mark changes so server sends updates
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SPacketSyncCurios(player.getId(), curiosItemHandler.getCurios()));
                player.containerMenu.broadcastChanges();
            });

        });
        ctx.setPacketHandled(true);
    }


}
