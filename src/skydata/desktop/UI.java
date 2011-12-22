/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * UI.java
 *
 * Created on 25-nov-2011, 10:56:51
 */

package skydata.desktop;

import java.awt.FileDialog;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import skydata.core.LocalConf;
import skydata.core.MainThread;
import skydata.core.WebService.DirList;
import skydata.core.WebService.Login;

/**
 *
 * @author jose.maria.palacio
 */
public class UI extends javax.swing.JFrame {

    private LocalConf conf;
    private MainThread mthread;
    private boolean start;
    private boolean login;
    
    /** Creates new form UI */
    public UI(LocalConf conf1, MainThread mthread1) {
        initComponents();
        URL url = LogIn.class.getResource("icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        
        this.setLocationRelativeTo(getRootPane());
        start = true;
        login = true;
        conf = conf1;
        mthread = mthread1;
        this.setVisible(true);

        TableColumn hdr = jFolders.getTableHeader().getColumnModel().getColumn(2);
        int sizec = 50;
        hdr.setPreferredWidth(sizec);
        hdr.setMaxWidth(sizec);
        hdr.setMinWidth(sizec);
        hdr.setWidth(sizec);
        TableColumnModel cModel = jFolders.getColumnModel();
        cModel.getColumn(2).setMaxWidth(sizec);
        cModel.getColumn(2).setMinWidth(sizec);
        cModel.getColumn(2).setWidth(sizec);
        cModel.getColumn(2).setPreferredWidth(sizec);

        jFolders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable)e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                if (e.getClickCount() == 2) {
                    if (column == 1) clickPath(row);
                }
                else if (column == 2) clickSync(row);
            }
        });
    }

    public void setMt(MainThread mt) {
        mthread = mt;
    }

    private boolean correctPath(int row) {
        DefaultTableModel mod = ((DefaultTableModel)jFolders.getModel());
        String name = (String)mod.getValueAt(row, 0);
        String path = (String)mod.getValueAt(row, 1);
        String p[] = null;
        if (File.separator.equals("\\")) {
            Pattern pt = Pattern.compile("\\\\|\\/");
            p = pt.split(path);
        }
        else p = path.split(File.separator);
        return p.length > 0 && p[p.length-1].equals(name);
    }

    private void clickSync(int row) {
        DefaultTableModel mod = ((DefaultTableModel)jFolders.getModel());
        Boolean b = (Boolean) mod.getValueAt(row, 2);
        String name = (String)mod.getValueAt(row, 0);
        boolean correct = correctPath(row);
        if (b.booleanValue()) {
            if (correctPath(row)) mthread.addFolder((String)mod.getValueAt(row, 1));
            else {
                mod.setValueAt(false, row, 2);
                JOptionPane.showMessageDialog(this,"The local folder must have the same"
                 + " name as the remote folder", "Path error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else mthread.removeFolderByName(name);
        if (!b.booleanValue() || correct) {
            ArrayList<ArrayList<String>> folders = conf.getFolders();
            String res = "0";
            if (b.booleanValue()) res = "1";
            int i = 0;
            while(i < folders.size() && !folders.get(i).get(0).equals(name)) ++i;
            if (i < folders.size()) folders.get(i).set(2, res);
            conf.setFolders(folders);
        }
    }
    
    private void clickPath(int row) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.showOpenDialog(this);
        File selFile = fc.getSelectedFile();
        if (selFile != null){
            DefaultTableModel mod = ((DefaultTableModel)jFolders.getModel());
            ArrayList<ArrayList<String>> folders = conf.getFolders();
            String name = (String)mod.getValueAt(row, 0);
            int i = 0;
            while(i < folders.size() && !folders.get(i).get(0).equals(name)) ++i;
            if (i < folders.size()) folders.get(i).set(1, selFile.getPath());
            conf.setFolders(folders);
            dirListChanged();
        }
    }

    void dirListChanged() {
        ((DefaultTableModel)jFolders.getModel()).setNumRows(0);
        ArrayList<ArrayList<String>> folders = conf.getFolders();
        DefaultTableModel mod = ((DefaultTableModel)jFolders.getModel());
        for (int i = 0; i < folders.size(); ++i) {
            int j = 0;
            while (j < jFolders.getRowCount() && 
                   !folders.get(i).get(0).equals((String)mod.getValueAt(j, 0))) {
                    ++j;
            }
            if (j < jFolders.getRowCount()) {
                mod.setValueAt(folders.get(i).get(1), j, 1);
                Boolean b = folders.get(i).get(2).equals("1");
                mod.setValueAt(b, j, 2);
            }
            else {
                String fname = folders.get(i).get(0);
                String fpath = folders.get(i).get(1);
                Boolean b = folders.get(i).get(2).equals("1");
                ((DefaultTableModel)jFolders.getModel()).addRow(new Object[]{fname, fpath, b});
            }
        }
    }

    public void loginBack() {
        start = true;
        login = true;
        jLogout.setText("Logout");
        jStartStop.setText("Stop");
        jStartStop.setEnabled(true);
        jRefresh.setEnabled(true);
        jFolders.setEnabled(true);
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jFolders = new javax.swing.JTable();
        jRefresh = new javax.swing.JButton();
        jLogout = new javax.swing.JButton();
        jStartStop = new javax.swing.JButton();
        jExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SkyData v0.1.2");
        setMinimumSize(new java.awt.Dimension(334, 200));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                ExitEvent(evt);
            }
        });

        jFolders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Folder Name", "Local Path", "Sync"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jFolders.setMinimumSize(new java.awt.Dimension(200, 150));
        jFolders.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jFolders);
        jFolders.getColumnModel().getColumn(1).setResizable(false);
        jFolders.getColumnModel().getColumn(2).setResizable(false);

        jRefresh.setText("Refresh");
        jRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRefreshActionPerformed(evt);
            }
        });

        jLogout.setText("Logout");
        jLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLogoutActionPerformed(evt);
            }
        });

        jStartStop.setText("Stop");
        jStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartStopActionPerformed(evt);
            }
        });

        jExit.setText("Exit");
        jExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jStartStop, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 292, Short.MAX_VALUE)
                        .addComponent(jLogout)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExit, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartStop)
                    .addComponent(jRefresh)
                    .addComponent(jExit)
                    .addComponent(jLogout))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRefreshActionPerformed
        Thread th = new Thread(new DirList());
        th.start();
}//GEN-LAST:event_jRefreshActionPerformed

    private void jLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLogoutActionPerformed
        if (login) {
            if (mthread != null) {
                mthread.finish();
                mthread = null;
            }
            jStartStop.setText("Start");
            jLogout.setText("Login");
            jStartStop.setEnabled(false);
            jRefresh.setEnabled(false);
            jFolders.setEnabled(false);
            start = false;
            login = false;
            Login.token = null;
        }
        else {
            this.setVisible(false);
            Main.startLogin();
        }
}//GEN-LAST:event_jLogoutActionPerformed

    private void jStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartStopActionPerformed
        if (start) {
            mthread.finish();
            mthread = null;
            jStartStop.setText("Start");
            start = false;
        }
        else {
            Main.inicia();
            jStartStop.setText("Stop");
            start = true;
        }
    }//GEN-LAST:event_jStartStopActionPerformed

    private void jExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExitActionPerformed
        if (mthread != null) {
            mthread.finish();
            mthread = null;
        }
        System.exit(0);
    }//GEN-LAST:event_jExitActionPerformed

    private void ExitEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_ExitEvent
        if (mthread != null) {
            mthread.finish();
            mthread = null;
        }
        this.dispose();
    }//GEN-LAST:event_ExitEvent


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jExit;
    private javax.swing.JTable jFolders;
    private javax.swing.JButton jLogout;
    private javax.swing.JButton jRefresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jStartStop;
    // End of variables declaration//GEN-END:variables

}
