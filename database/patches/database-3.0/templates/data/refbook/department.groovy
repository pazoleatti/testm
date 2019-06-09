package refbook // department_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.MembersFilterData
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.TAUserView
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode


/**
 * Cкрипт справочника "Подразделения" (id = 30).
 * ref_book_id = 30
 *
 * Проверки перенесены из провайдера RefBookDepartment, пронумерованы для удобства (этой нумерации нет в чтз).
 * В скрипт перенесены:
 *      проверки корректности 1..9 - http://conf.aplana.com/pages/viewpage.action?pageId=11378367
 *      проверки при редактировании записи шаги 3, 5, 6 - http://conf.aplana.com/pages/viewpage.action?pageId=11378355
*/

(new Department(this)).run();

@TypeChecked
class Department extends AbstractScriptClass {

    Long uniqueRecordId;
    Boolean isNewRecords;
    RefBookFactory refBookFactory
    CommonRefBookService commonRefBookService
    List<Map<String, RefBookValue>> saveRecords
    RefBookService refBookService
    DepartmentService departmentService

    Department() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    public Department(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("saveRecords")) {
            this.saveRecords = (List<Map<String, RefBookValue>>) scriptClass.getBinding().getProperty("saveRecords");
        }
        if (scriptClass.getBinding().hasVariable("uniqueRecordId")) {
            this.uniqueRecordId = (Long) scriptClass.getBinding().getProperty("uniqueRecordId");
        }
        if (scriptClass.getBinding().hasVariable("isNewRecords")) {
            this.isNewRecords = (Boolean) scriptClass.getBinding().getProperty("isNewRecords");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("commonRefBookService")) {
            this.commonRefBookService = (CommonRefBookService) scriptClass.getProperty("commonRefBookService")
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

    Integer ROOT_BANK_ID = 0

    Long REF_BOOK_ID = 30

    Map<Integer, com.aplana.sbrf.taxaccounting.model.Department> departmantMap = [:]

    Map<Integer, Integer> parentTBIdMap = [:]

    void save() {
        // главный банк
        com.aplana.sbrf.taxaccounting.model.Department rootBank = getDepartment(ROOT_BANK_ID)
        RefBookDataProvider dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID)
        RefBook refBook = commonRefBookService.get(REF_BOOK_ID)
        List<RefBookAttribute> attributes = refBook.getAttributes()

        // в saveRecords всегда одна запись
        for (Map<String, RefBookValue> saveRecord : saveRecords) {
            Integer recordId = uniqueRecordId
            DepartmentType type = (saveRecord.TYPE?.value != null ? DepartmentType.fromCode((Integer) saveRecord.TYPE?.value) : null)
            Integer parentDepartmentId = (Integer) saveRecord.PARENT_ID?.value

            if (!isNewRecords) {
                Map<String, RefBookValue> record = dataProvider.getRecordData((Long) recordId)

                DepartmentType oldType = DepartmentType.fromCode((Integer) record?.TYPE?.value)
                Boolean isChangeType = oldType != type

                // Проверки при редактировании записи http://conf.aplana.com/pages/viewpage.action?pageId=11378355
                // 3 шаг - проверки при редактировании
                if (isChangeType && DepartmentType.ROOT_BANK == oldType) {
                    throw new ServiceException("Подразделение не может быть отредактировано, так как для него нельзя изменить тип \"Банк\"!")
                }

                // шаг 4 на клиенте

                // шаг 5 - проверки при редактировании
                Integer oldTBId = (getParentTBId(recordId) ?: 0)
                Integer newTBId = saveRecord?.PARENT_ID?.value != null ? getParentTBId((Integer) saveRecord?.PARENT_ID?.value) : departmentService.getBankDepartment()?.id
                if (!newTBId) {
                    newTBId = recordId
                }
                Boolean isChangeTB = oldTBId != 0 && oldTBId != newTBId
                if (isChangeTB) {
                    throw new ServiceException("Подразделение не может быть отредактировано, так как невозможно его переместить в состав другого территориального банка!")
                }

                // 6 - проверки при редактировании
                List<DepartmentType> departmentTypes = []
                departmentTypes << DepartmentType.CSKO_PCP
                departmentTypes << DepartmentType.MANAGEMENT
                if (isChangeType && oldType in departmentTypes && !(type in departmentTypes)) {
                    MembersFilterData membersFilter = new MembersFilterData()
                    membersFilter.setDepartmentIds(new HashSet<Integer>(Arrays.asList(recordId)))
                    PagingResult<TAUserView> users = refBookService.getUsersByFilter(membersFilter)
                    if (!users.isEmpty()) {
                        for (TAUserView user : users) {
                            logger.error("Пользователь %s назначен подразделению %s", user.getName(), record?.NAME?.stringValue)
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
            List<String> errors = checkFillRequiredRefBookAtributes(attributes, saveRecord)
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
                com.aplana.sbrf.taxaccounting.model.Department currDepartment = getDepartment(recordId)
                boolean isChangeActive = saveRecord.IS_ACTIVE?.value != (currDepartment.isActive() ? 1 : 0)
                if (isChangeActive && saveRecord.IS_ACTIVE?.value == 0) {
                    List<com.aplana.sbrf.taxaccounting.model.Department> childIds = departmentService.getAllChildren(recordId)
                    for (com.aplana.sbrf.taxaccounting.model.Department child : childIds) {
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
                String actionName = (isNewRecords ? 'создано' : 'отредактировано')
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
    List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, Map<String, RefBookValue> record) {
        List<String> errors = []
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
    List<String> checkRefBookAtributeValues(List<RefBookAttribute> attributes, Map<String, RefBookValue> record) {
        List<String> errors = []
        for (RefBookAttribute a : attributes) {
            if ((a.id == 161L || a.id == 162L) && record.get(a.alias).getStringValue() != null && record.get(a.alias).getStringValue().contains("/")) {
                errors.add("Значение атрибута «" + a.name + "» не должно содержать символ «/»!")
            }
        }
        return errors
    }

    com.aplana.sbrf.taxaccounting.model.Department getDepartment(Integer id) {
        if (departmantMap.get(id) == null) {
            departmantMap.put(id, departmentService.get(id))
        }
        return departmantMap.get(id)
    }

    Integer getParentTBId(Integer parentId) {
        if (parentId == null) {
            return null
        }
        if (parentTBIdMap.get(parentId) == null) {
            parentTBIdMap.put(parentId, departmentService.getParentTBId(parentId))
        }
        return parentTBIdMap.get(parentId)
    }
}