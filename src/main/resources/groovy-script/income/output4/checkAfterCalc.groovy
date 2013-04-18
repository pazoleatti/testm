/**
 * Оставшиеся проверки
 * @author ekuvshinov
 */

//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService dictionaryRegionService
//com.aplana.sbrf.taxaccounting.service.script.DepartmentService departmentService

for (row in formData.dataRows) {

    //noinspection GroovyVariableNotAssigned
    if (row.getAlias() != 'total') {
        if (row.kpp == row.subjectCode && row.subjectCode == '0') {
            logger.error("Все суммы по операции нулевые!")
        }
    }
}