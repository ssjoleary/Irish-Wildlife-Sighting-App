package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSItem;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSParser;
import fyp.samoleary.WildlifePrototype2.Sighting.Sighting;
import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends Activity{
    RSSParser rssParser = new RSSParser();
    List<RSSItem> rssItems = new ArrayList<RSSItem>();
    // Progress Dialog
    private ProgressDialog pDialog;
    private WildlifeDB wildlifeDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
                /*new HttpHandler() {
                    @Override
                    public HttpUriRequest getHttpRequestMethod() {
                        return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

                    }
                    @Override
                    public void onResponse(String result) {
                        Intent i = new Intent(SplashScreen.this, SpeciesGuide.class);
                        i.putExtra("result", result);

                        startActivity(i);
                        finish();
                    }
                }.execute();*/
        pDialog = new ProgressDialog(this);
        wildlifeDB = new WildlifeDB(this);
        new RssSightingAsyncTask().execute(getString(R.string.rssfeed_sightings));
    }

    private class RssSightingAsyncTask extends AsyncTask<String, Void, String> {

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

                    if(!checkSightingExists(id.ownText())){
                        Element species = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td a").first();
                        Element date = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(5);
                        Element lat = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(18);
                        Element lng = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(19);
                        Element location = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(3);
                        Element animals = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(7);
                        if(species == null) {
                            Element speciesText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(2);
                            Element latText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(16);
                            Element lngText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(17);

                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), speciesText.ownText(), date.ownText(), latText.ownText(), lngText.ownText(), location.ownText(), animals.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            Log.d(LocationUtils.APPTAG, "Shouldn't be here!");
                        } else {
                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), species.ownText(), date.ownText(), lat.ownText(), lng.ownText(), location.ownText(), animals.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            Log.d(LocationUtils.APPTAG, "Shouldn't be here!");
                        }
                    } else {
                        Log.d(LocationUtils.APPTAG, "Should be here!");
                        return null;
                    }

                    /*for(Element element: elements){
                        Log.d(LocationUtils.APPTAG, element.ownText());
                    }*/
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

}
