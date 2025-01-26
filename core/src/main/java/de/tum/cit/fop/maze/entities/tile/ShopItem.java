package de.tum.cit.fop.maze.entities.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.Objects;

public class ShopItem extends TileEntity {
    private Collectable item;

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
        return item.getCollectableAttributes().toPrettyDescription() +
                "\n Price " + item.getCollectableAttributes().shopPrice + " coins";
    }

    @Override
    public void onPlayerStartContact(Contact c) {
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
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER) &&
                Objects.equals(LevelScreen.getInstance().hud.getItemDescription(),
                        description()) &&
                LevelScreen.getInstance().player.getGold() >= this.item.getCollectableAttributes().shopPrice) {
            LevelScreen.getInstance().player.collect(this.item);
            LevelScreen.getInstance().player.removeGold(this.item.getCollectableAttributes().shopPrice);
            toDestroy = true;
        }
    }
}
