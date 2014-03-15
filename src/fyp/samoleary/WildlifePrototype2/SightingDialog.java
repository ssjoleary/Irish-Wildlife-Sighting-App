package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

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
    private ImageView imgView;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

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
        imgView = (ImageView) findViewById(R.id.imageViewSightingDialog);

        Intent i = getIntent();
        Sighting sighting = (Sighting) i.getSerializableExtra(SIGHTING);

        species.setText(sighting.getSpecies());
        date.setText(sighting.getDate());
        location.setText(sighting.getLocation());
        animal.setText(Integer.toString(sighting.getAnimals()));
        lat.setText(String.format("%.5f", sighting.getSightingLat()));
        lng.setText(String.format("%.5f", sighting.getSightingLong()));

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();

        Uri imgUri = Uri.parse(mPrefs.getString("imgFileUri", "default"));
        if ((imgUri.toString().equals("default"))) {
            Log.d("IrishWildlife", "Received default");
        }else{
            try {
                Bitmap mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                imgView.setImageBitmap(mImgBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
