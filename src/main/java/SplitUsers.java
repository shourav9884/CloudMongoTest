import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by nati on 8/6/16.
 */
public class SplitUsers {
    private static int count = 1;
    private static ArrayList<Document> userList = new ArrayList<Document>();
    public static void main(String[] args) {
        split(50);
//        JsonArray array = readFromFile("");
//        System.out.println(array.size());
    }

    public static void split(final int size)
    {
        MongoClient mongo = new MongoClient();
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        final MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_only_1000");

        FindIterable<Document> iterable = newTable.find(new Document());



        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                Document userDocument = document;
                if(count%size==0)
                {
                    Gson gson = new GsonBuilder().create();
                    String json = gson.toJson(userList).toString();
                    String fileName = "users_"+(count-size)+"_"+count+".json";
                    saveToFile(json,fileName);
                    userList = new ArrayList<Document>();
                }

                userList.add(userDocument);
                count++;

            }
        });
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(userList).toString();
        String fileName = "users_"+(count-userList.size())+"_"+count+".json";
        saveToFile(json,fileName);

    }
    private static void saveToFile(String content,String fileName)
    {
        System.out.println(fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        System.out.println(content);

    }
    private  static JsonArray readFromFile(String fileName)
    {
        String sCurrentLine;
        BufferedReader br = null;

        try {
            String totalResult = "";
            br = new BufferedReader(new FileReader("split_user/full/users_1_1002.json"));
            while ((sCurrentLine = br.readLine()) != null) {
                totalResult+=sCurrentLine;
            }
            Gson gson = new GsonBuilder().create();
            JsonArray array = gson.fromJson(totalResult,JsonArray.class);
            return array;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

}
