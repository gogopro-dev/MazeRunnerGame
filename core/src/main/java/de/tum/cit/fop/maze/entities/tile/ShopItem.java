package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.BodyBits;
import de.tum.cit.fop.maze.essentials.Globals;
import de.tum.cit.fop.maze.level.LevelScreen;
import games.rednblack.miniaudio.MASound;

import java.util.Objects;

public class ShopItem extends TileEntity {
    private Collectable item;
    private transient MASound purchaseSound;

    public ShopItem() {
        super(1, 1, new BodyDef(), new FixtureDef());
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.fixedRotation = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(Globals.COLLECTABLE_DESCRIPTION_RANGE * Globals.CELL_SIZE_METERS);
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = BodyBits.TILE_ENTITY;
        fixtureDef.filter.maskBits = BodyBits.TILE_ENTITY_MASK;
    }

    public ShopItem(Collectable item) {
        this();
        this.item = item;
        init();
    }


    @Override
    protected void init() {
        this.item.init();
        purchaseSound = Assets.getInstance().getSound("purchase");
        purchaseSound.setSpatialization(false);
    }

    @Override
    void render(float delta) {
        item.render(delta, getSpriteDrawPosition().x(), getSpriteDrawPosition().y());
    }

    @Override
    public void dispose() {
        super.dispose();
        item.dispose();
    }

    private String description() {
        return item.getCollectableAttributes().toItemDescription() +
            "\n Price " + item.getCollectableAttributes().shopPrice + " coins";
    }

    @Override
    public void onPlayerStartContact(Contact c) {
        /// Raycast isn't necessary here, since in the shop you won't be able to see a collectable through the wall
        LevelScreen.getInstance().hud.setItemDescription(
            description()
        );
    }

    @Override
    public void onPlayerEndContact(Contact c) {
        if (Objects.equals(LevelScreen.getInstance().hud.getItemDescription(),
            description())) {
            LevelScreen.getInstance().hud.deleteDescription();
        }
    }

    @Override
    public void contactTick(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) &&
            Objects.equals(LevelScreen.getInstance().hud.getItemDescription(),
                description()) &&
            LevelScreen.getInstance().player.getGold() >= this.item.getCollectableAttributes().shopPrice) {
            purchaseSound.stop();
            purchaseSound.setLooping(false);
            purchaseSound.play();
            LevelScreen.getInstance().player.collect(this.item);
            LevelScreen.getInstance().player.removeGold(this.item.getCollectableAttributes().shopPrice);
            toDestroy = true;
        }
    }
}
