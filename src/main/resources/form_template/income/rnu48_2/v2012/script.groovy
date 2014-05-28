package form_template.income.rnu48_2.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState

import java.math.RoundingMode

/**
 * (РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
 * formTypeId=313
 *
 * @author vsergeev
 */

// графа 1 - number      - № пп
// графа   - fix
// графа 2 - kind        - Вид расходов
// графа 3 - summ        - Сумма, включаемая в состав материальных расходов

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRNU48_1()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkRNU48_1()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        checkRNU48_1()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        checkRNU48_1()
        importData()
        calc()
        logicCheck()
        break
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalRow = getDataRow(dataRows, 'total')
    totalRow.summ = calcTotal(dataRows)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    for (def row : dataRows) {
        if (row.getAlias() != 'total') {
            // 1. Обязательность заполнения поля графы 1..3
            checkNonEmptyColumns(row, row.getIndex(), ['summ'], logger, true)
        } else {
            //2. Проверка итоговых значений по всей форме
            if (row.summ != calcTotal(dataRows)) {
                logger.error(WRONG_TOTAL, getColumnName(row, 'summ'))
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // сбросить строки
    dataRows.each { row ->
        row.summ = 0
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source)?.allCached
                sourceDataRows.each { row ->
                    def curRow = getDataRow(dataRows, row.getAlias())
                    curRow.summ += (row.summ ?: 0)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

BigDecimal calcTotal(def dataRows) {
    BigDecimal sum = 0
    dataRows.each { row ->
        sum += (row.getAlias() != 'total' ? (row.summ ?: 0) : 0)
    }
    return sum?.setScale(2, RoundingMode.HALF_UP)
}

def checkRNU48_1() {
    // идентификатор формы рну-48.1
    def rnuId = 343
    if (formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkFormExistAndAccepted(rnuId, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, false, logger, true)
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'number'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 4, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'number'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'kind'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'summ'),
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    for (int i = 0; i < 4; i++) {
        def rowIndex = headRowCount + i
        if (xml.row[rowIndex] != null) {
            // графа 3 строки i
            dataRows[i].summ = parseNumber(xml.row[rowIndex].cell[3].text(), rowOffset + rowIndex + 1, 4 + colOffset, logger, true)
        } else {
            dataRows[i].summ = null
        }
    }
    dataRowHelper.update(dataRows)
}