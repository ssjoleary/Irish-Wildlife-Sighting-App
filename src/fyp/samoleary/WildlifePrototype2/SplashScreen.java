package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.Database.Constants;import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;
import fyp.samoleary.WildlifePrototype2.GMap.HttpHandler;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSItem;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSParser;
import fyp.samoleary.WildlifePrototype2.Sighting.Sighting;
import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends Activity{

    // Progress Dialog
    private ProgressDialog pDialog;
    private WildlifeDB wildlifeDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        pDialog = new ProgressDialog(this);
        wildlifeDB = new WildlifeDB(this);

        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Checking for recent sightings");
        pDialog.show();


        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

            }
            @Override
            public void onResponse(String result) {
                getLatestJSONSighting(result);
            }
        }.execute();

        /*new RssSightingAsyncTask().execute(getString(R.string.rssfeed_sightings));*/
    }

    private void getLatestJSONSighting(String result) {
        wildlifeDB.open();
        //wildlifeDB.dropTableRssSighting();

        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);


                    int ID = c.getInt("pk");

                    if(!checkSightingExists(String.valueOf(ID))) {
                        JSONObject fields = c.getJSONObject("fields");
                        int animals = fields.getInt("animals");
                        String sub_date = fields.getString("sub_date");
                        double latitude = fields.getDouble("latitude");
                        double longitude = fields.getDouble("longitude");
                        String location = fields.getString("location");
                        String species = fields.getString("species");
                        String name = fields.getString("name");
                        String imgurl = fields.getString("imageurl");
                        Sighting sighting = new Sighting(ID, species, sub_date, latitude, longitude, location, animals, name, imgurl);
                        wildlifeDB.insertInfoRssSighting(sighting);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pDialog.dismiss();
        wildlifeDB.close();
        Intent i = new Intent(SplashScreen.this, SpeciesGuide.class);

        startActivity(i);
        finish();
    }

    private class RssSightingAsyncTask extends AsyncTask<String, Void, String> {
        RSSParser rssParser = new RSSParser();
        List<RSSItem> rssItems = new ArrayList<RSSItem>();

        @Override
        protected void onPreExecute() {
            wildlifeDB.open();
            //wildlifeDB.dropTableRssSighting();
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage("Checking for recent sightings");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            // list of rss items
            rssItems = rssParser.getRSSFeedItems(url);

            Document doc;//Jsoup.parse(rssItems.get(0).getLink());
            try {
                for(RSSItem rssItem: rssItems) {
                    doc = Jsoup.connect(rssItem.getLink())
                            .userAgent("Mozilla/5.0 Gecko/20100101 Firefox/21.0")
                            .timeout(0)
                            .get();

                    Element id = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(0);
                    Log.d(LocationUtils.APPTAG, id.ownText());

                    if(!checkSightingExists(id.ownText())){
                        Element name = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(30);
                        Log.d(LocationUtils.APPTAG, name.ownText());

                        Element species = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td a").first();
                        Element date = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(5);
                        Element lat = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(18);
                        Element lng = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(19);
                        Element location = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(3);
                        Element animals = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(7);
                        if(species == null) {
                            Element nametwo = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(29);
                            Log.d(LocationUtils.APPTAG, nametwo.ownText());

                            Element speciesText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(2);
                            Element latText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(16);
                            Element lngText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(17);

                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), speciesText.ownText(), date.ownText(), latText.ownText(), lngText.ownText(), location.ownText(), animals.ownText(), nametwo.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            Log.d(LocationUtils.APPTAG, "Sighting added: species = null");
                        } else {
                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), species.ownText(), date.ownText(), lat.ownText(), lng.ownText(), location.ownText(), animals.ownText(), name.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            Log.d(LocationUtils.APPTAG, "Sighting added: species != null");
                        }
                    } else {
                        Log.d(LocationUtils.APPTAG, "Sighting not added");
                        return null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            wildlifeDB.close();
            Intent i = new Intent(SplashScreen.this, SpeciesGuide.class);

            startActivity(i);
            finish();
        }

    }

    private boolean checkSightingExists(String id) {

        Cursor cursor = wildlifeDB.getInfoRssSighting();
        startManagingCursor(cursor);
        if(cursor.moveToFirst()){
            do {
                int existingID = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ID));
                if(id.equals(Integer.toString(existingID))){
                    return true;
                }
            } while(cursor.moveToNext());
        }
        return false;
    }

}
