package de.tum.cit.fop.maze;

public final class Globals {

    /// Pixel per Meter
    public static final float PPM = 64f;
    /// Meter per Pixel
    public static final float MPP = 1 / PPM;
    public static final int CELL_SIZE = 16;

    public static final float TILEMAP_SCALE = 3;
    public static final float CELL_SIZE_METERS = (CELL_SIZE / PPM) * TILEMAP_SCALE;

    public static final float BOX2D_TIME_STEP = 1 / 120f;
    public static final int BOX2D_VELOCITY_ITERATIONS = 8;
    public static final int BOX2D_POSITION_ITERATIONS = 3;

}
