package com.aplana.sbrf.taxaccounting

/**
 * Утилита создания скрипта для создания макета через scriptExecution
 * Запускать через gradle installApp run
 * создает в папке template-creator текст скрипта
 * после его выполнения по необходимости добавить jrxml xsd в макет.
 */

static void main(String[] args) {
    // TODO поменять для частичного создания/обновления макетов (тип, макет не пересоздаются!)
    // флаги выполняемых действий
    def flags = [
            false, // тип
            true, // макет
            true  // скрипт
    ]
    String resourcePath = "./src/main/resources/com/aplana/sbrf/taxaccounting/"
    // TODO поменять на путь до нужного макета
    String templatePath = "../src/main/resources/form_template/income/declaration_bank_2/v2016/"
    def map = [
            // TODO заполнить вручную
            // id типа
            "%typeId%"       : '0',

            // имя типа
            "%typeName%"     : 'Название типа налоговой формы',

            // вид налога
            "%taxType%"      : 'TaxType.INCOME',

            // isIFRS
            "%isIfrs%"       : 'false',

            // имя ИФРС
            "%ifrsName%"     : '',

            // id версии макета
            "%templateId%"   : '0',

            // имя версии
            "%templateName%" : 'Название версии макета налоговой формы',

            // версия в формате 01.01.2015
            "%version%"      : '01.01.2016',
    ]

    // TODO поменять имя выходного файла
    String outputFileName = "scriptExecution_declaration_bank_v2016.txt"
    def writer
    try {
        File templateFile = new File(resourcePath + "scriptExecution_template_for_declaration.txt")
        if (!templateFile.exists()) {
            println("Отсутствует файл ${templateFile.name} для чтения")
            return
        }

        File scriptFile = new File(templatePath + "script.groovy")
        if (!scriptFile.exists()) {
            println("Отсутствует файл ${scriptFile.name} для чтения")
            return
        }

        // заполняется автоматом
        map["%createType%"]     = flags[0].toString()
        map["%createTemplate%"] = flags[1].toString()
        map["%addScript%"]      = flags[2].toString()

        // скрипт
        String scriptString = scriptFile.getText("UTF-8")
        StringBuilder sb = new StringBuilder()
        putEncodedText(sb, scriptString)
        map["%script%"] = sb.toString()

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
        sb.append("        longString += '").append(text).append("'\n")
        start += MAX_LENGTH
    }
}