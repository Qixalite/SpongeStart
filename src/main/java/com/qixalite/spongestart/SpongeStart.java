package com.qixalite.spongestart;

import com.qixalite.spongestart.tasks.*;
import com.qixalite.spongestart.util.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpongeStart implements Plugin<Project>  {

    public static final String RUNTIME_SCOPE = "RUNTIME_SPONGE";

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
            this.project.getConfigurations().maybeCreate(RUNTIME_SCOPE);
            setupTasks((SpongeStartExtension) projectAfter.getExtensions().getByName("sponge"));
        });
    }

    private void setupTasks(SpongeStartExtension extension){
        //accept eula tasks
        AcceptEulaTask acceptEulaTask = this.project.getTasks().create("acceptEula", AcceptEulaTask.class);
        acceptEulaTask.addFolder(new File(extension.getForgeServerFolder()));
        acceptEulaTask.addFolder(new File(extension.getVanillaServerFolder()));

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
        setupForgeServer.dependsOn(downloadForgeSetup);
        setupForgeServer.setFolder(new File(extension.getForgeServerFolder()));

        //sponge Vanilla tasks
        DownloadFromRepoTask setupVanillaServer = this.project.getTasks().create("setupVanillaServer", DownloadFromRepoTask.class);
        setupVanillaServer.setLocation(new File(extension.getVanillaServerFolder(), "server.jar"));
        setupVanillaServer.setRepoUrl(Constants.SPONGEVANILLA_REPO);
        setupVanillaServer.setNumber(extension.getSpongeVanillaBuild());

        //generate intelij tasks
        String intellijModule = getintellijModuleName();

        GenerateIntelijTask generateIntelijForge = this.project.getTasks().create("generateIntellijForgeTask", GenerateIntelijTask.class);
        generateIntelijForge.setModulename(intellijModule);
        generateIntelijForge.setTaskname("StartForgeServer");
        generateIntelijForge.setWorkingdir(extension.getForgeServerFolder());
        generateIntelijForge.setProject(this.project);
        generateIntelijForge.dependsOn(setupForgeServer);

        GenerateIntelijTask generateIntelijVanilla = this.project.getTasks().create("generateIntellijVanillaTask", GenerateIntelijTask.class);
        generateIntelijVanilla.setModulename(intellijModule);
        generateIntelijVanilla.setTaskname("StartVanillaServer");
        generateIntelijVanilla.setWorkingdir(extension.getVanillaServerFolder());
        generateIntelijVanilla.setRunoption("-scan-classpath");
        generateIntelijVanilla.setProject(this.project);
        generateIntelijVanilla.dependsOn(setupVanillaServer);

        Task generateIntellijTasks = this.project.getTasks().create("generateIntellijTasks").dependsOn(generateIntelijForge, generateIntelijVanilla);

        //clean tasks
        CleanFolderTask cleanVanilla = this.project.getTasks().create("cleanVanillaServer", CleanFolderTask.class);
        cleanVanilla.setFolder(new File(extension.getVanillaServerFolder()));

        CleanFolderTask cleanForge = this.project.getTasks().create("cleanForgeServer", CleanFolderTask.class);
        cleanForge.setFolder(new File(extension.getForgeServerFolder()));


        this.project.getTasks().create("cleanServer").dependsOn(cleanForge, cleanVanilla);
        this.project.getTasks().create("cleanCache", CleanFolderTask.class).setFolder(this.cachedDir);

        //stuff to make our lives easier
        this.project.getTasks().create("setupServer").dependsOn(setupForgeServer, setupVanillaServer, generateIntellijTasks);
        this.project.getTasks().create("setupVanilla").dependsOn(setupVanillaServer, generateIntelijVanilla);
        this.project.getTasks().create("setupForge").dependsOn(setupForgeServer, generateIntelijForge);

        if (extension.isEula()){
            setupForgeServer.dependsOn(acceptEulaTask);
            setupVanillaServer.dependsOn(acceptEulaTask);
        }
        addDependencies(extension.getForgeServerFolder() + "/server.jar", extension.getVanillaServerFolder() + "/server.jar");
    }

    private void addDependencies(String... files){
        try {
            for (String filePath : files) {
                this.project.getDependencies().add(SpongeStart.RUNTIME_SCOPE, this.project.files(filePath));
            }
            ((IdeaModel) this.project.getExtensions().getByName("idea"))
                    .getModule().getScopes().get("PROVIDED").get("plus")
                    .add(this.project.getConfigurations().getByName(SpongeStart.RUNTIME_SCOPE));

            //((IdeaModel) this.project.getExtensions().getByName("idea")).getModule().getScopes()

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyPlugins(){
        this.project.getPlugins().apply("java");
        this.project.getPlugins().apply("idea");
        //this.project.getPlugins().apply("eclipse");
    }

    private String getintellijModuleName(){
        return ((IdeaModel) this.project.getExtensions().getByName("idea"))
                .getModule().getName();
    }
}
