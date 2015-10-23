/*
    blob_data.id = '95b118fb-5d10-48a0-ad8e-bfdcdf126399'
 */
package refbook.jur_persons_terms

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Скрипт справочника «Правила назначения категории юридическому лицу» (id = 515)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def REF_BOOK_ID = 515

void save() {
    def refDataProvider = refBookFactory.getDataProvider(REF_BOOK_ID)
    saveRecords.each {
        def Long code = it.CODE?.referenceValue
        def Long minValue = it.MIN_VALUE?.numberValue
        def Long maxValue = it.MAX_VALUE?.numberValue

        // 1. Проверка наличия единственного правила для данного типа ЮЛ и данного объема доходов и расходов
        if (code && minValue != null) {
            String filter = "CODE = $code and (($minValue > MIN_VALUE and (MAX_VALUE is null or $minValue < MAX_VALUE  or $minValue = MAX_VALUE) or $minValue = MIN_VALUE) "
            if (maxValue) {
                filter += " or ($maxValue = MIN_VALUE or ($maxValue > MIN_VALUE and (MAX_VALUE is null or $maxValue < MAX_VALUE))))"
            } else {
                filter += " or (MAX_VALUE is null or MAX_VALUE > $minValue or MAX_VALUE = $minValue))"
            }
            def records = refDataProvider.getRecords(validDateFrom, null, filter, null)
            if (records && records.size() > 0 && (records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue != uniqueRecordId || records.size() > 1)) {
                logger.error("Для указанных условий в системе уже существует правило назначения  категории!")
            }
        }

        // 8. Проверка правильности заполнения полей «Дата наступления основания для включения в список» и «Дата наступления основания для исключении из списка»
        if (minValue != null && maxValue != null && minValue > maxValue) {
            logger.error("Поле «Максимальный объем доходов  и расходов» должно быть больше либо равно полю «Минимальный объем доходов и расходов»!")
        }
    }
}