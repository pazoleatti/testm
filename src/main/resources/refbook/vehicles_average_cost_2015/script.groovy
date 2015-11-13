package refbook.vehicles_average_cost_2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * blob_data.id = '54def165-b118-4d1e-a52c-be429ebd832e'
 *
 * Скрипт справочника "Средняя стоимость транспортных средств (2015)" (id = 218)
 *
 * @author Lhaziev
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
