package com.thomas15v.spongestart;

import com.thomas15v.spongestart.tasks.DownloadForgeTask;
import com.thomas15v.spongestart.tasks.DownloadFromRepoTask;
import com.thomas15v.spongestart.tasks.GenerateStart;
import com.thomas15v.spongestart.tasks.SetupForgeServer;
import com.thomas15v.spongestart.util.Constants;
import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.XmlProvider;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
           /* SetupServer setupServerTask = project.getTasks().create("SetupServer", SetupServer.class);
            {
                setupServerTask.setForgeBuild(extension.getForgeBuild());
                setupServerTask.setSpongeBuild(extension.getSpongeBuild());
                if (extension.getFolder() != null) {
                    setupServerTask.setFolder(new File(extension.get()));
                }
            }*/

            //generate start stuff
            /*GenerateStart generateStartTask = project.getTasks().create("generateStart", GenerateStart.class);
            {
                generateStartTask.setOutputDir(startDir);
                ConfigurableFileCollection col = project.files(startDir);
                project.getConfigurations().maybeCreate(RUNTIME_SCOPE);
                project.getDependencies().add(RUNTIME_SCOPE, col);
                setupIntellij();
                setupEclipse();
            }*/

            //setupServerTask.dependsOn(generateStartTask);

            setupTasks(extension);


        });
    }

    private void setupTasks(SpongeStartExtension extension){

        //generate start task
        GenerateStart generateStartTask = project.getTasks().create("generateStart", GenerateStart.class);
        generateStartTask.setOutputDir(startDir);
        ConfigurableFileCollection col = project.files(startDir);
        project.getConfigurations().maybeCreate(RUNTIME_SCOPE);
        project.getDependencies().add(RUNTIME_SCOPE, col);
        setupIntellij();
        setupEclipse();

        //SpongeForge Download Task
        DownloadFromRepoTask downloadSpongeForge = project.getTasks().create("downloadSpongeForge", DownloadFromRepoTask.class);
        downloadSpongeForge.setNumber(extension.getSpongeForgeBuild());
        downloadSpongeForge.setLocation(new File(extension.getForgeserverFolder(), Constants.SPONGEMOD_LOCATION));
        downloadSpongeForge.setRepoUrl(Constants.SPONGEFORGE_REPO);

        //Download Forge Task
        DownloadForgeTask downloadForgeSetup = project.getTasks().create("downloadForgeSetup", DownloadForgeTask.class);
        downloadForgeSetup.setDownloadSpongeForgeTask(downloadSpongeForge);
        downloadForgeSetup.dependsOn(downloadSpongeForge);
        downloadForgeSetup.setLocation(new File(extension.getForgeserverFolder(), Constants.FORGESETUP_LOCATION));
        downloadForgeSetup.setRepoUrl(Constants.FORGE_REPO);

        //Setup Forge task
        SetupForgeServer setupForgeServer = project.getTasks().create("SetupForgeServer", SetupForgeServer.class);
        setupForgeServer.dependsOn(downloadForgeSetup, generateStartTask);
        setupForgeServer.setFolder(new File(extension.getForgeserverFolder()));

        DownloadFromRepoTask setupVanillaServer = project.getTasks().create("setupVanillaServer", DownloadFromRepoTask.class);
        setupVanillaServer.setLocation(new File(extension.getVanillaserverFolder(), "server.jar"));
        setupVanillaServer.setRepoUrl(Constants.SPONGEVANILLA_REPO);
        setupVanillaServer.setNumber(extension.getSpongeVanillaBuild());
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

    private void setupEclipse(){
        EclipseModel eclipseConv = (EclipseModel) project.getExtensions().getByName("eclipse");
        eclipseConv.getClasspath().getPlusConfigurations().add(project.getConfigurations().getByName(RUNTIME_SCOPE));
    }
}
