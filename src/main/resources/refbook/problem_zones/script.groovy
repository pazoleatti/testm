package refbook.problem_zones

import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Cкрипт справочника «Реестр проблемных зон/ зон потенциального риска».
 * blob_data.id = '6922076c-84cd-4f9f-add2-79adfdd7d01e'
 * ref_book_id = 504
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def refBookCache = [:]

void save() {
    saveRecords.each {
        // 1. проверка правильности заполнения поля «Территориальный Банк»
        def departmentId = it?.DEPARTMENT_ID?.value
        def typeId = getRefBookValue(30L, departmentId)?.TYPE?.value
        if (DepartmentType.TERR_BANK.code != typeId) {
            logger.error('Поле «Территориальный Банк» должно быть заполнено подразделением, поле «Тип подразделения» которого равно «Территориальный банк» (справочник «Подразделения»)')
        }
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}