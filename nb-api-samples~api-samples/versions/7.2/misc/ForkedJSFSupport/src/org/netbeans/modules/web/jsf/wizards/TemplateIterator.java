/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class TemplateIterator implements TemplateWizard.Iterator {

    private int index;
    private transient WizardDescriptor.Panel[] panels;

    private TemplatePanel templatePanel;
    private transient SourceGroup[] sourceGroups;
    private static final String CSS_FOLDER = "css"; //NOI18N
    private static final String CSS_FOLDER2 = "resources/css"; //NOI18N
    private static final String CSS_EXT = "css"; //NOI18N
    private static final String XHTML_EXT = "xhtml";    //NOI18N
    private static final String ENCODING = "UTF-8"; //NOI18N
    private static String TEMPLATE_XHTML = "template.xhtml"; //NOI18N
    private static String TEMPLATE_XHTML2 = "template-jsf2.xhtml"; //NOI18N
    private static String FL_RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/facelets/resources/templates/"; //NOI18N

    /** Creates a new instance of TemplateIterator */
    public TemplateIterator() {
    }

    static FileObject createTemplate(Project project, FileObject targetDir, boolean addJSFFrameworkIfNecessary) throws IOException {
        FileObject result = null;
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject dir = wm.getDocumentBase();
            if (dir.getFileObject(TEMPLATE_XHTML) != null) {
                return null;
            }
            if (addJSFFrameworkIfNecessary && !JSFConfigUtilities.hasJsfFramework(dir)) {
                JSFConfigUtilities.extendJsfFramework(dir, false);
            }

            boolean isJSF20 = JSFUtils.isJSF20Plus(wm);
            String templateFile = TEMPLATE_XHTML;
            if (isJSF20) {
                templateFile = TEMPLATE_XHTML2;
            }
            String content = JSFFrameworkProvider.readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(FL_RESOURCE_FOLDER + templateFile), ENCODING);
            result = FileUtil.createData(targetDir, TEMPLATE_XHTML); //NOI18N
            JSFFrameworkProvider.createFile(result, content, ENCODING); //NOI18N
            DataObject dob = DataObject.find(result);
            if (dob != null) {
                JSFPaletteUtilities.reformat(dob);
            }
        }
        return result;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        final String targetName =  Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder( dir );
        if (df != null) {
            WebModule wm = WebModule.getWebModule(df.getPrimaryFile());
            if (wm != null) {
                final FileObject docBase = wm.getDocumentBase();
                if (!JSFConfigUtilities.hasJsfFramework(docBase)) {
                    JSFConfigUtilities.extendJsfFramework(dir, false);
                }
                final boolean isJSF20 = JSFUtils.isJSF20Plus(wm);

                df.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
                    public void run() throws IOException {
                        InputStream is;
                        FileObject target = df.getPrimaryFile().createData(targetName, XHTML_EXT);

                        String folderName = isJSF20 ? CSS_FOLDER2 : CSS_FOLDER;
                        FileObject cssFolder = docBase.getFileObject(folderName);
                        if (cssFolder == null)
//                            cssFolder = docBase.createFolder(folderName);
                            cssFolder = FileUtil.createFolder(docBase, folderName);
                        // name of the layout file
                        String layoutName = templatePanel.getLayoutFileName();
                        FileObject cssFile = cssFolder.getFileObject(layoutName, CSS_EXT); //NOI18N
                        if (cssFile == null){
                            cssFile = cssFolder.createData(layoutName, CSS_EXT);
                            is = templatePanel.getLayoutCSS();
                            JSFFrameworkProvider.createFile(cssFile, JSFFrameworkProvider.readResource(is, ENCODING), ENCODING);
                        }
                        String layoutPath = JSFUtils.getRelativePath(target, cssFile);
                        cssFile = cssFolder.getFileObject("default", CSS_EXT);  //NOI18N
                        if (cssFile == null){
                            cssFile = cssFolder.createData("default", CSS_EXT); //NOI18N
                            is = templatePanel.getDefaultCSS();
                            JSFFrameworkProvider.createFile(cssFile, JSFFrameworkProvider.readResource(is, ENCODING), ENCODING);
                        }
                        String defaultPath = JSFUtils.getRelativePath(target, cssFile);

                        is = templatePanel.getTemplate();
                        String content = JSFFrameworkProvider.readResource(is, ENCODING);
                        if (!isJSF20) {
                            content = content.replaceAll("h:head", "head").replaceAll("h:body", "body"); //NOI18N
                        }

                        HashMap args = new HashMap();
                        args.put("LAYOUT_CSS_PATH", layoutPath);    //NOI18N
                        args.put("DEFAULT_CSS_PATH", defaultPath);  //NOI18N
                        MapFormat formater = new MapFormat(args);
                        formater.setLeftBrace("__");    //NOI18N
                        formater.setRightBrace("__");   //NOI18N
                        formater.setExactMatch(false);
                        content = formater.format(content);

                        JSFFrameworkProvider.createFile(target, content, ENCODING);
                    }
                });

                FileObject target = df.getPrimaryFile().getFileObject(targetName, XHTML_EXT);
                DataObject dob = DataObject.find(target);
                JSFPaletteUtilities.reformat(dob);
                return Collections.singleton(dob);
            }
        }
        return Collections.EMPTY_SET;
    }

    public void initialize(TemplateWizard wiz) {
        //this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y",
                new Integer(index + 1), new Integer(panels.length));
    }

    public void previousPanel() {
        if (! hasPrevious()) throw new NoSuchElementException();
        index--;
    }

    public void nextPanel() {
        if (! hasNext()) throw new NoSuchElementException();
        index++;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups1 = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        SourceGroup[] sourceGroups;
        if (sourceGroups1.length < 2)
            sourceGroups = new SourceGroup[]{sourceGroups1[0], sourceGroups1[0]};
        else
            sourceGroups = sourceGroups1;

        templatePanel = new TemplatePanel(wiz);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = new JSFValidationPanel(
                Templates.createSimpleTargetChooser(project,sourceGroups,templatePanel));
        JComponent c = (JComponent)firstPanel.getComponent();
        Dimension d  = c.getPreferredSize();
        d.setSize(d.getWidth(), d.getHeight()+65);
        c.setPreferredSize(d);
        return new WizardDescriptor.Panel[] {
            firstPanel
        };
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
}
