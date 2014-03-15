package fyp.samoleary.WildlifePrototype2;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 11/02/14
 * Revision: 2
 * Revision History:
 * 1: 11/02/14
 * 2: 12/02/14
 * Description:
 */
public class SubmitActivity extends Activity {

    private Spinner speciesSpinner;
    private Spinner countySpinner;
    private Date date;
    private TextView date_view;
    private LatLng user_coordinates;
    public final static String USER_COORDINATES = "fyp.samoleary.WildlifePrototype2.COORDINATES";
    private TextView lat_view;
    private TextView long_view;
    private TextView animals_view;
    private TextView location_view;
    private Button submit_btn;
    private Sighting userSighting;
    private String species;
    private String county;
    private Button inclPhoto;

    private WildlifeDB wildlifeDB;

    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.submit_sighting);

        List<String> speciesList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.species_array)));
        List<String> countiesList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.counties_array)));

        speciesList.remove(0);
        countiesList.remove(0);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        date = new Date();

        speciesSpinner = (Spinner) findViewById(R.id.submit_form_spinner_species);
        countySpinner = (Spinner) findViewById(R.id.submit_form_spinner_county);
        date_view = (TextView) findViewById(R.id.submit_form_date_input);
        lat_view = (TextView) findViewById(R.id.submit_form_latitude_input);
        long_view = (TextView) findViewById(R.id.submit_form_longitude_input);
        animals_view = (TextView) findViewById(R.id.submit_form_animals);
        location_view = (TextView) findViewById(R.id.submit_form_location);
        submit_btn = (Button) findViewById(R.id.submit_form_btn);
        inclPhoto = (Button) findViewById(R.id.submit_form_inclphoto);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSighting();
            }
        });

        inclPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ArrayAdapter<String> adapter = new  ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speciesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(adapter);
        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                species = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapterCounty = new  ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countiesList);
        adapterCounty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countySpinner.setAdapter(adapterCounty);
        countySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                county = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date_view.setText(date.toString());

        Intent intent = getIntent();
        user_coordinates = intent.getParcelableExtra(USER_COORDINATES);
        lat_view.setText(String.format("%.5f", user_coordinates.latitude));
        long_view.setText(String.format("%.5f", user_coordinates.longitude));

        wildlifeDB = new WildlifeDB(this);
        wildlifeDB.open();
    }

    private void submitSighting() {
        userSighting = createSighting();

        wildlifeDB.insertInfo(userSighting);
        wildlifeDB.close();

        Intent intent = new Intent(this, GMapActivity.class);
        setResult(RESULT_OK, intent);
        intent.putExtra("userSighting", userSighting);
        finish();
    }

    private Sighting createSighting() {
        double sightingLat, sightingLong;
        String location;
        int animals;

        String dateOut = date.toString();
        sightingLat = user_coordinates.latitude;
        sightingLong = user_coordinates.longitude;
        if (location_view.getText().toString().equals("")) {
            location = "Not Set";
        } else {
            location = location_view.getText().toString();
        }
        location += ", " + county;

        if (animals_view.getText().toString().equals("")) {
            animals = 0;
        } else {
            animals = Integer.parseInt(animals_view.getText().toString());
        }
        return new Sighting(species, dateOut, sightingLat, sightingLong, location, animals);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        /*switch (view.getId()) {
            case R.id.submit_form_confidence_1:
                confidenceLvl_selected = 1;
                break;
            case R.id.submit_form_confidence_2:
                confidenceLvl_selected = 2;
                break;
            case R.id.submit_form_confidence_3:
                confidenceLvl_selected = 3;
                break;
        } */
    }
}
