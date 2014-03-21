package fyp.samoleary.WildlifePrototype2.RSSFeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.R;

import java.util.List;


public class RSSArrayAdapter extends ArrayAdapter<RSSItem> {
    private final Context context;
    private final List<RSSItem> rssItems;

    public RSSArrayAdapter(Context context, List<RSSItem> values) {
        super(context, R.layout.rss_item_list_row, values);
        this.context = context;
        this.rssItems = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rss_item_list_row, parent, false);

        assert rowView != null;
        TextView articleTitle = (TextView) rowView.findViewById(R.id.article_title);
        TextView articleDate = (TextView) rowView.findViewById(R.id.article_date);
        //TextView articleLink = (TextView) rowView.findViewById(R.id.article_link);
        TextView pageUrl = (TextView) rowView.findViewById(R.id.page_url);

        articleTitle.setText(rssItems.get(position).getTitle());
        articleDate.setText(rssItems.get(position).getPubdate());
        //articleLink.setText(rssItems.get(position).getLink());
        pageUrl.setText(rssItems.get(position).getLink());

        return rowView;
    }
}
