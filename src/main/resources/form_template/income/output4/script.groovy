import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*

/**
 * 6.3.4    Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        checkDecl()
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        calc()
        checkAfterCalc()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicCheck()
        calc()
        checkAfterCalc()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicCheck()
        calc()
        checkAfterCalc()
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
    r = null
    try {
        r = formData.getDataRow('total')
    } catch (IllegalArgumentException ignored) {
    }
    if (r != null && formData.dataRows.size() != 0 && currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1 && currentDataRow.getAlias() != 'total') {
        formData.deleteDataRow(r);
    }
    formData.dataRows.remove(currentDataRow)

// Обновим индексы http://jira.aplana.com/browse/SBRFACCTAX-1759
    i = 0;
    for (row in formData.dataRows) {
        i++
        row.number = i
    }
}

void addRow() {
    row = formData.createDataRow()
    // Если есть строка итого, удалим её
    if (formData.dataRows.size() != 0
            && formData.dataRows.get(formData.dataRows.size() - 1).getAlias() == 'total') {
        formData.dataRows.remove(formData.dataRows.size() - 1)
    }
    row = formData.appendDataRow()

    // Сделаем некоторые колонки изменяемыми
    for (alias in [
            'divisionName', 'stringCode', 'propertyPrice', 'workersCount', 'labalAboutPaymentTax', 'delta21', 'delta28'
    ]) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    row.number = formData.dataRows.size()
    row.delta21 = 0
    row.delta28 = 0
}

void checkAfterCalc() {
    for (row in formData.dataRows) {
        if (row.getAlias() != 'total') {
            if (row.kpp == row.subjectCode && row.subjectCode == '0') {
                logger.error("Все суммы по операции нулевые!")
            }
        }
    }
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

BigDecimal getTaxBase() {
    // Расчитываем распределяемая налоговая база за отчётный период
    formIncomeId = 302  // Сводная форма начисленных доходов (доходы сложные)
    formIncomePHYId = 301 // Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)
    formCostId = 303    // Сводная форма начисленных расходов (расходы сложные)
    formCostPHYId = 304 //Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)
    formIncome = FormDataService.find(formIncomeId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    formIncomePHY = FormDataService.find(formIncomePHYId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    formCost = FormDataService.find(formCostId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    formCostPHY = FormDataService.find(formCostPHYId, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
    BigDecimal taxBase = 0
    // доходы сложные
    if (formIncome != null) {
        for (value in formIncome.dataRows) {
            // Если ячейка не объеденена то она должна быть в списке
            String khy = value.getCell('incomeTypeId').hasValueOwner() ? value.getCell('incomeTypeId').getValueOwner().value : value.getCell('incomeTypeId').value

            BigDecimal incomeTaxSumS = (BigDecimal)(value.getCell('incomeTaxSumS').hasValueOwner() ? value.getCell('incomeTaxSumS').getValueOwner().value : value.getCell('incomeTaxSumS').value)
            incomeTaxSumS = incomeTaxSumS ?: 0
            //k1
            if (khy in ['10633', '10634', '10650', '10670']) {
                taxBase += incomeTaxSumS
            }
            //k5
            if (khy in ['10855', '10880', '10900']) {
                taxBase += incomeTaxSumS
            }
            //k6
            if (khy in ['10850']) {
                taxBase += incomeTaxSumS
            }
            //k7
            if (khy in ['11180', '11190', '11200', '11210', '11220', '11230', '11240', '11250', '11260']) {
                taxBase += incomeTaxSumS
            }
            //k8
            if (khy in ['11405', '11410', '11415', '13040', '13045', '13050', '13055', '13060', '13065', '13070', '13090', '13100', '13110', '13120', '13250', '13650', '13655', '13660', '13665', '13670', '13675', '13680', '13685', '13690', '13695', '13700', '13705', '13710', '13715', '13720', '13780', '13785', '13790', '13940', '13950', '13960', '13970', '13980', '13990', '14140', '14170', '14180', '14190', '14200', '14210', '14220', '14230', '14240', '14250', '14260', '14270', '14280', '14290' ]) {
                taxBase += incomeTaxSumS
            }
            //k13
            if (khy in ['10840']) {
                taxBase += incomeTaxSumS
            }
            //k15
            if (khy in ['10860']) {
                taxBase += incomeTaxSumS
            }
            //k16
            if (khy in ['10870']) {
                taxBase += incomeTaxSumS
            }
            //k19
            if (khy in ['10890']) {
                taxBase += incomeTaxSumS
            }
            //k21
            if (khy in ['13655', '13660', '13665', '13675', '13680', '13685', '13690', '13695', '13705', '13710', '13780', '13785', '13790' ]) {
                taxBase -= incomeTaxSumS
            }
        }
    }
    // Доходы простые
    if (formIncomePHY != null) {
        for (value in formIncomePHY.dataRows) {
            String khy = value.getCell('incomeTypeId').hasValueOwner() ? value.getCell('incomeTypeId').getValueOwner().value : value.getCell('incomeTypeId').value

            // графа 8
            BigDecimal rnu4Field5Accepted = (BigDecimal)(value.getCell('rnu4Field5Accepted').hasValueOwner() ? value.getCell('rnu4Field5Accepted').getValueOwner().value : value.getCell('rnu4Field5Accepted').value)
            rnu4Field5Accepted = rnu4Field5Accepted ?: 0

            // графа 5
            BigDecimal rnu6Field10Sum = (BigDecimal)(value.getCell('rnu6Field10Sum').hasValueOwner() ? value.getCell('rnu6Field10Sum').getValueOwner().value : value.getCell('rnu6Field10Sum').value)
            rnu6Field10Sum = rnu6Field10Sum ?: 0

            // графа 6
            BigDecimal rnu6Field12Accepted = (BigDecimal)(value.getCell('rnu6Field12Accepted').hasValueOwner() ? value.getCell('rnu6Field12Accepted').getValueOwner().value : value.getCell('rnu6Field12Accepted').value)
            rnu6Field12Accepted = rnu6Field12Accepted ?: 0
            //k2
            if (khy in ['10001', '10006', '10041', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10370', '10380', '10390', '10450', '10460', '10470', '10480', '10490', '10571', '10580', '10590', '10600', '10610', '10630', '10631', '10632', '10640', '10680', '10690', '10740', '10744', '10748', '10752', '10756', '10760', '10770', '10790', '10800', '11140', '11150', '11160', '11170', '11320', '11325', '11330', '11335', '11340', '11350', '11360', '11370', '11375']) {
                taxBase += rnu4Field5Accepted
            }
            //k3
            if (khy in ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640', '10680', '10690', '11340', '11350', '11370', '11375' ]) {
                taxBase += rnu6Field10Sum
            }
            //k4
            if (khy in ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350', '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640', '10680', '10690', '11340', '11350', '11370', '11375']) {
                taxBase -= rnu6Field12Accepted
            }
            //k9
            if (khy in ['11380', '11385', '11390', '11395', '11400', '11420', '11430', '11840', '11850', '11855', '11860', '11870', '11880', '11930', '11970', '12000', '12010', '12030', '12050', '12070', '12090', '12110', '12130', '12150', '12170', '12190', '12210', '12230', '12250', '12270', '12290', '12320', '12340', '12360', '12390', '12400', '12410', '12420', '12430', '12830', '12840', '12850', '12860', '12870', '12880', '12890', '12900', '12910', '12920', '12930', '12940', '12950', '12960', '12970', '12980', '12985', '12990', '13000', '13010', '13020', '13030', '13035', '13080', '13150', '13160', '13170', '13180', '13190', '13230', '13240', '13290', '13300', '13310', '13320', '13330', '13340', '13400', '13410', '13725', '13730', '13920', '13925', '13930', '14000', '14010', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14120', '14130', '14150', '14160' ]) {
                taxBase += rnu4Field5Accepted
            }
            //k10
            if (khy in ['11860', '11870', '11880', '11930', '11970', '12000', '13930', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14130', '14150', '14160']) {
                taxBase += rnu6Field10Sum
            }
            //k11
            if (khy in ['11860', '11870', '11880', '11930', '11970', '12000', '13930', '14020', '14030', '14040', '14050', '14060', '14070', '14080', '14090', '14100', '14110', '14130', '14150', '14160']) {
                taxBase -= rnu6Field12Accepted
            }
            //k12
            if (khy in ['13130', '13140' ]) {
                taxBase -= rnu4Field5Accepted
            }
            //k22
            if (khy in ['14000', '14010' ]) {
                taxBase -= rnu4Field5Accepted
            }
        }
    }
    // Расходы сложные
    if (formCost != null) {
        for (value in formCost.dataRows) {
            String khy = value.getCell('consumptionTypeId').hasValueOwner() ? value.getCell('consumptionTypeId').getValueOwner().value : value.getCell('consumptionTypeId').value

            // 9
            BigDecimal consumptionTaxSumS = (BigDecimal)(value.getCell('consumptionTaxSumS').hasValueOwner() ? value.getCell('consumptionTaxSumS').getValueOwner().value : value.getCell('consumptionTaxSumS').value)
            consumptionTaxSumS = consumptionTaxSumS ?: 0

            //k14
            if (khy in ['21780']) {
                taxBase += consumptionTaxSumS
            }
            //k17
            if (khy in ['21500']) {
                taxBase += consumptionTaxSumS
            }
            //k18
            if (khy in ['21510']) {
                taxBase += consumptionTaxSumS
            }
            //k20
            if (khy in ['21390']) {
                taxBase += consumptionTaxSumS
            }
            //k23
            if (khy in ['20320', '20321', '20470', '20750', '20755', '20760', '20765', '20770', '20775', '20780', '20785', '21210', '21280', '21345', '21355', '21365', '21370', '21375', '21380' ]) {
                taxBase -= consumptionTaxSumS
            }
            //k27
            if (khy in ['21450', '21740', '21750']) {
                taxBase -= consumptionTaxSumS
            }
            //k28
            if (khy in ['21770']) {
                taxBase -= consumptionTaxSumS
            }
            //k29
            if (khy in ['21662', '21664', '21666', '21668', '21670', '21672', '21674', '21676', '21678', '21680']) {
                taxBase -= consumptionTaxSumS
            }
            //k30
            if (khy in ['21520', '21530']) {
                taxBase -= consumptionTaxSumS
            }
            //k31
            if (khy in ['22500', '22505', '22585', '22590', '22595', '22660', '22664', '22668', '22670', '22690', '22695', '22700', '23120', '23130', '23140', '23240']) {
                taxBase -= consumptionTaxSumS
            }
            //k35
            if (khy in ['22492']) {
                taxBase += consumptionTaxSumS
            }
            //k36
            if (khy in ['23150']) {
                taxBase += consumptionTaxSumS
            }
            //k37
            if (khy in ['23160']) {
                taxBase += consumptionTaxSumS
            }
            //k38
            if (khy in ['23170']) {
                taxBase += consumptionTaxSumS
            }
            //k39
            if (khy in ['21760']) {
                taxBase -= consumptionTaxSumS
            }
            //k40
            if (khy in ['21460']) {
                taxBase -= consumptionTaxSumS
            }
            //k41
            if (khy in ['21470']) {
                taxBase -= consumptionTaxSumS
            }
            //k42
            if (khy in ['21385']) {
                taxBase -= consumptionTaxSumS
            }
        }
    }
    // Расходы простые
    if (formCostPHY != null) {
        for (value in formCostPHY.dataRows) {
            String khy = value.getCell('consumptionTypeId').hasValueOwner() ? value.getCell('consumptionTypeId').getValueOwner().value : value.getCell('consumptionTypeId').value

            // 8
            BigDecimal rnu5Field5Accepted = (BigDecimal)(value.getCell('rnu5Field5Accepted').hasValueOwner() ? value.getCell('rnu5Field5Accepted').getValueOwner().value : value.getCell('rnu5Field5Accepted').value)
            rnu5Field5Accepted = rnu5Field5Accepted ?: 0

            // 5
            BigDecimal rnu7Field10Sum = (BigDecimal)(value.getCell('rnu7Field10Sum').hasValueOwner() ? value.getCell('rnu7Field10Sum').getValueOwner().value : value.getCell('rnu7Field10Sum').value)
            rnu7Field10Sum = rnu7Field10Sum ?: 0

            // 6
            BigDecimal rnu7Field12Accepted = (BigDecimal)(value.getCell('rnu7Field10Sum').hasValueOwner() ? value.getCell('rnu7Field10Sum').getValueOwner().value : value.getCell('rnu7Field10Sum').value)
            rnu7Field12Accepted = rnu7Field12Accepted ?: 0

            //k24
            if (khy in ['20291', '20300', '20310', '20330', '20332', '20334', '20336', '20338', '20339', '20340', '20360', '20364', '20368', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500', '20510', '20520', '20530', '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20820', '20825', '20830', '20840', '20850', '20860', '20870', '20880', '20890', '20920', '20940', '20945', '20950', '20960', '20970', '21020','21025', '21030', '21050', '21055', '21060', '21065', '21080', '21130', '21140', '21150', '21154', '21158', '21170', '21270', '21290', '21295', '21300', '21305', '21310', '21315', '21320', '21325', '21340', '21350', '21360', '21400', '21405', '21410', '21580', '21590', '21600', '21610', '21620', '21660', '21700', '21710', '21720', '21730', '21790', '21800', '21810']) {
                taxBase -= rnu5Field5Accepted
            }
            //k25
            if (khy in ['20300', '20360', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500',  '20530',  '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20825', '20830', '20840', '20850', '20870', '20880', '20890', '20950', '20960', '20970', '21020', '21025', '21030', '21050', '21055', '21060', '21065', '21080',  '21130', '21140', '21150', '21154', '21158', '21170', '21400', '21405', '21410', '21580', '21590', '21620', '21660', '21700', '21710', '21730', '21790', '21800', '21810']) {
                taxBase -= rnu7Field10Sum
            }
            //k26
            if (khy in ['20300', '20360', '20370', '20430', '20434', '20438', '20440', '20442', '20446', '20448', '20450', '20452', '20454', '20456', '20458', '20460', '20464', '20468', '20475', '20480', '20485', '20490', '20500',  '20530',  '20540', '20550', '20690', '20694', '20698', '20700', '20710', '20810', '20812', '20814', '20816', '20825', '20830', '20840', '20850', '20870', '20880', '20890', '20950', '20960', '20970', '21020', '21025', '21030', '21050', '21055', '21060', '21065', '21080',  '21130', '21140', '21150', '21154', '21158', '21170', '21400', '21405', '21410', '21580', '21590', '21620', '21660', '21700', '21710', '21730', '21790', '21800', '21810']) {
                taxBase += rnu7Field12Accepted
            }
            //k32
            if (khy in ['22000', '22010', '22020', '22030', '22040', '22050', '22060', '22070', '22080', '22090', '22100', '22110', '22120', '22130', '22140', '22150', '22160', '22170', '22180', '22190', '22200', '22210', '22220', '22230', '22240', '22250', '22260', '22270', '22280', '22290', '22300', '22310', '22320', '22330', '22340', '22350', '22360', '22370', '22380', '22385', '22390', '22395', '22400', '22405', '22410', '22415', '22420', '22425', '22430', '22435', '22440', '22445', '22450', '22455', '22460', '22465', '22470', '22475', '22480', '22485', '22490', '22496', '22498', '22530', '22534', '22538', '22540', '22544', '22548', '22550', '22560', '22565', '22570', '22575', '22580', '22600', '22610', '22640', '22680', '22710', '22715', '22720', '22750', '22760', '22800', '22810', '22840', '22850', '22860', '22870', '23040', '23050', '23100', '23110', '23200', '23210', '23220', '23230', '23250', '23260', '23270', '23280' ]) {
                taxBase -= rnu5Field5Accepted
            }
            //k33
            if (khy in ['22570', '22575', '22580', '22720', '22750', '22760', '22800', '22810', '23200', '23210', '23230', '23250', '23260', '23270', '23280']) {
                taxBase -= rnu7Field10Sum
            }
            //k34
            if (khy in ['22570', '22575', '22580', '22720', '22750', '22760', '22800', '22810', '23200', '23210', '23230', '23250', '23260', '23270', '23280']) {
                taxBase += rnu7Field12Accepted
            }
        }
    }
    // taxBase = распределяемая налоговая база за отчётный период
    return taxBase
}

/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.1.9.1  Алгоритмы заполнения полей формы
 */
void calc() {
    for (row in formData.dataRows) {

        if (row.getAlias() != 'total') {

            for (alias in ['divisionName',
                    'stringCode', 'labalAboutPaymentTax',
                    'propertyPrice', 'workersCount',

            ]) {
                if (row.getCell(alias).value == null) {
                    logger.error('Поле ' + row.getCell(alias).column.name.replace('%', '') + ' не заполнено')
                }
            }
            if (row.stringCode == null || ![1, 2, 3, 4].contains(row.stringCode.intValue())) {
                logger.error("Код строки 002 неверный!")
            }
            if (row.labalAboutPaymentTax == null || ![1, 0].contains(row.labalAboutPaymentTax.intValue())) {
                logger.error("Код строки «Отметка о возложении обязанности по уплате налога» неверный!")
            }
        }
    }
    if (!logger.containsLevel(LogLevel.ERROR)) {

        if (formData.dataRows.size() != 0) {

            // Строка итого
            rowTotal = formData.createDataRow()
            if (formData.dataRows.get(formData.dataRows.size() - 1).getAlias() != 'total') {
                rowTotal = formData.appendDataRow('total')
            } else {
                rowTotal = formData.getDataRow('total')
            }
            rowTotal.bankName = "Итого: "
            rowTotal.propertyPrice = summ(formData, new ColumnRange('propertyPrice', 0, formData.dataRows.size() - 2))
            rowTotal.workersCount = summ(formData, new ColumnRange('workersCount', 0, formData.dataRows.size() - 2))

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

                    if (department.id == 113) {
                        row.bankName = 'Центральный аппарат'
                        row.bankCode = '790000'
                    } else {
                        row.bankName = departmentParent.name
                        row.bankCode = departmentParent.sbrfCode
                    }
                    row.divisionCode = department.sbrfCode
                    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
                    departmentParam = departmentService.getDepartmentParam((int) department.id)
                    row.kpp = departmentParam.kpp
                    row.subjectCode = department.dictRegionId
                    row.subjectName = dictionaryRegionService.getRegionByCode(department.getDictRegionId()).getName()
                    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
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
                    row.baseTaxOf = (BigDecimal) ((row.propertyWeight + row.countWeight) / 2).setScale(8, BigDecimal.ROUND_HALF_UP)
                }
            }

            //графа28
            for (row in formData.dataRows) {
                if (!rowTotal.getAlias().equals(row.getAlias())) {   // Пропустим строку итого
                    //noinspection GroovyVariableNotAssigned
                    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
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
                        subjectTaxSumPrevItogo = formPrev.dataRows.get(formPrev.dataRows.size() - 1).subjectTaxSum
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
                    BigDecimal temp = row.baseTaxOf - baseTaxOfPrev
                    row.changeShareBaseTax = temp.abs() / row.baseTaxOf
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
}

void logicCheck() {
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id)
    }
    if (formPrev == null) {
        logger.warn('Форма за предыдущий отчётный период не создавалась!')
    }
}