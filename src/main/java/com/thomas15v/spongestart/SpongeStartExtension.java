package com.thomas15v.spongestart;

public class SpongeStartExtension {


    private final transient SpongeStart spongeStart;
    protected int forgeBuild;
    protected int spongeBuild;
    protected String folder;

    public SpongeStartExtension(SpongeStart spongeStart) {
        this.spongeStart = spongeStart;
    }

    public int getForgeBuild() {
        return forgeBuild;
    }

    public int getSpongeBuild() {
        return spongeBuild;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setForgeBuild(int forgeBuild) {
        this.forgeBuild = forgeBuild;
    }

    public void setSpongeBuild(int spongeBuild) {
        this.spongeBuild = spongeBuild;
    }
}
