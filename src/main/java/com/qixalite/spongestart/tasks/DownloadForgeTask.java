package com.qixalite.spongestart.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import static com.qixalite.spongestart.Constants.FORGE_REPO;

public class DownloadForgeTask extends DownloadTask {

    private SpongeDownloadTask downloadSpongeForgeTask;

    public void setDownloadSpongeForgeTask(SpongeDownloadTask downloadSpongeForgeTask) {
        this.downloadSpongeForgeTask = downloadSpongeForgeTask;
    }

    @Override
    public void doStuff() {
        try {
            String key = downloadSpongeForgeTask.getMinecraft() + "-" + downloadSpongeForgeTask.getForge();
            this.setUrl(new URL(FORGE_REPO + key + "/forge-" + key + "-installer.jar"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        super.doStuff();
    }
}
