package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PixmapIO;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Watchable;

/**
 * Singleton class for saving the game state and settings to a .json file
 * using the Gson library
 * @see LevelScreen
 * @see PauseScreen
 * @see SettingsConfiguration
 * @see Assets
 * @see Gson
 */
public class SaveManager {

    /**
     * Ensures that the {@code saves} directory exists in the local file system.
     * If the directory does not exist, it creates it.
     * This method is used to guarantee that the necessary folder structure
     * is available for saving game data / configuration files.
     */
    private static void makeSavesDir() {
        if (!Gdx.files.local("saves").exists()) {
            Gdx.files.local("saves").mkdirs();
        }
    }


    /**
     * Save the current game state to a .json file and last frame to a .png file
     * @param index index of the save file
     * @throws IOException if the file cannot be written
     */
    public static void saveGame(int index) throws IOException {
        makeSavesDir();
        LevelScreen level = LevelScreen.getInstance();
        Gson gson = Assets.getInstance().gson;
        FileWriter writer = new FileWriter("saves/" + index + ".json");
        gson.toJson(level, writer);
        writer.close();
        if (PauseScreen.getInstance().getLastFrame() == null) {
            return;
        }

        /// Save the last frame to a .png file
        PixmapIO.writePNG(Gdx.files.local(
            "saves/" + index + ".png"
        ), PauseScreen.getInstance().getLastFrame().getTextureData().consumePixmap());

        /// Save the level data to a .json file
        writer = new FileWriter("saves/levelData_" + index + ".json");
        gson.toJson(level.getLevelData(), writer);
        writer.close();
    }

    /**
     * Save the current settings to a .json file
     * @throws IOException if the file cannot be written
     */
    public static void saveConfigurations() throws IOException{
        makeSavesDir();
        SettingsConfiguration settingsConfiguration = SettingsConfiguration.getInstance();
        Gson gson = Assets.getInstance().gson;
        FileWriter writer = new FileWriter("saves/settings.json");
        gson.toJson(settingsConfiguration, writer);
        writer.close();
    }

}
