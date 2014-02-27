/**
 * Скрипт генерации SQL-скрипта для обновления скриптов налоговых форм
 */

// Пути к корневой папке для определенного вида налога, например C:\workspace\sbrfacctax\src\main\resources\form_template\deal\
def scriptLocations = [
        'C:\\workspace\\sbrfacctax\\src\\main\\resources\\form_template\\deal\\',  // МУКС
        'C:\\workspace\\sbrfacctax\\src\\main\\resources\\form_template\\income\\', // Налог на прибыль
        'C:\\workspace\\sbrfacctax\\src\\main\\resources\\form_template\\transport\\' // Транспортный налог
]

// Маппинг
// FORM_TEMPLATE.ID -> Имя папки
// Можно закомментарить строки, которые не нужны

def map = [
        // МУКС
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
        394: "precious_metals_trade", // Купля-продажа драгоценных металлов
        376: "rent_provision", // Предоставление нежилых помещений в аренду
        383: "repo", // Сделки РЕПО
        381: "securities", // Приобретение и реализация ценных бумаг (долей в уставном капитале)
        375: "software_development", // Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий
        377: "tech_service", // Техническое обслуживание нежилых помещений
        379: "trademark", // Предоставление права пользования товарным знаком

        // Налог на прибыль
        500: "advanceDistribution",
        304: "outcome_simple",
        306: "output1",
        364: "rnu12",
        323: "rnu23",
        324: "rnu25",
        325: "rnu26",
        326: "rnu27",
        329: "rnu30",
        328: "rnu31",
        330: "rnu32_1",
        332: "rnu33",
        337: "rnu39_2",
        338: "rnu40_1",
        339: "rnu40_2",
        355: "rnu64",
        502: "rnu107",
        396: "rnu110",

        372: "app5",
        362: "f7_8",
        308: "output3",
        395: "rnu108",
        367: "rnu111",
        374: "rnu112",
        369: "rnu115",
        368: "rnu116",
        370: "rnu117",
        373: "rnu118",
        378: "rnu120",
        321: "rnu14",
        499: "rnu16",
        501: "rnu17",
        322: "rnu22",
        331: "rnu32_2",
        333: "rnu36_1",
        315: "rnu36_2",
        334: "rnu38_1",
        335: "rnu38_2",
        336: "rnu39_1",
        316: "rnu4",
        340: "rnu44",
        341: "rnu45",
        342: "rnu46",
        344: "rnu47",
        343: "rnu48_1",
        313: "rnu48_2",
        312: "rnu49",
        317: "rnu5",
        365: "rnu50",
        348: "rnu55",
        349: "rnu56",
        353: "rnu57",
        318: "rnu6",
        352: "rnu61",
        354: "rnu62",
        311: "rnu7",
        504: "rnu70_1",
        357: "rnu70_2",
        356: "rnu71_1",
        503: "rnu71_2",
        358: "rnu72",
        366: "rnu75",
        320: "rnu8",

        // Транспортный налог
        202: "benefit_vehicles",
        200: "summary",
        201: "vehicles"
]
// Проверка корневой папки
scriptLocations.each {
    if (!new File(it).exists()) {
        println("Bad folder address: $it")
        return
    }
}

// Выходной файл
def outFile = new File("out.sql")

// Вывод sql-скрипта в файл
outFile.withWriter { out ->
    out.println('DECLARE')
    def Map<Integer, Integer> scriptMap = new HashMap<Integer, Integer>()
    map.each {
        // Поиск скрипта во всех указанных папках
        def File scriptFile
        for (int i = 0; i < scriptLocations.size(); i++) {
            scriptFile = new File(scriptLocations.get(i) + it.value + "\\script.groovy")
            if (scriptFile.exists()) {
                break
            }
        }
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
