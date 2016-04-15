package ru.cap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Lacuna on 08.04.2016.
 */
public class TelegramBot implements Runnable, MyActionList{
    private String token;
    private static final String BEGINQUERY = "https://api.telegram.org/bot";
    private HashSet<Integer> ids;
    private Thread forUpdate;
    JsonParser parser;

    public TelegramBot(String token) {
        this.token = token;
        ids = new HashSet<>();
        forUpdate = new Thread(this);
        parser = new JsonParser();

        forUpdate.start();
    }

    public void sendToAll(String text) throws IOException {
        for (Integer id: ids) {
            sendMessage(text,id);
        }

    }

    public void sendMessage(String text, int chat_id) throws IOException {
        String queryString = BEGINQUERY + token + "/sendMessage?" +
                "text=" + text +
                "&chat_id=" + chat_id;

        URL url = new URL(queryString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null)
            builder.append(line);

        reader.close();
        connection.disconnect();

        System.out.println(builder.toString());
    }

    public ArrayList<Update> getUpdates() throws IOException {
        return getUpdates(0);
    }


    public ArrayList<Update> getUpdates(int update_id) throws IOException {
        ArrayList<Update> result = new ArrayList<>();
        String queryString = BEGINQUERY + token + "/getupdates?offset=" + update_id;

        URL url = new URL(queryString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null)
            builder.append(line);

        reader.close();
        connection.disconnect();
        JsonObject root = parser.parse(builder.toString()).getAsJsonObject();
        JsonArray updates = root.get("result").getAsJsonArray();
        if (updates.size() == 0)
            return result;

        for (JsonElement upd : updates){
            Update localUpdate = new Update();
            JsonObject jsonUpdate = upd.getAsJsonObject();
            localUpdate.update_id = jsonUpdate.get("update_id").getAsInt();
            localUpdate.messageText = jsonUpdate.get("message").getAsJsonObject().get("text").getAsString();
            localUpdate.fromId = jsonUpdate.get("message").getAsJsonObject().get("from").getAsJsonObject().get("id").getAsInt();
            result.add(localUpdate);
        }
        return result;
    }


    @Override
    public void run() {
        ArrayList<Update> updates;
        while (true){
            try{
                updates = getUpdates();
                for (Update update : updates){
                    String message = update.messageText;
                    if (message.equals("/start")) {
                        ids.add(update.fromId);
                        sendMessage("Ха-ха, теперь ты с нами!", update.fromId);
                    }
                    if (message.equals("/stop")) {
                        ids.remove(update.fromId);
                        sendMessage("Очень жаль,что мы прекратили сотрудничество", update.fromId);
                        sendMessage("А хотя, если не хочешь, не надо, пидр :)", update.fromId);
                    }
                }

                if (!updates.isEmpty())
                    getUpdates(updates.get(updates.size()-1).update_id+1);
                Thread.sleep(10 * 100);
            } catch (Exception ex){
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void proccesText(String message) {
        try {
            sendToAll(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
