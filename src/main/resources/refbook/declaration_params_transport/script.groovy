package refbook.declaration_params_property

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cкрипт справочника "Параметры представления деклараций по транспортному налогу" из КСШ (id = 210)
 *
 * @author Alexey Afanasyev
 */

switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def providerCache = [:]
@Field
def recordsCountCache = [:]
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

@Field
def refBookId = 210

boolean checkFormat(String enteredValue, String pat){
    Pattern p = Pattern.compile(pat);
    Matcher m = p.matcher(enteredValue);
    return m.matches();
}

void save() {
    saveRecords.each {
        def String taxOrganCode = it.TAX_ORGAN_CODE?.stringValue
        def String kpp = it.KPP?.stringValue
        // Проверка поля «Код налогового органа» на корректность формата введенных данных
        if (checkFormat(taxOrganCode, "[0-9]{4}")==false) {
            logger.error("Поле «Код налогового органа» должно быть заполнено согласно формату «[0-9]{4}»")
        }
        // Проверка поля «КПП» на корректность формата введенных данных
        if (checkFormat(kpp, "([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})")==false) {
            logger.error("Поле «КПП» должно быть заполнено согласно формату «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»")
        }

    }
}
