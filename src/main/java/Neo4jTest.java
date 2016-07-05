import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.neo4j.driver.v1.*;
import twitter4j.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nati on 7/4/16.
 */
public class Neo4jTest {
    public static void main(String[] args)
    {
        Neo4jTest test = new Neo4jTest();
        HashMap<Long,Status> statusHashMap= new HashMap<Long, Status>();
        HashMap<Long, User>  userHashMap= new HashMap<Long, User>();

        ArrayList<Status> statuses = test.getStatusFromMongo();
        ArrayList<String> relationShips = new ArrayList<String>();
        Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "123456" ) );
        Session session = driver.session();

        System.out.println("size: "+statuses.size());

        for (int i=0;i<statuses.size();i++)
        {
            Status status = statuses.get(i);
            if(!statusHashMap.containsKey(status.getId()))
            {
                statusHashMap.put(status.getId(),status);
            }

            User user = status.getUser();
            if(user!=null) {
                if (!userHashMap.containsKey(user.getId())) {
                    userHashMap.put(user.getId(), user);
                    String name = user.getName();
                    name = name.replace("'","");
                    name = name.replace("\\","");
                    name = name.replace("/","");
                    session.run("CREATE (a:User {name:'" + name + "',id:"+user.getId()+"})");

                }
            }
            Status retweetStatus = status.getRetweetedStatus();
            if(retweetStatus!=null)
            {
                User retweetUser = retweetStatus.getUser();
                if(retweetUser!=null) {
                    if (!userHashMap.containsKey(retweetUser.getId())) {
                        userHashMap.put(retweetUser.getId(), retweetUser);
                        String name = retweetUser.getName();
                        name = name.replace("'","");
                        name = name.replace("\\","");
                        name = name.replace("/","");

                        System.out.println("Retweet USER");

                        session.run("CREATE (a:User {name:'" + name + "',id:"+retweetUser.getId()+"})");
                        String relation = "CREATE (c:User{id:"+retweetUser.getId()+"})-[r:IS_SHARED_BY{count:1}]->(d:User{id:"+user.getId()+"})";
                        relationShips.add(relation);
                        session.run("MATCH (c:User{id:"+retweetUser.getId()+"}),(d:User{id:"+user.getId()+"}) \n" +
                                "CREATE (c)-[r:IS_SHARED_BY{count:1}]->(d) \n" +
                                "RETURN r");

                    }
                    else {
//                        StatementResult result = session.run("MATCH (c:User{id:"+retweetUser.getId()+"})-[r:IS_SHARED_BY]->(d:User{id:"+user.getId()+"}) RETURN r");
//                        boolean flag = true;
//                        while(result.hasNext())
//                        {
//                            flag = false;
//
//                        }
//                        if(flag)
//                        {
                        session.run("MATCH (c:User{id:"+retweetUser.getId()+"}),(d:User{id:"+user.getId()+"}) \n" +
                                "CREATE (c)-[r:IS_SHARED_BY{count:1}]->(d) \n" +
                                "RETURN r");
//                            session.run("CREATE (c:User{id:"+retweetUser.getId()+"})-[r:IS_SHARED_BY{count:1}]->(d:User{id:"+user.getId()+"})");
//                        }
                    }

                }

            }
            if(status.getInReplyToUserId() > 0)
            {
                if(!userHashMap.containsKey(status.getInReplyToUserId()))
                {
                    userHashMap.put(status.getInReplyToUserId(),null);
                    session.run("CREATE (a:User {id:"+status.getInReplyToUserId()+"})");

                }
                if(user.getId()!=status.getInReplyToUserId())
                {
                    session.run("MATCH (c:User{id:"+status.getInReplyToUserId()+"}),(d:User{id:"+user.getId()+"}) \n" +
                            "CREATE (d)-[r:REPLIES_ON{count:1}]->(c) \n" +
                            "RETURN r");
                }

            }
            if(i%1000==0) System.out.println("count:"+i);


        }
        session.close();
        driver.close();

        for (String relation:relationShips)
        {
            System.out.println(relation);
        }

    }
    private ArrayList<Status> getStatusFromMongo()
    {
        ArrayList<Status> statuses = new ArrayList<Status>();
        MongoClient mongo = new MongoClient( );
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        MongoCollection<Document> table = mongoDatabase.getCollection("twitt_col_jul_2");
        MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd");

        FindIterable<Document> iterable = newTable.find(new Document());
        for(Document doc:iterable)
        {
            Gson gson = new GsonBuilder().create();
//            System.out.println(doc.toJson());
            try {
                JSONObject jsonObject = new JSONObject(((Document)doc.get("data")).toJson());
                Long id = jsonObject.getJSONObject("id").getLong("$numberLong");
                jsonObject.remove("id");
                jsonObject.put("id",id);

                if(jsonObject.has("retweetedStatus")) {
                    JSONObject retweet = jsonObject.getJSONObject("retweetedStatus");
                    Long retweetID = retweet.getJSONObject("id").getLong("$numberLong");
                    retweet.remove("id");
//                    jsonObject.getJSONObject("retweetedStatus").put("id", retweet.getJSONObject("id").getLong("$numberLong"));
                    retweet.put("id", retweetID);
                    if(retweet.getJSONObject("user").get("id") instanceof JSONObject) {
                        if (retweet.getJSONObject("user").getJSONObject("id").has("$numberLong"))
                        {
                            JSONObject userJson = retweet.getJSONObject("user");
                            Long userID = userJson.getJSONObject("id").getLong("$numberLong");
                            userJson.remove("id");
                            userJson.put("id", userID);
                            retweet.put("user",userJson);
                        }
                    }
                    jsonObject.put("retweeted_status", retweet);

                }
                if(jsonObject.has("inReplyToUserId"))
                {
                    int replyUserId = jsonObject.getInt("inReplyToUserId");
                    if(replyUserId>0)
                    {
                        System.out.println(replyUserId);
                    }
                    jsonObject.put("in_reply_to_user_id",replyUserId);

                }
                if (jsonObject.getJSONObject("user").get("id") instanceof JSONObject) {
                    if (jsonObject.getJSONObject("user").getJSONObject("id").has("$numberLong")) {
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        Long userID = userJson.getJSONObject("id").getLong("$numberLong");
                        userJson.remove("id");
                        userJson.put("id", userID);
                        jsonObject.put("user", userJson);
                    }
                }
                if(jsonObject.has("userMentionEntities"))
                    jsonObject.remove("userMentionEntities");

                System.out.println(jsonObject.toString());

                Status status = TwitterObjectFactory.createStatus(jsonObject.toString());
                System.out.println(jsonObject.toString());
                statuses.add(status);
//                System.out.println("Status:"+status.getId()+", User:"+status.getUser().getName());
            } catch (TwitterException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return statuses;
    }

}
