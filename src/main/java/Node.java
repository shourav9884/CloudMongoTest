import java.util.List;

/**
 * Created by nati on 8/10/16.
 */
public class Node {
    private long userID;
    private List<Long> neighbours;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public List<Long> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<Long> neighbours) {
        this.neighbours = neighbours;
    }
}
