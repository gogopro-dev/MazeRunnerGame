package de.tum.cit.fop.maze.level.worldgen;

/** Represents the type of cell in the maze */
public enum CellType {
    NONE(-1),
    WALL(0),
    PATH(1),
    TRAP(2),
    ROOM_PATH(3),
    ROOM_WALL(4),
    DOOR(5),
    EXIT_DOOR(6),
    KEY_OBELISK(7),
    TREASURE_ROOM_ITEM(9),
    SHOP_ITEM(10);

    public final int value;

    CellType(int value) {
        this.value = value;
    }

    /**
     * Deserializes the cell type from an integer value
     * @param value integer value
     * @return {@link CellType} the cell type
     */
    public static CellType fromInt(int value) {
        for (CellType type : CellType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

    /**
     * @return {@code true} if the cell type is any path
     */
    public boolean isPath() {
        return this == PATH || this == ROOM_PATH;
    }

    /**
     * @return {@code true} if the cell type is walkable
     */
    public boolean isWalkable() {
        return this.isPath() || this == DOOR || this == EXIT_DOOR || this == KEY_OBELISK ||
            this == TRAP || this == TREASURE_ROOM_ITEM || this == SHOP_ITEM;
    }

    /**
     * @return {@code true} if the cell type is any wall
     */
    public boolean isWall() {
        return this == ROOM_WALL || this == WALL;
    }

    /**
     * @return {@code true} if the cell type is a room
     */
    public boolean isRoom() {
        return this == ROOM_PATH || this == ROOM_WALL || this == TREASURE_ROOM_ITEM || this == SHOP_ITEM;
    }

}
