package de.tum.cit.fop.maze.WorldGenerator;


import de.tum.cit.fop.maze.WorldGenerator.Rooms.Room;

import java.util.*;


/**
 * <h1>Maze generator</h1>
 * <p>
 *     <b>This class generates a maze using the Wilson's algorithm.</b> <br>
 *     The algorithm is as follows:
 *     <ol>
 *         <li>Start with a grid of unvisited cells.</li>
 *         <li>Choose a random cell and mark it as visited.</li>
 *         <li>Choose a random unvisited cell and mark it as visited.</li>
 *         <li>Create a random path from the first cell to the second cell.</li>
 *         <li>If the path loops back to a visited cell, remove the path and start over.</li>
 *         <li>Repeat steps 3-5 until all cells are visited.</li>
 *         <li>It generates a uniform simple maze with no loops and biases.</li>
 *     </ol>
 * </p>
 */
public final class MazeGenerator {
    // Auxiliary arrays
    private final HashMap<Cell, Integer> path;
    private final short[][] visited;
    private final List<List<Boolean>> willsonsCellGrid;
    private final ArrayList<Cell> visitedCells;
    private final ArrayList<Cell> unvisitedCells;
    private final List<List<Integer>> directions = Arrays.asList(
            Arrays.asList(0, 1),
            Arrays.asList(1, 0),
            Arrays.asList(0, -1),
            Arrays.asList(-1, 0)
    );

    public final int width;
    public final int height;
    public final List<List<MazeCell>> mazeGrid = new ArrayList<>();

    private final Random random;

    /**
     * <p>Constructor</p>
     * @param height The height of the maze. <b>It will be adjusted to be odd.</b>
     * @param width The width of the maze. <b>It will be adjusted to be odd.</b>
     * @param seed The seed for the random number generator.
     */
    public MazeGenerator(int height, int width, long seed) {

        this.path = new HashMap<>();
        this.random = new Random(seed);
        this.width = (2 * (width / 2)) + 1;  // Make sure width is odd
        this.height = (2 * (height / 2)) + 1;  // Make sure height is odd
        this.visited = new short[this.height][this.width];

        for (int i = 0; i < height; i++) {
            Arrays.fill(visited[i], (short) 0);
        }
        this.willsonsCellGrid = new ArrayList<>();
        for (int i = 0; i < this.height; i++) {
            List<Boolean> row = new ArrayList<>();
            for (int j = 0; j < this.width; j++) {
                row.add(false);
            }
            this.willsonsCellGrid.add(row);
        }
        this.visitedCells = new ArrayList<>();
        this.unvisitedCells = new ArrayList<>();
    }


    /**
     * <p>Fills the grid and the aux. arrays.</p>
     */
    private void clearArrays() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                willsonsCellGrid.get(i).set(j, false);
            }
        }

        // Fill unvisited cells
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    unvisitedCells.add(new Cell(i, j));
                }
            }
        }
        visitedCells.clear();
        path.clear();

    }

    /**
     * @param current The cell to set.
     */
    private void setTrue(Cell current){
        willsonsCellGrid.get(current.i()).set(current.j(), true);
    }

    /** Generates maze walls. */
    public void generateMazeWalls() {
        this.clearArrays();
        Cell current = unvisitedCells.remove(
                random.nextInt(unvisitedCells.size())
        );
        visitedCells.add(current);
        setTrue(current);

        while (!unvisitedCells.isEmpty()) {
            Cell first = unvisitedCells.get(random.nextInt(unvisitedCells.size()));
            current = first;
            do {
                int directionIndex = random.nextInt(4);
                while (!validDirection(current, directionIndex)) {
                    directionIndex = random.nextInt(4);
                }
                path.put(current, directionIndex);
                current = getNextCell(current, directionIndex, 2);
            } while (!visitedCells.contains(current));
            // Roll back to first cell
            current = first;

            // Until we reach the end
            while (true) {
                visitedCells.add(current);
                unvisitedCells.remove(current);
                setTrue(current);

                // Look at direction at next cell
                int directionIndex = path.get(current);
                Cell crossed = getNextCell(current, directionIndex, 1);
                setTrue(crossed);
                current = getNextCell(current, directionIndex, 2);
                if (visitedCells.contains(current)) {
                    path.clear();
                    break;
                }
            }
        }

        // Fill the actual maze grid
        for (int i = 0; i < height; i++) {
            List<MazeCell> row = new ArrayList<>();
            for (int j = 0; j < width; j++) {
                if (willsonsCellGrid.get(i).get(j)) {
                    row.add(new MazeCell(i, j, CellType.PATH));
                } else {
                    row.add(new MazeCell(i, j, CellType.WALL));
                }
            }
            mazeGrid.add(row);
        }
    }

    /**
     * Checks if it is possible to generate a room in the given location.
     * @param i The row.
     * @param j The column.
     * @param room The room to generate.
     *
     * @return
     * {@code true} if there is enough empty space in the room
     * {@code false} otherwise.
     */
    private boolean isPossibleToGenerateRoom(int i, int j, Room room) {
        for (int k = i; k < i + room.height; k++) {
            for (int l = j; l < j + room.width; l++) {
                if (k < 0 || k >= height || l < 0 || l >= width) {
                    return false;
                }
                if (mazeGrid.get(k).get(l).getCellType() == CellType.ROOM_PATH ||
                        mazeGrid.get(k).get(l).getCellType() == CellType.ROOM_WALL) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * <p>It generates a room with a certain {@link GeneratorStrategy GenetarorStrategy} using BFS,
     * placing the starting points accordingly.</p>
     * @param room The room to generate.
     */
    private void generateRoom(Room room) {
        clearVisited();
        boolean generated = false;
        Queue<MazeCell> queue = new LinkedList<>();
        if (room.generatorStrategy == GeneratorStrategy.CENTER) {
            int centerI = height / 2 - 2;
            int centerJ = width / 2 - 2;
            queue.add(mazeGrid.get(centerI).get(centerJ));
        } else if (room.generatorStrategy == GeneratorStrategy.RANDOM) {
            int i = random.nextInt(height);
            int j = random.nextInt(width);
            queue.add(mazeGrid.get(i).get(j));
        } else if (room.generatorStrategy == GeneratorStrategy.CORNERS) {
            queue.add(mazeGrid.get(0).get(0));
            queue.add(mazeGrid.get(0).get(width - 1));
            queue.add(mazeGrid.get(height - 1).get(0));
            queue.add(mazeGrid.get(height - 1).get(width - 1));
        } else if (room.generatorStrategy == GeneratorStrategy.SIDES) {
            for (int i = 0; i < height; i++) {
                queue.add(mazeGrid.get(i).get(0));
                queue.add(mazeGrid.get(i).get(width - 1));
            }
            for (int j = 0; j < width; j++) {
                queue.add(mazeGrid.get(0).get(j));
                queue.add(mazeGrid.get(height - 1).get(j));
            }
        } else if (room.generatorStrategy == GeneratorStrategy.AROUND_CENTER) {
            int centerI = height / 2;
            int centerJ = width / 2;
            List<MazeCell> candidates = new ArrayList<>();
            for (int i = centerI - height / 3 ; i < centerI + height / 3 ; ++i) {
                candidates.add(mazeGrid.get(i).get(centerJ - height / 3));
                candidates.add(mazeGrid.get(i).get(centerJ + height / 3));
            }
            for (int j = centerJ - width / 3 ; j < centerJ + width / 3 ; ++j) {
                candidates.add(mazeGrid.get(centerI + height / 3).get(j));
                candidates.add(mazeGrid.get(centerI - height / 3).get(j));
            }
            int pick = random.nextInt(candidates.size());
            queue.add(candidates.get(pick));
        }

        while (!queue.isEmpty()) {

            MazeCell current = queue.poll();
            int i = current.i;
            int j = current.j;
            if (visited[i][j] > 0) {
                continue;
            }
            visited[i][j] = 1;
            if (isPossibleToGenerateRoom(i, j, room)) {
                generated = true;
                List<MazeCell> roomCells = new ArrayList<>();
                for (int k = i; k < i + room.height; k++) {
                    for (int l = j; l < j + room.width; l++) {
                        MazeCell cell = mazeGrid.get(k).get(l);
                        roomCells.add(cell);
                        cell.setRoom(room);
                    }
                }
                room.setLocation(i, j);
                room.setRoomCells(roomCells);
                room.generate(mazeGrid, random);

                break;
            }
            // Out of bounds safety checks
            if (i + 1 < height) queue.add(mazeGrid.get(i + 1).get(j));
            if (i - 1 >= 0)     queue.add(mazeGrid.get(i - 1).get(j));
            if (j + 1 < width)  queue.add(mazeGrid.get(i).get(j + 1));
            if (j - 1 >= 0)     queue.add(mazeGrid.get(i).get(j - 1));


        }
        if (!generated) {
            throw new IllegalArgumentException("Room could not be generated.");
        }
    }

    /**
     * <p>Clears the visited array.</p>
     */
    private void clearVisited() {
        for (int i = 0; i < height; i++) {
            Arrays.fill(visited[i], (short) 0);
        }
    }

    /**
     * <p>Generates the rooms in the maze. Then it fixes incomplete corners (effective corners that miss a wall) and
     * reachability of the maze</p>
     * @param rooms The rooms to generate.
     */
    public void generateRooms(List<Room> rooms) {
        for (Room room : rooms) {
            generateRoom(room);
        }
        fixCorners();
        fixReachability();
    }



    /**
     * <p>Fixes the incomplete (effective) corners of the maze.</p>
     */
    private void fixCorners() {
        /*
         * Fix incomplete corners like this, where ! is missing corner cell:
         *      ###
         *    ##!
         */
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                boolean topWall = mazeGrid.get(i - 1).get(j).getCellType() == CellType.WALL;
                boolean bottomWall = mazeGrid.get(i + 1).get(j).getCellType() == CellType.WALL;
                boolean leftWall = mazeGrid.get(i).get(j - 1).getCellType() == CellType.WALL;
                boolean rightWall = mazeGrid.get(i).get(j + 1).getCellType() == CellType.WALL;
                boolean leftDiagonalTop = mazeGrid.get(i - 1).get(j - 1).getCellType() == CellType.PATH;
                boolean rightDiagonalTop = mazeGrid.get(i - 1).get(j + 1).getCellType() == CellType.PATH;
                boolean leftDiagonalBottom = mazeGrid.get(i + 1).get(j - 1).getCellType() == CellType.PATH;
                boolean rightDiagonalBottom = mazeGrid.get(i + 1).get(j + 1).getCellType() == CellType.PATH;

                if (mazeGrid.get(i).get(j).getCellType().isPath() &&
                        (topWall && leftWall && leftDiagonalTop) || (topWall && rightWall && rightDiagonalTop) ||
                        (bottomWall && leftWall && leftDiagonalBottom) ||
                        (bottomWall && rightWall && rightDiagonalBottom)) {
                    MazeCell temp = mazeGrid.get(i).get(j);
                    // Check so that we are consistent with the cell type (room or not)
                    temp.setCellType(
                            temp.getRoom() == null ? CellType.WALL : CellType.ROOM_WALL
                    );
                }
            }

        }
    }

    /**
     * <p>Fixes the reachability of the maze using flood-fill and DSU.
     * The idea is to remove a random wall between two disjoint components.</p>
     */
    private void fixReachability() {

        short counter = 1;
        clearVisited();
        Queue<MazeCell> queue = new LinkedList<>();

        // Flood fill disjoint components
        while (true) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    MazeCell cell = mazeGrid.get(i).get(j);
                    if (cell.getCellType().walkable() && visited[i][j] == 0) {
                        queue.add(cell);
                        break;
                    }
                }
                if (!queue.isEmpty()) {
                    break;
                }
            }
            if (queue.isEmpty()) break;
            while(!queue.isEmpty()) {
                MazeCell current = queue.poll();
                int i = current.i;
                int j = current.j;
                if (visited[i][j] > 0 || !mazeGrid.get(i).get(j).getCellType().walkable()) {
                    continue;
                }

                visited[i][j] = counter;
                if (i + 1 < height) queue.add(mazeGrid.get(i + 1).get(j));
                if (i - 1 >= 0)     queue.add(mazeGrid.get(i - 1).get(j));
                if (j + 1 < width)  queue.add(mazeGrid.get(i).get(j + 1));
                if (j - 1 >= 0)     queue.add(mazeGrid.get(i).get(j - 1));
            }
            ++counter;
        }
        List<PassagePossibility> possibilities = new ArrayList<>();
        DSU dsu = new DSU(counter);

        // Find all possible passages
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                MazeCell cell = mazeGrid.get(i).get(j);
                if (cell.getCellType() == CellType.WALL) {
                    MazeCell above = i - 1 >= 0 ? mazeGrid.get(i - 1).get(j) : null;
                    MazeCell below = i + 1 < height ? mazeGrid.get(i + 1).get(j) : null;
                    MazeCell left = j - 1 >= 0 ? mazeGrid.get(i).get(j - 1) : null;
                    MazeCell right = j + 1 < width ? mazeGrid.get(i).get(j + 1) : null;

                    if (above != null && below != null &&
                            above.getCellType() == CellType.PATH && below.getCellType() == CellType.PATH &&
                            !dsu.same(visited[i - 1][j], visited[i + 1][j])) {
                        possibilities.add(new PassagePossibility(above, cell, below));
                    }
                    if (left != null && right != null &&
                            left.getCellType() == CellType.PATH && right.getCellType() == CellType.PATH &&
                            !dsu.same(visited[i][j - 1], visited[i][j + 1])) {
                        possibilities.add(new PassagePossibility(left, cell, right));
                    }
                }
            }

        }
        // Randomize the possibilities
        Collections.shuffle(possibilities, random);
        for (PassagePossibility possibility : possibilities) {
            MazeCell a = possibility.a();
            MazeCell b = possibility.b();
            MazeCell wall = possibility.wall();
            if (dsu.same(visited[a.i][a.j], visited[b.i][b.j])) {
                continue;
            }
            wall.setCellType(CellType.PATH);
            dsu.union(visited[a.i][a.j], visited[b.i][b.j]);
        }
    }

    /**
     * <p>Gets the next cell in the given direction.</p>
     * @param current The current cell.
     * @param directionIndex The index of the direction.
     * @param distance The distance to the next cell.
     * @return The next cell.
     */
    private Cell getNextCell(Cell current, int directionIndex, int distance) {
        List<Integer> direction = directions.get(directionIndex);
        return new Cell(
                current.i() + direction.get(0) * distance,
                current.j() + direction.get(1) * distance
        );
    }

    /**
     * <p>Checks if the applied direction won't go out of bounds. I.e. - valid</p>
     * @param cell The current cell.
     * @param directionIndex The index of the direction.
     * @return True if the direction is valid, false otherwise.
     */
    private boolean validDirection(Cell cell, int directionIndex) {
        Cell newCell = getNextCell(cell, directionIndex, 2);
        return newCell.i() >= 0 && newCell.i() < height &&
                newCell.j() >= 0 && newCell.j() < width;
    }


    @Override
    public String toString() {
        Character spacing = ' ';
        StringBuilder sb = new StringBuilder();
        for (List<MazeCell> mazeCells : mazeGrid) {
            for (MazeCell mazeCell : mazeCells) {
                switch (mazeCell.getCellType()) {
                    case WALL ->        sb.append("#");
                    case PATH ->        sb.append(" ");
                    case TRAP ->        sb.append("T");
                    case ROOM_PATH ->   sb.append("!");
                    case ROOM_WALL ->   sb.append("@");
                    case DOOR ->        sb.append("D");
                    case EXIT_DOOR ->   sb.append("E");
                    case KEY_OBELISK -> sb.append("K");

                }
                sb.append(spacing);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
