package com.thomas15v.spongestart.maven;


import com.thomas15v.spongestart.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.net.*;
import java.util.zip.GZIPInputStream;

public class BuildNumberRepo {

    private URL url;
    private String artifactname;
    private String fileextension = ".jar";


    public BuildNumberRepo(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public BuildNumberRepo(URL url){
        this.url = url;
        this.artifactname = Util.getFileName(url);
    }

    public void setFileExtension(String fileextension) {
        this.fileextension = fileextension;
    }

    private Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        URLConnection connection = new URL(url, "maven-metadata.xml").openConnection();

        InputSource inputSource;
        //because sponge webserver encrypts everything, even if you don't support it.
        if ("gzip".equals(connection.getContentEncoding())){
            inputSource = new InputSource(new GZIPInputStream(connection.getInputStream()));
        } else {
            inputSource = new InputSource(connection.getInputStream());
        }

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        return b.parse(inputSource);
    }

    private URL formatForVersion(String version) throws MalformedURLException {
        return new URL(url, version + "/" + artifactname + "-" + version + fileextension);
    }

    public URL getFor(int number) throws Exception {
        NodeList nodeList = getDocument().getElementsByTagName("version");
        for (int i = 0; i < nodeList.getLength(); i++){
            String version = nodeList.item(i).getTextContent();
            if (version.endsWith(String.valueOf(number))){
                return formatForVersion(version);
            }
        }
        return null;
    }

    public URL getLatest() throws Exception {
        return formatForVersion(getDocument().getElementsByTagName("release").item(0).getTextContent());
    }
}
