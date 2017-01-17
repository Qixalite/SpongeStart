package com.qixalite.spongestart.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.utils.URIBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.qixalite.spongestart.util.Constants.SPONGE_DL_HOST;
import static com.qixalite.spongestart.util.Constants.SPONGE_PATH;

public class SpongeDownloadTask extends DownloadTask {

    public final static Map<String, String> types = new HashMap<>();


    private String type = "";
    private String mincraft = "";
    private Platform platform;
    private String forge;

    static {
        //lets make this morron proof
        types.put("latest", "stable");
        types.put("stable", "stable");
        types.put("bleeding", "bleeding");
        types.put("expiremental", "bleeding");
    }

    public enum Platform{
        VANILLA("spongevanila"),
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

    public void setMincraft(String mincraft) {
        this.mincraft = mincraft;
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
            forge = data.get("dependencies").getAsJsonObject().get("forge").getAsString();
            try {
                return data.get("artifacts")
                        .getAsJsonObject().get("").getAsJsonObject().get("url").getAsString();
            }catch (IndexOutOfBoundsException ignored){}

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void doStuff() {
        super.doStuff();
    }
}
