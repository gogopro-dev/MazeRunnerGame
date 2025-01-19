package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;

public final class Globals {
    public static final boolean DEBUG = true;
    public static final boolean FULLBRIGHT = true;

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
    public static final float PATHFINDING_SAFETY_GAP = CELL_SIZE_METERS / 7f;

    public static final float BOX2D_TIME_STEP = 1 / 120f;
    public static final int BOX2D_VELOCITY_ITERATIONS = 8;
    public static final int BOX2D_POSITION_ITERATIONS = 3;
    public static final int TRAP_DAMAGE = 10;
    public static float DEFAULT_CAMERA_ZOOM = 1.25f;
    public static final float TORCH_ACTIVATION_RADIUS = 0.75f;
    public static final float TORCH_GAP = 4.3f * 1.25f;
    public static final float TORCH_LIGHT_RADIUS = 10f;
    public static final float TRAP_LIGHT_RADIUS = 12f;
    public static final int RAY_AMOUNT = 100;
    public static final Color TORCH_LIGHT_COLOR = new Color(0xffa459ff);
    public static final Color TRAP_LIGHT_COLOR = new Color(0xff0000ff);

    public static final int PLAYER_DAMAGE = 2;
    public static final float PLAYER_ATTACK_DISTANCE = CELL_SIZE_METERS;
    public static final float PLAYER_ATTACK_KNOCKBACK = 10f;
}
