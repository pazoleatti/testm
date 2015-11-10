/*
    blob_data.id = 'c0bbc4ce-da4a-4959-8b75-7da021f4d9fd'
 */
package refbook.jur_persons

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
def REF_BOOK_ORG_CODE_ID = 513L

@Field
def REF_BOOK_TYPE_TCO_ID = 525L

@Field
def provider

@Field
def refBookCache = [:]

def getRecord(def refBookId, def recordId) {
    if (refBookCache[getRefBookCacheKey(refBookId, recordId)] != null) {
        return refBookCache[getRefBookCacheKey(refBookId, recordId)]
    } else {
        def provider = refBookFactory.getDataProvider(refBookId)
        def value = provider.getRecordData(recordId)
        refBookCache.put(getRefBookCacheKey(refBookId, recordId), value)
        return value
    }
}

String getAttrName(String code) {
    return refbook.getAttribute(code).getName()
}


void save() {
    refbook = refBookFactory.get(REF_BOOK_ID)
    provider = refBookFactory.getDataProvider(REF_BOOK_ID)

    saveRecords.each {
        def String swift = it.SWIFT?.stringValue
        def String inn = it.INN?.stringValue
        def String kio = it.KIO?.stringValue
        def String regNum = it.REG_NUM?.stringValue
        def String kpp = it.KPP?.stringValue
        def String taxCodeIncorporation = it.TAX_CODE_INCORPORATION?.stringValue
        def Date startDate = it.START_DATE?.dateValue
        def Date endDate = it.END_DATE?.dateValue
        def Long orgCode = getRecord(REF_BOOK_ORG_CODE_ID, it?.ORG_CODE.referenceValue)?.CODE.numberValue.longValue()
        def String type = getRecord(REF_BOOK_TYPE_TCO_ID, it?.TYPE.referenceValue)?.CODE.stringValue
        def Long vatStatus = it.VAT_STATUS?.referenceValue
        def Long depCriterion = it.DEP_CRITERION?.referenceValue
        def Long offshoreCode = it.OFFSHORE_CODE?.referenceValue

        // 1. Проверка поля «Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)»
        if (swift && swift.length() != 8 && swift.length() != 11) {
            logger.error('Поле «Код Swift» должно содержать 8 или 11 символов!')
        }

        // 2. Обязательное заполнение идентификационного кода для иностранной организации
        if (orgCode == 2 && !kio && !swift && !regNum) {
            logger.error('Для иностранной организации обязательно должно быть заполнено одно из следующих полей: «КИО», «Код SWIFT», «Регистрационный номер в стране инкорпорации»!')
        }

        // 3. Обязательное заполнение идентификационного кода для российской организации
        if (orgCode == 1 && (!inn || !kpp)) {
            logger.error('Для российской организации обязательно должны быть заполнены поля: «ИНН», «КПП»!')
        }

        // 4. Проверка на корректное заполнение идентификационного кода для российской организации
        // todo добавили новый аттриуб
        if (orgCode == 1 && (regNum || kio)) {
            List<String> attributeNames = new ArrayList<String>();
            if (regNum) {
                attributeNames.add("«${getAttrName('REG_NUM')}»")
            }
            if (kio) {
                attributeNames.add("«${getAttrName('KIO')}»")
            }
            if (taxCodeIncorporation) {
                attributeNames.add("«${getAttrName('TAX_CODE_INCORPORATION')}»")
            }

            if (attributeNames.size() == 1) {
                logger.error('Для российской организации нельзя указать поле %s!', attributeNames.get(0))
            } else {
                logger.error('Для российской организации нельзя указать поля %s!', StringUtils.join(attributeNames.toArray(), ',' as char))
            }
        }

        // 5. Проверка на корректное заполнение идентификационного кода для иностранной организации
        if (orgCode == 2 && (inn || kpp)) {
            List<String> attributeNames = new ArrayList<String>();
            if (inn) {
                attributeNames.add("«${getAttrName('INN')}»")
            }
            if (kpp) {
                attributeNames.add("«${getAttrName('KPP')}»")
            }

            logger.error('Для иностранной организации нельзя указать %s!', StringUtils.join(attributeNames.toArray(), ',' as char))
        }

        // 6. Уникальность поля ИНН
        checkUnique('INN', inn, "ИНН")

        // 7. Уникальность поля КИО
        checkUnique('KIO', kio, "КИО")

        // 8. Уникальность поля Код SWIFT
        checkUnique('SWIFT', swift, "кодом SWIFT")

        // 9. Уникальность поля Регистрационный номер в стране инкорпорации
        checkUnique('REG_NUM', regNum, "регистрационным номером в стране инкорпорации")

        // 10. Уникальность поля Код налогоплательщика в стране инкорпорации
        checkUnique('TAX_CODE_INCORPORATION', regNum, "кодом налогоплательщика в стране инкорпорации")

        // 11. Проверка правильности заполнения полей «Дата наступления основания для включения в список» и «Дата наступления основания для исключении из списка»
        if (type == "ВЗЛ" && startDate != null && endDate != null && startDate > endDate) {
            logger.error("Поле «Дата наступления основания для включения в список» должно быть больше или равно полю «Дата наступления основания для исключении из списка»!")
        }

        // 12. Заполнение обязательных полей для ВЗЛ
        if (type == "ВЗЛ" && (!startDate || !vatStatus || !depCriterion)) {
            List<String> attributeNames = new ArrayList<String>();
            if (!startDate) {
                attributeNames.add("«${getAttrName('START_DATE')}»")
            }
            if (!vatStatus) {
                attributeNames.add("«${getAttrName('VAT_STATUS')}»")
            }
            if (!depCriterion) {
                attributeNames.add("«${getAttrName('DEP_CRITERION')}»")
            }

            if (attributeNames.size() == 1) {
                logger.error('Для ВЗЛ обязательно должно быть заполнено поле %s!', attributeNames.get(0))
            } else {
                logger.error('Для ВЗЛ обязательно должны быть заполнены поля %s!', StringUtils.join(attributeNames.toArray(), ',' as char))
            }
        }

        // 13. Заполнение обязательных полей для РОЗ
        if (type == "РОЗ" && (!offshoreCode || !kio)) {
            List<String> attributeNames = new ArrayList<String>();
            if (!offshoreCode) {
                attributeNames.add("«${getAttrName('OFFSHORE_CODE')}»")
            }
            if (!kio) {
                attributeNames.add("«${getAttrName('KIO')}»")
            }
            if (attributeNames.size() == 1) {
                logger.error('Для Резидента оффшорной зоны обязательно должно быть заполнено поле «%s»!', attributeNames.get(0))
            } else {
                logger.error('Для Резидента оффшорной зоны обязательно должны быть заполнены поля %s!', StringUtils.join(attributeNames.toArray(), ',' as char))
            }
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

        // Алгоритм заполнения поля «IKKSR»
        def ikksr
        if (orgCode == 1) {
            if (inn && kpp) {
                ikksr = inn + " / " + kpp
            }
        } else if (orgCode == 2) {
            if (regNum) {
                ikksr = regNum
            } else if (taxCodeIncorporation) {
                ikksr = taxCodeIncorporation
            } else if (swift) {
                ikksr = swift
            } else if (kio) {
                ikksr = kio
            }
        }
        it.put("IKKSR", new RefBookValue(RefBookAttributeType.STRING, ikksr))

        // Алгоритм заполнения поля «IKSR»:
        def iksr
        if (orgCode == 1) {
            if (inn) {
                iksr = inn
            }
        } else if (orgCode == 2) {
            if (regNum) {
                iksr = regNum
            } else if (taxCodeIncorporation) {
                iksr = taxCodeIncorporation
            } else if (swift) {
                iksr = swift
            } else if (kio) {
                iksr = kio
            }
        }
        it.put("IKSR", new RefBookValue(RefBookAttributeType.STRING, iksr))
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