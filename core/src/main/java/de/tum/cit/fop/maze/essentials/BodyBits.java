package de.tum.cit.fop.maze.essentials;

public final class BodyBits {
    public static final short DECORATION = 0b0;
    public static final short ENTITY = 1;
    public static final short WALL = 1 << 2;
    public static final short ENEMY = 1 << 3;

    public static final short TILE_ENTITY = 1 << 4;
    public static final short LIGHT = 1 << 5;
    public static final short WALL_TRANSPARENT = 1 << 6;
    public static final short BARRIER_NO_LIGHT = 1 << 7;
    public static final short PROJECTILE = 1 << 8;


    public static final short LIGHT_MASK = WALL | BARRIER_NO_LIGHT;
    public static final short TILE_ENTITY_MASK = ENTITY | WALL | WALL_TRANSPARENT;
    public static final short ENTITY_MASK = WALL | TILE_ENTITY | WALL_TRANSPARENT | PROJECTILE;
    public static final short WALL_MASK = ENTITY | TILE_ENTITY | LIGHT | PROJECTILE;
    public static final short WALL_TRANSPARENT_MASK = WALL_MASK & ~LIGHT | PROJECTILE;
    public static final short ENEMY_MASK = ENEMY | WALL | PROJECTILE;
    public static final short BARRIER_NO_LIGHT_MASK = LIGHT | PROJECTILE;
    public static final short PROJECTILE_MASK = WALL | WALL_TRANSPARENT | ENTITY | ENEMY | BARRIER_NO_LIGHT;


}

