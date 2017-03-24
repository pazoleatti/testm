package form_template.fond.primary_1151111.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.IdentityObject
import com.aplana.sbrf.taxaccounting.model.PersonData
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.dao.identification.*
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.dao.identification.*

import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.raschsv.*
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field
import groovy.transform.Memoized
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.*
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.tools.DocGenerator

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import org.w3c.dom.Document

import javax.script.ScriptException
import java.awt.Color
import java.sql.ResultSet
import java.sql.SQLException;
import java.util.List

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
        calculate()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break;
    case FormDataEvent.CHECK:
        println "!CHECK!"
        checkData()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        // Подготовка для последующего формирования спецотчета
        println "!PREPARE_SPECIFIC_REPORT!"
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        // Формирование спецотчета
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

/**
 * Идентификатор шаблона РНУ-НДФЛ (первичная)
 */
@Field final int PRIMARY_1151111_TEMPLATE_ID = 200

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
@Field def dulActualCache = [:]
@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// Виды документов, удостоверяющих личность
@Field final long REF_BOOK_DOCUMENT_CODES_ID = RefBook.Id.DOCUMENT_CODES.id

// Страны
@Field final long REF_BOOK_COUNTRY_ID = RefBook.Id.COUNTRY.id

// Физ. лица
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]
@Field Map<Long, Map<String, RefBookValue>> personsActualCache = [:]
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id

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
@Field final String OSS_VNM = "П2.Расчет ОСС  ВНМ"
@Field final String OSS_ZAK = "П3.Расходы на ОСС Зак."
@Field final String FED_BUD = "П4.Выплаты за счет Фед.Бюдж."
@Field final String RAS_3T_427 = "П5.Расчет пп.3 п.1 ст.427"
@Field final String RAS_5T_427 = "П6.Расчет пп.5 п.1 ст.427"
@Field final String RAS_7T_427 = "П7.Расчет пп.7 п.1 ст.427"
@Field final String RAS_9T_427 = "П8.Сведения пп.9 п.1 ст.427"
@Field final String SVE_222_425 = "П9.Сведения а.2 пп.2 п.2 ст.425"
@Field final String SVE_13_422 = "П10.Сведния пп.1 п.3 ст.422"

// Имена псевдонима спецотчета
@Field final String PERSON_REPORT = "person_rep_param"
@Field final String CONSOLIDATED_REPORT = "consolidated_report"

@Field final Color ROWS_FILL_COLOR = new Color(255, 243, 203)
@Field final Color TOTAL_ROW_FILL_COLOR = new Color(186, 208, 80)

@Field final String PERSONAL_DATA_TOTAL_ROW_LABEL = "Всего за последние три месяца расчетного (отчетного) периода"

@Field final String TRANSPORT_FILE_TEMPLATE = "ТФ"

// TODO долго на 20к
// TODO snisl в fillRasch9st427Row
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

        logger.info("Заполнение листа \"Персониф. Сведения\"")
        fillPersSvConsSheet(workbook)

        logger.info("Заполнение листа \"Суммы страховых взносов\"")
        fillSumStrahVzn(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П1.Расчет ОПС ОМС\"")
        fillOpsOms(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П2.Расчет ОСС ВНМ\"")
        fillOssVnm(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П3.Расходы на ОСС Зак.\"")
        fillOssZak(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П4.Выплаты за счет Фед.Бюдж.\"")
        fillFedBud(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П5.Расчет пп.3 п.1 ст.427\"")
        fillRasch3st427(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П6.Расчет пп.5 п.1 ст.427\"")
        fillRasch5st427(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П7.Расчет пп.7 п.1 ст.427\"")
        fillRasch7st427(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П8.Расчет пп.9 п.1 ст.427\"")
        fillRasch9st427(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П9.Сведения а.2 пп.2 п.2 ст.425\"")
        fillRasch22425(raschsvObyazPlatSv, workbook)

        logger.info("Заполнение листа \"П10.Сведния пп.1 п.3 ст.422\"")
        fillRasch13422(raschsvObyazPlatSv, workbook)

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
List<RaschsvPersSvStrahLic> getrRaschsvPersSvStrahLicList(int startIndex, int pageSize) {
    def declarationId = declarationData.getId()
    def params = scriptSpecificReportHolder.getSubreportParamValues()
    return raschsvPersSvStrahLicService.findPersonBySubreportParams(declarationId, params, startIndex, pageSize)
}

def prepareSpecificReport() {
    PrepareSpecificReportResult result = new PrepareSpecificReportResult();
    List<Column> tableColumns = createTableColumns();
    List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    def rowColumns = createRowColumns()

    // Ограничение числа выводимых записей
    int startIndex = 1
    int pageSize = 10

    List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = getrRaschsvPersSvStrahLicList(startIndex, pageSize)

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
    // Дата формирования формы
    println declarationData.id
    def declarationDataFile = declarationService.findFilesWithSpecificType(declarationData.id, TRANSPORT_FILE_TEMPLATE).get(0)
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

    // Место предоставления
    def poMestuCodeParam = poMestuParam?.get(RF_CODE)?.value

    // Сведения о файле
    sheet.getRow(3).getCell(1).setCellValue(declarationData.fileName)
    sheet.getRow(4).getCell(1).setCellValue(applicationVersion)
    sheet.getRow(5).getCell(1).setCellValue("5.01")

    // Сведения о документе
    sheet.getRow(8).getCell(1).setCellValue("1151111")
    sheet.getRow(9).getCell(1).setCellValue(declarationDataFile.date.format("dd.MM.yyyy"))
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
def fillOpsOms(raschsvObyazPlatSv, workbook) {
    raschsvObyazPlatSv.raschsvSvOpsOmsList.each { svOpsOms ->
        def defaultSheetIndex = workbook.getSheetIndex(OPS_OMS)
        def sheet = workbook.cloneSheet(defaultSheetIndex)
        workbook.setSheetOrder(sheet.sheetName, defaultSheetIndex)
        workbook.setSheetName(defaultSheetIndex, OPS_OMS + " " + svOpsOms.tarifPlat)

        fillSingleRow(sheet, 7, svOpsOms.tarifPlat)

        // Расчет сумм взносов на обязательное пенсионное страхование
        def raschsvSvOpsRasch = svOpsOms.raschsvSvOpsOmsRaschList.find {NODE_NAME_RASCH_SV_OPS == it.nodeName}

        def raschsvSvOpsRaschKolOverall = raschsvSvOpsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_STRAH_LIC_VS == it.nodeName}?.raschsvKolLicTip
        def raschsvSvOpsRaschKolNach = raschsvSvOpsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_LIC_NACH_SV_VS == it.nodeName}?.raschsvKolLicTip
        def raschsvSvOpsRaschKolBas = raschsvSvOpsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_PREV_BAZ_OPS == it.nodeName}?.raschsvKolLicTip
        fillKolRow(sheet, 14, raschsvSvOpsRaschKolOverall)
        fillKolRow(sheet, 15, raschsvSvOpsRaschKolNach)
        fillKolRow(sheet, 16, raschsvSvOpsRaschKolBas)

        def raschsvSvOpsRaschSumNachislFl= raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_VYPL_NACHISL_FL == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumOblozen = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NE_OBLOZEN_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumBazNachisl = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumBazPrevysh = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_PREVYSH_OPS == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumNachisl = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumNachislNePrev = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV_NE_PREV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOpsRaschSumNachislPrev = raschsvSvOpsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NACHISL_SV_PREV == it.nodeName}?.raschsvSvSum1Tip
        fillSumRow(sheet, 21, raschsvSvOpsRaschSumNachislFl)
        fillSumRow(sheet, 22, raschsvSvOpsRaschSumOblozen)
        fillSumRow(sheet, 23, raschsvSvOpsRaschSumBazNachisl)
        fillSumRow(sheet, 24, raschsvSvOpsRaschSumBazPrevysh)
        fillSumRow(sheet, 25, raschsvSvOpsRaschSumNachisl)
        fillSumRow(sheet, 26, raschsvSvOpsRaschSumNachislNePrev)
        fillSumRow(sheet, 27, raschsvSvOpsRaschSumNachislPrev)

        // Расчет сумм взносов на обязательное медицинское страхование
        def raschsvSvOmsRasch = svOpsOms.raschsvSvOpsOmsRaschList.find {NODE_NAME_RASCH_SV_OMS == it.nodeName}

        def raschsvSvOmsRaschKolOverall = raschsvSvOmsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_STRAH_LIC_VS == it.nodeName}?.raschsvKolLicTip
        def raschsvSvOmsRaschKolNach = raschsvSvOmsRasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_LIC_NACH_SV_VS == it.nodeName}?.raschsvKolLicTip
        fillKolRow(sheet, 34, raschsvSvOmsRaschKolOverall)
        fillKolRow(sheet, 35, raschsvSvOmsRaschKolNach)

        def raschsvSvOmsRaschSumNachislFl = raschsvSvOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_VYPL_NACHISL_FL == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOmsRaschSumOblozen = raschsvSvOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NE_OBLOZEN_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOmsRaschSumBazNachisl = raschsvSvOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvOmsRaschSumBazPrevysh = raschsvSvOmsRasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_PREVYSH_OPS == it.nodeName}?.raschsvSvSum1Tip
        fillSumRow(sheet, 40, raschsvSvOmsRaschSumNachislFl)
        fillSumRow(sheet, 41, raschsvSvOmsRaschSumOblozen)
        fillSumRow(sheet, 42, raschsvSvOmsRaschSumBazNachisl)
        fillSumRow(sheet, 43, raschsvSvOmsRaschSumBazPrevysh)

        // Расчет сумм страховых взносов по дополнительному тарифу... указанных в пунктах 1 и 2 статьи 428 Налогового кодекса
        def raschsvSvDop428Rasch = svOpsOms.raschsvSvOpsOmsRaschList.find {NODE_NAME_RASCH_SV_428_12 == it.nodeName}

        fillSingleRow(sheet, 49, raschsvSvDop428Rasch?.prOsnSvDop)

        def raschsvSvDop428RaschKolOverall = raschsvSvDop428Rasch.raschsvSvOpsOmsRaschKolList.find {NODE_NAME_KOL_STRAH_LIC_VS == it.nodeName}?.raschsvKolLicTip
        fillKolRow(sheet, 54, raschsvSvDop428RaschKolOverall)

        def raschsvSvDop428RaschSumNachislFl = raschsvSvDop428Rasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_VYPL_NACHISL_FL == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvDop428RaschSumOblozen = raschsvSvDop428Rasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_NE_OBLOZEN_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvDop428RaschSumBazNachisl = raschsvSvDop428Rasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
        def raschsvSvDop428RaschSumBazPrevysh = raschsvSvDop428Rasch.raschsvSvOpsOmsRaschSumList.find {NODE_NAME_BAZ_PREVYSH_OPS == it.nodeName}?.raschsvSvSum1Tip
        fillSumRow(sheet, 59, raschsvSvDop428RaschSumNachislFl)
        fillSumRow(sheet, 60, raschsvSvDop428RaschSumOblozen)
        fillSumRow(sheet, 61, raschsvSvDop428RaschSumBazNachisl)
        fillSumRow(sheet, 62, raschsvSvDop428RaschSumBazPrevysh)
    }

    // Удаляем старый лист, из которого клонировали
    if (raschsvObyazPlatSv.raschsvSvOpsOmsList.size() > 0) {
        def defaultSheetIndex = workbook.getSheetIndex(OPS_OMS)
        workbook.removeSheetAt(defaultSheetIndex)
    }
}


/**
 * Заполняет данными лист "Расчет ОПС ОМС"
 */
def fillOssVnm(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(OSS_VNM)

    def raschsvOssVnm = raschsvObyazPlatSv?.raschsvOssVnm
    fillSingleRow(sheet, 7, raschsvOssVnm?.prizVypl)

    // Количество лиц
    def raschsvSvOssVnmKolOverall = raschsvOssVnm?.raschsvOssVnmKolList?.find {NODE_NAME_KOL_STRAH_LIC_VS == it.nodeName}?.raschsvKolLicTip

    fillKolRow(sheet, 14, raschsvSvOssVnmKolOverall)

    // Суммы
    def raschsvSvOssVnmSumNachislFl = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_VYPL_NACHISL_FL == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumOblozen = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_NE_OBLOZEN_SV == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazPrevysh = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_PREVYSH_SV == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazNachisl = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazNachislFarm = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_NACHISL_SV_FARM == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazNachEs = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_NACHISL_SV_NACH_ES == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazNachislPlat = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_NACHISL_SV_PAT == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumBazInLic = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_BAZ_NACHISL_IN_LIC == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumNachisl = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_NACHISL_SV == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumProizv = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_PROIZV_RASCH_SO == it.nodeName}?.raschsvSvSum1Tip
    def raschsvSvOssVnmSumVozm = raschsvOssVnm?.raschsvOssVnmSumList?.find {NODE_NAME_VOZM_RASCH_SO == it.nodeName}?.raschsvSvSum1Tip

    fillSumRow(sheet, 19, raschsvSvOssVnmSumNachislFl)
    fillSumRow(sheet, 20, raschsvSvOssVnmSumOblozen)
    fillSumRow(sheet, 21, raschsvSvOssVnmSumBazPrevysh)
    fillSumRow(sheet, 22, raschsvSvOssVnmSumBazNachisl)
    fillSumRow(sheet, 23, raschsvSvOssVnmSumBazNachislFarm)
    fillSumRow(sheet, 24, raschsvSvOssVnmSumBazNachEs)
    fillSumRow(sheet, 25, raschsvSvOssVnmSumBazNachislPlat)
    fillSumRow(sheet, 26, raschsvSvOssVnmSumBazInLic)
    fillSumRow(sheet, 27, raschsvSvOssVnmSumNachisl)
    fillSumRow(sheet, 28, raschsvSvOssVnmSumProizv)
    fillSumRow(sheet, 29, raschsvSvOssVnmSumVozm)

    // Сумма страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)
    def raschsvUplVsego =  raschsvOssVnm?.raschsvUplSvPrevList?.find {NODE_NAME_UPL_VSEGO_PER == it.nodeName}
    def raschsvUplVsego3m =  raschsvOssVnm?.raschsvUplSvPrevList?.find {NODE_NAME_UPL_VSEGO_POSL3M == it.nodeName}
    def raschsvUpl1Vsego3m = raschsvOssVnm?.raschsvUplSvPrevList?.find {NODE_NAME_UPL1_VSEGO_POSL3M == it.nodeName}
    def raschsvUpl2Vsego3m = raschsvOssVnm?.raschsvUplSvPrevList?.find {NODE_NAME_UPL2_VSEGO_POSL3M == it.nodeName}
    def raschsvUpl3Vsego3m = raschsvOssVnm?.raschsvUplSvPrevList?.find {NODE_NAME_UPL3_VSEGO_POSL3M == it.nodeName}

    fillPriznakRow(sheet, 36, raschsvUplVsego)
    fillPriznakRow(sheet, 37, raschsvUplVsego3m)
    fillPriznakRow(sheet, 38, raschsvUpl1Vsego3m)
    fillPriznakRow(sheet, 39, raschsvUpl2Vsego3m)
    fillPriznakRow(sheet, 40, raschsvUpl3Vsego3m)
}

/**
 * Заполняет данными лист "Выплаты за счет Фед.Бюдж."
 */
def fillFedBud(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(FED_BUD)

    def raschsvVyplFinFb = raschsvObyazPlatSv?.raschsvVyplFinFb

    // пострадавшим вследствие катастрофы на Чернобыльской АЭС
    def raschsvVyplChernobyl = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_CHERNOBYL == it.nodeName}
    fillFedBudRow(sheet, 10, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 11, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD == it.nodeName})
    fillFedBudRow(sheet, 12, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB == it.nodeName})
    fillFedBudRow(sheet, 13, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB1 == it.nodeName})
    fillFedBudRow(sheet, 14, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB2 == it.nodeName})
    fillFedBudRow(sheet, 15, raschsvVyplChernobyl?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})

    // пострадавшим вследствие аварии на производственном объединении «Маяк»
    def raschsvVyplMayk = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_MAYK == it.nodeName}
    fillFedBudRow(sheet, 20, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 21, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD == it.nodeName})
    fillFedBudRow(sheet, 22, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB == it.nodeName})
    fillFedBudRow(sheet, 23, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB1 == it.nodeName})
    fillFedBudRow(sheet, 24, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB2 == it.nodeName})
    fillFedBudRow(sheet, 25, raschsvVyplMayk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})

    // пострадавшим вследствие ядерных испытаний на Семипалатинском полигоне
    def raschsvVyplPolygon = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_POLYGON == it.nodeName}
    fillFedBudRow(sheet, 30, raschsvVyplPolygon?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 31, raschsvVyplPolygon?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})

    // вследствие радиационных аварий, кроме Чернобыльской АЭС
    def raschsvVyplPodrOsobRisk = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_PODR_OSOB_RISK == it.nodeName}
    fillFedBudRow(sheet, 36, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 37, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD == it.nodeName})
    fillFedBudRow(sheet, 38, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB == it.nodeName})
    fillFedBudRow(sheet, 39, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB1 == it.nodeName})
    fillFedBudRow(sheet, 40, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB2 == it.nodeName})
    fillFedBudRow(sheet, 41, raschsvVyplPodrOsobRisk?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})

    // Дополнительные выплаты пособий
    def raschsvVyplDop = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_DOP == it.nodeName}
    fillFedBudRow(sheet, 46, raschsvVyplDop?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 47, raschsvVyplDop?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD == it.nodeName})
    fillFedBudRow(sheet, 48, raschsvVyplDop?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})

    // Итого
    def raschsvVyplVsego = raschsvVyplFinFb?.raschsvVyplPrichinaList?.find {VYPL_FIN_FB_SV_VNF_VSEGO == it.nodeName}

    fillSingleRow(sheet, 51, raschsvVyplVsego?.svVnfUhodInv)

    fillFedBudRow(sheet, 54, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VR_NETR == it.nodeName})
    fillFedBudRow(sheet, 55, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD == it.nodeName})
    fillFedBudRow(sheet, 56, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB == it.nodeName})
    fillFedBudRow(sheet, 57, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB1 == it.nodeName})
    fillFedBudRow(sheet, 58, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_YHOD_REB2 == it.nodeName})
    fillFedBudRow(sheet, 59, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_OPL_DOP_VYH_INV == it.nodeName})
    fillFedBudRow(sheet, 60, raschsvVyplVsego?.raschsvRashVyplList?.find {VYPL_FIN_FB_SV_VNF_POS_VSEGO == it.nodeName})
}

/**
 * Заполняет данными лист "Расходы на ОСС Зак."
 */
def fillOssZak(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(OSS_ZAK)

    def raschsvRashOssZak = raschsvObyazPlatSv?.raschsvRashOssZak

    def vyplPosVrNetr = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ПосВрНетр" == it.nodeName}
    def posVrNetrSov = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ПосВрНетрСов" == it.nodeName}
    def posVrNetrIn = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ПосВрНетрИн" == it.nodeName}
    def posVrNetrInSov = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ПосВрНетрИнСов" == it.nodeName}
    def beremRod = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "БеремРод" == it.nodeName}
    def beremRodSov = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "БеремРодСов" == it.nodeName}
    def edPosRanBerem = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ЕдПосРанБерем" == it.nodeName}
    def edPosRojd = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ЕдПосРожд" == it.nodeName}
    def ejPosYhodReb = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ЕжПосУходРеб" == it.nodeName}
    def ejPosYhodReb1 = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ЕжПосУходРеб1" == it.nodeName}
    def ejPosYhodReb2 = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ЕжПосУходРеб2" == it.nodeName}
    def oplDopVyhInv = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "ОплДопВыхИнв" == it.nodeName}
    def cvdDopYhodInv = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "СВДопУходИнв" == it.nodeName}
    def cocPsPogreb = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "СоцПосПогреб" == it.nodeName}
    def itogo = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "Итого" == it.nodeName}
    def nachNevyplPos = raschsvRashOssZak?.raschsvRashOssZakRashList?.find { "НачНевыплПос" == it.nodeName}

    fillOssZak(sheet, 8, vyplPosVrNetr)
    fillOssZak(sheet, 9, posVrNetrSov)
    fillOssZak(sheet, 10, posVrNetrIn)
    fillOssZak(sheet, 11, posVrNetrInSov)
    fillOssZak(sheet, 12, beremRod)
    fillOssZak(sheet, 13, beremRodSov)
    fillOssZak(sheet, 14, edPosRanBerem)
    fillOssZak(sheet, 15, edPosRojd)
    fillOssZak(sheet, 16, ejPosYhodReb)
    fillOssZak(sheet, 17, ejPosYhodReb1)
    fillOssZak(sheet, 18, ejPosYhodReb2)
    fillOssZak(sheet, 19, oplDopVyhInv)
    fillOssZak(sheet, 20, cvdDopYhodInv)
    fillOssZak(sheet, 21, cocPsPogreb)
    fillOssZak(sheet, 22, itogo)
    fillOssZak(sheet, 24, nachNevyplPos)
}

/**
 * Заполняет данными лист
 *  "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 *  указанными в подпункте 3 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch3st427(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(RAS_3T_427)
    sheet.shiftRows(10, 12, 1)
    fillRasch3st427Row(sheet, sheet.createRow(12), raschsvObyazPlatSv?.raschsvPravTarif31427)
}

/**
 * Заполняет данными лист
 *  "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 *  указанными в подпункте 5 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch5st427(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(RAS_5T_427)
    sheet.shiftRows(9, 10, 1)
    fillRasch5st427Row(sheet, sheet.createRow(9), raschsvObyazPlatSv?.raschsvPravTarif51427)
}

/**
 * Заполняет данными лист
 *  "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 *  указанными в подпункте 7 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch7st427(raschsvObyazPlatSv, workbook) {
    def sheet = workbook.getSheet(RAS_7T_427)
    sheet.shiftRows(11, 12, 1)
    fillRasch7st427Row(sheet, sheet.createRow(11), raschsvObyazPlatSv?.raschsvPravTarif71427)
}

/**
 * Заполняет данными лист
 *  "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 *  указанными в подпункте 9 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch9st427(raschsvObyazPlatSv, workbook) {
    def startIndex = 10
    def sheet = workbook.getSheet(RAS_9T_427)
    def raschsvSvPrimTarif91427 = raschsvObyazPlatSv?.raschsvSvPrimTarif91427
    def raschsvSvedPatentList = raschsvSvPrimTarif91427?.raschsvSvedPatentList

    if (raschsvSvedPatentList) {
        sheet.shiftRows(startIndex, startIndex + 1, raschsvSvedPatentList.size() + 1)
        for (int i = 0; i < raschsvSvedPatentList.size(); i++) {
            fillRasch9st427Row(sheet, sheet.createRow(startIndex + i), raschsvSvedPatentList.get(i))
        }
    }
}

/**
 * Заполняет данными лист:
 * "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта
 * 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426)
 * Налогового кодекса Российской Федерации"
 */
def fillRasch22425(raschsvObyazPlatSv, workbook) {
    def startIndex = 10
    def sheet = workbook.getSheet(SVE_222_425)
    def raschsvSvPrimTarif22425 = raschsvObyazPlatSv?.raschsvSvPrimTarif22425
    def raschsvSvInoGrazdList = raschsvSvPrimTarif22425?.raschsvSvInoGrazdList

    if (raschsvSvInoGrazdList) {
        sheet.shiftRows(startIndex, startIndex + 1, raschsvSvInoGrazdList.size() + 1)
        for (int i = 0; i < raschsvSvInoGrazdList.size(); i++) {
            fillRasch22425Row(sheet, sheet.createRow(startIndex + i), raschsvSvInoGrazdList.get(i))
        }
    }
}

/**
 * Заполняет данными лист:
 * "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта
 * 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426)
 * Налогового кодекса Российской Федерации"
 */
def fillRasch13422(raschsvObyazPlatSv, workbook) {
    def startIndex = 9
    def blockShift = 5
    def sheet = workbook.getSheet(SVE_13_422)
    def raschsvSvPrimTarif13422 = raschsvObyazPlatSv?.raschsvSvPrimTarif13422
    def raschsvSvedObuchList = raschsvSvPrimTarif13422?.raschsvSvedObuchList

    if (raschsvSvedObuchList) {
        sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvSvedObuchList.size() + 1)
        for (int i = 0; i < raschsvSvedObuchList.size(); i++) {
            fillRasch13422Row(sheet, sheet.createRow(startIndex + i), raschsvSvedObuchList.get(i))
        }

        int n = startIndex + raschsvSvedObuchList.size() + blockShift
        for (int i = 0; i < raschsvSvedObuchList.size(); i++) {
            def raschsvSvReestrMdoList = raschsvSvedObuchList.get(i).raschsvSvReestrMdoList
            for (int j = 0; j < raschsvSvReestrMdoList.size(); j++) {
                fillRasch13422RowMdo(sheet, sheet.createRow(n++), raschsvSvedObuchList.get(i), raschsvSvReestrMdoList.get(j))
            }
        }
    }
}

/**
 * Заполняет значениями строки для количеств
 */
def fillKolRow(sheet, pointer, kolLicTip) {
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
def fillSumRow(sheet, pointer, sumLicTip) {
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

/**
 * Заполнение одиночной ячейки
 */
def fillSingleRow(sheet, pointer, value) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(value ?: "")
}

/**
 * Заполнение двух ячеек: признак-сумма
 */
def fillPriznakRow(sheet, pointer, raschsvUplSvPrev) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvUplSvPrev?.priznak ?: "")

    def cell2 = sheet.getRow(pointer).createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvUplSvPrev?.svSum ?: "")
}

/**
 * Заполнение строк для:
 * "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
 */
def fillOssZak(sheet, pointer, raschsvRashOssZakRash) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvRashOssZakRash?.chislSluch ?: "")

    def cell2 = sheet.getRow(pointer).createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvRashOssZakRash?.kolVypl ?: "")

    def cell3 = sheet.getRow(pointer).createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvRashOssZakRash?.rashVsego ?: "")

    def cell4 = sheet.getRow(pointer).createCell(4)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvRashOssZakRash?.rashFinFb ?: "")
}

/**
 * Заполнение строк для:
 * "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности ..."
 */
def fillFedBudRow(sheet, pointer, raschsvRashVypl) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = sheet.getRow(pointer).createCell(1)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvRashVypl?.chislPoluch ?: "")

    def cell2 = sheet.getRow(pointer).createCell(2)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvRashVypl?.kolVypl ?: "")

    def cell3 = sheet.getRow(pointer).createCell(3)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvRashVypl?.rashod ?: "")
}


/**
 * Заполнение одиночной строки для:
 * "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 * указанными в подпункте 3 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch3st427Row(sheet, row, raschsvPravTarif31427) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvPravTarif31427?.srChisl9mpr ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvPravTarif31427?.srChislPer ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvPravTarif31427?.doh2489mpr ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvPravTarif31427?.doh248Per ?: "")

    def cell5 = row.createCell(4)
    cell5.setCellStyle(style)
    cell5.setCellValue(raschsvPravTarif31427?.dohKr54279mpr ?: "")

    def cell6 = row.createCell(5)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvPravTarif31427?.dohKr5427Per ?: "")

    def cell7 = row.createCell(6)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvPravTarif31427?.dohDoh54279mpr ?: "")

    def cell8 = row.createCell(7)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvPravTarif31427?.dohDoh5427per ?: "")

    def cell9 = row.createCell(8)
    cell9.setCellStyle(style)
    cell9.setCellValue(raschsvPravTarif31427?.dataZapAkOrg?.format("dd.MM.yyyy") ?: "")

    def cell10 = row.createCell(9)
    cell10.setCellStyle(style)
    cell10.setCellValue(raschsvPravTarif31427?.nomZapAkOrg ?: "")
}

/**
 * Заполнение одиночной строки для:
 * "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 * указанными в подпункте 5 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
*/
def fillRasch5st427Row(sheet, row, raschsvPravTarif51427) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvPravTarif51427?.doh346_15vs ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvPravTarif51427?.doh6_427 ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvPravTarif51427?.dolDoh6_427 ?: "")
}

/**
 * Заполнение одиночной строки для:
 * "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 * указанными в подпункте 7 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch7st427Row(sheet, row, raschsvPravTarif71427) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvPravTarif71427?.dohVsPred ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvPravTarif71427?.dohVsPer ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvPravTarif71427?.dohCelPostPred ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvPravTarif71427?.dohCelPostPer ?: "")

    def cell5 = row.createCell(4)
    cell5.setCellStyle(style)
    cell5.setCellValue(raschsvPravTarif71427?.dohGrantPred ?: "")

    def cell6 = row.createCell(5)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvPravTarif71427?.dohGrantPer ?: "")

    def cell7 = row.createCell(6)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvPravTarif71427?.dohEkDeyatPred ?: "")

    def cell8 = row.createCell(7)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvPravTarif71427?.dohEkDeyatPer ?: "")

    def cell9 = row.createCell(8)
    cell9.setCellStyle(style)
    cell9.setCellValue(raschsvPravTarif71427?.dolDohPred ?: "")

    def cell10 = row.createCell(9)
    cell10.setCellStyle(style)
    cell10.setCellValue(raschsvPravTarif71427?.dolDohPer ?: "")
}

/**
 * Заполнение одиночной строки для:
 * "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками,
 * указанными в подпункте 9 пункта 1 статьи 427 Налогового кодекса Российской Федерации"
 */
def fillRasch9st427Row(sheet, row, raschsvSvedPatent) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvSvedPatent?.nomPatent ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvSvedPatent?.vydDeyatPatent ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvSvedPatent?.dataNachDeyst?.format("dd.MM.yyyy") ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvSvedPatent?.dataKonDeyst?.format("dd.MM.yyyy") ?: "")

    def cell5 = row.createCell(4)
    cell5.setCellStyle(style)
    //TODO
    cell5.setCellValue("" ?: "")

    def cell6 = row.createCell(5)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvSvedPatent?.raschsvSvSum1Tip?.sumVsegoPer ?: "")

    def cell7 = row.createCell(6)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvSvedPatent?.raschsvSvSum1Tip?.sumVsegoPosl3m ?: "")

    def cell8 = row.createCell(7)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvSvedPatent?.raschsvSvSum1Tip?.sum1mPosl3m ?: "")

    def cell9 = row.createCell(8)
    cell9.setCellStyle(style)
    cell9.setCellValue(raschsvSvedPatent?.raschsvSvSum1Tip?.sum2mPosl3m ?: "")

    def cell10 = row.createCell(9)
    cell10.setCellStyle(style)
    cell10.setCellValue(raschsvSvedPatent?.raschsvSvSum1Tip?.sum3mPosl3m ?: "")
}

/**
 * Заполнение одиночной строки для:
 * "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта
 * 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426) Налогового кодекса Российской Федерации"
 */
def fillRasch22425Row(sheet, row, raschsvSvInoGrazd) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvSvInoGrazd?.familia ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvSvInoGrazd?.imya ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvSvInoGrazd?.otchestvo ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvSvInoGrazd?.innfl ?: "")

    def cell5 = row.createCell(4)
    cell5.setCellStyle(style)
    cell5.setCellValue(raschsvSvInoGrazd?.snils ?: "")

    def cell6 = row.createCell(5)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvSvInoGrazd?.raschsvSvSum1Tip?.sumVsegoPer ?: "")

    def cell7 = row.createCell(6)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvSvInoGrazd?.raschsvSvSum1Tip?.sumVsegoPosl3m ?: "")

    def cell8 = row.createCell(7)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvSvInoGrazd?.raschsvSvSum1Tip?.sum1mPosl3m ?: "")

    def cell9 = row.createCell(8)
    cell9.setCellStyle(style)
    cell9.setCellValue(raschsvSvInoGrazd?.raschsvSvSum1Tip?.sum2mPosl3m ?: "")

    def cell10 = row.createCell(9)
    cell10.setCellStyle(style)
    cell10.setCellValue(raschsvSvInoGrazd?.raschsvSvSum1Tip?.sum3mPosl3m ?: "")
}

/**
 * Заполнение одиночной строки для:
 * Сведения, необходимые для применения положений подпункта 1 пункта 3 статьи 422
 */
def fillRasch13422Row(sheet, row, raschsvSvedObuch) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvSvedObuch?.unikNomer ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvSvedObuch?.familia ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvSvedObuch?.imya ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvSvedObuch?.otchestvo ?: "")

    def cell5 = row.createCell(4)
    cell5.setCellStyle(style)
    cell5.setCellValue("-")

    def cell6 = row.createCell(5)
    cell6.setCellStyle(style)
    cell6.setCellValue(raschsvSvedObuch?.spravNomer ?: "")

    def cell7 = row.createCell(6)
    cell7.setCellStyle(style)
    cell7.setCellValue(raschsvSvedObuch?.spravData?.format("dd.MM.yyyy") ?: "")

    def cell8 = row.createCell(7)
    cell8.setCellStyle(style)
    cell8.setCellValue(raschsvSvedObuch?.raschsvSvSum1Tip?.sumVsegoPer ?: "")

    def cell9 = row.createCell(8)
    cell9.setCellStyle(style)
    cell9.setCellValue(raschsvSvedObuch?.raschsvSvSum1Tip?.sumVsegoPosl3m ?: "")

    def cell10 = row.createCell(9)
    cell10.setCellStyle(style)
    cell10.setCellValue(raschsvSvedObuch?.raschsvSvSum1Tip?.sum1mPosl3m ?: "")

    def cell11 = row.createCell(10)
    cell11.setCellStyle(style)
    cell11.setCellValue(raschsvSvedObuch?.raschsvSvSum1Tip?.sum2mPosl3m ?: "")

    def cell12 = row.createCell(11)
    cell12.setCellStyle(style)
    cell12.setCellValue(raschsvSvedObuch?.raschsvSvSum1Tip?.sum3mPosl3m ?: "")
}

/**
 * Заполнение одиночной строки для:
 * Сведения, необходимые для применения положений подпункта 1 пункта 3 статьи 422
 */
def fillRasch13422RowMdo(sheet, row, raschsvSvedObuch, raschsvSvedObuchMdo) {
    def style = normalWithBorderStyle(sheet.getWorkbook())
    addFillingToStyle(style, ROWS_FILL_COLOR)

    def cell1 = row.createCell(0)
    cell1.setCellStyle(style)
    cell1.setCellValue(raschsvSvedObuch?.unikNomer ?: "")

    def cell2 = row.createCell(1)
    cell2.setCellStyle(style)
    cell2.setCellValue(raschsvSvedObuchMdo?.naimMdo ?: "")

    def cell3 = row.createCell(2)
    cell3.setCellStyle(style)
    cell3.setCellValue(raschsvSvedObuchMdo?.dataZapis?.format("dd.MM.yyyy") ?: "")

    def cell4 = row.createCell(3)
    cell4.setCellStyle(style)
    cell4.setCellValue(raschsvSvedObuchMdo?.nomerZapis ?: "")
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
@Field final NODE_NAME_BAZ_PREVYSH_SV = "БазПревышСВ"
@Field final NODE_NAME_BAZ_PREVYSH_OPS = "БазПревышОПС"
@Field final NODE_NAME_NACHISL_SV = "НачислСВ"
@Field final NODE_NAME_VOZM_RASH_SO = "ВозмРасхСО"
@Field final NODE_NAME_NACHISL_SV_NE_PREV = "НачислСВНеПрев"
@Field final NODE_NAME_NACHISL_SV_PREV = "НачислСВПрев"
@Field final NODE_NAME_BAZ_NACHISL_SV_DOP = "БазНачислСВДоп"
@Field final NODE_NAME_BAZ_NACHISL_SV_FARM = "БазНачСВФарм"
@Field final NODE_NAME_BAZ_NACHISL_SV_NACH_ES = "БазНачСВЧлЭС"
@Field final NODE_NAME_BAZ_NACHISL_SV_PAT = "БазНачСВПат"
@Field final NODE_NAME_BAZ_NACHISL_IN_LIC = "БазНачСВИнЛиц"
@Field final NODE_NAME_PROIZV_RASCH_SO = "ПроизвРасхСО"
@Field final NODE_NAME_VOZM_RASCH_SO = "ВозмРасхСО"
@Field final NODE_NAME_NACHISL_SV_DOP = "НачислСВДоп"
@Field final NODE_NAME_KOL_LIC_NACH_SV = "КолЛицНачСВ"
@Field final NODE_NAME_BAZ_NACHISL_SVDSO = "БазНачислСВДСО"
@Field final NODE_NAME_NACHISL_SVDSO = "НачислСВДСО"

@Field final NODE_NAME_RASCH_SV_OSS_VNM = "РасчСВ_ОСС.ВНМ"
@Field final NODE_NAME_UPL_SV_PREV = "УплСВПрев"
@Field final NODE_NAME_UPL_VSEGO_PER = "УплВсегоПер"
@Field final NODE_NAME_UPL_VSEGO_POSL3M = "УплВсегоПосл3М"
@Field final NODE_NAME_UPL1_VSEGO_POSL3M = "Упл1Посл3М"
@Field final NODE_NAME_UPL2_VSEGO_POSL3M = "Упл2Посл3М"
@Field final NODE_NAME_UPL3_VSEGO_POSL3M = "Упл3Посл3М"

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
@Field final VYPL_FIN_FB_SV_VNF_CHERNOBYL = "ЧернобАЭС"
@Field final VYPL_FIN_FB_SV_VNF_MAYK = "ПОМаяк"
@Field final VYPL_FIN_FB_SV_VNF_POLYGON = "СемипалатПолигон"
@Field final VYPL_FIN_FB_SV_VNF_PODR_OSOB_RISK = "ПодрОсобРиск"
@Field final VYPL_FIN_FB_SV_VNF_DOP = "ДопФЗ255"
@Field final VYPL_FIN_FB_SV_VNF_VSEGO = "Всего"

@Field final VYPL_FIN_FB_SV_VNF_POS_VR_NETR = "ПосВрНетр"
@Field final VYPL_FIN_FB_SV_VNF_POS_BEREM_ROD = "ПосБеремРод"
@Field final VYPL_FIN_FB_SV_VNF_POS_YHOD_REB = "ЕжПосУходРеб"
@Field final VYPL_FIN_FB_SV_VNF_POS_YHOD_REB1 = "ЕжПосУходРеб1"
@Field final VYPL_FIN_FB_SV_VNF_POS_YHOD_REB2 = "ЕжПосУходРеб2"
@Field final VYPL_FIN_FB_SV_VNF_OPL_DOP_VYH_INV = "ОплДопВыхИнв"
@Field final VYPL_FIN_FB_SV_VNF_POS_VSEGO = "Всего"


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

    ScriptUtils.checkInterrupted();

    // Скопируем поток
    byte[] content = IOUtils.toByteArray(ImportInputStream)

    // Проверим кодировку
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
    DocumentBuilder documentBuilder = factory.newDocumentBuilder()
    Document document = documentBuilder.parse(new ByteArrayInputStream(content));
    if (document.getXmlEncoding().toLowerCase() != "windows-1251") {
        logger.error("Файл '$UploadFileName' сформирован в кодировке отличной от 'windows-1251'.")
        return
    }

    ScriptUtils.checkInterrupted();

    // Проверка того, чтобы форма для данного периода и подразделения не была загружена ранее
    // Данный код отрабатывает, когда файл формы уже фактически сохранен в базу, поэтому при проверке сущестования формы для данного периода и подразделения не нужно учитывать данный файл в выборке
    def declarationDataList = declarationService.find(PRIMARY_1151111_TEMPLATE_ID, declarationData.departmentReportPeriodId)
    DeclarationData declarationDataClone = declarationDataList?.find{ it.fileName != UploadFileName }
    if (declarationDataClone != null) {

        // Период
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        def period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
        def periodCode = period?.CODE?.stringValue
        def periodName = period?.NAME?.stringValue
        def calendarStartDate = reportPeriod?.calendarStartDate

        // Подразделение
        Department department = departmentService.get(declarationData.departmentId)

        logger.error("""Файл \"$UploadFileName\" не загружен. Экземпляр формы уже существует в системе для подразделения \"${department.name}\"
                    в периоде $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")} года.""")
        return
    }

    ScriptUtils.checkInterrupted();

    // Валидация по схеме
    declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile, UploadFileName.substring(0, UploadFileName.lastIndexOf('.')))
    if (logger.containsLevel(LogLevel.WARNING)) {
        throw new ServiceException("ТФ не соответствует XSD-схеме. Загрузка невозможна.");
    }

    ScriptUtils.checkInterrupted();

    def fileNode = new XmlSlurper().parse(new ByteArrayInputStream(content));
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    // Запуск проверок, которые проводились при загрузке
    checkImportRaschsv(fileNode, UploadFileName)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    ScriptUtils.checkInterrupted();

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
            def nomKorr = documentNode.attributes()["НомКорр"]
            raschsvSvnpPodpisant.nomKorr = nomKorr ? Integer.parseInt(nomKorr) : 0
            documentNode.childNodes().each { raschetSvNode ->
                if (raschetSvNode.name == NODE_NAME_RASCHET_SV) {
                    // Разбор узла РасчетСВ
                    raschetSvNode.childNodes().each { raschetSvChildNode ->

                        ScriptUtils.checkInterrupted();

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
    ScriptUtils.checkInterrupted();

    // Сохранение коллекции объектов ПерсСвСтрахЛиц
    if (raschsvPersSvStrahLicList.size() >= 0) {
        // При добавлении (обновлении) записей в справочнике Физические лица, в объект ПерсСвСтрахЛиц будет добавлена ссылка на запись в справочнике Физические лица
        raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
    }

    ScriptUtils.checkInterrupted();

    // Сохранение Сведений о плательщике страховых взносов и Сведения о лице, подписавшем документ
    raschsvSvnpPodpisantService.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant)

    ScriptUtils.checkInterrupted();

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

    println "Проверки xml при загрузке (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки xml при загрузке (" + (System.currentTimeMillis() - time) + " мс)");
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
        logger.errorExp(CHECK_FILE_NAME, "Соответствие кода НО в файле и в имени",
                null, fileName, CHECK_FILE_NAME_NO)
    }

    // 1.1.2 Соответствие ИНН в файле и в имени
    if (!documentInn || documentInn != fileNameInn) {
        logger.errorExp(CHECK_FILE_NAME, "Соответствие ИНН в файле и в имени",
                null, fileName, CHECK_FILE_NAME_INN)
    }

    // 1.1.3 Соответствие КПП в файле и в имени
    if (!documentKpp || documentKpp != fileNameKpp) {
        logger.errorExp(CHECK_FILE_NAME, "Соответствие КПП в файле и в имени",
                null, fileName, CHECK_FILE_NAME_KPP)
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
            logger.warnExp(CHECK_PAYMENT_OKVED_NOT_FOUND, "Поиск ОКВЭД в справочнике 'Общероссийский классификатор видов экономической деятельности'",
                    null, documentOkved, fileName)
        }

        // 1.2.2 Корректность ИНН ЮЛ
        if (INN_JUR_LENGTH != documentInn.length() || !ScriptUtils.checkControlSumInn(documentInn)) {
            logger.warnExp(CHECK_PAYMENT_INN, "Корректность ИНН ЮЛ", null, documentInn, fileName)
        }

        npul?."$NODE_NAME_SV_REORG_YL".each { reorg ->
            def documentReorgForm = reorg?."@ФормРеорг" as String
            def documentReorgInn = reorg?."@ИННЮЛ" as String
            def documentReorgKpp = reorg?."@КПП" as String

            // 1.2.4, 1.2.5
            if (['1', '2', '3', '4', '5', '6', '7'].contains(documentReorgForm)) {
                // 1.2.4 Наличие ИНН реорганизованной организации
                if (!documentReorgInn) {
                    logger.warnExp(CHECK_PAYMENT_REORG_INN, "Наличие ИНН реорганизованной организации",
                            null, fileName, documentReorgForm)
                }
                // 1.2.5 Наличие КПП реорганизованной организации
                if (!documentReorgKpp) {
                    logger.warnExp(CHECK_PAYMENT_REORG_KPP, "Наличие КПП реорганизованной организации",
                            null, fileName, documentReorgForm)
                }
            }

            // 1.2.6 Корректность ИНН реорганизованной организации
            if (INN_JUR_LENGTH != documentReorgInn.length() || !ScriptUtils.checkControlSumInn(documentReorgInn)) {
                logger.warnExp(CHECK_PAYMENT_REORG_INN_VALUE, "Корректность ИНН реорганизованной организации",
                        null, documentReorgInn, fileName)
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
            logger.warnExp(CHECK_PAYMENT_IP_INN_VALUE, "Корректность ИНН плательщика страховых взносов (ИП)",
                    null, documentIpInn, fileName)
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
            logger.warnExp(CHECK_PAYMENT_FL_INN_VALUE, "Корректность ИНН плательщика страховых взносов (ФЛ)",
                    null, documentFlInn, fileName)
        }

        // 1.2.10 Соответствие адреса ФЛ (плательщика страховых взносов) ФИАС
        if (!isExistsAddress(documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet)) {
            logger.warnExp(CHECK_PAYMENT_FL_ADDR, "Соответствие адреса ФЛ (плательщика страховых взносов) ФИАС", null,
                    documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet, fileName)
        }

        // 1.2.11 Поиск кода гражданства в справочнике
        if (documentFlCountry && !isExistsOKSM(documentFlCountry)) {
            logger.warnExp(CHECK_PAYMENT_IP_COUNTRY, "Поиск кода гражданства в справочнике 'ОКСМ'",
                    null, documentFlCountry, fileName)
        }

        // 1.2.12 Поиск кода вида документа
        if (documentFlDocCode && !isExistsDocType(documentFlDocCode)) {
            logger.warnExp(CHECK_PAYMENT_IP_DOC, "Поиск кода вида документа в справочнике 'Коды документов, удостоверяющих личность'",
                    null, documentFlDocCode, fileName)
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
                logger.warnExp(CHECK_PODPISANT_EMPTY_FIO, "Наличие ФИО подписанта",
                        null, secondName, firstName, fileName)
            }

            // 1.3.2 Наличие сведений о представителе плательщика страховых взносов
            if (PODP_2 == prPodp && !docName) {
                logger.warnExp(CHECK_PODPISANT_EMPTY_DOC, "Наличие сведений о представителе плательщика страховых взносов",
                        null, docName, fileName)
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
            logger.errorExp(CHECK_CALCULATION_OBZ, "Наличие сводных данных об обязательствах плательщика страховых взносов",
                    null, fileName, documentPlaceCode)
        }

        payments.each { payment ->
            def oktmoCode = payment?."@ОКТМО" as String

            // 1.4.2 Поиск кода ОКТМО
            if (oktmoCode && !isExistsOKTMO(oktmoCode)) {
                logger.errorExp(CHECK_CALCULATION_OBZ_OKTMO, "Поиск кода ОКТМО в справочнике 'Общероссийский классификатор территорий муниципальных образований (ОКТМО)'",
                        null, oktmoCode, fileName)
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПС
            payment?."$NODE_NAME_UPL_PER_OPS".each { ops ->
                def kbkCode = ops?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.errorExp(CHECK_CALCULATION_KBK, "Поиск кода бюджетной классификации в справочнике 'Классификатор доходов бюджетов Российской Федерации'",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОМС
            payment?."$NODE_NAME_UPL_PER_OMS".each { oms ->
                def kbkCode = oms?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.errorExp(CHECK_CALCULATION_KBK, "Поиск кода бюджетной классификации в справочнике 'Классификатор доходов бюджетов Российской Федерации'",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПСДоп
            payment?."$NODE_NAME_UPL_PER_OPS_DOP".each { dop ->
                def kbkCode = dop?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.errorExp(CHECK_CALCULATION_KBK, "Поиск кода бюджетной классификации в справочнике 'Классификатор доходов бюджетов Российской Федерации'",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерДСО
            payment?."$NODE_NAME_UPL_PER_DSO".each { dso ->
                def kbkCode = dso?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.errorExp(CHECK_CALCULATION_KBK, "Поиск кода бюджетной классификации в справочнике 'Классификатор доходов бюджетов Российской Федерации'",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО.КБК", kbkCode, fileName)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПревОСС
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def kbkCode = uplPrevOss?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.errorExp(CHECK_CALCULATION_KBK, "Поиск кода бюджетной классификации в справочнике 'Классификатор доходов бюджетов Российской Федерации'",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.КБК", kbkCode, fileName)
                }
            }

            // 1.4.4 - 1.4.11
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def prevPer = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВПер" as String
                def uplPer = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУплПер" as String
                if (prevPer?.isEmpty() && uplPer?.isEmpty()) {
                    // 1.4.4 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы страховых взносов",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер")

                    // 1.4.8 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы превышения расходов над взносами",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер")
                }

                def prev1M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ1М" as String
                def upl1M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл1М" as String
                if (prev1M?.isEmpty() && upl1M?.isEmpty()) {
                    // 1.4.5 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы страховых взносов за первый месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М")

                    // 1.4.9 Наличие суммы страховых взносов
                     logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы превышения расходов над взносами за первый месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М")
                }

                def prev2M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ2М" as String
                def upl2M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл2М" as String
                if (prev2M?.isEmpty() && upl2M?.isEmpty()) {
                    // 1.4.6 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы страховых взносов за второй месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М")

                    // 1.4.10 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы превышения расходов над взносами за второй месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М")
                }

                def prev3M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ3М" as String
                def upl3M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл3М" as String
                if (prev3M?.isEmpty() && upl3M?.isEmpty()) {
                    // 1.4.7 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы страховых взносов за третий месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М")

                    // 1.4.11 Наличие суммы страховых взносов
                    logger.errorExp(CHECK_CALCULATION_SUMM, "Наличие суммы превышения расходов над взносами за третий месяц",
                            null, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М", fileName, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М")
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
                    def countryCode = inGra?."@Гражд" as String

                    String snils = inGra?."@СНИЛС" ?: "" as String
                    String lastName = inGra?."$NODE_NAME_FIO"?."@Фамилия" as String
                    String firstName = inGra?."$NODE_NAME_FIO"?."@Имя" as String
                    String middleName = inGra?."$NODE_NAME_FIO"?."@Отчество" ?: "" as String
                    String fioAndSNILS = "ФИО: " + lastName + " " + firstName + (middleName ? " " + middleName : "") + ", СНИЛС: $snils"

                    // 1.5.1 Корректность ИНН иностранного гражданина и лица без гражданства
                    if (innFl && (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl))) {
                        String pathError = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф2.2.425.СвИноГражд.ИННФЛ"
                        logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность ИНН иностранного гражданина и лица без гражданства", fioAndSNILS, pathError,
                                "$pathError='${innFl}' в транспортном файле '$fileName' некорректный")
                    }

                    // 1.5.2 Корректность СНИЛС иностранного гражданина и лица без гражданства
                    if (snils && !ScriptUtils.checkSnils(snils)) {
                        String pathError = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф2.2.425.СвИноГражд.СНИЛС"
                        logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность СНИЛС иностранного гражданина и лица без гражданства", fioAndSNILS, pathError,
                                "$pathError='${snils}' в транспортном файле '$fileName' некорректный")
                    }

                    // 1.5.3 Поиск кода гражданства иностранного гражданина и лица без гражданства в справочнике
                    if (countryCode && !isExistsOKSM(countryCode)) {
                        String pathError = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф2.2.425.СвИноГражд.Гражд"
                        logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Поиск кода гражданства иностранного гражданина и лица без гражданства в справочнике 'ОКСМ'", fioAndSNILS, pathError,
                                "$pathError='${countryCode}' не найден в справочнике ОКСМ")
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

            String snils = person?."$NODE_NAME_DAN_FL_POLUCH"?."@СНИЛС" ?: "" as String
            String lastName = person?."$NODE_NAME_DAN_FL_POLUCH"?."ФИО"?."@Фамилия" as String
            String firstName = person?."$NODE_NAME_DAN_FL_POLUCH"?."ФИО"?."@Имя" as String
            String middleName = person?."$NODE_NAME_DAN_FL_POLUCH"?."ФИО"?."@Отчество" ?: "" as String
            String fioAndSNILS = "ФИО: " + lastName + " " + firstName + (middleName ? " " + middleName : "") + ", СНИЛС: $snils"

            // 1.6.5 Принадлежность дат сведений по ФЛ к отчетному периоду
            if (!(docPeriod == personPeriod && docYear == personYear)) {
                String pathErrorPeriod = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.Период"
                String pathErrorYear = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ОтчетГод"
                logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Принадлежность дат сведений по ФЛ к отчетному периоду", fioAndSNILS, "$pathErrorPeriod, $pathErrorYear",
                        "$pathErrorPeriod='${personPeriod ?: ""}', $pathErrorYear='${personYear ?: ""}' в транспортном файле '$fileName' не входит в отчетный период формы")
            }

            person?."$NODE_NAME_DAN_FL_POLUCH".each { data ->
                def innFl = data?."@ИННФЛ" as String
                def docTypeCode = data?."@КодВидДок" as String
                def national = data?."@Гражд" as String
                def serNumDoc = data?."@СерНомДок" as String

                // 1.6.1 Корректность ИНН ФЛ - получателя дохода
                if (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ"
                    logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность ИНН ФЛ - получателя дохода", fioAndSNILS, pathError,
                            "$pathError='${innFl ?: ""}' в транспортном файле '$fileName' некорректный")
                }

                // 1.6.2 Корректность СНИЛС ФЛ - получателя дохода
                if (snils && !ScriptUtils.checkSnils(snils)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС"
                    logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность СНИЛС ФЛ - получателя дохода", fioAndSNILS, pathError,
                            "$pathError='${snils ?: ""}' в транспортном файле '$fileName' некорректный")
                }

                // 1.6.3 Поиск кода вида документа ФЛ - получателя дохода
                if (docTypeCode && !isExistsDocType(docTypeCode)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок"
                    logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Поиск кода вида документа ФЛ - получателя дохода в справочнике 'Коды документов, удостоверяющих личность'", fioAndSNILS, pathError,
                            "$pathError='${docTypeCode ?: ""}' не найден в справочнике 'Коды документов, удостоверяющих личность'")
                }

                // 1.6.4 Поиск кода гражданства ФЛ - получателя дохода в справочнике
                if (national && !isExistsOKSM(national)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд"
                    logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Поиск кода гражданства ФЛ - получателя дохода в справочнике 'ОКСМ'", fioAndSNILS, pathError,
                            "$pathError='${national ?: ""}' не найден в справочнике ОКСМ")
                }

                // 1.6.6 Корректность серии и номера ДУЛ
                if (serNumDoc && !ScriptUtils.checkDul(serNumDoc)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СерНомДок"
                    logger.errorExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность серии и номера ДУЛ", fioAndSNILS, pathError,
                            "$pathError='${serNumDoc ?: ""}' не соответствует порядку заполнения: знак 'N' не проставляется, серия и номер документа отделяются знаком ' ' ('пробел')")
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
/**
 * Порог схожести при идентификации физлиц 0..1000, 1000 - совпадение по всем параметрам
 */
@Field
int SIMILARITY_THRESHOLD = 700;

/**
 * Тип первичной формы данные которой используются для идентификации 100 - РНУ, 200 - 1151111
 */
@Field
int FORM_TYPE = 200;

def calcTimeMillis(long time) {
    long currTime = System.currentTimeMillis();
    return (currTime - time) + " мс)";
}

@Field List<Country> countryRefBookCache = [];

List<Country> getCountryRefBookList() {

    if (countryRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.COUNTRY.getId());

        refBookRecords.each { refBookValueMap ->
            Country country = new Country();
            country.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue());
            country.setCode(refBookValueMap?.get("CODE")?.getStringValue());

            countryRefBookCache.add(country);
        }
    }
    return countryRefBookCache;
}

@Field List<DocType> docTypeRefBookCache = [];

List<DocType> getDocTypeRefBookList() {
    if (docTypeRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.DOCUMENT_CODES.getId());
        refBookRecords.each { refBookValueMap ->
            DocType docType = new DocType();
            docType.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue());
            docType.setName(refBookValueMap?.get("NAME")?.getStringValue());
            docType.setCode(refBookValueMap?.get("CODE")?.getStringValue());
            docType.setPriority(refBookValueMap?.get("PRIORITY")?.getNumberValue()?.intValue());
            docTypeRefBookCache.add(docType);
        }
    }
    return docTypeRefBookCache;
}

@Field List<TaxpayerStatus> taxpayerStatusRefBookCache = [];

List<TaxpayerStatus> getTaxpayerStatusRefBookList() {
    if (taxpayerStatusRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId());
        refBookRecords.each { refBookValueMap ->
            TaxpayerStatus taxpayerStatus = new TaxpayerStatus();
            taxpayerStatus.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
            taxpayerStatus.setName(refBookValueMap?.get("NAME")?.getStringValue());
            taxpayerStatus.setCode(refBookValueMap?.get("CODE")?.getStringValue());
            taxpayerStatusRefBookCache.add(taxpayerStatus);
        }
    }
    return taxpayerStatusRefBookCache;
}

NaturalPersonPrimaryRowMapper createPrimaryRowMapper() {

    NaturalPersonPrimaryRowMapper naturalPersonRowMapper = new NaturalPersonPrimary1151111RowMapper();
    naturalPersonRowMapper.setLogger(logger);

    List<Country> countryList = getCountryRefBookList();
    naturalPersonRowMapper.setCountryCodeMap(countryList.collectEntries {
        [it.code, it]
    });

    //println "getCountryCodeMap "+naturalPersonRowMapper.getCountryCodeMap();

    List<DocType> docTypeList = getDocTypeRefBookList();
    naturalPersonRowMapper.setDocTypeCodeMap(docTypeList.collectEntries {
        [it.code, it]
    });

    //println "getDocTypeCodeMap "+naturalPersonRowMapper.getDocTypeCodeMap();

    List<TaxpayerStatus> taxpayerStatusList = getTaxpayerStatusRefBookList();
    naturalPersonRowMapper.setTaxpayerStatusCodeMap(taxpayerStatusList.collectEntries {
        [it.code, it]
    });

    //println "getTaxpayerStatusCodeMap "+naturalPersonRowMapper.getTaxpayerStatusCodeMap();

    return naturalPersonRowMapper;
}

NaturalPersonRefbookHandler createRefbookHandler() {

    NaturalPersonRefbookHandler refbookHandler = new NaturalPersonRefbookScriptHandler();

    refbookHandler.setLogger(logger);

    List<Country> countryList = getCountryRefBookList();
    refbookHandler.setCountryMap(countryList.collectEntries {
        [it.id, it]
    })


    List<DocType> docTypeList = getDocTypeRefBookList();
    refbookHandler.setDocTypeMap(docTypeList.collectEntries {
        [it.id, it]
    });

    List<TaxpayerStatus> taxpayerStatusList = getTaxpayerStatusRefBookList();
    refbookHandler.setTaxpayerStatusMap(taxpayerStatusList.collectEntries {
        [it.id, it]
    });

    return refbookHandler;
}

/**
 * Получить версию используемую для поиска записей в справочнике ФЛ
 */


@Field Date refBookPersonVersionTo = null;

def getRefBookPersonVersionTo() {
    if (refBookPersonVersionTo == null) {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.MONTH, 0);
        localCalendar.set(Calendar.DATE, 1);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);
        localCalendar.add(Calendar.YEAR, 10);
        refBookPersonVersionTo = localCalendar.getTime();
    }
    return refBookPersonVersionTo;
}

def getRefBookPersonVersionFrom() {
    return getReportPeriodStartDate();
}

def updatePrimaryToRefBookPersonReferences(primaryDataRecords){

    ScriptUtils.checkInterrupted();

    if (FORM_TYPE == 100){
        ndflPersonService.updateRefBookPersonReferences(primaryDataRecords);
    } else {
        raschsvPersSvStrahLicService.updateRefBookPersonReferences(primaryDataRecords)
    }
}

def calculate() {

    long timeFull = System.currentTimeMillis();
    long time = System.currentTimeMillis();

    logger.info("Начало расчета ПНФ");

    //выставляем параметр что скрипт не формирует новый xml-файл
    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

    //Получаем список всех ФЛ в первичной НФ
    List<NaturalPerson> primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFrom1151111(declarationData.id, createPrimaryRowMapper());

    logger.info("В ПНФ номер " + declarationData.id + " найдено записей о физ.лицах (" + primaryPersonDataList.size() + " записей, " + calcTimeMillis(time));

    Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries {
        [it.getPrimaryPersonId(), it]
    }

    ScriptUtils.checkInterrupted();

    //Заполнени временной таблицы версий
    time = System.currentTimeMillis();
    refBookPersonService.fillRecordVersions1151111(getRefBookPersonVersionTo());
    logger.info("Заполнение таблицы версий (" + calcTimeMillis(time));

    ScriptUtils.checkInterrupted();

    //Шаг 1. список физлиц первичной формы для создания записей в справочниках
    time = System.currentTimeMillis();
    List<NaturalPerson> insertPersonList = refBookPersonService.findPersonForInsertFromPrimary1151111(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createPrimaryRowMapper());
    logger.info("Предварительная выборка новых данных (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

    time = System.currentTimeMillis();
    createNaturalPersonRefBookRecords(insertPersonList);
    logger.info("Создание записей (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

    ScriptUtils.checkInterrupted();

    //Шаг 2. идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
    time = System.currentTimeMillis();
    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimary1151111(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + calcTimeMillis(time));


    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
    logger.info("Обновление записей (" + calcTimeMillis(time));

    ScriptUtils.checkInterrupted();

    time = System.currentTimeMillis();
    Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimary1151111(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("Основная выборка по всем параметрам (" + checkSimilarityPersonMap.size() + " записей, " + calcTimeMillis(time));

    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, checkSimilarityPersonMap);
    logger.info("Обновление записей (" + calcTimeMillis(time));

    logger.info("Завершение расчета ПНФ (" + calcTimeMillis(timeFull));
}

//---------------- Identification ----------------
// Далее идет код скрипта такой же как и в 1151111 возможно следует вынести его в отдельный сервис

def createNaturalPersonRefBookRecords(List<NaturalPerson> insertRecords) {

    int createCnt = 0;
    if (insertRecords != null && !insertRecords.isEmpty()) {

        List<Address> addressList = new ArrayList<Address>();
        List<PersonDocument> documentList = new ArrayList<PersonDocument>();
        List<PersonIdentifier> identifierList = new ArrayList<PersonIdentifier>();

        for (NaturalPerson person : insertRecords) {

            ScriptUtils.checkInterrupted();

            Address address = person.getAddress();
            if (address != null) {
                addressList.add(address);
            }

            PersonDocument personDocument = person.getPersonDocument();
            if (personDocument != null) {
                documentList.add(personDocument);
            }

            PersonIdentifier personIdentifier = person.getPersonIdentifier();
            if (personIdentifier != null) {
                identifierList.add(personIdentifier);
            }

        }

        //insert addresses batch
        insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressList, { address ->
            mapAddressAttr(address)
        });

        //insert persons batch
        insertBatchRecords(RefBook.Id.PERSON.getId(), insertRecords, { person ->
            mapPersonAttr(person)
        });

        //insert documents batch
        insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentList, { personDocument ->
            mapPersonDocumentAttr(personDocument)
        });

        //insert identifiers batch
        insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifierList, { personIdentifier ->
            mapPersonIdentifierAttr(personIdentifier)
        });

        //update reference to ref book

        updatePrimaryToRefBookPersonReferences(insertRecords);


        //Выводим информацию о созданных записях
        for (NaturalPerson person : insertRecords) {
            String noticeMsg = String.format("Создана новая запись в справочнике 'Физические лица': %d, %s %s %s", person.getId(), person.getLastName(), person.getFirstName(), (person.getMiddleName() ?: ""));
            logger.info(noticeMsg);
            createCnt++;
        }

    }

    logger.info("Создано записей: " + createCnt)

}

/**
 *
 * @param primaryPersonMap
 * @param similarityPersonMap
 * @return
 */
def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

    long time = System.currentTimeMillis();

    //println "updateNaturalPersonRefBookRecords similarityPersonMap.size=" + similarityPersonMap.size()

    //Проходим по списку и определяем наиболее подходящюю запись, если подходящей записи не найдено то содадим ее
    List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>();

    List<NaturalPerson> insertPersonList = new ArrayList<NaturalPerson>();
    //список записей для обновления атрибутов справочника физлиц
    List<Map<String, RefBookValue>> updatePersonList = new ArrayList<Map<String, RefBookValue>>();

    List<Address> insertAddressList = new ArrayList<Address>();
    List<Map<String, RefBookValue>> updateAddressList = new ArrayList<Map<String, RefBookValue>>();

    List<PersonDocument> insertDocumentList = new ArrayList<PersonDocument>();
    List<PersonDocument> updateDocumentList = new ArrayList<PersonDocument>();

    List<PersonIdentifier> insertIdentifierList = new ArrayList<PersonIdentifier>();
    List<Map<String, RefBookValue>> updateIdentifierList = new ArrayList<Map<String, RefBookValue>>();

    //primaryId - RefBookPerson
    HashMap<Long, NaturalPerson> conformityMap = new HashMap<Long, NaturalPerson>();

    int msgCnt = 0;
    int maxMsgCnt = 0;
    for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {

        long inTime = System.currentTimeMillis();

        ScriptUtils.checkInterrupted();

        Long primaryPersonId = entry.getKey();

        Map<Long, NaturalPerson> similarityPersonValues = entry.getValue();

        List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values());

        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);

        inTime = System.currentTimeMillis();
        NaturalPerson refBookPerson = refBookPersonService.identificatePerson(primaryPerson, similarityPersonList, SIMILARITY_THRESHOLD, logger);

        conformityMap.put(primaryPersonId, refBookPerson);

        //Адрес нужно создать заранее и получить Id
        if (refBookPerson != null) {
            if (primaryPerson.getAddress() != null && refBookPerson.getAddress() == null) {
                insertAddressList.add(primaryPerson.getAddress());
            }
        }

        if (msgCnt <= maxMsgCnt){
            logger.info("Идентификация (" + calcTimeMillis(inTime));
        }

        msgCnt++;
    }

    logger.info("Идентификация ФЛ, обновление адресов (" + calcTimeMillis(time));

    insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), insertAddressList, { address ->
        mapAddressAttr(address)
    });

    time = System.currentTimeMillis();

    int updCnt = 0;
    msgCnt = 0;
    maxMsgCnt = 0;
    for (Map.Entry<Long, NaturalPerson> entry : conformityMap.entrySet()) {

        long inTime = System.currentTimeMillis();

        ScriptUtils.checkInterrupted();

        Long primaryPersonId = entry.getKey();
        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);
        NaturalPerson refBookPerson = entry.getValue();

        AttributeCountChangeListener addressAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener personAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener documentAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener taxpayerIdentityAttrCnt = new AttributeCountChangeListener();

        if (refBookPerson != null) {

            primaryPerson.setId(refBookPerson.getId());

            //address
            if (primaryPerson.getAddress() != null) {
                if (refBookPerson.getAddress() != null) {
                    Map<String, RefBookValue> refBookAddressValues = mapAddressAttr(refBookPerson.getAddress());

                    fillSystemAliases(refBookAddressValues, refBookPerson.getAddress());

                    updateAddressAttr(refBookAddressValues, primaryPerson.getAddress(), addressAttrCnt);

                    if (addressAttrCnt.isUpdate()) {
                        updateAddressList.add(refBookAddressValues);
                    }
                }
            }


            //person
            Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(refBookPerson);
            fillSystemAliases(refBookPersonValues, refBookPerson);
            updatePersonAttr(refBookPersonValues, primaryPerson, personAttrCnt);
            if (personAttrCnt.isUpdate()) {
                updatePersonList.add(refBookPersonValues);
            }

            //documents
            PersonDocument primaryPersonDocument = primaryPerson.getPersonDocument();
            if (primaryPersonDocument != null) {
                Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                PersonDocument personDocument = BaseWeigthCalculator.findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());

                if (personDocument == null) {
                    insertDocumentList.add(primaryPersonDocument);
                    refBookPerson.getPersonDocumentList().add(primaryPersonDocument);
                }
            }


            //check inc report
            checkIncReportFlag(refBookPerson, updateDocumentList, documentAttrCnt);

            //identifiers
            PersonIdentifier primaryPersonIdentifier = primaryPerson.getPersonIdentifier();
            if (primaryPersonIdentifier != null) {
                //Ищем совпадение в списке идентификаторов
                PersonIdentifier refBookPersonIdentifier = findIdentifierByAsnu(refBookPerson, primaryPersonIdentifier.getAsnuId());

                if (refBookPersonIdentifier != null) {

                    String primaryInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());
                    String refbookInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());

                    if (!BaseWeigthCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {

                        AttributeChangeEvent changeEvent = new AttributeChangeEvent("INP", primaryInp);
                        changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.STRING, refbookInp));
                        changeEvent.setType(AttributeChangeEventType.REFRESHED);
                        taxpayerIdentityAttrCnt.processAttr(changeEvent);

                        Map<String, RefBookValue> refBookPersonIdentifierValues = mapPersonIdentifierAttr(refBookPersonIdentifier);
                        fillSystemAliases(refBookPersonIdentifierValues, refBookPersonIdentifier);
                        updateIdentifierList.add(refBookPersonIdentifierValues);
                    }

                } else {
                    insertIdentifierList.add(primaryPersonIdentifier);
                }
            }

            updatePersonReferenceList.add(primaryPerson);

            if (addressAttrCnt.isUpdate() || personAttrCnt.isUpdate() || documentAttrCnt.isUpdate() || taxpayerIdentityAttrCnt.isUpdate()) {

                def recordId = refBookPerson.getRecordId();

                logger.info(String.format("Обновлена запись в справочнике 'Физические лица': %d, %s %s %s", recordId,
                        refBookPerson.getLastName(),
                        refBookPerson.getFirstName(),
                        refBookPerson.getMiddleName()) + " " + buildRefreshNotice(addressAttrCnt, personAttrCnt, documentAttrCnt, taxpayerIdentityAttrCnt));
                updCnt++;
            }



        } else {
            //Если метод identificatePerson вернул null, то это означает что в списке сходных записей отсутствуют записи перевыщающие порог схожести
            insertPersonList.add(primaryPerson);
        }

        if (msgCnt < maxMsgCnt){
            logger.info("Идентификация и обновление (" + calcTimeMillis(inTime));
        }

        msgCnt++;

    }

    logger.info("Обновление ФЛ, документов, id (" + calcTimeMillis(time));
    time = System.currentTimeMillis();
    //println "crete and update reference"

    //crete and update reference
    createNaturalPersonRefBookRecords(insertPersonList);

    //update reference to ref book
    if (!updatePersonReferenceList.isEmpty()) {
        updatePrimaryToRefBookPersonReferences(updatePersonReferenceList);
    }

    logger.info("Обновление справочников (" + calcTimeMillis(time));
    time = System.currentTimeMillis();

    insertBatchRecords(RefBook.Id.ID_DOC.getId(), insertDocumentList, { personDocument ->
        mapPersonDocumentAttr(personDocument)
    });

    insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), insertIdentifierList, { personIdentifier ->
        mapPersonIdentifierAttr(personIdentifier)
    });

    List<Map<String, RefBookValue>> refBookDocumentList = new ArrayList<Map<String, RefBookValue>>();

    for (PersonDocument personDoc : updateDocumentList) {
        ScriptUtils.checkInterrupted();
        Map<String, RefBookValue> values = mapPersonDocumentAttr(personDoc);
        fillSystemAliases(values, personDoc);
        refBookDocumentList.add(values);
    }

    for (Map<String, RefBookValue> refBookValues : updateAddressList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.PERSON_ADDRESS.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updatePersonList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : refBookDocumentList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updateIdentifierList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    logger.info("Идентификация и обновление (" + calcTimeMillis(time));

    logger.info("Обновлено записей: " + updCnt);

}

def fillSystemAliases(Map<String, RefBookValue> values, RefBookObject refBookObject) {
    values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getId()));
    values.put("RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getRecordId()));
    values.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, refBookObject.getVersion()));
    values.put("STATUS", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getStatus()));
}


PersonIdentifier findIdentifierByAsnu(NaturalPerson person, Long asnuId) {
    for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
        if (asnuId != null && asnuId.equals(personIdentifier.getAsnuId())) {
            return personIdentifier;
        }
    }
    return null;
}



@Field
def INCLUDE_TO_REPORT = 1;

@Field
def NOT_INCLUDE_TO_REPORT = 0;

/**
 * Метод устанавливает признак включения в отчетность на основе приоритета
 */
def checkIncReportFlag(NaturalPerson naturalPerson, List<PersonDocument> updateDocumentList, AttributeCountChangeListener attrChangeListener) {

    List personDocumentList = naturalPerson.getPersonDocumentList();

    if (!personDocumentList.isEmpty()) {

        //сортировка по приоритету
        personDocumentList.sort { a, b -> (a.getDocType()?.getPriority() <=> b.getDocType()?.getPriority()) ?: (a.id <=> b.id) }

        for (int i = 0; i < personDocumentList.size(); i++) {

            PersonDocument personDocument = personDocumentList.get(i);

            String docInf = new StringBuilder().append(personDocument.getId()).append(", ").append(personDocument.getDocumentNumber()).append(" ").toString();
            if (i == 0) {
                if (!personDocument.getIncRep().equals(INCLUDE_TO_REPORT)) {

                    AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", INCLUDE_TO_REPORT);
                    changeEvent.setType(AttributeChangeEventType.REFRESHED);
                    changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.NUMBER, personDocument.getIncRep()));

                    attrChangeListener.processAttr(docInf, changeEvent);

                    personDocument.setIncRep(INCLUDE_TO_REPORT);

                    if (personDocument.getId() != null) {
                        updateDocumentList.add(personDocument);
                    }
                }
            } else {

                if (!personDocument.getIncRep().equals(NOT_INCLUDE_TO_REPORT)) {

                    AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", NOT_INCLUDE_TO_REPORT);
                    changeEvent.setType(AttributeChangeEventType.REFRESHED);

                    changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.NUMBER, personDocument.getIncRep()));
                    attrChangeListener.processAttr(docInf, changeEvent);

                    personDocument.setIncRep(NOT_INCLUDE_TO_REPORT);

                    if (personDocument.getId() != null) {
                        updateDocumentList.add(personDocument);
                    }
                }
            }
        }

    }
}

def updateAddressAttr(Map<String, RefBookValue> values, Address address, AttributeChangeListener attributeChangeListener) {
    putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType(), attributeChangeListener);
    putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId(), attributeChangeListener);
    putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode(), attributeChangeListener);
    putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict(), attributeChangeListener);
    putOrUpdate(values, "CITY", RefBookAttributeType.STRING, address.getCity(), attributeChangeListener);
    putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality(), attributeChangeListener);
    putOrUpdate(values, "STREET", RefBookAttributeType.STRING, address.getStreet(), attributeChangeListener);
    putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse(), attributeChangeListener);
    putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, address.getBuild(), attributeChangeListener);
    putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment(), attributeChangeListener);
    putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode(), attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno(), attributeChangeListener);
}

def mapAddressAttr(Address address) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType());
    putValue(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId());
    putValue(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode());
    putValue(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict());
    putValue(values, "CITY", RefBookAttributeType.STRING, address.getCity());
    putValue(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality());
    putValue(values, "STREET", RefBookAttributeType.STRING, address.getStreet());
    putValue(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse());
    putValue(values, "BUILD", RefBookAttributeType.STRING, address.getBuild());
    putValue(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment());
    putValue(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode());
    putValue(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno());
    return values;
}


def updatePersonAttr(Map<String, RefBookValue> values, NaturalPerson person, AttributeChangeListener attributeChangeListener) {
    putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), attributeChangeListener);
    putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), attributeChangeListener);
    putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), attributeChangeListener);
    putOrUpdate(values, "SEX", RefBookAttributeType.NUMBER, person.getSex(), attributeChangeListener);
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), attributeChangeListener);
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), attributeChangeListener);
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), attributeChangeListener);
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId(), attributeChangeListener);
    putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), attributeChangeListener);
    putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null, attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId(), attributeChangeListener);
    putOrUpdate(values, "PENSION", RefBookAttributeType.NUMBER, person.getPension(), attributeChangeListener);
    putOrUpdate(values, "MEDICAL", RefBookAttributeType.NUMBER, person.getMedical(), attributeChangeListener);
    putOrUpdate(values, "SOCIAL", RefBookAttributeType.NUMBER, person.getSocial(), attributeChangeListener);
    putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee(), attributeChangeListener);
    putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId(), attributeChangeListener);
    putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId(), attributeChangeListener);
    putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId, attributeChangeListener);
    putOrUpdate(values, "OLD_ID", RefBookAttributeType.REFERENCE, null, attributeChangeListener);
}

def mapPersonAttr(NaturalPerson person) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName());
    putValue(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName());
    putValue(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName());
    putValue(values, "SEX", RefBookAttributeType.NUMBER, person.getSex());
    putValue(values, "INN", RefBookAttributeType.STRING, person.getInn());
    putValue(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign());
    putValue(values, "SNILS", RefBookAttributeType.STRING, person.getSnils());
    putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId());
    putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate());
    putValue(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null);
    putValue(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId());
    putValue(values, "PENSION", RefBookAttributeType.NUMBER, person.getPension() ?: 2);
    putValue(values, "MEDICAL", RefBookAttributeType.NUMBER, person.getMedical() ?: 2);
    putValue(values, "SOCIAL", RefBookAttributeType.NUMBER, person.getSocial() ?: 2);
    putValue(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee() ?: 2);
    putValue(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId());
    putValue(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId());
    putValue(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId);
    putValue(values, "OLD_ID", RefBookAttributeType.REFERENCE, null);
    return values;
}

def mapPersonDocumentAttr(PersonDocument personDocument) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personDocument.getNaturalPerson().getId());
    putValue(values, "DOC_NUMBER", RefBookAttributeType.STRING, personDocument.getDocumentNumber());
    putValue(values, "ISSUED_BY", RefBookAttributeType.STRING, null);
    putValue(values, "ISSUED_DATE", RefBookAttributeType.DATE, null);
    def incRepVal = personDocument.getIncRep() != null ? personDocument.getIncRep() : 1;
    putValue(values, "INC_REP", RefBookAttributeType.NUMBER, incRepVal); //default value is 1
    putValue(values, "DOC_ID", RefBookAttributeType.REFERENCE, personDocument.getDocType()?.getId());
    return values;
}

def mapPersonIdentifierAttr(PersonIdentifier personIdentifier) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personIdentifier.getNaturalPerson().getId());
    putValue(values, "INP", RefBookAttributeType.STRING, personIdentifier.getInp());
    putValue(values, "AS_NU", RefBookAttributeType.REFERENCE, personIdentifier.getAsnuId());
    return values;
}

def insertBatchRecords(refBookId, identityObjectList, refBookMapper) {

    //подготовка записей
    if (identityObjectList != null && !identityObjectList.isEmpty()) {

        logger.info("Добавление записей: refBookId="+refBookId + ", size="+identityObjectList.size())

        List<RefBookRecord> recordList = new ArrayList<RefBookRecord>();
        for (IdentityObject identityObject : identityObjectList) {

            ScriptUtils.checkInterrupted();

            def values = refBookMapper(identityObject);
            recordList.add(createRefBookRecord(values));
        }

        //создание записей справочника
        List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, getRefBookPersonVersionFrom(), null, recordList);

        //установка id
        for (int i = 0; i < identityObjectList.size(); i++) {

            ScriptUtils.checkInterrupted();

            Long id = generatedIds.get(i);
            IdentityObject identityObject = identityObjectList.get(i);
            identityObject.setId(id);
        }
    }

}

def putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
    values.put(attrName, new RefBookValue(type, value));
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 */

def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, AttributeChangeListener attributeChangedListener) {
    putOrUpdate(valuesMap, attrName, type, value, attributeChangedListener, { attrType, valueA, valueB ->
        isAttrEquals(attrType, valueA, valueB);
    });
}

def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, AttributeChangeListener attributeChangedListener, attrEquator) {

    AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, value);

    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи, если новое значение задано и отличается от существующего
        changeEvent.setCurrentValue(refBookValue);

        if (value != null && !attrEquator(type, refBookValue.getValue(), value)) {
            //значения не равны, обновление
            refBookValue.setValue(value);
            changeEvent.setType(AttributeChangeEventType.REFRESHED);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
        changeEvent.setType(AttributeChangeEventType.CREATED);
    }

    attributeChangedListener.processAttr(changeEvent);
}

def isAttrEquals(RefBookAttributeType type, Object valueA, Object valueB) {
    if (type.equals(RefBookAttributeType.STRING)) {
        return BaseWeigthCalculator.isEqualsNullSafeStr(valueA, valueB);
    } else if (type.equals(RefBookAttributeType.DATE)) {
        return ScriptUtils.equalsNullSafe(valueA, valueB);
    } else {
        return ScriptUtils.equalsNullSafe(valueA, valueB);
    }
}

/**
 * Создание новой записи справочника адреса физлиц
 * @param person
 * @return
 */
def createRefBookRecord(Map<String, RefBookValue> values) {
    RefBookRecord record = new RefBookRecord();
    putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
    record.setValues(values);
    return record;
}

def buildRefreshNotice(AttributeCountChangeListener addressAttrCnt, AttributeCountChangeListener personAttrCnt, AttributeCountChangeListener documentAttrCnt, AttributeCountChangeListener taxpayerIdentityAttrCnt) {
    StringBuffer sb = new StringBuffer();
    appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb);
    appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_DOC.getId(), documentAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_TAX_PAYER.getId(), taxpayerIdentityAttrCnt, sb);
    return sb.toString();
}


@Field HashMap<Long, RefBook> mapRefBookToIdCache = new HashMap<Long, RefBook>();

def getRefBookFromCache(Long id) {
    RefBook refBook = mapRefBookToIdCache.get(id);
    if (refBook != null) {
        return refBook;
    } else {
        refBook = refBookFactory.get(id);
        mapRefBookToIdCache.put(id, refBook);
        return refBook;
    }
}

@Field Map<Long, Map<String, String>> refBookAttrCache = new HashMap<Long, Map<String, String>>();

def getAttrNameFromRefBook(Long id, String alias) {
    Map<String, String> attrMap = refBookAttrCache.get(id);
    if (attrMap != null) {
        return attrMap.get(alias);
    } else {
        attrMap = new HashMap<String, String>();
        RefBook refBook = getRefBookFromCache(id);
        List<RefBookAttribute> refBookAttributeList = refBook.getAttributes();
        for (RefBookAttribute attr : refBookAttributeList) {
            attrMap.put(attr.getAlias(), attr.getName());
        }
        refBookAttrCache.put(id, attrMap);
        return attrMap.get(alias);
    }
}

def appendAttrInfo(Long refBookId, AttributeCountChangeListener attrCounter, StringBuffer sb) {
    if (attrCounter != null && attrCounter.isUpdate()) {
        List<String> msgList = new ArrayList<String>();
        for (Map.Entry<String, String> msgEntry : attrCounter.getMessages()) {
            String aliasKey = msgEntry.getKey();
            String msg = msgEntry.getValue();
            msgList.add(new StringBuffer(getAttrNameFromRefBook(refBookId, aliasKey)).append(": ").append(msg).toString())
        }

        if (!msgList.isEmpty()) {
            sb.append(Arrays.toString(msgList.toArray()));
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
//--------------------------------------IDENTIFICATION END--------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

/**
 * Получить "Физические лица"
 * RASCHSV_PERS_SV_STRAH_LIC.PERSON_ID будет ссылаться на актуальную записи справочника ФЛ только после проведения расчета
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersonsByDeclarationDataId() {
    Long declarationDataId = declarationData.id;
    String whereClause = String.format("id in (select person_id from raschsv_pers_sv_strah_lic where declaration_data_id = %s)", declarationDataId)
    return getRefBookByRecordWhere(RefBook.Id.PERSON.id, whereClause)
}

/**
 * Получить актуальные на отчетную дату записи справочника "Физические лица"
 * @return Map<person_id, Map<имя_поля, значение_поля>>
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId() {
    String whereClause = """
        JOIN ref_book_person p ON (frb.record_id = p.record_id)
        JOIN raschsv_pers_sv_strah_lic np ON (np.declaration_data_id = ${declarationData.id} AND p.id = np.person_id)
    """
    def refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_PERSON_ID, whereClause, getReportPeriodEndDate() - 1)
    def refBookMapResult = [:]
    refBookMap.each { personId, refBookValue ->
        Long refBookRecordId = refBookValue.get(RF_RECORD_ID).value
        refBookMapResult.put(refBookRecordId, refBookValue)
    }
    return refBookMapResult
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
@Field final String RF_FOR_OPS_DOP = "FOR_OPS_DOP"

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

def checkData() {

    ScriptUtils.checkInterrupted();

    long time = System.currentTimeMillis();
    // Проверки xml
    checkDataXml()

    ScriptUtils.checkInterrupted();

    // Проверки БД
    checkDataDB()
    println "Все проверки (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Все проверки (" + (System.currentTimeMillis() - time) + " мс)");
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
    println "Суммовые проверки (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Суммовые проверки (" + (System.currentTimeMillis() - time) + " мс)");
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

    // ФЛ Map<person_id, RefBook>
    def personMap = getActualRefPersonsByDeclarationDataId()
    logger.info("Получены записи таблицы '%s' в количестве (%d записей).", "Физические лица", personMap.size())

    // ДУЛ Map<person_id, List<RefBook>>
    def dulMap = getActualRefDulByDeclarationDataId()
    logger.info("Получены записи таблицы '%s' (%d записей).", "ДУЛ", dulMap.size())

    // Коды видов документов
    def documentTypeActualList = getActualRefDocument()

    println "Загрузка справочников для проверок записей в БД (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Загрузка справочников для проверок записей в БД (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();

    // Проверки по плательщику страховых взносов
    raschsvPersSvStrahLicList.each { raschsvPersSvStrahLic ->

        ScriptUtils.checkInterrupted();

        String fioAndRecordId = "ФИО: " + raschsvPersSvStrahLic.familia + " " + raschsvPersSvStrahLic.imya + " " + raschsvPersSvStrahLic.otchestvo ?: "" + ", идентификатор ФЛ: '${raschsvPersSvStrahLic.recordId}'"

        // 3.1.1 Назначение ФЛ записи справочника "Физические лица"
        // Если personId не задан, то он принимает значение 0, а не null
        if (raschsvPersSvStrahLic.personId == null || raschsvPersSvStrahLic.personId == 0) {
            String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч"
            logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Назначение ФЛ записи справочника 'Физические лица'", fioAndRecordId, pathError,
                    "Отсутствует ссылка на запись справочника 'Физические лица' или запись неактуальна")
        } else {
            def personRecord = personMap.get(raschsvPersSvStrahLic.recordId)

            if (!personRecord) {
                String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч"
                logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Назначение ФЛ записи справочника 'Физические лица'", fioAndRecordId, pathError,
                        "Отсутствует ссылка на запись справочника 'Физические лица' или запись неактуальна")
            } else {
                // 3.1.2 Соответствие фамилии ФЛ и справочника
                if (raschsvPersSvStrahLic.familia != personRecord.get(RF_LAST_NAME).value) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Фамилия"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие фамилии ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.familia}' не равен фамилии='${personRecord.get(RF_LAST_NAME).value}' справочника 'Физические лица'")
                }

                // 3.1.3 Соответствие имени ФЛ и справочника
                if (raschsvPersSvStrahLic.imya != personRecord.get(RF_FIRST_NAME).value) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Имя"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие имени ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.imya}' не равен имени='${personRecord.get(RF_FIRST_NAME).value}' справочника 'Физические лица'")
                }

                // 3.1.4 Соответствие отчества ФЛ и справочника
                if (raschsvPersSvStrahLic.otchestvo != null && raschsvPersSvStrahLic.otchestvo != personRecord.get(RF_MIDDLE_NAME).value) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Отчество"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие отчества ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.otchestvo}' не равен отчеству='${personRecord.get(RF_MIDDLE_NAME).value}' справочника 'Физические лица'")
                }

                // 3.1.5 Соответствие даты рождения ФЛ и справочника
                if (raschsvPersSvStrahLic.dataRozd != personRecord.get(RF_BIRTH_DATE).value) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ДатаРожд"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие даты рождения ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${ScriptUtils.formatDate(raschsvPersSvStrahLic.dataRozd, "dd.MM.yyyy")}' не равен дате рождения='${ScriptUtils.formatDate(personRecord.get(RF_BIRTH_DATE).value, "dd.MM.yyyy")}' справочника 'Физические лица'")
                }

                // 3.1.6 Соответствие пола ФЛ и справочника
                if (raschsvPersSvStrahLic.pol != personRecord.get(RF_SEX)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Пол"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие пола ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.pol ?: ""}' не равен полу='${personRecord.get(RF_SEX)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.7 Соответствие признака ОПС ФЛ и справочника
                if (raschsvPersSvStrahLic.prizOps != personRecord.get(RF_PENSION)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОПС"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие признака ОПС ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.prizOps ?: ""}' не равен признаку ОПС='${personRecord.get(RF_PENSION)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.8 Соответствие признака ОМС ФЛ и справочника
                if (raschsvPersSvStrahLic.prizOms != personRecord.get(RF_MEDICAL)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОМС"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие признака ОМС ФЛ и справочника 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.prizOms ?: ""}' не равен признаку ОМС='${personRecord.get(RF_MEDICAL)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.9 Соответствие признака ОСС
                if (raschsvPersSvStrahLic.prizOss != personRecord.get(RF_SOCIAL)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ПризОСС"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие признака ОСС ФЛ справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.prizOss ?: ""}' не равен признаку ОСС='${personRecord.get(RF_SOCIAL)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.10 Соответсвие ИНН ФЛ - получателя дохода
                if (raschsvPersSvStrahLic.innfl != null && raschsvPersSvStrahLic.innfl != personRecord.get(RF_INN)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответсвие ИНН ФЛ - получателя дохода справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.innfl ?: ""}' не равен ИНН='${personRecord.get(RF_INN)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.11 Соответствие СНИЛС ФЛ - получателя дохода
                if (raschsvPersSvStrahLic.snils != personRecord.get(RF_SNILS)?.value?.toString()) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответсвие СНИЛС ФЛ - получателя дохода справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.snils ?: ""}' не равен СНИЛС='${personRecord.get(RF_SNILS)?.value?.toString() ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.12 Соответствие кода вида документа ФЛ - получателя дохода
                def allDocList = dulMap.get(personRecord.get("id")?.value) ?: []
                // Вид документа
                def personDocTypeList = []
                // Серия и номер документа
                def personDocNumberList = []
                allDocList.each { dul ->
                    personDocType = getRefBookByRecordIds(REF_BOOK_DOCUMENT_CODES_ID, (dul.get(RF_DOC_ID).value))
                    personDocTypeList.add(personDocType?.CODE?.stringValue)
                    personDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
                }
                if (!personDocTypeList?.contains(raschsvPersSvStrahLic.kodVidDoc)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие кода вида документа ФЛ - получателя дохода справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.kodVidDoc ?: ""}' не равен документу, удостоверяющему личность='${personDocTypeList?.join("', '") ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.13 Актуальность кода вида документа ФЛ - получателя дохода
                personDocTypeList?.each { personDocType ->
                    if (!documentTypeActualList.contains(personDocType)) {
                        String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок"
                        logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Актуальность кода вида документа ФЛ - получателя дохода", fioAndRecordId, pathError,
                                "В справочнике 'Физические лица' указаны неактуальные коды документов '${personDocType ?: ""}'")
                    }
                }

                // 3.1.14 Соответствие серии и номера документа
                if (!personDocNumberList?.contains(raschsvPersSvStrahLic.serNomDoc)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СерНомДок"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие серии и номера документа справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.serNomDoc ?: ""}' не равен серии и номеру ДУЛ='${personDocNumberList?.join("', '") ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.15 Соответсвие кода гражданства ФЛ - получателя дохода в справочнике
                if (raschsvPersSvStrahLic.grazd != citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP)?.value)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответсвие кода гражданства ФЛ - получателя дохода справочнику 'Физические лица'", fioAndRecordId, pathError,
                            "$pathError='${raschsvPersSvStrahLic.grazd ?: ""}' не равен гражданству='${citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP)?.value) ?: ""}' справочника 'Физические лица'")
                }

                // 3.1.16 Актуальность кода гражданства ФЛ
                citizenship = getRefBookByRecordIds(REF_BOOK_COUNTRY_ID, personRecord.get(RF_CITIZENSHIP)?.value)
                if (!citizenshipCodeActualList.contains(citizenship?.CODE?.stringValue)) {
                    String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд"
                    logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Актуальность кода гражданства ФЛ", fioAndRecordId, pathError,
                            "В справочнике 'Физические лица' указан неактуальный код гражданства='${personRecord.get(RF_RECORD_ID)?.value ?: ""}'")
                }
            }
        }
    }

    println "Проверки по плательщику страховых взносов (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки по плательщику страховых взносов (" + (System.currentTimeMillis() - time) + " мс)");

    ScriptUtils.checkInterrupted();

    // 3.2.1 Дубли физического лица рамках формы
    time = System.currentTimeMillis();
    def raschsvPersSvStrahLicDuplList = raschsvPersSvStrahLicService.findDublicatePersonsByDeclarationDataId(declarationData.id)
    if (!raschsvPersSvStrahLicDuplList && !raschsvPersSvStrahLicDuplList.isEmpty()) {
        // Мапа для группировки дублей <recordId, RaschsvPersSvStrahLic>
        def raschsvPersSvStrahLicDuplMap = [:]
        raschsvPersSvStrahLicDuplList.each { raschsvPersSvStrahLicDuplicate ->
            raschsvPersSvStrahLicList = raschsvPersSvStrahLicDuplMap.get(raschsvPersSvStrahLicDuplicate.recordId)
            if (raschsvPersSvStrahLicList == null) {
                raschsvPersSvStrahLicList = []
            }
            raschsvPersSvStrahLicList.add(raschsvPersSvStrahLicDuplicate)
            raschsvPersSvStrahLicDuplMap.put(raschsvPersSvStrahLicDuplicate.recordId, raschsvPersSvStrahLicList)
        }
        // Для каждого дубля выводим свое сообщение об ошибке
        raschsvPersSvStrahLicDuplMap.each { key, value ->
            def ids =  value*.recordId.join(", ")
            logger.warnExp("Найдено несколько записей, идентифицированных как одно физическое лицо с Идентификаторами ФЛ: %s", "Дубли физического лица рамках формы",
                    null, ids)
        }
    }
    println "Дубли физического лица рамках формы (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Дубли физического лица рамках формы (" + (System.currentTimeMillis() - time) + " мс)");

    ScriptUtils.checkInterrupted();

    // 3.2.2 Дубли физического лица в разных формах
    time = System.currentTimeMillis();
    raschsvPersSvStrahLicDuplList = raschsvPersSvStrahLicService.findDublicatePersonsByReportPeriodId(declarationData.id, declarationData.reportPeriodId)
    if (!raschsvPersSvStrahLicDuplList && !raschsvPersSvStrahLicDuplList.isEmpty()) {
        def recordIdDuplList = []
        def declarationDataIdDuplList = []
        raschsvPersSvStrahLicDuplList.each { raschsvPersSvStrahLicDupl ->
            // Будем брать дубли из других DeclarationData
            if (raschsvPersSvStrahLicDupl.declarationDataId != declarationData.id) {
                if (!recordIdDuplList.contains(raschsvPersSvStrahLicDupl.recordId)) {
                    recordIdDuplList.add(raschsvPersSvStrahLicDupl.recordId)
                }
                if (!declarationDataIdDuplList.contains(raschsvPersSvStrahLicDupl.declarationDataId)
                        && raschsvPersSvStrahLicDupl.declarationDataId != declarationData.id) {
                    declarationDataIdDuplList.add(raschsvPersSvStrahLicDupl.declarationDataId)
                }
            }
        }
        def startReportPeriod = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time?.format("dd.MM.yyyy")
        def endReportPeriod = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time?.format("dd.MM.yyyy")
        def msgError = "ФЛ с идентификаторами: "
        if (declarationDataIdDuplList.size() > 0) {
            msgError += recordIdDuplList.join(", ") + " найдены в других формах %s"
            def declarationDataList = declarationService.getDeclarationData(declarationDataIdDuplList)
            def declarationDataInfo = []
            declarationDataList.each { dd ->
                def declarationTypeName = declarationService.getTypeByTemplateId(dd.declarationTemplateId)?.name
                def departmentName = departmentService.get(dd.departmentId)?.name
                declarationDataInfo.add("\"$declarationTypeName\" \"$departmentName\" $startReportPeriod - $endReportPeriod")
            }
            logger.warnExp(msgError, "Дубли физического лица в разных формах",
                    null, declarationDataInfo.join(", "))
        }
    }
    println "Дубли физического лица в разных формах (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Дубли физического лица в разных формах (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Суммовые проверки
 * @return
 */
def checkDataDBSum() {

    RaschsvSvnpPodpisant raschsvSvnpPodpisant = raschsvSvnpPodpisantService.findRaschsvSvnpPodpisant(declarationData.id)

    BigDecimal svVyplMkSum1 = 0
    BigDecimal svVyplMkSum2 = 0
    BigDecimal svVyplMkSum3 = 0
    BigDecimal svVyplMkDopSum1 = 0
    BigDecimal svVyplMkDopSum2 = 0
    BigDecimal svVyplMkDopSum3 = 0
    // Map<Тариф, Array<Сумма_выплат_по_месяцам>> Сведения о сумме выплат по доп.тарифам
    Map<Integer, List<BigDecimal>> vyplSvDopMtMap = [:]
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
                    BigDecimal nachislSvCurr = raschsvVyplSvDopMt.nachislSv ?: 0

                    // Сведения о сумме выплат по доп.тарифам в пользу физ.лица по месяцам
                    Integer numberMonth = getNumberMonth(Integer.parseInt(raschsvVyplSvDopMt.mesyac), getReportPeriodEndDate())
                    if (numberMonth != null) {
                        if (numberMonth == 1) {
                            // 1-ый из 3-х последних месяцев
                            svVyplMkDopSum1 += nachislSvCurr
                        } else if (numberMonth == 2) {
                            // 2-ый из 3-х последних месяцев
                            svVyplMkDopSum2 += nachislSvCurr
                        } else if (numberMonth == 3) {
                            // 3-ий из 3-х последних месяцев
                            svVyplMkDopSum3 += nachislSvCurr
                        }

                        // Сведения о сумме выплат по доп.тарифам в пользу физ.лица по месяцам в разрезе тарифов
                        List<BigDecimal> vyplSvDopMtSumList = vyplSvDopMtMap.get(raschsvVyplSvDopMt.tarif) ?: []
                        vyplSvDopMtSumList[numberMonth - 1] = vyplSvDopMtSumList[numberMonth - 1] ?: 0
                        vyplSvDopMtSumList[numberMonth - 1] += nachislSvCurr
                        vyplSvDopMtMap.put(raschsvVyplSvDopMt.tarif, vyplSvDopMtSumList)
                    }
                }
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

    // РасчСВ_ОПС_ОМС.ТарифПлат
    boolean opsOmsIsExistTarifPlat_06 = false
    boolean opsOmsIsExistTarifPlat_08 = false
    boolean opsOmsIsExistTarifPlat_10 = false

    // *********************************** РасчСВ_ОПС_ОМС ***********************************

    def pathAttrOps = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС"
    def pathAttrOms = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОМС"

    // Перебор РасчСВ_ОПС_ОМС
    List<RaschsvSvOpsOms> raschsvSvOpsOmsList = raschsvSvOpsOmsService.findSvOpsOms(declarationData.id)
    for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {

        // РасчСВ_ОПС.НачислСВ
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

        // РасчСВ_ОМС.БазНачислСВ
        BigDecimal bazNachislSvCurr1 = 0
        BigDecimal bazNachislSvCurr2 = 0
        BigDecimal bazNachislSvCurr3 = 0

        // РасчСВ_ОМС.ВыплНачислФЛ
        BigDecimal vyplNachislFlCurr1 = 0
        BigDecimal vyplNachislFlCurr2 = 0
        BigDecimal vyplNachislFlCurr3 = 0

        // РасчСВ_ОМС.НеОбложенСВ
        BigDecimal neOblozenCurr1 = 0
        BigDecimal neOblozenCurr2 = 0
        BigDecimal neOblozenCurr3 = 0

        if (raschsvSvOpsOms.tarifPlat == "06") {
            opsOmsIsExistTarifPlat_06 = true
        } else if (raschsvSvOpsOms.tarifPlat == "08") {
            opsOmsIsExistTarifPlat_08 = true
        } else if (raschsvSvOpsOms.tarifPlat == "10") {
            opsOmsIsExistTarifPlat_10 = true
        }

        // РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_ОПС428, РасчСВ_ДСО
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
                            nachislSvNePrevCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            nachislSvNePrevSum1 += nachislSvNePrevCurr1
                            nachislSvNePrevCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            nachislSvNePrevSum2 += nachislSvNePrevCurr2
                            nachislSvNePrevCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
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
                        } else if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_BAZ_NACHISL_SV) {
                            // БазНачислСВ
                            bazNachislSvCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            bazNachislSvCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            bazNachislSvCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
                        } else if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_VYPL_NACHISL_FL) {
                            // ВыплНачислФЛ
                            vyplNachislFlCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            vyplNachislFlCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            vyplNachislFlCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
                        } else if (raschsvSvOpsOmsRaschSum.nodeName == NODE_NAME_NE_OBLOZEN_SV) {
                            // НеОбложенСВ
                            neOblozenCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
                            neOblozenCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
                            neOblozenCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
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
        if (!comparNumbEquals(nachislSvOpsCurr1, nachislSvNePrevCurr1 + nachislSvPrevCurr1)) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум1Посл3М = \"$nachislSvOpsCurr1\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум1Посл3М = \"$nachislSvNePrevCurr1\", " + pathAttrOps + ".НачислСВПрев.Сум1Посл3М = \"$nachislSvPrevCurr1\"."
            logger.warnExp("Сумма исчисленных взносов на ОПС %s не равна сумме %s",
                    "Сумма исчисленных взносов на ОПС равна сумме не превышающих и превышающих предельную величину базы",
                    null, pathAttrVal, pathAttrComp)
        }
        if (!comparNumbEquals(nachislSvOpsCurr2, nachislSvNePrevCurr2 + nachislSvPrevCurr2)) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум2Посл3М = \"$nachislSvOpsCurr2\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум2Посл3М = \"$nachislSvNePrevCurr2\", " + pathAttrOps + ".НачислСВПрев.Сум2Посл3М = \"$nachislSvPrevCurr2\"."
            logger.warnExp("Сумма исчисленных взносов на ОПС %s не равна сумме %s",
                    "Сумма исчисленных взносов на ОПС равна сумме не превышающих и превышающих предельную величину базы",
                    null, pathAttrVal, pathAttrComp)
        }
        if (!comparNumbEquals(nachislSvOpsCurr3, nachislSvNePrevCurr3 + nachislSvPrevCurr3)) {
            def pathAttrVal = pathAttrOps + ".НачислСВ.Сум3Посл3М = \"$nachislSvOpsCurr3\""
            def pathAttrComp = pathAttrOps + ".НачислСВНеПрев.Сум3Посл3М = \"$nachislSvNePrevCurr3\", " + pathAttrOps + ".НачислСВПрев.Сум3Посл3М = \"$nachislSvPrevCurr3\"."
            logger.warnExp("Сумма исчисленных взносов на ОПС %s не равна сумме %s",
                    "Сумма исчисленных взносов на ОПС равна сумме не превышающих и превышающих предельную величину базы",
                    null, pathAttrVal, pathAttrComp)
        }

        // 3.3.3.2 База для начисления равна разности сумм выплат и сумм, не подлежащих налогообложению (Проверки выполняются для каждого РасчСВ_ОМС)
        if (!comparNumbEquals(bazNachislSvCurr1, vyplNachislFlCurr1 + neOblozenCurr1)) {
            def pathAttrVal = pathAttrOms + ".БазНачислСВ.Сум1Посл3М = \"$bazNachislSvCurr1\""
            def pathAttrComp = pathAttrOms + ".ВыплНачислФЛ.Сум1Посл3М = \"$vyplNachislFlCurr1\", " + pathAttrOms + ".НеОбложенСВ.Сум1Посл3М = \"$neOblozenCurr1\"."
            logger.warnExp("%s не равен сумме: %s",
                    "База для начисления равна разности сумм выплат и сумм, не подлежащих налогообложению",
                    null, pathAttrVal, pathAttrComp)
        }
        if (!comparNumbEquals(bazNachislSvCurr2, vyplNachislFlCurr2 + neOblozenCurr2)) {
            def pathAttrVal = pathAttrOms + ".БазНачислСВ.Сум2Посл3М = \"$bazNachislSvCurr2\""
            def pathAttrComp = pathAttrOms + ".ВыплНачислФЛ.Сум2Посл3М = \"$vyplNachislFlCurr2\", " + pathAttrOms + ".НеОбложенСВ.Сум2Посл3М = \"$neOblozenCurr2\"."
            logger.warnExp("%s не равен сумме: %s",
                    "База для начисления равна разности сумм выплат и сумм, не подлежащих налогообложению",
                    null, pathAttrVal, pathAttrComp)
        }
        if (!comparNumbEquals(bazNachislSvCurr3, vyplNachislFlCurr3 + neOblozenCurr3)) {
            def pathAttrVal = pathAttrOms + ".БазНачислСВ.Сум3Посл3М = \"$bazNachislSvCurr3\""
            def pathAttrComp = pathAttrOms + ".ВыплНачислФЛ.Сум3Посл3М = \"$vyplNachislFlCurr3\", " + pathAttrOms + ".НеОбложенСВ.Сум3Посл3М = \"$neOblozenCurr3\"."
            logger.warnExp("%s не равен сумме: %s",
                    "База для начисления равна разности сумм выплат и сумм, не подлежащих налогообложению",
                    null, pathAttrVal, pathAttrComp)
        }
    }

    ScriptUtils.checkInterrupted();

    // 3.3.1.2 Сумма исчисленных страховых взносов по всем ФЛ равна значению исчисленных страховых взносов по ОПС в целом (с базы не превышающих предельную величину) (Проверки выполняются по всем РасчСВ_ОПС)
    if (raschsvSvnpPodpisant.nomKorr == 0) {
        if (!comparNumbEquals(nachislSvNePrevSum1, svVyplMkSum1)) {
            def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум1Посл3М = \"$nachislSvNePrevSum1\""
            logger.warnExp("%s не равен сумме исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ.",
                    "Сумма исчисленных страховых взносов по всем ФЛ равна значению исчисленных страховых взносов по ОПС в целом (с базы не превышающих предельную величину)",
                    null, pathAttrVal)
        }
        if (!comparNumbEquals(nachislSvNePrevSum2, svVyplMkSum2)) {
            def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум2Посл3М = \"$nachislSvNePrevSum2\""
            logger.warnExp("%s не равен сумме исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ.",
                    "Сумма исчисленных страховых взносов по всем ФЛ равна значению исчисленных страховых взносов по ОПС в целом (с базы не превышающих предельную величину)",
                    null, pathAttrVal)
        }
        if (!comparNumbEquals(nachislSvNePrevSum3, svVyplMkSum3)) {
            def pathAttrVal = pathAttrOps + ".НачислСВНеПрев.Сум3Посл3М = \"$nachislSvNePrevSum3\""
            logger.warnExp("%s не равен сумме исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину по всем ФЛ.",
                    "Сумма исчисленных страховых взносов по всем ФЛ равна значению исчисленных страховых взносов по ОПС в целом (с базы не превышающих предельную величину)",
                    null, pathAttrVal)
        }
    }

    // 3.3.1.3 Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов (Проверки выполняются по всем РасчСВ_ОПС)
    if (!comparNumbEquals(nachislSvOpsSum1, uplPerOpsSum1)) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум1Посл3М = \"$nachislSvOpsSum1\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл1М = \"$uplPerOpsSum1\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОПС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrComp, pathAttrVal)
    }
    if (!comparNumbEquals(nachislSvOpsSum2, uplPerOpsSum2)) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум2Посл3М = \"$nachislSvOpsSum2\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл2М = \"$uplPerOpsSum2\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОПС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrComp, pathAttrVal)
    }
    if (!comparNumbEquals(nachislSvOpsSum3, uplPerOpsSum3)) {
        def pathAttrVal = pathAttrOps + ".НачислСВ.Сум3Посл3М = \"$nachislSvOpsSum3\""
        def pathAttrComp = "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.СумСВУпл3М = \"$uplPerOpsSum3\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОПС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrComp, pathAttrVal)
    }

    // 3.3.2.1 Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу в целом (Проверки выполняются по всем РасчСВ_ОПС428)
    if (raschsvSvnpPodpisant.nomKorr == 0) {
        def pathAttr428_12 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.1-2.НачислСВДоп"
        def pathAttr428_3 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.3.НачислСВДоп"
        if (!comparNumbEquals(nachislSvDop428_12Sum1 + nachislSvDop428_3Sum1, svVyplMkDopSum1)) {
            logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ='%s' не равна сумме: %s.Сум1Посл3М='%s' + %s.Сум1Посл3М='%s'",
                    "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу в целом",
                    null, svVyplMkDopSum1, pathAttr428_12, nachislSvDop428_12Sum1, pathAttr428_3, nachislSvDop428_3Sum1)
        }
        if (!comparNumbEquals(nachislSvDop428_12Sum2 + nachislSvDop428_3Sum2, svVyplMkDopSum2)) {
            logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ='%s' не равна сумме: %s.Сум2Посл3М='%s' + %s.Сум2Посл3М='%s'",
                    "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу в целом",
                    null, svVyplMkDopSum2, pathAttr428_12, nachislSvDop428_12Sum2, pathAttr428_3, nachislSvDop428_3Sum2)
        }
        if (!comparNumbEquals(nachislSvDop428_12Sum3 + nachislSvDop428_3Sum3, svVyplMkDopSum3)) {
            logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу по всем ФЛ='%s' не равна сумме: %s.Сум3Посл3М='%s' + %s.Сум3Посл3М='%s'",
                    "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу в целом",
                    null, svVyplMkDopSum3, pathAttr428_12, nachislSvDop428_12Sum3, pathAttr428_3, nachislSvDop428_3Sum3)
        }
    }

    ScriptUtils.checkInterrupted();

    if (raschsvSvnpPodpisant.nomKorr == 0) {
        vyplSvDopMtMap.each { tarif, vyplSvDopMtSumList ->
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
                    String pathAttr428_12 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.1-2.НачислСВДоп"
                    BigDecimal nachisl428_12Sum_1 = nachisl428_12Sum1Map.get(prOsnSvDop)
                    if (!comparNumbEquals(nachisl428_12Sum_1, vyplSvDopMtSumList[0])) {
                        // 1-ый из трех последних месяцев
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум1Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 1 и 2 статьи 428)",
                                null, vyplSvDopMtSumList[0], pathAttr428_12, nachisl428_12Sum_1)
                    }
                    BigDecimal nachisl428_12Sum_2 = nachisl428_12Sum2Map.get(prOsnSvDop)
                    if (!comparNumbEquals(nachisl428_12Sum_2, vyplSvDopMtSumList[1])) {
                        // 2-ый из трех последних месяцев
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум2Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 1 и 2 статьи 428)",
                                null, vyplSvDopMtSumList[1], pathAttr428_12, nachisl428_12Sum_2)
                    }
                    BigDecimal nachisl428_12Sum_3 = nachisl428_12Sum3Map.get(prOsnSvDop)
                    if (!comparNumbEquals(nachisl428_12Sum_3, vyplSvDopMtSumList[2])) {
                        // 3-ий из трех последних месяцев
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункты 1 и 2 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум3Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 1 и 2 статьи 428)",
                                null, vyplSvDopMtSumList[2], pathAttr428_12, nachisl428_12Sum_3)
                    }
                }
            }

            // 3.3.2.3 Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 3 428)
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
                    String pathAttr428_3 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОПС_ОМС.РасчСВ_ОПС428.РасчСВ_428.3.НачислСВДоп"
                    BigDecimal nachisl428_3Sum_1 = nachisl428_3Sum1Map.get(klasUslTrud)
                    if (!comparNumbEquals(nachisl428_3Sum_1, vyplSvDopMtSumList[0])) {
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум1Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 3 428)",
                                null, vyplSvDopMtSumList[0], pathAttr428_3, nachisl428_3Sum_1)
                    }
                    BigDecimal nachisl428_3Sum_2 = nachisl428_3Sum2Map.get(klasUslTrud)
                    if (!comparNumbEquals(nachisl428_3Sum_2, vyplSvDopMtSumList[1])) {
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум2Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 3 428)",
                                null, vyplSvDopMtSumList[1], pathAttr428_3, nachisl428_3Sum_2)
                    }
                    BigDecimal nachisl428_3Sum_3 = nachisl428_3Sum3Map.get(klasUslTrud)
                    if (!comparNumbEquals(nachisl428_3Sum_3, vyplSvDopMtSumList[2])) {
                        logger.warnExp("Сумма исчисленных страховых взносов по дополнительному тарифу (пункт 3 статьи 428) по всем ФЛ='%s' не равна суммам %s.Сум3Посл3М='%s'",
                                "Сумма исчисленных страховых взносов по доп. тарифу по всем ФЛ равна значению исчисленных страховых взносов по доп. тарифу (п 3 428)",
                                null, vyplSvDopMtSumList[2], pathAttr428_3, nachisl428_3Sum_3)
                    }
                }
            }
        }
    }

    ScriptUtils.checkInterrupted();

    // 3.3.3.1 Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов (Проверки выполняются по всем РасчСВ_ОМС428)
    if (!comparNumbEquals(uplPerOmsSum1, nachislSvOmsSum1)) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум1Посл3М = \"$uplPerOmsSum1\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум1Посл3М = \"$nachislSvNePrevSum1\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОМС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(uplPerOmsSum2, nachislSvOmsSum2)) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум2Посл3М = \"$uplPerOmsSum2\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум2Посл3М = \"$nachislSvNePrevSum2\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОМС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(uplPerOmsSum3, nachislSvOmsSum3)) {
        def pathAttrVal = pathAttrOms + ".УплПерОПС.Сум3Посл3М = \"$uplPerOmsSum3\""
        def pathAttrComp = pathAttrOms + ".РасчСВ_ОПС_ОМС.РасчСВ_ОМС.НачислСВ.Сум3Посл3М = \"$nachislSvNePrevSum3\""
        logger.warnExp("Сумма страховых взносов, подлежащая уплате ОМС %s не равна сумме исчисленных страховых взносов %s",
                "Сумма страховых взносов подлежащая уплате равна сумме исчисленных страховых взносов",
                null, pathAttrVal, pathAttrComp)
    }

    ScriptUtils.checkInterrupted();

    // *********************************** РасчСВ_ОСС.ВНМ ***********************************
    RaschsvOssVnm raschsvOssVnm = raschsvOssVnmService.findOssVnm(declarationData.id)
    def pathAttrOss = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасчСВ_ОСС.ВНМ"
    // РасчСВ_ОСС.ВНМ .УплСВПрев
    List<RaschsvUplSvPrev> raschsvUplSvPrevList = raschsvOssVnm.raschsvUplSvPrevList
    BigDecimal uplSvPrevCurr1 = 0
    BigDecimal uplSvPrevCurr2 = 0
    BigDecimal uplSvPrevCurr3 = 0
    for (RaschsvUplSvPrev raschsvUplSvPrev : raschsvUplSvPrevList) {
        if (raschsvUplSvPrev.nodeName == "Упл1Посл3М") {
            uplSvPrevCurr1 = raschsvUplSvPrev.svSum ?: 0
        }
        if (raschsvUplSvPrev.nodeName == "Упл2Посл3М") {
            uplSvPrevCurr2 = raschsvUplSvPrev.svSum ?: 0
        }
        if (raschsvUplSvPrev.nodeName == "Упл3Посл3М") {
            uplSvPrevCurr3 = raschsvUplSvPrev.svSum ?: 0
        }
    }
    List<RaschsvOssVnmSum> raschsvOssVnmSumList = raschsvOssVnm.raschsvOssVnmSumList
    // РасчСВ_ОСС.ВНМ .НачислСВ
    BigDecimal ossNachislSvCurr1 = 0
    BigDecimal ossNachislSvCurr2 = 0
    BigDecimal ossNachislSvCurr3 = 0
    // РасчСВ_ОСС.ВНМ .ВозмРасхСО
    BigDecimal ossVosmRashSoCurr1 = 0
    BigDecimal ossVosmRashSoCurr2 = 0
    BigDecimal ossVosmRashSoCurr3 = 0
    // РасчСВ_ОСС.ВНМ .ПроизвРасхСО
    BigDecimal ossProizvRashSOCurr1 = 0
    BigDecimal ossProizvRashSOCurr2 = 0
    BigDecimal ossProizvRashSOCurr3 = 0
    for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnmSumList) {
        RaschsvSvSum1Tip raschsvSvSum1Tip = raschsvOssVnmSum.raschsvSvSum1Tip
        // НачислСВ
        if (raschsvOssVnmSum.nodeName == NODE_NAME_NACHISL_SV) {
            ossNachislSvCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
            ossNachislSvCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
            ossNachislSvCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
        }
        // ВозмРасхСО
        if (raschsvOssVnmSum.nodeName == NODE_NAME_VOZM_RASH_SO) {
            ossVosmRashSoCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
            ossVosmRashSoCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
            ossVosmRashSoCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
        }
        // ПроизвРасхСО
        if (raschsvOssVnmSum.nodeName == NODE_NAME_PROIZV_RASCH_SO) {
            ossProizvRashSOCurr1 = raschsvSvSum1Tip.sum1mPosl3m ?: 0
            ossProizvRashSOCurr2 = raschsvSvSum1Tip.sum2mPosl3m ?: 0
            ossProizvRashSOCurr3 = raschsvSvSum1Tip.sum3mPosl3m ?: 0
        }
    }

    // 3.3.4.1 Сумма подлежащая уплате равна исчислено + возмещено - произведено расходов
    if (!comparNumbEquals(uplSvPrevCurr1, ossNachislSvCurr1 + ossVosmRashSoCurr1 - ossProizvRashSOCurr1)) {
        String pathAttrVal = "${pathAttrOss}.УплСВПрев.Упл1Посл3М.Сумма='$uplSvPrevCurr1'"
        String pathAttrComp = "${pathAttrOss}.НачислСВ.Сум1Посл3М='$ossNachislSvCurr1' + ${pathAttrOss}.ВозмРасхСО.Сум1Посл3М='$ossVosmRashSoCurr1' - ${pathAttrOss}.ПроизвРасхСО.Сум1Посл3М='$ossProizvRashSOCurr1'"
        logger.warnExp("%s не равен %s",
                "Сумма подлежащая уплате равна исчислено + возмещено - произведено расходов",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(uplSvPrevCurr2, ossNachislSvCurr2 + ossVosmRashSoCurr2 - ossProizvRashSOCurr2)) {
        String pathAttrVal = "${pathAttrOss}.УплСВПрев.Упл2Посл3М.Сумма='$uplSvPrevCurr2'"
        String pathAttrComp = "${pathAttrOss}.НачислСВ.Сум2Посл3М='$ossNachislSvCurr2' + ${pathAttrOss}.ВозмРасхСО.Сум2Посл3М='$ossVosmRashSoCurr2' - ${pathAttrOss}.ПроизвРасхСО.Сум2Посл3М='$ossProizvRashSOCurr2'"
        logger.warnExp("%s не равен %s",
                "Сумма подлежащая уплате равна исчислено + возмещено - произведено расходов",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(uplSvPrevCurr3, ossNachislSvCurr3 + ossVosmRashSoCurr3 - ossProizvRashSOCurr3)) {
        String pathAttrVal = "${pathAttrOss}.УплСВПрев.Упл3Посл3М.Сумма='$uplSvPrevCurr3'"
        String pathAttrComp = "${pathAttrOss}.НачислСВ.Сум3Посл3М='$ossNachislSvCurr3' + ${pathAttrOss}.ВозмРасхСО.Сум3Посл3М='$ossVosmRashSoCurr3' - ${pathAttrOss}.ПроизвРасхСО.Сум3Посл3М='$ossProizvRashSOCurr3'"
        logger.warnExp("%s не равен %s",
                "Сумма подлежащая уплате равна исчислено + возмещено - произведено расходов",
                null, pathAttrVal, pathAttrComp)
    }

    ScriptUtils.checkInterrupted();

    // *********************************** РасхОССЗак ***********************************
    RaschsvRashOssZak raschsvRashOssZak = raschsvRashOssZakService.findRaschsvRashOssZak(declarationData.id)
    def pathAttrOssZak = "Файл.Документ.РасчетСВ.ОбязПлатСВ.РасхОССЗак"
    List<RaschsvRashOssZakRash> raschsvRashOssZakRashList = raschsvRashOssZak.raschsvRashOssZakRashList
    // РасхОССЗак.ЕжПосУходРеб
    Integer ezPosChislSluch = 0
    Integer ezPosKolVypl = 0
    BigDecimal ezPosRashVsego = 0
    BigDecimal ezPosRashFinFb = 0
    // РасхОССЗак.ЕжПосУходРеб1
    Integer ezPos1ChislSluch = 0
    Integer ezPos1KolVypl = 0
    BigDecimal ezPos1RashVsego = 0
    BigDecimal ezPos1RashFinFb = 0
    // РасхОССЗак.ЕжПосУходРеб2
    Integer ezPos2ChislSluch = 0
    Integer ezPos2KolVypl = 0
    BigDecimal ezPos2RashVsego = 0
    BigDecimal ezPos2RashFinFb = 0
    // РасхОССЗак. (по всем видам)
    BigDecimal ossZakRashVsegoSum = 0
    BigDecimal ossZakRashFinFBSum = 0
    // РасхОССЗак.Итого
    BigDecimal ossZakRashVsegoItogo = 0
    BigDecimal ossZakRashFinFBItogo = 0
    def kindAids = ["ПосВрНетр", "ПосВрНетрИн", "БеремРод", "ЕдПосРанБерем", "ЕдПосРожд", "ЕжПосУходРеб", "ОплДопВыхИнв", "СВДопУходИнв", "СоцПосПогреб"]
    for (RaschsvRashOssZakRash raschsvRashOssZakRash : raschsvRashOssZakRashList) {
        if (raschsvRashOssZakRash.nodeName == "ЕжПосУходРеб") {
            ezPosChislSluch = raschsvRashOssZakRash.chislSluch ?: 0
            ezPosKolVypl = raschsvRashOssZakRash.kolVypl ?: 0
            ezPosRashVsego = raschsvRashOssZakRash.rashVsego ?: 0
            ezPosRashFinFb = raschsvRashOssZakRash.rashFinFb ?: 0
        } else if (raschsvRashOssZakRash.nodeName == "ЕжПосУходРеб1") {
            ezPos1ChislSluch = raschsvRashOssZakRash.chislSluch ?: 0
            ezPos1KolVypl = raschsvRashOssZakRash.kolVypl ?: 0
            ezPos1RashVsego = raschsvRashOssZakRash.rashVsego ?: 0
            ezPos1RashFinFb = raschsvRashOssZakRash.rashFinFb ?: 0
        } else if (raschsvRashOssZakRash.nodeName == "ЕжПосУходРеб2") {
            ezPos2ChislSluch = raschsvRashOssZakRash.chislSluch ?: 0
            ezPos2KolVypl = raschsvRashOssZakRash.kolVypl ?: 0
            ezPos2RashVsego = raschsvRashOssZakRash.rashVsego ?: 0
            ezPos2RashFinFb = raschsvRashOssZakRash.rashFinFb ?: 0
        } else if (raschsvRashOssZakRash.nodeName == "Итого") {
            ossZakRashVsegoItogo = raschsvRashOssZakRash.rashVsego ?: 0
            ossZakRashFinFBItogo = raschsvRashOssZakRash.rashFinFb ?: 0
        }
        if (kindAids.contains(raschsvRashOssZakRash.nodeName)) {
            ossZakRashVsegoSum += raschsvRashOssZakRash.rashVsego ?: 0
            ossZakRashFinFBSum += raschsvRashOssZakRash.rashFinFb ?: 0
        }
    }

    // 3.3.4.2 Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей
    if (!comparNumbEquals(ezPosChislSluch, ezPos1ChislSluch + ezPos2ChislSluch)) {
        def pathAttrVal = pathAttrOssZak + ".ЕжПосУходРеб.ЧислСлуч = \"$ezPosChislSluch\""
        def pathAttrComp = pathAttrOssZak + ".ЕжПосУходРеб1.ЧислСлуч = \"$ezPos1ChislSluch\" + " + pathAttrOssZak + ".ЕжПосУходРеб2.ЧислСлуч = \"$ezPos2ChislSluch\"."
        logger.warnExp("%s не равен %s",
                "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(ezPosKolVypl, ezPos1KolVypl + ezPos2KolVypl)) {
        def pathAttrVal = pathAttrOssZak + ".ЕжПосУходРеб.КолВыпл = \"$ezPosKolVypl\""
        def pathAttrComp = pathAttrOssZak + ".ЕжПосУходРеб1.КолВыпл = \"$ezPos1KolVypl\" + " + pathAttrOssZak + ".ЕжПосУходРеб2.КолВыпл = \"$ezPos2KolVypl\"."
        logger.warnExp("%s не равен %s",
                "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(ezPosRashVsego, ezPos1RashVsego + ezPos2RashVsego)) {
        def pathAttrVal = pathAttrOssZak + ".ЕжПосУходРеб.РасхВсего = \"$ezPosRashVsego\""
        def pathAttrComp = pathAttrOssZak + ".ЕжПосУходРеб1.РасхВсего = \"$ezPos1RashVsego\" + " + pathAttrOssZak + ".ЕжПосУходРеб2.РасхВсего = \"$ezPos2RashVsego\"."
        logger.warnExp("%s не равен %s",
                "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(ezPosRashFinFb, ezPos1RashFinFb + ezPos2RashFinFb)) {
        def pathAttrVal = pathAttrOssZak + ".ЕжПосУходРеб.РасхФинФБ = \"$ezPosRashVsego\""
        def pathAttrComp = pathAttrOssZak + ".ЕжПосУходРеб1.РасхФинФБ = \"$ezPos1RashVsego\" + " + pathAttrOssZak + ".ЕжПосУходРеб2.РасхФинФБ = \"$ezPos2RashVsego\"."
        logger.warnExp("%s не равен %s",
                "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                null, pathAttrVal, pathAttrComp)
    }

    // 3.3.4.3 Итого равно сумме по всем видам пособий
    if (!comparNumbEquals(ossZakRashVsegoItogo, ossZakRashVsegoSum)) {
        def pathAttrVal = pathAttrOssZak + ".Итого.РасхВсего = \"$ossZakRashVsegoItogo\""
        def pathAttrComp = kindAids.join(", ") + " = \"$ossZakRashVsegoSum\""
        logger.warnExp("%s не равен сумме %s",
                "Итого равно сумме по всем видам пособий",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(ossZakRashFinFBItogo, ossZakRashFinFBSum)) {
        def pathAttrVal = pathAttrOssZak + ".Итого.РасхФинФБ = \"$ossZakRashFinFBItogo\""
        def pathAttrComp = kindAids.join(", ") + " = \"$ossZakRashFinFBSum\""
        logger.warnExp("%s не равен сумме %s",
                "Итого равно сумме по всем видам пособий",
                null, pathAttrVal, pathAttrComp)
    }

    ScriptUtils.checkInterrupted();

    // *********************************** ВыплФинФБ ***********************************
    RaschsvVyplFinFb raschsvVyplFinFb = raschsvVyplFinFbService.findRaschsvVyplFinFb(declarationData.id)
    def pathAttrVyplFinFB = "Файл.Документ.РасчетСВ.ОбязПлатСВ.ВыплФинФБ"
    List<RaschsvVyplPrichina> raschsvVyplPrichinaList = raschsvVyplFinFb.raschsvVyplPrichinaList
    Integer vsegoPosVrNetrChislPoluch = 0           // Всего.ПосВрНетр.ЧислПолуч
    Integer vsegoPosVrNetrKolVypl = 0               // Всего.ПосВрНетр.КолВыпл
    BigDecimal vsegoPosVrNetrRashod = 0             // Всего.ПосВрНетр.Расход
    Integer vsegoPosVrNetrChislPoluchSum = 0        // *.ПосВрНетр.ЧислПолуч
    Integer vsegoPosVrNetrKolVyplSum = 0            // *.ПосВрНетр.КолВыпл
    BigDecimal vsegoPosVrNetrRashodSum = 0          // *.ПосВрНетр.Расход

    Integer vsegoPosBeremRodChislPoluch = 0           // Всего.ПосБеремРод.ЧислПолуч
    Integer vsegoPosBeremRodKolVypl = 0               // Всего.ПосБеремРод.КолВыпл
    BigDecimal vsegoPosBeremRodRashod = 0             // Всего.ПосБеремРод.Расход
    Integer vsegoPosBeremRodChislPoluchSum = 0        // *.ПосБеремРод.ЧислПолуч
    Integer vsegoPosBeremRodKolVyplSum = 0            // *.ПосБеремРод.КолВыпл
    BigDecimal vsegoPosBeremRodRashodSum = 0          // *.ПосБеремРод.Расход

    Integer vsegoEzPosUhodReb1ChislPoluch = 0           // Всего.ЕжПосУходРеб1.ЧислПолуч
    Integer vsegoEzPosUhodReb1KolVypl = 0               // Всего.ЕжПосУходРеб1.КолВыпл
    BigDecimal vsegoEzPosUhodReb1Rashod = 0             // Всего.ЕжПосУходРеб1.Расход
    Integer vsegoEzPosUhodReb1ChislPoluchSum = 0        // *.ЕжПосУходРеб1.ЧислПолуч
    Integer vsegoEzPosUhodReb1KolVyplSum = 0            // *.ЕжПосУходРеб1.КолВыпл
    BigDecimal vsegoEzPosUhodReb1RashodSum = 0          // *.ЕжПосУходРеб1.Расход

    Integer vsegoEzPosUhodReb2ChislPoluch = 0           // Всего.ЕжПосУходРеб2.ЧислПолуч
    Integer vsegoEzPosUhodReb2KolVypl = 0               // Всего.ЕжПосУходРеб2.КолВыпл
    BigDecimal vsegoEzPosUhodReb2Rashod = 0             // Всего.ЕжПосУходРеб2.Расход
    Integer vsegoEzPosUhodReb2ChislPoluchSum = 0        // *.ЕжПосУходРеб2.ЧислПолуч
    Integer vsegoEzPosUhodReb2KolVyplSum = 0            // *.ЕжПосУходРеб2.КолВыпл
    BigDecimal vsegoEzPosUhodReb2RashodSum = 0          // *.ЕжПосУходРеб2.Расход

    for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplPrichinaList) {
        if (raschsvVyplPrichina.nodeName == "ЧернобАЭС" || raschsvVyplPrichina.nodeName == "ПОМаяк") {
            List<RaschsvRashVypl> raschsvRashVyplList = raschsvVyplPrichina.raschsvRashVyplList
            BigDecimal chernobAndMaykVsegoRashod = 0       // Всего.Расход
            BigDecimal chernobAndMaykPosVrNetrRashod = 0   // ПосВрНетр.Расход
            BigDecimal chernobAndMaykPosBeremRodRashod = 0 // ПосБеремРод.Расход
            BigDecimal chernobAndMaykUhodRebRashod = 0     // ЕжПосУходРеб.Расход
            Integer chernobAndMaykUhodRebKolVypl = 0       // ЕжПосУходРеб.КолВыпл
            BigDecimal chernobAndMaykUhodReb1Rashod = 0    // ЕжПосУходРеб1.Расход
            Integer chernobAndMaykUhodReb1KolVypl = 0      // ЕжПосУходРеб1.КолВыпл
            BigDecimal chernobAndMaykUhodReb2Rashod = 0    // ЕжПосУходРеб2.Расход
            Integer chernobAndMaykUhodReb2KolVypl = 0      // ЕжПосУходРеб2.КолВыпл
            for (RaschsvRashVypl raschsvRashVypl : raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "Всего") {
                    chernobAndMaykVsegoRashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ПосВрНетр") {
                    chernobAndMaykPosVrNetrRashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ПосБеремРод") {
                    chernobAndMaykPosBeremRodRashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ЕжПосУходРеб") {
                    chernobAndMaykUhodRebRashod = raschsvRashVypl.rashod ?: 0
                    chernobAndMaykUhodRebKolVypl = raschsvRashVypl.kolVypl ?: 0
                } else if (raschsvRashVypl.nodeName == "ЕжПосУходРеб1") {
                    chernobAndMaykUhodReb1Rashod = raschsvRashVypl.rashod ?: 0
                    chernobAndMaykUhodReb1KolVypl = raschsvRashVypl.kolVypl ?: 0
                } else if (raschsvRashVypl.nodeName == "ЕжПосУходРеб2") {
                    chernobAndMaykUhodReb2Rashod = raschsvRashVypl.rashod ?: 0
                    chernobAndMaykUhodReb2KolVypl = raschsvRashVypl.kolVypl ?: 0
                }
            }
            def nodeName = "." + raschsvVyplPrichina.nodeName + "."
            // 3.3.5.1 Всего расходов равно сумме расходов по всем видам пособий (ЧернобАЭС)
            // 3.3.5.3 Всего расходов равно сумме расходов по всем видам пособий (Маяк)
            BigDecimal chernobVsegoRashodSum = chernobAndMaykPosVrNetrRashod + chernobAndMaykPosBeremRodRashod + chernobAndMaykUhodRebRashod
            if (!comparNumbEquals(chernobAndMaykVsegoRashod, chernobVsegoRashodSum)) {
                def pathAttrVal = pathAttrVyplFinFB + nodeName + "Всего = \"$chernobAndMaykVsegoRashod\""
                def pathAttrComp = ["ПосВрНетр.Расход", "ПосБеремРод.Расход", "ЕжПосУходРеб.Расход"].join(", ") + " = \"$chernobVsegoRashodSum\""
                logger.warnExp("%s не равен сумме %s",
                        "Всего расходов равно сумме расходов по всем видам пособий (${raschsvVyplPrichina.nodeName})",
                        null, pathAttrVal, pathAttrComp)
            }

            // 3.3.5.2 Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей
            // 3.3.5.4 Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей
            BigDecimal chernobUhodRebRashodSum = chernobAndMaykUhodReb1Rashod + chernobAndMaykUhodReb2Rashod
            if (!comparNumbEquals(chernobAndMaykUhodRebRashod, chernobUhodRebRashodSum)) {
                def pathAttrVal = pathAttrVyplFinFB + nodeName + "ЕжПосУходРеб.Расход = \"$chernobAndMaykUhodRebRashod\""
                def pathAttrComp = ["ЕжПосУходРеб1.Расход", "ЕжПосУходРеб2.Расход"].join(", ") + " = \"$chernobUhodRebRashodSum\""
                logger.warnExp("%s не равен сумме %s",
                        "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                        null, pathAttrVal, pathAttrComp)
            }
            Integer chernobUhodRebKolVyplSum = chernobAndMaykUhodReb1KolVypl + chernobAndMaykUhodReb2KolVypl
            if (!comparNumbEquals(chernobAndMaykUhodRebKolVypl, chernobUhodRebKolVyplSum)) {
                def pathAttrVal = pathAttrVyplFinFB + nodeName + "ЕжПосУходРеб.КолВыпл = \"$chernobAndMaykUhodRebKolVypl\""
                def pathAttrComp = ["ЕжПосУходРеб1.КолВыпл", "ЕжПосУходРеб2.КолВыпл"].join(", ") + " = \"$chernobUhodRebKolVyplSum\""
                logger.warnExp("%s не равен сумме %s",
                        "Сумма ежемесячных пособий по уходу за ребенком равна сумме пособий за первого ребенка и второго и последующих детей",
                        null, pathAttrVal, pathAttrComp)
            }
        } else if (raschsvVyplPrichina.nodeName == "СемипалатПолигон") {
            BigDecimal semipalatVsegoRashod = 0       // Всего.Расход
            BigDecimal semipalatPosVrNetrRashod = 0   // ПосВрНетр.Расход
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "Всего") {
                    semipalatVsegoRashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ПосВрНетр") {
                    semipalatPosVrNetrRashod = raschsvRashVypl.rashod ?: 0
                }
            }
            // 3.3.5.5 Всего расходов равно пособиям по временной нетрудоспособности (Семипалат)
            if (!comparNumbEquals(semipalatVsegoRashod, semipalatPosVrNetrRashod)) {
                def pathAttrVal = pathAttrVyplFinFB + ".СемипалатПолигон.Всего.Расход = \"$semipalatVsegoRashod\""
                def pathAttrComp = pathAttrVyplFinFB + ".СемипалатПолигон.ПосВрНетр.Расход = \"$semipalatPosVrNetrRashod\""
                logger.warnExp("%s не равен сумме %s",
                        "Всего расходов равно пособиям по временной нетрудоспособности (Семипалат)",
                        null, pathAttrVal, pathAttrComp)
            }
        }

        if (raschsvVyplPrichina.nodeName == "Всего") {
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "ПосВрНетр") {
                    vsegoPosVrNetrChislPoluch = raschsvRashVypl.chislPoluch ?: 0
                    vsegoPosVrNetrKolVypl = raschsvRashVypl.kolVypl ?: 0
                    vsegoPosVrNetrRashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ПосБеремРод") {
                    vsegoPosBeremRodChislPoluch = raschsvRashVypl.chislPoluch ?: 0
                    vsegoPosBeremRodKolVypl = raschsvRashVypl.kolVypl ?: 0
                    vsegoPosBeremRodRashod = raschsvRashVypl.rashod ?: 0
                }
            }
        } else if (["ЧернобАЭС", "ПОМаяк", "СемипалатПолигон", "ПодрОсобРиск", "ДопФЗ255"].contains(raschsvVyplPrichina.nodeName)) {
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "ПосВрНетр") {
                    vsegoPosVrNetrChislPoluchSum += raschsvRashVypl.chislPoluch ?: 0
                    vsegoPosVrNetrKolVyplSum += raschsvRashVypl.kolVypl ?: 0
                    vsegoPosVrNetrRashodSum += raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ПосБеремРод") {
                    vsegoPosBeremRodChislPoluchSum += raschsvRashVypl.chislPoluch ?: 0
                    vsegoPosBeremRodKolVyplSum += raschsvRashVypl.kolVypl ?: 0
                    vsegoPosBeremRodRashodSum += raschsvRashVypl.rashod ?: 0
                }
            }
        }

        // 3.3.5.8 Всего пособий по уходу за первым ребенком равно сумме этих пособий по различным категориям
        // 3.3.5.9 Всего пособий по уходу за вторым ребенком и последующими равно сумме этих пособий по различным категориям
        if (raschsvVyplPrichina.nodeName == "Всего") {
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "ЕжПосУходРеб1") {
                    vsegoEzPosUhodReb1ChislPoluch = raschsvRashVypl.chislPoluch ?: 0
                    vsegoEzPosUhodReb1KolVypl = raschsvRashVypl.kolVypl ?: 0
                    vsegoEzPosUhodReb1Rashod = raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ЕжПосУходРеб2") {
                    vsegoEzPosUhodReb2ChislPoluch = raschsvRashVypl.chislPoluch ?: 0
                    vsegoEzPosUhodReb2KolVypl = raschsvRashVypl.kolVypl ?: 0
                    vsegoEzPosUhodReb2Rashod = raschsvRashVypl.rashod ?: 0
                }
            }
        } else if (["ЧернобАЭС", "ПОМаяк", "ПодрОсобРиск"].contains(raschsvVyplPrichina.nodeName)) {
            for (RaschsvRashVypl raschsvRashVypl : raschsvVyplPrichina.raschsvRashVyplList) {
                if (raschsvRashVypl.nodeName == "ЕжПосУходРеб1") {
                    vsegoEzPosUhodReb1ChislPoluchSum += raschsvRashVypl.chislPoluch ?: 0
                    vsegoEzPosUhodReb1KolVyplSum += raschsvRashVypl.kolVypl ?: 0
                    vsegoEzPosUhodReb1RashodSum += raschsvRashVypl.rashod ?: 0
                } else if (raschsvRashVypl.nodeName == "ЕжПосУходРеб2") {
                    vsegoEzPosUhodReb2ChislPoluchSum += raschsvRashVypl.chislPoluch ?: 0
                    vsegoEzPosUhodReb2KolVyplSum += raschsvRashVypl.kolVypl ?: 0
                    vsegoEzPosUhodReb2RashodSum += raschsvRashVypl.rashod ?: 0
                }
            }
        }
    }

    // 3.3.5.6 Всего пособий по нетрудоспособности равно сумме этих пособий по различным категориям
    if (!comparNumbEquals(vsegoPosVrNetrChislPoluch, vsegoPosVrNetrChislPoluchSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосВрНетр.ЧислПолуч = \"$vsegoPosVrNetrChislPoluch\""
        def pathAttrComp = ["ЧернобАЭС.ПосВрНетр.ЧислПолуч", "ПОМаяк.ПосВрНетр.ЧислПолуч", "СемипалатПолигон.ПосВрНетр.ЧислПолуч",
                            "ПодрОсобРиск.ПосВрНетр.ЧислПолуч", "ДопФЗ255.ПосВрНетр.ЧислПолуч"].join(", ") + " = \"$vsegoPosVrNetrChislPoluchSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по нетрудоспособности равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoPosVrNetrKolVypl, vsegoPosVrNetrKolVyplSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосВрНетр.КолВыпл = \"$vsegoPosVrNetrKolVypl\""
        def pathAttrComp = ["ЧернобАЭС.ПосВрНетр.КолВыпл", "ПОМаяк.ПосВрНетр.КолВыпл", "СемипалатПолигон.ПосВрНетр.КолВыпл",
                            "ПодрОсобРиск.ПосВрНетр.КолВыпл", "ДопФЗ255.ПосВрНетр.КолВыпл"].join(", ") + " = \"$vsegoPosVrNetrKolVyplSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по нетрудоспособности равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoPosVrNetrRashod, vsegoPosVrNetrRashodSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосВрНетр.Расход = \"$vsegoPosVrNetrRashod\""
        def pathAttrComp = ["ЧернобАЭС.ПосВрНетр.Расход", "ПОМаяк.ПосВрНетр.Расход", "СемипалатПолигон.ПосВрНетр.Расход",
                            "ПодрОсобРиск.ПосВрНетр.Расход", "ДопФЗ255.ПосВрНетр.Расход"].join(", ") + " = \"$vsegoPosVrNetrRashodSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по нетрудоспособности равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }

    // 3.3.5.7 Всего пособий беременности и родам равно сумме этих пособий по различным категориям
    if (!comparNumbEquals(vsegoPosBeremRodChislPoluch, vsegoPosBeremRodChislPoluchSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосБеремРод.ЧислПолуч = \"$vsegoPosBeremRodChislPoluch\""
        def pathAttrComp = ["ЧернобАЭС.ПосБеремРод.ЧислПолуч", "ПОМаяк.ПосБеремРод.ЧислПолуч", "ПодрОсобРиск.ПосБеремРод.ЧислПолуч",
                            "ДопФЗ255.ПосБеремРод.ЧислПолуч"].join(", ") + " = \"$vsegoPosBeremRodChislPoluchSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий беременности и родам равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoPosBeremRodKolVypl, vsegoPosBeremRodKolVyplSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосБеремРод.КолВыпл = \"$vsegoPosBeremRodKolVypl\""
        def pathAttrComp = ["ЧернобАЭС.ПосБеремРод.КолВыпл", "ПОМаяк.ПосБеремРод.КолВыпл", "ПодрОсобРиск.ПосБеремРод.КолВыпл",
                            "ДопФЗ255.ПосБеремРод.КолВыпл"].join(", ") + " = \"$vsegoPosBeremRodKolVyplSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий беременности и родам равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoPosBeremRodRashod, vsegoPosBeremRodRashodSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ПосБеремРод.Расход = \"$vsegoPosBeremRodRashod\""
        def pathAttrComp = ["ЧернобАЭС.ПосБеремРод.Расход", "ПОМаяк.ПосБеремРод.Расход", "ПодрОсобРиск.ПосБеремРод.Расход",
                            "ДопФЗ255.ПосБеремРод.Расход"].join(", ") + " = \"$vsegoPosBeremRodRashodSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий беременности и родам равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }

    // 3.3.5.8 Всего пособий по уходу за первым ребенком равно сумме этих пособий по различным категориям
    if (!comparNumbEquals(vsegoEzPosUhodReb1ChislPoluch, vsegoEzPosUhodReb1ChislPoluchSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб1.ЧислПолуч = \"$vsegoEzPosUhodReb1ChislPoluch\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб1.ЧислПолуч", "ПОМаяк.ЕжПосУходРеб1.ЧислПолуч", "ПодрОсобРиск.ЕжПосУходРеб1.ЧислПолуч"].join(", ") + " = \"$vsegoEzPosUhodReb1ChislPoluchSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за первым ребенком равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoEzPosUhodReb1KolVypl, vsegoEzPosUhodReb1KolVyplSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб1.КолВыпл = \"$vsegoEzPosUhodReb1KolVypl\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб1.КолВыпл", "ПОМаяк.ЕжПосУходРеб1.КолВыпл", "ПодрОсобРиск.ЕжПосУходРеб1.КолВыпл"].join(", ") + " = \"$vsegoEzPosUhodReb1KolVyplSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за первым ребенком равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoEzPosUhodReb1Rashod, vsegoEzPosUhodReb1RashodSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб1.Расход = \"$vsegoEzPosUhodReb1Rashod\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб1.Расход", "ПОМаяк.ЕжПосУходРеб1.Расход", "ПодрОсобРиск.ЕжПосУходРеб1.Расход"].join(", ") + " = \"$vsegoEzPosUhodReb1RashodSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за первым ребенком равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }

    // 3.3.5.9 Всего пособий по уходу за вторым ребенком и последующими равно сумме этих пособий по различным категориям
    if (!comparNumbEquals(vsegoEzPosUhodReb2ChislPoluch, vsegoEzPosUhodReb2ChislPoluchSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб2.ЧислПолуч = \"$vsegoEzPosUhodReb2ChislPoluch\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб2.ЧислПолуч", "ПОМаяк.ЕжПосУходРеб2.ЧислПолуч", "ПодрОсобРиск.ЕжПосУходРеб2.ЧислПолуч"].join(", ") + " = \"$vsegoEzPosUhodReb2ChislPoluchSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за вторым ребенком и последующими равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoEzPosUhodReb2KolVypl, vsegoEzPosUhodReb2KolVyplSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб2.КолВыпл = \"$vsegoEzPosUhodReb2KolVypl\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб2.КолВыпл", "ПОМаяк.ЕжПосУходРеб2.КолВыпл", "ПодрОсобРиск.ЕжПосУходРеб2.КолВыпл"].join(", ") + " = \"$vsegoEzPosUhodReb2KolVyplSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за вторым ребенком и последующими равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }
    if (!comparNumbEquals(vsegoEzPosUhodReb2Rashod, vsegoEzPosUhodReb2RashodSum)) {
        def pathAttrVal = pathAttrVyplFinFB + ".Всего.ЕжПосУходРеб2.Расход = \"$vsegoEzPosUhodReb2Rashod\""
        def pathAttrComp = ["ЧернобАЭС.ЕжПосУходРеб2.Расход", "ПОМаяк.ЕжПосУходРеб2.Расход", "ПодрОсобРиск.ЕжПосУходРеб2.Расход"].join(", ") + " = \"$vsegoEzPosUhodReb2RashodSum\""
        logger.warnExp("%s не равен сумме %s",
                "Всего пособий по уходу за вторым ребенком и последующими равно сумме этих пособий по различным категориям",
                null, pathAttrVal, pathAttrComp)
    }

    ScriptUtils.checkInterrupted();

    // *********************************** ПравТариф3.1.427 ***********************************
    RaschsvPravTarif31427 raschsvPravTarif31427 = raschsvPravTarif31427Service.findRaschsvPravTarif31427(declarationData.id)
    def pathAttrPravTarif31427 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф3.1.427"

    // 3.3.6.1 ПравТариф3.1.427 заполняется только, если код тарифа плательщика 06
    if (opsOmsIsExistTarifPlat_06 == true && raschsvPravTarif31427 == null ||
            opsOmsIsExistTarifPlat_06 == false && raschsvPravTarif31427 != null) {
        logger.warnExp("Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф3.1.427 заполняются при коде тарифа плательщика '06'",
                "Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф3.1.427 заполняются при коде тарифа плательщика '06'", null)
    }

    if (raschsvPravTarif31427 != null && opsOmsIsExistTarifPlat_06 == true) {
        // 3.3.6.2 Численность не менее 7
        if (raschsvPravTarif31427.srChisl9mpr < 7) {
            def pathAttrVal = pathAttrPravTarif31427 + ".СрЧисл_9МПр = \"$raschsvPravTarif31427.srChisl9mpr\""
            logger.warnExp("%s менее 7 чел. В соответствии с п.5 ст.427 НК РФ необходимо уточнить правомерность применения пониженного тарифа",
                    "Численность не менее 7",
                    null, pathAttrVal)
        }
        if (raschsvPravTarif31427.srChislPer < 7) {
            def pathAttrVal = pathAttrPravTarif31427 + ".СрЧисл_Пер = \"$raschsvPravTarif31427.srChislPer\""
            logger.warnExp("%s менее 7 чел. В соответствии с п.5 ст.427 НК РФ необходимо уточнить правомерность применения пониженного тарифа",
                    "Численность не менее 7",
                    null, pathAttrVal)
        }

        // 3.3.6.3 Доля не менее 90%
        if (raschsvPravTarif31427.dohDoh54279mpr < 90) {
            def pathAttrVal = pathAttrPravTarif31427 + ".ДолДох5.427_9МПр = \"$raschsvPravTarif31427.dohDoh54279mpr\""
            logger.warnExp("%s менее 90%%. В соответствии с п.5 ст.427 НК РФ необходимо уточнить правомерность применения пониженного тарифа",
                    "Доля не менее 90%%",
                    null, pathAttrVal)
        }
        if (raschsvPravTarif31427.dohDoh5427per < 90) {
            def pathAttrVal = pathAttrPravTarif31427 + ".ДолДох5.427_Пер = \"$raschsvPravTarif31427.dohDoh5427per\""
            logger.warnExp("%s менее 90%%. В соответствии с п.5 ст.427 НК РФ необходимо уточнить правомерность применения пониженного тарифа",
                    "Доля не менее 90%%",
                    null, pathAttrVal)
        }
    }

    ScriptUtils.checkInterrupted();

    // *********************************** ПравТариф5.1.427 ***********************************
    RaschsvPravTarif51427 raschsvPravTarif51427 = raschsvPravTarif51427Service.findRaschsvPravTarif51427(declarationData.id)
    def pathAttrPravTarif51427 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф5.1.427"

    // 3.3.7.1 ПравТариф5.1.427 заполняется только, если код тарифа плательщика 08
    if (opsOmsIsExistTarifPlat_08 == true && raschsvPravTarif51427 == null ||
            opsOmsIsExistTarifPlat_08 == false && raschsvPravTarif51427 != null) {
        logger.warnExp("Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф5.1.427 заполняются при коде тарифа плательщика '08'",
                "Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф5.1.427 заполняются при коде тарифа плательщика '08'", null)
    }

    if (raschsvPravTarif51427 != null && opsOmsIsExistTarifPlat_08 == true) {
        // 3.3.7.2 Сумма доходов всего не менее суммы доходов по п.6 ст. 427
        if (comparNumbGreater(raschsvPravTarif51427.doh6_427, raschsvPravTarif51427.doh346_15vs)) {
            def pathAttrVal = pathAttrPravTarif51427 + ".Дох346.15Вс = \"$raschsvPravTarif51427.doh346_15vs\""
            def pathAttrComp = pathAttrPravTarif51427 + ".Дох6.427 = \"$raschsvPravTarif51427.doh6_427\"."
            logger.warnExp("%s не может быть меньше %s",
                    "Сумма доходов всего не менее суммы доходов по п.6 ст. 427",
                    null, pathAttrVal, pathAttrComp)
        }
    }

    ScriptUtils.checkInterrupted();

    // *********************************** ПравТариф7.1.427 ***********************************
    RaschsvPravTarif71427 raschsvPravTarif71427 = raschsvPravTarif71427Service.findRaschsvPravTarif71427(declarationData.id)
    def pathAttrPravTarif71427 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф7.1.427"

    // 3.3.8.1 ПравТариф7.1.427 заполняется только, если код тарифа плательщика 10
    if (opsOmsIsExistTarifPlat_10 == true && raschsvPravTarif71427 == null ||
            opsOmsIsExistTarifPlat_10 == false && raschsvPravTarif71427 != null) {
        logger.warnExp("Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф7.1.427 заполняются при коде тарифа плательщика '10'",
                "Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.ПравТариф7.1.427 заполняются при коде тарифа плательщика '10'", null)
    }

    if (raschsvPravTarif71427 != null && opsOmsIsExistTarifPlat_10 == true) {
        // 3.3.8.2 	Сумма доходов всего не менее суммы доходов по отдельным разделам
        def dohVsPred = raschsvPravTarif71427.dohVsPred ?: 0
        def dohCelPostPred = raschsvPravTarif71427.dohCelPostPred ?: 0
        def dohGrantPred = raschsvPravTarif71427.dohGrantPred ?: 0
        def dohEkDeyatPred = raschsvPravTarif71427.dohEkDeyatPred ?: 0
        if (comparNumbGreater(dohCelPostPred + dohGrantPred + dohEkDeyatPred, dohVsPred)) {
            def pathAttrVal = pathAttrPravTarif71427 + ".ДохВсПред = \"$raschsvPravTarif71427.dohVsPred\""
            def pathAttrComp = pathAttrPravTarif71427 + ".ДохЦелПостПред = \"$dohCelPostPred\", "
            pathAttrComp += pathAttrPravTarif71427 + ".ДохГрантПред = \"$dohGrantPred\", "
            pathAttrComp += pathAttrPravTarif71427 + ".ДохЭкДеятПред = \"$dohEkDeyatPred\"."
            logger.warnExp("%s не может быть меньше суммы %s",
                    "Сумма доходов всего не менее суммы доходов по отдельным разделам",
                    null, pathAttrVal, pathAttrComp)
        }
    }

    ScriptUtils.checkInterrupted();

    // *********************************** СвПримТариф9.1.427 ***********************************
    RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 = raschsvSvPrimTarif91427Service.findRaschsvSvPrimTarif91427(declarationData.id)

    // 3.3.9.1 Элементы не заполнены
    if (raschsvSvPrimTarif91427 != null) {
        logger.warnExp("Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф9.1.427 заполняются только для ИП",
                "Элементы блока Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф9.1.427 заполняются только для ИП", null)
    }

    ScriptUtils.checkInterrupted();

    // *********************************** СвПримТариф2.2.425 ***********************************
    RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 = raschsvSvPrimTarif22425Service.findRaschsvSvPrimTarif22425(declarationData.id)
    def pathAttrSvPrimTarif22425 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф2.2.425"

    if (raschsvSvPrimTarif22425 != null) {
        RaschsvVyplatIt425 raschsvVyplatIt425 = raschsvSvPrimTarif22425.raschsvVyplatIt425
        RaschsvSvSum1Tip vyplatIt425Sum = raschsvVyplatIt425.raschsvSvSum1Tip
        BigDecimal vyplatIt425Sum1 = vyplatIt425Sum ? vyplatIt425Sum.sum1mPosl3m ?: 0 : 0
        BigDecimal vyplatIt425Sum2 = vyplatIt425Sum ? vyplatIt425Sum.sum2mPosl3m ?: 0 : 0
        BigDecimal vyplatIt425Sum3 = vyplatIt425Sum ? vyplatIt425Sum.sum3mPosl3m ?: 0 : 0
        BigDecimal svInoGrazdSum1 = 0
        BigDecimal svInoGrazdSum2 = 0
        BigDecimal svInoGrazdSum3 = 0
        for (RaschsvSvInoGrazd raschsvSvInoGrazd : raschsvSvPrimTarif22425.raschsvSvInoGrazdList) {
            RaschsvSvSum1Tip raschsvSvSum1Tip = raschsvSvInoGrazd.raschsvSvSum1Tip
            svInoGrazdSum1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
            svInoGrazdSum2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
            svInoGrazdSum3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
        }
        // 3.3.10.1 Итого выплат равно сумме по всем иностранным гражданам
        if (raschsvSvnpPodpisant.nomKorr == 0) {
            if (!comparNumbEquals(vyplatIt425Sum1, svInoGrazdSum1)) {
                def pathAttrVal = pathAttrSvPrimTarif22425 + ".ВыплатИт.Сум1Посл3М = \"$vyplatIt425Sum1\""
                logger.warnExp("%s не равен сумме выплат по всем иностранным гражданам",
                        "Итого выплат равно сумме по всем иностранным гражданам",
                        null, pathAttrVal)
            }
            if (!comparNumbEquals(vyplatIt425Sum2, svInoGrazdSum2)) {
                def pathAttrVal = pathAttrSvPrimTarif22425 + ".ВыплатИт.Сум2Посл3М = \"$vyplatIt425Sum2\""
                logger.warnExp("%s не равен сумме выплат по всем иностранным гражданам",
                        "Итого выплат равно сумме по всем иностранным гражданам",
                        null, pathAttrVal)
            }
            if (!comparNumbEquals(vyplatIt425Sum3, svInoGrazdSum3)) {
                def pathAttrVal = pathAttrSvPrimTarif22425 + ".ВыплатИт.Сум3Посл3М = \"$vyplatIt425Sum3\""
                logger.warnExp("%s не равен сумме выплат по всем иностранным гражданам",
                        "Итого выплат равно сумме по всем иностранным гражданам",
                        null, pathAttrVal)
            }
        }
    }

    ScriptUtils.checkInterrupted();

    // *********************************** СвПримТариф1.3.422 ***********************************
    RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 = raschsvSvPrimTarif13422Service.findRaschsvSvPrimTarif13422(declarationData.id)
    def pathAttrSvPrimTarif13422 = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СвПримТариф1.3.422"

    if (raschsvSvPrimTarif13422 != null) {
        RaschsvVyplatIt422 raschsvVyplatIt422 = raschsvSvPrimTarif13422.raschsvVyplatIt422
        RaschsvSvSum1Tip vyplatIt422Sum = raschsvVyplatIt422.raschsvSvSum1Tip
        BigDecimal vyplatIt422Sum1 = vyplatIt422Sum ? vyplatIt422Sum.sum1mPosl3m ?: 0 : 0
        BigDecimal vyplatIt422Sum2 = vyplatIt422Sum ? vyplatIt422Sum.sum2mPosl3m ?: 0 : 0
        BigDecimal vyplatIt422Sum3 = vyplatIt422Sum ? vyplatIt422Sum.sum3mPosl3m ?: 0 : 0
        BigDecimal svedObuchSum1 = 0
        BigDecimal svedObuchSum2 = 0
        BigDecimal svedObuchSum3 = 0
        for (RaschsvSvedObuch raschsvSvedObuch : raschsvSvPrimTarif13422.raschsvSvedObuchList) {
            RaschsvSvSum1Tip raschsvSvSum1Tip = raschsvSvedObuch.raschsvSvSum1Tip
            svedObuchSum1 += raschsvSvSum1Tip.sum1mPosl3m ?: 0
            svedObuchSum2 += raschsvSvSum1Tip.sum2mPosl3m ?: 0
            svedObuchSum3 += raschsvSvSum1Tip.sum3mPosl3m ?: 0
        }

        // 3.3.11.1 Итого выплат равно сумме по всем обучающимся
        if (raschsvSvnpPodpisant.nomKorr == 0) {
            if (!comparNumbEquals(vyplatIt422Sum1, svedObuchSum1)) {
                def pathAttrVal = pathAttrSvPrimTarif13422 + ".ВыплатИт.Сум1Посл3М = \"$vyplatIt422Sum1\""
                logger.warnExp("%s не равен сумме выплат по всем обучающимся",
                        "Итого выплат равно сумме по всем обучающимся",
                        null, pathAttrVal)
            }
            if (!comparNumbEquals(vyplatIt422Sum2, svedObuchSum2)) {
                def pathAttrVal = pathAttrSvPrimTarif13422 + ".ВыплатИт.Сум2Посл3М = \"$vyplatIt422Sum2\""
                logger.warnExp("%s не равен сумме выплат по всем обучающимся",
                        "Итого выплат равно сумме по всем обучающимся",
                        null, pathAttrVal)
            }
            if (!comparNumbEquals(vyplatIt422Sum3, svedObuchSum3)) {
                def pathAttrVal = pathAttrSvPrimTarif13422 + ".ВыплатИт.Сум3Посл3М = \"$vyplatIt422Sum3\""
                logger.warnExp("%s не равен сумме выплат по всем обучающимся",
                        "Итого выплат равно сумме по всем обучающимся",
                        null, pathAttrVal)
            }
        }
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
    if (logger.containsLevel(LogLevel.WARNING)) {
        throw new ServiceException("ТФ не соответствует XSD-схеме. Загрузка невозможна.");
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

    println "Загрузка справочников для xml-проверок (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Загрузка справочников для xml-проверок (" + (System.currentTimeMillis() - time) + " мс)");

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
                logger.warnExp("%s='%s' не совпадает с настройками подразделения.",
                        "Соответствие кода места, по которому предоставляется документ Настройкам подразделения",
                        null, pathAttr, poMestuCodeXml)
            }

            // 2.1.2 Актуальность кода места
            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
            def poMestuActualParam = mapActualPresentPlace.get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
            def poMestuCodeActualParam = poMestuActualParam?.get(RF_CODE)?.value
            if (poMestuCodeParam != poMestuCodeActualParam || !poMestuActualParam?.get(RF_FOR_FOND)?.value) {
                logger.warnExp("В настройках подразделений указан неактуальный код места, по которому предоставляется документ='%s'",
                        "Актуальность кода места, по которому предоставляется документ в справочнике 'Коды места представления расчета'",
                        null, poMestuCodeActualParam)
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
                        logger.warnExp("%s='%s' не совпадает с ОКВЭД='%s'",
                                "Соответствие ОКВЭД настройкам подразделения",
                                null, pathAttr, okvedCodeXml, okvedCodeParam)
                    }

                    // 2.1.4 Актуальность ОКВЭД
                    // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                    def okvedCodeActualParam = mapActualOkvedCode.get(departmentParamIncomeRow?.OKVED?.referenceValue)
                    if (okvedCodeParam != okvedCodeActualParam) {
                        def pathAttr = "Файл.Документ.СвНП.ОКВЭД"
                        logger.warnExp("%s='%s' неактуальный",
                                "Актуальность ОКВЭД в справочнике 'Общероссийский классификатор видов экономической деятельности'",
                                null, pathAttr, okvedCodeParam)
                    }

                    // 2.1.5 Соответсвие ИНН ЮЛ Общим параметрам
                    if (sberbankInnXml != sberbankInnParam) {
                        def pathAttr = "Файл.Документ.СвНП.НПЮЛ.ИННЮЛ"
                        logger.warnExp("%s='%s' не совпадает с Общим параметром 'ИНН ПАО Сбербанк'='%s'",
                                "Соответсвие ИНН ЮЛ",
                                null, pathAttr, sberbankInnXml, sberbankInnParam)
                    }

                    // 2.1.6 Соответсвие КПП ЮЛ настройкам подразделения
                    def kppParam = departmentParamIncomeRow?.KPP?.stringValue
                    if (kppXml != kppParam) {
                        def pathAttr = "Файл.Документ.СвНП.НПЮЛ.КПП"
                        logger.warnExp("%s='%s' не совпадает с КПП='%s'",
                                "Соответсвие КПП ЮЛ настройкам подразделения",
                                null, pathAttr, kppXml, kppParam)
                    }

                    // Если узел СвРеоргЮЛ существует
                    if (sVReorgYLIsExist) {
                        // 2.1.7 Соответствие формы реорганизации
                        def sVReorgYLFormParam = mapReorgFormCode.get(departmentParamIncomeRow?.REORG_FORM_CODE?.referenceValue)
                        if (sVReorgYLFormXml != sVReorgYLFormParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.ФормРеорг"
                            logger.warnExp("%s='%s' не совпадает c формой реорганизации='%s'",
                                    "Соответствие формы реорганизации настройкам подразделения",
                                    null, pathAttr, sVReorgYLFormXml, sVReorgYLFormParam)
                        }

                        // 2.1.8 Соответствие ИНН реорганизованной организации
                        def sVReorgYLInnParam = departmentParamIncomeRow?.REORG_INN?.stringValue
                        if (sVReorgYLInnXml != sVReorgYLInnParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.ИННЮЛ"
                            logger.warnExp("%s='%s' для организации плательщика страховых взносов не совпадает с ИНН реорганизованной организации='%s'",
                                    "Соответствие ИНН реорганизованной организации настройкам подразделения",
                                    null, pathAttr, sVReorgYLInnXml, sVReorgYLInnParam)
                        }

                        // 2.1.9 Соответствие КПП реорганизованной организации
                        def sVReorgYLKppParam = departmentParamIncomeRow?.REORG_KPP?.stringValue
                        if (sVReorgYLKppXml != sVReorgYLKppParam) {
                            def pathAttr = "Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.КПП"
                            logger.warnExp("%s='%s' не совпадает с КПП реорганизованной организации='%s'",
                                    "Соответствие КПП реорганизованной организации настройкам подразделения",
                                    null, pathAttr, sVReorgYLKppXml, sVReorgYLKppParam)
                        }
                    }

                } else if (documentChildNode.name == NODE_NAME_RASCHET_SV) {
                    // РасчетСВ
                    documentChildNode.childNodes().each { raschetSvChildNode ->

                        ScriptUtils.checkInterrupted();

                        // ОбязПлатСВ
                        if (raschetSvChildNode.name == NODE_NAME_OBYAZ_PLAT_SV) {

                            // 2.2.1 Соответствие кода ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            // xml-файл содержит только один узел ОбязПлатСВ, поэтому обращение к БД от сюда допустимо
                            def oktmoXml = raschetSvChildNode.attributes()[OBYAZ_PLAT_SV_OKTMO]
                            def oktmoParam = getRefBookValue(REF_BOOK_OKTMO_ID, departmentParamIncomeRow?.OKTMO?.referenceValue)
                            if (oktmoXml != oktmoParam?.CODE?.stringValue) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_OBYAZ_PLAT_SV, OBYAZ_PLAT_SV_OKTMO].join(".")
                                logger.warnExp("%s='%s' не совпадает с ОКТМО='%s'",
                                        "Соответствие кода ОКТМО настройкам подразделения",
                                        null, pathAttr, oktmoXml, oktmoParam?.CODE?.stringValue)
                            }

                            // 2.2.2 Актуальность ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                            if (oktmoParam && !isExistsOKTMO(oktmoParam?.CODE?.stringValue)) {
                                logger.warnExp("В настройках подразделений указан неактуальный ОКТМО='%s'",
                                        "Актуальность ОКТМО",
                                        null, oktmoParam?.CODE?.stringValue)
                            }

                            // РасчСВ_ОПС_ОМС
                            raschetSvChildNode.childNodes().each { raschSvOpsOmsNode ->
                                if (raschSvOpsOmsNode.name == NODE_NAME_RASCH_SV_OPS_OMS) {

                                    // 2.2.3 Соответствие кода тарифа плательщика справочнику
                                    def tariffPayerCodeXml = raschSvOpsOmsNode.attributes()[RASCH_SV_OPS_OMS_TARIF_PLAT]
                                    def tariffPayerParam = mapActualTariffPayerCode.get(tariffPayerCodeXml)
                                    if (tariffPayerParam != null && !tariffPayerParam?.get(RF_FOR_OPS_OMS)?.value) {
                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, RASCH_SV_OPS_OMS_TARIF_PLAT].join(".")
                                        logger.warnExp("%s='%s' не найден (не действует) в справочнике 'Коды тарифа плательщика'",
                                                "Соответствие кода тарифа плательщика справочнику 'Коды тарифа плательщика'",
                                                null, pathAttr, tariffPayerCodeXml)
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
                                                        logger.warnExp("%s='%s' не найден (не действует) в справочнике 'Основания заполнения'",
                                                                "Соответствие значения основания заполнения справочнику 'Основания заполнения'",
                                                                null, pathAttr, fillBaseXml)
                                                    }

                                                    // 2.2.5 Значение кода класса условий труда
                                                    if (!listActualHardWork.contains(hardWorkXml)) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV,
                                                                        NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, NODE_NAME_RASCH_SV_OPS428,
                                                                        NODE_NAME_RASCH_SV_428_3, RASCH_SV_OPS428_3_KLAS_USL_TRUD].join(".")
                                                        logger.warnExp("%s='%s' не найден (не действует) в справочнике 'Коды классов условий труда'",
                                                                "Соответствие значения кода класса условий труда справочнику 'Коды классов условий труда'",
                                                                null, pathAttr, hardWorkXml)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        } else if (raschetSvChildNode.name == NODE_NAME_PERS_SV_STRAH_LIC) {
                            // ПерсСвСтрахЛиц

                            // Получим ФИО и СНИЛС
                            String fioAndSNILS
                            raschetSvChildNode.childNodes().each { danFlPolushNode ->
                                String snils = danFlPolushNode.attributes()[DAN_FL_POLUCH_SNILS]
                                danFlPolushNode.childNodes().each { fioNode ->
                                    if (fioNode.name == NODE_NAME_FIO) {
                                        // Разбор узла ФИО
                                        String lastName = fioNode.attributes()[FIO_FAMILIA]
                                        String firstName = fioNode.attributes()[FIO_IMYA]
                                        String middleName = fioNode.attributes()[FIO_OTCHESTVO_NAME] ?: ""
                                        fioAndSNILS = "ФИО: $lastName $firstName ${(middleName ? " " + middleName : "")}, СНИЛС: $snils"
                                    }
                                }
                            }

                            // 2.3.1 Корректность номера корректировки
                            def nomKorrPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_NOM_KORR]
                            if (nomKorrDocXml != nomKorrPersXml) {
                                String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.НомКорр"
                                logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность номера корректировки", fioAndSNILS, pathError,
                                        "$pathError='${nomKorrPersXml}' не соответствует номеру корректировки файла '$fileName'")
                            }

                            // 2.3.2 Корректность периода
                            def periodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_PERIOD]
                            if (periodDocXml != periodPersXml) {
                                String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.Период"
                                logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность периода", fioAndSNILS, pathError,
                                        "$pathError='${periodPersXml}' не соответствует периоду файла '$fileName'")
                            }

                            // 2.3.3 Корректность отчетного года
                            def otchetGodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]
                            if (otchetGodDocXml != otchetGodPersXml) {
                                String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ОтчетГод"
                                logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Корректность отчетного года", fioAndSNILS, pathError,
                                        "$pathError='${otchetGodPersXml}' не соответствует отчетному году файла '$fileName'")
                            }

                            raschetSvChildNode.childNodes().each { persSvStrahLicChildNode ->
                                if (persSvStrahLicChildNode.name == NODE_NAME_SV_VYPL_SVOPS) {
                                    // СвВыплСВОПС
                                    persSvStrahLicChildNode.childNodes().each { svVyplSvopsChildNode ->
                                        // ВыплСВДоп
                                        if (svVyplSvopsChildNode.name == NODE_NAME_VYPL_SV_DOP) {
                                            svVyplSvopsChildNode.childNodes().each { vyplSvDopMtNode ->
                                                // ВыплСВДопМТ
                                                if (vyplSvDopMtNode.name == NODE_NAME_VYPL_SV_DOP_MT) {
                                                    // 2.3.4 Значение кода тарифа
                                                    def tariffPayerCodeXml = vyplSvDopMtNode.attributes()[VYPL_SV_DOP_MT_TARIF]
                                                    actualTariffPayerCode = mapActualTariffPayerCode.get(tariffPayerCodeXml)
                                                    if (!actualTariffPayerCode && actualTariffPayerCode?.get(RF_FOR_OPS_DOP)?.value) {
                                                        String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.СвВыплСВОПС.ВыплСВДоп.ВыплСВДопМТ.Тариф"
                                                        logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие значения кода тарифа справочнику 'Коды тарифа плательщика'", fioAndSNILS, pathError,
                                                                "$pathError='$tariffPayerCodeXml' не найден (не действует) в справочнике 'Коды тарифа плательщика'")
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
                                                    if (!listPersonCategory?.contains(kodKatLisCodeXml)) {
                                                        String pathError = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.СвВыплСВОПС.СвВыпл.СвВыплМК.КодКатЛиц"
                                                        logger.warnExp("Ошибка в значении: %s. Текст ошибки: %s.", "Соответствие значения кода категории застрахованного лица справочнику 'Коды категорий застрахованных лиц'", fioAndSNILS, pathError,
                                                                "$pathError='$kodKatLisCodeXml' не найден (не действует) в справочнике 'Коды категорий застрахованных лиц'")
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
    println "Проверки xml (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки xml (" + (System.currentTimeMillis() - time) + " мс)");

    // ------------Сводные данные об обязательствах плательщика
}

/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

/**
 * Сравнение чисел с плавающей точкой через эпсилон-окрестности
 */
boolean comparNumbEquals(def d1, def d2) {
    return (Math.abs(d1 - d2) < 0.001)
}
boolean comparNumbGreater(double d1, double d2) {
    return (d1 - d2 > 0.001)
}

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
 * Выгрузка из справочников по условию и версии
 * @param refBookId
 * @param whereClause
 * @return
 * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
 */
def getRefBookByRecordVersionWhere(def long refBookId, def whereClause, def version) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
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
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataId() {
    if (dulActualCache.isEmpty()) {
        String whereClause = """
            JOIN ref_book_person p ON (frb.person_id = p.id)
            JOIN raschsv_pers_sv_strah_lic np ON (np.declaration_data_id = ${declarationData.id} AND p.id = np.person_id)
        """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_ID_DOC_ID, whereClause, getReportPeriodEndDate() - 1)

        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            def dulList = dulActualCache.get(refBookPersonId);
            if (dulList == null) {
                dulList = [];
            }
            dulList.add(refBookValues);
            dulActualCache.put(refBookPersonId, dulList)
        }
    }
    return dulActualCache
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





//TODO вынес handler в скрипт, чтобы не обновлять ядро на нексте

/**
 * @author Andrey Drunk
 */
public class NaturalPersonRefbookScriptHandler extends NaturalPersonRefbookHandler {

    /**
     *
     */
    private Map<Long, NaturalPerson> refbookPersonTempMap;

    /**
     * Карта для создания идкнтификаторов ФЛ
     */
    private Map<Long, Map<Long, PersonIdentifier>> identitiesMap;

    /**
     * Карта для создания документов ФЛ
     */
    private Map<Long, Map<Long, PersonDocument>> documentsMap;

    /**
     * Кэш справочника страны
     */
    private Map<Long, Country> countryMap;

    /**
     * Кэш справочника статусы Налогоплателищика
     */
    private Map<Long, TaxpayerStatus> taxpayerStatusMap;

    /**
     * Кэш справочника типы документов
     */
    private Map<Long, DocType> docTypeMap;

    /**
     *
     */
    public NaturalPersonRefbookScriptHandler() {
        super();
        refbookPersonTempMap = new HashMap<Long, NaturalPerson>();
        identitiesMap = new HashMap<Long, Map<Long, PersonIdentifier>>();
        documentsMap = new HashMap<Long, Map<Long, PersonDocument>>();
    }

    @Override
    public void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException {

        //Идентификатор записи первичной формы
        Long primaryPersonId = SqlUtils.getLong(rs, PRIMARY_PERSON_ID);

        //if (primaryPersonId == null) {throw new ServiceException("Не задано значение PRIMARY_PERSON_ID");}

        //Список сходных записей
        Map<Long, NaturalPerson> similarityPersonMap = map.get(primaryPersonId);

        if (similarityPersonMap == null) {
            similarityPersonMap = new HashMap<Long, NaturalPerson>();
            map.put(primaryPersonId, similarityPersonMap);
        }

        //Идентификатор справочника
        Long refBookPersonId = SqlUtils.getLong(rs, REFBOOK_PERSON_ID);

        NaturalPerson naturalPerson = similarityPersonMap.get(refBookPersonId);

        if (naturalPerson == null) {
            naturalPerson = buildNaturalPerson(rs, refBookPersonId, primaryPersonId);
            similarityPersonMap.put(refBookPersonId, naturalPerson);
        }


        //Добавляем документы физлица
        addPersonDocument(rs, naturalPerson);

        //Добавляем идентификаторы
        addPersonIdentifier(rs, naturalPerson);

        //Адрес
        Address address = buildAddress(rs);
        naturalPerson.setAddress(address);

        //System.out.println(rowNum + ", primaryPersonId=" + primaryPersonId + ", [" + naturalPerson + "][" + Arrays.toString(naturalPerson.getPersonDocumentList().toArray()) + "][" + Arrays.toString(naturalPerson.getPersonIdentityList().toArray()) + "][" + address + "]");

    }

    private void addPersonIdentifier(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {

        Long primaryPersonId = naturalPerson.getPrimaryPersonId();
        Long refBookPersonId = naturalPerson.getId();
        Long personIdentifierId = SqlUtils.getLong(rs, "book_id_tax_payer_id");
        Map<Long, PersonIdentifier> personIdentityMap = identitiesMap.get(refBookPersonId);

        if (personIdentityMap == null) {
            personIdentityMap = new HashMap<Long, PersonIdentifier>();
            identitiesMap.put(refBookPersonId, personIdentityMap);
        }

        if (personIdentifierId != null && !personIdentityMap.containsKey(personIdentifierId)) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setId(personIdentifierId);

            personIdentifier.setRecordId(SqlUtils.getLong(rs, "tax_record_id"));
            personIdentifier.setStatus(SqlUtils.getInteger(rs, "tax_status"));
            personIdentifier.setVersion(rs.getDate("tax_version"));

            personIdentifier.setInp(rs.getString("inp"));
            personIdentifier.setAsnuId(SqlUtils.getLong(rs, "as_nu"));
            personIdentifier.setNaturalPerson(naturalPerson);
            personIdentityMap.put(personIdentifierId, personIdentifier);
            naturalPerson.getPersonIdentityList().add(personIdentifier);
        }
    }

    private void addPersonDocument(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
        Long primaryPersonId = naturalPerson.getPrimaryPersonId();
        Long refBookPersonId = naturalPerson.getId();
        Long docId = SqlUtils.getLong(rs, "ref_book_id_doc_id");
        Map<Long, PersonDocument> pesonDocumentMap = documentsMap.get(refBookPersonId);

        if (pesonDocumentMap == null) {
            pesonDocumentMap = new HashMap<Long, PersonDocument>();
            documentsMap.put(refBookPersonId, pesonDocumentMap);
        }

        if (docId != null && !pesonDocumentMap.containsKey(docId)) {
            Long docTypeId = SqlUtils.getLong(rs, "doc_id");
            DocType docType = getDocTypeById(docTypeId);
            PersonDocument personDocument = new PersonDocument();
            personDocument.setId(docId);

            personDocument.setRecordId(SqlUtils.getLong(rs, "doc_record_id"));
            personDocument.setStatus(SqlUtils.getInteger(rs, "doc_status"));
            personDocument.setVersion(rs.getDate("doc_version"));

            personDocument.setDocType(docType);
            personDocument.setDocumentNumber(rs.getString("doc_number"));
            personDocument.setIncRep(SqlUtils.getInteger(rs, "inc_rep"));
            personDocument.setNaturalPerson(naturalPerson);
            pesonDocumentMap.put(docId, personDocument);
            naturalPerson.getPersonDocumentList().add(personDocument);
        }
    }

    private NaturalPerson buildNaturalPerson(ResultSet rs, Long refBookPersonId, Long primaryPersonId) throws SQLException {
        NaturalPerson naturalPerson = refbookPersonTempMap.get(refBookPersonId);
        if (naturalPerson != null) {
            return naturalPerson;
        } else {

            NaturalPerson person = new NaturalPerson();

            //person
            person.setId(refBookPersonId);

            //TODO Разделить модель на два класса NaturalPerson для представления данных первичной формы и данных справочника
            //person.setPrimaryPersonId(primaryPersonId);

            person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
            person.setStatus(SqlUtils.getInteger(rs, "person_status"));
            person.setVersion(rs.getDate("person_version"));

            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setSex(SqlUtils.getInteger(rs, "sex"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerStatus(getTaxpayerStatusById(SqlUtils.getLong(rs, "taxpayer_state")));
            person.setCitizenship(getCountryById(SqlUtils.getLong(rs, "citizenship")));

            //additional
            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "medical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));
            person.setSourceId(SqlUtils.getLong(rs, "source_id"));
            person.setRecordId(SqlUtils.getLong(rs, "record_id"));

            refbookPersonTempMap.put(refBookPersonId, person);

            return person;
        }


    }

    private Address buildAddress(ResultSet rs) throws SQLException {
        Long addrId = SqlUtils.getLong(rs, "REF_BOOK_ADDRESS_ID");
        if (addrId != null) {
            Address address = new Address();

            address.setId(addrId);
            address.setRecordId(SqlUtils.getLong(rs, "addr_record_id"));
            address.setStatus(SqlUtils.getInteger(rs, "addr_status"));
            address.setVersion(rs.getDate("addr_version"));

            address.setAddressType(SqlUtils.getInteger(rs, "address_type"));
            address.setCountry(getCountryById(SqlUtils.getLong(rs, "country_id")));
            address.setRegionCode(rs.getString("region_code"));
            address.setPostalCode(rs.getString("postal_code"));
            address.setDistrict(rs.getString("district"));
            address.setCity(rs.getString("city"));
            address.setLocality(rs.getString("locality"));
            address.setStreet(rs.getString("street"));
            address.setHouse(rs.getString("house"));
            address.setBuild(rs.getString("build"));
            address.setAppartment(rs.getString("appartment"));
            address.setAddressIno(rs.getString("address"));
            return address;
        } else {
            return null;
        }
    }

    public Map<Long, Country> getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }

    public Map<Long, TaxpayerStatus> getTaxpayerStatusMap() {
        return taxpayerStatusMap;
    }

    public void setTaxpayerStatusMap(Map<Long, TaxpayerStatus> taxpayerStatusMap) {
        this.taxpayerStatusMap = taxpayerStatusMap;
    }

    public Map<Long, DocType> getDocTypeMap() {
        return docTypeMap;
    }

    public void setDocTypeMap(Map<Long, DocType> docTypeMap) {
        this.docTypeMap = docTypeMap;
    }

    private TaxpayerStatus getTaxpayerStatusById(Long taxpayerStatusId) {
        if (taxpayerStatusId != null) {
            return taxpayerStatusMap != null ? taxpayerStatusMap.get(taxpayerStatusId) : new TaxpayerStatus(taxpayerStatusId, null);
        } else {
            return null;
        }
    }

    private Country getCountryById(Long countryId) {
        if (countryId != null) {
            return countryMap != null ? countryMap.get(countryId) : new Country(countryId, null);
        } else {
            return null;
        }
    }

    private DocType getDocTypeById(Long docTypeId) {
        if (docTypeId != null) {
            return docTypeMap != null ? docTypeMap.get(docTypeId) : new DocType(docTypeId, null);
        } else {
            return null;
        }
    }


}