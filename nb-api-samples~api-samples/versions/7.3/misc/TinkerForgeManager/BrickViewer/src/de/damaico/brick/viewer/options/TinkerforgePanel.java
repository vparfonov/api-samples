/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.damaico.brick.viewer.options;

import org.openide.util.NbPreferences;

public final class TinkerforgePanel extends javax.swing.JPanel
{

    private final TinkerforgeOptionsPanelController controller;

    public TinkerforgePanel(TinkerforgeOptionsPanelController controller)
    {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        port = new javax.swing.JSlider();
        host = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TinkerforgePanel.class, "TinkerforgePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TinkerforgePanel.class, "TinkerforgePanel.jLabel2.text")); // NOI18N

        port.setMaximum(4230);
        port.setMinimum(4220);
        port.setPaintLabels(true);
        port.setValue(4223);

        host.setText(org.openide.util.NbBundle.getMessage(TinkerforgePanel.class, "TinkerforgePanel.host.text")); // NOI18N
        host.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                hostActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(host))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hostActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_hostActionPerformed
    {//GEN-HEADEREND:event_hostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostActionPerformed

    void load()
    {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(TinkerforgePanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
         host.setText(NbPreferences.forModule(TinkerforgePanel.class).get("hostProperty", "localhost"));
         port.setValue(NbPreferences.forModule(TinkerforgePanel.class).getInt("portProperty", 4223));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    void store()
    {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(TinkerforgePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        NbPreferences.forModule(TinkerforgePanel.class).put("hostProperty", host.getText());
        NbPreferences.forModule(TinkerforgePanel.class).putInt("portProperty", port.getValue());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
    }

    boolean valid()
    {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField host;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSlider port;
    // End of variables declaration//GEN-END:variables
}
