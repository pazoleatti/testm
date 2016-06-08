package refbook.vehicles_average_cost_2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import java.text.SimpleDateFormat

/**
 * blob_data.id = '54def165-b118-4d1e-a52c-be429ebd832e'
 *
 * Скрипт справочника "Средняя стоимость транспортных средств (с 2015)" (id = 218)
 *
 * @author Bkinzyabulatov
 */
switch (formDataEvent) {
    case FormDataEvent.PRE_CALCULATION_CHECK:
        logger.setMessageDecorator(null)
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Не выбран файл");
        } else if (!fileName.endsWith(".xml")) {
            logger.error("Файл должен иметь расширение \"xml\"!" + fileName)
        }
        break
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

@Field
def REFBOOK_ID = 218

@Field
def REFBOOK_AVG_ID = 211

@Field
def providerCache = [:]
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

@Field
def EMPTY_DATA_ERROR = "Сообщение не содержит значений, соответствующих загружаемым данным!"

@Field
def titleMap = ["N": "N", "№": "N", "Марка": "BREND", "Модель (Версия)": "MODEL", "Объем двигателя": "ENGINE_VOLUME", "Тип двигателя": "ENGINE_TYPE", "Количество лет, прошедших с года выпуска": "YOM_RANGE"]

@Field
SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')

def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                def key = getRefBookCacheKey(refBookId, recordId)
                return refBookCache.get(key)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null) {
            def key = getRefBookCacheKey(refBookId, recordId)
            refBookCache.put(key, retVal)
        }
        return retVal
    }
    return null
}

def getRecord(def refBookId, def recordId) {
    if (refBookCache[getRefBookCacheKey(refBookId, recordId)] != null) {
        return refBookCache[getRefBookCacheKey(refBookId, recordId)]
    } else {
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        def value = provider.getRecordData(recordId)
        refBookCache.put(getRefBookCacheKey(refBookId, recordId), value)
        return value
    }
}

@Field
def avgCostRecords

def getAvgCostRecords() {
    if (avgCostRecords == null) {
        def dataProviderAvgCost = formDataService.getRefBookProvider(refBookFactory, REFBOOK_AVG_ID, providerCache)
        avgCostRecords = dataProviderAvgCost.getRecords(dateFrom, null, null, null)?.records
    }
    return avgCostRecords
}


void importFromXML() {
    if (inputStream == null) {
        logger.error('Файл не содержит данных!')
        return
    }

    def dataProvider = formDataService.getRefBookProvider(refBookFactory, REFBOOK_ID, providerCache)

    List<Map<String, RefBookValue>> fileRecords = new ArrayList<Map<String, RefBookValue>>()
    def List<Map<String, RefBookValue>> createList = new ArrayList<Map<String, RefBookValue>>() // Новые элементы
    def Map<Long, Map<String, RefBookValue>> updateMap = new HashMap<Long, Map<String, RefBookValue>>() // Измененные элементы
    def List<Map<String, RefBookValue>> existList = new ArrayList<Map<String, RefBookValue>>() // Существующие элементы

    try {
        def tableRows = []
        // парсим xml на строчки
        parseXml(inputStream, tableRows)
        if (tableRows.isEmpty()) {
            logger.error("В таблице файла отсутствуют строки с данными!")
            return
        }
        def refBook = refBookFactory.get(REFBOOK_ID)
        // заполняем, если есть пустые поля
        def emptyRowsMap = [:]
        // заполняем, если содержимое строки не соответствует типу справочника (т.к. строка, то только по длине)
        def invalidRowsMap = [:]
        // неверные категории стредней стоимости
        def wrongAvgCost = []
        // проверяем данные из файла на пустоту и формат; заполняем структуру для справочника
        boolean foundRows = fillFileRecords(tableRows, fileRecords, emptyRowsMap, invalidRowsMap, wrongAvgCost)
        if (!wrongAvgCost.isEmpty()) {
            logger.error("В справочнике \"Категории средней стоимости транспортных средств\" отсутствуют следующие категории, указанные в файле: \"%s\"!", wrongAvgCost.join("\", \""))
        }
        scriptStatusHolder.setTotalCount(fileRecords.size())

        if (!foundRows) {
            logger.error("В таблице файла отсутствуют строки с данными!")
        }
        // выводим незаполненные поля
        emptyRowsMap.each { avgId, aliasRowMap ->
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
            aliasRowMap.each { alias, rowNumberList ->
                logger.error("\"%s\", строки %s таблицы файла: Атрибут \"%s\" не заполнен!",
                        avgCost, rowNumberList.join(", "), titleMap[titleMap.find { it.value.equals(alias) }?.key])
            }
        }
        if (!emptyRowsMap.isEmpty()) {
            return
        }
        // выводим некорректные поля
        invalidRowsMap.each { avgId, aliasRowMap ->
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
            aliasRowMap.each { String alias, def rowNumberList ->
                def refBookAttr = refBook.getAttribute(alias)
                def attrTypeString = ""
                if (RefBookAttributeType.STRING.equals(refBookAttr.getAttributeType())) {
                    attrTypeString = "Строка/" + refBookAttr.maxLength + "/"
                }
                logger.error("\"%s\", строки %s таблицы файла: Значение атрибута \"%s\" файла не соответствуют типу справочного атрибута (%s)!",
                        avgCost, rowNumberList.join(", "), titleMap[titleMap.find { it.value.equals(alias) }?.key], attrTypeString)
            }
        }
        if (!invalidRowsMap.isEmpty()) {
            return
        }

        Map<String, Map<String, RefBookValue>> fileRecordsMap = new HashMap<String, Map<String, RefBookValue>>()
        fileRecords.each { recordsMap ->
            fileRecordsMap.put(getFuzzyFilter(recordsMap).toLowerCase(), recordsMap)
        }

        // для точного соответствия данных в справочнике (пропускаем, делаем неточное, потом все равно сравниваем)
//    def aliases = [/*"AVG_COST", */"BREND", "MODEL", "ENGINE_VOLUME", "ENGINE_TYPE", "YOM_RANGE"]
//    String filter = "(" + fileRecords.collect { recordsMap ->
//        "AVG_COST = " + recordsMap.AVG_COST.value + " AND " + aliases.collect { alias ->
//            String.format("LOWER(%s)=LOWER('%s')", alias, recordsMap[alias].value)
//        }.join(" AND ")
//    }.join(") OR (") + ")"
//    List<Long> recordIds = dataProvider.getUniqueRecordIds(dateFrom, filter)
//    Map<Long, Map<String, RefBookValue>> existRecords = [:]
//    if (recordIds != null && !recordIds.isEmpty()) {
//        existRecords = dataProvider.getRecordData(recordIds)
//    }
        // для неточного соответствия данных в справочнике (уникальные графы)
        String filterFuzzy = "(" + fileRecords.collect { recordsMap ->
            getFuzzyFilter(recordsMap)
        }.join(") OR (") + ")"
        // 5. Система получает записи справочника с аналогичными значениями уникальных атрибутов
        List<Long> recordIds = dataProvider.getUniqueRecordIds(dateFrom, filterFuzzy)
        Map<Long, Map<String, RefBookValue>> fuzzyRecords = new HashMap<Long, Map<String, RefBookValue>>()
        if (recordIds != null && !recordIds.isEmpty()) {
            fuzzyRecords = dataProvider.getRecordData(recordIds)
        }
        // заполняем id из бд
        fuzzyRecords.each { Long uniqueRecordId, Map<String, RefBookValue> recordsMap ->
            String key = getFuzzyFilter(recordsMap).toLowerCase()
            Map<String, RefBookValue> fileValueMap = fileRecordsMap.get(key)
            // запись есть
            if (fileValueMap != null) {
                def uniqueRecordValue = recordsMap[RefBook.RECORD_ID_ALIAS]
                fileValueMap.put(RefBook.RECORD_ID_ALIAS, uniqueRecordValue)
                if (!(fileValueMap.AVG_COST.numberValue.equals(recordsMap.AVG_COST.numberValue)) ||
                        !(fileValueMap.YOM_RANGE.stringValue.equals(recordsMap.YOM_RANGE.stringValue))) {
                    updateMap.put(uniqueRecordId, fileValueMap)
                } else {
                    existList.add(fileValueMap)
                }
            }
        }
        fileRecordsMap.each { String key, Map<String, RefBookValue> fileValueMap ->
            if (fileValueMap[RefBook.RECORD_ID_ALIAS] == null) {
                createList.add(fileValueMap)
            }
        }

        // создаем новые версии/записи справочника
        if (!createList.empty) {
            dataProvider.createRecordVersionWithoutLock(logger, dateFrom, dateTo, createList)
        }

        // пытаемся
        updateMap.each { uniqueRecordId, fileRecord ->
            dataProvider.updateRecordVersionWithoutLock(logger, uniqueRecordId, dateFrom, dateTo, fileRecord)
        }

    } finally {
        def avgRowNumMap = [:]
        if (fileRecords.size() != 0 && fileRecords.size() == createList.size()) {
            logger.info("Все строки таблицы файла: В справочнике созданы записи, действующие с %s по %s.",
                    sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
        }
        createList.each { fileValueMap ->
            def avgId = fileValueMap.AVG_COST.value
            if (avgRowNumMap[avgId] == null) {
                avgRowNumMap.put(avgId, [])
            }
            avgRowNumMap.get(avgId).add(fileValueMap.N.value)
        }
        avgRowNumMap.keySet().sort().each { avgId ->
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
            def rowNumbers = avgRowNumMap[avgId].sort { String a, String b ->
                (a as Integer).compareTo(b as Integer)
            }
            logger.info("\"%s\", строки %s таблицы файла: В справочнике созданы записи, действующие с %s по %s.",
                    avgCost, rowNumbers.join(", "), sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
        }

        if (fileRecords.size() != 0 && fileRecords.size() == existList.size()) {
            logger.info("Все строки таблицы файла: В справочнике уже существуют такие записи, действующие с %s по %s.",
                    sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
        }

        avgRowNumMap = [:]
        existList.each { fileValueMap ->
            def avgId = fileValueMap.AVG_COST.value
            if (avgRowNumMap[avgId] == null) {
                avgRowNumMap.put(avgId, [])
            }
            avgRowNumMap.get(avgId).add(fileValueMap.N.value)
        }
        avgRowNumMap.keySet().sort().each { avgId ->
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
            def rowNumbers = avgRowNumMap[avgId].sort { String a, String b ->
                (a as Integer).compareTo(b as Integer)
            }
            logger.info("\"%s\", строки %s таблицы файла: В справочнике созданы записи, действующие с %s по %s.",
                    avgCost, rowNumbers.join(", "), sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
        }
    }

    scriptStatusHolder.setSuccessCount(createList.size() + updateMap.keySet().size())
}

void parseXml(def inputStream, def tableRows) {
    def valueTags = ['wordDocument', 'body', 'tbl', 'tr', 'tc', 'p', 'r', 't']
    def valueRowTags = ['wordDocument', 'body', 'tbl', 'tr']
    def valueEndTags = ['wordDocument', 'body', 'tbl', 'tr', 'tc']
    def optionalTags = ['sect']
    def tableRowName = 'tr'
    def tableCellName = 'tc'
    def tName = 't'

    def elements = []
    def reader
    try {
        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = xmlFactory.createXMLStreamReader(inputStream)

        def tableRow = null
        def elementText = ""
        while (reader.hasNext()) {
            if (reader.startElement) {
                def tag = reader.name.localPart
                elements.add(tag)
                if (tableRowName.equals(tag) && isEnteredNodes(valueRowTags, optionalTags, elements)) {
                    tableRow = []
                    tableRows.add(tableRow)
                }
                // есть ячейки без данных и тега t
                if (tableCellName.equals(tag)) {
                    elementText = ""
                }
                if (tName.equals(tag) && isEnteredNodes(valueTags, optionalTags, elements)) {
                    try {
                        elementText = elementText + reader.elementText
                        // endElement не отрабатывает для тега t
                        elements.remove(tName)
                    } catch (XMLStreamException e) {
                        // для случаев когда текст лежит не в t
                    }
                }
            } else if (reader.endElement) {
                def tag = reader.name.localPart
                if (tableCellName.equals(tag) && isEnteredNodes(valueEndTags, optionalTags, elements)) {
                    tableRow.add(replaceQuotes(elementText.replaceAll(/\s+/, ' ').trim()))
                }
                elements.remove(tag)
            }
            reader.next()
        }
    } finally {
        reader?.close()
    }
}

boolean fillFileRecords(def tableRows, def fileRecords, def emptyRowsMap, def invalidRowsMap, def wrongAvgCost) {
    boolean foundHeader = false
    boolean prevHeaderRow = false // если строку разбило на две промежуточным заголовком
    boolean foundRows = false
    def indexMap = [:]
    Long avgCostId = null
    def refBook = refBookFactory.get(REFBOOK_ID)
    for (int i = 0; i < tableRows.size(); i++) {
        def tableRow = tableRows[i]
        if (!foundHeader) {
            tableRow.eachWithIndex { String value, int j ->
                def key = titleMap.keySet().find { it.equalsIgnoreCase(value) }
                def alias = titleMap[key] // регистр не важен
                if (alias != null) {
                    indexMap.put(alias, j)
                }
            }
            if (!indexMap.isEmpty()) {
                if (indexMap.keySet().size() != 6) {
                    throw new ServiceException("В заголовке таблицы файла должны быть столбцы \"N\", \"Марка\", \"Модель (Версия)\", \"Объем двигателя\", \"Тип двигателя\", \"Количество лет, прошедших с года выпуска\"!")
                } else {
                    // определили порядок вынимаемых граф
                    foundHeader = true
                }
            }
        } else {
            if (tableRow.size() == 1) {
                String avgCostString = tableRow[0]?.replace('млн.руб', 'млн. руб')
                Long temp = getAvgCostRecords().find { map -> avgCostString.equalsIgnoreCase(map.NAME.stringValue) }?.record_id?.value
                if (temp == null) {
                    avgCostString = avgCostString?.replace('рублей', 'руб.') // FIXME
                    temp = getAvgCostRecords().find { map -> avgCostString.equalsIgnoreCase(map.NAME.stringValue) }?.record_id?.value
                }
                if (temp != null) {
                    avgCostId = temp
                } else {
                    wrongAvgCost.add(avgCostString)
                }
            } else {
                String rowNumber = (tableRow as List).get(indexMap["N"] as Integer)
                // строки с заголовками пропускаем
                if (titleMap.keySet().find { it.equalsIgnoreCase(rowNumber) } != null) {
                    prevHeaderRow = true
                    continue
                }
                if (rowNumber == null || rowNumber.isEmpty()) {
                    if (!prevHeaderRow) {
                        throw new ServiceException("По всем строкам таблицы файла должен быть заполнен столбец \"N\"!")
                    } else {
                        // если строка разделилась на две промежуточным заголовком
                        def recordsMap = fileRecords.last()
                        indexMap.each { String alias, Integer j ->
                            String value = tableRow[j]
                            if (value != null && !value.isEmpty()) {
                                def newValue = recordsMap.get(alias).value + " " + value
                                recordsMap.put(alias, new RefBookValue(RefBookAttributeType.STRING, newValue))
                            }
                        }
                    }
                }
                if (prevHeaderRow) {
                    prevHeaderRow = false
                    continue
                }
                foundRows = true
                Map<String, RefBookValue> recordsMap = new HashMap<String, RefBookValue>()
                if (avgCostId == null) {
                    throw new ServiceException("Пропущена или не определена строка со средней стоимостью!")
                } else {
                    recordsMap.put("AVG_COST", new RefBookValue(RefBookAttributeType.REFERENCE, avgCostId))
                }
                indexMap.each { String alias, Integer j ->
                    String value = tableRow[j]
                    // проверка на пустоту
                    if (value != null && !value.isEmpty()) {
                        if (refBook.getAttributes().find { alias.equals(it.alias) } != null) {
                            def refBookAttr = refBook.getAttribute(alias)
                            if (value.length() > refBookAttr.maxLength) {
                                putRowNumInMap(invalidRowsMap, avgCostId, alias, rowNumber)
                            } else {
                                recordsMap.put(alias, new RefBookValue(RefBookAttributeType.STRING, value))
                            }
                        } else if ("N".equals(alias)) {
                            recordsMap.put(alias, new RefBookValue(RefBookAttributeType.STRING, value))
                        }
                    } else {
                        putRowNumInMap(emptyRowsMap, avgCostId, alias, rowNumber)
                    }
                }
                if (recordsMap.keySet().size() == refBook.getAttributes().size() + 1) {
                    fileRecords.add(recordsMap)
                }
            }
        }
    }
    return foundRows
}


@Field
def uniqueAliases = ["BREND", "MODEL", "ENGINE_VOLUME", "ENGINE_TYPE"]

String getFuzzyFilter(def recordsMap) {
    return uniqueAliases.collect { alias -> String.format("LOWER(%s)=LOWER('%s')", alias, recordsMap[alias].value) }.join(" AND ")
}

/**
 * Ищет, зашел ли reader в текущие узлы (неточное совпадение)
 * @param nodeNames проверяемые элементы xml
 * @param optionalNodeNames необязательные элементы xml
 * @param elements незакрытые элементы
 * @return
 */
boolean isEnteredNodes(List<String> nodeNames, List<String> optionalNodeNames, List<String> elements) {
    def fullNodes = nodeNames + optionalNodeNames
    return (elements.containsAll(nodeNames) && elements.size() == nodeNames.size()) || (elements.containsAll(fullNodes) && elements.size() == fullNodes.size())
}

// заносим строки в
void putRowNumInMap(def rowsMap, def avgCostId, def alias, def rowNumber) {
    if (rowsMap[avgCostId] == null) {
        rowsMap.put(avgCostId, [:])
    }
    def aliasRowMap = rowsMap[avgCostId]
    if (aliasRowMap[alias] == null) {
        aliasRowMap.put(alias, [])
    }
    if (rowNumber) {
        aliasRowMap.getAt(alias).add(rowNumber)
    }
}
