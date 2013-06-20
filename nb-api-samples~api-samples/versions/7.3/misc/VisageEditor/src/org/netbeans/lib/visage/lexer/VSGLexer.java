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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.visage.lexer;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRReaderStream;
import org.netbeans.api.visage.lexer.VSGTokenId;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge between netbeans lexer system and ANTLR lexer based on compiler.
 *
 * @author Rastislav Komara (<a href="mailto:rastislav.komara@sun.com">RKo</a>)
 */
public class VSGLexer implements org.netbeans.spi.lexer.Lexer<VSGTokenId> {

    private static Logger log = Logger.getLogger(VSGLexer.class.getName());
    private Lexer lexer;
    private TokenFactory<VSGTokenId> tokenFactory;
    protected LexerInput lexerInput;
    private LexerRestartInfo<VSGTokenId> info;
    private long st;

    public VSGLexer(LexerRestartInfo<VSGTokenId> info) throws IOException {
        super();
        if (log.isLoggable(Level.FINE)) log.fine("Creating new lexer"); // NOI18N
        this.lexer = new v4Lexer();
        this.info = info;
    }

    private void configureLexer(LexerRestartInfo<VSGTokenId> info) {
        try {
            lexerInput = info.input();
            final LexerInputStream reader = new LexerInputStream();
            reader.setLexerInput(lexerInput);

            ANTLRReaderStream input = new ANTLRInputStream(reader, "UTF-8"); //NOI18N
            lexer = new v4Lexer(input);
            final LexerState ls = (LexerState) info.state();
            if (ls != null) {
                final Lexer.BraceQuoteTracker bqt = ls.getTracker(lexer);
                if (log.isLoggable(Level.FINE) && bqt != null) {
                    log.fine("StateIn: " + bqt.toString()); // NOI18N
                }
                lexer.setBraceQuoteTracker(bqt);
            }
            tokenFactory = info.tokenFactory();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Token<VSGTokenId> nextToken() {
        if (lexer == null) {
            throw new IllegalStateException("Internal implementation of lexer is null. You need to create new instance first!"); // NOI18N
        }

        if (info != null) {
            configureLexer(info);
            info = null;
            if (log.isLoggable(Level.FINE)) log.fine("Reseting lexer"); // NOI18N
        }
        st = System.currentTimeMillis();
        final org.antlr.runtime.Token token = lexer.nextToken();
        if (token.getType() == org.antlr.runtime.Token.EOF) {
            final int rl = lexerInput.readLength();
            if (rl > 0) {
                if (log.isLoggable(Level.WARNING))
                    log.warning("There are still " + rl + " characters unparsed."); // NOI18N
                return tokenFactory.createToken(VSGTokenId.UNKNOWN, rl);
            } else {
                return null;
            }
        }
        String text = token.getText();
        VSGTokenId id = getId(token);
        if (VSGTokenId.COMMENT == id && text.startsWith("/**")) { // NOI18N
            id = VSGTokenId.DOC_COMMENT;
        }
        assert id != null;
        String fixedText = id.getFixedText();
        return (fixedText != null)
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(
                    id,
                    text != null ? text.length() : 0,
                    lexer.getSharedState().failed ? PartType.START : PartType.COMPLETE);
    }

    private VSGTokenId getId(org.antlr.runtime.Token token) {
        return VSGTokenId.getId(token.getType());
    }

    public Object state() {
        final Lexer.BraceQuoteTracker bqt = lexer.getBraceQuoteTracker();
        if (log.isLoggable(Level.FINEST) && bqt != null) {
            log.finest("StateOut: " + bqt.toString()); // NOI18N
        }
        if (bqt == null) {
            return null;
        }
        return new LexerState(bqt);
    }

    public void release() {
        long tt = System.currentTimeMillis() - st;
        if (log.isLoggable(Level.FINE))
            log.fine("Releasing lexer @line: " + lexer.getLine() + " total time: " + tt + "ms"); // NOI18N
        lexer = null;
    }


    static class LexerInputStream extends InputStream {
        private LexerInput input;
        private byte[] bytes = new byte[0];
        private int i = 0;

        public void setLexerInput(LexerInput input) {
            this.input = input;
        }

        public int read() throws IOException {
            if (i >= bytes.length) {
                final int c = input.read();
                if (c == LexerInput.EOF) return -1;
                bytes = new String(new char[] {(char)c}).getBytes("UTF-8"); //NOI18N
                i = 0;
            }
            return bytes[i++];
        }
    }

    private static class LexerState {
        private final BQLexerContainer state;

        private LexerState(Lexer.BraceQuoteTracker tracker) {
            this.state = BQLexerContainer.freezeState(tracker);
        }

        public Lexer.BraceQuoteTracker getTracker(Lexer lexer) {
            return BQLexerContainer.unfreezeState(state, lexer);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LexerState that = (LexerState) o;

            return !(state != null ? !state.equals(that.state) : that.state != null);

        }

        public int hashCode() {
            return (state != null ? state.hashCode() : 0);
        }
    }

    private static class BQLexerContainer {
        private final int braceDepth;
        private final char quote;
        private final boolean percentIsFormat;
        private final BQLexerContainer previous;

        private BQLexerContainer(int braceDepth, char quote, boolean percentIsFormat, BQLexerContainer previous) {
            this.braceDepth = braceDepth;
            this.quote = quote;
            this.percentIsFormat = percentIsFormat;
            this.previous = previous;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BQLexerContainer that = (BQLexerContainer) o;

            if (braceDepth != that.braceDepth) return false;
            if (percentIsFormat != that.percentIsFormat) return false;
            if (quote != that.quote) return false;
            if (previous != null ? !previous.equals(that.previous) : that.previous != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = braceDepth;
            result = 31 * result + (int) quote;
            result = 31 * result + (percentIsFormat ? 1 : 0);
            result = 31 * result + (previous != null ? previous.hashCode() : 0);
            return result;
        }

        static BQLexerContainer freezeState(Lexer.BraceQuoteTracker t) {
            BQLexerContainer root = null;
            while (t != null) {
                root = new BQLexerContainer(t.getBraceDepth(), t.getQuote(), t.isPercentIsFormat(), root);
                t = t.getNext();
            }
            return root;
        }

        static Lexer.BraceQuoteTracker unfreezeState(BQLexerContainer c, Lexer lexer) {
            Lexer.BraceQuoteTracker root = null;
            while (c != null) {
                root = lexer.createBQT(root, c.quote, c.percentIsFormat);
                root.setBraceDepth(c.braceDepth);
                c = c.previous;
            }
            return root;
        }
    }
}
