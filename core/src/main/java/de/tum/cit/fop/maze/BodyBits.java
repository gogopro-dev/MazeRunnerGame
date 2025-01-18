package de.tum.cit.fop.maze;

public final class BodyBits {
    public static final short ENTITY = 0b0000001;
    public static final short WALL = 0b0000010;
    public static final short ENEMY = 0b0000100;
    public static final short DECORATION = 0b000000;
    public static final short TILE_ENTITY = 0b0010000;
    public static final short LIGHT = 0b0100000;
    public static final short WALL_NO_LIGHT = 0b1000000;

    public static final short LIGHT_MASK = WALL;
    public static final short TILE_ENTITY_MASK = ENTITY | WALL;
    public static final short ENTITY_MASK = WALL | TILE_ENTITY | WALL_NO_LIGHT;
    public static final short WALL_MASK = ENTITY | TILE_ENTITY | LIGHT;
    public static final short WALL_NO_LIGHT_MASK = WALL_MASK & ~LIGHT;
    public static final short ENEMY_MASK = ENEMY | WALL;


}

