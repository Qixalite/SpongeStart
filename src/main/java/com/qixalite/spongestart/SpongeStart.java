package com.qixalite.spongestart;

import com.qixalite.spongestart.tasks.*;
import com.qixalite.spongestart.util.Constants;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpongeStart implements Plugin<Project>  {

    public static final String PROVIDED_SCOPE = "spongeStart_Provided";

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
        this.project.getGradle().addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(Gradle gradle) {

            }

            @Override
            public void settingsEvaluated(Settings settings) {

            }

            @Override
            public void projectsLoaded(Gradle gradle) {

            }

            @Override
            public void projectsEvaluated(Gradle gradle) {

            }

            @Override
            public void buildFinished(BuildResult result) {
                setupIntellij();
            }
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

        //SpongeForge Download Task
        SpongeDownloadTask downloadSpongeForge = this.project.getTasks().create("downloadSpongeForge", SpongeDownloadTask.class);
        downloadSpongeForge.setLocation(new File(extension.getForgeServerFolder(), Constants.SPONGEMOD_LOCATION));
        downloadSpongeForge.setMincraft(extension.getMinecraft());
        downloadSpongeForge.setType(extension.getType());
        downloadSpongeForge.setPlatform(SpongeDownloadTask.Platform.FORGE);

        //Download Forge Task
        DownloadForgeTask downloadForgeSetup = this.project.getTasks().create("downloadForgeSetup", DownloadForgeTask.class);
        downloadForgeSetup.setDownloadSpongeForgeTask(downloadSpongeForge);
        downloadForgeSetup.dependsOn(downloadSpongeForge);
        downloadForgeSetup.setLocation(new File(extension.getForgeServerFolder(), Constants.FORGESETUP_LOCATION));

        //Setup Forge task
        SetupForgeServer setupForgeServer = this.project.getTasks().create("SetupForgeServer", SetupForgeServer.class);
        setupForgeServer.dependsOn(downloadForgeSetup, generateStartTask);
        setupForgeServer.setFolder(new File(extension.getForgeServerFolder()));

        //sponge Vanilla tasks
        SpongeDownloadTask setupVanillaServer = this.project.getTasks().create("setupVanillaServer", SpongeDownloadTask.class);
        setupVanillaServer.setLocation(new File(extension.getVanillaServerFolder(), "server.jar"));
        setupVanillaServer.setMincraft(extension.getMinecraft());
        setupVanillaServer.setType(extension.getType());
        setupVanillaServer.setPlatform(SpongeDownloadTask.Platform.VANILLA);

        //generate intelij tasks
        String intellijModule = getintellijModuleName();

        GenerateIntelijTask generateIntelijForge = this.project.getTasks().create("generateIntellijForgeTask", GenerateIntelijTask.class);
        generateIntelijForge.setModulename(intellijModule);
        generateIntelijForge.setTaskname("StartForgeServer");
        generateIntelijForge.setWorkingdir(extension.getForgeServerFolder());
        generateIntelijForge.dependsOn(setupForgeServer);
        generateIntelijForge.setRunoption(extension.getExtraProgramParameters());

        GenerateIntelijTask generateIntelijVanilla = this.project.getTasks().create("generateIntellijVanillaTask", GenerateIntelijTask.class);
        generateIntelijVanilla.setModulename(intellijModule);
        generateIntelijVanilla.setTaskname("StartVanillaServer");
        generateIntelijVanilla.setWorkingdir(extension.getVanillaServerFolder());
        generateIntelijVanilla.setRunoption("-scan-classpath " + extension.getExtraProgramParameters());
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
        Map<String, Map<String, Collection<Configuration>>> scopes = ((IdeaModel) getProject().getExtensions().getByName("idea"))
                .getModule().getScopes();

        Configuration compileConfiguration = getProject().getConfigurations().getByName("compile");
        ResolvedConfiguration resolvedconfig = compileConfiguration.getResolvedConfiguration();

        resolvedconfig.getFirstLevelModuleDependencies().stream().
                filter(resolvedDependency -> resolvedDependency.getName().startsWith("org.spongepowered")).forEach(
                spongeApi ->
                        spongeApi.getAllModuleArtifacts()
                                .forEach(file ->
                                        getProject().getDependencies().add(SpongeStart.PROVIDED_SCOPE, file.getModuleVersion().getId().toString())
                                )

        );
        addExtraConfiguration(getProject().getConfigurations().stream().filter(c -> c.getName().startsWith("forge")).collect(Collectors.toList()));
        Configuration provided = getProject().getConfigurations().getByName(SpongeStart.PROVIDED_SCOPE);

        scopes.get("COMPILE").get("minus")
                .add(provided);
        scopes.get("PROVIDED").get("plus")
                .add(provided);
    }

    private void addExtraConfiguration(List<Configuration> configurations){
        configurations.stream().filter(Objects::nonNull)
                .forEach(configuration -> configuration.getResolvedConfiguration()
                        .getResolvedArtifacts().forEach(dep -> this.getProject().getDependencies()
                                .add(SpongeStart.PROVIDED_SCOPE, dep.getModuleVersion().getId().toString())));

    }

    private String getintellijModuleName(){
        IdeaModel ideaModel =  ((IdeaModel) this.project.getExtensions().getByName("idea"));
        //todo find a way to read idea's sourcesets
        return ideaModel.getModule().getName() + "_main";
    }

    public Project getProject() {
        return project;
    }
}
