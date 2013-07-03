/**
 * Сводная форма " Доходы, учитываемые в простых РНУ" уровня обособленного подразделения
 *
 * @since 6.06.2013
 * @author auldanov
 */

/**
 * Состав графов
 * 1. КНУ - incomeTypeId
 * 2. Группа доходов - incomeGroup
 * 3. Вид дохода по операции - incomeTypeByOperation
 * 4. Балансовый счёт по учёту дохода - accountNo
 * 5. РНУ-6 (графа 10) cумма - rnu6Field10Sum
 * 6. сумма - rnu6Field12Accepted
 * 7. в т.ч. учтено в предыдущих налоговых периодах по графе 10 - rnu6Field12PrevTaxPeriod
 * 8. РНУ-4 (графа 5) сумма - rnu4Field5Accepted
 * 9. Логическая проверка - logicalCheck
 * 10. Счёт бухгалтерского учёта - accountingRecords
 * 11. в Приложении №5 - opuSumByEnclosure2
 * 12. в Таблице "Д" - opuSumByTableD
 * 13. в бухгалтерской отчётности - opuSumTotal
 * 14. Расхождение - difference
 *
 */


/**
 * Роутинг приложения
 * formDataEvent (com.aplana.sbrf.taxaccounting.model.FormDataEvent)
 */
switch (formDataEvent){
// TODO написать роутинг
// создать
    case FormDataEvent.CREATE :
        checkCreation()
        break
// расчитать
    case FormDataEvent.CALCULATE :
        logicalCheck()
        calcForm()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        logicalCheck()
        calcForm()
        break
// проверить
    case FormDataEvent.CHECK :
        logicalCheck()
        calcForm()
        break
// утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        logicalCheck()
        calcForm()
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        logicalCheck()
        calcForm()
        break
// вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        logicalCheck()
        calcForm()
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkDeclarationBankOnCancelAcceptance()
        break
// после принятия из утверждена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED :
        break
// после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        break
}

/**
 * 6.1.2.8.2    Алгоритмы заполнения полей формы при расчете данных формы
 */
def calcForm(){
    /** КНУ 40001 */
    def row40001 = formData.getDataRow("R53")
    (2..52).each{ n ->
        // «графа 5» =сумма значений  «графы 5» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum?:0)  + (formData.getDataRow("R"+n).rnu6Field10Sum?:0)

        // «графа 6» =сумма значений  «графы 6» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted?:0) + (formData.getDataRow("R"+n).rnu6Field12Accepted ?:0)

        // «графа 7» =сумма значений  «графы 7» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod?:0) + (formData.getDataRow("R"+n).rnu6Field12PrevTaxPeriod ?:0)

        // «графа 8» =сумма значений  «графы 8» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted?:0) + (formData.getDataRow("R"+n).rnu4Field5Accepted ?:0)
    }


    /** КНУ 40002 */
    def row40002 = formData.getDataRow("R156")
    (55..155).each{ n ->
        // «графа 5» =сумма значений  «графы 5» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum?:0) + (formData.getDataRow("R"+n).rnu6Field10Sum?:0)

        // «графа 6» =сумма значений  «графы 6» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted?:0) + (formData.getDataRow("R"+n).rnu6Field12Accepted?:0)

        // «графа 7» =сумма значений  «графы 7» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod?:0) + (formData.getDataRow("R"+n).rnu6Field12PrevTaxPeriod?:0)

        // «графа 8» =сумма значений  «графы 8» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted?:0) + (formData.getDataRow("R"+n).rnu4Field5Accepted?:0)
    }
}

/**
 * 6.1.2.8.3.1  Логические проверки
 * Завяжемся на цвета а не на номера строк,
 * а-ля точное отображение аналитики
 */
def logicalCheck(){
    logger.info('-->');
    formData.dataRows.each{ row ->
        /**
         * Графа 9
         * Номера строк: Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * Если Сумма <0, то «графа 9»= «ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ » Иначе «графа 9»= Сумма ,где
         * Сумма= ОКРУГЛ( «графа5»-(«графа 6»-«графа 7»);2)
         */
        if (isCalcField(row.getCell("logicalCheck"))){
            row.logicalCheck = ((BigDecimal) ((row.rnu6Field10Sum?:0) - (row.rnu6Field12Accepted?:0) + (row.rnu6Field12PrevTaxPeriod?:0))).setScale(2, BigDecimal.ROUND_HALF_UP).toString() ?: "Требуется объяснение"
        }


        /**
         * Графа 11
         * Номера строк: Все строки. Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * «графа 11» = сумма значений «графы 6» формы «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
         * для тех строк, для которых значение «графы 4»  равно значению «графы 4» текущей строки
         *
         * TODO Гриша сказал что строка там только одна будет и он изменит аналитику
         * TODO При получении доходов сложных проверить статус нужно?
         */
        if (isCalcField(row.getCell("opuSumByEnclosure2"))){
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = FormDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
            if (formData302 != null){
                formData302.dataRows.each{ rowOfForm302 ->
                    if (rowOfForm302.incomeBuhSumAccountNumber == row.accountNo){
                        sum6ColumnOfForm302 += rowOfForm302.incomeBuhSumAccepted ?:0
                    }
                }
                row.opuSumByEnclosure2 = sum6ColumnOfForm302
            }
        }


        /**
         * Графа 12
         * Номера строк: Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         *
         * Алгоритм заполннеия:
         * «графа 12» = сумма значений «графы 8» для тех строк,
         * для которых значение «графы 4»  равно значению «графы 4» текущей строки.
         *
         * TODO Гриша сказал что строка там только одна будет и он изменит аналитику
         */
        if (isCalcField(row.getCell("opuSumByTableD"))){
            def sum8Column = 0
            formData.dataRows.each{ irow ->
                if (irow.accountNo == row.accountNo){
                    sum8Column += irow.rnu4Field5Accepted?:0
                }
            }
            row.opuSumByTableD = sum8Column
        }


        /**
         * Графа 13
         * Номера строк:
         * Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         * за исключением строк  118-119, 141-142
         *
         * Алгоритм заполннеия:
         * «графа13» =  сумма значений поля «Сумма, руб» для всех записей «Отчета о прибылях и убытках»  для которых выполняются следующие условия:
         * Выбираются данные отчета за период, для которого сформирована текущая форма
         * Значение поля «Кода ОПУ» Отчета о прибылях и убытках совпадает со значением «графы 10» текущей строки текущей формы.
         */
        if (isCalcField(row.getCell("opuSumTotal")) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])){
            income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords, formData.departmentId).each{ income102 ->
                row.opuSumTotal = (row.opuSumTotal?:0) + income102.totalSum
            }
        }

        /**
         * Графа 13
         * Номера строк: 118-119, 141-142
         *
         * Алгоритм заполннеия:
         *  «графа13» =сумма значений поля «Обороты по дебету, руб» для всех записей «Оборотной Ведомости», для которых выполняются следующие условия:
         *  Выбираются данные отчета за период, для которого сформирована текущая форма
         *  Значение поля «Номер счета» совпадает совпадает со значением «графы 10» текущей строки текущей формы.
         */
        if (row.getAlias() in ['R118', 'R119', 'R141', 'R142']){
            income101Dao.getIncome101(formData.reportPeriodId, row.accountingRecords, formData.departmentId).each{ income101 ->
                row.opuSumTotal =  (row.opuSumTotal?:0) + income101.debetRate
            }
        }




        /**
         * Графа 14
         * Номера строк:
         * Все строки. Для ячеек, обозначенных как вычисляемые (см. Табл. 12)
         * за исключением строк  118-119, 141-142
         *
         * Алгоритм заполннеия:
         *  «графа 14» = («графа 11» + «графа 12») – «графа 13»
         */
        if (isCalcField(row.getCell("difference")) && !(row.getAlias() in ['R118', 'R119', 'R141', 'R142'])){
            row.difference = (row.opuSumByEnclosure2?:0) + (row.opuSumByTableD ?:0)- (row.opuSumTotal ?:0)
        }


        /**
         * Графа 14
         * Номера строк:
         * 118-119
         *
         * Алгоритм заполннеия:
         *  «графа 14» = «графа 13» - «графа 8»
         */
        if (row.getAlias() in ['R118', 'R119']){
            row.difference = (row.opuSumTotal?:0) - (row.rnu4Field5Accepted?:0)
        }

        /**
         * Графа 14
         * Номера строк:
         * 141-142
         *
         * Алгоритм заполннеия:
         *  «графа 14» = «графа 13» - ( А+ Б)
         *   А – значение «графы 8» для строки 141
         *   Б – значение «графы 8» для строки 142
         */
        if (row.getAlias() in ['R141', 'R142']){
            row.difference = (row.opuSumTotal?:0) - ( (formData.getDataRow("R141").rnu4Field5Accepted?:0) + (formData.getDataRow("R142").rnu4Field5Accepted?:0))
        }
    }
    logger.info('<--');
}

/**
 * Функция возвращает тип ячейки вычисляемая она или нет
 * @param cell
 * @return
 */
def isCalcField(Cell cell){
    return cell.getStyleAlias() == "Контрольные суммы"
}

/**
 * Функция проверки является ли ячейка рекадтируемой
 * @param cell
 * @return
 */
def isEditableField(Cell cell){
    return cell.getStyleAlias() == "Редактируемая"
}

/**
 * Консолидация формы
 */
def consolidation(){
    if (!isTerBank()) {
        return
    }

    // очистить форму
    formData.getDataRows().each{ row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'logicalCheck', 'accountingRecords'].each{ alias->
            row.getCell(alias).setValue(null)
        }
    }
    // получить данные из источников
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'logicalCheck', 'accountingRecords'].each {
                    if (row.getCell(it).getValue() != null) {
                        if (isCalcField(row.getCell(it)) || isEditableField(row.getCell(it)))
                            rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
}


/**
 * Скрипт для проверки создания.
 *
 * @author auldanov
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
}

/**
 * Проверка на террбанк.
 */
def isTerBank() {
    boolean isTerBank = false
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isTerBank = true
        }
    }
    return isTerBank
}


// обертка предназначенная для прямых вызовов функции без formData
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, {return true;})
}

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

/**
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (!isBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}