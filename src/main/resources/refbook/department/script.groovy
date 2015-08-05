package refbook.department

import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import groovy.transform.Field

/**
 * Cкрипт справочника "Подразделения" (id = 30).
 * ref_book_id = 30
 *
 * Проверки перенесены из провайдера RefBookDepartment, пронумерованы для удобства (этой нумерации нет в чтз).
*/

switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def ROOT_BANK_ID = 0

@Field
def REF_BOOK_ID = 30

void save() {
    // главный банк
    Department rootBank = departmentService.get(ROOT_BANK_ID)
    def dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID)
    def refBook = refBookFactory.get(REF_BOOK_ID)
    def attributes = refBook.getAttributes()

    for (def saveRecord : saveRecords) {
        def recordId = null
        if (!isNewRecords) {
            def filter = "CODE = " + saveRecord.CODE
            def records = dataProvider.getRecords(validDateFrom, null, filter, null)
            if (!records?.isEmpty()) {
                recordId = records.get(0)?.record_id?.value?.intValue()
            }
        }

        DepartmentType type = (saveRecord.TYPE?.value != null ? DepartmentType.fromCode(saveRecord.TYPE?.value?.intValue()) : null)
        def parentDepartmentId = saveRecord.PARENT_ID?.value?.intValue()

        // 1
        if (parentDepartmentId != null && type == DepartmentType.ROOT_BANK) {
            logger.error('Подразделение с типом "Банк" не может иметь родительское подразделение!')
            break
        }

        // 2
        if (saveRecord.TYPE && saveRecord.TYPE.value != 1 && saveRecord.PARENT_ID?.value == null) {
            logger.error("Для подразделения должен быть указан код родительского подразделения!")
            break
        }

        // 3
        if (rootBank != null && type == DepartmentType.ROOT_BANK && (recordId == null || rootBank.id != recordId)) {
            logger.error('Подразделение с типом "Банк" уже существует!')
            break
        }

        // 4
        if (type != null && DepartmentType.TERR_BANK.getCode() == type.getCode() &&
                parentDepartmentId != null && rootBank != null && parentDepartmentId != rootBank.id) {
            logger.error("Территориальный банк может быть подчинен только Банку!")
            break
        }

        // 5
        def errors = checkFillRequiredRefBookAtributes(attributes, saveRecord)
        if (!errors.isEmpty()) {
            logger.error("Поля " + errors + " являются обязательными для заполнения")
        }

        // 6
        // Проверка корректности значений атрибутов
        errors = checkRefBookAtributeValues(attributes, saveRecord)
        if (!errors.isEmpty()) {
            for (String error : errors) {
                logger.error(error)
            }
            break
        }

        // 7
        // Новое подразделение не имеет смысла проверять
        if (recordId) {
            Department currDepartment = departmentService.get(recordId)
            boolean isChangeActive = saveRecord.IS_ACTIVE?.value != (currDepartment.isActive() ? 1 : 0)
            if (isChangeActive && saveRecord.IS_ACTIVE?.value == 0) {
                List<Department> childIds = departmentService.getAllChildren(recordId)
                for (Department child : childIds) {
                    if (recordId != child.getId() && child.isActive()) {
                        logger.error('Подразделение не может быть отредактировано, так как нельзя установить для него признак "Недействующее", если в его составе находится действующее подразделение!')
                        break
                    }
                }
            }
        }

        // 8
        // Если нет родительского то это подразделение Банк
        if (parentDepartmentId == null) {
            continue
        }
        //Проверяем аттрибут "действующее подразделение" у родительского подразделения
        Department parentDep = departmentService.get(parentDepartmentId)
        if (!parentDep.isActive() && saveRecord.IS_ACTIVE?.value == 1) {
            def actionName = (isNewRecords ? 'создано' : 'отредактировано')
            logger.error("Подразделение не может быть $actionName, так как ему не может быть установлен признак \"Действующее\", если оно находится в составе недействующего подразделения!")
        }
    }
}

/**
 * Получить названия незаполненных обязательных полей.
 * Взят из RefBookUtils.checkFillRequiredRefBookAtributes.
 *
 * @param attributes атрибуты справочника
 * @param record запись для проверки
 */
def checkFillRequiredRefBookAtributes(def attributes, def record) {
    def errors = []
    for (RefBookAttribute a : attributes) {
        if (a.isRequired() && (!record.containsKey(a.alias) || record.get(a.alias).isEmpty())) {
            errors.add(a.name)
        }
    }
    return errors
}

/**
 * Получить сообщения с ошибками в названиях подразделений.
 * Проверка взята из RefBookUtils.checkRefBookAtributeValues.
 *
 * @param attributes атрибуты справочника
 * @param record запись для проверки
 */
def checkRefBookAtributeValues(def attributes, def record) {
    def errors = []
    for (RefBookAttribute a : attributes) {
        if ((a.id == 161L || a.id == 162L) && record.get(a.alias).getStringValue() != null && record.get(a.alias).getStringValue().contains("/")) {
            errors.add("Значение атрибута «" + a.name + "» не должно содержать символ «/»!")
        }
    }
    return errors
}