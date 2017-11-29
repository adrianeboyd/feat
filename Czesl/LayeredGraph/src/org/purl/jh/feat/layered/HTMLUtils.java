/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.purl.jh.feat.layered;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;

import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.MutableAttributeSet;

/**
 * Adapted from: http://www.rgagnon.com/javadetails/java-0424.html
 *
 * @author adriane
 */
public class HTMLUtils {

    private HTMLUtils() {
    }

    public static List<String> extractText(Reader reader) throws IOException {
        final ArrayList<String> list = new ArrayList<>();

        ParserDelegator parserDelegator = new ParserDelegator();
        ParserCallback parserCallback = new ParserCallback() {
            @Override
            public void handleText(final char[] data, final int pos) {
                list.add(new String(data));
            }

            @Override
            public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
            }

            @Override
            public void handleEndTag(Tag t, final int pos) {
            }

            @Override
            public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) {
            }

            @Override
            public void handleComment(final char[] data, final int pos) {
            }

            @Override
            public void handleError(final java.lang.String errMsg, final int pos) {
            }
        };
        parserDelegator.parse(reader, parserCallback, true);
        return list;
    }
}
