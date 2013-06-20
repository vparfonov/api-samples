/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.newhtmlsnippets;

import javax.swing.text.*;
import org.openide.text.NbDocument;

/**
 *
 * @author geertjan
 */
public class HTMLPaletteUtilities {
    
     public static void insert(final String s, final JTextComponent target) throws BadLocationException {
        
        final StyledDocument doc = (StyledDocument)target.getDocument();
        
        class AtomicChange implements Runnable {
            
            @Override
            public void run() {
                Document value = target.getDocument();
                if (value == null)
                    return;
                try {
                    insert(s, target, doc);
                } catch (BadLocationException e) {}
            }
        }
        
        try {
            NbDocument.runAtomicAsUser(doc, new AtomicChange());
        } catch (BadLocationException ex) {}
        
    }
    
    private static int insert(String s, JTextComponent target, Document doc) throws BadLocationException {
        
        int start = -1;
        
        try {
            
            //firstly, find selected text range:
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //then, replace selected text range with the inserted one:
            start = caret.getDot();
            doc.insertString(start, s, null);
        
        } catch (BadLocationException ble) {}
        
        return start;

    }

}
