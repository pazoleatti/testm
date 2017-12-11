package com.aplana.generatorTF;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static com.aplana.generatorTF.Dictionary.regionsDictionary;

class Utils {

    static String generateSnils(Random r) {
        String snils = "";
        int controlSum = 0;
        int tmp = 0;

        int k = 7;
        for (int i = 0; i < 2; i++) {
            tmp = r.nextInt(2);
            controlSum += tmp * k;
            snils += "00" + tmp + "-";
            k -= 3;
        }

        tmp = 100 + r.nextInt(899);
        controlSum += 3 * tmp / 100 + 2 * (tmp / 10 % 10) + tmp % 100;
        snils += tmp + "-";

        String strControlSum = String.valueOf(controlSum % 101);
        if (strControlSum.length() == 1) {
            snils += "0" + strControlSum;
        } else {
            snils += strControlSum.charAt(strControlSum.length() - 2);
            snils += strControlSum.charAt(strControlSum.length() - 1);
        }

        return snils;
    }

    static String generateNumberDul(Random r) {
        String numberDul = "";

        numberDul += (10 + r.nextInt(89)) + " " + (10 + r.nextInt(89)) + " " + (100000 + r.nextInt(899999));

        return numberDul;
    }

    /**
     * Генерирует дату
     *
     * @param r
     * @return
     */
    static String generateDate(Random r) {
        Calendar calendar = Calendar.getInstance();
        int year = randomBetween(1970, 2000, r);
        int dayOfYear = randomBetween(1, calendar.getActualMaximum(Calendar.DAY_OF_YEAR), r);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return formatter.format(calendar.getTime());
    }

    /**
     * Генерирует случайное целое число для интервала
     *
     * @param start
     * @param end
     * @param r
     * @return
     */
    static int randomBetween(int start, int end, Random r) {
        int difference = end - start;
        return r.nextInt(difference) + start;
    }

    /**
     * генерирует ИНН
     *
     * @param r
     * @return
     */
    static String generateInn(Random r) {
        StringBuilder builder = new StringBuilder(regionsDictionary.get(r.nextInt(regionsDictionary.size())));
        for (int i = 0; i < 8; i++) {
            builder.append(generateDigit(r));
        }
        builder.append(computeControlDigit(builder.toString(), new int[]{7, 2, 4, 10, 3, 5, 9, 4, 6, 8}));
        builder.append(computeControlDigit(builder.toString(), new int[]{3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8}));
        return builder.toString();
    }

    /**
     * Генерирует цифру
     *
     * @param r
     * @return
     */
    static char generateDigit(Random r) {
        String digits = "0123456789";
        return digits.charAt(r.nextInt(10));
    }

    /**
     * Расчет контрольной цифры для ИНН.
     *
     * @param inn
     * @param weights коэффициенты весов
     * @return
     */
    private static int computeControlDigit(String inn, int[] weights) {
        int key = 0;
        // Замечено что до jdk 8 использование String#split с параметром "" - возвращает массив где первый элемент ""
        // поэтому вводится дополнительная проверка на длину строки и массива весовых коэффициентов чтобы в случае
        // наличия убрать лишний символ ""
        String[] innSplited = inn.split("");
        String[] innAsArray = new String[weights.length];
        if (innSplited.length == weights.length) {
            innAsArray = innSplited;
        } else {
            for (int i = 0; i < innSplited.length; i++) {
                if (!innSplited[i].equals("")) {
                    innAsArray[i-1] = innSplited[i];
                }
            }
        }
        for (int i = 0; i < weights.length; i++) {
            key += Integer.valueOf(innAsArray[i]) * weights[i];
        }
        return key % 11 % 10;
    }
}
