package com.qixalite.spongestart.tasks;

import com.qixalite.spongestart.SpongeStart;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class GenerateIntelijTask extends DefaultTask {

    private String runoption = "";
    private String workingdir = "";

    private String taskname = "";
    private String modulename = "";

    @TaskAction
    public void doStuff(){
        try {
            String clazz = new JarFile(new File(workingdir + "/server.jar")).getManifest().getMainAttributes().getValue("Main-Class");
            Map<String, String> configs = new HashMap<>();
            configs.put("MAIN_CLASS_NAME", clazz);
            configs.put("WORKING_DIRECTORY", "$PROJECT_DIR$/" + workingdir);
            configs.put("PROGRAM_PARAMETERS", runoption);
            generateConfig(Paths.get(".idea/runConfigurations/" + taskname + ".xml"), configs );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateConfig(Path file, Map<String,String> params) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getClass().getClassLoader().getResourceAsStream("runconfig.xml"));
        NodeList nodeList = document.getElementsByTagName("option");
        for (int i = 0; i < nodeList.getLength(); i++) {
            NamedNodeMap attr = nodeList.item(i).getAttributes();
            params.forEach((key, value) -> {
                if (attr.getNamedItem("name").getNodeValue().equals(key)){
                    attr.getNamedItem("value").setNodeValue(value);
                }
            });
        }
        document.getElementsByTagName("module").item(0).getAttributes().getNamedItem("name").setNodeValue(modulename);
        document.getElementsByTagName("configuration").item(0).getAttributes().
                getNamedItem("name").setNodeValue(taskname);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        if (Files.exists(file)) {
            Files.delete(file);
        }
        Files.createDirectories(file.getParent());
        Files.createFile(file);

        Result result = new StreamResult(Files.newOutputStream(file));
        transformer.transform(new DOMSource(document), result);
    }

    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    public void setRunoption(String runoption) {
        this.runoption = runoption;
    }

    public void setWorkingdir(String workingdir) {
        this.workingdir = workingdir;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }
}
