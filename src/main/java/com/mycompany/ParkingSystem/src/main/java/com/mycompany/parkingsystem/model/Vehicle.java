package com.mycompany.parkingsystem.model;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * Represents a vehicle in the parking simulation with realistic physics.
 */
public class Vehicle {
    private Point2D.Double position;
    private Point2D.Double velocity;
    private double length;
    private double width;
    private double angle; // in degrees
    private double speed; // in meters per second
    private double acceleration; // in m/s²
    private double steeringAngle; // in degrees
    private boolean userControlled;
    private boolean parked;
    private Color color;
    private String licensePlate;
    
    // Physics constants
    private static final double MAX_FORWARD_SPEED = 5.0; // m/s (~18 km/h)
    private static final double MAX_REVERSE_SPEED = 2.0; // m/s
    private static final double MAX_ACCELERATION = 2.5; // m/s²
    private static final double MAX_DECELERATION = 6.0; // m/s² (braking)
    private static final double MAX_STEERING_ANGLE = 35.0; // degrees
    private static final double STEERING_RESPONSE = 80.0; // degrees per second
    private static final double WHEELBASE_RATIO = 0.6; // wheelbase/length ratio
    private static final double FRICTION = 0.8; // rolling friction coefficient
    private static final double AIR_RESISTANCE = 0.1; // air resistance coefficient
    
    // Vehicle state
    private boolean reversing;
    private double targetSteeringAngle;
    private double targetSpeed;
    
    public Vehicle(Point2D.Double position, double length, double width) {
        this.position = new Point2D.Double(position.x, position.y);
        this.velocity = new Point2D.Double(0, 0);
        this.length = length;
        this.width = width;
        this.angle = 0.0;
        this.speed = 0.0;
        this.acceleration = 0.0;
        this.steeringAngle = 0.0;
        this.targetSteeringAngle = 0.0;
        this.targetSpeed = 0.0;
        this.userControlled = false;
        this.parked = false;
        this.reversing = false;
        this.color = generateRandomColor();
        this.licensePlate = generateLicensePlate();
    }
    
    /**
     * Updates the vehicle's state based on physics simulation.
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(double deltaTime) {
        if (parked) {
            // Parked vehicles don't move
            speed = 0;
            acceleration = 0;
            steeringAngle = 0;
            return;
        }
        
        // Update steering (smooth steering response)
        if (steeringAngle < targetSteeringAngle) {
            steeringAngle = Math.min(targetSteeringAngle, 
                                  steeringAngle + STEERING_RESPONSE * deltaTime);
        } else if (steeringAngle > targetSteeringAngle) {
            steeringAngle = Math.max(targetSteeringAngle, 
                                  steeringAngle - STEERING_RESPONSE * deltaTime);
        }
        
        // Update speed based on acceleration
        if (Math.abs(speed - targetSpeed) > 0.1) {
            double accelerationDirection = Math.signum(targetSpeed - speed);
            acceleration = accelerationDirection * MAX_ACCELERATION;
            speed += acceleration * deltaTime;
            
            // Apply speed limits
            if (reversing) {
                speed = Math.max(-MAX_REVERSE_SPEED, Math.min(speed, 0));
            } else {
                speed = Math.max(0, Math.min(speed, MAX_FORWARD_SPEED));
            }
        } else {
            // Apply friction when not accelerating
            double friction = FRICTION * deltaTime;
            if (Math.abs(speed) > friction) {
                speed -= Math.signum(speed) * friction;
            } else {
                speed = 0;
            }
        }
        
        // Apply air resistance (quadratic drag)
        double airDrag = AIR_RESISTANCE * speed * speed * deltaTime;
        if (Math.abs(speed) > airDrag) {
            speed -= Math.signum(speed) * airDrag;
        } else {
            speed = 0;
        }
        
        // Update position based on speed and angle
        if (Math.abs(speed) > 0.01) {
            // Convert angle to radians for calculation
            double angleRad = Math.toRadians(angle);
            
            // Calculate velocity components
            velocity.x = speed * Math.cos(angleRad);
            velocity.y = speed * Math.sin(angleRad);
            
            // Update position
            position.x += velocity.x * deltaTime;
            position.y += velocity.y * deltaTime;
            
            // Adjust angle based on steering (bicycle model)
            if (Math.abs(steeringAngle) > 0.1 && Math.abs(speed) > 0.1) {
                double wheelbase = length * WHEELBASE_RATIO;
                double turningRadius = wheelbase / Math.sin(Math.toRadians(Math.abs(steeringAngle)));
                double angularVelocity = (speed / turningRadius) * Math.signum(steeringAngle);
                angle += Math.toDegrees(angularVelocity) * deltaTime;
                
                // Normalize angle to 0-360
                angle = (angle % 360 + 360) % 360;
            }
        } else {
            velocity.x = 0;
            velocity.y = 0;
        }
    }
    
    // Control methods
    public void accelerate(double amount) {
        reversing = false;
        targetSpeed = amount * MAX_FORWARD_SPEED;
    }
    
    public void reverse(double amount) {
        reversing = true;
        targetSpeed = -amount * MAX_REVERSE_SPEED;
    }
    
    public void brake(double amount) {
        if (speed > 0) {
            targetSpeed = Math.max(0, speed - amount * MAX_DECELERATION);
        } else if (speed < 0) {
            targetSpeed = Math.min(0, speed + amount * MAX_DECELERATION);
        }
    }
    
    public void handbrake() {
        // Immediate stop with skidding effect
        targetSpeed = 0;
        speed *= 0.7; // Rapid deceleration
    }
    
    public void steer(double amount) {
        targetSteeringAngle = amount * MAX_STEERING_ANGLE;
    }
    
    public void centerSteering() {
        targetSteeringAngle = 0;
    }
    
    // Movement primitives for parking algorithm
    public void move(double steering, double throttle) {
        steer(steering / MAX_STEERING_ANGLE);
        if (throttle > 0) {
            accelerate(throttle / MAX_FORWARD_SPEED);
        } else if (throttle < 0) {
            reverse(-throttle / MAX_REVERSE_SPEED);
        } else {
            targetSpeed = 0;
        }
    }
    
    public void rotate(double degrees) {
        angle += degrees;
        angle = (angle % 360 + 360) % 360;
    }
    
    // Helper methods
    private Color generateRandomColor() {
        return new Color(
            (int)(Math.random() * 156) + 100, // Avoid very dark colors
            (int)(Math.random() * 156) + 100,
            (int)(Math.random() * 156) + 100
        );
    }
    
    private String generateLicensePlate() {
        String letters = "ABCDEFGHJKLMNPRSTUVWXYZ"; // Similar to real license plates
        String numbers = "0123456789";
        StringBuilder plate = new StringBuilder();
        
        // Format: XX-999-X
        plate.append(letters.charAt((int)(Math.random() * letters.length())));
        plate.append(letters.charAt((int)(Math.random() * letters.length())));
        plate.append('-');
        for (int i = 0; i < 3; i++) {
            plate.append(numbers.charAt((int)(Math.random() * numbers.length())));
        }
        plate.append('-');
        plate.append(letters.charAt((int)(Math.random() * letters.length())));
        
        return plate.toString();
    }
    
    // Getters and setters
    public Point2D.Double getPosition() {
        return new Point2D.Double(position.x, position.y);
    }

    public void setPosition(Point2D.Double position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }
    
    public Point2D.Double getVelocity() {
        return velocity;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getAngle() {
        return angle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public double getAcceleration() {
        return acceleration;
    }
    
    public double getSteeringAngle() {
        return steeringAngle;
    }
    
    public boolean isUserControlled() {
        return userControlled;
    }
    
    public void setUserControlled(boolean userControlled) {
        this.userControlled = userControlled;
    }
    
    public boolean isParked() {
        return parked;
    }
    
    public void setParked(boolean parked) {
        this.parked = parked;
        if (parked) {
            speed = 0;
            steeringAngle = 0;
        }
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public boolean isReversing() {
        return reversing;
    }
    
    public double getTurningRadius() {
        if (Math.abs(steeringAngle) < 0.1) return Double.POSITIVE_INFINITY;
        return (length * WHEELBASE_RATIO) / Math.sin(Math.toRadians(Math.abs(steeringAngle)));
    }
    
    public Point2D.Double[] getCornerPoints() {
        Point2D.Double[] corners = new Point2D.Double[4];
        double halfLength = length / 2;
        double halfWidth = width / 2;
        double angleRad = Math.toRadians(angle);
        
        // Front right
        corners[0] = new Point2D.Double(
            position.x + halfLength * Math.cos(angleRad) - halfWidth * Math.sin(angleRad),
            position.y + halfLength * Math.sin(angleRad) + halfWidth * Math.cos(angleRad)
        );
        
        // Front left
        corners[1] = new Point2D.Double(
            position.x + halfLength * Math.cos(angleRad) + halfWidth * Math.sin(angleRad),
            position.y + halfLength * Math.sin(angleRad) - halfWidth * Math.cos(angleRad)
        );
        
        // Rear left
        corners[2] = new Point2D.Double(
            position.x - halfLength * Math.cos(angleRad) + halfWidth * Math.sin(angleRad),
            position.y - halfLength * Math.sin(angleRad) - halfWidth * Math.cos(angleRad)
        );
        
        // Rear right
        corners[3] = new Point2D.Double(
            position.x - halfLength * Math.cos(angleRad) - halfWidth * Math.sin(angleRad),
            position.y - halfLength * Math.sin(angleRad) + halfWidth * Math.cos(angleRad)
        );
        
        return corners;
    }

    public void setTargetParkingSpace(ParkingSpace findRandomAvailableSpace) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void setSpeed(double d) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}