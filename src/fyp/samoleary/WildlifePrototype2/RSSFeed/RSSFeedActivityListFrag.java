package fyp.samoleary.WildlifePrototype2.RSSFeed;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.R;

import java.util.ArrayList;
import java.util.List;

public class RSSFeedActivityListFrag extends ListFragment {

    // Progress Dialog
    private ProgressDialog pDialog;

    RSSParser rssParser = new RSSParser();

    List<RSSItem> rssItems = new ArrayList<RSSItem>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();

        Intent intent = getActivity().getIntent();
        String rssUrl = intent.getStringExtra("rssUrl");

        if(rssUrl == null){
            rssUrl = settings.getString("rssUrl", "");
        }

        editor.putString("rssUrl", rssUrl);
        editor.commit();

        new loadRSSFeed().execute(rssUrl);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent in = new Intent(v.getContext(), DisPlayWebPageActivity.class);

        // getting page url
        String page_url = ((TextView) v.findViewById(R.id.page_url)).getText().toString();

        in.putExtra("page_url", page_url);
        startActivity(in);
    }

    /**
     * Background Async Task to get RSS data from URL
     * */
    class loadRSSFeed extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
        }

        /**
         * getting Inbox JSON
         * */
        @Override
        protected String doInBackground(String... args) {
            String url = args[0];
            // list of rss items
            rssItems = rssParser.getRSSFeedItems(url);

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String args) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            RSSArrayAdapter adapter = new RSSArrayAdapter(getActivity().getLayoutInflater().getContext(), rssItems);
            setListAdapter(adapter);

        }

    }


}
