package de.tum.cit.fop.maze;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import de.tum.cit.fop.maze.entities.Attackable;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.tile.Collectable;
import de.tum.cit.fop.maze.entities.tile.Projectile;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.GSONPostRestorable;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelData;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;

public class ActiveItem implements GSONPostRestorable {
    private transient Animation<TextureRegion> projectileFlyingAnimation;
    private transient Animation<TextureRegion> projectileDestroyedAnimation;
    private transient Animation<TextureRegion> useAnimation;
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


    float animationTimer = 0;
    private boolean projectileFacingRight = true;
    private final Collectable collectable;
    private final ActiveItemProperties properties;
    private ArrayList<UseRecord> uses = new ArrayList<>();

    public enum ActiveItemType {
        FIREBALL
    }

    public record ActiveItemProperties(
            ActiveItemType type,
            Collectable.CollectableType associatedCollectable,
            String texture,
            float projectileFlyingAnimationDuration,
            float projectileDestroyedAnimationDuration,
            float projectileSpeed, int projectileDamage, float projectileWidthCells, float projectileHeightCells,
            float projectileBlastRadiusCells) {

    }

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
        useAnimation = new Animation<>(
                Globals.useActiveFrameDuration,
                textureAtlas.findRegions(properties.texture + "_use"),
                Animation.PlayMode.NORMAL
        );
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

    public ActiveItem(ActiveItemType itemType) {
        this.properties = Assets.getInstance().getActiveItems().stream().filter(
                it -> it.type == itemType
        ).findFirst().orElseThrow();
        this.collectable = new Collectable(properties.associatedCollectable);
        loadAnimations();
    }

    public void renderIcon(float delta, float x, float y) {
        animationTimer += delta;
        this.collectable.render(delta, x, y);
    }

    public void use() {
        var temp = new UseRecord();
        temp.isLeft = !LevelScreen.getInstance().player.isFacingRight();
        uses.add(temp);

    }

    public void tick(float delta) {
        SpriteBatch batch = LevelScreen.getInstance().batch;
        Player player = LevelScreen.getInstance().player;
        ArrayList<UseRecord> deletions = new ArrayList<>();
        for (UseRecord record : uses) {
            record.useTime += delta;

            if (record.projectile == null) {
                record.projectile = new Projectile(
                        1, 1, this.properties.projectileSpeed, !record.isLeft
                );
                LevelScreen.getInstance().tileEntityManager.createTileEntity(record.projectile, player.getPosition());
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
            } else {
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
}
