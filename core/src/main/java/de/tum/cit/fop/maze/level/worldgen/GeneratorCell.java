package de.tum.cit.fop.maze.level.worldgen;
import com.badlogic.gdx.utils.Null;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.level.worldgen.rooms.Room;

/**
 * Used to represent a cell in the maze.
 */
public final class GeneratorCell {
    /// Row index
    private int i;
    /// Column index
    private int j;
    /// The {@link CellType} of the cell
    private CellType cellType;
    /// The room the cell is part of
    private @Null Room room = null;

    public GeneratorCell(int i, int j, CellType cellType) {
        this.i = i;
        this.j = j;
        this.cellType = cellType;
    }

    /**
     * Constructor for a MazeCell with default cell type NONE
     * @param i - row index
     * @param j - column index
     */
    public GeneratorCell(int i, int j) {
        this.i = i;
        this.j = j;
        this.cellType = CellType.NONE;
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


    /**
     * Cantor pairing function
     * @param a {@link Integer} a
     * @param b {@link Integer} b
     * @return the cantor pairing of a and b (hash)
     */
    int cantor(int a, int b) {
        return (a + b + 1) * (a + b) / 2 + b;
    }

    @Override
    public int hashCode() {
        return cantor(i, cantor(j, cellType.value));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeneratorCell other)) return false;
        return i == other.i && j == other.j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public Direction getDirection(GeneratorCell other) {
        if (this.i == other.i) {
            if (this.j < other.j) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        } else {
            if (this.i < other.i) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        }
    }
}
