package refbook.vehicles_tax_rate

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 *
 * Скрипт справочника "Ставки транспортного налога" (id=41)
 *
 * @author Ellina Mamedova
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def code = it.CODE?.stringValue
        def pattern = /[0-9]{3}([0-9]{2}|[?]{2})/
        if (code && !(code ==~ pattern)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, pattern)
        }
    }
}