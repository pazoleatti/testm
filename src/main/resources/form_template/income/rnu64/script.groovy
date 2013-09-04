import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

import java.text.SimpleDateFormat

/**
 * РНУ-64
 * @author auldanov
 *
 * @version 55
 *
 * Описание столбцов
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки Справочник
 * 4. dealingNumber - Номер сделки
 * 5. bondKind - Вид ценных бумаг
 * 6. costs - Затраты (руб.коп.)
 */

/**
 * Выполнение действий по событиям
 *
 */
switch (formDataEvent) {
// Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
    case FormDataEvent.CHECK:
        //1. Логические проверки значений налоговой формы
        logicalCheck()
        //2. Проверки соответствия НСИ
        //NCICheck()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        checkBeforeCreate()
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        //2.    Логические проверки значений налоговой.
        // ?? logicalCheck()
        //3.    Проверки соответствия НСИ.
        break
// Инициирование Пользователем перехода «Подготовить»
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        //2.    Логические проверки значений налоговой формы.
        logicalCheck()
        //3.    Проверки соответствия НСИ.
        break

// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        // 1.   Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        checkOnCancelAcceptance()

        break

// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break

// после вернуть из принята в подготовлена
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_PREPARED:
        acceptance()
        break

// Инициирование Пользователем  выполнение перехода в статус «Принята» из Подготовлена
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        checkOnPrepareOrAcceptance('Принятие')
        // 2.   Логические проверки значений налоговой формы.
        //       logicalChecks()
        // 3.   Проверки соответствия НСИ.
        //       checkNSI()

        break

// отменить принятие
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        // 1.   Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        checkOnCancelAcceptance()
        break

// Событие добавить строку
    case FormDataEvent.ADD_ROW:
        addNewRow()
        setRowIndex()
        break

// событие удалить строку
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        setRowIndex()
        break

    case FormDataEvent.CALCULATE:
        form = getData(formData)
        deleteAllStatic(form)
        sort()
        fillForm()
        logicalCheck()
        break

    case FormDataEvent.COMPOSE:
        consolidation()
        deleteAllStatic(form)
        sort()
        fillForm()
        logicalCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
    // загрузка xml
    case FormDataEvent.IMPORT :
        importData()
        break
}

/**
 * Добавление новой строки
 */
def addNewRow() {
    def data = getData(formData)
    DataRow<Cell> newRow = formData.createDataRow()
    ['date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удаление строки
 *
 * @author Ivildanov
 * вынес пересчет №пп в отдельную процедуру
 */
def deleteRow() {
    // def row = (DataRow)additionalParameter
    def row = currentDataRow
    if (!isTotalRow(row)) {
        // удаление строки
        getData(formData).delete(row)
    }
}

/**
 * Заполнение полей формы
 * 6.1.2.3  Алгоритмы заполнения полей формы
 */
def fillForm() {
    def data = getData(formData)

    def i = 1
    getRows(data).each { row ->
        if (isTotalRow(row)) {
            row.number = i++
        }
    }
    data.save(getRows(data))

    // добавляем строки итого
    def newRowQuarter = formData.createDataRow()
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowQuarter.getCell("fix").setColSpan(4)
    newRowQuarter.fix = "Итого за текущий квартал"
    // 6 графа Содержит сумму значений "графы 6" для всех строк данной таблицы, за исключением итоговых строк («Итого за текущий квартал», «Итого за текущий отчетный (налоговый) период»)
    newRowQuarter.costs = getQuarterTotal()
    newRowQuarter.setAlias("totalQuarter")
    setTotalStyle(newRowQuarter)
    data.insert(newRowQuarter, getRows(data).size() + 1)

    // строка Итого за текущий отчетный (налоговый) период
    def newRowTotal = formData.createDataRow()
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowTotal.getCell("fix").setColSpan(4)
    newRowTotal.fix = "Итого за текущий отчетный (налоговый) период"
    newRowTotal.costs = getTotalValue()
    newRowTotal.setAlias("total")
    setTotalStyle(newRowTotal)
    data.insert(newRowTotal, getRows(data).size() + 1)
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic(DataRowHelper form) {
    List<DataRow<Cell>> forDelete = new ArrayList<>();
    for (row in form.allCached) {
        if (row.getAlias() != null) {
            forDelete.add(row)
        }
    }
    for (row in forDelete) {
        form.delete(row)
    }
}

/**
 * Логические проверки
 */
def logicalCheck() {
    def data = getData(formData)
    def totalQuarterRow = null
    def totalRow = null
    getRows(data).each { row ->
        // Обязательность заполнения поля графы (с 1 по 6); фатальная; Поле ”Наименование поля” не заполнено!
        if (!isTotalRow(row)) {
            def requiredColumns = ['number', 'date', 'part', 'dealingNumber', 'bondKind', 'costs']
            if(!checkRequiredColumns(row, requiredColumns)){
                return false;
            }

            reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
            reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
            // Проверка даты совершения операции и границ отчетного периода; фатальная; Дата совершения операции вне границ отчетного периода!
            if (!isTotalRow(row) && row.date != null && !(
            (reportPeriodStartDate.getTime().equals(row.date) || row.date.after(reportPeriodStartDate.getTime())) &&
                    (reportPeriodEndDate.getTime().equals(row.date) || row.date.before(reportPeriodEndDate.getTime()))
            )) {
                logger.error("В строке \"№ пп\" равной " + row.number + " дата совершения операции вне границ отчетного периода!")
            }

            getRows(data).each { rowItem ->
                if (!isTotalRow(row) && row.number == rowItem.number && !row.equals(rowItem)) {
                    logger.error("В строке \"№ пп\" равной " + row.number + " нарушена уникальность номера по порядку!")
                }
            }

            // Проверка на нулевые значения; фатальная; Все суммы по операции нулевые!
            if (row.costs == 0) {
                logger.error("В строке \"№ пп\" равной " + row.number + " все суммы по операции нулевые!")
            }
            // Проверка актуальности поля «Часть сделки»; не фатальная;
            if (row.part != null && getPart(row.part) == null) {
                logger.warn("В строке \"№ пп\" равной " + row.number + " поле ”Часть сделки” указано неверно!")
            }
        } else if (isMainTotalRow(row)) {
            totalRow = row
        } else if (isQuarterTotal(row)) {
            totalQuarterRow = row
        }
    }

    // проверка на наличие итоговых строк, иначе будет ошибка
    if (totalQuarterRow != null || totalRow != null) {
        // Проверка итоговых значений за текущий квартал; фатальная; Итоговые значения за текущий квартал рассчитаны неверно!
        if (totalQuarterRow != null && totalQuarterRow.costs != getQuarterTotal()) {
            logger.error('Итоговые значения за текущий квартал рассчитаны неверно!')
        }

        // Проверка итоговых значений за текущий отчётный (налоговый) период; фатальная; Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!
        if (totalRow != null && totalRow.costs != getTotalValue()) {
            logger.error('Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!')
        }
    }

}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 */
def checkRequiredColumns(def row, def columns) {
    def data = getData(formData)
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
        def index = getRows(data).indexOf(row) + 1
        def errorMsg = colNames.join(', ')
        logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
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
 * Скрипт для проверки создания.
 */
void checkBeforeCreate() {
    // отчётный период
    //def reportPeriod = reportPeriodService.get()

    //проверка периода ввода остатков
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

}

/**
 * Проверка является ли строка итововой за текущий квартал
 */
def isQuarterTotal(row) {
    row.getAlias() == 'totalQuarter'
}

/**
 * Проверка является ли строка итововой (последняя строка)
 */
def isMainTotalRow(row) {
    row.getAlias() == 'total'
}

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. за квартал, либо основной)
 */
def isTotalRow(row) {
    return row.getAlias() == 'total' || row.getAlias() == 'totalQuarter'
}

// функция возвращает итоговые значения за текущий квартал
def getQuarterTotal() {
    def data = getData(formData)
    def row6val = 0
    getRows(data).each { row ->
        if (!isTotalRow(row)) {
            row6val += row.costs ?: 0
        }
    }
    row6val
}

// Функция возвращает итоговые значения за текущий отчётный (налоговый) период
def getTotalValue() {
    def data = getData(formData)
    quarterRow = data.getDataRow(getRows(data), 'totalQuarter')
    // возьмем форму за предыдущий отчетный период
    def prevQuarter = quarterService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevQuarter != null) {
        prevQuarterFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevQuarter.id);

        if (prevQuarterFormData != null && prevQuarterFormData.state == WorkflowState.ACCEPTED) {
            prevQuarterData = getData(prevQuarterFormData)
            def prevQuarterTotalRow = prevQuarterData.getDataRow(getRows(prevQuarterData), "total")
            return quarterRow.costs + prevQuarterTotalRow.costs
        } else {
            //  Если предыдущей формы нет (либо она не принята)  то B = 0
            return quarterRow.costs
        }

    } else {
        return quarterRow.costs
    }
}

/**
 * Установка номера строки.
 *
 * @author Ivildanov
 */
void setRowIndex() {
    def data = getData(formData)
    def i = 1;
    getRows(data).each { rowItem ->
        if (!isTotalRow(rowItem)) {
            rowItem.number = i++
        }
    }
    data.save(getRows(data))
}

/**
 * Скрипт для сортировки.
 *
 * @author Ivildanov
 */
void sort() {
    data = getData(formData)
    if (data!=null && !data.getAllCached().isEmpty()) {
        data.getAllCached().sort({ DataRow a, DataRow b ->
            def aTime = a.date!=null?(a.date as Date).time:null
            def bTime = b.date!=null?(b.date as Date).time:null
            if (aTime == bTime) {
                def aNumber = a.dealingNumber!=null?Integer.valueOf(a.dealingNumber as String):null;
                def bNumber = b.dealingNumber!=null?Integer.valueOf(b.dealingNumber as String):null;
                return aNumber <=> bNumber
            }
            return aTime <=> bTime
        })
    }
}

/**
 * Для перевода сводной налогой формы в статус "принят".
 */
void acceptance() {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.PRIMARY).each()
            {
                formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
            }
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
 * Проверки при переходе "Отменить принятие" в подготовлена.
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            if (formData.getKind().getId() == 1) { // если форма первичная
                logger.error("Нельзя отменить принятие налоговой формы, так как уже «Утверждена» или «Принята» консолидированная налоговая форма.")
            } else {    // если форма консолидированая
                logger.error("Нельзя отменить принятие налоговой формы, так как уже «Утверждена» или «Принята» консолидированная налоговая форма вышестоящего уровня.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    getRows(data).clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size() + 1)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
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
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    def cached = data.getAllCached()
    return cached
}

/**
 * Получить код части сделки
 */
def getPart(def part) {
    return refBookService.getNumberValue(60, part, 'CODE');
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'fix', 'date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '' || !fileName.contains('.xml')) {
        return
    }

    def is = ImportInputStream
    if (is == null) {
        return
    }

    def xmlString = importService.getData(is, fileName)
    if (xmlString == null || xmlString == '') {
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }

    // сохранить начальное состояние формы
    def data = getData(formData)
    def rowsOld = getRows(data)
    try {
        // добавить данные в форму
        addData(xml)

        // расчитать и проверить
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicalCheck()
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
    }
    // откатить загрузку если есть ошибки
    if (logger.containsLevel(LogLevel.ERROR)) {
        data.clear()
        data.insert(rowsOld, 1)
    } else {
        logger.info('Данные загружены')
    }
    data.commit()
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
void addData(def xml) {
    def tmp
    def indexRow = 0
    def newRows = []
    def index
    def refDataProvider = refBookFactory.getDataProvider(60)

    // TODO (Aydar Kadyrgulov)  Проверка корректности данных
    for (def row : xml.exemplar.table.detail.record) {
        indexRow++
        index = 0

        def newRow = getNewRow()

        // графа 1
        newRow.number = getNumber(row.field[index].@value.text())
        index++

        // графа 2
        newRow.date = getDate(row.field[index].@value.text())
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        tmp = null
        if (row.field[index].@value.text() != null &&
                row.field[index].@value.text().trim() != '') {
            def records = refDataProvider.getRecords(new Date(), null, "CODE = '" + row.field[index].@value.text() + "'", null);
            if (records != null && !records.getRecords().isEmpty()) {
                tmp = records.getRecords().get(0).get('record_id').getNumberValue()
            }
        }
        newRow.part = tmp
        index++

        // графа 4
        newRow.dealingNumber = row.field[index].@value.text()
        index++

        // графа 5
        newRow.bondKind = row.field[index].@value.text()
        index++

        // графа 6
        newRow.costs = getNumber(row.field[index].@value.text())

        newRows.add(newRow)
    }


    // проверка итоговых данных
    if (xml.exemplar.table.total.record.field.size() > 0 && !newRows.isEmpty()) {
        def totalRow = formData.createDataRow()

        totalRow.costs = 0

        newRows.each { row ->
            totalRow.costs += (row.costs != null ? row.costs : 0)
        }

        for (def row : xml.exemplar.table.total.record) {
            // графа 6
            if (totalRow.costs != getNumber(row.field[5].@value.text())) {
                logger.error('Итоговые значения неправильные.')
                return
            }
        }
    }
    def data = getData(formData)
    data.clear()
    data.insert(newRows, 1)
    data.commit()
}

/**
 * Получить новую строку с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

    // графа 1..10
    ['date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (isEmpty(value)) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Расчет полностью
 */
void calc() {
    form = getData(formData)
    deleteAllStatic(form)
    sort()
    fillForm()
}