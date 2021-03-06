package com.dbw.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dbw.state.XmlStateBuilder;
import com.dbw.state.XmlStateTag;
import com.google.inject.Singleton;

@Singleton
public class HtmlInXmlEscaper {
    private final String[][] htmlCharsWithReplacements = new String[][]{
        {"&", "&amp;"},
        {">", "&gt;"},
        {"<", "&lt;"},
        {"\"", "&quot;"},
        {"'", "&apos;"},
        {"/", "&#47;"},
        {"\\", "&#92;"}
    };

    public boolean isHtmlBetweenXmlTags(String data) {
        int expectedGtOrLtSymbols = 4 + countColumnStateTags(data) * 2;
        long numberOfGtSymbols = data.chars().filter(ch -> ch == '>').count();
        long numberOfLtSymbols = data.chars().filter(ch -> ch == '<').count();
        return numberOfGtSymbols > expectedGtOrLtSymbols || numberOfLtSymbols > expectedGtOrLtSymbols;
    }

    private int countColumnStateTags(String data) {
        Pattern pattern = Pattern.compile(String.format("<%s\\s.*?>", XmlStateBuilder.XML_COLUMN_STATE_TAG));
        Matcher matcher = pattern.matcher(data);
        int count = 0;
        while (matcher.find()){
            count +=1;
        }
        return count;
    }

    public String escapeHtmlBetweenXmlTags(String xmlData, XmlStateTag xmlColumnStateTag) {
        String escapedData = xmlData;

        String xmlColumnStateStartTag = xmlColumnStateTag.startTag();
        Pattern xmlColumnStateStartTagPattern = Pattern.compile(xmlColumnStateStartTag);

        String xmlColumnStateEndTag = xmlColumnStateTag.endTag();
        Pattern xmlColumnStateEndTagPattern = Pattern.compile(xmlColumnStateEndTag);
        
        Pattern xmlColumnStateTagWithContentPattern = Pattern.compile(xmlColumnStateStartTag + ".+?" + xmlColumnStateEndTag);
        Matcher columnStateTagsContentsMatcher = xmlColumnStateTagWithContentPattern.matcher(escapedData);

        while (columnStateTagsContentsMatcher.find()) {
            String content = extractContent(columnStateTagsContentsMatcher.group(), xmlColumnStateStartTagPattern, xmlColumnStateEndTagPattern);
            String escapedContent = escapeContent(content);
            escapedData = replaceOldWithEscapedContentInXmlData(escapedData, content, escapedContent);
        }
        return escapedData;
    }

    private String extractContent(String xmlTag, Pattern startTagPattern, Pattern endTagPattern) {
        Matcher columnStateStartTagMatcher = startTagPattern.matcher(xmlTag);
        String content = columnStateStartTagMatcher.replaceAll("");
        Matcher columnStateEndTagMatcher = endTagPattern.matcher(content);
        content = columnStateEndTagMatcher.replaceAll("");
        return content;
    }

    private String escapeContent(String content) {
        String escapedContent = content;
        for (String[] htmlCharWithReplacement : htmlCharsWithReplacements) {
            escapedContent = escapedContent.replace(htmlCharWithReplacement[0], htmlCharWithReplacement[1]);
        }
        return escapedContent;
    }

    private String replaceOldWithEscapedContentInXmlData(String xmlData, String oldContent, String escapedContent) {
        return xmlData.replace(oldContent, escapedContent);
    }
    
}
