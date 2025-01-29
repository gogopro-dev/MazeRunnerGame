package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Class for the fade overlay.</br>
 * Used to fade in and out the screen.
 */
public class FadeOverlay implements Disposable {
    private final ShapeRenderer shapeRenderer;
    private final float fadeSpeed = 0.5f; // Adjust this to control fade speed
    private float alpha = 0;
    private State state = State.NONE;
    private float fadeOutSpeed;

    /**
     * Enum for the state of the fade overlay
     */
    public enum State {
        NONE,
        FADING_IN,
        FADING_OUT
    }

    /**
     * Constructor for the fade overlay
     * Creates a new shape renderer
     */
    public FadeOverlay() {
        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Starts the fade in
     */
    public void startFadeIn() {
        state = State.FADING_IN;
        alpha = 0;
        fadeOutSpeed = 1.5f;
    }

    /**
     * Starts the fade out.
     * The fade out speed is set to 0.5f.</br>
     * This is used only on startup
     */
    public void startFadeOut() {
        state = State.FADING_OUT;
        alpha = 1;
        fadeOutSpeed = 1f;
    }

    /**
     * @return True if the fade in is finished
     */
    public boolean isFinishedIn() {
        return state == State.FADING_OUT;
    }

    /**
     * @return True if the fade out is finished
     */
    public boolean isFinishedOut() {
        return state == State.NONE;
    }

    /**
     * Renders the overlay
     * @param delta The time since last render in seconds
     */
    public void render(float delta) {
        if (state == State.NONE) return;

        /// Update alpha based on state
        if (state == State.FADING_IN) {
            alpha += delta * fadeSpeed;
            if (alpha >= 1) {
                alpha = 1;
                state = State.FADING_OUT;
            }
        } else if (state == State.FADING_OUT) {
            /// Fade out based on the fadeOutSpeed attribute
            /// Default value is 1.5f, but it is adjusted in the main menu
            alpha -= delta * fadeSpeed * fadeOutSpeed;
            if (alpha <= 0) {
                alpha = 0;
                state = State.NONE;
            }
        }

        /// Render overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, alpha);
        shapeRenderer.rect(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
