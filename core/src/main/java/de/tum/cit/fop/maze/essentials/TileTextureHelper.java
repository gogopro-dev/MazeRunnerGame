package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

/**
 * Singleton class, preprocessing and scaling textures, necessary
 */
public class TileTextureHelper {
    private boolean texturesLoaded = false;
    private final String texturePath;
    private final Map<String, List<TextureRegion>> textures;

    public record TextureResult(TextureRegion textureRegion, int index) {
    }

    public record TextureWithIndex(String texture, int index) {
    }

    public TileTextureHelper(String texturePath) {
        this.texturePath = texturePath;
        this.textures = new HashMap<>();
        loadTextures();
    }

    /**
     * Load all textures from the texture atlas, preprocess them and caches the result
     */
    public void loadTextures() {
        // Load textures here
        if (texturesLoaded) {
            return;
        }
        TextureAtlas atlas = new TextureAtlas(Gdx.files.local(texturePath));
        atlas.getRegions().forEach(region -> {
            var val = textures.putIfAbsent(region.name, new ArrayList<>(List.of(region)));
            if (val != null) {
                while (val.size() <= region.index){
                    val.add(null);
                }
                val.set(region.index, region);
            }
        });
        texturesLoaded = true;
    }

    /**
     * Get a texture by name and index
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @param index the index of the texture
     * @return {@link TextureRegion} the texture
     */
    public TextureRegion getTexture(String name, int index) {
        try {
            return textures.get(name).get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Texture not found: " + name + " index: " + index);
        }
    }

    /**
     * Get a texture with a chance of variation
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @param defaultTileChance the chance {@code 0.0 - 1} of the default tile to be chosen
     * @return {@link TextureRegion} the texture
     */
    public TextureResult getTextureWithVariationChance(String name, double defaultTileChance, Random random) {
        List<TextureRegion> textureRegions = textures.get(name);
        if (textureRegions == null) {
            throw new IllegalArgumentException("Texture not found: " + name);
        }
        defaultTileChance = Math.min(1, Math.max(0, defaultTileChance));
        ///  If there is only one texture, return it or if the random float is less than the default tile chance
        if (textureRegions.size() == 1 || random.nextDouble() < defaultTileChance) {
            return new TextureResult(
                    textureRegions.get(0),
                    0
            );
        }
        int index = random.nextInt(textureRegions.size() - 1) + 1;
        return new TextureResult(textureRegions.get(index), index);
    }


    /**
     * Get a texture with a default chance of no variation 0.95
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @return {@link TextureRegion} the texture
     */
    public TextureResult getTextureWithVariationChance(String name, Random random) {
        return getTextureWithVariationChance(name, 0.7, random);
    }

    /**
     * Get a texture by name
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @return {@link TextureRegion} the texture
     */
    public TextureRegion getTexture(String name) {
        try {
            return textures.get(name).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }


    public boolean isTexturesLoaded() {
        return texturesLoaded;
    }

    public List<TextureRegion> getTextures(String name) {
        return textures.get(name);
    }
}
