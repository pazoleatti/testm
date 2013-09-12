import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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
	// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkBeforeCreate()
        break

	// Инициирование Пользователем расчёта данных формы (нажатие на кнопку «Рассчитать»)
    case FormDataEvent.CALCULATE:

        if (checkRequiredField()){
            fillForm()
            checkNSI()
            logicalChecks()
            sort()
            // сохраним данные
            getDataRowHelper().save(getDataRows())
        }

        break

	// Инициирование Пользователем проверки данных формы
    case FormDataEvent.CHECK:

        checkRequiredField()
        logicalChecks()
        checkNSI()

        break
	// Инициирование Пользователем добавления строки
    case FormDataEvent.ADD_ROW:
        // добавляем строку
        addRow()
        // проставляем порядковый номер №пп
        setRowIndex()
        //
        getDataRowHelper().save(getDataRows())
        break

	// Инициирование Пользователем удаления строки
    case FormDataEvent.DELETE_ROW:
        // удаляем строку
        deleteRow()
        // проставляем порядковый номер №пп
        setRowIndex()

        getDataRowHelper().save(getDataRows())
        break

    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        checkRequiredField()
        logicalChecks()
        checkNSI()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:    //..
        // (Ramil Timerbaev) проверка производится в ядре
        break
// обобщить (3.10.1)
    case FormDataEvent.COMPOSE:
        // 1.	Объединение строк (см. раздел 3.10.2).
        // 1.1.	Система должна перенести строки из форм-источников в форму-приемник.
        consolidation()
        //1.2.	Система должна выполнить расчет значений строк итогов (либо фиксированных строк).
        fillForm()
        //1.3.	Система должна выполнить сортировку строк.
        sort()
        //1.4.	Система должна заполнить значение графы «№ пп».
        setRowIndex()
        getDataRowHelper().save(getDataRows())
        getDataRowHelper().commit()

        break
}

/**
 * Скрипт для добавления новой строки.
 */
void addRow() {
    def newRow = formData.createDataRow()

    ['codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each { column ->
        newRow.getCell(column).editable = true
        newRow.getCell(column).setStyleAlias("Редактируемое поле")
    }

    def index = (currentDataRow != null ? currentDataRow.getIndex() : getDataRows().size())
    dataRowHelper.insert(newRow, index + 1)
}

/**
 * Установка номера строки.
 */
void setRowIndex() {
    def index = 1
    for (def row : getDataRows()) {
        row.rowNumber = index++
    }

}

/**
 * Скрипт для проверки создания.
 */
void checkBeforeCreate() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

}

/**
 * Проверка обязательных полей.
 */
def checkRequiredField() {
    for (def row : getDataRows()) {

        def errorMsg = ''

        // 2-7
        ['codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each { column ->
            if (row.getCell(column) != null && (row.getCell(column).getValue() == null || ''.equals(row.getCell(column).getValue()))) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(column).getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("В строке "+row.getIndex()+" не заполнены поля : $errorMsg.")
            return false;
        }
    }
    return true
}

/*
 * Скрипт для удаления строки.
 */
void deleteRow() {
    def row = currentDataRow
    if (row != null) {
        // удаление строки
        getDataRowHelper().delete(row)
    }
}

/**
 * Скрипт логические проверки сводной формы.
 */
void logicalChecks() {
    for (def row : getDataRows()) {
        // Поверка на соответствие дат использования льготы
        if (!(row.benefitEndDate > row.benefitStartDate)) {
            logger.error("Строка $row.rowNumber : Неверно указаны Даты начала и Дата окончания использования льготы! !")
        }
    }

    /**
     * Во вторую форму по транспорту (202) добавить лог. проверку того,
     * что форма не содержит записей с разными льготами для одинаковых ТС
     * (ТС с одинаковым кодом ОКАТО, Идентификационным номером, рег. номером).
     */

    // Ошибки которые возникли
    def errors = [];

    for (def row : getDataRows()){
        // текущий вариант проверки
        def variant = [
            okato : row.codeOKATO,
            identNumber : row.identNumber,
            regNumber: row.regNumber,
            lines: []
        ]
        variant.lines.add(row.getIndex())


        // проверка с остальными строками
        for (def r : getDataRows()) {

            if (!r.getIndex().equals(row.getIndex())){
                if (r.codeOKATO.equals(row.codeOKATO)
                        && r.identNumber.equals(row.identNumber)
                        && r.regNumber.equals(row.regNumber)
                        && (
                         r.taxBenefitCode != row.taxBenefitCode ||
                        !r.benefitStartDate.equals(row.benefitStartDate) ||
                        !r.benefitEndDate.equals(row.benefitEndDate)
                )
                ){
                    variant.lines.add(r.getIndex())
                }
            }
        }

        def contains = errors.findAll{ el ->
            el.codeOKATO.equals(row.codeOKATO)
            el.identNumber.equals(row.identNumber)
            el.regNumber.equals(row.regNumber)
        }
        if (contains.size() == 0){
            errors.add(variant)
        }
    }

    // показ ошибок
    errors.each{ e ->
        if (e.lines.size() > 1){
            logger.error("Форма содержит записи с разными льготами для одинаковых ТС. Строки: "+e.lines.join(', '))
        }
    }

}

/**
 * Скрипт для проверки соответствия НСИ.
 */
void checkNSI() {
    for (def row : getDataRows()) {

        // Проверка ОКАТО
        if (!checkOkato(row.codeOKATO)){
            logger.error("Неверный код ОКАТО. Строка: "+row.getIndex());
        }

        /**
         * Проверка льготы
         */
        if (!checkBenefit(row.taxBenefitCode, row.codeOKATO)){
            logger.error("Выбранная льгота для текущего региона не предусмотрена . Строка: "+row.getIndex())
        }

    }
}

/**
 * Алгоритмы заполнения полей формы
 */
void fillForm() {
    getDataRows().each { row ->

		//ничего не делаем
    }
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

/**
 * Консолидация.
 */
void consolidation() {
    // удаляем все строки
    getDataRows().clear()

    // скопировать строки из источников в данную форму
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                int cnt = 1
                sourceDataRows.each { row ->
                    row.rowNumber = cnt++
                    dataRowHelper.insert(row, getDataRows().size() ?: 1)
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * 1. Проверка ОКАТО
 * В справочнике «Коды ОКАТО» должна быть строка, для которой выполняется условие:
 * «графа 2» (поле «Код ОКАТО») текущей строки формы = «графа 1» (поле «Код ОКАТО») строки справочника
 */
boolean checkOkato(codeOKATO){
    if (codeOKATO != null && getRefBookValue(3, codeOKATO, "OKATO") != null){
            return true;
    }
    return false;
}

/**
 * 2. Проверка льготы
 * Для выбранного значения в «графе 5» (20210, 20220, 20230) в справочнике «Параметры налоговых льгот» существует запись по текущему региону:
 * атрибут «Код региона» справочника «Параметры налоговых льгот» соответствует значению «графы 2» («Код по ОКАТО»).
 */
boolean checkBenefit(taxBenefitCode, okato){
	if (taxBenefitCode != null && getRefBookValue(6, taxBenefitCode, "CODE") in [20210, 20220, 20230]){
        def refTaxBenefitParameters = refBookFactory.getDataProvider(7)
        def region = getRegionByOkatoOrg(okato)
        query = "TAX_BENEFIT_ID ="+taxBenefitCode+" AND DICT_REGION_ID = "+region.record_id
        if (refTaxBenefitParameters.getRecords(new Date(), null, query, null).getRecords().size() == 0){
            return false;
    	}
    }
    return true;
}


/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias){
    def  refDataProvider = refBookFactory.getDataProvider(refBookID)
    def record = refDataProvider.getRecordData(recordId)

    return record != null ? record.get(alias) : null;
}


def getDataRows() {
    dataRowHelper.getAllCached()
}


def getDataRowHelper() {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    } else {
        throw new Exception("Ошибка получения dataRows")
    }
}

/**
 * Получение региона по коду ОКАТО
 * @param okato
 */
def getRegionByOkatoOrg(okatoCell){
    /*
    * первые две цифры проверяемого кода ОКАТО
    * совпадают со значением поля «Определяющая часть кода ОКАТО»
    * справочника «Коды субъектов Российской Федерации»
    */
    // провайдер для справочника - Коды субъектов Российской Федерации
    def okato =  getRefBookValue(3, okatoCell, "OKATO")
    def  refDataProvider = refBookFactory.getDataProvider(4)
    def records = refDataProvider.getRecords(new Date(), null, "OKATO_DEFINITION like '"+okato.toString().substring(0, 2)+"%'", null).getRecords()

    if (records.size() == 1){
        return records.get(0);
    } else if (records.size() == 0){
        logger.error("Не удалось определить регион по коду ОКАТО. Строка: "+row.getIndex())
        return null;
    } else{
        /**
         * Если первые пять цифр кода равны "71140" то код ОКАТО соответствует
         * Ямало-ненецкому АО (код 89 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg89 = records.find{ it.OKATO.toString().substring(0, 4).equals("71140")}
        if (reg89 != null) return reg89;

        /**
         * Если первые пять цифр кода равны "71100" то
         * код ОКАТО соответствует Ханты-мансийскому АО
         * (код 86 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg86 = records.find{ it.OKATO.toString().substring(0, 4).equals("71100")}
        if (reg86 != null) return reg86;

        /**
         * Если первые четыре цифры кода равны "1110"
         * то код ОКАТО соответствует Ненецкому АО
         * (код 83 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg83 = records.find{ it.OKATO.toString().substring(0, 4).equals("1110")}
        if (reg83 != null) return reg83;

        logger.error("Не удалось определить регион по коду ОКАТО. Строка: "+row.getIndex())
        return null;
    }
}
