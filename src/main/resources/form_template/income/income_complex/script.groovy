import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для
 * Форма "6.1.1	Сводная форма начисленных доходов уровня обособленного подразделения".  ("Доходы сложные")
 *
 * Версия ЧТЗ: 64
 *
 * @author vsergeev
 *
 * Графы:
 *
 * ********** 6.1.2 Сводная форма "Доходы, учитываемые в простых РНУ" уровня обособленного подразделения **********
 *
 * 1    incomeTypeId                КНУ
 * 2    incomeGroup                 Группа доходов
 * 3    incomeTypeByOperation       Вид дохода по операции
 * 4    accountNo                   Балансовый счёт по учёту дохода
 * 5    rnu6Field10Sum              РНУ-6 (графа 10) cумма
 * 6    rnu6Field12Accepted         сумма
 * 7    rnu6Field12PrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах по графе 10
 * 8    rnu4Field5Accepted          РНУ-4 (графа 5) сумма
 * 9    logicalCheck                Логическая проверка
 * 10   accountingRecords           Счёт бухгалтерского учёта
 * 11   opuSumByEnclosure2          в Приложении №5
 * 12   opuSumByTableD              в Таблице "Д"
 * 13   opuSumTotal                 в бухгалтерской отчётности
 * 14   difference                  Расхождение
 *
 * ********** 6.1.1	Сводная форма начисленных доходов уровня обособленного подразделения **********
 *
 * 1   incomeTypeId                 КНУ
 * 2   incomeGroup                  Группа доходов
 * 3   incomeTypeByOperation        Вид дохода по операциям
 * 4   incomeBuhSumAccountNumber    балансовый счёт по учёту дохода
 * 5   incomeBuhSumRnuSource        источник информации в РНУ
 * 6   incomeBuhSumAccepted         сумма
 * 7   incomeBuhSumPrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах
 * 8   incomeTaxSumRnuSource        источник информации в РНУ
 * 9   incomeTaxSumS                сумма
 * 10  rnuNo                        форма РНУ
 * 11  logicalCheck                 Логическая проверка
 * 12  accountingRecords            Счёт бухгалтерского учёта
 * 13  opuSumByEnclosure2           в Приложении №5
 * 14  opuSumByTableD               в Таблице "Д"
 * 15  opuSumTotal                  в бухгалтерской отчётности
 * 16  difference                   Расхождение
 */

if (formData.id != null) dataRowsHelper = formDataService.getDataRowHelper(formData)

switch (formDataEvent) {
// создать
    case FormDataEvent.CREATE :
        checkCreation()
        break
// расчитать
    case FormDataEvent.CALCULATE :
        checkAndCalc()
        break
// проверить
    case FormDataEvent.CHECK :
        checkAndCalc()
        break
    case FormDataEvent.COMPOSE:
        DataRowHelper form = formDataService.getDataRowHelper(formData)
        consolidationBank(form)
        break
// утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        break
// вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        checkAndCalc()
        checkDeclarationBankOnAcceptance()
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkDeclarationBankOnCancelAcceptance()
        break
// после принятия из утверждена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED :
        break
// после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

//def fill(){
//    dataRowsHelper.getAllCached().each { row ->
//        getRequiredColsAliases().each {
//            def cell = row.getCell(it)
//            if (cell.isEditable()) {
//                cell.setValue(1)
//            }
//        }
//    }
//}

def consolidationBank(DataRowHelper formTarget) {
    if (formTarget == null) {
        return
    }

    // очистить форму
    formTarget.getAllCached().each { row ->
        ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each { alias ->
            if (row.getCell(alias).isEditable()) {
                row.getCell(alias).setValue(0)
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null)
        }
        if (row.getAlias() in ['R30', 'R85']) {
            row.incomeTaxSumS = 0
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (DataRow<Cell> row : formDataService.getDataRowHelper(child).allCached) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = formTarget.getDataRow(formTarget.getAllCached(), row.getAlias())
                ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each {
                    if (row.getCell(it).getValue() != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    formTarget.save(formTarget.allCached)
    formTarget.commit()
}

/**
 * В рамках выполнения  логических проверок система должна осуществлять расчет значений вычисляемых ячеек 
 * контрольных столбцов, описанных в Табл. 5 раздела 6.1.1.7 как вычисляемые. (c) ЧТЗ
 */
def checkAndCalc() {
    if (checkRequiredFields()) {
        calcValues()
        calcTotal()
    }
}

def calcValues() {
    calc4to5()      //рассчет строк 4 и 5
    calc35to40()    //рассчет строк 35-40
}

/**
 * проверка заполнения обязательных полей
 */
def checkRequiredFields() {
    boolean isValid = true

    def requiredColsAliases = getRequiredColsAliases()

    def rnd = new Random()

    dataRowsHelper.getAllCached().each { dataRow ->
        for (def colAlias : requiredColsAliases) {
            cell = dataRow.getCell(colAlias)
            if (cell.editable && isBlankOrNull(cell.getValue())) {
                isValid = false
                def rowNumber = dataRowsHelper.getAllCached().indexOf(dataRow) + 1
                logger.error("Строка $rowNumber: обязательные поля не заполнены!")
                break
            }
        }
    }

    return isValid
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 35-40
 */
def calc35to40() {
    getRowsAliasesFor35to40().each { rowAlias ->
        def dataRow = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(),rowAlias)

        final income101Data = getIncome101Data(dataRow)

        if (income101Data == null || income101Data.isEmpty()) {     //Нет данных об оборотной ведомости
            return
        }

        dataRow.with{
//          графа  14
            opuSumByTableD = getOpuSumByTableDFor35to40(dataRow, income101Data)

//          графа  15
            opuSumTotal = getOpuSumTotalFor35to40(dataRow, income101Data)
//          графа  16
            difference = getDifferenceFor35to40(dataRow)
        }

    }
    dataRowsHelper.save(dataRowsHelper.getAllCached())
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 35-40
 * Графа 16
 */
def getDifferenceFor35to40(def dataRow) {
    dataRow.with {
        return incomeTaxSumS - (opuSumTotal - opuSumByTableD)      //«графа 16» = «графа 9» - ( «графа 15» – «графа 14»)
    }
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 35-40
 * Графа 15
 */
def getOpuSumTotalFor35to40(def dataRow, def income101Data) {
    if (income101Data != null && ! income101Data.isEmpty()) {
        return income101Data.sum { income101Row ->
            if (income101Data.account == dataRow.accountingRecords) {
                return income101Row.outcomeDebetRemains
            } else {
                return 0
            }
        }
    } else {
        return 0
    }
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 35-40
 * Графа 14
 */
def getOpuSumByTableDFor35to40(def dataRow, def income101Data){
    if (income101Data != null && ! income101Data.isEmpty()) {
        return income101Data.sum { income101Row ->
            if (income101Data.account == dataRow.accountingRecords) {
                return (! isBlankOrNull(income101Row.incomeDebetRemains)) ? income101Row.incomeDebetRemains : 0
            } else {
                return 0
            }
        }
    } else {
        return 0
    }
}

/**
 * возвращает данные из Оборотной Ведомости за период, для которого сформирована текущая форма
 */
def getIncome101Data(def dataRow) {
    def account = dataRow.accountingRecords
    def reportPeriodId = formData.reportPeriodId

    // Справочник 50 - "Оборотная ведомость (Форма 0409101-СБ)"
    def refDataProvider = refBookFactory.getDataProvider(50)
    def records = refDataProvider.getRecords(reportPeriodService.getEndDate(formData.reportPeriodId).time, null,  "ACCOUNT = '" + account + "'", null)
    return records.getRecords()
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 */
def calc4to5() {
    getRowsAliasesFor4to5().each { rowAlias ->
        def dataRow = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(),rowAlias)

        final summaryIncomeSimpleFormHelper = getSummaryIncomeSimpleFormHelper()
        final income102Data = getIncome102Data(dataRow)

        dataRow.with {
//          графа  11
            logicalCheck = getLogicalCheckFor4to5(dataRow)
//          графа  13
            opuSumByEnclosure2 = getOpuSumByEnclosure2For4to5(dataRow)
//          графа  14
            opuSumByTableD = getOpuSumByTableDFor4to5(dataRow, summaryIncomeSimpleFormHelper)
//          графа  15
            opuSumTotal = getOpuSumTotalFor4to5(dataRow, income102Data)
//          графа  16
            difference = getDifferenceFor4to5(dataRow)
        }

    }
    dataRowsHelper.save(dataRowsHelper.getAllCached())
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 * Графа 16
 */
def getDifferenceFor4to5(def dataRow) {
    dataRow.with {
        return (opuSumByEnclosure2 + opuSumByTableD) - opuSumTotal     //«графа 16» = («графа 13» + «графа 14») – «графа 15»
    }
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 * Графа 15
 */
def getOpuSumTotalFor4to5(def dataRow, def income102Data) {
    if (income102Data != null && ! income102Data.isEmpty()) {
        return income102Data.sum { income102DataRow ->
            if (income102DataRow.opuCode == dataRow.accountingRecords) {
                return (! isBlankOrNull(income102DataRow.totalSum)) ? 0 : income102DataRow.totalSum
            }

            return 0
        }
    } else {
        return 0
    }
}

/**
 * получаем отчет о прибылях и убытках на период, для которого сформирована текущая форма
 */
def getIncome102Data(def dataRow){
    def account = dataRow.accountingRecords
    def reportPeriodId = formData.reportPeriodId

    return income102Dao.getIncome102(reportPeriodId, account)
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 * Графа 14
 */
def getOpuSumByTableDFor4to5(def dataRow, def summaryIncomeSimpleFormHelper) {
    if (summaryIncomeSimpleFormHelper != null && summaryIncomeSimpleFormHelper.getAllCached()!=null && !summaryIncomeSimpleFormHelper.getAllCached().isEmpty()) {
        def sum = 0;
        for(def row : summaryIncomeSimpleFormHelper.getAllCached()){
            if (row.accountNo == dataRow.incomeBuhSumAccountNumber && row.rnu4Field5Accepted!=null) {
                sum += row.rnu4Field5Accepted
            }
        }
        return sum;
    } else {
        return 0
    }
}

/**
 * получаем formData для формы 6.1.2 «Сводная форма " Доходы, учитываемые в простых РНУ" уровня обособленного подразделения»
 * SBRFACCTAX-2749 Доходы сложные - за какой период брать данные по форме "доходы простые"
 */
def getSummaryIncomeSimpleFormHelper() {
    def formId = 301
    def formDataKind = FormDataKind.SUMMARY
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId
    // TODO (Aydar Kadyrgulov) Проверить, существует ли за этот же период простая форма. если нет, то вывести сообщение.
    def summaryIncomeSimpleFormData = formDataService.find(formId, formDataKind, departmentId, reportPeriodId)
    def summaryIncomeSimpleDataRowsHelper = null

    if (summaryIncomeSimpleFormData != null && summaryIncomeSimpleFormData.id != null) summaryIncomeSimpleDataRowsHelper = formDataService.getDataRowHelper(summaryIncomeSimpleFormData)
    /*if (summaryIncomeSimpleDataRowsHelper == null || summaryIncomeSimpleDataRowsHelper.getAllCached().isEmpty()) {
        logger.error('Нет информации в отчёте Доходы простые')
    }*/

    return summaryIncomeSimpleDataRowsHelper
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 * Графа 13
 */
def getOpuSumByEnclosure2For4to5(def dataRow) {
    return dataRow.incomeBuhSumAccepted
}

/**
 * Расчет контрольных граф Сводной формы начисленных доходов
 * № строки 4-5
 * Графа 11
 */
def getLogicalCheckFor4to5(def dataRow) {
    dataRow.with {
        def sum = incomeTaxSumS - (incomeBuhSumAccepted - incomeBuhSumPrevTaxPeriod)
        return  (sum < 0) ? 'Требуется уточнение' : getBigDecimalAsString(sum)
    }
}

/**
 * возвращает BigDecimal в виде строки, округленной до 2 знаков после запятой
 */
def getBigDecimalAsString(BigDecimal sum) {
    return sum.setScale(2, java.math.RoundingMode.HALF_UP).toString()
}

/**
 * Табл. 6 Алгоритмы заполнения вычисляемых полей фиксированных строк
 * Сводной формы начисленных доходов уровня обособленного подразделения
 */
def calcTotal() {
    final colName = getColAliasForTotal()

    final firstTotalRowAlias = getFirstTotalRowAlias()
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(),firstTotalRowAlias)[colName] = calcTotal(colName, getRowsAliasesForFirstControlSum())

    final secondTotalRowAlias = getSecondTotalRowAlias()
    dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(),secondTotalRowAlias)[colName] = calcTotal(colName, getRowsAliasesForSecondControlSum())
    dataRowsHelper.save(dataRowsHelper.getAllCached())
}

/**
 * подсчитываем сумму для столбца colName в строках rowsAliases
 */
def calcTotal(def colName, def rowsAliases) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    return rowsAliases.sum { rowAlias ->
        def tmp = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(),rowAlias)[colName]
        return (tmp == null) ? 0 : tmp
    }
}

/**
 * возвращает алиас столбца, по которому нужно подвести итоги
 */
def getColAliasForTotal() {
    return 'incomeTaxSumS'
}

/**
 * возвращает список алиасов столбцов, в которых есть ячейки, доступные для редактирования
 */
def getRequiredColsAliases() {
    return ['incomeBuhSumRnuSource', 'incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS', 'rnuNo']
}

/**
 * возвращает алиасы строк 4-5 для расчета контрольных граф Сводной формы начисленных доходов
 */
def getRowsAliasesFor4to5() {
    return ['R4', 'R5']
}

/**
 * возвращает алиасы строк 35-30 для расчета контрольных граф Сводной формы начисленных доходов
 */
def getRowsAliasesFor35to40() {
    return ['R35', 'R36', 'R37', 'R38', 'R39', 'R40']
}

/**
 * возвращает список строк, по которым надо подвести итоги для первой итоговой строки
 */
def getRowsAliasesForFirstControlSum() {
    return ['R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15', 'R16', 'R17',
            'R18', 'R19', 'R20', 'R21', 'R22', 'R23', 'R24', 'R25', 'R26', 'R27', 'R28', 'R29']
}

/**
 * возвращает список строк, по которым надо подвести итоги для второй итоговой строки
 */
def getRowsAliasesForSecondControlSum() {
    return ['R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42', 'R43', 'R44', 'R45', 'R46',
            'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'R54', 'R55', 'R56', 'R57', 'R58', 'R59', 'R60', 'R61',
            'R62', 'R63', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76',
            'R77', 'R78', 'R79', 'R80', 'R81', 'R82', 'R83', 'R84']
}

/**
 * возвращает алиас для первой строки итогов
 */
def getFirstTotalRowAlias() {
    return 'R30'
}

/**
 * возвращает алиас для второй строки итогов
 */
def getSecondTotalRowAlias() {
    return 'R85'
}

/********************************   ОБЩИЕ ФУНКЦИИ   ********************************/

/**
 * false, если в строке нет символов или строка null
 * true, если в строке есть символы
 */
boolean isBlankOrNull(value) {
    return (value == null || value.equals(''))
}

/***************** ПРОВЕРКИ ИЗМЕНЕНИЯ СОСТОЯНИЯ ФОРМЫ *****************/

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 21.02.2013 12:30
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
    if (isTerBank()) {
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
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (isTerBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}