package form_template.deal.corporate_credit

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Предоставление корпоративного кредита
 *
 * похож на  software_development (Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий)
 * похож на  trademark (Предоставление права пользования товарным знаком)
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        calc()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkMatrix()
        logicCheck()
        calc()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkMatrix()
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
    if (currentDataRow != null) {
        recalcRowNum()
        formData.dataRows.remove(currentDataRow)
    }
}

void recalcRowNum() {
    def i = formData.dataRows.indexOf(currentDataRow)

    for (row in formData.dataRows[i..formData.dataRows.size()-1]) {
        row.getCell('rowNumber').value = i++
    }
}

void addRow() {
    row = formData.createDataRow()
    for (alias in ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)

    row.getCell('count').value = 1

    row.getCell('rowNumber').value = formData.dataRows.size()
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Cформирована ли для выбранного пользователем отчета форма-приемник (консолидированный отчет или «Матрица»)
 */
void checkMatrix() {
    // TODO
    if (false) {
        // сформирована и имеет статус, отличный от «Создана»
        logger.error("Принятие отчета невозможно, т.к. уже подготовлена форма-приемник.")
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    for (row in formData.dataRows) {
        rowNum = row.getCell('rowNumber').value
        docDateCell = row.getCell('docDate')
        for (alias in ['fullNamePerson', 'inn', 'countryName', 'sum', 'docNumber', 'docDate',
                'count', 'price', 'cost', 'dealDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }
        }
        // Проверка количества
        if (row.getCell('count').value != 1) {
            logger.error('В графе  «'+row.getCell('count').column.name+'» может  быть указано только  значение «1»!')
        }
        //  Корректность даты договора
        def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            msg = docDateCell.column.name
            if (dt > dTo) {
                logger.error("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.error("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Проверка доходов
        sumCell = row.getCell('sum')
        priceCell = row.getCell('price')
        costCell = row.getCell('cost')
        if (priceCell.value != sumCell.value) {
            logger.error('«' + priceCell.column.name + '» в строке ' + rowNum + ' не может отличаться от «' + sumCell.column.name + '»!')
        }
        if (costCell.value != sumCell.value) {
            logger.error('«' + costCell.column.name + '» в строке ' + rowNum + ' не может отличаться от «' + sumCell.column.name + '»!')
        }
        // Корректность даты совершения сделки
        dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            logger.error('«' + dealDateCell.column.name + '» не может быть меньше «' + docDateCell.column.name + '» в строке ' + rowNum + '!')
        }
    }
    checkNSI()
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI() {
    for (row in formData.dataRows) {
        // TODO добавить проверки НСИ
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    for (row in formData.dataRows) {
        // В поле "Количество" подставляется значение «1»
        row.getCell('count').value = 1
        // Расчет поля "Цена"
        row.getCell('price').value = row.getCell('sum').value
        // Расчет поля "Стоимость"
        row.getCell('cost').value = row.getCell('sum').value
        // TODO расчет полей по справочникам
    }
}