package fyp.samoleary.WildlifePrototype2;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

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

    private Spinner spinner;
    private int spinnerPos;
    private Date date;
    private TextView date_view;
    private LatLng user_coordinates;
    public final static String USER_COORDINATES = "fyp.samoleary.WildlifePrototype2.COORDINATES";
    private TextView lat_view;
    private TextView long_view;
    private Button submit_btn;
    private Sighting userSighting;
    private int confidenceLvl_selected;

    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.submit_sighting);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        date = new Date();

        spinner = (Spinner) findViewById(R.id.submit_form_spinner);
        date_view = (TextView) findViewById(R.id.submit_form_date_input);
        lat_view = (TextView) findViewById(R.id.submit_form_latitude_input);
        long_view = (TextView) findViewById(R.id.submit_form_longitude_input);
        submit_btn = (Button) findViewById(R.id.submit_form_btn);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //submitSighting();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPos = parent.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        date_view.setText(date.toString());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.species_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Intent intent = getIntent();
        user_coordinates = intent.getParcelableExtra(USER_COORDINATES);
        lat_view.setText(String.format("%.5f", user_coordinates.latitude));
        long_view.setText(String.format("%.5f", user_coordinates.longitude));
    }

    private void submitSighting() {
        userSighting = createSighting();

        Intent intent = new Intent(this, GMapActivity.class);
        intent.putExtra("userSighting", userSighting);

    }

    private Sighting createSighting() {
        /**int speciesID, confidenceLvl;
        double sightingLat, sightingLong;

        speciesID = spinnerPos;
        confidenceLvl = confidenceLvl_selected;

        String dateOut = date.toString();
        sightingLat = user_coordinates.latitude;
        sightingLong = user_coordinates.longitude;

        return new Sighting(speciesID, confidenceLvl, dateOut, sightingLat, sightingLong);*/
        return null;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.submit_form_confidence_1:
                confidenceLvl_selected = 1;
                break;
            case R.id.submit_form_confidence_2:
                confidenceLvl_selected = 2;
                break;
            case R.id.submit_form_confidence_3:
                confidenceLvl_selected = 3;
                break;
        }
    }
}
