# SpongeStart
Gradle plugin to run sponge inside your workspace.

## Example Build.gradle for your project
```groovy
apply plugin: 'java'
apply plugin: 'SpongeStart'

sourceCompatibility = 1.8

buildscript{
    repositories{
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.qixalite:SpongeStart:1.1'
    }
}

repositories {
    mavenCentral()
    maven {
        url 'https://repo.spongepowered.org/maven/'
    }
}

sponge{
   eula true
   
   //optional configs
   spongeVanillaBuild 'LATEST'
   spongeForgeBuild 'LATEST'
   forgeserverFolder 'run/forge'
   vanillaserverFolder 'run/vanilla'
}

dependencies{
    //you still need to set your dependency on sponge, this plugin only handles the running part.
    compile 'org.spongepowered:spongeapi:3.1.0-SNAPSHOT'
}
```

## Run Configurations for your IDE

####**Start SpongeForge Server:**
>
- **Mainclass**: `StartServer`
- **Working Directory**: `run/forge`

####**Start SpongeVanilla Server:**
>
- **Mainclass**: `StartServer`
- **Working Directory**: `run/vanilla`
- **arguments**: `-scan-classpath`
