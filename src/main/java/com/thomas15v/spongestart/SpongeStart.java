package com.thomas15v.spongestart;

import com.thomas15v.spongestart.tasks.GenerateStart;
import com.thomas15v.spongestart.tasks.SetupServer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.io.File;

public class SpongeStart implements Plugin<Project>  {

    private static final String RUNTIME_SCOPE = "RUNTIME_SPONGE";

    private Project project;
    private File startDir;

    @Override
    public void apply(Project project) {
        this.project = project;
        this.startDir = new File(project.getGradle().getGradleUserHomeDir(), "caches/SpongeStart/start");
        applyPlugins();
        project.getExtensions().create("sponge", SpongeStartExtension.class, this);

        project.afterEvaluate(projectAfter -> {
            SpongeStartExtension extension = (SpongeStartExtension) projectAfter.getExtensions().getByName("sponge");

            //setupserver stuff
            {
                SetupServer setupServerTask = project.getTasks().create("SetupServer", SetupServer.class);
                setupServerTask.setForgeBuild(extension.getForgeBuild());
                setupServerTask.setSpongeBuild(extension.getSpongeBuild());
                if (extension.getFolder() != null) {
                    setupServerTask.setFolder(new File(extension.getFolder()));
                }
            }

            //generate tasks stuff
            {
                GenerateStart generateStartTask = project.getTasks().create("generateStart", GenerateStart.class);
                generateStartTask.setOutputDir(startDir);

                ConfigurableFileCollection col = project.files(startDir);
                project.getConfigurations().maybeCreate(RUNTIME_SCOPE);
                project.getDependencies().add(RUNTIME_SCOPE, col);
            }

            setupIntellij();
        });
    }

    private void setupIntellij(){
        IdeaModel ideaConv = (IdeaModel) project.getExtensions().getByName("idea");
        ideaConv.getModule().getScopes().get("RUNTIME").get("plus").add(project.getConfigurations().getByName(RUNTIME_SCOPE));
    }

    private void applyPlugins(){
        project.getPlugins().apply("java");
        project.getPlugins().apply("idea");
        project.getPlugins().apply("eclipse");
    }
}
