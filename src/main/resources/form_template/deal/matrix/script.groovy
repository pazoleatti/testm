package form_template.deal.matrix

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

/**
 * 400 - Матрица
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

// 1.	dealNum1	п. 010 "Порядковый номер сделки по уведомлению"
// 2.	interdependenceSing	п. 100
// 3.	f121	п. 121
// 4.	f122	п. 122
// 5.	f123	п. 123
// 6.	f124	п. 124
// 7.	f131	п. 131
// 8.	f132	п. 132
// 9.	f133	п. 133
// 10.	f134	п. 134
// 11.	f135	п. 135 (до 2014 г. / после 2014 г.)
// 12.	similarDealGroup	п. 200 "Группа однородных сделок"
// 13.	dealNameCode	п. 210 "Код наименования сделки"
// 14.	taxpayerSideCode	п. 211 "Код стороны сделки, которой является налогоплательщик"
// 15.	dealPriceSign	п. 220 "Признак определения цены сделки с учетом особенностей, предусмотренных статьей 105.4 НК РФ (регулируемые цены)"
// 16.	dealPriceCode	п. 230 "Код определения цены сделки"
// 17.	dealMemberCount	п. 260 "Количество участников сделки"
// 18.	income	п. 300 "Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 19.	incomeIncludingRegulation	п. 301 "в том числе сумма доходов по сделкам, цены которых подлежат регулированию"
// 20.	outcome	п. 310 "Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях"
// 21.	outcomeIncludingRegulation	п. 311 "в том числе сумма расходов по сделкам, цены которых подлежат регулированию"
// 22.	dealNum2	п. 010 "Порядковый номер сделки по уведомлению (из раздела 1А)"
// 23.	dealType	п. 020 "Тип предмета сделки"
// 24.	dealSubjectName	п. 030 "Наименование предмета сделки"
// 25.	dealSubjectCode1	п. 040 "Код предмета сделки (код по ТН ВЭД)"
// 26.	dealSubjectCode2	п. 043 "Код предмета сделки (код по ОКП)"
// 27.	dealSubjectCode3	п. 045 "Код предмета сделки (код по ОКВЭД)"
// 28.	otherNum	п. 050 "Номер другого участника сделки"
// 29.	contractNum	п. 060 "Номер договора"
// 30.	contractDate	п. 065 "Дата договора"
// 31.	countryCode	п. 070 "Код страны происхождения предмета сделки по классификатору ОКСМ (цифровой)"
// 32.	countryCode1	Код страны по классификатору ОКСМ (цифровой)
// 33.	region1	Регион (код)
// 34.	city1	Город
// 35.	locality1	Населенный пункт (село, поселок и т.д.)
// 36.	countryCode2	Код страны по классификатору ОКСМ (цифровой)
// 37.	region2	Регион (код)
// 38.	city2	Город
// 39.	locality2	Населенный пункт (село, поселок и т.д.)
// 40.	deliveryCode	п. 100 "Код условия поставки (заполняется только для товаров)"
// 41.	okeiCode	п. 110 "Код единицы измерения по ОКЕИ"
// 42.	count	п. 120 "Количество"
// 43.	price	п. 130 "Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб."
// 44.	total	п. 140 "Итого стоимость без учета НДС, акцизов и пошлины, руб."
// 45.	dealDoneDate	п. 150 "Дата совершения сделки (цифрами день, месяц, год)"
// 46.	dealNum3	п. 010 "Порядковый номер сделки (из раздела 1А)"
// 47.	dealMemberNum	п. 015 "Порядковый номер участника сделки (из раздела 1Б)"
// 48.	organInfo	п. 020 "Сведения об организации"
// 49.	countryCode3	п. 030 "Код страны по классификатору ОКСМ"
// 50.	organName	п. 040 "Наименование организации"
// 51.	organINN	п. 050 "ИНН организации"
// 52.	organKPP	п. 060 "КПП организации"
// 53.	organRegNum	п. 070 "Регистрационный номер организации в стране ее регистрации (инкорпорации)"
// 54.	taxpayerCode	п. 080 "Код налогоплательщика в стране регистрации (инкорпорации) или его аналог (если имеется)"
// 55.	address	п. 090 "Адрес"

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование новой матрицы невозможно, т.к. матрица с указанными параметрами уже сформирована.')
    }
}

void addRow() {
    addRow(formData.createDataRow(), currentDataRow)
}

void addRow(DataRow<Cell> row, DataRow<Cell> currentRow) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)

    // TODO пересмотреть редактируемость (пока все редактируемо)
    for (column in formData.getFormColumns()) {
        if (column.alias.equals('dealNum1') || column.alias.equals('dealNum2') || column.alias.equals('dealNum3')
                || column.alias.equals('groupName')) {
            continue
        }
        row.getCell(column.alias).editable = true
        row.getCell(column.alias).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    dataRowHelper.save(dataRowHelper.getAllCached())
}

/**
 * Логические проверки
 */
void logicCheck() {
    def String YES_NO = "Да/Нет"
    def String OKSM = "ОКСМ"
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Обязательные поля
        // TODO нет в ТЗ

        // 2. Проверка наличия элемента справочника «Да/Нет» (графы 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15)
        checkNSI(row, "f121", YES_NO, 38)
        checkNSI(row, "f122", YES_NO, 38)
        checkNSI(row, "f123", YES_NO, 38)
        checkNSI(row, "f124", YES_NO, 38)
        checkNSI(row, "f131", YES_NO, 38)
        checkNSI(row, "f132", YES_NO, 38)
        checkNSI(row, "f133", YES_NO, 38)
        checkNSI(row, "f134", YES_NO, 38)
        checkNSI(row, "f135", YES_NO, 38)
        checkNSI(row, "similarDealGroup", YES_NO, 38)
        // TODO графа 13 не того типа в ТЗ
        checkNSI(row, "dealPriceSign", YES_NO, 38)

        // 3. Проверка наличия элемента справочника «Коды сторон сделки» (графа 14)
        checkNSI(row, "taxpayerSideCode", "Коды стороны сделки", 65)

        // 4 и 5. Проверка наличия элемента справочника «Коды типов предмета сделки» (графы 23, 26)
        checkNSI(row, "dealType", "Коды типов предмета сделки", 64)
        // TODO одно из двух:
        //checkNSI(row, "dealSubjectCode2", "Коды типов предмета сделки", 64)
        //checkNSI(row, "dealSubjectCode2", "Коды ОКП на основании общероссийского классификатора продукции (ОКП)", 68)

        // 6. Проверка наличия элемента справочника «ОКСМ» (графы 31, 32, 36, 49)
        checkNSI(row, "countryCode", OKSM, 10)
        checkNSI(row, "countryCode1", OKSM, 10)
        checkNSI(row, "countryCode2", OKSM, 10)
        checkNSI(row, "countryCode3", OKSM, 10)

        // 7 и 8. Проверка наличия элемента справочника «Коды субъектов Российской Федерации» (графы 33, 37)
        checkNSI(row, "region1", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)

        // 9. Проверка наличия элемента справочника «Коды условий поставки»  в графе 40)
        checkNSI(row, "deliveryCode", "Коды условий поставки", 63)

        // 10. Проверка наличия элемента справочника «Коды единиц измерения на основании ОКЕИ»  (графа 41)
        checkNSI(row, "okeiCode", "Коды единиц измерения на основании ОКЕИ", 12)

        // 11. Проверка наличия элемента справочника «Сведения об организации» (графа 48)
        checkNSI(row, "organInfo", "Сведения об организации", 70)
    }
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("В справочнике «$msg» не найден элемент графы «$msg2», указанный в строке $rowNum!")
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // Порядковый номер строки
        row.dealNum1 = index
        row.dealNum2 = index
        row.dealNum3 = index
        index++
    }

    dataRowHelper.update(dataRows)
}

/**
 * Сортировка строк по графе п. 040 "Наименование организации"
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.sort { it.organName }
    dataRowHelper.save(dataRows);
}
/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()
        def int index = 1
        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }
            if (row.getAlias() == null) {
            }
            if (nextRow == null || row.organName != nextRow.organName) {

                def newRow = formData.createDataRow()
                newRow.getCell('groupName').colSpan = 56
                if (row.organName != null)
                    newRow.groupName = refBookService.getRecordData(9, row.organName).NAME.stringValue
                newRow.setAlias('grp#'.concat(i.toString()))
                dataRowHelper.insert(newRow, ++i +1 - index)
                index = 1
            } else {
                index++
            }
        }
    }
}
/**
 * Удаление всех статическиех строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias() == null) {
                    def row = buildRow(srcRow, source.getFormType())
                    addRow(row, null)
                }
            }
        }
    }
}

/**
 * Подготовка строки матрицы из первичных и консолидированных отчетов модуля УКС
 * @param row
 * @param type
 */
DataRow<Cell> buildRow(DataRow<Cell> srcRow, FormType type) {
    println(">>> buildRow type = "+type.id+" "+type.name+" srcRow = "+srcRow)
    // Общие значения

    // "Да"
    def Long recYesId = null
    // "Нет"
    def Long recNoId = null

    def valYes = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 0", null)
    def valNo = refBookFactory.getDataProvider(38L).getRecords(new Date(), null, "CODE = 1", null)
    if (valYes != null && valYes.size() == 1) {
        recYesId = valYes.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
    }
    if (valNo != null && valNo.size() == 1) {
        recNoId = valNo.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
    }

    def DataRow<Cell> row = formData.createDataRow()
    /*
    // Графа 2
    def val2 = refBookFactory.getDataProvider(69L).getRecords(new Date(), null, "CODE = 1", null)
    if (val2 != null && val2.size() == 1) {
        row.interdependenceSing = val2.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
    }

    // Графа 3
    // row.f121, заполняется после графы 50

    // Графа 4
    row.f122 = recNoId

    // Графа 5
    // row.f123, заполняется после графы 50

    // Графа 6
    row.f124 = recNoId

    // Графа 7
    // row.f131, заполняется после графы 50

    // Графа 8
    // row.f132, заполняется после графы 50

    // Графа 9
    // row.f133, заполняется после графы 50

    // Графа 10
    // row.f134, заполняется после графы 50

    // Графа 11
    // row.f135, заполняется после графы 45

    // Графа 12
    row.similarDealGroup = recNoId

    // Графа 13
    def String val13 = null
    switch (type.id) {
        case 376:
            val13 = '002'
            break
        case 377:
        case 380:
        case 382:
        case 375:
            val13 = '019'
            break
        case 379:
        case 381:
        case 393:
        case 394:
            val13 = '016'
            break
        case 383:
        case 391:
        case 392:
            val13 = '032'
            break
        case 384:
            val13 = '015'
            break
        case 385:
            val13 = '029'
            break
        case 386:
        case 388:
            val13 = '003'
            break
        case 387:
        case 389:
            val13 = '012'
            break
        case 390:
            val13 = '017'
            break
    }
    if (val13 != null && type.id != 375) {
        def values13 = refBookFactory.getDataProvider(67L).getRecords(new Date(), null, "NAME = '$val13'", null)
        if (values13 != null && values13.size() == 1) {
            row.dealNameCode = values13.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // Графа 14
    def String val14 = null
    switch (type.id) {
        case 376:
            val14 = '004'
            break
        case 377:
        case 375:
        case 380:
            val14 = '012'
            break
        case 379:
            val14 = '029'
            break
        case 381:
            if (srcRow.outcomeSum == null) {
                val14 = '027'
            }
            if (srcRow.incomeSum == null) {
                val14 = '026'
            }
            break
        case 382:
            val14 = '011'
            break
        case 383:
        case 391:
        case 392:
        case 393:
            val14 = '052'
            break
        case 384:
            if (srcRow.transactionType != null) {
                def val14Rec = refBookFactory.getDataProvider(16L).getRecordData(srcRow.transactionType)
                if (val14Rec.CODE != null) {
                    if (val14Rec.CODE.equals('S')) {
                        val14 = '027'
                    } else if (val14Rec.CODE.equals('B')) {
                        val14 = '026'
                    }
                }
            }
            break
        case 385:
        case 387:
        case 389:
            val14 = '022'
            break
        case 386:
        case 388:
            val14 = '005'
            break
        case 390:
            val14 = '030'
            break
        case 394:
            if (srcRow.outcomeSum == null) {
                val14 = '027'
            }
            if (srcRow.incomeSum == null) {
                val14 = '026'
            }
            break
    }
    if (val14 != null) {
        def values14 = refBookFactory.getDataProvider(65L).getRecords(new Date(), null, "CODE = '$val14'", null)
        if (values14 != null && values14.size() == 1) {
            row.taxpayerSideCode = values14.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // Графа 15
    // справочное, заполняется после графы 50, по-умолчанию 0
    row.dealPriceSign = recNoId

    // Графа 16
    def int val16 = 0
    switch (type.id) {
        case 385:
            val16 = 3
            break
        case 384:
            if (srcRow.transactionMode != null) {
                def val16Rec = refBookFactory.getDataProvider(14L).getRecordData(srcRow.transactionMode)
                if (val16Rec.ID != null && val16Rec.ID == 1) {
                    val16 = 2
                }
            }
            break
    }
    def values16 = refBookFactory.getDataProvider(66L).getRecords(new Date(), null, "CODE = $val16", null)
    if (values16 != null && values16.size() == 1) {
        row.dealPriceCode = values16.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
    }

    // Графа 17
    row.dealMemberCount = 2

    // Графа 18
    switch (type.id) {
        case 376:
            row.income = srcRow.incomeBankSum
            break
        case 382:
            row.income = srcRow.bankIncomeSum
            break
        case 379:
        case 387:
            row.income = srcRow.sum
            break
        case 377:
        case 375:
        case 380:
            row.income = 0
            break
        case 381:
            row.income = srcRow.cost
            break
        case 383:
            row.income = srcRow.priceFirstRub
            break
        case 384:
            row.income = srcRow.transactionSumRub
            break
        case 385:
            row.income = srcRow.totalCost
            break
        case 386:
        case 388:
        case 389:
            row.income = srcRow.total
            break
        case 390:
        case 391:
            row.income = srcRow.total
            break
        case 392:
            row.income = srcRow.cost
            break
        case 393:
            row.income = srcRow.totalNds
            break
        case 394:
            row.income = srcRow.total
            break
    }

    // Графа 20
    switch (type.id) {
        case 377:
            row.outcome = srcRow.bankSum
        case 375:
            row.outcome = srcRow.expensesSum
        case 380:
            row.outcome = srcRow.sum
            break
    }

    // Графа 23
    def int val23 = 2
    switch (type.id) {
        case 379:
        case 385:
            val23 = 3
            break
        case 393:
        case 394:
            val23 = 1
            break
    }
    def values23 = refBookFactory.getDataProvider(64L).getRecords(new Date(), null, "CODE = $val23", null)
    if (values23 != null && values23.size() == 1) {
        row.dealType = values23.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
    }

    // Графа 24
    switch (type.id) {
        case 376:
            row.dealSubjectName = 'Предоставление в аренду нежилых помещений'
            break
        case 377:
            row.dealSubjectName = 'Оказание услуг по техническому обслуживанию'
            break
        case 375:
            row.dealSubjectName = 'Информационно-технологические услуги'
            break
        case 379:
            row.dealSubjectName = 'Предоставление прав пользования торговым знаком'
            break
        case 380:
            row.dealSubjectName = 'Приобретение услуг по организации и проведению торгов по реализации имущества'
            break
        case 381:
            row.dealSubjectName = 'Купля-продажа ценных бумаг'
            break
        case 382:
        case 384:
            row.dealSubjectName = 'Оказание банковских услуг'
            break
        case 383:
            row.dealSubjectName = 'РЕПО'
            break
        case 385:
            row.dealSubjectName = 'Уступка прав требования по кредитным договорам'
            break
        case 386:
        case 388:
            row.dealSubjectName = 'Предоставление банковских гарантий и иных аналогичных инструментов'
            break
        case 387:
        case 389:
            row.dealSubjectName = 'Предоставление денежных средств на условиях возвратности, платности, срочности'
            break
        case 390:
            row.dealSubjectName = 'Операции с иностранной валютой'
            break
        case 391:
        case 392:
            row.dealSubjectName = 'Операции с финансовыми инструментами срочных сделок'
            break
        case 393:
        case 394:
            row.dealSubjectName = 'Купля-продажа драгоценного металла'
            break
    }

    // Графа 26
    if (type.id == 393) {
        dealSubjectCode2 = srcRow.okpCode
    }

    // Графа 27
    def String val27 = null
    switch (type.id) {
        case 376:
            val27 = '70.20.2'
            break
        case 377:
            val27 = '70.32.2'
            break
        case 375:
            val27 = '72.2'
            break
        case 379:
        case 380:
            val27 = '74.8'
            break
        case 381:
        case 384:
        case 386:
        case 388:
        case 391:
        case 392:
        case 393:
        case 394:
            val27 = '65.23'
            break
        case 382:
            val27 = '67.12'
            break
        case 383:
        case 385:
        case 387:
        case 389:
            val27 = '65.22'
            break
        case 390:
            val27 = '65.12'
            break
    }

    if (val27 != null) {
        def values27 = refBookFactory.getDataProvider(34L).getRecords(new Date(), null, "CODE = '$val27'", null)
        if (values27 != null && values27.size() == 1) {
            row.dealSubjectCode3 = values27.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
    }

    // Графа 28
    row.otherNum = 1

    // Графа 29
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
            row.contractNum = srcRow.contractNum
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 391:
        case 394:
            row.contractNum = srcRow.docNumber
            break
        case 390:
            row.contractNum = srcRow.docNum
            break
    }

    // Графа 30
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
            row.contractDate = srcRow.contractDate
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            row.contractDate = srcRow.docDate
            break
    }

    // Заполнение графы 15
    Calendar compareCalendar15 = Calendar.getInstance()
    compareCalendar15.set(2011, 12, 28)
    if (compareCalendar15.getTime().equals(row.contractDate) && "123".equals(row.contractNum)) {
        row.dealPriceSign = recYesId
    }

    // Графа 31
    switch (type.id) {
        case 393:
            row.countryCode = srcRow.unitCountryCode
            break
        case 394:
            row.countryCode = srcRow.countryCodeNumeric
            break
    }

    // Графа 32, Графа 33, Графа 34, Графа 35
    if (type.id == 393 || type.id == 394) {
        def values32 = refBookFactory.getDataProvider(18L).getRecordData(srcRow.signPhis)
        if (values32 != null && values32.SIGN.stringValue.equals("Физическая поставка")) {
            if (type.id == 393) {
                row.countryCode1 = srcRow.countryCode2
                row.region1 = srcRow.region1
                row.city1 = srcRow.city1
                row.locality1 = srcRow.settlement1
            }

            if (type.id == 394) {
                row.countryCode1 = srcRow.countryCodeNumeric
                row.region1 = srcRow.regionCode
                row.city1 = srcRow.city
                row.locality1 = srcRow.locality
            }
        }
    }

    // Графа 36, Графа 37, Графа 38, Графа 39
    switch (type.id) {
        case 376:
        case 377:
            row.countryCode2 = srcRow.country
            row.region2 = srcRow.region
            row.city2 = srcRow.city
            row.locality2 = srcRow.settlement
            break
        case 393:
            row.countryCode2 = srcRow.countryCode3
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.settlement2
            break
        case 394:
            row.countryCode2 = srcRow.countryCodeNumeric2
            row.region2 = srcRow.region2
            row.city2 = srcRow.city2
            row.locality2 = srcRow.locality2
            break
        default:
            def values36 = refBookFactory.getDataProvider(10L).getRecords(new Date(), null, "CODE = '643'", null)
            if (values36 != null && values36.size() == 1) {
                row.countryCode2 = values36.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            }
            def values37 = refBookFactory.getDataProvider(4L).getRecords(new Date(), null, "CODE = '77'", null)
            if (values37 != null && values37.size() == 1) {
                row.region2 = values37.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            }
            row.city2 = 'Москва'
            row.locality2 = row.city2
            break
    }

    // Графа 40
    if (type.id == 393) {
        row.deliveryCode = srcRow.conditionCode
    }
    if (type.id == 394) {
        row.deliveryCode = srcRow.deliveryCode
    }

    // Графа 41
    if (type.id == 381 || type.id == 385) {
        row.okeiCode = srcRow.okeiCode
    } else {
        def String val41 = null
        switch (type.id) {
            case 376:
            case 377:
                val41 = '055'
                break
            case 375:
            case 379:
            case 380:
            case 382:
            case 383:
            case 384:
            case 390:
            case 391:
            case 392:
            case 386:
            case 387:
            case 388:
            case 389:
            case 393:
            case 394:
                val41 = '796'
                break
        }
        if (val41 != null ) {
            def values41 = refBookFactory.getDataProvider(12L).getRecords(new Date(), null, "CODE = '$val41'", null)
            if (values41 != null && values41.size() == 1) {
                row.okeiCode = values41.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
            }
        }
    }

    // Графа 42
    switch (type.id) {
        case 376:
        case 377:
        case 381:
        case 385:
        case 387:
        case 389:
        case 393:
        case 394:
            row.count = srcRow.count
            break
        case 384:
            row.count = srcRow.bondCount
            break
        default:
            row.count = 1
            breal
    }

    // Графа 43
    switch (type.id) {
        case 376:
        case 377:
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
            row.price = srcRow.price
            break
        case 383:
            row.price = srcRow.priceFirstCurrency
            break
        case 384:
            row.price = srcRow.priceOne
            break
        case 385:
        case 386:
        case 388:
        case 389:
        case 394:
            row.price = srcRow.price
            break
    }

    // Графа 44
    switch (type.id) {
        case 376:
        case 377:
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
            row.total = srcRow.cost
            break
        case 385:
            row.total = srcRow.totalCost
            break
        case 386:
        case 388:
        case 389:
        case 394:
            row.total = srcRow.total
            break
        case 390:
        case 391:
            row.total = srcRow.total
            break
        case 392:
            row.total = srcRow.cost
            break
        case 393:
            row.total = srcRow.totalNds
            break
    }

    // Графа 45
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 385:
        case 392:
        case 393:
            row.dealDoneDate = srcRow.transactionDate
            break
        case 375:
        case 379:
        case 381:
        case 387:
            row.dealDoneDate = srcRow.dealDate
            break
        case 380:
            row.dealDoneDate = srcRow.date
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            row.dealDoneDate = srcRow.dealDoneDate
            break
        case 384:
            row.dealDoneDate = srcRow.transactionDeliveryDate
            break
    }

    // Графа 47
    row.dealMemberNum = row.otherNum

    // Графа 49
    switch (type.id) {
        case 376:
        case 377:
            row.countryCode3 = srcRow.country
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
            row.countryCode3 = srcRow.countryCode
            break
        case 383:
        case 389:
        case 390:
        case 391:
        case 392:
        case 394:
            row.countryCode3 = srcRow.countryCode
            break
        case 393:
            row.countryCode3 = srcRow.countryCode1
            break
        case 384:
            row.countryCode3 = srcRow.contraCountryCode
            break
        case 385:
            row.countryCode3 = srcRow.country
            break
        case 386:
            row.countryCode3 = srcRow.countryCode
            break
        case 388:
            row.countryCode3 = srcRow.countryName
            break
        case 387:
            row.countryCode3 = srcRow.countryName
            break
    }

    // Графа 50
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
            row.organName = srcRow.jurName
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 387:
            row.organName = srcRow.fullNamePerson
            break
        case 384:
            row.organName = srcRow.contraName
            break
        case 385:
        case 392:
        case 393:
            row.organName = srcRow.name
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            row.organName = srcRow.fullName
            break
    }
    */
    // Графа 3
    // TODO Вопрос по атрибуту
    // def val2 = refBookFactory.getDataProvider(9L).getRecordData(row.organName)
    row.f121 = recYesId

    // Графа 5
    // TODO Вопрос по атрибуту
    row.f123 = recYesId

    // Графа 7
    // TODO Вопрос по атрибуту
    row.f131 = recYesId

    // Графа 8
    // TODO Вопрос по атрибуту
    row.f132 = recYesId

    // Графа 9
    // TODO Вопрос по атрибуту
    row.f133 = recYesId

    // Графа 10

    // TODO заполнить далее графа_10
    // Графа 11
    if (row.dealDoneDate != null || row.organName != null) {
        Calendar compareCalendar11 = Calendar.getInstance()
        compareCalendar11.set(2014, 1, 1)

        def val11 = refBookFactory.getDataProvider(9L).getRecordData(row.organName)

        // TODO Вопрос по атрибуту
        //if(row.dealDoneDate.before(compareCalendar11.getTime()) || (val11 != null && val11.???.numVal == 1)) {
        //  row.f135 = recNoId
        //}
    }

    // Графа 48
    if (row.organName != null) {
        def val48 = refBookFactory.getDataProvider(9L).getRecordData(row.organName)
        row.organInfo = val48.ORGANIZATION.stringValue;
    }

    // Графа 51
    // row.organINN = // Справочное TODO из графы 50

    // Графа 52
    // row.organKPP = // Справочное TODO из графы 50

    // Графа 53
    // row.organRegNum = // Справочное TODO из графы 50

    // Графа 54
    // row.taxpayerCode = // Справочное TODO из графы 50

    // Графа 55
    // row.address = // Справочное TODO из графы 50

    return row
}