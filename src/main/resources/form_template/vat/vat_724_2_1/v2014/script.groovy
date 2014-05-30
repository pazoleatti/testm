package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (724.2.1) Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом
 * налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория
 * Российской Федерации, а также суммы оплаты, частичной оплаты в счет предстоящих поставок (выполнения работ,
 * оказания услуг), длительность производственного цикла изготовления которых составляет свыше шести месяцев
 *
 * formTemplateId=601
 *
 * TODO:
 *      - нет справочнкика «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ»
 *              http://jira.aplana.com/browse/SBRFACCTAX-7396
 *      - расчет графы 4 и 5 не доделан и недопроверен из за справочника «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ»
 *
 * @author Stanislav Yasinskiy
 */

// графа 1 - rowNum         № пп
// графа 2 - code           Код операции
// графа 3 - name           Наименование операции  -
// графа 4 - realizeCost    Стоимость реализованных (переданных) товаров (работ, услуг) без НДС
// графа 5 - obtainCost     Стоимость приобретенных товаров  (работа, услуг), не облагаемых НДС

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevCalcCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        prevCalcCheck()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        prevCalcCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        prevCalcCheck()
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

// Автозаполняемые атрибуты (графа 4, 5)
@Field
def autoFillColumns = ['realizeCost', 'obtainCost']

// Проверяемые на пустые значения атрибуты (группа 1..5)
@Field
def nonEmptyColumns = ['rowNum', 'code', 'name', 'realizeCost', 'obtainCost']

// Поля, для которых подсчитываются итоговые значения (графа 4, 5)
@Field
def totalColumns = ['realizeCost', 'obtainCost']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Cправочник «Отчет о прибылях и убытках (Форма 0409102-СБ)»
@Field
def income102Data = null

@Field
def dateFormat = "dd.MM.yyyy"

@Field
def calcRowAlias4 = ['R1', 'R2', 'R3', 'R4', 'R7', 'R8', 'R9', 'R10', 'R16']

@Field
def calcRowAlias5 = ['R7', 'R8']

//// Кастомные методы

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }

        // графа 4
        row.realizeCost = (row.getAlias() in calcRowAlias4 ? calc4(row) : row.realizeCost)

        // графа 5
        row.obtainCost = (row.getAlias() in calcRowAlias5 ? calc5(row) : null)
    }

    // подсчет итогов
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        itog.getCell(alias).setValue(itogValues[alias], itog.getIndex())
    }
    dataRowHelper.save(dataRows);
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }

        // 1. Проверка заполнения граф (по графе 5 обязательны тока строки 7 и 8)
        def columns = (row.getAlias() in calcRowAlias5 ? nonEmptyColumns : nonEmptyColumns - 'obtainCost')
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // 2. Проверка итоговых значений
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        if (itog.getCell(alias).value != itogValues[alias]) {
            logger.error(WRONG_TOTAL, getColumnName(itog, alias))
        }
    }
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

// Получение данных из справочника «Отчет о прибылях и убытках» для текужего подразделения и отчетного периода
def getIncome102Data() {
    if (income102Data == null) {
        def filter = "REPORT_PERIOD_ID = ${formData.reportPeriodId} AND DEPARTMENT_ID = ${formData.departmentId}"
        income102Data = refBookFactory.getDataProvider(52L)?.getRecords(getReportPeriodEndDate(), null, filter, null)
    }
    return income102Data
}

// Проверка наличия необходимых записей в справочнике «Отчет о прибылях и убытках»
void checkIncome102() {
    // Наличие экземпляра Отчета о прибылях и убытках подразделения и периода, для которых сформирована текущая форма
    if (getIncome102Data() == null) {
        throw new ServiceException("Экземпляр Отчета о прибылях и убытках за период " +
                "${getReportPeriodStartDate().format(dateFormat)} - ${getReportPeriodEndDate().format(dateFormat)} " +
                "не существует (отсутствуют данные для расчета)!")
    }
}

void prevCalcCheck() {

    // 1. Проверка превышения разрядности граф - сделано в ядре
    // сделано в ядре

    // 2. Проверка наличия экземпляра «Отчета о прибылях и убытках» по соответствующему подразделению за соответствующий налоговый период
    checkIncome102()

    // 3. Проверка наличия символов ОПУ в Экземпляре Отчета о прибылях и убытках, необходимых для заполнения «Графы 4»	Экземпляр «Отчета о прибылях и убытках» подразделения и периода, для которых сформирована текущая форма, содержит записи по всем символам ОПУ, необходимым для заполнения «Графы 4» на основе справочника «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» 	1	Строка <Номер строки>: Экземпляр Отчета о прибылях и убытках за период <Дата начала отчетного периода> - <Дата окончания отчётного периода> не содержит записей для заполнения графы 4 по следующим символам ОПУ: «Перечень символов ОПУ через запятую»! Расчеты не могут быть выполнены.
    // 4. Проверка наличия символов ОПУ в Экземпляре Отчета о прибылях и убытках, необходимых для заполнения «Графы 5»	Экземпляр «Отчета о прибылях и убытках» подразделения и периода, для которых сформирована текущая форма, содержит записи по всем символам ОПУ, необходимым для заполнения «Графы 5» на основе справочника «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» 	1	Строка <Номер строки>: Экземпляр Отчета о прибылях и убытках за период <Дата начала отчетного периода> - <Дата окончания отчётного периода> не содержит записей для заполнения графы 5 по следующим символам ОПУ: «Перечень символов ОПУ через запятую»! Расчеты не могут быть выполнены.
    // эти проверки происходят при расчетах в методе getOpuCodes

    // 5. Проверка наличия соответствия кода операций символам ОПУ для графы 4	В справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» существует запись для «Графы 2» и «Графы 4» по строкам 1-4, 7-10, 16	1	Строка <Номера строк>: В справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» нет данных для заполнения графы 4! Расчеты не могут быть выполнены.
    // 6. Проверка наличия соответствия кода операций символам ОПУ для графы 5	В справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» существует запись для «Графы 2» и «Графы 5» по строкам 7 и 8	1	Строка <Номера строк>: В справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» нет данных для заполнения графы 5! Расчеты не могут быть выполнены.
    // эти проверки происходят при расчетах в методе getSumByOpuCodes
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.VAT) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.realizeCost = (row.realizeCost ?: 0) + (srcRow.realizeCost ?: 0)
                    row.obtainCost = (row.obtainCost ?: 0) + (srcRow.obtainCost ?: 0)
                }
            }
        }
    }

    dataRowHelper.update(dataRows)
}

def calc4(def row) {
    return calc4or5(row, 4)
}

def calc5(def row) {
    return calc4or5(row, 5)
}

/**
 * Посчитать значение для графы 4 или 5.
 *
 * @param row строка
 * @param columnFlag признак графы: 0 – Графа 4, 1 – Графа 5
 */
def calc4or5(def row, def columnNumber) {
    // список кодов ОПУ из справочника
    // TODO (Ramil Timerbaev) когда будет готов справочник - раскомментировать
    // def opuCodes = getOpuCodes(row.code, row.getIndex(), columnNumber)
    // сумма кодов ОПУ из отчета 102
    // def sum = getSumByOpuCodes(opuCodes, row.getIndex(), columnNumber)
    def sum = 182 // TODO (Ramil Timerbaev) костыль
    return roundValue(sum, 2)
}

/**
 * Получить список кодов ОПУ.
 *
 * @param code код операции
 * @param index номер строки
 * @param columnFlag номер графы (4 или 5)
 */
def getOpuCodes(def code, def index, def columnNumber) {
    // признак графы: 0 – Графа 4, 1 – Графа 5
    def columnFlag = (columnNumber == 4 ? 0 : 1)
    // TODO (Ramil Timerbaev) еще не готов справочник «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ»
    // потом поправить фильтр и id справочника
    def filter = "(Код операции или столбец 1 справочника) = $code AND (Графа налоговой формы 724.2.1 или столбец 2 справочника) = $columnFlag"
    def records = refBookFactory.getDataProvider(00L)?.getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records == null || records.isEmpty()) {
        // условия выполнения расчетов
        // 5, 6. Проверка наличия соответствия кода операций символам ОПУ для графы 4/5
        throw new ServiceException("Строка $index: В справочнике «%s» нет данных для заполнения графы $columnNumber! Расчеты не могут быть выполнены.",
                refBookFactory.get(00L).name)
    }
    def opuCodes = []
    records.each { record ->
        opuCodes.add(record?.NAME?.value)
    }

    return opuCodes
}

/**
 * Посчитать сумму по кодам ОПУ.
 *
 * @param opuCodes список кодов ОПУ
 */
def getSumByOpuCodes(def opuCodes, def index, def columnNumber) {
    def tmp = BigDecimal.ZERO
    def hasData = false
    for (def income102Row : getIncome102Data()) {
        if (income102Row?.OPU_CODE?.value in opuCodes) {
            tmp += (income102Row?.TOTAL_SUM?.value ?: 0)
            hasData = true
        }
    }
    if (!hasData) {
        // условия выполнения расчетов
        // 3, 4. Проверка наличия символов ОПУ в Экземпляре Отчета о прибылях и убытках, необходимых для заполнения «Графы 4/5»
        def start = getReportPeriodStartDate().format(dateFormat)
        def end = getReportPeriodEndDate().format(dateFormat)
        throw new ServiceException("Строка $index: Экземпляр Отчета о прибылях и убытках за период $start - $end не содержит записей " +
                "для заполнения графы $columnNumber по следующим символам ОПУ: «${opuCodes.join(', ')}»! Расчеты не могут быть выполнены.")
    }
    return tmp
}

def calcItog(def dataRows) {
    def itogValues = [:]
    totalColumns.each {alias ->
        itogValues[alias] = roundValue(0)
    }
    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        totalColumns.each { alias ->
            itogValues[alias] += roundValue(row.getCell(alias).value ?: 0)
        }
    }
    return itogValues
}

def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}