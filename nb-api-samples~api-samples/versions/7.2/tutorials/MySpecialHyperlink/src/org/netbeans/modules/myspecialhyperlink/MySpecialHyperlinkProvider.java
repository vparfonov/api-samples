package org.netbeans.modules.myspecialhyperlink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class MySpecialHyperlinkProvider implements HyperlinkProvider {

    public static final Pattern MY_SPECIAL_PATTERN =
            Pattern.compile(".*\\[myspecial:(.*?):myspecial\\].*");
    private String target;
    private int targetStart;
    private int targetEnd;

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        return verifyState(doc, offset);
    }

    public boolean verifyState(Document doc, int offset) {
        TokenHierarchy hi = TokenHierarchy.get(doc);
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        if (ts != null) {
            ts.move(offset);
            ts.moveNext();
            Token<HTMLTokenId> tok = ts.token();
            int newOffset = ts.offset();
            String matcherText = tok.text().toString();
            Matcher m = MY_SPECIAL_PATTERN.matcher(matcherText);
            if (m.matches()) {
                target = m.group(1);
                int idx = matcherText.indexOf(target);
                targetStart = newOffset + idx;
                targetEnd = targetStart + target.length();
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getHyperlinkSpan(Document document, int offset) {
        if (verifyState(document, offset)) {
            return new int[]{targetStart, targetEnd};
        } else {
            return null;
        }
    }

    @Override
    public void performClickAction(Document document, int offset) {
        if (verifyState(document, offset)) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(target);
            DialogDisplayer.getDefault().notify(msg);
        }
    }
    
}
