package com.thomas15v.spongestart;

import com.thomas15v.spongestart.tasks.*;
import com.thomas15v.spongestart.util.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.io.File;

public class SpongeStart implements Plugin<Project>  {

    private static final String RUNTIME_SCOPE = "RUNTIME_SPONGE";

    private Project project;
    private File cachedDir;
    private File startDir;
    private File downloadCacheDir;

    @Override
    public void apply(Project project) {
        this.project = project;
        this.cachedDir = new File(project.getGradle().getGradleUserHomeDir(), "caches/SpongeStart/");
        this.startDir = new File(this.cachedDir, "start");
        this.downloadCacheDir = new File(this.cachedDir, "downloads");

        DownloadTask.setCacheDir(this.downloadCacheDir);

        applyPlugins();
        this.project.getExtensions().create("sponge", SpongeStartExtension.class, this);

        this.project.afterEvaluate(projectAfter -> {
            setupTasks((SpongeStartExtension) projectAfter.getExtensions().getByName("sponge"));
        });
    }

    private void setupTasks(SpongeStartExtension extension){
        //accept eula tasks
        AcceptEulaTask acceptEulaTask = this.project.getTasks().create("acceptEula", AcceptEulaTask.class);
        acceptEulaTask.addFolder(new File(extension.getForgeServerFolder()));
        acceptEulaTask.addFolder(new File(extension.getVanillaServerFolder()));

        //generate start task
        GenerateStart generateStartTask = this.project.getTasks().create("generateStart", GenerateStart.class);
        generateStartTask.setOutputDir(this.startDir);

        this.project.getConfigurations().maybeCreate(RUNTIME_SCOPE);
        this.project.getDependencies().add(RUNTIME_SCOPE, this.project.files(this.startDir));

        setupIntellij();
        setupEclipse();

        //SpongeForge Download Task
        DownloadFromRepoTask downloadSpongeForge = this.project.getTasks().create("downloadSpongeForge", DownloadFromRepoTask.class);
        downloadSpongeForge.setNumber(extension.getSpongeForgeBuild());
        downloadSpongeForge.setLocation(new File(extension.getForgeServerFolder(), Constants.SPONGEMOD_LOCATION));
        downloadSpongeForge.setRepoUrl(Constants.SPONGEFORGE_REPO);

        //Download Forge Task
        DownloadForgeTask downloadForgeSetup = this.project.getTasks().create("downloadForgeSetup", DownloadForgeTask.class);
        downloadForgeSetup.setDownloadSpongeForgeTask(downloadSpongeForge);
        downloadForgeSetup.dependsOn(downloadSpongeForge);
        downloadForgeSetup.setLocation(new File(extension.getForgeServerFolder(), Constants.FORGESETUP_LOCATION));
        downloadForgeSetup.setRepoUrl(Constants.FORGE_REPO);

        //Setup Forge task
        SetupForgeServer setupForgeServer = this.project.getTasks().create("SetupForgeServer", SetupForgeServer.class);
        setupForgeServer.dependsOn(downloadForgeSetup, generateStartTask);
        setupForgeServer.setFolder(new File(extension.getForgeServerFolder()));

        //sponge Vanilla tasks
        DownloadFromRepoTask setupVanillaServer = this.project.getTasks().create("setupVanillaServer", DownloadFromRepoTask.class);
        setupVanillaServer.setLocation(new File(extension.getVanillaServerFolder(), "server.jar"));
        setupVanillaServer.setRepoUrl(Constants.SPONGEVANILLA_REPO);
        setupVanillaServer.setNumber(extension.getSpongeVanillaBuild());

        //clean tasks
        CleanFolderTask cleanVanilla = this.project.getTasks().create("cleanVanillaServer", CleanFolderTask.class);
        cleanVanilla.setFolder(new File(extension.getVanillaServerFolder()));

        CleanFolderTask cleanForge = this.project.getTasks().create("cleanForgeServer", CleanFolderTask.class);
        cleanForge.setFolder(new File(extension.getForgeServerFolder()));

        this.project.getTasks().create("cleanServer", CleanFolderTask.class).dependsOn(cleanForge, cleanVanilla);

        this.project.getTasks().create("cleanCache", CleanFolderTask.class).setFolder(this.cachedDir);

        //stuff to make our lives easier
        this.project.getTasks().create("setupServer").dependsOn(setupForgeServer, setupVanillaServer);

        if (extension.isEula()){
            setupForgeServer.dependsOn(acceptEulaTask);
            setupVanillaServer.dependsOn(acceptEulaTask);
        }
    }

    private void applyPlugins(){
        this.project.getPlugins().apply("java");
        this.project.getPlugins().apply("idea");
        this.project.getPlugins().apply("eclipse");
    }

    private void setupIntellij(){
        ((IdeaModel) this.project.getExtensions().getByName("idea"))
                .getModule().getScopes().get("RUNTIME").get("plus")
                .add(this.project.getConfigurations().getByName(RUNTIME_SCOPE));
    }

    private void setupEclipse(){
        ((EclipseModel) this.project.getExtensions().getByName("eclipse"))
                .getClasspath().getPlusConfigurations()
                .add(this.project.getConfigurations().getByName(RUNTIME_SCOPE));
    }
}
