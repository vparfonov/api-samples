package org.netbeans.examples.modules.povproject;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.examples.api.povray.RendererService;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

public class RendererServiceImpl extends RendererService implements FileChangeListener {

    private PovrayProject proj;

    public RendererServiceImpl(PovrayProject proj) {
        this.proj = proj;
    }

    PovrayProject getProject() {
        return proj;
    }

    private FileObject getRendererSettingsFolder() {
        String folderName = "Povray/RendererSettings";
        FileObject result = FileUtil.getConfigFile(folderName);
        if (result == null && !logged) {
            //Corrupted userdir or something is very very wrong.
            //Log it and move on.
            Exceptions.printStackTrace(
                    new IllegalStateException("Renderer settings dir missing!"));
            logged = true;
        }
        return result;
    }
    private static boolean logged = false;

    private FileObject fileFor(String settingsName) {
        FileObject settingsFolder = getRendererSettingsFolder();
        FileObject result;
        if (settingsFolder != null) { //should never be null
            result = settingsFolder.getFileObject(settingsName);
        } else {
            result = null;
        }
        return result;
    }

    private void setPreferredRendererSettingsName(String val) {
        getPreferences().put(KEY_PREFERRED_SETTINGS, val);
    }
    private static final String KEY_PREFERRED_SETTINGS = "preferredSettings";

    static Preferences getPreferences() {
        return Preferences.userNodeForPackage(RendererServiceImpl.class);
    }

    @Override
    public FileObject render(FileObject scene, String propertiesName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject render(FileObject scene, Properties renderSettings) {
        Povray pov = new Povray(this, scene, renderSettings);
        FileObject result;
        try {
            result = pov.render();
            if (!listeningToImagesFolder) {
                startListeningToImagesFolder();
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            result = null;
        }
        return result;
    }

    @Override
    public FileObject render(FileObject scene) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject render() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getAvailableRendererSettingsNames() {
        FileObject settingsFolder = getRendererSettingsFolder();
        String[] result;
        if (settingsFolder != null) {
            //Use a DataFolder here, so our ordering attributes in the layer
            //file are applied, and our returned String array will be in the
            //order we want
            DataFolder fld = DataFolder.findFolder(settingsFolder);
            DataObject[] kids = fld.getChildren();
            result = new String[kids.length];
            for (int i = 0; i < kids.length; i++) {
                result[i] = kids[i].getPrimaryFile().getNameExt();
            }
        } else {
            result = new String[0];
        }
        return result;
    }

    @Override
    public Properties getRendererSettings(String name) {
        Properties result = new Properties();
        FileObject settingsFile = fileFor(name);
        if (settingsFile != null) {
            try {
                result.load(new BufferedInputStream(settingsFile.getInputStream()));
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            Exceptions.printStackTrace(
                    new NullPointerException("Requested non-existent settings "
                    + "file " + name));
        }
        return result;
    }

    @Override
    public String getPreferredRendererSettingsNames() {
        String result = getPreferences().get(KEY_PREFERRED_SETTINGS, null);
        if (result == null) {
            result = "640x480.properties";
        }
        return result;
    }

    @Override
    public String getDisplayName(String settingsName) {
        FileObject file = fileFor(settingsName);
        String result;
        if (file != null) {
            DataObject dob;
            try {
                dob = DataObject.find(file);
                result = dob.getNodeDelegate().getDisplayName();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                result = "[error]";
            }
        } else {
            result = "";
        }
        return result;
    }
    //Keep a list of the paths we are currently listening to
    private Set scenesListenedTo = new HashSet();
    private boolean listeningToImagesFolder = false;

    @Override
    protected void listenerAdded(FileObject scene, ChangeListener l) {
        synchronized (this) {
            if (scenesListenedTo.add(scene.getPath())) {
                if (scenesListenedTo.size() == 1 || !listeningToImagesFolder) {
                    //This is the first call, so we should start listening
                    //on the images folder
                    startListeningToImagesFolder();
                }
                listenTo(scene);
            }
        }
    }

    @Override
    protected void noLongerListeningTo(FileObject scene) {
        synchronized (this) {
            scenesListenedTo.remove(scene.getPath());
        }
    }

    private void startListeningToImagesFolder() {
        FileObject imageFolder = proj.getImagesFolder(false);
        listeningToImagesFolder = imageFolder != null;
        if (listeningToImagesFolder) {
            listenTo(imageFolder);
        }
    }

    private void listenTo(FileObject file) {
        //Add ourselves as a weak listener to the file.  This way we can still
        //be garbage collected if the project is closed
        FileChangeListener stub = (FileChangeListener) WeakListeners.create(
                FileChangeListener.class, this, file);

        file.addFileChangeListener(stub);
    }

    @Override
    public void fileFolderCreated(FileEvent fileEvent) {
        //Do nothing
    }

    @Override
    public void fileDataCreated(FileEvent fileEvent) {
        FileObject created = fileEvent.getFile();
        fireSceneChange(created);
    }

    @Override
    public void fileChanged(FileEvent fileEvent) {
        FileObject changed = fileEvent.getFile();
        fireSceneChange(changed);
    }

    @Override
    public void fileDeleted(FileEvent fileEvent) {
        FileObject deleted = fileEvent.getFile();
        fireSceneChange(deleted);
        if (deleted.isFolder() && "images".equals(deleted.getNameExt())) {
            //The images folder was deleted, reset our listening flags
            fireSceneChange(null);
            listeningToImagesFolder = false;
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fileRenameEvent) {
        //do nothing
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        //do nothing
    }
}
