/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.person.viewer;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.Collection;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.person.domain.Person;
import org.person.domain.capabilities.Synchronizable;
import org.person.model.PersonChildFactory;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.person.viewer//PersonViewer//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "PersonViewerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "org.person.viewer.PersonViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PersonViewerAction",
        preferredID = "PersonViewerTopComponent")
@Messages({
    "CTL_PersonViewerAction=PersonViewer",
    "CTL_PersonViewerTopComponent=PersonViewer Window",
    "HINT_PersonViewerTopComponent=This is a PersonViewer window"
})
public final class PersonViewerTopComponent extends TopComponent implements LookupListener, ExplorerManager.Provider {

    private ExplorerManager em = new ExplorerManager();

    public PersonViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_PersonViewerTopComponent());
        setToolTipText(Bundle.HINT_PersonViewerTopComponent());
        setLayout(new BorderLayout());
        BeanTreeView btv = new BeanTreeView();
        btv.setRootVisible(false);
        add(btv, BorderLayout.CENTER);
        em.setRootContext(new AbstractNode(Children.create(new PersonChildFactory(), true)));
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    Lookup.Result<Person> personResult;

    @Override
    public void componentOpened() {
        personResult = Utilities.actionsGlobalContext().lookupResult(Person.class);
        personResult.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        personResult.removeLookupListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends Person> p = personResult.allInstances();
        if (p.size() == 1) {
            Person currentPerson = p.iterator().next();
            for (Node node : em.getRootContext().getChildren().getNodes()) {
                if (node.getLookup().lookup(Synchronizable.class).getPerson() == currentPerson) {
                    try {
                        em.setSelectedNodes(new Node[]{node});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}