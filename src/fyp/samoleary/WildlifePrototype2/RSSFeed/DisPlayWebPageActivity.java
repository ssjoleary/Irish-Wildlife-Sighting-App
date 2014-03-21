package fyp.samoleary.WildlifePrototype2.RSSFeed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import fyp.samoleary.WildlifePrototype2.R;

public class DisPlayWebPageActivity extends Activity {

    WebView webview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.webview);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent in = getIntent();
        String page_url = in.getStringExtra("page_url");

        webview = (WebView) findViewById(R.id.webpage);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(page_url);

        webview.setWebViewClient(new DisPlayWebPageActivityClient());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
