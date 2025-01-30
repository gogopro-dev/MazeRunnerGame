package de.tum.cit.fop.maze.level.worldgen;

/**
 * <h2>Disjoint Set Union data structure</h2>
 * <p>Used for finding connected components in the maze</p>
 */
public class DSU {
    private final int[] parent;
    private final int[] rank;

    public DSU(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Check if two nodes are in the same connected component
     *
     * @param x node 1
     * @param y node 2
     * @return {@code true} if the nodes are in the same CC
     */
    public boolean same(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * Find the root of the connected component
     *
     * @param node node to find
     * @return {@link Integer} the root of the connected component
     */
    public int find(int node) {
        if (parent[node] != node) {
            parent[node] = find(parent[node]);
        }
        return parent[node];
    }

    /**
     * Unite two disjoint components
     *
     * @param x node 1
     * @param y node 2
     */
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return;
        }
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
    }
}
