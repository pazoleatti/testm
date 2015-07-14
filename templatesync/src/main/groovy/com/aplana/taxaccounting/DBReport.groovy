package com.aplana.taxaccounting

import groovy.sql.Sql
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.apache.commons.io.IOUtils
import org.xml.sax.SAXException;

/**
 * Отчет сравнения Git и БД
 */
class DBReport {
    // Сравнение шаблонов в БД
    def static void compareDBFormTemplate(def prefix1, def prefix2) {
        // Запросы на получение макетов
        def sqlTemplate1 = "SELECT ft1.id " +
                " ,ft1.type_id " +
                " ,ft1.data_rows " +
                " ,ft1.fixed_rows " +
                " ,ft1.NAME " +
                " ,ft1.fullname " +
                " ,ft1.header AS code " +
                " ,ft1.data_headers " +
                " ,to_char(ft1.version, 'RRRR') AS version " +
                " ,(select to_char(MIN(ft2.version) - INTERVAL '1' day, 'RRRR') from form_template ft2 where ft1.type_id = ft2.type_id AND TRUNC(ft2.version, 'DD') > ft1.version AND ft2.STATUS IN (0,1,2) group by ft2.type_id) AS versionEnd " +
                " ,ft1.STATUS " +
                " ,ft1.script " +
                " ,ft1.monthly " +
                "FROM form_template ft1 " +
                "WHERE  " +
                " ft1.STATUS NOT IN (-1,2)"
        def sqlTemplate2 = sqlTemplate1

        // Запросы на получение колонок
        def sqlColumns1 = "select id, name, form_template_id, ord, alias, type, width, precision, max_length, checking, attribute_id, format, filter, parent_column_id, (select alias from form_column fc2 where fc2.id = fc1.parent_column_id) as parent_alias, attribute_id2, numeration_row from form_column fc1 where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by ord"
        def sqlColumns2 = sqlColumns1

        // Запросы на получение стилей
        def sqlStyles1 = "select alias, form_template_id, font_color, back_color, italic, bold from form_style where form_template_id in (select distinct id from form_template where status not in (-1, 2)) order by alias"
        def sqlStyles2 = sqlStyles1

        // Перечень всех версий в БД (заполняется в getTemplates)
        def allVersions = [:]

        // Макеты
        def templates1 = getTemplates(prefix1, sqlTemplate1, sqlColumns1, sqlStyles1, allVersions)
        def templates2 = getTemplates(prefix2, sqlTemplate2, sqlColumns2, sqlStyles2, allVersions)

        // Построение отчета
        def report = new File(Main.REPORT_DB_NAME)
        if (report.exists()) {
            report.delete()
        }

        // Данные для отображения отличий в графах НФ
        def columnTableData = [:]

        // Данные для отображения отличий в шапках НФ
        def headerTableData = [:]

        // Данные для отображения отличий в стилях НФ
        def styleTableData = [:]

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
                            td(colspan: 17, class: 'hdr', Main.TAX_FOLDERS[taxName])
                        }
                        tr {
                            th(rowspan: 2, 'type_id')
                            th(rowspan: 2, 'Название')
                            th(rowspan: 2, 'Версия')
                            th(rowspan: 2, "$prefix1 id")
                            th(rowspan: 2, "$prefix2 id")
                            th(colspan: 12, 'Результат сравнения')
                        }
                        tr {
                            th 'name'
                            th 'fullname'
                            th 'endversion'
                            th 'header'
                            th 'fixedrows'
                            th 'datarows'
                            th 'dataheaders'
                            th 'status'
                            th 'script'
                            th 'columns'
                            th 'monthly'
                            th 'styles'
                        }

                        // Сортировка
                        def sorted = Main.TEMPLATE_NAME_TO_TYPE_ID[taxName].sort(){a, b -> a.value <=> b.value}

                        // Сравнение
                        sorted.each { folderName, type_id ->
                            if (type_id > 0) {
                                allVersions[type_id].each { version ->
                                    // Макеты
                                    def tmp1 = templates1.templateMap[type_id]?.get(version)
                                    def tmp2 = templates2.templateMap[type_id]?.get(version)

                                    // Графы
                                    def columnsSet1 = templates1.columnsMap[tmp1?.id]
                                    def columnsSet2 = templates2.columnsMap[tmp2?.id]

                                    // Стили
                                    def stylesSet1 = templates1.stylesMap[tmp1?.id]
                                    def stylesSet2 = templates2.stylesMap[tmp2?.id]

                                    // Имя из первого макета, а при его отсутствии — из второго
                                    def name = tmp1?.name
                                    if (name == null) {
                                        name = tmp2?.name
                                    }

                                    XMLUnit.setIgnoreWhitespace(true)
                                    XMLUnit.setIgnoreComments(true)
                                    XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
                                    XMLUnit.setNormalizeWhitespace(true)

                                    boolean dataRowsEqual = true
                                    Diff diff
                                    if (tmp1?.data_rows != null && tmp2?.data_rows != null) {
                                        diff = XMLUnit.compareXML(tmp1.data_rows, tmp2.data_rows)
                                        dataRowsEqual = diff.similar()
                                    } else if (tmp1?.data_rows != null || tmp2?.data_rows != null) {
                                        dataRowsEqual = false
                                    }

                                    // Признак сравнения
                                    def nameC = tmp1?.name == tmp2?.name ? '+' : '—'
                                    def fullnameC = tmp1?.fullname == tmp2?.fullname ? '+' : '—'
                                    def versionEndC = tmp1?.versionEnd == tmp2?.versionEnd ? '+' : '—'
                                    def codeC = tmp1?.code == tmp2?.code ? '+' : '—'
                                    def fixedrowsC = tmp1?.fixed_rows == tmp2?.fixed_rows ? '+' : '—'
                                    def datarowsC = dataRowsEqual ? '+' : '—'
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
                                            data.versionEnd = version
                                            data.prefix1 = prefix1
                                            data.prefix2 = prefix2
                                            columnTableData.put("_${type_id}_${version}", data)
                                        }
                                    }

                                    // Сравнение стилей
                                    def styleDiff = null
                                    def styleHeaders = ['alias', 'font_color', 'back_color', 'italic', 'bold']
                                    if (stylesSet1 != null && stylesSet2 == null || stylesSet1 == null && stylesSet2 != null) {
                                        styleDiff = "Нет в ${stylesSet1 == null ? prefix1 : prefix2}"
                                    } else if (stylesSet1 != null && stylesSet2 != null) {
                                        def changesMap = [:]

                                        for (def i = 0; i < Math.max(stylesSet1.size(), stylesSet2.size()); i++) {
                                            def style1 = stylesSet1.size() > i ? stylesSet1.getAt(i) : null
                                            def style2 = stylesSet2.size() > i ? stylesSet2.getAt(i) : null

                                            styleHeaders.each { styleHeader ->
                                                if (style1 == null || style2 == null || style1[styleHeader] != style2[styleHeader]) {
                                                    if (!changesMap.containsKey(i)) {
                                                        changesMap.put(i, [])
                                                    }
                                                    changesMap[i].add(styleHeader)
                                                }
                                            }
                                        }

                                        if (!changesMap.isEmpty()) {
                                            styleDiff = "Подробнее..."
                                            def data = new Expando()
                                            data.tmp1 = tmp1
                                            data.tmp2 = tmp2
                                            data.name = name
                                            data.changesMap = changesMap
                                            data.headers = styleHeaders
                                            data.stylesSet1 = stylesSet1
                                            data.stylesSet2 = stylesSet2
                                            data.type_id = type_id
                                            data.version = version
                                            data.prefix1 = prefix1
                                            data.prefix2 = prefix2
                                            styleTableData.put("s_${type_id}_${version}", data)
                                        }
                                    }

                                    def columnsC = colDiff == null ? '+' : '—'
                                    def stylesC = styleDiff == null ? '+' : '—'
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

                                        if (versionEndC == '+') {
                                            td(class: 'td_ok', versionEndC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.versionEnd}, $prefix2 = ${tmp2?.versionEnd}", versionEndC)
                                        }

                                        if (codeC == '+') {
                                            td(class: 'td_ok', codeC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.code}, $prefix2 = ${tmp2?.code}", codeC)
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

                                        if (stylesC == '+') {
                                            td(class: 'td_ok', stylesC)
                                        } else {
                                            td(class: 'td_error cln', title: styleDiff, 'data-tbl': "#s_${type_id}_${version}", stylesC)
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
                // Скрытый блок для отображения отличий в стилях НФ
                styleTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение стилей шаблона вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        table(class: 'rt') {
                            // Вывод таблицы с колонками 1
                            printStylesTable(builder, data.changesMap, data.headers, data.prefix1, data.stylesSet1)
                            // Вывод таблицы с колонками 2
                            printStylesTable(builder, data.changesMap, data.headers, data.prefix2, data.stylesSet2)
                        }
                    }
                }
            }
        }
        writer.close()
        println("See ${Main.REPORT_DB_NAME} for details")
    }

    // Получение макетов
    def private static getTemplates(def prefix, def sqlTemplate, def sqlColumns, def sqlStyles, def allVersions) {
        println("DBMS connect: $prefix")
        def retVal = new Expando()

        def sql = Sql.newInstance(Main.DB_URL, prefix, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def templateMap = [:]
        def columnsMap = [:]
        def stylesMap = [:]

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
            version.versionEnd = it.versionEnd
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

        // Стили
        sql.eachRow(sqlStyles) {
            def form_template_id = it.form_template_id as Integer
            if (!stylesMap.containsKey(form_template_id)) {
                stylesMap.put(form_template_id, [] as Set)
            }
            def style = new Expando()
            style.alias = it.alias
            style.font_color = StyleColor.getById(it.font_color as Integer)
            style.back_color = StyleColor.getById(it.back_color as Integer)
            style.italic = it.italic == 1
            style.bold = it.bold == 1
            stylesMap[form_template_id].add(style)
        }

        sql.close()
        println("Load DB form_template from $prefix OK")
        retVal.templateMap = templateMap
        retVal.columnsMap = columnsMap
        retVal.stylesMap = stylesMap
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

    // Вывод шапки для styles
    def private static printStylesTable(def builder, def changesMap, def headers, def prefix, def stylesSet) {
        // Название таблицы
        builder.tr {
            td(colspan: 4, class: 'hdr', prefix)
        }
        builder.tr {
            headers.each { header ->
                th header
            }
        }
        // Содержимое таблицы
        stylesSet.eachWithIndex { style, i ->
            builder.tr {
                headers.each { header ->
                    td((changesMap[i]?.contains(header) ? [class: 'td_error'] : [:]), style[header])
                }
            }
        }
    }

    // Сравнение шаблонов деклараций в БД
    def static void compareDBDeclarationTemplate(def prefix1, def prefix2) {
        // Запросы на получение макетов
        def sqlTemplate1 = "select dt1.id," +
                " dt1.declaration_type_id as type_id," +
                " dt1.name," +
                " to_char(dt1.version, 'RRRR') AS version," +
                " (select to_char(MIN(dt2.version) - INTERVAL '1' day, 'RRRR') from declaration_template dt2 where dt1.declaration_type_id = dt2.declaration_type_id AND TRUNC(dt2.version, 'DD') > dt1.version AND dt2.STATUS IN (0,1,2) group by dt2.declaration_type_id) AS versionEnd," +
                " dt1.status," +
                " dt1.create_script as script, " +
                " (select data from blob_data where id = dt1.xsd) As xsd, " +
                " (select data from blob_data where id = dt1.jrxml) As jrxml " +
                "from declaration_template dt1 " +
                "where" +
                " dt1.status not in (-1,2)"
        def sqlTemplate2 = sqlTemplate1

        // Перечень всех версий в БД (заполняется в getDeclarationTemplates)
        def allVersions = [:]

        // Макеты
        def templates1 = getDeclarationTemplates(prefix1, sqlTemplate1, allVersions)
        def templates2 = getDeclarationTemplates(prefix2, sqlTemplate2, allVersions)

        // Построение отчета
        def report = new File(Main.REPORT_DECL_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        def writer = new FileWriter(new File(Main.REPORT_DECL_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение макетов деклараций в $prefix1 и $prefix2"
                style(type: "text/css", Main.HTML_STYLE)
                script('', type: 'text/javascript', src: 'http://code.jquery.com/jquery-1.9.1.min.js')
                script('', type: 'text/javascript', src: 'http://code.jquery.com/ui/1.10.3/jquery-ui.min.js')
                link('', rel: 'stylesheet', href: 'http://code.jquery.com/ui/1.10.3/themes/black-tie/jquery-ui.css')
            }
            body {
                p "Сравнение макетов деклараций в БД $prefix1 и $prefix2:"
                table(class: 'rt') {
                    Main.TAX_FOLDERS.keySet().each { taxName ->
                        tr {
                            td(colspan: 11, class: 'hdr', Main.TAX_FOLDERS[taxName])
                        }
                        tr {
                            th(rowspan: 2, 'type_id')
                            th(rowspan: 2, 'Название')
                            th(rowspan: 2, 'Версия')
                            th(rowspan: 2, "$prefix1 id")
                            th(rowspan: 2, "$prefix2 id")
                            th(colspan: 6, 'Результат сравнения')
                        }
                        tr {
                            th 'name'
                            th 'endversion'
                            th 'status'
                            th 'script'
                            th 'xsd'
                            th 'jrxml'
                        }

                        // Сортировка
                        def sorted = Main.TEMPLATE_NAME_TO_TYPE_ID[taxName].sort(){a, b -> -(a.value) <=> -(b.value)}

                        // Сравнение
                        sorted.each { folderName, temp_type_id ->
                            if (temp_type_id < 0) {
                                def type_id = -temp_type_id
                                allVersions[type_id].each{ version ->
                                    // Макеты
                                    def tmp1 = templates1.templateMap[type_id]?.get(version)
                                    def tmp2 = templates2.templateMap[type_id]?.get(version)

                                    // Имя из первого макета, а при его отсутствии — из второго
                                    def name = tmp1?.name
                                    if (name == null) {
                                        name = tmp2?.name
                                    }

                                    XMLUnit.setIgnoreWhitespace(true)
                                    XMLUnit.setIgnoreComments(true)
                                    XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
                                    XMLUnit.setNormalizeWhitespace(true)

                                    boolean xsdEquals = true
                                    Diff diff
                                    if (tmp1?.xsd != null && tmp2?.xsd != null) {
                                        diff = XMLUnit.compareXML(tmp1.xsd, tmp2.xsd)
                                        xsdEquals = diff.similar()
                                    } else if (tmp1?.xsd != null || tmp2?.xsd != null) {
                                        xsdEquals = false
                                    }
                                    boolean jrxmlEquals = true
                                    if (tmp1?.jrxml != null && tmp2?.jrxml != null) {
                                        diff = XMLUnit.compareXML(tmp1.jrxml, tmp2.jrxml)
                                        jrxmlEquals = diff.similar()
                                    } else if (tmp1?.jrxml != null || tmp2?.jrxml != null) {
                                        jrxmlEquals = false
                                    }

                                    // Признак сравнения
                                    def nameC = tmp1?.name == tmp2?.name ? '+' : '—'
                                    def versionEndC = tmp1?.versionEnd == tmp2?.versionEnd ? '+' : '—'
                                    def statusC = tmp1?.status == tmp2?.status ? '+' : '—'
                                    def scriptC = tmp1?.script == tmp2?.script ? '+' : '—'
                                    def xsdC = xsdEquals ? '+' : '—'
                                    def jrxmlC = jrxmlEquals ? '+' : '—'

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

                                        if (versionEndC == '+') {
                                            td(class: 'td_ok', versionEndC)
                                        } else {
                                            td(class: 'td_error', title: "$prefix1 = ${tmp1?.versionEnd}, $prefix2 = ${tmp2?.versionEnd}", versionEndC)
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

                                        if (xsdC == '+') {
                                            td(class: 'td_ok', xsdC)
                                        } else {
                                            td(class: 'td_error', title: 'См. БД', xsdC)
                                        }
                                        if (jrxmlC == '+') {
                                            td(class: 'td_ok', jrxmlC)
                                        } else {
                                            td(class: 'td_error', title: 'См. БД', jrxmlC)
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
        println("See ${Main.REPORT_DECL_DB_NAME} for details")
    }

    // Сравнение скриптов справочников в БД
    def static void compareDBRefbookScript(def prefix1, def prefix2) {
        // Запросы на получение справочников
        def sqlTemplate1 = "select rb.id, rb.name, (select data from blob_data where id = rb.script_id) as script from ref_book rb where rb.visible = 1"
        def sqlTemplate2 = sqlTemplate1

        def refbooks1 = getRefbooks(prefix1, sqlTemplate1)
        def refbooks2 = getRefbooks(prefix2, sqlTemplate2)

        // Построение отчета
        def report = new File(Main.REPORT_REFBOOK_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        def writer = new FileWriter(new File(Main.REPORT_REFBOOK_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение скриптов справочников в $prefix1 и $prefix2"
                style(type: "text/css", Main.HTML_STYLE)
                script('', type: 'text/javascript', src: 'http://code.jquery.com/jquery-1.9.1.min.js')
                script('', type: 'text/javascript', src: 'http://code.jquery.com/ui/1.10.3/jquery-ui.min.js')
                link('', rel: 'stylesheet', href: 'http://code.jquery.com/ui/1.10.3/themes/black-tie/jquery-ui.css')
            }
            body {
                p "Сравнение скриптов справочников в БД $prefix1 и $prefix2:"
                table(class: 'rt') {
                    tr {
                        th 'id'
                        th 'Название'
                        th 'Результат сравнения'
                    }
                    tr {
                        th 'id'
                        th 'name'
                        th 'script'
                    }

                    // Сортировка
                    def sorted = Main.REFBOOK_FOLDER_NAME_TO_ID.sort(){a, b -> a.value <=> b.value}

                    // Сравнение
                    sorted.each { folderName, id ->
                        // Макеты
                        def tmp1 = refbooks1.find { it.id == id }
                        def tmp2 = refbooks2.find { it.id == id }

                        def name = tmp1?.name
                        if (name == null) {
                            name = tmp2?.name
                        }

                        // Признак сравнения
                        def scriptC = tmp1?.script == tmp2?.script ? '+' : '—'

                        tr(class: ((tmp1?.id != null && tmp2?.id != null) ? 'nr' : 'er')) {
                            td id
                            td name

                            if (scriptC == '+') {
                                td(class: 'td_ok', scriptC)
                            } else {
                                td(class: 'td_error', title: 'См. БД', scriptC)
                            }
                        }
                    }
                }
            }
        }
        writer.close()
        println("See ${Main.REPORT_REFBOOK_DB_NAME} for details")
    }

    // Сравнение типов НФ/деклараций
    def static void compareDBTypes(def prefix1, def prefix2) {
        // Запросы на получение справочников
        def sqlTemplate1 = "select id, tax_type, name, status, code, is_ifrs, ifrs_name, 0 as flag from form_type where status not in (-1, 2) " +
                "union all " +
                "(select id, tax_type, name, status, null as code, is_ifrs, ifrs_name, 1 as flag from declaration_type where status not in (-1, 2)) " +
                "order by tax_type"
        def sqlTemplate2 = sqlTemplate1

        def types1 = getFormDeclarationTypes(prefix1, sqlTemplate1)
        def types2 = getFormDeclarationTypes(prefix2, sqlTemplate2)

        // Построение отчета
        def report = new File(Main.REPORT_TYPE_DB_NAME)
        if (report.exists()) {
            report.delete()
        }
        def writer = new FileWriter(new File(Main.REPORT_TYPE_DB_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение типов форм/деклараций в $prefix1 и $prefix2"
                style(type: "text/css", Main.HTML_STYLE)
                script('', type: 'text/javascript', src: 'http://code.jquery.com/jquery-1.9.1.min.js')
                script('', type: 'text/javascript', src: 'http://code.jquery.com/ui/1.10.3/jquery-ui.min.js')
                link('', rel: 'stylesheet', href: 'http://code.jquery.com/ui/1.10.3/themes/black-tie/jquery-ui.css')
            }
            body {
                p "Сравнение типов форм/деклараций в БД $prefix1 и $prefix2:"
                table(class: 'rt') {
                    tr {
                        th 'id'
                        th 'Наименование'
                        th 'tax_type'
                        th 'name'
                        th 'status'
                        th 'code'
                        th 'is_ifrs'
                        th 'ifrs_name'
                    }

                    [0, 1].each{ typeType ->
                        tr {
                            td(colspan: 8, class: 'hdr', typeType ? "Декларации" : "Налоговые формы")
                        }
                        def types = (types1[typeType] + types2[typeType]).collect { it.id }.unique(true)

                        // Сравнение
                        types.each { id ->
                            def tmp1 = types1[typeType].find { it.id == id }
                            def tmp2 = types2[typeType].find { it.id == id }

                            def name = tmp1?.name
                            if (name == null) {
                                name = tmp2?.name
                            }
                            def taxType = tmp1?.tax_type

                            // Признак сравнения
                            def nameC = tmp1?.name == tmp2?.name ? '+' : '—'
                            def statusC = tmp1?.status == tmp2?.status ? '+' : '—'
                            def codeC = tmp1?.code == tmp2?.code ? '+' : '—'
                            def isIfrsC = tmp1?.is_ifrs == tmp2?.is_ifrs ? '+' : '—'
                            def ifrsNameC = tmp1?.ifrs_name == tmp2?.ifrs_name ? '+' : '—'

                            tr(class: ((tmp1?.id != null && tmp2?.id != null) ? 'nr' : 'er')) {
                                td id
                                td name
                                td taxType

                                if (nameC == '+') {
                                    td(class: 'td_ok', nameC)
                                } else {
                                    td(class: 'td_error', title: "$prefix1: ${tmp1?.name}; $prefix2: ${tmp2?.name}", nameC)
                                }
                                if (statusC == '+') {
                                    td(class: 'td_ok', statusC)
                                } else {
                                    td(class: 'td_error', title: "$prefix1: ${tmp1?.status}; $prefix2: ${tmp2?.status}", statusC)
                                }
                                if (codeC == '+') {
                                    td(class: 'td_ok', codeC)
                                } else {
                                    td(class: 'td_error', title: "$prefix1: ${tmp1?.code}; $prefix2: ${tmp2?.code}", codeC)
                                }
                                if (isIfrsC == '+') {
                                    td(class: 'td_ok', isIfrsC)
                                } else {
                                    td(class: 'td_error', title: "$prefix1: ${tmp1?.isIfrs}; $prefix2: ${tmp2?.isIfrs}", isIfrsC)
                                }
                                if (ifrsNameC == '+') {
                                    td(class: 'td_ok', ifrsNameC)
                                } else {
                                    td(class: 'td_error', title: "$prefix1: ${tmp1?.ifrs_name}; $prefix2: ${tmp2?.ifrs_name}", ifrsNameC)
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.close()
        println("See ${Main.REPORT_REFBOOK_DB_NAME} for details")
    }

    def private static getDeclarationTemplates(def prefix, def sqlTemplate, def allVersions) {
        println("DBMS connect: $prefix")
        def retVal = new Expando()

        def templateMap = [:]

        def sql = Sql.newInstance(Main.DB_URL, prefix, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        try {
            sql.eachRow(sqlTemplate) {
                def type_id = it.type_id as Integer
                if (templateMap[type_id] == null) {
                    templateMap.put((Integer) it.type_id, [:])
                }
                // Версия макета
                def version = new Expando()
                version.id = it.id as Integer
                version.type_id = it.type_id as Integer
                version.name = it.name
                version.version = it.version
                version.versionEnd = it.versionEnd
                version.status = it.status
                version.script = it.script?.characterStream?.text?.trim()?.replaceAll("\r", "")
                if (it.xsd) {
                    try {
                        version.xsd = XMLUnit.buildControlDocument(IOUtils.toString(it.xsd.binaryStream))
                    } catch (SAXException e) {
                        println("Ошибка при разборе XSD декларации id = ${it.id} \"${version.name}\"")
                    }
                }
                if (it.jrxml) {
                    try {
                        version.jrxml = XMLUnit.buildControlDocument(IOUtils.toString(it.jrxml.binaryStream, "UTF-8"))
                    } catch (SAXException e) {
                        println("Ошибка при разборе JRXML декларации id = ${it.id} \"${version.name}\"")
                    }
                }
                templateMap[type_id].put(it.version, version)
                if (!allVersions.containsKey(type_id)) {
                    allVersions.put(type_id, [] as Set)
                }
                allVersions[type_id].add(version.version)
            }
        } finally {
            sql.close()
        }
        println("Load DB declaration_template from $prefix OK")
        retVal.templateMap = templateMap
        return retVal

    }

    def private static getFormDeclarationTypes(def prefix, def sqlTemplate) {
        println("DBMS connect: $prefix")
        def formTypes = []
        def declarationTypes = []

        def sql = Sql.newInstance(Main.DB_URL, prefix, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        try {
            sql.eachRow(sqlTemplate) {
                // Версия макета
                def type = new Expando()
                type.key = it.flag + it.id
                type.id = it.id as Integer
                type.tax_type = it.tax_type
                type.name = it.name
                type.status = it.status
                type.code = it.code // для деклараций = null
                type.is_ifrs = it.is_ifrs
                type.ifrs_name = it.ifrs_name
                type.flag = it.flag // для форм = 0, для деклараций = 1
                (it.flag == 0) ? formTypes.add(type) : declarationTypes.add(type)
            }
        } finally {
            sql.close()
        }
        println("Load DB form/declaration type from $prefix OK")
        formTypes.sort{ it.id }.sort { it.tax_type }
        declarationTypes.sort{ it.id }.sort { it.tax_type }
        return [formTypes, declarationTypes]
    }

    def private static getRefbooks(def prefix, def sqlTemplate) {
        println("DBMS connect: $prefix")
        def refbooks = []

        def sql = Sql.newInstance(Main.DB_URL, prefix, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        try {
            sql.eachRow(sqlTemplate) {
                def refbook = new Expando()
                refbook.id = it.id as Integer
                refbook.name = it.name
                if (it.script) {
                    refbook.script = IOUtils.toString(it.script.binaryStream, "UTF-8")?.trim()?.replaceAll("\r", "")
                }
                refbooks.add(refbook)
            }
        } finally {
            sql.close()
        }
        println("Load DB ref_book from $prefix OK")
        return refbooks

    }
}
