package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;

public class DownloadForgeTask extends DownloadFromRepoTask {

    private DownloadFromRepoTask downloadSpongeForgeTask;

    public void setDownloadSpongeForgeTask(DownloadFromRepoTask downloadSpongeForgeTask) {
        this.downloadSpongeForgeTask = downloadSpongeForgeTask;
    }

    @Override
    public void doStuff() {
        setNumber(Util.getFileName(downloadSpongeForgeTask.getUrl()).split("-")[2]);
        repo.setFileExtension("-installer.jar");
        super.doStuff();
    }
}
