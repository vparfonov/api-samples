package org.abc.navigator;

import java.awt.BorderLayout;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@NavigatorPanel.Registration(mimeType = "text/x-abc", displayName = "Abc File Content")
public class AbcNavigatorPanel extends JPanel implements NavigatorPanel, LookupListener {

    JScrollPane pane;
    Lookup lookup;
    InstanceContent ic = new InstanceContent();

    public AbcNavigatorPanel() {
        setLayout(new BorderLayout());
        pane = new JScrollPane();
        add(pane, BorderLayout.CENTER);
        lookup = new AbstractLookup(ic);
        
    }
    Result<Scene> scenesInLookup;

    @Override
    public void panelActivated(Lookup lkp) {
        scenesInLookup = lkp.lookupResult(Scene.class);
        scenesInLookup.addLookupListener(this);
        resultChanged(new LookupEvent(scenesInLookup));
    }

    @Override
    public void panelDeactivated() {
        scenesInLookup.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent le) {
        if (!scenesInLookup.allInstances().isEmpty()) {
            Scene s = scenesInLookup.allInstances().iterator().next();
            if (s != null) {
                pane.setViewportView(s.createSatelliteView());
                ic.set(Collections.singleton(s), null);
            }
        } else {
            pane.setViewportView(new JLabel("<no file selected>"));
        }
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public String getDisplayName() {
        return "Abc File Content";
    }

    @Override
    public String getDisplayHint() {
        return "Abc File Content";
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}