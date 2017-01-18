package com.qixalite.spongestart.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.utils.URIBuilder;
import org.gradle.api.GradleException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.qixalite.spongestart.Constants.*;

public class SpongeDownloadTask extends DownloadTask {

    public final static Map<String, String> types = new HashMap<>();


    private String type = "";
    private String minecraft = "";
    private Platform platform;
    private String artifactType = "";
    private String forge;

    private String forgebuild;

    static {
        //lets make this morron proof
        types.put("latest", "stable");
        types.put("stable", "stable");
        types.put("bleeding", "bleeding");
        types.put("expiremental", "bleeding");
    }

    public enum Platform{
        VANILLA("spongevanilla"),
        FORGE("spongeforge");

        private String name;

        Platform(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMinecraft(String minecraft) {
        this.minecraft = minecraft;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getMinecraft() {
        return minecraft;
    }

    public String getForge() {
        return forge;
    }

    private String getDownloadUrl(Platform platform, String minecraft, String type) throws IOException {
        type = types.get(type.toLowerCase());
        try {
            URIBuilder builder = new URIBuilder();
            URL url = builder.setScheme("https").setHost(SPONGE_DL_HOST).
                    setPath(String.format(SPONGE_PATH, platform.getName())).
                    setParameter("changelog", "false").setParameter("minecraft", minecraft).setParameter("type", type)
                    .build().toURL();
            URLConnection connection = url.openConnection();
            InputSource inputSource;

            //because sponge webserver encrypts everything, even if you don't support it.
            if ("gzip".equals(connection.getContentEncoding())){
                inputSource = new InputSource(new GZIPInputStream(connection.getInputStream()));
            } else {
                inputSource = new InputSource(connection.getInputStream());
            }

            Reader reader = new InputStreamReader(inputSource.getByteStream());

            JsonArray versions = new JsonParser().parse(reader).getAsJsonArray();
            JsonObject data = versions.get(0).getAsJsonObject();
            JsonObject dependencies = data.get("dependencies").getAsJsonObject();
            if (dependencies.has("forge")){
                this.forge = dependencies.get("forge").getAsString();
            }
            System.out.println(dependencies);
            if (dependencies.has("minecraft")){
                this.minecraft = dependencies.get("minecraft").getAsString();
            }
            try {
                return data.get("artifacts")
                        .getAsJsonObject().get(artifactType).getAsJsonObject().get("url").getAsString();
            }catch (IndexOutOfBoundsException ignored){}

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setVersion(String version){
        if (version == null){
            return;
        }
        if (version.contains("-")){
            String[] versionData = version.split("-");
            System.out.println(versionData.length);
            if (platform == Platform.FORGE && versionData.length == 5 ){
                minecraft = versionData[0];
                forgebuild = versionData[1];
                return;
            } else if (platform == Platform.VANILLA && versionData.length == 4){
                minecraft = versionData[0];
                try {
                    setUrl(new URL(SPONGE_REPO + platform.getName() + "/" + version + "/" + platform.getName() + "-" + version + ".jar"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        throw new GradleException(version + ": Is definally not a valid version string");
    }

    @Override
    public void doStuff() {
        try {
            if (forgebuild != null){
                forge = getForgeFor(forgebuild);
                System.out.println(forge);
            }
            if (getUrl() == null) {
                setUrl(new URL(getDownloadUrl(platform, minecraft, type)));
            }
            super.doStuff();
        } catch (Exception e) {
            throw new GradleException(e.getMessage(), e);
        }
    }


    private Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        URLConnection connection = new URL(FORGE_REPO + "maven-metadata.xml").openConnection();

        InputSource inputSource;
        //because sponge webserver encrypts everything, even if you don't support it.
        if ("gzip".equals(connection.getContentEncoding())){
            inputSource = new InputSource(new GZIPInputStream(connection.getInputStream()));
        } else {
            inputSource = new InputSource(connection.getInputStream());
        }

        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
    }


    public String getForgeFor(String buildNumber) throws Exception {
        NodeList nodeList = this.getDocument().getElementsByTagName("version");
        for (int i = 0; i < nodeList.getLength(); i++){
            String item = nodeList.item(i).getTextContent();
            if (item.endsWith(buildNumber)){
                return item.split("-")[1];
            }
        }
        return null;
    }
}
