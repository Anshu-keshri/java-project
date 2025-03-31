package com.mycompany.parkingsystem.model;

import java.awt.geom.Point2D;

/**
 * Represents a parking space in the simulation
 */
public class ParkingSpace {
    private final String id;
    private final Point2D.Double location;
    private final double width;
    private final double length;
    private final ParkingSpaceType type;
    private boolean occupied;
    private double angle; // For angled parking spaces
    
    /**
     * Creates a new parking space
     * @param location The position of the space (top-left corner)
     * @param width The width of the space (shorter dimension)
     * @param length The length of the space (longer dimension)
     * @param type The type of parking space
     * @param id Unique identifier for the space
     */
    public ParkingSpace(Point2D.Double location, double width, double length, 
                       ParkingSpaceType type, String id) {
        if (location == null || type == null || id == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if (width <= 0 || length <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        
        this.location = location;
        this.width = width;
        this.length = length;
        this.type = type;
        this.id = id;
        this.occupied = false;
        this.angle = 0;
        
        // Set default angle based on space type
        if (type == ParkingSpaceType.ANGLE) {
            this.angle = 45; // Standard angle parking
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public Point2D.Double getLocation() {
        return new Point2D.Double(location.x, location.y);
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public ParkingSpaceType getType() {
        return type;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public double getAngle() {
        return angle;
    }

    // Setters
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    /**
     * Calculates the center point of the parking space
     * @return The center point
     */
    public Point2D.Double getCenter() {
        return new Point2D.Double(
            location.x + length / 2,
            location.y + width / 2
        );
    }
    
    @Override
    public String toString() {
        return String.format("ParkingSpace[id=%s, type=%s, location=(%.2f,%.2f), size=%.2fx%.2f, occupied=%s, angle=%.1f°",
            id, type, location.x, location.y, length, width, occupied, angle);
    }
}