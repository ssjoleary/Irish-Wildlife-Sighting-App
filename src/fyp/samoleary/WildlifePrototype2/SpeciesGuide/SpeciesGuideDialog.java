package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.content.Intent;
import android.content.res.Resources;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.R;

import java.util.Locale;

/**
 * Created by ssjoleary on 17/03/2014.
 */
public class SpeciesGuideDialog extends FragmentActivity implements TextToSpeech.OnInitListener {
    private TextView speciesClassification;
    //private TextView speciesTitle;
    private TextToSpeech tts;
    private int position;
    private String[] values;
    private Button latinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.species_list_detail);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        values = getResources().getStringArray(R.array.species_array_id);

        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(1);
        tts.setPitch(1);

        //speciesTitle = (TextView) findViewById(R.id.species_detail_title);
        ImageView speciesImage = (ImageView) findViewById(R.id.species_detail_image);
        speciesClassification = (TextView) findViewById(R.id.species_detail_classification);

        VideoFragment f = VideoFragment.newInstance("oCwq9oPOfNk");
        getSupportFragmentManager().beginTransaction().replace(R.id.species_detail_videoFragment, f).commit();

        String speciesTitleText = "title_"+values[position];
        int speciesTitleID = this.getResources().getIdentifier(speciesTitleText, "string", this.getPackageName());
        setTitle(speciesTitleID);

        String speciesSnippetText = "classification_"+values[position];
        int speciesClassificationID = this.getResources().getIdentifier(speciesSnippetText, "string", this.getPackageName());
        speciesClassification.setText(speciesClassificationID);

        latinBtn = (Button) findViewById(R.id.guide_latin);
        latinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });

        int imageViewID = this.getResources().getIdentifier(values[position], "drawable", this.getPackageName());
        speciesImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), imageViewID, 50, 50));

        Button okBtn = (Button) findViewById(R.id.species_dialog_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void speakOut() {
        String text = "latin_"+values[position];
        int textID = this.getResources().getIdentifier(text, "string", this.getPackageName());
        String latin = getResources().getString(textID);
        tts.speak(latin, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.ENGLISH);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                latinBtn.setEnabled(true);
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
