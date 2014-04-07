package fyp.samoleary.WildlifePrototype2.Sighting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.R;

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


        TextView date = (TextView) findViewById(R.id.sighting_date);
        TextView location = (TextView) findViewById(R.id.sighting_location);
        TextView animal = (TextView) findViewById(R.id.sighting_animal);
        TextView lat = (TextView) findViewById(R.id.sighting_lat);
        TextView lng = (TextView) findViewById(R.id.sighting_lng);
        TextView observer = (TextView) findViewById(R.id.sighting_observer);
        WebView webView = (WebView) findViewById(R.id.webView);
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
        setTitle(sighting.getSpecies());
        date.setText(sighting.getDate());
        location.setText(sighting.getLocation());
        animal.setText(Integer.toString(sighting.getAnimals()));
        lat.setText(String.format("%.5f", sighting.getSightingLat()));
        lng.setText(String.format("%.5f", sighting.getSightingLong()));
        observer.setText(sighting.getName());

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();

        String imgUri = sighting.getImgUrlString();//Uri.parse(mPrefs.getString("imgFileUri", "default"));
        Log.d(LocationUtils.APPTAG, "SightingDialog: imgUrl: " + imgUri);
        if(!imgUri.equals("image")){
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.setInitialScale(25);
            webView.getSettings().setJavaScriptEnabled(true);
            //webView.getSettings().setLoadWithOverviewMode(true);
            //webView.getSettings().setUseWideViewPort(true);
            webView.setWebViewClient(new DisPlayWebPageActivityClient());
            webView.loadUrl("http://i.imgur.com/" + imgUri + ".jpg");
        }

        /*if ((imgUri.equals("default"))) {webview.setWebViewClient(new DisPlayWebPageActivityClient());
            Log.d("IrishWildlife", "Received default");
        }else{
            try {
                Bitmap mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imgUri));
                imgView.setImageBitmap(mImgBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private class DisPlayWebPageActivityClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        /**
         * Notify the host application that a page has started loading. This method
         * is called once for each main frame load so a page with iframes or
         * framesets will call onPageStarted one time for the main frame. This also
         * means that onPageStarted will not be called when the contents of an
         * embedded frame changes, i.e. clicking a link whose target is an iframe.
         *
         * @param view The WebView that is initiating the callback.
         * @param url The url to be loaded.
         * @param favicon The favicon for this page if it already exists in the
         *            database.
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            setProgressBarIndeterminateVisibility(true);
        }

        /**
         * Notify the host application that a page has finished loading. This method
         * is called only for main frame. When onPageFinished() is called, the
         * rendering picture may not be updated yet. To get the notification for the
         * new Picture, use {@link WebView.PictureListener#onNewPicture}.
         *
         * @param view The WebView that is initiating the callback.
         * @param url The url of the page.
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            setProgressBarIndeterminateVisibility(false);
        }

    }

}
