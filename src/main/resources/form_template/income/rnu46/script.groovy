/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение значений за предыдущий месяц, за предыдущие месяцы
 *		- проверка уникальности инвентарного номера
 *
 * @author rtimerbaev
 */


import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        acceptance()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - rowNumber
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    def data = data
    def size = getRows(data).size()
    def index = currentDataRow != null ? (currentDataRow.getIndex()+1) : (size == 0 ? 1 : (size+1))
    // графа 2..7, 9, 17..19
    ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed', 'usefulLifeWithUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    data.insert(newRow, index)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    data.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = data
    def rows = getRows(data)
    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2..7, 9, 17..19)
    def requiredColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

    for (def row : rows) {
        if (row.monthsUsed >= getTermAttribute(row.usefulLife)) {
            requiredColumns.add('usefulLifeWithUsed')
        }
        if (!checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
        if (requiredColumns.contains('usefulLifeWithUsed')) {
            requiredColumns.remove('usefulLifeWithUsed')
        }
    }

    /*
     * Расчеты.
     */

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')
    def check17 = format.parse('01.01.2006')
    // последнее число предыдущего месяца
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    def lastDayPrevMonth = (endDate ? endDate.getTime() : null)

    /** Дата начала отчетного периода. */
    def tmpDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def rpStartDate = (tmpDate ? tmpDate.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmpDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    def rpEndDate = (tmpDate ? tmpDate.getTime() : null)

    def tmp
    rows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 8
        // TODO (Ramil Timerbaev) спросить у аналитика
        if (row.monthsUsed < getTermAttribute(row.usefulLife)) {
            if (row.specCoef > 0) {
                row.usefulLifeWithUsed = roundTo(((getTermAttribute(row.usefulLife) - row.monthsUsed) / row.specCoef), 0)
            } else {
                row.usefulLifeWithUsed = roundTo((getTermAttribute(row.usefulLife) - row.monthsUsed), 0)
            }
        }
        logger.info('графа 8 = ' + row.usefulLifeWithUsed)

        // графа 10
        tmp = 0
        if (row.amortGroup in ['1', '2', '8', '9', '10']) {
            tmp = row.cost * 10
        } else if (row.amortGroup in ('3'..'7')) {
            tmp = row.cost * 30
        } else if (row.exploitationStart < check17) {
            tmp = 0
        }
        row.cost10perMonth = roundTo(tmp, 2)

        // графа 11
        // TODO (Ramil Timerbaev) getFromOld() = «Графа 11» РНУ-46 за предыдущий месяц
/*        boolean isJanuary = false // TODO (Aydar Kadyrgulov) сделать проверку "Если форма создается за январь". пока можем проверить только отчетный период
        if (isJanuary) {
            row.cost10perTaxPeriod = row.cost10perMonth
        } else {
            row.cost10perTaxPeriod = row.cost10perMonth + getFromOld()
        }*/

        // графа 12
        // TODO (Aydar Kadyrgulov) getFromOld() = «Графа 12» (РНУ-46 за предыдущий месяц)
        if (rpStartDate < row.exploitationStart && row.exploitationStart < rpEndDate) {
            row.cost10perExploitation = row.cost10perMonth
        } else {
            row.cost10perExploitation =  row.cost10perMonth + getFromOld()
        }

        // графа 13
        if (row.usefulLifeWithUsed != 0) {
            tmp = (1 / row.usefulLifeWithUsed) * 100
        }
        row.amortNorm = roundTo(tmp, 0)

        // графа 14
        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        if (row.usefullLifeEnd > lastDay2001) {
            // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
            tmp = (row.cost?:0 - row.cost10perExploitation?:0 - row.amortExploitation?:0) / (row.usefullLifeEnd - lastDayPrevMonth)
        } else {
            tmp = row.cost / 84
        }
        row.amortMonth = roundTo(tmp, 2)

        // графа 11, 15, 16
        if (isFirstMonth()) {
            row.cost10perTaxPeriod = row.cost10perMonth
            row.amortTaxPeriod = row.amortMonth
            row.amortExploitation = row.amortMonth
        } else {
            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
            row.cost10perTaxPeriod = getFromOld() + row.cost10perMonth

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
            row.amortTaxPeriod = getFromOld() + row.amortMonth

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
            row.amortExploitation = getFromOld() + row.amortMonth
        }

// графа 1  - rowNumber
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd

    }
    data.save(getRows(data))
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка амортизационной группы (графа 5)
    if (false) {
        logger.error('Амортизационная группа не существует!')
        return false
    }

    // 2. Проверка срока полезного использования (графа 6)
    if (false) {
        logger.error('Срок полезного использования указан неверно!')
        return false
    }
    return true
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = data
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')

    // список проверяемых столбцов (графа 1..18)
    def columns = ['rowNumber', 'invNumber', 'name', 'cost', 'amortGroup',
            'usefulLife', 'monthsUsed', 'usefulLifeWithUsed', 'specCoef',
            'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
            'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation',
            'exploitationStart', 'usefullLifeEnd', 'rentEnd']

    // последнее число предыдущего месяца
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def lastDayPrevMonth = (tmp ? tmp.getTime() : null)

    /** Дата начала отчетного периода. */
    def tmpDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def rpStartDate = (tmpDate ? tmpDate.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmpDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    def rpEndDate = (tmpDate ? tmpDate.getTime() : null)


    // 4. Проверки существования необходимых экземпляров форм
    if(getFromOld() == null) {
        logger.error('Отсутствуют данные за прошлые отчетные периоды!')
        return false
    }

    for (def row : getRows(data)) {
        // 1. Обязательность заполнения поля (графа 1..18)
        if (!checkRequiredColumns(row, columns, useLog)) {
            return
        }

        for (def rowB : getRows(data)) {
            // 2. Проверка на уникальность поля «№ пп»
            if(!row.equals(rowB) && row.rowNumber ==rowB.rowNumber){
                logger.error("В строке $row.rowNumber нарушена уникальность номера по порядку!")
                return false
            }
            // 3. Проверка на уникальность поля «инвентарный номер»
            if(!row.equals(rowB) && row.inventoryNumber ==rowB.inventoryNumber){
                logger.error("В строке $row.rowNumber нарушена уникальность инвентарного номера!")
                return false
            }
        }
    }

    for (def row : getRows(data)) {

         // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            logger.error('Все суммы по операции нулевые!')
            return false
        }

        // 6. Проверка суммы расходов в виде капитальных вложений с начала года
        if (row.cost10perTaxPeriod >= row.cost10perMonth &&
                // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
                row.cost10perTaxPeriod == row.cost10perTaxPeriod + getFromOld() &&
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 10 всех предыдущих месяцев (текущего года)
                row.cost10perTaxPeriod == getFromOld()

                // TODO (Aydar Kadyrgulov) удалить && false
                && false
        ) {
            logger.error('Неверная сумма расходов в виде капитальных вложений с начала года!')
            return false
        }

        // 7. Проверка суммы начисленной амортизации с начала года
        if (row.amortTaxPeriod < row.amortMonth ||
                // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
                row.amortTaxPeriod != row.cost10perTaxPeriod + getFromOld() ||
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 14 всех предыдущих месяцев текущего года
                row.amortTaxPeriod != getFromOld()) {
            logger.error('Неверная сумма начисленной амортизации с начала года!')
            return false
        }

        // 8. Арифметические проверки расчета неитоговых графы
        // Графы 8, 10-16, 18 всех строк формы содержат значения, рассчитанные согласно алгоритмам расчета граф 8, 10-16, 18 из раздела 6.3.4.1

        // Арифметическая проверка графы 8
        // TODO (Aydar Kadyrgulov)
        if (row.specCoef < 0) {
            tmp = (getTermAttribute(row.usefulLife) - row.monthsUsed) / row.specCoef
        } else {
            tmp = 0 // TODO (Ramil Timerbaev) не описано в чтз
        }
        if (row.usefulLifeWithUsed != roundTo(tmp, 0)) {
            logger.warn('Неверное значение графы «Срок полезного использования с учётом срока эксплуатации предыдущими собственниками (арендодателями, ссудодателями) либо установленный самостоятельно, (мес.)»!')
        }

        // Арифметическая проверка графы 10
        tmp = 0
        if (row.amortGroup in ['1', '2', '8', '9', '10']) {
            tmp = row.cost * 0.1
        } else if (row.amortGroup in ('3'..'7')) {
            tmp = row.cost * 0.3
        }
        if (row.cost10perMonth != roundTo(tmp, 2)) {
            logger.error('Неверное значение графы «10%% (30%%) от первоначальной стоимости, включаемые в расходы.За месяц»!')
            return false
        }

        // Арифметическая проверка графы 11
        if (isFirstMonth()) {
            tmp = row.cost10perMonth

            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
        } else {
            tmp = getFromOld() + row.cost10perMonth
        }
        if (row.cost10perTaxPeriod != tmp) {
            logger.error('Неверное значение графы «10%% (30%%) от первоначальной стоимости, включаемые в расходы.с начала налогового периода»!')
            return false
        }

        // Арифметическая проверка графы 12
        if (row.exploitationStart > rpStartDate && row.exploitationStart < rpEndDate) {
            if (row.cost10perExploitation != row.cost10perMonth) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа «10% (30%) от первоначальной стоимости, включаемые в расходы. С даты ввода в эксплуатацию»")
                return false
            }
        } else {
            // TODO (Ramil Timerbaev) getFromOld() = 12 графа предыдущего месяца
            if (row.cost10perExploitation != row.cost10perMonth + getFromOld()) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа «10% (30%) от первоначальной стоимости, включаемые в расходы. С даты ввода в эксплуатацию»")
                return false
            }
        }

        // Арифметическая проверка графы 13
        if (row.usefulLifeWithUsed != 0) {
            tmp = (1 / row.usefulLifeWithUsed) * 100
        } else if (row.usefulLifeWithUsed == 0) {
            tmp = 0 // TODO (Ramil Timerbaev) уточнить
        }
        if (row.amortNorm != roundTo(tmp, 0)) {
            logger.warn('Неверное значение графы «Норма амортизации (процентов в мес.)»!')
        }

        // Арифметическая проверка графы 14
        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        if (row.usefullLifeEnd > lastDay2001) {
            // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
            tmp = (row.cost - row.cost10perExploitation - row.amortExploitation) / (row.usefullLifeEnd - lastDayPrevMonth)
        } else if (row.usefullLifeEnd <= lastDay2001) {
            tmp = row.cost / 84
        }
        // TODO (Ramil Timerbaev) убрать && false
        if (row.amortMonth != roundTo(tmp, 2) && false) {
            logger.warn('Неверно рассчитана графа «Сумма начисленной амортизации.за месяц»!')
        }

        // 12. Арифметическая проверка графы 15
        if (isFirstMonth()) {
            tmp = row.amortMonth

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
        } else {
            tmp = getFromOld() + row.amortMonth
        }
        if (row.amortTaxPeriod != tmp) {
            logger.warn('Неверное значение графы «Сумма начисленной амортизации.с начала налогового периода»!')
        }

        // 13. Арифметическая проверка графы 16
        if (isFirstMonth()) {
            tmp = row.amortMonth

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
        } else {
            tmp = getFromOld() + row.amortMonth
        }
        if (row.amortExploitation != tmp) {
            logger.warn('Неверное значение графы «Сумма начисленной амортизации.с даты ввода в эксплуатацию»!')
        }
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    data.insert(row, getRows(data).size()+1)
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Получить значение из предыдущего месяца.
 */
def getFromOld() {
    // TODO (Ramil Timerbaev)
    // пока блокируется http://jira.aplana.com/browse/SBRFACCTAX-4515
    /*
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-25 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    */
    return 0
}

/**
 * Первый ли это месяц (январь)
 */
def isFirstMonth() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    if (reportPeriod != null && reportPeriod.getOrder() == 1) {
        return true
    }
    return false
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(data).indexOf(row)+1
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

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
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
 * Получить данные формы.
 *
 * @param formData форма
 */
def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

def DataRowHelper getData(){
    return getData(formData)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int round) {
    if (value != null) {
        return value.setScale(round, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Получить атрибут 645 - "Срок полезного использования (месяцев)" справочник 71 - "Амортизационные группы".
 *
 * @param id идентификатор записи справочника
 */
def getTermAttribute(def id) {
    return refBookService.getNumberValue(71, id, 'TERM')
}