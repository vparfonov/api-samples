/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.myorg.myeditor;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.TopComponent;

@TopComponent.Description(preferredID = "MyEditorTopComponent",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MyEditorAction")
@ActionID(category = "Window", id = "org.myorg.myviewer.MyEditorTopComponent")
@ActionReference(path = "Menu/Window")
public class MyEditor extends TopComponent implements ExplorerManager.Provider {

    private final ExplorerManager mgr = new ExplorerManager();

    public MyEditor() {
        initComponents();
        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
        mgr.setRootContext(new EventNode());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
