import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nati on 7/29/16.
 */
public class HtmlParse {

    public static FollowerEntity getFromUrl(String url, UserEntity firstUser, UserEntity secondUser)
    {
        FollowerEntity followerEntity = new FollowerEntity();
        followerEntity.setFirstUser(firstUser);
        followerEntity.setSecondUser(secondUser);

        url = url+firstUser.getScreenName()+"/"+secondUser.getScreenName();
        System.out.println(url);
        boolean flag = true;

        while(true) {

            try {
                Document doc = Jsoup.connect(url).get();
                Elements newsHeadlines = doc.select(".wrapper .boxw div div");
                for (Element e : newsHeadlines) {
                    Elements users = e.select("#url a");
                    String userA = null;
                    String userB = null;
                    if (users.size() == 2) {
                        userA = users.get(0).ownText().replace("@", "");

                        userB = users.get(1).ownText().replace("@", "");
                        users.get(0).remove();
                        users.get(1).remove();
                        flag = false;
                    }

                    Elements el = e.select("#url");
                    el.remove();
//                    System.out.println(e);
                    if (userA != null && userB != null) {
                        System.out.println("A: " + userA + " B: " + userB);
                        String decision = e.html().replace("and  are ", "");
                        System.out.println("===" + decision);

                        if (firstUser.getScreenName().equals(userA) && secondUser.getScreenName().equals(userB)) {
                            if (decision.equals("mutual friends on Twitter"))
                                followerEntity.setMutual(true);
                            else if (decision.equals("is following  on Twitter"))
                                followerEntity.setFollowing(true);
                        } else if (firstUser.getScreenName().equals(userB) && secondUser.getScreenName().equals(userA)) {
                            if (decision.equals("is following  on Twitter"))
                                followerEntity.setReverseFollowing(true);
                        }
                    }
                }
                if(!flag)
                    break;

//            System.out.println(newsHeadlines.html());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return followerEntity;
    }
    public static void main(String[] args){
        HtmlParse htmlParse = new HtmlParse();
        UserEntity firstEntity = new UserEntity();
        firstEntity.setScreenName("papon001");
        UserEntity secondEntity = new UserEntity();
        secondEntity.setScreenName("mehrab_morshed");
        FollowerEntity followerEntity = htmlParse.getFromUrl("http://twopcharts.com/relationship/",firstEntity,secondEntity);
        System.out.println(followerEntity);

        MongoClient mongo = new MongoClient();
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        MongoCollection<org.bson.Document> newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_user_followers");
        org.bson.Document document = new org.bson.Document();

        document.put("user_name","papon001");
        document.put("user_id",11);

        ArrayList<org.bson.Document> documents = new ArrayList<org.bson.Document>();
        documents.add(followerEntity.getDocument());
        document.put("follower",documents);

        newTable.insertOne(document);

    }
}
