package de.tum.cit.fop.maze;

public final class BodyBits {
    public static final short ENTITY = 0b00001;
    public static final short WALL = 0b00010;
    public static final short ENEMY = 0b00100;
    public static final short DECORATION = 0b0000;
    public static final short TILE_ENTITY = 0b10000;

    public static final short TILE_ENTITY_MASK = ENTITY;
    public static final short ENTITY_MASK = WALL | TILE_ENTITY;
    public static final short WALL_MASK = ENTITY;
    public static final short ENEMY_MASK = ENEMY | WALL;


}

