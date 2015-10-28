package form_template.income.declaration_bank_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по налогу на прибыль (Банк) (с периода год 2014)
 * Формирование XML для декларации налога на прибыль.
 * версия 2015 года
 * declarationTemplateId=21687
 *
 * @author Bulat.Kinzyabulatov
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
        generateXML()
        break
    default:
        return
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

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)
    def departmentFormType = formDataCollection?.records?.find { it.formType.id == sourceFormTypeId }
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
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

// Запуск генерации XML.
void generateXML() {

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
    def reportPeriod = reportPeriodService.get(reportPeriodId)

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

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)

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
    def dataRowsAdvance = getDataRows(formDataCollection, 500)

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
    def dataRowsApp2 = getDataRows(formDataCollection, 415)
    def isCFOApp2 = false

    // Приложение №2 "Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ)."
    def dataRowsApp2_old = getDataRows(formDataCollection, 418)
    if (dataRowsApp2 == null) {
        isCFOApp2 = true
        dataRowsApp2 = dataRowsApp2_old
    } else if (dataRowsApp2_old != null) {
        logger.warn("Неверно настроены источники декларации Банка! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Консолидация произведена из «%s».",
                formTypeService.get(415).name, formTypeService.get(418)?.name, formTypeService.get(415)?.name)
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
    /** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10840. */
    def viruchRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10840]))
    /** ПрибРеалАИ. Код строки декларации 040. Код вида дохода = 10845. */
    def pribRealAI = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10845]))
    /** УбытРеалАИ. Код строки декларации 060. Код вида расхода = 21780. */
    def ubitRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21780]))
    /** ЦенРеалПрЗУ. Код строки декларации 240. Код вида дохода = 10890. */
    def cenRealPrZU = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10890]))
    /** УбытРеалПрЗУ. Код строки декларации 260. Код вида расхода = 21390. */
    def ubitRealPrZU = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21390]))
    /** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10860. */
    def viruchRealPTDoSr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10860]))
    /** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. Не заполняется с 2015 года. */
    def viruchRealPTPosSr = getLong(0)
    /** Убыт1Соот269. Код строки декларации 140. Код вида расхода = 21490. */
    def ubit1Soot269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21490]))
    /** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21500. */
    def ubit1Prev269 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21500]))
    /** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
    def ubit2RealPT = empty
    /** Убыт2ВнРасх. Код строки декларации 170. Код вида расхода = 22700. */
    def ubit2VnRash = empty
    // Приложение № 3 к Листу 02 - конец

    /** ПрПодп. */
    def prPodp = signatoryId
    /** ВырРеалТовСоб. Код строки декларации 011. */
    def virRealTovSob = getVirRealTovSob(dataRowsComplexIncome, dataRowsSimpleIncome)
    /** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
    def virRealImPrav = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10855, 10870, 10880, 10900]))
    /** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
    def virRealImProch = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10850]))
    /** ВырРеалВс. Код строки декларации 010. */
    def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
    /** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
    def virRealCBVs = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260]))
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
    /** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21760. */
    def ostStRealAI = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21760]))
    /** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
    def realImushPrav = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21450, 21470, 21740, 21750]))
    /** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
    def priobrRealImush = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21770]))
    /* АктивРеалПред. Код строки декларации 061. */
    def activRealPred = empty
    /** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
    def priobrRealCB = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680]))
    /** СумОтклЦен. Код строки декларации 071. Код вида расходов = 21685, 21690, 21695. */
    def sumOtklCen = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21685, 21690, 21695]))
    /** ПриобРеалЦБОрг. Код строки декларации 072. Расходы, связанные с приобритением и реализацией (выбытием, в том числе погашением) ценных бумаг, обращающихся на организованном рынке ценных бумаг */
    def priobRealCBOrg = empty
    /** СумОтклЦенОрг. Код строки декларации 073. Суммы отклонения от максимальной (расчетной) цены */
    def sumOtklCenOrg = empty

    /** УбытПрошОбсл. Код строки декларации 090. */
    def ubitProshObsl = empty
    /** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21460. */
    def stoimRealPTDoSr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21460]))
    /** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. Не заполняется с 2015 года. */
    def stoimRealPTPosSr = getLong(0)
    /** РасхРеалТов. Код строки декларации 190. */
    def rashRealTov = empty
    /** РасхДоговДУИ. Код строки декларации 220. */
    def rashDolgovDUI = empty
    /** РасхДоговДУИ_ВнР. Код строки декларации 221. */
    def rashDolgovDUI_VnR = empty
    /** НеВозЗатрПрЗУ. Код строки декларации 250. Код вида расхода = 21385. */
    def neVozZatrPrZU = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21385]))
    /** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
    def rashOper32 = ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr + rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + neVozZatrPrZU
    /** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21520, 21530. */
    def ubitRealAmIm = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21520, 21530]))
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
    /** ОтклВырЦБОбр. Код строки декларации 021. Код вида дохода = 11270, 11280, 11290. */
    def otklVirCBOrb = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11270, 11280, 11290]))
    /** ОтклВырЦБНеОбр. Код строки декларации 022. Код вида дохода = 11300, 11310. */
    def otklVirCBNeObr = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [11300, 11310]))
    /** ВнеРеалДохВс. Код строки декларации 100. */
    def vneRealDohVs = dohVnereal
    /** ВнеРеалДохСт. Код строки декларации 102. Код вида дохода = 13250. */
    def vneRealDohSt = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [13250]))
    /** ВнеРеалДохБезв. Код строки декларации 103. Код вида дохода = 13150. */
    def vneRealDohBezv = getLong(getSimpleIncomeSumRows8(dataRowsSimpleIncome, [13150]))
    /** ВнеРеалДохИзл. Код строки декларации 104. */
    def vneRealDohIzl = getLong(getSimpleIncomeSumRows8(dataRowsSimpleIncome, [13410]))
    /** ВнеРеалДохВРасх. Код строки декларации 105. Код вида дохода = 10910. */
    def vneRealDohVRash = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [10910]))
    /** ВнеРеалДохРынЦБДД. Код строки декларации 106. Код вида дохода = 13940, 13950, 13960, 13970, 13980, 13990. */
    def vneRealDohRinCBDD = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [13940, 13950, 13960, 13970, 13980, 13990]))
    /** ВнеРеалДохКор. Код строки декларации 107. Код вида дохода = 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320. */
    def vneRealDohCor = getLong(getComplexIncomeSumRows9(dataRowsComplexIncome, [14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320]))
    /** Налоги. Код строки декларации 041. */
    def nalogi = getNalogi(dataRowsSimpleConsumption)
    /** РасхКапВл10. Код строки декларации 042. Код вида расхода = 20760. */
    def rashCapVl10 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20760]))
    /** РасхКапВл30. Код строки декларации 043. Код вида расхода = 20765. */
    def rashCapVl30 = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20765]))
    /** РасхЗемУч30пр. Код строки декларации 049. Код вида расхода = 21370. */
    def rashZemUch30pr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21370]))
    /** РасхЗемУчСрокРас. Код строки декларации 050. Код вида расхода = 21380. */
    def rashZemUchSrocRas = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21380]))
    /** РасхЗемУчСрокАр. Код строки декларации 051. Код вида расхода = 21375. */
    def rashZemUchSrocAr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21375]))
    /** РасхЗемУчВс. Код строки декларации 047. Код вида дохода = 21370, 21375, 21380. */
    def rashZemUchVs = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [21370, 21375, 21380]))

    /** СумАмортПерЛ. Код строки декларации 131. Код вида расхода = 20750, 20755, 20770, 20775, 20780, 20785. */
    def sumAmortPerL = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20750, 20755, 20770, 20775, 20780, 20785]))
    /** СумАмортПерНмАЛ. Код строки декларации 132. Код вида расхода = 20755. */
    def sumAmortPerNmAL = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [20755]))
    /** РасхВнереалПрДО. Код строки декларации 201. */
    def rashVnerealPrDO = getLong(getRashVnerealPrDO(dataRowsComplexConsumption, dataRowsSimpleConsumption))
    /** УбытРеалПравТр. Код строки декларации 203. Код вида расхода = 22695, 22700. */
    def ubitRealPravTr = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [22695, 22700]))
    /** РасхЛиквОС. Код строки декларации 204. Код вида расхода = 22690. */
    def rashLikvOS = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [22690]))
    /** РасхШтраф. Код строки декларации 205. */
    def rashShtraf = getRashShtraf(dataRowsSimpleConsumption)
    /** РасхРынЦБДД. Код строки декларации 206. Код вида расхода = 23120, 23130, 23140. */
    def rashRinCBDD = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, [23120, 23130, 23140]))

    // Приложение к налоговой декларации
    /** СвЦелСред - блок. Табл. 34. Алгоритмы заполнения отдельных атрибутов «Приложение к налоговой декларации»  декларации Банка по налогу на прибыль. */
    def svCelSred = new HashMap()
    if (dataRowsComplexConsumption != null) {
        // 700, 770, 890
        [700: [20750], 770: [20321], 890: [21280]].each { id, codes ->
            def result = getLong(getComplexConsumptionSumRows9(dataRowsComplexConsumption, codes))
            if (result != 0) {
                svCelSred[id] = result
            }
        }
    }
    if (dataRowsSimpleConsumption != null) {
        // 780, 811, 812, 813, 940, 950
        [780:[20530], 811:[20700], 812:[20698], 813:[20690], 940:[23040], 950:[23050]].each { id, codes ->
            def result = getLong(getCalculatedSimpleConsumption(dataRowsSimpleConsumption, codes))
            if (result != 0) {
                svCelSred[id] = result
            }
        }
        // 790
        [790:[20501]].each { id, codes ->
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
                            def avPlat1 = (long) list02Row300 / 3
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
                                    РасхЗемУчСрокРас : rashZemUchSrocRas,
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
                                // УбытРеалПравТр : ubitRealPravTr, не заполняется с 2015 года
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
                        dataRowsDividend.each { row ->
                            // Лист 03 А
                            // 0..n
                            НалДохДив(
                                    [КатегорНА : row.taCategory] +
                                        (row.inn ? [ИННЮЛ_ЭмЦБ : row.inn] : [:]) +
                                    [ВидДив : row.dividendType,
                                    НалПер : row.taxPeriod,
                                    ОтчетГод : row.financialYear.format('yyyy'),
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
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [13655, 13675, 13705, 13780, 13785, 13790])
                            stavNal = 15
                            break
                        case 2:
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [13660, 13680, 13695, 13710])
                            stavNal = 9
                            break
                        case 3:
                            nalBaza04 = getComplexIncomeSumRows9(dataRowsComplexIncome, [13665, 13685, 13690])
                            stavNal = 0
                            break
                        case 4:
                            nalBaza04 = getSimpleIncomeSumRows8(dataRowsSimpleIncome, [14010])
                            stavNal = 13
                            nalDivNeRFPred = (isFirstPeriod ? 0 : nalDivNeRFPredOld + nalDivNeRFOld)
                            nalDivNeRF = (getAliasFromForm(dataRowsSum, 'taxSum', 'SUM_DIVIDENDS') ?: 0)
                            break
                        case 5:
                            nalBaza04 = getSimpleIncomeSumRows8(dataRowsSimpleIncome, [14015])
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

                // Приложение к налоговой декларации
                if (svCelSred.size() > 0) {
                    def tmpArray = []
                    svCelSred.each { id, value ->
                        tmpArray.add(id)
                    }

                    // 0..1
                    ДохНеУч_РасхУч() {
                        tmpArray.sort().each { id ->
                            // 1..n
                            СвЦелСред(
                                    КодВидРасход : id,
                                    СумРасход : getLong(svCelSred[id]))
                        }
                    }
                }
                // Приложение к налоговой декларации - конец

                // Приложение №2 к налоговой декларации - формирутеся только для периода "год"
                if (isTaxPeriod) {
                    // сортируем по ФИО, потом по остальным полям
                    if (isCFOApp2 && dataRowsApp2 != null) {
                        def sortColumns = ['surname', 'name', 'patronymic', 'innRF', 'inn', 'taxRate']
                        sortRows(dataRowsApp2, sortColumns)
                    }

                    //ДатаСправ   Дата составления
                    def dataSprav = (docDate != null ? docDate : new Date()).format("dd.MM.yyyy")
                    //Тип         Тип
                    def type = String.format("%02d", reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId))

                    if (dataRowsApp2) {
                        fillRecordsMap([4L, 10L, 350L, 360L, 370L])
                    }

                    def index = 0
                    for (def row : dataRowsApp2) {
                        //НомерСправ  Справка №
                        def nomerSprav = isCFOApp2 ? (++index) : row.refNum
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
                        СведДохФЛ(
                                НомерСправ : nomerSprav,
                                ДатаСправ : dataSprav,
                                Тип : type) {
                            //1..1
                            ФЛПолучДох(
                                    ИННФЛ : innFL,
                                    ИННИно : innIno,
                                    СтатусНП : statusNP,
                                    ДатаРожд : dataRozhd,
                                    Гражд : grazhd,
                                    КодВидДок : kodVidDok,
                                    СерНомДок : serNomDok
                            ) {
                                // 1..1
                                ФИО([Фамилия : surname, Имя : givenName] + (parentName != null ? [Отчество : parentName] : []))
                                //0..1
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
                                //0..1
                                АдрМЖИно(ОКСМ : oksm, АдрТекст : adrText)
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
                            //0..1
                            СпрДохФЛ() {
                                3.times{ index_1 ->
                                    //КодДоход    040 (Код дохода)
                                    def kodDohod040 = getRefBookValue(370, row["col_040_${index_1 + 1}"])?.CODE?.value
                                    //СумДоход    041 (Сумма дохода)
                                    def sumDohod041 = row["col_041_${index_1 + 1}"]

                                    СумДох(КодДоход : kodDohod040, СумДоход : sumDohod041) {
                                        5.times{ index_2 ->
                                            //КодВычет    042 (Код вычета)
                                            def kodVichet042 = getRefBookValue(350, row["col_042_${index_1 + 1}_${index_2 + 1}"])?.CODE?.value
                                            //СумВычет    043 (Сумма вычета)
                                            def sumVichet043 = row["col_043_${index_1 + 1}_${index_2 + 1}"]

                                            //1..n
                                            СумВыч(КодВычет : kodVichet042, СумВычет : sumVichet043)
                                        }
                                    }
                                }
                            }

                            //0..1
                            НалВычСтанд() {
                                2.times{ index_1 ->
                                    //КодВычет    051 (Код вычета)
                                    def kodVichet051 = getRefBookValue(350, row["col_051_3_${index_1 + 1}"])?.CODE?.value
                                    //СумВычет    052 (Сумма вычета)
                                    def sumVichet052 = row["col_052_3_${index_1 + 1}"]
                                    //1..n
                                    СумВыч(КодВычет : kodVichet051, СумВычет : sumVichet052)
                                }
                            }
                        }
                    }
                }
                // Приложение №2 к налоговой декларации - конец
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

    // Код вида дохода = 10633, 10634, 10650, 10670
    result += getComplexIncomeSumRows9(dataRows, [10633, 10634, 10650, 10670])

    // Код вида дохода = 10001, 10006, 10041, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370,
    // 10380, 10390, 10450, 10460, 10470, 10480, 10490, 10571, 10580, 10590, 10600, 10610, 10630,
    // 10631, 10632, 10640, 10680, 10690, 10740, 10744, 10748, 10752, 10756, 10760, 10770, 10790,
    // 10800, 11140, 11150, 11160, 11170, 11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375
    result += getSimpleIncomeSumRows8(dataRowsSimple, [10001, 10006, 10041, 10300, 10310, 10320,
                                                       10330, 10340, 10350, 10360, 10370, 10380, 10390, 10450, 10460, 10470, 10480, 10490,
                                                       10571, 10580, 10590, 10600, 10610, 10630, 10631, 10632, 10640, 10680, 10690, 10740,
                                                       10744, 10748, 10752, 10756, 10760, 10770, 10790, 10800, 11140, 11150, 11160, 11170,
                                                       11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375])

    // Код вида доходов = 10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470,
    // 10480, 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375
    def codes = [10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470, 10480,
                 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375]

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

    // Код вида дохода = 11405, 11410, 11415, 13040, 13045, 13050, 13055, 13060, 13065,
    // 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665, 13670,
    // 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
    // 13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180,
    // 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290,
    // 14300, 14310, 14320
    result += getComplexIncomeSumRows9(dataRows, [11405, 11410, 11415, 13040, 13045, 13050, 13055,
                                                  13060, 13065, 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665,
                                                  13670, 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
                                                  13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180, 14190,
                                                  14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290, 14300, 14310, 14320])

    // Код вида дохода = 11380, 11385, 11390, 11395, 11400, 11420, 11430, 11840, 11850, 11855,
    // 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030, 12050, 12070, 12090, 12110, 12130,
    // 12150, 12170, 12190, 12210, 12230, 12250, 12270, 12290, 12320, 12340, 12360, 12390, 12400,
    // 12410, 12420, 12430, 12830, 12840, 12850, 12860, 12870, 12880, 12890, 12900, 12910, 12920,
    // 12930, 12940, 12950, 12960, 12970, 12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035,
    // 13080, 13130, 13140, 13150, 13160, 13170, 13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330,
    // 13340, 13400, 13410, 13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040,
    // 14050, 14060, 14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160
    result += getSimpleIncomeSumRows8(dataRowsSimple, [11380, 11385, 11390, 11395, 11400, 11420,
                                                       11430, 11840, 11850, 11855, 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030,
                                                       12050, 12070, 12090, 12110, 12130, 12150, 12170, 12190, 12210, 12230, 12250, 12270,
                                                       12290, 12320, 12340, 12360, 12390, 12400, 12410, 12420, 12430, 12830, 12840, 12850,
                                                       12860, 12870, 12880, 12890, 12900, 12910, 12920, 12930, 12940, 12950, 12960, 12970,
                                                       12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035, 13080, 13130, 13140, 13150, 13160, 13170,
                                                       13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330, 13340, 13400, 13410,
                                                       13725, 13730, 13920, 13925, 13930, 14000, 14010, 14015, 14020, 14030, 14040, 14050, 14060,
                                                       14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160])

    // Код вида дохода = 11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
    // 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160
    def codes = [11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
                 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160]
    // графа 5
    result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

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
        // Код вида доходов = 13655, 13660, 13665, 13675, 13680, 13685, 13690,
        // 13695, 13705, 13710, 13780, 13785, 13790
        result += getComplexIncomeSumRows9(dataRowsComplex,
                [13655, 13660, 13665, 13675, 13680, 13685, 13690, 13695, 13705, 13710, 13780, 13785, 13790])
    }
    if (dataRowsSimple != null) {
        // Код вида дохода = 14000
        result += getSumRowsByCol(dataRowsSimple, 'incomeTypeId', 'rnu4Field5Accepted', [14000, 14010, 14015])
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

    // Код вида расхода = 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
    // 22670, 22690, 22695, 23120, 23130, 23140, 23240 - графа 9
    result += getComplexConsumptionSumRows9(dataRows, [22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
                                                       22670, 22690, 22695, 23120, 23130, 23140, 23240])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100, 22110,
    // 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260,
    // 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
    // 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455, 22460, 22465, 22470,
    // 22475, 22480, 22485, 22490, 22496, 22498, 22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565,
    // 22570, 22575, 22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800, 22810, 22840,
    // 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210, 23220, 23230, 23250, 23260, 23270, 23280
    def knu = [ 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070,
                22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180,
                22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
                22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390,
                22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445,
                22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498,
                22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565, 22570, 22575,
                22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800,
                22810, 22840, 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210,
                23220, 23230, 23250, 23260, 23270, 23280 ]
    result += getCalculatedSimpleConsumption(dataRowsSimple, knu)

    // Код вида расхода = 22492, 23150, 23160, 23170 - графа 9
    result -= getComplexConsumptionSumRows9(dataRows, [22492, 23150, 23160, 23170])

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

    // Код вида расхода = 20320, 20321, 20470, 20750, 20755, 20760, 20765, 20770,
    // 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640
    result += getComplexConsumptionSumRows9(dataRows, [20320, 20321, 20470, 20750, 20755, 20760, 20765,
                                                       20770, 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640])

    // Код вида расхода = 20291, 20300, 20310, 20330, 20332, 20334, 20336, 20338,
    // 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440, 20442,
    // 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
    // 20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694,
    // 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840,
    // 20850, 20860, 20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970,
    // 21020, 21025, 21030, 21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150,
    // 21154, 21158, 21170, 21270, 21290, 21295, 21300, 21305, 21310, 21315, 21320,
    // 21325, 21340, 21350, 21360, 21400, 21405, 21410, 21580, 21590, 21600, 21610,
    // 21620, 21660, 21700, 21710, 21720, 21730, 21790, 21800, 21810
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [20291, 20300, 20310, 20330, 20332, 20334,
                                                            20336, 20338, 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440,
                                                            20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
                                                            20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694, 20698,
                                                            20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840, 20850, 20860,
                                                            20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970, 21020, 21025, 21030,
                                                            21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21270,
                                                            21290, 21295, 21300, 21305, 21310, 21315, 21320, 21325, 21340, 21350, 21360, 21400,
                                                            21405, 21410, 21580, 21590, 21600, 21610, 21620, 21660, 21700, 21710, 21720, 21730,
                                                            21790, 21800, 21810])

    // графа 5
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810
    result += getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20300, 20360, 20370, 20430,
                                                                                      20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                      20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                      20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                      20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                      21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                      21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810])

    // графа 6
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 2162021660, 21700, 21710, 21730, 21790, 21800, 21810
    result -= getSumRowsByCol(dataRowsSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20300, 20360, 20370, 20430,
                                                                                           20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
                                                                                           20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
                                                                                           20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
                                                                                           20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
                                                                                           21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
                                                                                           2162021660, 21700, 21710, 21730, 21790, 21800, 21810])

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
    // Код вида расхода = 22500, 22505
    result += getComplexConsumptionSumRows9(dataRows, [22500, 22505])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100,
    // 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230,
    // 22240, 22250, 22260, 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360,
    // 22370, 22380, 22385, 22390, 22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435,
    // 22440, 22445, 22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498
    result += getSimpleConsumptionSumRows8(dataRowsSimple, [22000, 22010, 22020, 22030, 22040, 22050,
                                                            22060, 22070, 22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170,
                                                            22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
                                                            22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
                                                            22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455,
                                                            22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498])
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
    // Код вида доходов = 22750, 22760, 22800, 22810
    def codes = [22750, 22760, 22800, 22810]

    result += getSimpleConsumptionSumRows8(dataRows, codes)

    // графа 5
    result += getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu7Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu7Field12Accepted', codes)

    return getLong(result)
}

/**
 * Суммы налогов и сборов, начисленные в порядке, установленном законодательством Российской Федерации
 * о налогах и сборах, за исключением налогов, перечисленных в ст. 270 НК.
 *
 * @param dataRows строки расходы простые
 */
def getNalogi(def dataRows) {
    def result = 0

    // Код вида расхода = 20830, 20840, 20850, 20860, 20870, 20880, 20890
    result += getSimpleConsumptionSumRows8(dataRows, [20830, 20840, 20850, 20860, 20870, 20880, 20890])

    // графа 5
    // Код вида дохода = 20830, 20840, 20850, 20870, 20880, 20890
    result += getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu7Field10Sum',
            [20830, 20840, 20850, 20870, 20880, 20890])

    // графа 6
    // Код вида дохода = 20830, 20840, 20850, 20870, 20880, 20890
    result -= getSumRowsByCol(dataRows, 'consumptionTypeId', 'rnu7Field12Accepted',
            [20830, 20840, 20850, 20870, 20880, 20890])

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
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
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

/**
 * Получение провайдера с использованием кеширования.
 *
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
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
    List<FormData> formList = formDataCollection.records.findAll { it.getFormType().getId() == formTypeId };
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?:[])
    }
    return dataRows.isEmpty() ? null : dataRows
}

/** Отменить принятие. Проверить наличие декларации ОП. */
void сancelAccepted() {
    // вид декларации 19 - декларация ОП
    def declarationTypeId = 19

    if (declarationService.checkExistDeclarationsInPeriod(declarationTypeId, declarationData.reportPeriodId)) {
        throw new Exception('Отменить принятие данной декларации Банка невозможно. Так как в текущем периоде создана декларация ОП по прибыли!')
    }
}

def getReportPeriod9month(def reportPeriod) {
    if (reportPeriod == null) {
        return null;
    }
    def code = getRefBookValue(8, reportPeriod.dictTaxPeriodId)?.CODE?.value
    if (code == '34') { // период "год"
        return getReportPeriod9month(reportPeriodService.getPrevReportPeriod(reportPeriod.id));
    } else if (code == '33') { // период "9 месяцев"
        return reportPeriod;
    }
    return null;
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
                    refBookCache.put(key, refBookService.getRecordData(refBookId, recordId));
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
