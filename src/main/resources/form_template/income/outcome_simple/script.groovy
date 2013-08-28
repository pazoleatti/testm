package form_template.income.outcome_simple

import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import javax.script.ScriptException
import java.text.SimpleDateFormat

/**
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @version 46
 */

switch (formDataEvent) {
// создать
    case FormDataEvent.CREATE:
        checkCreation()
        break
// расчитать
    case FormDataEvent.CALCULATE:
        checkAndCalc()
        save(getData(formData))
        break
// обобщить
    case FormDataEvent.COMPOSE:
        isBank() ? consolidationBank() : consolidationSummary()
        break
// проверить
    case FormDataEvent.CHECK:
        checkAndCalc()
        //testFillForm()
        break
// проверить при переводе в утверждена
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        checkAndCalc()
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        checkAndCalc()
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkAndCalc()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            checkDeclarationBankOnAcceptance()
        }
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        checkDeclarationBankOnCancelAcceptance()
        break
// после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED:
        checkDeclarationBankOnCancelAcceptance()
        break
}

// графа  1 - consumptionTypeId
// графа  2 - consumptionGroup
// графа  3 - consumptionTypeByOperation
// графа  4 - consumptionAccountNumber
// графа  5 - rnu7Field10Sum
// графа  6 - rnu7Field12Accepted
// графа  7 - rnu7Field12PrevTaxPeriod
// графа  8 - rnu5Field5Accepted
// графа  9 - logicalCheck
// графа 10 - accountingRecords
// графа 11 - opuSumByEnclosure2
// графа 12 - opuSumByTableP
// графа 13 - opuSumTotal
// графа 14 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    calculationBasicSum()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        calculationControlGraphs()
    }
}

/**
 * Вычисление сумм.
 */
void calculationBasicSum() {
    def data = getData(formData)
    if (data == null) {
        return
    }

    /*
     * Проверка объязательных полей
     */
    def requiredColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']
    for (def row : getRows(data)) {
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчет сумм
     */
    def row50001 = getRowByAlias(data, 'R107')
    def row50002 = getRowByAlias(data, 'R212')

    // суммы для графы 5..8
    ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        row50001.getCell(alias).setValue(getSum(alias, 'R2', 'R106'))
        row50002.getCell(alias).setValue(getSum(alias, 'R109', 'R211'))
    }

    calculationControlGraphs()
}

/**
 * Скрипт для заполнения контрольных полей.
 *
 * В текущей таблице нет 10й графы, следственно
 * нужно учесть что графы > 10 считаются "-1"
 *
 * @author rtimerbaev
 * @since 21.03.2013 13:00
 * @version 14 05.03.2013
 */
void calculationControlGraphs() {
    def data = getData(formData)
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataComplex = getFormDataComplex()
    def income102NotFound = []
    for (def row : getRows(data)) {
        // исключить итоговые строки
        if (row.getAlias() in ['R107', 'R212', 'R1', 'R108']) {
            continue
        }
        //Строки 213-217 расчет 8-й графы
        if (row.getAlias() in ['R213', 'R214', 'R215', 'R216', 'R217']) {
            def formDataRNU14 = getFormDataRNU14()
            if (formDataRNU14 != null) {
                for (def rowRNU14 : getData(formDataRNU14).getAllCached()) {
                    if (row.consumptionTypeId == rowRNU14.knu) {
                        row.rnu5Field5Accepted = rowRNU14.overApprovedNprms
                    }
                }
            }
            continue
        }
        if (!isEmpty(row.rnu7Field10Sum) && !isEmpty(row.rnu7Field12Accepted) &&
                !isEmpty(row.rnu7Field12PrevTaxPeriod)) {
            // графы 9 = ОКРУГЛ(«графа 5» - («графа 6» - «графа 7»); 2)
            tmp = round(row.rnu7Field10Sum - (row.rnu7Field12Accepted - row.rnu7Field12PrevTaxPeriod), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }
        // графа 11
        row.opuSumByEnclosure2 = getSumFromComplex(formDataComplex,
                'consumptionBuhSumAccountNumber', 'consumptionBuhSumAccepted', row.consumptionAccountNumber)
        //logger.info("alias = %s graph11 = %s", row.getAlias(), row.opuSumByEnclosure2.toString())
        // графа 12
        if (row.getAlias() in ['R105', 'R209']) {
            tmp = calcColumn6(['R105', 'R209'])
        } else if (row.getAlias() in ['R106', 'R211']) {
            tmp = calcColumn6(['R106', 'R211'])
        } else if (row.getAlias() in ['R104', 'R208']) {
            tmp = calcColumn6(['R104', 'R208'])
        } else {
            tmp = row.rnu5Field5Accepted
        }
        row.opuSumByTableP = tmp

        // графа 13
        def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords)
        if (income102 == null || income102.isEmpty()) {
            income102NotFound += getIndex(row) + 1
            tmp = 0
        } else {
            tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
        }
        row.opuSumTotal = tmp

        // графа 14
        row.difference = (getValue(row.opuSumByEnclosure2) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }
}

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:40
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
}

/**
 * Проверки наличия декларации Банка при принятии нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Принятие налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Скрипт для консолидации данных из сводных расходов простых уровня ОП в сводные уровня банка.
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:50
 */
def consolidationBank() {
    def data = getData(formData)
    if (data == null) {
        return
    }

    // очистить форму
    data.getAllCached().each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }

    def needCalc = false

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 304) {
            needCalc = true
            for (def row : child.getDataRows()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = data.getDataRow(data.getAllCached(), row.getAlias())
                ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each {
                    if (row.getCell(it).getValue() != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    if (needCalc) {
        checkAndCalc()
    }
    data.commit()
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
}

/**
 * Консолидация данных из рну-7 и рну-5 в сводные расходы простые уровня ОП.
 */
def consolidationSummary() {
    def data = getData(formData)
    if (data == null) {
        return
    }
    // очистить форму
    getRows(data).each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }

    // справочник 27 "Классификатор расходов Сбербанка России для целей налогового учёта"
    def refDataProvider = refBookFactory.getDataProvider(27)

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def dataOld = null
    if (reportPeriod != null && reportPeriod.order != 1) {
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        if (prevReportPeriod != null) {
            def formDataOld = formDataService.find(formData.getFormType().getId(), formData.getKind(), formDataDepartment.id, prevReportPeriod.getId())
            dataOld = getData(formDataOld)
            if (dataOld != null) {
                // данные за предыдущий отчетный период рну-7
                ([3, 12] + (15..35) + (38..49) + (51..54) + (56..58) + (62..78) + (91..95) + (98..101) +
                        (103..106) + (181..183) + (190..194) + [199, 204, 205] + (207..211)).each {
                    def alias = 'R' + it
                    def row = getRowByAlias(data, alias)

                    // графа 5
                    row.rnu7Field10Sum = getRowByAlias(dataOld, alias).rnu7Field10Sum
                    // графа 6
                    row.rnu7Field12Accepted = getRowByAlias(dataOld, alias).rnu7Field12Accepted
                    // графа 7
                    row.rnu7Field12PrevTaxPeriod = getRowByAlias(dataOld, alias).rnu7Field12PrevTaxPeriod
                }
                // данные за предыдущий отчетный период рну-5
                ((2..106) + (109..211)).each {
                    def alias = 'R' + it
                    def row = getRowByAlias(data, alias)

                    // графа 8
                    row.rnu5Field5Accepted = getRowByAlias(dataOld, alias).rnu5Field5Accepted
                }
            }
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            switch (child.formType.id) {
            // рну 7
                case 311:
                    ([3, 12] + (15..35) + (38..49) + (51..54) + (56..58) + (62..78) + (91..95) + (98..101) +
                            (103..106) + (181..183) + (190..194) + [199, 204, 205] + (207..211)).each {
                        def alias = 'R' + it
                        def row = getRowByAlias(data, alias)
                        def recordId = getRecordId(refDataProvider, row.consumptionTypeId)

                        // сумма графы 10 рну-7
                        def sum10 = 0
                        // сумма графы 12 рну-7
                        def sum12 = 0
                        // сумма графы 10 рну-7 для графы 7
                        def sum = 0
                        if (recordId != null) {
                            sum10 = getSumForColumn5or6or8(child, recordId, row.consumptionAccountNumber, 'code', 'balance', 'taxAccountingRuble')
                            sum12 = getSumForColumn5or6or8(child, recordId, row.consumptionAccountNumber, 'code', 'balance', 'ruble')
                            sum = getSumForColumn7(child, recordId, row.consumptionAccountNumber)
                        }

                        // графа 5
                        row.rnu7Field10Sum = (row.rnu7Field10Sum ?: 0) + sum10
                        // графа 6
                        row.rnu7Field12Accepted = (row.rnu7Field12Accepted ?: 0) + sum12
                        // графа 7
                        row.rnu7Field12PrevTaxPeriod = (row.rnu7Field12PrevTaxPeriod ?: 0) + sum
                    }
                    break

            // рну 5
                case 317:
                    ((2..106) + (109..211)).each {
                        def alias = 'R' + it
                        def row = getRowByAlias(data, alias)
                        def recordId = getRecordId(refDataProvider, row.consumptionTypeId)

                        // сумма графы 5 рну-5
                        def sum5 = 0
                        if (recordId != null) {
                            sum5 = getSumForColumn5or6or8(child, recordId, row.consumptionAccountNumber, 'code', 'number', 'sum')
                        }

                        // графа 8
                        row.rnu5Field5Accepted = (row.rnu5Field5Accepted ?: 0) + sum5
                    }
                    break
            }
        }
    }

    save(data) // TODO (Ramil Timerbaev) возможно это надо убрать, но без этого при отладке данные не сохранялись
    data.commit()
    logger.info('Формирование сводной формы уровня обособленного подразделения прошло успешно.')
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * Проверка на террбанк.
 */
def isTerBank() {
    boolean isTerBank = false
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isTerBank = true
        }
    }
    return isTerBank
}

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def data = getData(formData)
    def from = data.getDataRowIndex(data.getAll(), rowFromAlias) + 1
    def to = data.getDataRowIndex(data.getAll(), rowToAlias) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, data.getAll(), new ColumnRange(columnAlias, from, to))
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
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
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
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getData(formData).getAllCached().indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить сумму значений из расходов сложных.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromComplex(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : getData(data).getAllCached()) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

/**
 * Получить значение для графы 12. Сумма значении графы 6 указанных строк
 *
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcColumn6(def aliasRows) {
    def sum = 0
    def data = getData(formData)
    aliasRows.each { alias ->
        sum += data.getDataRow(data.getAllCached(), alias).rnu7Field12Accepted
    }
    return sum
}

/**
 * Получить данные формы "расходы сложные" (id = 303)
 */
def getFormDataComplex() {
    return formDataService.find(303, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить данные формы РНУ-14 (id = 321)
 */
def getFormDataRNU14() {
    return formDataService.find(321, FormDataKind.PRIMARY, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

// TODO (Ramil Timerbaev) убрать если не надо
/**
 * Функция заполнения тестовыми данными
 */
def testFillForm() {
    def data = getData(formData)
    data.getAllCached().each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            cell = row.getCell(alias)
            if (cell.isEditable() && (cell.getValue() == null || cell.getValue() == '')) {
                cell.setValue(1);
            }
        }
    }

    data.save(data.getAllCached());
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
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
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
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Проверить наличие итоговой строки.
 *
 * @param data данные нф (helper)
 */
def hasTotal(def data) {
    for (def row : getRows(data)) {
        if (row.getAlias() == 'total') {
            return true
        }
    }
    return false
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
 * Получить идентификатор записи справочника 27 "Классификатор расходов Сбербанка России для целей налогового учёта"
 * по значению атрибута "Код налогового учёта".
 *
 * @param refDataProvider справочник
 * @param value код налогового учёта
 */
def getRecordId(def refDataProvider, def value) {
    def records = refDataProvider.getRecords(new Date(), null, "CODE = '" + value + "'", null)
    if (records != null && !records.getRecords().isEmpty()) {
        def record = records.getRecords().getAt(0)
        if (record != null) {
            return getValue(record, 'record_id')
        }
    }
    return null
}

/**
 * Получить значение атрибута строки справочника.

 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE:
            return value.getDateValue()
        case RefBookAttributeType.NUMBER:
            return value.getNumberValue()
        case RefBookAttributeType.STRING:
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE:
            return value.getReferenceValue()
    }
    return null
}

/**
 * Получить сумму строк графы нф соответствующих двум условиям.
 *
 * @param form нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 * @param value2 значение приемника для второго условия
 * @param alias1 алиас графы для первого условия
 * @param alias2 алиас графы для второго условия
 * @param resultAlias алиас графы суммирования
 */
def getSumForColumn5or6or8(def form, def value1, def value2, def alias1, def alias2, def resultAlias) {
    def sum = 0
    def data = getData(form)
    if (data == null) {
        return sum
    }
    def tmpValueA = value2.replace('.', '')
    def tmpValueB
    getRows(data).each { row ->
        tmpValueB = (row.getCell(alias2).getValue() ?
            refBookService.getStringValue(27, row.getCell(alias2).getValue(), 'NUMBER') : null)

        if (value1 == row.getCell(alias1).getValue() && tmpValueA == tmpValueB) {
            sum += (row.getCell(resultAlias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Получить сумму строк графы нф соответствующих двум условиям.
 *
 * @param form нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 * @param value2 значение приемника для второго условия
 */
def getSumForColumn7(def form, def value1, def value2) {
    def sum = 0
    def data = getData(form)
    if (data == null) {
        return sum
    }
    SimpleDateFormat formatY = new SimpleDateFormat('yyyy')
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def tmpValueA = value2.replace('.', '')
    def tmpValueB
    getRows(data).each { row ->
        tmpValueB = (row.balance ? row.balance.replace('.', '') : null)
        if (value1 == row.code && tmpValueA == tmpValueB &&
                row.ruble != null && row.ruble != 0) {
            // получить (дату - 3 года)
            dateFrom = format.parse('01.01.' + (Integer.valueOf(formatY.format(row.docDate)) - 3))
            // получить налоговые и отчетные периоды за найденый промежуток времени [(дата - 3года)..дата]
            def taxPeriods = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, dateFrom, row.docDate)
            taxPeriods.each { taxPeriod ->
                def id = taxPeriod.getId()
                def reportPeriods = reportPeriodService.listByTaxPeriod(id)
                reportPeriods.each { reportPeriod ->
                    // в каждой форме относящейся к этим периодам ищем соответствующие строки и суммируем по 10 графе
                    def f = formDataService.find(form.getFormType().getId(), FormDataKind.PRIMARY, form.getDepartmentId(), reportPeriod.getId())
                    def d = getData(f)
                    if (d != null) {
                        getRows(d).each { r ->
                            // графа  4 - balance
                            // графа  5 - docNumber
                            // графа  6 - docDate
                            // графа 10 - taxAccountingRuble
                            if (r.balance == row.balance && r.docNumber == row.docNumber && r.docDate == row.docDate) {
                                sum += (r.taxAccountingRuble ?: 0)
                            }
                        }
                    }
                }
            }
        }
    }
    return sum
}