package de.tum.cit.fop.maze.essentials;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * A simple debug renderer that can draw lines between points.
 */
public class DebugRenderer {
    private static DebugRenderer instance;
    private final ShapeRenderer shapeRenderer;
    private final List<Function<Void, Void>> spawnedShapes = new ArrayList<>();
    private final boolean drawDebug = Globals.DEBUG;

    public static DebugRenderer getInstance() {
        if (instance == null) {
            instance = new DebugRenderer();
        }
        return instance;
    }

    public DebugRenderer() {
        if (instance != null) {
            throw new IllegalStateException("DebugRenderer already created");
        }
        instance = this;
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(LevelScreen.getInstance().camera.combined);
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
        if (!drawDebug) {
            return;
        }
        shapeRenderer.line(start.x(), start.y(), end.x(), end.y(), color, color);
    }

    /**
     * Draw a rectangle between two points.
     */
    public void drawRectangle(AbsolutePoint start, AbsolutePoint end, Color color) {
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
        if (!drawDebug) {
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
        if (!drawDebug) {
            return;
        }
        spawnedShapes.add((Void v) -> {
            shapeRenderer.circle(center.x(), center.y(), radius, 50);
            return null;
        });
    }

    /**
     * Draw a circle at a point.
     *
     * @param center
     * @param radius
     */
    public void drawCircle(AbsolutePoint center, float radius) {
        if (!drawDebug) {
            return;
        }
        shapeRenderer.circle(center.x(), center.y(), radius);
    }

    public void drawLine(AbsolutePoint start, AbsolutePoint end) {
        if (!drawDebug) {
            return;
        }
        drawLine(start, end, Color.WHITE);
    }

    public void begin() {
        shapeRenderer.begin();
    }

    public void end() {
        for (Function<Void, Void> f : spawnedShapes) {
            f.apply(null);
        }
        shapeRenderer.end();
    }

}
