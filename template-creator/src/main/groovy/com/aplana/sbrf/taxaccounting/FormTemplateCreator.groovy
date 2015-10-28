package com.aplana.sbrf.taxaccounting

import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn
import com.aplana.sbrf.taxaccounting.model.DateColumn
import com.aplana.sbrf.taxaccounting.model.FormTemplateContent
import com.aplana.sbrf.taxaccounting.model.NumericColumn
import com.aplana.sbrf.taxaccounting.model.RefBookColumn
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.FormTemplate

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

/**
 * Утилита создания скрипта для создания макета через scriptExecution
 * Запускать через gradle installApp run
 * создает в папке template-creator текст скрипта
 * после его выполнения необходимо запустить create_form_data_nnn.txt c <a href="http://conf.aplana.com/pages/viewpage.action?pageId=20384703">страницы</a>
 */

static void main(String[] args) {
    // TODO поменять для частичного создания/обновления макетов (тип, макет и колонки не пересоздаются!)
    // флаги выполняемых действий (тип, шаблон, колонки, стили, фикс.строки, заголовки, скрипты)
    def flags = [
            true, // тип
            true, // шаблон
            true, // колонки
            true, // стили
            true, // фикс.строки
            true, // заголовки
            true  // скрипты
    ]
    String resourcePath = "./src/main/resources/com/aplana/sbrf/taxaccounting/"
    String templatePath = "../src/main/resources/form_template/etr/etr_4_16/v2015/" // TODO поменять на путь до нужного макета
    def map = [ // TODO заполнить
                // заполняем вручную
                "%1%"  : '716',             // id типа НФ
                "%2%"  : 'Приложение 4-16. Доходы и расходы, не учитываемые для целей налогообложения по налогу на прибыль, и их влияние на финансовый результат', // имя типа НФ
                "%3%"  : 'TaxType.ETR',     // вид налога
                "%4%"  : 'false',           // isIFRS
                "%5%"  : '',                // имя ИФРС
                "%26%" : '4-16',            // код НФ
                "%6%"  : '716',             // id версии макета НФ
                "%10%" : '01.01.2015',      // версия в формате 01.01.2015
                "%11%" : 'false',           // ежемесячность
                "%18%" : 'true',            // использование периода сравнения
                "%27%" : 'true',            // признак расчета нарастающим итогом
                "%28%" : 'true'             // отображать кнопку "Обновить"
    ]

    String outputFileName = "scriptExecution_etr_4_16.txt" // TODO поменять имя выходного файла
    def writer
    try {
        File templateFile = new File(resourcePath + "scriptExecution_template.txt")
        if (!templateFile.exists()) {
            println("Отсутствует файл ${templateFile.name} для чтения")
            return
        }

        File contentFile = new File(templatePath + "content.xml")
        if (!contentFile.exists()) {
            println("Отсутствует файл ${contentFile.name} для чтения")
            return
        }

        File dataRowsFile = new File(templatePath + "rows.xml")
        if (!dataRowsFile.exists()) {
            println("Отсутствует файл ${dataRowsFile.name} для чтения")
            return
        }

        File dataHeadersFile = new File(templatePath + "headers.xml")
        if (!dataHeadersFile.exists()) {
            println("Отсутствует файл ${dataHeadersFile.name} для чтения")
            return
        }

        File scriptFile = new File(templatePath + "script.groovy")
        if (!scriptFile.exists()) {
            println("Отсутствует файл ${scriptFile.name} для чтения")
            return
        }

        FormTemplate ft = new FormTemplate()
        FormTemplateContent ftc;
        JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ftc = (FormTemplateContent) jaxbUnmarshaller.unmarshal(
                new InputStreamReader(new ByteArrayInputStream(contentFile.text.getBytes())));
        ftc.fillFormTemplate(ft);

        // заполняется автоматом
        map["%7%"] = String.valueOf(ft.isFixedRows())
        map["%8%"] = ft.getName().replaceAll(/"/, /\"/).replaceAll(/\n/, ' ').replaceAll(/ +/, " ")
        map["%9%"] = ft.getFullName().replaceAll(/"/, /\"/).replaceAll(/\n/, ' ').replaceAll(/ +/, " ")
        map["%12%"] = ft.getHeader()
        map["%19%"] = flags[0].toString()
        map["%20%"] = flags[1].toString()
        map["%21%"] = flags[2].toString()
        map["%22%"] = flags[3].toString()
        map["%23%"] = flags[4].toString()
        map["%24%"] = flags[5].toString()
        map["%25%"] = flags[6].toString()

    // добавляем колонки
        StringBuilder sb = new StringBuilder()
        ft.getColumns().eachWithIndex { def column, index ->
            String columnClassName = column.getClass().simpleName
            sb.append("${index == 0 ? 'def Column ' : '        '}formColumn = new $columnClassName()\n")
            sb.append("        formColumn.setOrder(${column.order})\n")
            sb.append("        formColumn.setName('${column.name.replaceAll(/"/, /\"/).replaceAll(/\n/, ' ').replaceAll(/ +/, ' ')}')\n")
            sb.append("        formColumn.setAlias('${column.alias}')\n")
            sb.append("        formColumn.setColumnType(ColumnType.find{ it.title.equals('${column.columnType.title}')})\n")
            sb.append("        formColumn.setWidth(${column.getWidth()})\n")
            sb.append("        formColumn.setChecking(${column.checking})\n")
            switch (columnClassName) {
                case NumericColumn.class.simpleName :
                    sb.append("        formColumn.setPrecision(${((NumericColumn)column).precision ?: 0})\n")
                    sb.append("        formColumn.setMaxLength(${((NumericColumn)column).maxLength})\n")
                    break
                case AutoNumerationColumn.class.simpleName :
                    sb.append("        formColumn.setNumerationType(NumerationType.getById(${((AutoNumerationColumn)column).numerationType?.id ?: 0}))\n")
                    break
                case DateColumn.class.simpleName :
                    sb.append("        formColumn.setFormatId(${((DateColumn)column).formatId})\n")
                    break
                case RefBookColumn.class.simpleName :
                    sb.append("        formColumn.setRefBookAttributeId(${((RefBookColumn)column).refBookAttributeId})\n")
                    sb.append("        formColumn.setRefBookAttributeId2(${((RefBookColumn)column).refBookAttributeId2})\n")
                    sb.append("        formColumn.setNameAttributeId(${((RefBookColumn)column).nameAttributeId})\n")
                    sb.append("        formColumn.setFilter('${((RefBookColumn)column).filter ?: ''}' ?: null)\n")
                    break
                case ReferenceColumn.class.simpleName :
                    sb.append("        formColumn.setParentAlias('${((ReferenceColumn)column).parentAlias ?: ''}')\n")
                    sb.append("        formColumn.setRefBookAttributeId(${((ReferenceColumn)column).refBookAttributeId})\n")
                    sb.append("        formColumn.setRefBookAttributeId2(${((ReferenceColumn)column).refBookAttributeId2})\n")
                    break
                case StringColumn.class.simpleName :
                    sb.append("        formColumn.setMaxLength(${((StringColumn)column).maxLength})\n")
                    sb.append("        formColumn.setPrevLength(${((StringColumn)column).prevLength})\n")
            }
            sb.append("        formColumns.add(formColumn)\n\n")
        }
        map["%13%"] = sb.toString()
    // добавляем стили
        sb = new StringBuilder()
        ft.getStyles().eachWithIndex { def style, index ->
            sb.append("${index == 0 ? 'def FormStyle ' : '        '}formStyle = new FormStyle()\n")
            sb.append("        formStyle.setAlias('${style.alias}')\n")
            sb.append("        formStyle.setBackColor(Color.getById(${style.backColor.id}))\n")
            sb.append("        formStyle.setBold(${style.bold})\n")
            sb.append("        formStyle.setFontColor(Color.getById(${style.fontColor.id}))\n")
            sb.append("        formStyle.setItalic(${style.italic})\n")
            sb.append("        formStyles.add(formStyle)\n\n")
        }
        map["%14%"] = sb.toString()

        String dataRowsString = dataRowsFile.getText("UTF-8")
        String dataHeadersString = dataHeadersFile.getText("UTF-8")

    // добавляем строки
        sb = new StringBuilder()
        putEncodedText(sb, dataRowsString)
        map["%15%"] = sb.toString()

    // добавляем заголовки
        sb = new StringBuilder()
        putEncodedText(sb, dataHeadersString)
        map["%16%"] = sb.toString()

        String scriptString = scriptFile.getText("UTF-8")
        sb = new StringBuilder()
        putEncodedText(sb, scriptString)
        map["%17%"] = sb.toString()

        writer = new FileWriter(new File(outputFileName))
        String text = templateFile.text
        map.each { def key, value ->
            text = text.replace(key, value ?: "")
        }
        writer.write(text)
    } catch (Exception e) {
        println("Error: ${e.getMessage()}")
    } finally {
        writer?.close()
    }
}

def static putEncodedText(StringBuilder sb, String string) {
    final int MAX_LENGTH = 25000
    string = string.getBytes("UTF-8").encodeBase64().toString()
    int start = 0
    while (start < string.length()) {
        int end = (start + MAX_LENGTH) < string.length() ? (start + MAX_LENGTH) : string.length()
        String text = string.substring(start, end)
        sb.append("        longString += '").append(text).append("'\n\r")
        start += MAX_LENGTH
    }
}