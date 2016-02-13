package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
            if (new File(folder, "libraries").exists()){
                throw new GradleException("Setup has already run, do \"gradle cleanForgeServer\" before running this command again");
            }
            getLogger().lifecycle("Starting setup");
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("java -jar setup.jar --installServer", null, folder);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            while (pr.isAlive()){
                String line = reader.readLine();
                if (line != null)
                    getLogger().lifecycle(line);
            }
            new File(folder, "setup.jar").delete();

            for (File file : folder.listFiles()) {
                if (file.getName().endsWith("-universal.jar")){
                    file.renameTo(new File(folder, "server.jar"));
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
