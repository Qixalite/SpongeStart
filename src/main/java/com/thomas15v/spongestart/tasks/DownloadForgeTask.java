package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;

import java.net.URL;

public class DownloadForgeTask extends DownloadFromRepoTask {

    private DownloadFromRepoTask downloadSpongeForgeTask;

    public void setDownloadSpongeForgeTask(DownloadFromRepoTask downloadSpongeForgeTask) {
        this.downloadSpongeForgeTask = downloadSpongeForgeTask;
    }

    @Override
    public void doStuff() {
        //http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.8.9-11.15.1.1741/forge-1.8.9-11.15.1.1741-installer.jar
        setNumber(Util.getFileName(downloadSpongeForgeTask.getUrl()).split("-")[2]);
        repo.setFileextension("-installer.jar");
        super.doStuff();
    }
}
