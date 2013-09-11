package form_template.deal.letter_of_credit

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 386 - Предоставление инструментов торгового финансирования и непокрытых аккредитивов
 * (похож на guarantees "Предоставление гарантий")
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
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
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
// импорт из xls
    case FormDataEvent.IMPORT :
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
    dataRowHelper.save(dataRowHelper.getAllCached())
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = 0
    [
            'fullName',
            'docNumber',
            'docDate',
            'dealNumber',
            'dealDate',
            'sum',
            'dealDoneDate'
    ].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while(pointRow.getAlias()!=null && index>0){
            pointRow = dataRows.get(--index)
        }
        if(index!=currentDataRow.getIndex() && dataRows.get(index).getAlias()==null){
            index++
        }
    }else if (size>0) {
        for(int i = size-1;i>=0;i--){
            def pointRow = dataRows.get(i)
            if(pointRow.getAlias()==null){
                index = dataRows.indexOf(pointRow)+1
                break
            }
        }
    }
    dataRowHelper.insert(row, index+1)
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
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
        def dealDateCell = row.getCell('dealDate')
        def docDateCell = row.getCell('docDate')
        [
                'rowNumber',   // № п/п
                'fullName',    // Полное наименование юридического лица с указанием ОПФ
                'inn',         // ИНН/КИО
                'countryCode', // Страна регистрации
                'docNumber',   // Номер договора
                'docDate',     // Дата договора
                'dealNumber',  // Номер сделки
                'dealDate',    // Дата сделки
                'sum',         // Сумма доходов Банка по данным бухгалтерского учета, руб.
                'price',       // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'total',       // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'dealDoneDate' // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        //  Корректность даты договора
        def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            if (dt > dTo) {
                logger.warn("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.warn("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Корректность даты сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Проверка доходности
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
        }
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
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
    def int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // Порядковый номер строки
        row.rowNumber = index++
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итог"
        row.total = row.sum

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.numberValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
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
        def row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()
        def newRow = formData.createDataRow()

        newRow.getCell('itog').colSpan = 8
        newRow.setAlias('itg')
        newRow.itog = 'Подитог:'
        newRow.getCell('fix').colSpan = 2

        // Расчеты подитоговых значений
        def BigDecimal sumItg = 0, priceItg = 0, totalItg = 0
        for (row in dataRows) {

            def sum = row.sum
            def total = row.total
            def price = row.price

            sumItg += sum != null ? sum : 0
            priceItg += price != null ? price : 0
            totalItg += total != null ? total : 0
        }

        newRow.sum = sumItg
        newRow.price = priceItg
        newRow.total = totalItg

        dataRowHelper.insert(newRow, dataRows.size()+1)
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

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог')
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
    try{
        if (!checkTableHead(xml, 3)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
//        logicCheck()
    } catch(Exception e) {
        logger.error(""+e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 2) {
            continue
        }

        if ((row.cell.find{it.text()!=""}.toString())=="") {
            break
        }

        def newRow = formData.createDataRow()
        [
                'fullName',    // Полное наименование юридического лица с указанием ОПФ
                'docNumber',   // Номер договора
                'docDate',     // Дата договора
                'dealNumber',  // Номер сделки
                'dealDate',    // Дата сделки
                'sum',         // Сумма доходов Банка по данным бухгалтерского учета, руб.
                'dealDoneDate' // Дата совершения сделки
        ].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - 2

        // графа 2
        def val1 = refBookFactory.getDataProvider(9L).getRecords(
                new Date(),
                null,
                "NAME = '"+row.cell[indexCell].text()+"'",
                null)
        if (val1 != null && val1.size() == 1) {
            newRow.fullName = val1.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        }
        indexCell++

        // графа 3
        indexCell++

        // графа 4
        indexCell++
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
        newRow.sum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        indexCell++

        // графа 11
        indexCell++

        // графа 12
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 2)
    }
}


/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 11
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }

    def result = (
            xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[2].cell[0] == 'гр. 2' &&

            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] == 'гр. 3' &&

            xml.row[0].cell[2] == 'Страна регистрации' &&
            xml.row[2].cell[2] == 'гр. 4' &&

            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] == 'гр. 5' &&

            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] == 'гр. 6' &&

            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[2].cell[6] == 'гр. 7' &&

            xml.row[0].cell[7] == 'Дата сделки' &&
            xml.row[2].cell[7] == 'гр. 8' &&

            xml.row[0].cell[8] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[8] == 'гр. 9' &&

            xml.row[0].cell[9] == 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.' &&
            xml.row[2].cell[9] == 'гр. 10' &&

            xml.row[0].cell[10] == 'Итого стоимость без учета НДС, акцизов и пошлины, руб.' &&
            xml.row[2].cell[10] == 'гр. 11' &&

            xml.row[0].cell[11] == 'Дата совершения сделки' &&
            xml.row[2].cell[11] == 'гр. 12')
    return result
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
        throw new Exception("Строка ${indexRow+3} столбец ${indexCell+1} содержит недопустимый тип данных!")
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
        throw new Exception("Строка ${indexRow+3} столбец ${indexCell+1} содержит недопустимый тип данных!")
    }
}
