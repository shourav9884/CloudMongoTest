import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by nati on 7/6/16.
 */
public class FollowerTest {
    public static void main(String[] args) throws TwitterException, IOException {
        String consumerKey="yGoqCT9g0iMlzSt8IXxMsZfkV";
        String consumerSecret = "YxOc7TzXgZIZvMssLGI5HUFTYDU5mfNevE4V8Ckk9LNsJQ5FF8";

        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try{
                if(pin.length() > 0){
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                }else{
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if(401 == te.getStatusCode()){
                    System.out.println("Unable to get the access token.");
                }else{
                    te.printStackTrace();
                }
            }
        }
        if(accessToken!=null) {

            MongoClient mongo = new MongoClient();
            MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
            final MongoCollection<Document> table = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_user_lt_5000");
            MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_user_follower_list");


            FindIterable<Document> iterable = table.find(new Document());
            final ArrayList<Long> userIds = new ArrayList<Long>();

            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
                    long userId = 0;
                    if(document.get("id").getClass() == Integer.class)
                    {
                        userId = (Integer) document.get("id");
                    }
                    else if(document.get("id").getClass() == Long.class)
                    {
                        userId = ((Long) document.get("id"));
                    }
                    if(userId >0)
                        userIds.add(userId);

                }
            });
            int i=0;
            while(i<userIds.size())
            {
                long lCursor = -1;
                long userID = userIds.get(i);
                IDs folllowerIds;
                ArrayList<Long> followerList = new ArrayList<Long>();

                Document document = new Document();
                document.put("user_id",userID);

//            IDs friendsIDs = twitter.getFriendsIDs(userID, lCursor);
//            System.out.println(twitter.showUser(userID).getName());
                System.out.println("==========================");
                try {
                    do {
                        folllowerIds = twitter.friendsFollowers().getFollowersIDs(userID, lCursor,5000);
                        lCursor = folllowerIds.getNextCursor();
                        System.out.println(folllowerIds.getIDs().length);

//                    followerList.addAll();

                        for (long j : folllowerIds.getIDs()) {
                            followerList.add(j);
//                    System.out.println("follower ID #" + i);
//                    System.out.println(twitter.showUser(i).getName());
                        }
                        Thread.sleep(1000*65);
                    } while (folllowerIds.hasNext());
                    document.put("follower_list",followerList);
                    newTable.insertOne(document);
                    i++;
                    System.out.println("Count: "+i);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

        }



    }
}
