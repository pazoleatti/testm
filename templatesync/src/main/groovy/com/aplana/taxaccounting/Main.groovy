package com.aplana.taxaccounting

import groovy.sql.Sql

/**
 * Утилита для синхронизации шаблонов НФ
 * http://jira.aplana.com/browse/SBRFACCTAX-4929
 *
 * Запуск командой gradle:
 * gradle run
 *
 * Сборка исполнимого приложения:
 * gradle installApp
 *
 * @author Dmitriy Levykin
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 */
class Main {

    // Параметры подключения к БД
    def static DB_URL = 'jdbc:oracle:thin:@//172.16.127.16:1521/ORCL.APLANA.LOCAL'
    def static DB_USER = 'TAX_0_3_7'
    def static DB_PASSWORD = 'TAX'

    // Путь к папке с шаблонами
    def static SRC_FOLDER_PATH = '../src/main/resources/form_template'
    def
    static TAX_FOLDERS = ['deal': 'МУКС', 'income': 'Налог на прибыль', /*'vat': 'НДС',*/ 'transport': 'Транспортный налог']

    def static REPORT_NAME = 'report.html'

    // Имя папки → FORM_TYPE.ID
    // Нужно перечислить все папки, иначе будет ошибка
    def static TEMPLATE_NAME_TO_TYPE_ID = [
            'deal': [
                    'auctions_property': 380,
                    'bank_service': 382,
                    'bank_service_income': 398,
                    'bank_service_outcome': 399,
                    'bonds_trade': 384,
                    'corporate_credit': 387,
                    'credit_contract': 385,
                    'foreign_currency': 390,
                    'forward_contracts': 391,
                    'guarantees': 388,
                    'guarantees_involvement': 401,
                    'interbank_credits': 389,
                    'letter_of_credit': 386,
                    'matrix': 400,
                    'nondeliverable': 392,
                    'notification': -1,
                    'organization_matching': 410,
                    'precious_metals_deliver': 393,
                    'precious_metals_trade': 394,
                    'rent_provision': 376,
                    'repo': 383,
                    'rights_acquisition': 404,
                    'securities': 381,
                    'software_development': 375,
                    'summary': 409,
                    'take_corporate_credit': 397,
                    'take_interbank_credit': 402,
                    'take_itf': 403,
                    'tech_service': 377,
                    'trademark': 379
            ],
            'income': [
                    'advanceDistribution': 500,
                    'app5': 372,
                    'declaration_bank': -1,
                    'declaration_op': -1,
                    'f7_8': 362,
                    'income_complex': 302,
                    'income_simple': 301,
                    'outcome_complex': 303,
                    'outcome_simple': 304,
                    'output1': 306,
                    'output2': 307,
                    'output3': 308,
                    'rnu107': -1,
                    'rnu108': -1,
                    'rnu110': -1,
                    'rnu111': -1,
                    'rnu112': -1,
                    'rnu115': -1,
                    'rnu116': -1,
                    'rnu117': -1,
                    'rnu118': -1,
                    'rnu119': -1,
                    'rnu12': 364,
                    'rnu120': -1,
                    'rnu14': 321,
                    'rnu16': 499,
                    'rnu17': 501,
                    'rnu22': 322,
                    'rnu23': 323,
                    'rnu25': 324,
                    'rnu26': 325,
                    'rnu27': 326,
                    'rnu30': 329,
                    'rnu31': 328,
                    'rnu32_1': 330,
                    'rnu32_2': 331,
                    'rnu33': 332,
                    'rnu36_1': 333,
                    'rnu36_2': 315,
                    'rnu38_1': 334,
                    'rnu38_2': 335,
                    'rnu39_1': 336,
                    'rnu39_2': 337,
                    'rnu4': 316,
                    'rnu40_1': 338,
                    'rnu40_2': 339,
                    'rnu44': 340,
                    'rnu45': 341,
                    'rnu46': 342,
                    'rnu47': 344,
                    'rnu48_1': 343,
                    'rnu48_2': 313,
                    'rnu49': 312,
                    'rnu5': 317,
                    'rnu50': 365,
                    'rnu51': 345,
                    'rnu53': 346,
                    'rnu54': 347,
                    'rnu55': 348,
                    'rnu56': 349,
                    'rnu57': 353,
                    'rnu59': 350,
                    'rnu6': 318,
                    'rnu60': 351,
                    'rnu61': 352,
                    'rnu62': 354,
                    'rnu64': 355,
                    'rnu7': 311,
                    'rnu70_1': 504,
                    'rnu70_2': 357,
                    'rnu71_1': 356,
                    'rnu71_2': 503,
                    'rnu72': 358,
                    'rnu75': 366,
                    'rnu8': 320
            ],
            'vat': [
                    'vat_724_1': -1,
                    'vat_724_2_1': -1,
                    'vat_724_2_2': -1,
                    'vat_724_4': -1,
                    'vat_724_6': -1,
                    'vat_937_1': -1,
                    'vat_937_2_13': -1,
                    'vat_973_1_14': -1
            ],
            'transport': [
                    'benefit_vehicles': 202,
                    'declaration': -1,
                    'summary': 200,
                    'vehicles': 201
            ]
    ]

    // FORM_TYPE.ID → Версия макета
    def static getDBVersions() {
        println("DBMS connect: url=$DB_URL user=$DB_USER")
        def sql = Sql.newInstance(DB_URL, DB_USER, DB_PASSWORD, "oracle.jdbc.OracleDriver")
        def map = [:]

        sql.eachRow("select id, type_id, to_char(version, 'RRRR') as version, name, script from form_template where status not in (-1, 2)") {
            def type_id = it.type_id as Integer
            if (map[type_id] == null) {
                map.put((Integer) it.type_id, [:])
            }
            // Версия макета
            def version = new Expando()
            version.id = it.id as Integer
            version.type_id = it.type_id as Integer
            version.version = it.version
            version.name = it.name

            version.script = it.script?.characterStream?.text
            map[type_id].put(it.version, version)
        }
        sql.close()
        println("DBMS Ok")
        return map
    }

    // Сравнение git-версии с версией в БД
    def static scanSrcFolder(def versionsMap, def folderName) {
        def map = [:]
        def scanResult = []

        def folderFile = new File("$SRC_FOLDER_PATH/$folderName")
        map.put(folderFile, [:])
        // По видам НФ
        folderFile.eachDir { templateFolder ->
            // Id типа формы
            def Integer id = TEMPLATE_NAME_TO_TYPE_ID[folderName][templateFolder.name]
            // Новый тип в результат
            map[folderFile].put(templateFolder.name, [:])

            if (id != null) { // Id типа определен
                if (id != -1) { // Id типа формы, которую нужно пропустить
                    // Версии формы из БД
                    def versions = versionsMap[id]

                    if (versions != null) {
                        // Версии, которые есть в БД, но нет в git
                        def notExistVersions = versions.clone()

                        // По версиям
                        templateFolder.eachDir { versionFolder ->

                            def result = new Expando()
                            result.folder = templateFolder.name
                            result.folderFull = versionFolder.absolutePath
                            result.id = id
                            // Версия в git
                            def version = versionFolder.name.substring(1)

                            if (versions[version] == null) { // Версия есть в git, но нет в БД
                                result.check = "В БД не найдена версия макета для папки «${templateFolder.absolutePath}» c form_type.id = $id"
                                result.versionGit = version
                                result.error = true
                                scanResult.add(result)
                            } else { // Версии совпали
                                result.name = versions[version]?.name
                                result.versionGit = version
                                result.versionDB = versions[version]?.version
                                scanResult.add(result)

                                // Сравненение скриптов
                                def scriptFile = new File("$versionFolder/script.groovy")
                                if (!scriptFile.exists()) {
                                    result.check = "Скрипт не найден в «${scriptFile.absolutePath}»"
                                    result.error = true
                                } else {
                                    def dbScript = versions[version]?.script?.trim()?.replaceAll("\r", "")
                                    def gitScript = scriptFile?.text?.trim()?.replaceAll("\r", "")
                                    if (dbScript == gitScript) {
                                        result.check = "Ok"
                                    } else {
                                        result.check = "Скрипты отличаются"
                                        result.error = true
                                    }
                                }
                                // Удаляем из списка не найденных
                                notExistVersions.remove(version)
                            }
                        }
                        // Проверка версий, которых нет в git
                        notExistVersions.each { version, template ->
                            def result = new Expando()
                            result.versionDB = version
                            result.name = template.name
                            result.id = id
                            result.check = "Нет в git! (id=${template.id})"
                            result.error = true
                            scanResult.add(result)
                        }
                    } else {
                        def result = new Expando()
                        result.id = id
                        result.check = "В БД нет версий для макета с form_type.id = $id!"
                        result.error = true
                        scanResult.add(result)
                    }
                }
            } else {
                def result = new Expando()
                result.check = "Для папки «${templateFolder.absolutePath}» не найдено соответствие в TEMPLATE_NAME_TO_TYPE_ID. Необходимо его задать!"
                result.error = true
                scanResult.add(result)
            }
        }
        return scanResult
    }

    // Отчет в html-файле
    private static void printReport(def versionsMap) {
        def writer = new FileWriter(new File(REPORT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                title "Сравнение макетов"
                style(type: "text/css", '''
                        table {
                            font-family: verdana,helvetica,arial,sans-serif;
                            background-color: #FBFEFF;
                            width: 100%;
                            border-collapse: collapse;
                        }
                        td, th {
                            padding: 2px 5px 2px 5px;
                            border: 1px solid #E2E5E6;
                        }
                        tr:hover {
                            background-color: #E2E5E6;
                        }
                        th {
                            background-color: #0C183D;
                            color: white;
                        }
                        .td_ok {
                            color: #009900;
                            font-weight: bold;
                        }
                        .td_error {
                           color: #FF0000;
                        }
                        .hdr {
                            color: #0C183D;
                            font-weight: bold;
                            text-align: center;
                            background-color: white;
                            padding: 10px;
                        }
                        ''')
            }
            body {
                p "Сравнение макетов в БД $DB_USER и git:"
                table {
                    TAX_FOLDERS.keySet().each { folderName ->
                        def scanResult = scanSrcFolder(versionsMap, folderName)
                        if (!scanResult.isEmpty()) {
                            tr {
                                td(colspan: 6, class: 'hdr', TAX_FOLDERS[folderName])
                            }
                            tr {
                                th 'Id'
                                th 'Папка'
                                th 'Название'
                                th 'Версия git'
                                th 'Версия БД'
                                th 'Сравнение'
                            }
                            scanResult.each { result ->
                                tr {
                                    td result.id
                                    td {
                                        a(href: result.folderFull, result.folder)
                                    }
                                    td result.name
                                    td result.versionGit
                                    td result.versionDB
                                    td(class: (result.error ? 'td_error' : 'td_ok'), result.check)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static void main(String[] args) {
        // Удаление старого отчета, если есть
        def report = new File(REPORT_NAME)
        if (report.exists()) {
            report.delete()
        }
        // Построение отчета сравнения
        printReport(getDBVersions())
    }
}
