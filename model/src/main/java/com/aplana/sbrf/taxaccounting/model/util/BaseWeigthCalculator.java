package com.aplana.sbrf.taxaccounting.model.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для сравнения полей класса с учетом веса парамеров
 *
 * @param <T> тип класса
 */
public abstract class BaseWeigthCalculator<T> implements WeigthCalculator<T> {

    /**
     * Вес параметра
     */
    private double weigth;

    /**
     * Конструктор
     *
     * @param weigth вес параметра
     */
    public BaseWeigthCalculator(double weigth) {
        this.weigth = weigth;
    }

    /**
     * Метод в котором реализовано сравнение параметров
     */
    @Override
    public abstract double calc(T a, T b);


    /**
     * Получить вес параметра
     *
     * @return вес параметра
     */
    public double getWeigth() {
        return weigth;
    }

//        protected double compareString(String a, String b, double weight) {
//            System.out.println(a+"="+b);
//            return equalsNullSafe(prepareStr(a), prepareStr(b)) ? weigth : 0D;
//        }

    /**
     * Сравнить строки исключая пробелы, без учета регистра
     */
    protected double compareString(String a, String b) {
        return equalsNullSafe(prepareStr(a), prepareStr(b)) ? weigth : 0D;
    }

    /**
     * Сравнить числа
     */
    protected double compareNumber(Number a, Number b) {
        return equalsNullSafe(a, b) ? weigth : 0D;
    }

    /**
     * Сравнить даты
     */
    protected double compareDate(Date a, Date b) {
        return equalsNullSafe(formatDate(a), formatDate(b)) ? weigth : 0D;
    }

    /**
     * Метод переводит строку в нижний регистр и удаляет все пробельные символы
     *
     * @param string исходная строка
     * @return строка подготовленная для сравнения
     */
    protected String prepareStr(String string) {
        if (string != null) {
            return string.replaceAll("\\s", "").toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * @param date
     * @return
     */
    private String formatDate(Date date) {
        if (date != null) {
            return new SimpleDateFormat("dd.MM.yyyy").format(date);
        } else {
            return null;
        }
    }

    /**
     * Основное условие сравнения параметров, если оба параметра не заданы то считается что они равны, если
     * только один из параметров не задан то нет
     */
    protected <T> boolean equalsNullSafe(T a, T b) {
        boolean result = false;
        if (a == null && b == null) {
            result = true;
        } else if (a != null && b != null) {
            result = a.equals(b);
        } else {
            result = false;
        }
        return result;
    }
}
