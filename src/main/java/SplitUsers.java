import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by nati on 8/6/16.
 */
public class SplitUsers {
    private static int count = 1;
    private static ArrayList<Document> userList = new ArrayList<Document>();
    public static void main(String[] args) {
//        split(20);
//        JsonArray array = readFromFile("");
//        System.out.println(array.size());
        fromFileToMongo();

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
    public static void saveToFile(String content,String fileName)
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
    public static JSONArray readFromFile(String fileName)
    {
        String sCurrentLine;
        BufferedReader br = null;

        try {
            String totalResult = "";
            br = new BufferedReader(new FileReader(fileName));
            while ((sCurrentLine = br.readLine()) != null) {
                totalResult+=sCurrentLine;
            }
            Gson gson = new GsonBuilder().create();
            JSONParser parser = new JSONParser();

            JSONArray jsonArray = (JSONArray) parser.parse(totalResult);

            return jsonArray;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;


    }

    public static void fromFileToMongo()
    {
        String fileName = "final-out.txt";

        MongoClient mongo = new MongoClient( );
        MongoDatabase mongoDatabase = mongo.getDatabase("twitt_db");
        MongoCollection newTable = mongoDatabase.getCollection("twitt_col_dhaka_bd_with_k");
        String sCurrentLine;
        BufferedReader br = null;

        String totalResult = "";
        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((sCurrentLine = br.readLine()) != null) {
                JSONParser parser = new JSONParser();
                JSONArray array = (JSONArray) parser.parse(sCurrentLine);

                Document document = new Document();
                document.put("userID",array.get(0));
                document.put("kValue",array.get(1));
                newTable.insertOne(document);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


}
