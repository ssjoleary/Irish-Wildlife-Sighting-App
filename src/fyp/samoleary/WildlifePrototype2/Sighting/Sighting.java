package fyp.samoleary.WildlifePrototype2.Sighting;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

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
public class Sighting implements Serializable, ClusterItem {
    private final static long serialVersionUID = 1;

    private int ID;
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

    public Sighting(int ID, String species, String date, String sightingLat, String sightingLong, String location, String animals) {
        this.ID = ID;
        if(species.equals("")){
            this.species = "N/A";
        } else {
            this.species = species;
        }
        if(date.equals("")){
            this.date = "N/A";
        } else {
            this.date = date;
        }
        if(sightingLat.equals("")){
            this.sightingLat = 0;
        } else {
            this.sightingLat = Double.parseDouble(sightingLat);
        }
        if(sightingLong.equals("")){
            this.sightingLong = 0;
        } else {
            this.sightingLong = Double.parseDouble(sightingLong);
        }
        if(location.equals("")){
            this.location = "N/A";
        } else {
            this.location = location;
        }
        if(animals.equals("")){
            this.animals = 0;
        } else {
            this.animals = Integer.parseInt(animals);
        }
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

    @Override
    public LatLng getPosition() {
        return new LatLng(sightingLat, sightingLong);
    }

    public int getID() {
        return ID;
    }
}
