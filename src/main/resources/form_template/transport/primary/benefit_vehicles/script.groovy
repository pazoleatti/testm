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

/**
 * Проверки первичных / консолидированных налоговых форма
 */

// Инициирование Пользователем перехода «Подготовить»     //..
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        // (Ramil Timerbaev) проверка производится в ядре
        // 2.	Логические проверки значений налоговой формы.
        //       logicalChecks()
        // 3.	Проверки соответствия НСИ.
        //       checkNSI()

        break

// Инициирование Пользователем  выполнение перехода в статус «Принята» из Подготовлена      //..
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        // (Ramil Timerbaev) проверка производится в ядре
        // 2.	Логические проверки значений налоговой формы.
        //       logicalChecks()
        // 3.	Проверки соответствия НСИ.
        //       checkNSI()

        break

// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:    //..
        // 1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        // (Ramil Timerbaev) проверка производится в ядре

        break

// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:    //..
        // (Ramil Timerbaev) проверка производится в ядре
        break

// после вернуть из принята в подготовлена
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_PREPARED:    //..
        // (Ramil Timerbaev) проверка производится в ядре
        break

//отменить принятие консолидированной формы
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:    //..
        // 1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
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

        // Проверка кода льготы
// НУЖНО ДОПИСАТЬ ПРОВЕРКУ КОДА ЛЬГОТЫ

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
 * 	Скрипт для определения наличия идентичных строк в форме строке nrow
 *
 * @param nrow строка
 */
boolean isRowInDataRows(def nrow) {
    getDataRows().each { row ->
        if (row.equals(nrow)) {
            return true
        }
    }
    return false
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
boolean checkBenefit(taxBenefitCode){
  //!!!   НУЖНО ДОБАВИТЬ ПРОВЕРКУ ДЛЯ КОДА ЛЬГОТЫ, АНАЛОГИЧНО СВОДНОЙ. 
  	// (gavanesov) скопировано из сводной
 /* 
	if (row.taxBenefitCode != null){
        def refTaxBenefitParameters = refBookFactory.getDataProvider(7)
        def region = getRegionByOkatoOrg(row.okato)
        query = "TAX_BENEFIT_ID ="+row.taxBenefitCode+" AND DICT_REGION_ID = "+region.record_id
        if (refTaxBenefitParameters.getRecords(new Date(), null, query, null).getRecords().size() == 0){
                logger.error("Выбранная льгота для текущего региона не предусмотрена . Строка: "+row.getIndex())
    	}
    }  
  
  */
    return false;
}


/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias){
    //logger.info("refBookID, recordId, alias = "+refBookID+", "+ recordId+", "+alias)
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
