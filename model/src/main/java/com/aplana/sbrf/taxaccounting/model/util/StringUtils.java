package com.aplana.sbrf.taxaccounting.model.util;


public final class StringUtils {

    private StringUtils(){}

    /**
     * Удаление двоиных пробелов, пробелов перед и после текста,
     * замена вида кавычек, замена переноса строки на пробел.
     * @param uncleanString исходная строка
     */
    public static String cleanString(String uncleanString) {
        if (uncleanString != null) {
            return uncleanString.replaceAll("\n"," ").trim().replaceAll("\\s{2,}", " ").replaceAll("['`«»„“‘’”]", "\"");
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
					buf.append(decorator);
					buf.append(array[i]);
					buf.append(decorator);
				} else {
					buf.append(array[i]);
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Подбирает склонения слов для чисел. <br />
	 * Примеры:
	 * <pre>
	 * Вход: 1, "попытка", "попытки", "попыток"
	 * Выход: "1 попытка"
	 * </pre>
	 * <pre>
	 * Вход: 37, "год", "года", "лет"
	 * Выход: "37 лет"
	 * </pre>
	 *
	 * @param value значение для которого требуется подобрать окончание
	 * @param s1 окончание для чисел оканчивающихся на 1, кроме оканчивающихся на 11
	 * @param s2 окончание для чисел оканчивающихся на 2, 3, 4, кроме оканчивающихся на 12, 13, 14
	 * @param s3 окончание для всех остальных значений
	 * @return строка-окончание
	 */
	public static String getNumberString(int value, String s1, String s2, String s3) {
		int t = Math.abs(value);
		int d = t % 100;
		if (d < 10 || d > 20) {
			switch (t % 10) {
				case 1:
					return s1;
				case 2:
				case 3:
				case 4:
					return s2;
				default:
					return s3;
			}
		} else {
			return s3;
		}
	}

}