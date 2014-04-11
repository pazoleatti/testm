package form_template.deal.organization_matching.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        accepted()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'country', 'regNum', 'taxpayerCode', 'address', 'inn', 'kpp', 'code',
        'offshore', 'dopInfo', 'skolkovo', 'editSign', 'refBookRecord']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'name', 'country', 'address', 'inn', 'code', 'editSign']

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
    if (updateList.size() > 0) {
        updateList.each { map ->
            refDataProvider.updateRecordVersion(logger, map.get(RefBook.RECORD_ID_ALIAS)?.value, null, null, map);
        }
    }
    if (insertList.size() > 0) {
        def records = []
        insertList.each { map ->
            RefBookRecord rec = new RefBookRecord()
            rec.setRecordId(null)
            rec.setValues(map)
            records.add(rec)
        }
        refDataProvider.createRecordVersion(logger, new Date(), null, records)
    }
    if (deleteList.size() > 0) {
        // TODO (Ramil Timerbaev) при выполнении ругается что на этой форме есть ссылки на удаляемые справочные данные.
        // Пока сказали оставить, возможно надо будет удалять строки с удаляемыми справочными данными
        refDataProvider.deleteRecordVersions(logger, deleteList)
    }
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

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def isHaveDuplicates = false
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        editSignCode = (row.editSign != null) ? refBookService.getRecordData(80, row.editSign).CODE.numberValue : null
        // Проверка заполненности полей в строках НЕ на удаление
        if (editSignCode == null || editSignCode != 2) {
            checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)
        } else {
            // Проверка заполненности полей в строках на удаление
            checkNonEmptyColumns(row, rowNum, ['rowNum', 'editSign'], logger, false)
        }
        // Проверка на заполнение атрибута «Запись справочника»
        if (row.editSign != null && row.refBookRecord == null && editSignCode != 0) {
            def msg = row.getCell('refBookRecord').column.name
            logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
        }
        // Проверка уникальности полей в рамках справочника «Организации – участники контролируемых сделок»
        if (row.editSign == null || editSignCode == 0) {
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

// Алгоритмы заполнения полей формы
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