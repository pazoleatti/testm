package form_template.income.rnu27

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.12 (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK:
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (formPrev == null || formPrev.state != WorkflowState.ACCEPTED) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        allCheck()
        break
    case FormDataEvent.CALCULATE:
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (formPrev == null || formPrev.state != WorkflowState.ACCEPTED) {
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
        if (formPrev == null || formPrev.state != WorkflowState.ACCEPTED) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        formPrev
        // Проверка: Форма РНУ-27 предыдущего отчетного периода существует и находится в статусе «Принята»
        if (formPrev == null || formPrev.state != WorkflowState.ACCEPTED) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
}

// графа 1  - число  number № пп
// графа 2  - строка issuer эмитит
// графа 3  - строка regNumber гос номер
// графа 4  - строка tradeNumber Номер сделки
// графа 5  - строка currency Валюта выпуска облигации
// графа 6  - число  prev Размер лота на отчётную дату по депозитарному учёту (шт.). Предыдущую
// графа 7  - число  current Размер лота на отчётную дату по депозитарному учёту (шт.). Текущую
// графа 8  - число  reserveCalcValuePrev Расчётная величина резерва на предыдущую отчётную дату (руб.коп.)
// графа 9  - число  cost Стоимость по цене приобретения (руб.коп.)
// графа 10 - строка signSecurity Признак ценной бумаги на текущую отчётную дату
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

    for (DataRow row in formData.dataRows) {
        if (row.getAlias() == null) {
            if (row.currency == 0) {
                // LC Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.currency) {
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
            if (row.signSecurity == "x" && (row.reserveCalcValue != row.reserveCreation || row.reserveCreation != 0)) {
                logger.warn("Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (row.signSecurity == "+") {
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
                for (DataRow rowPrev in formPrev.dataRows) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn("РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6»  = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in formPrev.dataRows) {
                    if (row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        logger.error("РНУ сформирован некорректно! Не выполняется условие: Если  «графа  4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8  = графа 15 формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }

            // LC Проверка на заполнение поля «<Наименование поля>»
            // @FIXME ниже production версия кода, сейчаз часть проверок отключим чтобы тестировать
//            for (alias in ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
//                    'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation']) {
//                if (row.getCell(alias).value == null) {
//                    setError(row.getCell(alias).column)
//                }
//            }
            for (alias in ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
                    'reserveCalcValue', 'reserveCreation']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            //Проверка на уникальность поля «№ пп»
            if (row.currency == 'RUR') {
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
            List checks = ['currency', 'reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
            Map<String, Object> value = [:]
            value.put('currency', calc5(row))
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
                    log("calc = " + value.get(check).toString())
                    log("row = " + row.getCell(check).value.toString())
                    logger.error("Неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '') + "!")
                }
            }

        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(formData.dataRows.indexOf(row))

            for (column in itogoColumns) {
                if (row.get(column) != srow.get(column)) {
                    logger.error("Итоговые значения по «<"+ getPrevRowWithoutAlias(row).regNumber+">» рассчитаны неверно!")
                    break
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(formData.dataRows.indexOf(row))

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
        DataRow itogoPrev = formPrev.getDataRow('itogo')
        DataRow itogo = formData.getDataRow('itogo')
        if (itogo != null && itogoPrev != null && itogo.prev != itogoPrev.current) {
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 6 = «Итого» по графе 7 формы РНУ-27 за предыдущий отчётный период")
        }
    }
    // LC • Проверка корректности заполнения РНУ
    if (formPrev != null) {
        DataRow itogoPrev = formPrev.getDataRow('itogo')
        DataRow itogo = formData.getDataRow('itogo')
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
        for (DataRow rowPrev in formPrev.dataRows) {
            if (rowPrev.getAlias() != null && rowPrev.reserveCalcValue > 0) {
                int count = 0
                for (DataRow row in formData.dataRows) {
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
    int pos = formData.dataRows.indexOf(row)
    for (int i = pos; i >= 0; i++) {

        if ( getRow(i).getAlias() == null) {
            return row
        }
    }
    throw new IllegalArgumentException()
}

// todo Проверки НСИ: нет справочников
void checkNSI() {

}

void allCheck() {
    logicalCheck()
}

// список столбцов, для которых нужно считать итоги
List getItogoColumns() {
    return ['prev', 'current', 'reserveCalcValuePrev', 'cost', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        for (int i = 0; i < formData.dataRows.size(); i++) {
            DataRow<Cell> row = formData.dataRows.get(i)
            DataRow<Cell> nextRow = getRow(i + 1)
            int j = 0

            if (row.getAlias() == null && nextRow == null || row.issuer != nextRow.issuer) {
                def itogIssuerRow = calcItogIssuer(i)
                formData.dataRows.add(i + 1, itogIssuerRow)
                j++
            }

            if (row.getAlias() == null && nextRow == null || row.regNumber != nextRow.regNumber || row.issuer != nextRow.issuer) {
                def itogRegNumberRow = calcItogRegNumber(i)
                formData.dataRows.add(i + 1, itogRegNumberRow)
                j++
            }
            i += j  // Обязательно чтобы избежать зацикливания в простановке
        }

        def rowItogo = calcItogo()
        formData.dataRows.add(rowItogo)
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
    newRow
}

/**
 * Расчет итоговой строки
 * @author ivildanov
 */
def calcItogo() {
    // создаем строку
    def rowItogo = formData.createDataRow()
    rowItogo.setAlias('itogo')
    rowItogo.issuer = "Общий итог"

    // заполняем начальными данными-нулями
    for (column in itogoColumns) {
        rowItogo.getCell(column).value = new BigDecimal(0)
    }

    // ищем снизу вверх итоговую строку по эмитету
    for (int j = formData.dataRows.size() - 1; j >= 0; j--) {
        DataRow<Cell> srow = formData.dataRows.get(j)
        if ((srow.getAlias() != null) && (srow.getAlias().indexOf('itogoIssuer') != -1)) {
            for (column in itogoColumns) {
                if (srow.get(column) != null) {
                    rowItogo.getCell(column).value = rowItogo.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }

    rowItogo
}

/**
 * Получение строки по номеру
 * @author ivildanov
 */
DataRow<Cell> getRow(int i) {
    if ((i < formData.dataRows.size()) && (i >= 0)) {
        return formData.dataRows.get(i)
    } else {
        return null
    }
}

/**
 * 3.1.1.1	Алгоритмы заполнения полей формы
 * Табл. 59 Алгоритмы заполнения полей формы «Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения»
 */

void calc() {
    for (row in formData.dataRows) {
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
        for (DataRow row in formData.dataRows) {
            i++
            row.number = i  // @todo http://jira.aplana.com/browse/SBRFACCTAX-2548 блокирует
            row.currency = calc5(row)
            row.reserveCalcValuePrev = calc8(row)
            row.marketQuotation = calc11(row)
            row.rubCourse = calc12(row)
            row.marketQuotationInRub = calc13(row)
            row.costOnMarketQuotation = calc14(row)
            row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
    }
}

/**
 * Расчет графы 5
 */
@SuppressWarnings("GroovyUnusedDeclaration")
String calc5(DataRow row) {
    return 'RUR'    // @todo  Расчёт графы 5 после http://jira.aplana.com/browse/SBRFACCTAX-2376 сейчаз проставим принудительно
}

/**
 * Расчет графы 8
 */
BigDecimal calc8(DataRow row) {
    // Расчет графы 8 в соответсвие коментарию Аванесова http://jira.aplana.com/browse/SBRFACCTAX-2562
    temp = new BigDecimal(0)
    tempCount = 0
    if (formPrev != null) {
        for (DataRow rowPrev in formPrev.dataRows) {
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
def calc11(DataRow row) {
    if (row.currency == 'RUR') {
        return null
    }
}

/**
 * Расчет графы 12
 * @author ivildanov
 */
def calc12(DataRow row) {
    if (row.currency == 'RUR') {
        return null
    }
}

/**
 * Расчет графы 13
 * @author ivildanov
 */
def calc13(DataRow row) {
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

    if (row.signSecurity == "+") {
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
    if (row.reserveCalcValue - row.reserveCalcValuePrev > 0) {
        return round((BigDecimal) (row.marketQuotation - row.prev), 2)
    } else {
        return (BigDecimal) 0
    }
}

/**
 * Расчет графы 17
 * @author ivildanov
 */
BigDecimal calc17(DataRow row) {

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
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1	Перечень полей формы
 */
void sort() {
    formData.dataRows.sort({ DataRow a, DataRow b ->
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
        formData.dataRows.remove(currentDataRow)
    }
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    Iterator<DataRow> iterator = formData.dataRows.iterator() as Iterator<DataRow>
    while (iterator.hasNext()) {
        row = (DataRow) iterator.next()
        if (row.getAlias() != null) {
            iterator.remove()
        }
    }
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
    DataRow<Cell> newRow = formData.createDataRow()
    int index // Здесь будет позиция вставки

    if (formData.dataRows.size() > 0) {
        DataRow<Cell> selectRow
        // Форма не пустая
        log("Форма не пустая")
        log("size = " + formData.dataRows.size())
        if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
            // Значит выбрал строку куда добавлять
            log("Строка вставки выбрана")
            log("indexOf = " + formData.dataRows.indexOf(currentDataRow))
            selectRow = currentDataRow
        } else {
            // Строку не выбрал поэтому добавляем в самый конец
            log("Строка вставки не выбрана, поставим в конец формы")
            selectRow = formData.dataRows.get(formData.dataRows.size() - 1) // Вставим в конец
        }

        int indexSelected = formData.dataRows.indexOf(selectRow)
        log("indexSelected = " + indexSelected.toString())

        // Определим индекс для выбранного места
        if (selectRow.getAlias() == null) {
            // Выбрана строка не итого
            log("Выбрана строка не итого")
            index = indexSelected // Поставим на то место новую строку
        } else {
            // Выбрана строка итого, для статических строг итого тут проще и надо фиксить под свою форму
            // Для динимаческих строк итого идём вверх пока не встретим конец формы или строку не итого
            log("Выбрана строка итого")

            for (index = indexSelected; index >= 0; index--) {
                log("loop index = " + index.toString())
                if (formData.dataRows.get(index).getAlias() == null) {
                    log("Нашел строку отличную от итого")
                    index++
                    break
                }
            }
            if (index < 0) {
                // Значит выше строки итого нет строк, добавим новую в начало
                log("выше строки итого нет строк")
                index = 0
            }
            log("result index = " + index.toString())
        }
    } else {
        // Форма пустая поэтому поставим строку в начало
        log("Форма пустая поэтому поставим строку в начало")
        index = 0
    }
    formData.dataRows.add(index, newRow)
    [
            'issuer', 'regNumber', 'tradeNumber', 'prev', 'current', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

FormData getFormPrev() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = FormDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}