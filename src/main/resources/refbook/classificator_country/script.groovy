package refbook.classificator_country

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Cкрипт справочника «ОК 025-2001 (Общероссийский классификатор стран мира)» (id = 10)
 *
 * @author Bulat Kinzyabulatov
 */

(new classificator_country(this)).run();

@TypeChecked
class classificator_country extends AbstractScriptClass {

    private classificator_country() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    classificator_country(scriptClass) {
        super(scriptClass)
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.SAVE:
                save()
                break
        }
    }

    void save() {
        saveRecords.each { Map<String, RefBookValue> item ->
            String code = item.CODE?.stringValue
            String code2 = item.CODE_2?.stringValue
            String code3 = item.CODE_3?.stringValue
            Pattern pattern = Pattern.compile("[0-9]{3}")
            Matcher matcher = pattern.matcher(code)
            if (code && !(matcher.matches())) {
                logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, pattern)
            }
            if (code2?.length() != 2 || code2?.trim()?.length() != 2) {
                logger.error("Поле «Код (2-х букв.)» должно содержать 2 символа.")
            }
            if (code3?.length() != 3 || code3?.trim()?.length() != 3) {
                logger.error("Поле «Код (3-х букв.)» должно содержать 3 символа.")
            }
        }
    }
}



