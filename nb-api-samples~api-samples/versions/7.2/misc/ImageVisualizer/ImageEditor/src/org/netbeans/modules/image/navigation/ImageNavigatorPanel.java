/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.image.navigation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.image.ImageDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;

/**
 *
 * @author jpeska
 */
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType="image/png", displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType="image/jpeg", displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType="image/bmp", displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType="image/gif", displayName="#Navigator_DisplayName")
})
public class ImageNavigatorPanel implements NavigatorPanel {

    /**
     * holds UI of this panel
     */
    private ImagePreviewPanel panelUI;
    /**
     * template for finding data in given context. Object used as example,
     * replace with your own data source, for example JavaDataObject etc
     */
    private static final Lookup.Template MY_DATA = new Lookup.Template(ImageDataObject.class);
    /**
     * current context to work on
     */
    private Lookup.Result currentContext;
    /**
     * listener to context changes
     */
    private LookupListener contextListener;
    /**
     * Listens for changes on image file.
     */
    private FileChangeListener fileChangeListener;
    private long lastSaveTime = -1;
    private DataObject currentDataObject;
    private static final RequestProcessor WORKER = new RequestProcessor(ImageNavigatorPanel.class.getName());

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImageNavigatorPanel.class, "Navigator_DisplayName");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(ImageNavigatorPanel.class, "Navigator_DisplayHint");
    }

    @Override
    public JComponent getComponent() {
        if (lastSaveTime == -1) {
            lastSaveTime = System.currentTimeMillis();
        }
        if (panelUI == null) {
            panelUI = new ImagePreviewPanel();
        }
        return panelUI;
    }

    @Override
    public void panelActivated(Lookup context) {
        // lookup context and listen to result to get notified about context changes
        currentContext = context.lookup(MY_DATA);
        currentContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection data = currentContext.allInstances();
        currentDataObject = getDataObject(data);
        if (currentDataObject == null) {
            return;
        }
        if (fileChangeListener == null) {
            fileChangeListener = new ImageFileChangeAdapter();
        }
        currentDataObject.getPrimaryFile().addFileChangeListener(fileChangeListener);
        setNewContent(currentDataObject);
    }

    @Override
    public void panelDeactivated() {
        currentContext.removeLookupListener(getContextListener());
        currentContext = null;
        currentDataObject.getPrimaryFile().removeFileChangeListener(fileChangeListener);
        currentDataObject = null;
    }

    @Override
    public Lookup getLookup() {
        // go with default activated Node strategy
        return null;
    }

    private void setNewContent(final DataObject dataObject) {
        if (dataObject == null) {
            return;
        }

        WORKER.post(new Runnable() {

            @Override
            public void run() {
                InputStream inputStream;
                try {
                    FileObject fileObject = dataObject.getPrimaryFile();
                    if (fileObject == null) {
                        return;
                    }
                    inputStream = fileObject.getInputStream();
                    if (inputStream == null) {
                        return;
                    }
                    final BufferedImage image = ImageIO.read(inputStream);
                    if (panelUI == null) {
                        getComponent();
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            panelUI.setImage(image);                    
                        }
                    });
                    inputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImageNavigatorPanel.class.getName()).info(NbBundle.getMessage(ImageNavigatorPanel.class, "ERR_IOFile"));
                }
            }
        });

    }

    private DataObject getDataObject(Collection data) {
        DataObject dataObject = null;
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof DataObject) {
                dataObject = (DataObject) o;
                break;
            }
        }
        return dataObject;
    }

    /**
     * Accessor for listener to context
     */
    private LookupListener getContextListener() {
        if (contextListener == null) {
            contextListener = new ContextListener();
        }
        return contextListener;
    }

    /**
     * Listens to changes of context and triggers proper action
     */
    private class ContextListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            Collection data = ((Lookup.Result) ev.getSource()).allInstances();
            currentDataObject = getDataObject(data);
            setNewContent(currentDataObject);
        }
    }

    private class ImageFileChangeAdapter extends FileChangeAdapter {

        @Override
        public void fileChanged(final FileEvent fe) {
            if (fe.getTime() > lastSaveTime) {
                lastSaveTime = System.currentTimeMillis();

                // Refresh image viewer
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            currentDataObject = DataObject.find(fe.getFile());
                            setNewContent(currentDataObject);
                        } catch (DataObjectNotFoundException ex) {
                            Logger.getLogger(ImageNavigatorPanel.class.getName()).info(NbBundle.getMessage(ImageNavigatorPanel.class, "ERR_DataObject"));
                        }
                    }
                });
            }
        }
    }
}
