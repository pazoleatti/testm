package com.aplana.sbrf.taxaccounting.model.util;


public final class StringUtils {

    /**
     * Удаление двоиных пробелов, пробелов перед и после текста,
     * замена вида кавычек.
     */
    public static  String cleanString(String uncleanString) {
        String cleanString = uncleanString.trim();
        cleanString = cleanString.replaceAll("\\s{2,}", " ");
        cleanString = cleanString.replaceAll("['`«»]", "\"");

        return cleanString;
    }
}