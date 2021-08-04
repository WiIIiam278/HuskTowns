package me.william278.husktowns.util.claimviewer;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class ParticleLocation extends Location implements Comparable<ParticleLocation> {

    public ParticleLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    /**
     * Returns true if a→b→c is a counterclockwise turn.
     * See https://algs4.cs.princeton.edu/99hull/Point2D.java.html
     *
     * @param a first point
     * @param b second point
     * @param c third point
     * @return { -1, 0, +1 } if a→b→c is a { clockwise, collinear; counterclockwise } turn.
     */
    public static int ccw(ParticleLocation a, ParticleLocation b, ParticleLocation c) {
        double area2 = ((b.getX() - a.getX()) * (c.getZ() - a.getZ())) - ((b.getZ() - a.getZ()) * (c.getX() - a.getX()));
        if (area2 < 0) {
            return -1;
        } else if (area2 > 0) {
            return +1;
        } else {
            return 0;
        }
    }

    /**
     * Compares two points by polar angle (between 0 and 2&pi;) with respect to this point.
     * @return the comparator
     */
    public Comparator<ParticleLocation> polarOrder() {
        return new PolarOrder();
    }

    @Override
    public int compareTo(@NotNull ParticleLocation that) {
        if (this.getZ() < that.getZ()) return -1;
        if (this.getZ() > that.getZ()) return +1;
        return Double.compare(this.getX(), that.getX());
    }

    // compare other points relative to polar angle (between 0 and 2pi) they make with this Point
    private class PolarOrder implements Comparator<ParticleLocation> {
        public int compare(ParticleLocation q1, ParticleLocation q2) {
            double dx1 = q1.getX() - getX();
            double dy1 = q1.getZ() - getZ();
            double dx2 = q2.getX() - getX();
            double dy2 = q2.getZ() - getZ();

            if (dy1 >= 0 && dy2 < 0) return -1;    // q1 above; q2 below
            else if (dy2 >= 0 && dy1 < 0) return +1;    // q1 below; q2 above
            else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
                if (dx1 >= 0 && dx2 < 0) return -1;
                else if (dx2 >= 0 && dx1 < 0) return +1;
                else return 0;
            } else return -ccw(ParticleLocation.this, q1, q2);     // both above or below

            // Note: ccw() recomputes dx1, dy1, dx2, and dy2
        }
    }
}
