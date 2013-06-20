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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

public class TemplateClientIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private TemplateClientPanel templateClientPanel;
    private transient SourceGroup[] sourceGroups;
    private static final String ENCODING = "UTF-8"; //NOI18N
    
    private static String END_LINE = System.getProperty("line.separator"); //NOI18N
    
    /** Creates a new instance of TemplateClientIterator */
    public TemplateClientIterator() {
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        final String targetName =  Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder( dir );
        
        df.getPrimaryFile().getFileSystem().runAtomicAction(new FileSystem.AtomicAction(){
            
            public void run() throws IOException {
                InputStream is = templateClientPanel.getTemplateClient();
                String content = JSFFrameworkProvider.readResource(is, ENCODING);
                FileObject target = df.getPrimaryFile().createData(targetName, "xhtml");
                String relativePath = JSFUtils.getRelativePath(target, templateClientPanel.getTemplate());
                String definedTags = createDefineTags(templateClientPanel.getTemplateData(),
                        ((content.indexOf("<html") == -1)?1:3));    //NOI18N
                
                HashMap args = new HashMap();
                args.put("TEMPLATE", relativePath); //NOI18N
                args.put("DEFINE_TAGS", definedTags);   //NOI18N
                
                MapFormat formater = new MapFormat(args);
                formater.setLeftBrace("__");    //NOI18N
                formater.setRightBrace("__");   //NOI18N
                formater.setExactMatch(false);
                
                content = formater.format(content);
                
                JSFFrameworkProvider.createFile(target, content, ENCODING);
            }
            
        });

        FileObject target = df.getPrimaryFile().getFileObject(targetName, "xhtml");
        DataObject dob = DataObject.find(target);
        if (dob != null) {
            JSFPaletteUtilities.reformat(dob);
        }
        return Collections.singleton(dob);
    }
    
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        templateClientPanel=new TemplateClientPanel(wiz);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(project,sourceGroups,templateClientPanel);
        
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
    
    private String createDefineTags(Collection<String> data, int indent) {
        StringBuffer sb = new StringBuffer();
        final String basicIndent = "    ";
        
        for (String temp : data) {
            sb.append(END_LINE);
            for (int i = 0; i < indent; i++)
                sb.append(basicIndent);
            sb.append("<ui:define name=\"").append(temp).append("\">"); //NOI18N
            sb.append(END_LINE);
            for (int i = 0; i < (indent + 1); i++)
                sb.append(basicIndent);
            sb.append(temp);
            sb.append(END_LINE);
            for (int i = 0; i < indent; i++)
                sb.append(basicIndent);
            sb.append("</ui:define>");  //NOI18N
            sb.append(END_LINE);
        }
        
        return sb.toString();
    }
}
