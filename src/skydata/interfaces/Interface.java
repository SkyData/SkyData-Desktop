/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skydata.interfaces;

import java.util.ArrayList;
import java.util.TreeMap;
import skydata.core.FolderTree.RootFolder;
import skydata.core.threads.SyncQueue;
import skydata.desktop.Main;

/**
 *
 * @author ivan
 */
public class Interface {

    public static void createListeners(ArrayList<RootFolder> folders, ArrayList<SyncQueue> queues) {
        Main.createListeners(folders, queues);
    }

    public static void removeListeners() {
        Main.removeListeners();
    }

    public static void logIn() {
        Main.logIn();
    }

    @Deprecated
    public static void dirListCallback(ArrayList<TreeMap<String,Object>> list) {
        Main.dirListCallback(list);
    }
    
    public static void dirListChanged() {
        Main.dirListChanged();
    }

    public static void loadTrustStore() {
        System.setProperty("javax.net.ssl.trustStore", "mytruststore"); 
        System.setProperty("javax.net.ssl.trustStorePassword", "skypass");
    }

}
