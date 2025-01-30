package de.tum.cit.fop.maze.gson;

/**
 * Interface for classes that need to restore their state after deserialization.
 */
public interface GSONPostRestorable {
    void restore();
}
