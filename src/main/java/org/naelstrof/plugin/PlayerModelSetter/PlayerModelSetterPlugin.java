package org.naelstrof.plugin.PlayerModelSetter;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class PlayerModelSetterPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static Path playerModelData;
    private static JsonElement playerData;

    public PlayerModelSetterPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        //getDataDirectory()
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        playerModelData = Paths.get(getDataDirectory().toString(), "playerModels.json");
        if (Files.exists(playerModelData)) {
            try {
                byte[] bytes = Files.readAllBytes(playerModelData);
                String text = new String(bytes, StandardCharsets.UTF_8);
                playerData = JsonParser.parseString(text);
            } catch (Exception ex) {
                LOGGER.atSevere().log(ex.toString());
                LOGGER.atSevere().log("Failed to load file " + playerModelData);
            }
        } else {
            playerData = new JsonObject();
        }
    }

    public static String GetModelForPlayer(Player p) {
        JsonObject obj = playerData.getAsJsonObject();
        if (obj.has(p.getDisplayName())) {
            return obj.get(p.getDisplayName()).getAsString();
        }
        return null;
    }

    public static void SetModelForPlayer(Player p, Ref<EntityStore> ref, String modelName) {
        JsonObject obj = playerData.getAsJsonObject();
        if (modelName == null) {
            if (obj.has(p.getDisplayName())) {
                obj.remove(p.getDisplayName());
            }
            return;
        }
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
        if (modelAsset == null) {
            throw new RuntimeException("Model not found for player " + p.getDisplayName() + ": " + modelName);
        }
        Model model = Model.createUnitScaleModel(modelAsset);
        ModelComponent playerModelComponent = new ModelComponent(model);
        ref.getStore().replaceComponent(ref, ModelComponent.getComponentType(), playerModelComponent);

        obj.addProperty(p.getDisplayName(), modelName);
        try {
            Files.createDirectories(playerModelData.getParent());
            Files.writeString(playerModelData, obj.toString(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.atSevere().log(ex.toString());
            LOGGER.atSevere().log("Failed to write file " + playerModelData);
        }

        //PersistentModel playerPersistentModel = new PersistentModel(model.toReference());
        //ref.getStore().replaceComponent(ref, PersistentModel.getComponentType(), playerPersistentModel);
    }

    public static void OnPlayerReady(PlayerReadyEvent e) {
        try {
            SetModelForPlayer(e.getPlayer(), e.getPlayerRef(), GetModelForPlayer(e.getPlayer()));
        } catch (Exception ex) {
            LOGGER.atSevere().log(ex.toString());
        }
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.getCommandRegistry().registerCommand(new SetMyModelCommand(this.getName(), this.getManifest().getVersion().toString()));
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerModelSetterPlugin::OnPlayerReady);
    }
}