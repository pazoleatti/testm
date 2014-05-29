package com.aplana.taxaccounting

import groovy.sql.Sql

/**
 * Отчет сравнения Git и БД
 */
class DBReport {
    // Сравненение шаблонов в БД
    def static void compareDBFormTemplate(def prefix1, def prefix2) {
        // 1
        println("DBMS connect: $prefix1")
        def sql = Sql.newInstance(Main.DB_URL, prefix1, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

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
            version.script = it.script?.characterStream?.text?.trim()?.replaceAll("\r", "")
            version.monthly = it.monthly as Integer
            map1[type_id].put(it.version, version)
            if (!allVersions.containsKey(type_id)) {
                allVersions.put(type_id, [] as Set)
            }
            allVersions[type_id].add(version.version)
        }

        // Графы
        sql.eachRow("select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, (select alias from form_column fc2 where fc2.id = fc1.parent_column_id) as parent_alias, attribute_id2 from form_column fc1 where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord") {
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
            column.parent_alias = it.parent_alias
            column.attribute_id2 = it.attribute_id2
            columns1[form_template_id].add(column)
        }

        sql.close()
        println("Load DB form_template1 OK")

        // 2
        println("DBMS connect: $prefix2")
        sql = Sql.newInstance(Main.DB_URL, prefix2, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

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
            version.script = it.script?.characterStream?.text?.trim()?.replaceAll("\r", "")
            version.monthly = it.monthly as Integer
            map2[type_id].put(it.version, version)
            if (!allVersions.containsKey(type_id)) {
                allVersions.put(type_id, [] as Set)
            }
            allVersions[type_id].add(version.version)
        }

        // Графы
        sql.eachRow("select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, (select alias from form_column fc2 where fc2.id = fc1.parent_column_id) as parent_alias, attribute_id2 from form_column fc1 where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord") {
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
            column.parent_alias = it.parent_alias
            column.attribute_id2 = it.attribute_id2
            columns2[form_template_id].add(column)
        }

        sql.close()
        println("Load DB form_template2 OK")

        // Построение отчета
        def report = new File(Main.REPORT_DB_NAME)
        if (report.exists()) {
            report.delete()
        }

        def writer = new FileWriter(new File(Main.REPORT_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)

        def columnTableData = [:]

        def headerTableData = [:]

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

                        Main.TEMPLATE_NAME_TO_TYPE_ID[taxName].each { folderName, type_id ->
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

                                    def headerDiff = null
                                    if (tmp1?.data_headers == null && tmp2?.data_headers != null || tmp1?.data_headers != null && tmp2?.data_headers == null) {
                                        headerDiff = "Нет в ${tmp1?.data_headers == null ? prefix1 : prefix2}"
                                    } else if (tmp1?.data_headers != null && tmp2?.data_headers != null && tmp1?.data_headers != tmp2?.data_headers) {
                                        // Сравнение заголовков
                                        def root1 = new XmlParser().parseText(tmp1.data_headers)
                                        def root2 = new XmlParser().parseText(tmp2.data_headers)

                                        // Сравнение
                                        def header1 = []
                                        def header2 = []
                                        def headerCompare = [] as Set

                                        // Шапка макета 1
                                        root1.each { row ->
                                            def headerRow = []
                                            header1.add(headerRow)
                                            row.each { col ->
                                                headerRow.add(col.@value)
                                            }
                                        }

                                        // Шапка макета 2
                                        root2.each { row ->
                                            def headerRow = []
                                            header2.add(headerRow)
                                            row.each { col ->
                                                headerRow.add(col.@value)
                                            }
                                        }

                                        // Сравнение ячеек шапки
                                        for (def i = 0; i < Math.max(header1.size(), header2.size()); i++) {
                                            def row1 = header1.size() > i ? header1.getAt(i) : null
                                            def row2 = header2.size() > i ? header2.getAt(i) : null

                                            for (
                                                    def j = 0; j < Math.max(row1 == null ? 0 : row1.size(), row2 == null ? 0 : row2.size()); j++) {
                                                def cell1 = row1 == null ? null : (row1.size() > j ? row1.getAt(j) : null)
                                                def cell2 = row2 == null ? null : (row2.size() > j ? row2.getAt(j) : null)

                                                if (cell1 != cell2) {
                                                    headerCompare.add("$i $j")
                                                }
                                            }
                                        }

                                        // Невидимые ячейки
                                        def hiddenCell1 = []
                                        def hiddenCell2 = []

                                        def skipRowAliases = [:]
                                        def rowCounter = 0
                                        root1.each { row ->
                                            def skipCol = 0
                                            def colCounter = 0
                                            row.each { col ->
                                                if (skipCol != 0) {
                                                    skipCol--
                                                    hiddenCell1.add("${rowCounter} ${colCounter}")
                                                } else {
                                                    if (skipRowAliases[col.@alias] != null && skipRowAliases[col.@alias] != 0) {
                                                        skipRowAliases[col.@alias] = skipRowAliases[col.@alias] - 1
                                                        hiddenCell1.add("${rowCounter} ${colCounter}")
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

                                        skipRowAliases = [:]
                                        rowCounter = 0
                                        root2.each { row ->
                                            def skipCol = 0
                                            def colCounter = 0
                                            row.each { col ->
                                                if (skipCol != 0) {
                                                    skipCol--
                                                    hiddenCell2.add("${rowCounter} ${colCounter}")
                                                } else {
                                                    if (skipRowAliases[col.@alias] != null && skipRowAliases[col.@alias] != 0) {
                                                        skipRowAliases[col.@alias] = skipRowAliases[col.@alias] - 1
                                                        hiddenCell2.add("${rowCounter} ${colCounter}")
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
                                        headerCompare.removeAll(hiddenCell1.intersect(hiddenCell2))

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
                                            headerTableData.put("h_${type_id}_${version}", data)
                                        }
                                    }

                                    def colDiff = null
                                    if (columnsSet1 != null && columnsSet2 == null || columnsSet1 == null && columnsSet2 != null) {
                                        colDiff = "Нет в ${columnsSet1 == null ? prefix1 : prefix2}"
                                    } else if (columnsSet1 != null && columnsSet2 != null) {
                                        def changesMap = [:]

                                        for (def i = 0; i < Math.max(columnsSet1.size(), columnsSet2.size()); i++) {
                                            def col1 = columnsSet1.size() > i ? columnsSet1.getAt(i) : null
                                            def col2 = columnsSet2.size() > i ? columnsSet2.getAt(i) : null
                                            if (col1?.name != col2?.name) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("name")
                                            }
                                            if (col1?.ord != col2?.ord) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("ord")
                                            }
                                            if (col1?.alias != col2?.alias) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("alias")
                                            }
                                            if (col1?.type != col2?.type) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("type")
                                            }
                                            if (col1?.width != col2?.width) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("width")
                                            }
                                            if (col1?.precision != col2?.precision) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("precision")
                                            }
                                            if (col1?.max_length != col2?.max_length) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("max_length")
                                            }
                                            if (col1?.checking != col2?.checking) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("checking")
                                            }
                                            if (col1?.attribute_id != col2?.attribute_id) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("attribute_id")
                                            }
                                            if (col1?.format != col2?.format) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("format")
                                            }
                                            if (col1?.filter != col2?.filter) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("filter")
                                            }
                                            if (col1?.parent_alias != col2?.parent_alias) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("parent_alias")
                                            }
                                            if (col1?.attribute_id2 != col2?.attribute_id2) {
                                                if (!changesMap.containsKey(i)) {
                                                    changesMap.put(i, [])
                                                }
                                                changesMap[i].add("attribute_id2")
                                            }
                                        }

                                        if (!changesMap.isEmpty()) {
                                            colDiff = "Подробнее…"
                                            def data = new Expando()
                                            data.tmp1 = tmp1
                                            data.tmp2 = tmp2
                                            data.name = name
                                            data.changesMap = changesMap
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
                columnTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение граф шаблона вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        table(class: 'rt') {
                            tr {
                                td(colspan: 12, class: 'hdr', data.prefix1)
                            }
                            tr {
                                th 'ord'
                                th 'alias'
                                th 'type'
                                th 'width'
                                th 'precision'
                                th 'max_length'
                                th 'checking'
                                th 'attribute_id'
                                th 'format'
                                th 'filter'
                                th 'parent_alias'
                                th 'attribute_id2'
                            }

                            def changesMap = data.changesMap

                            data.columnsSet1.eachWithIndex { column, i ->
                                tr {
                                    if (!changesMap[i]?.contains('ord')) {
                                        td column.ord
                                    } else {
                                        td(class: 'td_error', column.ord)
                                    }

                                    if (!changesMap[i]?.contains('alias')) {
                                        td column.alias
                                    } else {
                                        td(class: 'td_error', column.alias)
                                    }

                                    if (!changesMap[i]?.contains('type')) {
                                        td column.type
                                    } else {
                                        td(class: 'td_error', column.type)
                                    }

                                    if (!changesMap[i]?.contains('width')) {
                                        td column.width
                                    } else {
                                        td(class: 'td_error', column.width)
                                    }

                                    if (!changesMap[i]?.contains('precision')) {
                                        td column.precision
                                    } else {
                                        td(class: 'td_error', column.precision)
                                    }

                                    if (!changesMap[i]?.contains('max_length')) {
                                        td column.max_length
                                    } else {
                                        td(class: 'td_error', column.max_length)
                                    }

                                    if (!changesMap[i]?.contains('checking')) {
                                        td column.checking
                                    } else {
                                        td(class: 'td_error', column.checking)
                                    }

                                    if (!changesMap[i]?.contains('attribute_id')) {
                                        td column.attribute_id
                                    } else {
                                        td(class: 'td_error', column.attribute_id)
                                    }

                                    if (!changesMap[i]?.contains('format')) {
                                        td column.format
                                    } else {
                                        td(class: 'td_error', column.format)
                                    }

                                    if (!changesMap[i]?.contains('filter')) {
                                        td column.filter
                                    } else {
                                        td(class: 'td_error', column.filter)
                                    }

                                    if (!changesMap[i]?.contains('parent_alias')) {
                                        td column.parent_alias
                                    } else {
                                        td(class: 'td_error', column.parent_alias)
                                    }

                                    if (!changesMap[i]?.contains('attribute_id2')) {
                                        td column.attribute_id2
                                    } else {
                                        td(class: 'td_error', column.attribute_id2)
                                    }
                                }
                            }

                            tr {
                                td(colspan: 12, class: 'hdr', data.prefix2)
                            }
                            tr {
                                th 'ord'
                                th 'alias'
                                th 'type'
                                th 'width'
                                th 'precision'
                                th 'max_length'
                                th 'checking'
                                th 'attribute_id'
                                th 'format'
                                th 'filter'
                                th 'parent_alias'
                                th 'attribute_id2'
                            }

                            data.columnsSet2.eachWithIndex { column, i ->
                                tr {
                                    if (!changesMap[i]?.contains('ord')) {
                                        td column.ord
                                    } else {
                                        td(class: 'td_error', column.ord)
                                    }

                                    if (!changesMap[i]?.contains('alias')) {
                                        td column.alias
                                    } else {
                                        td(class: 'td_error', column.alias)
                                    }

                                    if (!changesMap[i]?.contains('type')) {
                                        td column.type
                                    } else {
                                        td(class: 'td_error', column.type)
                                    }

                                    if (!changesMap[i]?.contains('width')) {
                                        td column.width
                                    } else {
                                        td(class: 'td_error', column.width)
                                    }

                                    if (!changesMap[i]?.contains('precision')) {
                                        td column.precision
                                    } else {
                                        td(class: 'td_error', column.precision)
                                    }

                                    if (!changesMap[i]?.contains('max_length')) {
                                        td column.max_length
                                    } else {
                                        td(class: 'td_error', column.max_length)
                                    }

                                    if (!changesMap[i]?.contains('checking')) {
                                        td column.checking
                                    } else {
                                        td(class: 'td_error', column.checking)
                                    }

                                    if (!changesMap[i]?.contains('attribute_id')) {
                                        td column.attribute_id
                                    } else {
                                        td(class: 'td_error', column.attribute_id)
                                    }

                                    if (!changesMap[i]?.contains('format')) {
                                        td column.format
                                    } else {
                                        td(class: 'td_error', column.format)
                                    }

                                    if (!changesMap[i]?.contains('filter')) {
                                        td column.filter
                                    } else {
                                        td(class: 'td_error', column.filter)
                                    }

                                    if (!changesMap[i]?.contains('parent_alias')) {
                                        td column.parent_alias
                                    } else {
                                        td(class: 'td_error', column.parent_alias)
                                    }

                                    if (!changesMap[i]?.contains('attribute_id2')) {
                                        td column.attribute_id2
                                    } else {
                                        td(class: 'td_error', column.attribute_id2)
                                    }
                                }
                            }
                        }
                    }
                }

                headerTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение заголовков шаблона вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        def headerCompare = data.headerCompare

                        // Вывод шапки 1
                        div(class: 'hdrh', data.prefix1)
                        table(class: 'rt') {
                            def skipRowAliases = [:]
                            def rowCounter = 0
                            data.root1.each { row ->
                                tr {
                                    def skipCol = 0
                                    def colCounter = 0
                                    row.each { col ->
                                        if (skipCol != 0) {
                                            skipCol--
                                        } else {
                                            if (skipRowAliases[col.@alias] != null && skipRowAliases[col.@alias] != 0) {
                                                skipRowAliases[col.@alias] = skipRowAliases[col.@alias] - 1
                                            } else {
                                                def colSpan = col.@colSpan
                                                def rowSpan = col.@rowSpan

                                                if (colSpan != '1') {
                                                    skipCol = colSpan.toInteger() - 1
                                                }

                                                if (rowSpan != '1') {
                                                    skipRowAliases.put(col.@alias, rowSpan.toInteger() - 1)
                                                }

                                                if (!headerCompare.contains("$rowCounter $colCounter")) {
                                                    td(colspan: colSpan, rowspan: rowSpan, col.@value)
                                                } else {
                                                    td(class: 'td_error', colspan: colSpan, rowspan: rowSpan, col.@value)
                                                }
                                            }
                                        }
                                        colCounter++
                                    }
                                }
                                rowCounter++
                            }
                        }

                        // Вывод шапки 2
                        div(class: 'hdrh', data.prefix2)
                        table(class: 'rt') {
                            def skipRowAliases = [:]
                            def rowCounter = 0
                            data.root2.each { row ->
                                tr {
                                    def skipCol = 0
                                    def colCounter = 0
                                    row.each { col ->
                                        if (skipCol != 0) {
                                            skipCol--
                                        } else {
                                            if (skipRowAliases[col.@alias] != null && skipRowAliases[col.@alias] != 0) {
                                                skipRowAliases[col.@alias] = skipRowAliases[col.@alias] - 1
                                            } else {
                                                def colSpan = col.@colSpan
                                                def rowSpan = col.@rowSpan

                                                if (colSpan != '1') {
                                                    skipCol = colSpan.toInteger() - 1
                                                }

                                                if (rowSpan != '1') {
                                                    skipRowAliases.put(col.@alias, rowSpan.toInteger() - 1)
                                                }

                                                if (!headerCompare.contains("$rowCounter $colCounter")) {
                                                    td(colspan: colSpan, rowspan: rowSpan, col.@value)
                                                } else {
                                                    td(class: 'td_error', colspan: colSpan, rowspan: rowSpan, col.@value)
                                                }
                                            }
                                        }
                                        colCounter++
                                    }
                                }
                                rowCounter++
                            }
                        }
                    }
                }
            }
        }
        writer.close()
        println("See ${Main.REPORT_DB_NAME} for details")
    }
}
