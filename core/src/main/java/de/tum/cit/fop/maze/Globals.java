package de.tum.cit.fop.maze;

public final class Globals {

    /// Pixel per Meter
    public static final float PPM = 64f;
    /// Meter per Pixel
    public static final float MPP = 1 / PPM;
    public static final int CELL_SIZE = 16;


    public static final float ENTITY_PATHFINDING_RANGE = 6 * 3;
    public static final float ENEMY_PATHFINDING_INTERVAL = 0.1f;

    public static final float TRAP_SPAWN_CHANCE = 0.06f;

    public static final float TILEMAP_SCALE = 4f;
    public static final float CELL_SIZE_METERS = (CELL_SIZE / PPM) * TILEMAP_SCALE;
    public static final float TRAP_SAFETY_PADDING = CELL_SIZE_METERS / 12;

    public static final float BOX2D_TIME_STEP = 1 / 120f;
    public static final int BOX2D_VELOCITY_ITERATIONS = 8;
    public static final int BOX2D_POSITION_ITERATIONS = 3;
    public static final int TRAP_DAMAGE = 10;
    public static final float DEFAULT_CAMERA_ZOOM = 1.25f;
    public static final boolean DEBUG = true;

}
