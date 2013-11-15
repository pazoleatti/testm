/**
 * Скрипт генерации SQL-скрипта для обновления скриптов налоговых форм
 */

// Путь к корневой папке для определенного вида налога, например C:\workspace\sbrfacctax\src\main\resources\form_template\deal\
def scriptLocation = 'C:\\Users\\user\\workspaceIdea\\sbrfacctax\\src\\main\\resources\\form_template\\deal\\'

// Маппинг
// FORM_TEMPLATE.ID -> Имя папки
// Можно закомментарить строки, которые не нужны
def map = [
        380: "auctions_property", // Приобретение услуг по организации и проведению торгов по реализации имущества
        382: "bank_service", // Оказание банковских услуг
        384: "bonds_trade", // Реализация и приобретение ценных бумаг
        387: "corporate_credit", // Предоставление корпоративного кредита
        385: "credit_contract", // Уступка прав требования по кредитным договорам
        390: "foreign_currency", // Купля-продажа иностранной валюты
        391: "forward_contracts", // Поставочные срочные сделки, базисным активом которых является иностранная валюта
        388: "guarantees", // Предоставление гарантий
        389: "interbank_credits", // Предоставление межбанковских кредитов
        386: "letter_of_credit", // Предоставление инструментов торгового финансирования и непокрытых аккредитивов
        400: "matrix", // Матрица
        392: "nondeliverable", // Беспоставочные срочные сделки
        410: "organization_matching", // Согласование организации
        393: "precious_metals_deliver", // Поставочные срочные сделки с драгоценными металлами
        //394: "precious_metals_trade", // Купля-продажа драгоценных металлов
        376: "rent_provision", // Предоставление нежилых помещений в аренду
        383: "repo", // Сделки РЕПО
        381: "securities", // Приобретение и реализация ценных бумаг (долей в уставном капитале)
        375: "software_development", // Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий
        377: "tech_service", // Техническое обслуживание нежилых помещений
        379: "trademark" // Предоставление права пользования товарным знаком
]

// Проверка корневой папки
def folder = new File(scriptLocation)
if (!folder.exists()) {
    println("Bad folder address: $scriptLocation")
    return
}

// Выходной файл
def outFile = new File("out.sql")

// Вывод sql-скрипта в файл
outFile.withWriter { out ->
    out.println('DECLARE')
    def Map<Integer, Integer> scriptMap = new HashMap<Integer, Integer>()
    map.each {
        def scriptFile = new File(scriptLocation + it.value + "\\script.groovy")
        // Пропускаем несуществующие файлы
        if (!scriptFile.exists()) {
            println("Skip bad script address: " + scriptFile.absolutePath)
            return
        }

        // Текст скрипта
        String text = scriptFile.text

        // Деление на части
        int seek = 0
        int step = 4000  // Символов
        int counter = 1
        while (seek < text.length()) {
            String subClob = text.length() > seek + step ? text.substring(seek, seek + step) : text.substring(seek, text.length())
            out.println(String.format("SCRIPT_%d_%d CLOB := '%s';", it.key, counter, subClob.replaceAll("'", "''")))
            seek += step
            counter++
        }
        scriptMap.put(it.key, counter - 1)
    }

    out.println('BEGIN')

    scriptMap.keySet().each {

        StringBuilder sb = new StringBuilder();
        def count = scriptMap.get(it)
        for (int i = 1; i <= count; i++) {
            sb.append("SCRIPT_${it}_${i}")
            if (i != count) {
                sb.append(" || ")
            }
        }
        out.println("   UPDATE FORM_TEMPLATE SET SCRIPT = ${sb.toString()} WHERE ID = $it;")
    }
    out.print('END;')
}

print('Script ' + outFile.absolutePath + ' created')
