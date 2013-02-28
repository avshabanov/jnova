/*
 * Copyright 2013 Alexander Shabanov - http://alexshabanov.com.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truward.jnova.java.parser.impl;

import com.truward.jnova.java.code.PredefinedNames;
import com.truward.jnova.java.parser.Lexer;
import com.truward.jnova.java.parser.Token;
import com.truward.jnova.java.source.*;
import com.truward.jnova.util.diagnostics.DiagnosticsLog;
import com.truward.jnova.util.diagnostics.parameter.Offset;
import com.truward.jnova.util.naming.Symbol;
import com.truward.jnova.util.naming.SymbolTable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.truward.jnova.java.parser.Token.*;

/**
 * Lexer implementation.
 */
public final class LexerImpl implements Lexer {

    //
    // Output variables; set by nextToken():
    //

    /**
     * The token, set by nextToken().
     */
    private Token token;

    /**
     * Allow hex floating-point literals.
     */
    private boolean allowHexFloats;

    /**
     * The token's position, 0-based offset from beginning of text.
     */
    private int pos;

    /**
     * Character position just after the last character of the token.
     */
    private int endPos;

    /**
     * The last character position of the previous token.
     */
    private int prevEndPos;

    /**
     * The position where a lexical error occurred;
     */
    private int errPos = Position.NOPOS;

    /**
     * The name of an identifier or token:
     */
    private Symbol name;

    /**
     * The radix of a numeric literal token.
     */
    private int radix;

    /**
     * Determines whether to debug scanner or not.
     */
    private boolean scannerDebug;

    /**
     * Has a @deprecated been encountered in last doc comment?
     * This needs to be reset by client.
     */
    protected boolean deprecatedFlag;

    /**
     * A character buffer for literals.
     */
    private char[] sbuf = new char[128];
    private int sp;

    /**
     * The input buffer, index of next chacter to be read, index of one past last character in buffer.
     */
    private char[] buf;
    private int bp;
    private int buflen;
    private int eofPos;

    /**
     * The current character.
     */
    private char ch;

    /**
     * The buffer index of the last converted unicode character
     */
    private int unicodeConversionBp = -1;

    /**
     * The log to be used for error reporting.
     */
    @Resource
    private DiagnosticsLog log;

    @Resource
    private ParserBundle bundle;

    @Resource
    private Source source;

    @Resource
    private PredefinedNames names;

    @Resource
    private Keywords keywords;

    @Resource
    private SymbolTable symTable;

    private static final boolean hexFloatsWork = hexFloatsWork();

    private static boolean hexFloatsWork() {
        try {
            Float.valueOf("0x1.0p1");
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }



    @PostConstruct
    public void postConstruct() {
        allowHexFloats = source.allowHexFloats();
        scannerDebug = false; // TODO: get scanner debug variable from somewhere
    }

    @Override
    public void setSource(char[] input, int inputLength) {
        eofPos = inputLength;
        if (inputLength == input.length) {
            if (input.length > 0 && Character.isWhitespace(input[input.length - 1])) {
                inputLength--;
            } else {
                char[] newInput = new char[inputLength + 1];
                System.arraycopy(input, 0, newInput, 0, input.length);
                input = newInput;
            }
        }
        buf = input;
        buflen = inputLength;
        buf[buflen] = LayoutCharacters.EOI;
        bp = -1;
        scanChar();
    }

    /**
     * Report an error at the given position using the provided arguments.
     * @param pos Position in the source stream.
     * @param key Error key.
     * @param args Error arguments.
     */
    private void lexError(int pos, String key, Object... args) {
        log.error(bundle.message(key, args), Offset.at(pos));
        token = ERROR;
        errPos = pos;
    }

    /**
     * Report an error at the current token position using the provided arguments.
     * @param key Error key.
     * @param args Error arguments.
     */
    private void lexError(String key, Object... args) {
        lexError(pos, key, args);
    }

    /**
     * Convert an ASCII digit from its base (8, 10, or 16) to its value.
     * @param base Radix base.
     * @return Numeric digit.
     */
    private int digit(int base) {
        char c = ch;
        int result = Character.digit(c, base);
        if (result >= 0 && c > 0x7f) {
            lexError(pos+1, "illegal.nonascii.digit");
            ch = "0123456789abcdef".charAt(result);
        }
        return result;
    }

//    /**
//     * Returns a copy of the input buffer, up to its inputLength.
//     * Unicode escape sequences are not translated.
//     * @return Character array.
//     */
//    public char[] getRawCharacters() {
//        char[] chars = new char[buflen];
//        System.arraycopy(buf, 0, chars, 0, buflen);
//        return chars;
//    }

    /**
     * Returns a copy of a character array subset of the input buffer.
     * The returned array begins at the <code>beginIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     * Thus the length of the substring is <code>endIndex-beginIndex</code>.
     * This behavior is like
     * <code>String.substring(beginIndex, endIndex)</code>.
     * Unicode escape sequences are not translated.
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     * @return Character array.
     * @throws IndexOutOfBoundsException if either offset is outside of the
     *         array bounds
     */
    public char[] getRawCharacters(int beginIndex, int endIndex) {
        int length = endIndex - beginIndex;
        char[] chars = new char[length];
        System.arraycopy(buf, beginIndex, chars, 0, length);
        return chars;
    }

    /**
     * Convert unicode escape; bp points to initial '\' character (Spec 3.3).
     */
    private void convertUnicode() {
        if (ch == '\\' && unicodeConversionBp != bp) {
            bp++; ch = buf[bp];
            if (ch == 'u') {
                do {
                    bp++; ch = buf[bp];
                } while (ch == 'u');
                int limit = bp + 3;
                if (limit < buflen) {
                    int d = digit(16);
                    int code = d;
                    while (bp < limit && d >= 0) {
                        bp++; ch = buf[bp];
                        d = digit(16);
                        code = (code << 4) + d;
                    }
                    if (d >= 0) {
                        ch = (char)code;
                        unicodeConversionBp = bp;
                        return;
                    }
                }
                lexError(bp, "illegal.unicode.esc");
            } else {
                bp--;
                ch = '\\';
            }
        }
    }

    /**
     * Read next character.
     */
    private void scanChar() {
        ch = buf[++bp];
        if (ch == '\\') {
            convertUnicode();
        }
    }

    /**
     * Read next character in comment, skipping over double '\' characters.
     */
    private void scanCommentChar() {
        scanChar();
        if (ch == '\\') {
            if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
                bp++;
            } else {
                convertUnicode();
            }
        }
    }

    /**
     * Append a character to sbuf.
     * @param ch Char to be appended.
     */
    private void putChar(char ch) {
        if (sp == sbuf.length) {
            char[] newsbuf = new char[sbuf.length * 2];
            System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
            sbuf = newsbuf;
        }
        sbuf[sp++] = ch;
    }

    /** Read next character in character or string literal and copy into sbuf.
     */
    private void scanLitChar() {
        if (ch == '\\') {
            if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
                bp++;
                putChar('\\');
                scanChar();
            } else {
                scanChar();
                switch (ch) {
                case '0': case '1': case '2': case '3':
                case '4': case '5': case '6': case '7':
                    char leadch = ch;
                    int oct = digit(8);
                    scanChar();
                    if ('0' <= ch && ch <= '7') {
                        oct = oct * 8 + digit(8);
                        scanChar();
                        if (leadch <= '3' && '0' <= ch && ch <= '7') {
                            oct = oct * 8 + digit(8);
                            scanChar();
                        }
                    }
                    putChar((char)oct);
                    break;
                case 'b':
                    putChar('\b'); scanChar(); break;
                case 't':
                    putChar('\t'); scanChar(); break;
                case 'n':
                    putChar('\n'); scanChar(); break;
                case 'f':
                    putChar('\f'); scanChar(); break;
                case 'r':
                    putChar('\r'); scanChar(); break;
                case '\'':
                    putChar('\''); scanChar(); break;
                case '\"':
                    putChar('\"'); scanChar(); break;
                case '\\':
                    putChar('\\'); scanChar(); break;
                default:
                    lexError(bp, "illegal.esc.char");
                }
            }
        } else if (bp != buflen) {
            putChar(ch); scanChar();
        }
    }

    /** Read fractional part of hexadecimal floating point number.
     */
    private void scanHexExponentAndSuffix() {
        if (ch == 'p' || ch == 'P') {
            putChar(ch);
            scanChar();
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }
            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');
                if (!allowHexFloats) {
                    lexError("unsupported.fp.lit");
                    allowHexFloats = true;
                }
                else if (!hexFloatsWork)
                    lexError("unsupported.cross.fp.lit");
            } else
                lexError("malformed.fp.lit");
        } else {
            lexError("malformed.fp.lit");
        }
        if (ch == 'f' || ch == 'F') {
            putChar(ch);
            scanChar();
            token = FLOATLITERAL;
        } else {
            if (ch == 'd' || ch == 'D') {
                putChar(ch);
                scanChar();
            }
            token = DOUBLELITERAL;
        }
    }

    /** Read fractional part of floating point number.
     */
    private void scanFraction() {
        while (digit(10) >= 0) {
            putChar(ch);
            scanChar();
        }
        int sp1 = sp;
        if (ch == 'e' || ch == 'E') {
            putChar(ch);
            scanChar();
            if (ch == '+' || ch == '-') {
                putChar(ch);
                scanChar();
            }
            if ('0' <= ch && ch <= '9') {
                do {
                    putChar(ch);
                    scanChar();
                } while ('0' <= ch && ch <= '9');
                return;
            }
            lexError("malformed.fp.lit");
            sp = sp1;
        }
    }

    /**
     * Read fractional part and 'd' or 'f' suffix of floating point number.
     */
    private void scanFractionAndSuffix() {
        this.radix = 10;
        scanFraction();
        if (ch == 'f' || ch == 'F') {
            putChar(ch);
            scanChar();
            token = FLOATLITERAL;
        } else {
            if (ch == 'd' || ch == 'D') {
                putChar(ch);
                scanChar();
            }
            token = DOUBLELITERAL;
        }
    }

    /**
     * Read fractional part and 'd' or 'f' suffix of floating point number.
     * @param seendigit True if digit has been seen.
     */
    private void scanHexFractionAndSuffix(boolean seendigit) {
        this.radix = 16;
        assert ch == '.';
        putChar(ch);
        scanChar();
        while (digit(16) >= 0) {
            seendigit = true;
            putChar(ch);
            scanChar();
        }
        if (!seendigit)
            lexError("invalid.hex.number");
        else
            scanHexExponentAndSuffix();
    }

    /** Read a number.
     *  @param radix  The radix of the number; one of 8, 10, 16.
     */
    private void scanNumber(int radix) {
        this.radix = radix;
        // for octal, allow base-10 digit in case it's a float literal
        int digitRadix = (radix <= 10) ? 10 : 16;
        boolean seendigit = false;
        while (digit(digitRadix) >= 0) {
            seendigit = true;
            putChar(ch);
            scanChar();
        }
        if (radix == 16 && ch == '.') {
            scanHexFractionAndSuffix(seendigit);
        } else if (seendigit && radix == 16 && (ch == 'p' || ch == 'P')) {
            scanHexExponentAndSuffix();
        } else if (radix <= 10 && ch == '.') {
            putChar(ch);
            scanChar();
            scanFractionAndSuffix();
        } else if (radix <= 10 &&
                   (ch == 'e' || ch == 'E' ||
                    ch == 'f' || ch == 'F' ||
                    ch == 'd' || ch == 'D')) {
            scanFractionAndSuffix();
        } else {
            if (ch == 'l' || ch == 'L') {
                scanChar();
                token = LONGLITERAL;
            } else {
                token = INTLITERAL;
            }
        }
    }

    /**
     * Read an identifier.
     */
    @SuppressWarnings({"ConstantConditions"})
    private void scanIdent() {
        boolean isJavaIdentifierPart;
        char high;
        do {
            if (sp == sbuf.length) putChar(ch); else sbuf[sp++] = ch;
            // optimization, was: putChar(ch);

            scanChar();
            switch (ch) {
            case 'A': case 'B': case 'C': case 'D': case 'E':
            case 'F': case 'G': case 'H': case 'I': case 'J':
            case 'K': case 'L': case 'M': case 'N': case 'O':
            case 'P': case 'Q': case 'R': case 'S': case 'T':
            case 'U': case 'V': case 'W': case 'X': case 'Y':
            case 'Z':
            case 'a': case 'b': case 'c': case 'd': case 'e':
            case 'f': case 'g': case 'h': case 'i': case 'j':
            case 'k': case 'l': case 'm': case 'n': case 'o':
            case 'p': case 'q': case 'r': case 's': case 't':
            case 'u': case 'v': case 'w': case 'x': case 'y':
            case 'z':
            case '$': case '_':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            case '\u0000': case '\u0001': case '\u0002': case '\u0003':
            case '\u0004': case '\u0005': case '\u0006': case '\u0007':
            case '\u0008': case '\u000E': case '\u000F': case '\u0010':
            case '\u0011': case '\u0012': case '\u0013': case '\u0014':
            case '\u0015': case '\u0016': case '\u0017':
            case '\u0018': case '\u0019': case '\u001B':
            case '\u007F':
                break;
            case '\u001A': // EOI is also a legal identifier part
                if (bp >= buflen) {
                    name = symTable.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
                    return;
                }
                break;
            default:
                if (ch < '\u0080') {
                    // all ASCII range chars already handled, above
                    isJavaIdentifierPart = false;
                } else {
                    high = scanSurrogates();
                    if (high != 0) {
                        if (sp == sbuf.length) {
                            putChar(high);
                        } else {
                            sbuf[sp++] = high;
                        }
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(
                            Character.toCodePoint(high, ch));
                    } else {
                        isJavaIdentifierPart = Character.isJavaIdentifierPart(ch);
                    }
                }
                if (!isJavaIdentifierPart) {
                    name = symTable.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
                    return;
                }
            }
        } while (true);
    }



    /**
     * Are surrogates supported?
     */
    final static boolean surrogatesSupported = surrogatesSupported();
    private static boolean surrogatesSupported() {
        try {
            Character.isHighSurrogate('a');
            return true;
        } catch (NoSuchMethodError ex) {
            return false;
        }
    }

    /**
     * Scan surrogate pairs.  If 'ch' is a high surrogate and the next character is a low surrogate,
     * then put the low surrogate in 'ch', and return the high surrogate. otherwise, just return 0.
     *
     * @return High surrogate or 0.
     */
    private char scanSurrogates() {
        if (surrogatesSupported && Character.isHighSurrogate(ch)) {
            char high = ch;

            scanChar();

            if (Character.isLowSurrogate(ch)) {
                return high;
            }

            ch = high;
        }

        return 0;
    }

    /**
     * Return true if ch can be part of an operator.
     * @param ch Character to be tested.
     * @return True, if ch can be a part of an operator.
     */
    private boolean isSpecial(char ch) {
        switch (ch) {
        case '!': case '%': case '&': case '*': case '?':
        case '+': case '-': case ':': case '<': case '=':
        case '>': case '^': case '|': case '~':
        case '@':
            return true;
        default:
            return false;
        }
    }

    /** Read longest possible sequence of special characters and convert
     *  to token.
     */
    private void scanOperator() {
        while (true) {
            putChar(ch);
            Symbol newname = symTable.fromChars(sbuf, 0, sp);
            if (keywords.key(newname) == IDENTIFIER) {
                sp--;
                break;
            }
            name = newname;
            token = keywords.key(newname);
            scanChar();
            if (!isSpecial(ch)) break;
        }
    }

    /**
     * Scan a documention comment; determine if a deprecated tag is present.
     * Called once the initial /, * have been skipped, positioned at the second *
     * (which is treated as the beginning of the first line).
     * Stops positioned at the closing '/'.
     */
    @SuppressWarnings({"fallthrough", "ConstantConditions"})
    private void scanDocComment() {
        boolean deprecatedPrefix;

        forEachLine:
        while (bp < buflen) {

            // Skip optional WhiteSpace at beginning of line
            while (bp < buflen && (ch == ' ' || ch == '\t' || ch == LayoutCharacters.FF)) {
                scanCommentChar();
            }

            // Skip optional consecutive Stars
            while (bp < buflen && ch == '*') {
                scanCommentChar();
                if (ch == '/') {
                    return;
                }
            }

            // Skip optional WhiteSpace after Stars
            while (bp < buflen && (ch == ' ' || ch == '\t' || ch == LayoutCharacters.FF)) {
                scanCommentChar();
            }

            deprecatedPrefix = false;
            // At beginning of line in the JavaDoc sense.
            if (bp < buflen && ch == '@' && !deprecatedFlag) {
                scanCommentChar();
                if (bp < buflen && ch == 'd') {
                    scanCommentChar();
                    if (bp < buflen && ch == 'e') {
                        scanCommentChar();
                        if (bp < buflen && ch == 'p') {
                            scanCommentChar();
                            if (bp < buflen && ch == 'r') {
                                scanCommentChar();
                                if (bp < buflen && ch == 'e') {
                                    scanCommentChar();
                                    if (bp < buflen && ch == 'c') {
                                        scanCommentChar();
                                        if (bp < buflen && ch == 'a') {
                                            scanCommentChar();
                                            if (bp < buflen && ch == 't') {
                                                scanCommentChar();
                                                if (bp < buflen && ch == 'e') {
                                                    scanCommentChar();
                                                    if (bp < buflen && ch == 'd') {
                                                        deprecatedPrefix = true;
                                                        scanCommentChar();
                                                    }}}}}}}}}}}
            if (deprecatedPrefix && bp < buflen) {
                if (Character.isWhitespace(ch)) {
                    deprecatedFlag = true;
                } else if (ch == '*') {
                    scanCommentChar();
                    if (ch == '/') {
                        deprecatedFlag = true;
                        return;
                    }
                }
            }

            // Skip rest of line
            while (bp < buflen) {
                switch (ch) {
                case '*':
                    scanCommentChar();
                    if (ch == '/') {
                        return;
                    }
                    break;
                case LayoutCharacters.CR: // (Spec 3.4)
                    scanCommentChar();
                    if (ch != LayoutCharacters.LF) {
                        continue forEachLine;
                    }
                    /* fall through to LF case */
                case LayoutCharacters.LF: // (Spec 3.4)
                    scanCommentChar();
                    continue forEachLine;
                default:
                    scanCommentChar();
                }
            } // rest of line
        } // forEachLine
    }

    /**
     * Called when a complete comment has been scanned. pos and endPos
     * will mark the comment boundary.
     * @param style Style of the comment.
     */
    protected void processComment(CommentStyle style) {
        if (scannerDebug) {
            System.out.println("processComment(" + pos + "," + endPos + "," + style + ")=|" +
                    new String(getRawCharacters(pos, endPos)) + "|");
        }
    }

    /**
     * Called when a complete whitespace run has been scanned. pos and endPos
     * will mark the whitespace boundary.
     */
    protected void processWhiteSpace() {
        if (scannerDebug) {
            System.out.println("processWhitespace(" + pos + "," + endPos + ")=|" +
                    new String(getRawCharacters(pos, endPos)) + "|");
        }
    }

    /**
     * Called when a line terminator has been processed.
     */
    protected void processLineTerminator() {
        if (scannerDebug) {
            System.out.println("processWhitespace(" + pos + "," + endPos + ")=|" +
                    new String(getRawCharacters(pos, endPos)) + "|");
        }
    }


    @Override
    public String stringVal() {
        return new String(sbuf, 0, sp);
    }

    @Override
    @SuppressWarnings({"ConstantConditions"})
    public void nextToken() {
        try {
            prevEndPos = endPos;
            sp = 0;

            while (true) {
                pos = bp;
                switch (ch) {
                    case ' ': // (Spec 3.6)
                    case '\t': // (Spec 3.6)
                    case LayoutCharacters.FF: // (Spec 3.6)
                        do {
                            scanChar();
                        } while (ch == ' ' || ch == '\t' || ch == LayoutCharacters.FF);
                        endPos = bp;
                        processWhiteSpace();
                        break;
                    case LayoutCharacters.LF: // (Spec 3.4)
                        scanChar();
                        endPos = bp;
                        processLineTerminator();
                        break;
                    case LayoutCharacters.CR: // (Spec 3.4)
                        scanChar();
                        if (ch == LayoutCharacters.LF) {
                            scanChar();
                        }
                        endPos = bp;
                        processLineTerminator();
                        break;
                    case 'A': case 'B': case 'C': case 'D': case 'E':
                    case 'F': case 'G': case 'H': case 'I': case 'J':
                    case 'K': case 'L': case 'M': case 'N': case 'O':
                    case 'P': case 'Q': case 'R': case 'S': case 'T':
                    case 'U': case 'V': case 'W': case 'X': case 'Y':
                    case 'Z':
                    case 'a': case 'b': case 'c': case 'd': case 'e':
                    case 'f': case 'g': case 'h': case 'i': case 'j':
                    case 'k': case 'l': case 'm': case 'n': case 'o':
                    case 'p': case 'q': case 'r': case 's': case 't':
                    case 'u': case 'v': case 'w': case 'x': case 'y':
                    case 'z':
                    case '$': case '_':
                        scanIdent();
                        return;
                    case '0':
                        scanChar();
                        if (ch == 'x' || ch == 'X') {
                            scanChar();
                            if (ch == '.') {
                                scanHexFractionAndSuffix(false);
                            } else if (digit(16) < 0) {
                                lexError("invalid.hex.number");
                            } else {
                                scanNumber(16);
                            }
                        } else {
                            putChar('0');
                            scanNumber(8);
                        }
                        return;
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        scanNumber(10);
                        return;
                    case '.':
                        scanChar();
                        if ('0' <= ch && ch <= '9') {
                            putChar('.');
                            scanFractionAndSuffix();
                        } else if (ch == '.') {
                            putChar('.'); putChar('.');
                            scanChar();
                            if (ch == '.') {
                                scanChar();
                                putChar('.');
                                token = ELLIPSIS;
                            } else {
                                lexError("malformed.fp.lit");
                            }
                        } else {
                            token = DOT;
                        }
                        return;
                    case ',':
                        scanChar(); token = COMMA; return;
                    case ';':
                        scanChar(); token = SEMI; return;
                    case '(':
                        scanChar(); token = LPAREN; return;
                    case ')':
                        scanChar(); token = RPAREN; return;
                    case '[':
                        scanChar(); token = LBRACKET; return;
                    case ']':
                        scanChar(); token = RBRACKET; return;
                    case '{':
                        scanChar(); token = LBRACE; return;
                    case '}':
                        scanChar(); token = RBRACE; return;
                    case '/':
                        scanChar();
                        if (ch == '/') {
                            do {
                                scanCommentChar();
                            } while (ch != LayoutCharacters.CR && ch != LayoutCharacters.LF && bp < buflen);
                            if (bp < buflen) {
                                endPos = bp;
                                processComment(CommentStyle.LINE);
                            }
                            break;
                        } else if (ch == '*') {
                            scanChar();
                            CommentStyle style;
                            if (ch == '*') {
                                style = CommentStyle.JAVADOC;
                                scanDocComment();
                            } else {
                                style = CommentStyle.BLOCK;
                                while (bp < buflen) {
                                    if (ch == '*') {
                                        scanChar();
                                        if (ch == '/') break;
                                    } else {
                                        scanCommentChar();
                                    }
                                }
                            }
                            if (ch == '/') {
                                scanChar();
                                endPos = bp;
                                processComment(style);
                                break;
                            } else {
                                lexError("unclosed.comment");
                                return;
                            }
                        } else if (ch == '=') {
                            name = names.slashequals;
                            token = SLASHEQ;
                            scanChar();
                        } else {
                            name = names.slash;
                            token = SLASH;
                        }
                        return;
                    case '\'':
                        scanChar();
                        if (ch == '\'') {
                            lexError("empty.char.lit");
                        } else {
                            if (ch == LayoutCharacters.CR || ch == LayoutCharacters.LF)
                                lexError(pos, "illegal.line.end.in.char.lit");
                            scanLitChar();
                            if (ch == '\'') {
                                scanChar();
                                token = CHARLITERAL;
                            } else {
                                lexError(pos, "unclosed.char.lit");
                            }
                        }
                        return;
                    case '\"':
                        scanChar();
                        while (ch != '\"' && ch != LayoutCharacters.CR && ch != LayoutCharacters.LF && bp < buflen)
                            scanLitChar();
                        if (ch == '\"') {
                            token = STRINGLITERAL;
                            scanChar();
                        } else {
                            lexError(pos, "unclosed.str.lit");
                        }
                        return;
                    default:
                        if (isSpecial(ch)) {
                            scanOperator();
                        } else {
                            boolean isJavaIdentifierStart;
                            if (ch < '\u0080') {
                                // all ASCII range chars already handled, above
                                isJavaIdentifierStart = false;
                            } else {
                                char high = scanSurrogates();
                                if (high != 0) {
                                    if (sp == sbuf.length) {
                                        putChar(high);
                                    } else {
                                        sbuf[sp++] = high;
                                    }

                                    isJavaIdentifierStart = Character.isJavaIdentifierStart(
                                            Character.toCodePoint(high, ch));
                                } else {
                                    isJavaIdentifierStart = Character.isJavaIdentifierStart(ch);
                                }
                            }
                            if (isJavaIdentifierStart) {
                                scanIdent();
                            } else if (bp == buflen || ch == LayoutCharacters.EOI && bp+1 == buflen) { // JLS 3.5
                                token = EOF;
                                pos = bp = eofPos;
                            } else {
                                lexError("illegal.char", String.valueOf((int)ch));
                                scanChar();
                            }
                        }
                        return;
                }
            }
        } finally {
            endPos = bp;

            if (scannerDebug) {
                System.out.println("nextToken(" + pos + "," + endPos + ")=|" +
                        new String(getRawCharacters(pos, endPos)) + "|");
            }
        }
    }

    @Override
    public Symbol name() {
        return name;
    }

    @Override
    public int prevEndPos() {
        return prevEndPos;
    }

    @Override
    public int pos() {
        return pos;
    }

    @Override
    public int errPos() {
        return errPos;
    }

    @Override
    public void setErrPos(int pos) {
        errPos = pos;
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public int radix() {
        return radix;
    }

    @Override
    public String docComment() {
        return null;
    }

    @Override
    public boolean deprecatedFlag() {
        return deprecatedFlag;
    }

    @Override
    public void resetDeprecatedFlag() {
        deprecatedFlag = false;
    }
}
