package com.restaurant.pos.domain.model;

/**
 * Restaurant table. Used for order assignment.
 */
public class Table {

    private int number;
    private boolean occupied;

    public Table() {
    }

    public Table(int number) {
        this.number = number;
        this.occupied = false;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
