/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skydata.desktop;

import java.io.File;
import java.util.Date;
import net.contentobjects.jnotify.JNotifyListener;
import skydata.core.FolderTree.Diff;
import skydata.core.FolderTree.FileNode;
import skydata.core.FolderTree.FolderNode;
import skydata.core.threads.SyncQueue;

/**
 *
 * @author Red
 */
class Listener implements JNotifyListener {
    
    SyncQueue sync;
    
    public Listener(SyncQueue s) {
        super();
        sync = s;
    }

    private void addDiff(String s, boolean remove) {
        File f = new File(s);
        if (!f.getName().endsWith("-conflict")) {
            Diff d;
            if(f.isFile()) {
                FileNode n = new FileNode(new Date(f.lastModified()), f);
                if (remove) d = new Diff(Diff.REMOVE, n);
                else d = new Diff(Diff.ADD, n);
            } else {
                FolderNode n = new FolderNode(new Date(f.lastModified()), f);
                if (remove) d = new Diff(Diff.REMOVE, n);
                else d = new Diff(Diff.ADD, n);
            }
            sync.addDiff(d);
        }
    }

    public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
        addDiff(rootPath+File.separator+oldName, true);
        addDiff(rootPath+File.separator+newName, false);
        //System.out.println("rename: " + rootPath + oldName);
    }

    public void fileModified(int wd, String rootPath, String name) {
        addDiff(rootPath+File.separator+name, false);
        //System.out.println("modify: " + rootPath + name);
    }

    public void fileDeleted(int wd, String rootPath, String name) {
        addDiff(rootPath+File.separator+name, true);
        //System.out.println("delete: " + rootPath + name);
    }

    public void fileCreated(int wd, String rootPath, String name) {
        addDiff(rootPath+File.separator+name, false);
        //System.out.println("create: " + rootPath + name);
    }

    void print(String msg) {
        System.err.println(msg);
    }
}

