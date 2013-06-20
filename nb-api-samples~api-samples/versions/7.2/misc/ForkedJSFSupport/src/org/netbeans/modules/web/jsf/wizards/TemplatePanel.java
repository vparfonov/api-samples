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
import java.io.InputStream;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class TemplatePanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private TemplatePanelVisual component;
    private WizardDescriptor wizard;
    
    /** Creates a new instance of TemplatePanel */
    public TemplatePanel(WizardDescriptor wizard) {
        this.wizard = wizard;
        component = null;
    }
    
    public Component getComponent() {
        if (component == null)
            component = new TemplatePanelVisual();
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(TemplatePanel.class);
    }
    
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
            if (preferences.get("Facelets", "").equals("")) { //NOI18N
                ClassPath cp  = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
                boolean faceletsPresent = cp.findResource(JSFUtils.MYFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null || //NOI18N
                                          cp.findResource("com/sun/facelets/Facelet.class") !=null || //NOI18N
                                          cp.findResource("com/sun/faces/facelets/Facelet.class") !=null || // NOI18N
                                          cp.findResource("javax/faces/view/facelets/FaceletContext.class") != null; //NOI18N
                if (!faceletsPresent) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(TemplatePanel.class, "ERR_NoJSFLibraryFound"));
                    return false;
                }
            }
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    InputStream getTemplate(){
        getComponent();
        return component.getTemplate();
    }
    
    InputStream getDefaultCSS(){
        getComponent();
        return component.getDefaultCSS();
    }
    
    InputStream getLayoutCSS(){
        getComponent();
        return component.getLayoutCSS();
    }
    
    String getLayoutFileName(){
        getComponent();
        return component.getLayoutFileName();
    }
}
