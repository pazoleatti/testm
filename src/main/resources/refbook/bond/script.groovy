package refbook.bond

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Cкрипт справочника «Ценные бумаги» (id = 84)
 * Diasoft
 * blob_data.id = b43514e5-55dd-4b74-9ce5-60007c32d6a6
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        importFromNSI()
        break
}

@Field
def REFBOOK_BOND_ID = 84

@Field
def REFBOOK_EMITENT_ID = 100

@Field
def REFBOOK_BOND_TYPE_ID = 89

@Field
def REFBOOK_BOND_SIGN_ID = 62

@Field
def REFBOOK_CURRENCY_ID = 15

void importFromNSI() {
    // На вход могут поступать как «Эмитенты», так и «Ценные бумаги», скрит сам должен определить «свой» ли файл
    println("Import Bonds: file name = $fileName")

    // Список добавляемых
    def addList = []
    // Список обновляемых
    def updList = []

    // Строки файла
    def lines = []

    // Проверки
    inputStream?.eachLine { line ->
        if ((line=~ /;/).count != 9) {
            // Не «Ценные бумаги»
            scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
            scriptStatusHolder.setStatusMessage("Неверная структура файла «$fileName»!")
            return
        }
        lines.add(line)
    }

    println("Import Bonds: strings count = " + lines.size())

    if (lines.isEmpty()) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
        scriptStatusHolder.setStatusMessage("Неверная структура файла «$fileName»!")
        return
    }

    def actualDate = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime()

    def dataProviderBond = refBookFactory.getDataProvider(REFBOOK_BOND_ID)
    def dataProviderEmitent = refBookFactory.getDataProvider(REFBOOK_EMITENT_ID)
    def dataProviderBondType = refBookFactory.getDataProvider(REFBOOK_BOND_TYPE_ID)
    def dataProviderBondSign = refBookFactory.getDataProvider(REFBOOK_BOND_SIGN_ID)
    def dataProviderCurrency = refBookFactory.getDataProvider(REFBOOK_CURRENCY_ID)

    // Получение актуальной версии справочника «Ценные бумаги»
    def actualBondList = dataProviderBond.getRecords(actualDate, null, null, null)
    println("Import Bonds: Current Bonds found record count = " + actualBondList?.size())
    def actualBondMap = [:]
    actualBondList?.each { map ->
        actualBondMap.put(map.ID.numberValue, map)
    }

    // Получение актуальной версии справочника «Эмитенты»
    def actualEmitentList = dataProviderEmitent.getRecords(actualDate, null, null, null)
    println("Import Bonds: Current Emitent found record count = " + actualEmitentList?.size())
    def actualEmitentnMap = [:]
    actualEmitentList?.each { map ->
        actualEmitentnMap.put(map.CODE.stringValue, map)
    }

    // Получение актуальной версии справочника «Виды ценных бумаг»
    def actualBondTypeList = dataProviderBondType.getRecords(actualDate, null, null, null)
    println("Import Bonds: Current Bonds Types found record count = " + actualBondTypeList?.size())
    def actualBondTypeMap = [:]
    actualBondTypeList?.each { map ->
        actualBondTypeMap.put(map.CODE.numberValue as String, map)
    }

    // Получение актуальной версии справочника «Общероссийский классификатор валют»
    def actualCurrencyList = dataProviderCurrency.getRecords(actualDate, null, null, null)
    println("Import Bonds: Current Currency found record count = " + actualCurrencyList?.size())
    def actualCurrencyMap = [:]
    actualCurrencyList?.each { map ->
        actualCurrencyMap.put(map.CODE.stringValue, map)
    }

    // Получение актуальной версии справочника «Признаки ценных бумаг»
    def actualBondSignList = dataProviderBondSign.getRecords(actualDate, null, null, null)
    println("Import Bonds: Current Bonds Sign found record count = " + actualBondSignList?.size())
    def actualBondSignMap = [:]
    actualBondSignList?.each { map ->
        actualBondSignMap.put(map.CODE.stringValue, map)
    }

    def dateFormat = new SimpleDateFormat("dd.MM.yyyy")

    lines.each { line ->
        def lineStrs = line.split(";")
        def id = lineStrs[0] as String
        def issuer = lineStrs[1] as String
        def codeCur = lineStrs[2] as String
        def fullName = lineStrs[3] as String
        def shortName = lineStrs[4] as String
        def regNum = lineStrs[5] as String
        def sign = lineStrs[6]?.replace('0','-')?.replace('1','+') as String
        def type = lineStrs[7] as String
        def startDateStr = lineStrs[8] as String
        def endDateStr = lineStrs[9] as String
        def startDate = (startDateStr == null || startDateStr.isEmpty()) ?: dateFormat.parse(startDateStr)
        def endDate = (endDateStr == null || endDateStr.isEmpty()) ?: dateFormat.parse(endDateStr)

        if (id != null && !id.isEmpty()) {
            // Проверка ссылки на «Эмитент»
            if (issuer != null && !issuer.isEmpty() && !actualEmitentnMap.containsKey(issuer)) {
                logger.error("Для записи «Идентификатор ценной бумаги» = $id файла «$fileName» не удается заполнить атрибут «Эмитент» = $issuer, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Цифровой код валюты выпуска»
            if (codeCur != null && !codeCur.isEmpty() && !actualCurrencyMap.containsKey(codeCur)) {
                logger.error("Для записи «Идентификатор ценной бумаги» = $id файла «$fileName» не удается заполнить атрибут «Цифровой код валюты выпуска» = $codeCur, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Признак ценной бумаги»
            if (sign != null && !sign.isEmpty() && !actualBondSignMap.containsKey(sign)) {
                logger.error("Для записи «Идентификатор ценной бумаги» = $id файла «$fileName» не удается заполнить атрибут «Признак ценной бумаги» = $sign, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Тип (вид) ценной бумаги»
            if (type != null && !type.isEmpty() && !actualBondTypeMap.containsKey(type)) {
                logger.error("Для записи «Идентификатор ценной бумаги» = $id файла «$fileName» не удается заполнить атрибут «Тип (вид) ценной бумаги» = $type, т.к. такой элемент отсутствует в Системе!")
            }
        }

//        if (code != null && !code.isEmpty()) {
//            def actualRecord = actualEmitentnMap.get(code)
//            if (actualRecord == null) {
//                // Добавление новой записи
//                def record = new RefBookRecord()
//                map = [:]
//                map.put("ID", new RefBookValue(RefBookAttributeType.NUMBER, code))
//                map.put("ISSUER", new RefBookValue(RefBookAttributeType.REFERENCE, code))
//
//
//                map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name))
//                map.put("FULL_NAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
//                record.setValues(map)
//                addList.add(record)
//            } else {
//                // Обновление существующей версии
//                if (actualRecord.NAME.stringValue != name || actualRecord.FULL_NAME.stringValue != fullName) {
//                    actualRecord.put("NAME", new RefBookValue(RefBookAttributeType.STRING, name))
//                    actualRecord.put("FULL_NAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
//                    updList.add(actualRecord)
//                }
//            }
//        }
    }

    println("Import Bonds: File records size = ${lines.size()}")

    println("Import Bonds: Add count = ${addList.size()}, Update count = ${updList.size()}")

//    if (!logger.containsLevel(LogLevel.ERROR) && !addList.isEmpty()) {
//        dataProvider.createRecordVersion(logger, actualDate, null, addList)
//    }
//
//    if (!logger.containsLevel(LogLevel.ERROR) && !updList.isEmpty()) {
//        dataProvider.updateRecords(actualDate, updList)
//    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
}