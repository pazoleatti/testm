package form_template.transport.vehicles

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
/**
 * Форма "Сведения о транспортных средствах, по которым уплачивается транспортный налог".
 * @author ivildanov
 *
 *
 *
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

        if (checkRequiredField()){
            fillForm()
            checkNSI()
            logicalChecks()
            sort()
            // проставляем порядковый номер №пп
            setRowIndex()
            // сохраним данные
            getDataRowHelper().save(getDataRows())
        }

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
        //
        getDataRowHelper().save(getDataRows())
        break

    case FormDataEvent.DELETE_ROW:
        // удаляем строку
        deleteRow()
        // проставляем порядковый номер №пп
        setRowIndex()

        getDataRowHelper().save(getDataRows())
        break

    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        checkRequiredField()
        logicalChecks()
        checkNSI()

        break

// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:    //..
        // 1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        // (Ramil Timerbaev) проверка производится в ядре
        break

// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:    //..
        // 2.	Логические проверки значений налоговой формы.
        logicalChecks()
        // 3.	Проверки соответствия НСИ.
        checkNSI()
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
        getDataRowHelper().save(getDataRows())
        getDataRowHelper().commit()

        break
}

/**
 * Скрипт для добавления новой строки.
 */
void addRow() {
    def newRow = formData.createDataRow()

    ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd'].each { column ->
        newRow.getCell(column).editable = true
        newRow.getCell(column).setStyleAlias("Редактируемое поле")
    }

    def index = (currentDataRow != null ? currentDataRow.getIndex() : getDataRows().size())
    dataRowHelper.insert(newRow, index + 1)
}

/**
 * Установка номера строки.
 */
void setRowIndex() {
    def index = 1
    for (def row : getDataRows()) {
        row.rowNumber = index++
    }

}

/**
 * Скрипт для проверки создания.
 */
void checkBeforeCreate() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

}

/**
 * Проверка обязательных полей.
 */
def checkRequiredField() {
    for (def row : getDataRows()) {

        def errorMsg = ''

        // 1-7,9-13
        ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate'].each { column ->
            if (row.getCell(column) != null && (row.getCell(column).getValue() == null || ''.equals(row.getCell(column).getValue()))) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(column).getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("В строке "+row.getIndex()+" не заполнены поля : $errorMsg.")
            return false;
        }
    }
    return true
}

/*
 * Скрипт для удаления строки.
 */
void deleteRow() {
    def row = currentDataRow
    if (row != null) {
        // удаление строки
        getDataRowHelper().delete(row)
    }
}

/**
 * Скрипт логические проверки сводной формы.
 */
void logicalChecks() {
    for (def row : getDataRows()) {

        // Проверка на соответствие дат при постановке (снятия) с учёта
        if (!(row.regDateEnd == null || row.regDateEnd.compareTo(row.regDate) > 0)) {
            logger.error("Строка $row.rowNumber : Дата постановки (снятия) с учёта неверная!")
        }

        //Если «графа 16» заполнена, то Заполнена  «графа 15»
        if (row.stealDateEnd != null && row.stealDateStart == null){
            logger.error("Строка $row.rowNumber : Не заполнено поле «Дата угона».")
        }
    }
}

/**
 * Скрипт для проверки соответствия НСИ.
 */
void checkNSI() {
    for (def row : getDataRows()) {

        // Проверка ОКАТО
        if (!checkOkato(row.codeOKATO)){
            logger.error("Неверный код ОКАТО!");
        }

        // 2. Проверка муниципального образования
        if (!checkRegionName(row.regionName)){
            logger.error("Неверное наименование муниципального образования!!");
        }

        // 3. Проверка кода вида ТС
        if (checkTsTypeCode(row.tsTypeCode)){
            // 4. Проверка вида ТС
            if (!checkTsType(row.tsTypeCode, row.tsType)){
                logger.error('Неверный вид ТС!')
            }
        } else{
            logger.error('Неверный код вида транспортного средства!')
        }

        // 5. Проверка кода экологического класса
        if (!checkEcoClass(row.ecoClass)){
            logger.error('Неверный код экологического класса!')
        }

        // 6. Проверка кода ед. измерения мощности
        if (!checkBaseUnit(row.baseUnit)){
            logger.error('Неверный код ед. измерения мощности!')
        }
   }
}

/**
 * Алгоритмы заполнения полей формы
 */
void fillForm() {
    getDataRows().each { row ->

        // заполнение графы 3 на основе графы 2
        if (row.codeOKATO != null) {
            row.regionName = row.codeOKATO
        }

        // заполнение графы 5 на основе графы 4
        if (row.tsTypeCode != null) {
            row.tsType = row.tsTypeCode
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
    getDataRows().sort { a, b ->
        okatoA = getRefBookValue(3, a.codeOKATO, "OKATO")
        okatoB = getRefBookValue(3, b.codeOKATO, "OKATO")
        int val = okatoA.getStringValue().compareTo(okatoB.getStringValue())
        if (val == 0) {
            def regionA = getRefBookValue(3, a.regionName, "NAME")
            def regionB = getRefBookValue(3, b.regionName, "NAME")
            val = regionA.getStringValue().compareTo(regionB.getStringValue())

            if (val == 0) {
                def tsTypeCodeA = getRefBookValue(42, a.tsTypeCode, "CODE")
                def tsTypeCodeB = getRefBookValue(42, b.tsTypeCode, "CODE")
                val = tsTypeCodeA.getStringValue().compareTo(tsTypeCodeB.getStringValue())
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
    getDataRows().clear()

    // скопировать строки из источников в данную форму
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                int cnt = 1
                sourceDataRows.each { row ->
                    row.rowNumber = cnt++
                    dataRowHelper.insert(row, getDataRows().size() ?: 1)
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
    getDataRows().each { row ->
        if (row.equals(nrow)) {
            return true
        }
    }
    return false
}


/**
 * 1. Проверка ОКАТО
 * В справочнике «Коды ОКАТО» должна быть строка, для которой выполняется условие:
 * «графа 2» (поле «Код ОКАТО») текущей строки формы = «графа 1» (поле «Код ОКАТО») строки справочника
 */
boolean checkOkato(codeOKATO){
    if (codeOKATO != null && getRefBookValue(3, codeOKATO, "OKATO") != null){
            return true;
    }
    return false;
}

/**
 * Проверка муниципального образования
 * «Графа 3» (поле «Муниципальное образование, на территории которого зарегистрировано
 * транспортное средство (ТС)») текущей строки формы = «графа 2» (поле «Наименование»)
 * строки справочника «Коды ОКАТО» для указанного значения в «графе 2»
 * (поле «Код ОКАТО») текущей формы
 */
def checkRegionName(regionName){
    if (regionName != null && getRefBookValue(3, regionName, "NAME") != null){
        return true;
    }
    return false
}

/**
 * Проверка кода вида ТС
 * В справочнике «Коды видов транспортных средств» должна быть строка, для которой выполняется условие:
 * «графа 4» (поле «Код вида ТС») текущей строки формы = «графа 3» (поле «Код вида ТС») строки справочника
 */
def checkTsTypeCode(tsTypeCode){
    if (tsTypeCode != null && getRefBookValue(42, tsTypeCode, "CODE") != null){
        return true;
    }
    return false
}

/**
 * Проверка вида ТС
 *
 * «Графа 5» (поле «Вид ТС») текущей строки формы = «графа 1»
 * (поле «Наименование вида транспортного средства») строки справочника
 * «Коды видов транспортных средств» для указанного значения в «графе 4»
 * («Код вида ТС») текущей формы
 */

def checkTsType(tsTypeCode, tsType){
    if (tsTypeCode != null && tsType != null && tsTypeCode == tsType){
        return true;
    }
    return false
}

/**
 * Проверка кода экологического класса
 * В справочнике «Экологические классы» должна быть строка, для которой выполняется условие:
 * «графа 8» (поле «Экологический класс») текущей строки формы = «графа 1» (поле «Код экологического класса») строки справочника
 */
def checkEcoClass(ecoClass){
    if (ecoClass != null && getRefBookValue(40, ecoClass, "CODE") != null){
        return true;
    }
    return false
}

/**
 * В справочнике «Коды единиц измерения налоговой базы на основании ОКЕИ» должна быть строка, для которой выполняется условие:
 *  «графа 11» (поле «Мощности (ед. измерения)») текущей строки формы = «графа 1» (поле «Код единицы измерения») строки справочника
 *
 *  Проверка кода ед. измерения мощности
 */
def checkBaseUnit(baseUnit){
    if (baseUnit != null && getRefBookValue(12, baseUnit, "CODE") != null){
        return true;
    }
    return false
}


/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias){
    //logger.info("refBookID, recordId, alias = "+refBookID+", "+ recordId+", "+alias)
    def  refDataProvider = refBookFactory.getDataProvider(refBookID)
    def record = refDataProvider.getRecordData(recordId)

    return record != null ? record.get(alias) : null;
}


def getDataRows() {
    dataRowHelper.getAllCached()
}


def getDataRowHelper() {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    } else {
        throw new Exception("Ошибка получения dataRows")
    }
}
