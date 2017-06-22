package Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

@ParseClassName("Relation")
public class Relation extends ParseObject {

    public User getTrainer() {
        return (User) getParseUser("trainer");
    }
    public void setTrainer(User value) {
        put("trainer", value);
    }

    public User getClient() {
        return (User) getParseUser("client");
    }
    public void setClient(User value) {
        put("client", value);
    }

}
