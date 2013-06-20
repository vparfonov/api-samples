/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.benchmark.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

public class BenchmarkGeneratorWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BenchmarkGeneratorVisualPanel2 component;
    private String fileLoc;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BenchmarkGeneratorVisualPanel2 getComponent() {
        if (component == null) {
            component = new BenchmarkGeneratorVisualPanel2();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        fileLoc = wiz.getProperty("fileLoc").toString();
        try {
            getComponent().setfileLoc(fileLoc);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        List<String> selectedProps = getComponent().getSelectedProps();
        for (int i = 0; i < selectedProps.size(); i++) {
            String string = selectedProps.get(i);
            wiz.putProperty("prop" + i, string);
        }
    }
    
}
