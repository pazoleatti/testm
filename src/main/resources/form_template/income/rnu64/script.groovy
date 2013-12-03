package form_template.income.rnu64

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

/**
 * РНУ-64 "Регистр налогового учёта затрат, связанных с проведением сделок РЕПО"
 * formTemplateId=355
 *
 * @author auldanov
 *
 * Описание столбцов
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки Справочник
 * 4. dealingNumber - Номер сделки
 * //5. bondKind - Вид ценных бумаг //графу удалили
 * 5. costs - Затраты (руб.коп.)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
        }
        break
    case FormDataEvent.MIGRATION:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            def dataRowHelper = formDataService.getDataRowHelper(formData)
            dataRowHelper.insert(getCalcTotalRow(), dataRowHelper.getAllCached().size() + 1)
        }
        break
}
// все атрибуты
@Field
def allColumns = ['number', 'date', 'part', 'dealingNumber', 'costs']

// Редактируемые атрибуты
@Field
def editableColumns = ['date', 'part', 'dealingNumber', 'costs']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod) {
        reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        formPrev = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
        if (formPrev == null) {
            def formName = formData.getFormType().getName()
            throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
        }
    }
}

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    ['date', 'part', 'dealingNumber', 'costs'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    if (dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    dataRows.sort({ DataRow a, DataRow b ->
        def aTime = a.date != null ? (a.date as Date).time : null
        def bTime = b.date != null ? (b.date as Date).time : null
        if (aTime == bTime) {
            aNumber = a.dealingNumber != null ? a.dealingNumber as String : null;
            bNumber = b.dealingNumber != null ? b.dealingNumber as String : null;
            return aNumber <=> bNumber
        }
        return aTime <=> bTime
    })

    def i = 1
    for (def row in dataRows) {
        if (isTotalRow(row)) {
            row.number = i++
        }
    }
    dataRowHelper.save(dataRows)

    // добавляем строки итого
    def newRowQuarter = formData.createDataRow()
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowQuarter.getCell("fix").setColSpan(4)
    newRowQuarter.fix = "Итого за текущий квартал"
    // 6 графа Содержит сумму значений "графы 6" для всех строк данной таблицы, за исключением итоговых строк («Итого за текущий квартал», «Итого за текущий отчетный (налоговый) период»)
    newRowQuarter.costs = getQuarterTotal()
    newRowQuarter.setAlias("totalQuarter")
    setTotalStyle(newRowQuarter)
    dataRowHelper.insert(newRowQuarter, dataRows.size() + 1)

    // строка Итого за текущий отчетный (налоговый) период
    def newRowTotal = formData.createDataRow()
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowTotal.getCell("fix").setColSpan(4)
    newRowTotal.fix = "Итого за текущий отчетный (налоговый) период"
    newRowTotal.costs = getTotalValue()
    newRowTotal.setAlias("total")
    setTotalStyle(newRowTotal)
    dataRowHelper.insert(newRowTotal, dataRows.size() + 1)
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def totalRow = null
    def totalQuarterRow = null
    reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)

    for (def row : dataRows) {
        // 1. Проверка на заполнение поля
        if (!isTotalRow(row)) {
            def requiredColumns = ['number', 'date', 'part', 'dealingNumber', 'costs']
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 2. Проверка даты совершения операции и границ отчетного периода
            if (row.date != null && !(
            (reportPeriodStartDate.getTime().equals(row.date) || row.date.after(reportPeriodStartDate.getTime())) &&
                    (reportPeriodEndDate.getTime().equals(row.date) || row.date.before(reportPeriodEndDate.getTime()))
            )) {
                logger.error("В строке " + row.number + " дата совершения операции вне границ отчетного периода!")
                return false
            }

            // 3. Проверка на уникальность поля «№ пп»
            for (def rowItem : dataRows) {
                if (!isTotalRow(row) && row.number == rowItem.number && !row.equals(rowItem)) {
                    logger.error("В строке " + row.number + " нарушена уникальность номера по порядку!")
                    return false
                }
            }

            // 4. Проверка на нулевые значения
            if (row.costs == 0) {
                logger.error("В строке " + row.number + " все суммы по операции нулевые!")
                return false
            }

        } else if (isMainTotalRow(row)) {
            totalRow = row
        } else if (isQuarterTotal(row)) {
            totalQuarterRow = row
        }
    }

    // проверка на наличие итоговых строк, иначе будет ошибка
    if (totalQuarterRow != null || totalRow != null) {
        // 5. Проверка итоговых значений за текущий квартал
        if (totalQuarterRow != null && totalQuarterRow.costs != getQuarterTotal()) {
            logger.error('Итоговые значения за текущий квартал рассчитаны неверно!')
            return false
        }

        // 6. Проверка итоговых значений за текущий отчётный (налоговый) период
        if (totalRow != null && totalRow.costs != getTotalValue()) {
            logger.error('Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!')
            return false
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
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
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

// Проверка при создании формы.
void checkCreation() {
    // отчётный период
    //def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Проверка является ли строка итововой за текущий квартал
 */
def isQuarterTotal(row) {
    row.getAlias() == 'totalQuarter'
}

/**
 * Проверка является ли строка итововой (последняя строка)
 */
def isMainTotalRow(row) {
    row.getAlias() == 'total'
}

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. за квартал, либо основной)
 */
def isTotalRow(row) {
    return row.getAlias() == 'total' || row.getAlias() == 'totalQuarter'
}

// функция возвращает итоговые значения за текущий квартал
def getQuarterTotal() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def row6val = 0
    for (def row : dataRows) {
        if (!isTotalRow(row)) {
            row6val += row.costs ?: 0
        }
    }
    row6val
}

// Функция возвращает итоговые значения за текущий отчётный (налоговый) период
def getTotalValue() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    quarterRow = dataRowHelper.getDataRow(dataRows, 'totalQuarter')
    // возьмем форму за предыдущий отчетный период
    def prevQuarter = quarterService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevQuarter != null) {
        prevQuarterFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevQuarter.id);

        if (prevQuarterFormData != null && prevQuarterFormData.state == WorkflowState.ACCEPTED) {
            prevQuarterData = formDataService.getDataRowHelper(prevQuarterFormData)
            def prevQuarterTotalRow = prevQuarterData.getDataRow(prevQuarterData.getAllCached(), "total")
            return quarterRow.costs + prevQuarterTotalRow.costs
        } else {
            //  Если предыдущей формы нет (либо она не принята)  то B = 0
            return quarterRow.costs
        }

    } else {
        return quarterRow.costs
    }
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        rows.add(row)
                    }
                }
            }
        }
    }
    formDataService.getDataRowHelper(formData).save(rows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'fix', 'date', 'part', 'dealingNumber', 'costs'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    String charset = ""
    // TODO в дальнейшем убрать возможность загружать RNU для импорта!
    if (formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml') ||
            formDataEvent == FormDataEvent.MIGRATION && fileName.contains('.xml')) {
        if (!fileName.contains('.xml')) {
            logger.error('Формат файла должен быть *.xml')
            return
        }
    } else {
        if (!fileName.contains('.r')) {
            logger.error('Формат файла должен быть *.rnu')
            return
        }
        charset = 'cp866'
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(is, fileName, charset)
    if (xmlString == null || xmlString == '') {
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
        def totalLoad = addData(xml, fileName)

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, def fileName) {
    def index
    def date = new Date()
    def cache = [:]

    def data = formDataService.getDataRowHelper(formData)
    data.clear()
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def newRows = []

    def records
    def totalRecords
    def type
    if (formDataEvent == FormDataEvent.MIGRATION ||
            formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml')) {
        records = xml.exemplar.table.detail.record
        totalRecords = xml.exemplar.table.total.record
        type = 1 // XML
    } else {
        records = xml.row
        totalRecords = xml.rowTotal
        type = 2 // RNU
    }

    for (def row : records) {
        index = 0
        def newRow = getNewRow()

        // графа 1
        newRow.number = getNumber(getCellValue(row, index, type))
        index++

        // графа 2
        newRow.date = getDate(getCellValue(row, index, type), format)
        index++

        // графа 3 - справочник 60 "Части сделок"
        tmp = null
        if (row.field[index].@value.text() != null && getCellValue(row, index, type).trim() != '') {
            tmp = getRecordId(60, 'CODE', getCellValue(row, index, type), date, cache)
        }
        newRow.part = tmp
        index++

        // графа 4
        newRow.dealingNumber = getCellValue(row, index, type, true)
        index++

        // графа 5
        // TODO bondKind выпилена из РНУ, а из файла для импорта?
        // index++

        // графа 6
        newRow.costs = getNumber(getCellValue(row, index, type))

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (totalRecords.size() >= 1) {
        def row = totalRecords[0]
        def totalRow = formData.createDataRow()

        // графа 6
        totalRow.costs = getNumber(getCellValue(row, 5, type))

        return totalRow
    } else {
        return null
    }
}

// для получения данных из RNU или XML
String getCellValue(def row, int index, def type, boolean isTextXml = false){
    if (type==1) {
        if (isTextXml) {
            return row.field[index].text()
        } else {
            return row.field[index].@value.text()
        }
    }
    return row.cell[index+1].text()
}

/**
 * Получить новую строку с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

    // графа 2..5
    ['date', 'part', 'dealingNumber', 'costs'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, def format) {
    if (isEmpty(value)) {
        return null
    }
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в дату. " + e.message)
    }
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Рассчитать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [6: 'costs']

    def totalCalc = getCalcTotalRow()
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
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.getCell("fix").setColSpan(4)
    totalRow.fix = "Итоги"
    totalRow.setAlias("total")
    setTotalStyle(totalRow)
    totalRow.costs = getSum('costs')
    return totalRow
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def from = 0
    def to = dataRows.size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}