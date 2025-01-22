package de.tum.cit.fop.maze.level.worldgen;


import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.worldgen.rooms.*;

import java.util.*;


/**
 * <h1>Maze generator</h1>
 * <p>
 *     <b>This class generates a maze using the Wilson's algorithm.</b> <br>
 *     The algorithm has the following steps:
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
    /// Auxiliary arrays
    /// The path from the current cell to the next cell
    private final HashMap<GeneratorCell, Integer> path;
    ///  The visited cells
    private final short[][] visited;
    /// The wilson's cell grid of booleans to mark walls
    private final List<List<Boolean>> willsonsCellGrid;
    private final ArrayList<GeneratorCell> visitedCells;
    private final ArrayList<GeneratorCell> unvisitedCells;
    /// The directions to move in the maze
    private final List<List<Integer>> directions = Arrays.asList(
            Arrays.asList(0, 1),
            Arrays.asList(1, 0),
            Arrays.asList(0, -1),
            Arrays.asList(-1, 0)
    );
    /// The width value to use for generation
    private final int generatorWidth;
    /// The height value to use for generation
    private final int generatorHeight;

    public final int width;
    public final int height;

    /// Final grid
    public final ArrayList<ArrayList<GeneratorCell>> grid = new ArrayList<>();

    private final Random random;

    /**
     * <p>Constructor</p>
     * @param height The generatorHeight of the maze. <b>It will be adjusted to be odd.</b>
     * @param width The generatorWidth of the maze. <b>It will be adjusted to be odd.</b>
     * @param seed The seed for the random number generator.
     */
    public MazeGenerator(int height, int width, long seed) {

        this.path = new HashMap<>();
        this.random = new Random(seed);
        this.generatorWidth = (2 * (width / 2)) + 1;  // Make sure generatorWidth is odd
        this.generatorHeight = (2 * (height / 2)) + 1;  // Make sure generatorHeight is odd
        this.width = width + 2 + (width % 2 == 0 ? 1 : 0);
        this.height = height + 2 + (height % 2 == 0 ? 1 : 0);
        this.visited = new short[this.generatorHeight][this.generatorWidth];

        for (int i = 0; i < height; i++) {
            Arrays.fill(visited[i], (short) 0);
        }
        this.willsonsCellGrid = new ArrayList<>();
        for (int i = 0; i < this.generatorHeight; i++) {
            List<Boolean> row = new ArrayList<>();
            for (int j = 0; j < this.generatorWidth; j++) {
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
        for (int i = 0; i < generatorHeight; i++) {
            for (int j = 0; j < generatorWidth; j++) {
                willsonsCellGrid.get(i).set(j, false);
            }
        }

        // Fill unvisited cells
        for (int i = 0; i < generatorHeight; i++) {
            for (int j = 0; j < generatorWidth; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    unvisitedCells.add(new GeneratorCell(i, j));
                }
            }
        }
        visitedCells.clear();
        path.clear();

    }

    /**
     * @param current The cell to set.
     */
    private void setTrue(GeneratorCell current) {
        willsonsCellGrid.get(current.getI()).set(current.getJ(), true);
    }

    public void generate() {

        generateMazeWalls();
        generateRooms(List.of(
            new Entrance(),
            new Shop(),
            new ItemsRoom(),
            new KeyObelisk()
        ));

        // Surround grid with walls
        grid.add(0, new ArrayList<>());
        grid.add(new ArrayList<>());
        for (int i = 0; i < width; i++) {
            grid.get(0).add(new GeneratorCell(0, i, CellType.WALL));
            grid.get(height - 1).add(new GeneratorCell(height - 1, i, CellType.WALL));
        }
        for (int i = 0; i < height; i++) {
            grid.get(i).add(0, new GeneratorCell(i, 0, CellType.WALL));
            grid.get(i).add(new GeneratorCell(i, width - 1, CellType.WALL));
        }
        /// Update all cells coordinates
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid.get(i).get(j).setI(i);
                grid.get(i).get(j).setJ(j);
            }
        }
        generateTraps();
        generateExit();
    }

    private void generateTraps() {
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                GeneratorCell current = grid.get(i).get(j);
                GeneratorCell above = grid.get(i - 1).get(j);
                GeneratorCell below = grid.get(i + 1).get(j);
                GeneratorCell left = grid.get(i).get(j - 1);
                GeneratorCell right = grid.get(i).get(j + 1);
                int totalSurroundingWallCount = 0;
                for (GeneratorCell cell : List.of(above, below, left, right)) {
                    if (cell.getCellType().isWall()) {
                        totalSurroundingWallCount++;
                    }
                }
                if (!current.getCellType().isRoom() && current.getCellType().isPath() &&
                    (
                        (above.getCellType().isWall() && below.getCellType().isWall()) ||
                            (left.getCellType().isWall() && right.getCellType().isWall())
                    )
                    && totalSurroundingWallCount < 3
                    && random.nextDouble() < Globals.TRAP_SPAWN_CHANCE) {
                    current.setCellType(CellType.TRAP);
                }
            }
        }
    }

    /** Generates maze walls. */
    private void generateMazeWalls() {
        this.clearArrays();
        GeneratorCell current = unvisitedCells.remove(
                random.nextInt(unvisitedCells.size())
        );
        visitedCells.add(current);
        setTrue(current);

        while (!unvisitedCells.isEmpty()) {
            GeneratorCell first = unvisitedCells.get(random.nextInt(unvisitedCells.size()));
            current = first;
            do {
                int directionIndex = random.nextInt(4);
                while (!validDirection(current, directionIndex)) {
                    directionIndex = random.nextInt(4);
                }
                path.put(current, directionIndex);
                current = getNextCell(current, directionIndex, 2);
            } while (!visitedCells.contains(current));
            /// Roll back to first cell
            current = first;

            /// Until we reach the end
            while (true) {
                visitedCells.add(current);
                unvisitedCells.remove(current);
                setTrue(current);

                // Look at direction at next cell
                int directionIndex = path.get(current);
                GeneratorCell crossed = getNextCell(current, directionIndex, 1);
                setTrue(crossed);
                current = getNextCell(current, directionIndex, 2);
                if (visitedCells.contains(current)) {
                    path.clear();
                    break;
                }
            }
        }

        /// Fill the final maze grid
        for (int i = 0; i < generatorHeight; i++) {
            ArrayList<GeneratorCell> row = new ArrayList<>();
            for (int j = 0; j < generatorWidth; j++) {
                if (willsonsCellGrid.get(i).get(j)) {
                    row.add(new GeneratorCell(i, j, CellType.PATH));
                } else {
                    row.add(new GeneratorCell(i, j, CellType.WALL));
                }
            }
            grid.add(row);
        }
    }

    /**
     * Checks if it is possible to generate a room in the given location.
     * @param i The row.
     * @param j The column.
     * @param room The room to generate.
     *
     * @return
     * {@link true} if there is enough empty space in the room
     * {@code false} otherwise.
     */
    private boolean isPossibleToGenerateRoom(int i, int j, Room room) {
        for (int k = i; k < i + room.height; k++) {
            for (int l = j; l < j + room.width; l++) {
                if (k < 0 || k >= generatorHeight || l < 0 || l >= generatorWidth) {
                    return false;
                }
                if (grid.get(k).get(l).getCellType() == CellType.ROOM_PATH ||
                        grid.get(k).get(l).getCellType() == CellType.ROOM_WALL) {
                    return false;
                }
                if (k > 0 && l > 0 && k < generatorHeight - 1 && l < generatorWidth - 1) {
                    // check if no room cell is adjacent
                    if (grid.get(k - 1).get(l).getCellType().isRoom() ||
                            grid.get(k + 1).get(l).getCellType().isRoom() ||
                            grid.get(k).get(l - 1).getCellType().isRoom() ||
                            grid.get(k).get(l + 1).getCellType().isRoom()) {
                        return false;
                    }

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
        Queue<GeneratorCell> queue = new LinkedList<>();

        if (room.generatorStrategy == GeneratorStrategy.CENTER) {
            int centerI = generatorHeight / 2;
            int centerJ = generatorWidth / 2;
            queue.add(grid.get(centerI).get(centerJ));
        } else if (room.generatorStrategy == GeneratorStrategy.RANDOM) {
            int i = random.nextInt(generatorHeight);
            int j = random.nextInt(generatorWidth);
            queue.add(grid.get(i).get(j));
        } else if (room.generatorStrategy == GeneratorStrategy.CORNERS) {
            queue.add(grid.get(0).get(0));
            queue.add(grid.get(0).get(generatorWidth - 1));
            queue.add(grid.get(generatorHeight - 1).get(0));
            queue.add(grid.get(generatorHeight - 1).get(generatorWidth - 1));
        } else if (room.generatorStrategy == GeneratorStrategy.SIDES) {
            for (int i = 0; i < generatorHeight; i++) {
                queue.add(grid.get(i).get(0));
                queue.add(grid.get(i).get(generatorWidth - 1));
            }
            for (int j = 0; j < generatorWidth; j++) {
                queue.add(grid.get(0).get(j));
                queue.add(grid.get(generatorHeight - 1).get(j));
            }
        } else if (room.generatorStrategy == GeneratorStrategy.AROUND_CENTER) {
            int centerI = generatorHeight / 2;
            int centerJ = generatorWidth / 2;
            List<GeneratorCell> candidates = new ArrayList<>();
            for (int i = centerI - generatorHeight / 3; i < centerI + generatorHeight / 3; ++i) {
                candidates.add(grid.get(i).get(centerJ - generatorHeight / 3));
                candidates.add(grid.get(i).get(centerJ + generatorHeight / 3));
            }
            for (int j = centerJ - generatorWidth / 3; j < centerJ + generatorWidth / 3; ++j) {
                candidates.add(grid.get(centerI + generatorHeight / 3).get(j));
                candidates.add(grid.get(centerI - generatorHeight / 3).get(j));
            }
            int pick = random.nextInt(candidates.size());
            queue.add(candidates.get(pick));
        }


        while (!queue.isEmpty()) {

            GeneratorCell current = queue.poll();
            int i = current.getI();
            int j = current.getJ();
            if (visited[i][j] > 0) {
                continue;
            }
            visited[i][j] = 1;
            if (isPossibleToGenerateRoom(i, j, room)) {
                generated = true;
                List<GeneratorCell> roomCells = new ArrayList<>();
                for (int k = i; k < i + room.height; k++) {
                    for (int l = j; l < j + room.width; l++) {
                        GeneratorCell cell = grid.get(k).get(l);
                        roomCells.add(cell);
                        cell.setRoom(room);
                    }
                }
                room.setLocation(i, j);
                room.setRoomCells(roomCells);
                room.generate(grid, random);

                break;
            }
            /// Out of bounds safety checks
            if (i + 1 < generatorHeight) queue.add(grid.get(i + 1).get(j));
            if (i - 1 >= 0)     queue.add(grid.get(i - 1).get(j));
            if (j + 1 < generatorWidth) queue.add(grid.get(i).get(j + 1));
            if (j - 1 >= 0)     queue.add(grid.get(i).get(j - 1));


        }
        if (!generated) {
            throw new IllegalArgumentException("Room could not be generated.");
        }
    }

    /**
     * <p>Clears the visited array.</p>
     */
    private void clearVisited() {
        for (int i = 0; i < generatorHeight; i++) {
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

        fixReachability();
        fixDoubleWalls();
        fixCorners();
    }


    /**
     * <p>Generates an exit cell on the top of the maze</p>
     */
    public void generateExit() {
        ArrayList<GeneratorCell> eligibleCells = new ArrayList<>();
        for (int j = 0; j < width; ++j) {
            boolean wallBelow = grid.get(1).get(j).getCellType().isWall();
            if (!wallBelow) {
                eligibleCells.add(grid.get(0).get(j));
            }
        }
        if (eligibleCells.isEmpty()) {
            throw new IllegalArgumentException("No eligible cells for exit door");
        }
        int doorPosition = random.nextInt(eligibleCells.size());
        eligibleCells.get(doorPosition).setCellType(CellType.EXIT_DOOR);
    }

    private void fixDoubleWalls() {
        for (int i = 1; i < generatorHeight; i++) {
            for (int j = 1; j < generatorWidth; j++) {
                boolean leftWall = grid.get(i).get(j - 1).getCellType() == CellType.WALL;
                boolean currentRoomWall = grid.get(i).get(j).getCellType() == CellType.ROOM_WALL;
                boolean currentWall = grid.get(i).get(j).getCellType().isWall();
                boolean topWall = grid.get(i - 1).get(j).getCellType().isWall();
                boolean bottomWall = i > generatorHeight - 2 || grid.get(i + 1).get(j).getCellType().isWall();
                if (currentRoomWall && leftWall) {
                    grid.get(i).get(j - 1).setCellType(CellType.PATH);
                }
                boolean is3wallVerticalBlock;
                if (!(i > 1 && i < generatorHeight - 1)) is3wallVerticalBlock = true;
                else {
                    boolean underBottomWall = i < generatorHeight - 2 && grid.get(i + 2).get(j).getCellType().isWall();
                    boolean aboveTopWall = grid.get(i - 2).get(j).getCellType().isWall();
                    is3wallVerticalBlock = (topWall && currentWall && bottomWall) ||
                        (currentWall && bottomWall && underBottomWall) ||
                        (aboveTopWall && topWall && currentWall);
                }
                boolean currentCellIsRoom = grid.get(i).get(j).getCellType().isRoom();
                boolean topCellIsRoom = grid.get(i - 1).get(j).getCellType().isRoom();
                if (topWall && currentWall && !(topCellIsRoom && currentCellIsRoom) && !is3wallVerticalBlock) {
                    if (topCellIsRoom) {
                        grid.get(i).get(j).setCellType(CellType.PATH);
                    } else {
                        grid.get(i - 1).get(j).setCellType(CellType.PATH);
                    }
                    System.out.println("Fixing double walls");
                }

            }
        }
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
        for (int i = 1; i < generatorHeight - 1; i++) {
            for (int j = 1; j < generatorWidth - 1; j++) {
                boolean currentIsRoom = grid.get(i).get(j).getCellType().isRoom();

                boolean topWall = grid.get(i - 1).get(j).getCellType().isWall();
                boolean bottomWall = grid.get(i + 1).get(j).getCellType().isWall();
                boolean leftWall = grid.get(i).get(j - 1).getCellType().isWall();
                boolean rightWall = grid.get(i).get(j + 1).getCellType().isWall();
                boolean currentWall = grid.get(i).get(j).getCellType().isWall();
                boolean leftDiagonalTopWall = grid.get(i - 1).get(j - 1).getCellType().isWall();

                boolean leftDiagonalTop = grid.get(i - 1).get(j - 1).getCellType() == CellType.PATH;
                boolean rightDiagonalTop = grid.get(i - 1).get(j + 1).getCellType() == CellType.PATH;
                boolean leftDiagonalBottom = grid.get(i + 1).get(j - 1).getCellType() == CellType.PATH;
                boolean rightDiagonalBottom = grid.get(i + 1).get(j + 1).getCellType() == CellType.PATH;

                if (grid.get(i).get(j).getCellType().isPath() &&
                        (topWall && leftWall && leftDiagonalTop) ||
                        (topWall && rightWall && rightDiagonalTop) ||
                        (bottomWall && leftWall && leftDiagonalBottom) ||
                        (bottomWall && rightWall && rightDiagonalBottom)) {
                    GeneratorCell temp = grid.get(i).get(j);
                    // Check so that we are consistent with the cell type (room or not)
                    temp.setCellType(
                            temp.getRoom() == null ? CellType.WALL : CellType.ROOM_WALL
                    );
                }
                if (!currentIsRoom && currentWall && leftWall && topWall && leftDiagonalTopWall) {
                    grid.get(i).get(j).setCellType(CellType.PATH);
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
        Queue<GeneratorCell> queue = new LinkedList<>();

        /// Flood fill disjoint components
        while (true) {
            for (int i = 0; i < generatorHeight; i++) {
                for (int j = 0; j < generatorWidth; j++) {
                    GeneratorCell cell = grid.get(i).get(j);
                    if (cell.getCellType().isWalkable() && visited[i][j] == 0) {
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
                GeneratorCell current = queue.poll();
                int i = current.getI();
                int j = current.getJ();
                if (visited[i][j] > 0 || !grid.get(i).get(j).getCellType().isWalkable()) {
                    continue;
                }

                visited[i][j] = counter;
                if (i + 1 < generatorHeight) queue.add(grid.get(i + 1).get(j));
                if (i - 1 >= 0)     queue.add(grid.get(i - 1).get(j));
                if (j + 1 < generatorWidth) queue.add(grid.get(i).get(j + 1));
                if (j - 1 >= 0)     queue.add(grid.get(i).get(j - 1));
            }
            ++counter;
        }
        List<PassagePossibility> possibilities = new ArrayList<>();
        DSU dsu = new DSU(counter);

        /// Find all possible passages
        for (int i = 0; i < generatorHeight; i++) {
            for (int j = 0; j < generatorWidth; j++) {
                GeneratorCell cell = grid.get(i).get(j);
                if (cell.getCellType() == CellType.WALL) {
                    GeneratorCell above = i - 1 >= 0 ? grid.get(i - 1).get(j) : null;
                    GeneratorCell below = i + 1 < generatorHeight ? grid.get(i + 1).get(j) : null;
                    GeneratorCell left = j - 1 >= 0 ? grid.get(i).get(j - 1) : null;
                    GeneratorCell right = j + 1 < generatorWidth ? grid.get(i).get(j + 1) : null;

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
        /// Randomize the possibilities
        Collections.shuffle(possibilities, random);
        for (PassagePossibility possibility : possibilities) {
            GeneratorCell a = possibility.a();
            GeneratorCell b = possibility.b();
            GeneratorCell wall = possibility.wall();
            if (dsu.same(visited[a.getI()][a.getJ()], visited[b.getI()][b.getJ()])) {
                continue;
            }
            wall.setCellType(CellType.PATH);
            dsu.union(visited[a.getI()][a.getJ()], visited[b.getI()][b.getJ()]);
        }

    }

    /**
     * <p>Gets the next cell in the given direction.</p>
     * @param current The current cell.
     * @param directionIndex The index of the direction.
     * @param distance The distance to the next cell.
     * @return {@link GeneratorCell} The next cell.
     */
    private GeneratorCell getNextCell(GeneratorCell current, int directionIndex, int distance) {
        List<Integer> direction = directions.get(directionIndex);
        return new GeneratorCell(
            current.getI() + direction.get(0) * distance,
            current.getJ() + direction.get(1) * distance
        );
    }

    /**
     * <p>Checks if the applied direction won't go out of bounds. I.e. - valid</p>
     * @param cell The current cell.
     * @param directionIndex The index of the direction.
     * @return {@code True} if the direction is valid, false otherwise.
     */
    private boolean validDirection(GeneratorCell cell, int directionIndex) {
        GeneratorCell newCell = getNextCell(cell, directionIndex, 2);
        return newCell.getI() >= 0 && newCell.getI() < generatorHeight &&
            newCell.getJ() >= 0 && newCell.getJ() < generatorWidth;
    }


    @Override
    public String toString() {
        Character spacing = ' ';
        StringBuilder sb = new StringBuilder();
        for (List<GeneratorCell> generatorCells : grid) {
            for (GeneratorCell generatorCell : generatorCells) {
                switch (generatorCell.getCellType()) {
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

    public Random getRandom() {
        return random;
    }
}
