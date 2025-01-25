package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.JsonWriter;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.Player;

import java.io.FileWriter;
import java.io.IOException;

public class SaveManager {
    private static boolean saved = false;
    public static void saveGame() throws IOException {
        if (saved) {
            return;
        }
        saved = true;
        LevelScreen level = LevelScreen.getInstance();
        Gson gson = Assets.getInstance().gson;
        FileWriter writer = new FileWriter("level.json");
        gson.toJson(level, writer);
        writer.close();
        // TODO: Save the player's last frame
        /*PixmapIO.writePNG(Gdx.files.local("save0.png"),
            PauseScreen.getInstance().getLastFrame().getTextureData().consumePixmap()
        );*/
        //Enemy e = gson.fromJson(gson.toJson(level.enemyManager.getEnemies().get(0)), Enemy.class);
        return;
    }
}
