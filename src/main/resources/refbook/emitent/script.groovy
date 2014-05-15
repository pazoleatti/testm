package refbook.emitent

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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
    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    logger.info("fileName = $fileName")
    def addList = []
    // TODO Необходимо реализовать поиск уже имеющихся записей и формировать три списка — на создание, изменение, обновление http://conf.aplana.com/pages/viewpage.action?pageId=12322877
    inputStream?.eachLine { line ->
        def lineStrs = line.split(";")
        def code = lineStrs[0]
        def name = lineStrs[1]
        def fullName = lineStrs[2]

        def record = new RefBookRecord()
        map = [:]
        map.put("CODE", new RefBookValue(RefBookAttributeType.STRING, code))
        map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name))
        map.put("FULL_NAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
        record.setRecordId(null)
        record.setValues(map)
        addList.add(record)
    }

    println("File records size = ${addList.size()}")

    dataProvider.createRecordVersion(logger, new Date(), null, addList)
}
