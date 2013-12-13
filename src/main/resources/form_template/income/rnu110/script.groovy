package form_template.income.rnu110

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-110) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению имущества в аренду Взаимозависимым лицам и резидентам оффшорных зон цен, не соответствующих рыночному уровню
 * formTemplateId=396
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
}

// графа 1 - rowNumber
// графа 2 - personName
// графа 3 - date
// графа 4 - code
// графа 5 - baseNumber
// графа 6 - baseDate
// графа 7 - rent
// графа 8 - rentMarket
// графа 9 - factRentSum
// графа 10 - marketRentSum
// графа 11 - addRentSum

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'fix', 'personName', 'date', 'code', 'baseNumber', 'baseDate', 'rent', 'rentMarket',
        'factRentSum', 'marketRentSum', 'addRentSum']

// Редактируемые атрибуты
@Field
def editableColumns = ['personName', 'date', 'code', 'baseNumber', 'baseDate', 'rent', 'rentMarket', 'marketRentSum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = allColumns

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['rent', 'rentMarket', 'factRentSum', 'marketRentSum', 'addRentSum']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
    if (getRNU(316) == null) {
        throw new ServiceException(" Не найдены экземпляры «РНУ-4» за текущий отчетный период!")
    }
    if (getRNU(318) == null) {
        throw new ServiceException(" Не найдены экземпляры «РНУ-6» за текущий отчетный период!")
    }
}

def getRNU(def id) {
    def rnu = formDataService.find(id, formData.kind, formData.departmentId, formData.reportPeriodId)
    def data = rnu != null ? formDataService.getDataRowHelper(rnu) : null
    if (data != null) {
        def Map<String, Long> map = new HashMap<String, Long>()
        for (def rowOld : data.getAllCached()) {
            if (rowOld.getAlias() != null) {
                map.put(rowOld.fix.replace('Итого по КНУ ', ''), rowOld.sum)
            }
        }
        return map
    }
    return null
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (row in dataRows) {
            // графа 1
            row.rowNumber = ++index
            // графа 4
            // TODO КНУ из Приложения №4 , соответствующий операции, по которой производится начисление в налоговом учёте дохода.
            // row.code =
            // графа 9
            // TODO фактическая сумма дохода, подлежащая начислению исходя из ставки по договору, соответствующая сумме, отражённой в РНУ-4 (РНУ-6).
            row.factRentSum = 0
            // графа 11
            if (row.marketRentSum != null && row.factRentSum != null)
                row.addRentSum = row.marketRentSum - row.factRentSum
            else
                row.addRentSum = null
        }
        dataRowHelper.save(dataRows)
    }

    // посчитать итого по графе 2
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.personName
        }
        // если код расходы поменялся то создать новую строку "итого по "графа 2""
        if (tmp != row.personName) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по "Графа 2""
        if (i == dataRowHelper.getAllCached().size() - 1) {
            totalColumns.each {
                if (row.getCell(it).getValue() != null)
                    sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(row.personName, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            if (row.getCell(it).getValue() != null)
                sums[it] += row.getCell(it).getValue()
        }
        tmp = row.personName
    }

    // добавить "итого по "графа 2"" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }
}

// Логические проверки
def logicCheck() {
    def tmp
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (!dataRows.isEmpty()) {
        /** Дата начала отчетного периода. */
        tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
        def a = (tmp ? tmp.getTime() : null)

        /** Дата окончания отчетного периода. */
        tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def b = (tmp ? tmp.getTime() : null)

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : dataRows) {
            if (row.getAlias() != null) {
                hasTotal = true
                continue
            }
            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            // 1. Заполненность обязательных графов
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 2. Проверка на уникальность поля «№ пп»
            for (def rowB : dataRows) {
                if (!row.equals(rowB) && row.rowNumber == rowB.rowNumber) {
                    logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
                }
            }

            // 3. Проверка даты совершения операции и границ отчётного периода
            if (row.date < a || b < row.date) {
                logger.error(errorMsg + "Дата совершения операции вне границ отчётного периода!")
            }

            // 4. Арифметические проверки графы 11
            // графа 11
            if (row.marketRentSum != null && row.factRentSum != null && row.addRentSum != row.marketRentSum - row.factRentSum) {
                logger.error(errorMsg + "Неверно рассчитана графа \"Сумма доначисления арендной платы до рыночного уровня арендной ставки\"!")
            }

            // 5. Арифметическая проверка итоговых значений по Взаимозависимым  лицам (резидентам оффшорных зон)
            if (row.personName != null && !totalGroupsName.contains(row.personName)) {
                totalGroupsName.add(row.personName)
            }

            //Проверки соответствия НСИ
            checkNSI(28, row, 'code')
        }

        if (hasTotal) {
            // 5. Проверка итоговых значений по кодам классификации расхода
            for (def personName : totalGroupsName) {
                def totalRowAlias = 'total' + personName
                if (!checkAlias(dataRows, totalRowAlias)) {
                    logger.error("Итоговые значения по наименованию взаимозависимого лица (резидента оффшорной зоны) $personName не рассчитаны! Необходимо расчитать данные формы.")
                }
                def row = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), totalRowAlias)
                for (def alias : totalColumns) {
                    if (calcSumByCode(personName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по наименованию взаимозависимого лица (резидента оффшорной зоны) $personName рассчитаны неверно!")
                    }
                }
            }
        }
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверить существования строки по алиасу.
 *
 * @param list строки нф
 * @param rowAlias алиас
 * @return <b>true</b> - строка с указанным алиасом есть, иначе <b>false</b>
 */
def checkAlias(def list, def rowAlias) {
    if (rowAlias == null || rowAlias == "" || list == null || list.isEmpty()) {
        return false
    }
    for (def row : list) {
        if (row.getAlias() == rowAlias) {
            return true
        }
    }
    return false
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по ' + alias
    newRow.getCell('fix').colSpan = 6
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

/**
 * Посчитать сумму указанного графа для строк с общим наименованием взаимозависимого лица (резидента оффшорной зоны)
 *
 * @param personName Наименование взаимозависимого лица (резидента оффшорной зоны)
 * @param alias название графа
 */
def calcSumByCode(def personName, def alias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def sum = 0
    dataRowHelper.getAllCached().each { row ->
        if (row.getAlias() == null && row.personName == personName) {
            sum += row.getCell(alias).getValue() ?: 0
        }
    }
    return sum
}