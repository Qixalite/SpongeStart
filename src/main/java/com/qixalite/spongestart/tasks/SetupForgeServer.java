package com.qixalite.spongestart.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLClassLoader;

public class SetupForgeServer extends DefaultTask {

    private File folder = new File("run");

    public void setFolder(File folder) {
        if (folder != null)
            this.folder = folder;
    }

    @TaskAction
    private void setupForge(){
        try {
            this.getLogger().lifecycle("Starting setup");

            Process pr = new ProcessBuilder()
                    .command("java -jar setup.jar --installServer".split(" "))
                    .directory(this.folder)
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            while (pr.isAlive()){
                String line = reader.readLine();
                if (line != null)
                    this.getLogger().lifecycle(line);
            }


            new File(this.folder, "setup.jar").delete();;
            try {
                pr.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File serverjar = new File(this.folder, "setup.jar");
            serverjar.delete();

            for (File file : folder.listFiles((dir, name) -> name.endsWith("-universal.jar"))) {
                file.renameTo(new File(this.folder, "server.jar"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
