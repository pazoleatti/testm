package com.aplana.taxaccounting

import groovy.sql.Sql

/**
 * Утилита сравнения скриптов из git и БД с учетом версионирования.
 * Если скрипты в БД не актуальны, то они обновляются.
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
    def static DB_USER = 'TAX_0_3_8'
    def static DB_PASSWORD = 'TAX'
    // Схема для сравнения макетов
    def static DB_USER_COMPARE = 'TAX_0_3_7'

    // Путь к папке с шаблонами
    def static SRC_FOLDER_PATH = '../src/main/resources/form_template'
    def static TAX_FOLDERS = ['deal': 'МУКС',
            'income': 'Налог на прибыль',
            'vat': 'НДС',
            'transport': 'Транспортный налог']

    def static REPORT_GIT_NAME = 'report_git_db_compare.html'
    def static REPORT_DB_NAME = 'report_db_compare.html'

    def static HTML_STYLE = '''
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
                        '''

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
                    'declaration': -1,
                    'vat_724_1': 600,
                    'vat_724_2_1': 601,
                    'vat_724_2_2': 602,
                    'vat_724_4': 603,
                    'vat_724_6': 604,
                    'vat_724_7': 605,
                    'vat_937_1': 606,
                    'vat_937_2': 608,
                    'vat_937_2_13': 609,
                    'vat_973_1_14': 607
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

        sql.eachRow("select id, type_id, to_char(version, 'RRRR') as version, name, script, status from form_template where status not in (-1, 2)") {
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
            version.status = it.status
            version.script = it.script?.characterStream?.text
            map[type_id].put(it.version, version)
        }
        sql.close()
        println("Load DB form_template OK")
        return map
    }

    // Сравнение git-версии с версией в БД и загрузка в случае отличий
    def static scanSrcFolderAndUpdateDb(def versionsMap, def folderName, def sql) {
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
                                result.status = versions[version]?.status
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
                                        result.error = true
                                        def updateResult = -1
                                        if (sql == null) {
                                            result.check = "Скрипт устарел"
                                        } else {
                                            def error = "Число измененных строк не равно 1."
                                            try {
                                                updateResult = sql.executeUpdate("update form_template set script = ? where id = ? and type_id = ?", scriptFile?.text, versions[version].id, id)
                                                println("Update form_template id = ${versions[version].id}, type_id=$id")
                                            } catch (Exception ex) {
                                                error = ex.getLocalizedMessage()
                                                ex.printStackTrace()
                                            }
                                            result.check = updateResult == 1 ? "Скрипт устарел и был обновлен" : "Скрипт устарел. Ошибка обновления: $error"
                                        }
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
                            result.status = template.status
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

    // Загрузка из git в БД и отчет в html-файле
    private static void updateScripts(def versionsMap, def checkOnly = true) {
        println("DBMS connect: url=$DB_URL user=$DB_USER")
        def sql = Sql.newInstance(DB_URL, DB_USER, DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def writer = new FileWriter(new File(REPORT_GIT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                title "Сравнение макетов"
                style(type: "text/css", HTML_STYLE)
            }
            body {
                p "Сравнение макетов в БД $DB_USER и git:"
                table {
                    TAX_FOLDERS.keySet().each { folderName ->
                        def scanResult = scanSrcFolderAndUpdateDb(versionsMap, folderName, checkOnly ? null : sql)
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
                                    if (result.status != 0) {
                                        td {
                                            s result.name
                                        }
                                    } else {
                                        td result.name
                                    }
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
        writer.close()
        sql.close()
        def action = checkOnly ? 'Check' : 'Update'
        println("$action DB form_template OK")
    }

    // Сравненение шаблонов в БД
    static void compareDBFormTemplate(def prefix1, def prefix2) {
        // 1
        println("DBMS connect: url=$DB_URL user=$prefix1")
        def sql = Sql.newInstance(DB_URL, prefix1, DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def allVersions = [:]

        def map1 = [:]
        def columns1 = [:]

        sql.eachRow("select id, type_id, data_rows, fixed_rows, name, fullname, code, data_headers, to_char(version, 'RRRR') as version, status, script, monthly from form_template where status not in (-1, 2)") {
            def type_id = it.type_id as Integer
            if (map1[type_id] == null) {
                map1.put((Integer) it.type_id, [:])
            }
            // Версия макета
            def version = new Expando()
            version.id = it.id as Integer
            version.type_id = it.type_id as Integer
            version.data_rows = it.data_rows?.characterStream?.text
            version.fixed_rows = it.fixed_rows as Integer
            version.name = it.name
            version.fullname = it.fullname
            version.code = it.code
            version.data_headers = it.data_headers?.characterStream?.text
            version.version = it.version
            version.status = it.status
            version.script = it.script?.characterStream?.text
            version.monthly = it.monthly as Integer
            map1[type_id].put(it.version, version)
            if (!allVersions.containsKey(type_id)) {
                allVersions.put(type_id, [] as Set)
            }
            allVersions[type_id].add(version.version)
        }

        // Графы
        sql.eachRow("select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, attribute_id2 from form_column where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord") {
            def form_template_id = it.form_template_id as Integer
            if (!columns1.containsKey(form_template_id)) {
                columns1.put(form_template_id, [] as Set)
            }
            def column = new Expando()
            column.id = it.id as Integer
            column.form_template_id = form_template_id
            column.ord = it.ord
            column.alias = it.alias
            column.type = it.type
            column.width = it.width
            column.precision = it.precision
            column.max_length = it.max_length
            column.checking = it.checking
            column.attribute_id = it.attribute_id
            column.format = it.format
            column.filter = it.filter
            column.parent_column_id = it.parent_column_id
            column.parent_column_id = it.parent_column_id
            column.attribute_id2 = it.attribute_id2
            columns1[form_template_id].add(column)
        }

        sql.close()
        println("Load DB form_template1 OK")

        // 2
        println("DBMS connect: url=$DB_URL user=$prefix2")
        sql = Sql.newInstance(DB_URL, prefix2, DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def map2 = [:]
        def columns2 = [:]

        sql.eachRow("select id, type_id, data_rows, fixed_rows, name, fullname, code, data_headers, to_char(version, 'RRRR') as version, status, script, monthly from form_template where status not in (-1, 2)") {
            def type_id = it.type_id as Integer
            if (map2[type_id] == null) {
                map2.put((Integer) it.type_id, [:])
            }
            // Версия макета
            def version = new Expando()
            version.id = it.id as Integer
            version.type_id = it.type_id as Integer
            version.data_rows = it.data_rows?.characterStream?.text
            version.fixed_rows = it.fixed_rows as Integer
            version.name = it.name
            version.fullname = it.fullname
            version.code = it.code
            version.data_headers = it.data_headers?.characterStream?.text
            version.version = it.version
            version.status = it.status
            version.script = it.script?.characterStream?.text
            version.monthly = it.monthly as Integer
            map2[type_id].put(it.version, version)
            if (!allVersions.containsKey(type_id)) {
                allVersions.put(type_id, [] as Set)
            }
            allVersions[type_id].add(version.version)
        }

        // Графы
        sql.eachRow("select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, attribute_id2 from form_column where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord") {
            def form_template_id = it.form_template_id as Integer
            if (!columns2.containsKey(form_template_id)) {
                columns2.put(form_template_id, [] as Set)
            }
            def column = new Expando()
            column.id = it.id as Integer
            column.form_template_id = form_template_id
            column.ord = it.ord
            column.alias = it.alias
            column.type = it.type
            column.width = it.width
            column.precision = it.precision
            column.max_length = it.max_length
            column.checking = it.checking
            column.attribute_id = it.attribute_id
            column.format = it.format
            column.filter = it.filter
            column.parent_column_id = it.parent_column_id
            column.parent_column_id = it.parent_column_id
            column.attribute_id2 = it.attribute_id2
            columns2[form_template_id].add(column)
        }

        sql.close()
        println("Load DB form_template2 OK")

        // Построение отчета
        def report = new File(REPORT_DB_NAME)
        if (report.exists()) {
            report.delete()
        }

        def writer = new FileWriter(new File(REPORT_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                title "Сравнение макетов в $prefix1 и $prefix2"
                style(type: "text/css", HTML_STYLE)
            }
            body {
                p "Сравнение макетов в БД $prefix1 и $prefix2:"
                table {
                    TAX_FOLDERS.keySet().each { taxName ->
                        tr {
                            td(colspan: 12, class: 'hdr', TAX_FOLDERS[taxName])
                        }
                        tr {
                            th(rowspan: 2, 'Id')
                            th(rowspan: 2, 'Название')
                            th(rowspan: 2, 'Версия')
                            th(colspan: 9, 'Результат сравнения')
                        }
                        tr {
                            th 'name'
                            th 'fullname'
                            th 'fixedrows'
                            th 'datarows'
                            th 'dataheaders'
                            th 'status'
                            th 'script'
                            th 'columns'
                            th 'monthly'
                        }

                        TEMPLATE_NAME_TO_TYPE_ID[taxName].each { folderName, type_id ->
                            if (type_id != -1) {
                                allVersions[type_id].each { version ->
                                    // Сравнение
                                    def tmp1 = map1[type_id]?.get(version)
                                    def tmp2 = map2[type_id]?.get(version)

                                    def columnsSet1 = columns1[tmp1?.id]
                                    def columnsSet2 = columns2[tmp2?.id]

                                    def name = tmp1?.name
                                    if (name == null) {
                                        name = tmp2?.name
                                    }

                                    def nameC = tmp1?.name == tmp2?.name ? '+' : '—'
                                    def fullnameC = tmp1?.fullname == tmp2?.fullname ? '+' : '—'
                                    def fixedrowsC = tmp1?.fixed_rows == tmp2?.fixed_rows ? '+' : '—'
                                    def datarowsC = tmp1?.data_rows == tmp2?.data_rows ? '+' : '—'
                                    def dataheadersC = tmp1?.data_headers == tmp2?.data_headers ? '+' : '—'
                                    def statusC = tmp1?.status == tmp2?.status ? '+' : '—'
                                    def scriptC = tmp1?.script == tmp2?.script ? '+' : '—'

                                    def columnsChangesList = [] as Set
                                    if (columnsSet1 != null && columnsSet2 == null || columnsSet1 == null && columnsSet2 != null) {
                                        columnsChangesList.add("Нет в ${columnsSet1 == null ? prefix1 : prefix2}")
                                    } else if (columnsSet1 != null && columnsSet2 != null) {
                                        if (columnsSet1.size() != columnsSet2.size()) {
                                            columnsChangesList.add("Не совпадает количество граф.")
                                        } else {
                                            for (int i = 0; i < columnsSet1.size(); i++) {
                                                def col1 = columnsSet1.getAt(i)
                                                def col2 = columnsSet2.getAt(i)
                                                if (col1.name != col2.name) {
                                                    columnsChangesList.add("name")
                                                }
                                                if (col1.ord != col2.ord) {
                                                    columnsChangesList.add("ord")
                                                }
                                                if (col1.alias != col2.alias) {
                                                    columnsChangesList.add("alias")
                                                }
                                                if (col1.type != col2.type) {
                                                    columnsChangesList.add("type")
                                                }
                                                if (col1.width != col2.width) {
                                                    columnsChangesList.add("width")
                                                }
                                                if (col1.precision != col2.precision) {
                                                    columnsChangesList.add("precision")
                                                }
                                                if (col1.max_length != col2.max_length) {
                                                    columnsChangesList.add("max_length")
                                                }
                                                if (col1.checking != col2.checking) {
                                                    columnsChangesList.add("checking")
                                                }
                                                if (col1.attribute_id != col2.attribute_id) {
                                                    columnsChangesList.add("attribute_id")
                                                }
                                                if (col1.format != col2.format) {
                                                    columnsChangesList.add("format")
                                                }
                                                if (col1.filter != col2.filter) {
                                                    columnsChangesList.add("filter")
                                                }
                                                if (col1.parent_column_id != col2.parent_column_id) {
                                                    columnsChangesList.add("parent_column_id")
                                                }
                                                if (col1.attribute_id2 != col2.attribute_id2) {
                                                    columnsChangesList.add("attribute_id2")
                                                }
                                            }
                                        }
                                    }

                                    def columnsC = columnsChangesList.isEmpty() ? '+' : '—'
                                    def monthlyC = tmp1?.monthly == tmp2?.monthly ? '+' : '—'

                                    tr {
                                        td type_id
                                        td name
                                        td version
                                        if (nameC == '+') {
                                            td(class: 'td_ok', nameC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.name}, $prefix2 = ${tmp2?.name}", nameC)
                                        }

                                        if (fullnameC == '+') {
                                            td(class: 'td_ok', fullnameC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.fullname}, $prefix2 = ${tmp2?.fullname}", fullnameC)
                                        }

                                        if (fixedrowsC == '+') {
                                            td(class: 'td_ok', fixedrowsC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.fixed_rows}, $prefix2 = ${tmp2?.fixed_rows}", fixedrowsC)
                                        }

                                        if (datarowsC == '+') {
                                            td(class: 'td_ok', datarowsC)
                                        } else {
                                            td(class: 'td_error', title: 'См. БД', datarowsC)
                                        }

                                        if (dataheadersC == '+') {
                                            td(class: 'td_ok', dataheadersC)
                                        } else {
                                            td(class: 'td_error', title: 'См. БД', dataheadersC)
                                        }

                                        if (statusC == '+') {
                                            td(class: 'td_ok', statusC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.status}, $prefix2 = ${tmp2?.status}", statusC)
                                        }

                                        if (scriptC == '+') {
                                            td(class: 'td_ok', scriptC)
                                        } else {
                                            td(class: 'td_error', title: 'См. БД', scriptC)
                                        }

                                        if (columnsC == '+') {
                                            td(class: 'td_ok', columnsC)
                                        } else {
                                            td(class: 'td_error', title: "Отличия: ${columnsChangesList.join(', ')}", columnsC)
                                        }

                                        if (monthlyC == '+') {
                                            td(class: 'td_ok', monthlyC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.monthly}, $prefix2 = ${tmp2?.monthly}", monthlyC)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.close()
    }

    static void main(String[] args) {
        // Удаление старого отчета, если есть
        def report = new File(REPORT_GIT_NAME)
        if (report.exists()) {
            report.delete()
        }
        // Построение отчета сравнения
        updateScripts(getDBVersions(), true)
        println("See REPORT_GIT_NAME for details")
        // Сравнение схем в БД
        compareDBFormTemplate(DB_USER, DB_USER_COMPARE)
    }
}
