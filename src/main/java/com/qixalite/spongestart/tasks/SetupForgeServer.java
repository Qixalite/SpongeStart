package com.qixalite.spongestart.tasks;

import com.qixalite.spongestart.SpongeStart;
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
            if (new File(this.folder, "libraries").exists()){
                throw new GradleException("Setup has already run, do \"gradle cleanForgeServer\" before running this command again");
            }

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

            try {
                pr.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File setupJar = new File(this.folder, "setup.jar");
            setupJar.delete();

            File serverJar = new File(this.folder, "server.jar");
            for (File file : folder.listFiles((dir, name) -> name.endsWith("-universal.jar"))) {
                file.renameTo(serverJar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
