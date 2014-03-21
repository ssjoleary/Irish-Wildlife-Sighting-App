package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.R;

/**
 * Created by ssjoleary on 17/03/2014.
 */
public class SpeciesGuideDialog extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.species_list_detail);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", -1);
        String[] values = getResources().getStringArray(R.array.species_array_id);

        TextView speciesTitle = (TextView) findViewById(R.id.species_detail_title);
        ImageView speciesImage = (ImageView) findViewById(R.id.species_detail_image);
        TextView speciesClassification = (TextView) findViewById(R.id.species_detail_classification);

        VideoFragment f = VideoFragment.newInstance("oCwq9oPOfNk");
        getSupportFragmentManager().beginTransaction().replace(R.id.species_detail_videoFragment, f).commit();

        String speciesTitleText = "title_"+values[position];
        int speciesTitleID = this.getResources().getIdentifier(speciesTitleText, "string", this.getPackageName());
        speciesTitle.setText(speciesTitleID);

        String speciesSnippetText = "classification_"+values[position];
        int speciesClassificationID = this.getResources().getIdentifier(speciesSnippetText, "string", this.getPackageName());
        speciesClassification.setText(speciesClassificationID);

        int imageViewID = this.getResources().getIdentifier(values[position], "drawable", this.getPackageName());
        speciesImage.setImageResource(imageViewID);

        Button okBtn = (Button) findViewById(R.id.species_dialog_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
