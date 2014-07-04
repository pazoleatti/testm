import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DateColumn
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormTemplate
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.Formats
import com.aplana.sbrf.taxaccounting.model.NumericColumn
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import org.springframework.jndi.JndiTemplate
import groovy.transform.Field

import javax.sql.DataSource
import java.nio.charset.Charset
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

/**
 * Скрипт для подготовки примеров транспортных файлов для всех видов первичных налоговых форм.
 *
 *      - при массовой выгрузке выдает ошибку (что то с таймаутами), но файлы выгржаются нормально
 *      - для рну 36.2 после выгрузки надо выставить название разделов А и Б в конце строк
 */

calcData()
loadData()
createFiles()

// Папка для сохранения транспортных файлов
@Field
String path = "C:\\temp\\tf\\test\\"

// TODO (Ramil Timerbaev) надо задать параметры при которых найдутся все формы
@Field
def departmentId = 4
@Field
def taxPeriodId = 10002
@Field
def reportPeriodId = 103
@Field
def periodOrder = 1

enum Exclude {
    FIX("all"), // исключать все фиксированные строки
    TOTAL("total"), // исключать строки содержащие в алиасе total
    SECTION_TOTAL("total"), // исключать строки содержащие в алиасе total (для форм с разделами)
    SECTION2_TOTAL("total"), // исключать строки содержащие в алиасе total (для форм с разделами и подразделами - рну 39.1 и 39.2)
    EXCEPT_CONTAIN_TOTAL("total"), // исключать строки не содержащие в алиасе total но не равные total (для рну 36.2)
    NO(""); // не исплючать строки
    private Exclude(def v) {
        value = v
    }
    final String value
}

// список id макетов
@Field
def formTemplateIds = []

// для получения списка макетов форм по прибыли
// select ft.id, ft.name from form_template ft, form_type f where f.id = ft.type_id and f.tax_type = 'I' order by ft.name

// мапа с id маков и с признаком того как обрабатывать формы с аливасами:
// 1. исключать все фиксированные/итоговые/подитоговые - Exclude.FIX
// 2. не исключать строки, обрабатывать все строки (для фиксированных форм) - Exclude.NO (или пустая строка)
// 3. исплючать строки если в их алиасе есть заданное значение (например «total») - Exclude.SECTION_TOTAL
// макеты форм (372, 364, 321, 499, 501, 322, 323, 1324, 1325, 1326, 329, 1328, 330, 331, 332, 333, 315, 334, 335, 336, 337, 316, 338, 339, 340, 341, 342, 344, 343, 313, 312, 317, 365, 348, 349, 353, 318, 352, 354, 1355, 311, 504, 357, 356, 503, 358, 366, 320, 362)
@Field
def formTemplatesExcludeRowMap = [
        372  : Exclude.FIX,           // (Приложение 5) Сведения для расчета налога на прибыль
        364  : Exclude.FIX,           // (РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам
        321  : Exclude.FIX,           // (РНУ-14) Регистр налогового учёта нормируемых расходов
        499  : Exclude.FIX,           // (РНУ-16) Регистр налогового учёта доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учётной политикой для целей налогообложения ОАО «Сбербанк России»
        501  : Exclude.FIX,           // (РНУ-17) Регистр налогового учёта расходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учётной политикой для целей налогообложения ОАО «Сбербанк России»
        322  : Exclude.FIX,           // (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования
        323  : Exclude.FIX,           // (РНУ-23) Регистр налогового учёта доходов по выданным гарантиям
        1324 : Exclude.FIX,           // (РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения
        1325 : Exclude.FIX,           // (РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения
        1326 : Exclude.FIX,		      // (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
        329  : Exclude.SECTION_TOTAL, // (РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.
        1328 : Exclude.NO,            // (РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям
        330  : Exclude.SECTION_TOTAL, // (РНУ-32.1) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 1
        331  : Exclude.FIX,		      // (РНУ-32.2) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 2
        332  : Exclude.FIX,		      // (РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО
        333  : Exclude.SECTION_TOTAL, // (РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1
        315  : Exclude.EXCEPT_CONTAIN_TOTAL,      // (РНУ-36.2) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 2
        334  : Exclude.FIX,		      // (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1
        335  : Exclude.NO, 		      // (РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2
        336  : Exclude.SECTION_TOTAL, // (РНУ-39.1) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 1
        337  : Exclude.SECTION_TOTAL, // (РНУ-39.2) Регистр налогового учёта процентного дохода по коротким позициям
        316  : Exclude.FIX,		      // (РНУ-4) Простой регистр налогового учёта «доходы»
        338  : Exclude.SECTION_TOTAL, // (РНУ-40.1) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 1
        339  : Exclude.SECTION_TOTAL, // (РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2
        340  : Exclude.FIX,		      // (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее, чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств введённых в эксплуатацию после 01.01.2013
        341  : Exclude.FIX,		      // (РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»
        342  : Exclude.FIX,		      // (РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»
        344  : Exclude.NO, 		      // (РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам, а также расходов в виде капитальных вложений»
        343  : Exclude.FIX,		      // (РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
        313  : Exclude.TOTAL,    	  // (РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.»
        312  : Exclude.SECTION_TOTAL, // (РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»
        317  : Exclude.FIX,		      // (РНУ-5) Простой регистр налогового учёта «расходы»
        365  : Exclude.FIX,		      // (РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»
        348  : Exclude.FIX,		      // (РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов
        349  : Exclude.FIX,		      // (РНУ-56) Регистр налогового учёта процентного дохода по дисконтным векселям сторонних эмитентов
        353  : Exclude.FIX,		      // (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
        318  : Exclude.FIX,		      // (РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления
        352  : Exclude.FIX,		      // (РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России», учёт которых требует применения метода начисления
        354  : Exclude.FIX,		      // (РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»
        1355 : Exclude.FIX,    	      // (РНУ-64) Регистр налогового учёта затрат, связанных с проведением сделок РЕПО
        311  : Exclude.FIX,		      // (РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления
        504  : Exclude.FIX,		      // (РНУ-70.1) Регистр налогового учёта уступки права требования до наступления, предусмотренного кредитным договором срока погашения основного долга
        357  : Exclude.FIX,		      // (РНУ-70.2) Регистр налогового учёта уступки права требования до наступления, предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
        356  : Exclude.FIX,		      // (РНУ-71.1) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга
        503  : Exclude.FIX,		      // (РНУ-71.2) Регистр налогового учёта уступки права требования после предусмотренного кредитным договором срока погашения основного долга в отношении сделок уступки прав требования в пользу Взаимозависимых лиц и резидентов оффшорных зон
        358  : Exclude.FIX,		      // (РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными
        366  : Exclude.FIX,		      // (РНУ-75) Регистр налогового учета доходов по операциям депозитария
        320  : Exclude.FIX,		      // (РНУ-8) Простой регистр налогового учёта «Требования»
        362  : Exclude.SECTION_TOTAL  // (Ф 7.8) Реестр совершенных операций с ценными бумагами по продаже и погашению, а также по открытию-закрытию короткой позиции
]

// для этих макетов нумерация разделов буквами
@Field
def sectionsMap = [
        329 : ['А', 'Б'],    // (РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.
        333 : ['А', 'Б'],    // (РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1
]

@Field
def refBookCache = [:]

// хранит некоторую информацию о макете, доступ по id шаблона формы
@Field
def formTemplatesMap = [:]

// хранит информацию о стобцах формы, доступ по id шаблона формы
@Field
def columnsMap = [:]

// хранит информацию о скрытых столбцах, доступ по id столбца
@Field
def hiddenColumnsMap = [:]

// хранит информацию об атрибутах справочников, доступ по id атрибута
@Field
def refBookAttributesMap = [:]

@Field
def formDataMap = [:]

// год для названия формы
@Field
def year = null

// отчетный период
@Field
def reportPeriod = null

@Field
def END_ROW = "\r\n"

@Field
def SEPARATOR = "|"

// id текущей формы для вывода в лог
@Field
def currFormId = null

@Field
def currRow = null

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def createFiles() {
    // получение форм
    def dataRowsMap = getAllDataRows()

    dataRowsMap.each { id, dataRows ->
        currFormId = formDataMap[id].id

        FormTemplate template = formTemplatesMap[id]
        StringBuilder sb

        // заголовок
        sb = getHeader(template)
        sb.append(END_ROW)

        // блок данных
        Exclude exclude = formTemplatesExcludeRowMap[id]
        def index = 0
        def sectionIndex = 0
        def sectionValue = (id == 329 ? '0' : null) // для макета 329 (рну 30) первые строки это раздел 0
        for (def row : dataRows) {
            def alias = row.getAlias()
            // для форм имеющих разделы, если строка не содержит в алиасе 'total' (значит это заголовок), то это новый раздел
            if (exclude == Exclude.SECTION_TOTAL && alias != null && !alias.contains(Exclude.SECTION_TOTAL.value)) {
                // наращивает номер раздела
                sectionValue = (sectionsMap[id] != null ? sectionsMap[id][sectionIndex++] : ++sectionIndex)
            } else if (exclude == Exclude.SECTION2_TOTAL && alias != null && !alias.contains(Exclude.SECTION_TOTAL.value) && alias.size() == 2) {
                // определяет номер раздела и подраздела для рну 39.1 и 39.2 - если алиас строки A1 или B1,  то в конце строки тф A|1 или Б|1
                sectionValue = (alias[0] == 'A' ? 'А' : 'Б') + SEPARATOR + alias[1]
            }

            // обработать строки имеющие алиас (итоги/подитоги/фиксированные строки)
            if (isExcludeRow(row, exclude)) {
                continue
            }

            index++
            currRow = row

            // получить строку
            def rowValues = getRowValues(row, index, sectionValue, id)
            sb.append(rowValues)
        }

        // итоговая строка
        sb.append(END_ROW)
        def totalRow = getTotalRow(dataRows, id, exclude)
        sb.append(totalRow)

        // сохранение файла
        String fileName = getFileName(template)
        save(sb.toString(), fileName)

        log("TemplateId $id - ${template.name} >>>>>>>> " + path + fileName + ".rnu") // TODO (Ramil Timerbaev)
    }
}

/**
 * Получить название транспорного файла.
 * Имеет формат <Код налоговой формы><Код подразделения><Код периода><Календарный год><Месяц>
 *
 * @param templateId шаблон
 */
def getFileName(FormTemplate template) {
    StringBuilder sb = new StringBuilder()
    String value

    // <Код налоговой формы>
    value = addUnderline(template.getCode(), 9)
    value = value.replace("\\", "_").replace("/", "_") // TODO (Ramil Timerbaev) в некоторых шаблонах есть значение '/', при сохранении ругается, потом убрать
    sb.append(value)

    // <Код подразделения>
    value = addUnderline(getRefBookValue(30, departmentId as Long)?.SBRF_CODE?.value, 17)
    sb.append(value)

    // <Код периода>
    value = addUnderline(getRefBookValue(8, getReportPeriod().dictTaxPeriodId)?.CODE?.value, 2)
    sb.append(value)

    // <Календарный год>
    sb.append(getYear())

    // <Месяц>
    if (template.isMonthly()) {
        value = addUnderline(periodOrder.toString(), 2)
        sb.append(value)
    }

    return sb.toString()
}

/** Дополнить подчеркниваниями. */
def addUnderline(String value, int width) {
    if (value == null) {
        value = ''
    }
    if (value.size() > width) {
        return value
    } else {
        return '_' * (width - value.size()) + value
    }
}

def getHeader(FormTemplate template) {
    StringBuilder sb = new StringBuilder()
    String value

    // 1. Время создания транспортного файла
    value = (new Date()).format('yyyy.MM.dd HH.mm.ss')
    sb.append(value).append(SEPARATOR)

    // 2. Наименование подразделения
    value = getRefBookValue(30, departmentId as Long)?.NAME?.value
    sb.append(value).append(SEPARATOR)

    // 3. Наименование вида налоговой формы
    sb.append(template.getName()).append(SEPARATOR)

    // 4. Наименование АСНУ
    // сказали что этот параметр можно не определять
    sb.append('тестовое наименование АСНУ').append(SEPARATOR)

    sb.append(END_ROW)
    return sb
}

/* Получить данные форм. */
def getAllDataRows() {
    def formTemplates = []
    formTemplateIds.each { id ->
        formTemplates.add(formTemplatesMap[id])
    }
    def kind = FormDataKind.PRIMARY
    def dataRowsMap = [:]

    formTemplates.each { FormTemplate formTemplate ->
        def formData
        if (formTemplate?.isMonthly()) {
            formData = formDataService.findMonth(formTemplate.type.id, kind, departmentId, taxPeriodId, periodOrder)
        } else {
            formData = formDataService.find(formTemplate.type.id, kind, departmentId, reportPeriodId)
        }
        if (formData != null) {
            formDataMap[formTemplate.id] = formData
            dataRowsMap[formTemplate.id] = formDataService.getDataRowHelper(formData)?.all
        } else {
            log(LogLevel.WARNING, "Can't find form ${formTemplate.name}.")
        }
    }
    return dataRowsMap
}

/** Получить данные. */
void loadData() {
    try {
        JndiTemplate template = new JndiTemplate()
        def DataSource dataSource = template.lookup('java:comp/env/jdbc/TaxAccDS')
        def Connection connection = dataSource.connection
        def statement = connection.createStatement()

        // макеты
        loadFormTemplates(statement)

        // столбцы
        loadColumnsData(statement)

        // атрибуты
        loadRefBookAttributes(statement)

        connection.close()
    } catch (Exception ex) {
        logger.error("Error in loadData: ${ex.getLocalizedMessage()}")
    }
}

/** Получить макеты форм. */
void loadFormTemplates(Statement statement) {
    // TODO (Ramil Timerbaev) возможно надо будет изменить запрос, имзенив получение макетов по id типа формы и версии, а не по id макета
    // получить данные шаблонов: id, name, monthly из form_template и id из form_type. Остальные данные не нужны
    ResultSet resultSet = statement.executeQuery(
            "select ft.id as id, ft.name as name, ft.monthly as monthly, ft.code as code, f.id as form_type_id " +
                    "from form_template ft, form_type f " +
                    "where ft.id in (${formTemplateIds.join(', ')}) and ft.type_id = f.id")

    while (resultSet.next()) {
        FormTemplate formTemplate = new FormTemplate()

        formTemplate.setId(resultSet.getInt("id"))
        formTemplate.setName(resultSet.getString("name"))
        formTemplate.setMonthly(resultSet.getBoolean("monthly"))
        formTemplate.setCode(resultSet.getString("code"))

        FormType formType = new FormType()
        formType.setId(resultSet.getInt("form_type_id"))
        formTemplate.setType(formType)

        // заполнение глобальной мапы с шаблонами
        formTemplatesMap[formTemplate.id] = formTemplate
    }
}

/** Получить данные стобцов. */
void loadColumnsData(Statement statement) {
    formTemplateIds.each { id ->
        columnsMap[id] = [:]
        hiddenColumnsMap[id] = [:]
    }
    resultSet = statement.executeQuery(
            "select fc.* from form_column fc where fc.form_template_id in (${formTemplateIds.join(', ')})")

    while (resultSet.next()) {
        Column column
        switch (resultSet.getString("type")) {
            case 'S' : // S - строка
                column = new StringColumn()
                column.setMaxLength(resultSet.getInt("MAX_LENGTH"))
                break
            case 'N' : // N – число
                column = new NumericColumn()
                column.setMaxLength(resultSet.getInt("MAX_LENGTH"))
                column.setPrecision(resultSet.getInt("PRECISION"))
                break
            case 'D' : // D – дата
                column = new DateColumn()
                def format = resultSet.getInt("FORMAT")
                column.setFormatId(format == 0 ? 1 : format)
                break
            case 'R' : // R - ссылка
                column = new ReferenceColumn()
                column.setParentId(resultSet.getInt("PARENT_COLUMN_ID"))
                // column.setParentAlias(resultSet.getString(""))
                column.setRefBookAttributeId(resultSet.getLong("ATTRIBUTE_ID"))
                column.setRefBookAttributeId2(resultSet.getLong("ATTRIBUTE_ID2"))
                break
            case 'A' : // A - автонумеруемая графа
                column = new AutoNumerationColumn()
                // column.set(resultSet.getInt(""))
                break
            default:
                column = new StringColumn()
        }
        column.setId(resultSet.getInt("id"))
        column.setName(resultSet.getString("name"))
        column.setAlias(resultSet.getString("alias"))
        column.setWidth(resultSet.getInt("width"))
        column.setOrder(resultSet.getInt("ord"))

        def templateId = resultSet.getInt("FORM_TEMPLATE_ID")
        def alias = column.getAlias()
        columnsMap[templateId][alias] = column
        if (column.width == 0) {
            hiddenColumnsMap[templateId][alias] = column
        }
    }
}

/** Получить данные атрибутов справочников*/
def loadRefBookAttributes(Statement statement) {
    resultSet = statement.executeQuery("select rb.* from ref_book_attribute rb")

    while (resultSet.next()) {
        RefBookAttribute attribute = new RefBookAttribute()
        attribute.setId(resultSet.getLong("ID"))
        attribute.setRefBookId(resultSet.getLong("REF_BOOK_ID"))
        attribute.setName(resultSet.getString("NAME"))
        attribute.setAlias(resultSet.getString("ALIAS"))
        // Типа атрибута (1-строка; 2-число; 3-дата-время; 4-ссылка)
        def value = RefBookAttributeType.STRING
        switch (resultSet.getInt("TYPE")) {
            case 1 :
                value = RefBookAttributeType.STRING
                break
            case 2 :
                value = RefBookAttributeType.NUMBER
                break
            case 3 :
                value = RefBookAttributeType.DATE
                break
            case 4 :
                value = RefBookAttributeType.REFERENCE
                break
        }
        attribute.setAttributeType(value)
        // attribute.set(resultSet.getInt("ORD"))
        // attribute.set(resultSet.get("REFERENCE_ID")) // это значение есть в базе, но не хранится в модели. Получать его через refBookAttributeId
        attribute.setRefBookAttributeId(resultSet.getLong("ATTRIBUTE_ID"))
        attribute.setVisible(resultSet.getBoolean("VISIBLE"))
        attribute.setPrecision(resultSet.getInt("PRECISION"))
        attribute.setWidth(resultSet.getInt("WIDTH"))
        attribute.setRequired(resultSet.getBoolean("REQUIRED"))
        attribute.setUnique(resultSet.getBoolean("IS_UNIQUE"))
        attribute.setSortOrder(resultSet.getInt("SORT_ORDER"))
        attribute.setFormat(Formats.getById(resultSet.getInt("FORMAT")))
        attribute.setReadOnly(resultSet.getBoolean("READ_ONLY"))
        attribute.setMaxLength(resultSet.getInt("MAX_LENGTH"))

        refBookAttributesMap[attribute.id] = attribute
    }
}

/**
 * Сформировать строку транспортного файла по строке налоговой формы.
 *
 * @param row строка нф
 * @param index номер строки в тф
 * @param section значение для служебного поля в конце строки
 * @param templateId id макета
 */
def getRowValues(def row, def index, def section, def templateId) {
    StringBuilder rowValues = new StringBuilder()
    rowValues.append(index).append(SEPARATOR)
    def columns = row.keySet()
    def overrideWithHidden = null

    for (def alias : columns) {
        // пропустить скрытые столбцы
        if (hiddenColumnsMap[templateId][alias]) {
            overrideWithHidden = getCellValue(row, templateId, alias)
            continue
        }

        // если есть значение для скрытой ячейки, то записать его значение в следующую ячейку справа
        def value
        if (overrideWithHidden != null && overrideWithHidden != "") {
            value = overrideWithHidden
            overrideWithHidden = null
        } else {
            // получение значения ячейки
            value = getCellValue(row, templateId, alias)
        }

        // проверка наличия в значении знака «|»
        if (value != null && value.contains(SEPARATOR)) {
            log(LogLevel.WARNING, "Значение содержит запрещеный символ «$SEPARATOR»: formId $currFormId, row ${row.getIndex()}, column $alias, value = $value")
            value = value.toString().replace('|', 'SEPARATOR')
        }

        if (value != null && !"".equals(value)) {
            rowValues.append(value)
        }

        rowValues.append(SEPARATOR)
    }

    // служебное поле для подитогов/разделов
    if (section != null && section != "") {
        rowValues.append(section)
    }
    rowValues.append(END_ROW)
    return rowValues.toString().replace("«", "\"").replace("»", "\"")
}

/**
 * Получить значение ячейки.
 * Для строк, чисел и нумерации - строковое отображение. Для даты - отображение по фомрату.
 * Для справочных и зависимых значении - отображаемые значения
 *
 * @param row строка
 * @param templateId id макета
 * @param alias алиас столбца ячейки
 */
String getCellValue(def row, def templateId, def alias) {
    def value = null
    Column column = columnsMap[templateId][alias]
    if (row.getCell(alias).getValueOwner() != null) {
        return value
    }
    if (column instanceof StringColumn) {
        // строка
        value = row.getCell(alias).value
    } else if (column instanceof NumericColumn) {
        // число
        value = row.getCell(alias).value
    } else if (column instanceof DateColumn) {
        // дата
        def format = Formats.getById(column.formatId).format
        value = row.getCell(alias).value?.format(format)
    } else if (column instanceof ReferenceColumn) {
        // зависимое или справочное значение, получать если строка без алиаса
        if (row.getAlias() == null) {
            value = getReferenceValue(row, alias, columnsMap[templateId])
        }
    } else if (column instanceof AutoNumerationColumn) {
        // автонумеруемое
        value = row.getCell(alias).value
    }
    return value?.toString()
}

/**
 * Получить зависимое или справочное значение.
 *
 * @param row строка формы
 * @param alias алиас ячейки
 * @param columns мапа со списком столбцов формы
 */
def getReferenceValue(def row, def alias, def columns) {
    ReferenceColumn column = columns[alias]

    def refBookId
    def recordId
    def attribute = column.refBookAttributeId
    def attribute2 = column.refBookAttributeId2

    ReferenceColumn needColumn = column

    if (column.parentId != null) {
        // если зависимая ячейка, то ищем родительсий столбец
        for (def i : columns.keySet()) {
            Column col = columns[i]
            if (col.id == column.parentId && col instanceof ReferenceColumn) {
                needColumn = col
                break
            }
        }
        if (needColumn == null) {
            log(LogLevel.WARNING, "Error: can't find parent cell for reference cell: formId $currFormId, row ${row.getIndex()}, column $alias")
        }
    } else {
        // справочная ячейка
    }
    RefBookAttribute refBookAttribute = refBookAttributesMap[needColumn.refBookAttributeId]

    refBookId = refBookAttribute.refBookId
    recordId = row.getCell(needColumn.alias).value

    currRow = row

    return getRefBookValue(refBookId, recordId, attribute, attribute2)
}

/**
 * Получить разыменованое значение из справочника.
 *
 * @param refBookId id справочника
 * @param recordId id записи справочника
 * @param attributeId id атрибута для отображения (значение которого надо вернуть)
 * @param attributeId2 id атрибута для отображения (значение которого надо вернуть) - если атрибут справочника является ссылкой
 */
def getRefBookValue(def refBookId, def recordId, def attributeId, def attributeId2) {
    def record = getRefBookValue(refBookId, recordId)
    RefBookAttribute refBookAttribute = refBookAttributesMap[attributeId]
    def value = record?.get(refBookAttribute.alias)?.value

    if (attributeId2 == null || attributeId2 == 0) {
        // значение по ссылке не ссылочное
        switch (refBookAttribute.attributeType) {
            case RefBookAttributeType.STRING :
            case RefBookAttributeType.NUMBER :
                return value
                break
            case RefBookAttributeType.DATE :
                def format = Formats.getById(refBookAttribute.format)
                return value?.format(format)
                break
            default :
                log(LogLevel.WARNING, "Error: refBook attribute wrong identified. " +
                        "FormId $currFormId, row ${currRow.getIndex()}, " +
                        "refBookId ${refBookAttribute.refBookId}, record ${record?.record_id?.value}, " +
                        "attribute ${refBookAttribute.alias}, attrinbuteId ${refBookAttribute.id}, value $value.")
                return value
        }
    } else {
        // значение по ссылке ссылочное
        RefBookAttribute needRefBookAttribute = refBookAttributesMap[refBookAttribute.refBookAttributeId]
        def needRefBookId = needRefBookAttribute.refBookId
        def needRecordId = record?.get(refBookAttribute.alias)?.value
        return getRefBookValue(needRefBookId, needRecordId, attributeId2, null)
    }
}

void save(String data, def fileName) {
    FileOutputStream outFile = new FileOutputStream(path + fileName + ".rnu")
    outFile.write(data.getBytes(Charset.forName("cp866")))
    outFile.close()
}

def getYear() {
    if (year == null) {
        year = taxPeriodService.get(taxPeriodId)?.year
    }
    return year
}

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(reportPeriodId)
    }
    return reportPeriod
}

/**
 * Получить итоговую строку.
 *
 * @param dataRows строки формы
 * @param templateId шаблон формы
 * @param exclude как обрабатывать строки имеющие алиас
 */
String getTotalRow(def dataRows, def templateId, Exclude exclude) {
    // столбцы формы по порядку
    def columnsSort = []
    def map = columnsMap[templateId]
    map.each { def alias, Column column ->
        columnsSort.add(column)
    }
    columnsSort.sort { Column column -> column.order }

    // найти числовые столбцы
    def totalAliases = []
    columnsSort.each { Column column ->
        if (column instanceof NumericColumn) {
            totalAliases.add(column.alias)
        }
    }

    def total = [:]

    // задать начальные значения
    totalAliases.each { alias ->
        total[alias] = BigDecimal.ZERO
    }

    // посчитать суммы
    for (def row : dataRows) {
        if (isExcludeRow(row, exclude)) {
            continue
        }
        totalAliases.each { alias ->
            total[alias] += (row.getCell(alias).value ?: BigDecimal.ZERO)
        }
    }

    // сформировать строку итогов
    StringBuilder sb = new StringBuilder()
    sb.append(SEPARATOR)
    columnsSort.each { Column column ->
        if (column.width > 0) {
            if (column.alias in totalAliases) {
                sb.append(total[column.alias])
            }
            sb.append(SEPARATOR)
        }
    }
    sb.append(END_ROW)
    return sb.toString()
}

void log(def s, def ...args) {
    log(LogLevel.INFO, s, args)
}

void log(LogLevel level, def s, def ...args) {
    if (level == LogLevel.ERROR) {
        logger.error(s, args)
    } else if (level == LogLevel.WARNING) {
        logger.warn(s, args)
    } else {
        logger.info(s, args)
    }

    def mes = String.format(s, args)
    System.out.println(mes)
}

/** Подготавливает данные. */
void calcData() {
    formTemplatesExcludeRowMap.each { id, fix ->
        formTemplateIds.add(id)
    }
}

/** Пропускать ли строки имеющие алиас (итоги/подитоги/фиксированные строки). */
def isExcludeRow(def row, Exclude exclude) {
    if (row.getAlias() != null) {
        if (exclude == Exclude.FIX || exclude == Exclude.SECTION_TOTAL || exclude == Exclude.SECTION2_TOTAL) {
            return true
        } else if (exclude == Exclude.TOTAL && row.getAlias().contains(Exclude.TOTAL.value)) {
            return true
        } else if (exclude == Exclude.EXCEPT_CONTAIN_TOTAL &&
                (row.getAlias() == Exclude.EXCEPT_CONTAIN_TOTAL.value ||
                !row.getAlias().contains(Exclude.EXCEPT_CONTAIN_TOTAL.value))) {
            // пропустить строки равные total и строки не содержащие total (или по простому остаются строки содержащие total)
            return true
        }
    }
    return false
}