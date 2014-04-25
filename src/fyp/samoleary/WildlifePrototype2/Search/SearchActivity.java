package fyp.samoleary.WildlifePrototype2.Search;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import fyp.samoleary.WildlifePrototype2.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    private Button cancel;
    private Button search;
    private SearchParams searchParams;

    public void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search_sightings);
        init();
    }
    public void init(){
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

        cancel = (Button) findViewById(R.id.search_cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(11, returnIntent);
                SearchActivity.this.finish();
            }
        });
        search = (Button) findViewById(R.id.search_search_btn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                //new HttpAsyncTask().execute("http://fyp-irish-wildlife.herokuapp.com/sightings/getspecificsighting/");
                if (countySpinner.getItemAtPosition(countyPos).toString().equals("Any...")) {
                    if (speciesSpinner.getItemAtPosition(speciesPos).toString().equals("Any...")) {
                        setResult(10, returnIntent);
                        finish();
                    } else {
                        returnIntent.putExtra("species", speciesSpinner.getItemAtPosition(speciesPos).toString());
                        setResult(12, returnIntent);
                        finish();
                    }
                } else {
                    if (speciesSpinner.getItemAtPosition(speciesPos).toString().equals("Any...")) {
                        returnIntent.putExtra("county", countySpinner.getItemAtPosition(countyPos).toString());
                        setResult(13, returnIntent);
                        finish();
                    } else {
                        returnIntent.putExtra("county", countySpinner.getItemAtPosition(countyPos).toString());
                        returnIntent.putExtra("species", speciesSpinner.getItemAtPosition(speciesPos).toString());
                        setResult(14, returnIntent);
                        finish();
                    }
                }

            }

        });
    }
    public static String POST(String url, SearchParams searchActivity){
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
            jsonObject.accumulate("species", searchActivity.getSpecies());
            jsonObject.accumulate("county", searchActivity.getCounty());

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
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

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

            searchParams = new SearchParams();
            searchParams.setCounty(countySpinner.getItemAtPosition(countyPos).toString());
            searchParams.setSpecies(speciesSpinner.getItemAtPosition(speciesPos).toString());
            return POST(urls[0],searchParams);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            validate(result);
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private void validate(String result) {
        Intent returnIntent = new Intent();
        if (result.equals("[]")) {
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
        else {
            returnIntent.putExtra("result", result);
            setResult(RESULT_OK, returnIntent);
            finish();
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
        return result.toString();

    }

}

