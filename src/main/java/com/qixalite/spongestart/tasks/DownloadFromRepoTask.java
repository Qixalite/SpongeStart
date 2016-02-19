package com.qixalite.spongestart.tasks;

import com.qixalite.spongestart.maven.BuildNumberRepo;

import java.net.MalformedURLException;

public class DownloadFromRepoTask extends DownloadTask {

    private String version;
    private String repoUrl;
    protected BuildNumberRepo repo;

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        try {
            this.repo = new BuildNumberRepo(repoUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setNumber(String version) {
        this.version = version;
    }

    @Override
    public void doStuff() {
        try {
            if ("LATEST".equalsIgnoreCase(this.version)){
                this.setUrl(this.repo.getLatest());
            }else {
                int number = Integer.valueOf(this.version);
                this.setUrl(this.repo.getFor(number));
            }
            super.doStuff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
