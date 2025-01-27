package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PixmapIO;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.Assets;

import java.io.FileWriter;
import java.io.IOException;

public class SaveManager {
    public static void saveGame(int index) throws IOException {
        LevelScreen level = LevelScreen.getInstance();
        Gson gson = Assets.getInstance().gson;
        FileWriter writer = new FileWriter("saves/" + index + ".json");
        gson.toJson(level, writer);
        writer.close();
        if (PauseScreen.getInstance().getLastFrame() == null) {
            return;
        }
        PixmapIO.writePNG(Gdx.files.local(
            "saves/" + index + ".png"
        ), PauseScreen.getInstance().getLastFrame().getTextureData().consumePixmap());

    }
}
