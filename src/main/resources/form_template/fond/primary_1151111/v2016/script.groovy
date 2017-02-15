package form_template.fond.primary_1151111.v2016

import javax.script.ScriptException
import java.awt.Color
import java.text.SimpleDateFormat

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
        break
    case FormDataEvent.CHECK:
        println "!CHECK!"
        checkData()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        println "!CREATE_SPECIFIC_REPORT!"
        def writer = scriptSpecificReportHolder.getFileOutputStream()
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        def workbook = getSpecialReportTemplate()
        fillGeneralList(workbook)
        if (alias.equalsIgnoreCase(PERSON_REPORT)) {
            fillPersSvSheet(workbook)
        } else if (alias.equalsIgnoreCase(CONSOLIDATED_REPORT)) {
            fillPersSvConsSheet(workbook)
            fillSumStrahVzn(workbook)
        }
        workbook.write(writer)
        writer.close()
        scriptSpecificReportHolder
                .setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
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
@Field final long REF_BOOK_DOCUMENT_ID = RefBook.Id.DOCUMENT_CODES.id

// Страны
@Field final long REF_BOOK_COUNTRY_ID = RefBook.Id.COUNTRY.id

// Физ. лица
@Field def personsCache = [:]
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

// Имена псевдонима спецотчета
@Field final String PERSON_REPORT = "person_rep_param"
@Field final String CONSOLIDATED_REPORT = "consolidated_report"

@Field final Color ROWS_FILL_COLOR = new Color(255, 243, 203)
@Field final Color TOTAL_ROW_FILL_COLOR = new Color(186, 208, 80)

@Field final String PERSONAL_DATA_TOTAL_ROW_LABEL = "Всего за последние три месяца расчетного (отчетного) периода"

// Персонифицированные данные о ФЛ
@Field def raschsvPersSvStrahLic = null

// Находит в базе данных RaschsvPersSvStrahLic
RaschsvPersSvStrahLic getrRaschsvPersSvStrahLic() {
    if (raschsvPersSvStrahLic == null) {
        def declarationId = declarationData.getId()
        def params = scriptSpecificReportHolder.getSubreportParamValues()
        raschsvPersSvStrahLic = raschsvPersSvStrahLicService.findPersonBySubreportParams(declarationId, params)
    }
    return raschsvPersSvStrahLic
}

// Находит в базе данных список List RaschsvPersSvStrahLic
List<RaschsvPersSvStrahLic> getrRaschsvPersSvStrahLicList() {
    [TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA]
}

// Находит в БД RaschsvObyazPlatSv
RaschsvObyazPlatSv getRaschsvObyazPlatSv() {
    // TODO нужен в RaschsvObyazPlatSvDao select по declarationDataId
    TestDataHolder.getInstance().RASCHSV_OOBYAZ_PLAT_SV
}

// Находит в БД RaschsvUplPer
List<RaschsvUplPer> getRaschsvUplPer() {
    // TODO нужен RaschsvUplPerDao select по obyazPlatSvId и nodeName, лучше бы сделать nodeName в виде enum
    [TestDataHolder.getInstance().RASCHSV_UPL_PER]
}

RaschsvUplPrevOss getRaschsvUplPrevOss() {
    // TODO Нужен select в RaschsvUplPrevOssDao по obyazPlatSvId
    TestDataHolder.getInstance().RASCHSV_UPL_PREV_OSS
}

/****************************************************************************
 *  Блок заполнения данными титульной страницы                              *
 *                                                                          *
 * **************************************************************************/

def fillGeneralList(final XSSFWorkbook workbook) {
    // TODO необходимо реализовать классы соответствующие данным и dao
    def sheet = workbook.getSheet(COMMON_SHEET)
    def nomCorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def period = getProvider(REPORT_PERIOD_TYPE_ID).getRecordData(reportPeriod.dictTaxPeriodId)?.CODE?.value
    def reportYear = reportPeriod.taxPeriod.year
    def refDepartmentParam = getRefDepartment(declarationData.departmentId)
    def refDepartmentParamRow = getRefDepartmentParamTable(refDepartmentParam?.id?.value)
    def taxOrganCode = refDepartmentParamRow?.TAX_ORGAN_CODE?.value
    def raschsvSvnpPodpisant = raschsvSvnpPodpisantService.findRaschsvSvnpPodpisant(declarationData.id)
    /*def xmlStream = declarationService.getXmlStream(declarationData.getId())
    def slurper = new XmlSlurper()
    def Файл = slurper.parse(xmlStream)
    sheet.getRow(3).getCell(1).setCellValue(Файл.@ИдФайл.toString())
    sheet.getRow(4).getCell(1).setCellValue(Файл.@ВерсПрог.toString())
    sheet.getRow(5).getCell(1).setCellValue(Файл.@ВерсФорм.toString())
    sheet.getRow(8).getCell(1).setCellValue(Файл.Документ.@КНД.toString())
    sheet.getRow(9).getCell(1).setCellValue(Файл.Документ.@ДатаДок.toString())
    sheet.getRow(10).getCell(1).setCellValue(Файл.Документ.@НомКорр.toString())
    sheet.getRow(11).getCell(1).setCellValue(Файл.Документ.@Период.toString())
    sheet.getRow(12).getCell(1).setCellValue(Файл.Документ.@ОтчетГод.toString())
    sheet.getRow(13).getCell(1).setCellValue(Файл.Документ.@КодНО.toString())
    sheet.getRow(14).getCell(1).setCellValue(Файл.Документ.@ПоМесту.toString())*/
    sheet.getRow(3).getCell(1).setCellValue(declarationData.fileName)
    sheet.getRow(4).getCell(1).setCellValue(applicationVersion)
    sheet.getRow(5).getCell(1).setCellValue("5.01")
    sheet.getRow(8).getCell(1).setCellValue("1151111")
    sheet.getRow(9).getCell(1).setCellValue(new Date().format("dd.MM.yyyy"))
    sheet.getRow(10).getCell(1).setCellValue(nomCorr)
    sheet.getRow(11).getCell(1).setCellValue(period)
    sheet.getRow(12).getCell(1).setCellValue(reportYear)
    sheet.getRow(13).getCell(1).setCellValue(taxOrganCode)
    // TODO: Сделать получение данных соответствующих Файл.Документ.@ПоМесту
    sheet.getRow(14).getCell(1).setCellValue("")
    sheet.getRow(18).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpOkved)
    sheet.getRow(19).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpTlph)
    sheet.getRow(21).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpNaimOrg)
    sheet.getRow(22).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpInnyl)
    sheet.getRow(23).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpKpp)
    sheet.getRow(25).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgForm)
    sheet.getRow(26).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgInnyl)
    sheet.getRow(27).getCell(1).setCellValue(raschsvSvnpPodpisant.svnpSvReorgKpp)
    sheet.getRow(30).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantPrPodp)
    sheet.getRow(31).getCell(1).setCellValue(raschsvSvnpPodpisant.familia + " " + raschsvSvnpPodpisant.imya + " " + raschsvSvnpPodpisant.otchestvo)
    sheet.getRow(33).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantNaimDoc)
    sheet.getRow(34).getCell(1).setCellValue(raschsvSvnpPodpisant.podpisantNaimOrg)
    // TODO заменить на выгрузку из БД
   /* sheet.getRow(18).getCell(1).setCellValue(Файл.Документ.СвНП.@ОКВЭД.toString())
    sheet.getRow(19).getCell(1).setCellValue(Файл.Документ.СвНП.@Тлф.toString())
    sheet.getRow(21).getCell(1).setCellValue(Файл.Документ.СвНП.НПЮЛ.@НаимОрг.toString())
    sheet.getRow(22).getCell(1).setCellValue(Файл.Документ.СвНП.НПЮЛ.@ИННЮЛ.toString())
    sheet.getRow(23).getCell(1).setCellValue(Файл.Документ.СвНП.НПЮЛ.@КПП.toString())
    sheet.getRow(25).getCell(1).setCellValue(Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.@ФормРеорг.toString())
    sheet.getRow(26).getCell(1).setCellValue(Файл.Документ.СвНП.НПЮЛ.СвРеоргЮЛ.@ИННЮЛ.toString())
    sheet.getRow(27).getCell(1).setCellValue(Файл.Документ.СвНП.AAНПЮЛ.СвРеоргЮЛ.@КПП.toString())
    sheet.getRow(30).getCell(1).setCellValue(Файл.Документ.Подписант.@ПрПодп.toString())
    sheet.getRow(31).getCell(1).setCellValue(Файл.Документ.Подписант.@Фамилия.toString() + " " + Файл.Документ.Подписант.@Имя.toString() + " " + Файл.Документ.Подписант.@Отчество.toString())
    sheet.getRow(33).getCell(1).setCellValue(Файл.Документ.Подписант.СвПред.@НаимДок.toString())
    sheet.getRow(34).getCell(1).setCellValue(Файл.Документ.Подписант.СвПред.@НаимОрг.toString())*/
}

/****************************************************************************
 *  Блок заполнения данными листа песонифицированных сведений ФЛ            *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист персонифицированных сведений
def fillPersSvSheet(final XSSFWorkbook workbook) {
    def startIndex = 3;
    def pointer = startIndex
    def raschsvPersSvStrahLic = getrRaschsvPersSvStrahLic()
    pointer += fillRaschsvPersSvStrahLicTable(pointer, [raschsvPersSvStrahLic], workbook, PERSONAL_DATA)
    pointer += 5
    pointer += fillRaschSvVyplat(pointer, raschsvPersSvStrahLic, workbook)
    pointer += 6
    pointer += fillRaschSvVyplatDop(pointer, raschsvPersSvStrahLic, workbook)
}

// Создает строку для таблицы Персонифицированные сведения о застрахованных лицах
int fillRaschsvPersSvStrahLicTable(final int startIndex, final List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList, final XSSFWorkbook workbook, final String sheetName) {
    def raschsvPersSvStrahLicListSize = raschsvPersSvStrahLicList.size()
    def sheet = workbook.getSheet(sheetName)
    if (!sheetName.equals(CONS_PERSONAL_DATA)) {
        sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvPersSvStrahLicListSize + 1)
    }
    for (int i = 0; i < raschsvPersSvStrahLicListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschsvPersSvStrahLicRow(raschsvPersSvStrahLicList[i], row)
    }
    return raschsvPersSvStrahLicListSize
}

/*
 * Заполняет данными строку из таблицы Персонифицированные сведения о
 * застрахованных лицах
 **/
def fillCellsOfRaschsvPersSvStrahLicRow(final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFRow row) {
    def leftStyle = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())
    def centerStyle = normalWithBorderStyleCenterAligned(row.getSheet().getWorkbook())
    def defaultStyle = normalWithBorderStyle(row.getSheet().getWorkbook())
    def cell0 = row.createCell(0)
    cell0.setCellStyle(leftStyle)
    cell0.setCellValue(raschsvPersSvStrahLic.getNomer())
    def cell1 = row.createCell(1)
    cell1.setCellStyle(leftStyle)
    cell1.setCellValue(formatDate(raschsvPersSvStrahLic.getSvData(), "dd.MM.yyyy"))
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
    cell10.setCellValue(raschsvPersSvStrahLic.getDataRozd())
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

//  Создает строки для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
int fillRaschSvVyplat(final int startIndex, final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFWorkbook workbook) {
    def raschsvSvVypl = raschsvPersSvStrahLic.raschsvSvVypl
    def raschsvSvVyplMkList = raschsvSvVypl?.raschsvSvVyplMkList
    def raschsvSvVyplMkListSize = raschsvSvVyplMkList?.size()
    def sheet = workbook.getSheet(PERSONAL_DATA)
    sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvSvVyplMkListSize + 1)
    for (int i = 0; i < raschsvSvVyplMkListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatMt(raschsvPersSvStrahLic, raschsvSvVyplMkList[i], row)
    }
    fillCellsOfRaschSvVyplat(raschsvSvVypl, sheet.createRow(raschsvSvVyplMkListSize + startIndex))
    return raschsvSvVyplMkListSize
}

// Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
def fillCellsOfRaschSvVyplatMt(final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final RaschsvSvVyplMk raschsvSvVyplMk, final XSSFRow row) {
    def styleCenter = normalWithBorderStyleCenterAligned(row.getSheet().getWorkbook())
    def styleLeft = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())

    addFillingToStyle(styleCenter, ROWS_FILL_COLOR)
    addFillingToStyle(styleLeft, ROWS_FILL_COLOR)

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
    cell3.setCellStyle(styleLeft)
    cell3.setCellValue(raschsvSvVyplMk.sumVypl)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleLeft)
    cell4.setCellValue(raschsvSvVyplMk.vyplOps)
    def cell5 = row.createCell(5)
    cell5.setCellStyle(styleLeft)
    cell5.setCellValue(raschsvSvVyplMk.vyplOpsDog)
    def cell6 = row.createCell(6)
    cell6.setCellStyle(styleLeft)
    cell6.setCellValue(raschsvSvVyplMk.nachislSv)
}

// Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
def fillCellsOfRaschSvVyplat(final RaschsvSvVypl raschsvSvVypl, final XSSFRow row) {
    def styleLeft = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())
    addFillingToStyle(styleLeft, TOTAL_ROW_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleLeft)
    cell0.setCellValue(PERSONAL_DATA_TOTAL_ROW_LABEL)
    row.getSheet().addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 2));
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleLeft)
    cell3.setCellValue(raschsvSvVypl.sumVyplVs3)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleLeft)
    cell4.setCellValue(raschsvSvVypl.vyplOpsVs3)
    def cell5 = row.createCell(5)
    cell5.setCellStyle(styleLeft)
    cell5.setCellValue(raschsvSvVypl.vyplOpsDogVs3)
    def cell6 = row.createCell(6)
    cell6.setCellStyle(styleLeft)
    cell6.setCellValue(raschsvSvVypl.nachislSvVs3)
}

// Заполняет таблицу "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
int fillRaschSvVyplatDop(final int startIndex, final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final XSSFWorkbook workbook) {
    def raschsvSvVyplDop = raschsvPersSvStrahLic.raschsvVyplSvDop
    def raschsvSvVyplDopMtList = raschsvSvVyplDop?.raschsvVyplSvDopMtList
    def raschsvSvVyplDopMtListSize = raschsvSvVyplDopMtList?.size()
    def sheet = workbook.getSheet(PERSONAL_DATA)
    for (int i = 0; i < raschsvSvVyplDopMtListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatDopMt(raschsvPersSvStrahLic, raschsvSvVyplDopMtList[i], row)
    }
    fillCellsOfRaschSvVyplatDop(raschsvSvVyplDop, sheet.createRow(raschsvSvVyplDopMtListSize + startIndex))
    return raschsvSvVyplDopMtListSize
}

// Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
def fillCellsOfRaschSvVyplatDopMt(final RaschsvPersSvStrahLic raschsvPersSvStrahLic, final RaschsvVyplSvDopMt raschsvSvVyplDopMt, final XSSFRow row){
    def styleCenter = normalWithBorderStyleCenterAligned(row.getSheet().getWorkbook())
    def styleLeft = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())
    addFillingToStyle(styleCenter, ROWS_FILL_COLOR)
    addFillingToStyle(styleLeft, ROWS_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleCenter)
    cell0.setCellValue(raschsvPersSvStrahLic.getNomer())
    def cell1 = row.createCell(1)
    cell1.setCellStyle(styleCenter)
    cell1.setCellValue(raschsvSvVyplDopMt.mesyac)
    def cell2 = row.createCell(2)
    cell2.setCellStyle(styleCenter)
    cell2.setCellValue(raschsvSvVyplDopMt.tarif)
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleLeft)
    cell3.setCellValue(raschsvSvVyplDopMt.vyplSv)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleLeft)
    cell4.setCellValue(raschsvSvVyplDopMt.nachislSv)
}

// Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
def fillCellsOfRaschSvVyplatDop (final RaschsvVyplSvDop raschsvVyplSvDop, final XSSFRow row) {
    def styleLeft = normalWithBorderStyleLeftAligned(row.getSheet().getWorkbook())
    addFillingToStyle(styleLeft, TOTAL_ROW_FILL_COLOR)
    def cell0 = row.createCell(0)
    cell0.setCellStyle(styleLeft)
    cell0.setCellValue(PERSONAL_DATA_TOTAL_ROW_LABEL)
    row.getSheet().addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 2));
    def cell3 = row.createCell(3)
    cell3.setCellStyle(styleLeft)
    cell3.setCellValue(raschsvVyplSvDop.vyplSvVs3)
    def cell4 = row.createCell(4)
    cell4.setCellStyle(styleLeft)
    cell4.setCellValue(raschsvVyplSvDop.nachislSvVs3)
}



/****************************************************************************
 *  Блок заполнения данными листа 3.Персониф. Сведения                      *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист 3.Персониф. Сведения
def fillPersSvConsSheet(final XSSFWorkbook workbook) {
    def startIndex = 3
    def pointer = startIndex
    def raschsvPersSvStrahLic = getrRaschsvPersSvStrahLicList()
    pointer += fillRaschsvPersSvStrahLicTable(pointer, raschsvPersSvStrahLic, workbook, CONS_PERSONAL_DATA)
}

/****************************************************************************
 *  Блок заполнения данными листа Суммы страховых взносов                   *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист "Суммы страховых взносов"
def fillSumStrahVzn(final XSSFWorkbook workbook) {
    def startIndex = 2
    def pointer = startIndex
    def raschsvObyazPlatSv = getRaschsvObyazPlatSv()
    def raschsvUplPerOps = getRaschsvUplPer()
    def raschsvUplPerOms = getRaschsvUplPer()
    def raschsvUplPerOpsDop = getRaschsvUplPer()
    def raschsvUplPerOssDop = getRaschsvUplPer()
    def raschsvUplPrevOss = getRaschsvUplPrevOss()
    fillRaschsvObyazPlatSv(pointer, raschsvObyazPlatSv, workbook)
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOps, workbook)
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOms, workbook)
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOpsDop, workbook)
    pointer += 4
    pointer += fillSumStrahVznOPSTable(pointer, raschsvUplPerOssDop, workbook)
    pointer += 5
    fillSumStrahVznNetrud(pointer, raschsvUplPrevOss, workbook)
}

// Заполняет ОКТМО
def fillRaschsvObyazPlatSv(final int startIndex, final RaschsvObyazPlatSv raschsvObyazPlatSv, final XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    def row = sheet.getRow(startIndex)
    row.createCell(1).setCellValue(raschsvObyazPlatSv.oktmo)
}

/* Создает строки таблиц "Сумма страховых взносов на обязательное пенсионное страхование, подлежащая уплате за
 * расчетный (отчетный) период", "Сумма страховых взносов на обязательное медицинское страхование, подлежащая уплате за
 * расчетный (отчетный) период", "Сумма страховых взносов на обязательное пенсионное страхование по дополнительному
 * тарифу, подлежащая уплате за расчетный (отчетный) период", "Сумма страховых взносов на дополнительное социальное
 * обеспечение, подлежащая уплате за расчетный (отчетный) период"
 **/
int fillSumStrahVznOPSTable(final int startIndex, final List<RaschsvUplPer> raschsvUplPerList, final XSSFWorkbook workbook) {
    def raschsvUplPerListSize = raschsvUplPerList.size()
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvUplPerListSize + 1)
    for (int i = 0; i < raschsvUplPerListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschsvUplPerRow(raschsvUplPerList[i], row)
    }
    return raschsvUplPerListSize
}

/* Заполняет строку таблиц "Сумма страховых взносов на обязательное пенсионное страхование, подлежащая уплате за
 * расчетный (отчетный) период", "Сумма страховых взносов на обязательное медицинское страхование, подлежащая уплате за
 * расчетный (отчетный) период", "Сумма страховых взносов на обязательное пенсионное страхование по дополнительному
 * тарифу, подлежащая уплате за расчетный (отчетный) период", "Сумма страховых взносов на дополнительное социальное
 * обеспечение, подлежащая уплате за расчетный (отчетный) период"
 **/
def fillCellsOfRaschsvUplPerRow(final RaschsvUplPer raschsvUplPer, final XSSFRow row) {
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

/* Создает строки таблицы "Сумма страховых взносов на обязательное социальное страхование на случай временной
 *  нетрудоспособности и в связи с материнством, подлежащая уплате за расчетный (отчетный) период / Сумма превышения
 *  произведенных плательщиком расходов"
 */
def fillSumStrahVznNetrud(final int startIndex, final RaschsvUplPrevOss raschsvUplPrevOss, final XSSFWorkbook workbook) {
    def sheet = workbook.getSheet(SUM_STRAH_VZN)
    def row = sheet.createRow(startIndex)
    fillCellsOfRaschsvUplPrevOss(raschsvUplPrevOss, row)
}

/* Заполняет стироку таблицы "Сумма страховых взносов на обязательное социальное страхование на случай временной
 *  нетрудоспособности и в связи с материнством, подлежащая уплате за расчетный (отчетный) период / Сумма превышения
 *  произведенных плательщиком расходов"
 */
def fillCellsOfRaschsvUplPrevOss(final RaschsvUplPrevOss raschsvUplPrevOss, final XSSFRow row) {
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


/****************************************************************************
 *  Блок стилизации                                                         *
 *                                                                          *
 *  Методы отвечающие за представление документа                            *
 *                                                                          *
 * **************************************************************************/

// Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем слева
def normalWithBorderStyleLeftAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_LEFT)
    thinBorderStyle(style)
    return style
}

// Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
def normalWithBorderStyleCenterAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_CENTER)
    thinBorderStyle(style)
    return style
}

// Создать стиль ячейки с нормальным шрифтом с тонкими границами
def normalWithBorderStyle(workbook) {
    def style = workbook.createCellStyle()
    thinBorderStyle(style)
    return style
}

// Добавляет к стилю ячейки тонкие границы
def thinBorderStyle(final style) {
    style.setBorderTop(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderRight(CellStyle.BORDER_THIN)
    return style
}

// Добавляет к стилю заливку
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

// Создает отформатироованную строку из объекта даты на основе передаваемого шаблона
def formatDate(final date, final pattern) {
    def formatter = new SimpleDateFormat(pattern)
    formatter.format(date)
}


/**
 * Получить Параметры подразделения по сборам, взносам
 * @return
 */
def getRefDepartment(def departmentId) {
    if (departmentParam == null) {
        println departmentId
        def departmentParamList = getProvider(950).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
def getRefDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        println departmentParamId
        println declarationData.kpp
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(951).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
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
        final VYPL_MT1 = new RaschsvSvVyplMk();
        VYPL_MT1.setMesyac("Январь")
        VYPL_MT1.setKodKatLic("1")
        VYPL_MT1.setSumVypl(300)
        VYPL_MT1.setVyplOps(100)
        VYPL_MT1.setVyplOpsDog(100)
        VYPL_MT1.setNachislSv(150)
        final VYPL_MT2 = new RaschsvSvVyplMk();
        VYPL_MT2.setMesyac("Февраль")
        VYPL_MT2.setKodKatLic("1")
        VYPL_MT2.setSumVypl(300)
        VYPL_MT2.setVyplOps(100)
        VYPL_MT2.setVyplOpsDog(100)
        VYPL_MT2.setNachislSv(100)
        final VYPL_MT3 = new RaschsvSvVyplMk();
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
        final VYPL_DOP_MT1 = new RaschsvVyplSvDopMt()
        VYPL_DOP_MT1.mesyac = "Январь"
        VYPL_DOP_MT1.tarif = "abc"
        VYPL_DOP_MT1.nachislSv = 200
        VYPL_DOP_MT1.vyplSv = 200
        final VYPL_DOP_MT2 = new RaschsvVyplSvDopMt()
        VYPL_DOP_MT2.mesyac = "Февраль"
        VYPL_DOP_MT2.tarif = "xyz"
        VYPL_DOP_MT2.nachislSv = 100
        VYPL_DOP_MT2.vyplSv = 100
        final VYPL_DOP_MT3 = new RaschsvVyplSvDopMt()
        VYPL_DOP_MT3.mesyac = "Март"
        VYPL_DOP_MT3.tarif = "abc"
        VYPL_DOP_MT3.nachislSv = 200
        VYPL_DOP_MT3.vyplSv = 200
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
@Field final MAX_COUNT_PERV_SV_STRAH_LIC = 1000
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

// Шаблоны имен файлов
@Field final String NO_RASCHSV_PATTERN = "NO_RASCHSV_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String KV_OTCH_PATTERN = "KV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UO_OTCH_PATTERN = "UO_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String IV_OTCH_PATTERN = "IV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UU_OTCH_PATTERN = "UU_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";

/**
 * Разбор xml-файлов
 */
void importData() {

    Pattern patternNoRaschsv = Pattern.compile(NO_RASCHSV_PATTERN);
    Pattern patternKvOtch = Pattern.compile(KV_OTCH_PATTERN);
    Pattern patternUoOtch = Pattern.compile(UO_OTCH_PATTERN);
    Pattern patternIvOtch = Pattern.compile(IV_OTCH_PATTERN);
    Pattern patternUuOtch = Pattern.compile(UU_OTCH_PATTERN);

    if (patternNoRaschsv.matcher(UploadFileName).matches()) {
        importPrimaryData()
    } else if (patternKvOtch.matcher(UploadFileName).matches() ||
            patternUoOtch.matcher(UploadFileName).matches() ||
            patternIvOtch.matcher(UploadFileName).matches() ||
            patternUuOtch.matcher(UploadFileName).matches()) {
        importAnswerData()
    }
}

/**
 * Разбор xml-файлов ответов ФНС
 */
void importAnswerData() {
    // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-338
    // ожидаю https://jira.aplana.com/browse/SBRFNDFL-381
    // Установка состояния ЭД для декларации на основании веса документа и даты создания документа
}

/**
 * Разбор xml-файла ТФ 1151111 (первичный) с сохранением в БД
 */
void importPrimaryData() {

    // Валидация по схеме
    declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile)

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def fileNode = new XmlSlurper().parse(ImportInputStream);
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    // Проверки при загрузке
//    checkImportRaschsv(fileNode)
//    if (logger.containsLevel(LogLevel.ERROR)) {
//        return
//    }

    // Набор объектов ПерсСвСтрахЛиц
    def raschsvPersSvStrahLicList = []

    // Идентификатор декларации для которой загружаются данные
    declarationDataId = declarationData.getId()

    // Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ
    RaschsvSvnpPodpisant raschsvSvnpPodpisant = new RaschsvSvnpPodpisant()
    raschsvSvnpPodpisant.declarationDataId = declarationDataId

    //Два списка для создания новых записей и для обновления существующих
    List<RaschsvPersSvStrahLic> createdPerson = new ArrayList<RaschsvPersSvStrahLic>();
    List<RaschsvPersSvStrahLic> updatedPerson = new ArrayList<RaschsvPersSvStrahLic>();
    def updCnt = 0, createCnt = 0;

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
                                raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
                                raschsvPersSvStrahLicList = []
                            }
                            RaschsvPersSvStrahLic raschsvPersSvStrahLic = parseRaschsvPersSvStrahLic(raschetSvChildNode, declarationDataId)

                            // Идентификация физического лица
                            PersonData personData = createPersonData(raschsvPersSvStrahLic);
                            Long refBookPersonId = refBookPersonService.identificatePerson(personData, SIMILARITY_THRESHOLD);
                            raschsvPersSvStrahLic.personId = refBookPersonId
                            if (refBookPersonId != null) {
                                //обновление записи
                                updatedPerson.add(raschsvPersSvStrahLic);
                                updCnt++;
                            } else {
                                //Новые записи помещаем в список для пакетного создания
                                createdPerson.add(raschsvPersSvStrahLic);
                                createCnt++;
                            }

                            raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic)
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
    if (raschsvPersSvStrahLicList.size() > 0) {
        raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
    }

    // Сохранение Сведений о плательщике страховых взносов и Сведения о лице, подписавшем документ
    raschsvSvnpPodpisantService.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant)

    // Расчет сводных показателей
    raschSvItog(fileNode, declarationDataId)

    // Добавление записей в справочник Физические лица
    if (createdPerson != null && !createdPerson.isEmpty()) {
//        createRefbookPersonData(createdPerson);
    }
    // Добавление записей в справочнике Физические лица
    if (updatedPerson != null && !updatedPerson.isEmpty()) {
//        updateRefbookPersonData(updatedPerson);
    }

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
@Field final CHECK_PAYMENT_OKVED_NOT_FOUND = "Файл.Документ.СвНП.ОКВЭД = \"%s\" не найден в справочнике ОКВЭД"
@Field final CHECK_PAYMENT_INN = "Некорректный Файл.Документ.СвНП.НПЮП.ИННЮЛ = \"%s\" для организации - плательщика страховых взносов в транспортном файле \"%s\""
@Field final CHECK_PAYMENT_REORG_INN = "Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ реорганизованной организации для организации плательщика страховых взносов с ИНН %s"
@Field final CHECK_PAYMENT_REORG_KPP = "Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП реорганизованной организации для организации плательщика страховых взносов с ИНН %s"
@Field final CHECK_PAYMENT_REORG_INN_VALUE = "Некорректный Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ = \"%s\" реорганизованной организации для организации плательщика страховых взносов с ИНН %s"
@Field final CHECK_PAYMENT_IP_INN_VALUE = "Некорректный Файл.Документ.СвНП.НПИП.ИННФЛ = \"%s\" индивидуального предпринимателя - плательщика страховых взносов в транспортном файле \"%s\""
@Field final CHECK_PAYMENT_IP_COUNTRY = "Файл.Документ.СвНП.НПИП.СвНПФЛ.Гражд = \"%s\" ФЛ с ИНН \"%s\" не найден в справочнике ОКСМ"
@Field final CHECK_PAYMENT_IP_DOC = "Файл.Документ.СвНП.НПФЛ.СвНПФЛ.УдЛичнФЛ.КодВидДок = \"%s\" ФЛ с ИНН %s не найден в справочнике \"Коды документов, удостоверяющих личность\""
@Field final CHECK_PAYMENT_FL_INN_VALUE = "Некорректный Файл.Документ.СвНП.НПФЛ.ИННФЛ = \"%s\" физического лица - плательщика страховых взносов в транспортном файле \"%s\""
@Field final CHECK_PAYMENT_FL_ADDR = "В справочнике отсутствует Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'%s'/'%s'/'%s'/'%s'/'%s'\" для ФЛ с ИНН \"%s\""
@Field final CHECK_PODPISANT_EMPTY_FIO = "Не заполнено Файл.Документ.Подписант.ФИО=\"'%s'/'%s'\" в транспортном файле \"%s\""
@Field final CHECK_PODPISANT_EMPTY_DOC = "Не заполнено Файл.Документ.Подписант.СвПред.НаимДок = \"%s\" в транспортном файле \"%s\""
@Field final CHECK_CALCULATION_OBZ = "Не заполнены Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \"%s\""
@Field final CHECK_CALCULATION_OBZ_OKTMO = "Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"%s\" транспортного файла \"%s\" не найден в справочнике"
@Field final CHECK_CALCULATION_KBK = "%s = \"%s\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""
@Field final CHECK_CALCULATION_SUMM = "Не заполнено %s в транспортном файле \"%s\""
@Field final CHECK_TARIFF_INN = "Некорректный Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.ИННФЛ = \"%s\" иностранного гражданина, необходимый для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 в транспортном файле \"%s\""
@Field final CHECK_TARIFF_SNILS = "Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.СНИЛС = \"%s\" иностранного гражданина с ИНН \"%s\" не найден в справочнике ОКСМ"
@Field final CHECK_TARIFF_COUNTRY = "Некорректный Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.Гражд = \"%s\" иностранного гражданина, необходимый для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 в транспортном файле \"%s\""
@Field final CHECK_PERSON_INN = "Некорректный Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ = \"%s\" для получателя доходов в транспортном файле \"%s\""
@Field final CHECK_PERSON_SNILS = "Некорректный Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС = \"%s\" для получателя доходов"
@Field final CHECK_PERSON_DOCTYPE = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок = \"%s\" получателя доходов с СНИЛС \"%s\" не найден в справочнике \"Коды документов, удостоверяющих личность\""
@Field final CHECK_PERSON_OKSM = "Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд = \"%s\" получателя доходов с СНИЛС  \"%s\" не найден в справочнике ОКСМ"
@Field final CHECK_PERSON_PERIOD = "Период расчетных сведений Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц = \"'%s'/'%s'\" в транспортном файле \"%s\" не входит в отчетный период формы"
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
void checkImportRaschsv(fileNode) {
    checkRaschsvFileName(fileNode)
    checkPayment(fileNode)
    checkPodpisant(fileNode)
    checkPayer(fileNode)
    checkTariff_2_2_425(fileNode)
    checkFL(fileNode)
}

/**
 * 1.1 Проверки соответствия данных имени файла и содержимого файла
 *
 * @param fileNode корневой узел XML
 */
void checkRaschsvFileName(fileNode) {
    def fileNameNo = null
    def fileNameInn = null
    def fileNameKpp = null

    UploadFileName.find(/NO_RASCHSV_([^_]+)_([^_]+)_(\d{10})(\d{9})_(\d{8})_(.+)\.xml/) { fullMatch, tn, no, inn, kpp, date, guid ->
        fileNameNo = no
        fileNameInn = inn
        fileNameKpp = kpp
    }

    def documentNo = fileNode?."$NODE_NAME_DOCUMENT"?."@КодНО" as String
    def documentInn = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"?."$NODE_NAME_NPYL"?."@ИННЮЛ" as String
    def documentKpp = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"?."$NODE_NAME_NPYL"?."@КПП" as String

    // 1.1.1 Соответствие кода НО в файле и в имени
    if (!documentNo || documentNo != fileNameNo) {
        logger.error(CHECK_FILE_NAME, UploadFileName, CHECK_FILE_NAME_NO)
    }

    // 1.1.2 Соответствие ИНН в файле и в имени
    if (!documentInn || documentInn != fileNameInn) {
        logger.error(CHECK_FILE_NAME, UploadFileName, CHECK_FILE_NAME_INN)
    }

    // 1.1.3 Соответствие КПП в файле и в имени
    if (!documentKpp || documentKpp != fileNameKpp) {
        logger.error(CHECK_FILE_NAME, UploadFileName, CHECK_FILE_NAME_KPP)
    }
}

/**
 * 1.2 Проверки по плательщику страховых взносов
 *
 * @param fileNode корневой узел XML
 */
def checkPayment(fileNode) {
    checkPaymentJL(fileNode)
    checkPaymentIP(fileNode)
    checkPaymentFL(fileNode)
}

/**
 * Проверки 1.2 для НПЮЛ
 */
def checkPaymentJL(fileNode) {
    def documentSvNP = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"
    def documentOkved = documentSvNP?."@ОКВЭД" as String

    documentSvNP?."$NODE_NAME_NPYL".each { npul ->
        def documentInn = npul?."@ИННЮЛ" as String

        // 1.2.1 Поиск ОКВЭД в справочнике
        if (documentOkved && !isExistsOkved(documentOkved)) {
            logger.warn(CHECK_PAYMENT_OKVED_NOT_FOUND, documentOkved)
        }

        // 1.2.2 Корректность ИНН ЮЛ
        if (INN_JUR_LENGTH != documentInn.length() || !ScriptUtils.checkControlSumInn(documentInn)) {
            logger.warn(CHECK_PAYMENT_INN, documentInn, UploadFileName)
        }

        npul?."$NODE_NAME_SV_REORG_YL".each { reorg ->
            def documentReorgForm = reorg?."@ФормРеорг" as String
            def documentReorgInn = reorg?."@ИННЮЛ" as String
            def documentReorgKpp = reorg?."@КПП" as String

            // 1.2.4, 1.2.5
            if (['1', '2', '3', '4', '5', '6', '7'].contains(documentReorgForm)) {
                // 1.2.4 Наличие ИНН реорганизованной организации
                if (!documentReorgInn) {
                    logger.warn(CHECK_PAYMENT_REORG_INN, documentInn)
                }
                // 1.2.5 Наличие КПП реорганизованной организации
                if (!documentReorgKpp) {
                    logger.warn(CHECK_PAYMENT_REORG_KPP, documentInn)
                }
            }

            // 1.2.6 Корректность ИНН реорганизованной организации
            if (INN_JUR_LENGTH != documentReorgInn.length() || !ScriptUtils.checkControlSumInn(documentReorgInn)) {
                logger.warn(CHECK_PAYMENT_REORG_INN_VALUE, documentReorgInn, documentInn)
            }
        }
    }
}

/**
 * Проверки 1.2 для НПИП
 */
def checkPaymentIP(fileNode) {
    def documentSvNP = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_SV_NP"

    documentSvNP?."$NODE_NAME_NPIP".each { ip ->
        def documentIpInn = ip?."@ИННФЛ" as String

        // 1.2.8 Корректность ИНН плательщика страховых взносов (ИП)
        if (documentIpInn && (INN_IP_LENGTH != documentIpInn.length() || !ScriptUtils.checkControlSumInn(documentIpInn))) {
            logger.warn(CHECK_PAYMENT_IP_INN_VALUE, documentIpInn, UploadFileName)
        }
    }
}

/**
 * Проверки 1.2 для НПФЛ
 */
def checkPaymentFL(fileNode) {
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
            logger.warn(CHECK_PAYMENT_FL_INN_VALUE, documentFlInn, UploadFileName)
        }

        // 1.2.10 Соответствие адреса ФЛ (плательщика страховых взносов) ФИАС
        if (!isExistsAddress(documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet)) {
            logger.warn(CHECK_PAYMENT_FL_ADDR,
                    documentFlAddrRegion, documentFlAddrArea, documentFlAddrCity, documentFlAddrLocality, documentFlAddrStreet,
                    documentFlInn
            )
        }

        // 1.2.11 Поиск кода гражданства в справочнике
        if (documentFlCountry && !isExistsOKSM(documentFlCountry)) {
            logger.warn(CHECK_PAYMENT_IP_COUNTRY, documentFlCountry, documentFlInn)
        }

        // 1.2.12 Поиск кода вида документа
        if (documentFlDocCode && !isExistsDocType(documentFlDocCode)) {
            logger.warn(CHECK_PAYMENT_IP_DOC, documentFlDocCode, documentFlInn)
        }
    }
}

/**
 * Проверки 1.3
 */
def checkPodpisant(fileNode) {
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
                logger.warn(CHECK_PODPISANT_EMPTY_FIO, secondName, firstName, UploadFileName)
            }

            // 1.3.2 Наличие сведений о представителе плательщика страховых взносов
            if (PODP_2 == prPodp && !docName) {
                logger.warn(CHECK_PODPISANT_EMPTY_DOC, docName, UploadFileName)
            }
        }
    }
}

/**
 * Проверки 1.4
 */
def checkPayer(fileNode) {
    def documentPlaceCode = fileNode?."$NODE_NAME_DOCUMENT"?."@ПоМесту" as String
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"

    raschets.each { raschet ->
        def payments = raschet?."$NODE_NAME_OBYAZ_PLAT_SV"

        // 1.4.1 Наличие сводных данных об обязательствах плательщика страховых взносов
        if ('124' != documentPlaceCode && payments.isEmpty()) {
            logger.error(CHECK_CALCULATION_OBZ, UploadFileName)
        }

        payments.each { payment ->
            def oktmoCode = payment?."@ОКТМО" as String

            // 1.4.2 Поиск кода ОКТМО
            if (oktmoCode && !isExistsOKTMO(oktmoCode)) {
                logger.error(CHECK_CALCULATION_OBZ_OKTMO, oktmoCode, UploadFileName)
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПС
            payment?."$NODE_NAME_UPL_PER_OPS".each { ops ->
                def kbkCode = ops?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.КБК", kbkCode)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОМС
            payment?."$NODE_NAME_UPL_PER_OMS".each { oms ->
                def kbkCode = oms?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС.КБК", kbkCode)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерОПСДоп
            payment?."$NODE_NAME_UPL_PER_OPS_DOP".each { dop ->
                def kbkCode = dop?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп.КБК", kbkCode)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПерДСО
            payment?."$NODE_NAME_UPL_PER_DSO".each { dso ->
                def kbkCode = dso?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО.КБК", kbkCode)
                }
            }

            // 1.4.3 Поиск кода бюджетной классификации: УплПревОСС
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def kbkCode = uplPrevOss?."@КБК" as String
                if (kbkCode && !isExistsKBK(kbkCode)) {
                    logger.error(CHECK_CALCULATION_KBK, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.КБК", kbkCode)
                }
            }

            // 1.4.4 - 1.4.11
            payment?."$NODE_NAME_UPL_PREV_OSS".each { uplPrevOss ->
                def prevPer = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВПер" as String
                def uplPer = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУплПер" as String
                if (prevPer?.isEmpty() && uplPer?.isEmpty()) {
                    // 1.4.4 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер", UploadFileName)
                    // 1.4.8 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер", UploadFileName)
                }

                def prev1M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ1М" as String
                def upl1M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл1М" as String
                if (prev1M?.isEmpty() && upl1M?.isEmpty()) {
                    // 1.4.5 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М", UploadFileName)
                    // 1.4.9 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М", UploadFileName)
                }

                def prev2M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ2М" as String
                def upl2M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл2М" as String
                if (prev2M?.isEmpty() && upl2M?.isEmpty()) {
                    // 1.4.6 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М", UploadFileName)
                    // 1.4.10 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М", UploadFileName)
                }

                def prev3M = uplPrevOss?."$NODE_NAME_PREV_RASH_OSS"?."@ПревРасхСВ3М" as String
                def upl3M = uplPrevOss?."$NODE_NAME_UPL_PER_OSS"?."@СумСВУпл3М" as String
                if (prev3M?.isEmpty() && upl3M?.isEmpty()) {
                    // 1.4.7 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М", UploadFileName)
                    // 1.4.11 Наличие суммы страховых взносов
                    logger.error(CHECK_CALCULATION_SUMM, "Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М", UploadFileName)
                }
            }
        }
    }
}

/**
 * Проверки 1.5
 */
def checkTariff_2_2_425(fileNode) {
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"

    raschets.each { raschet ->
        raschet?."$NODE_NAME_OBYAZ_PLAT_SV".each { payment ->
            payment?."$NODE_NAME_SV_PRIM_TARIF2_2_425".each { tariff ->
                tariff?."$NODE_NAME_SV_INO_GRAZD".each { inGra ->
                    def innFl = inGra?."@ИННФЛ" as String
                    def snils = inGra?."@СНИЛС" as String
                    def countryCode = inGra?."@Гражд" as String

                    // 1.5.1 Корректность ИНН иностранного гражданина и лица без гражданства
                    if (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl)) {
                        logger.error(CHECK_TARIFF_INN, innFl, UploadFileName)
                    }

                    // 1.5.2 Корректность СНИЛС иностранного гражданина и лица без гражданства
                    if (snils && !ScriptUtils.checkSnils(snils)) {
                        logger.error(CHECK_TARIFF_SNILS, snils, innFl)
                    }

                    // 1.5.3 Поиск кода гражданства иностранного гражданина и лица без гражданства в справочнике
                    if (countryCode && !isExistsOKSM(countryCode)) {
                        logger.error(CHECK_TARIFF_COUNTRY, countryCode, UploadFileName)
                    }
                }
            }
        }
    }
}

/**
 * Проверки 1.6
 */
def checkFL(fileNode) {
    def raschets = fileNode?."$NODE_NAME_DOCUMENT"?."$NODE_NAME_RASCHET_SV"
    def docPeriod = fileNode?."$NODE_NAME_DOCUMENT"?."@Период" as String
    def docYear = fileNode?."$NODE_NAME_DOCUMENT"?."@ОтчетГод" as String

    raschets.each { raschet ->
        raschet?."$NODE_NAME_PERS_SV_STRAH_LIC".each { person ->
            def personPeriod = person?."@Период" as String
            def personYear = person?."@ОтчетГод" as String

            // 1.6.5 Принадлежность дат сведений по ФЛ к отчетному периоду
            if (!(docPeriod == personPeriod && docYear == personYear)) {
                logger.error(CHECK_PERSON_PERIOD, personPeriod, personYear, UploadFileName)
            }

            person?."$NODE_NAME_DAN_FL_POLUCH".each { data ->
                def snils = data?."@СНИЛС" as String
                def innFl = data?."@ИННФЛ" as String
                def docTypeCode = data?."@КодВидДок" as String
                def national = data?."@Гражд" as String
                def serNumDoc = data?."@СерНомДок" as String

                // 1.6.1 Корректность ИНН ФЛ - получателя дохода
                if (INN_IP_LENGTH != innFl?.length() || !ScriptUtils.checkControlSumInn(innFl)) {
                    logger.error(CHECK_PERSON_INN, innFl, UploadFileName)
                }

                // 1.6.2 Корректность СНИЛС ФЛ - получателя дохода
                if (snils && !ScriptUtils.checkSnils(snils)) {
                    logger.error(CHECK_PERSON_SNILS, snils)
                }

                // 1.6.3 Поиск кода вида документа ФЛ - получателя дохода
                if (docTypeCode && !isExistsDocType(docTypeCode)) {
                    logger.error(CHECK_PERSON_DOCTYPE, docTypeCode, snils, )
                }

                // 1.6.4 Поиск кода гражданства ФЛ - получателя дохода в справочнике
                if (national && !isExistsOKSM(national)) {
                    logger.error(CHECK_PERSON_OKSM, national, snils)
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
        def sumVypl = (vypl."@СумВыпл" as String ?:"0").toBigDecimal()
        def sumVyplOps = (vypl."@ВыплОПС" as String ?:"0").toBigDecimal()
        def sumVyplOpsDog = (vypl."@ВыплОПСДог" as String ?:"0").toBigDecimal()
        def sumNachisl = (vypl."@НачислСВ" as String ?:"0").toBigDecimal()

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
        def vyplSv = (vypl."@ВыплСВ" as String ?:"0").toBigDecimal()
        def nachislSv = (vypl."@НачислСВ" as String ?:"0").toBigDecimal()

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

            if(raschsvUplPerList.size() >= MAX_COUNT_UPL_PER) {
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

/**
 * Создание объекта по которуму будет идентифицироваться физлицо в справочнике
 * @param person
 * @return
 */
PersonData createPersonData(RaschsvPersSvStrahLic raschsvPersSvStrahLic) {
    PersonData personData = new PersonData();

    personData.lastName = raschsvPersSvStrahLic.familia;
    personData.firstName = raschsvPersSvStrahLic.imya;
    personData.middleName = raschsvPersSvStrahLic.otchestvo;

    personData.inn = raschsvPersSvStrahLic.innfl;
    personData.snils = raschsvPersSvStrahLic.snils;
    personData.birthDate = raschsvPersSvStrahLic.dataRozd;

    personData.citizenshipId = findCountryId(raschsvPersSvStrahLic.grazd);
    personData.sex = raschsvPersSvStrahLic.pol;

    //Документы
    personData.documentTypeId = findDocumentTypeByCode(raschsvPersSvStrahLic.kodVidDoc);
    personData.documentNumber = raschsvPersSvStrahLic.serNomDoc;

    return personData;
}

/**
 * Добавление записей в справочник "Физические лица"
 * @param personList
 * @return
 */
def createRefbookPersonData(List<RaschsvPersSvStrahLic> personList) {

    logger.info("Подготовка к созданию данных по физ.лицам: " + personList.size() + " записей");

    //создание записей справочника физлиц
    List<RefBookRecord> personRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        RaschsvPersSvStrahLic person = personList.get(i)
        RefBookRecord refBookRecord = createPersonRecord(person);
        personRecords.add(refBookRecord);
    }

    //сгенерированные идентификаторы справочника физлиц
    List<Long> personIds = getProvider(RefBook.Id.PERSON.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, personRecords)
    logger.info("В справочнике 'Физические лица' создано записей: " + personIds.size());

    //создание записей справочников документы и идентфикаторы физлиц
    List<RefBookRecord> documentsRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        RaschsvPersSvStrahLic person = personList.get(i)
        person.setPersonId(personIds.get(i)); // выставляем присвоенный Id
        documentsRecords.add(createIdentityDocRecord(person));
    }

    List<Long> docIds = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, documentsRecords)
    logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size());
}

/**
 * Обновление записей в справочнике "Физические лица"
 * @param personList
 * @return
 */
def updateRefbookPersonData(List<RaschsvPersSvStrahLic> personList) {

    logger.info("Подготовка к обновлению данных по физлицам: " + personList.size());

    Date versionFrom = getVersionFrom();

    List<Long> personIds = getPersonIds(personList);

    //-----<INITIALIZE_CACHE_DATA>-----
    //PersonId : Физлица
    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefPersons(personIds);
    //PersonId :  UniqId:Документы
    Map<Long, List<Map<String, RefBookValue>>> identityDocMap = getRefDul(personIds)
    //-----<INITIALIZE_CACHE_DATA_END>-----

    for (RaschsvPersSvStrahLic person : personList) {
        def personId = person.getPersonId();
        Map<String, RefBookValue> refBookPersonValues = refBookPerson.get(personId);

        updatePersonRecord(refBookPersonValues, person);

        getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, personId, versionFrom, null, refBookPersonValues);

        //Обновление списка документов
        if (person.serNomDoc != null && !person.serNomDoc.isEmpty() && person.kodVidDoc != null && !person.kodVidDoc.isEmpty()) {
            updateIdentityDocRecords(identityDocMap.get(personId), person);
        }
    }
    logger.info("В справочнике 'Физические лица' обновлено записей: " + personList.size());
}

/**
 * Создание новой записи справочника физлиц
 * @param person класс предоставляющий данные для заполнения справочника
 * @param asnuId идентификатор АСНУ в справочнике АСНУ
 * @return запись справочника
 */
RefBookRecord createPersonRecord(RaschsvPersSvStrahLic person) {
    RefBookRecord refBookRecord = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillRaschsvPersSvStrahLicAttr(values, person);
    refBookRecord.setValues(values);
    return refBookRecord;
}

/**
 * Обновление атрибутов записи справочника физлиц
 * @param values
 * @param person
 * @return
 */
def updatePersonRecord(Map<String, RefBookValue> values, RaschsvPersSvStrahLic person) {
    fillRaschsvPersSvStrahLicAttr(values, person);
}

/**
 * Заполнение аттрибутов справочника физлиц
 * @param values карта для хранения значений атрибутов
 * @param person класс предоставляющий данные для заполнения справочника
 * @return
 */
def fillRaschsvPersSvStrahLicAttr(Map<String, RefBookValue> values, RaschsvPersSvStrahLic person) {

    Long countryId = findCountryId(person.grazd);

    putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.familia);
    putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.imya);
    putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.otchestvo);
    putOrUpdate(values, "SEX", RefBookAttributeType.STRING, person.pol?.toInteger());
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.innfl);
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, null);
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.snils);
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
    putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.dataRozd);
    putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, null);
    putOrUpdate(values, "PENSION", RefBookAttributeType.NUMBER, person.prizOps?.toInteger());
    putOrUpdate(values, "MEDICAL", RefBookAttributeType.NUMBER, person.prizOms?.toInteger());
    putOrUpdate(values, "SOCIAL", RefBookAttributeType.NUMBER, person.prizOss?.toInteger());
    putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, 2);
    putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, countryId);
    putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, null);
    putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, null);
    putOrUpdate(values, "DUBLICATES", RefBookAttributeType.REFERENCE, null);
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 */
def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value) {
    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи
        if (value != null) {
            refBookValue.setValue(value);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
    }
}

/**
 * Обновление документов
 * @param identityDocRefBook
 * @param person
 * @return
 */
def updateIdentityDocRecords(List<Map<String, RefBookValue>> identityDocRefBook, RaschsvPersSvStrahLic person) {
    Map<Long, String> docCodes = getRefDocument()

    //Id типа документа - приоритет,
    Map<Long, Integer> docPriorities = getRefDocumentPriority();

    //Идентификатор типа документа
    Long docTypeId = docCodes.find { it.value == person.kodVidDoc }?.key;

    if (docTypeId != null) {

        //Ищем документ с таким же типом
        Map<String, RefBookValue> findedDoc = identityDocRefBook?.find {
            docTypeId.equals(it.get("DOC_ID")) && person.serNomDoc?.equalsIgnoreCase(it.get("DOC_NUMBER"));
        };

        List<Map<String, RefBookValue>> identityDocRecords = new ArrayList<Map<String, RefBookValue>>();

        if (findedDoc != null) {
            //документ с таким типом и номером существует, ничего не делаем
            return;
        } else {
            RefBookRecord refBookRecord = createIdentityDocRecord(person);
            List<Long> ids = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, getVersionFrom(), null, Arrays.asList(refBookRecord));
            Map<String, RefBookValue> values = refBookRecord.getValues();
            values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, ids.first()));
            identityDocRecords.add(values);
        }

        if (identityDocRefBook != null && !identityDocRefBook.isEmpty()) {
            identityDocRecords.addAll(identityDocRecords);
        }

        //Если документов несколько - выбираем по приоритету какой использовать в отчетах
        if (identityDocRecords.size() > 1) {
            //сбрасываем текушие
            identityDocRecords.each {
                it.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "0"));
            }
            Map<String, RefBookValue> minimalPrior = identityDocRecords.min {
                docPriorities.get(it.get("DOC_ID"));
            }

            minimalPrior?.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "1"));
        }

        //Обновление признака включается в отчетность
        for (Map<String, RefBookValue> identityDocsValues : identityDocRecords) {
            Long uniqueId = identityDocsValues.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
            getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, versionFrom, null, identityDocsValues);
        }
    }
}

/**
 * По цифровому коду страны найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findCountryId(countryCode) {
    def citizenshipCodeMap = getRefCitizenship();
    return countryCode != null && !countryCode.isEmpty() ? citizenshipCodeMap.find {
        it.value == countryCode
    }?.key : null;
}

/**
 * Документы, удостоверяющие личность
 */
RefBookRecord createIdentityDocRecord(RaschsvPersSvStrahLic person) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillIdentityDocAttr(values, person);
    record.setValues(values);
    return record;
}

/**
 * Заполнение аттрибутов справочника документов
 * @param values карта для хранения значений атрибутов
 * @param person класс предоставляющий данные для заполнения справочника
 * @return
 */
def fillIdentityDocAttr(Map<String, RefBookValue> values, RaschsvPersSvStrahLic person) {

    values.put("PERSON_ID", new RefBookValue(RefBookAttributeType.REFERENCE, person.personId));
    values.put("DOC_NUMBER", new RefBookValue(RefBookAttributeType.STRING, person.serNomDoc));
    values.put("ISSUED_BY", new RefBookValue(RefBookAttributeType.STRING, null));
    values.put("ISSUED_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
    //Признак включения в отчет, при создании ставиться 1, при обновлении надо выбрать с минимальным приоритетом
    values.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "1"));
    values.put("DOC_ID", new RefBookValue(RefBookAttributeType.REFERENCE, findDocumentTypeByCode(person.kodVidDoc)));
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

// Коды ОКТМО
@Field def oktmoCodeCache = [:]
@Field def oktmoCodeActualCache = [:]

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

//Приоритет документов удостоверяющих личность <Идентификатор, Приоритет(int)>
@Field def documentPriorityCache = [:]

// Страны
@Field def citizenshipCache = [:]

// Кэш для справочников
@Field def refBookCache = [:]

// Поля справочников
@Field final String RF_CODE = "CODE"
@Field final String RF_FOR_FOND = "FOR_FOND"
@Field final String RF_FOR_OPS_OMS = "FOR_OPS_OMS"

def checkData() {
    def msgErrNotEquals = "Не совпадает значение %s = \"%s\" плательщика страховых взносов %s."

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

    // Общие параметры
    def mapConfigurationParam = getConfigurationParam()

    // Коды тарифа плательщика
    def mapActualTariffPayerCode = getActualTariffPayerCode()

    // Основания заполнения сумм страховых взносов
    def listActualFillBase = getActualFillBaseCode()

    // Коды классов условий труда
    def listActualHardWork = getActualHardWork()

    // Коды категорий застрахованных лиц
    def listPersonCategory = getActualPersonCategory()

    def xmlStream = declarationService.getXmlStream(declarationData.id)
    def fileNode = new XmlSlurper().parse(xmlStream);

    // ------------Проверки по плательщику страховых взносов RASCHSV_SVNP_PODPISANT

    fileNode.childNodes().each { documentNode ->
        // Документ
        if (documentNode.name == NODE_NAME_DOCUMENT) {

            // 2.1.1 Соответствие кода места настройкам подразделения
            def poMestuCodeXml = documentNode.attributes()[DOCUMENT_PO_MESTU]
            def poMestuParam = mapPresentPlace.get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
            def poMestuCodeParam = poMestuParam?.get(RF_CODE)?.value
            if (poMestuCodeXml != poMestuCodeParam) {
                def pathPoMestu = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, DOCUMENT_PO_MESTU].join(".")
                logger.warn("Код места, по которому предоставляется документ " + pathPoMestu + " = \"" + poMestuCodeXml + "\" не совпадает с настройками подразделения.")
            }

            // 2.1.2 Актуальность кода места
            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
            def poMestuActualParam = mapActualPresentPlace.get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)
            def poMestuCodeActualParam = poMestuActualParam?.get(RF_CODE)?.value
            if (poMestuCodeParam != poMestuCodeActualParam || !poMestuActualParam?.get(RF_FOR_FOND)?.value) {
                logger.warn("В настройках подразделений указан неактуальный код места, по которому предоставляется документ = \"" + poMestuCodeActualParam + "\"")
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

                    // НПЮЛ
                    documentChildNode.childNodes().each { NPYLNode ->
                        if (NPYLNode.name == NODE_NAME_NPYL) {
                            sberbankInnXml = NPYLNode.attributes()[NPYL_INNYL]
                            kppXml = NPYLNode.attributes()[NPYL_KPP]

                            // СвРеоргЮЛ
                            NPYLNode.childNodes().each { sVReorgYLNode ->
                                if (sVReorgYLNode.name == NODE_NAME_SV_REORG_YL) {
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
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_SV_NP, SV_NP_OKVED].join(".")
                        logger.warn(sprintf(msgErrNotEquals, pathAttr, okvedCodeXml, "КПП = \"" + kppXml + "\" с настройками подразделения"))
                    }

                    // 2.1.4 Актуальность ОКВЭД
                    // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                    def okvedCodeActualParam = mapActualOkvedCode.get(departmentParamIncomeRow?.OKVED?.referenceValue)
                    if (okvedCodeParam != okvedCodeActualParam) {
                        logger.warn("В настройках подразделений указан неактуальный ОКВЭД = \"" + okvedCodeParam + "\" с настройками подразделения")
                    }

                    // 2.1.5 Соответсвие ИНН ЮЛ Общим параметрам
                    def sberbankInnParam = mapConfigurationParam.get("SBERBANK_INN")
                    if (sberbankInnXml != sberbankInnParam) {
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_NPYL, NPYL_INNYL].join(".")
                        logger.warn("Не совпадает " + pathAttr + " = \"" + sberbankInnXml + "\" для организации - плательщика страховых взносов с настройками подразделения.")
                    }

                    // 2.1.6 Соответсвие КПП ЮЛ настройкам подразделения
                    def kppParam = departmentParamIncomeRow?.KPP?.stringValue
                    if (kppXml != kppParam) {
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_SV_NP, NODE_NAME_NPYL, NPYL_KPP].join(".")
                        logger.warn(sprintf(msgErrNotEquals, pathAttr, kppXml, "ИНН = \"" + sberbankInnXml + "\" с настройками подразделения"))
                    }

                    // 2.1.7 Соответствие формы реорганизации
                    def sVReorgYLFormParam = mapReorgFormCode.get(departmentParamIncomeRow?.REORG_FORM_CODE?.referenceValue)
                    if (sVReorgYLFormXml != sVReorgYLFormParam) {
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_SV_NP, NODE_NAME_NPYL, NODE_NAME_SV_REORG_YL, SV_REORG_YL_FORM_REORG].join(".")
                        logger.warn(sprintf(msgErrNotEquals, pathAttr, sVReorgYLFormXml, "КПП = \"" + kppXml + "\" с настройками подразделения"))
                    }

                    // 2.1.8 Соответствие ИНН реорганизованной организации
                    def sVReorgYLInnParam = departmentParamIncomeRow?.REORG_INN?.stringValue
                    if (sVReorgYLInnXml != sVReorgYLInnParam) {
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_SV_NP, NODE_NAME_NPYL, NODE_NAME_SV_REORG_YL, SV_REORG_YL_INNYL].join(".")
                        logger.warn(sprintf(msgErrNotEquals, pathAttr, sVReorgYLInnXml, "КПП = \"" + kppXml + "\" с настройками подразделения"))
                    }

                    // 2.1.9 Соответствие КПП реорганизованной организации
                    def sVReorgYLKppParam = departmentParamIncomeRow?.REORG_KPP?.stringValue
                    if (sVReorgYLKppXml != sVReorgYLKppParam) {
                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_SV_NP, NODE_NAME_NPYL, NODE_NAME_SV_REORG_YL, SV_REORG_YL_KPP].join(".")
                        logger.warn(sprintf(msgErrNotEquals, pathAttr, sVReorgYLKppXml, "КПП = \"" + kppXml + "\" с настройками подразделения"))
                    }
                } else if (documentChildNode.name == NODE_NAME_RASCHET_SV) {
                    // РасчетСВ
                    documentChildNode.childNodes().each { raschetSvChildNode ->

                        // ОбязПлатСВ
                        if (raschetSvChildNode.name == NODE_NAME_OBYAZ_PLAT_SV) {

                            // 2.2.1 Соответствие кода ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            def oktmoXml = raschetSvChildNode.attributes()[OBYAZ_PLAT_SV_OKTMO]
                            def oktmoParam = getRefBookValue(REF_BOOK_OKTMO_ID, departmentParamIncomeRow?.OKTMO?.referenceValue)
                            oktmoParam.each {key, value -> logger.info(key + " = " + value.toString())}
                            if (oktmoXml != oktmoParam?.CODE?.stringValue) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_OBYAZ_PLAT_SV, OBYAZ_PLAT_SV_OKTMO].join(".")
                                logger.warn(sprintf(msgErrNotEquals, pathAttr, oktmoXml, "КПП = \"" + kppXml + "\" с настройками подразделения"))
                            }

                            // 2.2.2 Актуальность ОКТМО (справочник ОКТМО очень большой, поэтому обращаться к нему будем по записи)
                            // При оценке актуальности значения справочника берутся НЕ на последний день отчетного периода, а на ТЕКУЩУЮ СИСТЕМНУЮ ДАТУ.
                            if (oktmoParam != isActualRecordOKTMO(departmentParamIncomeRow?.OKTMO?.referenceValue)) {
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
                                        logger.warn("Код тарифа плательщика " + pathAttr + " = \"" + tariffPayerCodeXml + "\" не найден (не действует) в справочнике \"Коды тарифа плательщика\".")
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
                                                        logger.warn("Код основания заполнения " + pathAttr + "\" = " + fillBaseXml + "\" не найден (не действует) в справочнике \"Основания заполнения\".")
                                                    }

                                                    // 2.2.5 Значение кода класса условий труда
                                                    if (!listActualHardWork.contains(hardWorkXml)) {
                                                        def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV,
                                                                        NODE_NAME_OBYAZ_PLAT_SV, NODE_NAME_RASCH_SV_OPS_OMS, NODE_NAME_RASCH_SV_OPS428,
                                                                        NODE_NAME_RASCH_SV_428_3, RASCH_SV_OPS428_3_KLAS_USL_TRUD].join(".")
                                                        logger.warn("Код класса условий труда " + pathAttr + "\" = " + hardWorkXml + "\" не найден (не действует) в справочнике \"Коды классов условий труда\".")
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
                                logger.warn(pathAttr + " = \"" + nomKorrPersXml + "\" не соответствует номеру корректировки файла \"" + UploadFileName + "\"")
                            }

                            // 2.3.2 Корректность периода
                            def periodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_PERIOD]
                            if (periodDocXml != periodPersXml) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, PERV_SV_STRAH_LIC_PERIOD].join(".")
                                logger.warn(pathAttr + " = \"" + periodPersXml + "\" не соответствует номеру корректировки файла \"" + UploadFileName + "\"")
                            }

                            // 2.3.3 Корректность отчетного года
                            def otchetGodPersXml = raschetSvChildNode.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]
                            if (otchetGodDocXml != otchetGodPersXml) {
                                def pathAttr = [NODE_NAME_FILE, NODE_NAME_DOCUMENT, NODE_NAME_RASCHET_SV, NODE_NAME_PERS_SV_STRAH_LIC, PERV_SV_STRAH_LIC_OTCHET_GOD].join(".")
                                logger.warn(pathAttr + " = \"" + otchetGodPersXml + "\" не соответствует отчетному году файла \"" + UploadFileName + "\"")
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
 * @param ndflPersonList
 * @return
 */
def getPersonIds(def ndflPersonList) {
    def personIds = []
    ndflPersonList.each { ndflPerson ->
        // todo Почему ndflPerson.personId != 0
        if (ndflPerson.personId != null && ndflPerson.personId != 0) {
            personIds.add(ndflPerson.personId)
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

Integer getInteger (String val) {
    if (val != null) {
        if (val != "") {
            return val.toInteger()
        }
    }
    return null
}

Long getLong (String val) {
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
// todo добавить обработку дублей
def getRefPersons(def personIds) {
    if (personsCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
//        def dublMap = [:]
//        def dublList = []
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)

            // Добавим дубликат в Мапу
//            if (person.get(RF_DUBLICATES).value != null && !dublMap.get(person.get(RF_DUBLICATES).value)) {
//                dublMap.put(person.get(RF_DUBLICATES).value, personId)
//                dublList.add(person.get(RF_DUBLICATES).value)
//            }
        }
        // Получим оригинальные записи, если имеются дубликаты
//        if (dublMap.size() > 0) {
//            def refBookDublMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, dublList)
//            refBookDublMap.each { originalPersonId, person ->
//                dublPersonId = dublMap.get(originalPersonId).value
//            }
//        }
    }
    return personsCache;
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
 * @param recordIds - коллекция идентификаторов записей справочника
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
 * Получить все актуальные записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getActualRefBookByRecordId(def long refBookId, def recordId) {
    if (recordId == null) {
        return [:]
    }
    def filter = "ID = $recordId"
    def refBookList = getProvider(refBookId).getRecords(new Date(), null, filter, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
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
 * Получить "Общие параметры"
 * @return
 */
def getConfigurationParam() {
    if (configurationParamCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_CONFIGURATION_PARAM_ID)
        refBookMap.each { refBook ->
            configurationParamCache.put(refBook?.CODE?.stringValue, refBook?.VALUE?.stringValue)
        }
    }
    return configurationParamCache
}

/**
 * Получить "Коды ОКТМО"
 * @return
 */
def getOKTMO() {
    if (oktmoCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_OKTMO_ID)
        refBookMap.each { refBook ->
            oktmoCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return oktmoCodeCache
}

/**
 * Получить актульные "Коды ОКТМО"
 * @return
 */
def isActualRecordOKTMO(def recordId) {
    def refBookMap = getActualRefBookByRecordId(REF_BOOK_OKTMO_ID, recordId)
    refBookMap.each { refBook ->
        return true
    }
    return false
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
    return documentTypeMap.find {
        it.value?.equalsIgnoreCase(code)
    }?.key;
}

/**
 * Получить "Коды видов документов, удостоверяющих личность"
 * @return
 */
def getRefDocument() {
    if (documentCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentCodesCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return documentCodesCache;
}

/**
 * Получить "Приоритет видов документов, удостоверяющих личность"
 * @return
 */
def getRefDocumentPriority() {
    if (documentPriorityCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentPriorityCache.put(refBook?.id?.numberValue, refBook?.PRIORITY?.stringValue)
        }
    }
    return documentPriorityCache;
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