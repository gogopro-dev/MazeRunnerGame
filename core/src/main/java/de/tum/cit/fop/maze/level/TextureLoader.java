package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.TexturePreprocessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton class, preprocessing and scaling textures, necessary
 */
public class TextureLoader {
    private boolean texturesLoaded = false;
    private final String texturePath;
    private final Random random;
    private final Map<String, List<TextureRegion>> textures;

    public TextureLoader(String texturePath, Random random) {
        this.texturePath = texturePath;
        this.random = random;
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
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(texturePath));
        TexturePreprocessor.processAtlas(atlas, Globals.SCALING_RATIO);
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
            return null;
        }
    }

    /**
     * Get a texture with a chance of variation
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @param defaultTileChance the chance {@code 0.0 - 1} of the default tile to be chosen
     * @return {@link TextureRegion} the texture
     */
    public TextureRegion getTextureWithVariationChance(String name, double defaultTileChance) {
        List<TextureRegion> textureRegions = textures.get(name);
        if (textureRegions == null) {
            throw new IllegalArgumentException("Texture not found: " + name);
        }
        defaultTileChance = Math.min(1, Math.max(0, defaultTileChance));
        ///  If there is only one texture, return it or if the random float is less than the default tile chance
        if (textureRegions.size() == 1 || random.nextDouble() < defaultTileChance) {
            return textureRegions.get(0);
        }
        return textureRegions.get(random.nextInt(textureRegions.size() - 1) + 1);
    }


    /**
     * Get a texture with a default chance of no variation 0.95
     * @param name the name of the texture (e.g. "floor", "wall", etc.)
     * @return {@link TextureRegion} the texture
     */
    public TextureRegion getTextureWithVariationChance(String name) {
        return getTextureWithVariationChance(name, 0.95);
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
