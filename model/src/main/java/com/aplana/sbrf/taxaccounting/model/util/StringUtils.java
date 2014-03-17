package com.aplana.sbrf.taxaccounting.model.util;


public final class StringUtils {

    /**
     * Удаление двоиных пробелов, пробелов перед и после текста,
     * замена вида кавычек.
     */
    public static  String cleanString(String uncleanString) {
        if (uncleanString != null) {
            String cleanString = uncleanString.trim();
            cleanString = cleanString.replaceAll("\\s{2,}", " ");
            cleanString = cleanString.replaceAll("['`«»]", "\"");
            return cleanString;
        } else {
            return null;
        }

    }

    public static String join(Object[] array, char separator){
        return join(array, String.valueOf(separator), null);
    }

	public static String join(Object[] array, String separator, String decorator) {
		if (array == null) {
			return null;
		}

		int startIndex = 0;
		int endIndex = array.length;

		int bufSize = (endIndex - startIndex);

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
		StringBuffer buf = new StringBuffer(bufSize);

		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				if (decorator != null) {
					buf.append(decorator + array[i] + decorator);
				} else {
					buf.append(array[i]);
				}
			}
		}
		return buf.toString();
	}
}