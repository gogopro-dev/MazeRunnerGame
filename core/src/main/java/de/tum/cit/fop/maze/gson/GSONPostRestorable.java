package de.tum.cit.fop.maze.gson;

/**
 * Interface for classes that need to restore their state after deserialization from GSON.
 */
public interface GSONPostRestorable {
    /// Perform restore operation
    void restore();
}
