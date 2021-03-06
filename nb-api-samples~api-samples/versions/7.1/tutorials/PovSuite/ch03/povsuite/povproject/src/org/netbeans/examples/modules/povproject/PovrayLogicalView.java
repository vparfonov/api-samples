/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.examples.modules.povproject;

import java.awt.Image;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author geertjan
 */
class PovrayLogicalView implements LogicalViewProvider {

    private final PovrayProject project;

    public PovrayLogicalView(PovrayProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        try {

            //Get the scenes directory, creating if deleted
            FileObject scenes = project.getScenesFolder(true);

            //Get the DataObject that represents it
            DataFolder scenesDataObject =
                    DataFolder.findFolder(scenes);

            //Get its default node—we'll wrap our node around it to change the
            //display name, icon, etc.
            Node realScenesFolderNode = scenesDataObject.getNodeDelegate();

            //This FilterNode will be our project node
            return new ScenesNode(realScenesFolderNode, project);

        } catch (DataObjectNotFoundException donfe) {

	    Exceptions.printStackTrace(donfe);

            //Fallback—the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);

        }

    }

    /**
     * This is the node you actually see in the Projects window for the project
     */
    private static final class ScenesNode extends FilterNode {

        final PovrayProject project;

        public ScenesNode(Node node, PovrayProject project) throws DataObjectNotFoundException {
            super(node, new FilterNode.Children(node),
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both
                    new ProxyLookup(
                        Lookups.singleton(project),
                        node.getLookup())
                    );
            this.project = project;
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(
                    "org/netbeans/examples/modules/povproject/resources/scenes.gif");
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }

    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }
}
