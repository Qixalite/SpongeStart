package com.qixalite.spongestart.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import static com.qixalite.spongestart.util.Constants.FORGE_REPO;

public class DownloadForgeTask extends DownloadTask {

    private SpongeDownloadTask downloadSpongeForgeTask;

    public void setDownloadSpongeForgeTask(SpongeDownloadTask downloadSpongeForgeTask) {
        this.downloadSpongeForgeTask = downloadSpongeForgeTask;
    }

    @Override
    public void doStuff() {
        try {
            this.setUrl(new URL(FORGE_REPO + downloadSpongeForgeTask.getForge() + "/forge-" + downloadSpongeForgeTask.getForge() + "-installer.jar"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        super.doStuff();
    }
}
