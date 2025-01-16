package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import de.tum.cit.fop.maze.Globals;
import de.tum.cit.fop.maze.essentials.AbsolutePoint;
import de.tum.cit.fop.maze.essentials.BoundingRectangle;
import de.tum.cit.fop.maze.essentials.DebugRenderer;
import de.tum.cit.fop.maze.essentials.Utils;
import de.tum.cit.fop.maze.level.LevelScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h2>The manager for the enemies</h2>
 * <p>
 * The enemy manager is responsible for creating, rendering and ticking the enemies, including their AI.
 * </p>
 * <p>
 * A brief overview of the enemy AI:
 *     <ul>
 *         <li>Enemies have a vision range, and if the player is within this range and not blocked by any obstacles,
 *         the enemy will start chasing the player</li>
 *         <li>Enemies will follow the player until they reach the player or the player is
 *         no longer in their vision range</li>
 *         <li>Enemies will move randomly if the player is not in their vision range</li>
 * </p>
 */
public class EnemyManager {
    private final LevelScreen levelScreen;
    ///  The Libgdx executor for asynchronous tasks
    private final AsyncExecutor asyncExecutor;
    private final ArrayList<Enemy> enemies;
    private float accumulator = 0;
    ///  The number of launched tasks, used so that no new tasks are launched if there are already some running
    ///  (mainly for low-end devices)
    private final AtomicInteger launchedTasks = new AtomicInteger(0);

    public EnemyManager() {
        this.enemies = new ArrayList<>();
        this.asyncExecutor = new AsyncExecutor(8);
        this.levelScreen = LevelScreen.getInstance();
    }

    /**
     * Check if the player is seen by the enemy (not blocked by any obstacles and within the vision range)
     *
     * @param enemy The enemy to check
     * @return {@code true} if the player is seen by the enemy, {@code false} otherwise
     */
    private boolean isPlayerSeen(Enemy enemy) {
        BoundingRectangle enemyRect = enemy.boundingRectangle;
        AbsolutePoint enemyPosition = enemy.getPosition();
        AbsolutePoint playerPosition = levelScreen.player.getPosition();
        AbsolutePoint enemyEyes = new AbsolutePoint(
            enemyPosition.x(),
            enemyPosition.y() + enemyRect.height() / 3
        );
        float rayLength;
        if (enemy.isFacingRight() == playerPosition.onTheRightFrom(enemyPosition)) {
            rayLength = enemy.getConfig().attributes().visionRange() * Globals.CELL_SIZE_METERS * 3;
        } else {
            rayLength = enemy.getConfig().attributes().visionRange() * Globals.CELL_SIZE_METERS * 3 / 2f;
        }

        return Utils.isPlayerReachable(enemyEyes, rayLength);
    }

    /**
     * Create an enemy at the specified position
     *
     * @param enemy The enemy to create
     * @param x     The x coordinate of the enemy
     * @param y     The y coordinate of the enemy
     */
    public void createEnemy(Enemy enemy, float x, float y) {
        enemy.spawn(x, y, levelScreen.world);
        enemies.add(enemy);
    }

    /**
     * Render the enemies
     *
     * @param delta The time since the last frame
     */
    public void render(float delta) {
        tickEnemies(delta);
        for (Enemy enemy : enemies) {
            enemy.render(delta);
        }

    }

    private void removeEnemy(Enemy enemy) {
        enemy.dispose();
        enemies.remove(enemy);
    }

    /**
     * Recalculate the path of the enemies to the player
     */
    private void recalculatePaths() {
        if (launchedTasks.get() > 0) return;
        for (Enemy enemy : enemies) {
            if (!enemy.isMovingToPlayer()) {
                continue;
            }
            launchedTasks.incrementAndGet();
            asyncExecutor.submit(() -> {
                List<AbsolutePoint> path = LevelScreen.getInstance().map.pathfinder.aStar(
                    enemy,
                    LevelScreen.getInstance().player.getPosition()
                );
                if (path == null) {
                    launchedTasks.decrementAndGet();
                    enemy.setMovingToPlayer(false);
                    return true;
                }
                enemy.updatePath(path);
                launchedTasks.decrementAndGet();
                return true;
            });
        }

    }

    /**
     * Tick the enemies
     * @param delta The time since the last frame
     */
    public void tickEnemies(float delta) {
        boolean playerChased = false;
        accumulator += delta;
        if (accumulator >= Globals.ENEMY_PATHFINDING_INTERVAL) {
            accumulator = 0;
            recalculatePaths();
        }
        for (Enemy enemy : enemies) {
            playerChased |= enemy.isMovingToPlayer();
            tickEnemy(enemy);
        }
        levelScreen.player.setBeingChased(playerChased);
    }

    /**
     * Tick a single enemy
     *
     * @param enemy The enemy to tick
     */
    private void tickEnemy(Enemy enemy) {
        if (!enemy.isMovingToPlayer() && isPlayerSeen(enemy)) {
            enemy.setMovingToPlayer(true);
        }
        if (enemy.isMovingToPlayer()) {
            AbsolutePoint lastPoint = enemy.getPosition();
            AbsolutePoint currentPoint;
            for (int i = 0; i < enemy.getPath().size(); ++i) {
                currentPoint = enemy.getPath().get(i);
                DebugRenderer.getInstance().drawLine(lastPoint, currentPoint, Color.RED);
                lastPoint = currentPoint;
            }
            enemy.followPath();
        }

        /// Todo @Hlib move randomly*/
    }

    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
    }
}
