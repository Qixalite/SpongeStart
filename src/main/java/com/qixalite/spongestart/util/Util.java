package com.qixalite.spongestart.util;

import java.net.URL;

public class Util {

    public static String getFileName(URL url){
        String path = url.getPath();
        if (url.getPath().endsWith("/"))
            path = path.substring(0, url.getPath().length() - 1);
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
