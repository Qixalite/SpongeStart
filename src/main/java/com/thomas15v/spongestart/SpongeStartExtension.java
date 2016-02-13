package com.thomas15v.spongestart;

import java.io.File;

public class SpongeStartExtension {

    private final transient SpongeStart spongeStart;
    protected String spongeVanillaBuild = "LATEST";
    protected String spongeForgeBuild = "LATEST";

    protected String forgeserverFolder = "run" + File.separator + "forge";
    protected String VanillaserverFolder = "run" + File.separator + "vanilla";

    public SpongeStartExtension(SpongeStart spongeStart) {
        this.spongeStart = spongeStart;
    }

    public String getSpongeForgeBuild() {
        return spongeForgeBuild;
    }

    public String getSpongeVanillaBuild() {
        return spongeVanillaBuild;
    }

    public void setSpongeForgeBuild(String spongeForgeBuild) {
        this.spongeForgeBuild = spongeForgeBuild;
    }

    public void setSpongeVanillaBuild(String spongeVanillaBuild) {
        this.spongeVanillaBuild = spongeVanillaBuild;
    }


    public String getForgeserverFolder() {
        return forgeserverFolder;
    }

    public String getVanillaserverFolder() {
        return VanillaserverFolder;
    }

    public void setForgeserverFolder(String forgeserverFolder) {
        this.forgeserverFolder = forgeserverFolder;
    }

    public void setVanillaserverFolder(String vanillaserverFolder) {
        VanillaserverFolder = vanillaserverFolder;
    }
}
