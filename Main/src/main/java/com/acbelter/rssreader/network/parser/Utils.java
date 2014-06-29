package com.acbelter.rssreader.network.parser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
    public static String readXmlToString(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader inReader = null;
        String line;
        try {
            inReader = new BufferedReader(new InputStreamReader(is));
            while ((line = inReader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                }
            }
        }

        return StringEscapeUtils.unescapeXml(builder.toString());
    }
}
