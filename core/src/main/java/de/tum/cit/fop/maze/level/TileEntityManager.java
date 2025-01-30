package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.entities.tile.TileEntity;
import de.tum.cit.fop.maze.entities.tile.Trap;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class manages the tile entities in the game.
 * It is responsible for creating, removing and rendering the tile entities.
 */
public class TileEntityManager implements Disposable {
    private transient boolean loaded = false;
    private ArrayList<TileEntity> tileEntities = new ArrayList<TileEntity>();

    public void createTileEntity(TileEntity tileEntity, AbsolutePoint position) {
        createTileEntity(tileEntity, position.x(), position.y());
    }

    public void createTileEntity(TileEntity tileEntity, float x, float y) {
        tileEntities.add(tileEntity);
        tileEntity.spawn(x, y);
    }

    private void destroyTileEntity(TileEntity tileEntity) {
        tileEntity.dispose();
    }


    public void render(float delta) {
        boolean isAnyActiveTraps = false;
        TileEntity[] tileEntityPrimitive = tileEntities.toArray(new TileEntity[0]);
        /// Sort the tile entities by their y position
        Arrays.parallelSort(
            tileEntityPrimitive, (a, b) -> Float.compare(b.getPosition().y(), a.getPosition().y())
        );
        this.tileEntities = new ArrayList<>(Arrays.asList(tileEntityPrimitive));
        Iterator<TileEntity> it = tileEntities.iterator();

        while (it.hasNext()) {
            TileEntity tileEntity = it.next();
            if (tileEntity.toDestroy) {
                it.remove();
                destroyTileEntity(tileEntity);
                continue;
            }
            if (tileEntity.isOnPlayer()) {
                if (tileEntity instanceof Trap trap) {
                    isAnyActiveTraps |= trap.isActivated();
                }
                tileEntity.contactTick(delta);
            }
            tileEntity.renderTileEntity(delta);
        }
        LevelScreen.getInstance().player.setOnActiveTrap(isAnyActiveTraps);
    }

    @Override
    public void dispose() {
        for (TileEntity tileEntity : tileEntities) {
            tileEntity.dispose();
        }
    }

    public void restore() {
        if (loaded) {
            throw new IllegalStateException("Tile entity has already been loaded");
        }
        loaded = true;

        for (TileEntity tileEntity : tileEntities) {
            tileEntity.initialize();
            tileEntity.spawn(
                tileEntity.getSavedPosition().x(),
                tileEntity.getSavedPosition().y()
            );
        }
    }
}
