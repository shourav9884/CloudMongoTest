import org.bson.Document;

import java.io.Serializable;

/**
 * Created by nati on 7/29/16.
 */
public class FollowerEntity implements Serializable {

    Boolean isMutual;
    Boolean isFollowing;
    Boolean isReverseFollowing;
    UserEntity firstUser;
    UserEntity secondUser;



    public Boolean getMutual() {
        return isMutual;
    }

    public void setMutual(Boolean mutual) {
        isMutual = mutual;
    }

    public Boolean getFollowing() {
        return isFollowing;
    }

    public void setFollowing(Boolean following) {
        isFollowing = following;
    }

    public Boolean getReverseFollowing() {
        return isReverseFollowing;
    }

    public void setReverseFollowing(Boolean reverseFollowing) {
        isReverseFollowing = reverseFollowing;
    }

    public UserEntity getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(UserEntity firstUser) {
        this.firstUser = firstUser;
    }

    public UserEntity getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(UserEntity secondUser) {
        this.secondUser = secondUser;
    }
    public Document getDocument()
    {
        Document document  = new Document();
        document.put("isMutual",this.isMutual);
        document.put("isFollowing",this.isFollowing);
        document.put("isReverseFollowing",this.isReverseFollowing);
        document.put("firstUser",this.firstUser.document());
        document.put("secondUser",this.secondUser.document());
        return document;
    }



    @Override
    public String toString() {
        return "FollowerEntity{" +
                "isMutual=" + isMutual +
                ", isFollowing=" + isFollowing +
                ", isReverseFollowing=" + isReverseFollowing +
                ", firstUser=" + firstUser +
                ", secondUser=" + secondUser +
                '}';
    }
}
