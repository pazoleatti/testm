package form_template.deal.organization_matching

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Согласование организации
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
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
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    dataRowHelper.save(dataRowHelper.getAllCached())
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    dataRowHelper.insert(row, index)
    dataRows.add(row)
    ['name', 'country', 'regNum', 'taxpayerCode', 'address', 'inn', 'kpp', 'code'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.save(dataRows)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        ['name', 'country', 'regNum', 'code'].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
// Проверка уникальности Регистрационного номера организации в стране ее регистрации (инкорпорации)
        def list = []
        // TODO графа 3 (regNum) должна быть уникальна в рамках справочника «Организации – участники контролируемых сделок»
        def regNumCell = row.getCell('regNum')
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
            logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
        }

        // TODO второй варинат проверки, если прокатит - оставить его, первый - убрать
        // Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
       ['regNum', 'taxpayerCode', 'inn', 'kpp'].each {
            def rowCell = row.getCell(it)
            if (rowCell.value != null && !rowCell.value.toString().isEmpty()) {
                // TODO Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
                if (false) {
                    def msg = rowCell.column.name
                    logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
        }
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        // TODO расчет полей по справочникам
    }
}