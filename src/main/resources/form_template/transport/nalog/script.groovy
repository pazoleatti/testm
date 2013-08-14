import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.dao.script.TransportTaxDao

/**
 * Форма "Сведения о транспортных средствах, по которым уплачивается транспортный налог".
 * @author ivildanov
 *
 * todo задать соответствующим графам коды справочников ( сейчас их нет в системе )
 * todo при совпадении каких ячеек, можно считать, что строки идентичны
 */

/**
 * Графы
 * 1 № пп  -  rowNumber
 * 2 Код ОКАТО  -  codeOKATO
 * 3 Муниципальное образование, на территории которого зарегистрировано транспортное средство (ТС)  -  regionName
 * 4 Код вида ТС  -  tsTypeCode
 * 5 Вид ТС  -  tsType
 * 6 Идентификационный номер  -  identNumber
 * 7 Марка  -  model
 * 8 Экологический класс  -  ecoClass
 * 9 Регистрационный знак  -  regNumber
 * 10 Мощность (величина)  -  powerVal
 * 11 Мощность (ед. измерения)  -  baseUnit
 * 12 Год изготовления  -  year
 * 13 Регистрация (дата регистрации)  -  regDate
 * 14 Регистрация (дата снятия с регистрации)  -  regDateEnd
 * 15 Сведения об угоне (дата начала розыска ТС)  -  stealDateStart
 * 16 Сведения об угоне (дата возврата ТС)  -  stealDateEnd
 *
 * ['rowNumber', 'codeOKATO', 'regionName', 'tsTypeCode', 'tsType', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']
 */

switch (formDataEvent) {
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkBeforeCreate()
        break

// Инициирование Пользователем расчёта данных формы (нажатие на кнопку «Рассчитать»)
    case FormDataEvent.CALCULATE:
        fillForm()

        checkRequiredField()
        checkNSI()
        logicalChecks()

        sort()
        break

// Инициирование Пользователем проверки данных формы
    case FormDataEvent.CHECK:

        checkRequiredField()
        logicalChecks()
        checkNSI()

        break

    case FormDataEvent.ADD_ROW:
        // добавляем строку
        addRow()
        // проставляем порядковый номер №пп
        setRowIndex()
        break

    case FormDataEvent.DELETE_ROW:
        // удаляем строку
        deleteRow()
        // проставляем порядковый номер №пп
        setRowIndex()
        break

/**
 * Проверки первичных / консолидированных налоговых форма
 */

// Инициирование Пользователем перехода «Подготовить»     //..
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        // (Ramil Timerbaev) проверка производится в ядре
        // 2.	Логические проверки значений налоговой формы.
        //       logicalChecks()
        // 3.	Проверки соответствия НСИ.
        //       checkNSI()

        break

// Инициирование Пользователем  выполнение перехода в статус «Принята» из Подготовлена      //..
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        // (Ramil Timerbaev) проверка производится в ядре
        // 2.	Логические проверки значений налоговой формы.
        //       logicalChecks()
        // 3.	Проверки соответствия НСИ.
        //       checkNSI()

        break

// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:    //..
        // 1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        // (Ramil Timerbaev) проверка производится в ядре

        break

// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:    //..
        // (Ramil Timerbaev) проверка производится в ядре
        break

// после вернуть из принята в подготовлена
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_PREPARED:    //..
        // (Ramil Timerbaev) проверка производится в ядре
        break

//отменить принятие консолидированной формы
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:    //..
        // 1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        // (Ramil Timerbaev) проверка производится в ядре
        break

// обобщить (3.10.1)
    case FormDataEvent.COMPOSE:
        // 1.	Объединение строк (см. раздел 3.10.2).
        // 1.1.	Система должна перенести строки из форм-источников в форму-приемник.
        consolidation()
        //1.2.	Система должна выполнить расчет значений строк итогов (либо фиксированных строк).
        fillForm()
        //1.3.	Система должна выполнить сортировку строк.
        sort()
        //1.4.	Система должна заполнить значение графы «№ пп».
        setRowIndex()
        break
}

/**
 * Скрипт для добавления новой строки.
 */
void addRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(newRow)

    ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd'].each { column ->
        newRow.getCell(column).editable = true
        newRow.getCell(column).setStyleAlias("Редактируемое поле")
    }
}

/**
 * Установка номера строки.
 */
void setRowIndex() {
    def index = 0
    for (def row : formData.dataRows) {
        row.rowNumber = index + 1
        index += 1
    }
}

/**
 * Скрипт для проверки создания.
 */
void checkBeforeCreate() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

}

/**
 * Проверка обязательных полей.
 */
void checkRequiredField() {
    for (def row : formData.dataRows) {

        def errorMsg = ''

        // 1-7,9-13
        ['rowNumber', 'codeOKATO', 'regionName', 'tsTypeCode', 'tsType', 'identNumber', 'model', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate'].each { column ->
            if (row.getCell(column) != null && (row.getCell(column).getValue() == null || ''.equals(row.getCell(column).getValue()))) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(column).getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("В строке $row.rowNumber не заполнены поля : $errorMsg.")
        }
    }
}

/*
 * Скрипт для удаления строки.
 */
void deleteRow() {
    def row = currentDataRow
    if (row != null) {
        // удаление строки
        formData.deleteDataRow(row)
    }
}

/**
 * Скрипт логические проверки сводной формы.
 */
void logicalChecks() {
    for (def row : formData.dataRows) {

        // Проверка на соответствие дат при постановке (снятия) с учёта
        if (!(row.regDateEnd > row.regDate)) {
            logger.error("Строка $row.rowNumber : Дата постановки (снятия) с учёта неверная!")
        }
    }
}

/**
 * Скрипт для проверки соответствия НСИ.
 */
void checkNSI() {
    for (def row : formData.dataRows) {

        // 1. Проверка ОКАТО
        if (row.codeOKATO != null) {
            // TODO переписать проверку ОКАТО. ОКАТО переехал в версионный справочник.
            // if (!transportTaxDao.validateOkato(row.codeOKATO)) {
            //    logger.error('Неверный код ОКАТО!')
            //}
        }

        // 2. Проверка муниципального образования
        if (row.regionName != null) {
            // TODO переписать проверку
            // if (row.regionName != transportTaxDao.getRegionName(row.codeOKATO)) {
            //     logger.error('Неверное наименование муниципального образования!')
            // }
        }

        // 3. Проверка кода вида ТС
        if (row.tsTypeCode != null) {
            if (transportTaxDao.validateTransportTypeCode(row.tsTypeCode)) {

                // 4. Проверка вида ТС
                if (transportTaxDao.getTsTypeName(row.tsTypeCode) != row.tsType) {
                    logger.error('Неверный вид ТС!')
                }

            } else {
                logger.error('Неверный код вида транспортного средства!')
            }
        }

        // 5. Проверка кода экологического класса
        if (row.ecoClass != null) {
            if (!transportTaxDao.validateEcoClass(row.ecoClass)) {
                logger.error('Неверный код экологического класса!')
            }
        }

        // 6. Проверка кода ед. измерения мощности
        if (row.baseUnit != null) {
            if (!transportTaxDao.validateTaxBaseUnit(row.baseUnit)) {
                logger.error('Неверный код ед. измерения мощности!')
            }
        }
    }
}

/**
 * Алгоритмы заполнения полей формы
 */
void fillForm() {
    formData.dataRows.each { row ->

        // заполнение графы 3 на основе графы 2
        if (row.codeOKATO != null) {
            // TODO Переписать
            //row.regionName = transportTaxDao.getRegionName(row.codeOKATO)
        }

        // заполнение графы 5 на основе графы 4
        if (row.tsTypeCode != null) {
            row.tsType = transportTaxDao.getTsTypeName(row.tsTypeCode)
        }
    }
}

/**
 * Скрипт для сортировки.
 */
void sort() {
    // сортировка
    // 1 - ОКАТО
    // 2 - Муниципальное образование
    // 3 - Код вида ТС
    formData.dataRows.sort { a, b ->
        int val = (a.codeOKATO ?: "").compareTo(b.codeOKATO ?: "")
        if (val == 0) {
            val = (a.regionName ?: "").compareTo(b.regionName ?: "")

            if (val == 0) {
                val = (a.tsTypeCode ?: "").compareTo(b.tsTypeCode ?: "")
            }
        }
        return val
    }
}

/**
 * Консолидация.
 */
void consolidation() {

    // удаляем все строки
    formData.dataRows.clear()

    // скопировать строки из источников в данную форму
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {

                // todo 3.10.2.1 Обработка идентичных строк
                source.dataRows.each { row ->

                    if (isRowInDataRows(row)) {
                        logger.error('Идентичные строки')
                    } else {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * 	Скрипт для определения наличия идентичных строк в форме строке nrow
 *
 * @param nrow строка
 */
boolean isRowInDataRows(def nrow) {
    formData.dataRows.each { row ->
        if (row.equals(nrow)) {
            return true
        }
    }
    return false
}