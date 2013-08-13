import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * РНУ-64
 * @author auldanov
 *
 * @version 55
 *
 * Описание столбцов
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки
 * 4. dealingNumber - Номер сделки
 * 5. bondKind - Вид ценных бумаг
 * 6. costs - Затраты (руб.коп.)
 */

/**
 * Выполнение действий по событиям
 *
 */
switch (formDataEvent){
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
        fillForm()
        logicalCheck()

        sort()
        break

    case FormDataEvent.COMPOSE:
        consolidation()
        fillForm()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        fillForm()
        logicalCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
}



/**
 * Добавление новой строки
 */
def addNewRow(){
    def data = getData(formData)
    DataRow<Cell> newRow = formData.createDataRow()
    int index // Здесь будет позиция вставки

    if (getRows(data).size() > 0) {
        DataRow<Cell> selectRow
        // Форма не пустая
        log("Форма не пустая")
        log("size = " + getRows(data).size())
        if (currentDataRow != null && getRows(data).indexOf(currentDataRow) != -1) {
            // Значит выбрал строку куда добавлять
            log("Строка вставки выбрана")
            log("indexOf = " + getRows(data).indexOf(currentDataRow))
            selectRow = currentDataRow
        } else {
            // Строку не выбрал поэтому добавляем в самый конец
            log("Строка вставки не выбрана, поставим в конец формы")
            selectRow = getRows(data).get(getRows(data).size() - 1) // Вставим в конец
        }

        int indexSelected = getRows(data).indexOf(selectRow)
        log("indexSelected = " + indexSelected.toString())

        // Определим индекс для выбранного места
        if (selectRow.getAlias() == null) {
            // Выбрана строка не итого
            log("Выбрана строка не итого")
            index = indexSelected // Поставим на то место новую строку
        } else {
            // Выбрана строка итого, для статических строг итого тут проще и надо фиксить под свою форму
            // Для динимаческих строк итого идём вверх пока не встретим конец формы или строку не итого
            log("Выбрана строка итого")

            for (index = indexSelected; index >= 0; index--) {
                log("loop index = " + index.toString())
                if (getRows(data).get(index).getAlias() == null) {
                    log("Нашел строку отличную от итого")
                    index++
                    break
                }
            }
            if (index < 0) {
                // Значит выше строки итого нет строк, добавим новую в начало
                log("выше строки итого нет строк")
                index = 0
            }
            log("result index = " + index.toString())
        }
    } else {
        // Форма пустая поэтому поставим строку в начало
        log("Форма пустая поэтому поставим строку в начало")
        index = 0
    }

    ['date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        newRow.getCell(it).editable = true
        //newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    getData(formData).insert(newRow,index+1)
}

def log(String message, Object... args) {
    //logger.info(message, args)
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
def fillForm(){
    def data = getData(formData)
    // удаляем строки итого
    def delRow = []
    getRows(data).each { row ->
        if (isTotalRow(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        data.delete(row)
    }

    def i=1
    getRows(data).each{ row ->
        row.number = i
        i++
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
    data.insert(newRowQuarter, getRows(data).size()+1)

    // строка Итого за текущий отчетный (налоговый) период
    def newRowTotal = formData.createDataRow()
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowTotal.getCell("fix").setColSpan(4)
    newRowTotal.fix = "Итого за текущий отчетный (налоговый) период"
    newRowTotal.costs = getTotalValue()
    newRowTotal.setAlias("total")
    data.insert(newRowTotal, getRows(data).size()+1)
}

/**
 * Логические проверки
 */
def logicalCheck(){
    def data = getData(formData)
    getRows(data).each{ row ->
        // Обязательность заполнения поля графы (с 1 по 6); фатальная; Поле ”Наименование поля” не заполнено!
        ['number', 'date', 'part', 'dealingNumber', 'bondKind', 'costs'].each{alias ->
            if (!isTotalRow(row) && (row[alias] == null || row[alias] == '')){
                logger.error('Поле ”'+row.getCell(alias).getColumn().getName()+'” не заполнено!')
            }
        }

        reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
        // Проверка даты совершения операции и границ отчетного периода; фатальная; Дата совершения операции вне границ отчетного периода!
        if (!isTotalRow(row) && row.date != null && !(
                (reportPeriodStartDate.getTime().equals(row.date) || row.date.after(reportPeriodStartDate.getTime())) &&
                (reportPeriodEndDate.getTime().equals(row.date) || row.date.before(reportPeriodEndDate.getTime()))
        )){
            logger.error('Дата совершения операции вне границ отчетного периода!')
        }

        getRows(data).each { rowItem ->
            if (!isTotalRow(row) && row.number == rowItem.number && !row.equals(rowItem)) {
                logger.error('Нарушена уникальность номера по порядку!')
            }
        }

        // Проверка на нулевые значения; фатальная; Все суммы по операции нулевые!
        if (row.costs == 0){
            logger.error('Все суммы по операции нулевые!')
        }
        // Проверка актуальности поля «Часть сделки»; не фатальная;
        if (row.part!=null && getPart(row.part)==null){
            logger.warn('Поле ”Часть сделки” указано неверно!');
        }
    }

    // проверка на наличие итоговых строк, иначе будет ошибка
    def totalQuarterRow = data.getDataRow(getRows(data),'totalQuarter')
    def totalRow = data.getDataRow(getRows(data),'total')
    if (totalQuarterRow!=null || totalRow!=null) {
        // Проверка итоговых значений за текущий квартал; фатальная; Итоговые значения за текущий квартал рассчитаны неверно!
        if (totalQuarterRow!=null && totalQuarterRow.costs != getQuarterTotal()) {
            logger.error('Итоговые значения за текущий квартал рассчитаны неверно!')
        }

        // Проверка итоговых значений за текущий отчётный (налоговый) период; фатальная; Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!
        if (totalRow!=null && totalRow.costs != getTotalValue()) {
            logger.error('Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!')
        }
    }

}

/**
 * Скрипт для проверки создания.
 */
void checkBeforeCreate() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
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
def isQuarterTotal(row){
    row.getAlias()=='totalQuarter'
}

/**
 * Проверка является ли строка итововой (последняя строка)
 */
def isMainTotalRow(row){
    row.getAlias()=='total'
}

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. за квартал, либо основной)
 */
def isTotalRow(row){
    return row.getAlias()=='total' || row.getAlias()=='totalQuarter'
}

// функция возвращает итоговые значения за текущий квартал
def getQuarterTotal(){
    def data = getData(formData)
    def row6val = 0
    getRows(data).each{ row->
        if (!isTotalRow(row)){
            row6val += row.costs?:0
        }
    }
    row6val
}

// Функция возвращает итоговые значения за текущий отчётный (налоговый) период
def getTotalValue(){
    def data = getData(formData)
    quarterRow = data.getDataRow(getRows(data),'totalQuarter')
    // возьмем форму за предыдущий отчетный период
    def prevQuarter = quarterService.getPrevReportPeriod(formData.reportPeriodId)
    if (prevQuarter != null) {
        log('Текущий период Id:' + formData.reportPeriodId)
        log('Предыдущий период найден Id:' + prevQuarter.id)
        prevQuarterFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, prevQuarter.id);

        if (prevQuarterFormData != null && prevQuarterFormData.state == WorkflowState.ACCEPTED) {
            prevQuarterData = getData(prevQuarterFormData)
            def prevQuarterTotalRow = prevQuarterData.getDataRow(getRows(prevQuarterData),"total")
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
        rowItem.number = i++
    }
}

/**
 * Скрипт для сортировки.
 *
 * @author Ivildanov
 */
void sort() {
    def data = getData(formData)
    // сортировка
    // 1 - Дата сделки
    // 2 - Номер сделки
    getRows(data).sort { a, b ->
        if (a == null || isTotalRow(a)) return 0
        int val = (a.date).compareTo(b.date)
        if (val == 0) {
            val = (a.dealingNumber ?: "").compareTo(b.dealingNumber ?: "")
        }
        return val
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
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,getRows(data).size()+1)
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
    return refBookService.getNumberValue(60,part,'CODE');
}
