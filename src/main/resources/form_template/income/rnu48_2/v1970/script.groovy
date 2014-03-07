package form_template.income.rnu48_2.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * "(РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
 * formTemplateId=313
 *
 * @author vsergeev
 */

// 0 - forTotalRow - костыль
// 1 - number      - № пп
// 2 - kind        - Вид расходов
// 3 - summ        - Сумма, включаемая в состав материальных расходов , (руб.)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkRNU48_1()
        calc()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

//// Кэши и константы

// Проверяемые на пустые значения атрибуты (графа )
@Field
def nonEmptyColumns = ['summ']

void checkCreation() {
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }
    formDataService.checkUnique(formData, logger)
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
            checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        } else {
            //2. Проверка итоговых значений по всей форме
            if (row.summ != calcTotal(dataRows)) {
                logger.error('Итоговые значения рассчитаны неверно!')
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
    logger.info('Формирование консолидированной формы прошло успешно.')
}

BigDecimal calcTotal(def dataRows) {
    def sum = 0
    dataRows.each { row ->
        sum += (row.getAlias() != 'total' ? (row.summ ?: 0) : 0)
    }
    return sum?.setScale(2, RoundingMode.HALF_UP)
}

def checkRNU48_1() {
    if (formData.kind == FormDataKind.PRIMARY && getFormDataRNU48_1() == null) {
        throw new ServiceException("Не найдены экземпляры «${formTypeService.get(343).name}» за текущий отчетный период!")
    }
}

def getFormDataRNU48_1() {
    def form = formDataService.find(343, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    return (form != null && form.state == WorkflowState.ACCEPTED ? form : null)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 3, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Вид расходов',
            (xml.row[0].cell[2]): 'Сумма, включаемая в состав материальных расходов , (руб.)',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): 'Стоимость введённого в эксплуатацию инвентаря и принадлежностей, стоимость которых до 40 000 руб.',
            (xml.row[3].cell[0]): '2',
            (xml.row[3].cell[1]): 'Расходы на приобретение печатных изданий',
            (xml.row[4].cell[0]): '3',
            (xml.row[4].cell[1]): 'Модернизация объектов основных средств (ранее отражённых в РНУ-48 (отчёт 1), относящихся к "0" группе, а также срок полезного использования которых истёк)',
            (xml.row[5].cell[0]): '4',
            (xml.row[5].cell[1]): 'Прочие материальные расходы'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта

    for(int i=1; i<=4; i++) {
        // графа 3 строки i
        if (xml.row[headRowCount + i] != null) {
            dataRows[i - 1].summ = parseNumber(xml.row[headRowCount + i].cell[2].text(), rowOffset + headRowCount + i, 3 + colOffset, logger, false)
        } else {
            dataRows[i - 1].summ = null
        }
    }

    dataRowHelper.update(dataRows)
}