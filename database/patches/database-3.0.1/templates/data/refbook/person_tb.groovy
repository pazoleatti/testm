package refbook // person_tb_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Скрипт справочника Список тербанков назначенных ФЛ шв = 908
 */
(new PersonTb(this)).run()

@TypeChecked
class PersonTb extends AbstractScriptClass {

    Map<String, RefBookValue> record
    DepartmentService departmentService

    @TypeChecked(TypeCheckingMode.SKIP)
    PersonTb(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("record")) {
            this.record = (Map<String, RefBookValue>) scriptClass.getBinding().getProperty("record")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getBinding().getProperty("departmentService")
        }
    }

    final String DEPARTMENT_ID_ALIAS = "TB_DEPARTMENT_ID"

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.SAVE:
                save()
                break
        }
    }

    void save() {
        com.aplana.sbrf.taxaccounting.model.Department department = departmentService.get(record.get(DEPARTMENT_ID_ALIAS).referenceValue.intValue())
        if (department.getType() != DepartmentType.TERR_BANK) {
            logger.error("Выбранное подразделение \"%s\", не является территориальным банком.", department.name)
        }
        if (!department.isActive()) {
            logger.error("Выбранное подразделение \"%s\", не активно.", department.name)
        }
    }
}