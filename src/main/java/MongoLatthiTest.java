import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.*;
import twitter4j.*;
import twitter4j.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nati on 8/10/16.
 */
public class MongoLatthiTest {
    public static void main(String[] args)
    {
//        ArrayList<Status> statuses = new ArrayList<>();
        ArrayList<Status> statuses = getStatusFromMongo();


        HashMap<Long,Status> statusHashMap= new HashMap<Long, Status>();
        HashMap<Long, User>  userHashMap= new HashMap<Long, User>();

        HashMap<Long,List<Long>> followerMap = new HashMap<Long, List<Long>>();

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
//                    session.run("CREATE (a:User {name:'" + name + "',id:"+user.getId()+"})");

                }
                if(!followerMap.containsKey(user.getId()))
                {
                    followerMap.put(user.getId(),new ArrayList<>());
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

//                        System.out.println("Retweet USER");

//                        session.run("CREATE (a:User {name:'" + name + "',id:"+retweetUser.getId()+"})");
//                        String relation = "CREATE (c:User{id:"+retweetUser.getId()+"})-[r:IS_SHARED_BY{count:1}]->(d:User{id:"+user.getId()+"})";
//                        relationShips.add(relation);

                    }
                    if(!followerMap.containsKey(retweetUser.getId()))
                    {
                        followerMap.put(retweetUser.getId(),new ArrayList<>());
                    }
                    List<Long> usersFollower = followerMap.get(user.getId());
                    List<Long> retweetFollower = followerMap.get(retweetUser.getId());
                    if(!usersFollower.contains(retweetUser.getId())){
                        usersFollower.add(retweetUser.getId());
                        followerMap.put(user.getId(),usersFollower);
                    }
                    if(!retweetFollower.contains(user.getId()))
                    {
                        retweetFollower.add(user.getId());
                        followerMap.put(retweetUser.getId(),retweetFollower);
                    }

                }

            }

            if(i%1000==0) System.out.println("count:"+i);


        }

        List<Node> nodeList = parseFromJsonFile();
        for (Node node:nodeList)
        {
            if(!followerMap.containsKey(node.getUserID()))
            {
                followerMap.put(node.getUserID(),node.getNeighbours());
            }
            for(int i=0;i<node.getNeighbours().size();i++)
            {
                if(followerMap.containsKey(node.getUserID()))
                {
                    List<Long> neighbours = followerMap.get(node.getUserID());
                    if(!neighbours.contains(node.getNeighbours().get(i)))
                        neighbours.add(node.getNeighbours().get(i));
                    followerMap.put(node.getUserID(),neighbours);
                }
                if(followerMap.containsKey(node.getNeighbours().get(i)))
                {
                    List<Long> neighbours = followerMap.get(node.getNeighbours().get(i));
                    if(!neighbours.contains(node.getUserID()))
                        neighbours.add(node.getUserID());
                    followerMap.put(node.getNeighbours().get(i),neighbours);
                }
                else {
                    List<Long> ids = new ArrayList<>();
                    ids.add(node.getUserID());
                    followerMap.put(node.getNeighbours().get(i),ids);
                }

            }
        }
        System.out.println(followerMap);
        System.out.println(userHashMap.size());

        generateInputFile(followerMap);
    }

    private static ArrayList<Status> getStatusFromMongo()
    {
        ArrayList<Status> statuses = new ArrayList<Status>();
        MongoClient mongo = new MongoClient( );
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_1640");

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
//                if(jsonObject.has("inReplyToUserId"))
//                {
//                    int replyUserId = jsonObject.getInt("inReplyToUserId");
//                    if(replyUserId>0)
//                    {
//                        System.out.println(replyUserId);
//                    }
//                    jsonObject.put("in_reply_to_user_id",replyUserId);
//
//                }
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

//                System.out.println(jsonObject.toString());

                Status status = TwitterObjectFactory.createStatus(jsonObject.toString());
//                System.out.println(jsonObject.toString());
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
    private static List<Node> parseFromJsonFile()
    {
        File folder = new File("followers");

        File[] listOfFiles = folder.listFiles();

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Node node = new Node();
                String fileName = listOfFiles[i].getName().replace(".json","");

                node.setUserID(Long.parseLong(fileName));

                List<Long> neighbours = new ArrayList<>();

                org.json.simple.JSONArray jsonArray = SplitUsers.readFromFile("followers/"+listOfFiles[i].getName());
                for(int j=0;j<jsonArray.size();j++)
                {
                    FollowerEntity followerEntity = new GsonBuilder().create().fromJson(jsonArray.get(j).toString(),FollowerEntity.class);
                    if(followerEntity != null && followerEntity.firstUser != null && followerEntity.secondUser != null) {
                        if (followerEntity.firstUser.getId().equals(node.getUserID())) {
                            if (!neighbours.contains(followerEntity.secondUser.getId()))
                                neighbours.add(followerEntity.secondUser.getId());
                        } else if (followerEntity.secondUser.getId().equals(node.getUserID())) {
                            if (!neighbours.contains(followerEntity.firstUser.getId()))
                                neighbours.add(followerEntity.firstUser.getId());
                        }
                    }

                }
                node.setNeighbours(neighbours);

            }
        }
        return nodes;

    }

    private static void generateInputFile(HashMap<Long, List<Long>> followerMap)
    {
        String content = "";
        for (Long userID:followerMap.keySet())
        {
            List<Long> followers = followerMap.get(userID);
            content += "["+userID+",0,[";
            for (int i=0;i<followers.size();i++)
            {
                content+="["+followers.get(i)+",0]";
                if(i!=followers.size()-1)
                {
                    content+=",";
                }
            }
            content+="]]\n";
        }
        SplitUsers.saveToFile(content,"nati-in.txt");
        System.out.println(content);

    }
}
