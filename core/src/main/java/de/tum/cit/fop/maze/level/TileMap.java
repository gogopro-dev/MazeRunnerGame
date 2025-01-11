package de.tum.cit.fop.maze.level;

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
import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.MazeCell;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import de.tum.cit.fop.maze.level.worldgen.rooms.Entrance;
import de.tum.cit.fop.maze.level.worldgen.rooms.Shop;
import de.tum.cit.fop.maze.level.worldgen.rooms.SpecialRoom;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.File;
import java.util.List;

import static de.tum.cit.fop.maze.Globals.*;

public class TileMap implements Disposable {
    private final TiledMap map;
    public final int width;
    public final int height;
    public final float heightMeters;
    public final float widthMeters;
    public final boolean[][] collisionMap;
    private final TextureLoader textures;

    /**
     * Create a new TileMap with a given height, width and seed
     * @param height the height of the map
     * @param width the width of the map
     * @param seed the seed for the map
     */
    public TileMap(int height, int width, int seed) {
        MazeGenerator generator = new MazeGenerator(height, width, seed);
        this.map = new TiledMap();
        // Always get width and height from the generator, because it always makes the parameters odd
        this.width = generator.width * 3;
        this.height = generator.height * 3;

        widthMeters = this.width * CELL_SIZE_METERS;
        heightMeters = this.height * CELL_SIZE_METERS;
        this.collisionMap = new boolean[this.height][this.width];

        textures = new TextureLoader("assets/tiles/tiles.atlas", generator.getRandom());
        createDebugLayer();


        generator.generateMazeWalls();
        generator.generateRooms(List.of(
            new Entrance(),
            new Shop(),
            new SpecialRoom()
        ));
        ///  Dimensions x3 since we want to have 3x3 tiles for each cell,
        ///  so we create larger environment for fights, etc.
        TiledMapTileLayer bottomLayer =
            new TiledMapTileLayer(this.width, this.height,
                Globals.CELL_SIZE,
                Globals.CELL_SIZE
            );
        TiledMapTileLayer topLayer =
            new TiledMapTileLayer(this.width, this.height,
                Globals.CELL_SIZE,
                Globals.CELL_SIZE
            );

        map.getLayers().add(bottomLayer);
        map.getLayers().add(topLayer);

        // System.out.println(generator);
        // Necessary paddings
        int startI = 2;
        int startJ = 1;
        for (int i = generator.height - 1; i >= 0; --i) {
            for (int j = generator.width - 1; j >= 0; --j) {
                MazeCell cell = generator.grid.get(i).get(j);
                int x = startJ + j * 3;
                int y = bottomLayer.getHeight() - (startI + i * 3);
                /// All non-walkable cells require hitboxes
                if (!cell.getCellType().isWalkable()) {
                    setCollisionSquare(x, y);
                }
                ///  Floor
                if (cell.getCellType().isWalkable()) {
                    setSquare(textures.getTextureWithVariationChance("floor"), x, y, false);
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

            }
        }
        reverseCollisionMapRows();
        generateHitboxes();

    }

    private void reverseCollisionMapRows() {
        for (int i = 0; i < height / 2; ++i) {
            for (int j = 0; j < width; ++j) {
                boolean tmp = collisionMap[i][j];
                collisionMap[i][j] = collisionMap[height - 1 - i][j];
                collisionMap[height - 1 - i][j] = tmp;
            }
        }
    }

    private void setCollisionSquare(int x, int y) {
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
            collisionMap[height - 1 - y - surrounding[1]][x + surrounding[0]] = true;
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
        setSquare(decoration, x, y, false);
    }

    /**
     * Set square 3x3 at x y position to be {@code cell}
     * @param texture the texture to set
     * @param x the x position
     * @param y the y position
     * @param topLayer the layer to set the cell in
     */
    private void setSquare(TextureRegion texture, int x, int y, boolean topLayer) {
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
            setCell(texture, x + surrounding[0], y + surrounding[1], topLayer);
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

    private void createRectangularHitbox(float x, float y, float hx, float hy, @Nullable FixtureDef fixtureDef) {
        // System.out.println("Creating hitbox at " + x + " " + y + " " + hx + " " + hy + " With cell size " + CELL_SIZE_METERS); ;
        hx *= CELL_SIZE_METERS;
        hy *= CELL_SIZE_METERS;
        x = x * CELL_SIZE_METERS * 2f + hx;
        y = y * CELL_SIZE_METERS * 2f + hy;

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

    private void createRectangularHitbox(float x, float y, float hx, float hy) {
        createRectangularHitbox(x, y, hx, hy, null);
    }

    private boolean isIsolatedCollidable(int i, int j) {
        if (i - 2 <= 0 || j - 2 <= 0 || i + 2 >= height || j + 2 >= width) {
            return false;
        }
        return !collisionMap[i - 2][j] && !collisionMap[i + 2][j] && !collisionMap[i][j - 2] && !collisionMap[i][j + 2];
    }

    private void generateVerticalHitboxes() {
        for (int j = 0; j < width; j += 3) {
            int x = (j - 1);
            int y = -1;
            int hy = 0;
            for (int i = 0; i < height; i += 3) {
                // System.out.println("Checking " + i + " " + j + " " + collisionMap[i][j]);
                if (collisionMap[i][j]) {
                    if (y == -1) {
                        y = i;
                    }
                    hy += 3;
                } else {
                    if (y != -1) {
                        if (hy > 3) {
                            /// HY reduced by 0.05 to avoid collision above the wall
                            createRectangularHitbox(x + CELL_SIZE_METERS * 2.66f, y, 3,
                                hy - CELL_SIZE_METERS * 0.05f);
                        }
                        y = -1;
                        hy = 0;
                    }
                }
            }
            if (y != -1 && hy > 3) {
                createRectangularHitbox(x + CELL_SIZE_METERS * 2.66f, y, 3,
                    hy - CELL_SIZE_METERS * 0.05f);
            }
        }

    }

    private void generateHorizontalHitboxes() {
        for (int i = 0; i < height; i += 3) {
            int x = -1;
            int y = (i - 1);
            int hx = 0;
            for (int j = 0; j < width; j += 3) {
                if (collisionMap[i][j]) {
                    if (x == -1) {
                        x = j;
                    }
                    hx += 3;
                } else {
                    if (x != -1) {
                        if (hx > 3) {
                            createRectangularHitbox(x, y + CELL_SIZE_METERS * 2.66f, hx, 3);
                        }
                        if (isIsolatedCollidable(i + 1, j - 2)) {
                            FixtureDef fixtureDef = new FixtureDef();
                            fixtureDef.filter.categoryBits = BodyBits.DECORATION;
                            createRectangularHitbox(x, y + CELL_SIZE_METERS * 2.66f, hx, 3, fixtureDef);
                        }

                        x = -1;
                        hx = 0;
                    }
                }
            }
            if (x != -1 && hx > 3) {
                createRectangularHitbox(x, y + CELL_SIZE_METERS * 2.66f, hx, 3);
            }
        }
    }


    private void generateHitboxes() {
        generateVerticalHitboxes();
        generateHorizontalHitboxes();
    }
}
