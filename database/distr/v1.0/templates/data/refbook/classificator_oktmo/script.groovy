package refbook.classificator_oktmo

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 *
 * Скрипт справочника "Общероссийский классификатор территорий муниципальных образований (ОКТМО)" (id=96)
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
        def pattern = /[0-9]{11}|[0-9]{8}/
        if (code && !(code ==~ pattern)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, pattern)
        }
    }
}