package refbook // classificator_okved_ref комментарий для локального поиска скрипта

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

new ClassificatorOkved(this).run();

@TypeChecked
class ClassificatorOkved extends AbstractScriptClass {

    List<Map<String, RefBookValue>> saveRecords;

    private ClassificatorOkved() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    ClassificatorOkved(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("saveRecords")) {
            this.saveRecords = (List<Map<String, RefBookValue>>) scriptClass.getBinding().getProperty("saveRecords");
        }
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




