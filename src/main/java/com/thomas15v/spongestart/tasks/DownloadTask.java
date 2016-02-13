package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadTask extends DefaultTask {

    private File location;
    private URL url;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    @TaskAction
    public void doStuff(){
        getLogger().lifecycle("Downloading: " + getUrl());
        try {
            if (location.exists()) {
                getLogger().lifecycle("Done! (cached)");
                return;
            }
            FileUtils.copyURLToFile(url, location);
            getLogger().lifecycle("Done!");
        } catch (IOException e) {
            throw new GradleException("Failed to download: " + url + " with: " + e.getMessage());
        }
    }

}
