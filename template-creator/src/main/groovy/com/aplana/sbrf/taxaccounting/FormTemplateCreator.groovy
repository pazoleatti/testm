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
 * Создает FormType, FormTemplate, FormColumn, FormStyle, строки и заголовки макета
 */

static void main(String[] args) {
    String resourcePath = "./src/main/resources/com/aplana/sbrf/taxaccounting/"
    String templatePath = "../src/main/resources/form_template/etr/etr_4_1/v2015/"
    def map = [ // TODO заполнить
                // заполняем вручную
                "%1%" : '700', // id типа НФ
                "%2%" : 'Абсолютная величина налоговых платежей', // имя типа НФ
                "%3%" : 'TaxType.ETR', // вид налога
                "%4%" : 'false', // isIFRS
                "%5%" : '', // имя ИФРС
                "%6%" : '700', // id версии макета НФ
                "%10%" : '01.01.2015', // версия в формате 01.01.2015
                "%11%" : 'false'] // ежемесячность

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

    // добавляем колонки
        StringBuilder sb = new StringBuilder()
        ft.getColumns().eachWithIndex { def column, index ->
            String columnClassName = column.getClass().simpleName
            sb.append("${index == 0 ? 'def Column ' : '    '}formColumn = new $columnClassName()\n")
            sb.append("    formColumn.setOrder(${column.order})\n")
            sb.append("    formColumn.setName('${column.name.replaceAll(/"/, /\"/).replaceAll(/\n/, ' ').replaceAll(/ +/, ' ')}')\n")
            sb.append("    formColumn.setAlias('${column.alias}')\n")
            sb.append("    formColumn.setColumnType(ColumnType.find{ it.title.equals('${column.columnType.title}')})\n")
            sb.append("    formColumn.setWidth(${column.getWidth()})\n")
            sb.append("    formColumn.setChecking(${column.checking})\n")
            switch (columnClassName) {
                case NumericColumn.class.simpleName :
                    sb.append("    formColumn.setPrecision(${((NumericColumn)column).precision ?: 0})\n")
                    sb.append("    formColumn.setMaxLength(${((NumericColumn)column).maxLength})\n")
                    break
                case AutoNumerationColumn.class.simpleName :
                    sb.append("    formColumn.setNumerationType(NumerationType.getById(${((AutoNumerationColumn)column).numerationType?.id ?: 0}))\n")
                    break
                case DateColumn.class.simpleName :
                    sb.append("    formColumn.setFormatId(${((DateColumn)column).formatId})\n")
                    break
                case RefBookColumn.class.simpleName :
                    sb.append("    formColumn.setRefBookAttributeId(${((RefBookColumn)column).refBookAttributeId})\n")
                    sb.append("    formColumn.setRefBookAttributeId2(${((RefBookColumn)column).refBookAttributeId2})\n")
                    sb.append("    formColumn.setNameAttributeId(${((RefBookColumn)column).nameAttributeId})\n")
                    sb.append("    formColumn.setFilter('${((RefBookColumn)column).filter ?: ''}' ?: null)\n")
                    break
                case ReferenceColumn.class.simpleName :
                    sb.append("    formColumn.setParentAlias('${((ReferenceColumn)column).parentAlias ?: ''}')\n")
                    sb.append("    formColumn.setRefBookAttributeId(${((ReferenceColumn)column).refBookAttributeId})\n")
                    sb.append("    formColumn.setRefBookAttributeId2(${((ReferenceColumn)column).refBookAttributeId2})\n")
                    break
                case StringColumn.class.simpleName :
                    sb.append("    formColumn.setMaxLength(${((StringColumn)column).maxLength})\n")
                    sb.append("    formColumn.setPrevLength(${((StringColumn)column).prevLength})\n")
            }
            sb.append("    formColumns.add(formColumn)\n\n")
        }
        map["%13%"] = sb.toString()
    // добавляем стили
        sb = new StringBuilder()
        ft.getStyles().eachWithIndex { def style, index ->
            sb.append("${index == 0 ? 'def FormStyle ' : '    '}formStyle = new FormStyle()\n")
            sb.append("    formStyle.setAlias('${style.alias}')\n")
            sb.append("    formStyle.setBackColor(Color.getById(${style.backColor.id}))\n")
            sb.append("    formStyle.setBold(${style.bold})\n")
            sb.append("    formStyle.setFontColor(Color.getById(${style.fontColor.id}))\n")
            sb.append("    formStyle.setItalic(${style.italic})\n")
            sb.append("    formStyles.add(formStyle)\n\n")
        }
        map["%14%"] = sb.toString()

        String dataRowsString = dataRowsFile.getText("UTF-8")
        String dataHeadersString = dataHeadersFile.getText("UTF-8")

    // добавляем строки
        sb = new StringBuilder()
        putEscapedText(sb, dataRowsString)
        map["%15%"] = sb.toString()

    // добавляем заголовки
        sb = new StringBuilder()
        putEscapedText(sb, dataHeadersString)
        map["%16%"] = sb.toString()

        String scriptString = scriptFile.getText("UTF-8")
        sb = new StringBuilder()
        putEscapedText(sb, scriptString)
        map["%17%"] = sb.toString()

        writer = new FileWriter(new File("scriptExecution_test.txt"))
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

def static putEscapedText(StringBuilder sb, String string) {
    string = string.getBytes("UTF-8").encodeBase64().toString()
    sb.append("longString = '").append(string).append("'")
}