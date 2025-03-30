/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingsystem.algorithm;
import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.Vehicle;
import java.awt.geom.Point2D;
import java.util.*;
/**
 *
 * @author DELL
 */
public class ParkingAlgorithm {
    private List<ParkingSpace> parkingSpaces;

    public ParkingAlgorithm(List<ParkingSpace> parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }

    // Find optimal parking space
    public ParkingSpace findOptimalParkingSpace(Vehicle vehicle) {
        return parkingSpaces.stream()
            .filter(space -> !space.isOccupied())
            .filter(space -> isSpaceSuitable(vehicle, space))
            .findFirst()
            .orElse(null);
    }

    // Check if space is suitable for vehicle
    private boolean isSpaceSuitable(Vehicle vehicle, ParkingSpace space) {
        // Check space dimensions
        return space.getWidth() >= vehicle.getWidth() * 1.5 &&
               space.getLength() >= vehicle.getLength() * 1.2;
    }

    // Complex parking maneuver
    public boolean parkVehicle(Vehicle vehicle, ParkingSpace space) {
        // Parking is a multi-step process
        try {
            approachParkingSpace(vehicle, space);
            alignVehicle(vehicle, space);
            reverseIntoParkingSpace(vehicle, space);
            finalAdjustment(vehicle, space);
            
            // Mark space as occupied
            space.setOccupied(true);
            return true;
        } catch (Exception e) {
            System.out.println("Parking failed: " + e.getMessage());
            return false;
        }
    }

    // Approach parking space
    private void approachParkingSpace(Vehicle vehicle, ParkingSpace space) {
        // Calculate approach angle and distance
        Point2D.Double spaceLocation = space.getLocation();
        double approachAngle = calculateApproachAngle(vehicle.getPosition(), spaceLocation);
        double approachDistance = calculateApproachDistance(vehicle, space);

        // Rotate and move vehicle
        vehicle.move(approachAngle, approachDistance);
    }

    // Align vehicle with parking space
    private void alignVehicle(Vehicle vehicle, ParkingSpace space) {
        // Precise alignment calculations
        double targetAngle = calculateParkingAngle(space);
        rotateVehicle(vehicle, targetAngle);
    }

    // Reverse into parking space
    private void reverseIntoParkingSpace(Vehicle vehicle, ParkingSpace space) {
        // Reverse with controlled steering
        double reverseDistance = space.getLength() * 0.9;
        vehicle.move(-15, reverseDistance); // Slight angle for realistic parking
    }

    // Final parking adjustment
    private void finalAdjustment(Vehicle vehicle, ParkingSpace space) {
        // Micro-adjustments to center vehicle
        vehicle.move(0, 0.1); // Small forward nudge
    }

    // Helper calculation methods
    private double calculateApproachAngle(Point2D.Double currentPos, Point2D.Double spacePos) {
        return Math.toDegrees(Math.atan2(spacePos.y - currentPos.y, spacePos.x - currentPos.x));
    }

    private double calculateApproachDistance(Vehicle vehicle, ParkingSpace space) {
        // Complex distance calculation considering vehicle and space characteristics
        return Math.sqrt(
            Math.pow(space.getLocation().x - vehicle.getPosition().x, 2) +
            Math.pow(space.getLocation().y - vehicle.getPosition().y, 2)
        );
    }

    private double calculateParkingAngle(ParkingSpace space) {
        // Determine optimal parking angle based on space type
        switch (space.getType()) {
            case PARALLEL: return 45;
            case PERPENDICULAR: return 0;
            case ANGLE: return 30;
            default: return 0;
        }
    }

    private void rotateVehicle(Vehicle vehicle, double targetAngle) {
        // Precise vehicle rotation
        double currentAngle = vehicle.getAngle();
        double rotationAngle = targetAngle - currentAngle;
        vehicle.move(rotationAngle, 0);
    }
}
