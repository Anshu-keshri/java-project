package com.mycompany.parkingsystem.controller;

import com.mycompany.parkingsystem.model.Vehicle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Advanced keyboard controller for vehicle input with smooth controls,
 * multiple control schemes, and enhanced driving physics.
 */
public class KeyboardController implements KeyListener, VehicleController {
    private Vehicle controlledVehicle;
    private Set<Integer> pressedKeys;

    public void setControlledVehicle(Vehicle v) {
        if (v == null) {
        throw new IllegalArgumentException("Vehicle cannot be null");
    }
    this.controlledVehicle = v;
    resetControls(); // Reset any existing control state
    
    // Optional: Initialize vehicle to neutral state
    v.setSpeed(0);
    v.steer(0);
    }
    
    // Control schemes
    public enum ControlScheme {
        ARCADE,  // Simplified controls (default)
        SIMULATION,  // More realistic physics
        PROFESSIONAL  // Advanced controls with manual transmission
    }
    
    private ControlScheme currentScheme = ControlScheme.ARCADE;
    
    // Control sensitivity parameters
    private double accelerationSensitivity = 1.0;
    private double steeringSensitivity = 1.0;
    private double brakingSensitivity = 1.0;
    
    // Control state
    private double currentThrottle = 0;
    private double currentSteering = 0;
    private boolean handbrakeEngaged = false;
    
    // Control constants
    private static final double MAX_THROTTLE_INPUT = 1.0;
    private static final double MAX_BRAKE_INPUT = 1.0;
    private static final double MAX_STEERING_INPUT = 1.0;
    private static final double STEERING_RETURN_RATE = 2.0; // Speed of wheel centering
    
    public KeyboardController(Vehicle vehicle) {
        this.controlledVehicle = vehicle;
        this.pressedKeys = new HashSet<>();
    }
    
    @Override
    public void update(double deltaTime) {
        // Process input based on current control scheme
        switch (currentScheme) {
            case ARCADE:
                updateArcadeControls(deltaTime);
                break;
            case SIMULATION:
                updateSimulationControls(deltaTime);
                break;
            case PROFESSIONAL:
                updateProfessionalControls(deltaTime);
                break;
        }
        
        // Apply gradual steering return when no keys are pressed
        if (!isSteeringKeyPressed()) {
            returnSteeringToCenter(deltaTime);
        }
    }
    
    private void updateArcadeControls(double deltaTime) {
        // Throttle control (up/down arrows)
        if (isKeyPressed(KeyEvent.VK_UP)) {
            currentThrottle = Math.min(currentThrottle + deltaTime * 2, MAX_THROTTLE_INPUT);
        } else if (isKeyPressed(KeyEvent.VK_DOWN)) {
            currentThrottle = Math.max(currentThrottle - deltaTime * 3, -MAX_THROTTLE_INPUT/2);
        } else {
            // Gradual throttle return
            currentThrottle *= Math.pow(0.2, deltaTime);
        }
        
        // Apply throttle/brake
        if (currentThrottle > 0) {
            controlledVehicle.accelerate(currentThrottle * accelerationSensitivity);
        } else if (currentThrottle < 0) {
            controlledVehicle.brake(-currentThrottle * brakingSensitivity);
        }
        
        // Steering control (left/right arrows)
        if (isKeyPressed(KeyEvent.VK_LEFT)) {
            currentSteering = Math.max(currentSteering - deltaTime * 3, -MAX_STEERING_INPUT);
        } else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
            currentSteering = Math.min(currentSteering + deltaTime * 3, MAX_STEERING_INPUT);
        }
        
        controlledVehicle.steer(currentSteering * steeringSensitivity);
        
        // Handbrake (space bar)
        if (isKeyPressed(KeyEvent.VK_SPACE) && !handbrakeEngaged) {
            controlledVehicle.handbrake();
            handbrakeEngaged = true;
        } else if (!isKeyPressed(KeyEvent.VK_SPACE)) {
            handbrakeEngaged = false;
        }
    }
    
    private void updateSimulationControls(double deltaTime) {
        // More realistic physics-based controls
        
        // Separate throttle and brake controls
        double throttleInput = 0;
        double brakeInput = 0;
        
        if (isKeyPressed(KeyEvent.VK_UP)) {
            throttleInput = MAX_THROTTLE_INPUT;
        }
        if (isKeyPressed(KeyEvent.VK_DOWN)) {
            brakeInput = MAX_BRAKE_INPUT;
        }
        
        // Apply inputs with more realistic response curves
        if (throttleInput > 0) {
            controlledVehicle.accelerate(throttleInput * accelerationSensitivity);
        }
        if (brakeInput > 0) {
            controlledVehicle.brake(brakeInput * brakingSensitivity);
        }
        
        // More gradual steering
        if (isKeyPressed(KeyEvent.VK_LEFT)) {
            currentSteering = Math.max(currentSteering - deltaTime * 1.5, -MAX_STEERING_INPUT);
        } else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
            currentSteering = Math.min(currentSteering + deltaTime * 1.5, MAX_STEERING_INPUT);
        }
        
        // Speed-sensitive steering
        double speedFactor = 1.0 - Math.min(1.0, controlledVehicle.getSpeed() / 10.0);
        controlledVehicle.steer(currentSteering * steeringSensitivity * speedFactor);
        
        // Handbrake with weight transfer simulation
        if (isKeyPressed(KeyEvent.VK_SPACE) && !handbrakeEngaged) {
            controlledVehicle.handbrake();
            handbrakeEngaged = true;
        } else if (!isKeyPressed(KeyEvent.VK_SPACE)) {
            handbrakeEngaged = false;
        }
    }
    
    private void updateProfessionalControls(double deltaTime) {
        // Advanced controls with gear shifting
        
        // Gear shifting (W/S for throttle/brake, A/D for gear up/down)
        // Implementation would require Vehicle class to support manual transmission
        
        // Placeholder - same as simulation for now
        updateSimulationControls(deltaTime);
    }
    
    private void returnSteeringToCenter(double deltaTime) {
        if (currentSteering > 0) {
            currentSteering = Math.max(0, currentSteering - deltaTime * STEERING_RETURN_RATE);
        } else if (currentSteering < 0) {
            currentSteering = Math.min(0, currentSteering + deltaTime * STEERING_RETURN_RATE);
        }
        controlledVehicle.steer(currentSteering * steeringSensitivity);
    }
    
    private boolean isSteeringKeyPressed() {
        return isKeyPressed(KeyEvent.VK_LEFT) || isKeyPressed(KeyEvent.VK_RIGHT);
    }
    
    // Configuration methods
    public void setControlScheme(ControlScheme scheme) {
        this.currentScheme = scheme;
        resetControls();
    }
    
    public void setSensitivity(double acceleration, double steering, double braking) {
        this.accelerationSensitivity = acceleration;
        this.steeringSensitivity = steering;
        this.brakingSensitivity = braking;
    }
    
    private void resetControls() {
        currentThrottle = 0;
        currentSteering = 0;
        handbrakeEngaged = false;
    }
    
    // Key input handling
    private boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        
        // Additional key bindings for control scheme switching
        switch (e.getKeyCode()) {
            case KeyEvent.VK_F1:
                setControlScheme(ControlScheme.ARCADE);
                break;
            case KeyEvent.VK_F2:
                setControlScheme(ControlScheme.SIMULATION);
                break;
            case KeyEvent.VK_F3:
                setControlScheme(ControlScheme.PROFESSIONAL);
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        
        // Reset throttle/brake when releasing keys in arcade mode
        if (currentScheme == ControlScheme.ARCADE) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                currentThrottle = 0;
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    // Getters for current control state
    public double getCurrentThrottle() {
        return currentThrottle;
    }
    
    public double getCurrentSteering() {
        return currentSteering;
    }
    
    public boolean isHandbrakeEngaged() {
        return handbrakeEngaged;
    }
    
    public ControlScheme getCurrentScheme() {
        return currentScheme;
    }
}