package com.aplana.generators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static com.aplana.generators.Dictionary.regionsDictionary;

class Utils {

    static String generateSnils(Random r) {
        int group1 = r.nextInt(1000), group2 = r.nextInt(1000), group3 = r.nextInt(1000);
        int controlSum = 9 * (group1 / 100) + 8 * (group1 / 10 % 10) + 7 * (group1 % 10) +
                6 * (group2 / 100) + 5 * (group2 / 10 % 10) + 4 * (group2 % 10) +
                3 * (group3 / 100) + 2 * (group3 / 10 % 10) + (group3 % 10);
        int controlSumMod = controlSum % 101;

        if (controlSumMod == 100) {
            controlSumMod = 0;
        }

        return String.format("%03d-%03d-%03d-%02d", group1, group2, group3, controlSumMod);
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
                    innAsArray[i - 1] = innSplited[i];
                }
            }
        }
        for (int i = 0; i < weights.length; i++) {
            key += Integer.valueOf(innAsArray[i]) * weights[i];
        }
        return key % 11 % 10;
    }
}
