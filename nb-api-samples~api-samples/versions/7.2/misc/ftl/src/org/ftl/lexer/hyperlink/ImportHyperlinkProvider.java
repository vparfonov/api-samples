/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ftl.lexer.hyperlink;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.ftl.lexer.SJTokenId;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

@MimeRegistration(mimeType = "text/html", service = HyperlinkProviderExt.class)
public class ImportHyperlinkProvider implements HyperlinkProviderExt {

    private int startOffset, endOffset;

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        String text = null;
        try {
            int idx = doc.getText(startOffset, endOffset - startOffset).lastIndexOf("/");
            text = doc.getText(startOffset, endOffset - startOffset).substring(idx);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "<html>Click to <b>open</b> <br/><font color=\"red\"" + text + "</font></html>";
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType ht) {
        try {
            String text = doc.getText(startOffset, endOffset - startOffset);
            FileObject fo = getFileObject(doc);
            String pathToFileToOpen = fo.getParent().getPath() + text;
            File fileToOpen = FileUtil.normalizeFile(new File(pathToFileToOpen));
            if (fileToOpen.exists()) {
                try {
                    FileObject foToOpen = FileUtil.toFileObject(fileToOpen);
                    DataObject.find(foToOpen).getLookup().lookup(OpenCookie.class).open();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                StatusDisplayer.getDefault().setStatusText(fileToOpen.getPath() + " doesn't exist!");
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        return od != null ? od.getPrimaryFile() : null;
    }

    private int[] getIdentifierSpan(Document doc, int offset) {
TokenHierarchy<?> th = TokenHierarchy.get(doc);
TokenSequence htmlTs = th.tokenSequence(Language.find("text/html"));
if (htmlTs == null || !htmlTs.moveNext() && !htmlTs.movePrevious()) {
    return null;
}
TokenSequence<SJTokenId> ts = htmlTs.embedded();
if (ts == null) {
    return null;
}
ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        Token t = ts.token();
        if (t.id().name().equals("STRING_LITERAL")) {
            //Correction for quotation marks around the token:
            startOffset = ts.offset() + 1;
            endOffset = ts.offset() + t.length() - 1;
            //Check that the previous token was an import statement,
            //otherwise we don't want our string literal hyperlinked:
            ts.movePrevious();
            Token prevToken = ts.token();
            if (prevToken.id().name().equals("IMPORT")) {
                return new int[]{startOffset, endOffset};
            } else {
                return null;
            }
        }
        return null;
    }
}
