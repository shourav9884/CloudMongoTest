import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by nati on 7/22/16.
 */
public class MongoTestEuro {
    private static int count = 0;

    public static void main(String[] args) {
        MongoClient mongo = new MongoClient();
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        MongoCollection<Document> table = mongoDatabase.getCollection("twitt_col");
        MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd");


        //        System.out.println(table.count());
        ArrayList<Long> tweetIds = new ArrayList<Long>();
        HashMap<Long, Document> statusMap = new HashMap<Long, Document>();


        Pattern regex = Pattern.compile("Orlando"); // should be m in your case
//        Pattern bdRegex = Pattern.compile("Bangladesh");
        BasicDBObject queryTag = new BasicDBObject("data.hashtagEntities.text", regex);
        BasicDBObject queryText = new BasicDBObject("data.text", regex);
//    BasicDBObject queryTagBD = new BasicDBObject("data.hashtagEntities.text", bdRegex);
//    BasicDBObject queryTextBD = new BasicDBObject("data.text", bdRegex);

        BasicDBList or = new BasicDBList();
        or.add(queryTag);
        or.add(queryText);
//    or.add(queryTagBD);
//    or.add(queryTextBD);
        BasicDBObject query = new BasicDBObject("$or", or);

        FindIterable<Document> iterable = table.find(new Document(query));

        for (Document doc : iterable) {
//            newTable.insertOne(doc);
//            Long id = ((Document) doc.get("data")).getLong("id");
//            Document mainDoc = ((Document) doc.get("data"));


//            if (!statusMap.containsKey(id)) statusMap.put(id, mainDoc);
//            statusMap.put(id,true);
            tweetIds.add(((Document) doc.get("data")).getLong("id"));
//            if (((Document) doc.get("data")).containsKey("retweetedStatus")) {
//                Long retweetId = ((Document) ((Document) doc.get("data")).get("retweetedStatus")).getLong("id");
//                Document retweetDoc = (Document) ((Document) doc.get("data")).get("retweetedStatus");
//                retweetDoc.put("nati_retweet", 1);
//                System.out.println("Retweet found");
//                if (!statusMap.containsKey(retweetId)) statusMap.put(retweetId, retweetDoc);
////                statusMap.put(retweetId,true);
//                System.out.println(((Document) doc.get("data")).get("retweetedStatus"));
//            }
//            if(count>40) break;
            System.out.println(count);
            count++;
        }
        System.out.println("Final count:====== "+count);
//    for (Document document:statusMap.values())
//    {
//        Document newDocument = new Document();
//        newDocument.put("data",document);
//        if(document.containsKey("nati_retweet")) System.out.println(document.get("nati_retweet"));
//        newTable.insertOne(newDocument);
//
//    }

//        BasicDBObject obj = new BasicDBObject ("data.retweetedStatus.id", new BasicDBObject("$in", tweetIds));
//        query = new BasicDBObject();
//        Pattern regexPattern = Pattern.compile("wales");
//        query.put("data.retweetedStatus.hashtagEntities.text", regexPattern);
//        iterable = table.find(new Document(obj));
//
//        for(Document doc:iterable)
//        {
//            Long newID = ((Document)doc.get("data")).getLong("id");
//            System.out.println("New ID: "+newID);
//            System.out.println(doc);
//            System.out.println("new "+count);
//            count++;
//        }

        //In query chalabo

//    System.out.println(statusMap.size());
//    System.out.println(tweetIds);

    }
}