package de.tum.cit.fop.maze;

public final class BodyBits {
    public static final short ENTITY = 0b0001;
    public static final short WALL = 0b0010;
    public static final short ENEMY = 0b0100;

    public static final short ENTITY_MASK = WALL;
    public static final short WALL_MASK = ENTITY;
    public static final short ENEMY_MASK = ENEMY | WALL;

    public static final short DECORATION = 0b0100;
}

