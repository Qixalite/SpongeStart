# SpongeStart [![forthebadge](https://forthebadge.com/images/badges/contains-cat-gifs.svg)](https://forthebadge.com) 
[![GitHub 
stars](https://img.shields.io/github/stars/Qixalite/SpongeStart.svg)](https://github.com/Qixalite/SpongeStart/stargazers) [![GitHub 
issues](https://img.shields.io/github/issues/Qixalite/SpongeStart.svg)](https://github.com/Qixalite/SpongeStart/issues) [![Build 
Status](https://travis-ci.org/Qixalite/SpongeStart.svg?branch=master)](https://travis-ci.org/Qixalite/SpongeStart)

Gradle plugin to run sponge inside your workspace.

## Example Build.gradle for your project
```groovy
plugins {
  id "com.qixalite.spongestart" version "1.6.0"
  id "java"
}

sourceCompatibility = 1.8

repositories {
    mavenCentral()
    maven {
        url 'https://repo.spongepowered.org/maven/'
    }
}

spongestart{
   eula true
   
   //optional settings, takes latest version by default
   minecraft '1.10.2'
   type 'bleeding'
   spongeForgeVersion '1.10.2-2202-5.1.0-BETA-2042'
   spongeVanillaVersion '1.10.2-5.0.0-BETA-89'
}

dependencies{
    //you still need to set your dependency on sponge, this plugin only handles the running part.
    compile 'org.spongepowered:spongeapi:3.1.0-SNAPSHOT'
}
```

##Commands
>`gradle setupServer`
> Generates a forge and vanilla server + intelij run configurations.

>`gradle setupVanilla`
> Generates a vanilla server + intelij run configurations.

>`gradle setupForge`
> Generates a forge server + intelij run configurations.


## Run Configurations for your IDE (in case they don't automatic generate)

####**Start SpongeForge Server:**
>
- **Mainclass**: `StartServer`
- **Working Directory**: `run/forge`

####**Start SpongeVanilla Server:**
>
- **Mainclass**: `StartServer`
- **Working Directory**: `run/vanilla`
- **arguments**: `-scan-classpath`
