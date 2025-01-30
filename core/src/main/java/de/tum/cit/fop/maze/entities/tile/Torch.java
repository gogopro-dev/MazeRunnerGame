package de.tum.cit.fop.maze.entities.tile;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import de.tum.cit.fop.maze.essentials.*;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import static de.tum.cit.fop.maze.essentials.Globals.*;

/**
 * Torch tile entity.
 * <p>
 *     A torch is a tile entity that can be lit by the player.
 *     The torch emits light when it is lit.
 *     The torch can be lit by the player if the player is holding a torch and is near the torch.
 * </p>
 */
public class Torch extends TileEntity {

    private boolean lit = false;
    private Direction direction;
    private transient PointLight light;
    private transient float elapsedTime = 0f;
    private transient float elapsedLitTime = 0f;
    private transient Animation<TextureAtlas.AtlasRegion> torchAnimation;
    private transient TextureRegion standTexture;
    private final transient MASound litSound;

    /**
     * Instantiates a new Torch.
     */
    public Torch() {
        super(1, 1, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(TORCH_ACTIVATION_RADIUS * Globals.CELL_SIZE_METERS);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        litSound = Assets.getInstance().getSound("torch_lit");
        litSound.setLooping(true);
    }

    /**
     * Instantiates a new Torch with a given direction.
     * @param direction the direction
     */
    public Torch(Direction direction) {
        this();
        this.direction = direction;
        init();
    }

    /**
     * Initializes the torch.
     */
    protected void init() {
        TextureAtlas textureAtlas = Assets.getInstance().getAssetManager()
            .get("assets/anim/tileEntities/tile_entities.atlas", TextureAtlas.class);
        switch (direction) {
            case UP -> {
                this.torchAnimation =
                    new Animation<>(
                        0.1f, textureAtlas.findRegions("front_torch"), Animation.PlayMode.LOOP
                    );
                this.standTexture = textureAtlas.findRegion("front_torch_stand");
            }
            case LEFT, RIGHT -> {

                this.torchAnimation =
                    new Animation<>(
                        0.1f, textureAtlas.findRegions("side_torch"), Animation.PlayMode.LOOP
                    );
                this.standTexture = textureAtlas.findRegion("side_torch_stand");
            }
            case DOWN -> {
                this.torchAnimation = new Animation<>(
                    0.1f, textureAtlas.findRegions("back_torch"), Animation.PlayMode.LOOP
                );
                this.standTexture = textureAtlas.findRegion("back_torch_stand");
            }
        }
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;
        if (lit) {
            elapsedLitTime += delta;
        }
        light.update();
        if (torchAnimation != null && standTexture != null) {
            TextureRegion frame = lit ? torchAnimation.getKeyFrame(elapsedTime, true) : standTexture;
            if (direction == Direction.RIGHT && !frame.isFlipX()) {
                frame.flip(true, false);
            } else if (direction == Direction.LEFT && frame.isFlipX()) {
                frame.flip(true, false);
            }

            batch.draw(frame,
                getSpriteDrawPosition().x(), getSpriteDrawPosition().y(),
                getSpriteDrawWidth(), getSpriteDrawHeight()
            );
        }
        if (lit && light.getDistance() < TORCH_LIGHT_RADIUS) {
            light.setDistance(Math.min(
                TORCH_LIGHT_RADIUS,
                Utils.easeOutCirc(elapsedLitTime * 0.77f) * TORCH_LIGHT_RADIUS
            ));
        }

    }

    @Override
    public void spawn(float x, float y) {
        super.spawn(x, y);
        light = new PointLight(
            LevelScreen.getInstance().rayHandler, RAY_AMOUNT, TORCH_LIGHT_COLOR, 0, x, y);
        light.setContactFilter(BodyBits.LIGHT, (short) 0, BodyBits.LIGHT_MASK);
        light.setActive(lit);
        light.setStaticLight(false);
    }

    @Override
    public void contactTick(float delta) {
        if (!lit &&
            LevelScreen.getInstance().player.isHoldingTorch() &&
            /// -1/4f for centering source point of the torch center
            Utils.isPlayerExposed(
                this.getPosition().addY(-1 / 4f * Globals.CELL_SIZE_METERS), TORCH_ACTIVATION_RADIUS * 2
            )
        ) {
            if (!lit) {
                lit = true;
                light.setActive(true);
                float x = getPosition().x();
                float y = getPosition().y();
                litSound.stop();
                litSound.setPosition(x, y, 0);
                litSound.setLooping(false);
                litSound.play();
            }
        }
    }


}
