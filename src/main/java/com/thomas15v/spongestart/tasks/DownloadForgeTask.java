package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.util.Util;

public class DownloadForgeTask extends DownloadFromRepoTask {

    private DownloadFromRepoTask downloadSpongeForgeTask;

    public void setDownloadSpongeForgeTask(DownloadFromRepoTask downloadSpongeForgeTask) {
        this.downloadSpongeForgeTask = downloadSpongeForgeTask;
    }

    @Override
    public void doStuff() {
        this.setNumber(Util.getFileName(this.downloadSpongeForgeTask.getUrl()).split("-")[2]);
        this.repo.setFileExtension("-installer.jar");
        super.doStuff();
    }
}
