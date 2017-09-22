package refbook.classificator_okved

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import java.util.regex.Pattern

/**
 * Cкрипт справочника «Общероссийский классификатор видов экономической деятельности» (id = 925)
 *
 * @author Bulat Kinzyabulatov
 */

(new classificator_okved(this)).run();

@TypeChecked
class classificator_okved extends AbstractScriptClass {

    private classificator_okved() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    classificator_okved(scriptClass) {
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
            def code = item.CODE?.stringValue
            List<Pattern> patterns = []
            patterns.add(Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}"))
            patterns.add(Pattern.compile("[0-9]{2}"))
            patterns.add(Pattern.compile("[0-9]{2}\\.[0-9]{1}"))
            patterns.add(Pattern.compile("[0-9]{2}\\.[0-9]{2}"))
            patterns.add(Pattern.compile("[0-9]{2}\\.[0-9]{2}\\.[0-9]{1}"))
            boolean matching = false
            for (Pattern p : patterns) {
                if (code && p.matcher(code).matches()) {
                    matching = true
                }
            }
            if (!matching) {
                StringBuilder patternStringBuilder = new StringBuilder()
                for (Pattern p : patterns) {
                    patternStringBuilder.append(p.pattern())
                    .append("\" / \"")
                }
                String patternString = patternStringBuilder.delete(patternStringBuilder.lastIndexOf("\" / \""), patternStringBuilder.length()).toString();
                logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, patternString)
            }
        }
    }
}




