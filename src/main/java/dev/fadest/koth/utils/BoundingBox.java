package dev.fadest.koth.utils;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A mutable axis aligned bounding box (AABB).
 * <p>
 * This basically represents a rectangular box (specified by minimum and maximum
 * corners) that can, for example, be used to describe the position and extents of
 * an object (such as an entity, block, or rectangular region) in 3D space. Its
 * edges and faces are parallel to the axes of the cartesian coordinate system.
 * <p>
 * The bounding box may be degenerate (one or more sides having the length 0).
 * <p>
 * Because bounding boxes are mutable, storing them long term may be dangerous
 * if they get modified later. If you want to keep around a bounding box, it may
 * be wise to call {@link #clone()} in order to get a copy.
 */
@SerializableAs("BoundingBox")
@Getter
@Setter
public class BoundingBox implements Cloneable, ConfigurationSerializable {

    private double minX, maxX;
    private double minY, maxY;
    private double minZ, maxZ;

    /**
     * Creates a new bounding box from the given corner coordinates.
     *
     * @param x1 the first corner's x value
     * @param y1 the first corner's y value
     * @param z1 the first corner's z value
     * @param x2 the second corner's x value
     * @param y2 the second corner's y value
     * @param z2 the second corner's z value
     */
    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.resize(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Creates a empty bounding
     *
     * @return the bounding box
     */
    @NotNull
    public static BoundingBox empty() {
        return new BoundingBox(0, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a new bounding box using the coordinates of the given vectors as
     * corners.
     *
     * @param corner1 the first corner
     * @param corner2 the second corner
     * @return the bounding box
     */
    @NotNull
    public static BoundingBox of(@NotNull Vector corner1, @NotNull Vector corner2) {
        return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    /**
     * Creates a new bounding box using the coordinates of the given locations
     * as corners.
     *
     * @param corner1 the first corner
     * @param corner2 the second corner
     * @return the bounding box
     */
    @NotNull
    public static BoundingBox of(@NotNull Location corner1, @NotNull Location corner2) {
        Preconditions.checkArgument(Objects.equals(corner1.getWorld(), corner2.getWorld()), "Locations from different worlds!");
        return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    /**
     * Creates a new bounding box using the coordinates of the given blocks as
     * corners.
     * <p>
     * The bounding box will be sized to fully contain both blocks.
     *
     * @param corner1 the first corner block
     * @param corner2 the second corner block
     * @return the bounding box
     */
    @NotNull
    public static BoundingBox of(@NotNull Block corner1, @NotNull Block corner2) {
        Preconditions.checkArgument(Objects.equals(corner1.getWorld(), corner2.getWorld()), "Blocks from different worlds!");

        int x1 = corner1.getX();
        int y1 = corner1.getY();
        int z1 = corner1.getZ();
        int x2 = corner2.getX();
        int y2 = corner2.getY();
        int z2 = corner2.getZ();

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2) + 1;
        int maxY = Math.max(y1, y2) + 1;
        int maxZ = Math.max(z1, z2) + 1;

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Creates a new bounding box using the given center and extents.
     *
     * @param center the center
     * @param x      1/2 the size of the bounding box along the x axis
     * @param y      1/2 the size of the bounding box along the y axis
     * @param z      1/2 the size of the bounding box along the z axis
     * @return the bounding box
     */
    @NotNull
    public static BoundingBox of(@NotNull Vector center, double x, double y, double z) {
        return new BoundingBox(center.getX() - x, center.getY() - y, center.getZ() - z, center.getX() + x, center.getY() + y, center.getZ() + z);
    }

    @NotNull
    public static BoundingBox deserialize(@NotNull Map<String, Object> args) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 0.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;

        if (args.containsKey("minX")) {
            minX = ((Number) args.get("minX")).doubleValue();
        }
        if (args.containsKey("minY")) {
            minY = ((Number) args.get("minY")).doubleValue();
        }
        if (args.containsKey("minZ")) {
            minZ = ((Number) args.get("minZ")).doubleValue();
        }
        if (args.containsKey("maxX")) {
            maxX = ((Number) args.get("maxX")).doubleValue();
        }
        if (args.containsKey("maxY")) {
            maxY = ((Number) args.get("maxY")).doubleValue();
        }
        if (args.containsKey("maxZ")) {
            maxZ = ((Number) args.get("maxZ")).doubleValue();
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Resizes this bounding box.
     *
     * @param x1 the first corner's x value
     * @param y1 the first corner's y value
     * @param z1 the first corner's z value
     * @param x2 the second corner's x value
     * @param y2 the second corner's y value
     * @param z2 the second corner's z value
     */
    public void resize(double x1, double y1, double z1, double x2, double y2, double z2) {
        NumberConversions.checkFinite(x1, "x1 not finite");
        NumberConversions.checkFinite(y1, "y1 not finite");
        NumberConversions.checkFinite(z1, "z1 not finite");
        NumberConversions.checkFinite(x2, "x2 not finite");
        NumberConversions.checkFinite(y2, "y2 not finite");
        NumberConversions.checkFinite(z2, "z2 not finite");

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    /**
     * Checks if this bounding box contains the specified position.
     * <p>
     * Positions exactly on the minimum borders of the bounding box are
     * considered to be inside the bounding box, while positions exactly on the
     * maximum borders are considered to be outside. This allows bounding boxes
     * to reside directly next to each other with positions always only residing
     * in exactly one of them.
     *
     * @param x the position's x coordinates
     * @param y the position's y coordinates
     * @param z the position's z coordinates
     * @return <code>true</code> if the bounding box contains the position
     */
    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x <= this.maxX
                && y >= this.minY && y <= this.maxY
                && z >= this.minZ && z <= this.maxZ;
    }

    /**
     * Checks if this bounding box contains the specified position.
     * <p>
     * Positions exactly on the minimum borders of the bounding box are
     * considered to be inside the bounding box, while positions exactly on the
     * maximum borders are considered to be outside. This allows bounding boxes
     * to reside directly next to each other with positions always only residing
     * in exactly one of them.
     *
     * @param position the position
     * @return <code>true</code> if the bounding box contains the position
     */
    public boolean contains(@NotNull Vector position) {
        return this.contains(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Checks if this bounding box contains the specified position.
     * <p>
     * Positions exactly on the minimum borders of the bounding box are
     * considered to be inside the bounding box, while positions exactly on the
     * maximum borders are considered to be outside. This allows bounding boxes
     * to reside directly next to each other with positions always only residing
     * in exactly one of them.
     *
     * @param position the position
     * @return <code>true</code> if the bounding box contains the position
     */
    public boolean contains(@NotNull Location position) {
        return this.contains(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Tries to normalize the min and max positions of each coordinate if they're not in the right value
     */
    public void normalize() {
        final int x1 = (int) minX;
        final int y1 = (int) minY;
        final int z1 = (int) minZ;
        final int x2 = (int) maxX;
        final int y2 = (int) maxY;
        final int z2 = (int) maxZ;

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2) + 1;
        this.maxY = Math.max(y1, y2) + 1;
        this.maxZ = Math.max(z1, z2) + 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maxX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BoundingBox)) return false;
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(maxX) != Double.doubleToLongBits(other.maxX)) return false;
        if (Double.doubleToLongBits(maxY) != Double.doubleToLongBits(other.maxY)) return false;
        if (Double.doubleToLongBits(maxZ) != Double.doubleToLongBits(other.maxZ)) return false;
        if (Double.doubleToLongBits(minX) != Double.doubleToLongBits(other.minX)) return false;
        if (Double.doubleToLongBits(minY) != Double.doubleToLongBits(other.minY)) return false;
        return Double.doubleToLongBits(minZ) == Double.doubleToLongBits(other.minZ);
    }

    @Override
    public String toString() {
        return "BoundingBox [minX=" +
                minX +
                ", minY=" +
                minY +
                ", minZ=" +
                minZ +
                ", maxX=" +
                maxX +
                ", maxY=" +
                maxY +
                ", maxZ=" +
                maxZ +
                "]";
    }

    /**
     * Creates a copy of this bounding box.
     *
     * @return the cloned bounding box
     */
    @NotNull
    @Override
    public BoundingBox clone() {
        try {
            return (BoundingBox) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("minX", minX);
        result.put("minY", minY);
        result.put("minZ", minZ);
        result.put("maxX", maxX);
        result.put("maxY", maxY);
        result.put("maxZ", maxZ);
        return result;
    }
}