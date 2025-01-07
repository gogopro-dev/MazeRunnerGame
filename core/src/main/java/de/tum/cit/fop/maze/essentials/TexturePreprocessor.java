package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.Globals;

/**
 * Preprocesses textures before rendering them, for example scaling them to a desired size
 */
public class TexturePreprocessor {


    /**
     * Scales a texture to the desired size using integer scaling using nearest neighbor interpolation
     * @param textureToScale the texture to scale
     * @param factor the factor to scale the texture by
     * @return {@link Texture} – the scaled texture
     */
    public static Texture integerScaling(TextureRegion textureToScale, int factor) {
        TextureData data = textureToScale.getTexture().getTextureData();
        int textureWidth = textureToScale.getRegionWidth();
        int textureHeight = textureToScale.getRegionHeight();
        if (!data.isPrepared()) {
            data.prepare();
        }
        Pixmap source = data.consumePixmap();
        Pixmap scaled = new Pixmap(
            textureWidth * factor,
            textureHeight * factor,
            source.getFormat()
        );
        for (int x = 0; x < scaled.getHeight(); x++) {
            for (int y = 0; y < scaled.getWidth(); y++) {
                scaled.drawPixel(
                    x, y,
                    source.getPixel(
                        x / factor + textureToScale.getRegionX(),
                        y / factor + textureToScale.getRegionY()
                    )
                );
            }
        }
        Texture scaledTexture = new Texture(scaled);

        /// Free resources
        if(!source.isDisposed()) source.dispose();
        if(!scaled.isDisposed()) scaled.dispose();

        return scaledTexture;
    }

    /**
     * Scales a texture to the desired size using integer scaling using nearest neighbor interpolation
     * @param atlas the texture to scale
     * @param factor the factor to scale the texture by
     */
    public static void processAtlas(TextureAtlas atlas, int factor) {
        for (int i = 0; i < atlas.getRegions().size; i++) {
            TextureAtlas.AtlasRegion region = atlas.getRegions().get(i);
            region.setRegion(integerScaling(region, factor));
        }
    }

    /**
     * Processes every texture in the atlas, it is usable right away afterward as usual
     * @param atlas - the atlas to process
     */
    public static void processAtlas(TextureAtlas atlas) {
        for (int i = 0; i < atlas.getRegions().size; i++) {
            TextureAtlas.AtlasRegion region = atlas.getRegions().get(i);
            region.setRegion(integerScaling(region, Globals.SCALING_RATIO));
        }
    }
}
