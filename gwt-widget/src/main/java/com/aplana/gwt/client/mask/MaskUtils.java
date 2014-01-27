package com.aplana.gwt.client.mask;

/**
 * Класс статических методов для работы с масками
 * @author aivanov
 */
public class MaskUtils {

    /**
     * Формирование строки которая показывается в элементе ввода
     * Например для маски 9999 (или XXXX) будет ____
     * для маски 999.999.999.999 (или XXX.XXX.XXX.XXX) ___.___.___.___
     * @param mask маска вида 99.9999 и пр.
     * @return строку вида ___.___
     */
    public static String createMaskPicture(String mask) {
        StringBuffer pic = new StringBuffer();
        for (char mc : mask.toCharArray()) {
            switch (mc) {
                case '9':
                case 'X':
                    pic.append("_");
                    break;
                default:
                    pic.append(mc);
            }
        }
        return pic.toString();
    }
}
