package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 6/06/14
 * Revision: 1
 * Revision History:
 * 1: 6/06/14
 * Description:
 */
public class SearchActivity extends Activity {
    private Spinner speciesSpinner;
    private Spinner countySpinner;
    private int speciesPos;
    private int countyPos;

    public void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.search_sightings);
        initSpinners();
    }
    public void initSpinners(){
        speciesSpinner = (Spinner) findViewById(R.id.search_species_spinner);
        countySpinner = (Spinner) findViewById(R.id.search_county_spinner);
        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speciesPos = parent.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        countySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countyPos = parent.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.species_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> countyadapter = ArrayAdapter.createFromResource(this,
                R.array.counties_array, android.R.layout.simple_spinner_item);
        countyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countySpinner.setAdapter(countyadapter);
    }

}

