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
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.tum.cit.fop.maze.essentials.Assets;
import de.tum.cit.fop.maze.essentials.BodyBits;
import de.tum.cit.fop.maze.essentials.TileTextureHelper;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.EnemyType;
import de.tum.cit.fop.maze.entities.tile.*;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Direction;
import de.tum.cit.fop.maze.gson.GSONPostRestorable;
import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.GeneratorCell;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import de.tum.cit.fop.maze.level.worldgen.rooms.Entrance;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static de.tum.cit.fop.maze.essentials.Globals.*;
import static java.lang.Math.max;

public class TileMap implements Disposable, GSONPostRestorable {
    private final TiledMap map = new TiledMap();
    public int width;
    public int height;
    private AbsolutePoint exitPosition;
    boolean[][] wallMap;
    public AbsolutePoint playerPosition;
    public transient float heightMeters;
    public transient float widthMeters;
    public transient Random random;
    private transient MazeGenerator generator;
    private transient final TileEntityManager tileEntityManager;
    private transient HashSet<Collectable> spawnedItems = new HashSet<>();

    /**
     * Create a new TileMap from Gson
     */
    private TileMap() {
        this.tileEntityManager = LevelScreen.getInstance().tileEntityManager;
        this.random = LevelScreen.getInstance().random;
    }


    public void restore() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        this.width = layer.getWidth();
        this.height = layer.getHeight();
        this.widthMeters = this.width * CELL_SIZE_METERS;
        this.heightMeters = this.height * CELL_SIZE_METERS;
        generateHitboxes();
    }


    public TileMap(MazeGenerator generator) {
        this(generator.height, generator.width, generator.getRandom(), generator);
    }

    public TileMap(int height, int width, Random random) {
        this(height, width, random, null);
    }
    /**
     * Create a new TileMap with a given height, width and seed
     * @param height the height of the map
     * @param width the width of the map
     * @param random the random instance to use
     */
    private TileMap(int height, int width, Random random, MazeGenerator generator) {
        if (generator == null) {
            this.generator = new MazeGenerator(height, width, random);
            this.generator.generate();
        } else {
            this.generator = generator;
        }

        ArrayList<AbsolutePoint> torches = new ArrayList<>();
        // Always get width and height from the generator, because it always makes the parameters odd

        this.random = random;
        this.width = this.generator.width * 3;
        this.height = this.generator.height * 3;
        this.tileEntityManager = LevelScreen.getInstance().tileEntityManager;
        System.out.println(generator);
        widthMeters = this.width * CELL_SIZE_METERS;
        heightMeters = this.height * CELL_SIZE_METERS;
        wallMap = new boolean[this.height][this.width];
        createDebugLayer();
        System.out.println(generator);

        ///  Dimensions x3 since we want to have 3x3 tiles for each cell,
        ///  so we create larger environment for fights, etc.
        TiledMapTileLayer layer =
            new TiledMapTileLayer(this.width, this.height,
                CELL_SIZE,
                CELL_SIZE
            );
        map.getLayers().add(layer);
        ///  It is fine to have an unchecked cast since the given array is always a list of CollectableAttributes.
        ///  Shallow copy of arrays to spawn items.
        @SuppressWarnings("unchecked") List<CollectableAttributes> shopPool =
            (List<CollectableAttributes>) Assets.getInstance().getShopPool().clone();
        @SuppressWarnings("unchecked") List<CollectableAttributes> treasurePool =
            (List<CollectableAttributes>) Assets.getInstance().getTreasurePool().clone();

        // System.out.println(generator);
        // Paddings to account for surrounding walls
        int startI = 2;
        int startJ = 1;
        for (int i = this.generator.height - 1; i >= 0; --i) {
            for (int j = this.generator.width - 1; j >= 0; --j) {
                final GeneratorCell cell = this.generator.grid.get(i).get(j);
                int x = startJ + j * 3;
                int y = layer.getHeight() - (startI + i * 3);
                AbsolutePoint currentCellCenter = getCellCenterMeters(x, y);

                DebugRenderer.getInstance().spawnRectangle(
                    new AbsolutePoint(x - 1, y - 1).toMetersFromCells(),
                    new AbsolutePoint(x + 2, y + 2).toMetersFromCells(),
                    Color.BLACK
                );
                /// All non-walkable cells require hitboxes
                if (cell.getCellType().isWall()) {
                    setHitboxWallSquare(x, y, wallMap);
                }
                /// Any cell that is not a wall is walkable, floor would be a background
                if (cell.getCellType().isWalkable()) {
                    if (cell.getCellType().isRoom() && !(cell.getRoom() instanceof Entrance)) {
                        setSquare("floor_room", x, y);
                    } else {
                        setSquare("floor", x, y);
                    }
                    tryTorchSpawn(i, j, cell, x, y, torches);
                }
                if (cell.getCellType().isPath() && !cell.getCellType().isRoom() &&
                    !this.generator.loadedFromProperties) {
                    if (random.nextFloat() <= ENEMY_SPAWN_CHANCE) {
                        spawnEnemies(x, y);
                    } else if (random.nextFloat() <= LOOTCONTAINER_SPAWN_CHANCE) {
                        spawnLootContainers(x, y, i, j);
                        ///(i, j, cell, x, y, torches);
                    }
                }
                if (cell.getCellType() == CellType.ENEMY) {
                    LevelScreen.getInstance().enemyManager.createEnemy(
                        new Enemy(Arrays.stream(
                            EnemyType.values()).skip(random.nextInt(EnemyType.values().length)
                        ).findFirst().get()),
                        currentCellCenter.x(),
                        currentCellCenter.y()
                    );
                }

                ///  Room walls
                if (cell.getCellType() == CellType.WALL || cell.getCellType() == CellType.ROOM_WALL) {
                    setDefaultWallSquare(x, y);
                    if (GenerationCases.verticalWallCase(i, j, this.generator)) {
                        setVerticalWallSquare(x, y);
                    }
                    if (GenerationCases.topVerticalCase(i, j, this.generator)) {
                        setVerticalWallSquare(x, y);
                        setCell("wallVerticalLeftCorner", x - 1, y + 1);
                        setCell("wallVerticalMiddleCorner", x, y + 1);
                        setCell("wallVerticalRightCorner", x + 1, y + 1);
                    }
                }
                if (cell.getCellType() == CellType.KEY_OBELISK) {
                    tileEntityManager.createTileEntity(
                        new Collectable(Collectable.CollectableType.KEY), currentCellCenter
                    );
                }
                if (cell.getCellType() == CellType.TREASURE_ROOM_ITEM) {
                    CollectableAttributes attribute = null;
                    for (CollectableAttributes attr : treasurePool) {
                        if (attr.spawnPriority) {
                            attribute = attr;
                            break;
                        }
                    }
                    if (attribute == null) {
                        attribute = treasurePool.get(random.nextInt(treasurePool.size()));
                    }
                    treasurePool.remove(attribute);
                    tileEntityManager.createTileEntity(new Collectable(attribute), currentCellCenter);
                }
                if (cell.getCellType() == CellType.EXIT_DOOR) {
                    GeneratorCell pathCell = GenerationCases.getFirstSurroundingPath(i, j, this.generator);
                    if (pathCell == null) {
                        throw new IllegalStateException("EXIT_DOOR is unreachable in this generation");
                    }
                    tileEntityManager.createTileEntity(
                        new ExitDoor(pathCell.getDirection(cell)), currentCellCenter
                    );
                    this.exitPosition = currentCellCenter;
                }
                if (cell.getCellType() == CellType.SHOP_ITEM) {
                    CollectableAttributes item = shopPool.get(random.nextInt(shopPool.size()));
                    shopPool.remove(item);
                    tileEntityManager.createTileEntity(
                        new ShopItem(new Collectable(item)), currentCellCenter
                    );
                }

                /// Trap
                if (cell.getCellType() == CellType.TRAP && !GenerationCases.isEdge(i, j, this.generator)) {
                    boolean vertical =
                        (this.generator.grid.get(i - 1).get(j).getCellType().isWall() ||
                            this.generator.grid.get(i - 1).get(j).getCellType().isDoor())
                            &&
                            (this.generator.grid.get(i + 1).get(j).getCellType().isWall() ||
                                this.generator.grid.get(i + 1).get(j).getCellType().isDoor());
                    spawnRandomTrap(x, y, vertical);
                }

                if (cell.getCellType() == CellType.PLAYER) {
                    setSquare("floor_room", x, y);
                    playerPosition = currentCellCenter;
                }



            }
        }
        reverseCollisionMapRows(wallMap);
        generateHitboxes();
    }

    public void spawnEnemies(int x, int y) {
        AbsolutePoint current = getCellCenterMeters(x, y);
        int enemyCount = 1;
        for (int i = 0; i < 8; ++i) {
            if (random.nextFloat() <= ENEMY_SPAWN_DENSITY) {
                ++enemyCount;
            }
        }
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if (enemyCount == 0) {
                    return;
                }
                if (i == 0 && j == 0) {
                    continue;
                }
                LevelScreen.getInstance().enemyManager.createEnemy(
                    new Enemy(
                        Arrays.stream(
                            EnemyType.values()).skip(random.nextInt(EnemyType.values().length)
                        ).findFirst().get()
                    ),
                    current.x() + i * CELL_SIZE_METERS,
                    current.y() + j * CELL_SIZE_METERS
                );
                --enemyCount;
            }
        }

    }

    private void spawnLootContainers(int x, int y, int iOrigin, int jOrigin) {
        AbsolutePoint current = getCellCenterMeters(x, y);
        int lootContainerCount = 1;
        for (int i = 0; i < 8; ++i) {
            if (random.nextFloat() <= LOOTCONTAINER_SPAWN_DENSITY) {
                ++lootContainerCount;
            }
        }
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if (lootContainerCount == 0) {
                    return;
                }
                if ((i == 0 && j == 0) || generator.grid.get(iOrigin).get(jOrigin + j).getCellType().isWall()) {
                    continue;
                }

                LevelScreen.getInstance().tileEntityManager.createTileEntity(
                    new LootContainer(
                        Arrays.stream(
                            LootContainer.LootContainerType.values()).skip(
                            random.nextInt(LootContainer.LootContainerType.values().length
                            )
                        ).findFirst().get()
                    ),
                    current.x() + j * CELL_SIZE_METERS,
                    current.y() + i * CELL_SIZE_METERS + CELL_SIZE_METERS / 1.5f
                );
                --lootContainerCount;
            }
        }
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

            if (closestTorch != null && torchPoint.distance(closestTorch) < TORCH_ACTIVATION_RADIUS * TORCH_GAP) {
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
            case DOWN -> torchPoint = current.addY(-CELL_SIZE_METERS);
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
     * Spawns a random trap at x y position
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
     * Reverses the collision map rows
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
     * Sets a wall square for the wallMap at x y position
     *
     * @param x       the x position
     * @param y       the y position
     * @param wallMap the collision map with the cells
     */
    private void setHitboxWallSquare(int x, int y, boolean[][] wallMap) {
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

    private void setHorizontalWallStack(int x, int y) {
        for (int k = -1; k < 2; ++k) {
            setCell("wallTop", x + k, y + 1);
        }

        for (int k = -1; k < 2; ++k) {
            setCell("wallMiddle", x + k, y);
        }

        for (int k = -1; k < 2; ++k) {
            setCell("wallBottom", x + k, y - 1);
        }
    }

    private void setVerticalWallStack(int x, int y) {
        for (int k = -1; k < 2; ++k) {
            setCell("wallVerticalLeft", x - 1, y + k);
        }

        for (int k = -1; k < 2; ++k) {
            setCell("wallVerticalMiddle", x, y + k);
        }

        for (int k = -1; k < 2; ++k) {
            setCell("wallVerticalRight", x + 1, y + k);
        }
    }

    /**
     * Sets a vertical wall at x y position
     * @param x the x position
     * @param y the y position
     */
    private void setVerticalWallSquare(int x, int y) {
        setVerticalWallStack(
            x, y);
    }

    /**
     * Sets a default wall at x y position
     * @param x the x position
     * @param y the y position
     */
    private void setDefaultWallSquare(int x, int y) {
        setHorizontalWallStack(
            x, y
        );
    }

    /**
     * Sets square 3x3 at x y position to be {@code cell}
     * @param textureName the texture to set
     * @param x the x position
     * @param y the y position
     */
    private void setSquare(String textureName, int x, int y) {
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
            setCell(textureName, x + surrounding[0], y + surrounding[1]);
        }
    }

    /**
     * Sets cell at x y position to be {@code texture}
     * @param textureName the texture to set
     * @param x the x position
     * @param y the y position
     */
    private void setCell(String textureName, int x, int y) {
        Cell mapCell = new Cell();
        TileTextureHelper.TextureResult result = Assets.getInstance().
            tileTextureHelper.getTextureWithVariationChance(textureName, random);
        mapCell.setTile(
            new StaticTiledMapTile(
                result.textureRegion()
            )
        );
        mapCell.getTile().getProperties().put(
            "textureName", textureName
        );
        mapCell.getTile().getProperties().put(
            "index", result.index()
        );
        ///  1 is always the top layer and 0 is the bottom layer
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        layer.setCell(x, y, mapCell);
    }


    /**
     * Adds a debug layer to the map, which fills the whole map with red
     */
    public void createDebugLayer() {
        TiledMapTileLayer layer =
            new TiledMapTileLayer(this.width, this.height,
                CELL_SIZE,
                CELL_SIZE
            );
        Pixmap pixmap = new Pixmap(CELL_SIZE, CELL_SIZE, Pixmap.Format.RGBA8888);
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
     * Calls {@link #createRectangularHitbox(float, float, float, float, FixtureDef)} with {@code fixtureDef} set to null
     */
    private void createRectangularHitbox(float x, float y, float hx, float hy) {
        createRectangularHitbox(x, y, hx, hy, null);
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
     * Check if the cell at i j position is isolated, requires special treatment, since we draw a decoration there
     *
     * @param x       the x position
     * @param y       the y position
     * @return {@code true} if the cell is isolated
     */
    private boolean isIsolatedCollidable(int x, int y) {
        if (y - 2 <= 0 || x - 2 <= 0 || y + 2 >= height || x + 2 >= width) {
            return false;
        }
        return !wallMap[y - 2][x] && !wallMap[y + 2][x] && !wallMap[y][x - 2] && !wallMap[y][x + 2];
    }

    /**
     * Generate hitboxes for the map
     */
    private void generateHitboxes() {
        generateVerticalHitboxes();
        generateHorizontalHitboxes();
    }

    private void generateVerticalHitboxes() {
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
                            FixtureDef temp = new FixtureDef();
                            temp.filter.categoryBits = BodyBits.WALL_TRANSPARENT;
                            temp.filter.maskBits = BodyBits.WALL_TRANSPARENT_MASK;
                            createRectangularHitbox(
                                x + 1, y + HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, 3f,
                                HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, temp);
                            createRectangularHitbox(x + 1f, y + 2.8f, 2.95f, hy - 2.8f);
                        }
                        y = -1;
                        hy = 0;
                    }
                }
            }
            if (y != -1 && hy > 3) {
                FixtureDef temp = new FixtureDef();
                temp.filter.categoryBits = BodyBits.WALL_TRANSPARENT;
                temp.filter.maskBits = BodyBits.WALL_TRANSPARENT_MASK;
                createRectangularHitbox(
                    x + 1, y + HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, 3f,
                    HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, temp);
                createRectangularHitbox(x + 1, y + 3f, 3, hy - 2.8f);
            }
        }

    }

    private void generateHorizontalHitboxes() {
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
                        if (hx > 3 || isIsolatedCollidable(j - 2, i + 1)) {
                            FixtureDef temp = new FixtureDef();
                            temp.filter.categoryBits = BodyBits.WALL_TRANSPARENT;
                            temp.filter.maskBits = BodyBits.WALL_TRANSPARENT_MASK;
                            createRectangularHitbox(
                                x + 0.04f, y + 1 + HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, hx - 0.07f,
                                HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, temp);
                            createRectangularHitbox(x + 0.12f, y + 3f + 0.6f, hx - 0.24f, 0.3f);
                        }

                        x = -1;
                        hx = 0;
                    }
                }
            }
            if (x != -1 && hx > 3) {
                FixtureDef temp = new FixtureDef();
                temp.filter.categoryBits = BodyBits.WALL_TRANSPARENT;
                temp.filter.maskBits = BodyBits.WALL_TRANSPARENT_MASK;
                createRectangularHitbox(
                    x + 0.07f, y + 1 + HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, hx - 0.07f,
                    HORIZONTAL_WALL_HITBOX_HEIGHT_CELLS, temp);
                createRectangularHitbox(x + 0.12f, y + 3f + 0.6f, hx - 0.24f, 0.3f);
            }
        }
    }

    public AbsolutePoint getExitPosition() {
        return exitPosition;
    }

    public TiledMap getMap() {
        return map;
    }

    public static class TiledMapAdapter extends TypeAdapter<TiledMap> {
        @Override
        public void write(JsonWriter jsonWriter, TiledMap tiledMap) throws IOException {
            TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
            TileTextureHelper.TextureWithIndex[][] textures =
                new TileTextureHelper.TextureWithIndex[layer.getWidth()][layer.getHeight()];
            for (int i = 0; i < layer.getWidth(); i++) {
                for (int j = 0; j < layer.getHeight(); j++) {
                    Cell cell = layer.getCell(i, j);
                    if (cell == null) {
                        continue;
                    }
                    String textureName = (String) cell.getTile().getProperties().get("textureName");
                    int index = (int) cell.getTile().getProperties().get("index");
                    textures[i][j] = new TileTextureHelper.TextureWithIndex(textureName, index);
                }
            }
            jsonWriter.beginObject();
            jsonWriter.name("width").value(layer.getWidth());
            jsonWriter.name("height").value(layer.getHeight());
            jsonWriter.name("tiles");
            jsonWriter.jsonValue(
                Assets.getInstance().gson.toJson(textures)
            );
            jsonWriter.endObject();
        }

        @Override
        public TiledMap read(JsonReader jsonReader) throws IOException {
            int width = 0, height = 0;
            jsonReader.beginObject();
            String fieldname = null;
            TiledMap map = new TiledMap();
            while (jsonReader.hasNext()) {
                JsonToken token = jsonReader.peek();

                if (token.equals(JsonToken.NAME)) {
                    //get the current token
                    fieldname = jsonReader.nextName();
                }
                switch (Objects.requireNonNull(fieldname)) {
                    case "width" -> width = jsonReader.nextInt();
                    case "height" -> height = jsonReader.nextInt();
                    case "tiles" -> {
                        if (width == 0 || height == 0) {
                            throw new IOException("Width and height must be set before tiles");
                        }
                        TileTextureHelper.TextureWithIndex[][] textures =
                            Assets.getInstance().gson.fromJson(jsonReader, TileTextureHelper.TextureWithIndex[][].class);

                        TiledMapTileLayer layer =
                            new TiledMapTileLayer(width, height,
                                CELL_SIZE,
                                CELL_SIZE
                            );
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (textures[i][j] == null) {
                                    continue;
                                }
                                Cell mapCell = new Cell();
                                TextureRegion result =
                                    Assets.getInstance().tileTextureHelper.getTexture(
                                        textures[i][j].texture(), textures[i][j].index()
                                    );
                                mapCell.setTile(
                                    new StaticTiledMapTile(
                                        result
                                    )
                                );
                                mapCell.getTile().getProperties().put("textureName", textures[i][j].texture());
                                mapCell.getTile().getProperties().put("index", textures[i][j].index());
                                layer.setCell(i, j, mapCell);
                            }
                        }
                        map.getLayers().add(layer);
                    }
                }
            }
            jsonReader.endObject();
            return map;
        }
    }
}
