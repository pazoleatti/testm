package refbook.account_plan

import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.LogMessageDecorator
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
 *
 * TODO:
 *      - оставил логи (log), потом убрать
 *      - оставил вывод в конце скрипта по мапе tmpMap (мапу тоже потом убрать)
 */

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importFromXML()
        break
}

@Field
SimpleDateFormat sdf = new SimpleDateFormat('yyyy.MM.dd')

@Field
def REF_BOOK_ID = 101L

// текст строго декоратора logger'а
@Field
def oldDecoratorMsg = ""

@Field
def BSSCH_FOR_LOG = ""

// Импорт записей из XML-файла
void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID)
    def defaultDate = sdf.parse('1970.01.01')
    def fileRecords = getFileRecords(inputStream)

    // def tmpMap = [:]
    // def tmpList = (-9..9)
    // tmpList.each {
    //     tmpMap[it] = []
    // }

    // в начало сообщении добавлять «Номер счета <BSSCH>: » (и текст существующего декоратора у logger'а)
    LogMessageDecorator oldDecorator = logger.getMessageDecorator()
    logger.setMessageDecorator(getLogMessageDecorator(oldDecorator))

    // мапа для хранения мап новыми записями по дате начала записи для операции 6
    // (дата начала записи -> мапа для хранения списка новых записей по дате окончания записи (дата окончания записи -> список записей))
    def newRecords6Map = [:]
    // мапа для хранения мап новыми записями по дате начала записи для операции 9
    def newRecords9Map = [:]

    for (def row : fileRecords) {
        def BSSCH    = row?.BSSCH?.value    // Номер счета
        def NMBSP    = row?.NMBSP?.value    // Полное наименование
        def DATE     = row?.DATE?.value     // Дата последнего изменения/ФП
        def BEG_DATE = row?.BEG_DATE?.value // Дата открытия
        def END_DATE = row?.END_DATE?.value // Дата закрытия

        def date = getSearchDate(DATE, BEG_DATE, END_DATE)
        def isNewRecord = false
        def isFirstVersionRecord = null
        def isLastVersionRecord = null
        def actualRecordId = null
        BSSCH_FOR_LOG = BSSCH

        // список всех версий записи по номеру счета
        def versionDateMap = getVersionDate(BSSCH, dataProvider)
        // log("$BSSCH=======date = " + date)
        if (versionDateMap != null) {
            // поиск актуальной версии среди всех версии записи
            actualRecordId = getActualRecordId(versionDateMap, date, dataProvider)
            // log("$BSSCH=======actualRecordId = $actualRecordId")
            if (actualRecordId) {
                def recordVersionsFlag = getRecordVersionsFlag(versionDateMap, actualRecordId)
                isFirstVersionRecord = recordVersionsFlag.isFirst
                isLastVersionRecord = recordVersionsFlag.isLast
            } else {
                isNewRecord = true
            }
        } else {
            isNewRecord = true
        }

        if (isNewRecord) {
            // не найдено записей, то по таблице 19:
            // 6.
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH)) {
                // tmpMap[6].add(BSSCH)
                // log("$BSSCH=======6. ")
                // должны быть выполнены шаги 4-6 сценария [3] раздела 3.1.7.1. -  создания элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                refBookRecord.setRecordId(null)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                // сохранить изменения по 9ой операции
                saveChanges(newRecords9Map, dataProvider)
                // добавить текущую запись для вставки
                addRecord(newRecords6Map, versionFrom, versionTo, refBookRecord)
            } else {
                // tmpMap[-6].add(BSSCH)
                // log("$BSSCH=======6 false. ")
            }
            continue
        }

        // сохранить изменения по 6ой операции
        saveChanges(newRecords6Map, dataProvider)

        def recId = actualRecordId
        def record = dataProvider.getRecordData(actualRecordId)
        def accountName = record?.ACCOUNT_NAME?.value
        RefBookRecordVersion recordVersion = dataProvider.getRecordVersionInfo(actualRecordId)

        // log("$BSSCH======recordVersion.versionStart = ${recordVersion.versionStart}")
        // log("$BSSCH======recordVersion.versionEnd   = ${recordVersion.versionEnd}")
        // log("$BSSCH====== isFirstVersionRecord = $isFirstVersionRecord")
        // log("$BSSCH====== isLastVersionRecord = $isLastVersionRecord")

        // сравнения с датой начала актуальности системы и даты из тф (если версия записи в системе одна то с BEG_DATE, если версии в системе несколько, то с DATE)
        def startDateFlag = ((isFirstVersionRecord ? BEG_DATE : DATE) == recordVersion.versionStart)
        def endDateFlag = (isLastVersionRecord ? END_DATE == recordVersion.versionEnd : true)

        // найдена одна запись, то по таблице 19:
        // 1, 2, 3 - Запись должна быть проигнорирована при загрузке
        if (NMBSP == accountName) {
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // tmpMap[1].add(BSSCH)
                // log("$BSSCH=======1. ")
                // операция 1
                continue
            } else if ((DATE > BEG_DATE && (END_DATE == null || DATE <= END_DATE)) && startDateFlag && endDateFlag) {
                // tmpMap[2].add(BSSCH)
                // log("$BSSCH=======2. ")
                // операция 2
                continue
            } else if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                    END_DATE == recordVersion.versionEnd) {
                // tmpMap[3].add(BSSCH)
                // log("$BSSCH=======3. ")
                // операция 3
                continue
            }
        }
        // 4, 5 - Запись справочника должна быть отредактирована
        if (NMBSP != accountName) {
            def edit = false
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // операция 4
                // tmpMap[4].add(BSSCH)
                // log("$BSSCH=======4. ")
                edit = true
            } else if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                    END_DATE == recordVersion.versionEnd) {
                // операция 5
                // tmpMap[5].add(BSSCH)
                // log("$BSSCH=======5. ")
                edit = true
            }
            if (edit) {
                // Запись справочника должна быть отредактирована: NMBSP должно быть присвоено атрибуту Наименование счета справочника Системы
                record?.ACCOUNT_NAME?.value = NMBSP

                // сохранить изменения по 9ой операции
                saveChanges(newRecords9Map, dataProvider)

                dataProvider.updateRecordVersionWithoutLock(logger, recId, null, null, record);
                continue
            }
        }
        // 7.
        if (DATE <= BEG_DATE && conditionForOperation8(BEG_DATE == recordVersion.versionStart, END_DATE == recordVersion.versionEnd)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH)) {
                // log("$BSSCH=======7. ")
                // tmpMap[7].add(BSSCH)
                // операция 7
                // должны быть выполнены шаги 4-8 сценария [3] раздела 3.1.7.3 - редактирование версии элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                record?.ACCOUNT_NAME?.value = NMBSP

                // сохранить изменения по 9ой операции
                saveChanges(newRecords9Map, dataProvider)

                dataProvider.updateRecordVersionWithoutLock(logger, recId, versionFrom, versionTo, record)
            } else {
                // tmpMap[-7].add(BSSCH)
                // log("$BSSCH=======7 false. ")
            }
            continue
        }

        // 8.
        if (isLastVersionRecord && DATE > BEG_DATE && (END_DATE != null && DATE > END_DATE) &&
                END_DATE != recordVersion.versionEnd) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH)) {
                // tmpMap[8].add(BSSCH)
                // log("$BSSCH=======8. ")
                // операция 8
                // должны быть выполнены шаги 4-8 сценария [3] раздела 3.1.7.3 - редактирование версии элемента справочника
                def versionFrom = recordVersion.versionStart
                def versionTo = END_DATE
                record?.ACCOUNT_NAME?.value = NMBSP

                // сохранить изменения по 9ой операции
                saveChanges(newRecords9Map, dataProvider)

                dataProvider.updateRecordVersionWithoutLock(logger, recId, versionFrom, versionTo, record)
            } else {
                // tmpMap[-8].add(BSSCH)
                // log("$BSSCH=======8 false. ")
            }
            continue
        }

        // 9.
        if (DATE > BEG_DATE && (END_DATE == null || DATE <= END_DATE)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH)) {
                // tmpMap[9].add(BSSCH)
                // log("$BSSCH=======9. ")
                // операция 9
                // Иначе должны быть выполнены шаги 5 и 6 сценария [3] раздела 3.1.7.2 - создания версии элемента справочника
                def versionFrom = DATE
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                def rbRecordId = dataProvider.getRecordId(recId)
                refBookRecord.setRecordId(rbRecordId)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                // добавить текущую запись для вставки
                addRecord(newRecords9Map, versionFrom, versionTo, refBookRecord)
            } else {
                // tmpMap[-9].add(BSSCH)
                // log("$BSSCH=======9 false. ")
            }
            continue
        }
    }

    // сохранить изменения по 6ой и 9ой операции
    saveChanges(newRecords6Map, dataProvider)
    saveChanges(newRecords9Map, dataProvider)

    // TODO (Ramil Timerbaev) потом убрать
    // tmpList.each {
    //     def value = tmpMap[it]
    //     log("========== $it = " + value.size())
    // }

    if (logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
    } else {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
    // вернуть старый декоратор
    logger.setMessageDecorator(oldDecorator)
}

// TODO (Ramil Timerbaev)
void log(def s) {
    // System.out.println(s)

    // logger.info(s)
}

void log(def s, def ...args) {
    String msg = String.format(s, args)
    // System.out.println(msg)

    // logger.info(s, args)
}

/**
 * Провить дату начала и конца.
 *
 * @param BEG_DATE дата начала
 * @param END_DATE дата конца
 * @param BSSCH номер
 * @return true - если ошибки нет, false - если ошибка есть
 */
def checkEndBegDates(def BEG_DATE,def END_DATE, def BSSCH) {
    if (END_DATE != null && END_DATE < BEG_DATE) {
        logger.error("Запись не может быть добавлена! Так как период актуальности данной записи некорректен (дата окончания меньше даты начала).")
        return false
    }
    return true
}

def getFileRecords(def inputStream) {
    if (inputStream.available() == 0) {
        logger.error("Файл пуст.")
        return null
    }
    def tableQN = QName.valueOf('table')
    def fieldQN = QName.valueOf('field')
    def recordQN = QName.valueOf('record')

    def isPlanbs = false // для определения раздела с данными для Плана счетов
    def recordMap = [:]
    def fileRecords = []

    def reader
    try {
        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = xmlFactory.createXMLStreamReader(inputStream, "windows-1251")

        // пройтись по файлу и собрать значения
        while (reader.hasNext()) {
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
                }
            } else if (reader.endElement) {
                if (isPlanbs && reader.name.equals(tableQN)) {
                    // закончился раздел с данными для Плана счетов - выходим из цикла
                    break
                } else if (isPlanbs && reader.name.equals(recordQN)) {
                    fileRecords.add(recordMap)
                    recordMap = [:]
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
 * @param versionDateMap мапа со всеми версиями записи
 * @param recId уникальный идентификатор записи
 * @return возвращает мапу: с ключами isFirst, isLast для определения версии записи
 */
def getRecordVersionsFlag(def versionDateMap, def recId) {
    def result = ['isFirst' : true, 'isLast' : true]

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
    return result
}

/**
 * Условие перед операцией 8.
 *
 * | BEG_DATE | END_DATE | результат |
 * |----------|----------|-----------|
 * | не равно | не равно | true      |
 * | не равно | равно    | true      |
 * | равно    | не равно | true      |
 * | равно    | равно    | false     |
 *
 * @param conditionBeg равен ли BEG_DATE дате начала актуальности
 * @param conditionEnd равен ли END_DATE дате окончания актуальности
 */
def conditionForOperation8(def conditionBeg, def conditionEnd) {
    return (!conditionBeg && !conditionEnd) || (conditionBeg ^ conditionEnd)
}

/**
 * Получить список всех версий записи по номеру счета.
 *
 * @param BSSCH номер счетоа
 * @return мапа уникальный идентификатор записи и дат начала актульности
 */
def getVersionDate(def BSSCH, def dataProvider) {
    // поиск по уникальному атрибуту, если находится запись, то она всегда одна
    def filter = "ACCOUNT = '$BSSCH'"
    List<Pair<Long, Long>> pairs = dataProvider.getRecordIdPairs(REF_BOOK_ID, null, null, filter)
    if (pairs == null || pairs.size() == 0) {
        return  null
    } else if (pairs.size() > 0) {
        def list = []
        for (Pair<Long, Long> pair : pairs) {
            list.add(pair.first)
        }
        return dataProvider.getRecordsVersionStart(list)
    }
}

/**
 * Получить идентификатор актуальной записи из мапы со всеми версиями записи.
 *
 * @param versionDateMap мапа со всеми версиями записи
 * @param date дата на которую надо получить данные
 * @param dataProvider
 * @return идентификатор записи (ref_book_record.id)
 */
def getActualRecordId(def versionDateMap, def date, def dataProvider) {
    def actualRecordId = null

    // поиск id актуальной записи на дату date
    def minDays = null
    versionDateMap.each { def key, value ->
        if (minDays != 0) {
            if (date == value || (date > value && (minDays == null  || date - value < minDays))) {
                minDays = date - value
                actualRecordId = key
            }
        }
    }
    // если нет актуальной записи, то брать следующую за ней
    if (actualRecordId == null) {
        minDays = null
        versionDateMap.each { def key, value ->
            if (date < value && (minDays == null  || date - value < minDays)) {
                minDays = date - value
                actualRecordId = key
            }
        }
    }
    return actualRecordId
}

/**
 * Получить новый декоратор для logger'а. Формат сообщения:<br>
 *     <Тест старого декоратора>: Номер счета <Номер счета>: сообещение
 *
 * @param oldDecorator старый декоратор
 */
def getLogMessageDecorator(LogMessageDecorator oldDecorator) {
    if (oldDecorator != null && oldDecorator instanceof ScriptMessageDecorator) {
        // получить текст строго декоратора
        oldDecoratorMsg = ((ScriptMessageDecorator) oldDecorator).getScriptName() + ": "
    }
    LogMessageDecorator logMessageDecorator = new LogMessageDecorator() {
        @Override
        String getDecoratedMessage(String message) {
            return oldDecoratorMsg + "Номер счета $BSSCH_FOR_LOG: $message"
        }
    }
    return logMessageDecorator
}

/**
 * Добавить запись в мапу для хранения мап со списками записей.
 *
 * @param recordsMap мап для хранения мап со списками записей
 * @param versionFrom дата начала актуальности записи
 * @param versionTo дата окончания актуальности записи
 * @param refBookRecord запись для добавления
 */
void addRecord(def recordsMap, def versionFrom, def versionTo, def refBookRecord) {
    // если на дату versionFrom были записи, то добавить новую запись
    if (recordsMap[versionFrom]) {
        // если на дату versionTo были записи. то добавить новую запись туда
        if (recordsMap[versionFrom][versionTo]) {
            recordsMap[versionFrom][versionTo].add(refBookRecord)
        } else {
            // записей до этого не было, добавить новый список с одной новой записью
            recordsMap[versionFrom][versionTo] = [refBookRecord]
        }
    } else {
        // на дату versionFrom не было записей, добавляем на эту запись мапу и на дату окончания versionTo добавить новый список с одной новой записью
        recordsMap[versionFrom] = [:]
        recordsMap[versionFrom][versionTo] = [refBookRecord]
    }
}

/**
 * Добавить записи из мап с записями в базу.
 *
 * @param recordsMap мапа для хранения мап со списком записей
 * @param dataProvider для доступа к справочнику
 */
void saveChanges(def recordsMap, def dataProvider) {
    if (!recordsMap.isEmpty()) {
        recordsMap.each { versionFrom, map ->
            map.each { versionTo, newRecords ->
                dataProvider.createRecordVersionWithoutLock(logger, versionFrom, versionTo, newRecords)
            }
        }
        recordsMap.clear()
    }
}