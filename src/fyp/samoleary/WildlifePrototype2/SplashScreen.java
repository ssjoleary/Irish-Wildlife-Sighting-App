package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Created by ssjoleary on 07/03/14.
 */
public class SplashScreen extends Activity{
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new HttpHandler() {
                    @Override
                    public HttpUriRequest getHttpRequestMethod() {

                        return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

                    }
                    @Override
                    public void onResponse(String result) {
                        Intent i = new Intent(SplashScreen.this, GMapActivity.class);
                        i.putExtra("result", result);

                        startActivity(i);
                        finish();
                    }
                }.execute();
            }
        }, SPLASH_TIME_OUT);

    }

}
