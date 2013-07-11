package form_template.deal.organization_matching

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Согласование организации
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
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
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        acceptance()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break
}

void deleteRow() {
    if (currentDataRow != null) {
        recalcRowNum()
        formData.dataRows.remove(currentDataRow)
    }
}

void recalcRowNum() {
    def i = formData.dataRows.indexOf(currentDataRow)

    for (row in formData.dataRows[i..formData.dataRows.size() - 1]) {
        row.getCell('rowNum').value = i++
    }
}

void addRow() {
    def row = formData.createDataRow()
    for (alias in ['name', 'country', 'regNum', 'taxpayerCode', 'address', 'inn', 'kpp', 'code']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)
    row.getCell('rowNum').value = formData.dataRows.size()
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        def rowNum = row.getCell('rowNum').value
        for (alias in ['name', 'country', 'regNum', 'code']) {
            def rowCell = row.getCell(alias)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.error("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
// Проверка уникальности Регистрационного номера организации в стране ее регистрации (инкорпорации)
        // TODO графа 3 (regNum) должна быть уникальна в рамках справочника «Организации – участники контролируемых сделок»
        def regNumCell = row.getCell('regNum')
        def list = []
        if (false) {
            list.add(regNumCell.column.name)
        }
// Проверка уникальности Кода налогоплательщика в стране регистрации (инкорпорации) или его аналог
        // TODO  Графа 4 (taxpayerCode) должна быть уникальна в рамках справочника «Организации – участники контролируемых сделок»
        def taxpayerCode = row.getCell('taxpayerCode')
        if (false) {
            list.add(taxpayerCode.column.name)
        }
// Проверка уникальности ИНН / КИО
        // TODO  Графа 6 (inn) должна быть уникальна в рамках справочника «Организации – участники контролируемых сделок»
        def inn = row.getCell('inn')
        if (false) {
            list.add(inn.column.name)
        }
// Проверка уникальности КПП
        // TODO  Графа 7 (kpp) должна быть уникальна в рамках справочника «Организации – участники контролируемых сделок»
        def kpp = row.getCell('kpp')
        if (false) {
            list.add(kpp.column.name)
        }
// результат проверок
        for (msg in list) {
            logger.error("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
        }

        // TODO второй варинат проверки, если прокатит - оставить его, первый - убрать
        // Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
        for (alias in ['regNum', 'taxpayerCode', 'inn', 'kpp']) {
            def rowCell = row.getCell(alias)
            if (rowCell.value != null && !rowCell.value.toString().isEmpty()) {
                // TODO Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
                if (false) {
                    def msg = rowCell.column.name
                    logger.error("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
        }
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    for (row in formData.dataRows) {
        // TODO расчет полей по справочникам
    }
}

/**
 * Инициация консолидации
 */
void acceptance() {
    // TODO  Данные отчета попадают в справочник «Организации – участники контролируемых сделок»
}