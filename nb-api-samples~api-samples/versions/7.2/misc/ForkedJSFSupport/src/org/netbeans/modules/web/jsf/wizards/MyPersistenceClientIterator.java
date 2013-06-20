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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporter;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.ProgressReporterDelegate;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao.EjbFacadeWizardIterator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao.AppServerValidationPanel;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.palette.items.FromEntityBase;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Buzek
 */
public class MyPersistenceClientIterator implements TemplateWizard.Iterator {

    private int index;
    private transient WizardDescriptor.Panel[] panels;

    static final String[] UTIL_CLASS_NAMES = {"JsfCrudELResolver", "JsfUtil", "PagingInfo"}; //NOI18N
    static final String[] UTIL_CLASS_NAMES2 = {"JsfUtil", "PaginationHelper"}; //NOI18N
    static final String UTIL_FOLDER_NAME = "util"; //NOI18N
    private static final String FACADE_SUFFIX = "Facade"; //NOI18N
    private static final String CONTROLLER_SUFFIX = "Controller";  //NOI18N
    private static final String CONVERTER_SUFFIX = "Converter";  //NOI18N
    private static final String JAVA_EXT = "java"; //NOI18N
    public static final String JSF2_GENERATOR_PROPERTY = "jsf2Generator"; // "true" if set otherwise undefined
    private static final String CSS_FOLDER = "resources/css/";  //NOI18N

    private transient WebModuleExtender wme;
    private transient ExtenderController ec;

    public Set instantiate(TemplateWizard wizard) throws IOException
    {
        final List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final String jsfFolder = (String) wizard.getProperty(WizardProperties.JSF_FOLDER);
        final Project project = Templates.getProject(wizard);
        final FileObject javaPackageRoot = (FileObject)wizard.getProperty(WizardProperties.JAVA_PACKAGE_ROOT_FILE_OBJECT);
        final String jpaControllerPkg = (String) wizard.getProperty(WizardProperties.JPA_CLASSES_PACKAGE);
        final String controllerPkg = (String) wizard.getProperty(WizardProperties.JSF_CLASSES_PACKAGE);
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        final FileObject resourcePackageRoot = (sgs.length > 0) ? sgs[0].getRootFolder() : javaPackageRoot;
        Boolean ajaxifyBoolean = (Boolean) wizard.getProperty(WizardProperties.AJAXIFY_JSF_CRUD);
        final boolean ajaxify = ajaxifyBoolean == null ? false : ajaxifyBoolean.booleanValue();

        // add framework to project first:
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        if (!fp.isInWebModule(wm)) {    //add jsf if not already present
            updateWebModuleExtender(project, wm, fp);
            wme.extend(wm);
        }

        Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
        final String preferredLanguage = preferences.get("jsf.language", "JSP");  //NOI18N

        final boolean jsf2Generator = "true".equals(wizard.getProperty(JSF2_GENERATOR_PROPERTY)) && "Facelets".equals(preferredLanguage);   //NOI18N
        final String bundleName = (String)wizard.getProperty(WizardProperties.LOCALIZATION_BUNDLE_NAME);

        boolean createPersistenceUnit = (Boolean) wizard.getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.CREATE_PERSISTENCE_UNIT);

        if (createPersistenceUnit) {
            PersistenceUnitWizardDescriptor puPanel = (PersistenceUnitWizardDescriptor) (panels[panels.length - 1] instanceof PersistenceUnitWizardDescriptor ? panels[panels.length - 1] : null);
            if(puPanel!=null) {
                    PersistenceUnit punit = Util.buildPersistenceUnitUsingData(project, puPanel.getPersistenceUnitName(), puPanel.getDBResourceSelection(), TableGeneration.NONE, puPanel.getSelectedProvider());
                    ProviderUtil.setTableGeneration(punit, puPanel.getTableGeneration(), puPanel.getSelectedProvider());
                    if (punit != null){
                        Util.addPersistenceUnitToProject( project, punit );
                    }
            }
        }

        final JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();

        final String title = NbBundle.getMessage(MyPersistenceClientIterator.class, "TITLE_Progress_Jsf_Pages"); //NOI18N
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle =
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        final ProgressPanel progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(handle);

        final ProgressReporter reporter = new ProgressReporterDelegate(
                progressContributor, progressPanel );

        final Runnable r = new Runnable() {

            public void run() {
                final boolean genSessionBean=J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported();
                try {
                    javaPackageRoot.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            handle.start();
                            int jpaProgressStepCount = genSessionBean ? EjbFacadeWizardIterator.getProgressStepCount(entities.size()) :  JpaControllerIterator.getProgressStepCount(entities.size());
                            int progressStepCount = jpaProgressStepCount + getProgressStepCount(ajaxify, jsf2Generator);
                            progressStepCount += ((jsf2Generator ? 5 : JSFClientGenerator.PROGRESS_STEP_COUNT) * entities.size());
                            progressContributor.start(progressStepCount);
                            FileObject jpaControllerPackageFileObject = FileUtil.createFolder(javaPackageRoot, jpaControllerPkg.replace('.', '/'));
                            if(genSessionBean)
                            {
                                EjbFacadeWizardIterator.generateSessionBeans(progressContributor, progressPanel, entities, project, jpaControllerPkg, jpaControllerPackageFileObject, false, false, null, null, true);
                            }
                            else
                            {
//                                assert !jsf2Generator : "jsf2 generator works only with EJBs";
                                JpaControllerIterator.generateJpaControllers(reporter,
                                        entities, project, jpaControllerPkg,
                                        jpaControllerPackageFileObject,
                                        embeddedPkSupport, false);
                            }
                            FileObject jsfControllerPackageFileObject = FileUtil.createFolder(javaPackageRoot, controllerPkg.replace('.', '/'));
                            if (jsf2Generator || "Facelets".equals(preferredLanguage)) {
                                Sources srcs = ProjectUtils.getSources(project);
                                SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
                                FileObject webRoot = sgWeb[0].getRootFolder();
                                generateJsfControllers2(progressContributor, progressPanel, jsfControllerPackageFileObject, controllerPkg, jpaControllerPkg, entities, ajaxify, project, jsfFolder, jpaControllerPackageFileObject, embeddedPkSupport, genSessionBean, jpaProgressStepCount, webRoot, bundleName, javaPackageRoot, resourcePackageRoot);
                                PersistenceUtils.logUsage(MyPersistenceClientIterator.class, "USG_PERSISTENCE_JSF", new Object[]{entities.size(), preferredLanguage});
                            } else {
                                generateJsfControllers(progressContributor, progressPanel, jsfControllerPackageFileObject, controllerPkg, jpaControllerPkg, entities, ajaxify, project, jsfFolder, jpaControllerPackageFileObject, embeddedPkSupport, genSessionBean, jpaProgressStepCount);
                                PersistenceUtils.logUsage(MyPersistenceClientIterator.class, "USG_PERSISTENCE_JSF", new Object[]{entities.size(), preferredLanguage});
                            }
                            progressContributor.progress(progressStepCount);
                        }
                    });
                } catch (IOException ioe) {
                    Logger.getLogger(MyPersistenceClientIterator.class.getName()).log(Level.INFO, null, ioe);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } finally {
                    progressContributor.finish();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressPanel.close();
                        }
                    });
                    handle.finish();
                }
            }
        };

        // Ugly hack ensuring the progress dialog opens after the wizard closes. Needed because:
        // 1) the wizard is not closed in the AWT event in which instantiate() is called.
        //    Instead it is closed in an event scheduled by SwingUtilities.invokeLater().
        // 2) when a modal dialog is created its owner is set to the foremost modal
        //    dialog already displayed (if any). Because of #1 the wizard will be
        //    closed when the progress dialog is already open, and since the wizard
        //    is the owner of the progress dialog, the progress dialog is closed too.
        // The order of the events in the event queue:
        // -  this event
        // -  the first invocation event of our runnable
        // -  the invocation event which closes the wizard
        // -  the second invocation event of our runnable

        SwingUtilities.invokeLater(new Runnable() {
            private boolean first = true;
            public void run() {
                if (!first) {
                    RequestProcessor.getDefault().post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });

        return Collections.singleton(DataFolder.findFolder(javaPackageRoot));
    }

    private static int getProgressStepCount(boolean ajaxify, boolean jsf2Generator) {
        int count = jsf2Generator ? UTIL_CLASS_NAMES2.length+1+1 : UTIL_CLASS_NAMES.length+2;    //2 "pre" messages (see generateJsfControllers) before generating util classes and controller/converter classes
        if (ajaxify) {
            count++;
        }
        return count;
    }

    private static void generateJsfControllers(ProgressContributor progressContributor, final ProgressPanel progressPanel, FileObject targetFolder, String controllerPkg, String jpaControllerPkg, List<String> entities, boolean ajaxify, Project project, String jsfFolder, FileObject jpaControllerPackageFileObject, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, boolean genSessionBean, int progressIndex) throws IOException {
        String progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Util_Pre"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);

        //copy util classes
        FileObject utilFolder = targetFolder.getFileObject(UTIL_FOLDER_NAME);
        if (utilFolder == null) {
            utilFolder = FileUtil.createFolder(targetFolder, UTIL_FOLDER_NAME);
        }
        String utilPackage = controllerPkg == null || controllerPkg.length() == 0 ? UTIL_FOLDER_NAME : controllerPkg + "." + UTIL_FOLDER_NAME;
        for (int i = 0; i < UTIL_CLASS_NAMES.length; i++){
            if (utilFolder.getFileObject(UTIL_CLASS_NAMES[i], JAVA_EXT) == null) {
                progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", UTIL_CLASS_NAMES[i] + "."+JAVA_EXT); //NOI18N
                progressContributor.progress(progressMsg, progressIndex++);
                progressPanel.setText(progressMsg);
                String content = JpaControllerUtil.readResource(MyPersistenceClientIterator.class.getClassLoader().getResourceAsStream(JSFClientGenerator.RESOURCE_FOLDER + UTIL_CLASS_NAMES[i] + ".java.txt"), "UTF-8"); //NOI18N
                content = content.replaceAll("__PACKAGE__", utilPackage);
                FileObject target = FileUtil.createData(utilFolder, UTIL_CLASS_NAMES[i] + "."+JAVA_EXT);//NOI18N
                String projectEncoding = JpaControllerUtil.getProjectEncodingAsString(project, target);
                JpaControllerUtil.createFile(target, content, projectEncoding);  //NOI18N
            }
            else {
                progressContributor.progress(progressIndex++);
            }
        }

        progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Controller_Converter_Pre"); //NOI18N"Preparing to generate JSF controllers and converters";
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);

        //If faces-config not exist it should be created
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        FileObject fo;
        if (configFiles.length == 0) {
            FileObject dest = wm.getWebInf();
            if (dest == null) {
                dest = wm.getDocumentBase().createFolder("WEB-INF");
            }
            fo = FacesConfigIterator.createFacesConfig(project, dest, "faces-config", false);
        } else {
            fo = configFiles[0];
        }

        int[] nameAttemptIndices = new int[entities.size()];
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        FileObject[] converterFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleControllerNameBase = simpleClassName + CONTROLLER_SUFFIX;
            String simpleControllerName = simpleControllerNameBase;
            while (targetFolder.getFileObject(simpleControllerName, JAVA_EXT) != null && nameAttemptIndices[i] < 1000) {
                simpleControllerName = simpleControllerNameBase + ++nameAttemptIndices[i];
            }
            String simpleConverterName = simpleClassName + CONVERTER_SUFFIX + (nameAttemptIndices[i] == 0 ? "" : nameAttemptIndices[i]);
            int converterNameAttemptIndex = 1;
            while (targetFolder.getFileObject(simpleConverterName, JAVA_EXT) != null && converterNameAttemptIndex < 1000) {
                simpleConverterName += "_" + converterNameAttemptIndex++;
            }
            controllerFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleControllerName, null);
            converterFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleConverterName, null);
        }

        if (ajaxify) {
            progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Add_Ajax_Lib"); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
            Library jsfExtensionsLib = LibraryManager.getDefault().getLibrary("jsf-extensions"); //NOI18N
            if (jsfExtensionsLib != null) {
                ProjectClassPathModifier.addLibraries(new Library[] {jsfExtensionsLib},
                        getSourceRoot(project), ClassPath.COMPILE);
            }
        }

        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            if (nameAttemptIndices[i] > 0) {
                firstLower += nameAttemptIndices[i];
            }
            if (jsfFolder.endsWith("/")) {
                jsfFolder = jsfFolder.substring(0, jsfFolder.length() - 1);
            }
            if (jsfFolder.startsWith("/")) {
                jsfFolder = jsfFolder.substring(1);
            }
            String controller = ((controllerPkg == null || controllerPkg.length() == 0) ? "" : controllerPkg + ".") + controllerFileObjects[i].getName();
            String simpleJpaControllerName = simpleClassName + (genSessionBean ? FACADE_SUFFIX : "JpaController"); //NOI18N
            FileObject jpaControllerFileObject = jpaControllerPackageFileObject.getFileObject(simpleJpaControllerName, JAVA_EXT);
            JSFClientGenerator.generateJSFPages(progressContributor, progressPanel, project, entityClass, jsfFolder, firstLower, controllerPkg, controller, targetFolder, controllerFileObjects[i], embeddedPkSupport, entities, ajaxify, jpaControllerPkg, jpaControllerFileObject, converterFileObjects[i], genSessionBean, progressIndex);
            progressIndex += JSFClientGenerator.PROGRESS_STEP_COUNT;
        }
    }

    public static boolean doesSomeFileExistAlready(FileObject javaPackageRoot, FileObject webRoot,
            String jpaControllerPkg, String jsfControllerPkg, String jsfFolder, List<String> entities,
            String bundleName) {
        for (String entity : entities) {
            String simpleControllerName = getFacadeFileName(entity);
            String pkg = jpaControllerPkg;
            if (pkg.length() > 0) {
                pkg += ".";
            }
            if (javaPackageRoot.getFileObject((pkg+simpleControllerName).replace('.', '/')+".java") != null) {
                return true;
            }
            simpleControllerName = getControllerFileName(entity);
            pkg = jsfControllerPkg;
            if (pkg.length() > 0) {
                pkg += ".";
            }
            if (javaPackageRoot.getFileObject((pkg+simpleControllerName).replace('.', '/')+".java") != null) {
                return true;
            }
            String fileName = getJsfFileName(entity, jsfFolder, "");
            if (webRoot.getFileObject(fileName+"View.xhtml") != null ||
                webRoot.getFileObject(fileName+"Edit.xhtml") != null ||
                webRoot.getFileObject(fileName+"List.xhtml") != null ||
                webRoot.getFileObject(fileName+"Create.xhtml") != null) {
                return true;
            }
        }
        bundleName = getBundleFileName(bundleName);
        if (javaPackageRoot.getFileObject(bundleName) != null) {
            return true;
        }
        return false;
    }

    private static void generateJsfControllers2(
            ProgressContributor progressContributor,
            final ProgressPanel progressPanel,
            FileObject targetFolder,
            String controllerPkg,
            String jpaControllerPkg,
            List<String> entities,
            boolean ajaxify,
            Project project,
            String jsfFolder,
            FileObject jpaControllerPackageFileObject,
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport,
            boolean genSessionBean,
            int progressIndex,
            FileObject webRoot,
            String bundleName,
            FileObject javaPackageRoot,
            FileObject resourcePackageRoot) throws IOException {
        String progressMsg;

        //copy util classes
        FileObject utilFolder = targetFolder.getFileObject(UTIL_FOLDER_NAME);
        if (utilFolder == null) {
            utilFolder = FileUtil.createFolder(targetFolder, UTIL_FOLDER_NAME);
        }
        String utilPackage = controllerPkg == null || controllerPkg.length() == 0 ? UTIL_FOLDER_NAME : controllerPkg + "." + UTIL_FOLDER_NAME;
        for (int i = 0; i < UTIL_CLASS_NAMES2.length; i++){
            if (utilFolder.getFileObject(UTIL_CLASS_NAMES2[i], JAVA_EXT) == null) {
                progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", UTIL_CLASS_NAMES2[i] + "."+JAVA_EXT); //NOI18N
                progressContributor.progress(progressMsg, progressIndex++);
                progressPanel.setText(progressMsg);
                FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject("/Templates/MyJSF/JSF_From_Entity_Wizard/"+UTIL_CLASS_NAMES2[i] + ".ftl");
                FileObject target = FileUtil.createData(utilFolder, UTIL_CLASS_NAMES2[i] + "."+JAVA_EXT);//NOI18N
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("packageName", utilPackage);
                params.put("comment", Boolean.FALSE); // NOI18N
                JSFPaletteUtilities.expandJSFTemplate(tableTemplate, params, target);
            } else {
                progressContributor.progress(progressIndex++);
            }
        }

        //int[] nameAttemptIndices = new int[entities.size()];
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String simpleControllerName = getControllerFileName(entities.get(i));
            controllerFileObjects[i] = targetFolder.getFileObject(simpleControllerName, JAVA_EXT);
            if (controllerFileObjects[i] == null) {
                controllerFileObjects[i] = targetFolder.createData(simpleControllerName, JAVA_EXT);
            }
        }

        Charset encoding = FileEncodingQuery.getEncoding(project.getProjectDirectory());
        if (webRoot.getFileObject(CSS_FOLDER+JSFClientGenerator.JSFCRUD_STYLESHEET) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(JSFClientGenerator.RESOURCE_FOLDER + JSFClientGenerator.JSFCRUD_STYLESHEET), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(webRoot, CSS_FOLDER+JSFClientGenerator.JSFCRUD_STYLESHEET);
            JSFFrameworkProvider.createFile(target, content, encoding.name());
            progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", target.getNameExt()); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
        }
        //Create template.xhtml if it is not created yet, because it is used by other generated templates
        if (webRoot.getFileObject(JSFClientGenerator.TEMPLATE_JSF_FL_PAGE) == null) {
            FileObject target = TemplateIterator.createTemplate(project, webRoot, false);
            progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", target.getNameExt()); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
        }

        List<TemplateData> bundleData = new ArrayList<TemplateData>();

        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleJpaControllerName = simpleClassName + (genSessionBean ? FACADE_SUFFIX : "JpaController"); //NOI18N

            progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", simpleClassName + "."+JAVA_EXT); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);

            FileObject template = FileUtil.getConfigRoot().getFileObject(PersistenceClientSetupPanelVisual.CONTROLLER_TEMPLATE);
            Map<String, Object> params = new HashMap<String, Object>();
            String controllerClassName = controllerFileObjects[i].getName();
            String managedBean = controllerClassName.substring(0, 1).toLowerCase() + controllerClassName.substring(1);
            params.put("managedBeanName", managedBean);
            // see issue #215703 - JSF 2.1 and CDI are not integrated enough
            // can be probably enabled again for JSF 2.2+ where it should work
            // params.put("cdiEnabled", isCdiEnabled(project));
            params.put("controllerPackageName", controllerPkg);
            params.put("controllerClassName", controllerClassName);
            params.put("entityFullClassName", entityClass);
            params.put(genSessionBean ? "ejbFullClassName" : "jpaControllerFullClassName", jpaControllerPkg+"."+simpleJpaControllerName);
            params.put(genSessionBean ? "ejbClassName" : "jpaControllerClassName", simpleJpaControllerName);
            params.put("entityClassName", simpleClassName);
            params.put("comment", Boolean.FALSE); // NOI18N
            params.put("bundle", bundleName); // NOI18N
            boolean isInjected = Util.isContainerManaged(project);
            if (!genSessionBean && isInjected) {
                params.put("isInjected", true); //NOI18N
            }
            String persistenceUnitName = Util.getPersistenceUnitAsString(project, simpleClassName);
            if ( persistenceUnitName != null) {
                params.put("persistenceUnitName", persistenceUnitName); //NOI18N
            }
            FromEntityBase.createParamsForConverterTemplate(params, targetFolder, entityClass);

            JSFPaletteUtilities.expandJSFTemplate(template, params, controllerFileObjects[i]);

            params = FromEntityBase.createFieldParameters(webRoot, entityClass, managedBean, managedBean+".selected", false, true, null);
            bundleData.add(new TemplateData(simpleClassName, (List<FromEntityBase.TemplateData>)params.get("entityDescriptors")));
            params.put("controllerClassName", controllerClassName);
            expandSingleJSFTemplate("create.ftl", entityClass, jsfFolder, webRoot, "Create", params, progressContributor, progressPanel, progressIndex++);
            expandSingleJSFTemplate("edit.ftl", entityClass, jsfFolder, webRoot, "Edit", params, progressContributor, progressPanel, progressIndex++);
            expandSingleJSFTemplate("view.ftl", entityClass, jsfFolder, webRoot, "View", params, progressContributor, progressPanel, progressIndex++);
            params = FromEntityBase.createFieldParameters(webRoot, entityClass, managedBean, managedBean+".items", true, true, null);
            params.put("controllerClassName", controllerClassName);
            expandSingleJSFTemplate("list.ftl", entityClass, jsfFolder, webRoot, "List", params, progressContributor, progressPanel, progressIndex++);

            String styleAndScriptTags = "<h:outputStylesheet name=\"css/"+JSFClientGenerator.JSFCRUD_STYLESHEET+"\"/>"; //NOI18N
            JSFClientGenerator.addLinkToListJspIntoIndexJsp(WebModule.getWebModule(project.getProjectDirectory()),
                    simpleClassName, styleAndScriptTags, "UTF-8", "/"+getJsfFileName(entityClass, jsfFolder, "List"));

        }

        progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", bundleName); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);

        FileObject template = FileUtil.getConfigRoot().getFileObject(PersistenceClientSetupPanelVisual.BUNDLE_TEMPLATE);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("entities", bundleData);
        params.put("comment", Boolean.FALSE);
        String bundleFileName = getBundleFileName(bundleName);
        FileObject target = resourcePackageRoot.getFileObject(bundleFileName);
        if (target == null) {
            target = FileUtil.createData(resourcePackageRoot, bundleFileName);
        }
        JSFPaletteUtilities.expandJSFTemplate(template, params, target);

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        FileObject fo;
        if (configFiles.length == 0) {
            FileObject dest = wm.getWebInf();
            if (dest == null) {
                dest = webRoot.createFolder("WEB-INF");
            }
            fo = FacesConfigIterator.createFacesConfig(project, dest, "faces-config", false);
        } else {
            fo = configFiles[0];
        }
        JSFConfigModel model = ConfigurationUtils.getConfigModel(fo, true);
        ResourceBundle rb = model.getFactory().createResourceBundle();
        rb.setVar("bundle");
        rb.setBaseName(bundleName);
        ResourceBundle existing = findBundle(model, rb);
        model.startTransaction();
        try {
            Application app;
            if (model.getRootComponent().getApplications().size() == 0) {
                app = model.getFactory().createApplication();
                model.getRootComponent().addApplication(app);
            } else {
                app = model.getRootComponent().getApplications().get(0);
            }
            if (existing != null) {
                app.removeResourceBundle(existing);
            }
            app.addResourceBundle(rb);
        } finally {
            try {
                model.endTransaction();
                model.sync();
            } catch (IllegalStateException ex) {
                IOException io = new IOException("Could not create faces config", ex);
                throw Exceptions.attachLocalizedMessage(io,
                        NbBundle.getMessage(MyPersistenceClientIterator.class, "ERR_UpdateFacesConfig",
                        Exceptions.findLocalizedMessage(ex)));
            }
            saveFacesConfig(fo);
        }

    }

    private static boolean isCdiEnabled(Project project) {
        CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
        return (cdiUtil == null) ? false : cdiUtil.isCdiEnabled();
    }

    private static void saveFacesConfig(FileObject fo) {
        DataObject facesDO;
        try {
            facesDO = DataObject.find(fo);
            if (facesDO !=null) {
                SaveCookie save = facesDO.getCookie(SaveCookie.class);
                if (save != null) {
                    save.save();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static ResourceBundle findBundle(JSFConfigModel model, ResourceBundle rb) {
        for (Application app : model.getRootComponent().getApplications()) {
            for (ResourceBundle bundle : app.getResourceBundles()) {
                if (bundle.getVar().equals(rb.getVar())) {
                    return bundle;
                }
            }
        }
        return null;
    }

    private static String getBundleFileName(String bundleName) {
        if (bundleName.startsWith("/")) {
            bundleName = bundleName.substring(1);
        }
        if (!bundleName.endsWith(".properties")) {
            bundleName = bundleName + ".properties"; //.substring(0, bundleName.length()-11);
        }
        return bundleName;
    }

    public static final class TemplateData {

        private String entityClassName;
        private List<FromEntityBase.TemplateData> entityDescriptors;

        public TemplateData(String entityClassName, List<FromEntityBase.TemplateData> entityDescriptors) {
            this.entityClassName = entityClassName;
            this.entityDescriptors = entityDescriptors;
        }

        public String getEntityClassName() {
            return entityClassName;
        }

        public List<FromEntityBase.TemplateData> getEntityDescriptors() {
            return entityDescriptors;
        }

    }

    private static String getControllerFileName(String entityClass) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        return simpleClassName + CONTROLLER_SUFFIX;
    }

    private static String getFacadeFileName(String entityClass) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        return simpleClassName + FACADE_SUFFIX;
    }

    private static String getJsfFileName(String entityClass, String jsfFolder, String name) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
        if (jsfFolder.endsWith("/")) {
            jsfFolder = jsfFolder.substring(0, jsfFolder.length() - 1);
        }
        if (jsfFolder.startsWith("/")) {
            jsfFolder = jsfFolder.substring(1);
        }
        if (jsfFolder.length() > 0) {
            return jsfFolder+"/"+firstLower+"/"+name;
        } else {
            return firstLower+"/"+name;
        }
    }

    private static void expandSingleJSFTemplate(String templateName, String entityClass,
            String jsfFolder, FileObject webRoot, String name, Map<String, Object> params,
            ProgressContributor progressContributor, ProgressPanel progressPanel, int progressIndex) throws IOException {
        FileObject template = FileUtil.getConfigRoot().getFileObject("/Templates/MyJSF/JSF_From_Entity_Wizard/"+templateName);
        String fileName = getJsfFileName(entityClass, jsfFolder, name);
        String  progressMsg = NbBundle.getMessage(MyPersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", fileName); //NOI18N
        progressContributor.progress(progressMsg, progressIndex);
        progressPanel.setText(progressMsg);

        FileObject jsfFile = webRoot.getFileObject(fileName+".xhtml");
        if (jsfFile == null) {
            jsfFile = FileUtil.createData(webRoot, fileName+".xhtml");
        }
        JSFPaletteUtilities.expandJSFTemplate(template, params, jsfFile);
    }

    /**
     * Convenience method to obtain the source root folder.
     * @param project the Project object
     * @return the FileObject of the source root folder
     */
    private static FileObject getSourceRoot(Project project) {
        if (project == null) {
            return null;
        }

        // Search the ${src.dir} Source Package Folder first, use the first source group if failed.
        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] grp = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grp.length; i++) {
            if ("${src.dir}".equals(grp[i].getName())) { // NOI18N
                return grp[i].getRootFolder();
            }
        }
        if (grp.length != 0) {
            return grp[0].getRootFolder();
        }

        return null;
    }

    @Override
    public void initialize(TemplateWizard wizard) {
        index = 0;
        wme = null;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }

        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        HelpCtx helpCtx;

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        if (wm.getJ2eeProfile().equals(Profile.JAVA_EE_6_WEB) || wm.getJ2eeProfile().equals(Profile.JAVA_EE_6_FULL) || JSFUtils.isJSF20Plus(wm)) {    //NOI18N
            wizard.putProperty(JSF2_GENERATOR_PROPERTY, "true");
            helpCtx = new HelpCtx("persistence_entity_selection_javaee6");  //NOI18N
        } else {
            helpCtx = new HelpCtx("persistence_entity_selection_javaee5");  //NOI18N
        }

        wizard.putProperty(PersistenceClientEntitySelection.DISABLENOIDSELECTION, Boolean.TRUE);
        WizardDescriptor.Panel secondPanel = new AppServerValidationPanel(
                new PersistenceClientEntitySelection(NbBundle.getMessage(MyPersistenceClientIterator.class, "LBL_EntityClasses"),
                        helpCtx, wizard)); // NOI18N
        PersistenceClientSetupPanel thirdPanel = new PersistenceClientSetupPanel(project, wizard);


        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        String[] names;
        ArrayList<WizardDescriptor.Panel> panelsList = new ArrayList<WizardDescriptor.Panel>();
        ArrayList<String> namesList = new ArrayList<String>();
        panelsList.add(secondPanel);
        panelsList.add(thirdPanel);
        namesList.add(NbBundle.getMessage(MyPersistenceClientIterator.class, "LBL_EntityClasses"));
        namesList.add(NbBundle.getMessage(MyPersistenceClientIterator.class, "LBL_JSFPagesAndClasses"));

        if (!fp.isInWebModule(wm)) {
//            updateWebModuleExtender(project, wm, fp);
//            JSFConfigurationWizardPanel jsfWizPanel = new JSFConfigurationWizardPanel(wme, ec);
//            thirdPanel.setFinishPanel(false);
//            panelsList.add(jsfWizPanel);
//            namesList.add(NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSF_Config_CRUD"));
        }

        boolean noPuNeeded = true;
        try {
            noPuNeeded = ProviderUtil.persistenceExists(project) || !ProviderUtil.isValidServerInstanceOrNone(project);
        } catch (InvalidPersistenceXmlException ex) {
            Logger.getLogger(JpaControllerIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: "+ ex.getPath()); //NOI18N
        }

        if(!noPuNeeded){
            panelsList.add(new PersistenceUnitWizardDescriptor(project));
            namesList.add(NbBundle.getMessage(MyPersistenceClientIterator.class, "LBL_PersistenceUnitSetup"));
        }

        panels = panelsList.toArray(new WizardDescriptor.Panel[0]);
        names = namesList.toArray(new String[0]);

        wizard.putProperty("NewFileWizard_Title",
            NbBundle.getMessage(MyPersistenceClientIterator.class, "Templates/Persistence/JsfFromDB"));
        Wizards.mergeSteps(wizard, panels, names);
    }

    private void updateWebModuleExtender(Project project, WebModule wm, JSFFrameworkProvider fp) {
        if (wme == null) {
            ec = ExtenderController.create();
            String j2eeLevel = wm.getJ2eePlatformVersion();
            ec.getProperties().setProperty("j2eeLevel", j2eeLevel);
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            if (moduleProvider != null) {
                String serverInstanceID = moduleProvider.getServerInstanceID();
                ec.getProperties().setProperty("serverInstanceID", serverInstanceID);
            }
            wme = fp.createWebModuleExtender(wm, ec);
        }
        wme.update();
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
        wme = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage (MyPersistenceClientIterator.class, "LBL_WizardTitle_FromEntity");
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

}
