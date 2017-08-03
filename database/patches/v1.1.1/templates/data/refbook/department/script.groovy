package refbook.department

import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.MembersFilterData
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.util.Pair
import groovy.transform.Field

/**
 * Cкрипт справочника "Подразделения" (id = 30).
 * ref_book_id = 30
 *
 * Проверки перенесены из провайдера RefBookDepartment, пронумерованы для удобства (этой нумерации нет в чтз).
 * В скрипт перенесены:
 *      проверки корректности 1..9 - http://conf.aplana.com/pages/viewpage.action?pageId=11378367
 *      проверки при редактировании записи шаги 3, 5, 6 - http://conf.aplana.com/pages/viewpage.action?pageId=11378355
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

@Field
def departmantMap = [:]
@Field
def parentTBIdMap = [:]

void save() {
    // главный банк
    Department rootBank = getDepartment(ROOT_BANK_ID)
    def dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID)
    def refBook = refBookFactory.get(REF_BOOK_ID)
    def attributes = refBook.getAttributes()

    // в saveRecords всегда одна запись
    for (def saveRecord : saveRecords) {
        Integer recordId = uniqueRecordId
        DepartmentType type = (saveRecord.TYPE?.value != null ? DepartmentType.fromCode(saveRecord.TYPE?.value?.intValue()) : null)
        def parentDepartmentId = saveRecord.PARENT_ID?.value?.intValue()

        if (!isNewRecords) {
            def record = dataProvider.getRecordData(recordId)

            def oldType = DepartmentType.fromCode(record?.TYPE?.value?.intValue())
            def isChangeType = oldType != type

            // Проверки при редактировании записи http://conf.aplana.com/pages/viewpage.action?pageId=11378355
            // 3 шаг - проверки при редактировании
            if (isChangeType && DepartmentType.ROOT_BANK == oldType) {
                throw new ServiceException("Подразделение не может быть отредактировано, так как для него нельзя изменить тип \"Банк\"!")
            }

            // шаг 4 на клиенте

            // шаг 5 - проверки при редактировании
            def oldTBId = (getParentTBId(recordId) ?: 0)
            def newTBId = saveRecord?.PARENT_ID?.value != null ? getParentTBId(saveRecord?.PARENT_ID?.value?.intValue()) : departmentService.getBankDepartment()?.id
            if (!newTBId) {
                newTBId = recordId
            }
            def isChangeTB = oldTBId != 0 && oldTBId != newTBId
            if (isChangeTB) {
                throw new ServiceException("Подразделение не может быть отредактировано, так как невозможно его переместить в состав другого территориального банка!")
            }

            // 6 - проверки при редактировании
            def departmentTypes = [DepartmentType.CSKO_PCP, DepartmentType.MANAGEMENT]
            if (isChangeType && oldType in departmentTypes && !(type in departmentTypes)) {
                MembersFilterData membersFilter = new MembersFilterData()
                membersFilter.setDepartmentIds(new HashSet<Integer>(Arrays.asList(recordId)))
                def users = formDataService.getUsersByFilter(membersFilter)
                if (!users.isEmpty()) {
                    for (def user : users) {
                        logger.error("Пользователь %s назначен подразделению %s", user.getName(), record?.NAME?.value)
                    }
                    throw new ServiceException("Подразделение не может быть отредактировано, так как для него нельзя изменить тип \"Управление\", если ему назначены пользователи!")
                }
            }
        }

        // Проверки корректности http://conf.aplana.com/pages/viewpage.action?pageId=11378367
        // 1 - проверка корректности
        if (parentDepartmentId != null && type == DepartmentType.ROOT_BANK) {
            logger.error('Подразделение с типом "Банк" не может иметь родительское подразделение!')
            break
        }

        // 2 - проверка корректности
        if (saveRecord.TYPE && saveRecord.TYPE.value != 1 && saveRecord.PARENT_ID?.value == null) {
            logger.error("Для подразделения должен быть указан код родительского подразделения!")
            break
        }

        // 3 - проверка корректности
        if (rootBank != null && type == DepartmentType.ROOT_BANK && (recordId == null || rootBank.id != recordId)) {
            logger.error('Подразделение с типом "Банк" уже существует!')
            break
        }

        // 4 - проверка корректности
        if (type != null && DepartmentType.TERR_BANK.getCode() == type.getCode() &&
                parentDepartmentId != null && rootBank != null && parentDepartmentId != rootBank.id) {
            logger.error("Территориальный банк может быть подчинен только Банку!")
            break
        }

        // 5 - проверка корректности
        def errors = checkFillRequiredRefBookAtributes(attributes, saveRecord)
        if (!errors.isEmpty()) {
            logger.error("Поля " + errors + " являются обязательными для заполнения")
        }

        // 6 - проверка корректности значений атрибутов
        errors = checkRefBookAtributeValues(attributes, saveRecord)
        if (!errors.isEmpty()) {
            for (String error : errors) {
                logger.error(error)
            }
            break
        }

        // 7 - проверка корректности
        // Новое подразделение не имеет смысла проверять
        if (recordId) {
            Department currDepartment = getDepartment(recordId)
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

        // 8  - проверка корректности
        // Если нет родительского, то это подразделение Банк, иначе проверяем аттрибут "действующее подразделение" у родительского подразделения
        if (saveRecord.IS_ACTIVE?.value == 1 && parentDepartmentId && !getDepartment(parentDepartmentId).isActive()) {
            def actionName = (isNewRecords ? 'создано' : 'отредактировано')
            logger.error("Подразделение не может быть $actionName, так как ему не может быть установлен признак \"Действующее\", если оно находится в составе недействующего подразделения!")
        }

        // 9 - проверка корректности
        //Получаем записи у которых совпали значения уникальных атрибутов
        RefBookRecord refBookRecord = new RefBookRecord()
        refBookRecord.setUniqueRecordId(recordId?.longValue())
        refBookRecord.setValues(saveRecord)
        List<Pair<String, String>> matchedRecords = refBookService.getMatchedRecordsByUniqueAttributes(recordId?.longValue(), attributes, [refBookRecord])
        if (matchedRecords != null && !matchedRecords.isEmpty()) {
            for (Pair<String, String> pair : matchedRecords) {
                logger.error(String.format("Нарушено требование к уникальности, уже существует подразделение %s с таким значением атрибута \"%s\"!",
                        pair.getFirst(), pair.getSecond()))
            }
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

Department getDepartment(def id) {
    if (departmantMap[id] == null) {
        departmantMap[id] = departmentService.get(id)
    }
    return departmantMap[id]
}

def getParentTBId(Integer parentId) {
    if (parentId == null) {
        return null
    }
    if (parentTBIdMap[parentId] == null) {
        parentTBIdMap[parentId] = departmentService.getParentTBId(parentId)
    }
    return parentTBIdMap[parentId]
}