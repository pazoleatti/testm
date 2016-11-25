package refbook.vehicles_tax_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Скрипт справочника "Ставки транспортного налога" (id=41)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
}

@Field
def REF_BOOK_ID = 41

@Field
def REF_BOOK_ECO_ID = 40

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

void addRow() {
    if (user?.departmentId) {
        regionId = departmentService.get(user.departmentId).getRegionId()
        if (regionId) {
            record.put("DECLARATION_REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, regionId));
        }
    }
}

void save() {
    def refBook = refBookFactory.get(REF_BOOK_ID)
    provider = refBookFactory.getDataProvider(REF_BOOK_ID)
    def Date start = Date.parse('dd.MM.yyyy', '01.01.2016')

    saveRecords.each {
        def declarationRegionId = it.DECLARATION_REGION_ID?.referenceValue // Код субъекта РФ представителя декларации
        def dictRegionId = it.DICT_REGION_ID?.referenceValue // Код региона РФ
        def code = it.CODE?.referenceValue // Код вида ТС
        def unitOfPower = it.UNIT_OF_POWER?.referenceValue // Ед. измерения мощности
        def value = it.VALUE?.numberValue // Ставка (руб.)
        def minAge = it.MIN_AGE?.numberValue
        def maxAge = it.MAX_AGE?.numberValue
        def minPower = it.MIN_POWER?.numberValue
        def maxPower = it.MAX_POWER?.numberValue
        def minEcoclass = it.MIN_ECOCLASS?.referenceValue
        def maxEcoclass = it.MAX_ECOCLASS?.referenceValue
        def boolean has1 = minAge || maxAge
        def boolean has2 = minPower || maxPower
        def boolean has3 = minEcoclass || maxEcoclass

        def boolean allRequired = declarationRegionId && dictRegionId && code && unitOfPower && value

        // 1. Проверка обязательности заполнения мощности
        if (allRequired && minPower != null && maxPower != null) {
            logger.error("Поле «%s» или поле «%s» должно быть обязательно заполнено", refBook.getAttribute('MIN_POWER').name, refBook.getAttribute('MAX_POWER').name)
        }

        // 2. Проверка типов дифференциации ставок
        if (allRequired && ((minAge || maxAge) && (minEcoclass || maxEcoclass) && (minPower || maxPower))) {
            logger.error("Ставка не может дифференцироваться по значениям мощности, срока использования и экологического класса одновременно")
        } else if (allRequired && ((minAge || maxAge) && (minEcoclass || maxEcoclass))) {
            logger.error("Ставка не может дифференцироваться по значению срока использования и по значению экологического класса одновременно")
        }

        // 3. Проверка корректности задания границ интервалов
        def String errStr = "Значение поля «%s» должно быть меньше или равно значению поля «%s»"
        if (minAge && maxAge && minAge > maxAge) {
            logger.error(errStr, refBook.getAttribute('MIN_AGE').name, refBook.getAttribute('MAX_AGE').name)
        }
        if (minPower && maxPower && minPower > maxPower) {
            logger.error(errStr, refBook.getAttribute('MIN_POWER').name, refBook.getAttribute('MAX_POWER').name)
        }
        if (minEcoclass && maxEcoclass && minEcoclass > maxEcoclass) {
            logger.error(errStr, refBook.getAttribute('MIN_ECOCLASS').name, refBook.getAttribute('MAX_ECOCLASS').name)
        }

        // 4, 5
        if (allRequired && !logger.containsLevel(LogLevel.ERROR)) {
            String filter = "DECLARATION_REGION_ID =" + declarationRegionId +
                    " and DICT_REGION_ID = " + dictRegionId +
                    " and CODE = " + code +
                    " and UNIT_OF_POWER = " + unitOfPower
            def pairs = provider.getRecordIdPairs(REF_BOOK_ID, null, false, filter)
            for (def pair : pairs) {
                if (recordCommonId && pair.second == recordCommonId) {
                    // проверка при создании новой версии, пропускаем элементы версии
                    continue
                } else if (!recordCommonId && pair.first == uniqueRecordId) {
                    // проверка при создания нового/сохранении существующего элемента
                    continue
                }
                def record = provider.getRecordVersionInfo(pair.first)
                Date fromDate = record.versionStart
                Date toDate = record.versionEnd
                if ((validDateTo == null || fromDate.compareTo(validDateTo) <= 0) &&
                        (toDate == null || toDate.compareTo(validDateFrom) >= 0)) {
                    record = getRecord(REF_BOOK_ID, pair.first)
                    def minAge1 = record.MIN_AGE?.numberValue
                    def maxAge1 = record.MAX_AGE?.numberValue
                    def minPower1 = record.MIN_POWER?.numberValue
                    def maxPower1 = record.MAX_POWER?.numberValue
                    def minEcoclass1 = record.MIN_ECOCLASS?.referenceValue
                    def maxEcoclass1 = record.MAX_ECOCLASS?.referenceValue
                    boolean recordHas1 = minAge1 || maxAge1
                    boolean recordHas2 = minPower1 || maxPower1
                    boolean recordHas3 = minEcoclass1 || maxEcoclass1

                    // 4. Проверка дифференциации ТС
                    if ((minAge || maxAge) && (minEcoclass1 || maxEcoclass1)) {
                        logger.error("Для вида ТС «%s» уже задана ставка, которая дифференцируется по мощности и экологическому классу", getRecord(42L, code)?.CODE.stringValue)
                    }
                    if ((minEcoclass || maxEcoclass) && (minAge1 || maxAge1)) {
                        logger.error("Для вида ТС «%s» уже задана ставка, которая дифференцируется по мощности и сроку использования", getRecord(42L, code)?.CODE.stringValue)
                    }

                    // 5. Проверка корректности интервалов дифференциации ставок
                    if (!logger.containsLevel(LogLevel.ERROR)) {
                        //Виды дифференциации ставки НЕ совпадают с видами дифференциации ставки сохраняемой записи
                        if ((has1 != recordHas1) || (has2 != recordHas2) || (has3 != recordHas3)) {
                            continue
                        }

                        boolean group1 = has1 && recordHas1
                        boolean group2 = has2 && recordHas2
                        boolean group3 = has3 && recordHas3

                        boolean badAge = false
                        if (group1) {
                            minAge1 = minAge1 ?: 0
                            maxAge1 = maxAge1 ?: 999
                            minAgeC = minAge ?: 0
                            maxAgeC = maxAge ?: 999
                            badAge = !((minAge1 < minAgeC && maxAge1 < minAgeC) || (minAge1 > maxAgeC && maxAge1 > maxAgeC))
                        }
                        boolean badPower = false
                        if (group2) {
                            minPower1 = minPower1 ?: 0
                            maxPower1 = maxPower1 ?: 9999999999999.99
                            minPowerC = minPower ?: 0
                            maxPowerC = maxPower ?: 9999999999999.99
                            badPower = !((minPower1 < minPowerC && maxPower1 < minPowerC) || (minPower1 > maxPowerC && maxPower1 > maxPowerC))
                        }
                        boolean badEcoclass = false
                        if (group3) {
                            minEcoclass1 = minEcoclass1 ? getRecord(REF_BOOK_ECO_ID, minEcoclass1.longValue())?.CODE.numberValue : 0
                            maxEcoclass1 = maxEcoclass1 ? getRecord(REF_BOOK_ECO_ID, maxEcoclass1.longValue())?.CODE.numberValue : 5
                            minEcoclassC = minEcoclass ? getRecord(REF_BOOK_ECO_ID, minEcoclass.longValue())?.CODE.numberValue : 0
                            maxEcoclassC = maxEcoclass ? getRecord(REF_BOOK_ECO_ID, maxEcoclass.longValue())?.CODE.numberValue : 5
                            badEcoclass = !((minEcoclass1 < minEcoclassC && maxEcoclass1 < minEcoclassC) || (minEcoclass1 > maxEcoclassC && maxEcoclass1 > maxEcoclassC))
                        }

                        error = false
                        if (group1 && group2) {
                            error = badAge && badPower
                        } else if (group1 && group3) {
                            error = badAge && badEcoclass
                        } else if (group1) {
                            error = badAge
                        } else if (group2) {
                            error = badPower
                        } else if (group3) {
                            error = badEcoclass
                        }

                        if (error) {
                            logger.error("В справочнике не должно быть записей с одинаковым значением полей «Код субъекта РФ представителя декларации», " +
                                    "«Код региона РФ», «Код вида ТС», «Ед. измерения мощности» и с пересекающимися интервалами срока использования, мощности и экологического класса")
                            break
                        }
                    }
                }
            }
        }
    }
}