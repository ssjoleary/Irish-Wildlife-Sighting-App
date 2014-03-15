package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 6/03/14
 * Revision: 1
 * Revision History:
 * 1: 6/03/14
 * Description:
 */
public class SightingDialog extends Activity {
    private final static String SIGHTING = "fyp.samoleary.WildlifePrototype2.SIGHTING";
    private TextView species;
    private TextView date;
    private TextView location;
    private TextView animal;
    private TextView lat;
    private TextView lng;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sighting_dialog);

        species = (TextView) findViewById(R.id.sighting_species);
        date = (TextView) findViewById(R.id.sighting_date);
        location = (TextView) findViewById(R.id.sighting_location);
        animal = (TextView) findViewById(R.id.sighting_animal);
        lat = (TextView) findViewById(R.id.sighting_lat);
        lng = (TextView) findViewById(R.id.sighting_lng);

        Intent i = getIntent();
        Sighting sighting = (Sighting) i.getSerializableExtra(SIGHTING);

        species.setText(sighting.getSpecies());
        date.setText(sighting.getDate());
        location.setText(sighting.getLocation());
        animal.setText(Integer.toString(sighting.getAnimals()));
        lat.setText(String.format("%.5f", sighting.getSightingLat()));
        lng.setText(String.format("%.5f", sighting.getSightingLong()));
    }
}
