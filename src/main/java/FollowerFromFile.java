import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by nati on 8/6/16.
 */
public class FollowerFromFile {
    public static void main(String[] args)
    {
        int size=20;
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter start: ");
        int start = reader.nextInt();
        System.out.println("Enter end: ");
        int end=reader.nextInt();

        final String BASE_PATH="split_user/20/";
        final String ALL_PATH="split_user/full/users_1_1002.json";

        ArrayList<String> fileNames = new ArrayList<String>();

        ArrayList<UserEntity> targetUsers=new ArrayList<UserEntity>();
        ArrayList<UserEntity> allUsers=new ArrayList<UserEntity>();

        for (int i=start; i< end;i++)
        {
            String fileName="users_"+(i*size)+"_"+((i+1)*size)+".json";
            fileNames.add(fileName);

        }
        for(int i=0;i<fileNames.size();i++)
        {
            System.out.println(BASE_PATH+fileNames.get(i));
            JSONArray userArray = SplitUsers.readFromFile(BASE_PATH+fileNames.get(i));

            for (int j=0;j<userArray.size();j++)
            {
                UserEntity userEntity = new UserEntity();
                JSONObject jsonObject = null;

                jsonObject = (JSONObject) userArray.get(i);
                userEntity.setScreenName((String) jsonObject.get("screenName"));
                int userId = 0;
                if(jsonObject.get("id").getClass() == Integer.class)
                {
                    userId = (Integer) jsonObject.get("id");
                }
                else if(jsonObject.get("id").getClass() == Long.class)
                {
                    userId = ((Long) jsonObject.get("id")).intValue();
                }
                userEntity.setId((long)userId);

                targetUsers.add(userEntity);



            }
        }

        JSONArray allUserArray = SplitUsers.readFromFile(ALL_PATH);
        for (int j=0;j<allUserArray.size();j++)
        {
            UserEntity userEntity = new UserEntity();
            JSONObject jsonObject = null;

            jsonObject = (JSONObject) allUserArray.get(j);
            userEntity.setScreenName((String) jsonObject.get("screenName"));
            int userId = 0;
            if(jsonObject.get("id").getClass() == Integer.class)
            {
                userId = (Integer) jsonObject.get("id");
            }
            else if(jsonObject.get("id").getClass() == Long.class)
            {
                userId = ((Long) jsonObject.get("id")).intValue();
            }
            userEntity.setId((long)userId);

            allUsers.add(userEntity);



        }

        Gson gson = new GsonBuilder().create();

        for (int i=0;i<targetUsers.size();i++)
        {
            ArrayList<JSONObject> followerList = new ArrayList<JSONObject>();

            UserEntity firstUser = new UserEntity();
            firstUser.setId(targetUsers.get(i).getId());
            firstUser.setScreenName(targetUsers.get(i).getScreenName());

            for (int j=0;j<allUsers.size();j++)
            {
                if(targetUsers.get(i).getScreenName().equals(allUsers.get(j).getScreenName())) continue;

                UserEntity secondUser = new UserEntity();
                secondUser.setId(allUsers.get(j).getId());
                secondUser.setScreenName(allUsers.get(j).getScreenName());

                FollowerEntity followerEntity = HtmlParse.getFromUrl("http://twopcharts.com/relationship/",firstUser,secondUser);
                if(followerEntity.getReverseFollowing() != null && followerEntity.getReverseFollowing() == true)
                {
                    followerList.add(followerEntity.getJsonObject());
                }

            }
            String content = gson.toJson(followerList);
            SplitUsers.saveToFile(content,firstUser.getId()+".json");
        }


    }
}
