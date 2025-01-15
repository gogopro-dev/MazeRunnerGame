package de.tum.cit.fop.maze.entities.tile;

import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;

public class TileEntityManager {
    private final ArrayList<TileEntity> tileEntities = new ArrayList<TileEntity>();

    public void createTileEntity(TileEntity tileEntity, float x, float y) {
        tileEntities.add(tileEntity);
        tileEntity.spawn(x, y);
    }

    public void removeTileEntity(TileEntity tileEntity) {
        tileEntities.remove(tileEntity);
    }


    public void render(float delta) {
        boolean isAnyActiveTraps = false;
        for (TileEntity tileEntity : tileEntities) {
            if (tileEntity.isActivated()) {
                if (tileEntity instanceof Trap trap) {
                    isAnyActiveTraps |= trap.isActivated();
                }
                tileEntity.contactTick(delta);
            }
            tileEntity.render(delta);
        }
        LevelScreen.getInstance().player.setOnTrap(isAnyActiveTraps);
    }

}
