package org.netbeans.modules.countries;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class StandardCompletionItem implements CompletionItem {

    private int dotOffset;
    private String text;
    private static ImageIcon fieldIcon;
    private static Color fieldColor = Color.decode("0x0000B2");
    private int caretOffset;

    public StandardCompletionItem(String text, int dotOffset, int caretOffset, int type) {
        this.text = text;
        this.dotOffset = dotOffset;
        this.caretOffset = caretOffset;
        if (type == 1){
            fieldIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/countries/resources/country.png"));
        } else {
            fieldIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/countries/resources/city.png"));
        } 
    }

    @Override
    public void defaultAction(JTextComponent component) {
        try {
            StyledDocument doc = (StyledDocument) component.getDocument();
            //Here we remove the characters starting at the start offset
            //and ending at the point where the caret is currently found:
            doc.remove(dotOffset, caretOffset - dotOffset);
            doc.insertString(dotOffset, text, null);
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
        return CompletionUtilities.getPreferredWidth(text, null, graphics, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(fieldIcon, text, null, g, defaultFont,
                (selected ? Color.white : fieldColor), width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                completionResultSet.setDocumentation(new CountriesCompletionDocumentation(StandardCompletionItem.this));
                completionResultSet.finish();
            }
        });
    }

    public class CountriesCompletionDocumentation implements CompletionDocumentation {

        private StandardCompletionItem item;

        public CountriesCompletionDocumentation(StandardCompletionItem item) {
            this.item = item;
        }

        @Override
        public String getText() {
            return "Information about " + item.text;
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String string) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
        
    }

    @Override
    public CompletionTask createToolTipTask() {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                JToolTip toolTip = new JToolTip();
                toolTip.setTipText("Press Enter to insert \"" + text + "\"");
                completionResultSet.setToolTip(toolTip);
                completionResultSet.finish();
            }
        });
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }
}
