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
        def formPrev = getFormPrev()
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE:
        def formPrev = getFormPrev()
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        recalculateNumbers()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        recalculateNumbers()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        def formPrev = getFormPrev()
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        logicalCheck() && checkNSI()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        def formPrev = getFormPrev()
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
        if (!hasError() && logicalCheck() && checkNSI()) {
            // для сохранения изменений приемников
            getData(formData).commit()
        }
        break
    case FormDataEvent.IMPORT :
        def formPrev = getFormPrev()
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        importData()
        if (!hasError()) {
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            !hasError() && logicalCheck() && checkNSI()
        }
        break
    case FormDataEvent.MIGRATION :
        def formPrev = getFormPrev()
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (!isBalancePeriod && (formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
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
def logicalCheck() {
    def data = getData(formData)

    // LC Проверка на заполнение поля «<Наименование поля>»
    for (row in data.getAllCached()) {
        def requiredColumns = ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
                'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
        if(!checkRequiredColumns(row,requiredColumns)){
            return false
        }
    }
    if (hasError()) {
        return
    }

    // Проверока наличия итоговой строки
    if (!checkAlias(getRows(data), 'itogo')) {
        logger.error('Итоговые значения не рассчитаны')
        return false
    }

    def formPrev = getFormPrev()
    def dataPrev = getData(formPrev)

    for (DataRow row in data.getAllCached()) {
        if (row.getAlias() == null) {

            def index = row.number
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            if (row.current == 0) {
                // 2. LC Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.current) {
                    logger.warn(errorMsg + "графы 8 и 17 неравны!")
                }
                // 3. LC • Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != row.costOnMarketQuotation || row.cost != row.reserveCalcValue || row.cost == 0) {
                    logger.warn(errorMsg + "графы 9, 14 и 15 ненулевые!")
                }
            }
            // 4. LC • Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != row.recovery || row.recovery != 0)) {
                logger.error(errorMsg + "графы 8 и 17 ненулевые!")
                return false
            }
            // 5. LC • Проверка необращающихся облигаций (графа 10 = «x»)
            if (getSign(row.signSecurity) == "x" && (row.reserveCalcValue != row.reserveCreation || row.reserveCreation != 0)) {
                logger.warn(errorMsg + "облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (getSign(row.signSecurity) == "+") {
                // 6. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    logger.error(errorMsg + "облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                    return false
                }
                // LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    logger.error(errorMsg + "облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                    return false
                }
                // LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    logger.error(errorMsg + "облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                    return false
                }
            }
            // 9. LC • Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                logger.error(errorMsg + "резерв сформирован неверно!")
                return false
            }
            // LC • Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current < 0 || row.cost < 0 || row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
                logger.warn(errorMsg + "резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in dataPrev.getAllCached()) {
                    if (!isFixedRow(rowPrev) && row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6»  = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in dataPrev.getAllCached()) {
                    if (!isFixedRow(rowPrev) && row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        logger.error(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8  = графа 15 формы РНУ-27 за предыдущий отчётный период")
                        return false
                    }
                }
            }

            //Проверка на уникальность поля «№ пп»
            if (getCurrency(row.currency) == 'RUR') {
                // LC Проверка графы 11
                if (row.marketQuotation != null) {
                    logger.error(errorMsg + "неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                    return false
                }
                // LC Проверка графы 12
                if (row.rubCourse != null) {
                    logger.error(errorMsg + "неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                    return false
                }
            }
            // LC Арифметическая проверка графы 13
            if (row.marketQuotation != null && row.rubCourse
                    && row.marketQuotationInRub != roundValue((BigDecimal) (row.marketQuotation * row.rubCourse), 2)) {
                logger.error(errorMsg + "неверно рассчитана графа «Рыночная котировка одной ценной бумаги в рублях»!")
                return false
            }

            // @author ivildanov
            // Арифметические проверки граф 5, 8, 11, 12, 13, 14, 15, 16, 17
            List checks = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
            def value = formData.createDataRow()
            value.reserveCalcValuePrev = calc8(row, formPrev)
            value.marketQuotation = calc11(row)
            value.rubCourse = calc12(row)
            value.marketQuotationInRub = calc13(row)
            value.costOnMarketQuotation = calc14(row)
            value.reserveCalcValue = calc15(row)
            value.reserveCreation = calc16(row)
            value.recovery = calc17(row)

            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    logger.error(errorMsg + "неверно рассчитана графа \"${row.getCell(check).column.name.replace('%', '')}\"! (${row.getCell(check).value} != ${value.get(check)})")
                    return false
                }
            }

        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(data.getAllCached().indexOf(row))

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения по «<"+ getPrevRowWithoutAlias(row).regNumber+">» рассчитаны неверно!")
                    return false
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(data.getAllCached().indexOf(row))

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения для «"+ getPrevRowWithoutAlias(row).issuer+"» рассчитаны неверно!")
                    return false
                }
            }
        }

        // LC 22
        if (row.getAlias() != null && row.getAlias() == 'itogo') {
            srow = calcItogo()

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения рассчитаны неверно!")
                    return false
                }
            }
        }
    }

    // LC • Проверка корректности заполнения РНУ
    if (dataPrev != null && checkAlias(getRows(dataPrev), 'itogo') && checkAlias(getRows(data), 'itogo')) {
        DataRow itogoPrev = getRowByAlias(dataPrev,'itogo')
        DataRow itogo = getRowByAlias(data,'itogo')
        if (itogo != null && itogoPrev != null && itogo.prev != itogoPrev.current) {
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 6 = «Итого» по графе 7 формы РНУ-27 за предыдущий отчётный период")
            return false
        }
        if (itogo != null && itogoPrev != null && itogo.reserveCalcValuePrev != itogoPrev.reserveCalcValue) {
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 8 = «Итого» по графе 15 формы РНУ-27 за предыдущий отчётный период")
            return false
        }
    }

    /** 1. LC Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (dataPrev != null) {
        List notFound = []
        List foundMany = []
        for (DataRow rowPrev in getRows(dataPrev)) {
            if (rowPrev.getAlias() == null && rowPrev.reserveCalcValue > 0) {
                int count = 0
                for (DataRow row in getData(formData).getAllCached()) {
                    if (row.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber) {
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
    return true
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getRows(getData(formData)).indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
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

def checkNSI() {
    getRows(getData(formData)).each{ DataRow row ->
        if (row.getAlias() == null) {
            def index = row.number
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }


            if (row.currency != null && getCurrency(row.currency) == null){
                logger.warn(errorMsg + 'валюта выпуска облигации указана неверно!');
            }
            if (row.signSecurity!=null && getSign(row.signSecurity)==null){
                logger.warn(errorMsg + 'признак ценной бумаги в справочнике отсутствует!');
            }
            if (row.currency != null && getCourse(row.currency, reportDate) == null){
                logger.warn(errorMsg + 'неверный курс валют!');
            }
        }
    }
    return true
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
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.contains('.r')) {
        logger.error('Формат файла должен быть *.r??')
        return
    }

    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml)

        // расчетать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }

    if (!hasError()) {
        logger.info('Закончена загрузка файла ' + fileName)
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
    if (!hasError()) {

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
        def requiredColumns = ['issuer', 'regNumber', 'tradeNumber', 'currency']
        if(!checkRequiredColumns(row,requiredColumns)){
            return
        }
    }
    if (!hasError()) {
        def formPrev = getFormPrev()
        BigDecimal i = 0
        for (DataRow row in data.getAllCached()) {
            i++
            row.number = i  // @todo http://jira.aplana.com/browse/SBRFACCTAX-2548 блокирует
            row.reserveCalcValuePrev = calc8(row, formPrev)
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
BigDecimal calc8(DataRow row, def formPrev) {
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
        return roundValue(temp, 2)
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
    if (getCurrency(row.currency) == 'RUR') {
        return null
    }
    return row.rubCourse
    //return getCourse(row.currency,reportDate)
}

/**
 * Расчет графы 13
 * @author ivildanov
 */
BigDecimal calc13(DataRow row) {
    // FIXME http://jira.aplana.com/browse/SBRFACCTAX-2995
    if (row.marketQuotation != null && row.rubCourse != null) {
        return roundValue((BigDecimal) (row.marketQuotation * row.rubCourse), 2)
    } else {
        return null
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
        return roundValue((BigDecimal) (row.current * row.marketQuotationInRub), 2)
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
            return roundValue((BigDecimal) (row.reserveCalcValue - row.reserveCalcValuePrev), 2)
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

        return roundValue((BigDecimal) (a), 2)
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
BigDecimal roundValue(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRow() {
    def data = getData(formData)
    DataRow<Cell> newRow = getNewRow()
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

def recalculateNumbers(){
    def index = 1
    def data = getData(formData)
    getRows(data).each{row->
        if(row.getAlias()==null){
            row.number = index++
        }
    }
    data.save(getRows(data))
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
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = getData(formData)
    data.clear()

    for (def row : xml.row) {
        def newRow = getNewRow()

        def indexCell = 0

        newRow.number = getNumber(row.cell[indexCell].text())
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

    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 6
        total.prev = getNumber(row.cell[5].text())

        // графа 7
        total.current = getNumber(row.cell[6].text())

        // графа 8
        total.reserveCalcValuePrev = getNumber(row.cell[7].text())

        // графа 9
        total.cost = getNumber(row.cell[8].text())

        // графа 14
        total.costOnMarketQuotation = getNumber(row.cell[13].text())

        // графа 15
        total.reserveCalcValue = getNumber(row.cell[14].text())

        // графа 16
        total.reserveCreation = getNumber(row.cell[15].text())

        // графа 17
        total.recovery = getNumber(row.cell[16].text())

        return total
    } else {
        return null
    }
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

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + " like '"+ value.replaceAll(' ', '')+ "%'"
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
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
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
    return  refBookService.getStringValue(15,currencyCode,'CODE')=='810'
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return (res.getRecords().isEmpty() ? null : res.getRecords().get(0).RATE.getNumberValue())
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

/**
 * Проверить существования строки по алиасу.
 *
 * @param list строки нф
 * @param rowAlias алиас
 * @return <b>true</b> - строка с указанным алиасом есть, иначе <b>false</b>
 */
def checkAlias(def list, def rowAlias) {
    if (rowAlias == null || rowAlias == "" || list == null || list.isEmpty()) {
        return false
    }
    for (def row : list) {
        if (row.getAlias() == rowAlias) {
            return true
        }
    }
    return false
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias() == 'itogo'
}

/**
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('itogo')
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def data = getData(formData)
    def totalColumns = [6:'prev', 7:'current', 8: 'reserveCalcValuePrev', 9:'cost', 14:'costOnMarketQuotation', 15:'reserveCalcValue',
            16 : 'reserveCreation', 17 : 'recovery']
    def totalCalc
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    // графы для которых надо вычислять итого и итого по ГРН (графа 4..7, 10..13)
    def totalColumns = getItogoColumns()
    def totalRow = formData.createDataRow()

    totalRow.setAlias('itogo')
    totalRow.regNumber = 'Общий итог'
    setTotalStyle(totalRow)
    def data = getData(formData)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias))
    }
    return totalRow
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias) {
    def from = 0
    def to = getRows(data).size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}
