package org.naelstrof.plugin.PlayerModelSetter;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class SetMyModelCommand extends CommandBase {

    private final String pluginName;
    private final String pluginVersion;

    private final RequiredArg<String> modelString = this.withRequiredArg("model", "What model you want to swap to, sexy? Put \"None\" to reset.", ArgTypes.STRING);

    public SetMyModelCommand(String pluginName, String pluginVersion) {
        super("mymodel", "Sets your player model without op!");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        String model = modelString.get(ctx);
        if (!ctx.isPlayer()) {
            ctx.sendMessage(Message.raw("Can only be executed as a player, sorry!"));
            return;
        }

        Player player = ctx.senderAs(Player.class);
        assert player.getWorld() != null;
        player.getWorld().execute(() -> {
            try {
                var ref = ctx.senderAsPlayerRef();
                if (Objects.equals(model, "None")) {
                    PlayerModelSetterPlugin.SetModelForPlayer(player, ref, null);
                    ctx.sendMessage(Message.raw("Cleared model from registry! Rejoin to refresh your model."));
                } else {
                    PlayerModelSetterPlugin.SetModelForPlayer(player, ref, model);
                    ctx.sendMessage(Message.raw("Successfully set your player model to " + model + "! :3"));
                }
            } catch (Exception ex) {
                ctx.sendMessage(Message.raw("Failed to set your model, maybe it wasn't found?"));
            }
        });
    }
}