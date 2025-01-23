package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.gson.RuntimeTypeAdapterFactory;

import java.util.ArrayList;

public final class Assets {
    private static Assets instance;
    private final AssetManager assetManager;
    private final ArrayList<CollectableAttributes> treasurePool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> shopPool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> lootContainerPool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> collectables = new ArrayList<>();
    private final ArrayList<Trap.TrapAttributes> traps = new ArrayList<>();
    private final ArrayList<Enemy.EnemyConfig> enemies = new ArrayList<>();
    public final Gson gson;

    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }

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
                .registerSubtype(ExitDoor.class, "Enemy");
        gsonBuilder.registerTypeAdapterFactory(tileEntities);
        instance = this;
        assetManager = new AssetManager(new LocalFileHandleResolver());
        gson = gsonBuilder.create();
    }

    private void registerJsonAdapters() {

    }

    private void queueLoading() {
        assetManager.load("assets/menu/menu.atlas", TextureAtlas.class);
        assetManager.load("assets/menu/menu_icons.atlas", TextureAtlas.class);
        assetManager.load("assets/temporary/collectables/collectables.atlas", TextureAtlas.class);
    }

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

    public void loadAllBlocking() {
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
