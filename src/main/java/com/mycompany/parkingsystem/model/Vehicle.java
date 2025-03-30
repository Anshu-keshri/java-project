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
public class Vehicle {
    private Point2D.Double position;
    private double length;
    private double width;
    private double angle;
    private double wheelBase;

    public Vehicle(Point2D.Double startPosition, double length, double width) {
        this.position = startPosition;
        this.length = length;
        this.width = width;
        this.angle = 0;
        this.wheelBase = length * 0.6; // Typical wheel base ratio
    }

    // Advanced movement method using Ackermann steering geometry
    public void move(double steeringAngle, double distance) {
        // Calculate turning radius
        double turningRadius = wheelBase / Math.tan(Math.toRadians(steeringAngle + 0.0001)); // Prevent division by zero
        
        // Calculate new position and orientation
        double newX = position.x + distance * Math.cos(Math.toRadians(angle));
        double newY = position.y + distance * Math.sin(Math.toRadians(angle));
        
        // Update position and angle
        position = new Point2D.Double(newX, newY);
        angle += (distance / turningRadius) * Math.toDegrees(1);
    }

    // Getters and additional methods
    public Point2D.Double getPosition() { return position; }
    public double getLength() { return length; }
    public double getWidth() { return width; }
    public double getAngle() { return angle; }
}
