package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class commonFunctions {
    public static Drawable getColoredDrawable(int width, int height, Color color) {
        // create simple font for Stamina Bar
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        return drawable;
    }
    public static Texture getColoredTexture(int width, int height, Color color) {
        // create simple font for Stamina Bar
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }
    public static Drawable turnPixmapToDrawable(Pixmap pixmap) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        return drawable;
    }

    public static Texture turnPixmapToTexture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        return texture;
    }
}
