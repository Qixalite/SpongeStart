package com.thomas15v.spongestart.tasks;

import com.thomas15v.spongestart.maven.BuildNumberRepo;

import java.net.MalformedURLException;

public class DownloadFromRepoTask extends DownloadTask {

    private String version;
    private String repoUrl;
    protected BuildNumberRepo repo;

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        try {
            repo = new BuildNumberRepo(repoUrl);
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
            if ("LATEST".equalsIgnoreCase(version)){
                setUrl(repo.getLatest());
            }else {
                int number = Integer.valueOf(version);
                setUrl(repo.getFor(number));
            }
            super.doStuff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
