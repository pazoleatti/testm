package refbook.account_plan

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * План счетов бухгалтерского учета
 * blob_data.id = 'e2a67f8a-b976-4696-a778-f21a0e602a3f'
 * ref_book_id = 101
 *
 * TODO:
 *      - скрипт недоделан: проблемы с пунтктом 7
 *      - закомментировал сохранения / изменения данных в базу
 *      - проверить массовую загрузку (загрузку большого файла с множеством строк)
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

// Импорт записей из XML-файла
void importFromXML() {
    def dataProvider = refBookFactory.getDataProvider(101L)

    def tableQN = QName.valueOf('table')
    def fieldQN = QName.valueOf('field')
    def recordQN = QName.valueOf('record')
    def commentQN = QName.valueOf('comment')

    def sdf = new SimpleDateFormat('yyyy.MM.dd')
    def defaultDate = sdf.parse('1970.01.01')
    def isPlanbs = false // для определения раздела с данными для Плана счетов
    def fileRecords = []
    def recordMap = [:]
    def fileRowIndex = 0
    def fileRowIndexMap = [:]

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

    println("Import account_plan: fileRecords = " + fileRecords.size()) // TODO (Ramil Timerbaev)

    for (def row : fileRecords) {
        def BSSCH    = row?.BSSCH?.value    // Номер счета
        def NMBSP    = row?.NMBSP?.value    // Полное наименование
        def DATE     = row?.DATE?.value     // Дата последнего изменения/ФП
        def BEG_DATE = row?.BEG_DATE?.value // Дата открытия
        def END_DATE = row?.END_DATE?.value // Дата закрытия

        // log("====== BSSCH = $BSSCH, NMBSP = $NMBSP, DATE = $DATE, BEG_DATE = $BEG_DATE, END_DATE = $END_DATE") // TODO (Ramil Timerbaev)

        def date = (DATE > BEG_DATE ? DATE : BEG_DATE)
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

        // log("$BSSCH================records = ${records.size()}") // TODO (Ramil Timerbaev)

        if (count > 1) {
            logger.error("$BSSCH: найдено более одной записи с заданным номером счета!")
            continue
        } else if (count == 0) {
            // не найдено записей, то по таблице 19:
            // 5.
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // должны быть выполнены шаги 4-6 сценария [3] раздела 3.1.7.1. -  создания элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                refBookRecord.setRecordId(null)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                // log("======= 5. >>>> versionFrom = $versionFrom, versionTo = $versionTo, refBookRecord = $refBookRecord") // TODO (Ramil Timerbaev)

                // dataProvider.createRecordVersion(logger, versionFrom, versionTo, [refBookRecord])
            }
            continue
        }

        def record = maxRecord
        def accountName = record?.ACCOUNT_NAME?.value
        def recId = record?.record_id?.value

        RefBookRecordVersion recordVersion = maxRecordVersion
        recordVersion.versionEnd
        recordVersion.versionStart

        // log("======== recordVersion = ${recordVersion}") // TODO (Ramil Timerbaev)

        // найдена одна запись, то по таблице 19:
        // 1. и 2. - Запись должна быть проигнорирована при загрузке
        if (NMBSP == accountName) {
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // log("=======1. ignore") // TODO (Ramil Timerbaev)
                continue
            } else if (DATE > BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // log("=======2. ignore") // TODO (Ramil Timerbaev)
                continue
            }

        }
        // 3. и 4. - Запись справочника должна быть отредактирована
        if (NMBSP != accountName) {
            def edit = false
            if (DATE <= BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // log("=======3.") // TODO (Ramil Timerbaev)
                edit = true
            } else if (DATE > BEG_DATE && BEG_DATE == recordVersion.versionStart && END_DATE == recordVersion.versionEnd) {
                // log("=======4.") // TODO (Ramil Timerbaev)
                edit = true
            }
            if (edit) {
                // Запись справочника должна быть отредактирована: Значение атрибута NMBSP записи в ТФ должно быть присвоено атрибуту Наименование счета справочника Системы
                def versionFrom = null
                def versionTo = null
                record?.ACCOUNT_NAME?.value = NMBSP

                // log("======= 3. 4. >>>> recId = $recId, versionFrom = $versionFrom, versionTo = $versionTo, record = $record") // TODO (Ramil Timerbaev)

                // dataProvider.updateRecordVersion(logger, recId, null, null, record);
                continue
            }
        }
        // 6.
        if (DATE <= BEG_DATE && (BEG_DATE == recordVersion.versionStart || END_DATE == recordVersion.versionEnd)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // должны быть выполнены шаги 4-8 сценария [3] раздела 3.1.7.3 - редактирование версии элемента справочника
                def versionFrom = (BEG_DATE ?: defaultDate)
                def versionTo = END_DATE
                record?.ACCOUNT_NAME?.value = NMBSP

                // log("======= 6. >>>> recId = $recId, versionFrom = $versionFrom, versionTo = $versionTo, record = $record") // TODO (Ramil Timerbaev)

                // dataProvider.updateRecordVersion(logger, recId, versionFrom, versionTo, record)
            }
            continue
        }
        // 7.
        if (DATE > BEG_DATE && (BEG_DATE == recordVersion.versionStart || END_DATE == recordVersion.versionEnd)) {
            if (checkEndBegDates(BEG_DATE, END_DATE, BSSCH, fileRowIndexMap)) {
                // Иначе должны быть выполнены шаги 5 и 6 сценария [3] раздела 3.1.7.2 - создания версии элемента справочника
                def versionFrom = DATE
                def versionTo = END_DATE
                RefBookRecord refBookRecord = new RefBookRecord()
                def rbRecordId = dataProvider.getRecordId(recId)
                // log("============ rbRecordId = $rbRecordId") // TODO (Ramil Timerbaev)
                refBookRecord.setRecordId(rbRecordId)
                refBookRecord.setValues(['ACCOUNT' : row.BSSCH, 'ACCOUNT_NAME' : row.NMBSP])

                // log("======= 7. >>>> versionFrom = $versionFrom, versionTo = $versionTo, refBookRecord = $refBookRecord") // TODO (Ramil Timerbaev)

                // dataProvider.createRecordVersion(logger, versionFrom, versionTo, [refBookRecord])
            }
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
}

void log(def s, def ...args) {
    String msg = String.format(s, args)
    System.out.println(msg)
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

        // TODO (Ramil Timerbaev)
        log("Строка $index: Запись с (номер счета $BSSCH) не может быть добавлена! Так как период актуальности данной записи некорректен (дата окончания меньше даты начала).")

        return false
    }
    return true
}