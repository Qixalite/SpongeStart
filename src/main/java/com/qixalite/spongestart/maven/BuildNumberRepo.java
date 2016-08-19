package com.qixalite.spongestart.maven;

import com.qixalite.spongestart.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class BuildNumberRepo {

    private URL url;
    private String artifactName;
    private String fileExtension = ".jar";


    public BuildNumberRepo(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public BuildNumberRepo(URL url){
        this.url = url;
        this.artifactName = Util.getFileName(url);
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    private Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        URLConnection connection = new URL(this.url, "maven-metadata.xml").openConnection();

        InputSource inputSource;
        //because sponge webserver encrypts everything, even if you don't support it.
        if ("gzip".equals(connection.getContentEncoding())){
            inputSource = new InputSource(new GZIPInputStream(connection.getInputStream()));
        } else {
            inputSource = new InputSource(connection.getInputStream());
        }

        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
    }

    private URL formatForVersion(String version) throws MalformedURLException {
        return new URL(this.url, version + "/" + this.artifactName + "-" + version + this.fileExtension);
    }

    public URL getFor(int number) throws Exception {
        NodeList nodeList = this.getDocument().getElementsByTagName("version");
        for (int i = 0; i < nodeList.getLength(); i++){
            String version = nodeList.item(i).getTextContent();
            if (version.contains(String.valueOf(number))){
                return this.formatForVersion(version);
            }
        }
        return getFor(number - 1);
    }

    public URL getLatest() throws Exception {
        return this.formatForVersion(this.getDocument().getElementsByTagName("release").item(0).getTextContent());
    }
}
