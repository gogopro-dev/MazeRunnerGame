package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Represents a bounding rectangle around a body.
 * It is used to determine the size of the body, mainly for the AABB test.
 * It gets the size from all the fixtures of the body and adds a little extra size to it (for a safe measure).
 *
 * @param height The height of the bounding rectangle.
 * @param width  The width of the bounding rectangle.
 */
public record BoundingRectangle(float height, float width) {
    /**
     * Creates a bounding rectangle from a body.
     *
     * @param body The body to get the bounding rectangle from.
     * @return The bounding rectangle of the body.
     */
    public static BoundingRectangle fromBody(Body body) {
        // Get body position
        float x = body.getPosition().x;
        float y = body.getPosition().y;
        // Body get bounding box
        float min_X = Integer.MAX_VALUE;
        float max_X = Integer.MIN_VALUE;
        float min_Y = Integer.MAX_VALUE;
        float max_Y = Integer.MIN_VALUE;
        for (int i = 0; i < body.getFixtureList().size; i++) {
            Fixture fixture = body.getFixtureList().get(i);
            switch (fixture.getType()) {
                case Polygon -> {
                    PolygonShape shape = (PolygonShape) fixture.getShape();
                    for (int j = 0; j < shape.getVertexCount(); j++) {
                        Vector2 vertex = new Vector2();
                        shape.getVertex(j, vertex);
                        min_X = Math.min(min_X, vertex.x);
                        max_X = Math.max(max_X, vertex.x);
                        min_Y = Math.min(min_Y, vertex.y);
                        max_Y = Math.max(max_Y, vertex.y);
                    }
                }
                case Circle -> {
                    Shape shape = fixture.getShape();
                    min_X = Math.min(min_X, (x - shape.getRadius()));
                    max_X = Math.max(max_X, (x + shape.getRadius()));
                    min_Y = Math.min(min_Y, (y - shape.getRadius()));
                    max_Y = Math.max(max_Y, (y + shape.getRadius()));
                }
                default -> throw new IllegalStateException("Fixture type is not yet supported " + fixture.getType());
            }
        }

        // Extra size for the bounding box so that object is safely hitting no hitboxes
        return new BoundingRectangle(
            (max_Y - min_Y) + Globals.CELL_SIZE_METERS * 0.1f,
            (max_X - min_X) + Globals.CELL_SIZE_METERS * 0.1f
        );
    }
}
