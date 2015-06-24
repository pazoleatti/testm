/*
    blob_data.id = 'ba9bb7ca-697c-b0c2-9999-e262617A9784'
 */
package refbook.organization

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Cкрипт справочника «Организации - участники контролируемых сделок» (id = 9)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

void save() {
    saveRecords.each {
        def String inn = it.INN_KIO?.stringValue
        def String kpp = it.KPP?.stringValue
        def Long organization = getRefBookValue(70, it.ORGANIZATION?.referenceValue)?.CODE?.numberValue
        if (organization == 1 && (inn == null || inn == '')) {
            logger.error('Для организаций РФ атрибут «ИНН» является обязательным')
        }
        if (inn && inn ==~ INN_JUR_PATTERN) {
            if (!checkControlSumInn(inn)) {
                logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", "ИНН", inn);
            }
        } else if (inn) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "ИНН", inn, INN_JUR_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", INN_JUR_PATTERN, INN_JUR_MEANING)
        }
        if (kpp && !(kpp ==~ KPP_PATTERN)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "КПП", kpp, KPP_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", KPP_PATTERN, KPP_MEANING)
        }
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

boolean checkControlSumInn(String inn) {
    if (inn == null) {
        return false;
    }
    if (inn.length() == 10) {
        def koefArray10 = [2, 4, 10, 3, 5, 9, 4, 6, 8];
        int sum10 = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(inn.charAt(i))){
                return false;
            }
            sum10 += koefArray10[i] * Character.getNumericValue(inn.charAt(i));
        }
        return (sum10 % 11) % 10 == Character.getNumericValue(inn.charAt(9));
    } else if (inn.length() == 12){
        def koefArray11 = [7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
        def koefArray12 = [3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
        int sum11, sum12;
        sum11 = sum12 = 0;
        for (int i = 0; i < 10; i++) {
            if (!Character.isDigit(inn.charAt(i))){
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