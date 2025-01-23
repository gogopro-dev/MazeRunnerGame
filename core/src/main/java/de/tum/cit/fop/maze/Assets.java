package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.gson.Gson;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.entities.tile.CollectableAttributes;
import de.tum.cit.fop.maze.entities.tile.Trap;

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
    public static final Gson gson = new Gson();

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
        instance = this;
        assetManager = new AssetManager(new LocalFileHandleResolver());
    }

    private void queueLoading() {
        assetManager.load("assets/menu/menu.atlas", TextureAtlas.class);
        assetManager.load("assets/menu/menu_icons.atlas", TextureAtlas.class);
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
