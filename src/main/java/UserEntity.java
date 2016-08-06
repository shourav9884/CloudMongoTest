import org.bson.Document;

import java.io.Serializable;

/**
 * Created by nati on 7/30/16.
 */
public class UserEntity implements Serializable {
    String screenName;
    Long id;

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document document()
    {
        Document document = new Document();
        document.put("screenName",this.screenName);
        document.put("user_id",this.id);
        return document;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "screenName='" + screenName + '\'' +
                ", id=" + id +
                '}';
    }
}
