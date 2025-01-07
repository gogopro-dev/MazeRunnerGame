package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.worldgen.CellType;
import de.tum.cit.fop.maze.level.worldgen.MazeCell;
import de.tum.cit.fop.maze.level.worldgen.MazeGenerator;
import de.tum.cit.fop.maze.level.worldgen.rooms.Entrance;
import de.tum.cit.fop.maze.level.worldgen.rooms.Shop;
import de.tum.cit.fop.maze.level.worldgen.rooms.SpecialRoom;

import java.util.List;

import static de.tum.cit.fop.maze.Globals.SCALING_RATIO;

public class TileMap implements Disposable {
    private final TiledMap map;
    public final int width;
    public final int height;
    private final TextureLoader textures;

    /**
     * Create a new TileMap with a given height, width and seed
     * @param height
     * @param width
     * @param seed
     */
    public TileMap(int height, int width, int seed) {
        MazeGenerator generator = new MazeGenerator(height, width, seed);
        this.map = new TiledMap();
        // Always get width and height from the generator, because it always makes the parameters odd
        this.width = generator.width * 3;
        this.height = generator.height * 3;
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
                Globals.CELL_SIZE * SCALING_RATIO,
                Globals.CELL_SIZE * SCALING_RATIO
            );
        TiledMapTileLayer topLayer =
            new TiledMapTileLayer(this.width, this.height,
                Globals.CELL_SIZE * SCALING_RATIO,
                Globals.CELL_SIZE * SCALING_RATIO
            );

        map.getLayers().add(bottomLayer);
        map.getLayers().add(topLayer);
        System.out.println(generator);
        // Necessary paddings
        int startI = 2;
        int startJ = 1;
        for (int i = generator.height - 1; i >= 0; --i) {
            for (int j = generator.width - 1; j >= 0; --j) {
                MazeCell cell = generator.grid.get(i).get(j);
                int x = startJ + j * 3;
                int y = bottomLayer.getHeight() - (startI + i * 3);
                ///  Floor
                if (cell.getCellType().isWalkable()) {
                    setSquare(bottomLayer, textures.getTextureWithVariationChance("floor"), x, y);
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

        setSquare((TiledMapTileLayer) map.getLayers().get(0), decoration, x, y);
    }

    /**
     * Set square 3x3 at x y position to be {@code cell}
     * @param layer the layer to set the cell in
     * @param texture the texture to set
     * @param x the x position
     * @param y the y position
     */
    private void setSquare(TiledMapTileLayer layer, TextureRegion texture, int x, int y) {
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
            Cell mapCell = new Cell();
            mapCell.setTile(
                new StaticTiledMapTile(texture)
            );
            layer.setCell(x + surrounding[0], y + surrounding[1], mapCell);
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
                Globals.CELL_SIZE * SCALING_RATIO,
                Globals.CELL_SIZE * SCALING_RATIO
            );
        Pixmap pixmap = new Pixmap(Globals.CELL_SIZE * SCALING_RATIO, Globals.CELL_SIZE * SCALING_RATIO, Pixmap.Format.RGBA8888);
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
}
