package form_template.deal.organization_matching

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        accepted()
        break
}

void accepted() {
    def List<Map<String, RefBookValue>> updateList = new ArrayList<Map<String, RefBookValue>>()
    def List<Map<String, RefBookValue>> insertList = new ArrayList<Map<String, RefBookValue>>()
    def List<Long> deleteList = new ArrayList<Long>()
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (row in dataRows) {
        def Number operationCode = row.editSign != null ? refBookService.getRecordData(80, row.editSign).CODE.numberValue : 0
        switch (operationCode) {
            case 0:
                // добавление новой записи
                insertList.add(getRecord(row))
                break
            case 1:
                // изменение существующей записи
                updateList.add(getRecord(row))
                break
            case 2:
                // удаление существующей записи
                deleteList.add(row.refBookRecord)
                break
        }
    }

    def refDataProvider = refBookFactory.getDataProvider(9)
    if (updateList.size() > 0)
        refDataProvider.updateRecords(new Date(), updateList)
    if (insertList.size() > 0)
        refDataProvider.insertRecords(new Date(), insertList)
    if (deleteList.size() > 0)
        refDataProvider.deleteRecords(new Date(), deleteList)
}

Map<String, RefBookValue> getRecord(DataRow<Cell> row) {
    def Map<String, RefBookValue> map = new HashMap<String, RefBookValue>()
    map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, row.name))
    map.put("ORGANIZATION", new RefBookValue(RefBookAttributeType.REFERENCE, row.code))
    map.put("KPP", new RefBookValue(RefBookAttributeType.NUMBER, row.kpp))
    map.put("INN_KIO", new RefBookValue(RefBookAttributeType.STRING, row.inn))
    map.put("ADDRESS", new RefBookValue(RefBookAttributeType.STRING, row.address))
    map.put("TAXPAYER_CODE", new RefBookValue(RefBookAttributeType.STRING, row.taxpayerCode))
    map.put("REG_NUM", new RefBookValue(RefBookAttributeType.STRING, row.regNum))
    map.put("COUNTRY", new RefBookValue(RefBookAttributeType.REFERENCE, row.country))
    map.put("OFFSHORE", new RefBookValue(RefBookAttributeType.REFERENCE, row.offshore))
    map.put("DOP_INFO", new RefBookValue(RefBookAttributeType.STRING, row.dopInfo))
    map.put("SKOLKOVO", new RefBookValue(RefBookAttributeType.REFERENCE, row.skolkovo))
    map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, row.refBookRecord))

    return map
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    row.keySet().each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    ['name', 'country', 'regNum', 'taxpayerCode', 'address', 'inn', 'kpp', 'code',
            'offshore', 'dopInfo', 'skolkovo', 'editSign', 'refBookRecord'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def isHaveDuplicates = false
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        // Проверка заполненности полей в строках НЕ на удаление
        if (row.editSign == null || refBookService.getRecordData(80, row.editSign).CODE.numberValue != 2) {
            ['rowNum', 'name', 'country', 'address', 'inn', 'code', 'editSign', 'offshore', 'skolkovo'].each {
                def rowCell = row.getCell(it)
                if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                    def msg = rowCell.column.name
                    logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
                }
            }
        } else {
            // Проверка заполненности полей в строках на удаление
            ['rowNum', 'editSign'].each {
                def rowCell = row.getCell(it)
                if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                    def msg = rowCell.column.name
                    logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
                }
            }
        }
        // Проверка на заполнение атрибута «Запись справочника»
        if (row.editSign != null && row.refBookRecord == null && refBookService.getRecordData(80, row.editSign).CODE.numberValue != 0) {
            def msg = row.getCell('refBookRecord').column.name
            logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
        }
        // Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
        if (row.editSign == null || refBookService.getRecordData(80, row.editSign).CODE.numberValue == 0) {
            def refDataProvider = refBookFactory.getDataProvider(9)
            // Рег. номер организации
            def val = row.regNum
            if (val != null && !val.isEmpty()) {
                def res = refDataProvider.getRecords(new Date(), null, "REG_NUM = '$val'", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("regNum").column.name
                    logger.warn("Строка $rowNum: «$msg» уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
            // Код налогоплательщика
            val = row.taxpayerCode
            if (val != null && !val.isEmpty()) {
                def res = refDataProvider.getRecords(new Date(), null, "TAXPAYER_CODE = '$val'", null);
                if (res.getRecords().size() > 0) {
                    def msg = row.getCell("taxpayerCode").column.name
                    logger.warn("Строка $rowNum: «$msg» уже существует в справочнике «Организации – участники контролируемых сделок»!")
                }
            }
            // ИНН
            val = row.inn
            if (val != null) {
                def msg = row.getCell("inn").column.name
                if (!val.matches('([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}')) {
                    logger.error("Строка $rowNum: «$msg» содержит недопустимые символы!")
                } else {
                    def res = refDataProvider.getRecords(new Date(), null, "INN_KIO = '$val'", null);
                    if (res.getRecords().size() > 0) {
                        logger.warn("Строка $rowNum: «$msg» уже существует в справочнике «Организации – участники контролируемых сделок»!")
                    }
                }
            }
            // КПП
            val = row.kpp
            if (val != null) {
                val = val.toString()
                def msg = row.getCell("kpp").column.name
                if (!val.matches('([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-F]{2})([0-9]{3})')) {
                    logger.error("Строка $rowNum: «$msg» содержит недопустимые символы!")
                } else {
                    def res = refDataProvider.getRecords(new Date(), null, "KPP = $val", null);
                    if (res.getRecords().size() > 0) {
                        logger.warn("Строка $rowNum: «$msg» уже существует в справочнике «Организации – участники контролируемых сделок»!")
                    }
                }
            }
        }
        // Проверка существования записи
        if (row.refBookRecord != null && refBookService.getRecordData(9, row.refBookRecord) == null) {
            def msg = row.getCell('refBookRecord').column.name
            logger.warn("Строка $rowNum: В справочнике «Организации – участники контролируемых сделок» не найден элемент $msg!")
        }
        // Проверка уникальности ссылки на элемент справочника
        if (row.refBookRecord != null && dataRows.find { it.refBookRecord == row.refBookRecord && it.getIndex() != row.getIndex() } != null)
            isHaveDuplicates = true
    }

    if (isHaveDuplicates)
        logger.error("Одна запись справочника не может быть отредактирована более одного раза в одной и той же форме!")
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
        // Чистим ссылку на запись, если не меняем и не удаляем
        if (row.refBookRecord != null && (row.editSign == null || refBookService.getRecordData(80, row.editSign).CODE.numberValue == 0)) {
            row.refBookRecord = null
        }
    }
    dataRowHelper.update(dataRows);
}