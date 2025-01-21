package de.tum.cit.fop.maze;

public final class BodyBits {
    public static final short ENTITY = 0b00000001;
    public static final short WALL = 0b00000010;
    public static final short ENEMY = 0b00000100;
    public static final short DECORATION = 0b0000000;
    public static final short TILE_ENTITY = 0b00010000;
    public static final short LIGHT = 0b00100000;
    public static final short WALL_TRANSPARENT = 0b01000000;
    public static final short BARRIER_NO_LIGHT = 0b10000000;

    public static final short LIGHT_MASK = WALL | BARRIER_NO_LIGHT;
    public static final short TILE_ENTITY_MASK = ENTITY | WALL;
    public static final short ENTITY_MASK = WALL | TILE_ENTITY | WALL_TRANSPARENT;
    public static final short WALL_MASK = ENTITY | TILE_ENTITY | LIGHT;
    public static final short WALL_TRANSPARENT_MASK = WALL_MASK & ~LIGHT;
    public static final short ENEMY_MASK = ENEMY | WALL;
    public static final short BARRIER_NO_LIGHT_MASK = LIGHT;


}

