package refbook.account_plan

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
import groovy.transform.Field

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * План счетов бухгалтерского учета
 * blob_data.id = 'e2a67f8a-b976-4696-a778-f21a0e602a3f'
 * ref_book_id = 101
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importFromXML()
        break
}

@Field
SimpleDateFormat sdf = new SimpleDateFormat('yyyy.MM.dd')

@Field
def fileRowIndexMap = [:]

// Импорт записей из XML-файла
void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(101L)
    def defaultDate = sdf.parse('1970.01.01')
    def fileRecords = getFileRecords(inputStream)

    for (def row : fileRecords) {
        def BSSCH    = row?.BSSCH?.value    // Номер счета
        def NMBSP    = row?.NMBSP?.value    // Полное наименование
        def DATE     = row?.DATE?.value     // Дата последнего изменения/ФП
        def BEG_DATE = row?.BEG_DATE?.value // Дата открытия
        def END_DATE = row?.END_DATE?.value // Дата закрытия

        def date = getSearchDate(DATE, BEG_DATE, END_DATE)
        def filter = "ACCOUNT = '$BSSCH'"
        def records = dataProvider.getRecords(date, null, filter, null)

        // провека нескольких записей с максимальной датой
        def versionMap = [:]
        def maxRecord = null
        RefBookRecordVersion maxRecordVersion = null
        def count = 0
        for (def record : records) {
            def id = record?.record_id?.value
            RefBookRecordVersion recordVersion = dataProvider.getRecordVersionInfo(id)
            versionMap[id] = recordVersion
            if (maxRecordVersion == null || maxRecordVersion.versionEnd < recordVersion.versionEnd) {
                maxRecord = record
                maxRecordVersion = recordVersion
            }
        }
        for (def record : records) {
            def id = record?.record_id?.value
            if (versionMap[id].versionEnd == maxRecordVersion.versionEnd) {
                count++
            }
        }

        if (count > 1) {
            logger.error("$BSSCH: найдено более одной записи с заданным номером счета!")
            continue
        } else if (count == 0) {
            // не найдено записей, то по таблице 19:
            // 7.
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // должны быть выполнены шаги 4-6 сценария [3] раздела 3.1.7.1. -  создания элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                refBookRecord.setRecordId(null)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                dataProvider.createRecordVersion(logger, versionFrom, versionTo, [refBookRecord])
            }
            continue
        }

        def record = maxRecord
        def accountName = record?.ACCOUNT_NAME?.value
        def recId = record?.record_id?.value
        RefBookRecordVersion recordVersion = maxRecordVersion

        def versionResult = getRecordVersionsFlag(recId, filter, dataProvider)
        def isFirstVersionRecord = versionResult.isFirst
        def isLastVersionRecord = versionResult.isLast

        // сравнения с датой начала актуальности системы и даты из тф (если версия записи в системе одна то с BEG_DATE, если версии в системе несколько, то с DATE)
        def startDateFlag = ((isFirstVersionRecord ? BEG_DATE : DATE) == recordVersion.versionStart)
        def endDateFlag = (isLastVersionRecord ? END_DATE == recordVersion.versionEnd : true)

        // найдена одна запись, то по таблице 19:
        // 1, 2, 3 - Запись должна быть проигнорирована при загрузке
        if (NMBSP == accountName) {
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // операция 1
                continue
            } else if ((DATE > BEG_DATE && (END_DATE == null || DATE <= END_DATE)) && startDateFlag && endDateFlag) {
                // операция 2
                continue
            } else if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                    END_DATE == recordVersion.versionEnd) {
                // операция 3
                continue
            }
        }
        // 4, 5, 6 - Запись справочника должна быть отредактирована
        if (NMBSP != accountName) {
            def edit = false
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // операция 4
                edit = true
            } else if (((DATE > BEG_DATE && (END_DATE == null || DATE <= END_DATE) && startDateFlag) ||
                    (DATE > BEG_DATE && END_DATE != null && DATE > END_DATE)) && endDateFlag) {
                // операция 5
                edit = true
            } else if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                    END_DATE == recordVersion.versionEnd) {
                // операция 6
                edit = true
            }
            if (edit) {
                // Запись справочника должна быть отредактирована: NMBSP должно быть присвоено атрибуту Наименование счета справочника Системы
                record?.ACCOUNT_NAME?.value = NMBSP

                dataProvider.updateRecordVersion(logger, recId, null, null, record);
                continue
            }
        }
        // 8.
        if (DATE <= BEG_DATE && (BEG_DATE == recordVersion.versionStart || END_DATE == recordVersion.versionEnd)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // операция 8
                // должны быть выполнены шаги 4-8 сценария [3] раздела 3.1.7.3 - редактирование версии элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                record?.ACCOUNT_NAME?.value = NMBSP

                dataProvider.updateRecordVersion(logger, recId, versionFrom, versionTo, record)
            }
            continue
        }

        // 9.
        if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                END_DATE != recordVersion.versionEnd) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // операция 9
                // должны быть выполнены шаги 4-8 сценария [3] раздела 3.1.7.3 - редактирование версии элемента справочника
                def versionFrom = recordVersion.versionStart
                def versionTo = END_DATE
                record?.ACCOUNT_NAME?.value = NMBSP

                dataProvider.updateRecordVersion(logger, recId, versionFrom, versionTo, record)
            }
            continue
        }

        // 10.
        if (DATE > BEG_DATE && (END_DATE == null || DATE <= END_DATE) && (startDateFlag || endDateFlag)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // операция 10
                // Иначе должны быть выполнены шаги 5 и 6 сценария [3] раздела 3.1.7.2 - создания версии элемента справочника
                def versionFrom = DATE
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                def rbRecordId = dataProvider.getRecordId(recId)
                refBookRecord.setRecordId(rbRecordId)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                dataProvider.createRecordVersion(logger, versionFrom, versionTo, [refBookRecord])
            }
            continue
        }
    }

    if (logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
    } else {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
}

void log(def s) {
    System.out.println(s)

    logger.info(s)
}

void log(def s, def ...args) {
    String msg = String.format(s, args)
    System.out.println(msg)

    logger.info(s, args)
}

/**
 * Провить дату начала и конца.
 *
 * @param BEG_DATE дата начала
 * @param END_DATE дата конца
 * @param BSSCH номер
 * @param fileRowIndexMap мапа с номера строк тф
 * @return true - если ошибки нет, false - если ошибка есть
 */
def checkEndBegDates(def BEG_DATE,def END_DATE, def BSSCH, fileRowIndexMap) {
    if (END_DATE != null && END_DATE < BEG_DATE) {
        def index = fileRowIndexMap[BSSCH]
        logger.error("Строка $index: Запись с (номер счета $BSSCH) не может быть добавлена! Так как период актуальности данной записи некорректен (дата окончания меньше даты начала).")
        return false
    }
    return true
}

def getFileRecords(def inputStream) {
    def tableQN = QName.valueOf('table')
    def fieldQN = QName.valueOf('field')
    def recordQN = QName.valueOf('record')
    def commentQN = QName.valueOf('comment')

    def isPlanbs = false // для определения раздела с данными для Плана счетов
    def recordMap = [:]
    def fileRowIndex = 0
    fileRowIndexMap = [:]
    def fileRecords = []

    def reader
    try {
        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = xmlFactory.createXMLStreamReader(inputStream)

        // пройтись по файлу и собрать значения
        while (reader.hasNext()) {
            fileRowIndex++
            if (reader.startElement) {
                if (!isPlanbs && reader.name.equals(tableQN) && "Planbs" == reader.getAttributeValue(null, 'name')) {
                    // дальше данные Плана счетов
                    isPlanbs = true
                } else if (isPlanbs && reader.name.equals(fieldQN)) {
                    // получить значение
                    def name = reader.getAttributeValue(null, 'name')
                    def value = reader.getAttributeValue(null, 'value')
                    if (name in ['BSSCH', 'NMBSP']) {
                        recordMap[name] = new RefBookValue(RefBookAttributeType.STRING, replaceQuotes(value))
                    } else if (value && name in ['DATE', 'BEG_DATE', 'END_DATE']) {
                        recordMap[name] = new RefBookValue(RefBookAttributeType.DATE, sdf.parse(value))
                    }
                    // определение номера строки в тф
                    if (name == 'BSSCH') {
                        fileRowIndexMap[value] = fileRowIndex
                    }
                }
            } else if (reader.endElement) {
                if (isPlanbs && reader.name.equals(tableQN)) {
                    // закончился раздел с данными для Плана счетов - выходим из цикла
                    break
                } else if (isPlanbs && reader.name.equals(recordQN)) {
                    fileRecords.add(recordMap)
                    recordMap = [:]
                } else if (reader.name.equals(commentQN)) {
                    fileRowIndex--
                }
            }
            reader.next()
        }
    } finally {
        reader?.close()
    }
    return fileRecords
}

/** Получить дату для поиска. Вернет бОльшую среди DATE и BEG_DATE (или END_DATE если DATE больше всех). */
def getSearchDate(def DATE, def BEG_DATE, def END_DATE) {
    if (DATE > BEG_DATE) {
        if (END_DATE != null && DATE > END_DATE) {
            return END_DATE
        }
        return DATE
    }
    return BEG_DATE
}

/**
 * Получить мапу со значениями является ли версия записи (по id) первая или последняя.
 *
 * @param recId уникальный идентификатор записи
 * @param filter фильтр для поиска
 * @param dataProvider для обращения к справочнику
 * @return возвращает мапу: с ключами isFirst, isLast для определения версии записи
 */
def getRecordVersionsFlag(def recId, def filter, def dataProvider) {
    def result = ['isFirst' : true, 'isLast' : true]
    List<Pair<Long, Long>> allVersionRecords = dataProvider.checkRecordExistence(null, filter)
    if (allVersionRecords != null && allVersionRecords.size() > 1) {
        def list = []
        for (Pair<Long, Long> pair : allVersionRecords) {
            list.add(pair.first)
        }
        def versionDateMap = dataProvider.getRecordsVersionStart(list)
        def minDate = null
        def minId = null
        def maxDate = null
        def maxId = null
        versionDateMap.each { def key, value ->
            if (minDate == null || minDate > value) {
                minDate = value
                minId = key
            }
            if (maxDate == null || maxDate < value) {
                maxDate = value
                maxId = key
            }
        }
        result.isFirst = (minId == recId)
        result.isLast = (maxId == recId)
    }
    return result
}