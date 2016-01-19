package form_template.deal.app_6_15.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 6.15. Поставочные срочные сделки с драгоценными металлами
 *
 * formTemplateId = 837
 *
 * @author - Emamedova
 */
// графа    - fix
// графа 1  - rowNumber         - № п/п
// графа 2  - name              - Полное наименование юридического лица с указанием ОПФ
// графа 3	- dependence	    - Признак взаимозависимости
// графа 4  - iksr              - ИНН/ КИО
// графа 5  - countryName       - Наименование страны регистрации
// графа 6  - countryCode	    - Код страны регистрации по классификатору ОКСМ
// графа 7  - docNumber         - Номер договора
// графа 8  - docDate           - Дата договора
// графа 9  - dealNumber        - Номер сделки
// графа 10 - dealType          - Вид срочной сделки
// графа 11 - dealDate          - Дата заключения сделки
// графа 12 - innerCode			- Внутренний код
// графа 13 - dealCountryCode	- Код страны происхождения предмета сделки
// графа 14 - signPhis			- Признак физической поставки драгоценного металла
// графа 15 - signTransaction	- Признак внешнеторговой сделки
// графа 16 - countryCode2		- Код страны по классификатору ОКСМ
// графа 17 - region1			- Регион (код)
// графа 18 - city1				- Город
// графа 19 - settlement1		- Населенный пункт
// графа 20 - countryCode3		- Код страны по классификатору ОКСМ
// графа 21 - region2			- Регион (код)
// графа 22 - city2				- Город
// графа 23 - settlement2		- Населенный пункт
// графа 24 - conditionCode 	- Код условия поставки
// графа 25 - count				- Количество
// графа 26 - income            - Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 27 - outcome           - Сумма расходов Банка по данным бухгалтерского учета, руб.
// графа 28 - price             - Цена
// графа 29 - cost              - Стоимость
// графа 30 - dealDoneDate      - Дата совершения сделки

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}
//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['fix', 'rowNumber', 'name', 'dependence', 'iksr', 'countryName', 'countryCode', 'docNumber', 'docDate', 'dealNumber',
                  'dealType', 'dealDate', 'innerCode', 'dealCountryCode', 'signPhis', 'signTransaction', 'countryCode2', 'region1', 'city1', 'settlement1',
                  'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode', 'count', 'income', 'outcome', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealType', 'dealDate', 'innerCode', 'dealCountryCode', 'signPhis',
                       'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode',
                       'income', 'outcome', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'dependence', 'iksr', 'countryName', 'countryCode', 'signTransaction', 'count', 'price', 'cost']

//Непустые атрибуты
@Field
def nonEmptyColumns = ['name', 'dependence', 'docNumber', 'docDate', 'dealNumber', 'dealType', 'dealDate', 'innerCode', 'dealCountryCode', 'signPhis',
                       'signTransaction', 'count', 'price', 'cost', 'dealDoneDate']

// Группируемые атрибуты (графа 2, 7, 8, 14, 15)
@Field
def groupColumns = ['name', 'docNumber', 'docDate', 'signPhis', 'signTransaction']

@Field
def totalColumns = ['count', 'income', 'outcome', 'cost']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка признака взаимозависимости
        // всегда "Нет"

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка корректности даты заключения сделки
        if (row.docDate && row.dealDate && (row.docDate > row.dealDate || row.dealDate > getReportPeriodEndDate())) {
            def msg1 = row.getCell('dealDate').column.name
            def msg2 = row.getCell('docDate').column.name
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }

        // графа 14 = элемент с кодом «1» ("ОМС")
        def isOMS = (getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 1)
        // графа 14 = элемент с кодом «2» ("Физическая поставка")
        def isPhysics = (getRefBookValue(18, row.signPhis)?.CODE?.numberValue == 2)

        // Проверка признака физической поставки
        if (row.signPhis && !isOMS && !isPhysics) {
            def msg = row.getCell('signPhis').column.name
            logger.error("Строка $rowNum: Графа «$msg» может содержать только одно из значений: ОМС, Физическая поставка!")
        }

        // Проверка признака внешнеторговой сделки
        if (row.signTransaction && row.countryCode2 && row.countryCode3) {
            if (row.countryCode2 != row.countryCode3 && row.signTransaction != recYesId || row.countryCode2 == row.countryCode3 && row.signTransaction != recNoId) {
                def msg = row.getCell('signTransaction').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» не соответствует сведениям о стране отправки и стране доставки драгоценных металлов!")
            }
        }

        // Проверка зависимости от признака физической поставки
        if (row.signPhis && isOMS) {
            // графы 16 – 24 должны быть не заполнены
            def checkField = ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2', 'settlement2', 'conditionCode']
            for (it in checkField) {
                if (row.getCell(it).value) {
                    def msg = row.getCell('signPhis').column.name
                    logger.error("Строка $rowNum: Графы 12.1-12.4, 13.1-13.4, 14 не должны быть заполнены, т.к. в графе «$msg» указано значение «ОМС»!")
                    break
                }
            }
        } else if (row.signPhis && isPhysics) {
            // i. Графы 16, 20 должны быть заполнены
            if (row.countryCode2 == null || row.countryCode3 == null) {
                def msg1 = row.getCell('signPhis').column.name
                def msg2 = row.getCell('countryCode2').column.name
                def msg3 = row.getCell('countryCode3').column.name
                logger.error("Строка $rowNum: Графы «$msg2», «$msg3» должны быть заполнены, т.к. в графе «$msg1» указано значение «Физическая поставка»!")
            }

            // ii. Если графа 16 заполнена элементом с кодом 643, то графа 17 должна быть заполнена
            // iii. Если графа 16 заполнена элементом с кодом, отличным от 643, то графа 17 должна быть не заполнена
            def country = getRefBookValue(10, row.countryCode2)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region1').column.name
                if (country == '643' && row.region1 == null) {
                    logger.error("Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. указанная страна отправки Россия!")
                } else if (country != '643' && row.region1 != null) {
                    logger.error("Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к указанная страна отправки не Россия!")
                }
            }

            // iv. Если графа 20 заполнена элементом с кодом 643, то графа 21 должна быть заполнена
            // v. Если графа 20 заполнена элементом с кодом, отличным от 643, то графа 21 должна быть не заполнена
            country = getRefBookValue(10, row.countryCode3)?.CODE?.stringValue
            if (country != null) {
                def regionName = row.getCell('region2').column.name
                if (country == '643' && row.region2 == null) {
                    logger.error("Строка $rowNum: Графа «$regionName» должна быть заполнена, т.к. указанная страна доставки Россия!")
                } else if (country != '643' && row.region2 != null) {
                    logger.error("Строка $rowNum: Графа «$regionName» не должна быть заполнена, т.к. указанная страна доставки не Россия!")
                }
            }

            // vi. Должна быть заполнена графа 18 или 19
            if (row.city1 == null && row.settlement1 == null) {
                def msg1 = row.getCell('city1').column.name
                def msg2 = row.getCell('settlement1').column.name
                logger.error("Строка $rowNum: Должна быть заполнена одна из граф «$msg1» или «$msg2»!")
            }

            // vii. Должна быть заполнена графа 22 или 23
            if (row.city2 == null && row.settlement2 == null) {
                msg1 = row.getCell('city2').column.name
                msg2 = row.getCell('settlement2').column.name
                logger.error("Строка $rowNum: Должна быть заполнена одна из граф «$msg1» или «$msg2»!")
            }
        }

        // Проверка количества
        if (row.count != null && row.count != 1) {
            msg = row.getCell('count').column.name
            logger.error("Строка $rowNum: Графа «$msg» должна быть заполнена значением «1»!")
        }

        // Проверка на заполнение доходов и расходов
        boolean noOne = (row.income == null && row.outcome == null)
        boolean both = (row.income != null && row.outcome != null)
        if (noOne) {
            String msg1 = getColumnName(row, 'outcome')
            String msg2 = getColumnName(row, 'income')
            logger.error("Строка $rowNum: Должна быть заполнена хотя бы одна из граф «$msg1», «$msg2»!")
        }

        // Проверка положительной суммы дохода/расхода
        if (!noOne && !both) {
            sum = (row.income != null) ? row.income : row.outcome
            if (sum < 0) {
                msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }
        if (both) {
            if (row.income < 0 || row.outcome < 0 || (row.outcome <= 0 && row.outcome <= 0)) {
                String msg1 = getColumnName(row, 'outcome')
                String msg2 = getColumnName(row, 'income')
                logger.error("Строка $rowNum: Значение обеих граф «$msg1», «$msg2» должно быть неотрицательным, значение хотя бы одной из данных граф должно быть строго больше «0»!")
            }
        }

        // Проверка цены и стоимости
        if (!noOne && !both) {
            boolean comparePrice = row.price && row.price != sum
            boolean compareCost = row.cost && row.cost != sum
            msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
            String msg1 = getColumnName(row, 'price')
            String msg2 = getColumnName(row, 'cost')
            if (comparePrice) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg»!")
            }
            if (compareCost) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно значению графы «$msg»!")
            }
        }
        if (both) {
            absSum = (row.income - row.outcome).abs()
            boolean comparePrice = row.price && row.price != absSum
            boolean compareCost = row.cost && row.cost != absSum
            String msg1 = getColumnName(row, 'price')
            String msg2 = getColumnName(row, 'cost')
            String msg3 = getColumnName(row, 'income')
            String msg4 = getColumnName(row, 'outcome')
            if (comparePrice) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно модулю разности значений граф «$msg3» и «$msg4»!")
            }
            if (compareCost) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно модулю разности значений граф «$msg3» и «$msg4»!")
            }
        }

        // Проверка корректности даты совершения сделки
        if (row.dealDate && row.dealDoneDate && (row.dealDate > row.dealDoneDate || row.dealDoneDate > getReportPeriodEndDate())) {
            def msg1 = row.getCell('dealDoneDate').column.name
            def msg2 = row.getCell('dealDate').column.name
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }
    }

    // Проверка наличия всех фиксированных строк «Подитог»
    // Проверка отсутствия лишних фиксированных строк «Подитог»
    // Проверка итоговых значений по фиксированной строке «Подитог»
    checkItog(dataRows)

    // Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }

}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, groupColumns)

    // "Да" и "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {

        // Признак взаимозависимости
        def type = getRefBookValue(520, row.name)?.TYPE?.value
        def code = getRefBookValue(525, type)?.CODE?.value
        if (code) {
            row.dependence = (code == "ВЗЛ") ? recYesId : recNoId
        }

        // Признак внешнеторговой сделки
        row.signTransaction = (row.countryCode2 == row.countryCode3) ? recNoId : recYesId

        // Количество
        row.count = 1

        // Расчет поля "Цена", "Стоимость"
        if (row.income != null && row.outcome == null) {
            row.price = row.income
            row.cost = row.income
        } else if (row.outcome != null && row.income == null) {
            row.price = row.outcome
            row.cost = row.outcome

        } else if (row.outcome != null && row.income != null) {
            row.price = (row.income - row.outcome).abs()
            row.cost = (row.income - row.outcome).abs()
        }
    }

    // Добавление подитогов
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcGroupTotal(i, dataRows)
        }
    }, groupColumns)

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    sortFormDataRows(false)
}

// Расчет подитогового значения
DataRow<Cell> calcGroupTotal(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def str = getValuesByGroupColumn(tmpRow)
    def newRow = getSubTotalRow(i, str.toLowerCase().hashCode())

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}

/**
 * Получить подитоговую строку с заданными стилями.
 */
DataRow<Cell> getSubTotalRow(int i, def key) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Подитог'
    newRow.setAlias('itg' + key.toString() + '#' + i)
    newRow.getCell('fix').colSpan = 7
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 7
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 31
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null
    def totalRowFromFileMap = [:] // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains('Подитог')) {
            //для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def tmpRowValue = rows.get(rows.size() - 1)
            def str = getValuesByGroupColumn(tmpRowValue)
            def subTotalRow = getNewSubTotalRowFromXls(str.toLowerCase().hashCode(), rowValues, colOffset, fileRowIndex, rowIndex)
            def key = subTotalRow.getAlias().split('#')[0]
            if (totalRowFromFileMap[key] == null) {
                totalRowFromFileMap[key] = []
            }
            totalRowFromFileMap[key].add(subTotalRow)
            rows.add(subTotalRow)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        // получить посчитанные подитоги
        def tmpRows = calcGroupRows(rows)
        def tmpSubTotalRows = tmpRows.findAll { it.getAlias() }
        tmpSubTotalRows.each { subTotalRow ->
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias().split('#')[0])
            } else {
                def row = tmpRows[Integer.valueOf(subTotalRow.getAlias().split('#')[1])]
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, getValuesByGroupColumn(row)))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
    }

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][7]): 'Сведения о сделке']),
            ([(headerRows[1][1]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'dependence')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[2][12]): getColumnName(tmpRow, 'innerCode')]),
            ([(headerRows[2][13]): getColumnName(tmpRow, 'dealCountryCode')]),
            ([(headerRows[2][14]): getColumnName(tmpRow, 'signPhis')]),
            ([(headerRows[2][15]): getColumnName(tmpRow, 'signTransaction')]),
            ([(headerRows[2][16]): getColumnName(tmpRow, 'countryCode2')]),
            ([(headerRows[2][17]): getColumnName(tmpRow, 'region1')]),
            ([(headerRows[2][18]): getColumnName(tmpRow, 'city1')]),
            ([(headerRows[2][19]): getColumnName(tmpRow, 'settlement1')]),
            ([(headerRows[2][20]): getColumnName(tmpRow, 'countryCode3')]),
            ([(headerRows[2][21]): getColumnName(tmpRow, 'region2')]),
            ([(headerRows[2][22]): getColumnName(tmpRow, 'city2')]),
            ([(headerRows[2][23]): getColumnName(tmpRow, 'settlement2')]),
            ([(headerRows[1][24]): getColumnName(tmpRow, 'conditionCode')]),
            ([(headerRows[1][25]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][26]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[1][27]): getColumnName(tmpRow, 'outcome')]),
            ([(headerRows[1][28]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][29]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][30]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[3][1]): 'гр. 1']),
            ([(headerRows[3][2]): 'гр. 2.1']),
            ([(headerRows[3][3]): 'гр. 2.2']),
            ([(headerRows[3][4]): 'гр. 3']),
            ([(headerRows[3][5]): 'гр. 4.1']),
            ([(headerRows[3][6]): 'гр. 4.2']),
            ([(headerRows[3][7]): 'гр. 5']),
            ([(headerRows[3][8]): 'гр. 6']),
            ([(headerRows[3][9]): 'гр. 7.1']),
            ([(headerRows[3][10]): 'гр. 7.2']),
            ([(headerRows[3][11]): 'гр. 8']),
            ([(headerRows[3][12]): 'гр. 9.1']),
            ([(headerRows[3][13]): 'гр. 9.2']),
            ([(headerRows[3][14]): 'гр. 10']),
            ([(headerRows[3][15]): 'гр. 11']),
            ([(headerRows[3][16]): 'гр. 12.1']),
            ([(headerRows[3][17]): 'гр. 12.2']),
            ([(headerRows[3][18]): 'гр. 12.3']),
            ([(headerRows[3][19]): 'гр. 12.4']),
            ([(headerRows[3][20]): 'гр. 13.1']),
            ([(headerRows[3][21]): 'гр. 13.2']),
            ([(headerRows[3][22]): 'гр. 13.3']),
            ([(headerRows[3][23]): 'гр. 13.4']),
            ([(headerRows[3][24]): 'гр. 14']),
            ([(headerRows[3][25]): 'гр. 15']),
            ([(headerRows[3][26]): 'гр. 16']),
            ([(headerRows[3][27]): 'гр. 17']),
            ([(headerRows[3][28]): 'гр. 18']),
            ([(headerRows[3][29]): 'гр. 19']),
            ([(headerRows[3][30]): 'гр. 20'])
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile, values[4], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2.1
    newRow.name = recordId
    colIndex++

    // графа 2.2 Признак взаимозависимости
    newRow.dependence = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            def expectedValues = [map.NAME?.stringValue, map.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(10, values[colIndex], expectedValues, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7.1
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 7.2
    newRow.dealType = getRecordIdImport(85, 'CONTRACT_TYPE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9.1
    newRow.innerCode = getRecordIdImport(17, 'INNER_CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 9.2
    newRow.dealCountryCode = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.signPhis = getRecordIdImport(18, 'SIGN', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.signTransaction = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.1
    newRow.countryCode2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.2
    newRow.region1 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12.3
    newRow.city1 = values[colIndex]
    colIndex++

    // графа 12.4
    newRow.settlement1 = values[colIndex]
    colIndex++

    // графа 13.1
    newRow.countryCode3 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.2
    newRow.region2 = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.3
    newRow.city2 = values[colIndex]
    colIndex++

    // графа 13.4
    newRow.settlement2 = values[colIndex]
    colIndex++

    // графа 14
    newRow.conditionCode = getRecordIdImport(63, 'STRCODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 15..19
    ['count', 'income', 'outcome', 'price', 'cost'].each { alias ->
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графа 20
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 25
    def colIndex = 25
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 26
    colIndex = 26
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 27
    colIndex = 27
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 29
    colIndex = 29
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = dataRows.find { it.getAlias() == 'total' }
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), totalRow, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getSubTotalRow(rowIndex, key)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 25
    def colIndex = 25
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 26
    colIndex = 26
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 27
    colIndex = 27
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 29
    colIndex = 29
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

// Получить посчитанные подитоговые строки вместе со строками групп
def calcGroupRows(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcGroupTotal(i, rows)
        }
    }, groupColumns)

    return tmpRows
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRows = calcGroupRows(dataRows).findAll { it.getAlias() }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def column : totalColumns) {
                if (row1[column] != row2[column]) {
                    return getColumnName(row1, column)
                }
            }
            return null
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def values = []
    // графа 2.1
    if (row?.name) {
        def map = getRefBookValue(520, row.name)
        if (map != null) {
            values.add(map.NAME?.stringValue)
        }
    }
    if (values.isEmpty()) {
        values.add('графа 2.1 не задана')
    }

    // графа 5
    if (row?.docNumber) {
        values.add(row.docNumber)
    } else {
        values.add('графа 5 не задана')
    }

    // графа 6
    if (row?.docDate) {
        values.add(row.docDate?.format('dd.MM.yyyy'))
    } else {
        values.add('графа 6 не задана')
    }

    // графа 10
    def value = null
    if (row?.signPhis) {
        def map = getRefBookValue(18, row.signPhis)
        if (map != null) {
            value = map.SIGN?.stringValue
        }
    }
    if (!value) {
        value = 'графа 10 не задана'
    }
    values.add(value)

    // графа 11
    value = null
    if (row?.signTransaction) {
        def map = getRefBookValue(38, row.signTransaction)
        if (map != null) {
            value = map.VALUE?.stringValue
        }
    }
    if (!value) {
        value = 'графа 11 не задана'
    }
    values.add(value)

    return values.join('; ')
}