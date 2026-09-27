package net.reversteam.desecratedcompat.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.reversteam.desecratedcompat.network.ModNetwork;
import net.reversteam.desecratedcompat.network.OpenDialoguePacket;

@Mod.EventBusSubscriber
public class ServerDialogueCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("dia_dialogue")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(ServerDialogueCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        
        ModNetwork.INSTANCE.sendTo(
            new OpenDialoguePacket("dia", "This realm is not supposed to exist. This timeline was not planned. What trickery is this?"),
            player.connection.connection,
            NetworkDirection.PLAY_TO_CLIENT
        );
        
        return Command.SINGLE_SUCCESS;
    }
}