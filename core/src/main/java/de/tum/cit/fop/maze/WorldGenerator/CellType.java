package de.tum.cit.fop.maze.WorldGenerator;

/** Represents the type of cell in the maze */
public enum CellType {
    WALL(0),
    PATH(1),
    TRAP(2),
    ROOM_PATH(3),
    ROOM_WALL(4),
    DOOR(5),
    EXIT_DOOR(6),
    KEY_OBELISK(7);

    public final int value;

    CellType(int value) {
        this.value = value;
    }

    public static CellType fromInt(int value) {
        for (CellType type : CellType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

    public boolean isPath() {
        return this == PATH || this == ROOM_PATH;
    }

    public boolean walkable() {
        return this == PATH || this == ROOM_PATH || this == DOOR || this == EXIT_DOOR || this == KEY_OBELISK;
    }

}
