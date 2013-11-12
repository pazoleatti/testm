/*
    blob_data.id = '99e90406-60f0-4a87-b6f0-7f127abf1fbb'
 */
package refbook.metal_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import java.text.SimpleDateFormat

/**
 * скрипт загрузки справочника "Курсы драгоценных металлов" из КСШ
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromXML()
        break
}

void importFromXML() {
    def final REFBOOK_ID = 90
    stringCode = "CODE" // имя аттрибута "Внутренний код"
    stringRate = "RATE" // имя аттрибута "Курс драгоценного металла"

    def dataProvider = refBookFactory.getDataProvider(REFBOOK_ID)
    def refBook = refBookFactory.get(REFBOOK_ID)
    def SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
    def reader = null
    def Date version = null  //дата актуальности
    def boolean rateSector = false // флаг присутствия в секции с курсами
    def Map<String, RefBookValue> recordsMap // аттрибут и его значение
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>() // новые записи
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>() // измененные записи
    def Long code = null // код драг. металла
    def Double rate = null // курс драг. металла
    def RefBookAttributeType codeType = null // тип кода
    def RefBookAttributeType rateType = null // тип курса
    def Map<String, Number> recordsDB = new HashMap<String, Number>() // записи в БД

    refBook.attributes.each {
        if (it.alias.equals(stringCode))
            codeType = it.attributeType
        else if (it.alias.equals(stringRate))
            rateType = it.attributeType
    }

    try {

        def XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        reader = factory.createXMLStreamReader(inputStream)

        while (reader.hasNext()) {
            if (reader.startElement) {

                // Версия справочника
                if (reader.getName().equals(QName.valueOf("StartDateTime"))) {
                    version = sdf.parse(reader.getElementText())
                    dataProvider.getRecords(version, null, null, null).records.each {
                        if (it.get(stringCode) != null) {
                            recordsDB.put(it.get(stringCode).referenceValue, it.get(RefBook.RECORD_ID_ALIAS).numberValue)
                        }
                    }
                }

                //Дошли до секции с курсами
                if (reader.getName().equals(QName.valueOf("PreciousMetalRates"))) {
                    rateSector = true
                }

                // Код драг. металла
                if (rateSector && reader.getName().equals(QName.valueOf("Code"))) {
                    def String val = reader.getElementText()
                    def records = refBookFactory.getDataProvider(17).getRecords(version, null, "LOWER(INNER_CODE) = LOWER('$val')", null)
                    if (records.size() > 0) {
                        code = records.get(0).record_id.numberValue
                    } else{
                        throw new Exception("В справочнике «Коды драгоценных металлов» отсутствует элемент с кодом '$val'")
                    }
                }

                // Курс драг. металла
                if (rateSector && reader.getName().equals(QName.valueOf("Rate"))) {
                    rate = reader.getElementText().toDouble()
                }
            }

            // Запись в лист
            if (reader.endElement && reader.getName().equals(QName.valueOf("PreciousMetalRate"))) {
                recordsMap = new HashMap<String, RefBookValue>()
                recordsMap.put(stringCode, new RefBookValue(codeType, code))
                recordsMap.put(stringRate, new RefBookValue(rateType, rate))
                if (recordsDB.containsKey(code)) {
                    recordsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordsDB.get(code)))
                    updateList.add(recordsMap)
                } else {
                    insertList.add(recordsMap)
                }
            }

            reader.next()
        }
    } finally {
        reader?.close()
    }

   if (!updateList.empty)
       dataProvider.updateRecords(version, updateList)
   if (!insertList.empty)
       dataProvider.insertRecords(version, insertList)
}