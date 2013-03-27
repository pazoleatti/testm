//import com.aplana.sbrf.taxaccounting.model.DataRow
//import com.aplana.sbrf.taxaccounting.model.FormDataKind
//import com.aplana.sbrf.taxaccounting.model.log.LogLevel
//import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.1.9.1	Алгоритмы заполнения полей формы
 * @author ekuvshinov
 * @since 11.02.2013
 */
//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.service.script.FormDataService FormDataService
//com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService reportPeriodService
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.service.script.DepartmentService departmentService
//com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService dictionaryRegionService

// Стандартные значения для

//noinspection GroovyVariableNotAssigned
if (!logger.containsLevel(LogLevel.ERROR)) {

    if (formData.dataRows.size() != 0) {

        // Расчитываем распределяемая налоговая база за отчётный период
        formIncomeId = 302  // Сводная форма начисленных доходов (доходы сложные)
        formIncomePHYId = 301 // Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)
        formCostId = 303    // Сводная форма начисленных расходов (расходы сложные)
        formCostPHYId = 304 //Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)
        //noinspection GroovyVariableNotAssigned
        formIncome = FormDataService.find(formIncomeId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
        formIncomePHY = FormDataService.find(formIncomePHYId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
        formCost = FormDataService.find(formCostId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
        formCostPHY = FormDataService.find(formCostPHYId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
        taxBase = 0
        if (formIncome != null) {
            for (value in formIncome.dataRows) {
                //k1
                if (value.incomeTypeId in [10571, 10640, 10641, 10650, 10920]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k6
                if (value.incomeTypeId in [10871, 10873]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k7
                if (value.incomeTypeId in [10850]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k8
                if (value.incomeTypeId in [11271..11280]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k9
                if (value.incomeTypeId in ((11860..13610) + (13650..13700) + (13920..13961) + [10874])) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k15
                if (value.incomeTypeId in [10840]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k17
                if (value.incomeTypeId in [10860]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k18
                if (value.incomeTypeId in [10870]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
                //k21
                if (value.incomeTypeId in [10872]) {
                    if (value.incomeTaxSumS != null) {
                        taxBase += value.incomeTaxSumS
                    }
                }
            }
        }
        if (formIncomePHY != null) {
            for (value in formIncomePHY.dataRows) {
                //k2
                if (value.incomeTypeId in ((10001..10960) + [11140, 11160])) {
                    if (value.rnu4Field5Accepted != null) {
                        taxBase += value.rnu4Field5Accepted
                    }
                }
                //k3
                if (value.incomeTypeId in ((10001..10960) + [11140, 11160])) {
                    if (value.rnu6Field10Sum != null) {
                        taxBase += value.rnu6Field10Sum
                    }
                }
                //k4
                if (value.incomeTypeId in ((10001..10960) + [11140, 11160])) {
                    if (value.rnu4Field5PrevTaxPeriod != null) {
                        taxBase -= value.rnu4Field5PrevTaxPeriod
                    }
                }
                //k5
                if (value.incomeTypeId in ((10001..10960) + [11140, 11160])) {
                    if (value.rnu6Field12Accepted != null) {
                        taxBase -= value.rnu6Field12Accepted
                    }
                }
                //k10
                if (value.incomeTypeId in ((11380..13080) + (13100..13639) + [13763, 13930, 14000])) {
                    if (value.rnu4Field5Accepted != null) {
                        taxBase += value.rnu4Field5Accepted
                    }
                }
                //k11
                if (value.incomeTypeId in ((11380..13080) + (13100..13639) + [13763, 13930, 14000])) {
                    if (value.rnu6Field10Sum != null) {
                        taxBase += value.rnu6Field10Sum
                    }
                }
                //k12
                if (value.incomeTypeId in ((11380..13080) + (13100..13639) + [13763, 13930, 14000])) {
                    if (value.rnu4Field5PrevTaxPeriod != null) {
                        taxBase -= value.rnu4Field5PrevTaxPeriod
                    }
                }
                //k13
                if (value.incomeTypeId in ((11380..13080) + (13100..13639) + [13763, 13930, 14000])) {
                    if (value.rnu6Field12Accepted != null) {
                        taxBase -= value.rnu6Field12Accepted
                    }
                }
                //k14
                if (value.incomeTypeId in [13092]) {
                    if (value.rnu4Field5Accepted != null) {
                        taxBase -= value.rnu4Field5Accepted
                    }
                }
                //k23
                if (value.incomeTypeId in [14000]) {
                    if (value.rnu4Field5Accepted != null) {
                        taxBase -= value.rnu4Field5Accepted
                    }
                }
            }
        }
        if (formCost != null) {
            for (value in formCost.dataRows) {
                //k16
                if (value.consumptionTypeId in [21659]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase += value.consumptionTaxSumS
                    }
                }
                //k19
                if (value.consumptionTypeId in [21515]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase += value.consumptionTaxSumS
                    }
                }
                //k19
                if (value.consumptionTypeId in [21518]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase += value.consumptionTaxSumS
                    }
                }
                //k22
                if (value.consumptionTypeId in [21397]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase += value.consumptionTaxSumS
                    }
                }
                //k24
                if (value.consumptionTypeId in ((20320..21395) + (21400..21500) + (21530..21652) + (21654..21655))) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k29
                if (value.consumptionTypeId in [21653, 21656]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k30
                if (value.consumptionTypeId in [21658]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k31
                if (value.consumptionTypeId in [21662..21675]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k32
                if (value.consumptionTypeId in [21520, 21525]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k33
                if (value.consumptionTypeId in [21397]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k34
                if (value.consumptionTypeId in ((22482 - 22811) + (23110 - 23141))) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k39
                if (value.consumptionTypeId in [22481]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase += value.consumptionTaxSumS
                    }
                }
                //k40
                if (value.consumptionTypeId in [21657]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k41
                if (value.consumptionTypeId in [21507]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k42
                if (value.consumptionTypeId in [21510]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
                //k43
                if (value.consumptionTypeId in [21396]) {
                    if (value.consumptionTaxSumS != null) {
                        taxBase -= value.consumptionTaxSumS
                    }
                }
            }
        }
        if (formCostPHY != null) {
            for (value in formCostPHY.dataRows) {
                //k25
                if (value.consumptionTypeId in ((20291..21650) + [21660])) {
                    if (value.rnu5Field5Accepted != null) {
                        taxBase -= value.rnu5Field5Accepted
                    }
                }
                //k26
                if (value.consumptionTypeId in ((20291..21650) + [21660])) {
                    if (value.rnu7Field10Sum != null) {
                        taxBase -= value.rnu7Field10Sum
                    }
                }
                //k27
                if (value.consumptionTypeId in ((20291..21650) + [21660])) {
                    if (value.rnu5Field5PrevTaxPeriod != null) {
                        taxBase += value.rnu5Field5PrevTaxPeriod
                    }
                }
                //k28
                if (value.consumptionTypeId in ((20291..21650) + [21660])) {
                    if (value.rnu7Field12Accepted != null) {
                        taxBase += value.rnu7Field12Accepted
                    }
                }
                //k35
                if (value.consumptionTypeId in ((21680..22840) + (23040..23080))) {
                    if (value.rnu5Field5Accepted != null) {
                        taxBase -= value.rnu5Field5Accepted
                    }
                }
                //k36
                if (value.consumptionTypeId in ((21680..22840) + (23040..23080))) {
                    if (value.rnu7Field10Sum != null) {
                        taxBase -= value.rnu7Field10Sum
                    }
                }
                //k37
                if (value.consumptionTypeId in ((21680..22840) + (23040..23080))) {
                    if (value.rnu5Field5PrevTaxPeriod != null) {
                        taxBase += value.rnu5Field5PrevTaxPeriod
                    }
                }
                //k38
                if (value.consumptionTypeId in ((21680..22840) + (23040..23080))) {
                    if (value.rnu7Field12Accepted != null) {
                        taxBase += value.rnu7Field12Accepted
                    }
                }
            }
        }
        // taxBase = распределяемая налоговая база за отчётный период

        // Строка итого
        rowTotal = new DataRow();
        if (formData.dataRows.get(formData.dataRows.size() - 1).getAlias() != 'total') {
            rowTotal = formData.appendDataRow('total')
        } else {
            rowTotal = formData.getDataRow('total')
        }
        rowTotal.bankName = "Итого: "
        rowTotal.propertyPrice = summ(formData, new ColumnRange('propertyPrice', 0, formData.dataRows.size() - 2))
        rowTotal.workersCount = summ(formData, new ColumnRange('workersCount', 0, formData.dataRows.size() - 2))

        //noinspection GroovyVariableNotAssigned
        period = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        formPrev = null
        if (period != null) {
            formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, period.id)
        }

        // Заполним поля автоматически какие сможем на основе графы5
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                department = departmentService.get(row.divisionName.toString())
                departmentParent = departmentService.getTB(department.getTbIndex())
                row.bankName = departmentParent.name
                row.bankCode = departmentParent.sbrfCode
                row.divisionCode = department.sbrfCode
                departmentParam = departmentService.getDepartmentParam((int) department.id)
                row.kpp = new BigDecimal(departmentParam.kpp)
                row.subjectCode = department.dictRegionId
                row.subjectName = dictionaryRegionService.getRegionByCode(department.getDictRegionId()).getName()
                departmentParamIncome = departmentService.getDepartmentParamIncome(department.id)
                row.subjectTaxStavka = departmentParamIncome.taxRate
            }
        }

        // !!!!!!!! Нужно именно цикл по каждой строки для каждой колонки или потом проблемы с расчётами некоторых полей будут

        //графа1
        i = 0;
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                i++
                row.number = i
            }
        }

        //графа12
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.propertyWeight = (BigDecimal) ((row.propertyPrice / rowTotal.propertyPrice) * 100).setScale(8, BigDecimal.ROUND_HALF_UP)
            }
        }

        //графа13
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.countWeight = (BigDecimal) ((row.workersCount / rowTotal.workersCount) * 100).setScale(8, BigDecimal.ROUND_HALF_UP)
            }
        }

        //графа14
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.baseTaxOf = (BigDecimal) (row.propertyPrice + row.workersCount / 2).setScale(8, BigDecimal.ROUND_HALF_UP)
            }
        }

        //графа28
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                //noinspection GroovyVariableNotAssigned
                row.taxSumOutside = (BigDecimal) (departmentService.getDepartmentParamIncome(formData.departmentId).externalTaxSum * 18 / (18 + 2)).setScale(0, BigDecimal.ROUND_HALF_UP) *
                        (BigDecimal) (row.baseTaxOf / 100).setScale(0, BigDecimal.ROUND_HALF_UP) - row.delta28
                // externalTaxSum у каждого периода будет своя версия (Версионирование формы настроек)
            }
        }

        //графа15
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.baseTaxOfRub = (BigDecimal) (taxBase * row.baseTaxOf / 100).setScale(0, BigDecimal.ROUND_HALF_UP)
            }
        }

        // Графа17
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                temp = (BigDecimal) (row.baseTaxOfRub * row.subjectTaxStavka / 100).setScale(0, BigDecimal.ROUND_HALF_UP)
                row.subjectTaxSum = temp > 0 ? temp : 0
            }
        }

        //графа18
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                subjectTaxSumPrev = 0
                everyMontherPaymentAfterPeriodPrev = 0
                taxSumOutsidePrev = 0
                if (formPrev != null) {
                    for (rowPrev in formData.dataRows) {
                        if (row.divisionCode == rowPrev.divisionCode) {
                            subjectTaxSumPrev = rowPrev.subjectTaxSum
                            everyMontherPaymentAfterPeriodPrev = rowPrev.everyMontherPaymentAfterPeriod
                            taxSumOutsidePrev = rowPrev.taxSumOutside
                        }
                    }
                }
                row.subjectTaxCredit = subjectTaxSumPrev + everyMontherPaymentAfterPeriodPrev - taxSumOutsidePrev
            }
        }

        //графа19
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                if (row.subjectTaxSum > row.subjectTaxCredit + row.taxSumOutside) {
                    row.taxSumToPay = row.subjectTaxSum - row.subjectTaxCredit - row.taxSumOutside
                } else {
                    row.taxSumToPay = 0
                }
            }
        }

        //графа20
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                if (row.subjectTaxCredit + row.taxSumOutside > row.subjectTaxSum) {
                    row.taxSumToReduction = row.subjectTaxCredit - row.subjectTaxSum + row.taxSumOutside
                } else {
                    row.taxSumToReduction = 0
                }
            }
        }

        // Подсчитываем суммы по графам, возможно стоит это сделать в одном цикле.
        rowTotal.propertyWeight = summ(formData, new ColumnRange('propertyWeight', 0, formData.dataRows.size() - 2))
        rowTotal.countWeight = summ(formData, new ColumnRange('countWeight', 0, formData.dataRows.size() - 2))
        rowTotal.baseTaxOf = summ(formData, new ColumnRange('baseTaxOf', 0, formData.dataRows.size() - 2))
        rowTotal.baseTaxOfRub = summ(formData, new ColumnRange('baseTaxOfRub', 0, formData.dataRows.size() - 2))
        rowTotal.subjectTaxSum = summ(formData, new ColumnRange('subjectTaxSum', 0, formData.dataRows.size() - 2))
        rowTotal.subjectTaxCredit = summ(formData, new ColumnRange('subjectTaxCredit', 0, formData.dataRows.size() - 2))
        rowTotal.taxSumToPay = summ(formData, new ColumnRange('taxSumToPay', 0, formData.dataRows.size() - 2))
        rowTotal.taxSumToReduction = summ(formData, new ColumnRange('taxSumToReduction', 0, formData.dataRows.size() - 2))

        //графа21
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                subjectTaxSumPrevItogo = 0  // Может изменится
                if (formPrev != null) {
                    subjectTaxSumPrevItogo = formPrev.getDataRow('total').subjectTaxSum
                }

                if (reportPeriodService.get(formData.reportPeriodId).order == 1) {
                    row.everyMontherPaymentAfterPeriod = rowTotal.subjectTaxSum <= 0 ? 0 : row.subjectTaxSum
                } else if (reportPeriodService.get(formData.reportPeriodId).order == 2 || reportPeriodService.get(formData.reportPeriodId).order == 3) {
                    if (rowTotal.subjectTaxSum - subjectTaxSumPrevItogo <= 0) {
                        row.everyMontherPaymentAfterPeriod = 0
                    } else {
                        row.everyMontherPaymentAfterPeriod = (BigDecimal) ((rowTotal.subjectTaxSum - subjectTaxSumPrevItogo) * (row.subjectTaxSum / rowTotal.subjectTaxSum)).setScale(0, BigDecimal.ROUND_HALF_UP)
                    }
                } else {
                    row.everyMontherPaymentAfterPeriod = 0
                }
            }
        }

        //графа22
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                if (reportPeriodService.get(formData.reportPeriodId).order == 3) {
                    row.everyMonthForKvartalNextPeriod = row.everyMontherPaymentAfterPeriod
                } else {
                    row.everyMonthForKvartalNextPeriod = 0
                }
            }
        }

        //графа23
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                if ((BigDecimal) (row.everyMontherPaymentAfterPeriod - 3 * (row.everyMontherPaymentAfterPeriod / 3)).setScale(0, BigDecimal.ROUND_HALF_UP)) {  // графа23
                    row.avansPayments1 = (BigDecimal) (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP) + (row.everyMontherPaymentAfterPeriod - 3 * (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP))
                } else {
                    row.avansPayments1 = (BigDecimal) (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP)
                }
            }
        }

        //графа24
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.avansPayments2 = (BigDecimal) (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP)
            }
        }

        //графа25
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                if ((BigDecimal) (row.everyMontherPaymentAfterPeriod - 3 * (row.everyMontherPaymentAfterPeriod / 3)).setScale(0, BigDecimal.ROUND_HALF_UP)) {
                    row.avansPayments3 = (BigDecimal) (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP)
                } else {
                    row.avansPayments3 = (BigDecimal) (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP) +
                            (BigDecimal) (row.everyMontherPaymentAfterPeriod - 3 * (row.everyMontherPaymentAfterPeriod / 3).setScale(0, BigDecimal.ROUND_HALF_UP))
                }
            }
        }

        //графа27
        for (row in formData.dataRows) {
            if (reportPeriodService.get(formData.reportPeriodId).order != 1) {
                baseTaxOfPrev = 0
                if (formPrev != null) {
                    for (rowPrev in formData.dataRows) {
                        if (row.divisionCode == rowPrev.divisionCode) {
                            baseTaxOfPrev = rowPrev.baseTaxOf
                        }
                    }
                }
                row.changeShareBaseTax = Math.abs(row.baseTaxOf - baseTaxOfPrev) / row.baseTaxOf
            }
        }

        //графа29
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.thisFond = row.propertyPrice
            }
        }

        //графа30
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                row.thisQuantity = row.workersCount
            }
        }

        //графа31
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                propertyPricePrev = 0
                if (formPrev != null) {
                    for (rowPrev in formData.dataRows) {
                        if (row.divisionCode == rowPrev.divisionCode) {
                            propertyPricePrev = rowPrev.propertyPrice
                        }
                    }
                }
                row.lastFond = propertyPricePrev
            }
        }

        //графа32
        for (row in formData.dataRows) {
            if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                workersCountPrev = 0
                if (formPrev != null) {
                    for (rowPrev in formData.dataRows) {
                        if (row.divisionCode == rowPrev.divisionCode) {
                            workersCountPrev = rowPrev.workersCount
                        }
                    }
                }
                row.lastQuantity = workersCountPrev
            }
        }

        // Досчитаем строки итого
        rowTotal.everyMontherPaymentAfterPeriod = summ(formData, new ColumnRange('everyMontherPaymentAfterPeriod', 0, formData.dataRows.size() - 2))
        rowTotal.everyMonthForKvartalNextPeriod = summ(formData, new ColumnRange('everyMonthForKvartalNextPeriod', 0, formData.dataRows.size() - 2))
        rowTotal.avansPayments1 = summ(formData, new ColumnRange('avansPayments1', 0, formData.dataRows.size() - 2))
        rowTotal.avansPayments2 = summ(formData, new ColumnRange('avansPayments2', 0, formData.dataRows.size() - 2))
        rowTotal.avansPayments3 = summ(formData, new ColumnRange('avansPayments3', 0, formData.dataRows.size() - 2))
        rowTotal.taxSumOutside = summ(formData, new ColumnRange('taxSumOutside', 0, formData.dataRows.size() - 2))

    }

} else {
    logger.error('Не могу заполнить поля, есть ошибки')
}