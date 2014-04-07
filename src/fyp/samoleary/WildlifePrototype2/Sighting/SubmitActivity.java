package fyp.samoleary.WildlifePrototype2.Sighting;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.model.LatLng;
import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;
import fyp.samoleary.WildlifePrototype2.GMap.GMapActivity;
import fyp.samoleary.WildlifePrototype2.GMap.HttpHandler;
import fyp.samoleary.WildlifePrototype2.GetConnectivityStatus;
import fyp.samoleary.WildlifePrototype2.ImgurUploadTask;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
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
    private String mImageUri;
    private int nextID = -1;

    private static Context context;
    private MyImgurUploadTask mImgurUploadTask;
    private String mImgurUrl;

    private GetConnectivityStatus isConnected;

    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.submit_sighting);

        SubmitActivity.context = getApplicationContext();
        isConnected = new GetConnectivityStatus();

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

        submit_btn.setEnabled(false);
        getRecentSightings();
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(location_view.getText().toString().equals("") || animals_view.getText().toString().equals("")) {
                    Toast.makeText(getBaseContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
                } else if(isConnected.isConnected(context)) {
                    if(nextID != -1) {
                        Log.d(LocationUtils.APPTAG, "SubmitActivity: onClick: success");
                        if (mImageUri != null && mImgurUrl == null) {
                            Log.d(LocationUtils.APPTAG, "SubmitActivity: onClick: we're in");
                            new MyImgurUploadTask(Uri.parse(mImageUri)).execute();
                        } else {
                            submitSighting();
                        }
                        submit_btn.setText("Loading...");
                        submit_btn.setEnabled(false);
                    }
                } else {
                    Log.d(LocationUtils.APPTAG, "SubmitActivity: onClick: failure: nextID: " + nextID);
                    Toast.makeText(getBaseContext(), "Cannot report a sighting at this time! Check Connectivity.", Toast.LENGTH_LONG).show();
                }
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

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();
    }

    @Override
    public void onStop(){
        super.onStop();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
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

        wildlifeDB.open();
        wildlifeDB.insertInfoRssSighting(userSighting);
        wildlifeDB.close();

        //TODO send sighting to remote database
        Log.d(LocationUtils.APPTAG, "SubmitActivity: submitSighting: execute");
        new HttpAsyncTask().execute("http://fyp-irish-wildlife.herokuapp.com/sightings/postsighting/");

        Intent intent = new Intent(this, GMapActivity.class);
        setResult(RESULT_OK, intent);
        //intent.putExtra("userSighting", userSighting);
        finish();
    }

    public static String POST(String url, Sighting sightingSubmit){
        InputStream inputStream;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json;

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("species", sightingSubmit.getSpecies());
            jsonObject.accumulate("date", sightingSubmit.getDate());
            jsonObject.accumulate("lat", sightingSubmit.getSightingLat());
            jsonObject.accumulate("lng", sightingSubmit.getSightingLong());
            jsonObject.accumulate("location", sightingSubmit.getLocation());
            jsonObject.accumulate("animals", sightingSubmit.getAnimals());
            jsonObject.accumulate("name", sightingSubmit.getName());
            jsonObject.accumulate("imageurl", sightingSubmit.getImgUrlString());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();
            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(searchActivity);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                Log.d(LocationUtils.APPTAG, "SubmitActivity: POST: inputStream != null");
                result = convertInputStreamToString(inputStream);
            }
            else {
                Log.d(LocationUtils.APPTAG, "SubmitActivity: POST: inputStream = null: did not work");
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.d(LocationUtils.APPTAG, "SubmitActivity: HttpAsyncTask: doInBackground");
            return POST(urls[0], userSighting);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            showSuccess(result);
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        inputStream.close();
        Log.d(LocationUtils.APPTAG, "SubmitActivity: convertInputStreamToString: result.To.String(): " + result.toString());

        return result.toString();

    }

    private Sighting createSighting() {
        double sightingLat, sightingLong;
        String location;
        int animals;

        //int nextID = mPrefs.getInt("ID", 0);
        String name = mPrefs.getString("name", "Click to set up Profile");
        String phone = mPrefs.getString("phone", "Phone");
        String email = mPrefs.getString("email", "Email");
        Boolean isMember = mPrefs.getBoolean("isMember", false);

        String dateOut = date.toString();
        sightingLat = user_coordinates.latitude;
        sightingLong = user_coordinates.longitude;

        location = location_view.getText().toString();
        location += ", " + county;

        animals = Integer.parseInt(animals_view.getText().toString());
        if (mImgurUrl == null)
            mImgurUrl = "image";

        return new Sighting(nextID, species, dateOut, sightingLat, sightingLong, location, animals, name, mImgurUrl);
    }

    private void getRecentSightings() {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                setProgressBarIndeterminateVisibility(true);

                return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

            }
            @Override
            public void onResponse(String result) {
                getNextID(result);
                submit_btn.setEnabled(true);
                setProgressBarIndeterminateVisibility(false);
            }
        }.execute();
    }

    private void getNextID(String result) {
        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);

                    int ID = c.getInt("pk");
                    if(ID >= nextID){
                        nextID = ID + 1;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LocationUtils.APPTAG, "SubmitActivity: nextID: " + nextID);
    }

    private void showSuccess(String result) {
        //generateNoteOnSD("ErrorHtml", result);
        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);

                    String success = c.getString("success");
                    Log.d(LocationUtils.APPTAG, "isSuccessful: " + success);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    mImageUri = mPrefs.getString("imgFileUri", "default");
                    try {
                        Bitmap mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mImageUri));
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

    public static Context getAppContext() {
        return SubmitActivity.context;
    }

    private class MyImgurUploadTask extends ImgurUploadTask {
        public MyImgurUploadTask(Uri imageUri) {
            super(imageUri, getContentResolver());
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mImgurUploadTask != null) {
                boolean cancelled = mImgurUploadTask.cancel(false);
                if (!cancelled)
                    this.cancel(true);
            }
            mImgurUploadTask = this;
            mImgurUrl = null;
            //getView().findViewById(R.id.choose_image_button).setEnabled(false);
            //setImgurUploadStatus(R.string.choose_image_upload_status_uploading);
        }
        @Override
        protected void onPostExecute(String imageId) {
            super.onPostExecute(imageId);
            mImgurUploadTask = null;
            if (imageId != null) {
                mImgurUrl = "" + imageId;
                Log.d(LocationUtils.APPTAG, "imgur upload success: " + mImgurUrl);
                //setImgurUploadStatus(R.string.choose_image_upload_status_success);
                //if (isResumed()) {
                //    getView().findViewById(R.id.imgur_link_layout).setVisibility(View.VISIBLE);
                //    ((TextView) getView().findViewById(R.id.link_url)).setText(mImgurUrl);
                //}
            } else {
                mImgurUrl = null;
                Log.d(LocationUtils.APPTAG, "imgur upload error");
                //setImgurUploadStatus(R.string.choose_image_upload_status_failure);
                /*if (isResumed()) {
                    getView().findViewById(R.id.imgur_link_layout).setVisibility(View.GONE);
                    if (isVisible()) {
                        ((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(null);
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();
                    }
                }*/
            }
            submitSighting();
            /*if (isVisible())
                getView().findViewById(R.id.choose_image_button).setEnabled(true);*/
        }
    }

    /**public void generateNoteOnSD(String sFileName, String sBody){
     try
     {
     File root = new File(Environment.getExternalStorageDirectory(), "Documents");
     if (!root.exists()) {
     root.mkdirs();
     }
     File gpxfile = new File(root, sFileName);
     FileWriter writer = new FileWriter(gpxfile);
     writer.append(sBody);
     writer.flush();
     writer.close();
     Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
     }
     catch(IOException e)
     {
     e.printStackTrace();
     }
     }*/
}
