package ru.cap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Maksim on 08.04.2016.
 */
public class VKParser implements Runnable{
    private String domain = "parsewitholya";
    private int lastId;
    private MyActionList handler;
    private Thread forWork;


    public VKParser(MyActionList handler) throws IOException {
        this.handler = handler;
        lastId = 0;
        lastId = getLastId();
        forWork = new Thread(this);
        forWork.start();
    }

    private int getLastId() throws IOException {
        return getUpdateWall().id;
    }



    public Wall getUpdateWall() throws IOException {
        URL url = new URL("https://api.vk.com/method/wall.get?" +
                "domain=" + domain +
                "&count=1&v=5.50");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        StringBuilder builder = new StringBuilder();
        
        while ((line = reader.readLine()) != null)
            builder.append(line);
        
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(builder.toString()).getAsJsonObject();
        JsonObject wall = root.get("response").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
        int newID = wall.get("id").getAsInt();
        if (newID > lastId){
            lastId = newID;
            return new Wall(wall.get("text").getAsString(),newID);
        }
        return null;
    }

    @Override
    public void run() {
        while (true){
            try {
                Wall wall = getUpdateWall();
                if (wall != null)
                    handler.proccesText(wall.text);


                Thread.sleep(5 * 100);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
