package com.ecosimulator.model;

/**
 * Represents a cell in the ecosystem grid
 */
public class Cell {
    private String type;  // "empty", "prey", "predator", "third"
    private Animal animal;  // The animal in this cell (if any)
    
    public Cell() {
        this.type = "empty";
        this.animal = null;
    }
    
    public Cell(String type) {
        this.type = type;
        this.animal = null;
    }
    
    public Cell(String type, Animal animal) {
        this.type = type;
        this.animal = animal;
    }
    
    public boolean isEmpty() {
        return "empty".equals(type) && animal == null;
    }
    
    public boolean hasAnimal() {
        return animal != null;
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Animal getAnimal() {
        return animal;
    }
    
    public void setAnimal(Animal animal) {
        this.animal = animal;
        if (animal != null) {
            this.type = animal.getType();
        } else {
            this.type = "empty";
        }
    }
    
    public void clear() {
        this.type = "empty";
        this.animal = null;
    }
}
