package refbook.classificator_code_724_2_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» (id = 102)
 *
 * @author Bulat Kinzyabulatov
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def box = it.BOX_724_2_1?.value
        if (box != 0 && box != 1) {
            logger.error("Атрибут «Графа НФ 724.2.1 (0 – Графа 4; 1 – Графа 5)»: значение некорректно. Должно быть присвоено одно из допустимых значений: \"0\",\"1\".")
        }
    }
}
