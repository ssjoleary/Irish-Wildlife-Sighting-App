package fyp.samoleary.WildlifePrototype2;

import java.io.Serializable;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 12/02/14
 * Revision: 1
 * Revision History:
 * 1: 12/02/14
 * Description:
 */
public class Sighting implements Serializable {
    private final static long serialVersionUID = 1;

    private String species;
    private String date;
    private double sightingLat;
    private double sightingLong;
    private String location;
    private int animals;

    public Sighting(String species, String date, double sightingLat, double sightingLong, String location, int animals) {
        this.species = species;
        this.date = date;
        this.sightingLat = sightingLat;
        this.sightingLong = sightingLong;
        this.location = location;
        this.animals = animals;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getSightingLat() {
        return sightingLat;
    }

    public String getLatString() {
        return Double.toString(sightingLat);
    }

    public void setSightingLat(double sightingLat) {
        this.sightingLat = sightingLat;
    }

    public double getSightingLong() {
        return sightingLong;
    }

    public String getLngString() {
        return Double.toString(sightingLong);
    }

    public void setSightingLong(double sightingLong) {
        this.sightingLong = sightingLong;
    }

    public int getAnimals() {
        return animals;
    }

    public void setAnimals(int animals) {
        this.animals = animals;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
