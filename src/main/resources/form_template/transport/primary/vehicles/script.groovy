package form_template.transport.primary.vehicles

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.DataRow

/**
 * Форма "Сведения о транспортных средствах, по которым уплачивается транспортный налог".
 *
 * @author ivildanov
 * @author Stanislav Yasinskiy
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
    case FormDataEvent.CREATE:
        checkUniq()
        copyData()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck()
        sort()
        // проставляем порядковый номер №пп
        setRowIndex()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

// Проверяет уникальность в отчётном периоде и вид
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Алгоритм копирования данных из форм предыдущего периода при создании формы
void copyData() {
    // TODO разобраться
}

//Добавить новую строку
void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit',
            'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемое поле')
    }
    dataRowHelper.insert(row, index)
    setRowIndex()
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    setRowIndex()
}

// Установка номера строки
void setRowIndex() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def index = 1
    for (def row in dataRows) {
        row.rowNumber = index++
    }
    dataRowHelper.update(dataRows);
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        // заполнение графы 3 на основе графы 2
        row.regionName = row.codeOKATO
        // заполнение графы 5 на основе графы 4
        row.tsType = row.tsTypeCode
    }
    dataRowHelper.update(dataRows);
}

def boolean logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Проверенные строки (4-ая провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()
    for (def row in dataRows) {

        // 1. Проверка на заполнение поля
        def errorMsg = ''
        ['rowNumber', 'codeOKATO', 'regionName', 'tsTypeCode', 'tsType', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate'].each { it ->
            def cell = row.getCell(it)
            if (cell.getValue() == null || ''.equals(cell.getValue())) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + cell.getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("В строке " + row.getIndex() + " не заполнены поля : $errorMsg.")
            return false;
        }

        // 2. Проверка на соответствие дат при постановке (снятии) с учёта
        if (!(row.regDateEnd == null || row.regDateEnd.compareTo(row.regDate) > 0)) {
            logger.error("Строка $row.rowNumber : Дата постановки (снятия) с учёта неверная!")
        }

        // 3. Проврека на наличие даты угона при указании даты возврата
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            logger.error("Строка $row.rowNumber : Не заполнено поле «Дата угона».")
        }

        // 4. Проверка на наличие в списке ТС строк, для которых графы codeOKATO, identNumber, regNumber одинаковы
        if (!checkedRows.contains(row)) {
            errorMsg = ''
            for (def rowIn in dataRows) {
                if (!checkedRows.contains(rowIn) && row != rowIn && row.codeOKATO.equals(rowIn.codeOKATO) && row.identNumber.equals(rowIn.identNumber) && row.regNumber.equals(rowIn.regNumber)) {
                    checkedRows.add(rowIn)
                    errorMsg = ", $rowIn.rowNumber"

                }
            }
            if (!''.equals(errorMsg)) {
                logger.error("Обнаружены строки $row.rowNumber $errorMsg, у которых Код ОКАТО = " + row.codeOKATO + ", " +
                        "Идентификационный номер = " + row.identNumber + ", " +
                        "Регистрационный знак = " + row.regNumber + " совпадают!")
            }
        }
        checkedRows.add(row)

        // 5. Проверка на наличие в списке ТС строк, период владения которых не пересекается с отчётным / налоговым периодом, к которому относится налоговая форма
        // TODO  regDate...regDateEnd не пересекается с отчётным/налоговым периодом

        // Проверки соответствия НСИ
        checkNSI(row.getCell('codeOKATO'), "Неверный код ОКАТО!", 3)
        if(row.regionName != row.codeOKATO){
            logger.error("Неверное наименование муниципального образования!")
        }
        checkNSI(row.getCell('tsTypeCode'), "'Неверный код вида ТС!", 42)
        if(row.tsType != row.tsTypeCode){
            logger.error("Неверный вид ТС!")
        }
        checkNSI(row.getCell('ecoClass'), "'Неверный код экологического класса!", 40)
        checkNSI(row.getCell('baseUnit'), "Неверный код ед. измерения мощности!", 12)
    }
}

// Проверка соответствия НСИ
void checkNSI(Cell cell, String msg, Long id) {
    if (cell.value != null && refBookService.getRecordData(id, cell.value))
        logger.error(msg)
}

void checkNSI() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {

        // Проверка ОКАТО
        if (!checkOkato(row.codeOKATO)) {
            logger.error("Неверный код ОКАТО!");
        }

        // 2. Проверка муниципального образования
        if (!checkRegionName(row.regionName)) {
            logger.error("Неверное наименование муниципального образования!");
        }

        // 3. Проверка кода вида ТС
        if (checkTsTypeCode(row.tsTypeCode)) {
            // 4. Проверка вида ТС
            if (!checkTsType(row.tsTypeCode, row.tsType)) {
                logger.error('Неверный вид ТС!')
            }
        } else {
            logger.error('Неверный код вида транспортного средства!')
        }

        // 5. Проверка кода экологического класса
        if (!checkEcoClass(row.ecoClass)) {
            logger.error('Неверный код экологического класса!')
        }

        // 6. Проверка кода ед. измерения мощности
        if (!checkBaseUnit(row.baseUnit)) {
            logger.error('Неверный код ед. измерения мощности!')
        }
    }
}

// сортировка ОКАТО - Муниципальное образование - Код вида ТС
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().sort { a, b ->
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
 * 1. Проверка ОКАТО
 * В справочнике «Коды ОКАТО» должна быть строка, для которой выполняется условие:
 * «графа 2» (поле «Код ОКАТО») текущей строки формы = «графа 1» (поле «Код ОКАТО») строки справочника
 */
boolean checkOkato(codeOKATO) {
    if (codeOKATO != null && getRefBookValue(3, codeOKATO, "OKATO") != null) {
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
def checkRegionName(regionName) {
    if (regionName != null && getRefBookValue(3, regionName, "NAME") != null) {
        return true;
    }
    return false
}

/**
 * Проверка кода вида ТС
 * В справочнике «Коды видов транспортных средств» должна быть строка, для которой выполняется условие:
 * «графа 4» (поле «Код вида ТС») текущей строки формы = «графа 3» (поле «Код вида ТС») строки справочника
 */
def checkTsTypeCode(tsTypeCode) {
    if (tsTypeCode != null && getRefBookValue(42, tsTypeCode, "CODE") != null) {
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

def checkTsType(tsTypeCode, tsType) {
    if (tsTypeCode != null && tsType != null && tsTypeCode == tsType) {
        return true;
    }
    return false
}

/**
 * Проверка кода экологического класса
 * В справочнике «Экологические классы» должна быть строка, для которой выполняется условие:
 * «графа 8» (поле «Экологический класс») текущей строки формы = «графа 1» (поле «Код экологического класса») строки справочника
 */
def checkEcoClass(ecoClass) {
    if (ecoClass != null && getRefBookValue(40, ecoClass, "CODE") != null) {
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
def checkBaseUnit(baseUnit) {
    if (baseUnit != null && getRefBookValue(12, baseUnit, "CODE") != null) {
        return true;
    }
    return false
}

/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias) {
    //logger.info("refBookID, recordId, alias = "+refBookID+", "+ recordId+", "+alias)
    def refDataProvider = refBookFactory.getDataProvider(refBookID)
    def record = refDataProvider.getRecordData(recordId)

    return record != null ? record.get(alias) : null;
}