package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

import javax.swing.*;

/** Launches the desktop (LWJGL3) application. */
public class DesktopLauncher {
    public static void main(String[] args) {
        /*
         * If you use StartupHelper (automatically generated)
         * instead of just adding -XstartOnFirstThread,
         * the game will freeze after some amount of time on macOS.
         * This is due to the issues with (re-)starting new JVM on macOS.
         * Just adding -XstartOnFirstThread in JVM options will resolve
         * the issue on macOS.
         * However, StartupHelper might be helpful if you are on windows.
         * Check StartupHelper class for more info
         */
//        if (de.tum.cit.fop.maze.StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(LoadMenu.getInstance(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("MazeRunner");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(false);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        //// configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setHdpiMode(HdpiMode.Logical);
        configuration.setResizable(false);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
//        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }

}
