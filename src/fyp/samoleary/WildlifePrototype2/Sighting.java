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
    private String imgUri = null;

    public Sighting(String species, String date, double sightingLat, double sightingLong, String location, int animals, String imgUri) {
        this.species = species;
        this.date = date;
        this.sightingLat = sightingLat;
        this.sightingLong = sightingLong;
        this.location = location;
        this.animals = animals;
        this.imgUri = imgUri;
    }

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

    public String getDate() {
        return date;
    }

    public double getSightingLat() {
        return sightingLat;
    }

    public double getSightingLong() {
        return sightingLong;
    }

    public int getAnimals() {
        return animals;
    }

    public String getLocation() {
        return location;
    }

    public String getImgUriString() {
        if (imgUri == null) {
            imgUri = "default";
            return imgUri;
        }
        return imgUri;
    }

}
