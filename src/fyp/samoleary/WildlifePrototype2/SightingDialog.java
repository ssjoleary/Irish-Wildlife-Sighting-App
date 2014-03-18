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
import android.view.View;
import android.widget.Button;
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

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sighting_dialog);

        TextView species = (TextView) findViewById(R.id.sighting_species);
        TextView date = (TextView) findViewById(R.id.sighting_date);
        TextView location = (TextView) findViewById(R.id.sighting_location);
        TextView animal = (TextView) findViewById(R.id.sighting_animal);
        TextView lat = (TextView) findViewById(R.id.sighting_lat);
        TextView lng = (TextView) findViewById(R.id.sighting_lng);
        ImageView imgView = (ImageView) findViewById(R.id.imageViewSightingDialog);
        Button okBtn = (Button) findViewById(R.id.sighting_btn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent i = getIntent();
        Sighting sighting = (Sighting) i.getSerializableExtra(SIGHTING);

        assert sighting != null;
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

        String imgUri = sighting.getImgUriString();//Uri.parse(mPrefs.getString("imgFileUri", "default"));
        if ((imgUri.equals("default"))) {
            Log.d("IrishWildlife", "Received default");
        }else{
            try {
                Bitmap mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imgUri));
                imgView.setImageBitmap(mImgBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
