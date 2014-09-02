package com.aplana.taxaccounting

import groovy.sql.Sql

/**
 * Отчет сравнения Git и БД
 */
class DBReport {
    // Сравненение шаблонов в БД
    def static void compareDBFormTemplate(def prefix1, def prefix2) {
        // Запросы на получение макетов
        def sqlTemplate1 = "select id, type_id, data_rows, fixed_rows, name, fullname, header as code, data_headers, to_char(version, 'RRRR') as version, status, script, monthly from form_template where status not in (-1, 2)"
        def sqlTemplate2 = "select id, type_id, data_rows, fixed_rows, name, fullname, header as code, data_headers, to_char(version, 'RRRR') as version, status, script, monthly from form_template where status not in (-1, 2)"

        // Запросы на получение колонок
        def sqlColumns1 = "select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, (select alias from form_column fc2 where fc2.id = fc1.parent_column_id) as parent_alias, attribute_id2, numeration_row from form_column fc1 where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord"
        def sqlColumns2 = "select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, (select alias from form_column fc2 where fc2.id = fc1.parent_column_id) as parent_alias, attribute_id2, numeration_row from form_column fc1 where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord"

        // Перечень всех всерсий в БД (заполняется в getTemplates)
        def allVersions = [:]

        // Макеты
        def templates1 = getTemplates(prefix1, sqlTemplate1, sqlColumns1, allVersions)
        def templates2 = getTemplates(prefix2, sqlTemplate2, sqlColumns2, allVersions)

        // Построение отчета
        def report = new File(Main.REPORT_DB_NAME)
        if (report.exists()) {
            report.delete()
        }

        // Данные для отображения отличий в графах НФ
        def columnTableData = [:]

        // Данные для отображения отличий в шапках НФ
        def headerTableData = [:]

        def writer = new FileWriter(new File(Main.REPORT_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение макетов в $prefix1 и $prefix2"
                style(type: "text/css", Main.HTML_STYLE)
                script('', type: 'text/javascript', src: 'http://code.jquery.com/jquery-1.9.1.min.js')
                script('', type: 'text/javascript', src: 'http://code.jquery.com/ui/1.10.3/jquery-ui.min.js')
                link('', rel: 'stylesheet', href: 'http://code.jquery.com/ui/1.10.3/themes/black-tie/jquery-ui.css')
            }
            body {
                // Скрипт вызова диалога
                script(type: 'text/javascript', '''
                $(function() {
                    $('.cln').click(function() {
                        $($(this).data('tbl')).dialog("open");
                    });
                    $(".dlg").dialog({
                            modal: true,
                            autoOpen: false,
                            position: ['center', 'top'],
                    width: 1200
                    });
                });''')
                p "Сравнение макетов в БД $prefix1 и $prefix2:"
                table(class: 'rt') {
                    Main.TAX_FOLDERS.keySet().each { taxName ->
                        tr {
                            td(colspan: 14, class: 'hdr', Main.TAX_FOLDERS[taxName])
                        }
                        tr {
                            th(rowspan: 2, 'type_id')
                            th(rowspan: 2, 'Название')
                            th(rowspan: 2, 'Версия')
                            th(rowspan: 2, "$prefix1 id")
                            th(rowspan: 2, "$prefix2 id")
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

                        // Сравнение
                        Main.TEMPLATE_NAME_TO_TYPE_ID[taxName].each { folderName, type_id ->
                            if (type_id != -1) {
                                allVersions[type_id].each { version ->
                                    // Макеты
                                    def tmp1 = templates1.templateMap[type_id]?.get(version)
                                    def tmp2 = templates2.templateMap[type_id]?.get(version)

                                    // Графы
                                    def columnsSet1 = templates1.columnsMap[tmp1?.id]
                                    def columnsSet2 = templates2.columnsMap[tmp2?.id]

                                    // Имя из первого макета, а при его отсутствии — из второго
                                    def name = tmp1?.name
                                    if (name == null) {
                                        name = tmp2?.name
                                    }

                                    // Признак сравнения
                                    def nameC = tmp1?.name == tmp2?.name ? '+' : '—'
                                    def fullnameC = tmp1?.fullname == tmp2?.fullname ? '+' : '—'
                                    def fixedrowsC = tmp1?.fixed_rows == tmp2?.fixed_rows ? '+' : '—'
                                    def datarowsC = tmp1?.data_rows == tmp2?.data_rows ? '+' : '—'
                                    def dataheadersC = tmp1?.data_headers == tmp2?.data_headers ? '+' : '—'
                                    def statusC = tmp1?.status == tmp2?.status ? '+' : '—'
                                    def scriptC = tmp1?.script == tmp2?.script ? '+' : '—'

                                    // Сравнение шапок НФ
                                    def headerDiff = null
                                    if (tmp1?.data_headers == null && tmp2?.data_headers != null || tmp1?.data_headers != null && tmp2?.data_headers == null) {
                                        // Один заголовок отсутствует
                                        headerDiff = "Нет в ${tmp1?.data_headers == null ? prefix1 : prefix2}"
                                    } else if (tmp1?.data_headers != null && tmp2?.data_headers != null && tmp1?.data_headers != tmp2?.data_headers) {
                                        // Сравнение заголовков
                                        def root1 = new XmlParser().parseText(tmp1.data_headers)
                                        def root2 = new XmlParser().parseText(tmp2.data_headers)

                                        // Сравнение
                                        def header1 = getHeaderCells(root1) // Шапка макета 1
                                        def header2 = getHeaderCells(root2) // Шапка макета 2
                                        def headerCompare = [] as Set // Набор отличающихся ячеек

                                        // Сравнение ячеек шапки
                                        for (def i = 0; i < Math.max(header1.size(), header2.size()); i++) {
                                            def row1 = header1.size() > i ? header1.getAt(i) : null
                                            def row2 = header2.size() > i ? header2.getAt(i) : null
                                            for (def j = 0; j < Math.max(row1 == null ? 0 : row1.size(), row2 == null ? 0 : row2.size()); j++) {
                                                def cell1 = row1 == null ? null : (row1.size() > j ? row1.getAt(j) : null)
                                                def cell2 = row2 == null ? null : (row2.size() > j ? row2.getAt(j) : null)
                                                if (cell1 != cell2) {
                                                    headerCompare.add("$i $j")
                                                }
                                            }
                                        }

                                        // Невидимые ячейки
                                        def hiddenCells1 = getHiddenCells(root1)
                                        def hiddenCells2 = getHiddenCells(root2)

                                        // Не учитываем отличия в невидимых ячейках
                                        headerCompare.removeAll(hiddenCells1.intersect(hiddenCells2))

                                        if (headerCompare.isEmpty()) {
                                            headerDiff = "Есть отличия в невидимых ячейках"
                                            dataheadersC = '+/—'
                                        } else {
                                            headerDiff = "Подробнее…"
                                            def data = new Expando()
                                            data.root1 = root1
                                            data.root2 = root2
                                            data.name = name
                                            data.type_id = type_id
                                            data.version = version
                                            data.prefix1 = prefix1
                                            data.prefix2 = prefix2
                                            data.headerCompare = headerCompare
                                            data.hiddenCells1 = hiddenCells1
                                            data.hiddenCells2 = hiddenCells2
                                            headerTableData.put("h_${type_id}_${version}", data)
                                        }
                                    }

                                    // Сравнение граф НФ
                                    def colDiff = null
                                    def headers = ['ord', 'alias', 'name', 'type', 'width', 'precision', 'max_length', 'checking', 'attribute_id',
                                            'format', 'filter', 'parent_alias', 'attribute_id2', 'numeration_row']
                                    if (columnsSet1 != null && columnsSet2 == null || columnsSet1 == null && columnsSet2 != null) {
                                        colDiff = "Нет в ${columnsSet1 == null ? prefix1 : prefix2}"
                                    } else if (columnsSet1 != null && columnsSet2 != null) {
                                        def changesMap = [:]

                                        for (def i = 0; i < Math.max(columnsSet1.size(), columnsSet2.size()); i++) {
                                            def col1 = columnsSet1.size() > i ? columnsSet1.getAt(i) : null
                                            def col2 = columnsSet2.size() > i ? columnsSet2.getAt(i) : null

                                            headers.each { header ->
                                                if (col1 == null || col2 == null || col1[header] != col2[header]) {
                                                    if (!changesMap.containsKey(i)) {
                                                        changesMap.put(i, [])
                                                    }
                                                    changesMap[i].add(header)
                                                }
                                            }
                                        }

                                        if (!changesMap.isEmpty()) {
                                            colDiff = "Подробнее…"
                                            def data = new Expando()
                                            data.tmp1 = tmp1
                                            data.tmp2 = tmp2
                                            data.name = name
                                            data.changesMap = changesMap
                                            data.headers = headers
                                            data.columnsSet1 = columnsSet1
                                            data.columnsSet2 = columnsSet2
                                            data.type_id = type_id
                                            data.version = version
                                            data.prefix1 = prefix1
                                            data.prefix2 = prefix2
                                            columnTableData.put("_${type_id}_${version}", data)
                                        }
                                    }

                                    def columnsC = colDiff == null ? '+' : '—'
                                    def monthlyC = tmp1?.monthly == tmp2?.monthly ? '+' : '—'

                                    tr(class: ((tmp1?.id != null && tmp2?.id != null) ? 'nr' : 'er')) {
                                        td type_id
                                        td name
                                        td version
                                        td tmp1?.id
                                        td tmp2?.id

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
                                            def title = 'См. БД'
                                            def tdClass = 'td_error'
                                            if (tmp1?.data_rows == null && tmp2?.data_rows != null || tmp1?.data_rows != null && tmp2?.data_rows == null) {
                                                def emptyDataRows = "<?xml version=\"1.0\" encoding=\"utf-8\"?><rows/>"
                                                title = "Нет в ${tmp1?.data_rows == null ? prefix1 : prefix2}"
                                                if (tmp1?.data_rows?.trim() == emptyDataRows || tmp2?.data_rows?.trim() == emptyDataRows) {
                                                    tdClass = 'td_gr'
                                                    datarowsC = '+/—'
                                                    title = "$title, но оба пустые"
                                                } else {
                                                    // TODO Детальное сравнение
                                                }
                                            }
                                            td(class: tdClass, title: title, datarowsC)
                                        }

                                        if (dataheadersC == '+') {
                                            td(class: 'td_ok', dataheadersC)
                                        } else if (dataheadersC == '—') {
                                            td(class: 'td_error cln', title: headerDiff, 'data-tbl': "#h_${type_id}_${version}", dataheadersC)
                                        } else {
                                            td(class: 'td_gr', title: headerDiff, dataheadersC)
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
                                            td(class: 'td_error cln', title: colDiff, 'data-tbl': "#_${type_id}_${version}", columnsC)
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
                // Скрытый блок для отображения отличий в графах НФ
                columnTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение граф шаблона вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        table(class: 'rt') {
                            // Вывод таблицы с колонками 1
                            printColumnsTable(builder, data.changesMap, data.headers, data.prefix1, data.columnsSet1)
                            // Вывод таблицы с колонками 2
                            printColumnsTable(builder, data.changesMap, data.headers, data.prefix2, data.columnsSet2)
                        }
                    }
                }
                // Скрытый блок для отображения отличий шапках НФ
                headerTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение заголовков шаблона вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        // Вывод таблицы с шапкой 1
                        printHeaderTable(builder, data.prefix1, data.root1, data.headerCompare, data.hiddenCells1)
                        // Вывод таблицы с шапкой 2
                        printHeaderTable(builder, data.prefix2, data.root2, data.headerCompare, data.hiddenCells2)
                    }
                }
            }
        }
        writer.close()
        println("See ${Main.REPORT_DB_NAME} for details")
    }

    // Получение макетов
    def private static getTemplates(def prefix, def sqlTemplate, def sqlColumns, def allVersions) {
        println("DBMS connect: $prefix")
        def retVal = new Expando()

        def sql = Sql.newInstance(Main.DB_URL, prefix, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def templateMap = [:]
        def columnsMap = [:]

        sql.eachRow(sqlTemplate) {
            def type_id = it.type_id as Integer
            if (templateMap[type_id] == null) {
                templateMap.put((Integer) it.type_id, [:])
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
            version.script = it.script?.characterStream?.text?.trim()?.replaceAll("\r", "")
            version.monthly = it.monthly as Integer
            templateMap[type_id].put(it.version, version)
            if (!allVersions.containsKey(type_id)) {
                allVersions.put(type_id, [] as Set)
            }
            allVersions[type_id].add(version.version)
        }

        // Графы
        sql.eachRow(sqlColumns) {
            def form_template_id = it.form_template_id as Integer
            if (!columnsMap.containsKey(form_template_id)) {
                columnsMap.put(form_template_id, [] as Set)
            }
            def column = new Expando()
            column.id = it.id as Integer
            column.name = it.name
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
            column.parent_alias = it.parent_alias
            column.attribute_id2 = it.attribute_id2
            column.numeration_row = it.numeration_row
            columnsMap[form_template_id].add(column)
        }

        sql.close()
        println("Load DB form_template from $prefix OK")
        retVal.templateMap = templateMap
        retVal.columnsMap = columnsMap
        return retVal
    }

    // XML-data_headers → список списков ячеек
    def private static getHeaderCells(def root) {
        def retVal = []
        root.each { row ->
            def headerRow = []
            retVal.add(headerRow)
            row.each { col ->
                headerRow.add(col.@value)
            }
        }
        return retVal
    }

    // XML-data_headers → список невидимых ячеек
    def private static getHiddenCells(def root) {
        def retVal = []
        def skipRowAliases = [:]
        def rowCounter = 0
        root.each { row ->
            def skipCol = 0
            def colCounter = 0
            row.each { col ->
                if (skipCol != 0) {
                    skipCol--
                    retVal.add("${rowCounter} ${colCounter}")
                } else {
                    if (skipRowAliases[col.@alias] != null && skipRowAliases[col.@alias] != 0) {
                        skipRowAliases[col.@alias] = skipRowAliases[col.@alias] - 1
                        retVal.add("${rowCounter} ${colCounter}")
                    } else {
                        def colSpan = col.@colSpan
                        def rowSpan = col.@rowSpan
                        if (colSpan != '1') {
                            skipCol = colSpan.toInteger() - 1
                        }
                        if (rowSpan != '1') {
                            skipRowAliases.put(col.@alias, rowSpan.toInteger() - 1)
                        }
                    }
                }
                colCounter++
            }
            rowCounter++
        }
        return retVal
    }

    // Вывод шапки для data_headers
    def private static printHeaderTable(def builder, def prefix, def root, def headerCompare, def hiddenCells) {
        builder.div(class: 'hdrh', prefix)
        builder.table(class: 'rt') {
            def rowCounter = 0
            root.each { row ->
                tr {
                    def colCounter = 0
                    row.each { col ->
                        def key = "$rowCounter $colCounter"
                        if (!hiddenCells.contains(key)) {
                            td((headerCompare.contains(key) ? [class: 'td_error'] : [:]) +
                                    (col.@colSpan == '1' ? [:] : [colspan: col.@colSpan]) +
                                    (col.@rowSpan == '1' ? [:] : [rowspan: col.@rowSpan]),
                                    col.@value)
                        }
                        colCounter++
                    }
                }
                rowCounter++
            }
        }
    }

    // Вывод шапки для columns
    def private static printColumnsTable(def builder, def changesMap, def headers, def prefix, def columnsSet) {
        // Название таблицы
        builder.tr {
            td(colspan: 13, class: 'hdr', prefix)
        }
        builder.tr {
            headers.each { header ->
                th header
            }
        }
        // Содержимое таблицы
        columnsSet.eachWithIndex { column, i ->
            builder.tr {
                headers.each { header ->
                    td((changesMap[i]?.contains(header) ? [class: 'td_error'] : [:]), column[header])
                }
            }
        }
    }
}
