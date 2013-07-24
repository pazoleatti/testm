package form_template.deal.matrix

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Матрица
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED:
        acceptance()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование новой матрицы невозможно, т.к. матрица с указанными параметрами уже сформирована.')
    }
}

void addRow() {
    def row = formData.createDataRow()
    // TODO пересмотреть редактируемость (пока все редактируемо)
    for (column in formData.getFormColumns()) {
        if (column.alias.equals('dealNum1')) {
            continue;
        }
        row.getCell(column.alias).editable = true
        row.getCell(column.alias).setStyleAlias('Редактируемая')
    }
}

void deleteRow() {
    if (currentDataRow != null) {
        recalcRowNum()
        formData.dataRows.remove(currentDataRow)
    }
}

/**
 * Пересчет индексов строк перед удалением строки
 */
void recalcRowNum() {
    def i = formData.dataRows.indexOf(currentDataRow)

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.dealNum1 = i++
    }
}
/**
 * Логические проверки
 */
void logicCheck() {
}

/**
 * Консолидация
 */
void consolidation() {
}

/**
 * Инициация консолидации
 */
void acceptance() {
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
}
