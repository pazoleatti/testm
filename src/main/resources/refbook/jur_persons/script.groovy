/*
    blob_data.id = 'c0bbc4ce-da4a-4959-8b75-7da021f4d9fd'
 */
package refbook.jur_persons

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Скрипт справочника «Юридические лица» (id = 520)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def REF_BOOK_ID = 520

@Field
def provider

void save() {
    provider = refBookFactory.getDataProvider(REF_BOOK_ID)
    saveRecords.each {
        def String swift = it.SWIFT?.stringValue
        def String inn = it.INN?.stringValue
        def String kio = it.KIO?.stringValue
        def String regNum = it.REG_NUM?.stringValue
        def String kpp = it.KPP?.stringValue
        def Date startDate = it.START_DATE?.dateValue
        def Date endDate = it.END_DATE?.dateValue

        // 1. Проверка поля «Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)»
        if (swift && swift.length() != 8 && swift.length() != 11) {
            logger.error('Поле «Код Swift» должно содержать 8 или 11 символов!')
        }

        // 2. Проверка на заполнения идентификационного кода организации
        if (!inn && !kio && !swift && !regNum) {
            logger.error('Обязательно должно быть заполнено одно из следующих полей: «ИНН», «КИО», «Код SWIFT», «Регистрационный номер в стране инкорпорации»!')
        }

        // 3. Проверка на одновременное заполнение ИНН и КПП
        if ((inn && !kpp) || (kpp && !inn)) {
            logger.error('Обязательно должны быть указаны «ИНН» и «КПП»!')
        }

        // 4. Уникальность поля ИНН
        checkUnique('INN', inn, "ИНН")

        // 5. Уникальность поля КИО
        checkUnique('KIO', kio, "КИО")

        // 6. Уникальность поля Код SWIFT
        checkUnique('SWIFT', swift, "кодом SWIFT")

        // 7. Уникальность поля Регистрационный номер в стране инкорпорации
        checkUnique('REG_NUM', regNum, "регистрационным номером в стране инкорпорации")

        // 8. Проверка правильности заполнения полей «Дата наступления основания для включения в список» и «Дата наступления основания для исключении из списка»
        if (startDate != null && endDate != null && startDate > endDate) {
            logger.error("Поле «Дата наступления основания для включения в список» должно быть больше или равно полю «Дата наступления основания для исключении из списка»!")
        }

        // 3.2.3	Проверки атрибутов справочников на соответствие паттерну
        // ИНН
        if (inn && inn ==~ INN_JUR_PATTERN) {
            // 3.2.4	Проверки атрибутов справочников по контрольным числам
            if (!checkControlSumInn(inn)) {
                logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", "ИНН", inn);
            }
        } else if (inn) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "ИНН", inn, INN_JUR_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", INN_JUR_PATTERN, INN_JUR_MEANING)
        }
        // КПП
        if (kpp && !(kpp ==~ KPP_PATTERN)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "КПП", kpp, KPP_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", KPP_PATTERN, KPP_MEANING)
        }
        // КИО
        if (kio && kio ==~ INN_JUR_PATTERN) {
            // 3.2.4	Проверки атрибутов справочников по контрольным числам
            if (!checkControlSumInn(kio)) {
                logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", "КИО", kio);
            }
        } else if (kio) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "КИО", kio, INN_JUR_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", INN_JUR_PATTERN, INN_JUR_MEANING)
        }
    }
}

void checkUnique(def alias, def value, def msg) {
    if (value != null) {
        String filter = "LOWER($alias) = LOWER('$value')"
        def records =  provider.getRecords(validDateFrom, null, filter, null)
        if (records && records.size() > 0 && records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue != uniqueRecordId) {
            logger.error("В справочнике уже существует организация с данным $msg!")
        }
    }
}

boolean checkControlSumInn(String inn) {
    if (inn == null) {
        return false;
    }
    if (inn.length() == 10) {
        def koefArray10 = [2, 4, 10, 3, 5, 9, 4, 6, 8];
        int sum10 = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(inn.charAt(i))) {
                return false;
            }
            sum10 += koefArray10[i] * Character.getNumericValue(inn.charAt(i));
        }
        return (sum10 % 11) % 10 == Character.getNumericValue(inn.charAt(9));
    } else if (inn.length() == 12) {
        def koefArray11 = [7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
        def koefArray12 = [3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
        int sum11, sum12;
        sum11 = sum12 = 0;
        for (int i = 0; i < 10; i++) {
            if (!Character.isDigit(inn.charAt(i))) {
                return false;
            }
            sum11 += koefArray11[i] * Character.getNumericValue(inn.charAt(i));
            sum12 += koefArray12[i] * Character.getNumericValue(inn.charAt(i));
        }
        sum12 += koefArray12[10] * Character.getNumericValue(inn.charAt(10));
        return (sum11 % 11) % 10 == Character.getNumericValue(inn.charAt(10)) &&
                (sum12 % 11) % 10 == Character.getNumericValue(inn.charAt(11));
    }
    return false;
}