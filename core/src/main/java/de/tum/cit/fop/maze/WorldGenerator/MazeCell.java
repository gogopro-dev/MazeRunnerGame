package de.tum.cit.fop.maze.WorldGenerator;


import de.tum.cit.fop.maze.WorldGenerator.Rooms.Room;

/**
 * Used to represent a cell in the maze.
 */
public final class MazeCell {
    public final int i;
    public final int j;
    private CellType cellType;
    private Room room = null;

    public MazeCell(int i, int j, CellType cellType) {
        this.i = i;
        this.j = j;
        this.cellType = cellType;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    public CellType getCellType() {
        return cellType;
    }

    @Override
    public int hashCode() {
        return i * 31 + j;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MazeCell other)) return false;
        return i == other.i && j == other.j;
    }
}
