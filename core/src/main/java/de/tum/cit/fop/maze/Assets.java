package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.gson.RuntimeTypeAdapterFactory;
import de.tum.cit.fop.maze.level.TileMap;

import java.util.ArrayList;

/**
 * Singleton class, responsible for loading all assets and initializing the game
 */
public final class Assets {
    private static Assets instance;
    private final AssetManager assetManager;
    private final ArrayList<CollectableAttributes> treasurePool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> shopPool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> lootContainerPool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> collectables = new ArrayList<>();
    private final ArrayList<Trap.TrapAttributes> traps = new ArrayList<>();
    private final ArrayList<Enemy.EnemyConfig> enemies = new ArrayList<>();
    public final TileTextureHelper tileTextureHelper;
    public final Gson gson;

    /**
     * @return the singleton instance of the {@link Assets} class
     */
    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }

    /**
     * Private constructor to prevent instantiation from outside the class</br>
     * This constructor initializes the {@link Gson} object and the {@link AssetManager} object.
     * Registers the {@link TileEntity} subclasses with the {@link RuntimeTypeAdapterFactory}.
     * Initializes the {@link TileTextureHelper} object.
     * Initializes the {@link AssetManager} object.
     */
    private Assets() {

        if (instance != null) {
            throw new IllegalStateException("Assets is a singleton class");
        }
        GsonBuilder gsonBuilder = new GsonBuilder();

        final RuntimeTypeAdapterFactory<TileEntity> tileEntities =
            RuntimeTypeAdapterFactory.of(TileEntity.class, "classTypeName")
                .registerSubtype(Collectable.class, "Collectable")
                .registerSubtype(Trap.class, "Trap")
                .registerSubtype(Torch.class, "Torch")
                .registerSubtype(LootContainer.class, "LootContainer")
                .registerSubtype(ExitDoor.class, "ExitDoor")
                .registerSubtype(ShopItem.class, "ShopItem")
                .registerSubtype(ThrowableCollectable.class, "throwableCollectable");
        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.registerTypeAdapterFactory(tileEntities);
        gsonBuilder.registerTypeAdapter(
            TiledMap.class,
            new TileMap.TiledMapAdapter()
        );
        instance = this;
        tileTextureHelper = new TileTextureHelper("assets/tiles/tiles.atlas");
        assetManager = new AssetManager(new LocalFileHandleResolver());
        gson = gsonBuilder.create();
    }

    /**
     * Queues the loading of the assets
     */
    private void queueLoading() {
        assetManager.load("assets/menu/menu.atlas", TextureAtlas.class);
        assetManager.load("assets/menu/menu_icons.atlas", TextureAtlas.class);
        assetManager.load("assets/anim/player/character.atlas", TextureAtlas.class);
        assetManager.load("assets/anim/tileEntities/tile_entities.atlas", TextureAtlas.class);
        assetManager.load("assets/temporary/collectables/collectables.atlas", TextureAtlas.class);
    }

    /**
     * Loads the collectables from the collectables.json file
     * and adds them to the corresponding pools
     */
    private void loadCollectables() {
        for (CollectableAttributes attribute : gson.fromJson(
            Gdx.files.local("assets/configs/collectables.json").readString(),
            CollectableAttributes[].class
        )) {
            collectables.add(attribute);
            if (attribute.treasurePool) {
                treasurePool.add(attribute);
            }
            if (attribute.shopPool) {
                shopPool.add(attribute);
            }
            if (attribute.lootContainerPool) {
                lootContainerPool.add(attribute);
            }
        }
    }

    /**
     * Loads all assets blocking
     * and initializes the collectables
     */
    public void loadAllBlocking() {
        tileTextureHelper.loadTextures();
        queueLoading();
        assetManager.finishLoading();
        loadCollectables();

    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public ArrayList<CollectableAttributes> getTreasurePool() {
        return treasurePool;
    }

    public ArrayList<CollectableAttributes> getShopPool() {
        return shopPool;
    }

    public ArrayList<CollectableAttributes> getLootContainerPool() {
        return lootContainerPool;
    }

    public ArrayList<CollectableAttributes> getCollectables() {
        return collectables;
    }

    public ArrayList<Trap.TrapAttributes> getTraps() {
        return traps;
    }

    public ArrayList<Enemy.EnemyConfig> getEnemies() {
        return enemies;
    }
}
