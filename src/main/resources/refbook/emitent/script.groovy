package refbook.emitent

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Cкрипт справочника «Эмитенты» (id = 100)
 * Diasoft
 * blob_data.id = 4e1a8f16-3dfa-41b5-a71b-ba37efa2fadb
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importFromNSI()
        break
}

@Field
def REFBOOK_ID = 100

void importFromNSI() {
    // На вход могут поступать как «Эмитенты», так и «Ценные бумаги», скрит сам должен определить «свой» ли файл
    println("Import Emitent: file name = $fileName")

    // Список добавляемых
    def addList = []
    // Список обновляемых
    def updList = []

    // Строки файла
    def lines = []

    // Проверки
    inputStream?.eachLine { line ->
        if ((line=~ /;/).count != 4) {
            // Не «Эмитенты»
            scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
            return
        }
        lines.add(line)
    }

    if (scriptStatusHolder.getScriptStatus().equals(ScriptStatus.SKIP) || lines.isEmpty()) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
        scriptStatusHolder.setStatusMessage("Неверная структура файла «$fileName»!")
        return
    }

    println("Import Emitent: strings count = " + lines.size())

    def actualDate = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime()

    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)

    // Получение актуальной версии справочника
    def actualEmitentList = dataProvider.getRecords(actualDate, null, null, null)
    println("Import Emitent: Current Emitent found record count = " + actualEmitentList?.size())
    def actualEmitentnMap = [:]
    actualEmitentList?.each { map ->
        actualEmitentnMap.put(map.CODE.stringValue, map)
    }

    lines.each { line ->
        def lineStrs = line.split(";")
        def code = replaceQuotes(lineStrs[0] as String)
        def name = replaceQuotes(lineStrs[1] as String)
        def fullName = replaceQuotes(lineStrs[2] as String)

        if (code != null && !code.isEmpty()) {
            def actualRecord = actualEmitentnMap.get(code)
            if (actualRecord == null) {
                // Добавление новой записи
                def record = new RefBookRecord()
                map = [:]
                map.put("CODE", new RefBookValue(RefBookAttributeType.STRING, code))
                map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name))
                map.put("FULL_NAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
                record.setValues(map)
                addList.add(record)
            } else {
                // Обновление существующей версии
                if (actualRecord.NAME.stringValue != name || actualRecord.FULL_NAME.stringValue != fullName) {
                    actualRecord.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name))
                    actualRecord.put("FULL_NAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
                    updList.add(actualRecord)
                }
            }
        }
    }

    println("Import Emitent: File records size = ${lines.size()}")

    println("Import Emitent: Add count = ${addList.size()}, Update count = ${updList.size()}")

    if (!logger.containsLevel(LogLevel.ERROR) && !addList.isEmpty()) {
        dataProvider.createRecordVersionWithoutLock(logger, actualDate, null, addList)
    }

    if (!logger.containsLevel(LogLevel.ERROR) && !updList.isEmpty()) {
        dataProvider.updateRecordsWithoutLock(userInfo, actualDate, updList)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
}
