package form_template.deal.precious_metals_trade

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 * 394 - Купля-продажа драгоценных металлов
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
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
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        acceptance()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        deleteAllStatic()
        calc()
        addAllStatic()
        logicCheck()
        break
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = 0
    ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName',
            'foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2',
            'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while (pointRow.getAlias() != null && index > 0) {
            pointRow = dataRows.get(--index)
        }
        if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
            index++
        }
    } else if (size > 0) {
        for (int i = size - 1; i >= 0; i--) {
            def pointRow = dataRows.get(i)
            if (pointRow.getAlias() == null) {
                index = dataRows.indexOf(pointRow) + 1
                break
            }
        }
    }
    dataRowHelper.insert(row, index + 1)
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    def index = 1;
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = index++
        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        [
                'rowNum',         // № п/п
                'fullName',       // Полное наименование с указанием ОПФ
                'interdependence',// Признак взаимозависимости
                'inn',            // ИНН/КИО
                'countryName',    // Наименование страны регистрации
                'countryCode',    // Код страны по классификатору ОКСМ
                'docNumber',      // Номер договора
                'docDate',        // Дата договора
                'dealNumber',     // Номер сделки
                'dealDate',       // Дата заключения сделки
                'dealFocus',      // Направленности сделок
                'deliverySign',   // Признак физической поставки драгоценного металла
                'metalName',      // Наименование драгоценного металла
                'foreignDeal',    // Внешнеторговая сделка
                'count',          // Количество
                'price',          // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'total',          // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'dealDoneDate'    // Дата совершения сделки

        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("«$msg» в строке $rowNum не может быть вне налогового периода!")
        }
        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Зависимости от признака физической поставки
        if (getDeliverySign(row.deliverySign) == 1) {
            def msg1 = row.getCell('deliverySign').column.name
            ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode'].each {
                def cell = row.getCell(it)
                if (cell.value != null && !cell.value.toString().isEmpty()) {
                    def msg2 = cell.column.name
                    logger.warn("«$msg1» указан «ОМС», графа «$msg2» строки $rowNum заполняться не должна!")
                }
            }
        }
        // Проверка заполнения населенного пункта
        localityCell = row.getCell('locality');
        cityCell = row.getCell('city');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        localityCell = row.getCell('locality2');
        cityCell = row.getCell('city2');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.warn("Поля «$msgIn» и «$msgOut» в строке $rowNum не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
        }
        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("В графе «$msg» в строке $rowNum может  быть указано только значение «1»!")
        }
        // Проверка внешнеторговой сделки
        def msg14 = row.getCell('foreignDeal').column.name
        def sign = refBookService.getNumberValue(38, row.foreignDeal, 'CODE')
        if (row.countryCodeNumeric == row.countryCodeNumeric2 && sign != 0) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Нет»!")
        } else if (row.countryCodeNumeric != row.countryCodeNumeric2 && sign != 1) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Да»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Проверка заполнения стоимости сделки
        def total = row.getCell('total')
        def price = row.getCell('price')
        if (total.value != price.value) {
            def msg1 = total.column.name
            def msg2 = price.column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» в строке $rowNum!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric2", "ОКСМ", 10)
        checkNSI(row, "regionCode", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "metalName", "Коды драгоценных металлов", 17)
        checkNSI(row, "deliverySign", "Признаки физической поставки", 18)
        checkNSI(row, "deliveryCode", "Коды условий поставки", 63)
        checkNSI(row, "dealFocus", "Направленности сделок", 20)
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
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def index = 1;
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNum = index++
        // Расчет поля "Цена"
        if (row.incomeSum != null && row.outcomeSum != null) {
            row.price = row.incomeSum - row.outcomeSum
            if (row.price < 0)
                row.price = -row.price
        } else
            row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price
        // Расчет поля "Количество"
        row.count = 1

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.numberValue
            row.countryCode = map.COUNTRY.referenceValue
            row.countryName = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
            row.countryName = null
        }
        if (row.deliverySign == 1) {
            row.countryCodeNumeric = null
            row.regionCode = null
            row.city = null
            row.locality = null
            row.countryCodeNumeric2 = null
            row.region2 = null
            row.city2 = null
            row.locality2 = null
        }
        if (row.countryCodeNumeric == row.countryCodeNumeric2) {
            row.foreignDeal = Long.valueOf(182632)
        } else {
            row.foreignDeal = Long.valueOf(182633)
        }
    }

    dataRowHelper.update(dataRows);
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                }
            }
        }
    }
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
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
 * Сортировка строк по гр.
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.sort({ DataRow a, DataRow b ->
        // гр. 2.1, гр. 3, гр. 5, гр. 6, гр. 9, гр. 10, гр. 11, гр. 12, гр. 15
        sortRow(['fullName', 'inn', 'docNumber', 'docDate', 'dealFocus',
                'deliverySign', 'metalName', 'foreignDeal', 'deliveryCode'], a, b)
    })
    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        aD = a.getCell(param).value
        bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        def price = row.price
        def total = row.total

        priceItg += price != null ? price : 0
        totalItg += total != null ? total : 0
    }

    newRow.price = priceItg
    newRow.total = totalItg

    newRow
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }

            if (row.getAlias() == null)
                if (nextRow == null
                        || row.fullName != nextRow.fullName
                        || row.inn != nextRow.inn
                        || row.docNumber != nextRow.docNumber
                        || row.docDate != nextRow.docDate
                        || row.dealFocus != nextRow.dealFocus
                        || row.deliverySign != nextRow.deliverySign
                        || row.metalName != nextRow.metalName
                        || row.foreignDeal != nextRow.foreignDeal
                        || row.deliveryCode != nextRow.deliveryCode) {

                    def itogRow = calcItog(i)
                    dataRowHelper.insert(itogRow, ++i + 1)
                }
        }
    }
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.contains('.xls')) {
        logger.error('Формат файла должен быть *.xls')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    // добавить данные в форму
    try {
        if (!checkTableHead(xml, 4)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml, 3)
    } catch (Exception e) {
        logger.error("" + e.message)
    }
}

def isEquals(def xmlValue, String value) {
    return xmlValue.text().trim() == value.trim()
}
/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 28
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
    isEquals(xml.row[0].cell[0], 'Полное наименование с указанием ОПФ') &&
            isEquals(xml.row[2].cell[0], '2') &&
            isEquals(xml.row[3].cell[0], 'гр. 2.1') &&
            isEquals(xml.row[0].cell[1], 'Признак взаимозависимости') &&
            isEquals(xml.row[2].cell[1], '3') &&
            isEquals(xml.row[3].cell[1], 'гр. 2.2') &&
            isEquals(xml.row[0].cell[2], 'ИНН/ КИО') &&
            isEquals(xml.row[2].cell[2], '4') &&
            isEquals(xml.row[3].cell[2], 'гр. 3') &&
            isEquals(xml.row[0].cell[3], 'Наименование страны регистрации') &&
            isEquals(xml.row[2].cell[3], '5') &&
            isEquals(xml.row[3].cell[3], 'гр. 4.1') &&
            isEquals(xml.row[0].cell[4], 'Код страны регистрации по классификатору ОКСМ') &&
            isEquals(xml.row[2].cell[4], '6') &&
            isEquals(xml.row[3].cell[4], 'гр. 4.2') &&
            isEquals(xml.row[0].cell[5], 'Номер договора') &&
            isEquals(xml.row[2].cell[5], '7') &&
            isEquals(xml.row[3].cell[5], 'гр. 5') &&
            isEquals(xml.row[0].cell[6], 'Дата договора') &&
            isEquals(xml.row[2].cell[6], '8') &&
            isEquals(xml.row[3].cell[6], 'гр. 6') &&
            isEquals(xml.row[0].cell[7], 'Номер сделки') &&
            isEquals(xml.row[2].cell[7], '9') &&
            isEquals(xml.row[3].cell[7], 'гр. 7') &&
            isEquals(xml.row[0].cell[8], 'Дата заключения сделки') &&
            isEquals(xml.row[2].cell[8], '10') &&
            isEquals(xml.row[3].cell[8], 'гр. 8') &&
            isEquals(xml.row[0].cell[9], 'Направленность сделки') &&
            isEquals(xml.row[2].cell[9], '11') &&
            isEquals(xml.row[3].cell[9], 'гр. 9') &&
            isEquals(xml.row[0].cell[10], 'Признак физической поставки драгоценного металла') &&
            isEquals(xml.row[2].cell[10], '12') &&
            isEquals(xml.row[3].cell[10], 'гр. 10') &&
            isEquals(xml.row[0].cell[11], 'Наименование драгоценного металла') &&
            isEquals(xml.row[2].cell[11], '13') &&
            isEquals(xml.row[3].cell[11], 'гр. 11') &&
            isEquals(xml.row[0].cell[12], 'Внешнеторговая сделка') &&
            isEquals(xml.row[2].cell[12], '14') &&
            isEquals(xml.row[3].cell[12], 'гр. 12') &&
            //isEquals(xml.row[0].cell[13], 'Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами') &&
            isEquals(xml.row[1].cell[13], '\"Код страны по классификатору ОКСМ (цифровой)\"') &&
            isEquals(xml.row[2].cell[13], '15') &&
            isEquals(xml.row[3].cell[13], 'гр. 13.1') &&
            isEquals(xml.row[0].cell[14], '') &&
            isEquals(xml.row[1].cell[14], '\"Регион (код)\"') &&
            isEquals(xml.row[2].cell[14], '16') &&
            isEquals(xml.row[3].cell[14], 'гр. 13.2') &&
            isEquals(xml.row[0].cell[15], '') &&
            isEquals(xml.row[1].cell[15], 'Город') &&
            isEquals(xml.row[2].cell[15], '17') &&
            isEquals(xml.row[3].cell[15], 'гр. 13.3') &&
            isEquals(xml.row[0].cell[16], '') &&
            isEquals(xml.row[1].cell[16], 'Населенный пункт (село, поселок и т.д.)') &&
            isEquals(xml.row[2].cell[16], '18') &&
            isEquals(xml.row[3].cell[16], 'гр. 13.4') &&
            isEquals(xml.row[0].cell[17], 'Место совершения сделки (адрес места доставки (разгрузки драгоценного металла)') &&
            isEquals(xml.row[1].cell[17], 'Код страны по классификатору ОКСМ (цифровой)') &&
            isEquals(xml.row[2].cell[17], '19') &&
            isEquals(xml.row[3].cell[17], 'гр. 14.1') &&
            isEquals(xml.row[0].cell[18], '') &&
            isEquals(xml.row[1].cell[18], '\"Регион (код)\"') &&
            isEquals(xml.row[2].cell[18], '20') &&
            isEquals(xml.row[3].cell[18], 'гр. 14.2') &&
            isEquals(xml.row[0].cell[19], '') &&
            isEquals(xml.row[1].cell[19], 'Город') &&
            isEquals(xml.row[2].cell[19], '21') &&
            isEquals(xml.row[3].cell[19], 'гр. 14.3') &&
            isEquals(xml.row[0].cell[20], '') &&
            isEquals(xml.row[1].cell[20], 'Населенный пункт (село, поселок и т.д.)') &&
            isEquals(xml.row[2].cell[20], '22') &&
            isEquals(xml.row[3].cell[20], 'гр. 14.4') &&
            isEquals(xml.row[0].cell[21], 'Код условия поставки') &&
            isEquals(xml.row[2].cell[21], '23') &&
            isEquals(xml.row[3].cell[21], 'гр. 15') &&
            isEquals(xml.row[0].cell[22], 'Количество') &&
            isEquals(xml.row[2].cell[22], '24') &&
            isEquals(xml.row[3].cell[22], 'гр. 16') &&
            isEquals(xml.row[0].cell[23], 'Сумма доходов Банка по данным бухгалтерского учета, руб.') &&
            isEquals(xml.row[2].cell[23], '25') &&
            isEquals(xml.row[3].cell[23], 'гр. 17') &&
            isEquals(xml.row[0].cell[24], 'Сумма расходов Банка по данным бухгалтерского учета, руб.') &&
            isEquals(xml.row[2].cell[24], '26') &&
            isEquals(xml.row[3].cell[24], 'гр. 18') &&
            isEquals(xml.row[0].cell[25], 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.') &&
            isEquals(xml.row[2].cell[25], '27') &&
            isEquals(xml.row[3].cell[25], 'гр. 19') &&
            isEquals(xml.row[0].cell[26], 'Итого стоимость без учета НДС, акцизов и пошлины, руб.') &&
            isEquals(xml.row[2].cell[26], '28') &&
            isEquals(xml.row[3].cell[26], 'гр. 20') &&
            isEquals(xml.row[0].cell[27], 'Дата совершения сделки') &&
            isEquals(xml.row[2].cell[27], '29') &&
            isEquals(xml.row[3].cell[27], 'гр. 21'))

    return result
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, int headRowCount) {
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName',
                'foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2',
                'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNum = indexRow - headRowCount

        // графа 2.1
        newRow.fullName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 2.2
        newRow.interdependence = getRecordId(38, 'VALUE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
//        newRow.inn =
        indexCell++

        // графа 4.1
//        newRow.countryName =
        indexCell++

        // графа 4.2
//        newRow.countryCode =
        indexCell++

        // графа 5
        newRow.docNumber = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.dealNumber = row.cell[indexCell].text()
        indexCell++

        // графа 8
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.dealFocus = getRecordId(20, 'DIRECTION', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 10
        newRow.deliverySign = getRecordId(18, 'SIGN', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.metalName = getRecordId(17, 'INNER_CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.foreignDeal = getRecordId(38, 'VALUE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 13.1
        newRow.countryCodeNumeric = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 13.2
        String code = row.cell[indexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.regionCode = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell)
        indexCell++

        // графа 13.3
        newRow.city = row.cell[indexCell].text()
        indexCell++

        // графа 13.4
        newRow.locality = row.cell[indexCell].text()
        indexCell++

        // графа 14.1
        newRow.countryCodeNumeric2 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 14.2
        code = row.cell[indexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell)
        indexCell++

        // графа 14.3
        newRow.city2 = row.cell[indexCell].text()
        indexCell++

        // графа 14.4
        newRow.locality2 = row.cell[indexCell].text()
        indexCell++

        // графа 15
        newRow.deliveryCode = getRecordId(63, 'STRCODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 16
        //newRow.count
        indexCell++

        // графа 17
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 18
        newRow.outcomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 19
        //newRow.price
        indexCell++

        // графа 20
        //newRow.total
        indexCell++

        // графа 21
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - headRowCount)
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value, int indexRow, int indexCell) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String code, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter = code + "= '" + value + "'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    } else {
        throw new Exception("Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
    }
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, int indexRow, int indexCell) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}


/**
 * Получить признак физической поставки драгоценного металла
 */
def getDeliverySign(def deliverySign) {
    return  refBookService.getNumberValue(18,deliverySign,'CODE')
}
