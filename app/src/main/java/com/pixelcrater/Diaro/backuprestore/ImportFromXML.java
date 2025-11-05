package com.pixelcrater.Diaro.backuprestore;

import com.pixelcrater.Diaro.utils.AppLog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ImportFromXML {

    public ImportFromXML(String xmlFilePath) throws Exception {
        AppLog.d("xmlFilePath: " + xmlFilePath);

        // Parse XML document
        File xmlFile = new File(xmlFilePath);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlParser = dbf.newDocumentBuilder();
        Document doc = xmlParser.parse(xmlFile);

        // Check xml file structure
        Node rootNode = doc.getFirstChild();
        String rootNodeName = rootNode.getNodeName();

        if (rootNodeName.equals("database")) {
            // v1
            new ImportFromXMLv1(doc);
        } else if (rootNodeName.equals("data")) {
            NodeList nodeList = doc.getElementsByTagName("data");
            Element element = (Element) nodeList.item(0);

            String version = element.getAttribute("version");
            AppLog.d("version: " + version);

            if (version.equals("2")) {
                // v2
                new ImportFromXMLv2(doc);
            }
        }
    }
}
