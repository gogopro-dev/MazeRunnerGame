package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.JsonWriter;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.Assets;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.Player;

import java.io.IOException;

public class SaveManager {
    public static void saveGame() throws IOException {
        LevelScreen level = LevelScreen.getInstance();
        Gson gson = Assets.getInstance().gson;
        JsonWriter writer = new JsonWriter(Gdx.files.local("save0.json").writer(false));
        writer.json(gson.toJson(level));
        writer.close();
        // TODO: Save the player's last frame
        /*PixmapIO.writePNG(Gdx.files.local("save0.png"),
            PauseScreen.getInstance().getLastFrame().getTextureData().consumePixmap()
        );*/
        //Enemy e = gson.fromJson(gson.toJson(level.enemyManager.getEnemies().get(0)), Enemy.class);
        return;
    }
}
