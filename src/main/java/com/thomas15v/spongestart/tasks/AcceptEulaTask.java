package com.thomas15v.spongestart.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AcceptEulaTask extends DefaultTask {

    private List<File> folders = new ArrayList<>();

    public void addFolder(File folder) {
        this.folders.add(folder);
    }

    @TaskAction
    public void doStuff(){
        this.folders.forEach(folder ->{
                    try {
                        FileUtils.writeStringToFile(new File(folder, "eula.txt"), "eula=true");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );
    }
}
