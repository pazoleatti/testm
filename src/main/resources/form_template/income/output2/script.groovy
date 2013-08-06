package form_template.income.output2

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * 6.3.2    Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 */

DataRowHelper getDataRowsHelper() {
    DataRowHelper dataRowsHelper = null
    if (formData.id != null) {
        dataRowsHelper = formDataService.getDataRowHelper(formData)
    }
    return dataRowsHelper
}

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        checkDecl()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.IMPORT :
        // importData()
        break
}

// графа 1  - title
// графа 2  - zipCode
// графа 3  - subdivisionRF
// графа 4  - area
// графа 5  - city
// графа 6  - region
// графа 7  - street
// графа 8  - homeNumber
// графа 9  - corpNumber
// графа 10 - apartment
// графа 11 - surname
// графа 12 - name
// графа 13 - patronymic
// графа 14 - phone
// графа 15 - dividendDate
// графа 16 - sumDividend
// графа 17 - sumTax

void deleteRow() {
    if (currentDataRow != null) {
        dataRowsHelper.getAllCached().remove(currentDataRow)
    }
}

void addRow() {
    dataRowsHelper.insert(getNewRow(), dataRowsHelper.getAllCached().size() + 1)
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 */
void checkUniq() {

    FormData findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

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

void logicCheck() {
    for (row in dataRowsHelper.getAllCached()) {

        for (alias in ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']) {
            if (row.getCell(alias).value == null) {
                logger.error('Поле ' + row.getCell(alias).column.name.replace('%', '') + ' не заполнено')
            }
        }

        String zipCode = (String) row.zipCode;
        if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
            logger.error('Неправильно указан почтовый индекс!')
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            // @todo Вызывать работу со справочником по новому
            if (!dictionaryRegionService.isValidCode((String) row.subdivisionRF)) {
                logger.error('Неверное наименование субъекта РФ!')
            }
        }
    }
}

///**
// * Получить строку по алиасу.
// *
// * @param data данные нф (helper)
// */
//DataRow<Cell> getRows(def data) {
//    return data.getAllCached();
//}

///**
// * Сохранить измененные значения нф.
// *
// * @param data данные нф (helper)
// */
//void save(def data) {
//    data.save(getRows(data))
//}

///**
// * Вставить новую строку в конец нф.
// *
// * @param data данные нф
// * @param row строка
// */
//void insert(def data, def row) {
//    data.insert(row, getRows(data).size() + 1)
//}
//
///**
// * Удалить строку из нф
// *
// * @param data данные нф (helper)
// * @param row строка для удаления
// */
//void deleteRow(def data, def row) {
//    data.delete(row)
//}

///**
// * Получить данные формы.
// *
// * @param form форма
// */
//def getData(FormData form) {
//    form
//    if (form != null && form.id != null) {
//        return formDataService.getDataRowHelper(form)
//    }
//    return null
//}

///**
// * Проверить наличие итоговой строки.
// *
// * @param data данные нф (helper)
// */
//def hasTotal(FormData data) {
//    for (DataRow row: getRows(data)) {
//        if (row.getAlias() == 'total') {
//            return true
//        }
//    }
//    return false
//}

///**
// * Получение импортируемых данных.
// */
//void importData() {
//    // TODO (Ramil Timerbaev) Костыль! это значение должно передаваться в скрипт
//    def fileName = 'fileName.xls'
//
//    def is = ImportInputStream
//    if (is == null) {
//        return
//    }
//
//    def xmlString = importService.getData(is, fileName, 'windows-1251', '№ стр.', null);
//    if (xmlString == null) {
//        return
//    }
//
//    def xml = new XmlSlurper().parseText(xmlString)
//    if (xml == null) {
//        return
//    }
//
//    // количество строк в шапке
//    def headRowCount = 4
//
//    // проверка заголовка таблицы
//    if (!checkTableHead(xml, headRowCount)) {
//        logger.error('Заголовок таблицы не соответствует требуемой структуре!')
//        return
//    }
//    // TODO (Ramil Timerbaev) Проверка корректности данных (типы данных)
//    // TODO (Ramil Timerbaev) Проверка корректности данных
//    // добавить данные в форму
//    addData(xml, headRowCount)
//}

///**
// * Заполнить форму данными.
// *
// * @param xml данные
// * @param headRowCount количество строк в шапке
// */
//void addData(def xml, headRowCount) {
//    if (xml == null) {
//        return
//    }
//    def data = getData(formData)
//
//    // количество графов в таблице
//    def columnCount = 12
//
//    def tmp
//    for (def row : xml.row) {
//        // пропустить шапку таблицы
//        if (indexRow < headRowCount) {
//            continue
//        }
//
//        // проверить по грн итоговая ли это строка
//        tmp = (row.cell[0] != null ? row.cell[0].text() : null)
//        if (tmp != null && tmp.contains('Итог')) {
//            continue
//        }
//        if (row.cell.size() == columnCount) {
//            // проверить по номеру договора повторяющиеся записи
//            tmp = (row.cell[1] != null ? row.cell[1].text() : null)
//            def newRow = getRowByTradeNumber(data, tmp)
//            if (newRow == null) {
//                newRow = getNewRow()
//                insert(data, newRow)
//            }
//
//            // графа 1
//            newRow.title = row.cell[2].text()
//
//            // графа 2
//            newRow.zipCode = row.cell[7].text()
//
//            // графа 3
//            newRow.subdivisionRF = row.cell[8].text()
//
//            // графа 4
//            newRow.area = row.cell[10].text()
//
//            // графа 5
//            newRow.city = row.cell[11].text()
//
//            // графа 6
//            newRow.region = row.cell[12].text()
//
//            // графа 7
//            newRow.street = row.cell[13].text()
//
//            // графа 8
//            newRow.homeNumber = row.cell[14].text()
//
//            // графа 9
//            newRow.corpNumber = row.cell[15].text()
//
//            // графа 10
//            newRow.apartment = row.cell[16].text()
//
//            // TODO (Ramil Timerbaev) разбиение ФИО
//
//            'данные не представлены'
//            // графа 11 - newRow.surname = row.cell[index].text()
//            index++
//            // графа 12 - newRow.name = row.cell[index].text()
//            index++
//            // графа 13 - newRow.patronymic = row.cell[index].text()
//
//            // графа 14
//            newRow.phone = row.cell[18].text()
//
//            // графа 15
//            newRow.dividendDate = row.cell[19].text()
//
//            // графа 16
//            newRow.sumDividend = getNumber(row.cell[20].text())
//
//            // графа 17
//            newRow.sumTax = getNumber(row.cell[22].text())
//        }
//    }
//    save(data)
//}
//
///**
// * Получить числовое значение.
// *
// * @param value строка
// */
//def getNumber(def value) {
//    if (value == null) {
//        return null
//    }
//    def tmp = value.trim()
//    if ("".equals(tmp)) {
//        return null
//    }
//    // поменять запятую на точку и убрать пробелы
//    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
//    return new BigDecimal(tmp)
//}

/**
 * Получить новую стролу с заданными стилями.
 */
DataRow<Cell> getNewRow() {
    DataRow<Cell> row = formData.createDataRow()

    for (alias in ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment',
            'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    return row
}

///**
// * Проверить шапку таблицы.
// *
// * @param xml данные
// * @param headRowCount количество строк в шапке
// */
//def checkTableHead(def xml, def headRowCount) {
//    def colCount = 21
//    // проверить количество строк и голонок в шапке
//    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
//        return false
//    }
//    def result = (xml.row[0].cell[0] == '№ стр.' &&
//            xml.row[0].cell[1] == 'Код территориального банка' &&
//            xml.row[0].cell[2] == 'Наименование территориального банка' &&
//            xml.row[0].cell[3] == 'Наименование получателя' &&
//            xml.row[0].cell[4] == 'ИНН' &&
//            xml.row[0].cell[5] == 'КПП' &&
//            xml.row[0].cell[6] == 'Юридический адрес ( место жительства )' &&
//            xml.row[0].cell[7] == 'Юридический адрес ( место нахождения )' &&
//            xml.row[1].cell[7] == 'Почтовый индекс' &&
//            xml.row[1].cell[8] == 'Субъект Российской Федерации' &&
//            xml.row[2].cell[8] == 'Код' &&
//            xml.row[2].cell[9] == 'Наименование субъекта РФ' &&
//            xml.row[1].cell[10] == 'Район' &&
//            xml.row[1].cell[11] == 'Город' &&
//            xml.row[1].cell[12] == 'Населенный пункт (село, поселок и т.п.)' &&
//            xml.row[1].cell[13] == 'Улица (проспект, переулок и т.д.)' &&
//            xml.row[1].cell[14] == 'Номер дома (владения)' &&
//            xml.row[1].cell[15] == 'Номер корпуса (строения)' &&
//            xml.row[1].cell[16] == 'Номер офиса (квартиры)' &&
//            xml.row[0].cell[17] == 'Руководитель организации (Ф.И.О.)' &&
//            xml.row[0].cell[18] == 'Контактный телефон' &&
//            xml.row[0].cell[19] == 'Дата перечисления дивидентов' &&
//            xml.row[0].cell[20] == 'Сумма дивидентов' &&
//            xml.row[0].cell[21] == 'Сумма налога')
//    return result
//}