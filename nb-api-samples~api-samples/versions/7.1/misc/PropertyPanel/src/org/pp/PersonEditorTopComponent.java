/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.pp//PersonEditor//EN",
autostore = false)
@TopComponent.Description(preferredID = "PersonEditorTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.pp.PersonEditorTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_PersonEditorAction",
preferredID = "PersonEditorTopComponent")
@Messages({
    "CTL_PersonEditorAction=PersonEditor",
    "CTL_PersonEditorTopComponent=PersonEditor Window",
    "HINT_PersonEditorTopComponent=This is a PersonEditor window"
})
public final class PersonEditorTopComponent extends TopComponent implements LookupListener {

    Result<Node> personResult;

    public PersonEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_PersonEditorTopComponent());
        setToolTipText(Bundle.HINT_PersonEditorTopComponent());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        if (personResult.allInstances().iterator().hasNext()) {
            Node personNode = personResult.allInstances().iterator().next();
            Property<?>[] properties = personNode.getPropertySets()[0].getProperties();
            for (Property prop : properties) {
                if (prop.getName().equals("name")) {
                    prop.setValue("oneline", Boolean.TRUE);
                    namePropertyPanel.setProperty(prop);
                } else if (prop.getName().equals("city")) {
                    cityPropertyPanel.setProperty(prop);
                } else if (prop.getName().equals("photo")) {
                    prop.setValue("directories", Boolean.FALSE);
                    photoPropertyPanel.setProperty(prop);
                } else if (prop.getName().equals("courses")) {
                    coursesPropertyPanel.setProperty(prop);
                }
            }
        }
    }

    @Override
    public void componentOpened() {
        personResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        personResult.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        personResult.removeLookupListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        propertyPanel1 = new org.openide.explorer.propertysheet.PropertyPanel();
        namePropertyPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cityPropertyPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        photoPropertyPanel = new org.openide.explorer.propertysheet.PropertyPanel();
        jLabel4 = new javax.swing.JLabel();
        coursesPropertyPanel = new org.openide.explorer.propertysheet.PropertyPanel();

        javax.swing.GroupLayout propertyPanel1Layout = new javax.swing.GroupLayout(propertyPanel1);
        propertyPanel1.setLayout(propertyPanel1Layout);
        propertyPanel1Layout.setHorizontalGroup(
            propertyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 149, Short.MAX_VALUE)
        );
        propertyPanel1Layout.setVerticalGroup(
            propertyPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout namePropertyPanelLayout = new javax.swing.GroupLayout(namePropertyPanel);
        namePropertyPanel.setLayout(namePropertyPanelLayout);
        namePropertyPanelLayout.setHorizontalGroup(
            namePropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 246, Short.MAX_VALUE)
        );
        namePropertyPanelLayout.setVerticalGroup(
            namePropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PersonEditorTopComponent.class, "PersonEditorTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PersonEditorTopComponent.class, "PersonEditorTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PersonEditorTopComponent.class, "PersonEditorTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout cityPropertyPanelLayout = new javax.swing.GroupLayout(cityPropertyPanel);
        cityPropertyPanel.setLayout(cityPropertyPanelLayout);
        cityPropertyPanelLayout.setHorizontalGroup(
            cityPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        cityPropertyPanelLayout.setVerticalGroup(
            cityPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout photoPropertyPanelLayout = new javax.swing.GroupLayout(photoPropertyPanel);
        photoPropertyPanel.setLayout(photoPropertyPanelLayout);
        photoPropertyPanelLayout.setHorizontalGroup(
            photoPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        photoPropertyPanelLayout.setVerticalGroup(
            photoPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PersonEditorTopComponent.class, "PersonEditorTopComponent.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout coursesPropertyPanelLayout = new javax.swing.GroupLayout(coursesPropertyPanel);
        coursesPropertyPanel.setLayout(coursesPropertyPanelLayout);
        coursesPropertyPanelLayout.setHorizontalGroup(
            coursesPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        coursesPropertyPanelLayout.setVerticalGroup(
            coursesPropertyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(namePropertyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cityPropertyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(photoPropertyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(coursesPropertyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(namePropertyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cityPropertyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(photoPropertyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(coursesPropertyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.propertysheet.PropertyPanel cityPropertyPanel;
    private org.openide.explorer.propertysheet.PropertyPanel coursesPropertyPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private org.openide.explorer.propertysheet.PropertyPanel namePropertyPanel;
    private org.openide.explorer.propertysheet.PropertyPanel photoPropertyPanel;
    private org.openide.explorer.propertysheet.PropertyPanel propertyPanel1;
    // End of variables declaration//GEN-END:variables

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
}
