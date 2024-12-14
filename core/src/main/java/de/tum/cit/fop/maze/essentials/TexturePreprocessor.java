package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.Globals;

/**
 * Preprocesses textures before rendering them
 */
public class TexturePreprocessor {


    /**
     * Scales a texture to the desired size using integer scaling using nearest neighbor interpolation
     * @param textureToScale
     * @return
     */
    public static Texture integerScaling(TextureRegion textureToScale, int factor) {
        TextureData data = textureToScale.getTexture().getTextureData();
        int textureWidth = textureToScale.getRegionWidth();
        int textureHeight = textureToScale.getRegionHeight();
        if (!data.isPrepared()) {
            data.prepare();
        }
        Pixmap pixmap = data.consumePixmap();
        Pixmap scaled = new Pixmap(
            textureWidth * factor,
            textureHeight * factor,
            pixmap.getFormat()
        );
        for (int x = 0; x < scaled.getHeight(); x++) {
            for (int y = 0; y < scaled.getWidth(); y++) {
                scaled.drawPixel(
                    x, y,
                    pixmap.getPixel(
                        (x) / factor + textureToScale.getRegionX(),
                        y / factor + textureToScale.getRegionY()
                    )
                );
            }
        }
        return new Texture(scaled);
    }

    public static void processAtlas(TextureAtlas atlas, int factor) {
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            region.setRegion(integerScaling(region, factor));
        }
    }

    public static void processAtlas(TextureAtlas atlas) {
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            region.setRegion(integerScaling(region, Globals.SCALING_RATIO));
        }
    }
}
