package de.tum.cit.fop.maze.entities.tile;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static de.tum.cit.fop.maze.Globals.*;


public class Torch extends TileEntity {

    private boolean lit = false;
    private PointLight light;
    private float elapsedTime = 0f;
    private float elapsedLitTime = 0f;
    private final Direction direction;
    private final @Nullable Animation<TextureAtlas.AtlasRegion> torchAnimation;
    private final @Nullable TextureRegion standTexture;
    private final Random random = new Random();


    public Torch(Direction direction) {
        super(1, 1, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(TORCH_ACTIVATION_RADIUS * Globals.CELL_SIZE_METERS);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
        this.direction = direction;

        TextureAtlas textureAtlas = new TextureAtlas("anim/tileEntities/tile_entities.atlas");
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
            default -> {
                this.torchAnimation = null;
                this.standTexture = null;
            }
        }

    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;
        if (lit) {
            elapsedLitTime += delta;
        }
        if (torchAnimation != null && standTexture != null) {
            TextureRegion frame = lit ? torchAnimation.getKeyFrame(elapsedTime, true) : standTexture;
            if (direction == Direction.RIGHT && !frame.isFlipX()) {
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
        light.update();
    }

    @Override
    protected void spawn(float x, float y) {
        super.spawn(x, y);
        light = new PointLight(
            LevelScreen.getInstance().rayHandler, RAY_AMOUNT, TORCH_LIGHT_COLOR, 0, x, y);
        light.setContactFilter(BodyBits.LIGHT, (short) 0, BodyBits.LIGHT_MASK);
        light.setActive(false);
        light.setStaticLight(true);
    }


    @Override
    public void onPlayerStartContact(Contact c) {
        super.onPlayerStartContact(c);

    }

    @Override
    public void contactTick(float delta) {
        if (!lit &&
            LevelScreen.getInstance().player.isHoldingTorch() &&
            /// -1/4f for centering source point of the torch center
            Utils.isPlayerReachable(
                this.getPosition().addY(-1 / 4f * Globals.CELL_SIZE_METERS), TORCH_ACTIVATION_RADIUS * 2
            )
        ) {
            lit = true;
            light.setActive(true);
        }
    }


}
