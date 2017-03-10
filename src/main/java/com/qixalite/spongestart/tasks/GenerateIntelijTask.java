package com.qixalite.spongestart.tasks;

import com.google.common.io.Resources;
import com.qixalite.spongestart.SpongeStart;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.*;

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

public class GenerateIntelijTask extends SpongeStartTask {

    private String runoption = "";
    private String workingdir = "";

    private String taskname = "";
    private String modulename = "";

    @TaskAction
    public void doStuff(){
        try {
            Map<String, String> configs = new HashMap<>();
            configs.put("MAIN_CLASS_NAME", "StartServer");
            configs.put("WORKING_DIRECTORY", "$PROJECT_DIR$/" + workingdir);
            configs.put("PROGRAM_PARAMETERS", runoption);
            generateConfigAlternative(Paths.get(".idea/workspace.xml"), configs);
            /*
            if (Files.exists(Paths.get(".idea/runConfigurations/"))) {
                generateConfig(Paths.get(".idea/runConfigurations/" + taskname + ".xml"), configs);
            } else {

            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateConfig(Path file, Map<String,String> params) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                parse(this.getClass().getClassLoader().getResourceAsStream("runconfig.xml"));
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

    private void generateConfigAlternative(Path file, Map<String,String> params) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                parse(file.toString());
        Node run = null;
        NodeList component = document.getElementsByTagName("component");
        for (int i = 0; i < component.getLength(); i++) {
            if (component.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("RunManager")) {
                run = component.item(i);
                break;
            }
        }

        Element configuration = document.createElement("configuration");
        configuration.setAttribute("default", "false");
        configuration.setAttribute("name", taskname );
        configuration.setAttribute("type", "Application");
        configuration.setAttribute("factoryName", "Application");

        Element extension = document.createElement("extension");
        extension.setAttribute("name", "coverage");
        extension.setAttribute("enabled", "false");
        extension.setAttribute("merge", "false");
        extension.setAttribute("sample_coverage", "true");
        extension.setAttribute("runner", "idea");

        Element mainName = document.createElement("option");
        mainName.setAttribute("name", "MAIN_CLASS_NAME");
        mainName.setAttribute("value", "StartServer");

        Element workingDir = document.createElement("option");
        workingDir.setAttribute("name", "WORKING_DIRECTORY");
        workingDir.setAttribute("value", "file://$PROJECT_DIR$/"+workingdir);

        Element moduleName = document.createElement("module");
        moduleName.setAttribute("name", modulename);


        configuration.appendChild(extension);
        configuration.appendChild(mainName);
        configuration.appendChild(workingDir);
        configuration.appendChild(moduleName);

        run.appendChild(configuration);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        StreamResult result = new StreamResult(new File(file.toString()));
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
