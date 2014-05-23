package refbook.emitent

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
    case FormDataEvent.IMPORT:
        importFromNSI()
        break
}

@Field
def REFBOOK_ID = 100

void importFromNSI() {
    // На вход могут поступать как «Эмитенты», так и «Ценные бумаги», скрит сам должен определить «свой» ли файл

    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)

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
            return
        }
        lines.add(line)
    }

    println("Import Emitent: strings count = " + lines.size())

    if (lines.isEmpty()) {
        return
    }

    def actualDate = new GregorianCalendar(1970, Calendar.JANUARY, 1).getTime()

    // Получение актуальной версии справочника
    def actualEmitentList = dataProvider.getRecords(actualDate, null, null, null)
    println("Import Emitent: Current Emitent found record count = " + actualEmitentList?.size())
    def actualEmitentnMap = [:]
    actualEmitentList?.each { map ->
        actualEmitentnMap.put(map.CODE.stringValue, map)
    }

    lines.each { line ->
        def lineStrs = line.split(";")
        def code = lineStrs[0]
        def name = lineStrs[1]
        def fullName = lineStrs[2]

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

    if (!addList.isEmpty()) {
        dataProvider.createRecordVersion(logger, actualDate, null, addList)
    }

    if (!updList.isEmpty()) {
        dataProvider.updateRecords(actualDate, updList)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        logger.info("Импорт успешно выполнен.")
    }
}
