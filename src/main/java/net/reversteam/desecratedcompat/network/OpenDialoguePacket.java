package net.reversteam.desecratedcompat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenDialoguePacket {
    private final String npcId;
    private final String line;

    public OpenDialoguePacket(String npcId, String line) {
        this.npcId = npcId;
        this.line = line;
    }

    public static void encode(OpenDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.npcId, 32767);
        buf.writeUtf(msg.line, 32767);
    }

    public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
        return new OpenDialoguePacket(buf.readUtf(32767), buf.readUtf(32767));
    }

    public static void handle(OpenDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.reverseeon.mod.client.screen.DialogueScreen.open(msg.npcId, msg.line, 0, 0, 0, 0, 0);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}