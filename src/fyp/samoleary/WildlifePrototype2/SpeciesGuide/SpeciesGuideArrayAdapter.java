package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.R;

/**
 * Created by ssjoleary on 17/03/2014
 */
public class SpeciesGuideArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public SpeciesGuideArrayAdapter(Context context, String[] values) {
        super(context, R.layout.species_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.species_list_item, parent, false);

        assert rowView != null;
        TextView speciesTitle = (TextView) rowView.findViewById(R.id.species_title);
        TextView speciesSnippet = (TextView) rowView.findViewById(R.id.species_snippet);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.species_Img);

        String speciesTitleText = "title_"+values[position];
        int speciesTitleID = context.getResources().getIdentifier(speciesTitleText, "string", getContext().getPackageName());
        speciesTitle.setText(speciesTitleID);

        String speciesSnippetText = "snippet_"+values[position];
        int speciesSnippetID = context.getResources().getIdentifier(speciesSnippetText, "string", getContext().getPackageName());
        speciesSnippet.setText(speciesSnippetID);

        int imageViewID = context.getResources().getIdentifier(values[position], "drawable", getContext().getPackageName());
        imageView.setImageResource(imageViewID);

        return rowView;
    }

}
