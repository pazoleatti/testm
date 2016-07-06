package form_template.income.declaration_bank_2.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.JRPdfExporterParameter
import net.sf.jasperreports.engine.util.JRSwapFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по налогу на прибыль (Банк) (с периода год 2014)
 * Формирование XML для декларации налога на прибыль.
 * версия 2016 года
 * declarationTemplateId=21688
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        sourceCheck(true, LogLevel.WARNING)
        break
    case FormDataEvent.CHECK : // проверить
        def logLevel = declarationData.accepted ? LogLevel.WARNING : LogLevel.ERROR
        checkDepartmentParams(logLevel)
        sourceCheck(true, logLevel)
        logicCheck(logLevel)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        def logLevel = declarationData.accepted ? LogLevel.WARNING : LogLevel.ERROR
        checkDepartmentParams(logLevel)
        sourceCheck(true, logLevel)
        logicCheck(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED: // отменить принятие
        сancelAccepted()
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
        sourceCheck(true, LogLevel.WARNING)
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        sourceCheck(true, LogLevel.WARNING)
        generateXML(xml, true)
        break
    case FormDataEvent.CALCULATE_TASK_COMPLEXITY:
        calcTaskComplexity()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport()
        break
}

@Field
def version = '5.06'

// Кэш провайдеров
@Field
def providerCache = [:]

// Кэш значений справочника
@Field
def refBookCache = [:]

// значение подразделения из справочника 33
@Field
def departmentParam = null

// значение подразделения из справочника 330 (таблица)
@Field
def departmentParamTable = null

@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void checkDepartmentParams(LogLevel logLevel) {
    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamIncomeRow = getDepartmentParamTable(departmentParam.record_id.value)

    // Проверки подразделения
    def List<String> errorList = getErrorTable(departmentParamIncomeRow)
    for (String error : errorList) {
        logger.log(logLevel, String.format("На форме настроек подразделения текущего экземпляра декларации отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("На форме настроек подразделения текущего экземпляра декларации отсутствует значение атрибута %s!", error))
    }

    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("На форме настроек подразделения текущего экземпляра декларации неверно указано значение атрибута %s", error))
    }
}

// Проверка налоговой формы источника «Сведения о суммах налога на прибыль, уплаченного Банком за рубежом» (данная форма-источник создана и находится в статусе «Принята»)
private boolean sourceCheck(boolean loggerNeed, LogLevel logLevel) {
    def sourceFormTypeId = 421
    def sourceFormType = formTypeService.get(sourceFormTypeId)
    def success = true

    def formDataCollection = getAcceptedFormDataSources()
    def departmentFormType = formDataCollection?.records?.find { it.formType.id == sourceFormTypeId }
    def reportPeriod = getReportPeriod()
    if (departmentFormType == null) {
        if (loggerNeed) {
            logger.log(logLevel, "Не найден экземпляр «${sourceFormType.name}» за ${reportPeriod.name} ${reportPeriod.taxPeriod.year} в статусе «Принята» (налоговая форма не назначена источником декларации Банка/назначена источником, но не создана/назначена источником, создана, но не принята). При расчёте экземпляра декларации строка 240 Листа 02 будет заполнена значением «0»!")
        }
        success = false
    }
    return success
}

// Логические проверки.
void logicCheck(LogLevel logLevel) {
    def empty = 0
    // получение данных из xml'ки
    def reader = getXmlStreamReader(declarationData.reportPeriodId, declarationData.departmentId, false, false)
    if(reader == null){
        return
    }

    def elements = [:]

    def nalVipl311, nalIschisl, nalVipl311FB, nalIschislFB, nalVipl311Sub, nalIschislSub, stavNalFB, raschNalFound = false
    def vneRealDohSt, vneRealDohBezv, vneRealDohIzl, vneRealDohVRash, vneRealDohRinCBDD, vneRealDohCor, vneRealDohVs, dohVnerealFound = false
    def cosvRashVs, nalogi, rashCapVl10, rashCapVl30, rashZemUchVs, rashRealFound = false
    def rashVnerealPrDO, ubitRealPravTr, rashLikvOS, rashShtraf, rashRinCBDD, rashVnerealVs, rashVneRealFound = false
    def stoimRealPTDoSr, stoimRealPTFound = false
    def viruchRealPTDoSr, viruchRealPTFound = false
    def ubit1Prev269, ubit1Soot269, ubitRealPT1Found = false
    def kodNO, poMestu, documentFound = false
    def naimOrg, innJulNpJul, kppJulNpJul, npJulFound = false
    def prPodp, podpisantFound = false
    def signatorySurname, signatoryFirstName, fioFound = false
    def naimDok, svPredFound = false
    def oktmoNalPUAv, nalPUAvFound = false
    def oktmoNalPUMes, nalPUMesFound = false
    def okved, svNPFound = false
    def innJulSvReorgJul, kppSvReorgJul, formReorg, svReorgJulFound = false
    def versForm, idFile, kodNOProm, fileFound = false

    try { // ищем пока есть элементы и есть что искать
        while(reader.hasNext() &&
                !(raschNalFound && dohVnerealFound && rashRealFound && rashVneRealFound && stoimRealPTFound && viruchRealPTFound && ubitRealPT1Found)) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (!raschNalFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал'], elements)) {
                    raschNalFound = true
                    nalVipl311 = getXmlDecimal(reader, "НалВыпл311")
                    nalIschisl = getXmlDecimal(reader, "НалИсчисл")
                    nalVipl311FB = getXmlDecimal(reader, "НалВыпл311ФБ")
                    nalIschislFB = getXmlDecimal(reader, "НалИсчислФБ")
                    nalVipl311Sub = getXmlDecimal(reader, "НалВыпл311Суб")
                    nalIschislSub = getXmlDecimal(reader, "НалИсчислСуб")
                    stavNalFB = getXmlValue(reader, 'СтавНалФБ')
                } else if (!dohVnerealFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'ДохРеалВнеРеал', 'ДохВнеРеал'], elements)) {
                    dohVnerealFound = true
                    vneRealDohSt = getXmlDecimal(reader, "ВнеРеалДохСт")
                    vneRealDohBezv = getXmlDecimal(reader, "ВнеРеалДохБезв")
                    vneRealDohIzl = getXmlDecimal(reader, "ВнеРеалДохИзл")
                    vneRealDohVRash = getXmlDecimal(reader, "ВнеРеалДохВРасх")
                    vneRealDohRinCBDD = getXmlDecimal(reader, "ВнеРеалДохРынЦБДД")
                    vneRealDohCor = getXmlDecimal(reader, "ВнеРеалДохКор")
                    vneRealDohVs = getXmlDecimal(reader, "ВнеРеалДохВс")
                } else if (!rashRealFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РасхРеалВнеРеал', 'РасхРеал'], elements)) {
                    rashRealFound = true
                    cosvRashVs = getXmlDecimal(reader, "КосвРасхВс")
                    nalogi = getXmlDecimal(reader, "Налоги")
                    rashCapVl10 = getXmlDecimal(reader, "РасхКапВл10")
                    rashCapVl30 = getXmlDecimal(reader, "РасхКапВл30")
                    rashZemUchVs = getXmlDecimal(reader, "РасхЗемУчВс")
                } else if (!rashVneRealFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РасхРеалВнеРеал', 'РасхВнеРеал'], elements)) {
                    rashVneRealFound = true
                    rashVnerealPrDO = getXmlDecimal(reader, "РасхВнереалПрДО")
                    ubitRealPravTr = getXmlDecimal(reader, "УбытРеалПравТр")
                    rashLikvOS = getXmlDecimal(reader, "РасхЛиквОС")
                    rashShtraf = getXmlDecimal(reader, "РасхШтраф")
                    rashRinCBDD = getXmlDecimal(reader, "РасхРынЦБДД")
                    rashVnerealVs = getXmlDecimal(reader, "РасхВнеРеалВс")
                } else if (!stoimRealPTFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РасчРасхОпер', 'СтоимРеалПТ'], elements)) {
                    stoimRealPTFound = true
                    stoimRealPTDoSr = getXmlDecimal(reader, "СтоимРеалПТДоСр")
                } else if (!viruchRealPTFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РасчРасхОпер', 'ВыручРеалПТ'], elements)) {
                    viruchRealPTFound = true
                    viruchRealPTDoSr = getXmlDecimal(reader, "ВыручРеалПТДоСр")
                } else if (!ubitRealPT1Found && isCurrentNode(['Документ', 'Прибыль', 'РасчНал', 'РасчРасхОпер', 'УбытРеалПТ1'], elements)) {
                    ubitRealPT1Found = true
                    ubit1Prev269 = getXmlDecimal(reader, "Убыт1Прев269")
                    ubit1Soot269 = getXmlDecimal(reader, "Убыт1Соот269")
                } else if (!documentFound && isCurrentNode(['Документ'], elements)) {
                    documentFound = true
                    kodNO = getXmlValue(reader, 'КодНО')
                    poMestu = getXmlValue(reader, 'ПоМесту')
                } else if (!svNPFound && isCurrentNode(['Документ', 'СвНП'], elements)) {
                    svNPFound = true
                    okved = getXmlValue(reader, 'ОКВЭД')
                } else if (!podpisantFound && isCurrentNode(['Документ', 'Подписант'], elements)) {
                    podpisantFound = true
                    prPodp = getXmlValue(reader, 'ПрПодп')
                } else if (!fioFound && isCurrentNode(['Документ', 'Подписант', 'ФИО'], elements)) {
                    fioFound = true
                    signatorySurname = getXmlValue(reader, 'Фамилия')
                    signatoryFirstName = getXmlValue(reader, 'Имя')
                } else if (!svPredFound && isCurrentNode(['Документ', 'Подписант', 'СвПред'], elements)) {
                    svPredFound = true
                    naimDok = getXmlValue(reader, 'НаимДок')
                } else if (!npJulFound && isCurrentNode(['Документ', 'СвНП', 'НПЮЛ'], elements)) {
                    npJulFound = true
                    naimOrg = getXmlValue(reader, 'НаимОрг')
                    innJulNpJul = getXmlValue(reader, 'ИННЮЛ')
                    kppJulNpJul = getXmlValue(reader, 'КПП')
                } else if (!svReorgJulFound && isCurrentNode(['Документ', 'СвНП', 'НПЮЛ', 'СвРеоргЮЛ'], elements)) {
                    svReorgJulFound = true
                    innJulSvReorgJul = getXmlValue(reader, 'ИННЮЛ')
                    kppSvReorgJul = getXmlValue(reader, 'КПП')
                    formReorg = getXmlValue(reader, 'ФормРеорг')
                } else if (!nalPUAvFound && isCurrentNode(['Документ', 'Прибыль', 'НалПУ', 'НалПУАв'], elements)) {
                    nalPUAvFound = true
                    oktmoNalPUAv = getXmlValue(reader, 'ОКТМО')
                } else if (!nalPUMesFound && isCurrentNode(['Документ', 'Прибыль', 'НалПУ', 'НалПУМес'], elements)) {
                    nalPUMesFound = true
                    oktmoNalPUMes = getXmlValue(reader, 'ОКТМО')
                } else if (!fileFound && isCurrentNode([], elements)) {
                    fileFound = true
                    versForm = getXmlValue(reader, 'ВерсФорм')
                    idFile = getXmlValue(reader, 'ИдФайл')
                    def idFileParts = idFile?.split("_")
                    if (idFileParts.size() >= 3) { // на всякий случай
                        kodNOProm = idFileParts[2]
                    }
                }
            }
            if (reader.endElement) {
                elements[reader.name.localPart] = false
            }
            reader.next()
        }
    } finally {
        reader.close()
    }

    if (naimOrg == null || naimOrg.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Наименование организации (обособленного подразделения)", "НПЮЛ.НаимОрг", "Наименование для титульного листа"))
    }
    // is1_2 = если поле ОКТМО не заполнено в разделе 1.2
    boolean is1_2 = nalPUMesFound && (oktmoNalPUMes == null || oktmoNalPUMes.trim().isEmpty())
    if (oktmoNalPUAv == null || oktmoNalPUAv.trim().isEmpty() || is1_2) {
        logger.log(logLevel, getMessage("Подраздел 1.1" + (is1_2 ? ", 1.2" : ""), "Код по ОКТМО", "НалПУАв.ОКТМО" + (is1_2 ? ", НалПУМес.ОКТМО" : ""), "ОКТМО"))
    }
    if (innJulNpJul == null || innJulNpJul.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "ИНН налогоплательщика", "НПЮЛ.ИННЮЛ", "ИНН"))
    }
    if (kppJulNpJul == null || kppJulNpJul.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "КПП налогоплательщика", "НПЮЛ.КПП", "КПП"))
    }
    if (kodNO == null || kodNO.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Наименование xml файла (кон. налоговый орган) и титульный лист", "Код налогового органа", "Документ.КодНО", "Код налогового органа (кон.)"))
    }
    if (kodNOProm == null || kodNOProm.trim().isEmpty() || "null".equals(kodNOProm)) {
        logger.log(logLevel, getPromMessage("Код налогового органа (пром.)"))
    }
    if (okved == null || okved.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Код вида экономической деятельности и по классификатору ОКВЭД", "СвНП.ОКВЭД", "Код вида экономической деятельности и по классификатору ОКВЭД"))
    }
    if ((formReorg != null && formReorg != '0') && (innJulSvReorgJul == null || innJulSvReorgJul.trim().isEmpty())) {
        logger.log(logLevel, getReorgMessage("Титульный лист", "ИНН реорганизованной организации (обособленного подразделения)", "СвРеоргЮЛ.ИННЮЛ", "ИНН реорганизованного обособленного подразделения"))
    }
    if ((formReorg != null && formReorg != '0') && (kppSvReorgJul == null || kppSvReorgJul.trim().isEmpty())) {
        logger.log(logLevel, getReorgMessage("Титульный лист", "КПП реорганизованной организации (обособленного подразделения)", "СвРеоргЮЛ.КПП", "КПП реорганизованного обособленного подразделения"))
    }
    if (prPodp == null || prPodp.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Признак лица, подписавшего документ", "Подписант.ПрПодп", "Признак лица подписавшего документ"))
    }
    if (signatorySurname == null || signatorySurname.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Фамилия", "Подписант.Фамилия", "Фамилия подписанта"))
    }
    if (signatoryFirstName == null || signatoryFirstName.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Имя", "Подписант.Имя", "Имя подписанта"))
    }
    if (prPodp == '2' && (naimDok == null || naimDok.trim().isEmpty())) {
        logger.log(logLevel, getDocumentMessage("Титульный лист", "Наименование документа, подтверждающего полномочия представителя", "СвПред.НаимДок", "Наименование документа, подтверждающего полномочия представителя"))
    }
    if (poMestu == null || poMestu.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Код места, по которому представляется документ", "Документ.ПоМесту", "Код места, по которому представляется документ"))
    }
    if (stavNalFB == null || stavNalFB.trim().isEmpty()) {
        logger.log(logLevel, getTaxRateMessage("Лист 02 (Расчет налога)", "Ставка налога на прибыль, в федеральный бюджет", "РасчНал.СтавНалФБ", "Ставка налога"))
    }
    if (versForm == null || versForm.trim().isEmpty() || !version.equals(versForm)) {
        logger.log(logLevel, getVersionMessage(versForm, "Файл.ВерсФорм", "Версия формата"))
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (всего)
    if (nalVipl311 != null && nalIschisl != null && nalVipl311 > nalIschisl) {
        logger.log(logLevel, 'Сумма налога, выплаченная за пределами РФ (всего) превышает сумму исчисленного налога на прибыль (всего)!')
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в федеральный бюджет)
    if (nalVipl311FB != null && nalIschislFB != null &&
            nalVipl311FB > nalIschislFB) {
        logger.log(logLevel, 'Сумма налога, выплаченная за пределами РФ (в федеральный бюджет) превышает сумму исчисленного налога на прибыль (в федеральный бюджет)!')
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в бюджет субъекта РФ)
    if (nalVipl311Sub != null && nalIschislSub != null &&
            nalVipl311Sub > nalIschislSub) {
        logger.log(logLevel, 'Сумма налога, выплаченная за пределами РФ (в бюджет субъекта РФ) превышает сумму исчисленного налога на прибыль (в бюджет субъекта РФ)!')
    }

    // Проверки Приложения № 1 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные доходы (всего)»)
    // (ВнеРеалДохПр + ВнеРеалДохСт + ВнеРеалДохБезв + ВнеРеалДохИзл + ВнеРеалДохВРасх + ВнеРеалДохРынЦБДД + ВнеРеалДохКор) < ВнеРеалДохВс
    if (vneRealDohSt != null && vneRealDohBezv != null && vneRealDohIzl != null && vneRealDohVRash != null &&
            vneRealDohRinCBDD != null && vneRealDohCor != null && vneRealDohVs != null &&
            (empty + vneRealDohSt + vneRealDohBezv +
                    vneRealDohIzl + vneRealDohVRash + vneRealDohRinCBDD +
                    vneRealDohCor) > vneRealDohVs) {
        logger.log(logLevel, 'Показатель «Внереализационные доходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Косвенные расходы (всего)»)
    // КосвРасхВс < (Налоги + РасхКапВл10 + РасхКапВл30 + РасхТрудИнв + РасхОргИнв + РасхЗемУчВс + НИОКР)
    if (cosvRashVs != null && nalogi != null && rashCapVl10 != null && rashCapVl30 != null && rashZemUchVs != null &&
            cosvRashVs < (nalogi + rashCapVl10 + rashCapVl30 +
            empty + empty + rashZemUchVs + empty)) {
        logger.log(logLevel, 'Показатель «Косвенные расходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные расходы (всего)»)
    // (РасхВнереалПрДО + РасхВнереалРзрв + УбытРеалПравТр + РасхЛиквОС + РасхШтраф + РасхРынЦБДД) > РасхВнеРеалВс
    if (rashVnerealPrDO != null && ubitRealPravTr != null && rashLikvOS != null &&
            rashShtraf != null && rashRinCBDD != null && rashVnerealVs != null &&
            (rashVnerealPrDO + empty + ubitRealPravTr + rashLikvOS + rashShtraf + rashRinCBDD) > rashVnerealVs) {
        logger.log(logLevel, 'Показатель «Внереализационные расходы (всего)» меньше суммы его составляющих!')
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток) от реализации права требования
    // долга до наступления срока платежа, определенной налогоплательщиком в соответствии с п. 1 статьи 279 НК
    // строка 100 = ВыручРеалПТДоСр = viruchRealPTDoSr
    // строка 120 = СтоимРеалПТДоСр = stoimRealPTDoSr
    // строка 140 = Убыт1Соот269	= ubit1Soot269
    // строка 150 = Убыт1Прев269	= ubit1Prev269
    if (stoimRealPTDoSr != null && viruchRealPTDoSr != null && ubit1Prev269 != null && ubit1Soot269 != null &&
            (stoimRealPTDoSr > viruchRealPTDoSr ?
                    (ubit1Prev269 != stoimRealPTDoSr - viruchRealPTDoSr - ubit1Soot269)
                    : (ubit1Prev269 != 0))) {
        logger.warn('В Приложении 3 к Листу 02 строка 150 неверно указана сумма!')
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток), полученной налогоплательщиком
    // при уступке права требования долга после наступления срока платежа в соответствии с п. 2 статьи 279 НК
    // удалена с 2015 года
}

String getMessage(String place, String printName, String xmlName, String departmentName) {
    return String.format("%s. Обязательный для заполнения атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, departmentName)
}

String getPromMessage(String departmentName) {
    return String.format("Обязательный для заполнения атрибут «%s» в наименовании xml файла не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            departmentName, departmentName)
}

String getTaxRateMessage(String place, String printName, String xmlName, String departmentName) {
    return String.format("%s. Атрибут «%s» (%s) заполнен значением «0»! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения значение атрибута «%s» отсутствовало либо было равно значению «0».",
            place, printName, xmlName, departmentName)
}

String getVersionMessage(String value, String xmlName, String departmentName) {
    return String.format("Обязательный для заполнения атрибут «%s» (%s) заполнен неверно (%s)! Ожидаемое значение «$version». На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения было указано неверное значение атрибута «%s».",
            departmentName, xmlName, value?:'пустое значение', departmentName)
}

String getReorgMessage(String place, String printName, String xmlName, String departmentName) {
    return String.format("%s. Условно обязательный для заполнения (заполнен код формы реорганизации) атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, departmentName)
}

String getDocumentMessage(String place, String printName, String xmlName, String departmentName) {
    return String.format("%s. Условно обязательный для заполнения (Признак лица, подписавшего документ = 2) атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, departmentName)
}

@Field
Boolean isCFOApp2 = null

// Запуск генерации XML.
void generateXML(def xml, boolean showApp2) {

    def empty = 0

    def knd = '1151006'
    def kbk = '18210101011011000110'
    def kbk2 = '18210101012021000110'
    def typeNP = '1'

    def departmentId = declarationData.departmentId
    def reportPeriodId = declarationData.reportPeriodId

    // Параметры подразделения
    def incomeParams = getDepartmentParam()
    def incomeParamsTable = getDepartmentParamTable(incomeParams.record_id.value)

    def reorgFormCode = getRefBookValue(5, incomeParamsTable?.REORG_FORM_CODE?.value)?.CODE?.value
    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = useTaxOrganCodeProm() ? incomeParamsTable?.TAX_ORGAN_CODE_PROM?.value : taxOrganCode
    def okvedCode = getRefBookValue(34, incomeParamsTable?.OKVED_CODE?.value)?.CODE?.value
    def phone = incomeParamsTable?.PHONE?.value
    def name = incomeParamsTable?.NAME?.value
    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp
    def reorgInn = incomeParamsTable?.REORG_INN?.value
    def reorgKpp = incomeParamsTable?.REORG_KPP?.value
    def oktmo = getRefBookValue(96, incomeParamsTable?.OKTMO?.value)?.CODE?.value?.substring(0,8)
    def signatoryId = getRefBookValue(35, incomeParamsTable?.SIGNATORY_ID?.value)?.CODE?.value
    def taxRate = incomeParams?.TAX_RATE?.value ?: 0
    def formatVersion = incomeParams?.FORMAT_VERSION?.value
    def taxPlaceTypeCode = getRefBookValue(2, incomeParamsTable?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatorySurname = incomeParamsTable?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = incomeParamsTable?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = incomeParamsTable?.SIGNATORY_LASTNAME?.value
    def approveDocName = incomeParamsTable?.APPROVE_DOC_NAME?.value
    def approveOrgName = incomeParamsTable?.APPROVE_ORG_NAME?.value

    // Отчётный период.
    def reportPeriod = getReportPeriod()

    /** Предыдущий отчётный период. */
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId)

    /** XML декларации за предыдущий отчетный период. */
    def readerOld = getXmlStreamReader(prevReportPeriod?.id, departmentId, false, true)

    /** НалВыпл311ФБ за предыдущий отчетный период. Код строки декларации 250. */
    def nalVipl311FBOld = 0
    /** НалВыпл311Суб за предыдущий отчетный период. Код строки декларации 260. */
    def nalVipl311SubOld = 0
    /** НалИсчислФБ. Код строки декларации 190. */
    def nalIschislFBOld = 0
    /** НалИсчислСуб. Столбец «Сумма налога». */
    def nalIschislSubOld = 0
    /** АвПлатМесСуб. Код строки декларации 310. */
    def avPlatMesSubOld = 0
    /** АвНачислСуб. Код строки декларации 230. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
    def avNachislSubOld = 0
    /** АвПлатМесФБ. Код строки декларации 300. */
    def avPlatMesFBOld = 0

    def nalDivNeRFPredOldMap = [:]
    def nalDivNeRFOldMap = [:]
    def nalNachislPredOldMap = [:]
    def nalNachislPoslOldMap = [:]
    if (readerOld != null) {
        def elements = [:]
        def raschNalFound = false
        try {
            while(readerOld.hasNext()) {
                if (readerOld.startElement){
                    elements[readerOld.name.localPart] = true
                    if (!raschNalFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал'], elements)) {
                        raschNalFound = true // чтобы второй раз не искал
                        nalVipl311FBOld = getXmlDecimal(readerOld, "НалВыпл311ФБ") ?: 0
                        nalVipl311SubOld = getXmlDecimal(readerOld, "НалВыпл311Суб") ?: 0
                        nalIschislFBOld = getXmlDecimal(readerOld, "НалИсчислФБ") ?: 0
                        nalIschislSubOld = getXmlDecimal(readerOld, "НалИсчислСуб") ?: 0
                        avPlatMesSubOld = getXmlDecimal(readerOld, "АвПлатМесСуб") ?: 0
                        avNachislSubOld = getXmlDecimal(readerOld, "АвНачислСуб") ?: 0
                        avPlatMesFBOld = getXmlDecimal(readerOld, "АвПлатМесФБ") ?: 0
                    } else if (isCurrentNode(['Документ', 'Прибыль', 'НалДохСтав'], elements)) {
                        int vidDohod = getXmlDecimal(readerOld, "ВидДоход")?.intValue() ?: 0
                        nalDivNeRFPredOldMap[vidDohod] = getXmlDecimal(readerOld, 'НалДивНеРФПред') ?: 0
                        nalDivNeRFOldMap[vidDohod] = getXmlDecimal(readerOld, 'НалДивНеРФ') ?: 0
                        nalNachislPredOldMap[vidDohod] = getXmlDecimal(readerOld, 'НалНачислПред') ?: 0
                        nalNachislPoslOldMap[vidDohod] = getXmlDecimal(readerOld, 'НалНачислПосл') ?: 0
                    }
                }
                if (readerOld.endElement){
                    elements[readerOld.name.localPart] = false
                }
                readerOld.next()
            }
        } finally {
            readerOld.close()
        }
    }

    // Налоговый период.
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.getTaxPeriod().getId()) : null)

    /** Признак налоговый ли это период. */
    def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

    /** Признак первый ли это отчетный период. */
    def isFirstPeriod = (reportPeriod != null && reportPeriod.order == 1)

    /** Принятая декларация за период «9 месяцев» предыдущего налогового периода. */
    def reader9month = null
    /** Используемые поля декларации за период «9 месяцев» предыдущего налогового периода. */
    /** АвПлатУпл1КвФБ. Код строки декларации 330. */
    def avPlatUpl1CvFB9month = 0
    /** АвПлатУпл1КвСуб. Код строки декларации 340. */
    def avPlatUpl1CvSubB9month = 0

    if (isFirstPeriod) {
        reader9month = getXmlStreamReader(getReportPeriod9month(prevReportPeriod)?.id, departmentId, true, true)
        if (reader9month != null) {
            def elements = [:]
            def raschNalFound = false
            try {
                while (reader9month.hasNext()) {
                    if (reader9month.startElement) {
                        elements[reader9month.name.localPart] = true
                        if (!raschNalFound && isCurrentNode(['Документ', 'Прибыль', 'РасчНал'], elements)) {
                            raschNalFound = true // чтобы второй раз не искал
                            avPlatUpl1CvFB9month = getXmlDecimal(reader9month, "АвПлатУпл1КвФБ") ?: BigDecimal.ZERO
                            avPlatUpl1CvSubB9month = getXmlDecimal(reader9month, "АвПлатУпл1КвСуб") ?: BigDecimal.ZERO
                        }
                    }
                    if (reader9month.endElement){
                        elements[reader9month.name.localPart] = false
                    }
                    reader9month.next()
                }
            } finally {
                reader9month.close()
            }
        }
    }

    // Данные налоговых форм.

    def formDataCollection = getAcceptedFormDataSources()

    /** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
    def dataRowsComplexIncome = getDataRows(formDataCollection, 302)

    /** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
    def dataRowsSimpleIncome = getDataRows(formDataCollection, 305)

    /** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
    def dataRowsSimpleIncome_old = getDataRows(formDataCollection, 301)
    if (dataRowsSimpleIncome == null) {
        dataRowsSimpleIncome = dataRowsSimpleIncome_old
    } else if (dataRowsSimpleIncome_old != null) {
        logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Консолидация произведена из «%s».",
                formTypeService.get(305).name, formTypeService.get(301)?.name, formTypeService.get(305)?.name)
    }

    /** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
    def dataRowsComplexConsumption = getDataRows(formDataCollection, 303)

    /** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
    def dataRowsSimpleConsumption = getDataRows(formDataCollection, 304)

    /** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
    def dataRowsSimpleConsumption_old = getDataRows(formDataCollection, 310)
    if (dataRowsSimpleConsumption == null) {
        dataRowsSimpleConsumption = dataRowsSimpleConsumption_old
    } else if (dataRowsSimpleConsumption_old != null) {
        logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Консолидация произведена из «%s».",
                formTypeService.get(310).name, formTypeService.get(304)?.name, formTypeService.get(310)?.name)
    }

    /** Сводная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
    def dataRowsAdvance = getDataRows(formDataCollection, getAdvanceTypeId())

    /** Сведения для расчёта налога с доходов в виде дивидендов. */
    def dataRowsDividend = getDataRows(formDataCollection, 414)

    /** Расчет налога на прибыль с доходов, удерживаемого налоговым агентом. */
    /** либо */
    /** Сведения о дивидендах, выплаченных в отчетном квартале. */
    def dataRowsTaxAgent = getDataRows(formDataCollection, 416)

    /** Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика. */
    def dataRowsTaxSum = getDataRows(formDataCollection, 412)

    /** форма «Остатки по начисленным авансовым платежам». */
    def dataRowsRemains = getDataRows(formDataCollection, 309)

    /** Сведения о суммах налога на прибыль, уплаченного Банком за рубежом */
    def dataRowsSum = getDataRows(formDataCollection, 421)

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов"
    def dataRowsApp2
    if (showApp2) {
        dataRowsApp2 = getDataRows(formDataCollection, 415)
        isCFOApp2 = false

        // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ)."
        def dataRowsApp2_old = getDataRows(formDataCollection, 418)
        if (dataRowsApp2 == null) {
            isCFOApp2 = true
            dataRowsApp2 = dataRowsApp2_old
        } else if (dataRowsApp2_old != null) {
            logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Консолидация произведена из «%s».",
                    formTypeService.get(415).name, formTypeService.get(418)?.name, formTypeService.get(415)?.name)
        }
    }

    // Расчет значений для текущей декларации.

    // Период
    def period = 0
    if (reorgFormCode != null) {
        period = 50
    } else if (reportPeriod.order != null) {
        def values = [21, 31, 33, 34]
        period = values[reportPeriod.order - 1]
    }


    /** ВыручРеалТов. Код строки декларации 180. */
    def viruchRealTov = empty
    /** ДохДоговДУИ. Код строки декларации 210. */
    def dohDolgovDUI = empty
    /** ДохДоговДУИ_ВнР. Код строки декларации 211. */
    def dohDolgovDUI_VnR = empty
    /** УбытОбОбслНеобл. Код строки декларации 201. */
    def ubitObObslNeobl = empty
    /** УбытДоговДУИ. Код строки декларации 230. */
    def ubitDogovDUI = empty
    /** УбытПрошПер. Код строки декларации 301. */
    def ubitProshPer = empty
    /** СумБезнадДолг. Код строки декларации 302. */
    def sumBeznalDolg = empty
    /** УбытПриравнВс. Код строки декларации 300. */
    def ubitPriravnVs = ubitProshPer + sumBeznalDolg

    // Приложение № 3 к Листу 02
    /** КолОбРеалАИ. Код строки декларации 010. Код вида дохода = 10. */
    def colObRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10]))
    /** КолОбРеалАИУб. Код строки декларации 020. Код вида дохода = 20. */
    def colObRealAIUb = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [20]))
    /** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10910. */
    def viruchRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10910]))
    /** ПрибРеалАИ. Код строки декларации 040. Код вида дохода = 10920. */
    def pribRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10920]))
    /** УбытРеалАИ. Код строки декларации 060. Код вида расхода = 21420. */
    def ubitRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21420]))
    /** ЦенРеалПрЗУ. Код строки декларации 240. Код вида дохода = 10950. */
    def cenRealPrZU = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10950]))
    /** УбытРеалПрЗУ. Код строки декларации 260. С 1 квартала 2016 года не заполняется. */
    def ubitRealPrZU = empty
    /** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10970. */
    def viruchRealPTDoSr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10970]))
    /** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. Не заполняется с 2015 года. */
    def viruchRealPTPosSr = getLong(0)
    /** Убыт1Соот269. Код строки декларации 140. Код вида расхода = 21060. */
    def ubit1Soot269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21060]))
    /** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21070. */
    def ubit1Prev269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21070]))
    /** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
    def ubit2RealPT = empty
    /** Убыт2ВнРасх. Код строки декларации 170. Код вида расхода = 22700. */
    def ubit2VnRash = empty
    // Приложение № 3 к Листу 02 - конец

    /** ПрПодп. */
    def prPodp = signatoryId
    /** ВырРеалТовСоб. Код строки декларации 011. */
    def virRealTovSob = getVirRealTovSob(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10940, 10960, 10980. */
    def virRealImPrav = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10940, 10960, 10980]))
    /** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10930. */
    def virRealImProch = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10930]))
    /** ВырРеалВс. Код строки декларации 010. */
    def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
    /** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11040, 11050, 11060, 11070, 11080, 11090, 11100, 11110. */
    def virRealCBVs = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11040, 11050, 11060, 11070, 11080, 11090, 11100, 11110]))
    /** ВырРеалПред. Код строки декларации 023. */
    def virRealPred = empty
    /** ВыручОп302Ит. Код строки декларации 340. Строка 030 + строка 100 + строка 110 + строка 180 + (строка 210 – строка 211) + строка 240. */
    def viruchOp302It = viruchRealAI + viruchRealPTDoSr + viruchRealPTPosSr + viruchRealTov + dohDolgovDUI - dohDolgovDUI_VnR + cenRealPrZU

    /** СерЛицНедр. Лицензия: серия */
    def serLicNedr = empty
    /** НомЛицНедр. Лицензия: номер */
    def nomLicNedr = empty
    /** ВидЛицНедр. Лицензия: вид морского месторождения */
    def vidLicNedr = empty
    /** НомМорМест. Лицензия: номер морского месторождения */
    def nomMorMest = empty

    /** ДохРеал, ВырРеалИтог. */
    def dohReal = virRealVs + virRealCBVs + virRealPred + viruchOp302It
    /** ДохВнереал. Код строки декларации 100. */
    def dohVnereal = getDohVnereal(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ПрямРасхРеал. Код строки декларации 010. */
    def pramRashReal = empty
    /** ПрямРасхТоргВс. Код строки декларации 020. */
    def pramRashTorgVs = empty
    /** КосвРасхВс. Код строки декларации 040. */
    def cosvRashVs = getCosvRashVs(dataRowsComplexConsumption, dataRowsSimpleConsumption)
    /** РасхВнереалВС. Строка 200. */
    def rashVnerealVs = getRashVnerealVs(dataRowsComplexConsumption, dataRowsSimpleConsumption)
    /** РасхВнереал. Строка 200 + строка 300. */
    def rashVnereal = rashVnerealVs + ubitPriravnVs
    /** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21400. */
    def ostStRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21400]))
    /** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21080, 21100, 21390. */
    def realImushPrav = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21080, 21100, 21390]))
    /** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21410. */
    def priobrRealImush = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21410]))
    /* АктивРеалПред. Код строки декларации 061. */
    def activRealPred = empty
    /** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21230, 21240, 21250, 21260, 21270, 21280, 21290, 21300, 21310. */
    def priobrRealCB = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21230, 21240, 21250, 21260, 21270, 21280, 21290, 21300, 21310]))
    /** СумОтклЦен. Код строки декларации 071. Код вида расходов = 21320, 21330. */
    def sumOtklCen = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21320, 21330]))
    /** ПриобРеалЦБОрг. Код строки декларации 072. Расходы, связанные с приобритением и реализацией (выбытием, в том числе погашением) ценных бумаг, обращающихся на организованном рынке ценных бумаг */
    def priobRealCBOrg = empty
    /** СумОтклЦенОрг. Код строки декларации 073. Суммы отклонения от максимальной (расчетной) цены */
    def sumOtklCenOrg = empty

    /** УбытПрошОбсл. Код строки декларации 090. */
    def ubitProshObsl = empty
    /** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21050. */
    def stoimRealPTDoSr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21050]))
    /** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. Не заполняется с 2015 года. */
    def stoimRealPTPosSr = getLong(0)
    /** РасхРеалТов. Код строки декларации 190. */
    def rashRealTov = empty
    /** РасхДоговДУИ. Код строки декларации 220. */
    def rashDolgovDUI = empty
    /** РасхДоговДУИ_ВнР. Код строки декларации 221. */
    def rashDolgovDUI_VnR = empty
    /** НеВозЗатрПрЗУ. Код строки декларации 250. С 1 квартала 2016 года не заполняется. */
    def neVozZatrPrZU = empty
    /** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
    def rashOper32 = ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr + rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + neVozZatrPrZU
    /** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21110. */
    def ubitRealAmIm = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21110]))
    /** УбытРеалЗемУч. Код строки декларации 110. */
    def ubitRealZemUch = empty
    /** НадбПокПред. Код строки декларации 120. */
    def nadbPokPred = empty
    /** РасхУмРеал, РасхПризнИтого. Код строки декларации 130. */
    def rashUmReal = pramRashReal + pramRashTorgVs + cosvRashVs + realImushPrav +
            priobrRealImush + activRealPred + priobrRealCB + rashOper32 + ubitProshObsl +
            ubitRealAmIm + ubitRealZemUch + nadbPokPred
    /** Убытки, УбытОп302. Код строки декларации 360. Cтрока 060 + строка 150 + строка 160 + строка 201+ строка 230 + строка 260. */
    def ubitki = ubitRealAI + ubit1Prev269 + ubit2RealPT + ubitObObslNeobl + ubitDogovDUI + ubitRealPrZU
    /** ПрибУб. */
    def pribUb = dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki
    /** ДохИсклПриб. */
    def dohIsklPrib = getDohIsklPrib(dataRowsComplexIncome, dataRowsSimpleIncome)
    // НалБаза строка 60 - строка 70 - строка 80 - строка 90 - строка 400 (Приложение №2 к Листу 02)
    def nalBaza = pribUb - dohIsklPrib - 0 - 0 - 0
    /** НалБазаИсч, НалБазаОрг. */
    def nalBazaIsch = getNalBazaIsch(nalBaza, 0)
    /** НалИсчислФБ. Код строки декларации 190. */
    def nalIschislFB = getNalIschislFB(nalBazaIsch, taxRate)
    /** НалИсчислСуб. Код строки декларации 200. Столбец «Сумма налога». */
    def nalIschislSub = getTotalFromForm(dataRowsAdvance, 'taxSum')
    /** НалИсчисл. Код строки декларации 180. */
    def nalIschisl = nalIschislFB + nalIschislSub
    // НалВыпл311. Код строки декларации 240.
    def nalVipl311
    if (!sourceCheck(false, LogLevel.WARNING)) {
        nalVipl311 = 0
    } else {
        nalVipl311 = getAliasFromForm(dataRowsSum, 'taxSum', 'SUM_TAX')
    }
    /** НалВыпл311ФБ. Код строки декларации 250. */
    def nalVipl311FB = getLong(nalVipl311 * 2 / 20)
    /** НалВыпл311Суб. Код строки декларации 260. */
    def nalVipl311Sub = getLong(nalVipl311 - nalVipl311FB)

    /** АвПлатМесФБ. Код строки декларации 300. */
    def avPlatMesFB = isTaxPeriod ? empty : (nalIschislFB - (!isFirstPeriod ? nalIschislFBOld : 0))
    /** АвНачислФБ. Код строки декларации 220. */
    def avNachislFB
    if (isFirstPeriod) {
        if (reader9month != null) {
            avNachislFB = getLong(avPlatUpl1CvFB9month)
        } else {
            avNachislFB = getTotalFromForm(dataRowsRemains, 'sum1')
        }
    } else {
        avNachislFB = nalIschislFBOld - nalVipl311FBOld + avPlatMesFBOld
    }
    /** АвПлатМесСуб. Код строки декларации 310. */
    def avPlatMesSub = isTaxPeriod ? empty : (nalIschislSub - (isFirstPeriod ? 0 : nalIschislSubOld))
    /** АвПлатМес. */
    def avPlatMes = isTaxPeriod ? empty : (avPlatMesFB + avPlatMesSub)
    /** АвПлатУпл1КвФБ. */
    def avPlatUpl1CvFB = (reportPeriod != null && reportPeriod.order == 3 ? avPlatMesFB : empty)
    /** АвПлатУпл1КвСуб. Код строки декларации 340. */
    def avPlatUpl1CvSub = (reportPeriod != null && reportPeriod.order == 3 ? avPlatMesSub : empty)
    /** АвПлатУпл1Кв. */
    def avPlatUpl1Cv = (reportPeriod != null && reportPeriod.order == 3 ? avPlatUpl1CvFB + avPlatUpl1CvSub : empty)
    /** АвНачислСуб. Код строки декларации 230. 200 - 260 + 310. */
    def avNachislSub
    if (isFirstPeriod) {
        if (reader9month != null) {
            avNachislSub = getLong(avPlatUpl1CvSubB9month)
        } else {
            avNachislSub = getTotalFromForm( dataRowsRemains, 'sum2')
        }
    } else {
        avNachislSub =  getLong(nalIschislSubOld - nalVipl311SubOld + avPlatMesSubOld)
    }
    /** АвНачисл. Код строки декларации 210. */
    def avNachisl = avNachislFB + avNachislSub
    /** НалДоплФБ. Код строки декларации 270. */
    def nalDoplFB = getNalDopl(nalIschislFB, avNachislFB, nalVipl311FB)
    /** НалДоплСуб. Код строки декларации 271. */
    def nalDoplSub = getTotalFromForm(dataRowsAdvance, 'taxSumToPay')
    /** НалУменФБ. Код строки декларации 280. */
    def nalUmenFB = getNalUmen(avNachislFB, nalVipl311FB, nalIschislFB)
    /** НалУменСуб. Код строки декларации 281. */
    def nalUmenSub = getTotalFromForm(dataRowsAdvance, 'taxSumToReduction')
    /** ОтклВырЦБОбр. Код строки декларации 021. Код вида дохода = 11120, 11130. */
    def otklVirCBOrb = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11120, 11130]))
    /** ОтклВырЦБНеОбр. Код строки декларации 022. Код вида дохода = 11140, 11150. */
    def otklVirCBNeObr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11140, 11150]))
    /** ВнеРеалДохВс. Код строки декларации 100. */
    def vneRealDohVs = dohVnereal
    /** ВнеРеалДохСт. Код строки декларации 102. Код вида дохода = 16300. */
    def vneRealDohSt = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [16300]))
    /** ВнеРеалДохБезв. Код строки декларации 103. Код вида дохода = 16150. */
    def vneRealDohBezv = getLong(getSimpleIncomeSumRows8(dataRowsSimpleIncome, [16150]))
    /** ВнеРеалДохИзл. Код строки декларации 104. Код вида дохода = 16400, 16410. */
    def vneRealDohIzl = getLong(getSimpleIncomeSumRows8(dataRowsSimpleIncome, [16400, 16410]))
    /** ВнеРеалДохВРасх. Код строки декларации 105. Код вида дохода = 16420. */
    def vneRealDohVRash = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [16420]))
    /** ВнеРеалДохРынЦБДД. Код строки декларации 106. Код вида дохода = 16970, 16980, 16990, 17000, 17010, 17020, 17030, 17040, 17060, 17070, 17080, 17090, 17100, 17110, 17120. */
    def vneRealDohRinCBDD = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [16970, 16980, 16990, 17000, 17010, 17020, 17030, 17040, 17060, 17070, 17080, 17090, 17100, 17110, 17120]))
    /** ВнеРеалДохКор. Код строки декларации 107. Код вида дохода = 19000, 19030, 19060, 19090, 19120, 19150, 19180, 19210, 19240, 19270, 19300, 19330, 19360, 19390, 19420, 19450, 19480, 19510, 19540, 19570. */
    def vneRealDohCor = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [19000, 19030, 19060, 19090, 19120, 19150, 19180, 19210, 19240, 19270, 19300, 19330, 19360, 19390, 19420, 19450, 19480, 19510, 19540, 19570]))
    /** Налоги. Код строки декларации 041. */
    def nalogi = getNalogi(dataRowsComplexConsumption, dataRowsSimpleConsumption)
    /** РасхКапВл10. Код строки декларации 042. Код вида расхода = 20490. */
    def rashCapVl10 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20490]))
    /** РасхКапВл30. Код строки декларации 043. Код вида расхода = 20500. */
    def rashCapVl30 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20500]))
    /** РасхЗемУч30пр. Код строки декларации 049. Код вида расхода = 20970. */
    def rashZemUch30pr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20970]))
    /** РасхЗемУчСрокАр. Код строки декларации 051. Код вида расхода = 20980. */
    def rashZemUchSrocAr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20980]))
    /** РасхЗемУчВс. Код строки декларации 047. Код вида дохода = 20970, 20980. */
    def rashZemUchVs = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20970, 20980]))

    /** СумАмортПерЛ. Код строки декларации 131. Код вида расхода = 20470, 20510. */
    def sumAmortPerL = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20470, 20510]))
    /** СумАмортПерНмАЛ. Код строки декларации 132. Код вида расхода = 20470. */
    def sumAmortPerNmAL = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20470]))
    /** РасхВнереалПрДО. Код строки декларации 201. */
    def rashVnerealPrDO = getLong(getRashVnerealPrDO(dataRowsComplexConsumption, dataRowsSimpleConsumption))
    /** РасхЛиквОС. Код строки декларации 204. Код вида расхода = 26360. */
    def rashLikvOS = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [26360]))
    /** РасхШтраф. Код строки декларации 205. */
    def rashShtraf = getRashShtraf(dataRowsSimpleConsumption)
    /** РасхРынЦБДД. Код строки декларации 206. Код вида расхода = 26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890. */
    def rashRinCBDD = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890]))

    // Приложение к налоговой декларации
    /** СвЦелСред - блок. Табл. 36. Алгоритмы заполнения отдельных атрибутов «Приложение к налоговой декларации»  декларации Банка по налогу на прибыль. */
    def svCelSred = new HashMap()
    if (dataRowsComplexConsumption != null) {
        // 677, 700, 890
        def map = [
                677 : [20520],
                700 : [20480],
                890 : [20850]
        ]
        map.each { id, codes ->
            def result = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, codes))
            if (result != 0) {
                svCelSred[id] = result
            }
        }
    }
    if (dataRowsSimpleConsumption != null) {
        // 780, 790, 811, 812, 813
        def map = [
                780 : [20330],
                790 : [20320],
                811 : [20450],
                812 : [20400, 20410],
                813 : [20370, 20380]
        ]
        map.each { id, codes ->
            def result = getLong(getCalculatedSimpleConsumption(dataRowsSimpleConsumption, codes))
            if (result != 0) {
                svCelSred[id] = result
            }
        }
        // 940, 950
        map = [
                940 : [26500, 26510],
                950 : [26520]
        ]
        map.each { id, codes ->
            def result = getLong(getSimpleConsumptionSumRows8(dataRowsSimpleConsumption, codes))
            if (result != 0) {
                svCelSred[id] = result
            }
        }
    }
    // Приложение к налоговой декларации - конец

    if (xml == null) {
        return
    }

    // Формирование XML'ки.

    MarkupBuilder builder = new MarkupBuilder(xml)

    builder.Файл(
            ИдФайл : generateXmlFileId(taxOrganCodeProm, taxOrganCode),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion){

        // Титульный лист
        Документ(
                КНД :  knd,
                ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                Период : period,
                ОтчетГод : (taxPeriod != null ? taxPeriod.year : empty),
                КодНО : taxOrganCode,
                НомКорр : reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту : taxPlaceTypeCode) {

            СвНП(
                    [ОКВЭД : okvedCode] + (phone ? [Тлф : phone] : [:])) {

                НПЮЛ(
                        НаимОрг : name,
                        ИННЮЛ : inn,
                        КПП : kpp) {

                    if (reorgFormCode != null && !reorgFormCode.equals("")) {
                        СвРеоргЮЛ([ФормРеорг: reorgFormCode] +
                                (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ?
                                        [ИННЮЛ: reorgInn, КПП: reorgKpp] : [])
                        )
                    }
                }
            }

            Подписант(ПрПодп : prPodp) {
                ФИО(
                        [Фамилия : signatorySurname, Имя : signatoryFirstName] +
                                (signatoryLastName != null ? [Отчество : signatoryLastName] : [:]))
                if (prPodp != 1) {
                    СвПред(
                            [НаимДок : approveDocName] +
                                    (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                    )
                }
            }
            // Титульный лист - конец

            Прибыль() {
                НалПУ() {
                    // Раздел 1. Подраздел 1.1
                    // 0..n // всегда один
                    НалПУАв(ОКТМО: oktmo) {
                        def nalPu = (nalDoplFB != 0 ? nalDoplFB : -nalUmenFB)
                        // 0..1
                        ФедБдж(
                                КБК: kbk,
                                НалПУ: nalPu)

                        nalPu = 0
                        if (dataRowsAdvance != null) {
                            // получение строки подразделения "ЦА", затем значение столбца «Сумма налога к доплате [100]»
                            def rowForNalPu = getDataRow(dataRowsAdvance, 'ca')
                            // налПу = строка 070, если строка 070 == 0, то строка 080, если строка 080 == 0, то 0
                            nalPu = (rowForNalPu != null ?
                                    (rowForNalPu.taxSumToPay ?: (- (rowForNalPu.taxSumToReduction ?: 0))) : 0)
                            if (nalPu == null) {
                                nalPu = 0
                            }
                        }

                        // 0..1
                        СубБдж(
                                КБК : kbk2,
                                НалПУ : nalPu)
                    }
                    // Раздел 1. Подраздел 1.1 - конец

                    // Раздел 1. Подраздел 1.2
                    if (period != 34 && period != 50) {
                        // 0..n
                        // КвИсчислАв : '00',
                        НалПУМес(ОКТМО : oktmo) {
                            def list02Row300 = avPlatMesFB
                            def avPlat1 = roundValue(list02Row300 / 3, 0)
                            def avPlat2 = avPlat1
                            def avPlat3 = getLong(list02Row300 - avPlat1 - avPlat2)
                            // 0..1
                            ФедБдж(
                                    КБК : kbk,
                                    АвПлат1 : avPlat1,
                                    АвПлат2 : avPlat2,
                                    АвПлат3 : avPlat3)

                            avPlat1 = empty
                            avPlat2 = empty
                            avPlat3 = empty
                            if (!isTaxPeriod && dataRowsAdvance != null) {
                                // получение строки подразделения "ЦА", затем значение столбца «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)»
                                def rowForAvPlat = getDataRow(dataRowsAdvance, 'ca')
                                def avPlats = getAvPlats(rowForAvPlat, reportPeriod)
                                avPlat1 = avPlats[0]
                                avPlat2 = avPlats[1]
                                avPlat3 = avPlats[2]
                            }
                            // 0..1
                            СубБдж(
                                    КБК : kbk2,
                                    АвПлат1 : avPlat1,
                                    АвПлат2 : avPlat2,
                                    АвПлат3 : avPlat3)
                        }
                    }
                    // Раздел 1. Подраздел 1.2 - конец

                    if (dataRowsTaxSum != null) {
                        // Раздел 1. Подраздел 1.3
                        dataRowsTaxSum.sort {
                            getRefBookValue(24, it.paymentType)?.CODE?.value + it.okatoCode + it.budgetClassificationCode
                        }
                        dataRowsTaxSum.each { row ->
                            // 0..n
                            НалПУПроц(
                                    ВидПлат : getRefBookValue(24, row.paymentType)?.CODE?.value,
                                    ОКТМО : row.okatoCode?.substring(0,8),
                                    КБК : row.budgetClassificationCode) {

                                // 0..n
                                УплСрок(
                                        Срок : (row.dateOfPayment != null ? row.dateOfPayment.format('dd.MM.yyyy') : empty),
                                        НалПУ : getLong(row.sumTax))
                            }
                        }
                    }
                    // Раздел 1. Подраздел 1.3 - конец
                }

                // Лист 02
                // 0..4
                РасчНал(
                        ТипНП : typeNP,
//                        СерЛицНедр : serLicNedr,
//                        НомЛицНедр : nomLicNedr,
//                        ВидЛицНедр : vidLicNedr,
//                        НомМорМест : nomMorMest,
                        ДохРеал : dohReal,
                        ДохВнереал : dohVnereal,
                        РасхУмРеал : rashUmReal,
                        РасхВнереал : rashVnereal,
                        Убытки : ubitki,
                        ПрибУб : pribUb,
                        ДохИсклПриб : dohIsklPrib,
                        ПрибБРСт0 : empty,
                        СумЛьгот : empty,
                        НалБаза : nalBaza,
                        УбытУмНБ : empty,
                        НалБазаИсч : nalBazaIsch,
                        НалБазаИсчСуб : empty,
                        СтавНалВсего : empty,
                        СтавНалФБ : taxRate,
                        СтавНалСуб : empty,
                        СтавНалСуб284 : empty,
                        НалИсчисл : nalIschisl,
                        НалИсчислФБ : nalIschislFB,
                        НалИсчислСуб : nalIschislSub,
                        АвНачисл : avNachisl,
                        АвНачислФБ : avNachislFB,
                        АвНачислСуб : avNachislSub,
                        НалВыпл311 : nalVipl311,
                        НалВыпл311ФБ : nalVipl311FB,
                        НалВыпл311Суб : nalVipl311Sub,
                        НалДоплФБ : nalDoplFB,
                        НалДоплСуб : nalDoplSub,
                        НалУменФБ : nalUmenFB,
                        НалУменСуб : nalUmenSub,
                        АвПлатМес : avPlatMes,
                        АвПлатМесФБ : avPlatMesFB,
                        АвПлатМесСуб : avPlatMesSub,
                        АвПлатУпл1Кв : avPlatUpl1Cv,
                        АвПлатУпл1КвФБ : avPlatUpl1CvFB,
                        АвПлатУпл1КвСуб : avPlatUpl1CvSub) {
                    // Лист 02 - конец

                    // Приложение № 1 к Листу 02
                    // 0..1
                    ДохРеалВнеРеал(ТипНП : typeNP) {
                        // 0..1
                        ДохРеал(
                                ВырРеалПред : virRealPred,
                                ВырРеалОпер32 : viruchOp302It,
                                ВырРеалИтог : dohReal) {
                            // 0..1
                            ВырРеал(
                                    ВырРеалВс : virRealVs,
                                    ВырРеалТовСоб : virRealTovSob,
                                    ВырРеалТовПок : empty,
                                    ВырРеалИмПрав : virRealImPrav,
                                    ВырРеалИмПроч : virRealImProch)
                            // 0..1
                            ВырРеалЦБ(
                                    ВырРеалЦБВс : virRealCBVs,
                                    ОтклВырЦБОбр : otklVirCBOrb,
                                    ОтклВырЦБНеОбр : otklVirCBNeObr)
                            // 0..1
                            ВырРеалЦБОбр(
                                    ВырРеалЦБВс : empty,
                                    ОтклВырЦБМин : empty)
                        }
                        // 0..1
                        ДохВнеРеал(
                                ВнеРеалДохВс : vneRealDohVs,
                                ВнеРеалДохПр : empty,
                                ВнеРеалДохСт : vneRealDohSt,
                                ВнеРеалДохБезв : vneRealDohBezv,
                                ВнеРеалДохИзл : vneRealDohIzl,
                                ВнеРеалДохВРасх : vneRealDohVRash,
                                ВнеРеалДохРынЦБДД : vneRealDohRinCBDD,
                                ВнеРеалДохКор : vneRealDohCor)
                    }
                    // Приложение № 1 к Листу 02 - конец

                    // Приложение № 2 к Листу 02
                    // 0..1
                    РасхРеалВнеРеал(
                            ТипНП : typeNP
//                            СерЛицНедр: empty,
//                            НомЛицНедр: empty,
//                            ВидЛицНедр : empty,
//                            НомМорМест : empty
                    ) {
                        // 0..1
                        РасхРеал(
                                ПрямРасхРеал : pramRashReal,
                                РеалИмущПрав : realImushPrav,
                                ПриобрРеалИмущ : priobrRealImush,
                                АктивРеалПред : activRealPred,
                                ПриобРеалЦБ : priobrRealCB,
                                СумОтклЦен : sumOtklCen,
                                ПриобРеалЦБОрг : priobRealCBOrg,
                                СумОтклЦенОрг : sumOtklCenOrg,
                                РасхОпер32 : rashOper32,
                                УбытПрошОбсл : ubitProshObsl,
                                УбытРеалАмИм : ubitRealAmIm,
                                УбытРеалЗемУч : ubitRealZemUch,
                                НадбПокПред : nadbPokPred,
                                РасхПризнИтого : rashUmReal) {

                            // 0..1
                            ПрямРасхТорг(ПрямРасхТоргВс : pramRashTorgVs)
                            // 0..1
                            КосвРасх(
                                    КосвРасхВс : cosvRashVs,
                                    Налоги : nalogi,
                                    РасхКапВл10 : rashCapVl10,
                                    РасхКапВл30 : rashCapVl30,
                                    РасхТрудИнв : empty,
                                    РасхОргИнв : empty,
                                    РасхЗемУчВс : rashZemUchVs,
                                    РасхЗемУчСрокНП : empty,
                                    РасхЗемУч30пр : rashZemUch30pr,
                                    РасхЗемУчСрокРас : empty,
                                    РасхЗемУчСрокАр : rashZemUchSrocAr,
                                    НИОКР : empty,
                                    НИОКРнеПолРез : empty,
                                    НИОКРПер : empty,
                                    НИОКРПерНеРез : empty)
                        }
                        // 0..1
                        СумАморт(
                                СумАмортПерЛ : sumAmortPerL,
                                СумАмортПерНмАЛ : sumAmortPerNmAL,
                                СумАмортПерН : empty,
                                СумАмортПерНмАН : empty,
                                МетодНачАморт : '1')
                        // 0..1
                        РасхВнеРеал(
                                РасхВнеРеалВс : rashVnerealVs,
                                РасхВнереалПрДО : rashVnerealPrDO,
                                РасхВнереалРзрв : empty,
                                УбытРеалПравТр : empty, // не заполняется с 2015
                                РасхЛиквОС : rashLikvOS,
                                РасхШтраф : rashShtraf,
                                РасхРынЦБДД : rashRinCBDD)
                        // 0..1
                        УбытПриравн(
                                УбытПриравнВс : ubitPriravnVs,
                                УбытПрошПер : ubitProshPer,
                                СумБезнадДолг : sumBeznalDolg)
                        КорНБЛиш(КорНБЛишВс : empty) {
                            // КорНБЛишГод(Год: year, КорНБЛишВс: korNBlishVs)
                        }
                    }
                    // Приложение № 2 к Листу 02 - конец

                    // Приложение № 3 к Листу 02
                    // 0..1
                    РасчРасхОпер(
                            ТипНП : typeNP,
//                            СерЛицНедр : empty,
//                            НомЛицНедр : empty,
//                            ВидЛицНедр : empty,
//                            НомМорМест : empty,
                            КолОбРеалАИ :colObRealAI,
                            КолОбРеалАИУб : colObRealAIUb,
                            ВыручРеалАИ : viruchRealAI,
                            ОстСтРеалАИ : ostStRealAI,
                            ПрибРеалАИ : pribRealAI,
                            УбытРеалАИ : ubitRealAI,
                            ВыручРеалТов : viruchRealTov,
                            РасхРеалТов : empty,
                            УбытОбОбсл : empty,
                            УбытОбОбслНеобл : ubitObObslNeobl,
                            ДохДоговДУИ : dohDolgovDUI,
                            ДохДоговДУИ_ВнР : dohDolgovDUI_VnR,
                            РасхДоговДУИ : rashDolgovDUI,
                            РасхДоговДУИ_ВнР : rashDolgovDUI_VnR,
                            УбытДоговДУИ : empty,
                            ЦенРеалПрЗУ : cenRealPrZU,
                            НеВозЗатрПрЗУ : neVozZatrPrZU,
                            УбытРеалПрЗУ : ubitRealPrZU,
                            ВыручОп302Ит : viruchOp302It,
                            РасхОп302Ит : rashOper32,
                            УбытОп302 : ubitki) {
                        // 0..1
                        ВыручРеалПТ(
                                ВыручРеалПТДоСр : viruchRealPTDoSr,
                                // ВыручРеалПТПосСр : viruchRealPTPosSr не заполняется с 2015 года
                        )
                        // 0..1
                        СтоимРеалПТ(
                                СтоимРеалПТДоСр : stoimRealPTDoSr,
                                // СтоимРеалПТПосСр : stoimRealPTPosSr не заполняется с 2015 года
                        )
                        // 0..1
                        УбытРеалПТ1(
                                Убыт1Соот269 : ubit1Soot269,
                                Убыт1Прев269 : ubit1Prev269
                        )
                        //УбытРеалПТ2(
                        // Убыт2РеалПТ : ubit2RealPT, не заполняется с 2015 года
                        // Убыт2ВнРасх : ubit2VnRash не заполняется с 2015 года
                        //)
                    }
                    // Приложение № 3 к Листу 02 - конец

                    // Приложение № 5 к Листу 02
                    if (dataRowsAdvance != null && !dataRowsAdvance.isEmpty()) {
                        /** НалБазаОрг 030 = строка 120 Листа 02 */
                        def nalBazaOrg = nalBazaIsch
                        def rowCA = getDataRow(dataRowsAdvance, 'ca')
                        dataRowsAdvance.each { row ->
                            if (row.getAlias() == null) {
                                // подменяем строку "ЦА" строкой "ЦА скорректированное"
                                if (rowCA.regionBankDivision == row.regionBankDivision) {
                                    row = rowCA
                                }
                                // 0..n
                                РаспрНалСубРФ(
                                        ТипНП: typeNP,
                                        ОбРасч: getRefBookValue(26, row.calcFlag)?.CODE?.value,
                                        НаимОП: row.divisionName,
                                        КППОП: row.kpp,
                                        ОбязУплНалОП: getRefBookValue(25, row.obligationPayTax)?.CODE?.value,
                                        НалБазаОрг: nalBazaOrg,
                                        НалБазаБезЛиквОП: empty,
                                        ДоляНалБаз: row.baseTaxOf,
                                        НалБазаДоля: row.baseTaxOfRub,
                                        СтавНалСубРФ: row.subjectTaxStavka,
                                        СумНал: row.taxSum,
                                        НалНачислСубРФ: row.subjectTaxCredit,
                                        НалВыплВнеРФ: row.taxSumOutside,
                                        СумНалП: (row.taxSumToPay != 0) ? row.taxSumToPay : (-row.taxSumToReduction),
                                        МесАвПлат: (isTaxPeriod ? empty : (row.everyMontherPaymentAfterPeriod)),
                                        МесАвПлат1КвСлед: row.everyMonthForKvartalNextPeriod)
                            }
                        }
                    }
                    // Приложение № 5 к Листу 02 - конец
                }

                boolean isAgentUsed = false
                // 0..1
                НалУдНА() {
                    if (dataRowsDividend != null) {
                        for (row in dataRowsDividend) {
                            if (row.getAlias() != null) {
                                continue
                            }
                            // Лист 03 А
                            // 0..n
                            НалДохДив(
                                    [КатегорНА : row.taCategory] +
                                        (row.inn ? [ИННЮЛ_ЭмЦБ : row.inn] : [:]) +
                                    [ВидДив : row.dividendType,
                                    НалПер : row.taxPeriod,
                                    ОтчетГод : row.financialYear?.format('yyyy'),
                                    ДивРаспрПол : getLong(row.totalDividend),
                                    ДивВсего : getLong(row.dividendSumRaspredPeriod),
                                    ДивФЛРез : getLong(row.dividendRussianPersonal),
                                    ДивИнОрг : getLong(row.dividendForgeinOrgAll),
                                    ДивСтатНеУст : getLong(row.dividendTaxUnknown),
                                    ДивНеДоход : getLong(row.dividendNonIncome),
                                    ДивРаспрУм : getLong(row.dividendD1D2),
                                    НалИсчисл : getLong(row.taxSum),
                                    НалДивПред : getLong(row.taxSumFromPeriod),
                                    НалДивПосл : getLong(row.taxSumLast)]) {

                                ДивРосОрг(
                                        ДивРосОргВс : getLong(row.dividendRussianTotal),
                                        ДивРосСтав0 : getLong(row.dividendRussianStavka0),
                                        ДивРосСтав9 : getLong(row.dividendRussianStavka6),// для 9% алиас такой
                                        ДивРосСтавИн : getLong(row.dividendRussianStavka9), // алиас такой
                                        ДивРосНеНП : getLong(row. dividendRussianTaxFree),
                                )
                                // 0..1
                                ДивФЛНеРез(
                                        ДивФЛНеРезВс : getLong(row.dividendForgeinPersonalAll),
                                        ДивФЛСтав0 : getLong(row.dividendStavka0),
                                        ДивФЛСтав5 : getLong(row.dividendStavkaLess5),
                                        ДивФЛСтав10 : getLong(row.dividendStavkaMore5),
                                        ДивФЛСтавСв10 : getLong(row.dividendStavkaMore10))
                                // 0..1
                                ДивНА(
                                        ДивНАдоРас : getLong(row.dividendAgentAll),
                                        ДивНАБезУч0 : getLong(row.dividendAgentWithStavka0))
                                // 0..1
                                ДивНал(
                                        ДивНал9 : getLong(row.dividendSumForTaxStavka9),
                                        ДивНал0 : getLong(row.dividendSumForTaxStavka0))
                                // Лист 03 В
                                if (dataRowsTaxAgent != null && dataRowsDividend.indexOf(row) == 0) {
                                    isAgentUsed = true
                                    dataRowsTaxAgent.each { rowAgent ->
                                        // 0..n
                                        РеестрСумДив(
                                                ПрПринадл : 'А',
                                                Тип : rowAgent.recType,
                                                ДатаПерДив : (rowAgent.dividendDate != null ? rowAgent.dividendDate.format('dd.MM.yyyy') : empty),
                                                СумДив : getLong(rowAgent.sumDividend),
                                                СумНал : getLong(rowAgent.sumTax),) {

                                            СвПолуч(
                                                    [ИННПолуч : rowAgent.inn, КПППолуч : rowAgent.kpp, НаимПолуч : rowAgent.title] +
                                                            (rowAgent.phone ? [Тлф : rowAgent.phone] : [:])) {
                                                МНПолучРФ(
                                                        (rowAgent.zipCode ? [Индекс : rowAgent.zipCode] : [:]) +
                                                                [КодРегион : (getRefBookValue(4, rowAgent.subdivisionRF)?.CODE?.value ?: '00')] +
                                                                (rowAgent.area? [Район : rowAgent.area] : [:]) +
                                                                (rowAgent.city ? [Город : rowAgent.city] : [:]) +
                                                                (rowAgent.region ? [НаселПункт : rowAgent.region] : [:]) +
                                                                (rowAgent.street ? [Улица : rowAgent.street] : [:]) +
                                                                (rowAgent.homeNumber ? [Дом : rowAgent.homeNumber] : [:]) +
                                                                (rowAgent.corpNumber ? [Корпус : rowAgent.corpNumber] : [:]) +
                                                                (rowAgent.apartment ? [Кварт : rowAgent.apartment] : [:]))
                                                // 0..1
                                                ФИОРук(
                                                        [Фамилия : (rowAgent.surname ?: 'нет данных')] +
                                                                [Имя : (rowAgent.name ?: 'нет данных')] +
                                                                (rowAgent.patronymic ? [Отчество : rowAgent.patronymic] : [:]))
                                            }
                                        }
                                    }
                                }
                            }
                            // Лист 03 В - конец
                        }
                        // Лист 03 А - конец
                        // Лист 03 Б удален
                    }
                }

                // Лист 04
                def nalBaza04 = 0
                def dohUmNalBaz = 0
                def stavNal = 0
                def nalIschisl04 = 0
                def nalDivNeRFPred = 0
                def nalDivNeRF = 0
                def nalNachislPred = 0
                def nalNachislPosl = 0

                (1..6).each {
                    nalDivNeRFPred = 0
                    nalDivNeRF = 0

                    // за предыдущий отчетный период
                    def nalDivNeRFPredOld = nalDivNeRFPredOldMap[it] ?: 0
                    def nalDivNeRFOld = nalDivNeRFOldMap[it] ?: 0
                    def nalNachislPredOld = nalNachislPredOldMap[it] ?: 0
                    def nalNachislPoslOld = nalNachislPoslOldMap[it] ?: 0

                    switch (it) {
                        case 1:
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [16440, 16480, 16510, 16540, 16570, 16660, 16670])
                            stavNal = 15
                            break
                        case 2:
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [16450, 16490, 16520, 16580])
                            stavNal = 9
                            break
                        case 3:
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [16460, 16500, 16550])
                            stavNal = 0
                            break
                        case 4:
                            nalBaza04 = getSumRowsByCol(dataRowsSimpleIncome, 'incomeTypeId', 'rnu6Field10Sum', [17140])
                            stavNal = 13
                            nalDivNeRFPred = (isFirstPeriod ? 0 : nalDivNeRFPredOld + nalDivNeRFOld)
                            nalDivNeRF = (getAliasFromForm(dataRowsSum, 'taxSum', 'SUM_DIVIDENDS') ?: 0)
                            break
                        case 5:
                            nalBaza04 = getSumRowsByCol(dataRowsSimpleIncome, 'incomeTypeId', 'rnu6Field10Sum', [17150])
                            stavNal = 0
                            break
                        case 6:
                            nalBaza04 = 0
                            stavNal = 9
                            break
                    }
                    nalIschisl04 = getLong(nalBaza04 * stavNal / 100)
                    if (it in [3, 5]) {
                        nalNachislPred = 0
                        nalNachislPosl = 0
                    } else {
                        nalNachislPred = (isFirstPeriod ? 0 : nalNachislPredOld + nalNachislPoslOld)
                        nalNachislPosl = nalIschisl04 - nalDivNeRFPred - nalDivNeRF - nalNachislPred
                    }

                    if (!(it in [2, 3, 6] && !nalBaza04)) {
                        НалДохСтав(
                                ВидДоход: it,
                                НалБаза: getLong(nalBaza04),
                                ДохУмНалБаз: getLong(dohUmNalBaz),
                                СтавНал: getLong(stavNal),
                                НалИсчисл: nalIschisl04,
                                НалДивНеРФПред: getLong(nalDivNeRFPred),
                                НалДивНеРФ: getLong(nalDivNeRF),
                                НалНачислПред: getLong(nalNachislPred),
                                НалНачислПосл: getLong(nalNachislPosl))
                    }
                }
                // Лист 04 - конец

                // Лист 05 неактуален с 1 января 2015 года

                // Приложение №1 к налоговой декларации
                if (svCelSred.size() > 0) {
                    // 0..1
                    ДохНеУч_РасхУч() {
                        svCelSred.keySet().sort().each { id ->
                            // 1..n
                            СвЦелСред(
                                    КодВидРасход : id,
                                    СумРасход : getLong(svCelSred[id]))
                        }
                    }
                }
                // Приложение №1 к налоговой декларации - конец

                // Приложение №2 к налоговой декларации - формируется только для периода "год"
                if (isTaxPeriod && showApp2) {
                    def generateApp2 = getClosureApp2()
                    // closure для формирования по-новому
                    def generateApp2CFO = getClosureApp2CFO()
                    // сортируем по ФИО, потом по остальным полям
                    if (isCFOApp2 && dataRowsApp2 != null) {
                        def sortColumns = ['surname', 'name', 'patronymic', 'innRF', 'inn', 'taxRate']
                        sortRows(dataRowsApp2, sortColumns)
                    }

                    //ДатаСправ   Дата составления
                    def dataSprav = (docDate != null ? docDate : new Date()).format("dd.MM.yyyy")
                    //Тип         Тип
                    def type = String.format("%02d", reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId))

                    def groupsApp2 = [:]
                    if (dataRowsApp2) {
                        fillRecordsMap([4L, 10L, 350L, 360L, 370L])
                        groupRows(dataRowsApp2, groupsApp2)
                    }
                    if (!isCFOApp2) {
                        generateApp2(builder, dataRowsApp2, isCFOApp2, null, dataSprav, type)
                    } else {
                        def index = 0
                        groupsApp2.each { key, rows ->
                            index++
                            if (rows.size() == 1) {
                                generateApp2(builder, rows, isCFOApp2, index, dataSprav, type)
                            } else {
                                generateApp2CFO(builder, rows, index, dataSprav, type)
                            }
                        }
                    }
                }
                // Приложение №2 к налоговой декларации - конец
            }
        }
    }
}

void groupRows (def dataRows, def resultMap) {
    def groupColumns = ['innRF', 'inn', 'surname', 'name', 'patronymic', 'birthday', 'series', 'taxRate']
    dataRows.each { row ->
        def key = groupColumns.sum { alias ->
            String.valueOf(row[alias])
        }?.toLowerCase()?.hashCode()?.toString()
        if (resultMap[key] == null) {
            resultMap[key] = []
        }
        resultMap[key].add(row)
    }
}

void createSpecificReport() {
    switch (scriptSpecificReportHolder.declarationSubreport.alias) {
        case 'declaration_app2' :
            createSpecificReportApp2()
            break
        case 'declaration_no_app2' :
            createSpecificReportNoApp2()
            break
    }
}

void createSpecificReportNoApp2() {
    File xmlFile = File.createTempFile(scriptSpecificReportHolder.fileName, ".xml", new File(System.getProperty("java.io.tmpdir")));
    FileWriter fileWriter = null
    try {
        fileWriter = new FileWriter(xmlFile);
        fileWriter.write("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
        generateXML(fileWriter, false)
    } finally {
        fileWriter?.close();
    }
    FileInputStream fileInputStream = null
    try {
        fileInputStream = new FileInputStream(xmlFile)
        JRSwapFile jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
        try {
            def jasperPrint = declarationService.createJasperReport(fileInputStream, getJrxml(scriptSpecificReportHolder.getFileInputStream()), jrSwapFile, null);
            declarationService.exportPDF(jasperPrint, scriptSpecificReportHolder.getFileOutputStream())
            scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.declarationSubreport.name.replace(" ", "_") + ".pdf")
        } finally {
            if (jrSwapFile != null)
                jrSwapFile.dispose();
        }
    } finally {
        fileInputStream.close()
        xmlFile.delete()
    }
}

@Field
def entryFileNameList = []

void createSpecificReportApp2() {
    boolean endCert = false

    File xmlFile
    FileWriter fileWriter = null
    ZipArchiveOutputStream zos
    try {
        def groupedRowsApp2 = [:]
        // получил строки и расгруппировал их в карту
        def dataRowsApp2 = getDataRowsApp2(groupedRowsApp2)
        if (dataRowsApp2 == null || dataRowsApp2.isEmpty()) {
            return
        }

        xmlFile = File.createTempFile(scriptSpecificReportHolder.fileName, ".xml", new File(System.getProperty("java.io.tmpdir")));
        zos = new ZipArchiveOutputStream(scriptSpecificReportHolder.getFileOutputStream());
        zos.setEncoding("cp866");
        zos.setUseLanguageEncodingFlag(true);
        def jrxml = getJrxml(scriptSpecificReportHolder.getFileInputStream())
        def firstCertNum = 0
        def startNumber = 0
        while (!endCert) {
            // формируем xml
            try {
                fileWriter = new FileWriter(xmlFile);
                fileWriter.write("<?xml version=\"1.0\" encoding=\"windows-1251\"?>");
                // firstCertNum увеличивается внутри функции
                def tempList = generateXMLApp2(fileWriter, dataRowsApp2, groupedRowsApp2, firstCertNum)
                firstCertNum = tempList[0]
                endCert = tempList[1]
            } finally {
                fileWriter?.close();
            }
            // читаем xml и формируем jasper
            FileInputStream fileInputStream = null
            JRSwapFile jrSwapFile = null
            try {
                fileInputStream = new FileInputStream(xmlFile)
                def params = ['START_PAGE_NUMBER' : startNumber]

                jrSwapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 100);
                def jasperPrint = declarationService.createJasperReport(fileInputStream, jrxml, jrSwapFile, params);

                ZipArchiveEntry zipEntry = new ZipArchiveEntry(entryFileNameList.last());
                zos.putArchiveEntry(zipEntry);

                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setParameter(JRPdfExporterParameter.CHARACTER_ENCODING, "Ansi");
                exporter.getPropertiesUtil().setProperty(JRPdfExporterParameter.PROPERTY_SIZE_PAGE_TO_CONTENT, "true");
                exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, zos);
                exporter.exportReport();

                zos.closeArchiveEntry()
                startNumber = startNumber + jasperPrint.getPages().size()
            } finally {
                if (jrSwapFile != null)
                    jrSwapFile.dispose();
                fileInputStream.close()
            }
        }
        scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.declarationSubreport.name.replace(" ", "_") + ".zip")
    } finally {
        xmlFile?.delete()
        zos?.close()
    }
}

String getJrxml(def jrxmlInputStream) {
    StringWriter writer = new StringWriter();
    try {
        IOUtils.copy(jrxmlInputStream, writer, "UTF-8");
        return writer.toString();
    } finally {
        IOUtils.closeQuietly(jrxmlInputStream);
        IOUtils.closeQuietly(writer);
    }
}

def getDataRowsApp2(def groupsApp2) {
    def formDataCollection = getAcceptedFormDataSources()

    if (formDataCollection.records.find { [415, 418].contains(it.getFormType().getId()) } == null ) {
        logger.error("Формирование отчета невозможно, т.к. отсутствует форма-источник «%s»/«%s» в статусе «Принята»!",
                formTypeService.get(415)?.name, formTypeService.get(418)?.name)
        return null
    }

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов"
    def dataRowsApp2 = getDataRows(formDataCollection, 415)
    isCFOApp2 = false

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ)."
    def dataRowsApp2CFO = getDataRows(formDataCollection, 418)
    if (dataRowsApp2 == null) {
        isCFOApp2 = true
        dataRowsApp2 = dataRowsApp2CFO
    } else if (dataRowsApp2CFO != null) {
        logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Формирование спецотчета произведено из «%s».",
                formTypeService.get(415)?.name, formTypeService.get(418)?.name, formTypeService.get(415)?.name)
    }
    // сортируем по ФИО, потом по остальным полям
    if (isCFOApp2 && dataRowsApp2 != null) {
        def sortColumns = ['surname', 'name', 'patronymic', 'innRF', 'inn', 'taxRate']
        sortRows(dataRowsApp2, sortColumns)
    }

    if (dataRowsApp2) {
        fillRecordsMap([4L, 10L, 350L, 360L, 370L])
        if (isCFOApp2) {
            groupRows(dataRowsApp2, groupsApp2)
        }
    } else {
        logger.error("Формирование отчета невозможно, т.к. нет данных в форме-источнике «%s»!", formTypeService.get(isCFOApp2 ? 418 : 415)?.name)
    }
    return dataRowsApp2
}

// Запуск генерации XML для спецотчета Приложения 2 к декларации.
def generateXMLApp2(def xmlInputStream, def dataRowsApp2, def groupedRowsApp2, def firstCertNum) {
    def MAX_CERT_NUM = 500
    // Параметры подразделения
    def incomeParams = getDepartmentParam()
    def incomeParamsTable = getDepartmentParamTable(incomeParams.record_id.value)

    def taxOrganCode = incomeParamsTable?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = incomeParamsTable?.TAX_ORGAN_CODE_PROM?.value
    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp
    def formatVersion = incomeParams?.FORMAT_VERSION?.value

    // Данные налоговых форм.

    if (xmlInputStream == null) {
        return
    }

    // Формирование XML'ки.

    // closure для формирования по-старинке
    def generateApp2 = getClosureApp2()
    // closure для формирования по-новому
    def generateApp2Paging = getClosureApp2Paging()

    boolean endCert = false
    def firstPart = null
    def secondPart = null

    def builder = new MarkupBuilder(xmlInputStream)
    builder.Файл(
            ИдФайл : generateXmlFileId(taxOrganCodeProm, taxOrganCode),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion){

        // Титульный лист
        Документ() {
            СвНП() {
                НПЮЛ(
                        ИННЮЛ : inn,
                        КПП : kpp)
            }
            // Титульный лист - конец

            Прибыль() {
                // Приложение №2

                //ДатаСправ   Дата составления
                def dataSprav = (docDate != null ? docDate : new Date()).format("dd.MM.yyyy")
                //Тип         Тип
                def type = String.format("%02d", reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId))

                if (!isCFOApp2) {
                    generateApp2(builder, dataRowsApp2, isCFOApp2, null, dataSprav, type)
                    endCert = true
                } else {
                    def certNumber = 0
                    for (key in groupedRowsApp2.keySet()) {
                        certNumber++
                        // пропускаем использованные справки
                        if (certNumber < firstCertNum) {
                            continue
                        }
                        // берем ограниченное число справок
                        if (certNumber == (firstCertNum + MAX_CERT_NUM)) {
                            firstCertNum = firstCertNum + MAX_CERT_NUM
                            endCert = false
                            break
                        }
                        // выводим подходящие справки
                        def rows = groupedRowsApp2[key]
                        if (firstPart == null) {
                            firstPart = rows[0].surname.toLowerCase().substring(0, 2)
                        }
                        secondPart = rows[0].surname.toLowerCase().substring(0, 2)
                        generateApp2Paging(builder, rows, certNumber, dataSprav, type)
                    }
                    // кончились справки
                    if (certNumber != firstCertNum || certNumber == 0) {
                        endCert = true
                    }
                }
            }
        }
    }
    int index = 1
    def addString = ''
    while (entryFileNameList.contains(firstPart + "_" + secondPart + addString + ".pdf")) {
        index++
        addString = "_" + index
    }
    entryFileNameList.add(firstPart + "_" + secondPart + addString + ".pdf")
    return [firstCertNum, endCert]
}

/** closure для формирования приложения 2 по-старинке */
def getClosureApp2 () {
    return { MarkupBuilder innerBuilder, def dataRows, boolean isCFO, Integer index, def dataSprav, def type ->
        for (def row : dataRows) {
            //НомерСправ  Справка №
            def nomerSprav = isCFO ? index : row.refNum
            //ИННФЛ       ИНН
            def innFL = row.innRF
            //ИННИно       ИНН
            def innIno = row.inn
            //Фамилия     Фамилия
            def surname = row.surname
            //Имя         Имя
            def givenName = row.name
            //Отчество    Отчество
            def parentName = row.patronymic
            //СтатусНП    Статус налогоплательщика
            def statusNP = row.status
            //ДатаРожд    Дата рождения
            def dataRozhd = row.birthday?.format('dd.MM.yyyy')
            //Гражд       Гражданство (код страны)
            def grazhd = getRefBookValue(10, row.citizenship)?.CODE?.value
            //КодВидДок   Код вида документа, удостоверяющего личность
            def kodVidDok = getRefBookValue(360, row.code)?.CODE?.value
            //СерНомДок   Серия и номер документа
            def serNomDok = row.series
            //Индекс      Почтовый индекс
            def zipCode = row.postcode
            //КодРегион   Регион (код)
            def subdivisionRF = getRefBookValue(4, row.region)?.CODE?.value
            //Район       Район
            def area = row.district
            //Город       Город
            def city = row.city
            //НаселПункт  Населенный пункт (село, поселок)
            def region = row.locality
            //Улица       Улица (проспект, переулок)
            def street = row.street
            //Дом         Номер дома (владения)
            def homeNumber = row.house
            //Корпус      Номер корпуса (строения)
            def corpNumber = row.housing
            //Кварт       Номер квартиры
            def apartment = row.apartment
            //ОКСМ        Код страны
            def oksm = getRefBookValue(10, row.country)?.CODE?.value
            //АдрТекст    Адрес места жительства за пределами Российской Федерации
            def adrText = row.address
            //Ставка      Налоговая ставка
            def stavka = row.taxRate
            //СумДохОбщ   Общая сумма дохода
            def sumDohObsh = row.income
            //СумВычОбщ   Общая сумма вычетов
            def sumVichObsh = row.deduction
            //НалБаза     Налоговая база
            def nalBazaApp2 = row.taxBase
            //НалИсчисл   Сумма налога исчисленная
            def nalIschislApp2 = row.calculated
            //НалУдерж    Сумма налога удержанная
            def nalUderzh = row.withheld
            //НалУплач Сумма налога перечисленная
            def nalUplach = row.listed
            //НалУдержЛиш Сумма налога, излишне удержанная налоговым агентом
            def nalUderzhLish = row.withheldAgent
            //НалНеУдерж  Сумма налога, не удержанная налоговым агентом
            def nalNeUderzh = row.nonWithheldAgent

            // 0..n
            innerBuilder.СведДохФЛ(
                    НомерСправ : nomerSprav,
                    ДатаСправ : dataSprav,
                    Тип : type) {
                //1..1
                ФЛПолучДох(
                        (innFL ? [ИННФЛ: innFL] : [:]) +
                                (innIno ? [ИННИно: innIno] : [:]) +
                                [СтатусНП : statusNP,
                                 ДатаРожд : dataRozhd,
                                 Гражд    : grazhd,
                                 КодВидДок: kodVidDok,
                                 СерНомДок: serNomDok]
                ) {
                    // 1..1
                    ФИО([Фамилия : surname, Имя : givenName] + (parentName != null ? [Отчество : parentName] : []))
                    //0..1
                    if (subdivisionRF != null) {
                        АдрМЖРФ(
                                (zipCode ? [Индекс : zipCode] : [:]) +
                                        [КодРегион : subdivisionRF] +
                                        (area? [Район : area] : [:]) +
                                        (city ? [Город : city] : [:]) +
                                        (region ? [НаселПункт : region] : [:]) +
                                        (street ? [Улица : street] : [:]) +
                                        (homeNumber ? [Дом : homeNumber] : [:]) +
                                        (corpNumber ? [Корпус : corpNumber] : [:]) +
                                        (apartment ? [Кварт : apartment] : [:]))
                    }
                    //0..1
                    if (oksm != null || adrText) {
                        АдрМЖИно(ОКСМ : oksm, АдрТекст : adrText)
                    }
                }
                //1..1
                ДохНалПер(
                        [Ставка : stavka, СумДохОбщ : sumDohObsh] +
                                (sumVichObsh != null ? [СумВычОбщ : sumVichObsh] : []) +
                                [НалБаза : nalBazaApp2, НалИсчисл : nalIschislApp2] +
                                (nalUderzh != null ? [НалУдерж : nalUderzh] : []) +
                                (nalUplach != null ? [НалУплач : nalUplach] : []) +
                                (nalUderzhLish != null ? [НалУдержЛиш : nalUderzhLish] : []) +
                                (nalNeUderzh != null ? [НалНеУдерж : nalNeUderzh] : [])
                )
                boolean isSprDohFL = (1..3).find { index_1 ->
                    //КодДоход    040 (Код дохода) или СумДоход    041 (Сумма дохода) заполнены
                    row["col_040_${index_1}"] != null || row["col_041_${index_1}"] != null
                } != null

                //0..1
                if (isSprDohFL) {
                    СпрДохФЛ() {
                        3.times{ index_1 ->
                            //КодДоход    040 (Код дохода)
                            def kodDohod040 = getRefBookValue(370, row["col_040_${index_1 + 1}"])?.CODE?.value
                            //СумДоход    041 (Сумма дохода)
                            def sumDohod041 = row["col_041_${index_1 + 1}"]

                            // 0..n
                            if (kodDohod040 || sumDohod041 != null) {
                                СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041) {
                                    5.times{ index_2 ->
                                        //КодВычет    042 (Код вычета)
                                        def kodVichet042 = getRefBookValue(350, row["col_042_${index_1 + 1}_${index_2 + 1}"])?.CODE?.value
                                        //СумВычет    043 (Сумма вычета)
                                        def sumVichet043 = row["col_043_${index_1 + 1}_${index_2 + 1}"]

                                        //0..n
                                        if (kodVichet042 || sumVichet043 != null) {
                                            СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                boolean isNalVichStand = (1..2).find { index_1 ->
                    //КодВычет    051 (Код вычета) или СумВычет    052 (Сумма вычета) заполнены
                    row["col_051_3_${index_1}"] != null || row["col_052_3_${index_1}"] != null
                } != null

                //0..1
                if (isNalVichStand) {
                    НалВычСтанд() {
                        2.times{ index_1 ->
                            //КодВычет    051 (Код вычета)
                            def kodVichet051 = getRefBookValue(350, row["col_051_3_${index_1 + 1}"])?.CODE?.value
                            //СумВычет    052 (Сумма вычета)
                            def sumVichet052 = row["col_052_3_${index_1 + 1}"]
                            //0..n
                            if (kodVichet051 || sumVichet052 != null) {
                                СумВыч(КодВычет : kodVichet051, СумВычет : sumVichet052)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** closure для формирования приложения 2 с группировками по кодам дохода, но, к сожалению, не совсем корректной печатной формой */
def getClosureApp2CFO() {
    return { MarkupBuilder innerBuilder, def dataRows, Integer index, def dataSprav, def type ->
        def dataRow = dataRows[0]
        //НомерСправ  Справка №
        def nomerSprav = index
        //ИННФЛ       ИНН
        def innFL = dataRow.innRF
        //ИННИно       ИНН
        def innIno = dataRow.inn
        //Фамилия     Фамилия
        def surname = dataRow.surname
        //Имя         Имя
        def givenName = dataRow.name
        //Отчество    Отчество
        def parentName = dataRow.patronymic
        //СтатусНП    Статус налогоплательщика
        def statusNP = dataRow.status
        //ДатаРожд    Дата рождения
        def dataRozhd = dataRow.birthday?.format('dd.MM.yyyy')
        //Гражд       Гражданство (код страны)
        def grazhd = getRefBookValue(10, dataRow.citizenship)?.CODE?.value
        //КодВидДок   Код вида документа, удостоверяющего личность
        def kodVidDok = getRefBookValue(360, dataRow.code)?.CODE?.value
        //СерНомДок   Серия и номер документа
        def serNomDok = dataRow.series
        //Индекс      Почтовый индекс
        def zipCode = dataRow.postcode
        //КодРегион   Регион (код)
        def subdivisionRF = getRefBookValue(4, dataRow.region)?.CODE?.value
        //Район       Район
        def area = dataRow.district
        //Город       Город
        def city = dataRow.city
        //НаселПункт  Населенный пункт (село, поселок)
        def region = dataRow.locality
        //Улица       Улица (проспект, переулок)
        def street = dataRow.street
        //Дом         Номер дома (владения)
        def homeNumber = dataRow.house
        //Корпус      Номер корпуса (строения)
        def corpNumber = dataRow.housing
        //Кварт       Номер квартиры
        def apartment = dataRow.apartment
        //ОКСМ        Код страны
        def oksm = getRefBookValue(10, dataRow.country)?.CODE?.value
        //АдрТекст    Адрес места жительства за пределами Российской Федерации
        def adrText = dataRow.address
        //Ставка      Налоговая ставка
        def stavka = dataRow.taxRate

        //СумДохОбщ   Общая сумма дохода
        def sumDohObsh = BigDecimal.ZERO
        //СумВычОбщ   Общая сумма вычетов
        def sumVichObsh = BigDecimal.ZERO
        //НалБаза     Налоговая база
        def nalBazaApp2 = BigDecimal.ZERO
        //НалИсчисл   Сумма налога исчисленная
        def nalIschislApp2 = BigDecimal.ZERO
        //НалУдерж    Сумма налога удержанная
        def nalUderzh = BigDecimal.ZERO
        //НалУплач Сумма налога перечисленная
        def nalUplach = BigDecimal.ZERO
        //НалУдержЛиш Сумма налога, излишне удержанная налоговым агентом
        def nalUderzhLish = BigDecimal.ZERO
        //НалНеУдерж  Сумма налога, не удержанная налоговым агентом
        def nalNeUderzh = BigDecimal.ZERO

        dataRows.each { def row ->
            sumDohObsh = sumDohObsh.add(row.income ?: BigDecimal.ZERO)
            sumVichObsh = sumVichObsh.add(row.deduction ?: BigDecimal.ZERO)
            nalIschislApp2 = nalIschislApp2.add(row.calculated ?: BigDecimal.ZERO)
            nalUderzh = nalUderzh.add(row.withheld ?: BigDecimal.ZERO)
            nalUplach = nalUplach.add(row.listed ?: BigDecimal.ZERO)
            nalUderzhLish = nalUderzhLish.add(row.withheldAgent ?: BigDecimal.ZERO)
            nalNeUderzh = nalNeUderzh.add(row.nonWithheldAgent ?: BigDecimal.ZERO)
        }
        nalBazaApp2 = sumDohObsh - sumVichObsh

        // 0..n
        innerBuilder.СведДохФЛ(
                НомерСправ : nomerSprav,
                ДатаСправ : dataSprav,
                Тип : type) {
            //1..1
            ФЛПолучДох(
                    (innFL ? [ИННФЛ: innFL] : [:]) +
                            (innIno ? [ИННИно: innIno] : [:]) +
                            [СтатусНП : statusNP,
                             ДатаРожд : dataRozhd,
                             Гражд    : grazhd,
                             КодВидДок: kodVidDok,
                             СерНомДок: serNomDok]
            ) {
                // 1..1
                ФИО([Фамилия : surname, Имя : givenName] + (parentName != null ? [Отчество : parentName] : []))
                //0..1
                if (subdivisionRF != null) {
                    АдрМЖРФ(
                            (zipCode ? [Индекс : zipCode] : [:]) +
                                    [КодРегион : subdivisionRF] +
                                    (area? [Район : area] : [:]) +
                                    (city ? [Город : city] : [:]) +
                                    (region ? [НаселПункт : region] : [:]) +
                                    (street ? [Улица : street] : [:]) +
                                    (homeNumber ? [Дом : homeNumber] : [:]) +
                                    (corpNumber ? [Корпус : corpNumber] : [:]) +
                                    (apartment ? [Кварт : apartment] : [:]))
                }
                //0..1
                if (oksm != null || adrText) {
                    АдрМЖИно(ОКСМ : oksm, АдрТекст : adrText)
                }
            }
            //1..1
            ДохНалПер(
                    [Ставка : stavka, СумДохОбщ : sumDohObsh] +
                            (sumVichObsh != null ? [СумВычОбщ : sumVichObsh] : []) +
                            [НалБаза : nalBazaApp2, НалИсчисл : nalIschislApp2] +
                            (nalUderzh != null ? [НалУдерж : nalUderzh] : []) +
                            (nalUplach != null ? [НалУплач : nalUplach] : []) +
                            (nalUderzhLish != null ? [НалУдержЛиш : nalUderzhLish] : []) +
                            (nalNeUderzh != null ? [НалНеУдерж : nalNeUderzh] : [])
            )
            def sumDohod041Map = [:]
            def sumVichet043MapMap = [:]
            def nalVichStandMap = [:]
            for (def row : dataRows) {
                boolean isSprDohFL = (1..3).find { index_1 ->
                    //КодДоход    040 (Код дохода) или СумДоход    041 (Сумма дохода) заполнены
                    row["col_040_${index_1}"] != null || row["col_041_${index_1}"] != null
                } != null

                if (isSprDohFL) {
                    3.times{ index_1 ->
                        //КодДоход    040 (Код дохода)
                        def kodDohod040 = getRefBookValue(370, row["col_040_${index_1 + 1}"])?.CODE?.value
                        //СумДоход    041 (Сумма дохода)
                        def sumDohod041 = row["col_041_${index_1 + 1}"]

                        // 0..n
                        if (kodDohod040 || sumDohod041 != null) {
                            sumDohod041Map[kodDohod040] = sumDohod041
                            if (sumVichet043MapMap[kodDohod040] == null) {
                                sumVichet043MapMap[kodDohod040] = [:]
                            }
                            5.times{ index_2 ->
                                //КодВычет    042 (Код вычета)
                                def kodVichet042 = getRefBookValue(350, row["col_042_${index_1 + 1}_${index_2 + 1}"])?.CODE?.value
                                //СумВычет    043 (Сумма вычета)
                                def sumVichet043 = row["col_043_${index_1 + 1}_${index_2 + 1}"]

                                //0..n
                                if (kodVichet042 || sumVichet043 != null) {
                                    if (sumVichet043MapMap[kodDohod040][kodVichet042] == null) {
                                        sumVichet043MapMap[kodDohod040][kodVichet042] = []
                                    }
                                    sumVichet043MapMap[kodDohod040][kodVichet042].add(sumVichet043)
                                }
                            }
                        }
                    }
                }

                boolean isNalVichStand = (1..2).find { index_1 ->
                    //КодВычет    051 (Код вычета) или СумВычет    052 (Сумма вычета) заполнены
                    row["col_051_3_${index_1}"] != null || row["col_052_3_${index_1}"] != null
                } != null

                //0..1
                if (isNalVichStand) {
                    2.times{ index_1 ->
                        //КодВычет    051 (Код вычета)
                        def kodVichet051 = getRefBookValue(350, row["col_051_3_${index_1 + 1}"])?.CODE?.value
                        //СумВычет    052 (Сумма вычета)
                        def sumVichet052 = row["col_052_3_${index_1 + 1}"]
                        //0..n
                        if (kodVichet051 || sumVichet052 != null) {
                            if (nalVichStandMap[kodVichet051] == null) {
                                nalVichStandMap[kodVichet051] = []
                            }
                            nalVichStandMap[kodVichet051].add(sumVichet052)
                        }
                    }
                }
            }

            //0..1
            if (!sumDohod041Map.isEmpty()) {
                СпрДохФЛ() {
                    sumDohod041Map.each { def kodDohod040, sumDohod041 ->
                        // 0..n
                        СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041) {
                            if (!sumVichet043MapMap[kodDohod040].isEmpty()) {
                                sumVichet043MapMap[kodDohod040].each { def kodVichet042, sumVichet043List ->
                                    //0..n
                                    sumVichet043List.each { sumVichet043 ->
                                        СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //0..1
            if (!nalVichStandMap.isEmpty()) {
                НалВычСтанд() {
                    nalVichStandMap.each { def kodVichet051, sumVichet052List ->
                        //0..n
                        sumVichet052List.each { sumVichet052 ->
                            СумВыч(КодВычет : kodVichet051, СумВычет : sumVichet052)
                        }
                    }
                }
            }
        }
    }
}

/** closure для формирования приложения 2 с разделением на страницы */
def getClosureApp2Paging() {
    return { MarkupBuilder innerBuilder, def dataRows, Integer index, def dataSprav, def type ->
        def dataRow = dataRows[0]
        //НомерСправ  Справка №
        def nomerSprav = index
        //ИННФЛ       ИНН
        def innFL = dataRow.innRF
        //ИННИно       ИНН
        def innIno = dataRow.inn
        //Фамилия     Фамилия
        def surname = dataRow.surname
        //Имя         Имя
        def givenName = dataRow.name
        //Отчество    Отчество
        def parentName = dataRow.patronymic
        //СтатусНП    Статус налогоплательщика
        def statusNP = dataRow.status
        //ДатаРожд    Дата рождения
        def dataRozhd = dataRow.birthday?.format('dd.MM.yyyy')
        //Гражд       Гражданство (код страны)
        def grazhd = getRefBookValue(10, dataRow.citizenship)?.CODE?.value
        //КодВидДок   Код вида документа, удостоверяющего личность
        def kodVidDok = getRefBookValue(360, dataRow.code)?.CODE?.value
        //СерНомДок   Серия и номер документа
        def serNomDok = dataRow.series
        //Индекс      Почтовый индекс
        def zipCode = dataRow.postcode
        //КодРегион   Регион (код)
        def subdivisionRF = getRefBookValue(4, dataRow.region)?.CODE?.value
        //Район       Район
        def area = dataRow.district
        //Город       Город
        def city = dataRow.city
        //НаселПункт  Населенный пункт (село, поселок)
        def region = dataRow.locality
        //Улица       Улица (проспект, переулок)
        def street = dataRow.street
        //Дом         Номер дома (владения)
        def homeNumber = dataRow.house
        //Корпус      Номер корпуса (строения)
        def corpNumber = dataRow.housing
        //Кварт       Номер квартиры
        def apartment = dataRow.apartment
        //ОКСМ        Код страны
        def oksm = getRefBookValue(10, dataRow.country)?.CODE?.value
        //АдрТекст    Адрес места жительства за пределами Российской Федерации
        def adrText = dataRow.address
        //Ставка      Налоговая ставка
        def stavka = dataRow.taxRate

        //СумДохОбщ   Общая сумма дохода
        def sumDohObsh = BigDecimal.ZERO
        //СумВычОбщ   Общая сумма вычетов
        def sumVichObsh = BigDecimal.ZERO
        //НалБаза     Налоговая база
        def nalBazaApp2 = BigDecimal.ZERO
        //НалИсчисл   Сумма налога исчисленная
        def nalIschislApp2 = BigDecimal.ZERO
        //НалУдерж    Сумма налога удержанная
        def nalUderzh = BigDecimal.ZERO
        //НалУплач Сумма налога перечисленная
        def nalUplach = BigDecimal.ZERO
        //НалУдержЛиш Сумма налога, излишне удержанная налоговым агентом
        def nalUderzhLish = BigDecimal.ZERO
        //НалНеУдерж  Сумма налога, не удержанная налоговым агентом
        def nalNeUderzh = BigDecimal.ZERO

        dataRows.each { def row ->
            sumDohObsh = sumDohObsh.add(row.income ?: BigDecimal.ZERO)
            sumVichObsh = sumVichObsh.add(row.deduction ?: BigDecimal.ZERO)
            nalIschislApp2 = nalIschislApp2.add(row.calculated ?: BigDecimal.ZERO)
            nalUderzh = nalUderzh.add(row.withheld ?: BigDecimal.ZERO)
            nalUplach = nalUplach.add(row.listed ?: BigDecimal.ZERO)
            nalUderzhLish = nalUderzhLish.add(row.withheldAgent ?: BigDecimal.ZERO)
            nalNeUderzh = nalNeUderzh.add(row.nonWithheldAgent ?: BigDecimal.ZERO)
        }
        nalBazaApp2 = sumDohObsh - sumVichObsh

        // 0..n
        innerBuilder.СведДохФЛ(
                НомерСправ : nomerSprav,
                ДатаСправ : dataSprav,
                Тип : type) {
            //1..1
            ФЛПолучДох(
                    (innFL ? [ИННФЛ: innFL] : [:]) +
                            (innIno ? [ИННИно: innIno] : [:]) +
                            [СтатусНП : statusNP,
                             ДатаРожд : dataRozhd,
                             Гражд    : grazhd,
                             КодВидДок: kodVidDok,
                             СерНомДок: serNomDok]
            ) {
                // 1..1
                ФИО([Фамилия : surname, Имя : givenName] + (parentName != null ? [Отчество : parentName] : []))
                //0..1
                if (subdivisionRF != null) {
                    АдрМЖРФ(
                            (zipCode ? [Индекс : zipCode] : [:]) +
                                    [КодРегион : subdivisionRF] +
                                    (area? [Район : area] : [:]) +
                                    (city ? [Город : city] : [:]) +
                                    (region ? [НаселПункт : region] : [:]) +
                                    (street ? [Улица : street] : [:]) +
                                    (homeNumber ? [Дом : homeNumber] : [:]) +
                                    (corpNumber ? [Корпус : corpNumber] : [:]) +
                                    (apartment ? [Кварт : apartment] : [:]))
                }
                //0..1
                if (oksm != null || adrText) {
                    АдрМЖИно(ОКСМ : oksm, АдрТекст : adrText)
                }
            }
            //1..1
            ДохНалПер(
                    [Ставка : stavka, СумДохОбщ : sumDohObsh] +
                            (sumVichObsh != null ? [СумВычОбщ : sumVichObsh] : []) +
                            [НалБаза : nalBazaApp2, НалИсчисл : nalIschislApp2] +
                            (nalUderzh != null ? [НалУдерж : nalUderzh] : []) +
                            (nalUplach != null ? [НалУплач : nalUplach] : []) +
                            (nalUderzhLish != null ? [НалУдержЛиш : nalUderzhLish] : []) +
                            (nalNeUderzh != null ? [НалНеУдерж : nalNeUderzh] : [])
            )

            def sumDohod041Map = [:] // [(КодДоход : СумДоход) * n]
            def sumVichet043MapListMap = [:] // [КодДоход : ([КодВычет : СумВычет] * n)]
            def nalVichStandMapList = [] // [КодВычет : СумВычет] * n

            // заполняем мапы
            for (def row : dataRows) {
                boolean isSprDohFL = (1..3).find { index_1 ->
                    //КодДоход    040 (Код дохода) или СумДоход    041 (Сумма дохода) заполнены
                    row["col_040_${index_1}"] != null || row["col_041_${index_1}"] != null
                } != null

                if (isSprDohFL) {
                    3.times{ index_1 ->
                        //КодДоход    040 (Код дохода)
                        def kodDohod040 = getRefBookValue(370, row["col_040_${index_1 + 1}"])?.CODE?.value
                        //СумДоход    041 (Сумма дохода)
                        def sumDohod041 = row["col_041_${index_1 + 1}"]

                        // 0..n
                        if (kodDohod040 || sumDohod041 != null) {
                            sumDohod041Map[kodDohod040] = sumDohod041
                            if (sumVichet043MapListMap[kodDohod040] == null) {
                                sumVichet043MapListMap[kodDohod040] = []
                            }
                            5.times{ index_2 ->
                                //КодВычет    042 (Код вычета)
                                def kodVichet042 = getRefBookValue(350, row["col_042_${index_1 + 1}_${index_2 + 1}"])?.CODE?.value
                                //СумВычет    043 (Сумма вычета)
                                def sumVichet043 = row["col_043_${index_1 + 1}_${index_2 + 1}"]

                                //0..n
                                if (kodVichet042 || sumVichet043 != null) {
                                    sumVichet043MapListMap[kodDohod040].add(["$kodVichet042" : sumVichet043])
                                }
                            }
                        }
                    }
                }

                boolean isNalVichStand = (1..2).find { index_1 ->
                    //КодВычет    051 (Код вычета) или СумВычет    052 (Сумма вычета) заполнены
                    row["col_051_3_${index_1}"] != null || row["col_052_3_${index_1}"] != null
                } != null

                //0..1
                if (isNalVichStand) {
                    2.times{ index_1 ->
                        //КодВычет    051 (Код вычета)
                        def kodVichet051 = getRefBookValue(350, row["col_051_3_${index_1 + 1}"])?.CODE?.value
                        //СумВычет    052 (Сумма вычета)
                        def sumVichet052 = row["col_052_3_${index_1 + 1}"]
                        //0..n
                        if (kodVichet051 || sumVichet052 != null) {
                            nalVichStandMapList.add(["$kodVichet051" : sumVichet052])
                        }
                    }
                }
            }

            while (!sumVichet043MapListMap.isEmpty() || !nalVichStandMapList.isEmpty()) {
                PAGE() {
                    if (!sumVichet043MapListMap.isEmpty()) {
                        СпрДохФЛ() {
                            // максимум 3 блока кода дохода
                            int i = 0
                            def kodDohodList = sumVichet043MapListMap.keySet().asList()
                            while (i < 3 && !kodDohodList.isEmpty()) {
                                i++
                                def kodDohod040 = kodDohodList[0]
                                // максимум 3 блока
                                СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041Map[kodDohod040]) {
                                    def sumVichet043MapList = sumVichet043MapListMap[kodDohod040]
                                    // максимум 5 вычетов
                                    int j = 0
                                    while (j < 5 && !sumVichet043MapList.isEmpty()) {
                                        j++
                                        def miniMap = sumVichet043MapList[0]
                                        // miniMap = [КодВычет : СумВычет]
                                        miniMap.each { def kodVichet042, sumVichet043 ->
                                            СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                        }
                                        sumVichet043MapList.remove(0)
                                    }
                                    if (sumVichet043MapList.isEmpty()) {
                                        sumVichet043MapListMap.remove(kodDohod040)
                                        kodDohodList.remove(0)
                                    }
                                }
                            }
                        }
                    }

                    if (!nalVichStandMapList.isEmpty()) {
                        НалВычСтанд() {
                            // максимум 2 вычета
                            int i = 0
                            while (i < 2 && !nalVichStandMapList.isEmpty()) {
                                i++
                                def miniMap = nalVichStandMapList[0]
                                // miniMap = [КодВычет : СумВычет]
                                miniMap.each { def kodVichet051, sumVichet052 ->
                                    СумВыч(КодВычет : kodVichet051, СумВычет : sumVichet052)
                                }
                                nalVichStandMapList.remove(0)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ищет точное ли совпадение узлов дерева xml c текущими незакрытыми элементами
 * @param nodeNames ожидаемые элементы xml
 * @param elements незакрытые элементы
 * @return
 */
boolean isCurrentNode(List<String> nodeNames, Map<String, Boolean> elements) {
    nodeNames.add('Файл')
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    return enteredNodes.containsAll(nodeNames) && enteredNodes.size() == nodeNames.size()
}

// Получить округленное, целочисленное значение.
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return roundValue(value, 0)
}

/**
 * Получить сумму значении столбца по указанным строкам.
 *
 * @param dataRows строки нф
 * @param columnCode псевдоним столбца по которому отбирать данные для суммирования
 * @param columnSum псевдоним столбца значения которого надо суммировать
 * @param codes список значении, которые надо учитывать при суммировании
 */
def getSumRowsByCol(def dataRows, def columnCode, def columnSum, def codes) {
    def result = 0
    if (!dataRows) {
        return result
    }
    dataRows.each { row ->
        def cell = row.getCell(columnSum)
        if (row.getCell(columnCode).value in (String [])codes && !cell.hasValueOwner()) {
            result += (cell.value ?: 0)
        }
    }
    return result
}

/**
 * Получить сумму графы 9 формы доходы сложные.
 *
 * @param dataRows строки нф доходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexIncomeSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'incomeTaxSumS', codes)
}

/**
 * Получить сумму графы 9 формы расходы сложные.
 *
 * @param dataRows строки нф расходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexConsumptionSumRows9(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'consumptionTaxSumS', codes)
}

/**
 * Получить сумму графы 8 формы доходы простые.
 *
 * @param dataRows строки нф доходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleIncomeSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'incomeTypeId', 'rnu4Field5Accepted', codes)
}

/**
 * Получить сумму графы 8 формы расходы простые.
 *
 * @param dataRows строки нф расходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleConsumptionSumRows8(def dataRows, def codes) {
    return getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu5Field5Accepted', codes)
}

/** Подсчет простых расходов: сумма(графа 8 + графа 5 - графа 6). */
def getCalculatedSimpleConsumption(def dataRowsSimple, def codes) {
    def result = 0
    if (dataRowsSimple == null) {
        return result
    }
    dataRowsSimple.each { row ->
        if (row.getCell('consumptionTypeId').value in (String [])codes) {
            result +=
                    (row.rnu5Field5Accepted ?: 0) +
                            (row.rnu7Field10Sum ?: 0) -
                            (row.rnu7Field12Accepted ?: 0)
        }
    }
    return result
}

/**
 * Выручка от реализации товаров (работ, услуг) собственного производства (ВырРеалТовСоб).
 *
 * @param dataRows строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getVirRealTovSob(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 10830
    result += getComplexIncomeSumRows9(dataRows, [10830])

    // Код вида дохода = 10000, 10010, 10020, 10030, 10040, 10050, 10060, 10070, 10080,
    // 10090, 10100, 10110, 10120, 10130, 10140, 10150, 10160, 10170, 10180, 10190, 10200, 10210, 10220, 10230,
    // 10240, 10250, 10260, 10270, 10280, 10290, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370, 10380,
    // 10390, 10400, 10410, 10420, 10430, 10440, 10450, 10460, 10470, 10480, 10490, 10500, 10510, 10520, 10530,
    // 10540, 10550, 10560, 10570, 10580, 10590, 10600, 10610, 10620, 10630, 10640, 10650, 10660, 10670, 10680,
    // 10690, 10700, 10710, 10720, 10730, 10740, 10750, 10780, 10790, 10800, 10810, 10820, 10840, 10850, 10860,
    // 10870, 10890, 10900, 10990, 11000, 11010, 11020, 11030, 11160, 11170, 11180, 11190, 11200, 11210, 11220,
    // 11230, 11240, 11250, 11260, 11270, 11280, 11290, 11300, 11310
    result += getSimpleIncomeSumRows8(dataRowsSimple, [10000, 10010, 10020, 10030, 10040, 10050, 10060, 10070, 10080,
            10090, 10100, 10110, 10120, 10130, 10140, 10150, 10160, 10170, 10180, 10190, 10200, 10210, 10220, 10230,
            10240, 10250, 10260, 10270, 10280, 10290, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370, 10380,
            10390, 10400, 10410, 10420, 10430, 10440, 10450, 10460, 10470, 10480, 10490, 10500, 10510, 10520, 10530,
            10540, 10550, 10560, 10570, 10580, 10590, 10600, 10610, 10620, 10630, 10640, 10650, 10660, 10670, 10680,
            10690, 10700, 10710, 10720, 10730, 10740, 10750, 10780, 10790, 10800, 10810, 10820, 10840, 10850, 10860,
            10870, 10890, 10900, 10990, 11000, 11010, 11020, 11030, 11160, 11170, 11180, 11190, 11200, 11210, 11220,
            11230, 11240, 11250, 11260, 11270, 11280, 11290, 11300, 11310])

    // Код вида доходов = 10160, 10230, 10240, 10250, 10260, 10270, 10280, 10330, 10760, 10770, 10880, 11170, 11300, 11310
    def codes = [10160, 10230, 10240, 10250, 10260, 10270, 10280, 10330, 10760, 10770, 10880, 11170, 11300, 11310]

    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

/**
 * Получить внереализационные доходы (ДохВнереал, ВнеРеалДохВс).
 *
 * @param dataRows строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getDohVnereal(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // Код вида дохода = 14120, 15170, 15180, 15190, 15200, 15210, 15220, 15230,
    // 16110, 16120, 16130, 16140, 16280, 16290, 16300, 16310, 16420, 16430, 16440, 16450, 16460, 16470, 16480,
    // 16490, 16500, 16510, 16520, 16530, 16540, 16550, 16560, 16570, 16580, 16590, 16600, 16660, 16670, 16970,
    // 16980, 16990, 17000, 17010, 17020, 17030, 17040, 17050, 17060, 17070, 17080, 17090, 17100, 17110, 17120
    result += getComplexIncomeSumRows9(dataRows, [14120, 15170, 15180, 15190, 15200, 15210, 15220, 15230,
            16110, 16120, 16130, 16140, 16280, 16290, 16300, 16310, 16420, 16430, 16440, 16450, 16460, 16470, 16480,
            16490, 16500, 16510, 16520, 16530, 16540, 16550, 16560, 16570, 16580, 16590, 16600, 16660, 16670, 16970,
            16980, 16990, 17000, 17010, 17020, 17030, 17040, 17050, 17060, 17070, 17080, 17090, 17100, 17110, 17120])

    // Код вида дохода = 14000, 14010, 14020, 14030, 14040, 14050, 14060, 14070, 14080,
    // 14090, 14100, 14110, 14130, 14140, 14150, 14160, 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240,
    // 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320, 14330, 14340, 14350, 14360, 14370, 14380, 14390,
    // 14400, 14410, 14420, 14430, 14440, 14450, 14460, 14470, 14480, 14490, 14500, 14510, 14520, 14530, 14540,
    // 14550, 14560, 14570, 14580, 14590, 14600, 14610, 14620, 14630, 14640, 14650, 14660, 14670, 14680, 14690,
    // 14700, 14710, 14720, 14730, 14740, 14750, 14760, 14770, 14780, 14790, 14800, 14810, 14820, 14830, 14840,
    // 14850, 14860, 14870, 14880, 14890, 14900, 14910, 14920, 14930, 14940, 14950, 14960, 14970, 14980, 14990,
    // 15000, 15010, 15020, 15030, 15040, 15050, 15060, 15070, 15080, 15090, 15100, 15110, 15120, 15130, 15140,
    // 15150, 15160, 15260, 15270, 15280, 15290, 15300, 15310, 15320, 15330, 15340, 15350, 15360, 15370, 15380,
    // 15390, 15400, 15410, 15420, 15430, 15440, 15450, 15460, 15470, 15480, 15490, 15500, 15510, 15520, 15530,
    // 15540, 15550, 15560, 15570, 15580, 15590, 15600, 15610, 15620, 15630, 15640, 15650, 15660, 15670, 15680,
    // 15690, 15700, 15710, 15720, 15730, 15740, 15750, 15760, 15770, 15780, 15790, 15800, 15810, 15820, 15830,
    // 15840, 15850, 15860, 15870, 15880, 15890, 15900, 15910, 15920, 15930, 15940, 15950, 15960, 15970, 15980,
    // 15990, 16000, 16010, 16020, 16030, 16040, 16050, 16060, 16070, 16080, 16090, 16100, 16150, 16160, 16170,
    // 16180, 16190, 16200, 16210, 16220, 16230, 16240, 16250, 16260, 16270, 16320, 16330, 16340, 16350, 16360,
    // 16370, 16380, 16390, 16400, 16410, 16610, 16620, 16630, 16640, 16650, 16680, 16690, 16700, 16710, 16720,
    // 16730, 16740, 16750, 16760, 16770, 16780, 16790, 16800, 16810, 16820, 16830, 16840, 16850, 16860, 16870,
    // 16880, 16890, 16900, 16910, 16920, 16930, 16940, 16950, 16960, 17160, 17170, 17180, 17190, 17200, 17210,
    // 17220, 17230, 17240, 17250, 17260, 17270, 17280, 17290, 17300, 17310
    result += getSimpleIncomeSumRows8(dataRowsSimple, [14000, 14010, 14020, 14030, 14040, 14050, 14060, 14070, 14080,
            14090, 14100, 14110, 14130, 14140, 14150, 14160, 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240,
            14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320, 14330, 14340, 14350, 14360, 14370, 14380, 14390,
            14400, 14410, 14420, 14430, 14440, 14450, 14460, 14470, 14480, 14490, 14500, 14510, 14520, 14530, 14540,
            14550, 14560, 14570, 14580, 14590, 14600, 14610, 14620, 14630, 14640, 14650, 14660, 14670, 14680, 14690,
            14700, 14710, 14720, 14730, 14740, 14750, 14760, 14770, 14780, 14790, 14800, 14810, 14820, 14830, 14840,
            14850, 14860, 14870, 14880, 14890, 14900, 14910, 14920, 14930, 14940, 14950, 14960, 14970, 14980, 14990,
            15000, 15010, 15020, 15030, 15040, 15050, 15060, 15070, 15080, 15090, 15100, 15110, 15120, 15130, 15140,
            15150, 15160, 15260, 15270, 15280, 15290, 15300, 15310, 15320, 15330, 15340, 15350, 15360, 15370, 15380,
            15390, 15400, 15410, 15420, 15430, 15440, 15450, 15460, 15470, 15480, 15490, 15500, 15510, 15520, 15530,
            15540, 15550, 15560, 15570, 15580, 15590, 15600, 15610, 15620, 15630, 15640, 15650, 15660, 15670, 15680,
            15690, 15700, 15710, 15720, 15730, 15740, 15750, 15760, 15770, 15780, 15790, 15800, 15810, 15820, 15830,
            15840, 15850, 15860, 15870, 15880, 15890, 15900, 15910, 15920, 15930, 15940, 15950, 15960, 15970, 15980,
            15990, 16000, 16010, 16020, 16030, 16040, 16050, 16060, 16070, 16080, 16090, 16100, 16150, 16160, 16170,
            16180, 16190, 16200, 16210, 16220, 16230, 16240, 16250, 16260, 16270, 16320, 16330, 16340, 16350, 16360,
            16370, 16380, 16390, 16400, 16410, 16610, 16620, 16630, 16640, 16650, 16680, 16690, 16700, 16710, 16720,
            16730, 16740, 16750, 16760, 16770, 16780, 16790, 16800, 16810, 16820, 16830, 16840, 16850, 16860, 16870,
            16880, 16890, 16900, 16910, 16920, 16930, 16940, 16950, 16960, 17160, 17170, 17180, 17190, 17200, 17210,
            17220, 17230, 17240, 17250, 17260, 17270, 17280, 17290, 17300, 17310])

    // Код вида дохода = 14190, 14200, 14210, 14220, 14240, 14250, 15240, 15250, 17130, 17140,
    // 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320
    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', [14190, 14200, 14210, 14220, 14240,
            14250, 15240, 15250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320])
    // графа 6
    // Код вида дохода = 14190, 14200, 14210, 14220, 14240, 14250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', [14190, 14200, 14210, 14220, 14240,
            14250, 17130, 17140, 17150, 17250, 17260, 17270, 17280, 17300, 17310, 17320])

    return getLong(result)
}

/**
 * Получить доходы, исключаемые из прибыли (ДохИсклПриб).
 *
 * @param dataRowsComplex строки нф доходы сложные
 * @param dataRowsSimple строки нф доходы простые
 */
def getDohIsklPrib(def dataRowsComplex, def dataRowsSimple) {
    def result = 0.0

    if (dataRowsComplex != null) {
        // Код вида доходов = 16440, 16450, 16460, 16480, 16490, 16500, 16510, 16520, 16540, 16550, 16570, 16580, 16660, 16670
        result += getComplexIncomeSumRows9(dataRowsComplex,
                [16440, 16450, 16460, 16480, 16490, 16500, 16510, 16520, 16540, 16550, 16570, 16580, 16660, 16670])
    }
    if (dataRowsSimple != null) {
        // Код вида дохода = 17130, 17140, 17150
        result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', [17130, 17140, 17150])
    }
    return getLong(result)
}

/**
 * Получить внереализационные расходы (РасхВнереалВС).
 *
 * @param dataRows строки нф расходы сложные
 * @param dataRowsSimple строки нф расходы простые
 */
def getRashVnerealVs(def dataRows, def dataRowsSimple) {
    def result = 0.0

    // графа 9
    // Код вида расхода = 24660, 24670, 25680, 25690, 25830, 26310, 26320, 26330, 26340,
    // 26360, 26370, 26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890, 26900, 27060
    result += getComplexConsumptionSumRows9(dataRows, [24660, 24670, 25680, 25690, 25830, 26310, 26320, 26330, 26340,
            26360, 26370, 26820, 26830, 26840, 26850, 26860, 26870, 26880, 26890, 26900, 27060])

    // графа 8 + 5 - 6
    // Код вида расхода = 24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070, 24080, 24090, 24100, 24110, 24120, 24130, 24140,
    // 24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220, 24230, 24240, 24250, 24260, 24270, 24280, 24290,
    // 24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370, 24380, 24390, 24400, 24410, 24420, 24430, 24440,
    // 24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520, 24530, 24540, 24550, 24560, 24570, 24580, 24590,
    // 24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700, 24710, 24720, 24730, 24740, 24750, 24760, 24770,
    // 24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850, 24860, 24870, 24880, 24890, 24900, 24910, 24920,
    // 24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000, 25010, 25020, 25030, 25040, 25050, 25060, 25070,
    // 25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150, 25160, 25170, 25180, 25190, 25200, 25210, 25220,
    // 25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300, 25310, 25320, 25330, 25340, 25350, 25360, 25370,
    // 25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450, 25460, 25470, 25480, 25490, 25500, 25510, 25520,
    // 25530, 25540, 25550, 25560, 25570, 25580, 25590, 25600, 25610, 25620, 25630, 25640, 25650, 25660, 25670,
    // 25700, 25710, 25720, 25730, 25740, 25750, 25760, 25770, 25780, 25790, 25800, 25810, 25820, 25840, 25850,
    // 25860, 25870, 25880, 25890, 25900, 25910, 25920, 25930, 25940, 25950, 25960, 25970, 25980, 25990, 26000,
    // 26010, 26020, 26030, 26040, 26050, 26060, 26070, 26080, 26090, 26100, 26110, 26120, 26130, 26140, 26150,
    // 26160, 26170, 26180, 26190, 26200, 26210, 26220, 26230, 26240, 26250, 26260, 26270, 26280, 26290, 26300,
    // 26350, 26380, 26390, 26400, 26410, 26420, 26430, 26440, 26450, 26460, 26470, 26480, 26490, 26500, 26510,
    // 26520, 26530, 26540, 26550, 26560, 26570, 26580, 26590, 26600, 26610, 26620, 26630, 26640, 26650, 26660,
    // 26670, 26680, 26690, 26700, 26710, 26720, 26730, 26740, 26750, 26760, 26770, 26780, 26790, 26800, 26810,
    // 27010, 27020, 27030, 27040, 27050, 27070, 27080
    def knu = [24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070, 24080, 24090, 24100, 24110, 24120, 24130, 24140,
            24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220, 24230, 24240, 24250, 24260, 24270, 24280, 24290,
            24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370, 24380, 24390, 24400, 24410, 24420, 24430, 24440,
            24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520, 24530, 24540, 24550, 24560, 24570, 24580, 24590,
            24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700, 24710, 24720, 24730, 24740, 24750, 24760, 24770,
            24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850, 24860, 24870, 24880, 24890, 24900, 24910, 24920,
            24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000, 25010, 25020, 25030, 25040, 25050, 25060, 25070,
            25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150, 25160, 25170, 25180, 25190, 25200, 25210, 25220,
            25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300, 25310, 25320, 25330, 25340, 25350, 25360, 25370,
            25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450, 25460, 25470, 25480, 25490, 25500, 25510, 25520,
            25530, 25540, 25550, 25560, 25570, 25580, 25590, 25600, 25610, 25620, 25630, 25640, 25650, 25660, 25670,
            25700, 25710, 25720, 25730, 25740, 25750, 25760, 25770, 25780, 25790, 25800, 25810, 25820, 25840, 25850,
            25860, 25870, 25880, 25890, 25900, 25910, 25920, 25930, 25940, 25950, 25960, 25970, 25980, 25990, 26000,
            26010, 26020, 26030, 26040, 26050, 26060, 26070, 26080, 26090, 26100, 26110, 26120, 26130, 26140, 26150,
            26160, 26170, 26180, 26190, 26200, 26210, 26220, 26230, 26240, 26250, 26260, 26270, 26280, 26290, 26300,
            26350, 26380, 26390, 26400, 26410, 26420, 26430, 26440, 26450, 26460, 26470, 26480, 26490, 26500, 26510,
            26520, 26530, 26540, 26550, 26560, 26570, 26580, 26590, 26600, 26610, 26620, 26630, 26640, 26650, 26660,
            26670, 26680, 26690, 26700, 26710, 26720, 26730, 26740, 26750, 26760, 26770, 26780, 26790, 26800, 26810,
            27010, 27020, 27030, 27040, 27050, 27070, 27080]
    result += getCalculatedSimpleConsumption(dataRowsSimple, knu)

    // графа 9
    // Код вида расхода = 24650, 26910, 26920, 26930, 26940, 26950, 26960, 26970, 26980, 26990, 27000
    result -= getComplexConsumptionSumRows9(dataRows, [24650, 26910, 26920, 26930, 26940, 26950, 26960, 26970, 26980, 26990, 27000])

    return getLong(result)
}

/**
 * Получить налоговую базу для исчисления налога (НалБазаИсч, НалБазаОрг).
 *
 * @param row100 налоговая база
 * @param row110 сумма убытка или части убытка, уменьшающего налоговую базу за отчетный (налоговый) период
 */
def getNalBazaIsch(def row100, def row110) {
    def result
    if (row100 != null && row110 != null && (row100 < 0 || row100 == row110)) {
        result = 0.0
    } else {
        result = row100 - row110
    }
    return getLong(result)
}

/**
 * Получить сумму налога на прибыль к доплате в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма исчисленного налога на прибыль
 * @param value2 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value3 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 */
def getNalDopl(def value1, def value2, value3) {
    def result = 0
    if ((value1 - value2 - value3) > 0) {
        result = value1 - value2 - value3
    }
    return getLong(result)
}

/** Получить сумму исчисленного налога на прибыль, в федеральный бюджет (НалИсчислФБ). */
def getNalIschislFB(def row120, row150) {
    return getLong(row120 * row150 / 100)
}

/**
 * Получить сумму налога на прибыль к уменьшению в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value2 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 * @param value3 сумма исчисленного налога на прибыль
 */
def getNalUmen(def value1, def value2, value3) {
    def result = 0
    if ((value1 + value2 - value3) > 0) {
        result = value1 + value2 - value3
    }
    return getLong(result)
}

/**
 * Косвенные расходы, всего (КосвРасхВс).
 *
 * @param dataRows строки нф расходы сложные
 * @param dataRowsSimple строки нф расходы простые
 */
def getCosvRashVs(def dataRows, def dataRowsSimple) {
    def result = 0

    // Код вида расхода = 20020, 20030, 20050, 20130, 20200, 20210, 20280, 20470, 20490,
    // 20500, 20510, 20630, 20820, 20850, 20930, 20940, 20950, 20960, 20970, 20980, 21170
    result += getComplexConsumptionSumRows9(dataRows, [20020, 20030, 20050, 20130, 20200, 20210, 20280, 20470, 20490,
            20500, 20510, 20630, 20820, 20850, 20930, 20940, 20950, 20960, 20970, 20980, 21170])

    // Код вида расхода = 20000, 20010, 20040, 20060, 20070, 20080, 20090, 20100, 20110,
    // 20120, 20140, 20150, 20160, 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300,
    // 20310, 20320, 20330, 20340, 20350, 20360, 20370, 20380, 20400, 20410, 20430, 20450, 20530, 20540, 20550,
    // 20560, 20570, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660, 20670, 20680, 20690, 20700, 20710,
    // 20720, 20730, 20740, 20750, 20760, 20770, 20780, 20790, 20800, 20810, 20830, 20860, 20870, 20880, 20890,
    // 20900, 20910, 20920, 20990, 21000, 21010, 21020, 21040, 21120, 21130, 21140, 21150, 21160, 21180, 21190,
    // 21200, 21210, 21220, 21340, 21350, 21360, 21370, 21380, 21430, 21440, 21450, 21460, 21470, 21480, 21490
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [20000, 20010, 20040, 20060, 20070, 20080, 20090, 20100, 20110,
            20120, 20140, 20150, 20160, 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300,
            20310, 20320, 20330, 20340, 20350, 20360, 20370, 20380, 20400, 20410, 20430, 20450, 20530, 20540, 20550,
            20560, 20570, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660, 20670, 20680, 20690, 20700, 20710,
            20720, 20730, 20740, 20750, 20760, 20770, 20780, 20790, 20800, 20810, 20830, 20860, 20870, 20880, 20890,
            20900, 20910, 20920, 20990, 21000, 21010, 21020, 21040, 21120, 21130, 21140, 21150, 21160, 21180, 21190,
            21200, 21210, 21220, 21340, 21350, 21360, 21370, 21380, 21430, 21440, 21450, 21460, 21470, 21480, 21490])

    // графа 5
    // Код вида дохода = 20010, 20110, 20120, 20160,
    // 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
    // 20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
    // 20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
    // 21450, 21460, 21470
    result += getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20010, 20110, 20120, 20160,
            20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
            20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
            20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
            21450, 21460, 21470])

    // графа 6
    // Код вида дохода = 20010, 20110, 20120, 20160,
    // 20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
    // 20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
    // 20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
    // 21450, 21460, 21470
    result -= getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20010, 20110, 20120, 20160,
            20170, 20180, 20190, 20220, 20230, 20240, 20250, 20260, 20270, 20290, 20300, 20310, 20320, 20330, 20340,
            20350, 20360, 20370, 20380, 20400, 20410, 20450, 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660,
            20670, 20680, 20690, 20700, 20730, 20740, 20750, 20760, 20780, 20800, 20810, 20830, 21040, 21380, 21440,
            21450, 21460, 21470])

    return getLong(result)
}

/**
 * Расходы в виде процентов по долговым обязательствам любого вида, в том числе процентов, начисленных по ценным бумагам и иным обязательствам, выпущенным (эмитированным) налогоплательщиком (РасхВнереалПр-ДО =  РасхВнереалПрДО).
 *
 * @param dataRows строки нф расходы сложные
 * @param dataRowsSimple строки нф расходы простые
 */
def getRashVnerealPrDO(def dataRows, def dataRowsSimple) {
    def result = 0
    // Код вида расхода = 24660, 24670
    result += getComplexConsumptionSumRows9(dataRows, [24660, 24670])

    // Код вида расхода = 24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070,
    // 24080, 24090, 24100, 24110, 24120, 24130, 24140, 24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220,
    // 24230, 24240, 24250, 24260, 24270, 24280, 24290, 24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370,
    // 24380, 24390, 24400, 24410, 24420, 24430, 24440, 24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520,
    // 24530, 24540, 24550, 24560, 24570, 24580, 24590, 24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700,
    // 24710, 24720, 24730, 24740, 24750, 24760, 24770, 24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850,
    // 24860, 24870, 24880, 24890, 24900, 24910, 24920, 24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000,
    // 25010, 25020, 25030, 25040, 25050, 25060, 25070, 25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150,
    // 25160, 25170, 25180, 25190, 25200, 25210, 25220, 25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300,
    // 25310, 25320, 25330, 25340, 25350, 25360, 25370, 25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450,
    // 25460, 25470, 25480, 25490, 25500, 25510, 25520, 25530, 25540, 25550
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [24000, 24010, 24020, 24030, 24040, 24050, 24060, 24070,
            24080, 24090, 24100, 24110, 24120, 24130, 24140, 24150, 24160, 24170, 24180, 24190, 24200, 24210, 24220,
            24230, 24240, 24250, 24260, 24270, 24280, 24290, 24300, 24310, 24320, 24330, 24340, 24350, 24360, 24370,
            24380, 24390, 24400, 24410, 24420, 24430, 24440, 24450, 24460, 24470, 24480, 24490, 24500, 24510, 24520,
            24530, 24540, 24550, 24560, 24570, 24580, 24590, 24600, 24610, 24620, 24630, 24640, 24680, 24690, 24700,
            24710, 24720, 24730, 24740, 24750, 24760, 24770, 24780, 24790, 24800, 24810, 24820, 24830, 24840, 24850,
            24860, 24870, 24880, 24890, 24900, 24910, 24920, 24930, 24940, 24950, 24960, 24970, 24980, 24990, 25000,
            25010, 25020, 25030, 25040, 25050, 25060, 25070, 25080, 25090, 25100, 25110, 25120, 25130, 25140, 25150,
            25160, 25170, 25180, 25190, 25200, 25210, 25220, 25230, 25240, 25250, 25260, 25270, 25280, 25290, 25300,
            25310, 25320, 25330, 25340, 25350, 25360, 25370, 25380, 25390, 25400, 25410, 25420, 25430, 25440, 25450,
            25460, 25470, 25480, 25490, 25500, 25510, 25520, 25530, 25540, 25550])
    return getLong(result)
}

/**
 * Штрафы, пени и иные санкции за нарушение договорных или долговых обязательств,
 * возмещение причиненного ущерба (РасхШтраф).
 *
 * @param dataRows строки нф расходы простые
 */
def getRashShtraf(def dataRows) {
    def result = 0

    // графа 8 + 5 - 6
    // Код вида доходов = 26410, 26420, 26430, 26440
    result += getCalculatedSimpleConsumption(dataRows, [26410, 26420, 26430, 26440])

    return getLong(result)
}

/**
 * Суммы налогов и сборов, начисленные в порядке, установленном законодательством Российской Федерации
 * о налогах и сборах, за исключением налогов, перечисленных в ст. 270 НК.
 *
 * @param dataRows строки расходы простые
 * @param dataRowsSimple строки нф расходы простые
 */
def getNalogi(def dataRows, def dataRowsSimple) {
    def result = 0

    // Код вида расхода = 20630
    result += getComplexConsumptionSumRows9(dataRows, [20630])

    // графа 8 + 5 - 6
    // Код вида расхода = 20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660
    result += getCalculatedSimpleConsumption(dataRowsSimple, [20580, 20590, 20600, 20610, 20620, 20640, 20650, 20660])

    return getLong(result)
}

// Получить значения для АвПлат1 (220), АвПлат2 (230), АвПлат3 (240)
def getAvPlats(def row, def reportPeriod) {
    def appl5List02Row120 = (row != null && row.everyMontherPaymentAfterPeriod != null ? row.everyMontherPaymentAfterPeriod : 0)
    def avPlat1
    def avPlat2
    def avPlat3
    if (reportPeriod.taxPeriod.year >= 2015 || reportPeriod.taxPeriod.year == 2015 && reportPeriod.order > 2) {
        // с 9 месяцев 2015
        def a = roundValue(appl5List02Row120 / 3, 0)
        avPlat1 = a
        avPlat2 = a
        avPlat3 = a
        def b = a * 3
        def c = appl5List02Row120 - b
        if (c == -1) {
            avPlat1--
        } else if (c == 1) {
            avPlat3++
        }
    } else {
        // до 9 месяцев 2015
        avPlat1 = (long) appl5List02Row120 / 3
        avPlat2 = avPlat1
        avPlat3 = getLong(appl5List02Row120 - avPlat1 - avPlat2)
    }
    return [avPlat1, avPlat2, avPlat3]
}

/**
 * Получить значение столбца итоговой строки из налоговой формы.
 *
 * @param dataRows строки нф
 * @param columnName название столбца
 * @return значение столбца
 */
def getTotalFromForm(def dataRows, def columnName) {
    if (dataRows != null && !dataRows.isEmpty()) {
        def totalRow = getDataRow(dataRows, 'total')
        return getLong(totalRow.getCell(columnName).value)
    }
    return 0
}

/**
 * Получить xml декларации.
 *
 * @param reportPeriodId
 * @param departmentId
 */
def getXmlStreamReader(def reportPeriodId, def departmentId, def acceptedOnly, def anyPrevDeclaration) {
    if (reportPeriodId != null) {
        // вид декларации 11 - декларация банка
        def declarationTypeId = 11
        def xml = getStreamReader(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        if (xml != null) {
            return xml
        } else if (anyPrevDeclaration) { // для новой декларации можно поискать в прошлом периоде другую декларацию (обычную Банка)
            declarationTypeId = 9
            return getStreamReader(declarationTypeId, departmentId, reportPeriodId, acceptedOnly)
        }
    }
    return null
}

def getStreamReader(def declarationTypeId, def departmentId, def reportPeriodId, def acceptedOnly) {
    def declarationData = declarationService.getLast(declarationTypeId, departmentId, reportPeriodId)
    if (declarationData != null && declarationData.id != null && (!acceptedOnly || declarationData.accepted)) {
        return declarationService.getXmlStreamReader(declarationData.id)
    }
    return null
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, def precision) {
    ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
}

List<String> getErrorTable(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.value == null || record.NAME.value.isEmpty()) {
        errorList.add("«Наименование для титульного листа»")
    }
    if (record.OKTMO == null || record.OKTMO.value == null) {
        errorList.add("«ОКТМО»")
    }
    if (record.TAX_ORGAN_CODE?.value == null || record.TAX_ORGAN_CODE.value.isEmpty()) {
        errorList.add("«Код налогового органа (кон.)»")
    }
    if (useTaxOrganCodeProm() && (record.TAX_ORGAN_CODE_PROM?.value == null || record.TAX_ORGAN_CODE_PROM.value.isEmpty())) {
        errorList.add("«Код налогового органа (пром.)»")
    }
    if (record.OKVED_CODE?.value == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN?.value == null || record.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованного обособленного подразделения»")
        }
        if (record.REORG_KPP?.value == null || record.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованного обособленного подразделения»")
        }
    }
    if (record.SIGNATORY_ID?.value == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME?.value == null || record.SIGNATORY_SURNAME.value.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME?.value == null || record.SIGNATORY_FIRSTNAME.value.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    def signatoryId = getRefBookValue(35, record?.SIGNATORY_ID?.value)?.CODE?.value
    if ((signatoryId != null && signatoryId == 2) && (record.APPROVE_DOC_NAME?.value == null || record.APPROVE_DOC_NAME.value.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя» (Признак лица, подписавшего документ = 2)")
    }
    if (record.TAX_PLACE_TYPE_CODE?.value == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return reportPeriod
}

@Field
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = getReportPeriod()
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()

    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    // Декларация Банка в статусе отличном от «Принята».
    if (!declarationData.accepted && record.TAX_RATE?.value == null) {
        errorList.add("«Ставка налога»! При расчёте экземпляра декларации атрибут «Ставка налога на прибыль, в федеральный бюджет» будет заполнен значением «0»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.value == null || !record.FORMAT_VERSION.value.equals(version)) {
        errorList.add("«Версия формата» (${record.FORMAT_VERSION.value?:'пустое значение'})! Ожидаемое значение «$version».")
    }
    errorList
}

/** Получение провайдера с использованием кеширования. */
def getProvider(def long providerId) {
    return formDataService.getRefBookProvider(refBookFactory, providerId, providerCache)
}

BigDecimal getXmlDecimal(def reader, String attrName) {
    def value = getXmlValue(reader, attrName)
    if (!value) {
        return null
    }
    return new BigDecimal(value)
}

String getXmlValue(XMLStreamReader reader, String attrName) {
    return reader?.getAttributeValue(null, attrName)
}

/** Получить строки формы. */
def getDataRows(def formDataCollection, def formTypeId) {
    List<FormData> formList = formDataCollection.records.findAll { it.getFormType().getId() == formTypeId }
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?:[])
    }
    return dataRows.isEmpty() ? null : dataRows
}

def getCellCount(def formDataCollection, def formTypeIdList) {
    List<FormData> formList = formDataCollection.records.findAll { formTypeIdList.contains(it.getFormType().getId()) }
    def cellCount = 0
    for (def form : formList) {
        cellCount += ((formDataService.getDataRowHelper(form)?.count ?: 0) * (form.getFormColumns().size()))
    }
    return cellCount
}

/** Отменить принятие. Проверить наличие декларации ОП. */
void сancelAccepted() {
    // вид декларации 19 - декларация ОП
    def declarationTypeId = 19

    if (declarationService.checkExistDeclarationsInPeriod(declarationTypeId, declarationData.departmentReportPeriodId)) {
        throw new Exception('Отменить принятие данной декларации Банка невозможно. Так как в текущем периоде создана декларация ОП по прибыли!')
    }
}

def getReportPeriod9month(def reportPeriod) {
    if (reportPeriod == null) {
        return null
    }
    def code = getRefBookValue(8, reportPeriod.dictTaxPeriodId)?.CODE?.value
    if (code == '34') { // период "год"
        return getReportPeriod9month(reportPeriodService.getPrevReportPeriod(reportPeriod.id))
    } else if (code == '33') { // период "9 месяцев"
        return reportPeriod
    }
    return null
}

/**
 * Получить значение ячейки фиксированной строки из налоговой формы.
 *
 * @param dataRows строки нф
 * @param columnName название столбца
 * @param alias алиас строки
 * @return значение столбца
 *
 */
def getAliasFromForm(def dataRows, def columnName, def alias) {
    if (dataRows != null && !dataRows.isEmpty()) {
        def aliasRow = getDataRow(dataRows, alias)
        return getLong(aliasRow.getCell(columnName).value)
    }
    return 0
}

// Получить параметры подразделения (из справочника 33)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(33).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить параметры подразделения (из справочника 330)
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(330).getRecords(getEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

// Загрузка всех записей справочников в кеш.
def fillRecordsMap(def refBookIds) {
    refBookIds.each { refBookId ->
        def provider = refBookFactory.getDataProvider(refBookId)
        def records = provider.getRecords(getEndDate(), null, null, null)
        if (records) {
            records.each { record ->
                def recordId = record.get(RefBook.RECORD_ID_ALIAS).numberValue
                def key = getRefBookCacheKey(refBookId, recordId)
                if (!refBookCache.containsKey(key)) {
                    refBookCache.put(key, refBookService.getRecordData(refBookId, recordId))
                }
            }
        }
    }
}

def generateXmlFileId(String taxOrganCodeProm, String taxOrganCode) {
    def departmentParam = getDepartmentParam()
    if (departmentParam) {
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def fileId = TaxType.INCOME.declarationPrefix + '_' +
                taxOrganCodeProm + '_' +
                taxOrganCode + '_' +
                departmentParam.INN?.value +
                declarationData.kpp + "_" +
                date + "_" +
                UUID.randomUUID().toString().toUpperCase()
        return fileId
    }
    return null
}

void calcTaskComplexity() {
    switch (taskComplexityHolder.alias) {
        case 'declaration_app2' :
            calcTaskComplexityApp2()
            break
        case 'declaration_no_app2' :
            calcTaskComplexityNoApp2()
            break
    }
}

void calcTaskComplexityApp2() {
    def formDataCollection = getAcceptedFormDataSources()

    taskComplexityHolder.setValue(getCellCount(formDataCollection, [415, 418]))
}

void calcTaskComplexityNoApp2() {
    def formDataCollection = getAcceptedFormDataSources()

    taskComplexityHolder.setValue(getCellCount(formDataCollection, [302, 305, 301, 305, 303, 304, 310, getAdvanceTypeId(), 414, 416, 412, 309, 421]))
}

@Field
def acceptedFormDataSources = null

def getAcceptedFormDataSources() {
    if (acceptedFormDataSources == null) {
        acceptedFormDataSources = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)
    }
    return acceptedFormDataSources
}

/** Получить form_type_id "авансовых платежей". До 1 кв 2016 (включительно) используется макет 500, после - 507. */
def getAdvanceTypeId() {
    def reportPeriod = getReportPeriod()
    def isAfterFirstQuarter2016 = (reportPeriod?.taxPeriod?.year > 2016 || reportPeriod?.order > 1)
    return (isAfterFirstQuarter2016 ? 507 : 500)
}