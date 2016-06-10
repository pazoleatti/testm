package refbook.vehicles_average_cost_2015

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormLink
import com.aplana.sbrf.taxaccounting.model.RefBookTableRef
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.CheckCrossVersionsResult
import com.aplana.sbrf.taxaccounting.model.refbook.CrossResult
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookFactoryImpl
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils
import org.apache.commons.lang3.ArrayUtils;
import groovy.transform.Field
import org.springframework.dao.DataAccessException

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
def recordVersionCache = [:]

@Field
def titleMap = ["N": "N", "№": "N", "Марка": "BREND", "Модель (Версия)": "MODEL", "Объем двигателя": "ENGINE_VOLUME", "Тип двигателя": "ENGINE_TYPE", "Количество лет, прошедших с года выпуска": "YOM_RANGE"]

@Field
def avgCostRecords

def getAvgCostRecords() {
    if (avgCostRecords == null) {
        def dataProviderAvgCost = formDataService.getRefBookProvider(refBookFactory, REFBOOK_AVG_ID, providerCache)
        avgCostRecords = dataProviderAvgCost.getRecords(dateFrom, null, null, null)?.records
    }
    return avgCostRecords
}

RefBookRecordVersion getRefBookRecordVersion (def provider, Long recordId) {
    if (recordVersionCache.get(recordId) == null) {
        recordVersionCache.put(recordId, provider.getRecordVersionInfo(recordId))
    }
    return recordVersionCache.get(recordId)
}

void importFromXML() {
    if (inputStream == null) {
        logger.error('Файл не содержит данных!')
        return
    }

    def dataProvider = formDataService.getRefBookProvider(refBookFactory, REFBOOK_ID, providerCache)
    SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')

    List<Map<String, RefBookValue>> fileRecords = new ArrayList<Map<String, RefBookValue>>()
    def List<Map<String, RefBookValue>> createList = new ArrayList<Map<String, RefBookValue>>() // Новые элементы
    def Map<Long, Map<String, RefBookValue>> intersectMap = new HashMap<Long, Map<String, RefBookValue>>() // Пересекающиеся элементы, совпадающие по уник графам
    def Map<Long, Map<String, RefBookValue>> nearMap = new HashMap<Long, Map<String, RefBookValue>>() // Непересекающиеся элементы, совпадающие по уник графам
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

        Map<String, List<Map<String, RefBookValue>>> fileRecordsMap = new HashMap<String, List<Map<String, RefBookValue>>>()
        fileRecords.each { recordsMap ->
            def key = getFuzzyFilter(recordsMap).toLowerCase()
            if (fileRecordsMap[key] == null) {
                fileRecordsMap.put(key, [])
            }
            fileRecordsMap.get(key).add(recordsMap)
        }

        // для неточного соответствия данных в справочнике (уникальные графы)
        String filterFuzzy = "(" + fileRecords.collect { recordsMap ->
            getFuzzyFilter(recordsMap)
        }.join(") OR (") + ")"
        // 5. Система получает записи справочника с аналогичными значениями уникальных атрибутов (без даты)
        List<Long> recordIds = dataProvider.getUniqueRecordIds(null, filterFuzzy)
        //logger.info("recordIds = " + recordIds.toString())
        Map<Long, Map<String, RefBookValue>> fuzzyRecords = new HashMap<Long, Map<String, RefBookValue>>()
        if (recordIds != null && !recordIds.isEmpty()) {
            fuzzyRecords = dataProvider.getRecordData(recordIds)
        }
        //logger.info("fuzzyRecords = " + fuzzyRecords.toString())
        def fuzzyRecordsMap = [:]
        fuzzyRecords.each { Long uniqueRecordId, Map<String, RefBookValue> recordsMap ->
            String key = getFuzzyFilter(recordsMap).toLowerCase()
            if (fuzzyRecordsMap[key] == null) {
                fuzzyRecordsMap.put(key, [:])
            }
            fuzzyRecordsMap.get(key).put(uniqueRecordId, recordsMap)
        }

        //logger.info("fuzzyRecordsMap = " + fuzzyRecordsMap.toString())
        // проходим по строкам файла
        fileRecordsMap.each { String keyString, List<Map<String, RefBookValue>> fileValueMapList ->
            if (fileValueMapList.size() > 1) {
                logger.warn("В загружаемом файле есть две строки совпадающие по уникальным графам! " + fileValueMapList.toString())
            }
            def recordsMapMap = fuzzyRecordsMap[keyString]
            def existFileValueMap = fileValueMapList.find { fileValueMap ->
                recordsMapMap.each { Long uniqueRecordId, Map<String, RefBookValue> recordsMap ->
                    // полное совпадение
                    if ((fileValueMap.AVG_COST.numberValue.equals(recordsMap.AVG_COST.numberValue)) &&
                            (fileValueMap.YOM_RANGE.stringValue.equals(recordsMap.YOM_RANGE.stringValue))) {
                        fileValueMap.put(RefBook.RECORD_ID_ALIAS, recordsMap[RefBook.RECORD_ID_ALIAS])
                        return true
                    }
                }
                return false
            }
            if (existFileValueMap != null) {
                existList.add(existFileValueMap)
            } else {
                // неполное совпадение
                recordsMapMap.each { Long uniqueRecordId, Map<String, RefBookValue> recordsMap ->
                    RefBookRecordVersion recordVersion = getRefBookRecordVersion(dataProvider, uniqueRecordId)
                    def start = recordVersion.versionStart
                    def end = recordVersion.versionEnd
                    def recordId = dataProvider.getRecordId(recordsMap[RefBook.RECORD_ID_ALIAS].value)
                    fileValueMapList.each { fileValueMap ->
                        fileValueMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                        // если есть пересечение
                        if ((!(start < dateFrom) && (dateTo == null || !(dateTo < start))) ||
                                (!(dateFrom < start) && (end == null || !(end < dateFrom)))) {
                            intersectMap.put(uniqueRecordId, fileValueMap)
                        } else { // если нет пересечения, то
                            nearMap.put(uniqueRecordId, fileValueMap)
                        }
                    }
                }
            }
        }
        //logger.info("intersectMap = " + intersectMap.toString())
        //logger.info("nearMap = " + nearMap.toString())
        def nearFuzzyMap = [:]
        // версии справочников с такими же уникальными графами, но не пересекаются
        nearMap.each { def uniqueRecordId, fileValueMap ->
            String key = getFuzzyFilter(fileValueMap).toLowerCase()
            if (fileValueMap.get(key) == null) {
                nearFuzzyMap.put(key, [])
            }
            nearFuzzyMap.get(key).add(fileValueMap)
        }

        //logger.info("nearFuzzyMap = " + nearFuzzyMap.toString())
        nearFuzzyMap.each { String key, List<Map<String, RefBookValue>> fuzzyMapList ->
            // если больше двух записей с версиями похожими на загружаемую (по уник графам), то создаем новую запись
            def valueMap = fileRecordsMap[key][0]
            def recordId = fuzzyMapList.size() == 1 ? fuzzyMapList[0][RefBook.RECORD_ID_ALIAS].value : null
            valueMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
            createList.add(valueMap)
        }

        fileRecordsMap.each { String key, List<Map<String, RefBookValue>> fileValueMapList ->
            fileValueMapList.each { fileValueMap ->
                if (fileValueMap[RefBook.RECORD_ID_ALIAS] == null) {
                    createList.add(fileValueMap)
                }
            }
        }

        def errorRecords = []

        //logger.info(createList.toString())
        // создаем новые версии/записи справочника
        if (!createList.empty) {
            def recordList = []
            createList.each { map ->
                RefBookRecord record = new RefBookRecord()
                Long recordId = map[RefBook.RECORD_ID_ALIAS]?.value
                record.setRecordId(recordId)
                record.setValues(map)
                recordList.add(record)
            }
            createRecordVersionWithoutLock(logger, dateFrom, dateTo, recordList, errorRecords)
        }

        def avgRowNumMap = [:]
        if (intersectMap.isEmpty() && errorRecords.isEmpty()) {
            if (fileRecords.size() == createList.size()) {
                logger.info("Все строки таблицы файла: В справочнике созданы записи, действующие с %s по %s.",
                        sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
            } else {
                createList.each { fileValueMap ->
                    def avgId = fileValueMap.AVG_COST.value
                    if (avgRowNumMap[avgId] == null) {
                        avgRowNumMap.put(avgId, [])
                    }
                    avgRowNumMap.get(avgId).add(fileValueMap.N.value)
                }
                avgRowNumMap.keySet().sort().each { avgId ->
                    def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
                    def rowNumbers = avgRowNumMap[avgId].sort { it as Integer }
                    logger.info("\"%s\", строки %s таблицы файла: В справочнике созданы записи, действующие с %s по %s.",
                            avgCost, rowNumbers.join(", "), sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
                }
            }
        }

        scriptStatusHolder.setSuccessCount(createList.size() - errorRecords.size())
        scriptStatusHolder.setTotalCount(createList.size())

    } finally {
        def avgRowNumMap = [:]
        intersectMap.each { Long recordId, def fileValueMap ->
            def avgId = fileValueMap.AVG_COST.value
            def key = getFuzzyFilter(fileValueMap).toLowerCase()
            if (avgRowNumMap[avgId] == null) {
                avgRowNumMap.put(avgId, [:])
            }
            if (avgRowNumMap.get(avgId).get(key) == null) {
                avgRowNumMap.get(avgId).put(key, [:])
            }
            if (avgRowNumMap.get(avgId).get(key).get(recordId) == null) {
                avgRowNumMap.get(avgId).get(key).put(recordId, [])
            }
            avgRowNumMap.get(avgId).get(key).get(recordId).add(fileValueMap)
        }
        avgRowNumMap.keySet().sort().each { avgId ->
            def refBook = refBookFactory.get(REFBOOK_ID)
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == avgId }.NAME.value
            def keyIdValueMap = avgRowNumMap[avgId]
            keyIdValueMap.each { def key, def idValueMap ->
                def versionValuesMap = idValueMap.sort { it.value[0].N.value as Integer }
                def fileValueMapFirst = versionValuesMap.entrySet()[0].value[0]
                logger.error("\"%s\", строка %s таблицы файла: Нарушено требование к уникальности, уже существуют записи, действующие с %s по %s, с такими же значениями атрибутов \"Марка\", \"Модель (Версия)\", \"Объем двигателя\", \"Тип двигателя\", \"Количество лет, прошедших с года выпуска\" !",
                        avgCost, fileValueMapFirst.N.value, sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
                versionValuesMap.each { Long recordId, def fileValueMapList ->
                    RefBookRecordVersion recordVersion = getRefBookRecordVersion(dataProvider, recordId)
                    def attrValueString = uniqueAliases.collect { alias ->
                        def attr = refBook.getAttribute(alias)
                        return "\"" + attr.name + "\" = \"" + fileValueMapList[0][alias].value + "\""
                    }.join(", ")
                    logger.error("Запись с такими же значениями уникальных атрибутов: %s, действует с %s по %s",
                            attrValueString, sdf.format(recordVersion.versionStart), recordVersion.versionEnd ? sdf.format(recordVersion.versionEnd) : "\"-\"")
                }
            }
        }

        if (existList.size() != 0 && fileRecords.size() == existList.size()) {
            logger.info("Все строки таблицы файла: В справочнике уже существуют такие записи, действующие с %s по %s.",
                    sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
        } else {
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
                def rowNumbers = avgRowNumMap[avgId].sort { it as Integer }
                logger.info("\"%s\", строки %s таблицы файла: В справочнике уже существуют такие записи, действующие с %s по %s.",
                        avgCost, rowNumbers.join(", "), sdf.format(dateFrom), dateTo ? sdf.format(dateTo) : "\"-\"")
            }
        }
    }
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
def uniqueAliases = ["BREND", "MODEL", "ENGINE_VOLUME", "ENGINE_TYPE", "YOM_RANGE"]

@Field
def allAliases = ["AVG_COST", "BREND", "MODEL", "ENGINE_VOLUME", "ENGINE_TYPE", "YOM_RANGE"]

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

@Field
RefBookDao refBookDao = ((RefBookFactoryImpl) refBookFactory).refBookDao

def List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records, List<RefBookRecord> errorRecords) {
    try {
        def dataProvider = formDataService.getRefBookProvider(refBookFactory, REFBOOK_ID, providerCache)
        RefBook refBook = refBookFactory.get(REFBOOK_ID);
        List<RefBookAttribute> attributes = refBook.getAttributes();
        List<Long> excludedVersionEndRecords = new ArrayList<Long>();
        //Признак того, что для проверок дата окончания была изменена (была использована дата начала следующей версии)
        boolean dateToChangedForChecks = false;

        if (!refBook.isVersioned()) {
            //Устанавливаем минимальную дату
            versionFrom = new Date(0L);
        }

        long countIds = 0;
        for (RefBookRecord record : records) {
            if (record.getRecordId() == null) {
                countIds++;
                record.setVersionTo(versionTo);
            } else {
                //Получение фактической даты окончания, которая может быть задана датой начала следующей версии
                RefBookRecordVersion nextVersion = refBookDao.getNextVersion(REFBOOK_ID, record.getRecordId(), versionFrom);
                if (nextVersion != null) {
                    Date versionEnd = SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1);
                    if (versionEnd != null && versionFrom.after(versionEnd)) {
                        errorRecords.add(record)
                        logger.error("Дата окончания получена некорректно");
                        continue
                    }
                    record.setVersionTo(versionEnd);
                    dateToChangedForChecks = true;
                } else {
                    record.setVersionTo(versionTo);
                }
            }
        }
        records = records - errorRecords

        //Проверка корректности
        dataProvider.checkCorrectness(logger, refBook, null, versionFrom, attributes, records);

        if (refBook.isVersioned()) {
            for (RefBookRecord record : records) {
                //Проверка пересечения версий
                if (record.getRecordId() != null) {
                    def crossResults = refBookDao.checkCrossVersions(REFBOOK_ID, record.getRecordId(), versionFrom, record.getVersionTo(), null)
                    boolean needToCreateFakeVersion = crossVersionsProcessing(crossResults, versionFrom, record.getVersionTo(), record.getValues(), record, errorRecords, logger);
                    if (!needToCreateFakeVersion) {
                        //Добавляем запись в список тех, для которых не будут созданы фиктивные версии
                        excludedVersionEndRecords.add(record.getRecordId());
                    }
                }
            }
        }

        //logger.info(errorRecords.toString())
        if (!errorRecords.isEmpty()) {
            return null
        }
        //Создание настоящей и фиктивной версии
        for (RefBookRecord record : records) {
            if (dateToChangedForChecks) {
                //Возвращаем обратно пустую дату начала, т.к была установлена дата начала следующей версии для проверок
                record.setVersionTo(null);
                versionTo = null;
            }
        }
        records.each { record ->
            record.getValues().remove('N')
        }
        return dataProvider.createVersions(refBook, versionFrom, versionTo, records, countIds, excludedVersionEndRecords, logger);
    } catch (DataAccessException e) {
        throw new ServiceException("Запись не сохранена. Обнаружены фатальные ошибки!", e);
    }
}

boolean crossVersionsProcessing(List<CheckCrossVersionsResult> results, Date versionFrom, Date versionTo, Map<String, RefBookValue> values, def record, def errorRecords, Logger logger) {
    def format = new SimpleDateFormat("dd.MM.yyyy")
    for (CheckCrossVersionsResult result : results) {
        if (result.getResult() == CrossResult.FATAL_ERROR) {
            errorRecords.add(record)
            def avgCost = getAvgCostRecords().find { map -> map.record_id.value == values.AVG_COST.value }.NAME.value
            logger.error("\"%s\", строка %s таблицы файла: Обнаружено пересечение указанного периода действия (с %s по %s) с существующей версией записи!",
                    avgCost, values.N.value, format.format(dateFrom), dateTo ? format.format(dateTo) : "\"-\"");
            return false
        }
    }

    for (CheckCrossVersionsResult result : results) {
        if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
            //Ищем все ссылки на запись справочника в новом периоде
            Logger usageLogger = checkUsages(Arrays.asList(result.getRecordId()), versionFrom, versionTo, true);

            if (!usageLogger.containsLevel(LogLevel.ERROR)) {
                logger.info("Установлена дата окончания актуальности версии "+ format.format(SimpleDateUtils.addDayToDate(versionFrom, -1))+" для предыдущей версии");
            } else {
                errorRecords.add(record)
                def refBook = refBookFactory.get(REFBOOK_ID)
                def attrValueString = allAliases.collect { alias ->
                    def attr = refBook.getAttribute(alias)
                    return "\"" + attr.name + "\" = \"" + values[alias].value + "\""
                }.join(", ")
                def avgCost = getAvgCostRecords().find { map -> map.record_id.value == values.AVG_COST.value }.NAME.value
                logger.error("\"%s\", строка %s таблицы файла: При создании записи в справочнике не удалось установить дату окончания действия %s для предыдущей версии записи: %s, действует с %s, по %s! Т.к. для предыдущей версии записи:",
                        avgCost, values.N.value, format.format(versionFrom-1), attrValueString, format.format(versionFrom), versionTo ? format.format(versionTo) : "\"-\"")
                logger.entries.addAll(usageLogger.entries)
                return false
            }
        }
        if (result.getResult() == CrossResult.NEED_CHANGE) {
            refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
            return false;
        }
        if (result.getResult() == CrossResult.NEED_DELETE) {
            refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(result.getRecordId()));
        }
    }
    return true;
}

Logger checkUsages(List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod) {
    Logger localLogger = new Logger()

    //Проверка использования в справочниках
    List<String> refBooks = refBookDao.isVersionUsedInRefBooks(REFBOOK_ID, uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
            RefBookTableRef.getTablesIdByRefBook(REFBOOK_ID) != null ?
                    Arrays.asList(ArrayUtils.toObject(RefBookTableRef.getTablesIdByRefBook(REFBOOK_ID))) : null);
    for (String refBookMsg : refBooks) {
        localLogger.error(refBookMsg);
    }

    //Проверка использования в нф
    List<FormLink> forms = refBookDao.isVersionUsedInForms(REFBOOK_ID, uniqueRecordIds, versionFrom, versionTo, restrictPeriod);
    for (FormLink form : forms) {
        localLogger.error(form.getMsg());
    }

    //Проверка использования в настройках подразделений
    List<String> configs = refBookDao.isVersionUsedInDepartmentConfigs(REFBOOK_ID, uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
            RefBookTableRef.getTablesIdByRefBook(REFBOOK_ID) != null ?
                    Arrays.asList(ArrayUtils.toObject(RefBookTableRef.getTablesIdByRefBook(REFBOOK_ID))) : null);
    for (String configMsg : configs) {
        localLogger.error(configMsg);
    }
}