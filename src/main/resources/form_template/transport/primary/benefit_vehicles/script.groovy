package form_template.transport.primary.benefit_vehicles

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.DataRow

/**
 * Форма "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог".
 * @author gavanesov скопировал скрипт из формы "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог" 
 *                   изменил имена атрибутов, добавил комментарии о необходимости прописать проверки льгот
 *
 */

/**
 * Графы
 * 1 № пп  -  rowNumber
 * 2 Код ОКАТО  -  codeOKATO
 * 3 Идентификационный номер  -  identNumber
 * 4 Регистрационный знак  -  regNumber
 * 5 Код налоговой льготы - taxBenefitCode
 * 6 Дата начала Использование льготы - benefitStartDate
 * 7 Дата окончания Использование льготы - benefitEndDate
 *
 * ['rowNumber', 'codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        copyData()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicalCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        logicalCheck()
        break
}

// Проверяет уникальность в отчётном периоде и вид
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Алгоритм копирования данных из форм предыдущего периода при создании формы
void copyData() {
    // TODO
}

//Добавить новую строку
void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    ['codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемое поле')
    }
    dataRowHelper.insert(row, index)
    setRowIndex()
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    setRowIndex()
}

// Установка номера строки
void setRowIndex() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def index = 1
    for (def row in dataRows) {
        row.rowNumber = index++
    }
    dataRowHelper.update(dataRows);
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Проверенные строки (4-ая провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()
    for (def row in dataRows) {

        // 1. Проверка на заполнение поля
        def errorMsg = ''
        ['rowNumber', 'codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each { it ->
            def cell = row.getCell(it)
            if (cell.getValue() == null || ''.equals(cell.getValue())) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + cell.getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("Строка $row.rowNumber :  не заполнены поля : $errorMsg.")
            return;
        }

        // 2. Поверка на соответствие дат использования льготы
        if(!(row.benefitEndDate.compareTo(row.benefitStartDate) > 0)){
            logger.error("Строка $row.rowNumber :  Неверно указаны даты начала и окончания использования льготы!")
        }

        // 3. Проверка на наличие в списке ТС строк, для которых графы codeOKATO, identNumber, regNumber одинаковы
        if (!checkedRows.contains(row)) {
            errorMsg = ''
            for (def rowIn in dataRows) {
                if (!checkedRows.contains(rowIn) && row != rowIn && row.codeOKATO.equals(rowIn.codeOKATO) && row.identNumber.equals(rowIn.identNumber) && row.regNumber.equals(rowIn.regNumber)) {
                    checkedRows.add(rowIn)
                    errorMsg = ", $rowIn.rowNumber"

                }
            }
            if (!''.equals(errorMsg)) {
                logger.error("Обнаружены строки $row.rowNumber $errorMsg, у которых Код ОКАТО = " + row.codeOKATO + ", " +
                        "Идентификационный номер = " + row.identNumber + ", " +
                        "Регистрационный знак = " + row.regNumber + " совпадают!")
            }
        }
        checkedRows.add(row)

        // 4. Проверка на наличие в списке ТС строк, период использования льготы которых не пересекается с отчётным / налоговым периодом, к которому относится налоговая форма
        // TODO  benefitStartDate...benefitEndDate не пересекается с отчётным/налоговым периодом

        // Проверки соответствия НСИ
        checkNSI(row.getCell('codeOKATO'), "Неверный код ОКАТО!", 3)

        // Проверка льготы
        // TODO проверить
        if (!checkBenefit(row.taxBenefitCode, row.codeOKATO)) {
            logger.error("Строка $row.rowNumber : выбранная льгота для текущего региона не предусмотрена.")
        }
    }
}

// Проверка соответствия НСИ
void checkNSI(Cell cell, String msg, Long id) {
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null)
        logger.error(msg)
}


/**
 * Скрипт для сортировки.
 */
void sort() {

// НЕОБХОДИМО УДАЛИТЬ СОРТИРОВКУ ПО NAME И CODE  
    // сортировка
    // 1 - ОКАТО
    getDataRows().sort { a, b ->
        okatoA = getRefBookValue(3, a.codeOKATO, "OKATO")
        okatoB = getRefBookValue(3, b.codeOKATO, "OKATO")
        int val = okatoA.getStringValue().compareTo(okatoB.getStringValue())

        /*
          if (val == 0) {
              def regionA = getRefBookValue(3, a.regionName, "NAME")
              def regionB = getRefBookValue(3, b.regionName, "NAME")
              val = regionA.getStringValue().compareTo(regionB.getStringValue())

              if (val == 0) {
                  def tsTypeCodeA = getRefBookValue(42, a.tsTypeCode, "CODE")
                  def tsTypeCodeB = getRefBookValue(42, b.tsTypeCode, "CODE")
                  val = tsTypeCodeA.getStringValue().compareTo(tsTypeCodeB.getStringValue())
              }
          }
          */
        return val
    }
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        rows.add(row)
                    }
                }
            }
        }
    }
    formDataService.getDataRowHelper(formData).save(rows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * 2. Проверка льготы
 * Для выбранного значения в «графе 5» (20210, 20220, 20230) в справочнике «Параметры налоговых льгот» существует запись по текущему региону:
 * атрибут «Код региона» справочника «Параметры налоговых льгот» соответствует значению «графы 2» («Код по ОКАТО»).
 */
boolean checkBenefit(taxBenefitCode, okato) {
    if (taxBenefitCode != null && getRefBookValue(6, taxBenefitCode, "CODE").stringValue in ['20210', '20220', '20230']) {
        def refTaxBenefitParameters = refBookFactory.getDataProvider(7)
        def region = getRegionByOkatoOrg(okato)
        query = "TAX_BENEFIT_ID =" + taxBenefitCode + " AND DICT_REGION_ID = " + region.record_id
        if (refTaxBenefitParameters.getRecords(new Date(), null, query, null).getRecords().size() == 0) {
            return false;
        }
    }
    return true;
}

// Получение значения (разменовываение)
def getRefBookValue(refBookID, recordId, alias) {
    def record = refBookFactory.getDataProvider(refBookID).getRecordData(recordId)
    return record != null ? record.get(alias) : null;
}

/**
 * Получение региона по коду ОКАТО
 * @param okato
 */
def getRegionByOkatoOrg(okatoCell) {
    /*
    * первые две цифры проверяемого кода ОКАТО
    * совпадают со значением поля «Определяющая часть кода ОКАТО»
    * справочника «Коды субъектов Российской Федерации»
    */
    // провайдер для справочника - Коды субъектов Российской Федерации
    def okato = getRefBookValue(3, okatoCell, "OKATO")
    def refDataProvider = refBookFactory.getDataProvider(4)
    def records = refDataProvider.getRecords(new Date(), null, "OKATO_DEFINITION like '" + okato.toString().substring(0, 2) + "%'", null).getRecords()

    if (records.size() == 1) {
        return records.get(0);
    } else if (records.size() == 0) {
        logger.error("Не удалось определить регион по коду ОКАТО. Строка: " + row.getIndex())
        return null;
    } else {
        /**
         * Если первые пять цифр кода равны "71140" то код ОКАТО соответствует
         * Ямало-ненецкому АО (код 89 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg89 = records.find { it.OKATO.toString().substring(0, 4).equals("71140") }
        if (reg89 != null) return reg89;

        /**
         * Если первые пять цифр кода равны "71100" то
         * код ОКАТО соответствует Ханты-мансийскому АО
         * (код 86 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg86 = records.find { it.OKATO.toString().substring(0, 4).equals("71100") }
        if (reg86 != null) return reg86;

        /**
         * Если первые четыре цифры кода равны "1110"
         * то код ОКАТО соответствует Ненецкому АО
         * (код 83 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg83 = records.find { it.OKATO.toString().substring(0, 4).equals("1110") }
        if (reg83 != null) return reg83;

        logger.error("Не удалось определить регион по коду ОКАТО. Строка: " + row.getIndex())
        return null;
    }
}
