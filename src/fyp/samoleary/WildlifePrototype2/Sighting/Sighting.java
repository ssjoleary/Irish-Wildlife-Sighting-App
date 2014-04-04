package fyp.samoleary.WildlifePrototype2.Sighting;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import fyp.samoleary.WildlifePrototype2.LocationUtils;

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
    private String name;
    private String species;
    private String date;
    private double sightingLat;
    private double sightingLong;
    private String location;
    private int animals;
    private String imgUrl = "image";

    public Sighting(int ID, String species, String date, double sightingLat, double sightingLong, String location, int animals, String name, String imgUrl) {
        this.ID = ID;
        this.species = species;
        this.date = date;
        this.sightingLat = sightingLat;
        this.sightingLong = sightingLong;
        this.location = location;
        this.animals = animals;
        this.name = name;
        this.imgUrl = imgUrl;
    }

    public Sighting(int ID, String species, String date, String sightingLat, String sightingLong, String location, String animals, String name) {
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
            try{
                this.sightingLat = Double.parseDouble(sightingLat);
            } catch (NumberFormatException e) {
                this.sightingLong = 0;
                Log.d(LocationUtils.APPTAG, "NumberFormatException: Sighting.java: 62");
            }
        }
        if(sightingLong.equals("")){
            this.sightingLong = 0;
        } else {
            try {
                this.sightingLong = Double.parseDouble(sightingLong);
            } catch (NumberFormatException e) {
                this.sightingLong = 0;
                Log.d(LocationUtils.APPTAG, "NumberFormatException: Sighting.java: 72");
            }
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
        if(name.equals("")){
            this.name = "N/A";
        } else {
            this.name = name;
        }
    }

    /*public Sighting(String species, String date, double sightingLat, double sightingLong, String location, int animals) {
        this.species = species;
        this.date = date;
        this.sightingLat = sightingLat;
        this.sightingLong = sightingLong;
        this.location = location;
        this.animals = animals;
    }
    */
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

    public String getImgUrlString() {
        if (imgUrl == null) {
            imgUrl = "image";
            return imgUrl;
        }
        return imgUrl;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(sightingLat, sightingLong);
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
}
