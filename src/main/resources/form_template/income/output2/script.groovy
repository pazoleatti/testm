import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 6.3.2    Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        checkDecl()
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        break
    case FormDataEvent.CHECK:
        test() // TODO (Ramil Timerbaev)
        return
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
}

void deleteRow() {
    def data = getData(formData)
    // @todo убрать indexOf после http://jira.aplana.com/browse/SBRFACCTAX-2702
    if (currentDataRow != null && getRows(data).indexOf(currentDataRow) != -1) {
        getRows(data).remove(currentDataRow)
        save(data)
    }
}

void addRow() {
    def data = getData(formData)
    row = formData.createDataRow()
    for (alias in ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment',
            'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    getRows(data).add(row)
    save(data)
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 */
void checkUniq() {

    FormData findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error('Нельзя создавать форму с типом ${formData.kind?.name}')
    }
}

/**
 * Проверка наличия декларации для текущего department
 */
void checkDecl() {
    declarationType = 2;    // Тип декларации которую проверяем(Налог на прибыль)
    declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("Декларация банка находиться в статусе принята")
    }
}

void logicCheck() {
    def data = getData(formData)
    for (row in getRows(data)) {

        for (alias in ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']) {
            if (row.getCell(alias).value == null) {
                logger.error('Поле ' + row.getCell(alias).column.name.replace('%', '') + ' не заполнено')
            }
        }

        String zipCode = (String) row.zipCode;
        if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
            logger.error('Неправильно указан почтовый индекс!')
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (!dictionaryRegionService.isValidCode((String) row.subdivisionRF)) {
                logger.error('Неверное наименование субъекта РФ!')
            }
        }
    }
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Проверить наличие итоговой строки.
 *
 * @param data данные нф (helper)
 */
def hasTotal(def data) {
    for (def row: getRows(data)) {
        if (row.getAlias() == 'total') {
            return true
        }
    }
    return false
}