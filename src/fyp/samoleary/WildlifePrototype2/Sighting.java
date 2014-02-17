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
public class Sighting  implements Serializable {
    private final static long serialVersionUID = 1;

    private int speciesID;
    private int confidenceLvl;
    private String date;
    private double sightingLat;
    private double sightingLong;

    public Sighting(int speciesID, int confidenceLvl, String date, double sightingLat, double sightingLong) {
        this.speciesID = speciesID;
        this.confidenceLvl = confidenceLvl;
        this.date = date;
        this.sightingLat = sightingLat;
        this.sightingLong = sightingLong;
    }

    public int getSpeciesID() {
        return speciesID;
    }

    public void setSpeciesID(int speciesID) {
        this.speciesID = speciesID;
    }

    public int getConfidenceLvl() {
        return confidenceLvl;
    }

    public void setConfidenceLvl(int confidenceLvl) {
        this.confidenceLvl = confidenceLvl;
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

    public void setSightingLat(double sightingLat) {
        this.sightingLat = sightingLat;
    }

    public double getSightingLong() {
        return sightingLong;
    }

    public void setSightingLong(double sightingLong) {
        this.sightingLong = sightingLong;
    }

}
