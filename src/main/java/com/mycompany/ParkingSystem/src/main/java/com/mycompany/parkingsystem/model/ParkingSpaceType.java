package com.mycompany.parkingsystem.model;

import java.awt.Color;

/**
 * Enumerates the different types of parking spaces with additional metadata
 * to support advanced parking algorithms and visualization.
 */
public enum ParkingSpaceType {
    /**
     * Parallel parking spaces (along the curb)
     * - Typical angle: 0° (parallel to curb)
     * - Requires: Advanced maneuvering skills
     * - Difficulty: High
     */
    PARALLEL("Parallel", 0, 0.8, Color.decode("#FF6B6B")),
    
    /**
     * Perpendicular parking spaces (90° to curb)
     * - Typical angle: 90° to curb
     * - Requires: Standard driving skills
     * - Difficulty: Medium
     */
    PERPENDICULAR("Perpendicular", 90, 0.4, Color.decode("#4ECDC4")),
    
    /**
     * Angled parking spaces (typically 45° or 60°)
     * - Typical angle: 45° to curb
     * - Requires: Basic maneuvering skills
     * - Difficulty: Low-Medium
     */
    ANGLE("Angled", 45, 0.3, Color.decode("#45B7D1")),
    
    /**
     * Standard regular parking spaces
     * - Typical angle: 0° or 90° (context dependent)
     * - Requires: Basic driving skills
     * - Difficulty: Low
     */
    REGULAR("Regular", 0, 0.2, Color.decode("#A5D8A5")),
    
    /**
     * Compact car parking spaces
     * - Smaller than standard spaces
     * - Requires: Precise maneuvering
     * - Difficulty: Medium-High
     */
    COMPACT("Compact", 90, 0.5, Color.decode("#FFD166")),
    
    /**
     * Disabled parking spaces
     * - Wider than standard spaces
     * - Requires: Basic driving skills
     * - Difficulty: Very Low
     */
    DISABLED("Disabled", 90, 0.1, Color.decode("#7E8D85")),
    
    /**
     * Obstacles (pillars, planters, etc.)
     * - Not actual parking spaces
     * - Used for collision detection
     */
    OBSTACLE("Obstacle", 0, 1.0, Color.decode("#5C5C5C"));
    
    private final String displayName;
    private final int defaultAngle;
    private final double difficultyFactor;
    private final Color displayColor;
    
    ParkingSpaceType(String displayName, int defaultAngle, 
                    double difficultyFactor, Color displayColor) {
        this.displayName = displayName;
        this.defaultAngle = defaultAngle;
        this.difficultyFactor = difficultyFactor;
        this.displayColor = displayColor;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getDefaultAngle() {
        return defaultAngle;
    }
    
    public double getDifficultyFactor() {
        return difficultyFactor;
    }
    
    public Color getDisplayColor() {
        return displayColor;
    }
    
    public boolean isParkable() {
        return this != OBSTACLE;
    }
    
    public boolean requiresSpecialSkills() {
        return this == PARALLEL || this == COMPACT;
    }
    
    public static ParkingSpaceType fromString(String text) {
        for (ParkingSpaceType type : ParkingSpaceType.values()) {
            if (type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return REGULAR; // Default to regular if unknown
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}