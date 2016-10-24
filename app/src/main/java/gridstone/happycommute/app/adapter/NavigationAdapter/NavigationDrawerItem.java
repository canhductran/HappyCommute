package gridstone.happycommute.app.adapter.NavigationAdapter;

/**
 * Created by Matt on 23/9/2014.
 */
public class NavigationDrawerItem
{

    private String optionName; //The string that will appear as the item entry title (i.e "settings")
    private int optionIconResourceID;   //An icon relating to the option

    public NavigationDrawerItem(String optionName, int optionIconResourceID)
    {
        this.setOptionIconResourceID(optionIconResourceID);
        this.setOptionName(optionName);
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public int getOptionIconResourceID() {
        return optionIconResourceID;
    }

    public void setOptionIconResourceID(int optionIconResourceID) {
        this.optionIconResourceID = optionIconResourceID;
    }
}
