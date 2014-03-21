package fyp.samoleary.WildlifePrototype2.RSSFeed;

public class RSSItem {

    // All <item> node name
    String _title;
    String _link;
    String _description;
    String _pubdate;
    String _guid;

    // constructor
    public RSSItem(){

    }

    // constructor with parameters
    public RSSItem(String title, String link, String description, String pubdate, String guid){
        this._title = title;
        this._link = link;
        this._description = description;
        this._pubdate = pubdate;
        this._guid = guid;
    }

    /**
     * All SET methods
     * */
    public void setTitle(String title){
        this._title = title;
    }

    public void setLink(String link){
        this._link = link;
    }

    public void setDescription(String description){
        this._description = description;
    }

    public void setPubdate(String pubDate){
        this._pubdate = pubDate;
    }


    public void setGuid(String guid){
        this._guid = guid;
    }

    /**
     * All GET methods
     * */
    public String getTitle(){
        return this._title;
    }

    public String getLink(){
        return this._link;
    }

    public String getDescription(){
        return this._description;
    }

    public String getPubdate(){
        return this._pubdate;
    }

    public String getGuid(){
        return this._guid;
    }
}
