package refbook.bond

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Cкрипт справочника «Ценные бумаги» (id = 84)
 * Diasoft
 * blob_data.id = b43514e5-55dd-4b74-9ce5-60007c32d6a6
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importFromNSI()
        break
    case FormDataEvent.SAVE:
        save()
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

    if (scriptStatusHolder.getScriptStatus().equals(ScriptStatus.SKIP) || lines.isEmpty()) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
        scriptStatusHolder.setStatusMessage("Неверная структура файла «$fileName»!")
        return
    }

    println("Import Bonds: strings count = " + lines.size())

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
        actualBondMap.put(map?.ID?.numberValue?.toString(), map)
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
        def fullName = replaceQuotes(lineStrs[3] as String)
        def shortName = replaceQuotes(lineStrs[4] as String)
        def regNum = replaceQuotes(lineStrs[5] as String)
        def sign = lineStrs[6]?.replace('0','-')?.replace('1','+') as String
        def type = lineStrs[7] as String
        def startDateStr = lineStrs[8] as String
        def endDateStr = lineStrs[9] as String
        def startDate = (startDateStr == null || startDateStr.isEmpty()) ?: dateFormat.parse(startDateStr)
        def endDate = (endDateStr == null || endDateStr.isEmpty()) ?: dateFormat.parse(endDateStr)

        if (startDateStr != null && startDateStr.equals("01.01.1900")) {
            startDate = null
        }

        if (endDateStr != null && endDateStr.equals("01.01.1900")) {
            endDate = null
        }

        if (fullName.trim().isEmpty()) {
            fullName = null
        }

        if (shortName.trim().isEmpty()) {
            shortName = null
        }

        if (regNum.trim().isEmpty()) {
            regNum = null
        }

        if (id != null && !id.isEmpty()) {
            // TODO Заменить warn на error после ответов из Банка
            // Проверка ссылки на «Эмитент»
            def issuerRef =  actualEmitentnMap[issuer]?.get(RefBook.RECORD_ID_ALIAS)?.numberValue
            def String errIndex = "Для записи «Идентификатор ценной бумаги» = $id файла «$fileName»"
            if (issuer != null && !issuer.isEmpty() && issuerRef == null) {
                logger.warn(errIndex + " не удается заполнить атрибут «Эмитент» = $issuer, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Цифровой код валюты выпуска»
            def curRef =  actualCurrencyMap[codeCur]?.get(RefBook.RECORD_ID_ALIAS)?.numberValue
            if (codeCur != null && !codeCur.isEmpty() && curRef == null) {
                logger.warn(errIndex + " не удается заполнить атрибут «Цифровой код валюты выпуска» = $codeCur, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Признак ценной бумаги»
            def signRef =  actualBondSignMap[sign]?.get(RefBook.RECORD_ID_ALIAS)?.numberValue
            if (sign != null && !sign.isEmpty() && signRef == null) {
                logger.warn(errIndex + " не удается заполнить атрибут «Признак ценной бумаги» = $sign, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка ссылки на «Тип (вид) ценной бумаги»
            def typeRef =  actualBondTypeMap[type]?.get(RefBook.RECORD_ID_ALIAS)?.numberValue
            if (type != null && !type.isEmpty() && typeRef == null) {
                logger.warn(errIndex + " не удается заполнить атрибут «Тип (вид) ценной бумаги» = $type, т.к. такой элемент отсутствует в Системе!")
            }
            // Проверка диапазона дат
            if (startDate != null && endDate != null && startDate > endDate) {
                logger.error(errIndex + " поле «Дата окончания действия» (${endDate.format('dd.MM.yyyy')}) должно быть больше или равно полю «Дата начала действия» (${startDate.format('dd.MM.yyyy')})!")
            }

            def actualRecord = actualBondMap.get(id)

            if (actualRecord == null) {
                // Добавление новой записи
                def record = new RefBookRecord()
                map = [:]
                map.put("ID", new RefBookValue(RefBookAttributeType.NUMBER, id.toLong()))
                if (issuerRef != null) {
                    map.put("ISSUER", new RefBookValue(RefBookAttributeType.REFERENCE, issuerRef))
                }
                if (curRef != null) {
                    map.put("CODE_CUR", new RefBookValue(RefBookAttributeType.REFERENCE, curRef))
                }
                if (signRef != null) {
                    map.put("SIGN", new RefBookValue(RefBookAttributeType.REFERENCE, signRef))
                }
                if (typeRef != null) {
                    map.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, typeRef))
                }
                map.put("FULLNAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
                map.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, shortName))
                map.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, regNum))
                map.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, startDate))
                map.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, endDate))
                record.setValues(map)
                addList.add(record)
            } else {
                // Обновление существующей версии
                if (actualRecord.ISSUER.referenceValue != issuerRef
                        || actualRecord.CODE_CUR.referenceValue != curRef
                        || actualRecord.FULLNAME.stringValue != fullName
                        || actualRecord.SHORTNAME.stringValue != shortName
                        || actualRecord.REG_NUM.stringValue != regNum
                        || actualRecord.SIGN.referenceValue != signRef
                        || actualRecord.TYPE.referenceValue != typeRef
                        || actualRecord.START_DATE.dateValue != startDate
                        || actualRecord.END_DATE.dateValue != endDate
                ) {
                    actualRecord.put("ISSUER", new RefBookValue(RefBookAttributeType.REFERENCE, issuerRef))
                    actualRecord.put("CODE_CUR", new RefBookValue(RefBookAttributeType.REFERENCE, curRef))
                    actualRecord.put("FULLNAME", new RefBookValue(RefBookAttributeType.STRING, fullName))
                    actualRecord.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, shortName))
                    actualRecord.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, regNum))
                    actualRecord.put("SIGN", new RefBookValue(RefBookAttributeType.REFERENCE, signRef))
                    actualRecord.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, typeRef))
                    actualRecord.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, startDate))
                    actualRecord.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, endDate))
                    updList.add(actualRecord)
                }
            }
        }
    }

    println("Import Bonds: File records size = ${lines.size()}")

    println("Import Bonds: Add count = ${addList.size()}, Update count = ${updList.size()}")

    if (!logger.containsLevel(LogLevel.ERROR) && !addList.isEmpty()) {
        dataProviderBond.createRecordVersionWithoutLock(logger, actualDate, null, addList)
    }

    if (!logger.containsLevel(LogLevel.ERROR) && !updList.isEmpty()) {
        dataProviderBond.updateRecordsWithoutLock(userInfo, actualDate, updList)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
    }
}

void save() {
    saveRecords.each {
        def Date startDate = it.START_DATE?.dateValue
        def Date endDate = it.END_DATE?.dateValue
        if (startDate != null && endDate != null && startDate > endDate) {
            logger.error("Поле «Дата окончания действия» должно быть больше или равно полю «Дата начала действия»!")
        }
    }
}