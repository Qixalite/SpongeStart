package com.qixalite.spongestart.tasks;

import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GenerateStart extends DefaultTask {

    private static final String[] filenames = {"StartServer.java"};

    @OutputDirectory
    private File outputDir = new File("test");

    @TaskAction
    public void doStuff(){
        try {
            if (this.outputDir.exists()) {
                this.outputDir.delete();
            }

            this.outputDir.mkdirs();
            List<File> files = new ArrayList<>();

            for (String name : GenerateStart.filenames){
                InputStream link = ClassLoader.class.getResourceAsStream(name);
                File outputFile = new File(outputDir, name);

                IOUtils.copy(link, new FileOutputStream(outputFile));
                files.add(outputFile);
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
