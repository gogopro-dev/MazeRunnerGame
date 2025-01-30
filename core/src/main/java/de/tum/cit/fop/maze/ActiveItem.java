package de.tum.cit.fop.maze;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import de.tum.cit.fop.maze.entities.Attackable;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.entities.tile.Projectile;
import de.tum.cit.fop.maze.essentials.*;
import de.tum.cit.fop.maze.gson.GSONPostRestorable;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.ArrayList;

/**
 * Represents an active item that can be used during gameplay.
 * This class handles the lifecycle of items such as their usage, animations,
 * sounds, and interactions with the game world.
 * It implements the GSONPostRestorable interface to allow restoration
 * of its state after deserialization.
 */
public class ActiveItem implements GSONPostRestorable {
    private final Collectable collectable;
    private final ActiveItemProperties properties;
    private final ArrayList<UseRecord> uses = new ArrayList<>();
    private transient Animation<TextureRegion> projectileFlyingAnimation;
    private transient Animation<TextureRegion> projectileDestroyedAnimation;
    private transient MASound soundUse;
    private transient MASound soundDestroy;

    /**
     * Constructs an ActiveItem object based on the specified ActiveItemType.
     * Retrieves the corresponding properties of the active item from the Assets singleton
     * and initializes its associated Collectable.
     *
     * @param itemType The type of the active item to be created. Must be of type {@link ActiveItemType}.
     *                 This is used to filter and retrieve the matching active item properties
     *                */
    public ActiveItem(ActiveItemType itemType) {
        this.properties = Assets.getInstance().getActiveItems().stream().filter(
            it -> it.type == itemType
        ).findFirst().orElseThrow();
        this.collectable = new Collectable(properties.associatedCollectable);
        loadAnimations();
    }

    /**
     * Loads the necessary animations and sounds for the projectile associated with the active item.
     * This method initializes the flying and destroyed animations for the projectile using texture regions
     * retrieved from the texture atlas. Additionally, it loads the corresponding sounds, if specified in the
     * active item's properties.
     */
    private void loadAnimations() {
        TextureAtlas textureAtlas = Assets.getInstance().getAssetManager()
            .get("assets/anim/activeItems/activeItems.atlas", TextureAtlas.class);
        projectileFlyingAnimation = new Animation<>(
            properties.projectileFlyingAnimationDuration,
            textureAtlas.findRegions(properties.texture + "_flying"),
            Animation.PlayMode.LOOP
        );
        projectileDestroyedAnimation = new Animation<>(
            properties.projectileDestroyedAnimationDuration,
            textureAtlas.findRegions(properties.texture + "_destroyed"),
            Animation.PlayMode.NORMAL
        );
        if (properties.soundUse != null) {
            soundUse = Assets.getInstance().getSound(properties.soundUse);
            soundUse.setSpatialization(false);
        }
        if (properties.soundDestroy != null) {
            soundDestroy = Assets.getInstance().getSound(properties.soundDestroy);
        }
    }

    @Override
    public void restore() {
        collectable.restore();
        for (UseRecord record : uses) {
            record.projectile.restore();
            LevelScreen.getInstance().tileEntityManager.createTileEntity(
                record.projectile, record.projectile != null ? record.projectile.getSavedPosition() :
                    LevelScreen.getInstance().player.getPosition());
        }
        loadAnimations();
    }

    /**
     * Uses the active item
     */
    public void use() {
        var temp = new UseRecord();
        temp.isLeft = !LevelScreen.getInstance().player.isFacingRight();
        uses.add(temp);
        if (soundUse != null) {
            soundUse.stop();
            soundUse.setLooping(false);
            soundUse.play();
        }
    }

    /**
     * Tick the ActiveItem's logic
     * @param delta elapsed time
     */
    public void tick(float delta) {
        SpriteBatch batch = LevelScreen.getInstance().batch;
        Player player = LevelScreen.getInstance().player;
        ArrayList<UseRecord> deletions = new ArrayList<>();
        for (UseRecord record : uses) {
            record.useTime += delta;
            /// If no projectile has ben shot
            if (record.projectile == null) {
                record.projectile = new Projectile(
                    1, 1, this.properties.projectileSpeed, !record.isLeft
                );
                LevelScreen.getInstance().tileEntityManager.createTileEntity(record.projectile, player.getPosition());

            ///  If projectile has hit something
            } else if (!record.projectile.isFoundHit()) {
                record.flightTime += delta;
                AbsolutePoint position = record.projectile.getPosition();
                TextureRegion region = projectileFlyingAnimation.getKeyFrame(record.flightTime);
                if (record.isLeft) region.flip(true, false);
                batch.draw(
                    region,
                    position.x() - this.properties.projectileWidthCells * Globals.CELL_SIZE_METERS / 2f,
                    position.y() - this.properties.projectileHeightCells * Globals.CELL_SIZE_METERS / 2f,
                    this.properties.projectileWidthCells * Globals.CELL_SIZE_METERS,
                    this.properties.projectileHeightCells * Globals.CELL_SIZE_METERS
                );
                if (record.isLeft) region.flip(true, false);
            }
            ///  If projectile is flying
            else {
                AbsolutePoint position = record.projectile.getPosition();
                record.destroyTime += delta;
                TextureRegion region = projectileDestroyedAnimation.getKeyFrame(record.destroyTime);
                if (record.isLeft) region.flip(true, false);
                batch.draw(
                    region,
                    position.x() - this.properties.projectileWidthCells * Globals.CELL_SIZE_METERS / 2f,
                    position.y() - this.properties.projectileHeightCells * Globals.CELL_SIZE_METERS / 2f,
                    this.properties.projectileWidthCells * Globals.CELL_SIZE_METERS,
                    this.properties.projectileHeightCells * Globals.CELL_SIZE_METERS
                );
                if (record.isLeft) region.flip(true, false);
                if (projectileDestroyedAnimation.isAnimationFinished(record.destroyTime)) {
                    deletions.add(record);
                    record.light.remove();
                    record.projectile.toDestroy = true;
                    record.projectile = null;
                }
            }
            /// Process light animation
            if (record.light == null) {
                AbsolutePoint position = record.projectile != null ? record.projectile.getPosition() :
                    player.getPosition();
                record.light = new PointLight(
                    LevelScreen.getInstance().rayHandler, Globals.RAY_AMOUNT, Globals.TORCH_LIGHT_COLOR, 0,
                    position.x(), position.y()
                );
                Filter filter = new Filter();
                filter.categoryBits = BodyBits.LIGHT;
                filter.maskBits = BodyBits.LIGHT_MASK;
                record.light.setContactFilter(
                    filter
                );
                record.light.setSoft(true);
                record.light.setActive(true);
                record.light.attachToBody(player.getBody());
            }
            if (record.projectile != null && !record.lightAttachedToBody) {
                record.light.attachToBody(record.projectile.getBody());
                record.lightAttachedToBody = true;
            }
            if (record.light != null) {
                if (record.projectile != null && record.projectile.isFoundHit()) {
                    record.lightDecay += delta;
                    record.light.setDistance(15 - 15 * Utils.easeOutCirc(record.lightDecay / 2));
                    if (!record.hasDamaged) {
                        record.hasDamaged = true;
                        AbsolutePoint position = record.projectile.getPosition();
                        float posX1 = position.x() - this.properties.projectileBlastRadiusCells;
                        float posY1 = position.y() - this.properties.projectileBlastRadiusCells;
                        float posX2 = position.x() + this.properties.projectileBlastRadiusCells;
                        float posY2 = position.y() + this.properties.projectileBlastRadiusCells;
                        LevelScreen.getInstance().world.QueryAABB(
                            (fixture) -> {
                                Body body = fixture.getBody();
                                if (body != null && body.getUserData() instanceof Attackable attackable) {
                                    if (body.getUserData() instanceof Player) return true;
                                    attackable.takeDamage(this.properties.projectileDamage);
                                }
                                return true;
                            },
                            posX1, posY1, posX2, posY2
                        );
                        DebugRenderer.getInstance().spawnRectangle(
                            new AbsolutePoint(posX1, posY1),
                            new AbsolutePoint(posX2, posY2),
                            Color.ORANGE
                        );
                        if (soundDestroy != null) {
                            soundDestroy.stop();
                            soundDestroy.setPosition(position.x(), position.y(), 0);
                            soundDestroy.setLooping(false);
                            soundDestroy.play();
                        }
                    }
                } else {
                    record.lightTime += delta;
                    record.light.setDistance(Utils.easeOutCirc(record.lightTime) * 15);
                }
            }
        }
        for (UseRecord deletion : deletions) {
            uses.remove(deletion);
        }
    }

    public Collectable getCollectable() {
        return this.collectable;
    }

    public enum ActiveItemType {
        FIREBALL
    }

    private static class UseRecord {
        public Projectile projectile;
        public boolean isLeft = false;
        public float useTime = 0;
        public float flightTime = 0;
        public float destroyTime = 0;
        public float lightTime = 0;
        public float lightDecay = 0;
        public boolean lightAttachedToBody = false;
        public boolean hasDamaged = false;
        public transient Light light;
    }

    public record ActiveItemProperties(
        ActiveItemType type,
        Collectable.CollectableType associatedCollectable,
        String texture, String soundUse, String soundDestroy,
        float projectileFlyingAnimationDuration,
        float projectileDestroyedAnimationDuration,
        float projectileSpeed, int projectileDamage, float projectileWidthCells, float projectileHeightCells,
        float projectileBlastRadiusCells) {
    }
}
