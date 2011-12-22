/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package skydata.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.contentobjects.jnotify.JNotifyException;
import skydata.core.FolderTree.RootFolder;
import java.util.TreeMap;
import net.contentobjects.jnotify.JNotify;
import skydata.core.FileScanner;
import skydata.core.FolderTree.Diff;
import skydata.core.FolderTree.FileNode;
import skydata.core.FolderTree.FolderNode;
import skydata.core.FolderTree.Node;
import skydata.core.FolderTree.TreeFunctions;
import skydata.core.LocalConf;
import skydata.core.MainThread;
import skydata.core.WebService.DirList;
import skydata.core.WebService.Download;
import skydata.core.WebService.Login;
import skydata.core.WebService.RemoteDir;
import skydata.core.WebService.TreeService;
import skydata.core.WebService.Upload;
import skydata.core.threads.SyncQueue;
import skydata.core.threads.SyncThread;

/**
 *
 * @author jose.maria.palacio
 */
public class Main {

    private static ArrayList<Integer> listenerIDs;
    private static LogIn login;
    private static UI window;
    public static LocalConf conf;

    public static void createListeners(ArrayList<RootFolder> folders, ArrayList<SyncQueue> queues) {
        int mask = JNotify.FILE_CREATED
                | JNotify.FILE_DELETED
                | JNotify.FILE_MODIFIED
                | JNotify.FILE_RENAMED
                | JNotify.TREAT_MOVE_AS_DELETE_CREATE;

        for (int i = 0; i < folders.size(); ++i) {
            String path = folders.get(i).getPath();
            try {
                Integer lis = null;
                lis = new Integer(JNotify.addWatch(path, mask, true, new Listener(queues.get(i))));
                listenerIDs.add(lis);
            } catch (JNotifyException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void dirListCallback(ArrayList<TreeMap<String,Object>> list) {
        //if (window != null) window.dirListCallback(list);
    }
    
    
    public static void dirListChanged() {
        if (window != null) window.dirListChanged();
    }
    
    public static void removeListeners() {
        for (int i = 0; i < listenerIDs.size(); ++i) {
            try {
                boolean res;
                res = JNotify.removeWatch(listenerIDs.get(i));
                if (!res) {
                    // invalid watch ID specified.
                }
            } catch (JNotifyException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void logIn() {
        if (Login.token == null) login.logerror();
        else {
            login.dispose();
            inicia();
        }
    }

    public static void inicia() {
        MainThread mt = new MainThread(conf);
        Thread th = new Thread(mt);
        th.start();
        if (window == null) window = new UI(conf, mt);
        else {
            window.setMt(mt);
            window.loginBack();
        }
        dirListChanged();
    }

    public static void startLogin() {
        login = new LogIn();
    }
  
    public static void main(String[] args) {
        //System.out.println(FileNode.md5(new File("./20111215_095229.jpg")));
        listenerIDs = new ArrayList<Integer>();
        conf = new LocalConf("./skydata.conf");
        
        if (!conf.getUser().equals("")) {
            Login.doLogin(conf.getUser(), conf.getPass());
        }

        if (Login.token == null) startLogin();
        else inicia();
    }

}
