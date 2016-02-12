package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class SetupServer extends DefaultTask {

    static File folder = new File("run");
    private Optional<URL> forgeDownload;
    private Optional<URL> spongeDownload;

    @Input
    private int spongeBuild;
    @Input
    private int forgeBuild;

    public void setForgeBuild(int forgeBuild) {
        forgeDownload = Util.getForgeDownload(forgeBuild);
        this.forgeBuild = forgeBuild;
    }

    public void setSpongeBuild(int spongeBuild) {
        spongeDownload = Util.getSpongeDownload(spongeBuild, forgeBuild);
        this.spongeBuild = spongeBuild;
    }

    public void setFolder(File folder) {
        if (folder != null)
            this.folder = folder;
    }

    private void setupForge(){
        try {
            System.out.println("Downloading Forge Installer: " + forgeDownload.get());
            FileUtils.copyURLToFile(forgeDownload.get(), new File(folder, "setup.jar"));
            System.out.println("Done!");
            System.out.println("Starting setup");
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("java -jar setup.jar --installServer", null, folder);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            while (pr.isAlive()){
                String line = reader.readLine();
                if (line != null)
                    System.out.println(line);
            }
            new File(folder, "setup.jar").delete();

            for (File file : folder.listFiles()) {
                if (file.getName().endsWith("-universal.jar")){
                    file.renameTo(new File("server.jar"));
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSponge(){
        File modfolder = new File(folder, "mods");
        if (modfolder.exists())
            modfolder.delete();
        modfolder.mkdirs();
        try {
            System.out.println("Downloading SpongeForge: " + spongeDownload.get());
            FileUtils.copyURLToFile(spongeDownload.get(), new File(modfolder, "sponge-" + spongeBuild + ".jar"));
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TaskAction
    public void setup() {
        if (forgeDownload.isPresent() && spongeDownload.isPresent()) {
            setupForge();
            setupSponge();
        }
    }

    private String formatdownload(String string, String version){
        return String.format(string, version, version);
    }

    public int getForgeBuild() {
        return forgeBuild;
    }

    public int getSpongeBuild() {
        return spongeBuild;
    }
}
