import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;

/**
 * Created by nati on 7/30/16.
 */
public class MongoFindFollowers {
    public static void main(String[] args) {
        MongoClient mongo = new MongoClient();
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        final MongoCollection<Document> table = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_user_all");
        MongoCollection<Document> newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_user_followers");
        FindIterable<Document> iterable = table.find(new Document());
        final ArrayList<UserEntity> userNames = new ArrayList<UserEntity>();


        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                UserEntity userEntity = new UserEntity();
                userEntity.setScreenName((String)document.get("screenName"));
                int userId = 0;
                if(document.get("id").getClass() == Integer.class)
                {
                    userId = (Integer) document.get("id");
                }
                else if(document.get("id").getClass() == Long.class)
                {
                    userId = ((Long) document.get("id")).intValue();
                }
                userEntity.setId((long)userId);

                System.out.println(userEntity.toString());
                userNames.add(userEntity);

            }
        });

        for(int i=0;i<userNames.size();i++)
        {
            Document document = new Document();
            Gson gson = new GsonBuilder().create();

            document.put("user_name",userNames.get(i).getScreenName());
            document.put("user_id",userNames.get(i).getId());

            UserEntity firstUser = new UserEntity();
            firstUser.setId(userNames.get(i).getId());
            firstUser.setScreenName(userNames.get(i).getScreenName());
            ArrayList<Document> followerEntities = new ArrayList<Document>();

            for (int j=0;j<userNames.size();j++)
            {
                if(!userNames.get(i).getScreenName().equals(userNames.get(j).getScreenName()))
                {
                    UserEntity secondUser = new UserEntity();
                    secondUser.setId(userNames.get(j).getId());
                    secondUser.setScreenName(userNames.get(j).getScreenName());

                    FollowerEntity followerEntity = HtmlParse.getFromUrl("http://twopcharts.com/relationship/",firstUser,secondUser);
                    System.out.println(followerEntity);
                    if(followerEntity.getReverseFollowing() != null && followerEntity.getReverseFollowing() == true)
                    {
                        followerEntities.add(followerEntity.getDocument());
                    }

                    System.out.println("Count: "+(j+1));

                }
            }

            if(followerEntities != null && !followerEntities.isEmpty()) {
                document.put("followers",followerEntities);
                newTable.insertOne(document);
            }
        }
        System.out.println(userNames.size());
    }
}
