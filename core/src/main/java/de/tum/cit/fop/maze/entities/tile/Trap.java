package de.tum.cit.fop.maze.entities.tile;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.BodyBits;
import de.tum.cit.fop.maze.essentials.Globals;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;
import org.jetbrains.annotations.Nullable;

import static de.tum.cit.fop.maze.essentials.Globals.*;

/**
 * Represents a trap in the game
 */
public class Trap extends TileEntity {
    private transient Animation<TextureRegion> trapAnimation;
    private transient float elapsedTime = 0f;
    private transient float lastActivationTime = 0f;
    private boolean isActivated = false;
    public TrapAttributes attributes;
    private transient @Nullable PointLight light;

    /**
     * The attributes of a trap
     *
     * @param type          the type of the trap
     * @param frameDuration duration of each frame in seconds
     * @param damage        trap's damage
     * @param cooldown      cooldown of the trap in seconds
     * @param height        height of the trap in cells
     * @param width         width of the trap in cells
     * @param emitsLight    whether the trap emits light
     */
    public record TrapAttributes(
        TrapType type,
        float frameDuration,
        float damage,
        float cooldown,
        int height,
        int width,
        boolean emitsLight
    ) {
    }

    private Trap() {
        super(0, 0);
    }

    /**
     * Creates a new trap from a given type
     *
     * @param type the type of the trap
     */
    public Trap(TrapType type) {
        this();
        this.attributes = Assets.getInstance().getTraps().stream(
        ).filter(attribute -> attribute.type.equals(type)).findFirst().get();
        init();
    }

    protected void init() {
        this.width = attributes.width;
        this.height = attributes.height;
        ((PolygonShape) this.fixtureDef.shape).setAsBox(
            width * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING,
            height * Globals.CELL_SIZE_METERS / 2 - TRAP_SAFETY_PADDING
        );
        TextureAtlas textureAtlas = Assets.getInstance().getAssetManager()
            .get("assets/anim/tileEntities/tile_entities.atlas", TextureAtlas.class);
        trapAnimation = new Animation<>(
            this.attributes.frameDuration, textureAtlas.findRegions(
            this.attributes.type.name()), Animation.PlayMode.NORMAL
        );
    }

    public void render(float deltaTime) {
        elapsedTime += deltaTime;
        if (isActivated){
            if (trapAnimation.isAnimationFinished(elapsedTime)) {
                isActivated = false;
                lastActivationTime = elapsedTime;
            } else if (light != null) {
                light.setActive(true);
                if (light.getDistance() < TRAP_LIGHT_RADIUS) {
                    light.setDistance(TRAP_LIGHT_RADIUS * Utils.easeOutCirc(elapsedTime));
                }
            }
        } else {
            if (light != null && light.getDistance() > 0) {
                light.setDistance(
                    TRAP_LIGHT_RADIUS -
                        TRAP_LIGHT_RADIUS * Utils.easeOutCirc((elapsedTime - lastActivationTime) * 3f)
                );
            }
            if (elapsedTime - lastActivationTime >= attributes.cooldown) {
                isActivated = true;
                elapsedTime = 0f;
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        TextureRegion currentFrame;
        if (isActivated) {
            currentFrame = trapAnimation.getKeyFrame(elapsedTime, false);
        } else {
            /// trapAnimation idle state:
            currentFrame = trapAnimation.getKeyFrame(trapAnimation.getAnimationDuration(), false);
        }

        batch.draw(
            currentFrame,
            getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
            getSpriteDrawWidth(), getSpriteDrawHeight()
        );
    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        if (attributes.emitsLight) {
            light = new PointLight(
                LevelScreen.getInstance().rayHandler, RAY_AMOUNT, TRAP_LIGHT_COLOR,
                0, x, y
            );
            light.setContactFilter(BodyBits.LIGHT, (short) 0, BodyBits.LIGHT_MASK);
            light.setActive(false);
            light.setStaticLight(true);
        }
    }

    public boolean isActivated() {
        return isActivated;
    }

    public boolean isDamaging() {
        return isActivated && elapsedTime > 0.15f;
    }



}
