import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.User;

import java.util.HashMap;

/**
 * Created by nati on 7/22/16.
 */
public class MongoTestUser {
    private static int count =0;
    public static void main(String[] args) {
        MongoClient mongo = new MongoClient();
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        final MongoCollection<Document> table = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_user_all");
        final MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_1000");

        final HashMap<Integer, Document> userHashMap = new HashMap<Integer, Document>();

        FindIterable<Document> iterable = table.find(new Document("followersCount", new Document("$gt",1640)));


        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                Document userDocument = document;
                int userId = 0;
                if(userDocument.get("id").getClass() == Integer.class)
                {
                    userId = (Integer) userDocument.get("id");
                }
                else if(userDocument.get("id").getClass() == Long.class)
                {
                    userId = ((Long) userDocument.get("id")).intValue();
                }
                if(!userHashMap.containsKey(userId) && userId>0)
                {
                    System.out.println(userDocument.get("id").getClass());

//                    long userID = (Long) ;

                    newTable.insertOne(userDocument);
                    userHashMap.put(userId, userDocument);
                }
//                System.out.println(userDocument);

            }
        });
        System.out.println(userHashMap.size());


    }
}
