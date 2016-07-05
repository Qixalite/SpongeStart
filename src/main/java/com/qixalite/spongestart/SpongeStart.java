package com.qixalite.spongestart;

import com.qixalite.spongestart.tasks.*;
import com.qixalite.spongestart.util.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Task;

public class SpongeStart implements Plugin<Project>  {

    private static final String PROVIDED_SCOPE = "PROVIDED_SCOPE";

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
        this.project.getExtensions().create("spongestart", SpongeStartExtension.class, this);

        this.project.afterEvaluate(projectAfter -> {
            setupTasks((SpongeStartExtension) projectAfter.getExtensions().getByName("spongestart"));
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

        this.project.getConfigurations().maybeCreate(PROVIDED_SCOPE);
        this.project.getDependencies().add("runtime", this.project.files(this.startDir));

        setupIntellij();

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
        generateIntelijVanilla.dependsOn(setupVanillaServer, generateStartTask);

        Task generateIntellijTasks = this.project.getTasks().create("generateIntellijTasks").dependsOn(generateIntelijForge, generateIntelijVanilla);

        //clean tasks
        CleanFolderTask cleanVanilla = this.project.getTasks().create("cleanVanillaServer", CleanFolderTask.class);
        cleanVanilla.setFolder(new File(extension.getVanillaServerFolder()));

        CleanFolderTask cleanForge = this.project.getTasks().create("cleanForgeServer", CleanFolderTask.class);
        cleanForge.setFolder(new File(extension.getForgeServerFolder()));

        this.project.getTasks().create("cleanServer")
                .dependsOn(cleanForge, cleanVanilla)
                .setGroup(Constants.TASK_GROUP);
        this.project.getTasks().create("cleanSpongeStartCache", CleanFolderTask.class)
                .setFolder(this.cachedDir);

        //stuff to make our lives easier
        this.project.getTasks().create("setupServer")
                .dependsOn(setupForgeServer, setupVanillaServer, generateIntellijTasks)
                .setGroup(Constants.TASK_GROUP);

        this.project.getTasks().create("setupVanilla")
                .dependsOn(setupVanillaServer, generateIntelijVanilla)
                .setGroup(Constants.TASK_GROUP);
        this.project.getTasks().create("setupForge")
                .dependsOn(setupForgeServer, generateIntelijForge)
                .setGroup(Constants.TASK_GROUP);

        if (extension.isEula()){
            setupForgeServer.dependsOn(acceptEulaTask);
            setupVanillaServer.dependsOn(acceptEulaTask);
        }
    }

    private void applyPlugins(){
        this.project.getPlugins().apply("java");
        this.project.getPlugins().apply("idea");
    }

    private void setupIntellij(){
        Map<String, Map<String, Collection<Configuration>>> scopes = ((IdeaModel) this.project.getExtensions().getByName("idea"))
                .getModule().getScopes();

        Configuration compileConfiguration = this.project.getConfigurations().getByName("compile");
        ResolvedConfiguration resolvedconfig = compileConfiguration.getResolvedConfiguration();

        resolvedconfig.getFirstLevelModuleDependencies().stream().
                filter(resolvedDependency -> resolvedDependency.getName().startsWith("org.spongepowered:spongeapi")).forEach(
                spongeApi ->
                        spongeApi.getAllModuleArtifacts()
                                .forEach(file ->
                                        this.project.getDependencies().add(PROVIDED_SCOPE, file.getModuleVersion().getId().toString())
                                )

        );
        this.project.getConfigurations().forEach(System.out::println);
        addExtraConfiguration(project.getConfigurations().stream().filter(c -> c.getName().startsWith("forge")).collect(Collectors.toList()));
        Configuration provided = this.project.getConfigurations().getByName(SpongeStart.PROVIDED_SCOPE);
        scopes.get("COMPILE").get("minus")
                .add(provided);
        scopes.get("PROVIDED").get("plus")
                .add(provided);
    }

    private String getintellijModuleName(){
        return ((IdeaModel) this.project.getExtensions().getByName("idea"))
                .getModule().getName() + "_main";
    }

    private void addExtraConfiguration(List<Configuration> configurations){
        configurations.stream().filter(configuration -> configuration != null)
                .forEach(configuration -> configuration.getResolvedConfiguration()
                        .getResolvedArtifacts().forEach(dep -> this.project.getDependencies()
                                .add(PROVIDED_SCOPE, dep.getModuleVersion().getId().toString())));

    }
}
