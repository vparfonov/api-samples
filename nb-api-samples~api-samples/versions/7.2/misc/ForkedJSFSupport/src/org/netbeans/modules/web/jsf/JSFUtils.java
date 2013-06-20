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

package org.netbeans.modules.web.jsf;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.editor.ActionFactory;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl, Radko Najman, Martin Fousek
 */
public class JSFUtils {

    private static final String LIB_FOLDER = "lib";         //NOI18N

    // the names of bundled jsf libraries
    public static String DEFAULT_JSF_1_1_NAME = "jsf1102";  //NOI18N
    public static String DEFAULT_JSF_1_2_NAME = "jsf12";    //NOI18N
    public static String DEFAULT_JSF_2_0_NAME = "jsf20";    //NOI18N
    public static String DEFAULT_JSF_1_2_RI_NAME = "jsf12ri";    //NOI18N
    public static String DEFAULT_JSF_2_0_RI_NAME = "jsf20ri";    //NOI18N
    // the name of jstl libraryr
    public static String DEFAULT_JSTL_1_1_NAME = "jstl11";  //NOI18N

    public static final String FACES_EXCEPTION = "javax.faces.FacesException"; //NOI18N
    public static final String JSF_1_2__API_SPECIFIC_CLASS = "javax.faces.application.StateManagerWrapper"; //NOI18N
    public static final String JSF_2_0__API_SPECIFIC_CLASS = "javax.faces.application.ProjectStage"; //NOI18N
    public static final String JSF_2_1__API_SPECIFIC_CLASS = "javax.faces.component.TransientStateHelper"; //NOI18N
    public static final String JSF_2_0__IMPL_SPECIFIC_CLASS= "com.sun.faces.facelets.Facelet"; //NOI18N
    public static final String MYFACES_SPECIFIC_CLASS = "org.apache.myfaces.webapp.StartupServletContextListener"; //NOI18N

    //constants for web.xml
    protected static final String FACELETS_SKIPCOMMNETS = "javax.faces.FACELETS_SKIP_COMMENTS";
    protected static final String FACELETS_DEVELOPMENT = "facelets.DEVELOPMENT";
    protected static final String FACELETS_DEFAULT_SUFFIX = "javax.faces.DEFAULT_SUFFIX";
    public static final String FACES_PROJECT_STAGE = "javax.faces.PROJECT_STAGE";

    // usages logger
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.web.jsf"); // NOI18N

    /** This method finds out, whether the input file is a folder that contains
     * a jsf implementation or if file given if it contains required javax.faces.FacesException
     * class directly.
     *
     * @return null if the folder or file contains a JSF implemention or an error message
     */
    public static String isJSFLibraryResource(File resource) {
        String result = null;
        boolean isJSF = false;

        // path doesn't exist
        if (!resource.exists()) {
            result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_VALID_PATH", resource.getPath()); //NOI18N
        }

        if (resource.isDirectory()) {
            // Case of JSF version 2.1.2 and older - JSF library is created from packed directory
            File libFolder = new File(resource, LIB_FOLDER);
            if (libFolder.exists()) {
                File[] files = libFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        boolean accepted = false;
                        if (pathname.getName().endsWith(".jar")) { //NOI18N
                            accepted = true;
                        }
                        return accepted;
                    }
                });
                try {
                    List<File> list = Arrays.asList(files);
                    isJSF = Util.containsClass(list, FACES_EXCEPTION);
                } catch (IOException exception) {
                    Exceptions.printStackTrace(exception);
                }
            } else {
                result = NbBundle.getMessage(JSFUtils.class, "ERROR_THERE_IS_NOT_LIB_FOLDER", resource.getPath()); //NOI18N
            }
        } else {
            // Case of JSF version 2.1.3+ - JSF library is delivered as a single JAR file
            try {
                isJSF = Util.containsClass(Collections.singletonList(resource), FACES_EXCEPTION);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // jsf wasn't found (in the directory or inside selected JAR file)
        if (!isJSF) {
            result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_JSF_API", resource.getPath()); //NOI18N
        }

        return result;
    }

    public static boolean createJSFUserLibrary(File resource, String libraryName) throws IOException {
        if (!resource.exists()) {
            return false;
        }

        List<URL> urls = new ArrayList<URL>();
        if (resource.isDirectory()) {
            // JSF version 2.1.2-
            // find all jars in the folder/lib
            File libFolder = new File(resource, LIB_FOLDER);
            if (libFolder.isDirectory()) {
                File[] jars = libFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".jar"); //NOI18N
                    }
                });

                // obtain URLs of the jar file
                for (int i = 0; i < jars.length; i++) {
                    URL url = FileUtil.urlForArchiveOrDir(jars[i]);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }
        } else {
            // JSF version 2.1.3+
            urls.add(FileUtil.urlForArchiveOrDir(resource));
        }

        // create new library and regist in the Library Manager.
        Map<String, List<URL>> content = new HashMap<String, List<URL>>();
        content.put("classpath", urls); //NOI18N
        LibraryManager.getDefault().createLibrary("j2se", libraryName, libraryName, libraryName, content); //NOI18N
        return true;
    }

    /** Find the value of the facelets.DEVELOPMENT context parameter in the deployment descriptor.
     */
    public static boolean debugFacelets(FileObject dd) {
        boolean value = false;  // the default value of the facelets.DEVELOPMENT
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.DEVELOPMENT"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }

    /** Find the value of the facelets.SKIP_COMMENTS context parameter in the deployment descriptor.
     */
    public static boolean skipCommnets(FileObject dd) {
        boolean value = false;  // the default value of the facelets.SKIP_COMMENTS
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.SKIP_COMMENTS"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
    /** Returns relative path from one file to another file
     */
    public static String getRelativePath (FileObject fromFO, FileObject toFO){
        String path = "./";
        FileObject parent = fromFO.getParent();
        String tmpPath = null;
        while (parent != null && (tmpPath = FileUtil.getRelativePath(parent, toFO)) == null){
            parent = parent.getParent();
            path = path + "../";
        }

        return (tmpPath != null ? path + tmpPath : null);
    }

    public static boolean isJSF20Plus(WebModule wm) {
        if (wm == null)
            return false;
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        boolean isJSF20 = classpath.findResource(JSF_2_0__API_SPECIFIC_CLASS.replace('.', '/') + ".class") != null; //NOI18N
        return isJSF20;
    }

    public static boolean isJavaEE5(TemplateWizard wizard) {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Profile profile = wm.getJ2eeProfile();
            return (profile == Profile.JAVA_EE_5);
        }
        return false;
    }

    public static boolean isCDIEnabled(WebModule wm) {
        if (wm != null) {
            FileObject confRoot = wm.getWebInf();
            if (confRoot!=null && confRoot.getFileObject("beans.xml")!=null) {  //NOI18N
                return true;
            }
        }
        return false;
    }

     /**
     * Logs usage statistics data.
     *
     * @param srcClass source class
     * @param message USG message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }

    /**
     * Gets any fileObject inside the given web module.
     *
     * @param module web module to be scanned
     * @return fileObject if any found, {@code null} otherwise
     */
    public static FileObject getFileObject(WebModule module) {
        FileObject fileObject = module.getDocumentBase();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getDeploymentDescriptor();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getWebInf();
        if (fileObject != null) {
            return fileObject;
        }

        FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(module);
        if (facesConfigFiles != null && facesConfigFiles.length > 0) {
            return facesConfigFiles[0];
        }

        FileObject[] fileObjects = module.getJavaSources();
        if (fileObjects != null) {
            for (FileObject source : fileObjects) {
                if (source != null) {
                    return source;
                }
            }
        }
        return null;
    }
}
