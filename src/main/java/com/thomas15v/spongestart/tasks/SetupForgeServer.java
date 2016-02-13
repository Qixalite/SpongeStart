package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class SetupForgeServer extends DefaultTask {

    private File folder = new File("run");

    public void setFolder(File folder) {
        if (folder != null)
            this.folder = folder;
    }

    @TaskAction
    private void setupForge(){
        try {
            if (new File(this.folder, "libraries").exists()){
                throw new GradleException("Setup has already run, do \"gradle cleanForgeServer\" before running this command again");
            }

            this.getLogger().lifecycle("Starting setup");

            Process pr = Runtime.getRuntime().exec("java -jar setup.jar --installServer", null, this.folder);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            while (pr.isAlive()){
                String line = reader.readLine();
                if (line != null)
                    this.getLogger().lifecycle(line);
            }

            new File(this.folder, "setup.jar").delete();;

            for (File file : folder.listFiles((dir, name) -> name.endsWith("-universal.jar"))) {
                file.renameTo(new File(this.folder, "server.jar"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
