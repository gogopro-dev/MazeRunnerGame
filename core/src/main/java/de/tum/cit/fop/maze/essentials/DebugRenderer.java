package de.tum.cit.fop.maze.essentials;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.Queue;
import java.util.function.Function;

/**
 * A simple debug renderer that can draw lines between points.
 */
public class DebugRenderer {
    private static DebugRenderer instance;
    private final ShapeRenderer shapeRenderer;

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
        shapeRenderer.line(start.x(), start.y(), end.x(), end.y(), color, color);
    }

    public void drawLine(AbsolutePoint start, AbsolutePoint end) {
        drawLine(start, end, Color.WHITE);
    }

    public void begin() {
        shapeRenderer.begin();
    }

    public void end() {
        shapeRenderer.end();
    }

}
