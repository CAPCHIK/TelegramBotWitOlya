package ru.cap;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        // write your code here
        TelegramBot bot = new TelegramBot("208672762:AAEZmQCuRidPJxCL1BVPAMDk_nxVKxQxWhM");
        VKParser parser = new VKParser(bot);
        System.out.println(parser.getUpdateWall());
    }
}
