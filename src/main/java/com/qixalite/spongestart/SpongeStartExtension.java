package com.qixalite.spongestart;

import java.io.File;

public class SpongeStartExtension {

    private final transient SpongeStart spongeStart;

    protected String spongeVanillaBuild = "LATEST";
    protected String spongeForgeBuild = "LATEST";
    protected String forgeServerFolder = "run" + File.separator + "forge";
    protected String vanillaServerFolder = "run" + File.separator + "vanilla";
    protected String extraProgramParameters;
    /**
     * Automatically accept the eula upon generation. Can be done with the task acceptEula,
     * but still you typed it so you kinda accepted it right?
     */
    protected boolean eula = false;

    public SpongeStartExtension(SpongeStart spongeStart) {
        this.spongeStart = spongeStart;
    }

    public String getSpongeForgeBuild() {
        return this.spongeForgeBuild;
    }

    public String getSpongeVanillaBuild() {
        return this.spongeVanillaBuild;
    }

    public void setSpongeForgeBuild(String spongeForgeBuild) {
        this.spongeForgeBuild = spongeForgeBuild;
    }

    public void setSpongeVanillaBuild(String spongeVanillaBuild) {
        this.spongeVanillaBuild = spongeVanillaBuild;
    }

    public String getForgeServerFolder() {
        return this.forgeServerFolder;
    }

    public String getVanillaServerFolder() {
        return this.vanillaServerFolder;
    }

    public void setForgeServerFolder(String forgeServerFolder) {
        this.forgeServerFolder = forgeServerFolder;
    }

    public void setVanillaServerFolder(String vanillaServerFolder) {
        this.vanillaServerFolder = vanillaServerFolder;
    }

    public void setEula(boolean eula) {
        this.eula = eula;
    }

    public boolean isEula() {
        return this.eula;
    }

    public String getExtraProgramParameters() {
        return extraProgramParameters == null ? "" : extraProgramParameters;
    }

    public void setExtraProgramParameters(String extraProgramParameters) {
        this.extraProgramParameters = extraProgramParameters;
    }
}
