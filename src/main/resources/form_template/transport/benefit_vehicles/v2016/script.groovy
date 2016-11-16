package form_template.transport.benefit_vehicles.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import groovy.transform.Field

/**
 * Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог.
 *
 * formTypeId = 202
 * formTemplateId = 2202
 */

// графа 1  - rowNumber
// графа 2  - codeOKATO         - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 3  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 4  - tsType            - зависит от графы 3 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 5  - identNumber
// графа 6  - regNumber
// графа 7  - powerVal
// графа 8  - baseUnit          - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения на основании ОКЕИ»
// графа 9  - taxBenefitCode    - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 10 - taxBenefitBase    - зависит от графы 9 - атрибут 702 - BASE - «Основание», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 11 - benefitStartDate
// графа 12 - benefitEndDate

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkRegionId()
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            copyFromPrevForm()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
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
def allColumns = ['rowNumber', 'codeOKATO', 'tsTypeCode', 'tsType', 'identNumber', 'regNumber', 'powerVal',
        'baseUnit', 'taxBenefitCode', 'taxBenefitBase', 'benefitStartDate', 'benefitEndDate']

// графа 2, 3, 5..9, 11, 12
@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'powerVal',
        'baseUnit', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns

// графа 2, 3, 5..9, 11
@Field
def nonEmptyColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'powerVal', 'baseUnit', 'taxBenefitCode', 'benefitStartDate']

// графа 2 и остальные
@Field
def sortColumns = ['codeOKATO'] + (allColumns - 'codeOKATO' - 'rowNumber')

// дата окончания отчетного периода
@Field
def endDate = null

// отчетный период формы
@Field
def currentReportPeriod = null

@Field
def copyColumns = allColumns - 'rowNumber'

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                      boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                    boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // для логической проверки 5
    def equalsRowsMap = [:]

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    checkRegionId()

    // 10.a Проверка наличия сведений о ТС в форме «Сведения о транспортных средствах, по которым уплачивается транспортный налог»
    def rows201 = getDataRows201()
    if (rows201 == null) {
        def reportPeriod = getReportPeriod()
        def periodName = reportPeriod?.name
        def year = reportPeriod.taxPeriod.year
        logger.error("В Системе отсутствует форма «Сведения о транспортных средствах, по которым уплачивается налог» " +
                "в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                periodName, year, formDataDepartment.name)
    }

    for (def row : dataRows) {
        def index = row.getIndex()

        // 2. Проверка заполнения обязательных граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка корректности заполнения даты начала использования льготы
        if (row.benefitStartDate && row.benefitStartDate > getReportPeriodEndDate()) {
            def columnName11 = getColumnName(row, 'benefitStartDate')
            def dateInStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", index, columnName11, dateInStr)
        }

        // 4. Проверка корректности заполнения даты окончания использования льготы
        if (row.benefitStartDate && row.benefitEndDate && row.benefitStartDate > row.benefitEndDate) {
            def columnName12 = getColumnName(row, 'benefitEndDate')
            def columnName11 = getColumnName(row, 'benefitStartDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                    index, columnName12, columnName11)
        }

        // 5. Проверка на наличие в форме строк с одинаковым значением граф 2, 3, 5, 6, 7, 8 и пересекающимися периодами использования льготы
        // 6. Проверка количества льгот для одного ТС на форме
        // сбор данных
        def columnsForEquals = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'powerVal', 'baseUnit']
        def keyValues = columnsForEquals.collect { row[it] }
        def key = keyValues.join('#')
        if (equalsRowsMap[key] == null) {
            equalsRowsMap[key] = []
        }
        equalsRowsMap[key].add(row)

        // 7. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется в методе copyFromPrevForm()

        // 8. Проверка наличия формы «Сведения о транспортных средствах, по которым уплачивается налог»
        // Выполняется в методе copyFrom201()

        // 9. Проверка корректности заполнения кода налоговой льготы
        if (row.codeOKATO && row.taxBenefitCode) {
            def value2RegionId = getRegion(row.codeOKATO)?.record_id?.value
            def record7 = getAllRecords(7L).get(row.taxBenefitCode)
            def value9RegionId = record7?.DICT_REGION_ID?.value
            if (value2RegionId != value9RegionId) {
                def columnName2 = getColumnName(row, 'codeOKATO')
                def value2 = getRefBookValue(96L, row.codeOKATO)?.CODE?.value
                def columnName9 = getColumnName(row, 'taxBenefitCode')
                def regionCode = getAllRecords(4L).get(value9RegionId)?.CODE?.value
                logger.error("Строка %s: Значение графы «%s» (%s) должно относится к региону, " +
                        "в котором действует выбранная в графе «%s» льгота («%s»)",
                        index, columnName2, value2, columnName9, regionCode)
            }
        }

        // 10.b Проверка наличия сведений о ТС в форме «Сведения о транспортных средствах, по которым уплачивается транспортный налог»
        if (rows201 != null) {
            def hasRow = rows201.find {
                // графа 2 = графа 2
                row.codeOKATO == it.codeOKATO &&
                // графа 3 = графа 4
                row.tsTypeCode == it.tsTypeCode &&
                // графа 5 = графа 8
                row.identNumber == it.identNumber &&
                // графа 6 = графа 9
                row.regNumber == it.regNumber &&
                // графа 7 = графа 13
                row.powerVal == it.taxBase &&
                // графа 8 = графа 14
                row.baseUnit == it.baseUnit
            }
            if (!hasRow) {
                def value2 = getRefBookValue(96L, row.codeOKATO)?.CODE?.value ?: ''
                def value3 = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value ?: ''
                def value5 = row.identNumber ?: ''
                def value6 = row.regNumber ?: ''
                def value7 = row.powerVal ?: ''
                def value8 = getRefBookValue(12L, row.baseUnit)?.CODE?.value ?: ''
                logger.error("Строка %s: На форме «Сведения о транспортных средствах, по которым уплачивается налог» " +
                        "отсутствуют сведения о ТС с кодом ОКТМО «%s», кодом вида ТС «%s», идентификационным номером «%s», " +
                        "регистрационным знаком «%s», величиной мощности «%s» и единицей измерения мощности «%s»",
                        index, value2, value3, value5, value6, value7, value8)
            }
        }
    }

    // 5. Проверка на наличие в форме строк с одинаковым значением граф 2, 3, 5, 6, 7, 8 и пересекающимися периодами использования льготы
    for (def key : equalsRowsMap.keySet().toList()) {
        def rows = equalsRowsMap[key]
        if (rows.size() < 2) {
            continue
        }
        def hasCross = false
        for (int i = 0; i < rows.size(); i++) {
            def row1 = rows[i]
            for (int j = i + 1; j < rows.size(); j++) {
                def row2 = rows[j]
                def start1 = row1.benefitStartDate
                def start2 = row2.benefitStartDate
                def end1 = row1.benefitEndDate ?: getReportPeriodEndDate()
                def end2 = row1.benefitEndDate ?: getReportPeriodEndDate()
                if (start1 <= start2 && end1 >= start2 || start2 <= start1 && end2 >= start1) {
                    hasCross = true
                    break
                }
            }
            if (hasCross) {
                break
            }
        }
        if (hasCross) {
            def indexes = rows?.collect { it.getIndex() }
            def indexesInStr = indexes?.join(', ')
            def row = rows[0]
            def value2 = getRefBookValue(96, row.codeOKATO)?.CODE?.value ?: ''
            def value3 = getRefBookValue(42, row.tsTypeCode)?.CODE?.value ?: ''
            def value5 = row.identNumber ?: ''
            def value6 = row.regNumber ?: ''
            def value7 = row.powerVal ?: ''
            def value8 = getRefBookValue(12, row.baseUnit)?.CODE?.value ?: ''
            logger.error("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                    "Регистрационный знак «%s», Величина мощности «%s», Единица измерения мощности «%s»: " +
                    "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, " +
                    "идентификационным номером ТС, регистрационным знаком ТС, величиной мощности и " +
                    "единицей измерения мощности и пересекающимися периодами использования льготы",
                    indexesInStr, value2, value3, value5, value6, value7, value8)
        } else {
            // 6. Проверка количества льгот для одного ТС на форме
            def hasError = false
            def value9 = null
            for (def row : rows) {
                if (value9 == null && row.taxBenefitCode != null) {
                    value9 = getTaxBenefitCode(row.taxBenefitCode)
                    continue
                }
                if (row.taxBenefitCode && value9 && value9 != getTaxBenefitCode(row.taxBenefitCode)) {
                    hasError = true
                    break
                }
            }
            if (hasError) {
                def indexes = rows?.collect { it.getIndex() }
                def indexesInStr = indexes?.join(', ')
                logger.error("Строки %s: Для ТС не может быть указано  более одного вида льготы", indexesInStr)
            }
        }
    }
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (formData.kind == FormDataKind.CONSOLIDATED || getPrevReportPeriod()?.period == null) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

@Field
def prevReportPeriodMap = null

/**
 * Получить предыдущий отчетный период
 *
 * @return мапа с данными предыдущего периода:
 *      period - период (может быть null, если предыдущего периода нет);
 *      periodName - название;
 *      year - год;
 */
def getPrevReportPeriod() {
    if (prevReportPeriodMap != null) {
        return prevReportPeriodMap
    }
    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def find = false
    // предыдущий период в том же году, что и текущий, и номера периодов отличаются на единицу
    if (prevReportPeriod && reportPeriod.order > 1 && reportPeriod.order - 1 == prevReportPeriod.order &&
            reportPeriod.taxPeriod.year == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    // если текущий период первый в налоговом периоде, то предыдущий период должен быть последним, и года налоговых периодов должны отличаться на единицу
    if (!find && prevReportPeriod && reportPeriod.order == 1 && prevReportPeriod.order == 4 &&
            reportPeriod.taxPeriod.year - 1 == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    prevReportPeriodMap = [:]
    if (find) {
        prevReportPeriodMap.period = prevReportPeriod
        prevReportPeriodMap.periodName = prevReportPeriod.name
        prevReportPeriodMap.year = prevReportPeriod.taxPeriod.year
    } else {
        // получение названии периодов
        def filter = 'T = 1'
        def provider = formDataService.getRefBookProvider(refBookFactory, 8L, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        records?.sort { it?.END_DATE?.value }

        prevReportPeriodMap.period = null
        prevReportPeriodMap.periodName = records[reportPeriod.order - 2].NAME?.value
        prevReportPeriodMap.year = (reportPeriod.order == 1 ? reportPeriod.taxPeriod.year - 1 : reportPeriod.taxPeriod.year)
    }
    return prevReportPeriodMap
}

/** Копирование данных из форм предыдущего периода. */
void copyFromPrevForm() {
    // Логическая проверка 7 - нет формы предыдущего периода
    def prevDataRows = getPrevDataRows()
    if (prevDataRows == null) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        logger.warn("Данные по транспортным средствам из формы предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                periodName, year, formDataDepartment.name)

        copyFrom201()
        return
    }

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def endYearDate = Date.parse('dd.MM.yyyy', '31.12.' + year)
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def dataRows = []
    // 1 квартал - отбор подходящих строк
    // 2, 3, 4 квартал (год) - простое копирование всех строк
    def isFirstPeriod = (reportPeriod.order == 1)

    for (def prevRow : prevDataRows) {
        def useRow = (isFirstPeriod ? prevRow.benefitStartDate <= endYearDate && (prevRow.benefitEndDate == null || prevRow.benefitEndDate >= startYearDate) : true)
        if (!useRow) {
            continue
        }
        def newRow = getNewRow()
        copyColumns.each { alias ->
            newRow[alias] = prevRow[alias]
        }
        dataRows.add(newRow)
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // массовое разыменование справочных и зависимых значений
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns())
    sortRows(dataRows, sortColumns)
    dataRowHelper.saveSort()
}

// Логическая проверка 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «$formDataDepartment.name»")
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

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

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    def departmentRegionId = formDataDepartment.regionId
    if (!departmentRegionId) {
        logger.warn("Ну удалось заполнить графу «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»",
                getColumnName(tmpRow, 'taxBenefitCode'))
    }

    // заполнить кэш данными из справочника ОКТМО
    def limitRows = 10
    if (allValuesCount > limitRows) {
        fillRefBookCache(96L)
        fillRecordCache(96L, 'CODE', getReportPeriodEndDate())
    }

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
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, departmentRegionId)
        rows.add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][0]) : headers[0].rowNumber],
            [(headerRows[0][1]) : headers[0].codeOKATO],
            [(headerRows[0][2]) : headers[0].tsTypeCode],
            [(headerRows[0][3]) : headers[0].tsType],
            [(headerRows[0][4]) : headers[0].identNumber],
            [(headerRows[0][5]) : headers[0].regNumber],
            [(headerRows[0][6]) : headers[0].powerVal],
            [(headerRows[1][6]) : headers[1].powerVal],
            [(headerRows[1][7]) : headers[1].baseUnit],
            [(headerRows[0][8]) : headers[0].taxBenefitCode],
            [(headerRows[0][9]) : headers[0].taxBenefitBase],
            [(headerRows[0][10]): headers[0].benefitStartDate],
            [(headerRows[0][11]): headers[0].benefitEndDate]
    ]
    (0..11).each {
        headerMapping.add([(headerRows[2][it]) : (it + 1).toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param departmentRegionId регион подразделения
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def departmentRegionId) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    def int colIndex

    // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex = 1
    if (values[colIndex]) {
        newRow.codeOKATO = getRecordIdImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    } else {
        // 2. Проверка заполнения кода ОКТМО
        def columnName9 = getColumnName(newRow, 'taxBenefitCode')
        def columnName2 = getColumnName(newRow, 'codeOKATO')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, getXLSColumnName(9), columnName9, columnName2)
    }

    // графа 3 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    colIndex++
    def record42 = getRecordImport(42, 'CODE', values[colIndex].replace(' ', ''), fileRowIndex, colIndex + colOffset, false)
    newRow.tsTypeCode = record42?.record_id?.value

    // графа 4 - зависит от графы 3 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    colIndex++
    if (record42 != null) {
        def expectedValues = [record42?.NAME?.value]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'tsType'), record42?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 5
    colIndex++
    newRow.identNumber = values[colIndex]

    // графа 6
    colIndex++
    newRow.regNumber = values[colIndex]?.replace(' ', '')

    // графа 7
    colIndex++
    newRow.powerVal = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 8 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    colIndex++
    newRow.baseUnit = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9  - taxBenefitCode    - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7 «Параметры налоговых льгот транспортного налога»
    colIndex++
    if (departmentRegionId && newRow.codeOKATO) {
        def record6 = getAllRecords(6L)?.values()?.find { it?.CODE?.value == values[colIndex] }
        def regionId = getRegion(newRow.codeOKATO)?.record_id?.value
        def record7 = null
        if (record6) {
            record7 = getAllRecords(7L)?.values()?.find {
                it?.DECLARATION_REGION_ID?.value == departmentRegionId &&
                        it?.DICT_REGION_ID?.value == regionId &&
                        it?.TAX_BENEFIT_ID?.value == record6?.record_id?.value &&
                        it?.BASE?.value == values[colIndex + 1]
            }
        }
        if (record7) {
            newRow.taxBenefitCode = record7?.record_id?.value
        } else {
            // 3. Проверка наличия информации о налоговой льготе в справочнике «Параметры налоговых льгот транспортного налога»
            def columnName9 = getColumnName(newRow, 'taxBenefitCode')
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                    "«Параметры налоговых льгот транспортного налога» не найдена соответствующая запись",
                    fileRowIndex, getXLSColumnName(colIndex + 1), columnName9)
        }
    }

    // графа 10 - зависит от графы 9 - не проверяется, потому что используется для нахождения записи для графы 9
    colIndex++

    // графа 11
    colIndex++
    newRow.benefitStartDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex++
    newRow.benefitEndDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

def getRegion(def record96Id) {
    def record96 = getRefBookValue(96L, record96Id)
    def okato = getOkato(record96?.CODE?.stringValue)
    if (okato) {
        def allRecords = getAllRecords(4L).values()
        return allRecords.find { record ->
            record.OKTMO_DEFINITION.value == okato
        }
    }
    return null
}

def getOkato(String codeOkato) {
    if (!codeOkato || codeOkato.length() < 3) {
        return codeOkato
    }
    codeOkato = codeOkato?.substring(0, 3)
    if (codeOkato && !(codeOkato in ["719", "718", "118"])) {
        codeOkato = codeOkato?.substring(0, 2)
    }
    return codeOkato
}

/** Копирование данных из формы "Сведения о ТС" (typeId = 201). */
void copyFrom201() {
    def dataRows201 = getDataRows201()

    // Логическая проверка 8 - нет формы "Сведения о ТС" (typeId = 201)
    if (dataRows201 == null) {
        def reportPeriod = getReportPeriod()
        def periodName = reportPeriod?.name
        def year = reportPeriod?.taxPeriod?.year
        logger.warn("Данные по транспортным средствам из формы «Сведения о ТС, по которым уплачивается транспортный налог» не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                periodName, year, formDataDepartment.name)
        return
    }

    def dataRows = []
    for (def row : dataRows201) {
        def newRow = getNewRow()
        // графа 2 = графа 2
        newRow.codeOKATO = row.codeOKATO
        // графа 3 = графа 4
        newRow.tsTypeCode = row.tsTypeCode
        // графа 5 = графа 8
        newRow.identNumber = row.identNumber
        // графа 6 = графа 9
        newRow.regNumber = row.regNumber
        // графа 7 = графа 13
        newRow.powerVal = row.taxBase
        // графа 8 = графа 14
        newRow.baseUnit = row.baseUnit

        dataRows.add(newRow)
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

def getDataRows201() {
    def formData201 = formDataService.getLast(201, formData.kind, formDataDepartment.id, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
    if (formData201 != null && formData201.state == WorkflowState.ACCEPTED) {
        return formDataService.getDataRowHelper(formData201).allSaved
    }
    return null
}

/**
 * Получить значение "Код налоговой льготы" для графы 9.
 *
 * @param record7Id id записи справочника 7 «Параметры налоговых льгот транспортного налога»
 */
def getTaxBenefitCode(def record7Id) {
    def record7 = getAllRecords(7L).get(record7Id)
    def record6 = (record7 ? getAllRecords(6L).get(record7?.TAX_BENEFIT_ID?.value) : null)
    return record6?.CODE?.value
}

/** Заполнить refBookCache всеми записями справочника refBookId. */
void fillRefBookCache(def refBookId) {
    def records = getAllRecords2(refBookId)
    for (def record : records) {
        def recordId = record?.record_id?.value
        def key = getRefBookCacheKey(refBookId, recordId)
        if (refBookCache[key] == null) {
            refBookCache.put(key, record)
        }
    }
}

/**
 * Заполнить recordCache всеми записями справочника refBookId из refBookCache.
 *
 * @param refBookId идентификатор справочника
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param date дата по которой будет осуществляться поиск
 */
void fillRecordCache(def refBookId, def alias, def date) {
    def keys = refBookCache.keySet().toList()
    def needKeys = keys.findAll { it.contains(refBookId + SEPARATOR) }
    def dateSts = date.format('dd.MM.yyyy')
    def rb = refBookFactory.get(refBookId)
    for (def needKey : needKeys) {
        def recordId = refBookCache[needKey]?.record_id?.value
        def value = refBookCache[needKey][alias]?.value
        def filter = getFilter(alias, value, rb)
        def key = dateSts + filter
        if (recordCache[refBookId] == null) {
            recordCache[refBookId] = [:]
        }
        recordCache[refBookId][key] = recordId
    }
}

/**
 * Формирование фильтра. Взято из FormDataServiceImpl.getRefBookRecord(...)
 *
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param value значение атрибута справочника
 * @param rb справочник
 */
def getFilter(def alias, def value, def rb) {
    def filter
    if (value == null || value.isEmpty()) {
        filter = alias + " is null"
    } else {
        RefBookAttributeType type = rb.getAttribute(alias).getAttributeType()
        String template
        // TODO: поиск по выражениям с датами не реализован
        if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
            if (!isNumeric(value)) {
                // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                return null
            }
            template = "%s = %s"
        } else {
            template = "LOWER(%s) = LOWER('%s')"
        }
        filter = String.format(template, alias, value)
    }
    return filter
}

boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?")
}

@Field
def allRecordsMap2 = [:]

def getAllRecords2(def refbookId) {
    if (allRecordsMap2[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        allRecordsMap2[refbookId] = provider.getRecords(getReportPeriodEndDate(), null, null, null)
    }
    return allRecordsMap2[refbookId]
}