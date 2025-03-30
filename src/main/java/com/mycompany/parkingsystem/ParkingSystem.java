/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingsystem;
import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.Vehicle;
import com.mycompany.parkingsystem.model.ParkingSpaceType;
import com.mycompany.parkingsystem.algorithm.ParkingAlgorithm;
import java.awt.geom.Point2D;
import java.util.*;
/**
 *
 * @author DELL
 */
public class ParkingSystem {
    public static void main(String[] args) {
        // Create parking spaces
        List<ParkingSpace> spaces = Arrays.asList(
            new ParkingSpace(new Point2D.Double(10, 20), 2.5, 5.0, ParkingSpaceType.PARALLEL),
            new ParkingSpace(new Point2D.Double(15, 25), 2.3, 4.8, ParkingSpaceType.PERPENDICULAR)
        );

        // Create vehicle
        Vehicle car = new Vehicle(new Point2D.Double(0, 0), 4.5, 1.8);

        // Initialize parking algorithm
        ParkingAlgorithm parkingAlgorithm = new ParkingAlgorithm(spaces);

        // Find and park in a space
        ParkingSpace targetSpace = parkingAlgorithm.findOptimalParkingSpace(car);
        if (targetSpace != null) {
            boolean parkingSuccess = parkingAlgorithm.parkVehicle(car, targetSpace);
            System.out.println("Parking " + (parkingSuccess ? "Successful" : "Failed"));
            
            // Print final vehicle position and angle
            System.out.println("Final Vehicle Position: " + car.getPosition());
            System.out.println("Final Vehicle Angle: " + car.getAngle());
        } else {
            System.out.println("No suitable parking space found.");
        }
    }
}
