package com.qixalite.spongestart.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class Util {

    public static Optional<URL> getForgeDownload(int build){
       /* try {
            JsonObject jsonObject = getDownloadversion(build, Constants.forgeindex);
            String version = jsonObject.get("mcversion").getAsString() + "-" + jsonObject.get("version").getAsString();
            return Optional.of(new URL(formatdownload(Constants.forgeinstallerdownload, version)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        return Optional.empty();
    }

    public static Optional<URL> getSpongeDownload(int build, int forgebuild){
        /*try {
            JsonObject jsonObject = getDownloadversion(build, Constants.spongeindex);
            String version = jsonObject.get("mcversion").getAsString() + "-" + forgebuild + "-" + jsonObject.get("version").getAsString();
            return Optional.of(new URL(formatdownload(Constants.spongedownload, version)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        return Optional.empty();
    }

    private static JsonObject getDownloadversion(int build, String URL){
        try {
            URL index = new URL(URL);
            Reader in = new BufferedReader(new InputStreamReader(index.openStream()));
            JsonParser parser = new JsonParser();
            return parser.parse(in).getAsJsonObject().getAsJsonObject("number").getAsJsonObject(String.valueOf(build));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String formatdownload(String string, String version){
        return String.format(string, version, version);
    }

    public static String getFileName(URL url){
        String path = url.getPath();
        if (url.getPath().endsWith("/"))
            path = path.substring(0, url.getPath().length() - 1);
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
