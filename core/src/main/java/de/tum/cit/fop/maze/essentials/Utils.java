package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.fop.maze.entities.Enemy;
import de.tum.cit.fop.maze.entities.Entity;
import de.tum.cit.fop.maze.level.LevelScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Utility class providing various helper methods for graphic manipulations,
 * raycast operations, gameplay mechanics, easing functions, and scheduling tasks.
 */
public class Utils {
    public static Drawable getColoredDrawable(int width, int height, Color color) {
        /// create simple font for Stamina Bar
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

    /**
     * Check if the player is reachable (not blocked by any obstacles and within the vision range) using Box2D raycast
     *
     * @param sourcePoint The point to check from
     * @return {@code true} if the player is reachable {@code false} otherwise
     */
    public static boolean isPlayerExposed(AbsolutePoint sourcePoint) {
        LevelScreen levelScreen = LevelScreen.getInstance();
        ArrayList<AbsolutePoint> playerTrackingPoints = getTrackingPoits(levelScreen.player);
        boolean finalResult = false;

        for (AbsolutePoint playerTrackingPoint : playerTrackingPoints) {
            boolean[] result = {true};
            levelScreen.world.rayCast(
                (fixture, point, normal, fraction) -> {
                    if (fixture.getBody().getUserData() instanceof Enemy) return -1;
                    if (fixture.getFilterData().categoryBits == BodyBits.WALL ||
                        fixture.getFilterData().categoryBits == BodyBits.WALL_TRANSPARENT) {
                        result[0] = false;
                    }
                    return fraction;
                },
                sourcePoint.toVector2(),
                playerTrackingPoint.toVector2()
            );
            DebugRenderer.getInstance().drawLine(sourcePoint, playerTrackingPoint, result[0] ? Color.GREEN : Color.RED);
            finalResult |= result[0];

        }
        return finalResult;
    }

    /**
     * Get the tracking points for an entity
     * @param entity The entity to track
     * @return The tracking points
     */
    private static @NotNull ArrayList<AbsolutePoint> getTrackingPoits(Entity entity) {
        AbsolutePoint position = entity.getPosition();
        BoundingRectangle entityRectangle = entity.boundingRectangle;
        ArrayList<AbsolutePoint> trackingPoints = new ArrayList<>();
        /// We will track the player with 3 points on the top, middle and bottom of the player
        for (int i = 1; i <= 3; ++i) {
            trackingPoints.add(
                new AbsolutePoint(
                    position.x(),
                    position.y() - entityRectangle.height() / 1.7f + entityRectangle.height() * i / 3
                )
            );
        }
        return trackingPoints;
    }

    /**
     * Check if the entity is in shadow
     * @param entity The entity to check
     * @return {@code true} if the entity is in shadow, {@code false} otherwise
     */
    public static boolean isEntityInShadow(Entity entity) {
        ArrayList<AbsolutePoint> trackingPoints = getTrackingPoits(entity);
        boolean inShadow = true;
        for (AbsolutePoint trackingPoint : trackingPoints) {
            inShadow &= LevelScreen.getInstance().rayHandler.pointAtShadow(
                trackingPoint.x(),
                trackingPoint.y()
            );
        }
        return inShadow;
    }

    /**
     * Check if the entity is in light
     * @param entity The entity to check
     * @return {@code true} if the entity is in light, {@code false} otherwise
     */
    public static boolean isEntityInLight(Entity entity) {
        return !isEntityInShadow(entity);
    }

    /**
     * Check if the player is reachable (not blocked by any obstacles and within the vision range) using Box2D raycast
     * @param sourcePoint The point to check from
     * @param rayLength The length of the ray
     * @return {@code true} if the player is reachable {@code false} otherwise
     */
    public static boolean isPlayerExposed(AbsolutePoint sourcePoint, float rayLength) {
        LevelScreen levelScreen = LevelScreen.getInstance();
        AbsolutePoint playerPosition = levelScreen.player.getPosition();
        if (sourcePoint.distance(playerPosition) > rayLength) {
            return false;
        }
        return isPlayerExposed(sourcePoint);
    }

    /**
     * Easing easeInOutQuart for smooth transitions between states, based on the x value 0 to 1
     *
     * @param x The input value between {@code 0} and {@code 1} signifying the progress of the transition
     * @return The eased value between {@code 0} and {@code 1}
     */
    public static float easeInOutQuart(float x) {
        return x < 0.5 ? 8 * x * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 4) / 2);
    }

    /**
     * Easing easeOutCirc for smooth transitions between states, based on the x value 0 to 1
     *
     * @param x The input value between {@code 0} and {@code 1} signifying the progress of the transition
     * @return The eased value between {@code 0} and {@code 1}
     */
    public static float easeOutCirc(float x) {
        if (x > 1) return 1;
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));

    }

    /**
     * Interpolates between two colors based on the progress
     * @param tint The color to interpolate to
     * @param progress The progress of the interpolation
     * @return The interpolated color
     */
    public static Color tintInterpolation(Color tint, float progress) {
        float differenceR = 1 - tint.r;
        float differenceG = 1 - tint.g;
        float differenceB = 1 - tint.b;
        return new Color(
            tint.r + differenceR * progress,
            tint.g + differenceG * progress,
            tint.b + differenceB * progress,
            1
        );
    }

    /**
     * Schedule a function to run after a delay
     * @param function The function to run
     * @param delay The delay in seconds
     */
    public static void scheduleFunction(Runnable function, float delay) {
        new Thread(() -> {
            try {
                Thread.sleep((long) (delay * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            function.run();
        }).start();
    }

}
