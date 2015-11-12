package refbook.raising_rates_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Скрипт справочника «Повышающие коэффициенты транспортного налога» (id = 209)
 *
 * @author LHaziev
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def Long yearFrom = it.YEAR_FROM?.numberValue.longValue()
        def Long yearTo = it.YEAR_TO?.numberValue.longValue()
        if (yearFrom >= yearTo) {
            logger.error("Поле «Количество лет, прошедших с года выпуска ТС, до» должно быть больше поля «Количество лет, прошедших с года выпуска ТС, от»!")
        }
    }
}
