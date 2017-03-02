package form_template.fond.primary_1151111.v2016

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import javax.script.ScriptException
import java.awt.Color
import java.text.SimpleDateFormat

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.commons.io.IOUtils

import com.aplana.sbrf.taxaccounting.model.formdata.*
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.PersonData
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVyplMk
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDop
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDopMt
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRasch
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRaschSum
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOmsRaschKol
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnmSum
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnmKol
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplSvPrev
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZakRash
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplPrichina
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt427
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvedPatent
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt425
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvInoGrazd
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif13422
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplatIt422
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvedObuch
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvReestrMdo
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import groovy.transform.Field
import groovy.transform.Memoized

import java.util.regex.Pattern

//----------------------------------------------------------------------------------------------------------------------
// Счетчик для проверки соответствия числа узлов
// ПерсСвСтрахЛиц
testCntNodePersSvStrahLic = 0
// УплПерОПС
testCntNodeUplPerOPS = 0
// УплПерОМС
testCntNodeUplPerOMS = 0
// УплПерОПСДоп
testCntNodeUplPerOPSDop = 0
// УплПерДСО
testCntNodeUplPerDso = 0
// УплПерОСС
testCntNodeUplPerOSS = 0
// ПревРасхОСС
testCntNodePrevRashOSS = 0
// РасчСВ_ОПС_ОМС
testCntNodeRaschSvOpsDms = 0
// РасчСВ_ОСС.ВНМ
testCntNodeRaschSvOSSVnm = 0
// РасхОССЗак
testCntNodeRaschOSSZak = 0
// ВыплФинФБ
testCntNodeVyplFinFB = 0
// ПравТариф3.1.427
testCntNodePravTarif31427 = 0
// ПравТариф5.1.427
testCntNodePravTarif51427 = 0
// ПравТариф7.1.427
testCntNodePravTarif71427 = 0
// СвПримТариф9.1.427
testCntNodePravTarif91427 = 0
// СвПримТариф2.2.425
testCntNodePravTarif22425 = 0
// СвПримТариф1.3.422
testCntNodePravTarif13422 = 0
// СведПатент
testCntNodeSvedPatent = 0
// СвИноГражд
testCntNodeSvInoGrazd = 0
// СведОбуч
testCntNodeSvObuch = 0
//----------------------------------------------------------------------------------------------------------------------

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        println "!IMPORT_TRANSPORT_FILE!"
        importData()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.CALCULATE:
        println "!CALCULATE!"
        calculateData()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break;
    case FormDataEvent.CHECK:
        println "!CHECK!"
        checkData()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        println "!PREPARE_SPECIFIC_REPORT!"
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        println "!CREATE_SPECIFIC_REPORT!"
        createSpecificReport()
        break
    case FormDataEvent.DELETE:
        println "!DELETE!"
        deleteData()
        break
    default:
        break
}

// Параметры подразделения по сборам, взносам
@Field final long REF_BOOK_FOND_ID = RefBook.Id.FOND.id

// Параметры подразделения по сборам, взносам (таблица)
@Field final long REF_BOOK_FOND_DETAIL_ID = RefBook.Id.FOND_DETAIL.id

// Коды ОКВЭД
@Field final long REF_BOOK_OKVED_ID = RefBook.Id.OKVED.id

// Коды форм реорганизации (ликвидации) организации
@Field final long REF_BOOK_REORGANIZATION_ID = RefBook.Id.REORGANIZATION.id

// Коды мест предоставления документа
@Field final long REF_BOOK_PRESENT_PLACE_ID = RefBook.Id.PRESENT_PLACE.id

// Общие параметры
@Field final long REF_BOOK_CONFIGURATION_PARAM_ID = RefBook.Id.CONFIGURATION_PARAM.id

// Коды ОКТМО
@Field final long REF_BOOK_OKTMO_ID = RefBook.Id.OKTMO.id

// Коды тарифов плательщиков
@Field final long REF_BOOK_TARIFF_PAYER_ID = RefBook.Id.TARIFF_PAYER.id

// Основания заполнения сумм страховых взносов
@Field final long REF_BOOK_FILL_BASE_ID = RefBook.Id.FILL_BASE.id

// Коды классов условий труда
@Field final long REF_BOOK_HARD_WORK_ID = RefBook.Id.HARD_WORK.id

// Коды категорий застрахованных лиц
@Field final long REF_BOOK_PERSON_CATEGORY_ID = RefBook.Id.PERSON_CATEGORY.id

// Документ, удостоверяющий личность
@Field def dulCache = [:]
@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// Виды документов, удостоверяющих личность
@Field final long REF_BOOK_DOCUMENT_CODES_ID = RefBook.Id.DOCUMENT_CODES.id

// Страны
@Field final long REF_BOOK_COUNTRY_ID = RefBook.Id.COUNTRY.id

// Физ. лица
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]
@Field Map<Long, Map<String, RefBookValue>> personsActualCache = [:]
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id

// Порог схожести при идентификации физлиц 0..1000, 1000 - совпадение по всем параметрам
@Field def SIMILARITY_THRESHOLD = 700;

@Field final REPORT_PERIOD_TYPE_ID = 8

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Обработка события CREATE_SPECIFIC_REPORT
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

// Названия листов в Excel шаблоне
@Field final String COMMON_SHEET = "Общее"
@Field final String PERSONAL_DATA = "Сведения о ФЛ"
@Field final String CONS_PERSONAL_DATA = "3.Персониф. Сведения"
@Field final String SUM_STRAH_VZN = "Суммы страховых взносов"
@Field final String OPS_OMS = "П1.Расчет ОПС ОМС"

// Имена псевдонима спецотчета
@Field final String PERSON_REPORT = "person_rep_param"
@Field final String CONSOLIDATED_REPORT = "consolidated_report"

@Field final Color ROWS_FILL_COLOR = new Color(255, 243, 203)
@Field final Color TOTAL_ROW_FILL_COLOR = new Color(186, 208, 80)

@Field final String PERSONAL_DATA_TOTAL_ROW_LABEL = "Всего за последние три месяца расчетного (отчетного) периода"

// TODO Серия/номер ДУЛ, ИНН, СНИЛС должны быть текстовыми ячейками. Иначе пропадают ведущие нули.
// TODO варнинг ДА-НЕТ при открытие файла
// TODO долго на 20к
def createSpecificReport() {
    def workbook = getSpecialReportTemplate()

    def row = scriptSpecificReportHolder.getSelectedRecord()
    def writer = scriptSpecificReportHolder.getFileOutputStream()
    def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()

    if (alias.equalsIgnoreCase(PERSON_REPORT)) {
        raschsvPersSvStrahLic = getrRaschsvPersSvStrahLic(row.id.longValue())
        fillGeneralList(workbook)
        fillPersSvSheet(workbook)
    } else if (alias.equalsIgnoreCase(CONSOLIDATED_REPORT)) {
        logger.info("Начало формирование")

        logger.info("Извелечение данных из БД")
        def raschsvObyazPlatSv = raschsvObyazPlatSvService.findObyazPlatSv(declarationData.id)
        logger.info("Данные из БД получены")

        logger.info("Заполнение листа \"Общие\"")
        fillGeneralList(workbook)

        logger.info("Заполнение листа \"3.Персониф. Сведения\"")
        fillPersSvConsSheet(workbook)

        logger.info("Заполнение листа \"Суммы страховых взносов\"")
        fillSumStrahVzn(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П1.Расчет ОПС ОМС\"")
        fillOpsOms(raschsvObyazPlatSv, workbook)

        logger.info("Отчет сформирован")
    }

    workbook.write(writer)
    writer.close()
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + "_" + new Date().format("yyyyMMdd_HHmmss") + ".xlsx")
}

// Персонифицированные данные о ФЛ
@Field def raschsvPersSvStrahLic = null

// Находит в базе данных RaschsvPersSvStrahLic
RaschsvPersSvStrahLic getrRaschsvPersSvStrahLic(id) {
    raschsvPersSvStrahLicService.get(id)
}

// Находит в базе данных список List RaschsvPersSvStrahLic
List<RaschsvPersSvStrahLic> getrRaschsvPersSvStrahLicList() {
    def declarationId = declarationData.getId()
    def params = scriptSpecificReportHolder.getSubreportParamValues()
    raschsvPersSvStrahLicService.findPersonBySubreportParams(declarationId, params)
}

def prepareSpecificReport() {
    PrepareSpecificReportResult result = new PrepareSpecificReportResult();
    List<Column> tableColumns = createTableColumns();
    List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    def rowColumns = createRowColumns()
    List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = getrRaschsvPersSvStrahLicList()

    def lastNameWidth = 4
    def firstNameWidth = 4
    def middleNameWidth = 4

    raschsvPersSvStrahLicList.each { RaschsvPersSvStrahLic entry ->
        DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null))
        row.getCell("id").setNumericValue(entry.id)
        row.col1 = entry.innfl
        row.col2 = entry.snils
        row.col3 = entry.familia
        lastnameWidth = setColumnWidth(tableColumns.get(2), entry.familia, lastNameWidth)
        row.col4 = entry.imya
        firstNameWidth = setColumnWidth(tableColumns.get(3), entry.imya, firstNameWidth)
        row.col5 = entry.otchestvo
        middleNameWidth = setColumnWidth(tableColumns.get(4), entry.otchestvo, middleNameWidth)
        row.col6 = entry.dataRozd.format("dd.MM.yyyy")
        row.col7 = entry.serNomDoc
        dataRows.add(row)
    }
    result.setTableColumns(tableColumns);
    result.setDataRows(dataRows);
    scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
}

/**
 * Устанавливает ширину столбца в зависиимости от содержимого
 * @param column столбец
 * @param columnValue содержимое столбца
 * @param curWidth текущая ширина столбца
 * @return
 */
def setColumnWidth(column, columnValue, curWidth) {
    def newWidth = curWidth
    if (curWidth < columnValue.length() / 2) {
        newWidth = columnValue.length() / 2
        column.setWidth(newWidth.intValue())
    }
    return newWidth
}

def createTableColumns() {
    List<Column> tableColumns = new ArrayList<Column>();

    Column column1 = new StringColumn();
    column1.setAlias("col1");
    column1.setName("ИНН");
    column1.setWidth(6)
    tableColumns.add(column1);

    Column column2 = new StringColumn();
    column2.setAlias("col2");
    column2.setName("СНИЛС");
    column2.setWidth(7)
    tableColumns.add(column2);

    Column column3 = new StringColumn();
    column3.setAlias("col3");
    column3.setName("Фамилия");
    column3.setWidth(5)
    tableColumns.add(column3);

    Column column4 = new StringColumn();
    column4.setAlias("col4");
    column4.setName("Имя");
    column4.setWidth(5)
    tableColumns.add(column4);

    Column column5 = new StringColumn();
    column5.setAlias("col5");
    column5.setName("Отчество");
    column5.setWidth(5)
    tableColumns.add(column5);

    Column column6 = new StringColumn()
    column6.setAlias("col6")
    column6.setName("Дата рождения")
    column6.setWidth(5)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("col7")
    column7.setName("№ ДУЛ")
    column7.setWidth(8)
    tableColumns.add(column7)

    return tableColumns;
}

def createRowColumns() {
    List<Column> tableColumns = new ArrayList<Column>();

    Column idColumn = new NumericColumn();
    idColumn.setAlias("id");
    idColumn.setName("id");
    idColumn.setWidth(5)
    tableColumns.add(idColumn);

    Column column1 = new StringColumn();
    column1.setAlias("col1");
    column1.setName("ИНН");
    column1.setWidth(6)
    tableColumns.add(column1);

    Column column2 = new StringColumn();
    column2.setAlias("col2");
    column2.setName("СНИЛС");
    column2.setWidth(7)
    tableColumns.add(column2);

    Column column3 = new StringColumn();
    column3.setAlias("col3");
    column3.setName("Фамилия");
    column3.setWidth(5)
    tableColumns.add(column3);

    Column column4 = new StringColumn();
    column4.setAlias("col4");
    column4.setName("Имя");
    column4.setWidth(5)
    tableColumns.add(column4);

    Column column5 = new StringColumn();
    column5.setAlias("col5");
    column5.setName("Отчество");
    column5.setWidth(5)
    tableColumns.add(column5);

    Column column6 = new StringColumn()
    column6.setAlias("col6")
    column6.setName("Дата рождения")
    column6.setWidth(5)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("col7")
    column7.setName("№ ДУЛ")
    column7.setWidth(13)
    tableColumns.add(column7)

    return tableColumns;
}
/****************************************************************************
 *  Блок заполнения данными титульной страницы                              *
 *                                                                          *
 * **************************************************************************/

/**
 * Заполнить титульный лист excel представления
 * @param workbook
 * @return
 */
def fillGeneralList(final XSSFWorkbook workbook) {
    // Получчить титульный лист из шаблона
    def sheet = workbook.getSheet(COMMON_SHEET)
    // Номер корректироки
    def nomCorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    // получить отчетный год
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def period = getProvider(REPORT_PERIOD_TYPE_ID).getRecordData(reportPeriod.dictTaxPeriodId)?.CODE?.value
    def reportYear = reportPeriod.taxPeriod.year
    // Код налогового органа
    def taxOrganCode = declarationData.taxOrganCode
    // Подписант
    def raschsvSvnpPodpisant = raschsvSvnpPodpisantService.findRaschsvSvnpPodpisant(declarationData.id)

    def departmentParam = getDepartmentParam(declarationData.departmentId)
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam?.id.value)
    println departmentParamIncomeRow
    //def presentPlaceReference = departmentParamIncomeRow?.PRESENT_PLACE?.value
    def poMestuParam = getRefPresentPlace().get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
    /*if (presentPlaceReference != null) {
        poMestuParam = getRefPresentPlace().get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)

    }*/
    // Место предоставления
    def poMestuCodeParam = poMestuParam?.get(RF_CODE)?.value

    // Сведения о файле
    sheet.getRow(3).getCell(1).setCellValue(declarationData.fileName)
    sheet.getRow(4).getCell(1).setCellValue(applicationVersion)
    sheet.getRow(5).getCell(1).setCellValue("5.01")

    // Сведения о документе
    // TODO уточнить получение значения
    sheet.getRow(8).getCell(1).setCellValue("1151111")
    sheet.getRow(9).getCell(1).setCellValue(new Date().format("dd.MM.yyyy"))
    sheet.getRow(10).getCell(1).setCellValue(nomCorr?.toString())
    sheet.getRow(11).getCell(1).setCellValue(period)
    sheet.getRow(12).getCell(1).setCellValue(reportYear?.toString())
    sheet.getRow(13).getCell(1).setCellValue(taxOrganCode)
    sheet.getRow(14).getCell(1).setCellValue(poMestuCodeParam)

    // Сведения о плательщике страховых взносов
    sheet.getRow(18).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpOkved)
    sheet.getRow(19).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpTlph)

    // Плательщик страховых взносов - организация
    sheet.getRow(21).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpNaimOrg)
    sheet.getRow(22).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpInnyl)
    sheet.getRow(23).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpKpp)

    // Сведения о реорганизованной (ликвидированной) организации
    sheet.getRow(25).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgForm)
    sheet.getRow(26).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgInnyl)
    sheet.getRow(27).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgKpp)

    // Сведения о лице, подписавшем документ
    sheet.getRow(30).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantPrPodp)
    def familia = raschsvSvnpPodpisant.familia != null ? raschsvSvnpPodpisant.familia + " ": ""
    def imya = raschsvSvnpPodpisant.imya != null ? raschsvSvnpPodpisant.imya + " " : ""
    def otchestvo = raschsvSvnpPodpisant.otchestvo != null ? raschsvSvnpPodpisant.otchestvo : ""
    sheet.getRow(31).getCell(1).setCellValue(familia + imya + otchestvo)

    // Сведения о представителе
    sheet.getRow(33).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantNaimDoc)
    sheet.getRow(34).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantNaimOrg)
}

/****************************************************************************
 *  Блок заполнения данными листа песонифицированных сведений ФЛ            *
 *                                                                          *
 * **************************************************************************/

/**
 * Заполняет данными лист персонифицированных сведений
 * @param workbook
 * @return
 */
def fillPersSvSheet(final XSSFWorkbook workbook) {
    def startIndex = 3;
    def pointer = startIndex
    fillRaschsvPersSvStrahLicTable(pointer, raschsvPersSvStrahLic, workbook, PERSONAL_DATA)
    pointer += 6
    pointer += fillRaschSvVyplat(pointer, raschsvPersSvStrahLic, workbook)
    pointer += 6
    pointer += fillRaschSvVyplatDop(pointer, raschsvPersSvStrahLic, workbook)
}

/**
 * Создает строку для таблицы Персонифицированные сведения о застрахованных лицах
 * @param startIndex
 * @param raschsvPersSvStrahLic
 * @param workbook
 * @param sheetName
 * @return
 */
def fillRaschsvPersSvStrahLicTable(
        final int startIndex,
        final RaschsvPersSvStrahLic raschsvPersSvStrahLic,
        final XSSFWorkbook workbook, final String sheetName) {
    def sheet = workbook.getSheet(sheetName)
    sheet.shiftRows(startIndex, sheet.getLastRowNum(), 2)

    def row = sheet.createRow(3)
    fillCellsOfRaschsvPersSvStrahLicRow(raschsvPersSvStrahLic, row)
}

/*
 * Заполняет данными строку из таблицы Персонифицированные сведения о
 * застрахованных лицах
 **/

def fillCellsOfRaschsvPersSvStrahLicRow(RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFRow row) {
    def leftStyle = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())
    def centerStyle = normalWithBorderStyleCenterAligned(row.getSheet().getWorkbook())
    def defaultStyle = normalWithBorderStyle(row.getSheet().getWorkbook())
    def cell0 = row.createCell(0)
    cell0.setCellStyle(leftStyle)
    cell0.setCellValue(raschsvPersSvStrahLic.getNomer())
    def cell1 = row.createCell(1)
    cell1.setCellStyle(leftStyle)
    cell1.setCellValue(raschsvPersSvStrahLic.getSvData()?.format("dd.MM.yyyy"))
    def cell2 = row.createCell(2)
    cell2.setCellStyle(centerStyle)
    cell2.setCellValue(raschsvPersSvStrahLic.getNomKorr())
    def cell3 = row.createCell(3)
    cell3.setCellStyle(centerStyle)
    cell3.setCellValue(raschsvPersSvStrahLic.getPeriod())
    def cell4 = row.createCell(4)
    cell4.setCellStyle(defaultStyle)
    cell4.setCellValue(raschsvPersSvStrahLic.getOtchetGod())
    def cell5 = row.createCell(5)
    cell5.setCellStyle(defaultStyle)
    cell5.setCellValue(raschsvPersSvStrahLic.getFamilia())
    def cell6 = row.createCell(6)
    cell6.setCellStyle(defaultStyle)
    cell6.setCellValue(raschsvPersSvStrahLic.getImya())
    def cell7 = row.createCell(7)
    cell7.setCellStyle(defaultStyle)
    cell7.setCellValue(raschsvPersSvStrahLic.getOtchestvo())
    def cell8 = row.createCell(8)
    cell8.setCellStyle(defaultStyle)
    cell8.setCellValue(raschsvPersSvStrahLic.getInnfl())
    def cell9 = row.createCell(9)
    cell9.setCellStyle(defaultStyle)
    cell9.setCellValue(raschsvPersSvStrahLic.getSnils())
    def cell10 = row.createCell(10)
    cell10.setCellStyle(defaultStyle)
    cell10.setCellValue(raschsvPersSvStrahLic.getDataRozd()?.format("dd.MM.yyyy"))
    def cell11 = row.createCell(11)
    cell11.setCellStyle(defaultStyle)
    cell11.setCellValue(raschsvPersSvStrahLic.getGrazd())
    def cell12 = row.createCell(12)
    cell12.setCellStyle(defaultStyle)
    cell12.setCellValue(raschsvPersSvStrahLic.getPol())
    def cell13 = row.createCell(13)
    cell13.setCellStyle(defaultStyle)
    cell13.setCellValue(raschsvPersSvStrahLic.getKodVidDoc())
    def cell14 = row.createCell(14)
    cell14.setCellStyle(defaultStyle)
    cell14.setCellValue(raschsvPersSvStrahLic.getSerNomDoc())
    def cell15 = row.createCell(15)
    cell15.setCellStyle(defaultStyle)
    cell15.setCellValue(raschsvPersSvStrahLic.getPrizOps())
    def cell16 = row.createCell(16)
    cell16.setCellStyle(defaultStyle)
    cell16.setCellValue(raschsvPersSvStrahLic.getPrizOms())
    def cell17 = row.createCell(17)
    cell17.setCellStyle(defaultStyle)
    cell17.setCellValue(raschsvPersSvStrahLic.getPrizOss())
}

/**
 * Создает строки для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
 * @param startIndex
 * @param raschsvPersSvStrahLic
 * @param workbook
 * @return
 */
int fillRaschSvVyplat(
        final int startIndex, final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFWorkbook workbook) {
    def raschsvSvVypl = raschsvPersSvStrahLic?.raschsvSvVypl
    def raschsvSvVyplMkList = raschsvSvVypl?.raschsvSvVyplMkList
    def raschsvSvVyplMkListSize = raschsvSvVyplMkList != null ? raschsvSvVyplMkList.size() : 0
    def sheet = workbook.getSheet(PERSONAL_DATA)
    sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvSvVyplMkListSize + 1)
    for (int i = 0; i < raschsvSvVyplMkListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatMt(raschsvPersSvStrahLic, raschsvSvVyplMkList[i], row)
    }
    fillCellsOfRaschSvVyplat(raschsvSvVypl, sheet.createRow(raschsvSvVyplMkListSize + startIndex))
    return raschsvSvVyplMkListSize
}

/**
 * Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
 * @param raschsvPersSvStrahLic
 * @param raschsvSvVyplMk
 * @param row
 * @return
 */
def fillCellsOfRaschSvVyplatMt(
        final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final RaschsvSvVyplMk raschsvSvVyplMk, final XSSFRow row) {
    def workbook = row.getSheet().getWorkbook()
    def styleCenter = normalWithBorderStyleCenterAligned(workbook)
    def styleRight = normalWithBorderStyleRightAligned(workbook)
    styleRight.setDataFormat(workbook.createDataFormat().getFormat("0.00"))

    addFillingToStyle(styleCenter, ROWS_FILL_COLOR)
    addFillingToStyle(styleRight, ROWS_FILL_COLOR)

    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleCenter)
    cell0.setCellValue(raschsvPersSvStrahLic.getNomer())
    def cell1 = row.createCell(1)
    cell1.setCellStyle(styleCenter)
    cell1.setCellValue(raschsvSvVyplMk.mesyac)
    def cell2 = row.createCell(2)
    cell2.setCellStyle(styleCenter)
    cell2.setCellValue(raschsvSvVyplMk.kodKatLic)
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleRight)
    cell3.setCellValue(raschsvSvVyplMk.sumVypl)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleRight)
    cell4.setCellValue(raschsvSvVyplMk.vyplOps)
    def cell5 = row.createCell(5)
    cell5.setCellStyle(styleRight)
    cell5.setCellValue(raschsvSvVyplMk.vyplOpsDog)
    def cell6 = row.createCell(6)
    cell6.setCellStyle(styleRight)
    cell6.setCellValue(raschsvSvVyplMk.nachislSv)
}

/**
 * Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
 * @param raschsvSvVypl
 * @param row
 * @return
 */
def fillCellsOfRaschSvVyplat(final RaschsvSvVypl raschsvSvVypl, final XSSFRow row) {
    def workbook = row.getSheet().getWorkbook()
    def styleRight = boldWithBorderStyleRightAligned(workbook)
    styleRight.setDataFormat(workbook.createDataFormat().getFormat("0.00"))
    addFillingToStyle(styleRight, TOTAL_ROW_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleRight)
    cell0.setCellValue(PERSONAL_DATA_TOTAL_ROW_LABEL)
    row.getSheet().addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 2));
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleRight)
    cell3.setCellValue(raschsvSvVypl?.sumVyplVs3)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleRight)
    cell4.setCellValue(raschsvSvVypl?.vyplOpsVs3)
    def cell5 = row.createCell(5)
    cell5.setCellStyle(styleRight)
    cell5.setCellValue(raschsvSvVypl?.vyplOpsDogVs3)
    def cell6 = row.createCell(6)
    cell6.setCellStyle(styleRight)
    cell6.setCellValue(raschsvSvVypl?.nachislSvVs3)
}

// Заполняет таблицу "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
int fillRaschSvVyplatDop(
        final int startIndex, final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFWorkbook workbook) {
    def raschsvSvVyplDop = raschsvPersSvStrahLic.raschsvVyplSvDop
    def raschsvSvVyplDopMtList = raschsvSvVyplDop?.raschsvVyplSvDopMtList
    def raschsvSvVyplDopMtListSize = raschsvSvVyplDopMtList != null ? raschsvSvVyplDopMtList?.size() : 0
    def sheet = workbook.getSheet(PERSONAL_DATA)
    for (int i = 0; i < raschsvSvVyplDopMtListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatDopMt(raschsvPersSvStrahLic, raschsvSvVyplDopMtList[i], row)
    }
    fillCellsOfRaschSvVyplatDop(raschsvSvVyplDop, sheet.createRow(raschsvSvVyplDopMtListSize + startIndex))
    return raschsvSvVyplDopMtListSize
}


/**
 * Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
 * @param raschsvPersSvStrahLic
 * @param raschsvSvVyplDopMt
 * @param row
 * @return
 */
def fillCellsOfRaschSvVyplatDopMt(
        final RaschsvPersSvStrahLic raschsvPersSvStrahLic,
        final RaschsvVyplSvDopMt raschsvSvVyplDopMt, final XSSFRow row) {
    def workbook = row.getSheet().getWorkbook()
    def styleCenter = normalWithBorderStyleCenterAligned(workbook)
    def styleRight = normalWithBorderStyleRightAligned(workbook)
    styleRight.setDataFormat(workbook.createDataFormat().getFormat("0.00"))
    addFillingToStyle(styleCenter, ROWS_FILL_COLOR)
    addFillingToStyle(styleRight, ROWS_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleCenter)
    cell0.setCellValue(raschsvPersSvStrahLic?.getNomer())
    def cell1 = row.createCell(1)
    cell1.setCellStyle(styleCenter)
    cell1.setCellValue(raschsvSvVyplDopMt?.mesyac)
    def cell2 = row.createCell(2)
    cell2.setCellStyle(styleCenter)
    cell2.setCellValue(raschsvSvVyplDopMt?.tarif)
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleRight)
    cell3.setCellValue(raschsvSvVyplDopMt?.vyplSv)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleRight)
    cell4.setCellValue(raschsvSvVyplDopMt?.nachislSv)
}

/**
 * Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
 * @param raschsvVyplSvDop
 * @param row
 * @return
 */
def fillCellsOfRaschSvVyplatDop(final RaschsvVyplSvDop raschsvVyplSvDop, final XSSFRow row) {
    def workbook = row.getSheet().getWorkbook()
    def styleRight = boldWithBorderStyleRightAligned(workbook)
    styleRight.setDataFormat(workbook.createDataFormat().getFormat("0.00"))
    addFillingToStyle(styleRight, TOTAL_ROW_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleRight)
    cell0.setCellValue(PERSONAL_DATA_TOTAL_ROW_LABEL)
    row.getSheet().addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 2));
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleRight)
    cell3.setCellValue(raschsvVyplSvDop?.vyplSvVs3)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleRight)
    cell4.setCellValue(raschsvVyplSvDop?.nachislSvVs3)
}

/******************************** СВОДНЫЙ ОТЧЕТ **************************************************/
/****************************************************************************
 *  Блок заполнения данными листа 3.Персониф. Сведения                      *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист 3.Персониф. Сведения
def fillPersSvConsSheet(XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(CONS_PERSONAL_DATA)
    def raschsvPersSvStrahLicList = raschsvPersSvStrahLicService.findPersons(declarationData.id)

    raschsvPersSvStrahLicList.each { raschsvPersSvStrahLic ->
        sheet.shiftRows(sheet.getLastRowNum() + 1, sheet.getLastRowNum() + 2, 1)
        fillCellsOfRaschsvPersSvStrahLicRow(raschsvPersSvStrahLic, sheet.createRow(sheet.getLastRowNum() + 1))
    }
}

/****************************************************************************
 *  Блок заполнения данными листа Суммы страховых взносов                   *
 *                                                                          *
 * **************************************************************************/

/**
 * Заполняет данными лист "Сумма страховых взносов"
 */
def fillSumStrahVzn(raschsvObyazPlatSv, XSSFWorkbook workbook) {
    def pointer = 2

    // ОКТМО
    fillRaschsvObyazPlatSv(pointer, raschsvObyazPlatSv, workbook)

    // Сумма страховых взносов на обязательное пенсионное страхование
    def raschsvUplPerOps = raschsvObyazPlatSv.raschsvUplPerList.findAll {NODE_NAME_UPL_PER_OPS == it.nodeName}
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOps, workbook)

    // Сумма страховых взносов на обязательное медицинское страхование
    def raschsvUplPerOms = raschsvObyazPlatSv.raschsvUplPerList.findAll {NODE_NAME_UPL_PER_OMS == it.nodeName}
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOms, workbook)

    // Сумма страховых взносов на обязательное пенсионное страхование по дополнительному тарифу
    def raschsvUplPerOpsDop = raschsvObyazPlatSv.raschsvUplPerList.findAll {NODE_NAME_UPL_PER_OPS_DOP == it.nodeName}
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOpsDop, workbook)

    // Сумма страховых взносов на дополнительное социальное обеспечение
    def raschsvUplPerOssDop = raschsvObyazPlatSv.raschsvUplPerList.findAll {NODE_NAME_UPL_PER_DSO == it.nodeName}
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOssDop, workbook)

    // Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности
    pointer += 5
    fillSumStrahVznNetrud(pointer, raschsvObyazPlatSv.raschsvUplPrevOss, workbook)
}

// Заполняет ОКТМО
def fillRaschsvObyazPlatSv(int startIndex, RaschsvObyazPlatSv raschsvObyazPlatSv, XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    def row = sheet.getRow(startIndex)
    row.createCell(1).setCellValue(raschsvObyazPlatSv.oktmo)
}

/**
 * Создает строки таблиц:
 * "Сумма страховых взносов на обязательное пенсионное страхование, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на обязательное медицинское страхование, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на обязательное пенсионное страхование по дополнительному тарифу, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на дополнительное социальное обеспечение, подлежащая уплате за расчетный (отчетный) период"
 */
int fillSumStrahVznOPSTable(int startIndex, List<RaschsvUplPer> raschsvUplPerList, XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    def raschsvUplPerListSize = raschsvUplPerList.size()

    sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvUplPerListSize + 1)

    for (int i = 0; i < raschsvUplPerListSize; i++) {
        fillCellsOfRaschsvUplPerRow(raschsvUplPerList[i], sheet.createRow(i + startIndex))
    }

    return raschsvUplPerListSize
}

/**
 * Заполняет строку таблиц:
 * "Сумма страховых взносов на обязательное пенсионное страхование, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на обязательное медицинское страхование, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на обязательное пенсионное страхование по дополнительному тарифу, подлежащая уплате за расчетный (отчетный) период",
 * "Сумма страховых взносов на дополнительное социальное обеспечение, подлежащая уплате за расчетный (отчетный) период"
 */
def fillCellsOfRaschsvUplPerRow(RaschsvUplPer raschsvUplPer, XSSFRow row) {
    def style = normalWithBorderStyle(row.getSheet().getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell0 = row.createCell(0)
    cell0.setCellStyle(style)
    cell0.setCellValue(raschsvUplPer.kbk)
    def cell1 = row.createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvUplPer.sumSbUplPer)
    def cell2 = row.createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvUplPer.sumSbUpl1m)
    def cell3 = row.createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvUplPer.sumSbUpl2m)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvUplPer.sumSbUpl3m)
}

/**
 * Создает строки таблицы:
 * "Сумма страховых взносов на обязательное социальное страхование
 * на случай временной нетрудоспособности и в связи с материнством,
 * подлежащая уплате за расчетный (отчетный) период / Сумма превышения произведенных плательщиком расходов"
 */
def fillSumStrahVznNetrud(int startIndex, RaschsvUplPrevOss raschsvUplPrevOss, XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    def row = sheet.createRow(startIndex)
    fillCellsOfRaschsvUplPrevOss(raschsvUplPrevOss, row)
}

/**
 * Заполняет стироку таблицы:
 * "Сумма страховых взносов на обязательное социальное страхование
 * на случай временной нетрудоспособности и в связи с материнством,
 * подлежащая уплате за расчетный (отчетный) период / Сумма превышения произведенных плательщиком расходов"
 */
def fillCellsOfRaschsvUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss, XSSFRow row) {
    def style = normalWithBorderStyle(row.getSheet().getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell0 = row.createCell(0)
    cell0.setCellStyle(style)
    cell0.setCellValue(raschsvUplPrevOss.kbk)
    def cell1 = row.createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvUplPrevOss.sumSbUplPer)
    def cell2 = row.createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvUplPrevOss.sumSbUpl1m)
    def cell3 = row.createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvUplPrevOss.sumSbUpl2m)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvUplPrevOss.sumSbUpl3m)
    def cell5 = row.createCell(5)
    cell5.setCellStyle(style)
    cell5.setCellValue(raschsvUplPrevOss.prevRashSvPer)
    def cell6 = row.createCell(6)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvUplPrevOss.prevRashSv1m)
    def cell7 = row.createCell(7)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvUplPrevOss.prevRashSv2m)
    def cell8 = row.createCell(8)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvUplPrevOss.prevRashSv3m)
}

/**
 * Заполняет данными лист "Расчет ОПС ОМС"
 */
def fillOpsOms(raschsvObyazPlatSv, XSSFWorkbook workbook) {
    raschsvObyazPlatSv.raschsvSvOpsOmsList.each { svOpsOms ->
        //TODO clone sheet
        def sheet = workbook.getSheet(OPS_OMS)

        def raschsvSvOpsOmsRasch = svOpsOms.raschsvSvOpsOmsRaschList.find {NODE_NAME_RASCH_SV_OPS == it.nodeName}
        def raschsvSvOpsOmsRaschKolOverall = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_STRAH_LIC_VS == it.nodeName}?.raschsvKolLicTip
        def raschsvSvOpsOmsRaschKolNach = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_LIC_NACH_SV_VS == it.nodeName}?.raschsvKolLicTip
        def raschsvSvOpsOmsRaschKolBas = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_PREV_BAZ_OPS == it.nodeName}?.raschsvKolLicTip

        fillOpsOmsKolRow(sheet, 14, raschsvSvOpsOmsRaschKolOverall)
        fillOpsOmsKolRow(sheet, 15, raschsvSvOpsOmsRaschKolNach)
        fillOpsOmsKolRow(sheet, 16, raschsvSvOpsOmsRaschKolBas)

        def raschsvSvOpsOmsRaschSumNachislFl= raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_VYPL_NACHISL_FL == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumOblozen = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NE_OBLOZEN_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumBazNachisl = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumBazPrevysh = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_PREVYSH_OPS == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumNachisl = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumNachislNePrev = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV_NE_PREV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsOmsRaschSumNachislPrev = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV_PREV == it.nodeName}?.raschsvSvSum1Tip

        fillOpsOmsSumRow(sheet, 21, raschsvSvOpsOmsRaschSumNachislFl)
        fillOpsOmsSumRow(sheet, 22, raschsvSvOpsOmsRaschSumOblozen)
        fillOpsOmsSumRow(sheet, 23, raschsvSvOpsOmsRaschSumBazNachisl)
        fillOpsOmsSumRow(sheet, 24, raschsvSvOpsOmsRaschSumBazPrevysh)
        fillOpsOmsSumRow(sheet, 25, raschsvSvOpsOmsRaschSumNachisl)
        fillOpsOmsSumRow(sheet, 26, raschsvSvOpsOmsRaschSumNachislNePrev)
        fillOpsOmsSumRow(sheet, 27, raschsvSvOpsOmsRaschSumNachislPrev)
    }
}

/**
 * Заполняет значениями строки для количеств
 */
def fillOpsOmsKolRow(sheet, pointer, kolLicTip) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(kolLicTip?.kolVsegoPer ?: "")

    def cell2 = sheet.getRow(pointer).createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(kolLicTip?.kolVsegoPosl3m ?: "")

    def cell3 = sheet.getRow(pointer).createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(kolLicTip?.kol1mPosl3m ?: "")

    def cell4 = sheet.getRow(pointer).createCell(4)
    cell4.setCellStyle(style)
    cell4.setCellValue(kolLicTip?.kol2mPosl3m ?: "")

    def cell5 = sheet.getRow(pointer).createCell(5)
    cell5.setCellStyle(style)
    cell5.setCellValue(kolLicTip?.kol3mPosl3m ?: "")
}

/**
 * Заполняет значениями строки для сумм
 */
def fillOpsOmsSumRow(sheet, pointer, sumLicTip) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(sumLicTip?.sumVsegoPer ?: "")

    def cell2 = sheet.getRow(pointer).createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(sumLicTip?.sumVsegoPosl3m ?: "")

    def cell3 = sheet.getRow(pointer).createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(sumLicTip?.sum1mPosl3m ?: "")

    def cell4 = sheet.getRow(pointer).createCell(4)
    cell4.setCellStyle(style)
    cell4.setCellValue(sumLicTip?.sum2mPosl3m ?: "")

    def cell5 = sheet.getRow(pointer).createCell(5)
    cell5.setCellStyle(style)
    cell5.setCellValue(sumLicTip?.sum3mPosl3m ?: "")
}


/****************************************************************************
 *  Блок стилизации                                                         *
 *                                                                          *
 *  Методы отвечающие за представление документа                            *
 *                                                                          *
 * **************************************************************************/

 /**
 * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем слева
 * @param workbook
 * @return
 */
def normalWithBorderStyleLeftAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_LEFT)
    thinBorderStyle(style)
    return style
}

/**
 * Создать стиль ячейки с полужирным шрифотм с тонкими границами и выравниванием справа
 * @param workbook
 * @return
 */
def boldWithBorderStyleRightAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_RIGHT)
    XSSFFont font = workbook.createFont()
    font.setBoldweight(Font.BOLDWEIGHT_BOLD)
    style.setFont(font)
    thinBorderStyle(style)
    return style
}

/**
 * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем справа
 * @param workbook
 * @return
 */
def normalWithBorderStyleRightAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_RIGHT)
    thinBorderStyle(style)
    return style
}

/**
 * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
 * @param workbook
 * @return
 */
def normalWithBorderStyleCenterAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_CENTER)
    thinBorderStyle(style)
    return style
}

/**
 * Создать стиль ячейки с нормальным шрифтом с тонкими границами
 * @param workbook
 * @return
 */
def normalWithBorderStyle(workbook) {
    def style = workbook.createCellStyle()
    thinBorderStyle(style)
    return style
}

/**
 * Добавляет к стилю ячейки тонкие границы
 * @param style
 * @return
 */
def thinBorderStyle(final style) {
    style.setBorderTop(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderRight(CellStyle.BORDER_THIN)
    return style
}

/**
 * Добавляет к стилю заливку
 * @param style
 * @param color
 * @return
 */
def addFillingToStyle(final XSSFCellStyle style, final Color color) {
    style.setFillForegroundColor(new XSSFColor(color))
    style.setFillBackgroundColor(new XSSFColor(color))
    style.setFillPattern(CellStyle.SOLID_FOREGROUND)
}
/****************************************************************************
 *
 *  Вспомогательные методы                                                  *
 *                                                                          *
 * **************************************************************************/

// Находит в базе данных шаблон спецотчета по физическому лицу и возвращает его
def getSpecialReportTemplate() {
    def blobData = blobDataServiceDaoImpl.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
    new XSSFWorkbook(blobData.getInputStream())
}


/****************************************************************************
 *  Тестовые данные                                                         *
 *                                                                          *
 * **************************************************************************/
class TestDataHolder {
    final static testDataHolder = new TestDataHolder()

    final PODPISANT
    final FL_DATA
    final RASCHSV_OOBYAZ_PLAT_SV
    final RASCHSV_UPL_PER
    final RASCHSV_UPL_PREV_OSS

    static getInstance() {
        return testDataHolder
    }

    private TestDataHolder() {
        // Инициализация RaschsvPersSvStrahLic
        FL_DATA = new RaschsvPersSvStrahLic()
        FL_DATA.nomer = 1
        FL_DATA.svData = new Date()
        FL_DATA.nomKorr = 0
        FL_DATA.period = "21"
        FL_DATA.otchetGod = "2016"
        FL_DATA.familia = "Иванов"
        FL_DATA.imya = "Егор"
        FL_DATA.otchestvo = "Семенович"
        FL_DATA.innfl = "111222333444"
        FL_DATA.snils = "123-456"
        FL_DATA.dataRozd = new Date(1970, Calendar.JANUARY, 1)
        FL_DATA.grazd = "Россия"
        FL_DATA.pol = "м"
        FL_DATA.kodVidDoc = "1"
        FL_DATA.serNomDoc = "1234 567890"
        FL_DATA.prizOps = "1"
        FL_DATA.prizOms = "1"
        FL_DATA.prizOss = "1"


        final VYPL = new RaschsvSvVypl()
        VYPL.setSumVyplVs3(1000)
        VYPL.setVyplOpsVs3(300)
        VYPL.setVyplOpsDogVs3(300)
        VYPL.setNachislSvVs3(400)

        final VYPL_MT1 = new RaschsvSvVyplMk()
        VYPL_MT1.setMesyac("Январь")
        VYPL_MT1.setKodKatLic("1")
        VYPL_MT1.setSumVypl(300)
        VYPL_MT1.setVyplOps(100)
        VYPL_MT1.setVyplOpsDog(100)
        VYPL_MT1.setNachislSv(150)

        final VYPL_MT2 = new RaschsvSvVyplMk()
        VYPL_MT2.setMesyac("Февраль")
        VYPL_MT2.setKodKatLic("1")
        VYPL_MT2.setSumVypl(300)
        VYPL_MT2.setVyplOps(100)
        VYPL_MT2.setVyplOpsDog(100)
        VYPL_MT2.setNachislSv(100)

        final VYPL_MT3 = new RaschsvSvVyplMk()
        VYPL_MT3.setMesyac("Март")
        VYPL_MT3.setKodKatLic("1")
        VYPL_MT3.setSumVypl(400)
        VYPL_MT3.setVyplOps(100)
        VYPL_MT3.setVyplOpsDog(100)
        VYPL_MT3.setNachislSv(150)
        VYPL.raschsvSvVyplMkList = [VYPL_MT1, VYPL_MT2, VYPL_MT3]
        FL_DATA.raschsvSvVypl = VYPL

        final VYPL_DOP = new RaschsvVyplSvDop()
        VYPL_DOP.nachislSvVs3 = 500
        VYPL_DOP.vyplSvVs3 = 500
        final VYPL_DOP_MT1 = new RaschsvSvVyplMk()
        VYPL_DOP_MT1.mesyac = "Январь"
//        VYPL_DOP_MT1.tarif = "abc"
        VYPL_DOP_MT1.nachislSv = 200
        VYPL_DOP_MT1.sumVypl = 200
        final VYPL_DOP_MT2 = new RaschsvSvVyplMk()
        VYPL_DOP_MT2.mesyac = "Февраль"
//        VYPL_DOP_MT2.tarif = "xyz"
        VYPL_DOP_MT2.nachislSv = 100
        VYPL_DOP_MT2.sumVypl = 100
        final VYPL_DOP_MT3 = new RaschsvSvVyplMk()
        VYPL_DOP_MT3.mesyac = "Март"
//        VYPL_DOP_MT3.tarif = "abc"
        VYPL_DOP_MT3.nachislSv = 200
        VYPL_DOP_MT3.sumVypl = 200
        VYPL_DOP.raschsvVyplSvDopMtList = [VYPL_DOP_MT1, VYPL_DOP_MT2, VYPL_DOP_MT3]
        FL_DATA.raschsvVyplSvDop = VYPL_DOP

        // Инициализация RaschsvSvnpPodpisant
        PODPISANT = new RaschsvSvnpPodpisant()
        PODPISANT.setSvnpOkved("okved_test")
        PODPISANT.setSvnpTlph("phone_test")
        PODPISANT.setSvnpNaimOrg("nazvanie_test")
        PODPISANT.setSvnpInnyl("innYl_test")
        PODPISANT.setSvnpKpp("kpp_test")
        PODPISANT.setSvnpSvReorgForm("reorgForm_test")
        PODPISANT.setSvnpSvReorgInnyl("reorgInn_test")
        PODPISANT.setSvnpSvReorgKpp("reorgKpp_test")
        PODPISANT.setFamilia("familia_test")
        PODPISANT.setImya("imya_test")
        PODPISANT.setOtchestvo("otchestvo_test")
        PODPISANT.setPodpisantPrPodp("pravoPodpis_test")
        PODPISANT.setPodpisantNaimDoc("docName_test")
        PODPISANT.setPodpisantNaimOrg("orgName_test")

        // Инициализация RaschsvObyazPlatSv
        RASCHSV_OOBYAZ_PLAT_SV = new RaschsvObyazPlatSv()
        RASCHSV_OOBYAZ_PLAT_SV.oktmo = "oktmo_test"

        // Инициализация RaschsvUplPer
        RASCHSV_UPL_PER = new RaschsvUplPer()
        RASCHSV_UPL_PER.kbk = "kbk_test"
        RASCHSV_UPL_PER.sumSbUplPer = 1.00
        RASCHSV_UPL_PER.sumSbUpl1m = 2.00
        RASCHSV_UPL_PER.sumSbUpl2m = 3.00
        RASCHSV_UPL_PER.sumSbUpl3m = 4.00

        // Инициализация RaschsvUplPrevOss
        RASCHSV_UPL_PREV_OSS = new RaschsvUplPrevOss()
        RASCHSV_UPL_PREV_OSS.kbk = "kbk_test"
        RASCHSV_UPL_PREV_OSS.sumSbUplPer = 1
        RASCHSV_UPL_PREV_OSS.sumSbUpl1m = 2
        RASCHSV_UPL_PREV_OSS.sumSbUpl2m = 3
        RASCHSV_UPL_PREV_OSS.sumSbUpl3m = 4
        RASCHSV_UPL_PREV_OSS.prevRashSvPer = 5
        RASCHSV_UPL_PREV_OSS.prevRashSv1m = 6
        RASCHSV_UPL_PREV_OSS.prevRashSv2m = 7
        RASCHSV_UPL_PREV_OSS.prevRashSv3m = 8
    }
}
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Обработка события IMPORT_TRANSPORT_FILE
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

@Field final PATTERN_DATE_FORMAT = "dd.mm.yyyy"
@Field final ERROR_MESSAGE_NOT_MATCH_NODE_COUNT = "Не соответствие числа узлов для «%s»"

// Ограничение на число объектов в коллекциях
@Field final Integer MAX_COUNT_PERV_SV_STRAH_LIC = 1000
@Field final MAX_COUNT_UPL_PER = 1000
@Field final MAX_COUNT_SV_OPS_OMS = 1000

// Узлы
@Field final NODE_NAME_FILE = "Файл"
@Field final NODE_NAME_DOCUMENT = "Документ"

@Field final NODE_NAME_SV_NP = "СвНП"
@Field final NODE_NAME_NPYL = "НПЮЛ"
@Field final NODE_NAME_NPIP = "НПИП"
@Field final NODE_NAME_NPFL = "НПФЛ"
@Field final NODE_NAME_NPFL_INNFL = "ИННФЛ"
@Field final NODE_NAME_NPFL_SVNPFL = "СвНПФЛ"
@Field final NODE_NAME_NPFL_SVNPFL_ADDRMJRF = "АдрМЖРФ"
@Field final NODE_NAME_NPFL_SVNPFL_UDLICHFL = "УдЛичнФЛ"
@Field final NODE_NAME_SV_REORG_YL = "СвРеоргЮЛ"
@Field final NODE_NAME_PODPISANT = "Подписант"
@Field final NODE_NAME_SV_PRED = "СвПред"

@Field final NODE_NAME_RASCHET_SV = "РасчетСВ"
@Field final NODE_NAME_OBYAZ_PLAT_SV = "ОбязПлатСВ"
@Field final NODE_NAME_PERS_SV_STRAH_LIC = "ПерсСвСтрахЛиц"
@Field final NODE_NAME_DAN_FL_POLUCH = "ДанФЛПолуч"
@Field final NODE_NAME_FIO = "ФИО"
@Field final NODE_NAME_SV_VYPL_SVOPS = "СвВыплСВОПС"
@Field final NODE_NAME_SV_VYPL = "СвВыпл"
@Field final NODE_NAME_SV_VYPL_MK = "СвВыплМК"
@Field final NODE_NAME_VYPL_SV_DOP = "ВыплСВДоп"
@Field final NODE_NAME_VYPL_SV_DOP_MT = "ВыплСВДопМТ"
@Field final NODE_NAME_UPL_PER_OPS = "УплПерОПС"
@Field final NODE_NAME_UPL_PER_OMS = "УплПерОМС"
@Field final NODE_NAME_UPL_PER_OPS_DOP = "УплПерОПСДоп"
@Field final NODE_NAME_UPL_PER_DSO = "УплПерДСО"
@Field final NODE_NAME_UPL_PREV_OSS = "УплПревОСС"
@Field final NODE_NAME_UPL_PER_OSS = "УплПерОСС"
@Field final NODE_NAME_PREV_RASH_OSS = "ПревРасхОСС"

@Field final NODE_NAME_RASCH_SV_OPS_OMS = "РасчСВ_ОПС_ОМС"
@Field final NODE_NAME_RASCH_SV_OPS = "РасчСВ_ОПС"
@Field final NODE_NAME_RASCH_SV_OMS = "РасчСВ_ОМС"
@Field final NODE_NAME_RASCH_SV_OPS428 = "РасчСВ_ОПС428"
@Field final NODE_NAME_RASCH_SV_428_12 = "РасчСВ_428.1-2"
@Field final NODE_NAME_RASCH_SV_428_3 = "РасчСВ_428.3"
@Field final NODE_NAME_RASCH_SV_DSO = "РасчСВ_ДСО"
@Field final NODE_NAME_KOL_STRAH_LIC_VS = "КолСтрахЛицВс"
@Field final NODE_NAME_KOL_LIC_NACH_SV_VS = "КолЛицНачСВВс"
@Field final NODE_NAME_PREV_BAZ_OPS = "ПревБазОПС"
@Field final NODE_NAME_VYPL_NACHISL_FL = "ВыплНачислФЛ"
@Field final NODE_NAME_NE_OBLOZEN_SV = "НеОбложенСВ"
@Field final NODE_NAME_BAZ_NACHISL_SV = "БазНачислСВ"
@Field final NODE_NAME_BAZ_PREVYSH_OPS = "БазПревышОПС"
@Field final NODE_NAME_NACHISL_SV = "НачислСВ"
@Field final NODE_NAME_NACHISL_SV_NE_PREV = "НачислСВНеПрев"
@Field final NODE_NAME_NACHISL_SV_PREV = "НачислСВПрев"
@Field final NODE_NAME_BAZ_NACHISL_SV_DOP = "БазНачислСВДоп"
@Field final NODE_NAME_NACHISL_SV_DOP = "НачислСВДоп"
@Field final NODE_NAME_KOL_LIC_NACH_SV = "КолЛицНачСВ"
@Field final NODE_NAME_BAZ_NACHISL_SVDSO = "БазНачислСВДСО"
@Field final NODE_NAME_NACHISL_SVDSO = "НачислСВДСО"

@Field final NODE_NAME_RASCH_SV_OSS_VNM = "РасчСВ_ОСС.ВНМ"
@Field final NODE_NAME_UPL_SV_PREV = "УплСВПрев"

@Field final NODE_NAME_RASH_OSS_ZAK = "РасхОССЗак"

@Field final NODE_NAME_VYPL_FIN_FB = "ВыплФинФБ"

@Field final NODE_NAME_PRAV_TARIF3_1_427 = "ПравТариф3.1.427"

@Field final NODE_NAME_PRAV_TARIF5_1_427 = "ПравТариф5.1.427"

@Field final NODE_NAME_PRAV_TARIF7_1_427 = "ПравТариф7.1.427"

@Field final NODE_NAME_SV_PRIM_TARIF9_1_427 = "СвПримТариф9.1.427"
@Field final NODE_NAME_VYPLAT_IT = "ВыплатИт"
@Field final NODE_NAME_SVED_PATENT = "СведПатент"

@Field final NODE_NAME_SV_PRIM_TARIF2_2_425 = "СвПримТариф2.2.425"
@Field final NODE_NAME_SV_INO_GRAZD = "СвИноГражд"
@Field final NODE_NAME_SUM_VYPLAT = "СумВыплат"

@Field final NODE_NAME_SV_PRIM_TARIF1_3_422 = "СвПримТариф1.3.422"
@Field final NODE_NAME_SVED_OBUCH = "СведОбуч"
@Field final NODE_NAME_SV_REESTR_MDO = "СвРеестрМДО"
@Field final NODE_NAME_SPRAV_STUD_OTRYAD = "СправСтудОтряд"
@Field final NODE_NAME_SPRAV_FORM_OBUCH = "СправФормОбуч"

// Атрибуты узла Документ
@Field final DOCUMENT_PO_MESTU = "ПоМесту"

// Атрибуты узла СвНП
@Field final SV_NP_OKVED = "ОКВЭД"
@Field final SV_NP_TLPH = "Тлф"

// Атрибуты узла НПЮЛ
@Field final NPYL_NAIM_ORG = "НаимОрг"
@Field final NPYL_INNYL = "ИННЮЛ"
@Field final NPYL_KPP = "КПП"

// Атрибуты узла СвРеоргЮЛ
@Field final SV_REORG_YL_FORM_REORG = "ФормРеорг"
@Field final SV_REORG_YL_INNYL = "ИННЮЛ"
@Field final SV_REORG_YL_KPP = "КПП"

// Атрибуты узла Подписант
@Field final PODPISANT_PR_PODP = "ПрПодп"

// Атрибуты узла СвПред
@Field final SV_PRED_NAIM_DOC = "НаимДок"
@Field final SV_PRED_NAIM_ORG = "НаимОрг"

// Атрибуты узла ПерсСвСтрахЛиц
@Field final PERV_SV_STRAH_LIC_NOM_KORR = 'НомКорр'
@Field final PERV_SV_STRAH_LIC_PERIOD = "Период"
@Field final PERV_SV_STRAH_LIC_OTCHET_GOD = "ОтчетГод"
@Field final PERV_SV_STRAH_LIC_NOMER = "Номер"
@Field final PERV_SV_STRAH_LIC_SV_DATA = "Дата"

// Атрибуты узла ДанФЛПолуч
@Field final DAN_FL_POLUCH_INNFL = 'ИННФЛ'
@Field final DAN_FL_POLUCH_SNILS = 'СНИЛС'
@Field final DAN_FL_POLUCH_DATA_ROZD = 'ДатаРожд'
@Field final DAN_FL_POLUCH_GRAZD = 'Гражд'
@Field final DAN_FL_POLUCH_POL = 'Пол'
@Field final DAN_FL_POLUCH_KOD_VID_DOC = 'КодВидДок'
@Field final DAN_FL_POLUCH_SER_NOM_DOC = 'СерНомДок'
@Field final DAN_FL_POLUCH_PRIZ_OPS = 'ПризОПС'
@Field final DAN_FL_POLUCH_PRIZ_OMS = 'ПризОМС'
@Field final DAN_FL_POLUCH_PRIZ_OSS = 'ПризОСС'

// Атрибуты узла ФИО
@Field final FIO_FAMILIA = 'Фамилия'
@Field final FIO_IMYA = 'Имя'
@Field final FIO_OTCHESTVO_NAME = 'Отчество'

// Атрибуты узла СвВыпл
@Field final SV_VYPL_SUM_VYPL_VS3 = "СумВыплВс3"
@Field final SV_VYPL_VYPL_OPS_VS3 = "ВыплОПСВс3"
@Field final SV_VYPL_VYPL_OPS_DOG_VS3 = "ВыплОПСДогВс3"
@Field final SV_VYPL_NACHISL_SV_VS3 = "НачислСВВс3"

// Атрибуты узла СвВыплМК
@Field final SV_VYPL_MT_MESYAC = "Месяц"
@Field final SV_VYPL_MT_KOD_KAT_LIC = "КодКатЛиц"
@Field final SV_VYPL_MT_SUM_VYPL = "СумВыпл"
@Field final SV_VYPL_MT_VYPL_OPS = "ВыплОПС"
@Field final SV_VYPL_MT_VYPL_OPS_DOG = "ВыплОПСДог"
@Field final SV_VYPL_MT_NACHISL_SV = "НачислСВ"

// Атрибуты узла ВыплСВДоп
@Field final VYPL_SV_DOP_VYPL_SV_VS3 = "ВыплСВВс3"
@Field final VYPL_SV_DOP_NACHISL_SV_VS3 = "НачислСВВс3"

// Атрибуты узла ВыплСВДопМТ
@Field final VYPL_SV_DOP_MT_MESYAC = "Месяц"
@Field final VYPL_SV_DOP_MT_TARIF = "Тариф"
@Field final VYPL_SV_DOP_MT_VYPL_SV = "ВыплСВ"
@Field final VYPL_SV_DOP_MT_NACHISL_SV = "НачислСВ"

// Атрибуты узлов УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО, УплПерОСС
@Field final UPL_PER_KBK = "КБК"
@Field final UPL_PER_SUM_SV_UPL_PER = "СумСВУплПер"
@Field final UPL_PER_SUM_SV_UPL_1M = "СумСВУпл1М"
@Field final UPL_PER_SUM_SV_UPL_2M = "СумСВУпл2М"
@Field final UPL_PER_SUM_SV_UPL_3M = "СумСВУпл3М"

// Атрибуты узла ПревРасхОСС
@Field final PREV_RASH_KBK = "КБК"
@Field final PREV_RASH_PREV_RASH_SV_PER = "ПревРасхСВПер"
@Field final PREV_RASH_PREV_RASH_SV_1M = "ПревРасхСВ1М"
@Field final PREV_RASH_PREV_RASH_SV_2M = "ПревРасхСВ2М"
@Field final PREV_RASH_PREV_RASH_SV_3M = "ПревРасхСВ3М"

// Атрибуты узла ОбязПлатСВ
@Field final OBYAZ_PLAT_SV_OKTMO = "ОКТМО"

// Атрибуты узла РасчСВ_ОПС_ОМС
@Field final RASCH_SV_OPS_OMS_TARIF_PLAT = "ТарифПлат"
@Field final RASCH_SV_OPS428_12_PR_OSN_SV_DOP = "ПрОснСВДоп"
@Field final RASCH_SV_OPS428_3_KOD_OSNOV = "КодОснов"
@Field final RASCH_SV_OPS428_3_OSNOV_ZAP = "ОсновЗап"
@Field final RASCH_SV_OPS428_3_KLAS_USL_TRUD = "КласУслТруд"
@Field final RASCH_SV_DSO_PR_RASCH_SUM = "ПрРасчСум"

// Атрибуты узла РасчСВ_ОСС.ВНМ
@Field final RASCH_SV_OSS_VNM_PRIZ_VYPL = "ПризВыпл"

// Атрибуты узла ВыплФинФБ
@Field final VYPL_FIN_FB_SV_VNF_UHOD_INV = "СВВнФУходИнв"

// Атрибуты узла ПравТариф3.1.427
@Field final PRAV_TARIF3_1_427_SR_CHISL_9MPR = "СрЧисл_9МПр"
@Field final PRAV_TARIF3_1_427_SR_CHISL_PER = "СрЧисл_Пер"
@Field final PRAV_TARIF3_1_427_DOH248_9MPR = "Дох248_9МПр"
@Field final PRAV_TARIF3_1_427_DOH248_PER = "Дох248_Пер"
@Field final PRAV_TARIF3_1_427_DOH_KR5_427_9MPR = "ДохКр5.427_9МПр"
@Field final PRAV_TARIF3_1_427_DOH_KR5_427_PER = "ДохКр5.427_Пер"
@Field final PRAV_TARIF3_1_427_DOH_DOH5_427_9MPR = "ДолДох5.427_9МПр"
@Field final PRAV_TARIF3_1_427_DOH_DOH5_427_PER = "ДолДох5.427_Пер"

// Атрибуты узла ПравТариф5.1.427
@Field final PRAV_TARIF5_1_427_DOH346_15VS = "Дох346.15Вс"
@Field final PRAV_TARIF5_1_427_DOH6_427 = "Дох6.427"
@Field final PRAV_TARIF5_1_427_DOL_DOH6_427 = "ДолДох6.427"

// Атрибуты узла ПравТариф7.1.427
@Field final PRAV_TARIF7_1_427_DOH_VS_PRED = "ДохВсПред"
@Field final PRAV_TARIF7_1_427_DOH_VS_PER = "ДохВсПер"
@Field final PRAV_TARIF7_1_427_DOH_CEL_POST_PRED = "ДохЦелПостПред"
@Field final PRAV_TARIF7_1_427_DOH_CEL_POST_PER = "ДохЦелПостПер"
@Field final PRAV_TARIF7_1_427_DOH_GRANT_PRED = "ДохГрантПред"
@Field final PRAV_TARIF7_1_427_DOH_GRANT_PER = "ДохГрантПер"
@Field final PRAV_TARIF7_1_427_DOH_EK_DEYAT_PRED = "ДохЭкДеятПред"
@Field final PRAV_TARIF7_1_427_DOH_EK_DEYAT_PER = "ДохЭкДеятПер"
@Field final PRAV_TARIF7_1_427_DOL_DOH_PRED = "ДолДохПред"
@Field final PRAV_TARIF7_1_427_DOL_DOH_PER = "ДолДохПер"

// Атрибуты узла СведПатент
@Field final SVED_PATENT_NOM_PATENT = "НомПатент"
@Field final SVED_PATENT_VYD_DEYAT_PATENT = "ВидДеятПатент"
@Field final SVED_PATENT_DATA_NACH_DEYST = "ДатаНачДейст"
@Field final SVED_PATENT_DATA_KON_DEYST = "ДатаКонДейст"

// Атрибуты узла СвРеестрАкОрг
@Field final SV_REESTR_AK_ORG_DATA = "ДатаЗапАкОрг"
@Field final SV_REESTR_AK_ORG_NOM = "НомЗапАкОрг"

// Атрибуты типа КолЛицТип
@Field final KOL_LIC_TIP_KOL_VSEGO_PER = "КолВсегоПер"
@Field final KOL_LIC_TIP_KOL_VSEGO_POSL3M = "КолВсегоПосл3М"
@Field final KOL_LIC_TIP_KOL1_POSL3M = "Кол1Посл3М"
@Field final KOL_LIC_TIP_KOL2_POSL3M = "Кол2Посл3М"
@Field final KOL_LIC_TIP_KOL3_POSL3M = "Кол3Посл3М"

// Атрибуты типа СвСум1Тип
@Field final SV_SUM_1TIP_SUM_VSEGO_PER = "СумВсегоПер"
@Field final SV_SUM_1TIP_SUM_VSEGO_POSL3M = "СумВсегоПосл3М"
@Field final SV_SUM_1TIP_SUM1_POSL3M = "Сум1Посл3М"
@Field final SV_SUM_1TIP_SUM2_POSL3M = "Сум2Посл3М"
@Field final SV_SUM_1TIP_SUM3_POSL3M = "Сум3Посл3М"

// Атрибуты типа УплСВПревТип
@Field final UPL_SV_PREV_PRIZNAK = "Признак"
@Field final UPL_SV_PREV_SUMMA = "Сумма"

// Атрибуты типа РасхОССТип
@Field final RASH_OSS_TIP_CHISL_SLUCH = "ЧислСлуч"
@Field final RASH_OSS_TIP_KOL_VYPL = "КолВыпл"
@Field final RASH_OSS_TIP_RASH_VSEGO = "РасхВсего"
@Field final RASH_OSS_TIP_RASH_FIN_FB = "РасхФинФБ"

// Атрибуты типа РасхВыплТип
@Field final RASH_VYPL_TIP_CHISL_POLUCH = "ЧислПолуч"
@Field final RASH_VYPL_TIP_KOL_VYPL = "КолВыпл"
@Field final RASH_VYPL_TIP_RASHOD = "Расход"

// Атрибуты узла СвИноГражд
@Field final SV_INO_GRAZD_INNFL = "ИННФЛ"
@Field final SV_INO_GRAZD_SNILS = "СНИЛС"
@Field final SV_INO_GRAZD_GRAZD = "Гражд"

// Атрибуты узла СведОбуч
@Field final SVED_OBUCH_UNIK_NOMER = "УникНомер"

// Атрибуты узла СвРеестрМДО
@Field final SV_REESTR_MDO_NAIM_MDO = "НаимМДО"
@Field final SV_REESTR_MDO_DATA_ZAPIS = "ДатаЗапис"
@Field final SV_REESTR_MDO_NOMER_ZAPIS = "НомерЗапис"

// Атрибуты узлов СправСтудОтряд и СправФормОбуч
@Field final SPRAV_NOMER = "Номер"
@Field final SPRAV_DATA = "Дата"

/**
 * Разбор xml-файлов
 */
void importData() {

    // Валидация по схеме
    declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile)

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def fileNode = new XmlSlurper().parse(ImportInputStream);
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    // Запуск проверок, которые проводились при загрузке
    checkImportRaschsv(fileNode, UploadFileName)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    // Набор объектов ПерсСвСтрахЛиц
    def raschsvPersSvStrahLicList = []

    // Идентификатор декларации для которой загружаются данные
    declarationDataId = declarationData.getId()

    // Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ
    RaschsvSvnpPodpisant raschsvSvnpPodpisant = new RaschsvSvnpPodpisant()
    raschsvSvnpPodpisant.declarationDataId = declarationDataId

    fileNode.childNodes().each { documentNode ->
        raschsvSvnpPodpisant.svnpTlph = documentNode.name
        if (documentNode.name == NODE_NAME_DOCUMENT) {
            documentNode.childNodes().each { raschetSvNode ->
                if (raschetSvNode.name == NODE_NAME_RASCHET_SV) {
                    // Разбор узла РасчетСВ
                    raschetSvNode.childNodes().each { raschetSvChildNode ->
                        if (raschetSvChildNode.name == NODE_NAME_OBYAZ_PLAT_SV) {
                            // Разбор узла ОбязПлатСВ
                            parseRaschsvObyazPlatSv(raschetSvChildNode, declarationDataId)
                        } else if (raschetSvChildNode.name == NODE_NAME_PERS_SV_STRAH_LIC) {
                            // Разбор узла ПерсСвСтрахЛиц
                            if (raschsvPersSvStrahLicList.size() >= MAX_COUNT_PERV_SV_STRAH_LIC) {
                                // При добавлении (обновлении) записей в справочнике Физические лица, в объект ПерсСвСтрахЛиц будет добавлена ссылка на запись в справочнике Физические лица
                                raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
                                raschsvPersSvStrahLicList = []
                            }
                            raschsvPersSvStrahLicList.add(parseRaschsvPersSvStrahLic(raschetSvChildNode, declarationDataId))
                            testCntNodePersSvStrahLic++
                        }
                    }
                } else if (raschetSvNode.name == NODE_NAME_SV_NP) {
                    // Разбор узла СвНП
                    raschsvSvnpPodpisant = parseSvNP(raschetSvNode, raschsvSvnpPodpisant)
                } else if (raschetSvNode.name == NODE_NAME_PODPISANT) {
                    // Разбор узла Подписант
                    raschsvSvnpPodpisant = parsePodpisant(raschetSvNode, raschsvSvnpPodpisant)
                }
            }
        }
    }
    // Сохранение коллекции объектов ПерсСвСтрахЛиц
    if (raschsvPersSvStrahLicList.size() >= 0) {
        // При добавлении (обновлении) записей в справочнике Физические лица, в объект ПерсСвСтрахЛиц будет добавлена ссылка на запись в справочнике Физические лица
        raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
    }

    // Сохранение Сведений о плательщике страховых взносов и Сведения о лице, подписавшем документ
    raschsvSvnpPodpisantService.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant)

    // Расчет сводных показателей
    raschSvItog(fileNode, declarationDataId)

    // Тестирование соответствия числа узлов
    if (binding.variables.containsKey("countNodes")) {
        if (countNodes.get(NODE_NAME_PERS_SV_STRAH_LIC) != testCntNodePersSvStrahLic) {
            // ПерсСвСтрахЛиц
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_PERS_SV_STRAH_LIC)

        } else if (countNodes.get(NODE_NAME_UPL_PER_OPS) != testCntNodeUplPerOPS) {
            // УплПерОПС
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_UPL_PER_OPS)
        } else if (countNodes.get(NODE_NAME_UPL_PER_OPS) != testCntNodeUplPerOMS) {
            // УплПерОМС
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_UPL_PER_OPS)
        } else if (countNodes.get(NODE_NAME_UPL_PER_OPS_DOP) != testCntNodeUplPerOPSDop) {
            // УплПерОПСДоп
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_UPL_PER_OPS_DOP)
        } else if (countNodes.get(NODE_NAME_UPL_PER_DSO) != testCntNodeUplPerDso) {
            // УплПерДСО
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_UPL_PER_OSS)
        } else if (countNodes.get(NODE_NAME_UPL_PER_OSS) != testCntNodeUplPerOSS) {
            // УплПерОСС
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_PREV_RASH_OSS)
        } else if (countNodes.get(NODE_NAME_PREV_RASH_OSS) != testCntNodePrevRashOSS) {
            // ПревРасхОСС
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_UPL_PER_DSO)
        } else if (countNodes.get(NODE_NAME_RASCH_SV_OPS_OMS) != testCntNodeRaschSvOpsDms) {
            // РасчСВ_ОПС_ОМС
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_RASCH_SV_OPS_OMS)

        } else if (countNodes.get(NODE_NAME_RASCH_SV_OSS_VNM) != testCntNodeRaschSvOSSVnm) {
            // РасчСВ_ОСС.ВНМ
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_RASCH_SV_OSS_VNM)

        } else if (countNodes.get(NODE_NAME_RASH_OSS_ZAK) != testCntNodeRaschOSSZak) {
            // РасхОССЗак
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_RASH_OSS_ZAK)

        } else if (countNodes.get(NODE_NAME_VYPL_FIN_FB) != testCntNodeVyplFinFB) {
            // ВыплФинФБ
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_VYPL_FIN_FB)

        } else if (countNodes.get(NODE_NAME_PRAV_TARIF3_1_427) != testCntNodePravTarif31427) {
            // ПравТариф3.1.427
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_PRAV_TARIF3_1_427)
        } else if (countNodes.get(NODE_NAME_PRAV_TARIF5_1_427) != testCntNodePravTarif51427) {
            // ПравТариф5.1.427
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_PRAV_TARIF5_1_427)
        } else if (countNodes.get(NODE_NAME_PRAV_TARIF7_1_427) != testCntNodePravTarif71427) {
            // ПравТариф7.1.427
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_PRAV_TARIF7_1_427)
        } else if (countNodes.get(NODE_NAME_SV_PRIM_TARIF9_1_427) != testCntNodePravTarif91427) {
            // СвПримТариф9.1.427
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SV_PRIM_TARIF9_1_427)
        } else if (countNodes.get(NODE_NAME_SV_PRIM_TARIF2_2_425) != testCntNodePravTarif22425) {
            // СвПримТариф2.2.425
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SV_PRIM_TARIF2_2_425)
        } else if (countNodes.get(NODE_NAME_SV_PRIM_TARIF1_3_422) != testCntNodePravTarif13422) {
            // СвПримТариф1.3.422
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SV_PRIM_TARIF1_3_422)

        } else if (countNodes.get(NODE_NAME_SVED_PATENT) != testCntNodeSvedPatent) {
            // СведПатент
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SVED_PATENT)

        } else if (countNodes.get(NODE_NAME_SV_INO_GRAZD) != testCntNodeSvInoGrazd) {
            // СвИноГражд
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SV_INO_GRAZD)

        } else if (countNodes.get(NODE_NAME_SVED_OBUCH) != testCntNodeSvObuch) {
            // СведОбуч
            logger.error(ERROR_MESSAGE_NOT_MATCH_NODE_COUNT, NODE_NAME_SVED_OBUCH)
        }
    }
}
// Плательщик страховых взносов
@Field final PODP_1 = '1'
// Представитель плательщика страховых взносов
@Field final PODP_2 = '2'

// Проверки
@Field final INN_JUR_LENGTH = 10
@Field final INN_IP_LENGTH = 12
@Field final CHECK_FILE_NAME = "Ошибка заполнения атрибутов транспортного файла \"%s\": %s"
@Field final CHECK_FILE_NAME_NO = "код НО в имени не совпадает с кодом НО внутри файла"
@Field final CHECK_FILE_NAME_INN = "ИНН в имени не совпадает с ИНН внутри файла"
@Field final CHECK_FILE_NAME_KPP = "КПП в имени не совпадает с КПП внутри файла"
@Field final CHECK_PAYMENT_OKVED_NOT_FOUND = "Файл.Документ.СвНП.ОКВЭД = \"%s\" транспортного файла \"%s\" не найден в справочнике ОКВЭД"
@Field final CHECK_PAYMENT_INN = "Файл.Документ.СвНП.НПЮП.ИННЮЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PAYMENT_REORG_INN = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ в транспортном файле \"%s\" не заполнен, элемент является обязательным, так как форма реорганизации = \"%s\""
@Field final CHECK_PAYMENT_REORG_KPP = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП в транспортном файле \"%s\" не заполнен, элемент является обязательным, так как форма реорганизации = \"%s\""
@Field final CHECK_PAYMENT_REORG_INN_VALUE = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PAYMENT_IP_INN_VALUE = "Файл.Документ.СвНП.НПИП.ИННФЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PAYMENT_FL_INN_VALUE = "Файл.Документ.СвНП.НПФЛ.ИННФЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PAYMENT_FL_ADDR = "Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'%s'/'%s'/'%s'/'%s'/'%s'\" транспортного файла \"%s\" не найден в справочнике"
@Field final CHECK_PAYMENT_IP_COUNTRY = "Файл.Документ.СвНП.НПИП.СвНПФЛ.Гражд = \"%s\" транспортного файла \"%s\" не найден в справочнике ОКСМ"
@Field final CHECK_PAYMENT_IP_DOC = "Файл.Документ.СвНП.НПФЛ.СвНПФЛ.УдЛичнФЛ.КодВидДок = \"%s\" транспортного файла \"%s\" не найден в справочнике \"Коды документов, удостоверяющих личность\""
@Field final CHECK_PODPISANT_EMPTY_FIO = "Файл.Документ.Подписант.ФИО = \"'%s'/'%s'\" в транспортном файле \"%s\" не заполнен, элемент является обязательным, если признак лица, подписавшего документ = 2 или 1 и плательщиком является ЮЛ"
@Field final CHECK_PODPISANT_EMPTY_DOC = "Файл.Документ.Подписант.СвПред.НаимДок = \"%s\" в транспортном файле \"%s\" не заполнен, элемент является обязательным, так как признак лица, подписавшего документ = 2"
@Field final CHECK_CALCULATION_OBZ = "Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \"%s\" не заполнен, элемент является обязательным, так как код места = \"%s\""
@Field final CHECK_CALCULATION_OBZ_OKTMO = "Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"%s\" транспортного файла \"%s\" не найден в справочнике \"Общероссийский классификатор территорий муниципальных образований (ОКТМО)\""
@Field final CHECK_CALCULATION_KBK = "%s = \"%s\" транспортного файла \"%s\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""
@Field final CHECK_CALCULATION_SUMM = "%s в транспортном файле \"%s\" не заполнен, элемент является обязательным, так как не заполнен %s"
@Field final CHECK_TARIFF_INN = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.ИННФЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_TARIFF_SNILS = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.СНИЛС = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_TARIFF_COUNTRY = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.Гражд = \"%s\" иностранного гражданина \"%s %s\" не найден в справочнике ОКСМ"
@Field final CHECK_PERSON_INN = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PERSON_SNILS = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС = \"%s\" в транспортном файле \"%s\" некорректный"
@Field final CHECK_PERSON_DOCTYPE = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок = \"%s\" получателя доходов с СНИЛС \"%s\" транспортного файла \"%s\" не найден в справочнике \"Коды документов, удостоверяющих личность\""
@Field final CHECK_PERSON_OKSM = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд = \"%s\" ФЛ с СНИЛС \"%s\" транспортного файла \"%s\" не найден в справочнике ОКСМ"
@Field final CHECK_PERSON_PERIOD = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц = \"'%s'/'%s'\" в транспортном файле \"%s\" не входит в отчетный период формы"
@Field final CHECK_PERSON_DUL = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СерНомДок = \"%s\" не соответствует порядку заполнения: знак \"N\" не проставляется, серия и номер документа отделяются знаком \" \" (\"пробел\")"

/**
 * Существует ли CODE в справочнике ОКВЭД
 */
@Memoized
boolean isExistsOkved(code) {
    def dataProvider = refBookFactory.getDataProvider(RefBook.Id.OKVED.getId())
    return dataProvider.getRecordsCount(new Date(), "CODE = '$code'") > 0
}

/**
 * Существует ли адрес в справочнике адресов
 */
@Memoized
boolean isExistsAddress(regionCode, area, city, locality, street) {
    if (!regionCode || !area || !city || !locality || !street) {
        return false
    }

    return fiasRefBookService.findAddress(regionCode, area, city, locality, street).size() > 0
}

/**
 * Сущесвтует ли код в справочнике ОКСМ
 */
@Memoized
boolean isExistsOKSM(String code) {
    def dataProvider = refBookFactory.getDataProvider(RefBook.Id.COUNTRY.getId())
    return dataProvider.getRecordsCount(new Date(), "CODE = '$code'") > 0
}

/**
 * Сущесвтует ли код в справочнике видов документов
 */
@Memoized
boolean isExistsDocType(String code) {
    def dataProvider = refBookFactory.getDataProvider(RefBook.Id.DOCUMENT_CODES.getId())
    return dataProvider.getRecordsCount(new Date(), "CODE = '$code'") > 0
}

/**
 * Существует ли запись в ОКТМО
 */
@Memoized
boolean isExistsOKTMO(String code) {
    def dataProvider = refBookFactory.getDataProvider(RefBook.Id.OKTMO.getId())
    return dataProvider.getRecordsCount(new Date(), "CODE = '$code'") > 0
}

/**
 * Существует ли код КБК
 */
@Memoized
boolean isExistsKBK(String code) {
    def dataProvider = refBookFactory.getDataProvider(RefBook.Id.KBK.getId())
    return dataProvider.getRecordsCount(new Date(), "CODE = '$code'") > 0
}

/**
 * Проверки при загрузке
 *
 * Общие проверки (выполняются для ТФ в целом):
 *      1.1 Проверки соответствия данных имени файла и содержимого файла
 *      1.2 Проверки по плательщику страховых взносов
 *      1.3 Проверка подписанта
 *      1.4 Сводные данные об обязательствах плательщика
 * По каждому иностранному гражданину (Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд):
 *      Все проверки раздела 1.5 Проверки по каждому иностранному гражданину
 * По каждому ФЛ - получателю дохода (Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц):
 *      Все проверки  раздела 1.6 Проверки по каждому физическому лицу - получателю доходов
 */
void checkImportRaschsv(fileNode, fileName) {

    long time = System.currentTimeMillis();

    checkRaschsvFileName(fileNode, fileName)
    checkPayment(fileNode, fileName)
    checkPodpisant(fileNode, fileName)
    checkPayer(fileNode, fileName)
    checkTariff_2_2_425(fileNode, fileName)
    checkFL(fileNode, fileName)

    println "checkImportRaschsv " + (System.currentTimeMillis() - time);
    logger.info("checkImportRaschsv: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * 1.1 Проверки соответствия данных имени файла и содержимого файла
 *
 * @param fileNode корневой узел XML
 */
void checkRaschsvFileName(fileNode, fileName) {
    def fileNameNo = null
    def fileNameInn = null
    def fileNameKpp = null

    fileName.find(/NO_RASCHSV_([^_]+)_([^_]+)_(\d{10})(\d{9})_(\d{8})_(.+)\.xml/) { fullMatch, tn, no, inn, kpp, date, guid ->
        fileNameNo = no
        fileNameInn = inn
        fileNameKpp = kpp
    }

    def documentNo = fileNode?."$NODE_NAME_DOCUMENT"?."@КодНО" as String
    def documentInn = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"?."$NODE_NAME_NPYL"?."@ИННЮЛ" as String
    def documentKpp = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"?."$NODE_NAME_NPYL"?."@КПП" as String

    // 1.1.1 Соответствие кода НО в файле и в имени
    if (!documentNo || documentNo != fileNameNo) {
        logger.error(CHECK_FILE_NAME, fileName, CHECK_FILE_NAME_NO)
    }

    // 1.1.2 Соответствие ИНН в файле и в имени
    if (!documentInn || documentInn != fileNameInn) {
        logger.error(CHECK_FILE_NAME, fileName, CHECK_FILE_NAME_INN)
    }

    // 1.1.3 Соответствие КПП в файле и в имени
    if (!documentKpp || documentKpp != fileNameKpp) {
        logger.error(CHECK_FILE_NAME, fileName, CHECK_FILE_NAME_KPP)
    }
}

/**
 * 1.2 Проверки по плательщику страховых взносов
 *
 * @param fileNode корневой узел XML
 */
def checkPayment(fileNode, fileName) {
    checkPaymentJL(fileNode, fileName)
    checkPaymentIP(fileNode, fileName)
    checkPaymentFL(fileNode, fileName)
}

/**
 * Проверки 1.2 для НПЮЛ
 */
def checkPaymentJL(fileNode, fileName) {
    def documentSvNP = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"
    def documentOkved = documentSvNP?."@ОКВЭД" as String

    documentSvNP?."$NODE_NAME_NPYL".each { npul ->
        def documentInn = npul?."@ИННЮЛ" as String

        // 1.2.1 Поиск ОКВЭД в справочнике
        if (documentOkved && !isExistsOkved(documentOkved)) {
            logger.warn(CHECK_PAYMENT_OKVED_NOT_FOUND, documentOkved, fileName)
        }

        // 1.2.2 Корректность ИНН ЮЛ
        if (INN_JUR_LENGTH != documentInn.length() || !ScriptUtils.checkControlSumInn(documentInn)) {
            logger.warn(CHECK_PAYMENT_INN, documentInn, fileName)
        }

        npul?."$NODE_NAME_SV_REORG_YL".each { reorg ->
            def documentReorgForm = reorg?."@ФормРеорг" as String
            def documentReorgInn = reorg?."@ИННЮЛ" as String
            def documentReorgKpp = reorg?."@КПП" as String

            // 1.2.4, 1.2.5
            if (['1', '2', '3', '4', '5', '6', '7'].contains(documentReorgForm)) {
                // 1.2.4 Наличие ИНН реорганизованной организации
                if (!documentReorgInn) {
                    logger.warn(CHECK_PAYMENT_REORG_INN, fileName, documentReorgForm)
                }
                // 1.2.5 Наличие КПП реорганизованной организации
                if (!documentReorgKpp) {
                    logger.warn(CHECK_PAYMENT_REORG_KPP, fileName, documentReorgForm)
                }
            }

            // 1.2.6 Корректность ИНН реорганизованной организации
            if (INN_JUR_LENGTH != documentReorgInn.length() || !ScriptUtils.checkControlSumInn(documentReorgInn)) {
                logger.warn(CHECK_PAYMENT_REORG_INN_VALUE, documentReorgInn, fileName)
            }
        }
    }
}

/**
 * Проверки 1.2 для НПИП
 */
def checkPaymentIP(fileNode, fileName) {
    def documentSvNP = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"

    documentSvNP?."$NODE_NAME_NPIP".each { ip ->
        def documentIpInn = ip?."@ИННФЛ" as String

        // 1.2.8 Корректность ИНН плательщика страховых взносов (ИП)
        if (documentIpInn && (INN_IP_LENGTH != documentIpInn.length() || !ScriptUtils.checkControlSumInn(documentIpInn))) {
            logger.warn(CHECK_PAYMENT_IP_INN_VALUE, documentIpInn, fileName)
        }
    }
}

/**
 * Проверки 1.2 для НПФЛ
 */
def checkPaymentFL(fileNode, fileName) {
    def documentSvNP = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"

    documentSvNP?."$NODE_NAME_NPFL".each { npfl ->
        def documentFlInn = npfl?."$NODE_NAME_NPFL_INNFL" as String
        def documentFlCountry = npfl?."$NODE_NAME_NPFL_SVNPFL"?."@Гражд" as String
        def documentFlAddr = npfl?."$NODE_NAME_NPFL_SVNPFL"?."$NODE_NAME_NPFL_SVNPFL_ADDRMJRF"
        def documentFlAddrRegion = documentFlAddr?.'@КодРегион' as String
        def documentFlAddrArea = documentFlAddr?.'@Район' as String
        def documentFlAddrCity = documentFlAddr?.'@Город' as String
        def documentFlAddrLocality = documentFlAddr?.'@НаселПункт' as String
        def documentFlAddrStreet = documentFlAddr?.'@Улица' as String
        def documentFlDocCode = npfl?."$NODE_NAME_NPFL_SVNPFL"?."$NODE_NAME_NPFL_SVNPFL_UDLICHFL"?."@КодВидДок" as String

        // 1.2.9 Корректность ИНН плательщика страховых взносов (ФЛ)
        if (documentFlInn && (INN_IP_LENGTH != documentFlInn.length() || !ScriptUtils.checkControlSumInn(documentFlInn))) {
            logger.warn(CHECK_PAYMENT_FL_INN_VALUE, documentFlInn, fileName)
        }

        // 1.2.10 Соответствие адреса ФЛ (плательщика страховых взносов) ФИАС
        if (!isExistsAddress(documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet)) {
            logger.warn(CHECK_PAYMENT_FL_ADDR,
                    documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet,
                    fileName
            )
        }

        // 1.2.11 Поиск кода гражданства в справочнике
        if (documentFlCountry && !isExistsOKSM(documentFlCountry)) {
            logger.warn(CHECK_PAYMENT_IP_COUNTRY, documentFlCountry, fileName)
        }

        // 1.2.12 Поиск кода вида документа
        if (documentFlDocCode && !isExistsDocType(documentFlDocCode)) {
            logger.warn(CHECK_PAYMENT_IP_DOC, documentFlDocCode, fileName)
        }
    }
}

/**
 * Проверки 1.3
 */
def checkPodpisant(fileNode, fileName) {
    def documentSvNpYl = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"?."$NODE_NAME_NPYL"

    fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_PODPISANT".each { podpisant ->
        def prPodp = podpisant?."@ПрПодп" as String
        def fio = podpisant?."$NODE_NAME_FIO"
        def firstName = fio?."@Имя" as String
        def secondName = fio?."@Фамилия" as String
        def docName = podpisant?."$NODE_NAME_SV_PRED"?."@НаимДок" as String

        if (PODP_2 == prPodp || (PODP_1 == prPodp && documentSvNpYl.isEmpty())) {
            // 1.3.1 Наличие ФИО подписанта
            if (!firstName || !secondName) {
                logger.warn(CHECK_PODPISANT_EMPTY_FIO, secondName, firstName, fileName)
            }

            // 1.3.2 Наличие сведений о представителе плательщика страховых взносов
            if (PODP_2 == prPodp && !docName) {
                logger.warn(CHECK_PODPISANT_EMPTY_DOC, docName, fileName)
            }
        }
    }
}

/**
 * Проверки 1.4
 */
def checkPayer(fileNode, fileName) {
    def documentPlaceCode = fileNode?."$NODE_NAME_DOCUMENT"?."@ПоМесту" as String
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"

    raschets.each { raschet ->
        def payments = raschet?."$NODE_NAME_OBYAZ_PLAT_SV"

        // 1.4.1 Наличие сводных данных об обязательствах плательщика страховых взносов
        if ('124' != documentPlaceCode && payments.isEmpty()) {
            logger.error(CHECK_CALCULATION_OBZ, fileName, documentPlaceCode)
        }

        payments.each { payment ->
            def oktmoCode = payment?."@ОКТМО" as String

            // 1.4.2 Поиск кода ОКТМО
            if (oktmoCode && !isExistsOKTMO(oktmoCode)) {
                logger.error(CHECK_CALCULATION_OBZ_OKTMO, oktmoCode, fileName)
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПС
            payment?."$NODE_NAME_UPL_PER_OPS".each { ops ->
                def kbkCode = ops?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОМС
            payment?."$NODE_NAME_UPL_PER_OMS".each { oms ->
                def kbkCode = oms?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПСДоп
            payment?."$NODE_NAME_UPL_PER_OPS_DOP".each { dop ->
                def kbkCode = dop?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерДСО
            payment?."$NODE_NAME_UPL_PER_DSO".each { dso ->
                def kbkCode = dso?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПревОСС
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def kbkCode = uplPrevOss?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.4 - 1.4.11
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def prevPer = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВПер" as String
                def uplPer = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУплПер" as String
                if (prevPer?.isEmpty() && uplPer?.isEmpty()) {
                    // 1.4.4 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер")
                    // 1.4.8 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер")
                }

                def prev1M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ1М" as String
                def upl1M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл1М" as String
                if (prev1M?.isEmpty() && upl1M?.isEmpty()) {
                    // 1.4.5 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М")
                    // 1.4.9 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М")
                }

                def prev2M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ2М" as String
                def upl2M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл2М" as String
                if (prev2M?.isEmpty() && upl2M?.isEmpty()) {
                    // 1.4.6 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М")
                    // 1.4.10 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М")
                }

                def prev3M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ3М" as String
                def upl3M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл3М" as String
                if (prev3M?.isEmpty() && upl3M?.isEmpty()) {
                    // 1.4.7 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М")
                    // 1.4.11 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М")
                }
            }
        }
    }
}

/**
 * Проверки 1.5
 */
def checkTariff_2_2_425(fileNode, fileName) {
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"

    raschets.each { raschet ->
        raschet?."$NODE_NAME_OBYAZ_PLAT_SV".each { payment ->
            payment?."$NODE_NAME_SV_PRIM_TARIF2_2_425".each { tariff ->
                tariff?."$NODE_NAME_SV_INO_GRAZD".each { inGra ->
                    def innFl = inGra?."@ИННФЛ" as String
                    def snils = inGra?."@СНИЛС" as String
                    def countryCode = inGra?."@Гражд" as String
                    def surname = inGra?."$NODE_NAME_FIO"?."@Фамилия" as String
                    def firstName = inGra?."$NODE_NAME_FIO"?."@Имя" as String

                    // 1.5.1 Корректность ИНН иностранного гражданина и лица без гражданства
                    if (innFl && (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl))) {
                        logger.error(CHECK_TARIFF_INN, innFl, fileName)
                    }

                    // 1.5.2 Корректность СНИЛС иностранного гражданина и лица без гражданства
                    if (snils && !ScriptUtils.checkSnils(snils)) {
                        logger.error(CHECK_TARIFF_SNILS, snils, fileName)
                    }

                    // 1.5.3 Поиск кода гражданства иностранного гражданина и лица без гражданства в справочнике
                    if (countryCode && !isExistsOKSM(countryCode)) {
                        logger.error(CHECK_TARIFF_COUNTRY, countryCode, surname, firstName)
                    }
                }
            }
        }
    }
}

/**
 * Проверки 1.6
 */
def checkFL(fileNode, fileName) {
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"
    def docPeriod = fileNode?."$NODE_NAME_DOCUMENT"?."@Период" as String
    def docYear = fileNode?."$NODE_NAME_DOCUMENT"?."@ОтчетГод" as String

    raschets.each { raschet ->
        raschet?."$NODE_NAME_PERS_SV_STRAH_LIC".each { person ->
            def personPeriod = person?."@Период" as String
            def personYear = person?."@ОтчетГод" as String

            // 1.6.5 Принадлежность дат сведений по ФЛ к отчетному периоду
            if (!(docPeriod == personPeriod && docYear == personYear)) {
                logger.error(CHECK_PERSON_PERIOD, personPeriod, personYear, fileName)
            }

            person?."$NODE_NAME_DAN_FL_POLUCH".each { data ->
                def snils = data?."@СНИЛС" as String
                def innFl = data?."@ИННФЛ" as String
                def docTypeCode = data?."@КодВидДок" as String
                def national = data?."@Гражд" as String
                def serNumDoc = data?."@СерНомДок" as String

                // 1.6.1 Корректность ИНН ФЛ - получателя дохода
                if (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl)) {
                    logger.error(CHECK_PERSON_INN, innFl, fileName)
                }

                // 1.6.2 Корректность СНИЛС ФЛ - получателя дохода
                if (snils && !ScriptUtils.checkSnils(snils)) {
                    logger.error(CHECK_PERSON_SNILS, snils, fileName)
                }

                // 1.6.3 Поиск кода вида документа ФЛ - получателя дохода
                if (docTypeCode && !isExistsDocType(docTypeCode)) {
                    logger.error(CHECK_PERSON_DOCTYPE, docTypeCode, snils, fileName)
                }

                // 1.6.4 Поиск кода гражданства ФЛ - получателя дохода в справочнике
                if (national && !isExistsOKSM(national)) {
                    logger.error(CHECK_PERSON_OKSM, national, snils, fileName)
                }

                // 1.6.6 Корректность серии и номера ДУЛ
                if (serNumDoc && !ScriptUtils.checkDul(serNumDoc)) {
                    logger.error(CHECK_PERSON_DUL, serNumDoc)
                }
            }
        }
    }
}

/**
 * Удаляет данные из raschsv_kol_lic_tip и raschsv_sv_sum_1tip по declarationData.id
 */
def deleteData() {
    raschsvObyazPlatSvService.deleteFromLinkedTable(declarationData.id)
}

/**
 * Сохраняет в базу запись "Сводные показатели формы"
 *
 * @param declarationDataId
 * @return идентификатор записи
 */
def insertItogStrahLic(declarationDataId) {
    RaschsvItogStrahLic raschsvItogStrahLic = new RaschsvItogStrahLic()
    raschsvItogStrahLic.declarationDataId = declarationDataId
    return raschsvItogVyplService.insertItogStrahLic(raschsvItogStrahLic)
}

/**
 * Сохраняет в базу ."Сводные сведения о выплатах"
 */
def insertItogVypl(raschsvItogVyplSet) {
    raschsvItogVyplService.insertItogVypl(raschsvItogVyplSet)
}

/**
 * Сохраняет в базу ."Сводные сведения о выплатах"
 */
def insertItogVyplDop(raschsvItogVyplDopSet) {
    raschsvItogVyplService.insertItogVyplDop(raschsvItogVyplDopSet)
}

/**
 * Расчет сводных показателей
 *
 * @param fileNode корневой узел xml
 */
def raschSvItog(fileNode, declarationDataId) {
    Long raschsvItogStrahLicId = insertItogStrahLic(declarationDataId)
    raschSvItogMt(fileNode, raschsvItogStrahLicId)
    raschSvItogDop(fileNode, raschsvItogStrahLicId)
}

/**
 * Расчет данных раздела <Форма>."Сводные сведения о выплатах"
 */
def raschSvItogMt(fileNode, raschsvItogStrahLicId) {
    Map<Tuple2, RaschsvItogVypl> groups = [:]

    def vyplMk = fileNode?.
    "$NODE_NAME_DOCUMENT"?.
    "$NODE_NAME_RASCHET_SV"?.
    "$NODE_NAME_PERS_SV_STRAH_LIC"?.
    "$NODE_NAME_SV_VYPL_SVOPS"?.
    "$NODE_NAME_SV_VYPL"?.
            "$NODE_NAME_SV_VYPL_MK"

    // Расчет данных раздела <Форма>."Сводные сведения о выплатах"
    vyplMk.each { vypl ->
        def month = vypl."@Месяц" as String
        def codeCat = vypl."@КодКатЛиц" as String
        def sumVypl = (vypl."@СумВыпл" as String ?: "0").toBigDecimal()
        def sumVyplOps = (vypl."@ВыплОПС" as String ?: "0").toBigDecimal()
        def sumVyplOpsDog = (vypl."@ВыплОПСДог" as String ?: "0").toBigDecimal()
        def sumNachisl = (vypl."@НачислСВ" as String ?: "0").toBigDecimal()

        def groupKey = new Tuple2(month, codeCat)
        def groupValue = groups.get(groupKey)

        if (groupValue != null) {
            groupValue.kolFl += 1
            groupValue.sumVypl += sumVypl
            groupValue.vyplOps += sumVyplOps
            groupValue.vyplOpsDog += sumVyplOpsDog
            groupValue.sumNachisl += sumNachisl
        } else {
            groupValue = new RaschsvItogVypl()
            groupValue.mesyac = month
            groupValue.kodKatLic = codeCat
            groupValue.kolFl = 1
            groupValue.sumVypl = sumVypl
            groupValue.vyplOps = sumVyplOps
            groupValue.vyplOpsDog = sumVyplOpsDog
            groupValue.sumNachisl = sumNachisl
            groupValue.raschsvItogStrahLicId = raschsvItogStrahLicId

            groups.put(groupKey, groupValue)
        }
    }

    insertItogVypl(groups.values())
}

/**
 * Расчет данных раздела <Форма>."Сводные сведения о выплатах по доп. тарифам"
 */
def raschSvItogDop(fileNode, raschsvItogStrahLicId) {
    Map<Tuple2, RaschsvItogVyplDop> groups = [:]

    def vyplDop = fileNode?.
    "$NODE_NAME_DOCUMENT"?.
    "$NODE_NAME_RASCHET_SV"?.
    "$NODE_NAME_PERS_SV_STRAH_LIC"?.
    "$NODE_NAME_SV_VYPL_SVOPS"?.
    "$NODE_NAME_VYPL_SV_DOP"?.
            "$NODE_NAME_VYPL_SV_DOP_MT"

    vyplDop.each { vypl ->
        def month = vypl."@Месяц" as String
        def tarif = vypl."@Тариф" as String
        def vyplSv = (vypl."@ВыплСВ" as String ?: "0").toBigDecimal()
        def nachislSv = (vypl."@НачислСВ" as String ?: "0").toBigDecimal()

        def groupKey = new Tuple2(month, tarif)
        def groupValue = groups.get(groupKey)

        if (groupValue != null) {
            groupValue.kolFl += 1
            groupValue.sumVypl += vyplSv
            groupValue.sumNachisl += nachislSv
        } else {
            groupValue = new RaschsvItogVyplDop()
            groupValue.mesyac = month
            groupValue.tarif = tarif
            groupValue.kolFl = 1
            groupValue.sumVypl = vyplSv
            groupValue.sumNachisl = nachislSv
            groupValue.raschsvItogStrahLicId = raschsvItogStrahLicId

            groups.put(groupKey, groupValue)
        }
    }

    insertItogVyplDop(groups.values())
}

/**
 * Разбор узла СвНП
 * @param raschsvSvnpPodpisant
 * @return
 */
RaschsvSvnpPodpisant parseSvNP(Object svNPNode, RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
    raschsvSvnpPodpisant.svnpOkved = svNPNode.attributes()[SV_NP_OKVED]
    raschsvSvnpPodpisant.svnpTlph = svNPNode.attributes()[SV_NP_TLPH]
    svNPNode.childNodes().each { NPYLNode ->
        if (NPYLNode.name == NODE_NAME_NPYL) {
            // Разбор узла НПЮЛ
            raschsvSvnpPodpisant.svnpNaimOrg = NPYLNode.attributes()[NPYL_NAIM_ORG]
            raschsvSvnpPodpisant.svnpInnyl = NPYLNode.attributes()[NPYL_INNYL]
            raschsvSvnpPodpisant.svnpKpp = NPYLNode.attributes()[NPYL_KPP]
            NPYLNode.childNodes().each { sVReorgYLNode ->
                // Разбор узла СвРеоргЮЛ
                raschsvSvnpPodpisant.svnpSvReorgForm = sVReorgYLNode.attributes()[SV_REORG_YL_FORM_REORG]
                raschsvSvnpPodpisant.svnpSvReorgInnyl = sVReorgYLNode.attributes()[SV_REORG_YL_INNYL]
                raschsvSvnpPodpisant.svnpSvReorgKpp = sVReorgYLNode.attributes()[SV_REORG_YL_KPP]
            }
        }
    }

    return raschsvSvnpPodpisant
}

/**
 * Разбор узла Подписант
 * @param raschsvSvnpPodpisant
 * @return
 */
RaschsvSvnpPodpisant parsePodpisant(Object podpisantNode, RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
    raschsvSvnpPodpisant.podpisantPrPodp = podpisantNode.attributes()[PODPISANT_PR_PODP]
    podpisantNode.childNodes().each { podpisantChildNode ->
        if (podpisantChildNode.name == NODE_NAME_FIO) {
            // Разбор узла ФИО
            raschsvSvnpPodpisant.familia = podpisantChildNode.attributes()[FIO_FAMILIA]
            raschsvSvnpPodpisant.imya = podpisantChildNode.attributes()[FIO_IMYA]
            raschsvSvnpPodpisant.otchestvo = podpisantChildNode.attributes()[FIO_OTCHESTVO_NAME]
        } else if (podpisantChildNode.name == NODE_NAME_SV_PRED) {
            // Разбор узла СвПред
            raschsvSvnpPodpisant.podpisantNaimDoc = podpisantChildNode.attributes()[SV_PRED_NAIM_DOC]
            raschsvSvnpPodpisant.podpisantNaimOrg = podpisantChildNode.attributes()[SV_PRED_NAIM_ORG]
        }
    }

    return raschsvSvnpPodpisant
}

/**
 * Разбор узла ОбязПлатСВ
 * @param obyazPlatSvNode - узел ОбязПлатСВ
 * @param declarationDataId - идентификатор декларации для которой загружаются данные
 */
Long parseRaschsvObyazPlatSv(Object obyazPlatSvNode, Long declarationDataId) {
    RaschsvObyazPlatSv raschsvObyazPlatSv = new RaschsvObyazPlatSv()
    raschsvObyazPlatSv.declarationDataId = declarationDataId
    raschsvObyazPlatSv.oktmo = obyazPlatSvNode.attributes()[OBYAZ_PLAT_SV_OKTMO]

    // Сохранение ОбязПлатСВ
    def raschsvObyazPlatSvId = raschsvObyazPlatSvService.insertObyazPlatSv(raschsvObyazPlatSv)

    // Набор объектов УплПер
    def raschsvUplPerList = []

    // Набор объектов РасчСВ_ОПС_ОМС
    def raschsvSvOpsOmsList = []

    obyazPlatSvNode.childNodes().each { obyazPlatSvChildNode ->
        if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OPS ||
                obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OMS ||
                obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OPS_DOP ||
                obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_DSO) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узлов УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО
            //----------------------------------------------------------------------------------------------------------
            RaschsvUplPer raschsvUplPer = new RaschsvUplPer()
            raschsvUplPer.raschsvObyazPlatSvId = raschsvObyazPlatSvId
            raschsvUplPer.nodeName = obyazPlatSvChildNode.name
            raschsvUplPer.kbk = obyazPlatSvChildNode.attributes()[UPL_PER_KBK]
            raschsvUplPer.sumSbUplPer = getDouble(obyazPlatSvChildNode.attributes()[UPL_PER_SUM_SV_UPL_PER])
            raschsvUplPer.sumSbUpl1m = getDouble(obyazPlatSvChildNode.attributes()[UPL_PER_SUM_SV_UPL_1M])
            raschsvUplPer.sumSbUpl2m = getDouble(obyazPlatSvChildNode.attributes()[UPL_PER_SUM_SV_UPL_2M])
            raschsvUplPer.sumSbUpl3m = getDouble(obyazPlatSvChildNode.attributes()[UPL_PER_SUM_SV_UPL_3M])

            if (raschsvUplPerList.size() >= MAX_COUNT_UPL_PER) {
                raschsvUplPerService.insertUplPer(raschsvUplPerList)
                raschsvUplPerList = []
            }

            raschsvUplPerList.add(raschsvUplPer)

            if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OPS) {
                testCntNodeUplPerOPS++;
            } else if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OMS) {
                testCntNodeUplPerOMS++;
            } else if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_OPS_DOP) {
                testCntNodeUplPerOPSDop++;
            } else if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PER_DSO) {
                testCntNodeUplPerDso++;
            }

        } else if (obyazPlatSvChildNode.name == NODE_NAME_UPL_PREV_OSS) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла УплПревОСС
            //----------------------------------------------------------------------------------------------------------
            RaschsvUplPrevOss raschsvUplPrevOss = new RaschsvUplPrevOss()
            raschsvUplPrevOss.raschsvObyazPlatSvId = raschsvObyazPlatSvId
            raschsvUplPrevOss.kbk = obyazPlatSvChildNode.attributes()[PREV_RASH_KBK]

            obyazPlatSvChildNode.childNodes().each { uplPrevOssChildNode ->
                if (uplPrevOssChildNode.name == NODE_NAME_UPL_PER_OSS) {
                    // Разбор узла УплПерОСС
                    raschsvUplPrevOss.sumSbUplPer = getDouble(uplPrevOssChildNode.attributes()[UPL_PER_SUM_SV_UPL_PER])
                    raschsvUplPrevOss.sumSbUpl1m = getDouble(uplPrevOssChildNode.attributes()[UPL_PER_SUM_SV_UPL_1M])
                    raschsvUplPrevOss.sumSbUpl2m = getDouble(uplPrevOssChildNode.attributes()[UPL_PER_SUM_SV_UPL_2M])
                    raschsvUplPrevOss.sumSbUpl3m = getDouble(uplPrevOssChildNode.attributes()[UPL_PER_SUM_SV_UPL_3M])
                    testCntNodeUplPerOSS++
                } else if (uplPrevOssChildNode.name == NODE_NAME_PREV_RASH_OSS) {
                    // Разбор узла ПревРасхОСС
                    raschsvUplPrevOss.prevRashSvPer = getDouble(uplPrevOssChildNode.attributes()[PREV_RASH_PREV_RASH_SV_PER])
                    raschsvUplPrevOss.prevRashSv1m = getDouble(uplPrevOssChildNode.attributes()[PREV_RASH_PREV_RASH_SV_1M])
                    raschsvUplPrevOss.prevRashSv2m = getDouble(uplPrevOssChildNode.attributes()[PREV_RASH_PREV_RASH_SV_2M])
                    raschsvUplPrevOss.prevRashSv3m = getDouble(uplPrevOssChildNode.attributes()[PREV_RASH_PREV_RASH_SV_3M])
                    testCntNodePrevRashOSS++
                }
            }

            // Сохранение УплПревОСС
            raschsvUplPrevOssService.insertUplPrevOss(raschsvUplPrevOss)

        } else if (obyazPlatSvChildNode.name == NODE_NAME_RASCH_SV_OPS_OMS) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла РасчСВ_ОПС_ОМС
            //----------------------------------------------------------------------------------------------------------
            RaschsvSvOpsOms raschsvSvOpsOms = new RaschsvSvOpsOms()
            raschsvSvOpsOms.raschsvObyazPlatSvId = raschsvObyazPlatSvId
            raschsvSvOpsOms.tarifPlat = obyazPlatSvChildNode.attributes()[RASCH_SV_OPS_OMS_TARIF_PLAT]

            // Набор дочерних узлов РасчСВ_ОПС_ОМС
            def raschsvSvOpsOmsRaschList = []

            obyazPlatSvChildNode.childNodes().each { raschSvOpsOmsChildNode ->

                if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OPS ||
                        raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OMS) {

                    RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new RaschsvSvOpsOmsRasch()
                    raschsvSvOpsOmsRasch.nodeName = raschSvOpsOmsChildNode.name

                    // Набор сведений о сумме
                    def raschsvSvOpsOmsRaschSumList = []
                    // Набор сведений о количестве
                    def raschsvSvOpsOmsRaschKolList = []

                    // Разбор узлов РасчСВ_ОПС и РасчСВ_ОМС
                    raschSvOpsOmsChildNode.childNodes().each { raschSvOpsOmsChildChildNode ->
                        if (raschSvOpsOmsChildChildNode.name == NODE_NAME_KOL_STRAH_LIC_VS ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_KOL_LIC_NACH_SV_VS ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_PREV_BAZ_OPS) {
                            // Разбор узлов КолСтрахЛицВс, КолЛицНачСВВс, ПревБазОПС
                            RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol = new RaschsvSvOpsOmsRaschKol()
                            raschsvSvOpsOmsRaschKol.nodeName = raschSvOpsOmsChildChildNode.name
                            raschsvSvOpsOmsRaschKol.raschsvKolLicTip = parseRaschsvKolLicTip(raschSvOpsOmsChildChildNode)

                            raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol)
                        } else if (raschSvOpsOmsChildChildNode.name == NODE_NAME_VYPL_NACHISL_FL ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NE_OBLOZEN_SV ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_BAZ_NACHISL_SV ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_BAZ_PREVYSH_OPS ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NACHISL_SV ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NACHISL_SV_NE_PREV ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NACHISL_SV_PREV) {
                            // Разбор узлов ВыплНачислФЛ, НеОбложенСВ, БазНачислСВ, БазПревышОПС, НачислСВ, НачислСВНеПрев, НачислСВПрев
                            RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum = new RaschsvSvOpsOmsRaschSum()
                            raschsvSvOpsOmsRaschSum.nodeName = raschSvOpsOmsChildChildNode.name
                            raschsvSvOpsOmsRaschSum.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(raschSvOpsOmsChildChildNode)

                            raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum)
                        }
                    }

                    raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRaschSumList
                    raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRaschKolList
                    raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch)

                } else if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OPS428) {
                    // Разбор узла РасчСВ_ОПС428
                    raschSvOpsOmsChildNode.childNodes().each { raschSvOps428ChildNode ->
                        // Разбор узлов РасчСВ_428.1-2, РасчСВ_428.3
                        RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new RaschsvSvOpsOmsRasch()
                        raschsvSvOpsOmsRasch.nodeName = raschSvOps428ChildNode.name

                        // Набор сведений о сумме
                        def raschsvSvOpsOmsRaschSumList = []
                        // Набор сведений о количестве
                        def raschsvSvOpsOmsRaschKolList = []

                        raschsvSvOpsOmsRasch.prOsnSvDop = raschSvOps428ChildNode.attributes()[RASCH_SV_OPS428_12_PR_OSN_SV_DOP]
                        raschsvSvOpsOmsRasch.kodOsnov = raschSvOps428ChildNode.attributes()[RASCH_SV_OPS428_3_KOD_OSNOV]
                        raschsvSvOpsOmsRasch.osnovZap = raschSvOps428ChildNode.attributes()[RASCH_SV_OPS428_3_OSNOV_ZAP]
                        raschsvSvOpsOmsRasch.klasUslTrud = raschSvOps428ChildNode.attributes()[RASCH_SV_OPS428_3_KLAS_USL_TRUD]

                        raschSvOps428ChildNode.childNodes().each { raschSvOps428ChildChildNode ->
                            if (raschSvOps428ChildChildNode.name == NODE_NAME_KOL_LIC_NACH_SV) {
                                // Разбор узла КолЛицНачСВ
                                RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol = new RaschsvSvOpsOmsRaschKol()
                                raschsvSvOpsOmsRaschKol.nodeName = raschSvOps428ChildChildNode.name
                                raschsvSvOpsOmsRaschKol.raschsvKolLicTip = parseRaschsvKolLicTip(raschSvOps428ChildChildNode)

                                raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol)

                            } else if (raschSvOps428ChildChildNode.name == NODE_NAME_VYPL_NACHISL_FL ||
                                    raschSvOps428ChildChildNode.name == NODE_NAME_NE_OBLOZEN_SV ||
                                    raschSvOps428ChildChildNode.name == NODE_NAME_BAZ_NACHISL_SV_DOP ||
                                    raschSvOps428ChildChildNode.name == NODE_NAME_NACHISL_SV_DOP) {
                                // Разбор узлов ВыплНачислФЛ, НеОбложенСВ, БазНачислСВДоп, НачислСВДоп
                                RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum = new RaschsvSvOpsOmsRaschSum()
                                raschsvSvOpsOmsRaschSum.nodeName = raschSvOps428ChildChildNode.name
                                raschsvSvOpsOmsRaschSum.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(raschSvOps428ChildChildNode)

                                raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum)
                            }
                        }

                        raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRaschSumList
                        raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRaschKolList
                        raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch)
                    }
                } else if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_DSO) {

                    RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new RaschsvSvOpsOmsRasch()
                    raschsvSvOpsOmsRasch.nodeName = raschSvOpsOmsChildNode.name

                    // Набор сведений о сумме
                    def raschsvSvOpsOmsRaschSumList = []
                    // Набор сведений о количестве
                    def raschsvSvOpsOmsRaschKolList = []

                    // Разбор узла РасчСВ_ДСО
                    raschsvSvOpsOmsRasch.prRaschSum = raschSvOpsOmsChildNode.attributes()[RASCH_SV_DSO_PR_RASCH_SUM]

                    raschSvOpsOmsChildNode.childNodes().each { raschSvOpsOmsChildChildNode ->
                        if (raschSvOpsOmsChildChildNode.name == NODE_NAME_KOL_LIC_NACH_SV) {
                            // Разбор узла КолЛицНачСВ
                            RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol = new RaschsvSvOpsOmsRaschKol()
                            raschsvSvOpsOmsRaschKol.nodeName = raschSvOpsOmsChildChildNode.name
                            raschsvSvOpsOmsRaschKol.raschsvKolLicTip = parseRaschsvKolLicTip(raschSvOpsOmsChildChildNode)

                            raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol)
                        } else if (raschSvOpsOmsChildChildNode.name == NODE_NAME_VYPL_NACHISL_FL ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NE_OBLOZEN_SV ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_BAZ_NACHISL_SVDSO ||
                                raschSvOpsOmsChildChildNode.name == NODE_NAME_NACHISL_SVDSO) {
                            // Разбор узлов ВыплНачислФЛ, НеОбложенСВ, БазНачислСВДоп, НачислСВДоп
                            RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum = new RaschsvSvOpsOmsRaschSum()
                            raschsvSvOpsOmsRaschSum.nodeName = raschSvOpsOmsChildChildNode.name
                            raschsvSvOpsOmsRaschSum.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(raschSvOpsOmsChildChildNode)

                            raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum)
                        }
                    }

                    raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRaschSumList
                    raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRaschKolList
                    raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch)
                }
            }
            raschsvSvOpsOms.raschsvSvOpsOmsRaschList = raschsvSvOpsOmsRaschList

            if (raschsvSvOpsOmsList.size() >= MAX_COUNT_SV_OPS_OMS) {
                raschsvSvOpsOmsService.insertRaschsvSvOpsOms(raschsvSvOpsOmsList)
                raschsvSvOpsOmsList = []
            }

            raschsvSvOpsOmsList.add(raschsvSvOpsOms)
            testCntNodeRaschSvOpsDms++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_RASCH_SV_OSS_VNM) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла РасчСВ_ОСС.ВНМ
            //----------------------------------------------------------------------------------------------------------
            RaschsvOssVnm raschsvOssVnm = new RaschsvOssVnm()
            raschsvOssVnm.raschsvObyazPlatSvId = raschsvObyazPlatSvId
            raschsvOssVnm.prizVypl = obyazPlatSvChildNode.attributes()[RASCH_SV_OSS_VNM_PRIZ_VYPL]

            // Набор сумм страховых взносов к уплате
            def raschsvUplSvPrevList = []

            // Набор сведений о сумме
            def raschsvOssVnmSumList = []

            // Набор сведений о количестве
            def raschsvOssVnmKolList = []

            obyazPlatSvChildNode.childNodes().each { raschSvOssVnmChildNode ->
                if (raschSvOssVnmChildNode.name == NODE_NAME_UPL_SV_PREV) {
                    // Разбор узла УплСВПрев
                    raschSvOssVnmChildNode.childNodes().each { uplSvPrevNode ->
                        RaschsvUplSvPrev raschsvUplSvPrev = parseRaschsvUplSvPrev(uplSvPrevNode)
                        raschsvUplSvPrevList.add(raschsvUplSvPrev)
                    }
                } else {
                    if (raschSvOssVnmChildNode.name == NODE_NAME_KOL_STRAH_LIC_VS) {
                        // Разбор узла КолСтрахЛицВс
                        RaschsvOssVnmKol raschsvOssVnmKol = new RaschsvOssVnmKol()
                        raschsvOssVnmKol.nodeName = raschSvOssVnmChildNode.name
                        raschsvOssVnmKol.raschsvKolLicTip = parseRaschsvKolLicTip(raschSvOssVnmChildNode)
                        raschsvOssVnmKolList.add(raschsvOssVnmKol)
                    } else {
                        // Разбор остальных узлов
                        RaschsvOssVnmSum raschsvOssVnmSum = new RaschsvOssVnmSum()
                        raschsvOssVnmSum.nodeName = raschSvOssVnmChildNode.name
                        raschsvOssVnmSum.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(raschSvOssVnmChildNode)
                        raschsvOssVnmSumList.add(raschsvOssVnmSum)
                    }
                }
            }
            raschsvOssVnm.raschsvUplSvPrevList = raschsvUplSvPrevList
            raschsvOssVnm.raschsvOssVnmKolList = raschsvOssVnmKolList
            raschsvOssVnm.raschsvOssVnmSumList = raschsvOssVnmSumList

            raschsvOssVnmService.insertRaschsvOssVnm(raschsvOssVnm)
            testCntNodeRaschSvOSSVnm++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_RASH_OSS_ZAK) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла РасхОССЗак
            //----------------------------------------------------------------------------------------------------------
            RaschsvRashOssZak raschsvRashOssZak = new RaschsvRashOssZak()
            raschsvRashOssZak.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            // Набор данных о расходах
            def raschsvRashOssZakRashList = []

            obyazPlatSvChildNode.childNodes().each { raschOssZakChildNode ->

                RaschsvRashOssZakRash raschsvRashOssZakRash = new RaschsvRashOssZakRash()
                raschsvRashOssZakRash.nodeName = raschOssZakChildNode.name
                raschsvRashOssZakRash.chislSluch = getInteger(raschOssZakChildNode.attributes()[RASH_OSS_TIP_CHISL_SLUCH])
                raschsvRashOssZakRash.kolVypl = getInteger(raschOssZakChildNode.attributes()[RASH_OSS_TIP_KOL_VYPL])
                raschsvRashOssZakRash.rashVsego = getDouble(raschOssZakChildNode.attributes()[RASH_OSS_TIP_RASH_VSEGO])
                raschsvRashOssZakRash.rashFinFb = getDouble(raschOssZakChildNode.attributes()[RASH_OSS_TIP_RASH_FIN_FB])

                raschsvRashOssZakRashList.add(raschsvRashOssZakRash)
            }
            raschsvRashOssZak.raschsvRashOssZakRashList = raschsvRashOssZakRashList
            raschsvRashOssZakService.insertRaschsvRashOssZak(raschsvRashOssZak)
            testCntNodeRaschOSSZak++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_VYPL_FIN_FB) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла ВыплФинФБ
            //----------------------------------------------------------------------------------------------------------
            RaschsvVyplFinFb raschsvVyplFinFb = new RaschsvVyplFinFb()
            raschsvVyplFinFb.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            // Набор оснований выплат
            def raschsvVyplPrichinaList = []

            obyazPlatSvChildNode.childNodes().each { raschsvVyplPrichinaNode ->
                // Разбор узлов оснований выплат
                RaschsvVyplPrichina raschsvVyplPrichina = new RaschsvVyplPrichina()
                raschsvVyplPrichina.nodeName = raschsvVyplPrichinaNode.name
                raschsvVyplPrichina.svVnfUhodInv = getDouble(raschsvVyplPrichinaNode.attributes()[VYPL_FIN_FB_SV_VNF_UHOD_INV])

                // Набор произведенных выплат
                def raschsvRashVyplList = []
                raschsvVyplPrichinaNode.childNodes().each { raschsvRashVyplNode ->
                    // Разбор узлов информации о выплатах
                    RaschsvRashVypl raschsvRashVypl = new RaschsvRashVypl()
                    raschsvRashVypl.nodeName = raschsvRashVyplNode.name
                    raschsvRashVypl.chislPoluch = getInteger(raschsvRashVyplNode.attributes()[RASH_VYPL_TIP_CHISL_POLUCH])
                    raschsvRashVypl.kolVypl = getInteger(raschsvRashVyplNode.attributes()[RASH_VYPL_TIP_KOL_VYPL])
                    raschsvRashVypl.rashod = getDouble(raschsvRashVyplNode.attributes()[RASH_VYPL_TIP_RASHOD])

                    raschsvRashVyplList.add(raschsvRashVypl)
                }
                raschsvVyplPrichina.raschsvRashVyplList = raschsvRashVyplList

                raschsvVyplPrichinaList.add(raschsvVyplPrichina)
            }
            raschsvVyplFinFb.raschsvVyplPrichinaList = raschsvVyplPrichinaList
            raschsvVyplFinFbService.insertRaschsvVyplFinFb(raschsvVyplFinFb)
            testCntNodeVyplFinFB++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_PRAV_TARIF3_1_427) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла ПравТариф3.1.427
            //----------------------------------------------------------------------------------------------------------
            RaschsvPravTarif31427 raschsvPravTarif31427 = new RaschsvPravTarif31427()
            raschsvPravTarif31427.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            raschsvPravTarif31427.srChisl9mpr = getInteger(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_SR_CHISL_9MPR])
            raschsvPravTarif31427.srChislPer = getInteger(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_SR_CHISL_PER])
            raschsvPravTarif31427.doh2489mpr = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH248_9MPR])
            raschsvPravTarif31427.doh248Per = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH248_PER])
            raschsvPravTarif31427.dohKr54279mpr = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH_KR5_427_9MPR])
            raschsvPravTarif31427.dohKr5427Per = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH_KR5_427_PER])
            raschsvPravTarif31427.dohDoh54279mpr = getDouble(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH_DOH5_427_9MPR])
            raschsvPravTarif31427.dohDoh5427per = getDouble(obyazPlatSvChildNode.attributes()[PRAV_TARIF3_1_427_DOH_DOH5_427_PER])

            obyazPlatSvChildNode.childNodes().each { svReestrAkOrgNode ->
                // Разбор узла СвРеестрАкОрг
                raschsvPravTarif31427.dataZapAkOrg = getDate(svReestrAkOrgNode.attributes()[SV_REESTR_AK_ORG_DATA])
                raschsvPravTarif31427.nomZapAkOrg = svReestrAkOrgNode.attributes()[SV_REESTR_AK_ORG_NOM]
            }

            raschsvPravTarif31427Service.insertRaschsvPravTarif31427(raschsvPravTarif31427)
            testCntNodePravTarif31427++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_PRAV_TARIF5_1_427) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла ПравТариф5.1.427
            //----------------------------------------------------------------------------------------------------------
            RaschsvPravTarif51427 raschsvPravTarif51427 = new RaschsvPravTarif51427()
            raschsvPravTarif51427.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            raschsvPravTarif51427.doh346_15vs = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF5_1_427_DOH346_15VS])
            raschsvPravTarif51427.doh6_427 = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF5_1_427_DOH6_427])
            raschsvPravTarif51427.dolDoh6_427 = getDouble(obyazPlatSvChildNode.attributes()[PRAV_TARIF5_1_427_DOL_DOH6_427])

            raschsvPravTarif51427Service.insertRaschsvPravTarif51427(raschsvPravTarif51427)
            testCntNodePravTarif51427++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_PRAV_TARIF7_1_427) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла ПравТариф7.1.427
            //----------------------------------------------------------------------------------------------------------
            RaschsvPravTarif71427 raschsvPravTarif71427 = new RaschsvPravTarif71427()
            raschsvPravTarif71427.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            raschsvPravTarif71427.dohVsPred = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_VS_PRED])
            raschsvPravTarif71427.dohVsPer = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_VS_PER])
            raschsvPravTarif71427.dohCelPostPred = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_CEL_POST_PRED])
            raschsvPravTarif71427.dohCelPostPer = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_CEL_POST_PER])
            raschsvPravTarif71427.dohGrantPred = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_GRANT_PRED])
            raschsvPravTarif71427.dohGrantPer = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_GRANT_PER])
            raschsvPravTarif71427.dohEkDeyatPred = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_EK_DEYAT_PRED])
            raschsvPravTarif71427.dohEkDeyatPer = getLong(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOH_EK_DEYAT_PER])
            raschsvPravTarif71427.dolDohPred = getDouble(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOL_DOH_PRED])
            raschsvPravTarif71427.dolDohPer = getDouble(obyazPlatSvChildNode.attributes()[PRAV_TARIF7_1_427_DOL_DOH_PER])

            raschsvPravTarif71427Service.insertRaschsvPravTarif71427(raschsvPravTarif71427)
            testCntNodePravTarif71427++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_SV_PRIM_TARIF9_1_427) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла СвПримТариф9.1.427
            //----------------------------------------------------------------------------------------------------------
            RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 = new RaschsvSvPrimTarif91427()
            raschsvSvPrimTarif91427.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            // Итого выплат
            RaschsvVyplatIt427 raschsvVyplatIt427 = new RaschsvVyplatIt427()

            // Набор сведений о патентах
            def raschsvSvedPatentList = []

            obyazPlatSvChildNode.childNodes().each { svPrimTarif91427ChildNode ->
                if (svPrimTarif91427ChildNode.name == NODE_NAME_VYPLAT_IT) {
                    // Разбор узла ВыплатИт
                    raschsvVyplatIt427.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(svPrimTarif91427ChildNode)

                } else if (svPrimTarif91427ChildNode.name == NODE_NAME_SVED_PATENT) {
                    // Разбор узла СведПатент
                    RaschsvSvedPatent raschsvSvedPatent = new RaschsvSvedPatent()
                    raschsvSvedPatent.nomPatent = svPrimTarif91427ChildNode.attributes()[SVED_PATENT_NOM_PATENT]
                    raschsvSvedPatent.vydDeyatPatent = svPrimTarif91427ChildNode.attributes()[SVED_PATENT_VYD_DEYAT_PATENT]
                    raschsvSvedPatent.dataNachDeyst = getDate(svPrimTarif91427ChildNode.attributes()[SVED_PATENT_DATA_NACH_DEYST])
                    raschsvSvedPatent.dataKonDeyst = getDate(svPrimTarif91427ChildNode.attributes()[SVED_PATENT_DATA_KON_DEYST])

                    svPrimTarif91427ChildNode.childNodes().each { sumVyplatNode ->
                        // Разбор узла СумВыплат
                        raschsvSvedPatent.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(sumVyplatNode)
                    }
                    raschsvSvedPatentList.add(raschsvSvedPatent)
                    testCntNodeSvedPatent++
                }
            }
            raschsvSvPrimTarif91427.raschsvVyplatIt427 = raschsvVyplatIt427
            raschsvSvPrimTarif91427.raschsvSvedPatentList = raschsvSvedPatentList

            raschsvSvPrimTarif91427Service.insertRaschsvSvPrimTarif91427(raschsvSvPrimTarif91427)
            testCntNodePravTarif91427++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_SV_PRIM_TARIF2_2_425) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла СвПримТариф2.2.425
            //----------------------------------------------------------------------------------------------------------

            RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 = new RaschsvSvPrimTarif22425()
            raschsvSvPrimTarif22425.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            // Итого выплат
            RaschsvVyplatIt425 raschsvVyplatIt425 = new RaschsvVyplatIt425()

            // Сведения об иностранных гражданах, лицах без гражданства
            def raschsvSvInoGrazdList = []

            obyazPlatSvChildNode.childNodes().each { svPrimTarif22425ChildNode ->
                if (svPrimTarif22425ChildNode.name == NODE_NAME_VYPLAT_IT) {
                    // Разбор узла ВыплатИт
                    raschsvVyplatIt425.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(svPrimTarif22425ChildNode)

                } else if (svPrimTarif22425ChildNode.name == NODE_NAME_SV_INO_GRAZD) {
                    // Разбор узла СвИноГражд
                    RaschsvSvInoGrazd raschsvSvInoGrazd = new RaschsvSvInoGrazd()
                    raschsvSvInoGrazd.innfl = svPrimTarif22425ChildNode.attributes()[SV_INO_GRAZD_INNFL]
                    raschsvSvInoGrazd.snils = svPrimTarif22425ChildNode.attributes()[SV_INO_GRAZD_SNILS]
                    raschsvSvInoGrazd.grazd = svPrimTarif22425ChildNode.attributes()[SV_INO_GRAZD_GRAZD]

                    svPrimTarif22425ChildNode.childNodes().each { svPrimTarif22425ChildChildNode ->
                        if (svPrimTarif22425ChildChildNode.name == NODE_NAME_SUM_VYPLAT) {
                            // Разбор узла СумВыплат
                            raschsvSvInoGrazd.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(svPrimTarif22425ChildChildNode)
                        } else if (svPrimTarif22425ChildChildNode.name == NODE_NAME_FIO) {
                            // Разбор узла ФИО
                            raschsvSvInoGrazd.familia = svPrimTarif22425ChildChildNode.attributes()[FIO_FAMILIA]
                            raschsvSvInoGrazd.imya = svPrimTarif22425ChildChildNode.attributes()[FIO_IMYA]
                            raschsvSvInoGrazd.otchestvo = svPrimTarif22425ChildChildNode.attributes()[FIO_OTCHESTVO_NAME]
                        }
                    }
                    raschsvSvInoGrazdList.add(raschsvSvInoGrazd)
                    testCntNodeSvInoGrazd++
                }
            }
            raschsvSvPrimTarif22425.raschsvVyplatIt425 = raschsvVyplatIt425
            raschsvSvPrimTarif22425.raschsvSvInoGrazdList = raschsvSvInoGrazdList

            raschsvSvPrimTarif22425Service.insertRaschsvSvPrimTarif22425(raschsvSvPrimTarif22425)
            testCntNodePravTarif22425++

        } else if (obyazPlatSvChildNode.name == NODE_NAME_SV_PRIM_TARIF1_3_422) {
            //----------------------------------------------------------------------------------------------------------
            // Разбор узла СвПримТариф1.3.422
            //----------------------------------------------------------------------------------------------------------
            RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 = new RaschsvSvPrimTarif13422()
            raschsvSvPrimTarif13422.raschsvObyazPlatSvId = raschsvObyazPlatSvId

            // Итого выплат
            RaschsvVyplatIt422 raschsvVyplatIt422 = new RaschsvVyplatIt422()

            // Сведения об обучающихся
            def raschsvSvedObuchList = []

            obyazPlatSvChildNode.childNodes().each { svPrimTarif13422ChildNode ->
                if (svPrimTarif13422ChildNode.name == NODE_NAME_VYPLAT_IT) {
                    // Разбор узла ВыплатИт
                    raschsvVyplatIt422.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(svPrimTarif13422ChildNode)

                } else if (svPrimTarif13422ChildNode.name == NODE_NAME_SVED_OBUCH) {
                    // Разбор узла СведОбуч
                    RaschsvSvedObuch raschsvSvedObuch = new RaschsvSvedObuch()
                    raschsvSvedObuch.unikNomer = svPrimTarif13422ChildNode.attributes()[SVED_OBUCH_UNIK_NOMER]

                    // Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой
                    def raschsvSvReestrMdoList = []

                    svPrimTarif13422ChildNode.childNodes().each { svPrimTarif13422ChildChildNode ->
                        if (svPrimTarif13422ChildChildNode.name == NODE_NAME_SUM_VYPLAT) {
                            // Разбор узла СумВыплат
                            raschsvSvedObuch.raschsvSvSum1Tip = parseRaschsvSvSum1Tip(svPrimTarif13422ChildChildNode)
                        } else if (svPrimTarif13422ChildChildNode.name == NODE_NAME_FIO) {
                            // Разбор узла ФИО
                            raschsvSvedObuch.familia = svPrimTarif13422ChildChildNode.attributes()[FIO_FAMILIA]
                            raschsvSvedObuch.imya = svPrimTarif13422ChildChildNode.attributes()[FIO_IMYA]
                            raschsvSvedObuch.otchestvo = svPrimTarif13422ChildChildNode.attributes()[FIO_OTCHESTVO_NAME]
                        } else if (svPrimTarif13422ChildChildNode.name == NODE_NAME_SPRAV_STUD_OTRYAD ||
                                svPrimTarif13422ChildChildNode.name == NODE_NAME_SPRAV_FORM_OBUCH) {
                            // Разбор узлов СправСтудОтряд и СправФормОбуч
                            raschsvSvedObuch.spravNomer = svPrimTarif13422ChildChildNode.attributes()[SPRAV_NOMER]
                            raschsvSvedObuch.spravData = getDate(svPrimTarif13422ChildChildNode.attributes()[SPRAV_DATA])
                            raschsvSvedObuch.spravNodeName = svPrimTarif13422ChildChildNode.name
                        } else if (svPrimTarif13422ChildChildNode.name == NODE_NAME_SV_REESTR_MDO) {
                            // Разбор узла СвРеестрМДО
                            RaschsvSvReestrMdo raschsvSvReestrMdo = new RaschsvSvReestrMdo()
                            raschsvSvReestrMdo.naimMdo = svPrimTarif13422ChildChildNode.attributes()[SV_REESTR_MDO_NAIM_MDO]
                            raschsvSvReestrMdo.dataZapis = getDate(svPrimTarif13422ChildChildNode.attributes()[SV_REESTR_MDO_DATA_ZAPIS])
                            raschsvSvReestrMdo.nomerZapis = svPrimTarif13422ChildChildNode.attributes()[SV_REESTR_MDO_NOMER_ZAPIS]

                            raschsvSvReestrMdoList.add(raschsvSvReestrMdo)
                        }
                    }
                    raschsvSvedObuch.raschsvSvReestrMdoList = raschsvSvReestrMdoList

                    raschsvSvedObuchList.add(raschsvSvedObuch)
                    testCntNodeSvObuch++
                }
            }
            raschsvSvPrimTarif13422.raschsvVyplatIt422 = raschsvVyplatIt422
            raschsvSvPrimTarif13422.raschsvSvedObuchList = raschsvSvedObuchList

            raschsvSvPrimTarif13422Service.insertRaschsvSvPrimTarif13422(raschsvSvPrimTarif13422)
            testCntNodePravTarif13422++
        }
    }

    // Сохранение УплПер
    if (raschsvUplPerList.size() > 0) {
        raschsvUplPerService.insertUplPer(raschsvUplPerList)
    }

    // Сохранение РасчСВ_ОПС_ОМС
    if (raschsvSvOpsOmsList.size() > 0) {
        raschsvSvOpsOmsService.insertRaschsvSvOpsOms(raschsvSvOpsOmsList)
    }

    return raschsvObyazPlatSvId
}

/**
 * Разбор узла УплСВПревТип
 * @param uplSvPrevNode
 * @return
 */
RaschsvUplSvPrev parseRaschsvUplSvPrev(Object uplSvPrevNode) {
    RaschsvUplSvPrev raschsvUplSvPrev = new RaschsvUplSvPrev()
    raschsvUplSvPrev.nodeName = uplSvPrevNode.name
    raschsvUplSvPrev.priznak = uplSvPrevNode.attributes()[UPL_SV_PREV_PRIZNAK]
    raschsvUplSvPrev.svSum = getDouble(uplSvPrevNode.attributes()[UPL_SV_PREV_SUMMA])

    return raschsvUplSvPrev
}

/**
 * Разбор узла СвСум1Тип
 * @param svSum1TipNode
 * @return
 */
RaschsvSvSum1Tip parseRaschsvSvSum1Tip(Object svSum1TipNode) {
    RaschsvSvSum1Tip raschsvSvSum1Tip = new RaschsvSvSum1Tip()
    raschsvSvSum1Tip.sumVsegoPer = getDouble(svSum1TipNode.attributes()[SV_SUM_1TIP_SUM_VSEGO_PER])
    raschsvSvSum1Tip.sumVsegoPosl3m = getDouble(svSum1TipNode.attributes()[SV_SUM_1TIP_SUM_VSEGO_POSL3M])
    raschsvSvSum1Tip.sum1mPosl3m = getDouble(svSum1TipNode.attributes()[SV_SUM_1TIP_SUM1_POSL3M])
    raschsvSvSum1Tip.sum2mPosl3m = getDouble(svSum1TipNode.attributes()[SV_SUM_1TIP_SUM2_POSL3M])
    raschsvSvSum1Tip.sum3mPosl3m = getDouble(svSum1TipNode.attributes()[SV_SUM_1TIP_SUM3_POSL3M])

    return raschsvSvSum1Tip
}

/**
 * Разбор узла КолЛицТип
 * @param kolLicTip
 * @return
 */
RaschsvKolLicTip parseRaschsvKolLicTip(Object kolLicTip) {
    RaschsvKolLicTip raschsvKolLicTip = new RaschsvKolLicTip()
    raschsvKolLicTip.kolVsegoPer = getInteger(kolLicTip.attributes()[KOL_LIC_TIP_KOL_VSEGO_PER])
    raschsvKolLicTip.kolVsegoPosl3m = getInteger(kolLicTip.attributes()[KOL_LIC_TIP_KOL_VSEGO_POSL3M])
    raschsvKolLicTip.kol1mPosl3m = getInteger(kolLicTip.attributes()[KOL_LIC_TIP_KOL1_POSL3M])
    raschsvKolLicTip.kol2mPosl3m = getInteger(kolLicTip.attributes()[KOL_LIC_TIP_KOL2_POSL3M])
    raschsvKolLicTip.kol3mPosl3m = getInteger(kolLicTip.attributes()[KOL_LIC_TIP_KOL3_POSL3M])

    return raschsvKolLicTip
}

/**
 * Разбор узла ПерсСвСтрахЛиц
 * @param persSvStrahLicNode - узел ПерсСвСтрахЛиц
 * @param declarationDataId - идентификатор декларации для которой загружаются данные
 * @return
 */
RaschsvPersSvStrahLic parseRaschsvPersSvStrahLic(Object persSvStrahLicNode, Long declarationDataId) {
    RaschsvPersSvStrahLic raschsvPersSvStrahLic = new RaschsvPersSvStrahLic()
    raschsvPersSvStrahLic.declarationDataId = declarationDataId

    raschsvPersSvStrahLic.nomKorr = getInteger(persSvStrahLicNode.attributes()[PERV_SV_STRAH_LIC_NOM_KORR])
    raschsvPersSvStrahLic.period = persSvStrahLicNode.attributes()[PERV_SV_STRAH_LIC_PERIOD]
    raschsvPersSvStrahLic.otchetGod = persSvStrahLicNode.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]
    raschsvPersSvStrahLic.nomer = getInteger(persSvStrahLicNode.attributes()[PERV_SV_STRAH_LIC_NOMER])
    raschsvPersSvStrahLic.svData = getDate(persSvStrahLicNode.attributes()[PERV_SV_STRAH_LIC_SV_DATA])

    persSvStrahLicNode.childNodes().each { persSvStrahLicChildNode ->
        if (persSvStrahLicChildNode.name == NODE_NAME_DAN_FL_POLUCH) {
            // Разбор узла ДанФЛПолуч
            raschsvPersSvStrahLic.innfl = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_INNFL]
            raschsvPersSvStrahLic.snils = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_SNILS]
            raschsvPersSvStrahLic.dataRozd = getDate(persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_DATA_ROZD])
            raschsvPersSvStrahLic.grazd = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_GRAZD]
            raschsvPersSvStrahLic.pol = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_POL]
            raschsvPersSvStrahLic.kodVidDoc = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_KOD_VID_DOC]
            raschsvPersSvStrahLic.serNomDoc = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_SER_NOM_DOC]
            raschsvPersSvStrahLic.prizOps = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_PRIZ_OPS]
            raschsvPersSvStrahLic.prizOms = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_PRIZ_OMS]
            raschsvPersSvStrahLic.prizOss = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_PRIZ_OSS]
            persSvStrahLicChildNode.childNodes().each { fioNode ->
                if (fioNode.name == NODE_NAME_FIO) {
                    // Разбор узла ФИО
                    raschsvPersSvStrahLic.familia = fioNode.attributes()[FIO_FAMILIA]
                    raschsvPersSvStrahLic.imya = fioNode.attributes()[FIO_IMYA]
                    raschsvPersSvStrahLic.otchestvo = fioNode.attributes()[FIO_OTCHESTVO_NAME]
                }
            }
        } else if (persSvStrahLicChildNode.name == NODE_NAME_SV_VYPL_SVOPS) {
            // Разбор узла СвВыплСВОПС
            persSvStrahLicChildNode.childNodes().each { svVyplSvopsChildNode ->
                if (svVyplSvopsChildNode.name == NODE_NAME_SV_VYPL) {
                    // Разбор узла СвВыпл
                    RaschsvSvVypl raschsvSvVypl = new RaschsvSvVypl()
                    raschsvSvVypl.sumVyplVs3 = getDouble(svVyplSvopsChildNode.attributes()[SV_VYPL_SUM_VYPL_VS3])
                    raschsvSvVypl.vyplOpsVs3 = getDouble(svVyplSvopsChildNode.attributes()[SV_VYPL_VYPL_OPS_VS3])
                    raschsvSvVypl.vyplOpsDogVs3 = getDouble(svVyplSvopsChildNode.attributes()[SV_VYPL_VYPL_OPS_DOG_VS3])
                    raschsvSvVypl.nachislSvVs3 = getDouble(svVyplSvopsChildNode.attributes()[SV_VYPL_NACHISL_SV_VS3])

                    // Набор объектов СвВыплМК
                    def raschsvSvVyplMkList = []
                    svVyplSvopsChildNode.childNodes().each { svVyplMkNode ->
                        if (svVyplMkNode.name == NODE_NAME_SV_VYPL_MK) {
                            // Разбор узла СвВыплМК
                            RaschsvSvVyplMk raschsvSvVyplMk = new RaschsvSvVyplMk()
                            raschsvSvVyplMk.mesyac = svVyplMkNode.attributes()[SV_VYPL_MT_MESYAC]
                            raschsvSvVyplMk.kodKatLic = svVyplMkNode.attributes()[SV_VYPL_MT_KOD_KAT_LIC]
                            raschsvSvVyplMk.sumVypl = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_SUM_VYPL])
                            raschsvSvVyplMk.vyplOps = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_VYPL_OPS])
                            raschsvSvVyplMk.vyplOpsDog = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_VYPL_OPS_DOG])
                            raschsvSvVyplMk.nachislSv = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_NACHISL_SV])

                            raschsvSvVyplMkList.add(raschsvSvVyplMk)
                        }
                    }
                    raschsvSvVypl.raschsvSvVyplMkList = raschsvSvVyplMkList
                    raschsvPersSvStrahLic.raschsvSvVypl = raschsvSvVypl

                } else if (svVyplSvopsChildNode.name == NODE_NAME_VYPL_SV_DOP) {
                    // Разбор узла ВыплСВДоп
                    RaschsvVyplSvDop raschsvVyplSvDop = new RaschsvVyplSvDop()
                    raschsvVyplSvDop.vyplSvVs3 = getDouble(svVyplSvopsChildNode.attributes()[VYPL_SV_DOP_VYPL_SV_VS3])
                    raschsvVyplSvDop.nachislSvVs3 = getDouble(svVyplSvopsChildNode.attributes()[VYPL_SV_DOP_NACHISL_SV_VS3])

                    // Набор объектов ВыплСВДопМТ
                    def raschsvVyplSvDopMtList = []
                    svVyplSvopsChildNode.childNodes().each { vyplSvDopMtNode ->
                        if (vyplSvDopMtNode.name == NODE_NAME_VYPL_SV_DOP_MT) {
                            // Разбор узла ВыплСВДопМТ
                            RaschsvVyplSvDopMt raschsvVyplSvDopMt = new RaschsvVyplSvDopMt()
                            raschsvVyplSvDopMt.mesyac = vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_MESYAC]
                            raschsvVyplSvDopMt.tarif = vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_TARIF]
                            raschsvVyplSvDopMt.vyplSv = getDouble(vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_VYPL_SV])
                            raschsvVyplSvDopMt.nachislSv = getDouble(vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_NACHISL_SV])

                            raschsvVyplSvDopMtList.add(raschsvVyplSvDopMt)
                        }
                    }
                    raschsvVyplSvDop.raschsvVyplSvDopMtList = raschsvVyplSvDopMtList
                    raschsvPersSvStrahLic.raschsvVyplSvDop = raschsvVyplSvDop
                }
            }
        }
    }

    return raschsvPersSvStrahLic
}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Обработка события CALCULATE
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

def calcTimeMillis(long time){
    long currTime = System.currentTimeMillis();
    return " (" + (currTime - time) + " ms)";
}

def calculateData() {

    long timeFull = System.currentTimeMillis();
    long time = System.currentTimeMillis();

    logger.info("Начало расчета ПНФ");

    //выставляем параметр что скрипт не формирует новый xml-файл
    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

    List<RaschsvPersSvStrahLic> declarationFormPersonList = raschsvPersSvStrahLicService.findPersons(declarationData.id)

    logger.info("В ПНФ номер "+ declarationData.id + " найдено записей о физ.лицах: " + declarationFormPersonList.size() +" "+ calcTimeMillis(time));

    time = System.currentTimeMillis();

    Date actualVersion = new Date();
    Map<Long, List<PersonData>> refbookPersonData = refBookPersonService.findRefBookPersonByPrimary1151111(declarationData.id, null, actualVersion)

    //Два списка для создания новых записей и для обновления существующих
    List<PersonData> createdPersonList = new ArrayList<PersonData>();
    List<PersonData> updatedPersonList = new ArrayList<PersonData>();
    Map<Long, RaschsvPersSvStrahLic> resultMap = new HashMap<Long, RaschsvPersSvStrahLic>();
    for (RaschsvPersSvStrahLic declarationFormPerson : declarationFormPersonList) {

        PersonData personData = createPersonData(declarationFormPerson);

        List<PersonData> refBookPersonList = refbookPersonData.get(declarationFormPerson.id);
        Long refBookPersonId = refBookPersonService.identificatePerson(personData, refBookPersonList, SIMILARITY_THRESHOLD, new Logger());

        declarationFormPerson.setPersonId(refBookPersonId)

        //после идентификации выставим ссылку на запись справочника
        personData.setRefBookPersonId(refBookPersonId)
        personData.setSourceId(declarationFormPerson.getId());

        if (refBookPersonId != null) {
            //обновление записи
            updatedPersonList.add(personData);
        } else {
            //Новые записи помещаем в список для пакетного создания
            createdPersonList.add(personData);
        }

        resultMap.put(declarationFormPerson.getId(), declarationFormPerson);
    }

    logger.info("Идентификация завершена. Подготовленно записей для создания: " + createdPersonList.size() + ", подготовленно записей для обновления: " + updatedPersonList.size()+" "+ calcTimeMillis(time));
    time = System.currentTimeMillis();

    //Создание справочников
    if (!createdPersonList.isEmpty()) {
        createRefbookPersonData(createdPersonList, null);
    }

    println "create " + (System.currentTimeMillis() - time);
    logger.info("create: (" + (System.currentTimeMillis() - time) + " ms)");
    time = System.currentTimeMillis();

    //обновление ссылок на записи справочника
    updateReferenceToPersonId(resultMap, createdPersonList);
    updateReferenceToPersonId(resultMap, updatedPersonList);
    //сохранение обновленных ссылок
    raschsvPersSvStrahLicService.updatePersSvStrahLic(new ArrayList<RaschsvPersSvStrahLic>(resultMap.values()))

    println "update " + (System.currentTimeMillis() - time);
    logger.info("update: (" + (System.currentTimeMillis() - time) + " ms)");
    time = System.currentTimeMillis();

    //Обновление справочников
    if (!updatedPersonList.isEmpty()) {
        updateRefbookPersonData(updatedPersonList, null);
    }

    println "refresh " + (System.currentTimeMillis() - time);
    logger.info("refresh: (" + (System.currentTimeMillis() - time) + " ms)");


    logger.info("Завершение расчета ПНФ "+" "+ calcTimeMillis(timeFull));

}
//---------------- identification ----------------

/**
 * Обноаляет ссылки на справочник ФЛ
 * @param resultMap
 * @param personDataList
 */
def updateReferenceToPersonId(resultMap, List<PersonData> personDataList) {
    for (PersonData personData : personDataList) {
        resultMap.get(personData.getSourceId()).setPersonId(personData.getRefBookPersonId());
    }
}

/**
 * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
 * @param personMap
 * @return
 */
def getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    def addressIds = [];
    personMap.each { personId, person ->
        if (person.get(RF_ADDRESS).value != null) {
            addressIds.add(person.get(RF_ADDRESS).value)
        }
    }

    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
    if (refBookMap != null && !addressIds.isEmpty()) {
        return refBookMap;
    } else {
        return Collections.emptyMap();
    }

}

/**
 * Получить "Физические лица" getRefPersons
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersonsByDeclarationDataId() {
    Long declarationDataId = declarationData.id;
    String whereClause = String.format("id in(select person_id from raschsv_pers_sv_strah_lic where declaration_data_id = %s)", declarationDataId)
    return getRefBookByRecordWhere(RefBook.Id.PERSON.id, whereClause)
}

//Приоритет документов удостоверяющих личность <Идентификатор, Приоритет(int)>
@Field def documentPriorityCache = [:]

def getRefDocumentPriority() {
    if (documentPriorityCache.size() == 0) {
        def refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.id)
        refBookList.each { refBook ->
            documentPriorityCache.put(refBook?.id?.numberValue, refBook?.PRIORITY?.numberValue?.intValue())
        }
    }
    return documentPriorityCache;
}

@Field Map<Long, List<Map<String, RefBookValue>>> inpPersonRefBookCache = [:]

def getRefInpMapByDeclarationDataId() {

    if (inpPersonRefBookCache.isEmpty()) {
        Long declarationDataId = declarationData.id;
        String whereClause = String.format("person_id in(select person_id from raschsv_pers_sv_strah_lic where declaration_data_id = %s)", declarationDataId)
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_TAX_PAYER.id, whereClause)
        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            def inpList = inpPersonRefBookCache.get(refBookPersonId);
            if (inpList == null) {
                inpList = [];
                inpPersonRefBookCache.put(refBookPersonId, inpList)
            }
            inpList.add(refBookValues);
        }
    }
    return inpPersonRefBookCache;
}



def updateRefbookPersonData(List<PersonData> personList, Long asnuId) {

    Date versionFrom = getVersionFrom();

    //-----<INITIALIZE_CACHE_DATA>-----
    //PersonId : Физлица
    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefPersonsByDeclarationDataId();

    println "find refBook person="+refBookPerson

    //PersonId :  UniqId:Документы
    Map<Long, List<Map<String, RefBookValue>>> identityDocMap = getRefDulByDeclarationDataId()


    println "find identityDocMap="+identityDocMap

    //-----<INITIALIZE_CACHE_DATA_END>-----

    int updCnt = 0;
    for (PersonData person : personList) {
        def personId = person.getRefBookPersonId();

        Map<String, RefBookValue> refBookPersonValues = refBookPerson.get(personId);

        AttrCounter personAttrCnt = new AttrCounter();

        updatePersonRecord(refBookPersonValues, person, asnuId, null, personAttrCnt);

        if (personAttrCnt.isUpdate()) {
            getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, personId, versionFrom, null, refBookPersonValues);
        }

        AttrCounter documentAttrCnt = new AttrCounter();
        //Обновление списка документов
        //Проверка, если задан номер и тип документа
        if (person.getDocumentNumber() != null && !person.getDocumentNumber().isEmpty() && person.getDocumentTypeCode() != null && !person.getDocumentTypeCode().isEmpty()) {
            updateIdentityDocRecords(identityDocMap.get(personId), person, documentAttrCnt);
        }

        if (personAttrCnt.isUpdate() || documentAttrCnt.isUpdate()) {

            def recordId = refBookPersonValues.get("RECORD_ID")?.getNumberValue()?.longValue();
            logger.info(String.format("Обновлена запись в справочнике 'Физические лица': %d, %s %s %s", recordId,
                    person.getLastName(),
                    person.getFirstName(),
                    person.getMiddleName()) + " " + buildRefreshNotice(addressAttrCnt, personAttrCnt, documentAttrCnt, taxpayerIdentityAttrCnt));
            updCnt++;
        }
    }

    logger.info("Обновлено записей: " + updCnt);

}

def buildRefreshNotice(AttrCounter addressAttrCnt, AttrCounter personAttrCnt, AttrCounter documentAttrCnt, AttrCounter taxpayerIdentityAttrCnt) {
    StringBuffer sb = new StringBuffer();
    appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb);
    appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_DOC.getId(), documentAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_TAX_PAYER.getId(), taxpayerIdentityAttrCnt, sb);
    return sb.toString();
}

@Field HashMap<Long, RefBook> mapRefBookToIdCache = new HashMap<Long, RefBook>();
def getRefBookFromCache(Long id){
    RefBook refBook = mapRefBookToIdCache.get(id);
    if (refBook != null){
        return refBook;
    } else {
        refBook = refBookFactory.get(id);
        mapRefBookToIdCache.put(id, refBook);
        return refBook;
    }
}

@Field Map<Long, Map<String, String>> refBookAttrCache = new HashMap<Long, Map<String, String>>();
def getAttrNameFromRefBook(Long id, String alias){
    Map<String, String> attrMap = refBookAttrCache.get(id);
    if (attrMap != null){
        return attrMap.get(alias);
    } else {
        attrMap = new HashMap<String, String>();
        RefBook refBook = getRefBookFromCache(id);
        List<RefBookAttribute> refBookAttributeList = refBook.getAttributes();
        for (RefBookAttribute attr: refBookAttributeList){
            attrMap.put(attr.getAlias(), attr.getName());
        }
        refBookAttrCache.put(id, attrMap);
        return attrMap.get(alias);
    }
}

def appendAttrInfo(Long refBookId, AttrCounter attrCounter, StringBuffer sb) {

    if (attrCounter != null && attrCounter.isUpdate()) {

        List<String> msgList = new ArrayList<String>();

        for (Map.Entry<String, String> msgEntry: attrCounter.getMessages()){
            String aliasKey = msgEntry.getKey();
            String msg = msgEntry.getValue();
            msgList.add(new StringBuffer(getAttrNameFromRefBook(refBookId, aliasKey)).append(": ").append(msg).toString())
        }

        if (!msgList.isEmpty()){
            sb.append(Arrays.toString(msgList.toArray()));
        }

    }
}

/**
 * По списку
 * @param personList
 * @return
 */
def createRefbookPersonData(List<PersonData> personList, Long asnuId) {


    //создание записей справочника физлиц
    List<RefBookRecord> personRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        Long addressId = null;
        PersonData person = personList.get(i)
        RefBookRecord refBookRecord = createPersonRecord(person, asnuId, null, new EmptyChangedListener());
        personRecords.add(refBookRecord);
    }

    //сгенерированные идентификаторы справочника физлиц
    List<Long> personIds = getProvider(RefBook.Id.PERSON.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, personRecords)

    //создание записей справочников документы и идентфикаторы физлиц
    List<RefBookRecord> documentsRecords = new ArrayList<RefBookRecord>()

    for (int i = 0; i < personList.size(); i++) {
        PersonData person = personList.get(i)
        Long generatedId = personIds.get(i);
        person.setRefBookPersonId(generatedId); // выставляем присвоенный Id
        documentsRecords.add(createIdentityDocRecord(person, new EmptyChangedListener()));

    }

    List<Long> docIds = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, documentsRecords)

    //Выводим информацию о созданных записях
    for (int i = 0; i < personList.size(); i++) {
        Long personId = personIds.get(i);
        RefBookRecord personRecord = personRecords.get(i);
        //RefBookRecord documentsRecord = documentsRecords.get(i);
        Map<String, RefBookValue> personValues = personRecord.getValues()

        String noticeMsg = String.format("Создана новая запись в справочнике 'Физические лица': %d, %s %s %s",
                personId,
                personValues.get("LAST_NAME")?.getStringValue(),
                personValues.get("FIRST_NAME")?.getStringValue(),
                (personValues.get("MIDDLE_NAME")?.getStringValue() ?: ""));
        logger.info(noticeMsg);
    }


    logger.info("В справочнике 'Физические лица' создано записей: " + personIds.size());
    logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size());




}

/**
 * Создание новой записи справочника адреса физлиц
 * @param person
 * @return
 */
RefBookRecord creatAddressRecord(PersonData person, AttributeChangeListener attributeChangeListener) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillAddressAttr(values, person, attributeChangeListener);
    record.setValues(values);
    return record;
}

/**
 * Заполнение записи справочника адреса физлиц
 * @param values
 * @param person
 * @return
 */
def fillAddressAttr(Map<String, RefBookValue> values, PersonData person, AttributeChangeListener attributeChangeListener) {

    int addressType = person.getAddressIno() != null && !person.getAddressIno().isEmpty() ? 1 : 0;
    //Тип адреса. Значения: 0 - в РФ 1 - вне РФ
    Long countryId = findCountryId(person.getCountryCode())  //код страны проживания не РФ

    putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, addressType, attributeChangeListener);
    putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, countryId, attributeChangeListener);
    putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, person.getRegionCode(), attributeChangeListener);
    putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, person.getDistrict(), attributeChangeListener);
    putOrUpdate(values, "CITY", RefBookAttributeType.STRING, person.getCity(), attributeChangeListener);
    putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, person.getLocality(), attributeChangeListener);
    putOrUpdate(values, "STREET", RefBookAttributeType.STRING, person.getStreet(), attributeChangeListener);
    putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, person.getHouse(), attributeChangeListener);
    putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, person.getBuild(), attributeChangeListener);
    putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, person.getAppartment(), attributeChangeListener);
    putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, person.getPostalCode(), attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, person.getAddressIno(), attributeChangeListener);
}

/**
 * Создание новой записи справочника физлиц
 * @param person класс предоставляющий данные для заполнения справочника
 * @param asnuId идентификатор АСНУ в справочнике АСНУ
 * @return запись справочника
 */
RefBookRecord createPersonRecord(PersonData person, Long asnuId, Long addressId, AttributeChangeListener attributeChangeListener) {
    RefBookRecord refBookRecord = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillPersonAttr(values, person, asnuId, addressId, attributeChangeListener);
    refBookRecord.setValues(values);
    return refBookRecord;
}

/**
 * Обновление атрибутов записи справочника физлиц
 * @param values
 * @param person
 * @param asnuId
 * @param addressId
 * @return
 */
def updatePersonRecord(Map<String, RefBookValue> values, PersonData person, Long asnuId, Long addressId, AttributeChangeListener attributeChangeListener) {
    fillPersonAttr(values, person, asnuId, addressId, attributeChangeListener);
}

/**
 * Заполнение аттрибутов справочника физлиц
 * @param values карта для хранения значений атрибутов
 * @param person класс предоставляющий данные для заполнения справочника
 * @param asnuId ссылка на справочник АСНУ
 * @param addressId ссылка на справочник адреса физлиц
 * @return
 */
def fillPersonAttr(Map<String, RefBookValue> values, PersonData person, Long asnuId, Long addressId, AttributeChangeListener attributeChangeListener) {

    Long countryId = findCountryId(person.getCitizenship());
    Long statusId = null;

    putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), attributeChangeListener);
    putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), attributeChangeListener);
    putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), attributeChangeListener);
    putOrUpdate(values, "SEX", RefBookAttributeType.NUMBER, person.getSex() ?: null, attributeChangeListener);
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), attributeChangeListener);
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), attributeChangeListener);
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), attributeChangeListener);
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, null, attributeChangeListener);
    putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), attributeChangeListener);
    putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null, attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, addressId, attributeChangeListener);
    putOrUpdate(values, "PENSION", RefBookAttributeType.NUMBER, person.getPension(), attributeChangeListener);
    putOrUpdate(values, "MEDICAL", RefBookAttributeType.NUMBER, person.getMedical(), attributeChangeListener);
    putOrUpdate(values, "SOCIAL", RefBookAttributeType.NUMBER, person.getSocial(), attributeChangeListener);
    putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee(), attributeChangeListener);
    putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, countryId, attributeChangeListener);
    putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, statusId, attributeChangeListener);
    putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, asnuId, attributeChangeListener);
    putOrUpdate(values, "OLD_ID", RefBookAttributeType.REFERENCE, null, attributeChangeListener);

}

/**
 * Документы, удостоверяющие личность
 */
RefBookRecord createIdentityDocRecord(PersonData person, AttributeChangeListener attributeChangeListener) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillIdentityDocAttr(values, person, attributeChangeListener);
    record.setValues(values);
    return record;
}


def updateIdentityDocRecords(List<Map<String, RefBookValue>> identityDocRefBook, PersonData person, AttrCounter attrCounter) {

    Map<Long, String> docCodes = getRefDocument()

    //Идентификатор типа документа
    Long docTypeId = docCodes.find { it.value == person.getDocumentTypeCode() }?.key;

    if (docTypeId != null) {

        //Ищем документ с таким же типом
        Map<String, RefBookValue> findedDoc = identityDocRefBook?.find {
            Long docIdRef = it.get("DOC_ID")?.getReferenceValue();
            String docNumber = it.get("DOC_NUMBER")?.getStringValue();
            docTypeId.equals(docIdRef) && person.getDocumentNumber()?.equalsIgnoreCase(docNumber);
        };

        List<Map<String, RefBookValue>> identityDocRecords = new ArrayList<Map<String, RefBookValue>>();

        if (findedDoc != null) {
            //документ с таким типом и номером существует, ничего не делаем
            //return;
        } else {
            RefBookRecord refBookRecord = createIdentityDocRecord(person, attrCounter);
            List<Long> ids = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, getVersionFrom(), null, Arrays.asList(refBookRecord));

            //выставляем присвоеный ID и добавляем в общий список для выставления приоритетов
            Map<String, RefBookValue> values = refBookRecord.getValues();
            values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, ids.first()));
            identityDocRecords.add(values);
        }

        //Добавляем существующие документы если есть
        if (identityDocRefBook != null && !identityDocRefBook.isEmpty()) {
            identityDocRecords.addAll(identityDocRefBook);
        }

        List<Map<String, RefBookValue>> actualDocumentsList = updatePriority(identityDocRecords);

        //Обновление признака включается в отчетность
        for (int i = 0; i < identityDocRecords.size(); i++) {
            //небольшой баг, предполагалось что будет два списка для сравнения измененных значений вывода в логах, но в этом случае надо копировать и карты в этих списках. Поэтому сейчас смена приоритета в логах не отображается.
            Map<String, RefBookValue> identityDocsValues = identityDocRecords.get(i);
            Map<String, RefBookValue> actualDocsValues = actualDocumentsList.get(i);
            Integer incRepValue = actualDocsValues.get("INC_REP")?.getNumberValue()?.intValue();
            putOrUpdate(identityDocsValues, "INC_REP", RefBookAttributeType.NUMBER, incRepValue, attrCounter);
            if (attrCounter.isUpdate()) {
                Long uniqueId = identityDocsValues.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
                getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, versionFrom, null, identityDocsValues);
            }
        }

    } else {
        logger.error("Ошибка не найден тип документа с кодом " + person.getDocumentTypeCode())
    }
}

/**
 * Метод получает на вход список документов и возвращает на выходе новый список документов в котором флаг включения в отчет выставлен документу с минимальным приоритетом
 * @param identityDocRecords
 * @return
 */
List<Map<String, RefBookValue>> updatePriority(List<Map<String, RefBookValue>> identityDocRecords) {

    //Id типа документа - приоритет,
    Map<Long, Integer> docPriorities = getRefDocumentPriority();

    List<Map<String, RefBookValue>> result = new ArrayList<Map<String, RefBookValue>>(identityDocRecords)

    //сбрасываем флаг у всех документов
    result.each { valuesMap ->
        valuesMap.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 0));
    }

    Map<String, RefBookValue> minimalPrior = result.min {
        Long docIdRef = it.get("DOC_ID")?.getReferenceValue();
        Integer prior = docPriorities.get(docIdRef);
        return prior;
    }
    minimalPrior.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 1));
    return result;
}


def fillIdentityDocAttr(Map<String, RefBookValue> values, PersonData person, AttributeChangeListener attributeChangeListener) {
    putOrUpdate(values, "PERSON_ID", RefBookAttributeType.REFERENCE, person.getRefBookPersonId(), attributeChangeListener);
    putOrUpdate(values, "DOC_NUMBER", RefBookAttributeType.STRING, person.getDocumentNumber(), attributeChangeListener);
    putOrUpdate(values, "ISSUED_BY", RefBookAttributeType.STRING, null, attributeChangeListener);
    putOrUpdate(values, "ISSUED_DATE", RefBookAttributeType.DATE, null, attributeChangeListener);
    //Признак включения в отчет, при создании ставиться 1, при обновлении надо выбрать с минимальным приоритетом
    putOrUpdate(values, "INC_REP", RefBookAttributeType.NUMBER, 1, attributeChangeListener);
    putOrUpdate(values, "DOC_ID", RefBookAttributeType.REFERENCE, findDocumentTypeByCode(person.getDocumentTypeCode()), attributeChangeListener);
}

/**
 * Создание записи в справочнике Идентификаторы физлиц
 * @param person
 * @param asnuId
 * @return
 */
RefBookRecord createIdentityTaxpayerRecord(PersonData person, Long asnuId, AttributeChangeListener attributeChangeListener) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putOrUpdate(values, "PERSON_ID", RefBookAttributeType.REFERENCE, person.getRefBookPersonId(), attributeChangeListener);
    putOrUpdate(values, "INP", RefBookAttributeType.STRING, person.getInp(), attributeChangeListener);
    putOrUpdate(values, "AS_NU", RefBookAttributeType.REFERENCE, asnuId, attributeChangeListener);
    record.setValues(values);
    return record;
}

/**
 * Обновление записи в справочнике "Идентификаторы налогоплательщика"
 * @param taxpayerIdentityRefBook список записей справочника для текущего ФЛ
 * @param person ФЛ
 * @param asnuId id записи справочника АСНУ
 * @return
 */
def updateTaxpayerIdentity(List<Map<String, RefBookValue>> taxpayerIdentityRefBook, PersonData person, Long asnuId, AttrCounter attrCounter) {

    //Ищем в списке записей запись с такимже АСНУ, по постановке обновляем только ИНП в рамках одной АСНУ (корректировка)
    Long findedAsnuId = taxpayerIdentityRefBook?.find {
        asnuId.equals(it.get("AS_NU")?.getReferenceValue())
    }?.get("AS_NU")?.getReferenceValue();

    if (findedAsnuId != null) {
        for (Map<String, RefBookValue> refBookValues : taxpayerIdentityRefBook) {
            RefBookValue value = refBookValues.get("AS_NU");
            if (asnuId.equals(value?.getReferenceValue())) {
                //нашли запись с нужной АСНУ, обновляем ИНП
                Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
                putOrUpdate(refBookValues, "INP", RefBookAttributeType.STRING, person.getInp(), attrCounter);
                if (attrCounter.isUpdate()) {
                    getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getVersionFrom(), null, refBookValues);
                }
            }
        }
    } else {
        //Такой АСНУ нету, создаем новую запиь
        RefBookRecord refBookRecord = createIdentityTaxpayerRecord(person, asnuId, attrCounter);
        getProvider(RefBook.Id.ID_TAX_PAYER.getId()).createRecordVersionWithoutLock(logger, getVersionFrom(), null, Arrays.asList(refBookRecord));
    }
}

def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value) {
    AttributeChangeListener changedListener = new EmptyChangedListener()
    putOrUpdate(valuesMap, attrName, type, value, changedListener);
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 */
def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, AttributeChangeListener attributeChangedListener) {

    AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, value);

    RefBookValue refBookValue = valuesMap?.get(attrName);
    if (refBookValue != null) {
        //обновление записи, если новое значение задано и отличается от существующего
        Object currentValue = refBookValue.getValue();
        changeEvent.setCurrentValue(currentValue);
        if (value != null && !ScriptUtils.equalsNullSafe(currentValue, value)) {
            //значения не равны, обновление
            refBookValue.setValue(value);
            changeEvent.setType(EventType.REFRESHED);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
        changeEvent.setType(EventType.CREATED);
    }

    attributeChangedListener.processAttr(changeEvent);

}

enum EventType {
    IGNORED,
    CREATED,
    REFRESHED,
    DELETED,
}

public class AttributeChangeEvent {

    AttributeChangeEvent(String attrName, Object value) {
        this.attrName = attrName
        this.value = value
    }
    public EventType type = EventType.IGNORED;

    private String attrName;

    private Object currentValue;

    private Object value;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    Object getCurrentValue() {
        return currentValue
    }

    void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue
    }

    EventType getType() {
        return type
    }

    void setType(EventType type) {
        this.type = type
    }

    Object getValue() {
        return value
    }

    void setValue(Object value) {
        this.value = value
    }
}

class AttrCounter implements AttributeChangeListener {

    private int refreshed = 0;
    private int created = 0;
    private int ignored = 0;

    Map<String, String> msg = new HashMap<String, String>();

    @Override
    void processAttr(AttributeChangeEvent event) {
        if (EventType.CREATED.equals(event.type)) {
            created++;
            //if (event.getValue() != null) {sb.append("[").append(event.getAttrName()).append(": ").append(event.getValue()).append("]")}
        } else if (EventType.REFRESHED.equals(event.type)) {
            refreshed++;
            msg.put(event.getAttrName(), new StringBuilder().append(event.getCurrentValue()).append("->").append(event.getValue()))
        } else if (EventType.IGNORED.equals(event.type)) {
            ignored++;
        }
    }

    public Map<String, String> getMessages() {
        return msg;
    }

    public boolean isUpdate() {
        return (created != 0 || refreshed != 0)
    }

}

class EmptyChangedListener implements AttributeChangeListener {
    public void processAttr(AttributeChangeEvent event) {
        //do nothing...
    }
}

public interface AttributeChangeListener extends EventListener {
    public void processAttr(AttributeChangeEvent event);
}

def collectPersonIds(List<PersonData> personDataList) {
    def personIds = []
    personDataList.each { personData ->
        if (personData.refBookPersonId != null && personData.refBookPersonId != 0) {
            personIds.add(personData.refBookPersonId)
        }
    }
    return personIds;
}

/**
 * По цифровому коду страны найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findCountryId(countryCode) {
    def citizenshipCodeMap = getRefCitizenship();
    def result = countryCode != null && !countryCode.isEmpty() ? citizenshipCodeMap.find {
        it.value == countryCode
    }?.key : null;
    if (countryCode != null && !countryCode.isEmpty() && result == null) {
        logger.warn("В справочнике 'ОК 025-2001 (Общероссийский классификатор стран мира)' не найдена запись, страна с кодом " + countryCode);
    }
    return result;
}

/**
 * Создание объекта по которуму будет идентифицироваться физлицо в справочнике
 * @param person
 * @return
 */
PersonData createPersonData(RaschsvPersSvStrahLic person) {
    PersonData personData = new PersonData();

    personData.refBookPersonId = person.personId;

    personData.lastName = person.familia;
    personData.firstName = person.imya;
    personData.middleName = person.otchestvo;

    personData.inn = person.innfl;
    personData.snils = person.snils;
    personData.birthDate = person.dataRozd;

    personData.citizenshipId = findCountryId(person.grazd);
    personData.citizenship = person.grazd;
    personData.sex = person.pol?.toInteger();

    //Строка для вывода номера ФЛ в сообщениях
    personData.personNumber = person.nomer;

    //Документы
    personData.documentTypeCode = person.kodVidDoc;
    personData.documentTypeId = findDocumentTypeByCode(person.kodVidDoc);
    personData.documentNumber = person.serNomDoc;

    personData.useAddress = false;

    personData.pension = person.prizOps ? Integer.parseInt(person.prizOps) : 2;
    personData.medical = person.prizOms ? Integer.parseInt(person.prizOms) : 2;
    personData.social = person.prizOss ? Integer.parseInt(person.prizOss) : 2;
    personData.employee = 2;

    return personData;
}


//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Обработка события CHECK
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Кэш провайдеров
@Field def providerCache = [:]

// значение подразделения из справочника
@Field def departmentParam = null

// значение подразделения из справочника
@Field def departmentParamTable = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Дата начала отчетного периода
@Field def reportPeriodStartDate = null

// Коды ОКВЭД
@Field def okvedCodeCache = [:]
@Field def okvedCodeActualCache = [:]

// Коды формы реорганизации и ликвидации
@Field def reorgFormCodeCache = [:]

// Коды мест предоставления документа
@Field def presentPlaceCodeCache = [:]
@Field def presentPlaceCodeActualCache = [:]

// Общие параметры
@Field def configurationParamCache = [:]

// Коды тарифа плательщика
@Field def tariffPayerCodeActualCache = [:]

// Основания заполнения сумм страховых взносов
@Field def fillBaseCodeCache = []

// Коды классов условий труда
@Field def hardWorkCodeCache = []

// Коды категорий застрахованных лиц
@Field def personCategoryCodeCache = []

// Виды документов, удостоверяющих личность Мапа <Идентификатор, Код>
@Field def documentCodesCache = [:]
@Field def documentCodesActualCache = []


// Страны
@Field def citizenshipCache = [:]
@Field def citizenshipActualCache = []

// Кэш для справочников
@Field def refBookCache = [:]

// Поля справочников
@Field final String RF_CODE = "CODE"
@Field final String RF_FOR_FOND = "FOR_FOND"
@Field final String RF_FOR_OPS_OMS = "FOR_OPS_OMS"

// Поля справочника Физические лица
@Field final String RF_RECORD_ID = "RECORD_ID"
@Field final String RF_LAST_NAME = "LAST_NAME"
@Field final String RF_FIRST_NAME = "FIRST_NAME"
@Field final String RF_MIDDLE_NAME = "MIDDLE_NAME"
@Field final String RF_BIRTH_DATE = "BIRTH_DATE"
@Field final String RF_SEX = "SEX"
@Field final String RF_PENSION = "PENSION"
@Field final String RF_MEDICAL = "MEDICAL"
@Field final String RF_SOCIAL = "SOCIAL"
@Field final String RF_INN = "INN"
@Field final String RF_SNILS = "SNILS"
@Field final String RF_CITIZENSHIP = "CITIZENSHIP"
@Field final String RF_DOC_ID = "DOC_ID"
@Field final String RF_DOC_NUMBER = "DOC_NUMBER"
@Field final String RF_OLD_ID = "OLD_ID"

def checkData() {
    // Проверки xml
    checkDataXml()

    // Проверки БД
    checkDataDB()
}

/**
 * Проверки БД
 * @return
 */
def checkDataDB() {
    // Проверки по плательщику страховых взносов
    checkDataDBPerson()

    // Суммовые проверки
    long time = System.currentTimeMillis();
    checkDataDBSum()
    println "Суммовые проверки " + (System.currentTimeMillis() - time);
    logger.info("Суммовые проверки: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Проверки по плательщику страховых взносов
 * @return
 */
def checkDataDBPerson() {

    def raschsvPersSvStrahLicList = raschsvPersSvStrahLicService.findPersons(declarationData.id)

    long time = System.currentTimeMillis();

    // Гражданство
    def citizenshipCodeMap = getRefCitizenship();
    def citizenshipCodeActualList = getActualRefCitizenship();

    // Физические лица
    def personIds = getPersonIds(raschsvPersSvStrahLicList)
    def personMap = [:]

    // ДУЛ <person_id, массив_ДУЛ>
    def dulMap = [:]

    if (!personIds.isEmpty()) {
        personMap = getActualRefPersons(personIds)
        if (personMap.isEmpty()) {
            logger.error("Не найдены актуальные записи в справочнике \"Физические лица\".")
        }

        // Получим мапу ДУЛ
        dulMap = getRefDul(personIds)
    }

    // Коды видов документов
    def documentTypeActualList = getActualRefDocument()

    println "Загрузка справочников для проверок записей в БД / Проверки по плательщику страховых взносов " + (System.currentTimeMillis() - time);
    logger.info("Загрузка справочников для проверок записей в БД / Проверки по плательщику страховых взносов: (" + (System.currentTimeMillis() - time) + " ms)");

    // Идентификаторы ссылок на справочник Физические лица
    def personIdList = []

    time = System.currentTimeMillis();

    // Проверки по плательщику страховых взносов
    raschsvPersSvStrahLicList.each { raschsvPersSvStrahLic ->
        def fioBirthday = raschsvPersSvStrahLic.familia + " " + raschsvPersSvStrahLic.imya + " " + raschsvPersSvStrahLic.otchestvo + " " + raschsvPersSvStrahLic.dataRozd

        // 3.1.1 Назначение ФЛ записи справочника "Физические лица"
        // Если personId, то он принимает значение 0
        if (raschsvPersSvStrahLic.personId == null || raschsvPersSvStrahLic.personId == 0) {
            logger.warn("Отсутствует ссылка на запись справочника \"Физические лица\" для ФЛ " + fioBirthday)
        } else {
            if (!personMap.isEmpty()) {
                personIdList.add(raschsvPersSvStrahLic.personId)
                def person = personMap.get(raschsvPersSvStrahLic.personId)
                if (!person) {
                    logger.error("Не найдена актуальная запись в справочнике \"Физические лица\" для ФЛ " + fioBirthday)
                } else {
                    // 3.1.2 Соответствие фамилии ФЛ и справочника
                    if (raschsvPersSvStrahLic.familia != person.get(RF_LAST_NAME).value) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ФИО.Фамилия"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.familia}\" не равен фамилии = \"${person.get(RF_LAST_NAME).value}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.3 Соответствие имени ФЛ и справочника
                    if (raschsvPersSvStrahLic.imya != person.get(RF_FIRST_NAME).value) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ФИО.Имя"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.imya}\" не равен имени = \"${person.get(RF_FIRST_NAME).value}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.4 Соответствие отчества ФЛ и справочника
                    if (raschsvPersSvStrahLic.otchestvo != null && raschsvPersSvStrahLic.otchestvo != person.get(RF_MIDDLE_NAME).value) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ФИО.Отчество"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.otchestvo}\" не равен отчеству = \"${person.get(RF_MIDDLE_NAME).value}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.5 Соответствие даты рождения ФЛ и справочника
                    if (raschsvPersSvStrahLic.dataRozd != person.get(RF_BIRTH_DATE).value) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ДатаРожд"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.dataRozd}\" не равен дате рождения = \"${person.get(RF_BIRTH_DATE).value}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.6 Соответствие пола ФЛ и справочника
                    if (raschsvPersSvStrahLic.pol != person.get(RF_SEX)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Пол"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.pol}\" не равен полу = \"${person.get(RF_SEX)?.value?.toString()}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.7 Соответствие признака ОПС ФЛ и справочника
                    if (raschsvPersSvStrahLic.prizOps != person.get(RF_PENSION)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОПС"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.prizOps}\" не равен признаку ОПС = \"${person.get(RF_PENSION)?.value?.toString()}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.8 Соответствие признака ОМС ФЛ и справочника
                    if (raschsvPersSvStrahLic.prizOms != person.get(RF_MEDICAL)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОМС"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.prizOms}\" не равен признаку ОМС = \"${person.get(RF_MEDICAL)?.value?.toString()}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.9 Соответствие признака ОСС
                    if (raschsvPersSvStrahLic.prizOss != person.get(RF_SOCIAL)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОСС"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.prizOss}\" не равен признаку ОСС = \"${person.get(RF_SOCIAL)?.value?.toString()}\" справочника \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.10 Соответсвие ИНН ФЛ - получателя дохода
                    if (raschsvPersSvStrahLic.innfl != null && raschsvPersSvStrahLic.innfl != person.get(RF_INN)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.innfl}\" не равен ИНН = \"${person.get(RF_INN)?.value?.toString()}\" в справочнике \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.11 Соответствие СНИЛС ФЛ - получателя дохода
                    if (raschsvPersSvStrahLic.snils != person.get(RF_SNILS)?.value?.toString()) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.snils}\" не равен СНИЛС = \"${person.get(RF_SNILS)?.value?.toString()}\" в справочнике \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.12 Соответствие кода вида документа ФЛ - получателя дохода
                    def allDocList = dulMap.get(raschsvPersSvStrahLic.personId)
                    // Вид документа
                    def personDocTypeList = []
                    // Серия и номер документа
                    def personDocNumberList = []
                    allDocList.each { dul ->
                        personDocType = getRefBookByRecordIds(REF_BOOK_DOCUMENT_CODES_ID, (dul.get(RF_DOC_ID).value))
                        personDocTypeList.add(personDocType?.CODE?.stringValue)
                        personDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
                    }
                    if (!personDocTypeList.contains(raschsvPersSvStrahLic.kodVidDoc)) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.kodVidDoc}\" не равен документу, удостоверяющему личность = \"${personDocTypeList.join(", ")}\" в справочнике \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.13 Актуальность кода вида документа ФЛ - получателя дохода
                    personDocTypeList.each { personDocType ->
                        if (!documentTypeActualList.contains(personDocType)) {
                            logger.warn("В справочнике \"Физические лица.Документы, удостоверяющие личность\" указаны неактуальные коды документов для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                        }
                    }

                    // 3.1.14 Соответствие серии и номера документа
                    if (!personDocNumberList.contains(raschsvPersSvStrahLic.serNomDoc)) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СерНомДок"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.serNomDoc}\" не равен серии и номеру ДУЛ = \"${personDocNumberList.join(", ")}\" в справочнике \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.15 Соответсвие кода гражданства ФЛ - получателя дохода в справочнике
                    if (raschsvPersSvStrahLic.grazd != citizenshipCodeMap.get(person.get(RF_CITIZENSHIP)?.value)) {
                        def pathValue = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд"
                        logger.warn("$pathValue = \"${raschsvPersSvStrahLic.grazd}\" не равен гражданству = \"${citizenshipCodeMap.get(person.get(RF_CITIZENSHIP)?.value)}\" в справочнике \"Физические лица\" для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }

                    // 3.1.16 Актуальность кода гражданства ФЛ
                    citizenship = getRefBookByRecordIds(REF_BOOK_COUNTRY_ID, person.get(RF_CITIZENSHIP)?.value)
                    if (!citizenshipCodeActualList.contains(citizenship?.CODE?.stringValue)) {
                        logger.warn("В справочнике \"Физические лица.Документы, удостоверяющие личность\" указан неактуальный код гражданства для ФЛ с идентификатором ФЛ = \"${person.get(RF_RECORD_ID).value}\"")
                    }
                }
            }
        }
    }

    println "Проверки по плательщику страховых взносов " + (System.currentTimeMillis() - time);
    logger.info("Проверки по плательщику страховых взносов: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();

    // 3.2.1 Дубли физического лица рамках формы
    def raschsvPersSvStrahLicDuplList = raschsvPersSvStrahLicService.findDublicatePersonIdByDeclarationDataId(declarationData.id)
    def msgError  = "Найдено несколько записей, идентифицированных как одно физическое лицо с идентификаторами ФЛ: "
    // Мапа для группировки дублей <personId, RaschsvPersSvStrahLic>
    def raschsvPersSvStrahLicDuplMap = [:]
    if (!raschsvPersSvStrahLicDuplList && !raschsvPersSvStrahLicDuplList.isEmpty()) {
        raschsvPersSvStrahLicDuplList.each { raschsvPersSvStrahLicDuplicate ->
            raschsvPersSvStrahLicList = raschsvPersSvStrahLicDuplMap.get(raschsvPersSvStrahLicDuplicate.personId)
            if (raschsvPersSvStrahLicList == null) {
                raschsvPersSvStrahLicList = []
            }
            raschsvPersSvStrahLicList.add(raschsvPersSvStrahLicDuplicate)
            raschsvPersSvStrahLicDuplMap.put(raschsvPersSvStrahLicDuplicate.personId, raschsvPersSvStrahLicList)
        }
        // Для каждого дубля выводим свое сообщение об ошибке
        raschsvPersSvStrahLicDuplMap.each { key, value ->
            def ids =  value*.personId.join(", ")
            logger.warn(msgError + ids)
        }
    }

    println "Дубли физического лица рамках формы " + (System.currentTimeMillis() - time);
    logger.info("Дубли физического лица рамках формы: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();

    // 3.2.2 Дубли физического лица в разных формах
    if (!personIdList.isEmpty()) {
        raschsvPersSvStrahLicDuplList = raschsvPersSvStrahLicService.findDublicatePersonIdByReportPeriodId(personIdList, declarationData.reportPeriodId)
        msgError = "ФЛ с идентификаторами: "
        if (!raschsvPersSvStrahLicDuplList && !raschsvPersSvStrahLicDuplList.isEmpty()) {
            def personIdDuplList = []
            def declarationDataIdDuplList = []
            raschsvPersSvStrahLicDuplList.each { raschsvPersSvStrahLicDupl ->
                // Будем брать дубли из других DeclarationData
                if (raschsvPersSvStrahLicDupl.declarationDataId != declarationData.id) {
                    if (!personIdDuplList.contains(raschsvPersSvStrahLicDupl.personId)) {
                        personIdDuplList.add(raschsvPersSvStrahLicDupl.personId)
                    }
                    if (!declarationDataIdDuplList.contains(raschsvPersSvStrahLicDupl.declarationDataId)
                            && raschsvPersSvStrahLicDupl.declarationDataId != declarationData.id) {
                        declarationDataIdDuplList.add(raschsvPersSvStrahLicDupl.declarationDataId)
                    }
                }
            }
            if (declarationDataIdDuplList.size() > 0) {
                msgError += personIdDuplList.join(", ") + " найдены в других формах "
                def declarationDataList = declarationService.getDeclarationData(declarationDataIdDuplList)
                def declarationDataInfo = []
                declarationDataList.each { dd ->
                    def declarationTypeName = declarationService.getTypeByTemplateId(dd.declarationTemplateId)?.name
                    def departmentName = departmentService.get(dd.departmentId)?.name
                    def startDate = reportPeriodService.getStartDate(dd.reportPeriodId)?.time?.format("dd.MM.yyyy")
                    def endDate = reportPeriodService.getEndDate(dd.reportPeriodId)?.time?.format("dd.MM.yyyy")
                    declarationDataInfo.add("\"$declarationTypeName\" \"$departmentName\" $startDate - $endDate")
                }
                logger.warn(msgError + declarationDataInfo.join(", "))
            }
        }
    }

    println "Дубли физического лица в разных формах " + (System.currentTimeMillis() - time);
    logger.info("Дубли физического лица в разных формах: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Суммовые проверки
 * @return
 */
def checkDataDBSum() {

    BigDecimal svVyplMkSum1 = 0
    BigDecimal svVyplMkSum2 = 0
    BigDecimal svVyplMkSum3 = 0
    BigDecimal svVyplMkDopSum1 = 0
    BigDecimal svVyplMkDopSum2 = 0
    BigDecimal svVyplMkDopSum3 = 0
    // Сведения о сумме выплат по доп.тарифам
    Map<Integer, BigDecimal> vyplSvDopMtMap = [:]
    // Перебор ПерсСвСтрахЛиц
    List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = raschsvPersSvStrahLicService.findPersons(declarationData.id)
    for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
        // СвВыпл.СвВыплМК
        def raschsvSvVyplMkList = raschsvPersSvStrahLic?.raschsvSvVypl?.raschsvSvVyplMkList
        if (raschsvSvVyplMkList != null) {
            raschsvSvVyplMkList.each { raschsvSvVyplMk ->
                if (raschsvSvVyplMk.mesyac != null) {
                    def numberMonth = getNumberMonth(Integer.parseInt(raschsvSvVyplMk.mesyac), getReportPeriodEndDate())
                    if (numberMonth == 1) {
                        svVyplMkSum1 += raschsvSvVyplMk.nachislSv ?: 0
                    } else if (numberMonth == 2) {
                        svVyplMkSum2 += raschsvSvVyplMk.nachislSv ?: 0
                    } else if (numberMonth == 3) {
                        svVyplMkSum3 += raschsvSvVyplMk.nachislSv ?: 0
                    }
                }
            }
        }
        // ВыплСВДоп.ВыплСВДопМТ
        def raschsvVyplSvDopMtList = raschsvPersSvStrahLic?.raschsvVyplSvDop?.raschsvVyplSvDopMtList
        if (raschsvVyplSvDopMtList != null) {
            raschsvVyplSvDopMtList.each { raschsvVyplSvDopMt ->
                if (raschsvVyplSvDopMt.mesyac != null) {
                    def numberMonth = getNumberMonth(Integer.parseInt(raschsvVyplSvDopMt.mesyac), getReportPeriodEndDate())
                    if (numberMonth == 1) {
                        svVyplMkDopSum1 += raschsvVyplSvDopMt.nachislSv ?: 0
                    } else if (numberMonth == 2) {
                        svVyplMkDopSum2 += raschsvVyplSvDopMt.nachislSv ?: 0
                    } else if (numberMonth == 3) {
                        svVyplMkDopSum3 += raschsvVyplSvDopMt.nachislSv ?: 0
                    }
                }

                BigDecimal vyplSvDopMtSum = vyplSvDopMtMap.get(raschsvVyplSvDopMt.tarif) ?: 0
                vyplSvDopMtSum += raschsvVyplSvDopMt.nachislSv ?: 0
                vyplSvDopMtMap.put(raschsvVyplSvDopMt.tarif, vyplSvDopMtSum)
            }
        }
    }

    BigDecimal uplPerOpsSum1 = 0
    BigDecimal uplPerOpsSum2 = 0
    BigDecimal uplPerOpsSum3 = 0
    BigDecimal uplPerOmsSum1 = 0
    BigDecimal uplPerOmsSum2 = 0
    BigDecimal uplPerOmsSum3 = 0
    List<RaschsvUplPer> raschsvUplPerList = raschsvUplPerService.findUplPer(declarationData.id)
    for (RaschsvUplPer raschsvUplPer : raschsvUplPerList) {
        // УплПерОПС
        if (raschsvUplPer.nodeName == NODE_NAME_UPL_PER_OPS) {
            uplPerOpsSum1 = raschsvUplPer.sumSbUpl1m ?: 0
            uplPerOpsSum2 = raschsvUplPer.sumSbUpl2m ?: 0
            uplPerOpsSum3 = raschsvUplPer.sumSbUpl3m ?: 0
        } else if (raschsvUplPer.nodeName == NODE_NAME_UPL_PER_OMS) {
            // УплПерОМС
            uplPerOmsSum1 = raschsvUplPer.sumSbUpl1m ?: 0
            uplPerOmsSum2 = raschsvUplPer.sumSbUpl2m ?: 0
            uplPerOmsSum3 = raschsvUplPer.sumSbUpl3m ?: 0
        }
    }

    BigDecimal nachislSvOpsSum1 = 0
    BigDecimal nachislSvOpsSum2 = 0
    BigDecimal nachislSvOpsSum3 = 0

    BigDecimal nachislSvOmsSum1 = 0
    BigDecimal nachislSvOmsSum2 = 0
    BigDecimal nachislSvOmsSum3 = 0

    // РасчСВ_ОПС.НачислСВНеПрев
    BigDecimal nachislSvNePrevSum1 = 0
    BigDecimal nachislSvNePrevSum2 = 0
    BigDecimal nachislSvNePrevSum3 = 0

    // РасчСВ_428.1-2.НачислСВДоп
    BigDecimal nachislSvDop428_12Sum1 = 0
    BigDecimal nachislSvDop428_12Sum2 = 0
    BigDecimal nachislSvDop428_12Sum3 = 0

    // РасчСВ_428.3.НачислСВДоп
    BigDecimal nachislSvDop428_3Sum1 = 0
    BigDecimal nachislSvDop428_3Sum2 = 0
    BigDecimal nachislSvDop428_3Sum3 = 0

    Map<String, BigDecimal> nachisl428_12Sum1Map = [:]
    Map<String, BigDecimal> nachisl428_12Sum2Map = [:]
    Map<String, BigDecimal> nachisl428_12Sum3Map = [:]

    Map<String, BigDecimal> nachisl428_3Sum1Map = [:]
    Map<String, BigDecimal> nachisl428_3Sum2Map = [:]
    Map<String, BigDecimal> nachisl428_3Sum3Map = [:]

    def pathAttrOps = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС"

    // Перебор РасчСВ_ОПС_ОМС
    List<RaschsvSvOpsOms> raschsvSvOpsOmsList = raschsvSvOpsOmsService.findSvOpsOms(declarationData.id)
    for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {

        BigDecimal nachislSvOpsCurr1 = 0
        BigDecimal nachislSvOpsCurr2 = 0
        BigDecimal nachislSvOpsCurr3 = 0

        // РасчСВ_ОПС.НачислСВПрев
        BigDecimal nachislSvPrevCurr1 = 0
        BigDecimal nachislSvPrevCurr2 = 0
        BigDecimal nachislSvPrevCurr3 = 0

        // РасчСВ_ОПС.НачислСВНеПрев
        BigDecimal nachislSvNePrevCurr1 = 0
        BigDecimal nachislSvNePrevCurr2 = 0
        BigDecimal nachislSvNePrevCurr3 = 0

        List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = raschsvSvOpsOms.raschsvSvOpsOmsRaschList
        for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOmsRaschList) {
            if (raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_OPS ||
                    raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_OMS ||
                    raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_428_12 ||
                    raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_428_3) {
                List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList
                for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRaschSumList) {
                    RaschsvSvSum1Tip raschsvSvSum1Tip = raschsvSvOpsOmsRaschSum.raschsvSvSum1Tip

                    if (raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_OPS) {
                        // РасчСВ_ОПС
                        if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV) {
                            // НачислСВ
                            nachislSvOpsCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvOpsSum1 += nachislSvOpsCurr1
                            nachislSvOpsCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvOpsSum2 += nachislSvOpsCurr2
                            nachislSvOpsCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
                            nachislSvOpsSum3 += nachislSvOpsCurr3
                        } else if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV_NE_PREV) {
                            // НачислСВНеПрев
                            nachislSvNePrevCurr1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvNePrevSum1 += nachislSvNePrevCurr1
                            nachislSvNePrevCurr2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvNePrevSum2 += nachislSvNePrevCurr2
                            nachislSvNePrevCurr3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
                            nachislSvNePrevSum3 += nachislSvNePrevCurr3
                        } else if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV_PREV) {
                            // НачислСВПрев
                            nachislSvPrevCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvPrevCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvPrevCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
                        }
                    } else if (raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_OMS) {
                        // РасчСВ_ОМС
                        if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV) {
                            // НачислСВ
                            nachislSvOmsSum1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvOmsSum2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvOmsSum3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
                        }
                    } else if (raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_428_12) {
                        // РасчСВ_428.1-2
                        if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV_DOP) {
                            // НачислСВДоп
                            nachislSvDop428_12Sum1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvDop428_12Sum2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvDop428_12Sum3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0

                            BigDecimal nachisl428_12Sum_1 = nachisl428_12Sum1Map.get(raschsvSvOpsOmsRasch.prOsnSvDop) ?: 0
                            nachisl428_12Sum_1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachisl428_12Sum1Map.put(raschsvSvOpsOmsRasch.prOsnSvDop, nachisl428_12Sum_1)

                            BigDecimal nachisl428_12Sum_2 = nachisl428_12Sum2Map.get(raschsvSvOpsOmsRasch.prOsnSvDop) ?: 0
                            nachisl428_12Sum_2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachisl428_12Sum2Map.put(raschsvSvOpsOmsRasch.prOsnSvDop, nachisl428_12Sum_2)

                            BigDecimal nachisl428_12Sum_3 = nachisl428_12Sum3Map.get(raschsvSvOpsOmsRasch.prOsnSvDop) ?: 0
                            nachisl428_12Sum_3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
                            nachisl428_12Sum3Map.put(raschsvSvOpsOmsRasch.prOsnSvDop, nachisl428_12Sum_3)
                        }

                    } else if (raschsvSvOpsOmsRasch.nodeName == NODE_NAME_RASCH_SV_428_3) {
                        // РасчСВ_428.3
                        if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NACHISL_SV_DOP) {
                            // НачислСВДоп
                            nachislSvDop428_3Sum1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvDop428_3Sum2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvDop428_3Sum3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0

                            BigDecimal nachisl428_3Sum_1 = nachisl428_3Sum1Map.get(raschsvSvOpsOmsRasch.klasUslTrud) ?: 0
                            nachisl428_3Sum_1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachisl428_3Sum1Map.put(raschsvSvOpsOmsRasch.klasUslTrud, nachisl428_3Sum_1)

                            BigDecimal nachisl428_3Sum_2 = nachisl428_3Sum2Map.get(raschsvSvOpsOmsRasch.klasUslTrud) ?: 0
                            nachisl428_3Sum_2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachisl428_3Sum2Map.put(raschsvSvOpsOmsRasch.klasUslTrud, nachisl428_3Sum_2)

                            BigDecimal nachisl428_3Sum_3 = nachisl428_3Sum3Map.get(raschsvSvOpsOmsRasch.klasUslTrud) ?: 0
                            nachisl428_3Sum_3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
                            nachisl428_3Sum3Map.put(raschsvSvOpsOmsRasch.klasUslTrud, nachisl428_3Sum_3)
                        }
                    }
                }
            }
        }

        // 3.3.1.1 Сумма исчисленных взносов на ОПС равна сумме не превышающих и превышающих предельную величину базы (Проверки выполняются для каждого РасчСВ_ОПС)
        if (nachislSvOpsCurr1 != nachislSvNePrevCurr1 + nachislSvPrevCurr1) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум1Посл3М = \"$nachislSvOpsCurr1\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум1Посл3М = \"$nachislSvNePrevCurr1\", " + pathAttrOps + ".НачислСВПрев.Сум1Посл3М = \"$nachislSvPrevCurr1\"."
            logger.warn("Сумма исчисленных взносов на ОПС $pathAttrVal не равна сумме $pathAttrComp")
        }
        if (nachislSvOpsCurr2 != nachislSvNePrevCurr2 + nachislSvPrevCurr2) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум2Посл3М = \"$nachislSvOpsCurr2\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум2Посл3М = \"$nachislSvNePrevCurr2\", " + pathAttrOps + ".НачислСВПрев.Сум2Посл3М = \"$nachislSvPrevCurr2\"."
            logger.warn("Сумма исчисленных взносов на ОПС $pathAttrVal не равна сумме $pathAttrComp")
        }
        if (nachislSvOpsCurr3 != nachislSvNePrevCurr3 + nachislSvPrevCurr3) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум3Посл3М = \"$nachislSvOpsCurr3\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум3Посл3М = \"$nachislSvNePrevCurr3\", " + pathAttrOps + ".НачислСВПрев.Сум3Посл3М = \"$nachislSvPrevCurr3\"."
            logger.warn("Сумма исчисленных взносов на ОПС $pathAttrVal не равна сумме $pathAttrComp")
        }
    }

    // 3.3.1.2 Сумма исчисленных страховых взносов по всем ФЛ равна значению исчисленных страховых взносов по ОПС в целом (с базы не превышающих предельную величину) (Проверки выполняются по всем РасчСВ_ОПС)
    if (nachislSvNePrevSum1 != svVyplMkSum1) {
        def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум1Посл3М = \"$nachislSvNePrevSum1\""
        logger.warn("Сумма исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ не равна $pathAttrVal")
    }
    if (nachislSvNePrevSum2 != svVyplMkSum2) {
        def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум2Посл3М = \"$nachislSvNePrevSum2\""
        logger.warn("Сумма исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ не равна $pathAttrVal")
    }
    if (nachislSvNePrevSum3 != svVyplMkSum3) {
        def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум3Посл3М = \"$nachislSvNePrevSum3\""
        logger.warn("Сумма исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ не равна $pathAttrVal")
    }

    // 3.3.1.3 Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов (Проверки выполняются по всем РасчСВ_ОПС)
    if (nachislSvOpsSum1 != uplPerOpsSum1) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум1Посл3М = \"$nachislSvOpsSum1\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл1М = \"$uplPerOpsSum1\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОПС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }
    if (nachislSvOpsSum2 != uplPerOpsSum2) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум2Посл3М = \"$nachislSvOpsSum2\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл2М = \"$uplPerOpsSum2\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОПС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }
    if (nachislSvOpsSum3 != uplPerOpsSum3) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум3Посл3М = \"$nachislSvOpsSum3\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл3М = \"$uplPerOpsSum3\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОПС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }

    // 3.3.2.1 Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу в целом (Проверки выполняются по всем РасчСВ_ОПС428)
    def pathAttr428_12 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.1-2.НачислСВДоп"
    def pathAttr428_3 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.3.НачислСВДоп"
    if (nachislSvDop428_12Sum1 + nachislSvDop428_3Sum1 != svVyplMkDopSum1) {
        logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ не равна сумме: " + pathAttr428_12 + ".Сум1Посл3М = \"$nachislSvDop428_12Sum1\", " + pathAttr428_3 + ".Сум1Посл3М = \"$nachislSvDop428_3Sum1\"")
    }
    if (nachislSvDop428_12Sum2 + nachislSvDop428_3Sum2 != svVyplMkDopSum2) {
        logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ не равна сумме: " + pathAttr428_12 + ".Сум2Посл3М = \"$nachislSvDop428_12Sum2\", " + pathAttr428_3 + ".Сум2Посл3М = \"$nachislSvDop428_3Sum2\"")
    }
    if (nachislSvDop428_12Sum3 + nachislSvDop428_3Sum3 != svVyplMkDopSum3) {
        logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ не равна сумме: " + pathAttr428_12 + ".Сум3Посл3М = \"$nachislSvDop428_12Sum3\", " + pathAttr428_3 + ".Сум3Посл3М = \"$nachislSvDop428_3Sum3\"")
    }

    vyplSvDopMtMap.each { tarif, vyplSvDopMtSum ->
        // 3.3.2.2 Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 1 и 2 статьи 428)
        ["22", "21"].each {
            def prOsnSvDop = null
            switch (it) {
                case "22":
                    prOsnSvDop = "1"
                    break
                case "21":
                    prOsnSvDop = "2"
                    break
            }
            if (tarif == it) {
                BigDecimal nachisl428_12Sum_1 = nachisl428_12Sum1Map.get(prOsnSvDop)
                if (nachisl428_12Sum_1 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ не равна суммам $pathAttr428_12" + ".Сум1Посл3М = \"$nachisl428_12Sum_1\"")
                }
                BigDecimal nachisl428_12Sum_2 = nachisl428_12Sum2Map.get(prOsnSvDop)
                if (nachisl428_12Sum_2 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ не равна суммам $pathAttr428_12" + ".Сум2Посл3М = \"$nachisl428_12Sum_2\"")
                }
                BigDecimal nachisl428_12Sum_3 = nachisl428_12Sum3Map.get(prOsnSvDop)
                if (nachisl428_12Sum_3 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ не равна суммам $pathAttr428_12" + ".Сум3Посл3М = \"$nachisl428_12Sum_3\"")
                }
            }
        }

        // 3.3.2.3 Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛравна значению исчисленных страховых взносов по доп. тарифу (п 3 428)
        ["23", "24", "25", "26", "27"].each {
            def klasUslTrud = null
            switch (it) {
                case "23":
                    klasUslTrud = "1"
                    break
                case "24":
                    klasUslTrud = "2"
                    break
                case "25":
                    klasUslTrud = "3"
                    break
                case "26":
                    klasUslTrud = "4"
                    break
                case "27":
                    klasUslTrud = "5"
                    break
            }
            if (tarif == it) {
                BigDecimal nachisl428_3Sum_1 = nachisl428_3Sum1Map.get(klasUslTrud)
                if (nachisl428_3Sum_1 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ не равна суммам $pathAttr428_3" + ".Сум1Посл3М = \"$nachisl428_3Sum_1\"")
                }
                BigDecimal nachisl428_3Sum_2 = nachisl428_3Sum2Map.get(klasUslTrud)
                if (nachisl428_3Sum_2 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ не равна суммам $pathAttr428_3" + ".Сум2Посл3М = \"$nachisl428_3Sum_2\"")
                }
                BigDecimal nachisl428_3Sum_3 = nachisl428_3Sum3Map.get(klasUslTrud)
                if (nachisl428_3Sum_3 != vyplSvDopMtSum) {
                    logger.warn("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ не равна суммам $pathAttr428_3" + ".Сум3Посл3М = \"$nachisl428_3Sum_3\"")
                }
            }
        }
    }

    // 3.3.3.1 Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов (Проверки выполняются по всем РасчСВ_ОМС428)
    def pathAttrOms = "Файл.Документ.РасчетСВ.ОбязПлатСВ"
    if (uplPerOmsSum1 != nachislSvOmsSum1) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум1Посл3М = \"$uplPerOmsSum1\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум1Посл3М = \"$nachislSvNePrevSum1\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОМС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }
    if (uplPerOmsSum2 != nachislSvOmsSum2) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум2Посл3М = \"$uplPerOmsSum2\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум2Посл3М = \"$nachislSvNePrevSum2\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОМС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }
    if (uplPerOmsSum3 != nachislSvOmsSum3) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум3Посл3М = \"$uplPerOmsSum3\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум3Посл3М = \"$nachislSvNePrevSum3\""
        logger.warn("Сумма страховых взносов, подлежащая уплате ОМС $pathAttrVal не равна сумме исчисленных страховых взносов $pathAttrComp")
    }
}

/**
 * Возвращает порядковый номер месяца в списке последних трех месяцев отчетного периода,
 * если данный месяц принадлежит к последним трем месяцам отчетного периода
 * @return
 */
def getNumberMonth(def currMonth, def endDate) {
    def result = null
    def endMonth = endDate[Calendar.MONTH] + 1
    if (currMonth >= endMonth - 2 && currMonth <= endMonth) {
        switch (endMonth - currMonth) {
            case 0:
                result = 3
                break
            case 1:
                result = 2
                break
            case 2:
                result = 1
                break
        }
    }
    return result
}

/**
 * Проверки xml
 * @return
 */
def checkDataXml() {
    // Валидация по схеме
    declarationService.validateDeclaration(declarationData, userInfo, logger, null)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def xmlStream = declarationService.getXmlStream(declarationData.id)

    // Проверка является ли пакет пустым
//    println(xmlStream.getText())
//    logger.info(xmlStream.getText())

    def fileNode = new XmlSlurper().parse(xmlStream);
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    def fileName = declarationService.getXmlDataFileName(declarationData.id)

    // Проверки, которые проводились при загрузке
    checkImportRaschsv(fileNode, fileName)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def msgErrNotEquals = "Не совпадает значение %s = \"%s\" плательщика страховых взносов %s."

    long time = System.currentTimeMillis();

    // Параметры подразделения
    def departmentParam = getDepartmentParam(declarationData.departmentId)
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam?.id.value)

    // Коды ОКВЭД
    def mapOkvedCode = getRefOkvedCode()
    def mapActualOkvedCode = getRefActualOkvedCode()

    // Коды форм реорганизации и ликвидации
    def mapReorgFormCode = getRefReorgFormCode()

    // Коды мест предоставления документа
    def mapPresentPlace = getRefPresentPlace()
    def mapActualPresentPlace = getActualRefPresentPlace()

    // Получим ИНН из справочника "Общие параметры"
    ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
    def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)

    // Коды тарифа плательщика
    def mapActualTariffPayerCode = getActualTariffPayerCode()

    // Основания заполнения сумм страховых взносов
    def listActualFillBase = getActualFillBaseCode()

    // Коды классов условий труда
    def listActualHardWork = getActualHardWork()

    // Коды категорий застрахованных лиц
    def listPersonCategory = getActualPersonCategory()

    println "Загрузка справочников для xml-проверок: " + (System.currentTimeMillis() - time);
    logger.info("Загрузка справочников для xml-проверок: (" + (System.currentTimeMillis() - time) + " ms)");

    // ------------Проверки по плательщику страховых взносов RASCHSV_SVNP_PODPISANT
    time = System.currentTimeMillis();
    fileNode.childNodes().each { documentNode ->
        // Документ
        if (documentNode.name == NODE_NAME_DOCUMENT) {

            // 2.1.1 Соответствие кода места настройкам подразделения
            def poMestuCodeXml = documentNode.attributes()[DOCUMENT_PO_MESTU]
            def poMestuParam = mapPresentPlace.get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
            def poMestuCodeParam = poMestuParam?.get(RF_CODE)?.value
            if (poMestuCodeXml != poMestuCodeParam) {
                def pathAttr = "Файл.Документ.ПоМесту"
                logger.warn("$pathAttr = \"" + poMestuCodeXml + "\" не совпадает с настройками подразделения.")
            }

            // 2.1.2 Актуальность кода места
            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
            def poMestuActualParam = mapActualPresentPlace.get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
            def poMestuCodeActualParam = poMestuActualParam?.get(RF_CODE)?.value
            if (poMestuCodeParam != poMestuCodeActualParam || !poMestuActualParam?.get(RF_FOR_FOND)?.value) {
                logger.warn("В настройках подразделений указан неактуальный код места, по которому предоставляется документ = \"$poMestuCodeActualParam\"")
            }

            // НомКорр
            def nomKorrDocXml = documentNode.attributes()[PERV_SV_STRAH_LIC_NOM_KORR]

            // Период
            def periodDocXml = documentNode.attributes()[PERV_SV_STRAH_LIC_PERIOD]

            // ОтчетГод
            def otchetGodDocXml = documentNode.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]

            documentNode.childNodes().each { documentChildNode ->

                def kppXml = ""

                // СвНП
                if (documentChildNode.name == NODE_NAME_SV_NP) {

                    def sberbankInnXml = ""
                    def sVReorgYLFormXml = ""
                    def sVReorgYLInnXml = ""
                    def sVReorgYLKppXml = ""

                    boolean sVReorgYLIsExist = false

                    // НПЮЛ
                    documentChildNode.childNodes().each { NPYLNode ->
                        if (NPYLNode.name == NODE_NAME_NPYL) {
                            sberbankInnXml = NPYLNode.attributes()[NPYL_INNYL]
                            kppXml = NPYLNode.attributes()[NPYL_KPP]

                            // СвРеоргЮЛ
                            NPYLNode.childNodes().each { sVReorgYLNode ->
                                if (sVReorgYLNode.name == NODE_NAME_SV_REORG_YL) {
                                    sVReorgYLIsExist = true
                                    sVReorgYLFormXml = sVReorgYLNode.attributes()[SV_REORG_YL_FORM_REORG]
                                    sVReorgYLInnXml = sVReorgYLNode.attributes()[SV_REORG_YL_INNYL]
                                    sVReorgYLKppXml = sVReorgYLNode.attributes()[SV_REORG_YL_KPP]
                                }
                            }
                        }
                    }

                    // 2.1.3 Соответствие ОКВЭД настройкам подразделения
                    def okvedCodeXml = documentChildNode.attributes()[SV_NP_OKVED]
                    def okvedCodeParam = mapOkvedCode.get(departmentParamIncomeRow?.OKVED?.referenceValue)
                    if (okvedCodeXml != okvedCodeParam) {
                        def pathAttr = "Файл.Документ.СвНП.ОКВЭД"
                        logger.warn("$pathAttr = \"$okvedCodeXml\" не совпадает с ОКВЭД = \"$okvedCodeParam\"")
                    }

                    // 2.1.4 Актуальность ОКВЭД
                    // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                    def okvedCodeActualParam = mapActualOkvedCode.get(departmentParamIncomeRow?.OKVED?.referenceValue)
                    if (okvedCodeParam != okvedCodeActualParam) {
                        def pathAttr = "Файл.Документ.СвНП.ОКВЭД"
                        logger.warn("$pathAttr = \"$okvedCodeParam\" неактуаленый")
                    }

                    // 2.1.5 Соответсвие ИНН ЮЛ Общим параметрам
                    if (sberbankInnXml != sberbankInnParam) {
                        def pathAttr = "Файл.Документ.СвНП.НПЮЛ.ИННЮЛ"
                        logger.warn("$pathAttr = \"$sberbankInnXml\" не совпадает с Общим параметром \"ИНН ПАО Сбербанк\" = \"$sberbankInnParam\"")
                    }

                    // 2.1.6 Соответсвие КПП ЮЛ настройкам подразделения
                    def kppParam = departmentParamIncomeRow?.KPP?.stringValue
                    if (kppXml != kppParam) {
                        def pathAttr = "Файл.Документ.СвНП.НПЮЛ.КПП"
                        logger.warn("$pathAttr = \"$kppXml\" не совпадает с КПП = \"$kppParam\"")
                    }

                    // Если узел СвРеоргЮЛ существует
                    if (sVReorgYLIsExist) {
                        // 2.1.7 Соответствие формы реорганизации
                        def sVReorgYLFormParam = mapReorgFormCode.get(departmentParamIncomeRow?.REORG_FORM_CODE?.referenceValue)
                        if (sVReorgYLFormXml != sVReorgYLFormParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.ФормРеорг"
                            logger.warn("$pathAttr = \"$sVReorgYLFormXml\" не совпадает c формой реорганизации = \"$sVReorgYLFormParam\"")
                        }

                        // 2.1.8 Соответствие ИНН реорганизованной организации
                        def sVReorgYLInnParam = departmentParamIncomeRow?.REORG_INN?.stringValue
                        if (sVReorgYLInnXml != sVReorgYLInnParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.ИННЮЛ"
                            logger.warn("$pathAttr = \"$sVReorgYLInnXml\" для организации плательщика страховых взносов не совпадает с ИНН реорганизованной организации = \"$sVReorgYLInnParam\"")
                        }

                        // 2.1.9 Соответствие КПП реорганизованной организации
                        def sVReorgYLKppParam = departmentParamIncomeRow?.REORG_KPP?.stringValue
                        if (sVReorgYLKppXml != sVReorgYLKppParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.КПП"
                            logger.warn("$pathAttr = \"sVReorgYLKppXml\" не совпадает с КПП реорганизованной организации = \"$sVReorgYLKppParam\"")
                        }
                    }

                } else if (documentChildNode.name == NODE_NAME_RASCHET_SV) {
                    // РасчетСВ
                    documentChildNode.childNodes().each { raschetSvChildNode ->

                        // ОбязПлатСВ
                        if (raschetSvChildNode.name == NODE_NAME_OBYAZ_PLAT_SV) {

                            // 2.2.1 Соответствие кода ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            def oktmoXml = raschetSvChildNode.attributes()[OBYAZ_PLAT_SV_OKTMO]
                            def oktmoParam = getRefBookValue(REF_BOOK_OKTMO_ID, departmentParamIncomeRow?.OKTMO?.referenceValue)
                            if (oktmoXml != oktmoParam?.CODE?.stringValue) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_OBYAZ_PLAT_SV, OBYAZ_PLAT_SV_OKTMO].join(".")
                                logger.warn("$pathAttr = \"$oktmoXml\" не совпадает с ОКТМО = \"$oktmoParam\"")
                            }

                            // 2.2.2 Актуальность ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                            if (oktmoParam && !isExistsOKTMO(oktmoParam?.CODE?.stringValue)) {
                                logger.warn("В настройках подразделений указан неактуальный ОКТМО = \"" + oktmoParam + "\"")
                            }

                            // РасчСВ_ОПС_ОМС
                            raschetSvChildNode.childNodes().each { raschSvOpsOmsNode ->
                                if (raschSvOpsOmsNode.name == NODE_NAME_RASCH_SV_OPS_OMS) {

                                    // 2.2.3 Соответствие кода тарифа плательщика справочнику
                                    def tariffPayerCodeXml = raschSvOpsOmsNode.attributes()[RASCH_SV_OPS_OMS_TARIF_PLAT]
                                    def tariffPayerParam = mapActualTariffPayerCode.get(tariffPayerCodeXml)
                                    if (tariffPayerParam != null && !tariffPayerParam?.get(RF_FOR_OPS_OMS)?.value) {
                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, RASCH_SV_OPS_OMS_TARIF_PLAT].join(".")
                                        logger.warn("$pathAttr = \"$tariffPayerCodeXml\" не найден (не действует) в справочнике \"Коды тарифа плательщика\"")
                                    }

                                    // РасчСВ_ОПС428
                                    raschSvOpsOmsNode.childNodes().each { raschSvOps428ChildNode ->
                                        if (raschSvOps428ChildNode.name == NODE_NAME_RASCH_SV_OPS428) {

                                            // РасчСВ_428.3
                                            raschSvOps428ChildNode.childNodes().each { raschSvOps428_3_ChildNode ->
                                                if (raschSvOps428_3_ChildNode.name == NODE_NAME_RASCH_SV_428_3) {
                                                    def fillBaseXml = raschSvOps428_3_ChildNode.attributes()[RASCH_SV_OPS428_3_OSNOV_ZAP]
                                                    def hardWorkXml = raschSvOps428_3_ChildNode.attributes()[RASCH_SV_OPS428_3_KLAS_USL_TRUD]

                                                    // 2.2.4 Значение основания заполнения
                                                    if (!listActualFillBase.contains(fillBaseXml)) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV,
                                                                        NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, NODE_NAME_RASCH_SV_OPS428,
                                                                        NODE_NAME_RASCH_SV_428_3, RASCH_SV_OPS428_3_OSNOV_ZAP].join(".")
                                                        logger.warn("$pathAttr = \"$fillBaseXml\" не найден (не действует) в справочнике \"Основания заполнения\"")
                                                    }

                                                    // 2.2.5 Значение кода класса условий труда
                                                    if (!listActualHardWork.contains(hardWorkXml)) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV,
                                                                        NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, NODE_NAME_RASCH_SV_OPS428,
                                                                        NODE_NAME_RASCH_SV_428_3, RASCH_SV_OPS428_3_KLAS_USL_TRUD].join(".")
                                                        logger.warn("$pathAttr = \"$hardWorkXml\" не найден (не действует) в справочнике \"Коды классов условий труда\"")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        } else if (raschetSvChildNode.name == NODE_NAME_PERS_SV_STRAH_LIC) {
                            // ПерсСвСтрахЛиц

                            // 2.3.1 Корректность номера корректировки
                            def nomKorrPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_NOM_KORR]
                            if (nomKorrDocXml != nomKorrPersXml) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, PERV_SV_STRAH_LIC_NOM_KORR].join(".")
                                logger.warn(pathAttr + " = \"" + nomKorrPersXml + "\" не соответствует номеру корректировки файла \"" + fileName + "\"")
                            }

                            // 2.3.2 Корректность периода
                            def periodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_PERIOD]
                            if (periodDocXml != periodPersXml) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, PERV_SV_STRAH_LIC_PERIOD].join(".")
                                logger.warn(pathAttr + " = \"" + periodPersXml + "\" не соответствует номеру корректировки файла \"" + fileName + "\"")
                            }

                            // 2.3.3 Корректность отчетного года
                            def otchetGodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]
                            if (otchetGodDocXml != otchetGodPersXml) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, PERV_SV_STRAH_LIC_OTCHET_GOD].join(".")
                                logger.warn(pathAttr + " = \"" + otchetGodPersXml + "\" не соответствует отчетному году файла \"" + fileName + "\"")
                            }

                            raschetSvChildNode.childNodes().each { persSvStrahLicChildNode ->
                                def snils = ""
                                // ДанФЛПолуч
                                if (persSvStrahLicChildNode.name == NODE_NAME_DAN_FL_POLUCH) {
                                    // СНИЛС
                                    snils = persSvStrahLicChildNode.attributes()[DAN_FL_POLUCH_SNILS]
                                } else if (persSvStrahLicChildNode.name == NODE_NAME_SV_VYPL_SVOPS) {
                                    // СвВыплСВОПС
                                    persSvStrahLicChildNode.childNodes().each { svVyplSvopsChildNode ->
                                        // ВыплСВДоп
                                        if (svVyplSvopsChildNode.name == NODE_NAME_VYPL_SV_DOP) {
                                            svVyplSvopsChildNode.childNodes().each { vyplSvDopMtNode ->
                                                // ВыплСВДопМТ
                                                if (vyplSvDopMtNode.name == NODE_NAME_VYPL_SV_DOP_MT) {
                                                    // 2.3.4 Значение кода тарифа
                                                    def tariffPayerCodeXml = vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_TARIF]
                                                    if (!mapActualTariffPayerCode.find { key, value -> key == tariffPayerCodeXml }) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, NODE_NAME_SV_VYPL_SVOPS,
                                                                        NODE_NAME_VYPL_SV_DOP, NODE_NAME_VYPL_SV_DOP_MT, VYPL_SV_DOP_MT_TARIF].join(".")
                                                        logger.warn(pathAttr + " = \"" + tariffPayerCodeXml + "\" ФЛ с СНИЛС = \"" + snils + "\"  не найден (не действует) в справочнике \"Коды тарифа плательщика\".")
                                                    }
                                                }
                                            }
                                        } else if (svVyplSvopsChildNode.name == NODE_NAME_SV_VYPL) {
                                            // СвВыпл
                                            svVyplSvopsChildNode.childNodes().each { svVyplMkNode ->
                                                // СвВыплМК
                                                if (svVyplMkNode.name == NODE_NAME_SV_VYPL_MK) {
                                                    // 2.3.5 Значение кода категории застрахованного лица
                                                    def kodKatLisCodeXml = svVyplMkNode.attributes()[SV_VYPL_MT_KOD_KAT_LIC]
                                                    if (!listPersonCategory.contains(kodKatLisCodeXml)) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, NODE_NAME_SV_VYPL_SVOPS,
                                                                        NODE_NAME_SV_VYPL, NODE_NAME_SV_VYPL_MK, SV_VYPL_MT_KOD_KAT_LIC].join(".")
                                                        logger.warn(pathAttr + " = \"" + kodKatLisCodeXml + "\" ФЛ с СНИЛС = \"" + snils + "\"  не найден (не действует) в справочнике \"Коды категорий застрахованных лиц\".")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    println "Проверки xml: " + (System.currentTimeMillis() - time);
    logger.info("Проверки xml: (" + (System.currentTimeMillis() - time) + " ms)");

    // ------------Сводные данные об обязательствах плательщика
}

/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

/**
 * Получить Параметры подразделения по сборам, взносам
 * @return
 */
def getDepartmentParam(def departmentId) {
    if (departmentParam == null) {
        def departmentParamList = getProvider(REF_BOOK_FOND_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

/**
 * Получить Параметры подразделения по сборам, взносам (таблица)
 * @param departmentParamId
 * @return
 */
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "REF_BOOK_FOND_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(REF_BOOK_FOND_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

/**
 * Получить дату которая используется в качестве версии записей справочника
 * @return дата используемая в качестве даты версии справочника
 */
def getVersionFrom() {
    return getReportPeriodStartDate();
}

/**
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param raschsvPersSvStrahLicList
 * @return
 */
def getPersonIds(def raschsvPersSvStrahLicList) {
    def personIds = []
    raschsvPersSvStrahLicList.each { raschsvPersSvStrahLic ->
        if (raschsvPersSvStrahLic.personId != null && raschsvPersSvStrahLic.personId != 0) {
            personIds.add(raschsvPersSvStrahLic.personId)
        }
    }
    return personIds;
}

/**
 * Получить дату начала отчетного периода
 * @return
 */
def getReportPeriodStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

/**
 * Получить дату окончания отчетного периода
 * @return
 */
def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

/**
 *
 * @param refBookId
 * @param whereClause
 * @return
 */
def getRefBookByRecordWhere(def long refBookId, def whereClause) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
    if (refBookMap == null || refBookMap.size() == 0) {
        //throw new ScriptException("Не найдены записи справочника " + refBookId)
        return Collections.emptyMap();
    }
    return refBookMap
}


/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

/**
 * Разыменование записи справочника
 */
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

Date getDate(String val) {
    if (val != null) {
        if (val != "") {
            return new java.sql.Date(Date.parse(PATTERN_DATE_FORMAT, val).getTime())
        }
    }
    return null
}

Integer getInteger(String val) {
    if (val != null) {
        if (val != "") {
            return val.toInteger()
        }
    }
    return null
}

Long getLong(String val) {
    if (val != null) {
        if (val != "") {
            return val.toLong()
        }
    }
    return null
}

Double getDouble(String val) {
    if (val != null) {
        if (val != "") {
            return val.toDouble()
        }
    }
    return null
}

/**
 * Получить "Физические лица"
 * @param personIds
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersons(def personIds) {
    if (personsCache.isEmpty()) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)
        }
    }
    return personsCache;
}

/**
 * Получить аутальные записи справочника "Физические лица"
 * @param personIds
 * @return
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersons(def personIds) {
    if (personsActualCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
        refBookMap.each { personId, person ->
            // Получим актуальную версию на основании RECORD_ID найденной записи
            def actualPersonMap = getRefBookByFilter(REF_BOOK_PERSON_ID, "RECORD_ID = " + person.get(RF_RECORD_ID).value)
            if (actualPersonMap) {
                actualPersonMap.each { actualPerson ->
                    personsActualCache.put(personId, actualPerson)
                }
            }
        }
    }
    return personsActualCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 * todo Получение ДУЛ реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefDul(def personIds) {
    if (dulCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_DOC_ID, "PERSON_ID = " + personId.toString())
            def dulList = []
            refBookMap.each { refBook ->
                dulList.add(refBook)
            }
            dulCache.put(personId, dulList)
        }
    }
    return dulCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
def getRefDulByDeclarationDataId() {

        if (dulCache.isEmpty()) {
            Long declarationDataId = declarationData.id;
            String whereClause = String.format("person_id in(select person_id from raschsv_pers_sv_strah_lic where declaration_data_id = %s)", declarationDataId)
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_DOC.getId(), whereClause)

            refBookMap.each { personId, refBookValues ->
                Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
                def dulList = dulCache.get(refBookPersonId);
                if (dulList == null) {
                    dulList = [];
                    dulCache.put(refBookPersonId, dulList)
                }
                dulList.add(refBookValues);
            }
        }
        return dulCache;
}




/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Получить все актуальные записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getActualRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(new Date(), null, null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
 * @param refBookId - идентификатор справочника
 * @param recordIds - коллекция идентификаторов (поле id) записей справочника
 * @return - возвращает мапу
 */
def getRefBookByRecordIds(def long refBookId, def recordIds) {
    def refBookMap = getProvider(refBookId).getRecordData(recordIds)
    if (refBookMap == null || refBookMap.size() == 0) {
        throw new ScriptException("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookMap
}

/**
 * Получить все записи справочника по его идентификатору и фильтру (отсутствие значений не является ошибкой)
 * @param refBookId - идентификатор справочника
 * @param filter - фильтр
 * @return - возвращает лист
 */
def getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    return refBookList
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getRefOkvedCode() {
    if (okvedCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_OKVED_ID)
        refBookMap.each { refBook ->
            okvedCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return okvedCodeCache;
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getRefActualOkvedCode() {
    if (okvedCodeActualCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_OKVED_ID)
        refBookMap.each { refBook ->
            okvedCodeActualCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return okvedCodeActualCache;
}

/**
 * Получить "Коды форм реорганизации и ликвидации"
 * @return
 */
def getRefReorgFormCode() {
    if (reorgFormCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_REORGANIZATION_ID)
        refBookMap.each { refBook ->
            reorgFormCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return reorgFormCodeCache;
}

/**
 * Получить "Коды мест предоставления документа"
 * @return
 */
def getRefPresentPlace() {
    if (presentPlaceCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_PRESENT_PLACE_ID)
        refBookMap.each { refBook ->
            presentPlaceCodeCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return presentPlaceCodeCache
}

/**
 * Получить актуальные "Коды мест предоставления документа"
 * @return
 */
def getActualRefPresentPlace() {
    if (presentPlaceCodeActualCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_PRESENT_PLACE_ID)
        refBookMap.each { refBook ->
            presentPlaceCodeActualCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return presentPlaceCodeActualCache
}

/**
 * Получить "Коды тарифа плательщика"
 * @return
 */
def getActualTariffPayerCode() {
    if (tariffPayerCodeActualCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_TARIFF_PAYER_ID)
        refBookMap.each { refBook ->
            tariffPayerCodeActualCache.put(refBook?.CODE?.stringValue, refBook)
        }
    }
    return tariffPayerCodeActualCache
}

/**
 * Получить "Основания заполнения сумм страховых взносов"
 * @return
 */
def getActualFillBaseCode() {
    if (fillBaseCodeCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_FILL_BASE_ID)
        refBookMap.each { refBook ->
            fillBaseCodeCache.add(refBook?.CODE?.stringValue)
        }
    }
    return fillBaseCodeCache
}

/**
 * Получить "Коды классов условий труда"
 * @return
 */
def getActualHardWork() {
    if (hardWorkCodeCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_HARD_WORK_ID)
        refBookMap.each { refBook ->
            hardWorkCodeCache.add(refBook?.CODE?.stringValue)
        }
    }
    return hardWorkCodeCache
}

/**
 * Получить "Коды категорий застрахованных лиц"
 * @return
 */
def getActualPersonCategory() {
    if (personCategoryCodeCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_PERSON_CATEGORY_ID)
        refBookMap.each { refBook ->
            personCategoryCodeCache.add(refBook?.CODE?.stringValue)
        }
    }
    return personCategoryCodeCache
}

/**
 * По коду документа найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findDocumentTypeByCode(code) {
    Map<Long, String> documentTypeMap = getRefDocument()
    def result = documentTypeMap.find {
        it.value?.equalsIgnoreCase(code)
    }?.key;
    if (code != null && !code.isEmpty() && result == null) {
        logger.warn("В справочнике 'Виды документов' не найдена запись, вид документа с кодом " + code);
    }
    return result;
}

/**
 * Получить "Коды видов документов, удостоверяющих личность"
 * @return
 */
def getRefDocument() {
    if (documentCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_CODES_ID)
        refBookList.each { refBook ->
            documentCodesCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return documentCodesCache;
}

/**
 * Получить актуальные "Коды видов документов, удостоверяющих личность"
 * @return
 */
def getActualRefDocument() {
    if (documentCodesActualCache.size() == 0) {
        def refBookList = getActualRefBook(REF_BOOK_DOCUMENT_CODES_ID)
        refBookList.each { refBook ->
            documentCodesActualCache.add(refBook?.CODE?.stringValue)
        }
    }
    return documentCodesActualCache;
}


/**
 * Получить "Страны"
 * @return
 */
def getRefCitizenship() {
    if (citizenshipCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_COUNTRY_ID)
        refBookMap.each { refBook ->
            citizenshipCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return citizenshipCache;
}

/**
 * Получить актуальные "Страны"
 * @return
 */
def getActualRefCitizenship() {
    if (citizenshipActualCache.size() == 0) {
        def refBookMap = getActualRefBook(REF_BOOK_COUNTRY_ID)
        refBookMap.each { refBook ->
            citizenshipActualCache.add(refBook?.CODE?.stringValue)
        }
    }
    return citizenshipActualCache;
}