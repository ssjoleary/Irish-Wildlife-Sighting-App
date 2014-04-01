package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import fyp.samoleary.WildlifePrototype2.GMap.HttpHandler;
import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

public class SplashScreen extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
                new HttpHandler() {
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
                }.execute();
    }

}
