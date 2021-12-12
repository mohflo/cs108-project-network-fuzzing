package com.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;

public class Config {

    public static String readJsonFile(String filePath) {
        String str = "";
        try {
            FileReader fr = new FileReader(filePath);
            int i;
            while ((i = fr.read()) != -1)
                str += "" + ((char) i);

            fr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("config.json: \n" + str);
        return str;
    }

    public static TestToolConfig getTestConfig(String jsonFilePath) {
        TestToolConfig list = null;
        try {
            String jsonStr = readJsonFile(jsonFilePath);
            list = new Gson().fromJson(jsonStr, new TypeToken<TestToolConfig>() {
            }.getType());
        } catch (Exception e) {
            System.out.println("Invalid Json file: " + jsonFilePath);
            e.printStackTrace();
        }
        return list;
    }
}
