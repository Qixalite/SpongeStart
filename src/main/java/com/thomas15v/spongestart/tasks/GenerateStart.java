package com.thomas15v.spongestart.tasks;

import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas15v on 12/02/16.
 */
public class GenerateStart extends DefaultTask {

    private static final String[] filenames = {"StartServer.java"};

    @OutputDirectory
    private File outputDir;

    @TaskAction
    public void doStuff(){
        try {
            if (!outputDir.exists())
                outputDir.mkdirs();
            List<File> files = new ArrayList<>();
            for (String name : filenames){
                InputStream link = GenerateStart.class.getResourceAsStream("java." + name);
                File outputfile = new File(outputDir, name);
                IOUtils.copy(link, new FileOutputStream(outputfile));
                files.add(outputfile);
            }
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjects(files.toArray(new File[files.size()]));
            compiler.getTask(null, fileManager, null, null, null, compilationUnits1).call();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
}
