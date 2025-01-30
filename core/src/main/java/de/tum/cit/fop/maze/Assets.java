package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.essentials.SettingsConfiguration;
import de.tum.cit.fop.maze.gson.RuntimeTypeAdapterFactory;
import de.tum.cit.fop.maze.level.TileMap;
import games.rednblack.miniaudio.MAGroup;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;
import games.rednblack.miniaudio.loader.MASoundLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Singleton class, responsible for loading all assets and initializing the game
 */
public final class Assets implements Disposable {
    private static Assets instance;
    private final AssetManager assetManager;
    private final ArrayList<CollectableAttributes> treasurePool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> shopPool = new ArrayList<>();
    private final ArrayList<CollectableAttributes> lootContainerPool = new ArrayList<>();
    private final ArrayList<LootContainer.LootContainerAttributes> lootContainerConfig = new ArrayList<>();
    private final ArrayList<CollectableAttributes> collectables = new ArrayList<>();
    private final ArrayList<Trap.TrapAttributes> traps = new ArrayList<>();
    private final ArrayList<Enemy.EnemyConfig> enemies = new ArrayList<>();
    private final ArrayList<ActiveItem.ActiveItemProperties> activeItems = new ArrayList<>();
    public final TileTextureHelper tileTextureHelper;
    public final Gson gson;
    private HashMap<String, String> soundMap;
    public final MAGroup music;
    public final MAGroup sfx;
    public final MiniAudio soundEngine;
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
                .registerSubtype(Projectile.class, "Projectile");

        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.registerTypeAdapterFactory(tileEntities);
        gsonBuilder.registerTypeAdapter(
            TiledMap.class,
            new TileMap.TiledMapAdapter()
        );
        assetManager = new AssetManager(new LocalFileHandleResolver());
        soundEngine = LoadMenu.getInstance().getSoundEngine();
        assetManager.setLoader(
            MASound.class, new MASoundLoader(soundEngine,
                assetManager.getFileHandleResolver())
        );
        soundMap = new HashMap<>();
        instance = this;
        tileTextureHelper = new TileTextureHelper("assets/tiles/tiles.atlas");
        music = soundEngine.createGroup();
        music.setSpatialization(false);
        sfx = soundEngine.createGroup();
        music.setVolume(SettingsConfiguration.getInstance().getMusicVolume());
        sfx.setVolume(SettingsConfiguration.getInstance().getSfxVolume());

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
        assetManager.load("assets/collectables/collectables.atlas", TextureAtlas.class);
        assetManager.load("assets/anim/activeItems/activeItems.atlas", TextureAtlas.class);
        assetManager.load("assets/hud/hud.atlas", TextureAtlas.class);


        // Walk assets sounds recursively
        try (Stream<Path> stream = Files.walk(Paths.get(Gdx.files.local("assets/sounds").path()))) {
            stream.filter(Files::isRegularFile)
                .forEach(it -> {
                    assetManager.load(it.toString(), MASound.class);
                    soundMap.put(
                        Arrays.stream(it.getFileName().toString().split("\\."))
                            .findFirst().orElseThrow(), it.toString()
                    );
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Loads the collectables from the collectables.json file
     * and adds them to the corresponding pools
     */
    private void loadJson() {
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
        enemies.addAll(
            List.of(gson.fromJson(Gdx.files.local("assets/configs/enemyConfig.json").readString(),
                Enemy.EnemyConfig[].class
            ))
        );
        activeItems.addAll(
            List.of(gson.fromJson(Gdx.files.local("assets/configs/activeItems.json").readString(),
                ActiveItem.ActiveItemProperties[].class)
            )
        );

        traps.addAll(
            List.of(gson.fromJson(Gdx.files.local("assets/configs/trapConfig.json").readString(),
                Trap.TrapAttributes[].class)
        ));

        lootContainerConfig.addAll(
            List.of(gson.fromJson(Gdx.files.local("assets/configs/lootContainers.json").readString(),
                LootContainer.LootContainerAttributes[].class)
            )
        );

    }

    /**
     * Loads all assets blocking
     * and initializes the collectables
     */
    public void loadAllBlocking() {
        tileTextureHelper.loadTextures();
        queueLoading();
        assetManager.finishLoading();
        loadJson();

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

    public ArrayList<LootContainer.LootContainerAttributes> getLootContainerConfig() {
        return lootContainerConfig;
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

    public ArrayList<ActiveItem.ActiveItemProperties> getActiveItems() {
        return activeItems;
    }

    public MASound getSound(String name) {
        if (soundMap.containsKey(name)) {
            MASound sound = assetManager.get(soundMap.get(name), MASound.class);

            if (soundMap.get(name).contains("music")) {
                sound.setSpatialization(false);
                music.attachToThisNode(sound, 0);
            } else {
                sound.setLooping(false);
                sfx.attachToThisNode(sound, 0);
            }
            return sound;
        } else {
            throw new RuntimeException("Sound " + name + " not found");
        }
    }

    @Override
    public void dispose() {
        this.assetManager.dispose();
    }
}
