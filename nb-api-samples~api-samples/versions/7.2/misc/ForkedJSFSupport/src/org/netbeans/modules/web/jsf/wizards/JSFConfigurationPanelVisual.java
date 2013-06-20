/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentProvider;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel.LibraryType;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel.PreferredLanguage;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.URLMapper;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Petr Pisl, Radko Najman, alexeybutenko, Martin Fousek
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {

    private static final RequestProcessor RP = new RequestProcessor("JSF worker", 1);
    private static final Logger LOG = Logger.getLogger(JSFConfigurationPanelVisual.class.getName());

    private static final String JSF_SERVLET_NAME="Faces Servlet";   //NOI18N
    private String jsfServletName=null;
    private JSFConfigurationPanel panel;
    private boolean customizer;

    private final List<LibraryItem> jsfLibraries = new ArrayList<LibraryItem>();
    //    private final List<JsfComponentDescriptor> componentsLibraries = new ArrayList<JsfComponentDescriptor>();
    private final Map<JSFVersion, List<JsfComponentImplementation>> componentsMap = new HashMap<JSFVersion, List<JsfComponentImplementation>>();
    /**
     * Do not modify it directly, use setJsfVersion method
     */
    private JSFVersion currentJSFVersion = null;
    private final Set<ServerLibraryItem> serverJsfLibraries = new TreeSet<ServerLibraryItem>();
    private volatile boolean libsInitialized;
    private volatile boolean jsfComponentsInitialized;
    private String serverInstanceID;
    private final List<PreferredLanguage> preferredLanguages = new ArrayList<PreferredLanguage>();
    private String currentServerInstanceID;
    private boolean isWebLogicServer = false;

    // Jsf component libraries related
    private JSFComponentsTableModel jsfComponentsTableModel;
    private TreeMap<String, JsfComponentCustomizer> jsfComponentCustomizers = new TreeMap<String, JsfComponentCustomizer>();

    private static final Collection<? extends JsfComponentProvider> jsfComponentProviders =
            new ArrayList<JsfComponentProvider>(Lookups.forPath(JsfComponentProvider.COMPONENTS_PATH).
            lookupResult(JsfComponentProvider.class).allInstances());

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /** Libraries excluded from panel's {@link #jsfLibraries}. Libraries offered as registered in the IDE. */
    private static final Set<String> EXCLUDE_FROM_REGISTERED_LIBS = new HashSet<String>(Arrays.asList(
            "javaee-web-api-6.0", //NOI18N
            "javaee-api-6.0", //NOI18N
            "jsp-compilation", //NOI18N
            "jsp-compilation-syscp", //NOI18N
            "javaee-api-5.0")); //NOI18N

    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, boolean customizer) {
        this.panel = panel;
        this.customizer = customizer;
        this.jsfComponentsTableModel = new JSFComponentsTableModel(customizer);

        initComponents();

        tURLPattern.getDocument().addDocumentListener(this);
        cbPackageJars.setVisible(false);

        // update JSF components list
        jsfComponentsTable.setModel(jsfComponentsTableModel);
        JsfComponentsTableCellRenderer renderer = new JsfComponentsTableCellRenderer();
        renderer.setBooleanRenderer(jsfComponentsTable.getDefaultRenderer(Boolean.class));
        renderer.setJButtonRenderer(new JTableButtonRenderer());
        jsfComponentsTable.setDefaultRenderer(JsfComponentImplementation.class, renderer);
        jsfComponentsTable.setDefaultRenderer(Boolean.class, renderer);
        jsfComponentsTable.setDefaultRenderer(JButton.class, renderer);
        jsfComponentsTable.addMouseListener(new JsfComponentsMouseListener());
        jsfComponentsTableModel.addTableModelListener(new JsfComponentsTableModelListener());
        initJsfComponentTableVisualProperties(jsfComponentsTable);


        panel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initLibraries();
        initJsfComponentsLibraries();

        // in customizer preselected included JSF library
        if (customizer) {
            preselectJsfLibrary();
        }

        if (customizer) {
            enableComponents(false);
        } else {
            updateLibrary();
        }
    }

    private void preselectJsfLibrary() {
        Runnable jsfLibararyUiSwitcher = new Runnable() {
            @Override
            public void run() {
                // searching in IDE registered JSF libraries
                Project project = FileOwnerQuery.getOwner(panel.getWebModule().getDocumentBase());
                ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
                ClassPath compileClassPath = cpp.findClassPath(panel.getWebModule().getDocumentBase(), ClassPath.COMPILE);
                compileClassPath.entries();
                for (ClassPath.Entry entry : compileClassPath.entries()) {
                    for (final LibraryItem jsfLibrary : jsfLibraries) {
                        try {
                            List<URI> cps = jsfLibrary.getLibrary().getURIContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                            for (URI uri : cps) {
                                if (entry.getRoot() != null
                                        && entry.getRoot().equals(URLMapper.findFileObject(uri.toURL()))) {
                                    Mutex.EVENT.readAccess(new Runnable() {
                                        @Override
                                        public void run() {
                                            rbRegisteredLibrary.setSelected(true);
                                            enableComponents(false);
                                            cbLibraries.setSelectedItem(jsfLibrary.getLibrary().getDisplayName());
                                        }
                                    });
                                    return;
                                }
                            }
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                // searching in server registered JSF libraries
                J2eeModuleProvider jmp = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
                Set<ServerLibraryDependency> deps = getServerDependencies(jmp);
                for (ServerLibraryDependency serverLibraryDependency : deps) {
                    if (serverLibraryDependency.getName().startsWith("jsf")) { //NOI18N
                        ServerLibraryItem candidate = null;
                        for (final ServerLibraryItem serverLibraryItem : serverJsfLibraries) {
                            if (serverLibraryItem.getLibrary() != null) {
                                Version implVersion = serverLibraryItem.getLibrary().getImplementationVersion();
                                Version specVersion = serverLibraryItem.getLibrary().getSpecificationVersion();
                                if ((implVersion != null && implVersion.equals(serverLibraryDependency.getImplementationVersion()))
                                        || specVersion != null && specVersion.equals(serverLibraryDependency.getSpecificationVersion())) {
                                    selectServerLibraryItem(serverLibraryItem);
                                    return;
                                }
                            } else {
                                // IDE didn't recognize library correctly
                                candidate = serverLibraryItem;
                            }
                        }
                        if (candidate != null) {
                            selectServerLibraryItem(candidate);
                        }
                    }
                }
            }

            private void selectServerLibraryItem(final ServerLibraryItem item) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        rbServerLibrary.setSelected(true);
                        enableComponents(false);
                        serverLibraries.setSelectedItem(item);
                    }
                });
            }
        };

        RP.post(jsfLibararyUiSwitcher);
    }

    private static Set<ServerLibraryDependency> getServerDependencies(J2eeModuleProvider jmp) {
        try {
            return jmp.getConfigSupport().getLibraries();
        } catch (ConfigurationException e) {
            return Collections.emptySet();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void initLibraries() {
        long time = System.currentTimeMillis();
        if (libsInitialized) {
            return;
        }

        // init server libraries first
        initServerLibraries(false);

        final Vector<String> registeredItems = new Vector<String>();
        jsfLibraries.clear();

        final Runnable libraryFinder = new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    long time = System.currentTimeMillis();
                    List<URL> content;
                    for (Library library : LibraryManager.getDefault().getLibraries()) {
                        if (!"j2se".equals(library.getType())) { // NOI18N
                            continue;
                        }
                        if (library.getName().startsWith("facelets-") && !library.getName().endsWith("el-api") //NOI18N
                        && !library.getName().endsWith("jsf-ri") && !library.getName().endsWith("myfaces")) { //NOI18N
                            String displayName = library.getDisplayName();
                            registeredItems.add(displayName);
                            //TODO XX Add correct version
                            jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_2));
                        }

                        content = library.getContent("classpath"); //NOI18N
                        try {
                            if (Util.containsClass(content, JSFUtils.FACES_EXCEPTION)
                                    && !EXCLUDE_FROM_REGISTERED_LIBS.contains(library.getName())) {
                                registeredItems.add(library.getDisplayName());
                                boolean isJSF12 = Util.containsClass(content, JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
                                boolean isJSF20 = Util.containsClass(content, JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);
                                boolean isJSF21 = Util.containsClass(content, JSFUtils.JSF_2_1__API_SPECIFIC_CLASS);
                                if (isJSF21) {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_2_1));
                                } else if (isJSF20) {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_2_0));
                                } else if (isJSF12) {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_2));
                                } else {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_1));
                                }
                            }
                        } catch (IOException exception) {
                            LOG.log(Level.INFO, "", exception);
                        }
                    }

                    // if maven, exclude user defined libraries
                    Properties properties = panel.getController().getProperties();
                    Boolean isMaven = (Boolean) properties.getProperty("maven");
                    if (isMaven != null && isMaven.booleanValue()) {
                        removeUserDefinedLibraries(registeredItems);
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setRegisteredLibraryModel(registeredItems);
                            updatePreferredLanguages();
                            updateJsfComponents();
                        }
                    });
                    LOG.log(Level.FINEST, "Time spent in initLibraries in Runnable = {0} ms", (System.currentTimeMillis()-time));  //NOI18N
                }
            }
        };
        RP.post(libraryFinder);

        libsInitialized = true;
//        repaint();
        LOG.log(Level.FINEST, "Time spent in {0} initLibraries = {1} ms", new Object[]{this.getClass().getName(), System.currentTimeMillis()-time});   //NOI18N
    }

    private void initJsfComponentsLibraries() {
        if (jsfComponentsInitialized)
            return;

        List<JsfComponentImplementation> jsfComponentDescriptors = new ArrayList<JsfComponentImplementation>();
        for (JsfComponentProvider provider: jsfComponentProviders) {
            jsfComponentDescriptors.addAll(provider.getJsfComponents());
        }

        for (JSFVersion jsfVersion : JSFVersion.values()) {
            List<JsfComponentImplementation> list = componentsMap.get(jsfVersion);
            if (list == null) {
                list = new ArrayList<JsfComponentImplementation>();
                componentsMap.put(jsfVersion, list);
            }
            for (JsfComponentImplementation jsfImplementation : jsfComponentDescriptors) {
                if (jsfImplementation.getJsfVersion().contains(jsfVersion)) {
                    list.add(jsfImplementation);
                }
            }
        }
        jsfComponentsInitialized = true;
        if (currentJSFVersion != null) {
            updateJsfComponentsModel(currentJSFVersion);
        }
    }

    private void removeUserDefinedLibraries(Vector<String> registeredItems) {
        for (LibraryItem item : jsfLibraries) {
            Map<String, String> properties = item.getLibrary().getProperties();
            if (!properties.containsKey("maven-dependencies") //NOI18N
                    || properties.get("maven-dependencies").trim().isEmpty()) { //NOI18N
                registeredItems.remove(item.getLibrary().getDisplayName());
            }
        }
    }

    private void initServerLibraries(boolean updateUI) {
        serverJsfLibraries.clear();

        synchronized (this) {
            Set<JSFVersion> found = new HashSet<JSFVersion>();
            if (isServerRegistered(serverInstanceID)) {
                try {
                    ServerInstance.LibraryManager libManager = Deployment.getDefault().getServerInstance(serverInstanceID).getLibraryManager();
                    if (libManager != null) {
                        Set<ServerLibrary> libs = new HashSet<ServerLibrary>();
                        libs.addAll(libManager.getDeployedLibraries());
                        libs.addAll(libManager.getDeployableLibraries());

                        for (ServerLibrary lib : libs) {
                            // FIXME optimize
                            if ("JavaServer Faces".equals(lib.getSpecificationTitle())) { // NOI18N
                                if (Version.fromJsr277NotationWithFallback("2.1").equals(lib.getSpecificationVersion())) { //NOI18N
                                    serverJsfLibraries.add(new ServerLibraryItem(lib, JSFVersion.JSF_2_1));
                                    found.add(JSFVersion.JSF_2_1);
                                } else if (Version.fromJsr277NotationWithFallback("2.0").equals(lib.getSpecificationVersion())) { // NOI18N
                                    serverJsfLibraries.add(new ServerLibraryItem(lib, JSFVersion.JSF_2_0));
                                    found.add(JSFVersion.JSF_2_0);
                                } else if (Version.fromJsr277NotationWithFallback("1.2").equals(lib.getSpecificationVersion())) { // NOI18N
                                    serverJsfLibraries.add(new ServerLibraryItem(lib, JSFVersion.JSF_1_2));
                                    found.add(JSFVersion.JSF_1_2);
                                } else if (Version.fromJsr277NotationWithFallback("1.1").equals(lib.getSpecificationVersion())) { // NOI18N
                                    serverJsfLibraries.add(new ServerLibraryItem(lib, JSFVersion.JSF_1_1));
                                    found.add(JSFVersion.JSF_1_1);
                                } else {
                                    LOG.log(Level.INFO, "Unknown JSF version {0}", lib.getSpecificationVersion());
                                }
                            }
                        }
                    }
                } catch (InstanceRemovedException ex) {
                    LOG.log(Level.INFO, null, ex);
                    // use the old way
                }
            }

            File[] cp;
            J2eePlatform platform = null;
            try {
                if (isServerRegistered(serverInstanceID)) { //NOI18N
                    platform = Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
                }
            } catch (InstanceRemovedException ex) {
                platform = null;
                LOG.log(Level.INFO, org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "SERVER_INSTANCE_REMOVED"), ex);
            }
            // j2eeplatform can be null, when the target server is not accessible.
            if (platform != null) {
                cp = platform.getClasspathEntries();
            } else {
                cp = new File[0];
            }

            try {
                // XXX: there should be a utility class for this:
                boolean isJSF = Util.containsClass(Arrays.asList(cp), JSFUtils.FACES_EXCEPTION);
                boolean isJSF12 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
                boolean isJSF20 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);
                boolean isJSF21 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_2_1__API_SPECIFIC_CLASS);

                JSFVersion jsfVersion = null;
                if (isJSF21) {
                    jsfVersion = JSFVersion.JSF_2_1;
                } else if (isJSF20) {
                    jsfVersion = JSFVersion.JSF_2_0;
                } else if (isJSF12) {
                    jsfVersion = JSFVersion.JSF_1_2;
                } else if (isJSF) {
                    jsfVersion = JSFVersion.JSF_1_1;
                }
                if (jsfVersion != null && !found.contains(jsfVersion)) {
                    serverJsfLibraries.add(new ServerLibraryItem(null, jsfVersion));
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "", ex);
            }

            setServerLibraryModel(serverJsfLibraries);
            if (updateUI) {
                //updatePreferredLanguages();
            }
        }
    }

    private static boolean isServerRegistered(String serverInstanceID) {
        if (serverInstanceID != null && !"".equals(serverInstanceID) && !"DEV-NULL".equals(serverInstanceID)) {
            return true;
        }
        return false;
    }

    private void setRegisteredLibraryModel(Vector<String> items) {
        long time = System.currentTimeMillis();
        cbLibraries.setModel(new DefaultComboBoxModel(items));
        if (items.size() == 0) {
            rbRegisteredLibrary.setEnabled(false);
            cbLibraries.setEnabled(false);
            rbNewLibrary.setSelected(true);
            panel.setLibrary((Library) null);
        } else if (items.size() != 0 &&  panel.getLibraryType() == LibraryType.USED){
            if (!customizer) {
                rbRegisteredLibrary.setEnabled(true);
                cbLibraries.setEnabled(true);
            }
            rbRegisteredLibrary.setSelected(true);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
                setJsfVersion(jsfLibraries.get(cbLibraries.getSelectedIndex()).getVersion());
            }
        }

//        libsInitialized = true;
        repaint();
        LOG.finest("Time spent in "+this.getClass().getName() +" setLibraryModel = "+(System.currentTimeMillis()-time) +" ms");   //NOI18N
    }

    private void setServerLibraryModel(Collection<ServerLibraryItem> items) {
        serverLibraries.setModel(new DefaultComboBoxModel(items.toArray()));
        if (items.isEmpty()) {
            rbServerLibrary.setEnabled(false);
            serverLibraries.setEnabled(false);
            rbRegisteredLibrary.setSelected(true);
            panel.setServerLibrary((ServerLibrary) null);
        } else if (!items.isEmpty() && panel.getLibraryType() == LibraryType.SERVER){
            if (!customizer) {
                rbServerLibrary.setEnabled(true);
                serverLibraries.setEnabled(true);
            }
            rbServerLibrary.setSelected(true);
            if (!serverJsfLibraries.isEmpty()) {
                ServerLibraryItem item = (ServerLibraryItem) serverLibraries.getSelectedItem();
                if (item != null) {
                    panel.setServerLibrary(item.getLibrary());
                    setJsfVersion(item.getVersion());
                }
            }
        }

        repaint();
    }

    /**
     * Init Preferred Languages check box with "JSP" and/or "Facelets"
     * according to choosen library
     */
    private void updatePreferredLanguages() {
        boolean faceletsPresent = false;
        Library jsfLibrary = null;
        LibraryType libraryType = panel.getLibraryType();

        if (libraryType == null) {
            return;
        }
        if (libraryType == LibraryType.USED) {
            if (!libsInitialized) {
                initLibraries();
            }
            jsfLibrary = panel.getLibrary();
        } else if (libraryType == LibraryType.NEW) {
            if (panel.getNewLibraryName() != null) {
                jsfLibrary = LibraryManager.getDefault().getLibrary(panel.getNewLibraryName());
            }
        } else if (libraryType == LibraryType.SERVER) {
            //XXX: need to find lib version
            ServerLibraryItem item = (ServerLibraryItem) serverLibraries.getSelectedItem();
            if (item != null
                    && (item.getVersion() == JSFVersion.JSF_2_0 || item.getVersion() == JSFVersion.JSF_2_1)) {
                faceletsPresent = true;
            }
        }
        if (jsfLibrary != null) {
            if (jsfLibraries.get(cbLibraries.getSelectedIndex()).getVersion() == JSFVersion.JSF_2_0
                    || jsfLibraries.get(cbLibraries.getSelectedIndex()).getVersion() == JSFVersion.JSF_2_1) {
                faceletsPresent = true;
            } else {
                List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
                try {
                    faceletsPresent = Util.containsClass(content, "com.sun.facelets.Facelet") ||        //NOI18N
                                      Util.containsClass(content, "com.sun.faces.facelets.Facelet");    //NOI18N
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        preferredLanguages.clear();
        preferredLanguages.add(PreferredLanguage.JSP);
        if (faceletsPresent) {
            if (!customizer) {
                panel.setEnableFacelets(true);
            }

            if (panel.isEnableFacelets()) {
                preferredLanguages.add(0, PreferredLanguage.Facelets);
            } else {
                preferredLanguages.add(PreferredLanguage.Facelets);
            }
        } else {
            panel.setEnableFacelets(false);
        }
        cbPreferredLang.setModel(new DefaultComboBoxModel(preferredLanguages.toArray()));
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jsfTabbedPane = new javax.swing.JTabbedPane();
        libPanel = new javax.swing.JPanel();
        rbServerLibrary = new javax.swing.JRadioButton();
        rbRegisteredLibrary = new javax.swing.JRadioButton();
        cbLibraries = new javax.swing.JComboBox();
        rbNewLibrary = new javax.swing.JRadioButton();
        lDirectory = new javax.swing.JLabel();
        customBundleTextField = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();
        lVersion = new javax.swing.JLabel();
        jtNewLibraryName = new javax.swing.JTextField();
        serverLibraries = new javax.swing.JComboBox();
        cbPackageJars = new javax.swing.JCheckBox();
        confPanel = new javax.swing.JPanel();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbPreferredLang = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        componentsPanel = new javax.swing.JPanel();
        jsfComponentsLabel = new javax.swing.JLabel();
        jsfComponentsScrollPane = new javax.swing.JScrollPane();
        jsfComponentsTable = new javax.swing.JTable();

        setLayout(new java.awt.CardLayout());

        jsfTabbedPane.setMinimumSize(new java.awt.Dimension(106, 62));
        jsfTabbedPane.setPreferredSize(new java.awt.Dimension(483, 210));

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        buttonGroup1.add(rbServerLibrary);
        rbServerLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbNoAppend").charAt(0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        rbServerLibrary.setText(bundle.getString("LBL_Any_Library")); // NOI18N
        rbServerLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbServerLibraryItemStateChanged(evt);
            }
        });

        buttonGroup1.add(rbRegisteredLibrary);
        rbRegisteredLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbRegLibs").charAt(0));
        rbRegisteredLibrary.setSelected(true);
        rbRegisteredLibrary.setText(bundle.getString("LBL_REGISTERED_LIBRARIES")); // NOI18N
        rbRegisteredLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbRegisteredLibraryItemStateChanged(evt);
            }
        });

        cbLibraries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Search Libraries..." }));
        cbLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLibrariesActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbNewLibrary);
        rbNewLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbCrNewLib").charAt(0));
        rbNewLibrary.setText(bundle.getString("LBL_CREATE_NEW_LIBRARY")); // NOI18N
        rbNewLibrary.setToolTipText(bundle.getString("MSG_CreatingLibraries")); // NOI18N
        rbNewLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNewLibraryItemStateChanged(evt);
            }
        });

        lDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFDir").charAt(0));
        lDirectory.setLabelFor(customBundleTextField);
        lDirectory.setText(bundle.getString("LBL_INSTALL_DIR")); // NOI18N
        lDirectory.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N

        customBundleTextField.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N
        customBundleTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                customBundleTextFieldKeyPressed(evt);
            }
        });

        jbBrowse.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Browse").charAt(0));
        jbBrowse.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Browse")); // NOI18N
        jbBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "HINT_JSF_BROWSE_BTN")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        lVersion.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFVer").charAt(0));
        lVersion.setLabelFor(jtNewLibraryName);
        lVersion.setText(bundle.getString("LBL_VERSION")); // NOI18N
        lVersion.setToolTipText(bundle.getString("HINT_Version")); // NOI18N

        jtNewLibraryName.setToolTipText(bundle.getString("HINT_Version")); // NOI18N
        jtNewLibraryName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtNewLibraryNameKeyReleased(evt);
            }
        });

        serverLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverLibrariesActionPerformed(evt);
            }
        });

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs")); // NOI18N

        javax.swing.GroupLayout libPanelLayout = new javax.swing.GroupLayout(libPanel);
        libPanel.setLayout(libPanelLayout);
        libPanelLayout.setHorizontalGroup(
            libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbNewLibrary, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(libPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbPackageJars)
                            .addComponent(lVersion)
                            .addComponent(lDirectory))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 206, Short.MAX_VALUE))
                    .addGroup(libPanelLayout.createSequentialGroup()
                        .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(rbServerLibrary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rbRegisteredLibrary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbLibraries, 0, 316, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libPanelLayout.createSequentialGroup()
                                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jtNewLibraryName, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                                    .addComponent(customBundleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbBrowse))
                            .addComponent(serverLibraries, 0, 295, Short.MAX_VALUE))))
                .addContainerGap())
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbServerLibrary)
                    .addComponent(serverLibraries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbRegisteredLibrary)
                    .addComponent(cbLibraries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbNewLibrary)
                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbBrowse)
                    .addComponent(customBundleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDirectory))
                .addGroup(libPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtNewLibraryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbPackageJars)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        cbPackageJars.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_PackageJarToWar")); // NOI18N

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Libraries"), libPanel); // NOI18N

        lURLPattern.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lURLPattern").charAt(0));
        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern")); // NOI18N

        tURLPattern.setText(panel.getFacesMapping());

        cbPreferredLang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPreferredLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPreferredLangActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(cbPreferredLang);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_PREFERRED_LANGUAGE")); // NOI18N

        javax.swing.GroupLayout confPanelLayout = new javax.swing.GroupLayout(confPanel);
        confPanel.setLayout(confPanelLayout);
        confPanelLayout.setHorizontalGroup(
            confPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(confPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(confPanelLayout.createSequentialGroup()
                        .addComponent(lURLPattern)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tURLPattern, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                    .addGroup(confPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbPreferredLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        confPanelLayout.setVerticalGroup(
            confPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(confPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lURLPattern)
                    .addComponent(tURLPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(confPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbPreferredLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(98, 98, 98))
        );

        tURLPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_Mapping")); // NOI18N

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Configuration"), confPanel); // NOI18N

        jsfComponentsLabel.setText(getJsfComponentsLabelText());

        jsfComponentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jsfComponentsScrollPane.setViewportView(jsfComponentsTable);

        javax.swing.GroupLayout componentsPanelLayout = new javax.swing.GroupLayout(componentsPanel);
        componentsPanel.setLayout(componentsPanelLayout);
        componentsPanelLayout.setHorizontalGroup(
            componentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(componentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jsfComponentsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .addComponent(jsfComponentsLabel))
                .addContainerGap())
        );
        componentsPanelLayout.setVerticalGroup(
            componentsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jsfComponentsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsfComponentsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addContainerGap())
        );

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Components"), componentsPanel); // NOI18N

        add(jsfTabbedPane, "card10");
        jsfTabbedPane.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

private void rbServerLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbServerLibraryItemStateChanged
    updateLibrary();
    if (rbServerLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbServerLibraryItemStateChanged

private void jtNewLibraryNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtNewLibraryNameKeyReleased
    panel.setNewLibraryName(jtNewLibraryName.getText().trim());
}//GEN-LAST:event_jtNewLibraryNameKeyReleased

private void rbNewLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNewLibraryItemStateChanged
    updateLibrary();
    if (rbNewLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbNewLibraryItemStateChanged

private void cbLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLibrariesActionPerformed
    panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
    setJsfVersion(jsfLibraries.get(cbLibraries.getSelectedIndex()).getVersion());
    updatePreferredLanguages();
}//GEN-LAST:event_cbLibrariesActionPerformed

private void rbRegisteredLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbRegisteredLibraryItemStateChanged
    updateLibrary();
    if (rbRegisteredLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbRegisteredLibraryItemStateChanged

private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(NbBundle.getMessage(JSFConfigurationPanelVisual.class,"LBL_SelectLibraryLocation")); //NOI18N
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith(".jar") || f.isDirectory()) { //N0I18N
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_FileTypeInChooser"); //NOI18N
            }
        });
    chooser.setCurrentDirectory(new File(customBundleTextField.getText().trim()));

    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File selectedEntry = chooser.getSelectedFile();
        customBundleTextField.setText(selectedEntry.getAbsolutePath());
        setNewLibraryFolder();
    }
}//GEN-LAST:event_jbBrowseActionPerformed

private void customBundleTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customBundleTextFieldKeyPressed
    setNewLibraryFolder();
}//GEN-LAST:event_customBundleTextFieldKeyPressed

private void cbPreferredLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPreferredLangActionPerformed
    if (!customizer) {
        PreferredLanguage selectedLanguage = getPreferredLanguage();
        if (PreferredLanguage.Facelets == selectedLanguage) {
            panel.updateEnableFacelets(true, true);
        } else {
            panel.updateEnableFacelets(false, true);
        }
    }
}//GEN-LAST:event_cbPreferredLangActionPerformed

private void serverLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverLibrariesActionPerformed
    // TODO add your handling code here:
    ServerLibraryItem item = (ServerLibraryItem) serverLibraries.getSelectedItem();
    if (item != null) {
        panel.setServerLibrary(item.getLibrary());
    }
    updatePreferredLanguages();
}//GEN-LAST:event_serverLibrariesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbLibraries;
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JComboBox cbPreferredLang;
    private javax.swing.JPanel componentsPanel;
    private javax.swing.JPanel confPanel;
    private javax.swing.JTextField customBundleTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JLabel jsfComponentsLabel;
    private javax.swing.JScrollPane jsfComponentsScrollPane;
    private javax.swing.JTable jsfComponentsTable;
    private javax.swing.JTabbedPane jsfTabbedPane;
    private javax.swing.JTextField jtNewLibraryName;
    private javax.swing.JLabel lDirectory;
    private javax.swing.JLabel lURLPattern;
    private javax.swing.JLabel lVersion;
    private javax.swing.JPanel libPanel;
    private javax.swing.JRadioButton rbNewLibrary;
    private javax.swing.JRadioButton rbRegisteredLibrary;
    private javax.swing.JRadioButton rbServerLibrary;
    private javax.swing.JComboBox serverLibraries;
    private javax.swing.JTextField tURLPattern;
    // End of variables declaration//GEN-END:variables

    void enableComponents(boolean enable) {
        Component[] components;

        components = confPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }

        cbPreferredLang.setEnabled(true);
        jLabel1.setEnabled(true);

        components = libPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }

    }

    boolean valid() {
        ExtenderController controller = panel.getController();
        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")) { // NOI18N
            setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
            return false;
        }
        if (!isPatternValid(urlPattern)) {
            setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
            return false;
        }

        if (controller.getProperties().getProperty("NoDocBase") != null) {  //NOI18N
            setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_MissingDocBase"));
            return false;
        }

        controller.getProperties().setProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);

        if (rbRegisteredLibrary.isSelected()) {
            if (jsfLibraries == null || jsfLibraries.size() == 0) {
                setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_MissingJSF")); //NOI18N
                return false;
            }
        }

        if (rbNewLibrary.isSelected()) {
            // checking, whether the folder is the right one
            String customResource = customBundleTextField.getText().trim();
            String message;

            message = JSFUtils.isJSFLibraryResource(new File(customResource));
            if ("".equals(customResource)) { //NOI18N
                setInfoMessage(message);
                return false;
            }

            if (message != null) {
                setErrorMessage(message);
                return false;
            }
            // checking new library name
            String newLibraryName = jtNewLibraryName.getText().trim();
            if (newLibraryName.length() <= 0) {
                setInfoMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyNewLibraryName"));
                return false;
            }

            message = checkLibraryName(newLibraryName);
            if (message != null) {
                setErrorMessage(message);
                return false;
            }
            Library lib = LibraryManager.getDefault().getLibrary(newLibraryName);
            if (lib != null) {
                setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_AlreadyExists")); //NOI18N
                return false;
            }
        }
        if (!isServerRegistered(serverInstanceID)) {   //NOI18N
            setInfoMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ERR_MissingTargetServer")); //NOI18N
        }

        // check all enabled JSF component libraries
        for (JsfComponentImplementation jsfComponentDescriptor : getActivedJsfDescriptors()) {
            if (jsfComponentDescriptor.createJsfComponentCustomizer(null) != null
                    && !jsfComponentDescriptor.createJsfComponentCustomizer(null).isValid()) {
                String error = NbBundle.getMessage(JSFConfigurationPanelVisual.class,
                        "LBL_JsfComponentNotValid", jsfComponentDescriptor.getName()); //NOI18N
                setErrorMessage(error);
                return false;
            }
        }

        controller.setErrorMessage(null);
        return true;
    }

    /**
     * Sets the error message independently if it's called for the new project
     * wizard or the project properties.
     *
     * @param message error message which should be shown
     */
    private void setErrorMessage(String message) {
        ExtenderController controller = panel.getController();
        controller.setErrorMessage(message);
        if (!customizer) {
            controller.getProperties().setProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
        }
    }

    /**
     * Sets the info message.
     *
     * @param message info message which should be shown
     */
    private void setInfoMessage(String message) {
        ExtenderController controller = panel.getController();
        Properties properties = controller.getProperties();
        controller.setErrorMessage(null);
        properties.setProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
    }

    private static final char[] INVALID_PATTERN_CHARS = {'%', '+'}; // NOI18N

    private boolean isPatternValid(String pattern) {
        for (char c : INVALID_PATTERN_CHARS) {
            if (pattern.indexOf(c) != -1) {
                return false;
            }
        }

        if (pattern.startsWith("*.")){
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1
                    && p.indexOf('/') == -1 && !p.trim().equals(""))
                return true;
        }
        // pattern = "/.../*", where ... can't be empty.
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/"))
            return true;
        return false;
    }

    private boolean isWebLogic(String serverInstanceID) {
        if (!isServerRegistered(serverInstanceID)) {
            return false;
        }
        String shortName;
        try {
            shortName = Deployment.getDefault().getServerInstance(serverInstanceID).getServerID();
            if (shortName != null && shortName.toLowerCase().startsWith("weblogic")) {  //NOI18N
                return true;
            }
        } catch (InstanceRemovedException ex) {
            LOG.log(Level.INFO, "Server Instance was removed", ex); //NOI18N
        }
        return false;
    }

    void update() {
        Properties properties = panel.getController().getProperties();
        String j2eeLevel = (String)properties.getProperty("j2eeLevel"); // NOI18N
        Profile prof = j2eeLevel == null ? Profile.JAVA_EE_6_FULL : Profile.fromPropertiesString(j2eeLevel);
        serverInstanceID = (String)properties.getProperty("serverInstanceID"); //NOI18N
        Boolean isMaven = (Boolean) properties.getProperty("maven");
        if (isMaven != null && isMaven.booleanValue()) {
            setNewLibraryOptionVisible(false);
            if (!isServerRegistered(serverInstanceID)) {
                cbPackageJars.setVisible(true);
            }
        }
        initLibSettings(prof, serverInstanceID);
    }

    /**  Method looks at the project classpath and is looking for javax.faces.FacesException.
     *   If there is not this class on the classpath, then is offered appropriate jsf library
     *   according web module version.
     */
    private void initLibSettings(Profile profile, String serverInstanceID) {
        boolean serverChanged = isServerInstanceChanged();
        if (serverChanged) {
            initServerLibraries(true);
        }

        if (panel==null || panel.getLibraryType() == null || serverChanged) {
            if (serverJsfLibraries.isEmpty()) {
                rbServerLibrary.setVisible(false);
                serverLibraries.setVisible(false);
                Library preferredLibrary = null;
                if (profile.equals(Profile.JAVA_EE_6_FULL) || profile.equals(Profile.JAVA_EE_6_WEB) || profile.equals(Profile.JAVA_EE_5)) {
                    preferredLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_2_0_NAME);
                } else {
                    preferredLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_1_2_NAME);
                }

                if (preferredLibrary != null) {
                    // if there is a proffered library, select
                    rbRegisteredLibrary.setSelected(true);
                    cbLibraries.setSelectedItem(preferredLibrary.getDisplayName());
                    updateLibrary();
                } else {
                    // there is not a proffered library -> select one or select creating new one
                    if (jsfLibraries.isEmpty()) {
                        rbNewLibrary.setSelected(true);
                    }
                }
            } else {
                if (!rbServerLibrary.isVisible()) {
                    rbServerLibrary.setVisible(true);
                    serverLibraries.setVisible(true);
                    repaint();
                }
                rbServerLibrary.setSelected(true);
                if (panel !=null)
                    panel.setLibraryType(LibraryType.SERVER);
                enableNewLibraryComponent(false);
                enableDefinedLibraryComponent(false);
                isWebLogicServer = isWebLogic(serverInstanceID);
            }
        } else {
            switch( panel.getLibraryType()) {
                case NEW: {
                    rbNewLibrary.setSelected(true);
                    break;
                }
                case USED: {
                    rbRegisteredLibrary.setSelected(true);
                    break;
                }
                case SERVER: {
                    rbServerLibrary.setSelected(true);
                    enableDefinedLibraryComponent(false);
                    enableNewLibraryComponent(false);
                    break;
                }
            }

        }
    }

    void setJsfVersion(JSFVersion version) {
        if (version != currentJSFVersion) {
            currentJSFVersion = version;
            updateJsfComponentsModel(version);
        }
    }
    private boolean isServerInstanceChanged() {
        if ((serverInstanceID==null && currentServerInstanceID !=null) ||
                (serverInstanceID != null &&  !serverInstanceID.equals(currentServerInstanceID))) {
            currentServerInstanceID = serverInstanceID;
            return true;
        }
        return false;
    }

    private void setNewLibraryOptionVisible(boolean visible) {
        rbNewLibrary.setVisible(visible);
        lDirectory.setVisible(visible);
        lVersion.setVisible(visible);
        customBundleTextField.setVisible(visible);
        jbBrowse.setVisible(visible);
        jtNewLibraryName.setVisible(visible);
    }

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JSFConfigurationPanelVisual.class);
    }

    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public String getServletName(){
        return jsfServletName==null ? JSF_SERVLET_NAME : jsfServletName;
    }

    protected void setServletName(String name){
        jsfServletName = name;
    }

    public String getURLPattern(){
        return tURLPattern.getText();
    }

    protected void setURLPattern(String pattern){
        tURLPattern.setText(pattern);
    }

    public TreeMap<String, JsfComponentCustomizer> getJsfComponentCustomizers() {
        return jsfComponentCustomizers;
    }

    public void addJsfComponentCustomizer(String jsfComponentName, JsfComponentCustomizer jsfComponentCustomizer) {
        jsfComponentCustomizers.remove(jsfComponentName);
        jsfComponentCustomizers.put(jsfComponentName, jsfComponentCustomizer);
    }

    public List<? extends JsfComponentImplementation> getActivedJsfDescriptors() {
        List<JsfComponentImplementation> activatedDescriptors =  new ArrayList<JsfComponentImplementation>();
        for (int i = 0; i < jsfComponentsTableModel.getRowCount(); i++) {
            if (jsfComponentsTableModel.getItem(i).isSelected())
                activatedDescriptors.add(jsfComponentsTableModel.getItem(i).getJsfComponent());
        }
        return activatedDescriptors;
    }

    public List<? extends JsfComponentImplementation> getAllJsfDescriptors() {
        List<JsfComponentImplementation> allDescriptors =  new ArrayList<JsfComponentImplementation>();
        for (int i = 0; i < jsfComponentsTableModel.getRowCount(); i++) {
            allDescriptors.add(jsfComponentsTableModel.getItem(i).getJsfComponent());
        }
        return allDescriptors;
    }

    public boolean packageJars(){
        return cbPackageJars.isSelected();
    }

    @CheckForNull
    protected PreferredLanguage getPreferredLanguage() {
        Object selectedItem = cbPreferredLang.getSelectedItem();

        if (selectedItem instanceof PreferredLanguage) {
            return (PreferredLanguage) selectedItem;
        }
        return null;
    }


    private void updateLibrary(){
        if (cbLibraries.getItemCount() == 0)
            rbRegisteredLibrary.setEnabled(false);

        if (rbServerLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(false);
            enableServerLibraryComponent(true);
            panel.setLibraryType(LibraryType.SERVER);
            if (!serverJsfLibraries.isEmpty()) {
                ServerLibraryItem item = (ServerLibraryItem) serverLibraries.getSelectedItem();
                if (item != null) {
                    panel.setServerLibrary(item.getLibrary());
                    setJsfVersion(item.getVersion());
                }
            }
            panel.getController().setErrorMessage(null);
        } else if (rbRegisteredLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(true);
            enableServerLibraryComponent(false);
            panel.setLibraryType(LibraryType.USED);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
                panel.setServerLibrary(null);
                setJsfVersion(jsfLibraries.get(cbLibraries.getSelectedIndex()).getVersion());
            }
            panel.getController().setErrorMessage(null);
        } else if (rbNewLibrary.isSelected()){
            enableNewLibraryComponent(true);
            enableDefinedLibraryComponent(false);
            enableServerLibraryComponent(false);
            panel.setLibraryType(LibraryType.NEW);
            panel.setServerLibrary(null);
            setNewLibraryFolder();
        }
        updatePreferredLanguages();
    }
    private void updateJsfComponentsModel(JSFVersion version) {
        List<JsfComponentImplementation> descriptors = componentsMap.get(version);
        jsfComponentsTableModel.removeAllItems();
        if (descriptors != null) {
            for (JsfComponentImplementation descriptor : descriptors) {
                addFrameworkToModel(descriptor);
            }
        }

        jsfComponentsTable.setModel(jsfComponentsTableModel);
    }

    private void updateJsfComponents() {
        if (currentJSFVersion != null && customizer) {
           initJsfComponentLibraries(currentJSFVersion);
        }
    }

    private void enableDefinedLibraryComponent(boolean enabled){
        cbLibraries.setEnabled(enabled);
    }

    private void enableServerLibraryComponent(boolean enabled){
        serverLibraries.setEnabled(enabled);
    }

    private void enableNewLibraryComponent(boolean enabled){
        lDirectory.setEnabled(enabled);
        customBundleTextField.setEnabled(enabled);
        jbBrowse.setEnabled(enabled);
        lVersion.setEnabled(enabled);
        jtNewLibraryName.setEnabled(enabled);
    }

    private void setNewLibraryFolder() {
        String fileName = customBundleTextField.getText();

        if (fileName == null || "".equals(fileName)) { //NOI18N
            panel.setInstallResource(null);
        } else {
            File folder = new File(fileName);
            panel.setInstallResource(folder);
        }
    }

    // the name of the library is used as ant property
    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N

    private String checkLibraryName(String name) {
        String message = null;
        if (name.length() == 0) {
            message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryName");
        } else {
            if (!VALID_PROPERTY_NAME.matcher(name).matches()) {
                message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryNameCharacters");
            }
        }
        return message;
    }

    private String getJsfComponentsLabelText() {
        if (!customizer) {
            return org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_JSF_Components_Desc_New_Project"); //NOI18N
        } else {
            return org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_JSF_Components_Desc_Customizer"); //NOI18N
        }
    }

    private static class LibraryItem {

        private Library library;
        private JSFVersion version;

        public LibraryItem(Library library, JSFVersion version) {
            this.library = library;
            this.version = version;
        }

        public Library getLibrary() {
            return library;
        }

        public JSFVersion getVersion() {
            return version;
        }

        public String toString() {
            return library.getDisplayName();
        }
    }

    private static class ServerLibraryItem implements Comparable<ServerLibraryItem> {

        private final ServerLibrary library;

        private final JSFVersion version;

        private String name;

        public ServerLibraryItem(ServerLibrary library, JSFVersion version) {
            this.library = library;
            this.version = version;
        }

        public ServerLibrary getLibrary() {
            return library;
        }

        public JSFVersion getVersion() {
            return version;
        }

        @Override
        public String toString() {
            synchronized (this) {
                if (name != null) {
                    return name;
                }
            }

            StringBuilder sb = new StringBuilder();
            switch (version) {
                case JSF_1_0:
                    sb.append("JSF 1.0"); // NOI18N
                    break;
                case JSF_1_1:
                    sb.append("JSF 1.1"); // NOI18N
                    break;
                case JSF_1_2:
                    sb.append("JSF 1.2"); // NOI18N
                    break;
                case JSF_2_0:
                    sb.append("JSF 2.0"); // NOI18N
                    break;
                case JSF_2_1:
                    sb.append("JSF 2.1"); // NOI18N
                    break;
            }
            if (library != null && (library.getImplementationTitle() != null || library.getImplementationVersion() != null)) {
                sb.append(" "); // NOI18N
                sb.append("["); // NOI18N
                if (library.getImplementationTitle() != null) {
                    sb.append(library.getImplementationTitle());
                }
                if (library.getImplementationVersion() != null) {
                    if (library.getImplementationTitle() != null) {
                        sb.append(" - "); // NOI18N
                    }
                    sb.append(library.getImplementationVersion().toString());
                }
                sb.append("]"); // NOI18N
            }
            // result is the same as all fields are final
            synchronized (this) {
                name = sb.toString();
                return name;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServerLibraryItem other = (ServerLibraryItem) obj;
            if ((this.toString() == null) ? (other.toString() != null) : !this.toString().equals(other.toString())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.toString() != null ? this.toString().hashCode() : 0);
            return hash;
        }

        @Override
        public int compareTo(ServerLibraryItem o) {
            return -this.toString().compareTo(o.toString());
        }

    }

    private void initJsfComponentLibraries(JSFVersion version) {
        List<JsfComponentImplementation> descriptors = componentsMap.get(version);
        if (descriptors == null) {
            return;
        }

        for (int i = 0; i < descriptors.size(); i++) {
            JsfComponentImplementation jsfComponentDescriptor = descriptors.get(i);
            if (jsfComponentDescriptor.isInWebModule(panel.getWebModule())) {
                jsfComponentsTable.setValueAt(true, i, 0);
            }
        }
    }

    private void initJsfComponentTableVisualProperties(JTable table) {
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setTableHeader(null);

        table.setRowHeight(jsfComponentsTable.getRowHeight() + 4);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMaxWidth(100);

    }

    private void addFrameworkToModel(JsfComponentImplementation component) {
        jsfComponentsTableModel.addItem(new JSFComponentModelItem(component));
    }

    private static void fireJsfDialogUpdate(JsfComponentCustomizer jsfComponentExtender, DialogDescriptor dialogDescriptor) {
        if (jsfComponentExtender.getErrorMessage() != null) {
            dialogDescriptor.getNotificationLineSupport().setErrorMessage(jsfComponentExtender.getErrorMessage());
        } else if (jsfComponentExtender.getWarningMessage() != null) {
            dialogDescriptor.getNotificationLineSupport().setWarningMessage(jsfComponentExtender.getWarningMessage());
        } else {
            dialogDescriptor.getNotificationLineSupport().clearMessages();
        }
        dialogDescriptor.setValid(jsfComponentExtender.isValid());
    }

    public static class JsfComponentsTableCellRenderer extends DefaultTableCellRenderer {

        private TableCellRenderer jbuttonRenderer;
        private TableCellRenderer booleanRenderer;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JsfComponentImplementation) {
                JsfComponentImplementation item = (JsfComponentImplementation) value;
                Component comp = super.getTableCellRendererComponent(table, item.getName(), isSelected, false, row, column);
                if (comp instanceof JComponent) {
                    ((JComponent)comp).setOpaque(isSelected);
                }
                return comp;
            } else if (value instanceof Boolean && booleanRenderer != null) {
                    return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            } else {
                if (value instanceof JButton && jbuttonRenderer != null) {
                    return jbuttonRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                }
                else {
                    return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                }
            }
        }

        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }

        public void setJButtonRenderer(TableCellRenderer jbuttonRenderer) {
            this.jbuttonRenderer = jbuttonRenderer;
        }
    }

    private static class JTableButtonRenderer implements TableCellRenderer {

            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton button = (JButton)value;
                if (isSelected) {
                    button.setForeground(table.getSelectionForeground());
                    button.setBackground(table.getSelectionBackground());
                } else {
                    button.setForeground(table.getForeground());
                    button.setBackground(UIManager.getColor("Button.background"));
                }
                return button;
            }
    }

    private class JsfComponentsTableModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            if (jsfComponentsTable.getSelectedRow() == -1) {
                return;
            }
            panel.fireChangeEvent();
        }
    }

    private class JsfComponentsMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
                int column = jsfComponentsTable.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY()/jsfComponentsTable.getRowHeight();

            if (row < jsfComponentsTable.getRowCount()
                    && row >= 0
                    && column < jsfComponentsTable.getColumnCount()
                    && column >= 0) {
                Object value = jsfComponentsTable.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton)value).doClick();
                } else if (value instanceof Boolean) {
                    panel.fireChangeEvent();
                }
            }
        }
    }

    /**
     * Implements a TableModel.
     */
    public final class JSFComponentsTableModel extends AbstractTableModel {

        private final Class<?>[] COLUMN_TYPES = new Class<?>[] {Boolean.class, JsfComponentImplementation.class, JButton.class};
        private DefaultListModel model;
        private boolean inCustomizer;

        public JSFComponentsTableModel(boolean inCustomizer) {
            model = new DefaultListModel();
            this.inCustomizer = inCustomizer;
        }

        public int getColumnCount() {
            return COLUMN_TYPES.length;
        }

        public int getRowCount() {
            return model.size();
        }

        public Class getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }

        public Object getValueAt(final int row, int column) {
            final JSFComponentModelItem item = getItem(row);
            switch (column) {
                case 0: return item.isSelected();
                case 1: return item.getJsfComponent();
                case 2:
                    if (item.isClickable()) {
                        JButton button = new JButton(
                                NbBundle.getMessage(JSFConfigurationWizardPanelVisual.class, "LBL_MoreButton")); //NOI18N
                        button.addActionListener(new JSFComponentModelActionListener(item.getJsfComponent()));
                        return button;
                    } else {
                        return null;
                    }
                default:
                    return ""; //NOI18N
            }
        }

        public void setValueAt(Object value, int row, int column) {
            JSFComponentModelItem item = getItem(row);
            switch (column) {
                case 0: item.setSelected((Boolean) value);break;
                case 1: item.setJsfComponent((JsfComponentImplementation) value);break;
            }
            fireTableCellUpdated(row, column);
        }

        private JSFComponentModelItem getItem(int index) {
            return (JSFComponentModelItem) model.get(index);
        }

        public void addItem(JSFComponentModelItem item){
            model.addElement(item);
        }

        public void removeAllItems() {
            if (!model.isEmpty()) {
                model.removeAllElements();
            }
        }
    }

    private final class JSFComponentModelActionListener implements ActionListener {

        private JsfComponentImplementation jsfDescriptor;
        private JSFComponentWindowChangeListener listener;

        public JSFComponentModelActionListener(JsfComponentImplementation jsfDescriptor) {
            this.jsfDescriptor = jsfDescriptor;
            listener = new JSFComponentWindowChangeListener();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JsfComponentCustomizer jsfCustomizer = jsfDescriptor.createJsfComponentCustomizer(null);
            jsfCustomizer.addChangeListener(listener);

            final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    jsfCustomizer.getComponent(),
                    jsfDescriptor.getName(),
                    true,
                    null);
            dialogDescriptor.createNotificationLineSupport();
            // append help
            dialogDescriptor.setHelpCtx(jsfCustomizer.getHelpCtx());

            listener.setDialogDescriptor(dialogDescriptor);
            listener.setJsfComponentExtender(jsfCustomizer);

            // OK button will save JSF extender configuration
            ActionListener buttonsListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource().equals(DialogDescriptor.OK_OPTION)) {
                        addJsfComponentCustomizer(jsfDescriptor.getName(), jsfCustomizer);
                        jsfCustomizer.saveConfiguration();
                    }
                    panel.fireChangeEvent();
                }
            };
            dialogDescriptor.setButtonListener(buttonsListener);
            // set appropriate state of opened dialog - issue #206424
            fireJsfDialogUpdate(jsfCustomizer, dialogDescriptor);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
        }

    }

    private static final class JSFComponentWindowChangeListener implements ChangeListener {

        private DialogDescriptor dialogDescriptor;
        private JsfComponentCustomizer jsfComponentExtender;

        public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
            this.dialogDescriptor = dialogDescriptor;
        }

        public void setJsfComponentExtender(JsfComponentCustomizer jsfComponentExtender) {
            this.jsfComponentExtender = jsfComponentExtender;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            assert dialogDescriptor != null && jsfComponentExtender != null;
            fireJsfDialogUpdate(jsfComponentExtender, dialogDescriptor);
        }

    }

    private final class JSFComponentModelItem {
        private JsfComponentImplementation component;
        private Boolean selected;

        /** Creates a new instance of BeanFormProperty */
        public JSFComponentModelItem(JsfComponentImplementation component) {
            this.setJsfComponent(component);
            setSelected(Boolean.FALSE);
        }

        public JsfComponentImplementation getJsfComponent() {
            return component;
        }

        public void setJsfComponent(JsfComponentImplementation component) {
            this.component = component;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public Boolean isClickable() {
            return component.createJsfComponentCustomizer(null) != null;
        }
    }
}
