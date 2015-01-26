package form_template.income.output_4.v2008

/**
 * Сведения о суммах налога на прибыль, уплаченного Банком за рубежом
 * formTemplate =
 *
 * графа 1 - rowNum
 * графа 2 - name
 * графа 3 - sum
 *
 * @author Bulat Kinzyabulatov
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.ADDITIONAL) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        logicCheck()
        break
}

@Field
def nonEmptyColumns = ['sum']

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null, 3, 4)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 3, 4)

    def tmpRow = formData.createDataRow()

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1]) : getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'sum'),
            (xml.row[1].cell[0]) : '1',
            (xml.row[1].cell[1]) : '2',
            (xml.row[1].cell[2]) : '3',
            (xml.row[2].cell[0]) : '1',
            (xml.row[2].cell[1]) : 'Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде',
            (xml.row[3].cell[0]) : '2',
            (xml.row[3].cell[1]) : 'Сумма налога с выплаченных дивидендов за пределами Российской Федерации в последнем квартале отчётного периода'
    ]

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 1)
}

void addData(def xml, headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    for (int i in [1, 2]) {
        def row = xml.row[headRowCount + i]
        def int xlsIndexRow = rowOffset + headRowCount + i

        dataRows[i - 1].setImportIndex(xlsIndexRow)

        xmlIndexCol = 1
        dataRows[i - 1].sum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
    }
    dataRowHelper.save(dataRows)
}

