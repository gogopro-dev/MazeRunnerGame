package de.tum.cit.fop.maze.essentials;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A simple debug renderer that can draw lines between points.
 */
public class DebugRenderer {
    private static DebugRenderer instance;
    private final ShapeRenderer shapeRenderer;
    private final List<Function<Void, Void>> spawnedShapes = new ArrayList<>();
    private final boolean drawDebug = Globals.DEBUG;

    /**
     * Instantiates a new Debug renderer.
     */
    public DebugRenderer() {
        if (instance != null) {
            throw new IllegalStateException("DebugRenderer already created");
        }
        instance = this;
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(LevelScreen.getInstance().camera.combined);
    }

    public static DebugRenderer getInstance() {
        if (instance == null) {
            instance = new DebugRenderer();
        }
        return instance;
    }

    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
    }

    /**
     * Draw a line between two points.
     *
     * @param start The start point of the line.
     * @param end   The end point of the line.
     * @param color The color of the line.
     */
    public void drawLine(AbsolutePoint start, AbsolutePoint end, Color color) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        shapeRenderer.line(start.x(), start.y(), end.x(), end.y(), color, color);
    }

    /**
     * Draws a rectangle on the screen between two specified points with the given color.
     * The rectangle is only rendered if debugging mode is enabled.
     *
     * @param start The starting point (bottom-left corner) of the rectangle in absolute coordinates.
     * @param end   The ending point (top-right corner) of the rectangle in absolute coordinates.
     * @param color The color to fill the rectangle.
     */
    public void drawRectangle(AbsolutePoint start, AbsolutePoint end, Color color) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        shapeRenderer.rect(
            start.x(), start.y(),
            end.x() - start.x(),
            end.y() - start.y(),
            color, color, color, color
        );
    }

    public void spawnRectangle(AbsolutePoint start, AbsolutePoint end, Color color) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        if (spawnedShapes.size() > 10000) {
            return;
        }
        spawnedShapes.add((Void v) -> {
            shapeRenderer.rect(
                start.x(), start.y(),
                end.x() - start.x(),
                end.y() - start.y(),
                color, color, color, color
            );
            return null;
        });
    }


    public void spawnCircle(AbsolutePoint center, float radius) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        spawnedShapes.add((Void v) -> {
            shapeRenderer.circle(center.x(), center.y(), radius, 50);
            return null;
        });
    }

    /**
     * Draws a circle on the screen with the specified center point and radius if debugging mode is enabled.
     *
     * @param center The center point of the circle in absolute coordinates.
     * @param radius The radius of the circle.
     */
    public void drawCircle(AbsolutePoint center, float radius) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        shapeRenderer.circle(center.x(), center.y(), radius);
    }

    public void drawLine(AbsolutePoint start, AbsolutePoint end) {
        /// Inspection is redundant since it's set in Globals for convenience (it will always be either true or false)
        //noinspection ConstantValue
        if (!drawDebug) {
            return;
        }
        drawLine(start, end, Color.WHITE);
    }

    /**
     * Begins the drawing process for debug shapes using the internal ShapeRenderer instance.
     * This method should be called before rendering any debug shapes.
     */
    public void begin() {
        shapeRenderer.begin();
    }

    /**
     * Completes the rendering process for the debug shapes.
     * This method iterates through the list of all spawned shape functions,
     * invokes each of them, and then ends the shape rendering process using
     * the internal ShapeRenderer instance.
     * <p>
     * It is typically called after all debug shapes have been rendered
     * within a frame and the rendering process needs to be finalized.
     */
    public void end() {
        for (Function<Void, Void> f : spawnedShapes) {
            f.apply(null);
        }
        shapeRenderer.end();
    }

}
