package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для сравнения полей класса с учетом веса парамеров
 *
 * @param <T> тип класса
 */
public abstract class BaseWeightCalculator<T> implements WeightCalculator<T> {

    /**
     * Наименование сравнения
     */
    protected String name;

    /**
     * Вес параметра
     */
    protected double weight;

    /**
     * Конструктор
     *
     * @param weight вес параметра
     */
    public BaseWeightCalculator(String name, double weight) {
        this.weight = weight;
        this.name = name;
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
    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    /**
     * Сравнить строки исключая пробелы, без учета регистра
     */
    protected double compareString(String a, String b) {
        return isStringEquals(a, b) ? weight : 0D;
    }

    /**
     * Сравнить числа
     */
    protected double compareNumber(Number a, Number b) {
        return equalsNullSafe(a, b) ? weight : 0D;
    }

    /**
     * Сравнить даты
     */
    protected double compareDate(Date a, Date b) {
        return equalsNullSafe(formatDate(a), formatDate(b)) ? weight : 0D;
    }

    /**
     * Метод переводит строку в нижний регистр и удаляет все пробельные символы
     *
     * @param string исходная строка
     * @return строка подготовленная для сравнения
     */
    protected String prepareStr(String string) {
        return prepareString(string);
    }

    /**
     * Удалить все пробельные символы из строки
     *
     * @param string
     * @return
     */
    public static String prepareString(String string) {
        if (string != null) {
            return string.replaceAll("\\s", "").toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * Удалить все символы кроме букв алфавита и цифр
     *
     * @param string
     * @return
     */
    public static String prepareStringDul(String string) {
        if (string != null) {
            return string.replaceAll("[^А-Яа-я\\w]", "").toLowerCase();
        } else {
            return null;
        }
    }

    public static <T extends Number> T getIdOrNull(IdentityObject<T> identityObject) {
        if (identityObject != null) {
            return identityObject.getId();
        } else {
            return null;
        }
    }

    public static boolean isStringEquals(String a, String b) {
        return equalsNullSafe(prepareString(a), prepareString(b));
    }


    /**
     * @param date
     * @return
     */
    private static String formatDate(Date date) {
        if (date != null) {
            return new SimpleDateFormat("dd.MM.yyyy").format(date);
        } else {
            return null;
        }
    }


    protected static boolean equalsNullSafeStr(String a, String b) {
        return isEqualsNullSafeStr(a, b);
    }

    public static boolean isEqualsNullSafeStr(String a, String b) {
        return isValueEquals(prepareString(a), prepareString(b));
    }

    public static boolean isEqualsNullSafeStrForDul(String a, String b) {
        return isValueEquals(prepareStringDul(a), prepareStringDul(b));
    }

    public static String prepareSnils(String string) {
        if (string != null) {
            return string.replaceAll("[-]", "");
        } else {
            return null;
        }
    }

    /**
     * Основное условие сравнения параметров, если оба параметра не заданы то считается что они равны, если
     * только один из параметров не задан то нет
     */
    public static <T> boolean equalsNullSafe(T a, T b) {
        return isValueEquals(a, b);
    }


    public static <T> boolean isValueEquals(T a, T b) {
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

    public static IdDoc findDocument(NaturalPerson person, Long docTypeId, String docNumber) {
        for (IdDoc personDocument : person.getDocuments()) {
            RefBookDocType docType = personDocument.getDocType();
            if (docType != null) {
                if (isValueEquals(docTypeId, docType.getId())
                        && isEqualsNullSafeStrForDul(docNumber, personDocument.getDocumentNumber())) {
                    return personDocument;
                }
            }
        }
        return null;
    }

    public PersonIdentifier findIdentifier(NaturalPerson person, String inp, Long asnuId) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
            if (equalsNullSafe(BaseWeightCalculator.prepareString(inp), BaseWeightCalculator.prepareString(personIdentifier.getInp())) && equalsNullSafe(asnuId, personIdentifier.getAsnu().getId())) {
                return personIdentifier;
            }
        }
        return null;
    }

    public PersonIdentifier findIdentifierByAsnu(NaturalPerson person, Long asnuId) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
            if (personIdentifier.getAsnu() != null && personIdentifier.getAsnu().getId().equals(asnuId)) {
                return personIdentifier;
            }
        }
        return null;
    }


}
