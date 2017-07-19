package Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

@ParseClassName("Relation")
public class Relation extends ParseObject {

    public User getFollowing() {
        return (User) getParseUser("following");
    }
    public void setFollowing(User value) {
        put("following", value);
    }

    public User getIsFollowed() {
        return (User) getParseUser("isFollowed");
    }
    public void setIsFollowed(User value) {
        put("isFollowed", value);
    }

}
