package de.tum.cit.fop.maze.entities.tile;

import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class manages the tile entities in the game.
 * It is responsible for creating, removing and rendering the tile entities.
 */
public class TileEntityManager {
    private final ArrayList<TileEntity> tileEntities = new ArrayList<TileEntity>();

    public void createTileEntity(TileEntity tileEntity, float x, float y) {
        tileEntities.add(tileEntity);
        tileEntity.spawn(x, y);
    }

    private void destroyTileEntity(TileEntity tileEntity) {
        LevelScreen.getInstance().world.destroyBody(tileEntity.getBody());
    }


    public void render(float delta) {
        boolean isAnyActiveTraps = false;
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
            tileEntity.render(delta);
        }
        LevelScreen.getInstance().player.setOnActiveTrap(isAnyActiveTraps);
    }

}
