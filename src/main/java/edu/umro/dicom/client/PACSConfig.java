package edu.umro.dicom.client;

/*
 * Copyright 2012 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import edu.umro.util.Log;
import edu.umro.util.Utility;
import edu.umro.util.XML;

/**
 * Get the PACS configuration information.
 * 
 * @author Jim Irrer irrer@umich.edu
 * 
 */
public class PACSConfig {

    /** Name of configuration file. */
    private static final String CONFIG_FILE_NAME = "PACSConfig.xml";

    /** List of all possible configuration files. */
    private static final String[] CONFIG_FILE_LIST = {
            System.getProperty("pacsconfig"),
            CONFIG_FILE_NAME,
            "src\\main\\resources\\" + CONFIG_FILE_NAME
    };

    /** Instance of this object. */
    private volatile static PACSConfig pacsConfig = null;

    private PACS identity = null;

    private ArrayList<PACS> pacsList = null;

    /**
     * Read in the configuration for the client from the configuration file. Try
     * all files on the list and use the first one that parses.
     */
    private void parseConfigFile() {
        Document config = null;
        for (String configFileName : CONFIG_FILE_LIST) {
            try {
                Log.get().info("Trying configuration file " + (new File(configFileName)).getAbsolutePath());
                config = XML.parseToDocument(Utility.readFile(new File(configFileName)));
                
                identity = new PACS(XML.getSingleNode(config, "/PacsConfiguration/Identity/PACS"));
                
                pacsList = new ArrayList<PACS>();
                NodeList nodeList = XML.getMultipleNodes(config, "/PacsConfiguration/PacsList/PACS");
                for (int n = 0; n < nodeList.getLength(); n++) {
                    pacsList.add(new PACS(nodeList.item(n)));
                }

            }
            catch (Exception e) {
                ;
            }
            if (config != null) {
                Log.get().info("Using configuration file " + (new File(configFileName)).getAbsolutePath());
                break;
            }
        }
        if (config == null) {
            Log.get().severe("Unable to read and parse any configuration file of: " + CONFIG_FILE_LIST);
        }
    }

    /**
     * Construct a configuration object.
     */
    public PACSConfig() {
        parseConfigFile();
    }

    /**
     * Get the information that indicates how this program identifies itself to other PACS devices
     * 
     * @return Identity of this DICOM program.
     */
    public PACS getIdentity() {
        return identity;
    }

    /**
     * Get the list of PACS we know about.
     * 
     * @return The list of PACS we know about
     */
    public ArrayList<PACS> getPacsList() {
        return pacsList;
    }

    /**
     * Get the common instance of this configuration.
     * 
     * @return This configuration.
     */
    public static PACSConfig getInstance() {
        if (pacsConfig == null) {
            pacsConfig = new PACSConfig();
        }
        return pacsConfig;
    }

    /**
     * Force the PACS configuration to be refreshed from the file.
     * 
     * @return The new PACS configuration.
     */
    public static PACSConfig refresh() {
        pacsConfig = new PACSConfig();
        return pacsConfig;
    }
}
