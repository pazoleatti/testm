package form_template.income.rnu27

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.12 (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
 */

/** Признак периода ввода остатков. */
def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        allCheck()
        break
    case FormDataEvent.CALCULATE:
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
    case FormDataEvent.ADD_ROW:
        //deleteAllStatic()
        addNewRowwarnrmData()
        break
    case FormDataEvent.DELETE_ROW:
        //deleteAllStatic()
        deleteRow()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
    case FormDataEvent.IMPORT :
        importData()
        break
}

// графа 1  - число  number № пп
// графа 2  - строка issuer эмитит
// графа 3  - строка regNumber гос номер
// графа 4  - строка tradeNumber Номер сделки
// графа 5  - строка currency Валюта выпуска облигации (справочник)
// графа 6  - число  prev Размер лота на отчётную дату по депозитарному учёту (шт.). Предыдущую
// графа 7  - число  current Размер лота на отчётную дату по депозитарному учёту (шт.). Текущую
// графа 8  - число  reserveCalcValuePrev Расчётная величина резерва на предыдущую отчётную дату (руб.коп.)
// графа 9  - число  cost Стоимость по цене приобретения (руб.коп.)
// графа 10 - строка signSecurity Признак ценной бумаги на текущую отчётную дату (справочник)
// графа 11 - число  marketQuotation Quotation Рыночная котировка одной ценной бумаги в иностранной валюте
// графа 12 - число  rubCourse Курс рубля к валюте рыночной котировки
// графа 13 - число  marketQuotationInRub Рыночная котировка одной ценной бумаги в рублях
// графа 14 - число  costOnMarketQuotation costOnMarketQuotation
// графа 15 - число  reserveCalcValue Расчетная величина резерва на текущую отчётную дату (руб.коп.)
// графа 16 - число  reserveCreation Создание резерва (руб.коп.)
// графа 17 - число  recovery Восстановление резерва (руб.коп.)

/**
 * 6.11.2.4.1   Логические проверки
 */
void logicalCheck() {

    def data = getData(formData)
    def dataPrev = getData(formPrev)

    for (DataRow row in data.getAllCached()) {
        if (row.getAlias() == null) {
            if (row.current == 0) {
                // LC Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.current) {
                    logger.warn("Графы 8 и 17 неравны!")
                }
                // LC • Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != row.costOnMarketQuotation || row.cost != row.reserveCalcValue || row.cost == 0) {
                    logger.warn("Графы 9, 14 и 15 ненулевые!")
                }
            }
            // LC • Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != row.recovery || row.recovery != 0)) {
                logger.error("Графы 8 и 17 ненулевые!")
            }
            // LC • Проверка необращающихся облигаций (графа 10 = «x»)
            if (getSign(row.signSecurity) == "x" && (row.reserveCalcValue != row.reserveCreation || row.reserveCreation != 0)) {
                logger.warn("Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (getSign(row.signSecurity) == "+") {
                // LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    logger.error("Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    logger.error("Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    logger.error("Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
            }
            // LC • Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                logger.error("Резерв сформирован неверно!")
            }
            // LC • Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current < 0 || row.cost || row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
                logger.warn("Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in getData(formPrev).getAllCached()) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn("РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6»  = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in getData(formPrev).getAllCached()) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        logger.error("РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8  = графа 15 формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }

            // LC Проверка на заполнение поля «<Наименование поля>»
            for (alias in ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
                    'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            //Проверка на уникальность поля «№ пп»
            if (getCurrency(row.currency) == 'RUR') {
                // LC Проверка графы 11
                if (row.marketQuotation != null) {
                    logger.error("Неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                }
                // LC Проверка графы 12
                if (row.rubCourse != null) {
                    logger.error("Неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                }
            }
            // LC Арифметическая проверка графы 13
            if (row.marketQuotation != null && row.rubCourse
                    && row.marketQuotationInRub != round((BigDecimal) (row.marketQuotation * row.rubCourse), 2)) {
                logger.error("Неверно рассчитана графа «Рыночная котировка одной ценной бумаги в рублях»!")
            }

            // @author ivildanov
            // Арифметические проверки граф 5, 8, 11, 12, 13, 14, 15, 16, 17
            List checks = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
            Map<String, Object> value = [:]
            value.put('reserveCalcValuePrev', calc8(row))
            value.put('marketQuotation', calc11(row))
            value.put('rubCourse', calc12(row))
            value.put('marketQuotationInRub', calc13(row))
            value.put('costOnMarketQuotation', calc14(row))
            value.put('reserveCalcValue', calc15(row))
            value.put('reserveCreation', calc16(row))
            value.put('recovery', calc17(row))

            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    logger.error("Неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '') + "!")
                }
            }

        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(data.getAllCached().indexOf(row))

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения по «<"+ getPrevRowWithoutAlias(row).regNumber+">» рассчитаны неверно!")
                    break
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(data.getAllCached().indexOf(row))

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения для «"+ getPrevRowWithoutAlias(row).issuer+"» рассчитаны неверно!")
                    break
                }
            }
        }

        // LC 22
        if (row.getAlias() != null && row.getAlias() == 'itogo') {
            srow = calcItogo()

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения рассчитаны неверно!")
                    break
                }
            }
        }

    }

    // LC • Проверка корректности заполнения РНУ
    if (formPrev != null) {
        DataRow itogoPrev = dataPrev.getDataRow(dataPrev.getAllCached(),'itogo')
        DataRow itogo = data.getDataRow(data.getAllCached(),'itogo')
        if (itogo != null && itogoPrev != null && itogo.prev != itogoPrev.current) {
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 6 = «Итого» по графе 7 формы РНУ-27 за предыдущий отчётный период")
        }
    }
    // LC • Проверка корректности заполнения РНУ
    if (formPrev != null) {
        DataRow itogoPrev = dataPrev.getDataRow(dataPrev.getAllCached(),'itogo')
        DataRow itogo = data.getDataRow(data.getAllCached(),'itogo')
        if (itogo != null && itogoPrev != null && itogo.reserveCalcValuePrev != itogoPrev.reserveCalcValue) {
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 8 = «Итого» по графе 15 формы РНУ-27 за предыдущий отчётный период")
        }
    }

    /** LC Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (formPrev != null) {
        List notFound = []
        List foundMany = []
        for (DataRow rowPrev in getData(formPrev).getAllCached()) {
            if (rowPrev.getAlias() != null && rowPrev.reserveCalcValue > 0) {
                int count = 0
                for (DataRow row in getData(formData).getAllCached()) {
                    if (row.getAlias() != null && row.tradeNumber == rowPrev.tradeNumber) {
                        count++
                    }
                }
                if (count == 0) {
                    notFound.add(rowPrev.tradeNumber)
                }
                if (count != 0 && count != 1) {
                    foundMany.add(rowPrev.tradeNumber)
                }
            }
        }
        if (!notFound.isEmpty()) {
            StringBuilder sb = new StringBuilder("Отсутствуют строки с номерами сделок :")
            for (tradeNumber in notFound) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
        if (!foundMany.isEmpty()) {
            StringBuilder sb = new StringBuilder("Отсутствуют строки с номерами сделок :")
            for (tradeNumber in foundMany) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
    }
}

/**
 * @author ivildanov
 * Ищем вверх по форме первую строку без альяса
 */
DataRow getPrevRowWithoutAlias(DataRow row) {
    int pos = getData(formData).getAllCached().indexOf(row)
    for (int i = pos; i >= 0; i++) {

        if ( getRow(i).getAlias() == null) {
            return row
        }
    }
    throw new IllegalArgumentException()
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

void checkNSI() {
    getRows(getData(formData)).each{ DataRow row ->
        if (row.getAlias() == null) {
            if (row.currency != null && getCurrency(row.currency) == null){
                logger.warn('Валюта выпуска облигации указана неверно!');
            }
            if (row.signSecurity!=null && getSign(row.signSecurity)==null){
                logger.warn('Признак ценной бумаги в справочнике отсутствует!');
            }
            if (row.currency != null && getCourse(row.currency, reportDate) == null){
                logger.warn('Неверный курс валют!');
            }
        }
    }
}

void allCheck() {
    logicalCheck()
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        return
    }

    def is = ImportInputStream
    if (is == null) {
        return
    }

    if (!fileName.contains('.r')) {
        logger.error("Некорректное расширение файла")
        return
    }

    logger.info('Начата загрузка файла ' + fileName)

    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }
    // номер строки с которой брать данные (что бы пропустить шапку таблицы или лишнюю информацию)
    def startRow = 0

    // номер графы с которой брать данные (что бы пропустить нумерацию или лишнюю информацию)
    def startColumn = 1

    def totalColumns = [6:'prev', 7:'current', 9:'cost', 14:'costOnMarketQuotation', 15:'reserveCalcValue']

    // добавить данные в форму
    boolean canCommit = true
    try{
        def total = addData(xml)
        if (total!=null) {
            calc()
            logicalCheck(false)
            checkNSI()
        } else {
            logger.error("Нет итоговой строки.")
            canCommit = false
        }
        logger.info('Закончена загрузка файла ' + fileName)
    } catch(Exception e) {
        canCommit = false
    }
    //в случае ошибок откатить изменения
    if (!canCommit) {
        logger.error("Загрузка файла $fileName завершилась ошибкой")
    }
}

// список столбцов, для которых нужно считать итоги
List getItogoColumns() {
    return ['prev', 'current', 'reserveCalcValuePrev', 'cost', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    def data = getData(formData)
    if (!logger.containsLevel(LogLevel.ERROR)) {

        for (int i = 0; i < data.getAllCached().size(); i++) {
            DataRow<Cell> row = data.getAllCached().get(i)
            DataRow<Cell> nextRow = getRow(i + 1)
            int j = 0

            if (row.getAlias() == null && nextRow == null || row.issuer != nextRow.issuer) {
                def itogIssuerRow = calcItogIssuer(i)
                data.insert(itogIssuerRow, i + 2)
                j++
            }

            if (row.getAlias() == null && nextRow == null || row.regNumber != nextRow.regNumber || row.issuer != nextRow.issuer) {
                def itogRegNumberRow = calcItogRegNumber(i)
                data.insert(itogRegNumberRow, i + 2)
                j++
            }
            i += j  // Обязательно чтобы избежать зацикливания в простановке
        }

        def rowItogo = calcItogo()
        data.insert(rowItogo,data.getAllCached().size()+1)
    }
}

/**
 * Расчет итога Эмитета
 * @author ivildanov
 */
def calcItogIssuer(int i) {
    def newRow = formData.createDataRow()
    newRow.getCell('issuer').colSpan = 2
    newRow.setAlias('itogoIssuer#'.concat(i.toString()))

    String tIssuer = 'Эмитет'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tIssuer = getRow(j).issuer
            break
        }
    }

    newRow.issuer = tIssuer.concat(' Итог')

    for (column in itogoColumns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    for (int j = i; j >= 0; j--) {

        srow = getRow(j)

        if (srow.getAlias() == null) {
            if (((getRow(j).issuer != tIssuer))) {
                break
            }

            for (column in itogoColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).value = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }

    }
    setTotalStyle(newRow)
    newRow
}

/**
 * Расчет итога ГРН
 * @author ivildanov
 */
def calcItogRegNumber(int i) {
    // создаем итоговую строку ГРН
    def newRow = formData.createDataRow()
    newRow.getCell('regNumber').colSpan = 2
    newRow.setAlias('itogoRegNumber#'.concat(i.toString()))

    String tRegNumber = 'ГРН'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tRegNumber = getRow(j).regNumber
            break
        }
    }

    newRow.regNumber = tRegNumber.concat(' Итог')

    for (column in itogoColumns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {

        srow = getRow(j)

        if (srow.getAlias() == null) {

            if (((getRow(j).regNumber != tRegNumber))) {
                break
            }

            for (column in itogoColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).value = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    setTotalStyle(newRow)
    newRow
}

/**
 * Расчет итоговой строки
 * @author ivildanov
 */
def calcItogo() {
    def data = getData(formData)
    // создаем строку
    def rowItogo = formData.createDataRow()
    rowItogo.setAlias('itogo')
    rowItogo.issuer = "Общий итог"

    // заполняем начальными данными-нулями
    for (column in itogoColumns) {
        rowItogo.getCell(column).value = new BigDecimal(0)
    }

    // ищем снизу вверх итоговую строку по эмитету
    for (int j = data.getAllCached().size() - 1; j >= 0; j--) {
        DataRow<Cell> srow = data.getAllCached().get(j)
        if ((srow.getAlias() != null) && (srow.getAlias().indexOf('itogoIssuer') != -1)) {
            for (column in itogoColumns) {
                if (srow.get(column) != null) {
                    rowItogo.getCell(column).value = rowItogo.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    setTotalStyle(rowItogo)
    rowItogo
}

/**
 * Получение строки по номеру
 * @author ivildanov
 */
DataRow<Cell> getRow(int i) {
    def data = getData(formData)
    if ((i < data.getAllCached().size()) && (i >= 0)) {
        return data.getAllCached().get(i)
    } else {
        return null
    }
}

/**
 * 3.1.1.1	Алгоритмы заполнения полей формы
 * Табл. 59 Алгоритмы заполнения полей формы «Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения»
 */

void calc() {
    def data = getData(formData)
    for (row in data.getAllCached()) {
        // Проверим чтобы человек рукамми ввёл всё что необходимо
        for (alias in ['issuer', 'regNumber', 'tradeNumber']) {
            if (row.getCell(alias).value == null) {
                setError(row.getCell(alias).column)
            }
        }
    }
    if (!logger.containsLevel(LogLevel.ERROR)) {
        BigDecimal i = 0
        formPrev
        for (DataRow row in data.getAllCached()) {
            i++
            row.number = i  // @todo http://jira.aplana.com/browse/SBRFACCTAX-2548 блокирует
            row.reserveCalcValuePrev = calc8(row)
            row.marketQuotation = calc11(row)
            row.rubCourse = calc12(row)
            row.marketQuotationInRub = calc13(row)
            row.costOnMarketQuotation = calc14(row)
            row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
        data.save(data.getAllCached());
    }
}

/**
 * Расчет графы 8
 */
BigDecimal calc8(DataRow row) {
    // Расчет графы 8 в соответсвие коментарию Аванесова http://jira.aplana.com/browse/SBRFACCTAX-2562
    temp = new BigDecimal(0)
    tempCount = 0
    if (formPrev != null) {
        for (DataRow rowPrev in getData(formPrev).getAllCached()) {
            if (row.tradeNumber == rowPrev.tradeNumber) {
                temp = rowPrev.reserveCalcValue
                tempCount++
            }
        }
    }
    if (tempCount == 1) {
        return round(temp, 2)
    } else {
        return (BigDecimal) 0
    }
}

/**
 * Расчет графы 11
 * @author ivildanov
 */
BigDecimal calc11(DataRow row) {
    if (getCurrency(row.currency) == 'RUR') {
        return null
    }
    return row.marketQuotation
}

/**
 * Расчет графы 12
 */
BigDecimal calc12(DataRow row) {
    return getCourse(row.currency,reportDate)
}

/**
 * Расчет графы 13
 * @author ivildanov
 */
BigDecimal calc13(DataRow row) {
    // FIXME http://jira.aplana.com/browse/SBRFACCTAX-2995
    if (row.marketQuotation != null && row.rubCourse != null) {
        return round((BigDecimal) (row.marketQuotation * row.rubCourse), 2)
    }
}

/**
 * Расчет графы 14
 * @author ivildanov
 */
BigDecimal calc14(DataRow row) {

    if (row.marketQuotationInRub == null) {
        return (BigDecimal) 0
    } else {
        return round((BigDecimal) (row.current * row.marketQuotationInRub), 2)
    }
}

/**
 * Расчет графы 15
 * @author ivildanov
 */
BigDecimal calc15(DataRow row) {

    BigDecimal a

    if (row.cost != null) {
        a = row.cost
    } else {
        a = 0
    }

    if (getSign(row.signSecurity) == "+") {
        if (a - row.costOnMarketQuotation > 0) {
            return a - row.costOnMarketQuotation
        } else {
            return (BigDecimal) 0
        }

    } else {
        return (BigDecimal) 0
    }
}

/**
 * Расчет графы 16
 * @author ivildanov
 */
BigDecimal calc16(DataRow row) {
    if (row.reserveCalcValue!=null && row.reserveCalcValuePrev!=null) {
        if (row.reserveCalcValue - row.reserveCalcValuePrev > 0) {
            return round((BigDecimal) (row.marketQuotation - row.prev), 2)
        } else {
            return (BigDecimal) 0
        }
    } else {
        return null;
    }
}

/**
 * Расчет графы 17
 * @author ivildanov
 */
BigDecimal calc17(DataRow row) {

    if (row.reserveCalcValue!=null && row.reserveCalcValuePrev!=null) {
        BigDecimal a
        if (row.reserveCalcValue - row.reserveCalcValuePrev < 0) {
            a = row.reserveCalcValue - row.reserveCalcValuePrev
        } else {
            a = 0
        }
        // abs
        if (a < 0) {
            a = -a
        }

        return round((BigDecimal) (a), 2)
    } else {
        return null;
    }
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1	Перечень полей формы
 */
void sort() {
    getData(formData).getAllCached().sort({ DataRow a, DataRow b ->
        if (a.issuer == b.issuer && a.regNumber == b.regNumber) {
            return a.tradeNumber <=> b.tradeNumber
        }
        if (a.issuer == b.issuer) {
            return a.regNumber <=> b.regNumber
        }
        return a.issuer <=> b.issuer
    })
}

/**
 * Удаляет строку из формы
 */
void deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        getData(formData).delete(currentDataRow)
    }
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    def data = getData(formData)
    while(getStaticRow()!=null){
        data.delete(getStaticRow())
    }
}

def getStaticRow(){
    Iterator<DataRow> iterator = getData(formData).getAllCached().iterator() as Iterator<DataRow>
    while (iterator.hasNext()) {
        row = (DataRow) iterator.next()
        if (row.getAlias() != null) {
            return row;
        }
    }
    return null
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal round(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

void setError(Column c) {

    if (!c.name.empty) {
        logger.error('Поле ' + c.name.replace('%', '') + ' не заполнено')
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRowwarnrmData() {
    def data = getData(formData)
    DataRow<Cell> newRow = getNewRow()
    int index // Здесь будет позиция вставки

    if (data.getAllCached().size() > 0) {
        DataRow<Cell> selectRow
        // Форма не пустая
        if (currentDataRow != null && data.getAllCached().indexOf(currentDataRow) != -1) {
            // Значит выбрал строку куда добавлять
            selectRow = currentDataRow
        } else {
            // Строку не выбрал поэтому добавляем в самый конец
            selectRow = data.getAllCached().get(data.getAllCached().size() - 1) // Вставим в конец

            // добавил вставку тут, потому что по старому добавляет не в конец формы а перед последним элементом
            insert(data, newRow)
            return
        }

        int indexSelected = data.getAllCached().indexOf(selectRow)
        // Определим индекс для выбранного места
        if (selectRow.getAlias() == null) {
            // Выбрана строка не итого
            index = indexSelected // Поставим на то место новую строку
        } else {
            // Выбрана строка итого, для статических строг итого тут проще и надо фиксить под свою форму
            // Для динимаческих строк итого идём вверх пока не встретим конец формы или строку не итого

            for (index = indexSelected; index >= 0; index--) {
                if (data.getAllCached().get(index).getAlias() == null) {
                    index++
                    break
                }
            }
            if (index < 0) {
                // Значит выше строки итого нет строк, добавим новую в начало
                index = 0
            }
        }
    } else {
        // Форма пустая поэтому поставим строку в начало
        index = 0
    }

    data.insert(newRow, index+1)
}

FormData getFormPrev() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getData(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,data.getAllCached().size()+1)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}
/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
boolean addData(def xml, def startRow, def startColumn) {
    if (xml == null) {
        return
    }

    Date date = reportDate

    def cache = [:]
    def data = getData(formData)

    def total = formData.createDataRow()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= startRow) {// || indexRow>20) {
            continue
        }

        def newRow = getNewRow()

        def indexCell = startColumn

        newRow.rowNumber = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 2
        newRow.issuer = row.cell[indexCell].text()
        indexCell++

        // графа 3
        newRow.regNumber = row.cell[indexCell].text()
        indexCell++

        // графа 4
        newRow.tradeNumber = row.cell[indexCell].text()
        indexCell++

        // графа 5
        newRow.currency = getRecords(15, 'CODE_2', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 6
        newRow.prev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        newRow.current = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 8
        newRow.reserveCalcValuePrev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 9
        newRow.cost = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 10
        newRow.signSecurity = getRecords(62, 'CODE', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 11
        newRow.marketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 12
        newRow.rubCourse = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 13
        newRow.marketQuotationInRub = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 14
        newRow.costOnMarketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 15
        newRow.reserveCalcValue = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 16
        newRow.reserveCreation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 17
        newRow.recovery = getNumber(row.cell[indexCell].text())

        insert(data, newRow)
    }

    if (xml.rowTotal.size()==1)
        for (def row : xml.rowTotal) {
            // графа 6
            total.prev = getNumber(row.cell[6].text())

            // графа 7
            total.current = getNumber(row.cell[7].text())

            // графа 8

            // графа 9
            total.cost = getNumber(row.cell[9].text())

            // графа 10

            // графа 11

            // графа 12

            // графа 13

            // графа 14
            total.costOnMarketQuotation = getNumber(row.cell[14].text())

            // графа 15
            total.reserveCalcValue = getNumber(row.cell[15].text())
        }
    else {
        return null
    }

    data.commit()
    return total
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}

def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + "= '"+ value.replaceAll(' ', '')+"'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter]!=null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось определить элемент справочника!")
    return null;
}


/**
 * Получить буквенный код валюты
 */
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE_2')
}

/**
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return  refBookService.getStringValue(62,sign,'CODE')
}

/**
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    def cached = data.getAllCached()
    return cached
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE_2')=='810'
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return (res.getRecords().isEmpty() ? null : res.getRecords().get(0).RATE.getNumberValue())
    } else if (isRubleCurrency(currency)){
        return 1;
    } else {
        return null
    }
}

def getNewRow() {
    def row = formData.createDataRow()
    ['currency', 'issuer', 'regNumber', 'tradeNumber', 'prev', 'current', 'reserveCalcValuePrev',
            'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'recovery'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}