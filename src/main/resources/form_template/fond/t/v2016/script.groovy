package form_template.fond.t.v2016

import com.aplana.sbrf.taxaccounting.dao.impl.BlobDataDaoImpl
import com.aplana.sbrf.taxaccounting.dao.impl.DeclarationSubreportDaoImpl
import com.aplana.sbrf.taxaccounting.dao.impl.raschsv.RaschsvPersSvStrahLicDaoImpl
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVyplMt
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
import groovy.transform.Field

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
        parseRaschsv()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        def writer = scriptSpecificReportHolder.getFileOutputStream()
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        def workbook = getSpecialReportTemplate()
        /*fillGeneralList(workbook)
        if (alias.equalsIgnoreCase("person_report")) {
            fillPersSvSheet(workbook)
        } else if (alias.equalsIgnoreCase("consolidated_report")) {
            fillPersSvConsSheet(workbook)
        }*/
        workbook.write(writer)
        writer.close()
        scriptSpecificReportHolder
                .setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
        break
    default:
        break
}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
// Обработка события CREATE_SPECIFIC_REPORT
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

def enum Sheet {
    COMMON_SHEET("Общее"),
    PERSONAL_DATA("Сведения о ФЛ"),
    CONS_PERSONAL_DATA("3.Персониф. Сведения")

    def name

    Sheet(def n) {
        name = n;
    }

    def getName() {
        return name
    }
}

// Находит в базе данных RaschsvPersSvStrahLic
def getrRaschsvPersSvStrahLic() {
    TestDataHolder.getInstance().FL_DATA

}


// Находит в базе данных список RaschsvPersSvStrahLic
def getrRaschsvPersSvStrahLicList() {
    [TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA]
}


def getRaschsvSvnpPodpisant() {
    def raschsvSvnpPodpisantDao = getBeanByClass(RaschsvSvnpPodpisantDao.class)
    // TODO Нужен select в RaschsvSvnpPodpisantDao или брать из xml
    TestDataHolder.getInstance().PODPISANT
}
/****************************************************************************
 *  Блок заполнения данными титульной страницы                              *
 *                                                                          *
 * **************************************************************************/

def fillGeneralList(final workbook) {
    def sheet = workbook.getSheet(Sheet.COMMON_SHEET.getName())
    def declarationService = getBeanByClass(DeclarationService.class)
    def podpisant = getRaschsvSvnpPodpisant()
    def xmlStream = declarationService.getXmlStream(declarationData.getId())
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
    sheet.getRow(14).getCell(1).setCellValue(Файл.Документ.@ПоМесту.toString())
    sheet.getRow(18).getCell(1).setCellValue podpisant.svnpOkved
    sheet.getRow(19).getCell(1).setCellValue podpisant.svnpTlph
    sheet.getRow(21).getCell(1).setCellValue podpisant.svnpNaimOrg
    sheet.getRow(22).getCell(1).setCellValue podpisant.svnpInnyl
    sheet.getRow(23).getCell(1).setCellValue podpisant.svnpKpp
    sheet.getRow(25).getCell(1).setCellValue podpisant.svnpSvReorgForm
    sheet.getRow(26).getCell(1).setCellValue podpisant.svnpSvReorgInnyl
    sheet.getRow(27).getCell(1).setCellValue podpisant.svnpSvReorgKpp
    sheet.getRow(30).getCell(1).setCellValue podpisant.familia + " " + podpisant.imya + " " + podpisant.middleName
    sheet.getRow(31).getCell(1).setCellValue podpisant.podpisantPrPodp
    sheet.getRow(33).getCell(1).setCellValue podpisant.podpisantNaimDoc
    sheet.getRow(34).getCell(1).setCellValue podpisant.podpisantNaimOrg
}

/****************************************************************************
 *  Блок заполнения данными листа песонифицированных сведений ФЛ            *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист персонифицированных сведений
def fillPersSvSheet(final workbook) {
    def startIndex = 3;
    def pointer = startIndex
    def raschsvPersSvStrahLic = getrRaschsvPersSvStrahLic()
    pointer += fillRaschsvPersSvStrahLicTable(pointer, [raschsvPersSvStrahLic], workbook, Sheet.PERSONAL_DATA.getName())
    pointer += 5
    pointer += fillRaschSvVyplat(pointer, raschsvPersSvStrahLic, workbook)
    pointer += 6
    pointer += fillRaschSvVyplatDop(pointer, raschsvPersSvStrahLic, workbook)
}

// Создает строку для таблицы Персонифицированные сведения о застрахованных лицах
def fillRaschsvPersSvStrahLicTable(final startIndex, final raschsvPersSvStrahLicList, final workbook, final sheetName) {
    def raschsvPersSvStrahLicListSize = raschsvPersSvStrahLicList.size()
    def sheet = workbook.getSheet(sheetName)
    if (!sheetName.equals(Sheet.CONS_PERSONAL_DATA.getName())) {
        sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvPersSvStrahLicListSize + 1)
    }
    for (int i = 0; i < raschsvPersSvStrahLicListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschsvPersSvStrahLicRow(raschsvPersSvStrahLicList[i], row, workbook)
    }
    return raschsvPersSvStrahLicListSize
}

/*
 * Заполняет данными строку из таблицы Персонифицированные сведения о
 * застрахованных лицах
 **/
def fillCellsOfRaschsvPersSvStrahLicRow(final raschsvPersSvStrahLic, final row, final workbook) {
    def leftStyle = normalWithBorderStyleLeftAligned(workbook)
    def centerStyle = normalWithBorderStyleCenterAligned(workbook)
    def defaultStyle = normalWithBorderStyle(workbook)
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
    cell7.setCellValue(raschsvPersSvStrahLic.getMiddleName())
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
def fillRaschSvVyplat(final startIndex, final raschsvPersSvStrahLic, final workbook) {
    def raschsvSvVypl = raschsvPersSvStrahLic.raschsvSvVypl
    def raschsvSvVyplMtList = raschsvSvVypl.raschsvSvVyplMtList
    def raschsvSvVyplMtListSize = raschsvSvVyplMtList.size()
    def sheet = workbook.getSheet(Sheet.PERSONAL_DATA.getName())
    sheet.shiftRows(startIndex, sheet.getLastRowNum(), raschsvSvVyplMtListSize + 1)
    for (int i = 0; i < raschsvSvVyplMtListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatMt(raschsvPersSvStrahLic, raschsvSvVyplMtList[i], row)
    }
    fillCellsOfRaschSvVyplat(raschsvSvVypl, sheet.createRow(raschsvSvVyplMtListSize + startIndex))
    return raschsvSvVyplMtListSize
}

// Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
def fillCellsOfRaschSvVyplatMt(final raschsvPersSvStrahLic, final raschsvSvVyplMt, final row) {
    row.createCell(0).setCellValue(raschsvPersSvStrahLic.getNomer())
    row.createCell(1).setCellValue(raschsvSvVyplMt.mesyac)
    row.createCell(2).setCellValue(raschsvSvVyplMt.kodKatLic)
    row.createCell(3).setCellValue(raschsvSvVyplMt.sumVypl)
    row.createCell(4).setCellValue(raschsvSvVyplMt.vyplOps)
    row.createCell(5).setCellValue(raschsvSvVyplMt.vyplOpsDog)
    row.createCell(6).setCellValue(raschsvSvVyplMt.nachislSv)
}

// Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
def fillCellsOfRaschSvVyplat(final raschsvSvVypl, final row) {
    // TODO: объединить ячейки
    row.createCell(0).setCellValue("Всего за последние три месяца расчетного (отчетного) периода")
    row.createCell(3).setCellValue(raschsvSvVypl.sumVyplVs3)
    row.createCell(4).setCellValue(raschsvSvVypl.vyplOpsVs3)
    row.createCell(5).setCellValue(raschsvSvVypl.vyplOpsDogVs3)
    row.createCell(6).setCellValue(raschsvSvVypl.nachislSvVs3)
}

// Заполняет таблицу "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
def fillRaschSvVyplatDop(final startIndex, final raschsvPersSvStrahLic, final workbook) {
    def raschsvSvVyplDop = raschsvPersSvStrahLic.raschsvVyplSvDop
    def raschsvSvVyplDopMtList = raschsvSvVyplDop.raschsvVyplSvDopMtList
    def raschsvSvVyplDopMtListSize = raschsvSvVyplDopMtList.size()
    def sheet = workbook.getSheet(Sheet.PERSONAL_DATA.getName())
    for (int i = 0; i < raschsvSvVyplDopMtListSize; i++) {
        def row = sheet.createRow(i + startIndex)
        fillCellsOfRaschSvVyplatDopMt(raschsvPersSvStrahLic, raschsvSvVyplDopMtList[i], row)
    }
    fillCellsOfRaschSvVyplatDop(raschsvSvVyplDop, sheet.createRow(raschsvSvVyplDopMtListSize + startIndex))
    return raschsvSvVyplDopMtListSize
}

// Заполняет данными строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
def fillCellsOfRaschSvVyplatDopMt(final raschsvPersSvStrahLic, final raschsvSvVyplDopMt, final row){
    row.createCell(0).setCellValue(raschsvPersSvStrahLic.getNomer())
    row.createCell(1).setCellValue(raschsvSvVyplDopMt.mesyac)
    row.createCell(2).setCellValue(raschsvSvVyplDopMt.tarif)
    row.createCell(3).setCellValue(raschsvSvVyplDopMt.vyplSv)
    row.createCell(4).setCellValue(raschsvSvVyplDopMt.nachislSv)
}

// Заполняет данными итоговую строку для таблицы "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу"
def fillCellsOfRaschSvVyplatDop (final raschsvSvVyplDop, final row) {
    // TODO: объединить ячейки
    row.createCell(0).setCellValue("Всего за последние три месяца расчетного (отчетного) периода")
    row.createCell(3).setCellValue(raschsvSvVyplDop.vyplSvVs3)
    row.createCell(4).setCellValue(raschsvSvVyplDop.nachislSvVs3)
}

/****************************************************************************
 *  Блок заполнения данными листа 3.Персониф. Сведения                      *
 *                                                                          *
 * **************************************************************************/

// Заполняет данными лист 3.Персониф. Сведения
def fillPersSvConsSheet(final workbook) {
    def startIndex = 3
    def pointer = startIndex
    def raschsvPersSvStrahLic = getrRaschsvPersSvStrahLicList()
    pointer += fillRaschsvPersSvStrahLicTable(pointer, raschsvPersSvStrahLic, workbook, Sheet.CONS_PERSONAL_DATA.getName())
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
def thinBorderStyle(style) {
    style.setBorderTop(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderRight(CellStyle.BORDER_THIN)
    return style
}
/****************************************************************************
 *  Вспомогательные методы                                                  *
 *                                                                          *
 * **************************************************************************/

// Находит в базе данных шаблон спецотчета по физическому лицу и возвращает его
def getSpecialReportTemplate() {
    //def blobData = blobDataService.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
    println raschsvPersSvStrahLicService
    //new XSSFWorkbook(blobData.getInputStream())
}

// Создает отформатироованную строку из объекта даты на основе передаваемого шаблона
def formatDate(final date, final pattern) {
    def formatter = new SimpleDateFormat(pattern)
    formatter.format(date)
}

/****************************************************************************
 *  Тестовые данные                                                         *
 *                                                                          *
 * **************************************************************************/
class TestDataHolder {
    final static testDataHolder = new TestDataHolder()

    final PODPISANT
    final FL_DATA

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
        FL_DATA.imya = "Семенович"
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
        final VYPL_MT1 = new RaschsvSvVyplMt();
        VYPL_MT1.setMesyac("Январь")
        VYPL_MT1.setKodKatLic("1")
        VYPL_MT1.setSumVypl(300)
        VYPL_MT1.setVyplOps(100)
        VYPL_MT1.setVyplOpsDog(100)
        VYPL_MT1.setNachislSv(150)
        final VYPL_MT2 = new RaschsvSvVyplMt();
        VYPL_MT2.setMesyac("Февраль")
        VYPL_MT2.setKodKatLic("1")
        VYPL_MT2.setSumVypl(300)
        VYPL_MT2.setVyplOps(100)
        VYPL_MT2.setVyplOpsDog(100)
        VYPL_MT2.setNachislSv(100)
        final VYPL_MT3 = new RaschsvSvVyplMt();
        VYPL_MT3.setMesyac("Март")
        VYPL_MT3.setKodKatLic("1")
        VYPL_MT3.setSumVypl(400)
        VYPL_MT3.setVyplOps(100)
        VYPL_MT3.setVyplOpsDog(100)
        VYPL_MT3.setNachislSv(150)
        VYPL.raschsvSvVyplMtList = [VYPL_MT1, VYPL_MT2, VYPL_MT3]
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
        PODPISANT.setMiddleName("otchestvo_test")
        PODPISANT.setPodpisantPrPodp("pravoPodpis_test")
        PODPISANT.setPodpisantNaimDoc("docName_test")
        PODPISANT.setPodpisantNaimOrg("orgName_test")
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
@Field final NODE_NAME_DOCUMENT = "Документ"

@Field final NODE_NAME_SV_NP = "СвНП"
@Field final NODE_NAME_NPYL = "НПЮЛ"
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
@Field final NODE_NAME_SV_VYPL_MT = "СвВыплМК"
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
@Field final FIO_MIDDLE_NAME = 'Отчество'

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

void parseRaschsv() {

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def fileNode = new XmlSlurper().parse(ImportInputStream);
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
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
    if (raschsvPersSvStrahLicList.size() > 0) {
        raschsvPersSvStrahLicService.insertPersSvStrahLic(raschsvPersSvStrahLicList)
    }

    // Сохранение Сведений о плательщике страховых взносов и Сведения о лице, подписавшем документ
    raschsvSvnpPodpisantService.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant)

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
            raschsvSvnpPodpisant.middleName = podpisantChildNode.attributes()[FIO_MIDDLE_NAME]
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

                RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new RaschsvSvOpsOmsRasch()
                raschsvSvOpsOmsRasch.nodeName = raschSvOpsOmsChildNode.name

                // Набор сведений о сумме
                def raschsvSvOpsOmsRaschSumList = []
                // Набор сведений о количестве
                def raschsvSvOpsOmsRaschKolList = []

                if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OPS ||
                        raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OMS) {
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
                } else if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_OPS428) {
                    // Разбор узла РасчСВ_ОПС428
                    raschSvOpsOmsChildNode.childNodes().each { raschSvOps428ChildNode ->
                        // Разбор узлов РасчСВ_428.1-2, РасчСВ_428.3
                        raschsvSvOpsOmsRasch.prOsnSvDop = raschSvOpsOmsChildNode.attributes()[RASCH_SV_OPS428_12_PR_OSN_SV_DOP]
                        raschsvSvOpsOmsRasch.kodOsnov = raschSvOpsOmsChildNode.attributes()[RASCH_SV_OPS428_3_KOD_OSNOV]
                        raschsvSvOpsOmsRasch.osnovZap = raschSvOpsOmsChildNode.attributes()[RASCH_SV_OPS428_3_OSNOV_ZAP]
                        raschsvSvOpsOmsRasch.klasUslTrud = raschSvOpsOmsChildNode.attributes()[RASCH_SV_OPS428_3_KLAS_USL_TRUD]

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
                    }
                } else if (raschSvOpsOmsChildNode.name == NODE_NAME_RASCH_SV_DSO) {
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
                }
                raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRaschSumList
                raschsvSvOpsOmsRasch.raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRaschKolList
                raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch)
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
                raschsvRashOssZakRash.pashVsego = getDouble(raschOssZakChildNode.attributes()[RASH_OSS_TIP_RASH_VSEGO])
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
                            raschsvSvInoGrazd.middleName = svPrimTarif22425ChildChildNode.attributes()[FIO_MIDDLE_NAME]
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
                            raschsvSvedObuch.middleName = svPrimTarif13422ChildChildNode.attributes()[FIO_MIDDLE_NAME]
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
                    raschsvPersSvStrahLic.middleName = fioNode.attributes()[FIO_MIDDLE_NAME]
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
                    def raschsvSvVyplMtList = []
                    svVyplSvopsChildNode.childNodes().each { svVyplMkNode ->
                        if (svVyplMkNode.name == NODE_NAME_SV_VYPL_MT) {
                            // Разбор узла СвВыплМК
                            RaschsvSvVyplMt raschsvSvVyplMt = new RaschsvSvVyplMt()
                            raschsvSvVyplMt.mesyac = svVyplMkNode.attributes()[SV_VYPL_MT_MESYAC]
                            raschsvSvVyplMt.kodKatLic = svVyplMkNode.attributes()[SV_VYPL_MT_KOD_KAT_LIC]
                            raschsvSvVyplMt.sumVypl = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_SUM_VYPL])
                            raschsvSvVyplMt.vyplOps = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_VYPL_OPS])
                            raschsvSvVyplMt.vyplOpsDog = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_VYPL_OPS_DOG])
                            raschsvSvVyplMt.nachislSv = getDouble(svVyplMkNode.attributes()[SV_VYPL_MT_NACHISL_SV])

                            raschsvSvVyplMtList.add(raschsvSvVyplMt)
                        }
                    }
                    raschsvSvVypl.raschsvSvVyplMtList = raschsvSvVyplMtList
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