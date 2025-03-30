/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingsystem.model;
import java.awt.geom.Point2D;
/**
 *
 * @author DELL
 */
public class ParkingSpace {
    private Point2D.Double location;
    private double width;
    private double length;
    private ParkingSpaceType type;
    private boolean occupied;

    public ParkingSpace(Point2D.Double location, double width, double length, ParkingSpaceType type) {
        this.location = location;
        this.width = width;
        this.length = length;
        this.type = type;
        this.occupied = false;
    }

    // Getters and setters
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public Point2D.Double getLocation() { return location; }
    public ParkingSpaceType getType() { return type; }
    public double getWidth() { return width; }
    public double getLength() { return length; }
}
