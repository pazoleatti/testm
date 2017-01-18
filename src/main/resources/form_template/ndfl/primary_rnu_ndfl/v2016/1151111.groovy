package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.dao.impl.BlobDataDaoImpl
import com.aplana.sbrf.taxaccounting.dao.impl.DeclarationSubreportDaoImpl
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService
import java.text.SimpleDateFormat

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.context.ContextLoader

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVyplMt
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDop
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplSvDopMt


switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        //throw new RuntimeException("IMPORT_TRANSPORT_FILE")
        break;
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        def writer = scriptSpecificReportHolder.getFileOutputStream()
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        def workbook = getSpecialReportTemplate(alias)
        fillGeneralList(workbook)
        if (alias.equalsIgnoreCase("person_report")) {
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
    def raschsvPersSvStrahLicDao = getBeanByClass(RaschsvSvnpPodpisantDao.class)
    def inn = getInnFl()
    raschsvPersSvStrahLic = raschsvPersSvStrahLicDao.findPersonByInn(declarationData.geetId(), inn)

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

def getInnFl() {
    // TODO: определить как будет находится инн для передачи в аргумент raschsvPersSvStrahLicDao.findPersonByInn
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
    /*def leftStyle = normalWithBorderStyleLeftAligned(workbook)
    def centerStyle = normalWithBorderStyleCenterAligned(workbook)
    def defaultStyle = normalWithBorderStyle(workbook)*/
    def style = workbook.createCellStyle()
    def bordersProps = thinBorderStyle()
    def cells = [row.createCell(0), row.createCell(1), row.createCell(2), row.createCell(3), row.createCell(4),
                 row.createCell(5), row.createCell(6), row.createCell(7), row.createCell(8), row.createCell(9),
                 row.createCell(10), row.createCell(11), row.createCell(12), row.createCell(13), row.createCell(14),
                 row.createCell(15), row.createCell(16), row.createCell(17)]
    cells.each {it.setCellStyle(style)}
    cells.each {CellUtil.setCellStyleProperties it, bordersProps}
    CellUtil.setAlignment(cells[0], HorizontalAlignment.LEFT)
    CellUtil.setAlignment(cells[1], HorizontalAlignment.LEFT)
    CellUtil.setAlignment(cells[2], HorizontalAlignment.CENTER)
    CellUtil.setAlignment(cells[3], HorizontalAlignment.CENTER)

    cells[0].setCellValue(raschsvPersSvStrahLic.getNomer())
    cells[1].setCellValue(formatDate(raschsvPersSvStrahLic.getSvData(), "dd.MM.yyyy"))
    cells[2].setCellValue(raschsvPersSvStrahLic.getNomKorr())
    cells[3].setCellValue(raschsvPersSvStrahLic.getPeriod())
    cells[4].setCellValue(raschsvPersSvStrahLic.getOtchetGod())
    cells[5].setCellValue(raschsvPersSvStrahLic.getFamilia())
    cells[6].setCellValue(raschsvPersSvStrahLic.getImya())
    cells[7].setCellValue(raschsvPersSvStrahLic.getMiddleName())
    cells[8].setCellValue(raschsvPersSvStrahLic.getInnfl())
    cells[9].setCellValue(raschsvPersSvStrahLic.getSnils())
    cells[10].setCellValue(raschsvPersSvStrahLic.getDataRozd())
    cells[11].setCellValue(raschsvPersSvStrahLic.getGrazd())
    cells[12].setCellValue(raschsvPersSvStrahLic.getPol())
    cells[13].setCellValue(raschsvPersSvStrahLic.getKodVidDoc())
    cells[14].setCellValue(raschsvPersSvStrahLic.getSerNomDoc())
    cells[15].setCellValue(raschsvPersSvStrahLic.getPrizOps())
    cells[16].setCellValue(raschsvPersSvStrahLic.getPrizOms())
    cells[17].setCellValue(raschsvPersSvStrahLic.getPrizOss())
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
/*def normalWithBorderStyleLeftAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_LEFT)
    thinBorderStyle(style)
    return style
}*/

// Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
/*def normalWithBorderStyleCenterAligned(workbook) {
    def style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_CENTER)
    thinBorderStyle(style)
    return style
}*/

// Создать стиль ячейки с нормальным шрифтом с тонкими границами
/*def normalWithBorderStyle(workbook) {
    def style = workbook.createCellStyle()
    thinBorderStyle(style)
    return style
}*/

// Добавляет к стилю ячейки тонкие границы
def thinBorderStyle() {
    def propMap = new HashMap<String, Object>();
    propMap.put(CellUtil.BORDER_TOP, CellStyle.BORDER_THIN);
    propMap.put(CellUtil.BORDER_BOTTOM, CellStyle.BORDER_THIN);
    propMap.put(CellUtil.BORDER_LEFT, CellStyle.BORDER_THIN);
    propMap.put(CellUtil.BORDER_RIGHT, CellStyle.BORDER_THIN);
    return propMap
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
        FL_DATA.middleName = "Семенович"
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