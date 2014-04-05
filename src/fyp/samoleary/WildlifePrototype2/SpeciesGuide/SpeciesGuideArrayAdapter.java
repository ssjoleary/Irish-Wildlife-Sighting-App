package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.R;

/**
 * Created by ssjoleary on 17/03/2014
 */
public class SpeciesGuideArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    static class ViewHolder {
        public TextView speciesTitle;
        public TextView speciesSnippet;
        public ImageView imageView;
    }

    public SpeciesGuideArrayAdapter(Context context, String[] values) {
        super(context, R.layout.species_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.species_list_item, null);

            ViewHolder viewHolder = new ViewHolder();
            assert rowView != null;
            viewHolder.speciesTitle = (TextView) rowView.findViewById(R.id.species_title);
            viewHolder.speciesSnippet = (TextView) rowView.findViewById(R.id.species_snippet);
            viewHolder.imageView = (ImageView) rowView.findViewById(R.id.species_Img);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        /*assert convertView != null;
        TextView speciesTitle = (TextView) convertView.findViewById(R.id.species_title);
        TextView speciesSnippet = (TextView) convertView.findViewById(R.id.species_snippet);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.species_Img);*/

        String speciesTitleText = "title_"+values[position];
        int speciesTitleID = context.getResources().getIdentifier(speciesTitleText, "string", getContext().getPackageName());
        holder.speciesTitle.setText(speciesTitleID);

        String speciesSnippetText = "snippet_"+values[position];
        int speciesSnippetID = context.getResources().getIdentifier(speciesSnippetText, "string", getContext().getPackageName());
        holder.speciesSnippet.setText(speciesSnippetID);

        int imageViewID = context.getResources().getIdentifier(values[position], "drawable", getContext().getPackageName());
        holder.imageView.setImageResource(imageViewID);

        return rowView;
    }

}
