

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
//import twitter4j.*;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by nati on 6/11/16.
 */
public class Main {
    public static void main(String[] args) throws TwitterException, IOException {

        String consumerKey="yGoqCT9g0iMlzSt8IXxMsZfkV";
        String consumerSecret = "YxOc7TzXgZIZvMssLGI5HUFTYDU5mfNevE4V8Ckk9LNsJQ5FF8";

        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db_test");
        final MongoCollection table = mongoDatabase.getCollection("twitt_col_jul_3");


        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

//        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

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
        if(accessToken!=null)
        {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("yGoqCT9g0iMlzSt8IXxMsZfkV")
                    .setOAuthConsumerSecret("YxOc7TzXgZIZvMssLGI5HUFTYDU5mfNevE4V8Ckk9LNsJQ5FF8")
                    .setOAuthAccessToken(accessToken.getToken())
                    .setOAuthAccessTokenSecret(accessToken.getTokenSecret());
            StatusListener listener = new StatusListener(){
                public void onStatus(Status status) {
                    Gson gson = new GsonBuilder().create();
                    String string = gson.toJson(status);
//                    String userStr = gson.toJson(status.getUser());
//                    String locStr = gson.toJson(status.getGeoLocation());
//                    String reTweetStr = gson.toJson(status.getRetweetedStatus());


                    DBObject dbObject = (DBObject) JSON.parse(string);
//                    DBObject userObj = (DBObject)JSON.parse(userStr);
//                    DBObject locObj = (DBObject) JSON.parse(locStr);
//                    DBObject reTweetObj=(DBObject) JSON.parse(reTweetStr);
//                    jsonObject.
                    Document document = new Document();
                    document.put("data",dbObject);
//                    document.put("user_data",userObj);
//                    document.put("location",locObj);
//                    document.put("retweet",reTweetObj);

                    table.insertOne(document);


//                    document.put("raw_data",status);


                    System.out.println(status.getUser().getName() + " : " + status.getText());


                }
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

                public void onScrubGeo(long l, long l1) {

                }

                public void onStallWarning(StallWarning stallWarning) {

                }

                public void onException(Exception ex) {
                    ex.printStackTrace();
                }
            };
            TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
            twitterStream.addListener(listener);

            // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
            twitterStream.sample();
        }





    }
    private static void insertInMongo(Status status)
    {


//        document.put("data", "mkyong");
//        document.put("age", 30);
//        document.put("createdDate", new Date());

    }
}
