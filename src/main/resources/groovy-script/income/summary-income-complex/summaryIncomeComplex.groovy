/**
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 */

switch (formDataEvent) {
    // создать
    case FormDataEvent.CREATE :
        checkCreation()
        break
    // расчитать
    case FormDataEvent.CALCULATE :
        checkAndCalc()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        checkAndCalc()
        break
    // проверить
    case FormDataEvent.CHECK :
        checkAndCalc()
        break
    // утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        checkAndCalc()
        checkOnApproval()
        break
    // принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        checkAndCalc()
        checkOnAcceptance()
        break
    // вернуть из принята в утверждена
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED :
        checkOnCancelAcceptance()
        break
    // принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        checkAndCalc()
        checkDeclarationBankOnAcceptance()
        break
    // вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkDeclarationBankOnCancelAcceptance()
        break
    // после принятия из утверждена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED :
        acceptance()
        break
    // после вернуть из "Принята" в "Утверждена"
    case FormDataEvent.AFTER_MOVE_ACCEPTED_TO_APPROVED :
        checkDeclarationBankOnCancelAcceptance()
        acceptance()
        break
}

// графа  1 - incomeTypeId
// графа  2 - incomeGroup
// графа  3 - incomeTypeByOperation
// графа  4 - incomeBuhSumAccountNumber
// графа  5 - incomeBuhSumRnuSource
// графа  6 - incomeBuhSumAccepted
// графа  7 - incomeBuhSumPrevTaxPeriod
// графа  8 - incomeTaxSumRnuSource
// графа  9 - incomeTaxSumS
// графа 10 - rnuNo
// графа 11 - logicalCheck
// графа 12 - opuSumByEnclosure2
// графа 13 - opuSumByTableD
// графа 14 - opuSumTotal
// графа 15 - opuSumByOpu
// графа 16 - difference

/**
 * Проверить и расчитать.
 */
void checkAndCalc() {
    checkRequiredFields()
    consolidationCalc()
    calculationFields()
    calculationControlGraphs1()
    calculationControlGraphs2()
}

/**
 * Для перевода сводной налогой формы в статус "принят".
 *
 * @author rtimerbaev
 * @since 21.02.2013 18:10
 */
void acceptance() {
    departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each{
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Расчет контрольных граф (строки с КВД 400* и 600*) выполнена всоответствии с
 * Табл. 8 Расчет контрольных граф Сводной формы начисленных доходов.
 * Вызыватеся при логических проверках
 *
 * @author auldanov
 * @since 21.03.2013 12:10
 */
void calculationControlGraphs1() {
    // formData для Сводная форма 'Расшифровка видов доходов, учитываемых в простых РНУ' уровня обособленного подразделения
    FormData fromFormData = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

    /**
     * Так как заполнение строк с КВД 400* идентичны,
     * они только отличаются небольшим количеством изменяемых параметров,
     * решено было выделить в отдельную функцию.
     *
     * @param kvd поле КВД с таблицы, является алиасом для строки
     * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
     * @param rowTo для вычисления суммы по строкам, задает конечную строку
     */
    def fill400xRow = {kvd, rowFrom, rowTo->
        def row = formData.getDataRow(kvd)
        def sourceRow = fromFormData.getDataRow(kvd)
        //---40001---
        // графа 12
        row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu4Field5Accepted', rowFrom - 1, rowTo - 1))
        // графа 13
        row.opuSumByTableD = summ(fromFormData, new ColumnRange('rnu4Field5PrevTaxPeriod', rowFrom -1, rowTo -1))
        // графа 14
        row.opuSumTotal = (row.opuSumByTableD?:0) - (row.incomeBuhSumPrevTaxPeriod?:0)
        // графа 16
        row.difference = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
    }

    /**
     * Так как заполнение строк с КВД 600* идентичны,
     * они только отличаются небольшим количеством изменяемых параметров,
     * решено было выделить в отдельную функцию.
     *
     * @param kvd поле КВД с таблицы, является алиасом для строки
     * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
     * @param rowTo для вычисления суммы по строкам, задает конечную строку
     */
    def fill600xRow = {kvd, rowFrom, rowTo ->
        //---600x--
        def row = formData.getDataRow(kvd)
        def sourceRow = fromFormData.getDataRow(kvd)
        // графа 11
        def tmp = ((BigDecimal) summ(fromFormData, new ColumnRange('rnu6Field12Accepted', rowFrom - 1, rowTo - 1 ))).setScale(2, BigDecimal.ROUND_HALF_UP)
        row.logicalCheck = tmp.toString()
        // графа 12
        row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu6Field10Sum', rowFrom - 1, rowTo - 1))
        // графа 13
        row.opuSumByTableD = (tmp ?: 0) - (row.incomeBuhSumAccepted?:0)
        // графа 16
        row.difference = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
    }


    /**
     * Реализация алгоритма заполнения.
     */
    if (fromFormData != null) {
        //---40001---
        fill400xRow( 'R20', 3, 86)
        //---60001---
        fill600xRow('R21', 3, 86)
        //---40002---
        fill400xRow('R38', 90, 93)
        //---60002---
        fill600xRow('R39', 90, 93)
        //---40011---
        fill400xRow('R78', 98 , 209)
        //---60011---
        fill600xRow('R79', 98, 209)
        //---40012---
        fill400xRow('R95', 213, 214)
        //---40015---
        fill400xRow('R116', 217, 217)

        // Для строки с КВД 40016 чуть отличается от стандартного, тем что нет манипуляций с 14 графой.
        //---40016---
        def row = formData.getDataRow('R119')
        def sourceRow = fromFormData.getDataRow('R119')
        // графа 12
        row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu4Field5Accepted', 220 - 1, 221 - 1))
        // графа 16
        row.difference = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
    } else {
        // если дохода простого нет, то зануляем поля в которые должны были перетянуться данные

        // 40001, 40002, 40011, 40012, 40015
        ['R20', 'R38', 'R78', 'R95', 'R116'].each {
            def row = formData.getDataRow(it)
            //---40001---
            // графа 12
            row.opuSumByEnclosure2 = 0
            // графа 13
            row.opuSumByTableD = 0
            // графа 14
            row.opuSumTotal = 0
            // графа 16
            row.opuSumTotal = 0
        }
        // 60001, 60002, 60011,
        ['R21', 'R39', 'R79'].each {
            def row = formData.getDataRow(it)
            // графа 11
            row.logicalCheck = '0'
            // графа 12
            row.opuSumByEnclosure2 = 0
            // графа 13
            row.opuSumByTableD = 0
            // графа 16
            row.opuSumTotal = 0
        }
        //---40016---
        formData.getDataRow('R119').opuSumByEnclosure2 = 0;
        formData.getDataRow('R119').opuSumTotal = 0;
    }
}

/**
 * Расчет (контрольные графы).
 * Реализована как 2 часть Табл. 8 Расчет контрольных граф Сводной формы начисленных доходов
 * (строки начинаются с Контрольная сумма и до конца таблицы).
 *
 * @author auldanov
 * @since 22.03.2013 15:30
 */
void calculationControlGraphs2() {
    def specialNotation = 'Требуется объяснение'

    // ----Раздел А1
    // графа 11
    def tmpLogicalCheck = toBigDecimal(getCell('R21', 'logicalCheck').getValue())
    def tmpValue = ((BigDecimal) summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R3'), getDataRowIndex('R19'))) + tmpLogicalCheck).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R22', 'logicalCheck').setValue(tmpValue.toString())

    // графа 12
    tmp = summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R3'), getDataRowIndex('R19')))
    getCell('R22', 'opuSumByEnclosure2').setValue(
            tmp + getCellValue('R20', 'opuSumByEnclosure2')
                    + getCellValue('R21', 'opuSumByEnclosure2')
    )
    //logger.info ('tmp = ' + tmp.toString() + ' 20 = ' + getCellValue('R20', 'opuSumByEnclosure2').toString() + ' 21 = ' + getCellValue('R21', 'opuSumByEnclosure2').toString())
    // графа 13
    getCell('R22', 'opuSumByTableD').setValue(tmpValue - (getCell('R22', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R22', 'difference').setValue(
            substract(getCell('R22', 'opuSumByEnclosure2'), getCell('R22', 'incomeTaxSumS'))
    )

    // Раздел Б1
    // графа 11
    tmpValue = toBigDecimal(getCell('R39', 'logicalCheck').getValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R40', 'logicalCheck').setValue(tmpValue.toString())
    // графа 12
    getCell('R40', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R24'), getDataRowIndex('R37'))) + getCellValue('R38', 'opuSumByEnclosure2')
                    + getCellValue('R39', 'opuSumByEnclosure2')
    )
    // графа 13
    getCell('R40', 'opuSumByTableD').setValue(tmpValue - (getCell('R40', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R40', 'difference').setValue(
            substract(getCell('R40', 'opuSumByEnclosure2'), getCell('R40', 'incomeTaxSumS'))
    )

    // --Раздел А2
    // графа 11
    tmpLogicalCheck = toBigDecimal(getCell('R79', 'logicalCheck').getValue())
    tmpValue = ((BigDecimal) summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R43'), getDataRowIndex('R77'))) + tmpLogicalCheck).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R80', 'logicalCheck').setValue(tmpValue.toString())
    // графа 12
    getCell('R80', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R43'), getDataRowIndex('R77'))) + getCellValue('R78', 'opuSumByEnclosure2')
                    + getCellValue('R79', 'opuSumByEnclosure2')
    )
    // графа 13
    getCell('R80', 'opuSumByTableD').setValue(tmpValue - (getCell('R80', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R80', 'difference').setValue(
            substract(getCell('R80', 'opuSumByEnclosure2'), getCell('R80', 'incomeTaxSumS'))
    )

    // --- Раздел Б2
    // графа 12
    getCell('R96', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R82'), getDataRowIndex('R94'))) + getCellValue('R95', 'opuSumByEnclosure2')
    )
    // графа 16
    getCell('R96', 'difference').setValue(
            substract(getCell('R96', 'opuSumByEnclosure2'), getCell('R96', 'incomeTaxSumS'))
    )

    // Раздел В
    // графа 11
    tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R98'), getDataRowIndex('R103')))
    getCell('R104', 'logicalCheck').setValue(tmpValue.toString())

    // графа 12
    getCell('R104', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R98'), getDataRowIndex('R103')))
    )
    // графа 13
    getCell('R104', 'opuSumByTableD').setValue(tmpValue - (getCell('R104', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R104', 'difference').setValue(
            substract(getCell('R104', 'opuSumByEnclosure2'), getCell('R104', 'incomeTaxSumS'))
    )

    // --- Раздел Г
    // графа 11
    tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R106'), getDataRowIndex('R109')))
    getCell('R110', 'logicalCheck').setValue(tmpValue.toString())

    // графа 12
    getCell('R110', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R106'), getDataRowIndex('R109')))
    )
    // графа 13
    getCell('R110', 'opuSumByTableD').setValue(tmpValue - (getCell('R110', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R110', 'difference').setValue(
            substract(getCell('R110', 'opuSumByEnclosure2'), getCell('R110', 'incomeTaxSumS'))
    )

    // --Раздел Д
    // графа 12
    getCell('R117', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R112'), getDataRowIndex('R115'))) + getCellValue('R116', 'opuSumByEnclosure2')
    )
    // графа 16
    getCell('R117', 'difference').setValue(
            substract(getCell('R117', 'opuSumByEnclosure2'), getCell('R117', 'incomeTaxSumS'))
    )

    // --Раздел Е
    // графа 12
    getCell('R120', 'opuSumByEnclosure2').setValue(
            getCellValue('R119', 'opuSumByEnclosure2')
    )
    // графа 16
    getCell('R120', 'difference').setValue(
            substract(getCell('R120', 'opuSumByEnclosure2'), getCell('R120', 'incomeTaxSumS'))
    )

    // Раздел Ж
    // графа 11
    tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R122'), getDataRowIndex('R127')))
    getCell('R128', 'logicalCheck').setValue(tmpValue.toString())

    // графа 12
    getCell('R128', 'opuSumByEnclosure2').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R122'), getDataRowIndex('R127')))
    )
    // графа 13
    getCell('R128', 'opuSumByTableD').setValue(tmpValue - (getCell('R128', 'incomeBuhSumAccepted').getValue() ?: 0))

    // графа 16
    getCell('R128', 'difference').setValue(
            substract(getCell('R128', 'opuSumByEnclosure2'), getCell('R128', 'incomeTaxSumS'))
    )

    // раздел где много строк для графы 11
    for (row in ['R4', 'R5', 'R7', 'R19', 'R43', 'R45', 'R46', 'R47',
            'R48', 'R49', 'R50', 'R64', 'R70', 'R72', 'R73', 'R74',
            'R75', 'R76', 'R77', 'R98', 'R106', 'R122']) {

        // Очень ритуальный подсчёт суммы из за возможности объединения ячеек и проблемы сущетсвующий что у колонки с которой объединяем значение null
        sum6column = 0
        prev = 'not use null please :)'
        for (rowForSumm in formData.dataRows) {
            if (rowForSumm.incomeBuhSumAccountNumber != null
                    && rowForSumm.incomeBuhSumAccepted != null
                    && ((rowForSumm.incomeTypeId != null && rowForSumm.incomeTypeId == getCell(row, 'incomeTypeId').getValue())
                    || (rowForSumm.incomeTypeId == null && prev == getCell(row, 'incomeTypeId').getValue()))) {
                prev = getCell(row, 'incomeTypeId').getValue()
                sum6column += rowForSumm.incomeBuhSumAccepted
            } else {
                prev = 'not use null please :)'
            }
        }

        // тот же магический подсчёт
        sum7column = 0
        prev = 'not use null please :)'
        for (rowForSumm in formData.dataRows) {
            if (rowForSumm.incomeBuhSumAccountNumber != null
                    && rowForSumm.incomeBuhSumPrevTaxPeriod != null
                    && ((rowForSumm.incomeTypeId != null && rowForSumm.incomeTypeId == getCell(row, 'incomeTypeId').getValue())
                    || (rowForSumm.incomeTypeId == null && prev == getCell(row, 'incomeTypeId').getValue()))) {
                prev = getCell(row, 'incomeTypeId').getValue()
                sum7column += rowForSumm.incomeBuhSumPrevTaxPeriod
            } else {
                prev = 'not use null please :)'
            }
        }

        /*ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getDataRows().size() - 1)
        Double sum6column = summ(formData, columnRange6, columnRange6, { condRange ->
            return getCell(it, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
        })*/

        /*ColumnRange columnRange7 = new ColumnRange('incomeBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
      Double sum7column = summ(formData, columnRange7, columnRange7, { condRange ->
          return getCell(row, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
      })  */

        //logger.info(String.valueOf(row + " sum6 = " + sum6column))
        //logger.info(row + " sum6 = " + sum7column)
        def val = getCellValue(row, 'incomeTaxSumS') - (sum6column - sum7column)

        // другой вариант округления
        val = ((BigDecimal) val).setScale(2, BigDecimal.ROUND_HALF_UP)
        //val = round(val, 2)

        getCell(row, 'logicalCheck').setValue(
                val >= 0 ? val.toString() : specialNotation
        )
    }

    // получение данных из расходов сложных
    def formDataComplexConsumption = FormDataService.find(303, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    temp2 = 0
    temp3 = 0
    if (formDataComplexConsumption != null) {
        temp2 = formDataComplexConsumption.getDataRow('R90').getCell('consumptionTaxSumS').getValue()?:0
        temp3 = formDataComplexConsumption.getDataRow('R92').getCell('consumptionTaxSumS').getValue()?:0
    }
    // строка 9 графа 11
    temp = ((BigDecimal) (getCellValue('R9', 'incomeTaxSumS')
            - (getCellValue('R10', 'incomeTaxSumS') - temp3 + temp2)
    ).setScale(2, BigDecimal.ROUND_HALF_UP))
    getCell('R9', 'logicalCheck').setValue(temp == 0 ? '0' : 'Требуется объяснение')

    // строка 10 графа 11
    temp = ((BigDecimal) (getCellValue('R10', 'incomeTaxSumS')
            - (getCellValue('R9', 'incomeTaxSumS') - temp2 + temp3)
    ).setScale(2, BigDecimal.ROUND_HALF_UP))
    getCell('R10', 'logicalCheck').setValue(temp == 0 ? '0' : 'Требуется объяснение')

    /**
     * Все оставшиеся строки, не описанные выше в этой таблице.
     */
    // получение данных из доходов простых
    def formDataSimpleIncome = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

    List<DataRow> dataRows = formData.getDataRows()
    for (DataRow row : dataRows) {
        // проверка что строка не описана выше
        if (['R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43',
                'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51',
                'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R64',
                'R98', 'R99', 'R100', 'R106', 'R107',
                'R122', 'R123', 'R124'
        ].contains(row.getAlias())) {

            // графа 12
            summ = 0
            for(rowData in formData.dataRows) {
                if (rowData.incomeBuhSumAccepted != null && row.incomeBuhSumAccountNumber == rowData.incomeBuhSumAccountNumber) {
                    summ += rowData.incomeBuhSumAccepted
                }
            }

            row.getCell('opuSumByEnclosure2').setValue(summ);

            /*ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getFormColumns().size() - 1)
            row.getCell('opuSumByEnclosure2').setValue(
                    summ(formData, columnRange6, columnRange6, { condRange ->
                        return row.getCell('incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue()
                    })
            )*/
        }
        if (['R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43', 'R64',
                'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R54', 'R55', 'R56', 'R57', 'R58', 'R59',
                'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77',
                'R98', 'R99', 'R100', 'R106', 'R107', 'R122', 'R123', 'R124'
        ].contains(row.getAlias())) {
            // графа 15
            temp = income102Dao.getIncome102(formData.reportPeriodId, row.incomeBuhSumAccountNumber.toString().substring(8), formData.departmentId)
            if (temp == null) {
                logger.info("Нет информации в отчётах о прибылях и убытках")
                row.getCell('opuSumByOpu').setValue(0)
            } else {
                row.getCell('opuSumByOpu').setValue(temp.totalSum)
            }
        }

    }



    // строки 3-53, 60-131
    ((4..8) + (19) + (43..51) + (64) + (70..77) + (98..100) + (106..107) + (122..124)).each {
        def thisRow = formData.getDataRow("R" + it)

        if (formDataSimpleIncome != null) {
            // графа 13
            columnRange9 = new ColumnRange('rnu4Field5Accepted', 0, formDataSimpleIncome.getDataRows().size() - 1)
            thisRow.opuSumByTableD = summ(formDataSimpleIncome, columnRange9, columnRange9, { condRange ->
                return thisRow.incomeBuhSumAccountNumber == condRange.getCell('accountNo').getValue()
            })
        } else {
            thisRow.opuSumByTableD = 0
        }

        // графа 14
        if (thisRow.opuSumByEnclosure2 != null && thisRow.opuSumByTableD != null) {
            thisRow.opuSumTotal = thisRow.opuSumByEnclosure2 + thisRow.opuSumByTableD
        } else {
            thisRow.opuSumTotal = null
        }
    }

    /**
     * Все оставшиеся строки, не описанные выше в этой таблице «графа 16» = «графа 14» - «графа 15»
     */
    [
            'R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43', 'R64',
            'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R54', 'R55', 'R56', 'R57', 'R58', 'R59',
            'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R128',
            'R98', 'R99', 'R100', 'R106', 'R107', 'R122', 'R123', 'R124'].each{
        // графа 16
        getCell(it, 'difference').setValue(getCellValue(it, 'opuSumTotal') - getCellValue(it, 'opuSumByOpu'))
    }

    // строки 54-59, графы 13,14, 15, 16
    ['R54', 'R55', 'R56', 'R57', 'R58', 'R59'].each {
        value = getCell(it, 'incomeBuhSumAccountNumber').getValue().toString()
        account = value.substring(0, 3) + value.substring(4)
        temp = income101Dao.getIncome101(formData.reportPeriodId, account, formData.departmentId)
        if (temp == null) {
            logger.info("Нет данных об оборотной ведомости")
            getCell(it, 'opuSumByTableD').setValue(0)
            getCell(it, 'opuSumTotal').setValue(0)
        } else {
            getCell(it, 'opuSumByTableD').setValue(temp.incomeDebetRemains)
            getCell(it, 'opuSumTotal').setValue(temp.outcomeDebetRemains)
        }
        getCell(it, 'opuSumByOpu').setValue((BigDecimal) getCell(it, 'opuSumByTableD').getValue() - (BigDecimal) getCell(it, 'opuSumTotal').getValue())
        getCell(it, 'difference').setValue((getCell(it, 'opuSumByOpu').getValue() ?: 0) - (getCell(it, 'incomeTaxSumS').getValue() ?: 0))

    }
}

/**
 * Алгоритмы заполнения полей формы при расчете данных формы
 * Табл. 6 Алгоритмы заполнения вычисляемых полей фиксированных строк  Сводной формы начисленных доходов уровня обособленного подразделения
 * Расчет (основные графы).
 *
 * @author auldanov
 */
void calculationFields() {
    // ----Раздел А1
    // графа 6, 9
    ['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
        getCell('R22', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R2') + 1, getDataRowIndex('R22') - 1))
        );
    }

    // Раздел Б1
    // графа 6, 9
    ['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
        getCell('R40', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R23') + 1, getDataRowIndex('R40') - 1))
        );
    }

    // --Раздел А2
    // графа 6, 9
    ['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
        getCell('R80', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R42') + 1, getDataRowIndex('R80') - 1))
        );
    }

    // --- Раздел Б2
    // графа 9
    getCell('R96', 'incomeTaxSumS').setValue(
            summ(new ColumnRange("incomeTaxSumS", getDataRowIndex('R81') + 1, getDataRowIndex('R94') + 1))
    );

    // Раздел В
    // графа 6, 9
    ["incomeBuhSumAccepted", "incomeTaxSumS"].each {
        getCell('R104', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R97') + 1, getDataRowIndex('R104') - 1))
        );
    }

    // --- Раздел Г
    // графа 6, 9
    ['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
        getCell('R110', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R105') + 1, getDataRowIndex('R110') - 1))
        );
    }

    // --Раздел Д
    // графа 9
    getCell('R117', 'incomeTaxSumS').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R111') + 1, getDataRowIndex('R117') - 1))
    );

    // --Раздел Е
    // графа 9
    getCell('R120', 'incomeTaxSumS').setValue(
            summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R118') + 1, getDataRowIndex('R120') - 1))
    );

    // Раздел Ж
    // графа 6, 9
    ['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
        getCell('R128', it).setValue(
                summ(new ColumnRange(it, getDataRowIndex('R121') + 1, getDataRowIndex('R128') - 1))
        );
    }
}

/**
 * Скрипт для проверки создания.
 *
 * @author rtimerbaev
 * @since 21.02.2013 12:30
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
 * Проверки наличия декларации Банка при принятии нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnAcceptance() {
    if (isTerBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Принятие налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Проверки наличия декларации Банка при отмене принятия нф.
 *
 * @author rtimerbaev
 * @since 21.03.2013 11:00
 */
void checkDeclarationBankOnCancelAcceptance() {
    if (isTerBank()) {
        return
    }
    departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
        if (bank != null && bank.accepted) {
            logger.error('Отмена принятия налоговой формы невозможно, т.к. уже принята декларация Банка.')
        }
    }
}

/**
 * Проверка при осуществлении перевода формы в статус "Принята".
 *
 * @since 21.03.2013 17:00
 */
void checkOnAcceptance() {
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Принятие сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверка, наличия и статуса сводной формы уровня Банка  при осуществлении перевода формы в статус "Утверждена".
 *
 * @author auldanov
 * @since 21.03.2013 17:00
 */
void checkOnApproval() {
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error('Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            }
        }
    }
}

/**
 * Проверки при переходе "Отменить принятие"
 */
void checkOnCancelAcceptance() {
    if (isBank()) {
        return
    }
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY);
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData bank = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (bank != null && (bank.getState() == WorkflowState.APPROVED || bank.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Проверка обязательных полей.
 *
 * @author rtimerbaev
 * @since 20.03.2013 18:30
 */
void checkRequiredFields() {
    // 6, 7, 9 графы
    [
            'incomeBuhSumAccepted' : ((4..8) + [19] + (43..51) + [64] + (70..77) + [98, 99, 100, 106, 107, 122, 123, 124]),
            'incomeBuhSumPrevTaxPeriod' : ((4..8) + [19] + (43..51) + [64] + (70..77) + [122, 123, 124]),
            'incomeTaxSumS' : ([3, 4, 5, 7] + (9..19) + (24..37) + [43] + (45..50) + (52..70) + (72..77) + (82..94) + [98, 101, 102, 103, 106, 108, 109] + (112..115) + [122, 125, 126, 127, 130, 131])
    ].each() { colAlias, items ->
        def errorMsg = ''
        def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
        // разделы
        def sectionA1 = '', sectionB1 = '',
            sectionA2 = '', sectionB2 = '',
            sectionV = '', sectionG = '',
            sectionD = '', sectionE = '',
            sectionJ = '', sectionS = ''
        items.each { item->
            def row = formData.getDataRow('R' + item)
            if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
                switch (item) {
                    case (3..21) :
                        sectionA1 += getEmptyCellIncomeType(row)
                        break
                    case (24..39) :
                        sectionB1 += getEmptyCellIncomeType(row)
                        break
                    case (43..89) :
                        sectionA2 += getEmptyCellIncomeType(row)
                        break
                    case (82..95) :
                        sectionB2 += getEmptyCellIncomeType(row)
                        break
                    case (98..103) :
                        sectionV += getEmptyCellIncomeType(row)
                    case (106..19) :
                        sectionG += getEmptyCellIncomeType(row)
                    case (112..116) :
                        sectionD += getEmptyCellIncomeType(row)
                        break
                    case 119 :
                        sectionE += getEmptyCellIncomeType(row)
                        break
                    case (122..127) :
                        sectionJ += getEmptyCellIncomeType(row)
                        break
                    case (130..131) :
                        sectionS += getEmptyCellIncomeType(row)
                        break
                }
            }
        }

        errorMsg += addSector(errorMsg, sectionA1, '"А1"')
        errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
        errorMsg += addSector(errorMsg, sectionA2, '"А2"')
        errorMsg += addSector(errorMsg, sectionB2, '"Б2"')
        errorMsg += addSector(errorMsg, sectionV, '"В"')
        errorMsg += addSector(errorMsg, sectionG, '"Г"')
        errorMsg += addSector(errorMsg, sectionD, '"Д"')
        errorMsg += addSector(errorMsg, sectionE, '"Е"')
        errorMsg += addSector(errorMsg, sectionJ, '"Ж"')
        errorMsg += addSector(errorMsg, sectionS, '"специфичная информация"')

        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
        }
    }
}

/*
 * 6.2.1.8.2	Алгоритмы консолидации данных сводных форм обособленных подразделений.
 *
 * @author auldanov
 * @since 14.02.2013 11:30
 */
void consolidation() {
    /*
     * В цикле по всем формам-источникам с типом «Сводная форма "Расшифровка видов доходов,
     * учитываемых в простых РНУ" уровня обособленного подразделения» расчитать данные
     * для текущей формы путем складывания значений редактируемых ячеек.
     */

    // Обнулим данные в связи http://jira.aplana.com/browse/SBRFACCTAX-1861
    for(row in formData.dataRows ) {
        ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each {
            row.getCell(it).setValue(null);
        }
    }
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
        def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 302) {
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                /*
                 *	Для каждой ячейки граф с 1 по 10, обозначенной либо как редактируемая,
                 *	либо как расчетная, Система должна выполнять консолидацию данных из форм-источников.
                 *	(6, 7, 9)
                 */
                ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
}

/**
 * Алгоритмы консолидации данных выполнена в соответствии с
 * Табл. 9 Алгоритмы расчета ячеек, заполняемых в результате консолидации.
 *
 * @author auldanov
 * @since 22.02.2013 11:30
 */
void consolidationCalc() {
    FormData fromFormData = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

    //// formData для Сводная форма 'Расшифровка видов доходов, учитываемых в простых РНУ' уровня обособленного подразделения
    //FormData fromFormData = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    //
    ///**
    // * Так как заполнение строк с КВД 400* идентичны,
    // * они только отличаются небольшим количеством изменяемых параметров,
    // * решено было выделить в отдельную функцию.
    // *
    // * @param kvd поле КВД с таблицы, является алиасом для строки
    // * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
    // * @param rowTo для вычисления суммы по строкам, задает конечную строку
    // * @deprecated Удалить при необходимости отвязаться
    // */
    //def fill400xRow = {kvd, rowFrom, rowTo->
    //    def row = formData.getDataRow(kvd)
    //    def sourceRow = fromFormData.getDataRow(kvd)
    //    //---40001---
    //    // графа 7
    //    row.incomeBuhSumPrevTaxPeriod = sourceRow.rnu4Field5PrevTaxPeriod
    //    // графа 9
    //    row.incomeTaxSumS = sourceRow.rnu4Field5Accepted
    //}
    //
    ///**
    // * Так как заполнение строк с КВД 600* идентичны,
    // * они только отличаются небольшим количеством изменяемых параметров,
    // * решено было выделить в отдельную функцию.
    // *
    // * @param kvd поле КВД с таблицы, является алиасом для строки
    // * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
    // * @param rowTo для вычисления суммы по строкам, задает конечную строку
    // */
    //def fill600xRow = {kvd, rowFrom, rowTo ->
    //    //---600x--
    //    def row = formData.getDataRow(kvd)
    //    def sourceRow = fromFormData.getDataRow(kvd)
    //    // графа 6
    //    row.incomeBuhSumAccepted = sourceRow.rnu6Field12Accepted
    //    // графа 9
    //    row.incomeTaxSumS = sourceRow.rnu6Field10Sum
    //}

    /**
     * Реализация алгоритма заполнения.
     */
    if (fromFormData != null) {
        //---40001---
        // Графа 7
        formData.getDataRow("R20").getCell("incomeBuhSumPrevTaxPeriod").setValue(fromFormData.getDataRow("R87").getCell("rnu4Field5PrevTaxPeriod").getValue())
        // Графа 9
        formData.getDataRow("R20").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R87").getCell("rnu4Field5Accepted").getValue())
        //---60001---
        // Графа 6
        formData.getDataRow("R21").getCell("incomeBuhSumAccepted").setValue(fromFormData.getDataRow("R88").getCell("rnu6Field12Accepted").getValue())
        // Графа 9
        formData.getDataRow("R21").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R88").getCell("rnu6Field10Sum").getValue())
        //--40002--
        // Графа 7
        formData.getDataRow("R38").getCell("incomeBuhSumPrevTaxPeriod").setValue(fromFormData.getDataRow("R94").getCell("rnu4Field5PrevTaxPeriod").getValue())
        // Графа 9
        formData.getDataRow("R38").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R94").getCell("rnu4Field5Accepted").getValue())
        //---60002---
        // Графа 6
        formData.getDataRow("R39").getCell("incomeBuhSumAccepted").setValue(fromFormData.getDataRow("R95").getCell("rnu6Field12Accepted").getValue())
        // Графа 9
        formData.getDataRow("R39").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R95").getCell("rnu6Field10Sum").getValue())
        //--40011--
        // Графа 7
        formData.getDataRow("R78").getCell("incomeBuhSumPrevTaxPeriod").setValue(fromFormData.getDataRow("R210").getCell("rnu4Field5PrevTaxPeriod").getValue())
        // Графа 9
        formData.getDataRow("R78").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R210").getCell("rnu4Field5Accepted").getValue())
        //---60011---
        // Графа 6
        formData.getDataRow("R79").getCell("incomeBuhSumAccepted").setValue(fromFormData.getDataRow("R211").getCell("rnu6Field12Accepted").getValue())
        // Графа 9
        formData.getDataRow("R79").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R211").getCell("rnu6Field10Sum").getValue())
        //--40012--
        // Графа 7
        formData.getDataRow("R95").getCell("incomeBuhSumPrevTaxPeriod").setValue(fromFormData.getDataRow("R215").getCell("rnu4Field5PrevTaxPeriod").getValue())
        // Графа 9
        formData.getDataRow("R95").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R215").getCell("rnu4Field5Accepted").getValue())
        //--40015--
        // Графа 7
        formData.getDataRow("R116").getCell("incomeBuhSumPrevTaxPeriod").setValue(fromFormData.getDataRow("R218").getCell("rnu4Field5PrevTaxPeriod").getValue())
        // Графа 9
        formData.getDataRow("R116").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R218").getCell("rnu4Field5Accepted").getValue())
        //--40016--
        // Графа 9
        formData.getDataRow("R119").getCell("incomeTaxSumS").setValue(fromFormData.getDataRow("R222").getCell("rnu4Field5Accepted").getValue())

//    fill400xRow( 'R20', 3, 86)
//    //---60001---
//    fill600xRow('R21', 3, 86)
//    //---40002---
//    fill400xRow('R38', 90, 93)
//    //---60002---
//    fill600xRow('R39', 90, 93)
//    //---40011---
//    fill400xRow('R78', 98 , 209)
//    //---60011---
//    fill600xRow('R79', 98, 209)
//    //---40012---
//    fill400xRow('R95', 214, 214)
//    //---40015---
//    fill400xRow('R116', 217, 217)

        // Для строки с КВД 40016 чуть отличается от стандартного, тем что нет манипуляций с 14 графой.
        //---40016---
//    def row = formData.getDataRow('R119')
//    def sourceRow = fromFormData.getDataRow('R119')
//    // графа 9
//    row.incomeTaxSumS = sourceRow.rnu4Field5Accepted
    } else {
        // если дохода простого нет, то зануляем поля в которые должны были перетянуться данные

        // 40001, 40002, 40011, 40012, 40015
        ['R20', 'R38', 'R78', 'R95', 'R116'].each {
            def row = formData.getDataRow(it)
            //---40001---
            // графа 7
            row.incomeBuhSumPrevTaxPeriod = 0
            // графа 9
            row.incomeTaxSumS = 0
        }
        // 60001, 60002, 60011,
        ['R21', 'R39', 'R79'].each {
            def row = formData.getDataRow(it)
            //---600x--
            // графа 6
            row.incomeBuhSumAccepted = 0
            // графа 9
            row.incomeTaxSumS = 0
        }
        //---40016---
        formData.getDataRow('R119').incomeTaxSumS = 0;
    }
}

/*
 * Вспомогательные методы.
 */

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

/**
 * Получить число из строки.
 */
def toBigDecimal(String value) {
    if (value == null) {
        return new BigDecimal(0)
    }
    def result
    try {
        result = new BigDecimal(Double.parseDouble(value))
    } catch (NumberFormatException e) {
        result = new BigDecimal(0)
    }
    return result
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
int getDataRowIndex(String rowAlias) {
    return formData.getDataRowIndex(rowAlias)
}

/**
 * Прямое получения ячейки по столбцу и колонке.
 */
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, { return true; })
}

/**
 * Прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0.
 */
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column) == null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?: 0;
}

/**
 * Суммирует ячейки второго диапазона только для тех строк, для которых выполняется условие фильтрации. В данном
 * случае под условием фильтрации подразумевается равенство значений строк первого диапазона заранее заданному
 * значению. Является аналогом Excel функции 'СУММЕСЛИ' в нотации 'СУММЕСЛИ(диапазон, критерий, диапазон_суммирования)'
 * @see <a href='http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1'>СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
 *
 * @param formData таблица данных
 * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
 * @param filterValue значение фильтра
 * @param summRange диапазон суммирования
 * @return сумма ячеек
 */
double summ(FormData formData, Range conditionRange, Range summRange, filter) {
    Rect summRect = summRange.getRangeRect(formData)
    Rect condRange = conditionRange.getRangeRect(formData)
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES)

    double sum = 0
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias())
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                def summValue = summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias())
                if (summValue != null) {
                    BigDecimal temp
                    if (summValue instanceof String) {
                        temp = new BigDecimal(summValue.replace(',', '.'))
                    } else {
                        temp = summValue
                    }
                    sum += temp.doubleValue()
                }
            }
        }
    }
    return sum
}

/**
 * Получить разделить между названиями разделов.
 */
def getSectionSeparator(def value1, def value2) {
    return ((!''.equals(value1)) && !''.equals(value2) ? ', ' : '')
}

/**
 * Получить код строки в которой есть незаполненная ячейка.
 */
def getEmptyCellIncomeType(def row) {
    return (row.incomeTypeId != null ? row.incomeTypeId : 'пусто') + ', '
}

/**
 * Удалить последнюю запятую.
 */
def deleteLastSeparator(String values) {
    return values.substring(0, values.length() - 2)
}

/**
 * Добавить в сообщение коды незаполненных ячеек.
 *
 * @param errorMsg сообщение
 * @param values список незаполненных полей в виде строки (перечислены через запятую)
 * @param sectorName название раздела
 */
def addSector(def errorMsg, def values, def sectorName) {
    if (values != null && !''.equals(values)) {
        return getSectionSeparator(errorMsg, values) + sectorName + ' (' + deleteLastSeparator(values) + ')'
    } else {
        return ''
    }
}