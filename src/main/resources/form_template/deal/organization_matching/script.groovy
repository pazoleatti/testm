package form_template.deal.organization_matching

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 410 - Согласование организации
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
    ['name', 'country', 'regNum', 'taxpayerCode', 'address', 'inn', 'kpp', 'code', 'editSign', 'refBookRecord'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
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
        ['rowNum', 'name', 'country', 'regNum', 'address', 'inn', 'code'].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        if (row.editSign == 1 && row.refBookRecord == null) {
            def msg = row.getCell('refBookRecord').column.name
            logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
        }
        // Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
        if (row.editSign == null || row.editSign == 0) {
            def refDataProvider = refBookFactory.getDataProvider(9);
            // Рег. номер организации
            def val = row.regNum
            if (val != null && !val.isEmpty()) {
                def res = refDataProvider.getRecords(new Date(), null, "REG_NUM = '$val'", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("regNum").column.name
                    logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
            // Код налогоплательщика
            val =  row.taxpayerCode
            if ( val!= null && !val.isEmpty()) {
                def res = refDataProvider.getRecords(new Date(), null, "TAXPAYER_CODE = '$val'", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("taxpayerCode").column.name
                    logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
            // ИНН
            val =  row.inn
            if (val != null) {
                def res = refDataProvider.getRecords(new Date(), null, "INN_KIO = $val", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("inn").column.name
                    logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
            // КПП
            val =  row.kpp
            if (row.kpp != null) {
                def res = refDataProvider.getRecords(new Date(), null, "KPP = $val", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("kpp").column.name
                    logger.warn("«$msg» в строке $rowNum уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
        }
        // Проверка существования записи
        if (row.refBookRecord != null && refBookService.getRecordData(9, row.refBookRecord) == null) {
            def msg = row.getCell('refBookRecord').column.name
            logger.warn("В справочнике «Организации – участники контролируемых сделок» не найден элемент $msg, указанный в строке $rowNum!")
        }
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNum = row.getIndex()
    }
    dataRowHelper.update(dataRows);
}