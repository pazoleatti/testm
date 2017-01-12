package form_template.ndfl.rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.dao.impl.BlobDataDaoImpl
import com.aplana.sbrf.taxaccounting.dao.impl.DeclarationSubreportDaoImpl
import com.aplana.sbrf.taxaccounting.dao.impl.raschsv.RaschsvPersSvStrahLicDaoImpl
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVyplMt
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDop
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDopMt
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.context.ContextLoader

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

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        //throw new RuntimeException("IMPORT_TRANSPORT_FILE")
        break;
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        def writer = scriptSpecificReportHolder.getFileOutputStream()
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        def workbook = getSpecialReportTemplate(alias)
        fillTestGeneralList(workbook)
        if (alias.equalsIgnoreCase("person_rep")) {
            fillPersSvSheet(workbook)
        } else if (alias.equalsIgnoreCase("consolidated_report")) {
            fillPersSvConsSheet(workbook)
        }
        workbook.write(writer)
        writer.close()
        scriptSpecificReportHolder
                .setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
        break
}

// Находит в базе данных RaschsvPersSvStrahLic
def getrRaschsvPersSvStrahLic() {
    TestDataHolder.getInstance().FL_DATA
}


// Находит в базе данных список RaschsvPersSvStrahLic
def getrRaschsvPersSvStrahLicList() {
    [TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA, TestDataHolder.getInstance().FL_DATA]
}

/****************************************************************************
 *  Блок заполнения данными титульной страницы                              *
 *                                                                          *
 * **************************************************************************/

def fillTestGeneralList(final workbook) {
    def testDataHolder = TestDataHolder.getInstance()
    def sheet = workbook.getSheet(Sheet.COMMON_SHEET.getName())
    sheet.getRow(3).getCell(1).setCellValue(testDataHolder.FILE_NAME)
    sheet.getRow(4).getCell(1).setCellValue(testDataHolder.VERS_PROG)
    sheet.getRow(5).getCell(1).setCellValue(testDataHolder.VERS_FORMAT)
    sheet.getRow(8).getCell(1).setCellValue(testDataHolder.KND)
    sheet.getRow(9).getCell(1).setCellValue(testDataHolder.FORM_DATE)
    sheet.getRow(10).getCell(1).setCellValue(testDataHolder.CORRECT_NUMBER)
    sheet.getRow(11).getCell(1).setCellValue(testDataHolder.REPORT_PERIOD)
    sheet.getRow(12).getCell(1).setCellValue(testDataHolder.REPORT_YEAR)
    sheet.getRow(13).getCell(1).setCellValue(testDataHolder.CODE_NO)
    sheet.getRow(14).getCell(1).setCellValue(testDataHolder.PLACE)
    sheet.getRow(18).getCell(1).setCellValue(testDataHolder.OKVED)
    sheet.getRow(19).getCell(1).setCellValue(testDataHolder.PHONE_NUMBER)
    sheet.getRow(21).getCell(1).setCellValue(testDataHolder.ORG_NAME)
    sheet.getRow(22).getCell(1).setCellValue(testDataHolder.INN_ORG)
    sheet.getRow(23).getCell(1).setCellValue(testDataHolder.KPP_ORG)
    sheet.getRow(25).getCell(1).setCellValue(testDataHolder.CODE_REORG)
    sheet.getRow(26).getCell(1).setCellValue(testDataHolder.INN_REORG)
    sheet.getRow(27).getCell(1).setCellValue(testDataHolder.SIGNER)
    sheet.getRow(30).getCell(1).setCellValue(testDataHolder.SIGNER_FIO)
    sheet.getRow(31).getCell(1).setCellValue(testDataHolder.REPRESENTER)
    sheet.getRow(33).getCell(1).setCellValue(testDataHolder.REPRESENTER_ORG)
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
def getSpecialReportTemplate(alias) {
    def declarationSubreportDao = getBeanByClass(DeclarationSubreportDaoImpl.class)
    def subreport = declarationSubreportDao.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias)
    def blobDataDao = getBeanByClass(BlobDataDaoImpl.class)
    def blobData = blobDataDao.get(subreport.getBlobDataId())
    new XSSFWorkbook(blobData.getInputStream())
}

// Находит бин в Spring контексте по объекту класса бина
def getBeanByClass(final clazz) {
    def applicationContext = ContextLoader.getCurrentWebApplicationContext()
    applicationContext.getBean(clazz)
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

    final FILE_NAME = "test1"
    final VERS_PROG = "test2"
    final VERS_FORMAT = "test3"
    final KND = "test4"
    final FORM_DATE = "test5"
    final CORRECT_NUMBER = "test6"
    final REPORT_PERIOD = "test7"
    final REPORT_YEAR = "test8"
    final CODE_NO = "test9"
    final PLACE = "test10"
    final OKVED = "test11"
    final PHONE_NUMBER = "test12"
    final ORG_NAME = "test13"
    final INN_ORG = "test14"
    final KPP_ORG = "test15"
    final CODE_REORG = "test16"
    final INN_REORG = "test17"
    final KPP_REORG = "test18"

    final SIGNER = "test19"
    final SIGNER_FIO = "test20"

    final REPRESENTER = "test21"
    final REPRESENTER_ORG = "test22"

    final FL_DATA

    static getInstance() {
        return testDataHolder
    }

    private TestDataHolder() {
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
    }