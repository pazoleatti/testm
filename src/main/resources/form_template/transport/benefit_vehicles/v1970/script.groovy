package form_template.transport.benefit_vehicles.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
 * formTemplateId=202
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        copyData()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
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
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

// графа 1 - rowNumber         - № пп
// графа 2 - codeOKATO         - Код ОКТМО
// графа 3 - identNumber       - Идентификационный номер
// графа 4 - regNumber         - Регистрационный знак
// графа 5 - powerVal          - Мощность (величина)
// графа 6 - baseUnit          - Мощность (ед. измерения)
// графа 7 - taxBenefitCode    - Код налоговой льготы
// графа 8 - benefitStartDate  - Дата начала Использование льготы
// графа 9 - benefitEndDate    - Дата окончания Использование льготы

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

// Редактируемые атрибуты
@Field
def copyColumns = ['codeOKATO', 'identNumber', 'regNumber', 'powerVal', 'baseUnit',
        'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

@Field
def editableColumns = ['codeOKATO', 'identNumber', 'regNumber', 'powerVal', 'baseUnit',
        'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'codeOKATO', 'identNumber', 'regNumber', 'powerVal', 'baseUnit',
        'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber']

// дата начала отчетного периода
@Field
def startDate = null

// дата окончания отчетного периода
@Field
def endDate = null

@Field
def reportDay = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (!dataRows.isEmpty()) {
        sort(dataRows)
        def i = 1
        for (def row in dataRows) {
            row.rowNumber = i++
        }
        dataRowHelper.save(dataRows);
    }
}

// сортировка ОКТМО
void sort(def dataRows) {
    dataRows.sort { a, b ->
        def valA = getRefBookValue(96, a.codeOKATO)?.CODE?.stringValue
        def valB = getRefBookValue(96, b.codeOKATO)?.CODE?.stringValue
        return (valA != null && valB != null) ? valA.compareTo(valB) : 0
    }
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def String dFormat = "dd.MM.yyyy"

    // Проверенные строки (3-я провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()

    for (def row in dataRows) {

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)

        if (row.benefitStartDate != null && row.benefitEndDate != null) {
            // 2. Поверка на соответствие дат использования льготы
            if (row.benefitEndDate.compareTo(row.benefitStartDate) < 0) {
                logger.error(errorMsg + 'Неверно указаны даты начала и окончания использования льготы!')
            }

            // 4. Проверка на наличие в списке ТС строк, период использования льготы которых не пересекается
            // с отчётным / налоговым периодом, к которому относится налоговая форма
            if (row.benefitStartDate > dTo || row.benefitEndDate < dFrom) {
                logger.error(errorMsg + 'Период использования льготы ТС ('
                        + row.benefitStartDate.format(dFormat) + ' - ' + row.benefitEndDate.format(dFormat) + ') ' +
                        ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                        '), за который сформирована налоговая форма!')
            }
        }

        // 3. Проверка на наличие в списке ТС строк, для которых графы 2, 3, 4
        // («Код ОКТМО», «Идентификационный номер», «Регистрационный знак») одинаковы
        if (!checkedRows.contains(row)) {
            def errorRows = ''
            for (def rowIn in dataRows) {
                if (!checkedRows.contains(rowIn) && row != rowIn && isEquals(row, rowIn)) {
                    checkedRows.add(rowIn)
                    errorRows = ', ' + rowIn.getIndex()
                }
            }
            if (!''.equals(errorRows)) {
                logger.error("Обнаружены строки $index$errorRows, у которых " +
                        "Код ОКТМО = ${getRefBookValue(96, row.codeOKATO)?.CODE?.stringValue}, " +
                        "Идентификационный номер = $row.identNumber, " +
                        "Мощность (величина) = $row.powerVal, " +
                        "Мощность (ед. измерения) = ${getRefBookValue(12, row.baseUnit)?.CODE?.stringValue} " +
                        "совпадают!")
            }
        }
        checkedRows.add(row)

        /**
         * 6. Проверка льготы
         * Проверка осуществляется только для кодов 20210, 20220, 20230
         */
        if (row.taxBenefitCode != null && getRefBookValue(6, row.taxBenefitCode)?.CODE?.stringValue in ['20210', '20220', '20230']) {
            def region = getRegionByOKTMO(row.codeOKATO, errorMsg)
            if (region != null) {
                query = "TAX_BENEFIT_ID =" + row.taxBenefitCode + " AND DICT_REGION_ID = " + region.record_id
                if (getRecord(7, query, getReportDate()) == null) {
                    logger.error(errorMsg + "Выбранная льгота для текущего региона не предусмотрена!")
                }
            }
        }

        // 7. Проверка выбранного кода (актуально только для 0.3.5, в 0.3.7 будет не нужна - в конфигураторе столбца выставляется фильтр)
    }

    // 5. Проверка наличия формы предыдущего периода
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def str = ''
    if (reportPeriod.order == 4) {
        str += checkPrevPeriod(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        str += checkPrevPeriod(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        str += checkPrevPeriod(prevReportPeriod)
    } else {
        str = checkPrevPeriod(prevReportPeriod)
    }
    if (str.length() > 2) {
        logger.warn("Данные ТС из предыдущих отчётных периодов не были скопированы. В Системе " +
                "не создавались формы за следующие периоды: " + str.substring(0, str.size() - 2) + ".")
    }
}

/**
 * Получение региона по коду ОКТМО
 */
def getRegionByOKTMO(def oktmoCell, def errorMsg) {
    def reportDate = getReportDate()

    def oktmo3 = getRefBookValue(96, oktmoCell)?.CODE?.stringValue.substring(0, 2)
    if (oktmo3.equals("719")) {
        return getRecord(4, 'CODE', '89', null, null, reportDate);
    } else if (oktmo3.equals("718")) {
        return getRecord(4, 'CODE', '86', null, null, reportDate);
    } else if (oktmo3.equals("118")) {
        return getRecord(4, 'CODE', '83', null, null, reportDate);
    } else {
        def filter = "OKTMO_DEFINITION like '" + oktmo3.substring(0, 2) + "%'"
        def record = getRecord(4, filter, reportDate)
        if (record != null) {
            return record
        } else {
            logger.error(errorMsg + "Не удалось определить регион по коду ОКТМО")
            return null;
        }
    }
}

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                return refBookCache.get(recordId)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider
    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    provider = providerCache.get(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null)
            refBookCache.put(recordId, retVal)
        return retVal
    }
    return null
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    }
    return reportDay
}

def String checkPrevPeriod(def reportPeriod) {
    if (reportPeriod != null) {
        if (formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id) == null) {
            return reportPeriod.name + " " + reportPeriod.taxPeriod.year + ", "
        }
    }
    return ''
}

// Алгоритм копирования данных из форм предыдущего периода при создании формы
def copyData() {
    def rows = []
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (reportPeriod.order == 4) {
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, []))
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, rows))
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, rows))
    } else {
        rows += getPrevRowsForCopy(prevReportPeriod, [])
    }

    if (rows.size() > 0) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.save(rows)
    }
}

def copyRow(def row) {
    def newRow = formData.createDataRow()
    editableColumns.each { alias ->
        newRow.getCell(alias).editable = true
        newRow.getCell(alias).setStyleAlias("Редактируемая")
    }
    copyColumns.each { alias ->
        newRow.getCell(alias).setValue(row.getCell(alias).value, row.getIndex())
    }
    return newRow
}

//Получить строки для копирования за предыдущий отчетный период
def getPrevRowsForCopy(def reportPeriod, def rowsOldE) {
    def rows = []
    def rowsOld = []
    rowsOld.addAll(rowsOldE)
    if (reportPeriod != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id)
        def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)
        if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
            def dFrom = getReportPeriodStartDate()
            def dTo = getReportPeriodEndDate()
            for (def row in dataRowsOld) {
                if ((row.benefitEndDate != null && row.benefitEndDate < dFrom) || (row.benefitStartDate > dTo)) {
                    continue
                }

                // эта часть вроде как лишняя
                def benefitEndDate = row.benefitEndDate
                if (benefitEndDate == null || benefitEndDate > dTo) {
                    benefitEndDate = dTo
                }
                def benefitStartDate = row.benefitStartDate
                if (benefitStartDate < dFrom) {
                    benefitStartDate = dFrom
                }
                if (benefitStartDate > dTo || benefitEndDate < dFrom) {
                    continue
                }

                // исключаем дубли
                def need = true
                for (def rowOld in rowsOld) {
                    if (isEquals(row, rowOld)) {
                        need = false
                        break
                    }
                }
                if (need) {
                    row.setIndex(rowsOld.size())
                    newRow = copyRow(row)
                    rows.add(newRow)
                    rowsOld.add(newRow)
                }
            }
        }
    }
    return rows
}

def isEquals(def row1, def row2) {
    if (row1.codeOKATO == null || row1.identNumber == null || row1.powerVal == null || row1.baseUnit == null) {
        return true
    }
    return (row1.codeOKATO.equals(row2.codeOKATO) && row1.identNumber.equals(row2.identNumber)
            && row1.powerVal.equals(row2.powerVal) && row1.baseUnit.equals(row2.baseUnit))
}

def getReportPeriodStartDate() {
    if (!startDate) {
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Код ОКТМО',
            (xml.row[0].cell[2]): 'Идентификационный номер',
            (xml.row[0].cell[3]): 'Регистрационный знак',
            (xml.row[0].cell[4]): 'Мощность (величина)',
            (xml.row[0].cell[5]): 'Мощность (ед. измерения)',
            (xml.row[0].cell[6]): 'Код налоговой льготы',
            (xml.row[0].cell[7]): 'Использование льготы',
            (xml.row[1].cell[7]): 'Дата начала',
            (xml.row[1].cell[8]): 'Дата окончания'
    ]
    (0..8).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()


    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа 2
        newRow.codeOKATO = getRecordIdImport(96, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 3
        newRow.identNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 4
        newRow.regNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 5
        newRow.powerVal = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 6
        newRow.baseUnit = getRecordIdImport(12, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 7
        newRow.taxBenefitCode = getRecordIdImport(6, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 8
        newRow.benefitStartDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.benefitEndDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}