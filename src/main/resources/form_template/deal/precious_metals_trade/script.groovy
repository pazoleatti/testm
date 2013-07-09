package form_template.deal.precious_metals_trade

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * Купля-продажа драгоценных металлов
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        calc()
        addAllStatic()
        logicCheck()
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
        formData.dataRows.remove(currentDataRow)
        recalcRowNum()
    }
}

/**
 * Пересчет индексов строк перед удалением строки
 */
void recalcRowNum() {
    int i = 1
    for (row in formData.dataRows) {
        if (row.getAlias() == null) {
            row.getCell('rowNum').value = i++
        }
    }
}


void addRow() {
    row = formData.createDataRow()
    for (alias in ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate',
            'dealFocus', 'deliverySign', 'metalName', 'foreignDeal',
            'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2',
            'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    formData.dataRows.add(row)
    recalcRowNum()
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
        if (row.getAlias() != null) {
            continue
        }
        rowNum = row.getCell('rowNum').value
        dealDateCell = row.getCell('dealDate')
        for (alias in ['fullName', 'interdependence', 'inn', 'countryName', 'countryCode', 'docNumber', 'docDate',
                'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName', 'foreignDeal',
                'count', 'price', 'total', 'dealDoneDate']) {
            if (row.getCell(alias).value == null || row.getCell(alias).value.toString().isEmpty()) {
                logger.error('Графа «' + row.getCell(alias).column.name + '» в строке ' + rowNum + ' не заполнена!')
            }
        }
        //  Корректность даты договора
        def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        dt = row.getCell('docDate').value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            msg = row.getCell('docDate').column.name
            if (dt > dTo) {
                logger.error("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.error("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Корректность даты заключения сделки
        if (row.getCell('docDate').value > dealDateCell.value) {
            logger.error('«' + dealDateCell.column.name + '» не может быть меньше «' +
                    row.getCell('docDate').column.name + '» в строке ' + rowNum + '!')
        }
        // Зависимости от признака физической поставки
        // TODO (проверка связана со справочниками) if (row.getCell('deliverySign').value == 1 ){
        if (false) {
            for (alias in ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2']) {
                cell = row.getCell(alias)
                if (cell.value != null && !cell.value.toString().isEmpty()) {
                    logger.error('«' + row.getCell('deliverySign').column.name + '» указан «ОМС», ' +
                            'графа «' + cell.column.name + '» строки ' + rowNum + ' заполняться не должна!')
                }
            }
        }
        // Проверка заполнения населенного пункта
        localityCell = row.getCell('locality');
        cityCell = row.getCell('city');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.error(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        localityCell = row.getCell('locality2');
        cityCell = row.getCell('city2');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.error(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        // Проверка доходов и расходов
        if (row.getCell('incomeSum').value != null && row.getCell('outcomeSum').value != null) {
            logger.error('«' + row.getCell('incomeSum').column.name + '»' +
                    ' и «' + row.getCell('outcomeSum').column.name + '» в строке ' +
                    rowNum + ' не могут быть одновременно заполнены!')
        }
        if (row.getCell('incomeSum').value == null && row.getCell('outcomeSum').value == null) {
            logger.error('Одна из граф «' + row.getCell('incomeSum').column.name + '»' +
                    ' и «' + row.getCell('outcomeSum').column.name + '»' +
                    rowNum + ' должна быть заполнена!')
        }
        // Проверка на заполнение поля
        // TODO (проверка связана со справочниками) if ('поставочная сделка'.equals( row.getCell('deliverySign').value))
        if (false) {
            for (alias in ['foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2',
                    'region2', 'city2', 'locality2', 'deliveryCode']) {
                cell = row.getCell(alias)
                if (cell.value == null || cell.value.toString().isEmpty()) {
                    logger.error('Графа «' + cell.column.name + '» в строке ' + rowNum + ' не заполнена по поставочной сделке!')
                }
            }
        }
        // Проверка количества
        if (row.getCell('count').value != 1) {
            logger.error('В графе «' + row.getCell('count').column.name + '» в строке ' + rowNum + ' может  быть указано только  значение «1»!')
        }
        // Проверка внешнеторговой сделки
        // TODO (проверки связаны со справочниками)
        // Проверка стоимости
        total = row.getCell('total')
        count = row.getCell('count')
        price = row.getCell('price')
        if (total.value != count.value * price.value) {
            logger.error('«' + total.column.name + '» в строке ' + rowNum + ' не равна произведению цены и количества!')
        }
        // Корректность даты совершения сделки
        if (row.getCell('dealDoneDate').value < dealDateCell.value) {
            logger.error('«' + row.getCell('dealDoneDate').column.name + '» в строке ' + rowNum + ' не может быть меньше «' +
                    dealDateCell.column.name + '» в строке ' + rowNum + '!')
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
        // Расчет поля "Цена"
        row.getCell('price').value = row.getCell('incomeSum').value != null ? row.getCell('incomeSum').value : row.getCell('outcomeSum').value
        // Расчет поля "Итого"
        row.getCell('total').value = row.getCell('price').value
        // Расчет поля "Количество"
        row.getCell('count').value = 1
        // TODO расчет полей по справочникам
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def newRow = formData.createDataRow()

        newRow.getCell('fullName').value = 'Подитог:'
        newRow.setAlias('itg')
        newRow.getCell('fullName').colSpan = 22

        // Расчеты подитоговых значений
        BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
        for (row in formData.dataRows) {

            incomeSum = row.getCell('incomeSum').value
            outcomeSum = row.getCell('outcomeSum').value
            total = row.getCell('total').value

            incomeSumItg += incomeSum != null ? incomeSum : 0
            outcomeSumItg += outcomeSum != null ? outcomeSum : 0
            totalItg += total != null ? total : 0
        }

        newRow.getCell('incomeSum').value = incomeSumItg
        newRow.getCell('outcomeSum').value = outcomeSumItg
        newRow.getCell('total').value = totalItg

        formData.dataRows.add(newRow)
    }
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    for (Iterator<DataRow> iter = formData.dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
        }
    }
}