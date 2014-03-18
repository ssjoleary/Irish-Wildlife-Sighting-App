package fyp.samoleary.WildlifePrototype2;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    private ImageView imgView;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static Uri fileUri;

    private WildlifeDB wildlifeDB;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
    private String imgUri;

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

        imgView = (ImageView) findViewById(R.id.imageView);
        inclPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goGetPhoto();
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
        assert user_coordinates != null;
        lat_view.setText(String.format("%.5f", user_coordinates.latitude));
        long_view.setText(String.format("%.5f", user_coordinates.longitude));

        wildlifeDB = new WildlifeDB(this);
        wildlifeDB.open();

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();
    }

    private void goGetPhoto() {
        // Create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        mEditor.putString("imgFileUri", fileUri.toString());
        mEditor.commit();
        Log.d("Irish", fileUri.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // Set the image file name

        // Start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
        return new Sighting(species, dateOut, sightingLat, sightingLong, location, animals, imgUri);
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

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "IrishWildlifePhoto");
        Log.d("IrishWildlifePhoto", mediaStorageDir.getAbsolutePath());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("IrishWildlifePhoto", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        Log.d("IrishWildlife", mediaFile.getName());
        return mediaFile;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Image captured and saved to fileUri specified in the Intent
                    Toast.makeText(SubmitActivity.this, "Image saved to:\n" + data.getExtras().get("data"), Toast.LENGTH_LONG).show();
                    Bundle extras = data.getExtras();
                    Bitmap mImgBitmap = (Bitmap) extras.get("data");
                    imgView.setImageBitmap(mImgBitmap);
                } else {
                    Log.d("IrishWildlife", "Data is null: " + mPrefs.getString("imgFileUri", null));
                    imgUri = mPrefs.getString("imgFileUri", "default");
                    try {
                        Bitmap mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imgUri));
                        imgView.setImageBitmap(mImgBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(SubmitActivity.this, "Cancelled Image Capture", Toast.LENGTH_SHORT).show();
            } else {
                // Image capture failed, advise user
                Toast.makeText(SubmitActivity.this, "Failed Image Capture", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }
}
