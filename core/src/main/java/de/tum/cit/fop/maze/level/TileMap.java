package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.BodyBits;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.entities.EntityPathfinder;
import de.tum.cit.fop.maze.entities.tile.TileEntityManager;
import de.tum.cit.fop.maze.entities.tile.Torch;
import de.tum.cit.fop.maze.entities.tile.Trap;
import de.tum.cit.fop.maze.entities.tile.TrapType;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static de.tum.cit.fop.maze.Globals.*;
import static java.lang.Math.max;

public class TileMap implements Disposable {
    private final TiledMap map;
    public final int width;
    public final int height;
    public final float heightMeters;
    public final float widthMeters;
    public final EntityPathfinder pathfinder;
    private final TextureLoader textures;
    private final MazeGenerator generator;
    private final TileEntityManager tileEntityManager;

    /**
     * Create a new TileMap with a given height, width and seed
     * @param height the height of the map
     * @param width the width of the map
     * @param seed the seed for the map
     */
    public TileMap(int height, int width, int seed) {
        this.generator = new MazeGenerator(height, width, seed);
        this.map = new TiledMap();
        ArrayList<AbsolutePoint> torches = new ArrayList<>();
        // Always get width and height from the generator, because it always makes the parameters odd
        generator.generate();
        this.width = generator.width * 3;
        this.height = generator.height * 3;
        this.tileEntityManager = LevelScreen.getInstance().tileEntityManager;
        System.out.println(generator);
        widthMeters = this.width * CELL_SIZE_METERS;
        heightMeters = this.height * CELL_SIZE_METERS;
        boolean[][] wallMap = new boolean[this.height][this.width];

        textures = new TextureLoader("assets/tiles/tiles.atlas", generator.getRandom());
        createDebugLayer();
        System.out.println(generator);


        ///  Dimensions x3 since we want to have 3x3 tiles for each cell,
        ///  so we create larger environment for fights, etc.
        TiledMapTileLayer layer =
            new TiledMapTileLayer(this.width, this.height,
                Globals.CELL_SIZE,
                Globals.CELL_SIZE
            );
        map.getLayers().add(layer);



        // System.out.println(generator);
        // Paddings to account for surrounding walls
        int startI = 2;
        int startJ = 1;
        for (int i = generator.height - 1; i >= 0; --i) {
            for (int j = generator.width - 1; j >= 0; --j) {
                final GeneratorCell cell = generator.grid.get(i).get(j);
                int x = startJ + j * 3;
                int y = layer.getHeight() - (startI + i * 3);
                DebugRenderer.getInstance().spawnRectangle(
                    new AbsolutePoint(x - 1, y - 1).toMetersFromCells(),
                    new AbsolutePoint(x + 2, y + 2).toMetersFromCells(),
                    Color.BLACK
                );
                /// All non-walkable cells require hitboxes
                if (cell.getCellType().isWall()) {
                    setWallSquare(x, y, wallMap);

                }
                ///  Floor
                if (cell.getCellType().isWalkable()) {
                    setSquare(textures.getTextureWithVariationChance("floor"), x, y);
                    tryTorchSpawn(i, j, cell, x, y, torches);
                }
                ///  Room walls
                if (cell.getCellType() == CellType.WALL || cell.getCellType() == CellType.ROOM_WALL) {
                    setDefaultWallSquare(x, y);
                    if (GenerationCases.verticalWallCase(i, j, generator)) {
                        setVerticalWallSquare(x, y);
                    }
                    if (GenerationCases.topVerticalCase(i, j, generator)) {
                        setVerticalWallSquare(x, y);
                        TextureRegion leftCorner =
                            textures.getTextureWithVariationChance("wallVerticalLeftCorner");
                        TextureRegion middleCorner =
                            textures.getTextureWithVariationChance("wallVerticalMiddleCorner");
                        TextureRegion rightCorner =
                            textures.getTextureWithVariationChance("wallVerticalRightCorner");

                        setCell(leftCorner, x - 1, y + 1, true);
                        setCell(middleCorner, x, y + 1, true);
                        setCell(rightCorner, x + 1, y + 1, true);
                    }
                    if (GenerationCases.singleWallCase(i, j, generator)) {
                        setDecorationSquare(x, y);
                    }

                }

                /// Trap
                if (cell.getCellType() == CellType.TRAP && !GenerationCases.isEdge(i, j, generator)) {
                    boolean vertical =
                        this.generator.grid.get(i - 1).get(j).getCellType().isWall() &&
                            this.generator.grid.get(i + 1).get(j).getCellType().isWall();
                    spawnRandomTrap(x, y, vertical);
                }

            }
        }
        reverseCollisionMapRows(wallMap);
        generateHitboxes(wallMap);
        pathfinder = new EntityPathfinder();

    }

    private void tryTorchSpawn(int i, int j, GeneratorCell cell, int x, int y, ArrayList<AbsolutePoint> torches) {
        AbsolutePoint closestTorch = null;
        GeneratorCell torchCell = GenerationCases.getFirstSurroundingWall(i, j, generator);
        if (
            cell.getCellType() != CellType.TRAP &&
                !GenerationCases.isEdge(i, j, generator) &&
                torchCell != null
        ) {
            Direction torchDirection = cell.getDirection(torchCell);
            AbsolutePoint current = getCellCenterMeters(x, y);
            AbsolutePoint torchPoint = getTorchPoint(torchDirection, current);

            for (AbsolutePoint torch : torches) {
                if (closestTorch != null && torchPoint.distance(closestTorch) > torchPoint.distance(torch)) {
                    closestTorch = torch;
                } else if (closestTorch == null) {
                    closestTorch = torch;
                }
            }

            if (closestTorch != null && torchPoint.distance(closestTorch) < Torch.activationRadius * TORCH_GAP) {
                return;
            }
            torches.add(torchPoint);
            tileEntityManager.createTileEntity(
                new Torch(torchDirection), torchPoint.x(), torchPoint.y()
            );
            DebugRenderer.getInstance().spawnCircle(current, 0.25f);
        }
    }

    private static AbsolutePoint getTorchPoint(Direction torchDirection, AbsolutePoint current) {
        AbsolutePoint torchPoint;
        switch (
            torchDirection
        ) {
            case UP -> torchPoint = current.addY(CELL_SIZE_METERS * 3);
            case DOWN -> torchPoint = current.addY(-CELL_SIZE_METERS * 1.25f);
            case LEFT -> torchPoint = current.addX(-CELL_SIZE_METERS);
            case RIGHT -> torchPoint = current.addX(CELL_SIZE_METERS);
            default -> throw new IllegalStateException("Unexpected value: " + torchDirection);
        }
        return torchPoint;
    }

    /**
     *
     */
    private AbsolutePoint getCellCenterMeters(int x, int y) {
        return new AbsolutePoint(x + 0.5f, y + 0.5f).toMetersFromCells();
    }

    /**
     * Spawn a random trap at x y position
     * @param x the x position <b>(must be 3x3 cell center)</b>
     * @param y the y position <b>(must be 3x3 cell center)</b>
     * @param vertical {@code true} if the passage is vertical
     */
    private void spawnRandomTrap(int x, int y, boolean vertical) {

        AbsolutePoint center = getCellCenterMeters(x, y);
        DebugRenderer.getInstance().spawnCircle(center, 0.25f);
        float xActual = center.x();
        float yActual = center.y();
        if (!vertical) --yActual;
        TrapType trapType = vertical ?
            TrapType.pickRandomHorizontalTrap(generator.getRandom()) :
            TrapType.pickRandomVerticalTrap(generator.getRandom());

        Trap trap = new Trap(trapType);
        boolean singleCellTrap = trap.attributes.width() == 1 && trap.attributes.height() == 1;

        ///  If the trap is longer or higher than 3 cells (passage size),
        ///  we need to align it properly towards the carrying wall
        xActual -= max(0, trap.attributes.width() - 3) / 2f;
        yActual += max(0, trap.attributes.height() - 3) / 2f;
        if (singleCellTrap) {
            tileEntityManager.createTileEntity(trap, xActual, yActual);
            tileEntityManager.createTileEntity(
                new Trap(trapType), xActual - (!vertical ? 1 : 0), yActual - (vertical ? 1 : 0)
            );
            tileEntityManager.createTileEntity(
                new Trap(trapType), xActual + (!vertical ? 1 : 0), yActual + (vertical ? 1 : 0)
            );
            return;
        }
        tileEntityManager.createTileEntity(trap, xActual, yActual);
    }


    /**
     * Reverse the collision map rows
     *
     * @param collisionMap the collision map with the cells
     */
    private void reverseCollisionMapRows(boolean[][] collisionMap) {
        for (int i = 0; i < height / 2; ++i) {
            for (int j = 0; j < width; ++j) {
                boolean tmp = collisionMap[i][j];
                collisionMap[i][j] = collisionMap[height - 1 - i][j];
                collisionMap[height - 1 - i][j] = tmp;
            }
        }
    }

    /**
     * Set a wall square for the wallMap at x y position
     *
     * @param x       the x position
     * @param y       the y position
     * @param wallMap the collision map with the cells
     */
    private void setWallSquare(int x, int y, boolean[][] wallMap) {
        int[][] allSurrounding = {
            {0, 0},
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1},
            {-1, -1},
            {1, -1},
            {-1, 1},
            {1, 1}
        };
        ///  Height is reversed since the map is drawn from the top left corner
        for (int[] surrounding : allSurrounding) {
            wallMap[height - 1 - y - surrounding[1]][x + surrounding[0]] = true;
        }
    }

    private void setHorizontalWallStack(TextureRegion top, TextureRegion middle, TextureRegion bottom, int x, int y) {
        for (int k = -1; k < 2; ++k) {
            setCell(top, x + k, y + 1);
        }

        for (int k = -1; k < 2; ++k) {
            setCell(middle, x + k, y);
        }

        for (int k = -1; k < 2; ++k) {
            setCell(bottom, x + k, y - 1);
        }
    }

    private void setVerticalWallStack(TextureRegion left, TextureRegion middle, TextureRegion right, int x, int y) {
        for (int k = -1; k < 2; ++k) {
            setCell(left, x - 1, y + k, true);
        }

        for (int k = -1; k < 2; ++k) {
            setCell(middle, x, y + k);
        }

        for (int k = -1; k < 2; ++k) {
            setCell(right, x + 1, y + k);
        }
    }

    /**
     * Set a vertical wall at x y position
     * @param x the x position
     * @param y the y position
     */
    private void setVerticalWallSquare(int x, int y) {
        TextureRegion wallLeft =  textures.getTextureWithVariationChance("wallVerticalLeft");
        TextureRegion wallMiddle = textures.getTextureWithVariationChance("wallVerticalMiddle");
        TextureRegion wallRight = textures.getTextureWithVariationChance("wallVerticalRight");

        setVerticalWallStack(wallLeft, wallMiddle, wallRight, x, y);
    }

    /**
     * Set a default wall at x y position
     * @param x the x position
     * @param y the y position
     */
    private void setDefaultWallSquare(int x, int y) {
        TextureRegion wallTop = textures.getTextureWithVariationChance("wallTop");
        TextureRegion wallMiddle = textures.getTextureWithVariationChance("wallMiddle");
        TextureRegion wallBottom = textures.getTextureWithVariationChance("wallBottom");

        setHorizontalWallStack(wallTop, wallMiddle, wallBottom, x, y);
    }

    /**
     * Spawn a decoration at {@code x} {@code y} position
     * @param x the x position
     * @param y the y position
     */
    private void setDecorationSquare(int x, int y) {
        TextureRegion decoration = textures.getTextureWithVariationChance("decorWater");
        setSquare(decoration, x, y);
    }

    /**
     * Set square 3x3 at x y position to be {@code cell}
     * @param texture the texture to set
     * @param x the x position
     * @param y the y position
     */
    private void setSquare(TextureRegion texture, int x, int y) {
        int[][] allSurrounding = {
            {0, 0},
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1},
            {-1, -1},
            {1, -1},
            {-1, 1},
            {1, 1}
        };
        for (int[] surrounding : allSurrounding) {
            setCell(texture, x + surrounding[0], y + surrounding[1]);
        }
    }

    /**
     * Set cell at x y position to be {@code texture}
     * @param texture the texture to set
     * @param x the x position
     * @param y the y position
     * @param topLayer if the cell should be set in the top layer
     */
    private void setCell(TextureRegion texture, int x, int y, boolean topLayer) {
        Cell mapCell = new Cell();
        mapCell.setTile(
            new StaticTiledMapTile(texture)
        );
        ///  1 is always the top layer and 0 is the bottom layer
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(topLayer ? 1 : 0);
        layer.setCell(x, y, mapCell);
    }

    /**
     * Set cell at x y position to be {@code texture}
     * @param texture the texture to set
     * @param x the x position
     * @param y the y position
     */
    private void setCell(TextureRegion texture, int x, int y) {
        setCell(texture, x, y, false);
    }

    public TiledMap getMap() {
        return map;
    }

    /**
     * Adds a debug layer to the map, which fills the whole map with red
     */
    public void createDebugLayer() {
        TiledMapTileLayer layer =
            new TiledMapTileLayer(this.width, this.height,
                Globals.CELL_SIZE,
                Globals.CELL_SIZE
            );
        Pixmap pixmap = new Pixmap(Globals.CELL_SIZE, Globals.CELL_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.fill();
        Cell cell = new Cell();
        cell.setTile(
            new StaticTiledMapTile(new TextureRegion(new Texture(pixmap)))
        );
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                layer.setCell(i, j, cell);
            }
        }
        map.getLayers().add(layer);
    }

    @Override
    public void dispose() {
        map.dispose();
    }

    /**
     * Helper function to create a hitbox at x y position considering the cell size
     * Create a hitbox at x y position with hx hy size
     * @param x the x position
     * @param y the y position
     * @param hx the x size
     * @param hy the y size
     * @param fixtureDef the fixture definition
     */
    private void createRectangularHitbox(float x, float y, float hx, float hy, @Nullable FixtureDef fixtureDef) {
        // System.out.println("Creating hitbox at " + x + " " + y + " " + hx + " " + hy + " With cell size " + CELL_SIZE_METERS); ;
        hx *= CELL_SIZE_METERS / 2;
        hy *= CELL_SIZE_METERS / 2;
        x = x * CELL_SIZE_METERS + hx;
        y = y * CELL_SIZE_METERS + hy;

        LevelScreen screen = LevelScreen.getInstance();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body body = screen.world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(hx, hy);
        if (fixtureDef == null) {
            fixtureDef = new FixtureDef();
            fixtureDef.filter.categoryBits = BodyBits.WALL;
            fixtureDef.filter.maskBits = BodyBits.WALL_MASK;
        }
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    /**
     * Helper function to create a hitbox at x y position considering the cell size
     * Create a hitbox at x y position with hx hy size
     * @param x the x position
     * @param y the y position
     * @param hx the x size
     * @param hy the y size
     */
    private void createRectangularHitbox(float x, float y, float hx, float hy) {
        createRectangularHitbox(x, y, hx, hy, null);
    }

    /**
     * Check if the cell at i j position is isolated, requires special treatment, since we draw a decoration there
     *
     * @param x       the x position
     * @param y       the y position
     * @param wallMap the collision map with the cells
     * @return {@code true} if the cell is isolated
     */
    private boolean isIsolatedCollidable(int x, int y, boolean[][] wallMap) {
        if (y - 2 <= 0 || x - 2 <= 0 || y + 2 >= height || x + 2 >= width) {
            return false;
        }
        return !wallMap[y - 2][x] && !wallMap[y + 2][x] && !wallMap[y][x - 2] && !wallMap[y][x + 2];
    }

    private void generateVerticalHitboxes(boolean[][] wallMap) {
        for (int j = 0; j < width; j += 3) {
            int x = (j - 1);
            int y = -1;
            int hy = 0;
            for (int i = 0; i < height; i += 3) {
                // System.out.println("Checking " + i + " " + j + " " + wallMap[i][j]);
                if (wallMap[i][j]) {
                    if (y == -1) {
                        y = i;
                    }
                    hy += 3;
                } else {
                    if (y != -1) {
                        if (hy > 3) {
                            /// HY reduced by 0.05 to avoid collision above the wall
                            /// 1.8 is the height of the wall (less than usual 3 because of projection)
                            /// x + 1 and y + 1.8 are the starting point of the wall
                            createRectangularHitbox(x + 1, y + 1.5f + 0.04f, 3, hy - 0.08f - 1.5f);
                        }
                        y = -1;
                        hy = 0;
                    }
                }
            }
            if (y != -1 && hy > 3) {
                createRectangularHitbox(x + 1, y + 1.5f + 0.04f, 3, hy - 0.5f - 1.5f);
            }
        }

    }

    private void generateHorizontalHitboxes(boolean[][] wallMap) {
        for (int i = 0; i < height; i += 3) {
            int x = -1;
            int y = (i - 1);
            int hx = 0;
            for (int j = 0; j < width; j += 3) {
                if (wallMap[i][j]) {
                    if (x == -1) {
                        x = j;
                    }
                    hx += 3;
                } else {
                    if (x != -1) {
                        /// x is offset for 0.025f to avoid collision with the tiny pixel
                        if (hx > 3) {
                            createRectangularHitbox(x + 0.025f, y + 1 + 1.5f, hx - 0.05f, 1.5f);
                        }
                        if (isIsolatedCollidable(j - 2, i + 1, wallMap)) {
                            FixtureDef fixtureDef = new FixtureDef();
                            fixtureDef.filter.categoryBits = BodyBits.DECORATION;
                            createRectangularHitbox(x + 0.025f, y + 1 + 1.5f, hx - 0.05f, 1.5f, fixtureDef);
                        }

                        x = -1;
                        hx = 0;
                    }
                }
            }
            if (x != -1 && hx > 3) {
                createRectangularHitbox(x + 0.025f, y + 1 + 1.5f, hx - 0.05f, 1.5f);
            }
        }
    }

    /**
     * Generate hitboxes for the map
     *
     * @param wallMap the collision map with the cells
     */
    private void generateHitboxes(boolean[][] wallMap) {
        generateVerticalHitboxes(wallMap);
        generateHorizontalHitboxes(wallMap);
    }
}
