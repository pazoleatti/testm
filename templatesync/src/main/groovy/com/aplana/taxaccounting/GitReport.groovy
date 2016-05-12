package com.aplana.taxaccounting

import com.aplana.sbrf.taxaccounting.model.FormStyle
import org.apache.commons.io.IOUtils
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.w3c.dom.Document
import org.xml.sax.SAXException
import com.aplana.sbrf.taxaccounting.model.Color

/**
 * Отчет сравнения Git и БД
 */
class GitReport {
    // Загрузка из git в БД и отчет в html-файле
    def static void updateScripts(def versionsMap, def sql, def checkOnly = true) {
        def isBefore1_0 = !DBReport.checkExistShortName(Main.DB_USER)
        def writer = new FileWriter(new File(Main.REPORT_GIT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        // Данные для отображения отличий в графах НФ
        def columnTableData = [:]
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение макетов git и БД ${Main.DB_USER}"
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
                p "Сравнение макетов в БД ${Main.DB_USER} и git:"
                table(class: 'rt') {
                    Main.TAX_FOLDERS.keySet().each { folderName ->
                        def scanResult = scanSrcFolderAndUpdateDb(versionsMap, folderName, checkOnly ? null : sql, columnTableData, isBefore1_0)
                        if (!scanResult.isEmpty()) {
                            tr {
                                td(colspan: 16, class: 'hdr', Main.TAX_FOLDERS[folderName])
                            }
                            tr {
                                th 'type_id'
                                th 'fixed_rows'
                                th 'monthly'
                                th 'comparative'
                                th 'accruing'
                                th 'updating'
                                th 'Папка'
                                th 'Название'
                                th 'Версия git'
                                th 'Версия БД'
                                th 'Верхний колонтитул'
                                th 'Скрипты'
                                th 'Заголовки данных'
                                th 'Начальные данные'
                                th 'Стили'
                                th 'Графы'
                            }
                            scanResult.each { result ->
                                tr {
                                    td result.id
                                    td(class: (result.errorFixedRows ? 'td_error' : 'td_ok'), result.checkFixedRows)
                                    td(class: (result.errorMonthly ? 'td_error' : 'td_ok'), result.checkMonthly)
                                    td(class: (result.errorComparative ? 'td_error' : 'td_ok'), result.checkComparative)
                                    td(class: (result.errorAccruing ? 'td_error' : 'td_ok'), result.checkAccruing)
                                    td(class: (result.errorUpdating ? 'td_error' : 'td_ok'), result.checkUpdating)
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
                                    td(class: (result.errorCode ? 'td_error' : 'td_ok'), result.checkCode)
                                    td(class: (result.error ? 'td_error' : 'td_ok'), result.check)
                                    td(class: (result.errorHeader ? 'td_error' : 'td_ok'), result.checkHeader)
                                    td(class: (result.errorRows ? 'td_error' : 'td_ok'), result.checkRows)
                                    td(class: (result.errorStyle ? 'td_error' : 'td_ok'), result.checkStyle)
                                    if (result.errorColumns) {
                                        td(class: 'td_error cln', title: result.titleColumns, 'data-tbl': "#${result.tableColumns}", result.checkColumns)
                                    } else {
                                        td(class: 'td_ok', result.checkColumns)
                                    }
                                }
                            }
                        }
                    }
                }
                // Скрытый блок для отображения отличий в графах НФ
                columnTableData.each() { key, data ->
                    div(class: 'dlg', id: key, title: "Сравнение граф макета вида ${data.type_id} версии ${data.version} «${data.name}»") {
                        table(class: 'rt') {
                            // Вывод таблицы с колонками 1
                            DBReport.printColumnsTable(builder, data.changesMap, data.headers, data.prefix1, data.columnsSet1, isBefore1_0)
                            // Вывод таблицы с колонками 2
                            DBReport.printColumnsTable(builder, data.changesMap, data.headers, data.prefix2, data.columnsSet2, isBefore1_0)
                        }
                    }
                }
            }
        }
        writer.close()
        def action = checkOnly ? 'Check' : 'Update'
        println("$action DB form_template OK")
    }

    // Загрузка скриптов деклараций из git в БД и отчет в html-файле
    def static void updateDeclarationScripts(def versionsMap, def sql, def checkOnly = true) {
        def writer = new FileWriter(new File(Main.REPORT_DECL_GIT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение макетов деклараций git и БД ${Main.DB_USER}"
                style(type: "text/css", Main.HTML_STYLE)
            }
            body {
                p "Сравнение макетов деклараций в БД ${Main.DB_USER} и git:"
                table(class: 'rt') {
                    Main.TAX_FOLDERS.keySet().each { folderName ->
                        def scanResult = scanDeclarationSrcFolderAndUpdateDb(versionsMap, folderName, checkOnly ? null : sql)
                        if (scanResult && !scanResult.isEmpty()) {
                            tr {
                                td(colspan: 8, class: 'hdr', Main.TAX_FOLDERS[folderName])
                            }
                            tr {
                                th 'type_id'
                                th 'Папка'
                                th 'Название'
                                th 'Версия git'
                                th 'Версия БД'
                                th 'Сравнение скриптов'
                                th 'Сравнение jrxml'
                                th 'Сравнение xsd'
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
                                    td(class: (result.jrxmlError ? 'td_error' : 'td_ok'), result.jrxmlCheck)
                                    td(class: (result.xsdError ? 'td_error' : 'td_ok'), result.xsdCheck)
                                }
                            }
                        }
                    }
                }
            }
        }
        builder.close()
        def action = checkOnly ? 'Check' : 'Update'
        println("$action DB declaration_template OK")
    }

    static void checkRefBooks(def refbooks) {
        def writer = new FileWriter(new File(Main.REPORT_REFBOOK_GIT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение скриптов справочников git и БД ${Main.DB_USER}"
                style(type: "text/css", Main.HTML_STYLE)
            }
            body {
                p "Сравнение скриптов справочников в БД ${Main.DB_USER} и git:"
                table(class: 'rt') {
                    def scanResult = scanRefbookFolder(refbooks)
                    scanResult.sort { it.name }
                    if (scanResult && !scanResult.isEmpty()) {
                        tr {
                            th 'id'
                            th 'Папка'
                            th 'Название'
                            th 'Сравнение скриптов'
                        }
                        scanResult.each { result ->
                            tr {
                                td result.id
                                td {
                                    a(href: result.folderFull, result.folder)
                                }
                                td result.name
                                td(class: (result.error ? 'td_error' : 'td_ok'), result.check)
                            }
                        }
                    }
                }
            }
        }
    }

    // Сравнение git-версии с версией в БД и загрузка в случае отличий
    def static scanSrcFolderAndUpdateDb(def versionsMap, def folderName, def sql, def columnTableData, def isBefore1_0) {
        def scanResult = []

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
        XMLUnit.setNormalizeWhitespace(true)

        def folderFile = new File("${Main.SRC_FOLDER_PATH}/$folderName")
        // По видам НФ
        folderFile.eachDir { templateFolder ->
            // Id типа формы
            def Integer id = Main.TEMPLATE_NAME_TO_TYPE_ID[folderName][templateFolder.name]

            if (id != null) { // Id типа определен
                if (id > 0) { // Id типа формы, которую нужно пропустить
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

                                def codeDB = versions[version]?.code ?: ""
                                def fixed_rowsDB = versions[version]?.fixed_rows
                                def monthlyDB = versions[version]?.monthly
                                def comparativeDB = versions[version]?.comparative
                                def accruingDB = versions[version]?.accruing
                                def updatingDB = versions[version]?.updating

                                // Сравнение скриптов
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
                                            result.check = "Скрипты отличаются"
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

                                // Сравнение заголовка
                                def headerFile = new File("$versionFolder/headers.xml")
                                if (!headerFile.exists()) {
                                    result.checkHeader = "Файл заголовка данных не найден в «${scriptFile.absolutePath}»"
                                    result.errorHeader = true
                                } else {
                                    def dbHeader = versions[version]?.data_headers
                                    def gitHeader = XMLUnit.buildControlDocument(headerFile?.text)
                                    if (dbHeader != null && gitHeader != null) {
                                        Diff diff = XMLUnit.compareXML(dbHeader, gitHeader)
                                        def headerEqual = diff.similar()
                                        if (headerEqual) {
                                            result.checkHeader = "Ok"
                                        } else {
                                            result.errorHeader = true
                                            result.checkHeader = "Заголовки данных отличаются"
                                        }
                                    } else if (dbHeader != null || gitHeader != null) {
                                        result.errorHeader = true
                                        result.checkHeader = "Заголовок данных не обнаружен в БД"
                                    }
                                }

                                def contentFile = new File("$versionFolder/content.xml")
                                def xml = (contentFile.exists() ? new XmlSlurper().parseText(contentFile.getText()) : null)

                                // fixed_rows, monthly, comparative, accruing, updating
                                if (xml) {
                                    def codeXml = xml.header?.text() ?: xml.code?.text()
                                    def fixed_rowsXml = convertTextToBooleanInteger(xml.fixedRows?.text())
                                    def monthlyXml = convertTextToBooleanInteger(xml.monthly?.text())
                                    def comparativeXml = convertTextToBooleanInteger(xml.comparative?.text())
                                    def accruingXml = convertTextToBooleanInteger(xml.accruing?.text())
                                    def updatingXml = convertTextToBooleanInteger(xml.updating?.text())
                                    if (codeDB != codeXml) {
                                        result.checkCode = "Значение в БД \"$codeDB\" отличается от значения в GIT \"$codeXml\""
                                        result.errorCode = true
                                    } else {
                                        result.checkCode = "Ok"
                                        result.errorCode = false
                                    }
                                    compareDbXml(result, fixed_rowsDB, fixed_rowsXml, "checkFixedRows", "errorFixedRows")
                                    compareDbXml(result, monthlyDB, monthlyXml, "checkMonthly", "errorMonthly")
                                    compareDbXml(result, comparativeDB, comparativeXml, "checkComparative", "errorComparative")
                                    compareDbXml(result, accruingDB, accruingXml, "checkAccruing", "errorAccruing")
                                    compareDbXml(result, updatingDB, updatingXml, "checkUpdating", "errorUpdating")
                                }

                                // Сравнение стилей
                                def Map mapStylesXml = [:]
                                def stylesDb = versions[version]?.styles
                                if (!contentFile.exists()) {
                                    result.errorStyle = true
                                } else {
                                    // Собираем все стили из xml
                                    for (def styleXml : xml.styles) {
                                        FormStyle style = new FormStyle()
                                        style.alias = styleXml.alias.text()
                                        style.backColor = Color[styleXml.backColor.text()]
                                        style.fontColor = Color[styleXml.fontColor.text()]
                                        style.bold = Boolean.parseBoolean(styleXml.bold.text())
                                        style.italic = Boolean.parseBoolean(styleXml.italic.text())
                                        mapStylesXml.put(styleXml.alias.text(), style)
                                    }
                                    def errorMap = [:]
                                    // Находим стили
                                    def absentStyles = stylesDb.findAll { alias, styleDb -> mapStylesXml[alias] == null }
                                    // Проходим по стилям из базы и удаляем повторяющиеся из найденных в xml
                                    stylesDb.each { alias, styleDb ->
                                        def styleXml = mapStylesXml.remove(alias)
                                        if (styleXml != null) {
                                            def errorList = []
                                            ['backColor', 'fontColor', 'bold', 'italic'].each {
                                                if (styleXml[it] != styleDb[it]) {
                                                    errorList.add(it)
                                                }
                                            }
                                            if (!errorList.isEmpty()) {
                                                errorMap.put(alias, errorList)
                                            }
                                        }
                                    }
                                    if (!absentStyles.isEmpty()){
                                        result.checkStyle = "В DB обнаружены стили '${absentStyles.keySet().join('\', \'')}' отсутствующие в Git. "
                                        result.errorStyle = true
                                    }
                                    if (!mapStylesXml.isEmpty()) {
                                        result.checkStyle = (result.checkStyle?:"") + "В Git обнаружены стили '${mapStylesXml.keySet().join('\', \'')}' отсутствующие в DB. "
                                        result.errorStyle = true
                                    }
                                    if (!errorMap.isEmpty()){
                                        //TODO вывести различия?
                                        result.checkStyle = (result.checkStyle?:"") + "В Git и DB отличаются стили '${errorMap.keySet().join('\', \'')}'"
                                        result.errorStyle = true
                                    }
                                    if (absentStyles.isEmpty() && mapStylesXml.isEmpty() && errorMap.isEmpty()){
                                        result.checkStyle = "Ok"
                                        result.errorStyle = false
                                    }
                                }

                                // Сравнение фикс.строк
                                def rowsFile = new File("$versionFolder/rows.xml")
                                if (!rowsFile.exists()) {
                                    result.checkRows = "Файл фикс.строк не найден в «${scriptFile.absolutePath}»"
                                    result.errorRows = true
                                } else {
                                    def dbRows = versions[version]?.data_rows
                                    def gitRows = XMLUnit.buildControlDocument(rowsFile?.text?.replaceAll('stringValue=""', ''))
                                    if (dbRows != null && gitRows != null) {
                                        updateStyleAliases(dbRows, stylesDb)
                                        updateStyleAliases(gitRows, stylesDb + mapStylesXml)
                                        Diff diff = XMLUnit.compareXML(dbRows, gitRows)
                                        def rowsEqual = diff.similar()
                                        if (rowsEqual) {
                                            result.checkRows = "Ok"
                                        } else {
                                            result.errorRows = true
                                            result.checkRows = "Фикс.строки отличаются"
                                        }
                                    } else if (dbRows != null || gitRows != null) {
                                        if (dbRows == null && gitRows != null && rowsFile?.text?.contains('<rows/>')) {
                                            result.checkRows = "Ok"
                                        } else {
                                            result.errorRows = true
                                            result.checkRows = "Фикс.строк нет в БД"
                                        }
                                    }
                                }

                                // Сравнение граф НФ
                                if (!contentFile.exists()) {
                                    result.errorColumns = true
                                } else {
                                    def Map mapColumnsXml = [:]
                                    def columnsDb = versions[version]?.columns
                                    // собрать все графы из xml
                                    for (def columnXml : xml.columns) {
                                        def column = new Expando()
                                        column.name = columnXml.name.text() ?: null
                                        column.ord = convertTextToInteger(columnXml.order.text())
                                        column.alias = columnXml.alias.text()
                                        // stringColumn, numericColumn, dateColumn, autoNumerationColumn, refBookColumn, referenceColumn
                                        def type = columnXml.@"xsi:type".text()
                                        column.type = (type ? type[0].toUpperCase() : null)
                                        column.width = convertTextToInteger(columnXml.width.text()) ?: 0
                                        column.precision = convertTextToInteger(columnXml.precision.text())
                                        column.max_length = convertTextToInteger(columnXml.maxLength.text())
                                        column.checking = convertTextToBooleanInteger(columnXml.checking.text()) ?: 0
                                        column.attribute_id = convertTextToInteger(columnXml.refBookAttributeId.text())
                                        column.format = convertTextToInteger(columnXml.formatId.text())
                                        column.filter = columnXml.filter.text() ?: null
                                        // parent_column_id
                                        column.parentAlias = columnXml.parentAlias.text() ?: null
                                        column.attribute_id2 = convertTextToInteger(columnXml.refBookAttributeId2.text())
                                        // Тип нумерации строк для автонумеруемой графы (0 - последовательная, 1 - сквозная), раньше хрнилось в columns.type, теперь в columns.numerationType
                                        def numerationType = columnXml.numerationType.text() == 'SERIAL' ? 0 : (columnXml.numerationType.text() == 'CROSS' ? 1 : null)
                                        if (numerationType == null) {
                                            numerationType = convertTextToInteger(columnXml.type.text())
                                        }
                                        column.numeration_row = numerationType
                                        column.short_name = columnXml.shortName.text() ?: null
                                        mapColumnsXml.put(columnXml.alias.text(), column)
                                    }

                                    def changesMap = [:]
                                    def headers = ['name', 'ord', 'alias', 'type', 'width', 'precision', 'max_length',
                                            'checking', 'attribute_id', 'format', 'filter', /* 'parent_column_id', */ 'parentAlias',
                                            'attribute_id2', 'numeration_row']
                                    if (!isBefore1_0) {
                                        headers.add('short_name')
                                    }
                                    def columnsSet1 = mapColumnsXml.values().collect { it }
                                    def columnsSet2 = columnsDb.values().collect { it }
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
                                    // Находим отсутсвтующие в гите столбцы
                                    def absentColumns = columnsDb.findAll { alias, columnDb -> mapColumnsXml[alias] == null }
                                    // Проходим по стилям из базы и удаляем повторяющиеся из найденных в xml
                                    columnsDb.each { alias, styleDb ->
                                        mapColumnsXml.remove(alias)
                                    }

                                    if (!absentColumns.isEmpty()) {
                                        def tmpColumns = absentColumns.keySet().join("', '")
                                        result.checkColumns = "В DB обнаружены графы '$tmpColumns' отсутствующие в Git. "
                                        result.errorColumns = true
                                    }
                                    if (!mapColumnsXml.isEmpty()) {
                                        def tmpColumns = mapColumnsXml.keySet().join("', '")
                                        result.checkColumns = (result.checkColumns ?: "") + "В Git обнаружены графы '$tmpColumns' отсутствующие в DB. "
                                        result.errorColumns = true
                                    }
                                    if (absentColumns.isEmpty() && mapColumnsXml.isEmpty() && changesMap.isEmpty()) {
                                        result.checkColumns = "Ok"
                                        result.errorColumns = false
                                    }
                                    if (!changesMap.isEmpty()) {
                                        result.titleColumns = "Подробнее…"
                                        result.checkColumns = "-"
                                        result.errorColumns = true
                                        result.tableColumns = "_${id}_${version}"
                                        def data = new Expando()
                                        data.name = result.name
                                        data.changesMap = changesMap
                                        data.headers = headers
                                        data.columnsSet1 = columnsSet1
                                        data.columnsSet2 = columnsSet2
                                        data.type_id = id
                                        data.version = version
                                        data.versionEnd = version
                                        data.prefix1 = 'git'
                                        data.prefix2 = 'БД'
                                        columnTableData.put(result.tableColumns, data)
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
        scanResult.sort(new Comparator() {
            @Override
            int compare(Object o1, Object o2) {
                if (o1.id != o2.id) {
                    return o1.id <=> o2.id
                } else {
                    return o1.version <=> o2.version
                }
            }
        })
        return scanResult
    }

    static Integer convertTextToBooleanInteger(String value) {
        switch (value) {
            case "true" : return 1
            case "false" : return 0
            case null : return null
        }
        return null
    }

    static Integer convertTextToInteger(String value) {
        if (value) {
            return value as Integer
        }
        return null
    }

    static compareDbXml(def result, def dbValue, def xmlValue, def checkAlias, def errorAlias) {
        if (dbValue != xmlValue) {
            result[checkAlias] = "Значение в БД \"$dbValue\" отличается от значения в GIT \"$xmlValue\""
            result[errorAlias] = (dbValue || xmlValue)
        } else {
            result[checkAlias] = "Ok"
            result[errorAlias] = false
        }
    }

    // Сравнение git-версии декларации с версией в БД и загрузка в случае отличий
    def static scanDeclarationSrcFolderAndUpdateDb(def versionsMap, def folderName, def sql) {
        def scanResult = []

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
        XMLUnit.setNormalizeWhitespace(true)

        def folderFile = new File("${Main.SRC_FOLDER_PATH}/$folderName")
        // По видам НФ
        folderFile.eachDir { templateFolder ->
            // Id типа формы
            def Integer temp_id = Main.TEMPLATE_NAME_TO_TYPE_ID[folderName][templateFolder.name]

            if (temp_id != null) { // Id типа определен
                if (temp_id < 0) { // Id типа формы, которую нужно пропустить
                    def Integer id = -temp_id
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
                                result.check = "В БД не найдена версия макета для папки «${templateFolder.absolutePath}» c declaration_type.id = $id"
                                result.versionGit = version
                                result.error = true
                                scanResult.add(result)
                            } else { // Версии совпали
                                result.name = versions[version]?.name
                                result.status = versions[version]?.status
                                result.versionGit = version
                                result.versionDB = versions[version]?.version
                                scanResult.add(result)

                                // Сравнение скриптов
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
                                            result.check = "Скрипты отличаются"
                                        } else {
                                            def error = "Число измененных строк не равно 1."
                                            try {
                                                updateResult = sql.executeUpdate("update declaration_template set create_script = ? where id = ? and declaration_type_id = ?", scriptFile?.text, versions[version].id, id)
                                                println("Update declaration_template id = ${versions[version].id}, declaration_type_id=$id")
                                            } catch (Exception ex) {
                                                error = ex.getLocalizedMessage()
                                                ex.printStackTrace()
                                            }
                                            result.check = updateResult == 1 ? "Скрипт устарел и был обновлен" : "Скрипт устарел. Ошибка обновления: $error"
                                        }
                                    }
                                }
                                // Сравнение JRXML
                                def jrxmlFile = new File("$versionFolder/report.jrxml")
                                if (!jrxmlFile.exists()) {
                                    result.jrxmlCheck = "Макет Jasper не найден в «${jrxmlFile.absolutePath}»"
                                    result.jrxmlError = true
                                } else {
                                    def dbJrxml = versions[version]?.jrxml
                                    def gitJrxml = XMLUnit.buildControlDocument(jrxmlFile?.text)
                                    if (dbJrxml != null && gitJrxml != null) {
                                        Diff diff = XMLUnit.compareXML(dbJrxml, gitJrxml)
                                        def jrxmlEqual = diff.similar()
                                        if (jrxmlEqual) {
                                            result.jrxmlCheck = "Ok"
                                        } else {
                                            result.jrxmlError = true
                                            result.jrxmlCheck = "Макеты Jasper отличаются"
                                        }
                                    } else if (dbJrxml != null || gitJrxml != null) {
                                        result.jrxmlError = true
                                        result.jrxmlCheck = "Макет Jasper не обнаружен в БД"
                                    }
                                }
                                // Сравнение XSD
                                def xsdFiles = new File("$versionFolder").listFiles(new FilenameFilter() {
                                    @Override
                                    boolean accept(File dir, String name) {
                                        return name.toLowerCase().endsWith("xsd")
                                    }
                                })
                                if (xsdFiles.size() == 1) {
                                    def xsdFile = xsdFiles[0]
                                    def dbXsd = versions[version]?.xsd
                                    def gitXsd = XMLUnit.buildControlDocument(xsdFile?.text)
                                    if (dbXsd != null && gitXsd != null) {
                                        Diff diff = XMLUnit.compareXML(dbXsd, gitXsd)
                                        def xsdEqual = diff.similar()
                                        if (xsdEqual) {
                                            result.xsdCheck = "Ok"
                                        } else {
                                            result.xsdError = true
                                            result.xsdCheck = "Файлы XSD отличаются"
                                        }
                                    } else if (dbXsd != null || gitXsd != null) {
                                        result.xsdError = true
                                        result.xsdCheck = "Файл XSD не обнаружен в БД"
                                    }
                                } else if (xsdFiles.size() > 1 ) {
                                    result.xsdCheck = "Найдено более одного файла XSD в «${versionFolder.absolutePath}»"
                                    result.xsdError = true
                                } else if (versions[version]?.xsd == null) {
                                    result.xsdCheck = "Ok"
                                } else {
                                    result.xsdCheck = "Файл XSD не найден в «${versionFolder.absolutePath}»"
                                    result.xsdError = true
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
                        result.check = "В БД нет версий для макета с declaration_type.id = $id!"
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
        scanResult.sort(new Comparator() {
            @Override
            int compare(Object o1, Object o2) {
                if (o1.id != o2.id) {
                    return o1.id <=> o2.id
                } else {
                    return o1.version <=> o2.version
                }
            }
        })
        return scanResult
    }

    def static scanRefbookFolder(def refbooks) {
        def scanResult = []
        def missedRefbooks = refbooks.clone()

        def folderFile = new File("${Main.SRC_REFBOOK_PATH}")
        // По справочникам
        folderFile.eachDir { refbookFolder ->
            // Id справочника
            def Integer id = Main.REFBOOK_FOLDER_NAME_TO_ID[refbookFolder.name]

            if (id != null) {
                // Справочник из БД
                def refbook = refbooks.find { it.id == id }

                def result = new Expando()
                result.folder = refbookFolder.name
                result.folderFull = refbookFolder.absolutePath
                result.id = id
                scanResult.add(result)

                if (refbook == null) { // Скрипт есть в git, но нет в БД
                    result.check = "В БД не найден скрипт справочника для папки «${refbookFolder.absolutePath}» c ref_book.id = $id"
                    result.error = true
                } else {
                    result.name = refbook.name

                    // Сравнение скриптов
                    def scriptFile = new File("$refbookFolder/script.groovy")
                    if (!scriptFile.exists()) {
                        result.check = "Скрипт не найден в «${scriptFile.absolutePath}»"
                        result.error = true
                    } else {
                        def dbScript = refbook.script?.trim()?.replaceAll("\r", "")
                        def gitScript = scriptFile.text?.trim()?.replaceAll("\r", "")
                        if (dbScript == gitScript) {
                            result.check = "Ok"
                        } else {
                            result.error = true
                            result.check = "Скрипты отличаются"
                        }
                    }
                }
                missedRefbooks.remove(refbook)
            }
        }
        // Проверка скриптов справочников которых нет в git
        missedRefbooks.each { refbook ->
            def result = new Expando()
            result.id = refbook.id
            result.name = refbook.name
            result.check = "Нет в git! (id=${refbook.id})"
            result.error = true
            scanResult.add(result)
        }
        return scanResult
    }

    // FORM_TYPE.ID → Версия макета
    def static getDBVersions(def sql) {
        def map = [:]

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
        XMLUnit.setNormalizeWhitespace(true)

        def sqlTemplate = { boolean comparativeExist, accruingExist, updatingExist ->
            return "select id " +
                    " ,type_id " +
                    " ,to_char(version, 'RRRR') as version " +
                    " ,name " +
                    " ,header " +
                    " ,data_rows " +
                    " ,data_headers " +
                    " ,script " +
                    " ,status " +
                    " ,fixed_rows " +
                    " ,monthly " +
                    (comparativeExist ? " ,comparative " : "") +
                    (accruingExist ? " ,accruing " : "") +
                    (updatingExist ? " ,updating " : "") +
                    " from form_template where status not in (-1, 2)"
        }
        def tempMap = sql.firstRow("SELECT count(column_name) as result FROM user_tab_cols where table_name = 'FORM_TEMPLATE' and column_name = 'COMPARATIVE'")
        boolean comparativeExist = (tempMap.result as Integer) == 1
        tempMap = sql.firstRow("SELECT count(column_name) as result FROM user_tab_cols where table_name = 'FORM_TEMPLATE' and column_name = 'ACCRUING'")
        boolean accruingExist = (tempMap.result as Integer) == 1
        tempMap = sql.firstRow("SELECT count(column_name) as result FROM user_tab_cols where table_name = 'FORM_TEMPLATE' and column_name = 'UPDATING'")
        boolean updatingExist = (tempMap.result as Integer) == 1
        sql.eachRow(sqlTemplate(comparativeExist, accruingExist, updatingExist)) {
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
            version.code = it.header
            version.status = it.status
            version.fixed_rows = it.fixed_rows as Integer
            version.monthly = it.monthly as Integer
            version.comparative = comparativeExist ? (it.comparative as Integer) : null
            version.accruing = accruingExist ? (it.accruing as Integer) : null
            version.updating = updatingExist ? (it.updating as Integer) : null
            version.script = it.script?.characterStream?.text
            try {
                version.data_rows = it.data_rows ? XMLUnit.buildControlDocument(it.data_rows?.stringValue()?.replaceAll('stringValue=""', '')) : null
            } catch (SAXException e) {
                println("Ошибка при разборе DATA_ROWS id = ${it.id} \"${version.name}\"")
            }
            try {
                version.data_headers = it.data_headers ? XMLUnit.buildControlDocument(it.data_headers?.stringValue()?.replaceAll('stringValue=""', '')) : null
            } catch (SAXException e) {
                println("Ошибка при разборе DATA_HEADERS id = ${it.id} \"${version.name}\"")
            }
            map[type_id].put(it.version, version)
        }
        println("Load DB form_template OK")

        sql.eachRow("select fs.alias, ft.type_id, to_char(ft.version, 'RRRR') as version, fs.font_color, fs.back_color, fs.italic, fs.bold from form_style fs, form_template ft where fs.form_template_id=ft.id and ft.status not in (-1, 2)") {
            def type_id = it.type_id as Integer
            def version = it.version
            if (map[type_id][version].styles == null) {
                map[type_id][version].styles = [:]
            }
            // Стиль версии макета
            FormStyle style = new FormStyle()
            style.alias = it.alias
            style.fontColor = Color.getById(it.font_color as Integer)
            style.backColor = Color.getById(it.back_color as Integer)
            style.italic = it.italic == 1
            style.bold = it.bold == 1
            map[type_id][version].styles.put(it.alias, style)
        }
        println("Load DB form_style OK")

        def isBefore1_0 = !DBReport.checkExistShortName(Main.DB_USER)
        def sqlQuery = "select ft.type_id, to_char(ft.version, 'RRRR') as version, fc.id, fc.name, fc.ord, fc.alias, " +
                "fc.type, fc.width, fc.precision, fc.max_length, fc.checking, fc.attribute_id, fc.format, fc.filter, " +
                "fc.parent_column_id, fc.attribute_id2, fc.numeration_row" + (isBefore1_0 ? "" : ", fc.short_name") +" \n" +
                "from form_column fc, form_template ft \n" +
                "where fc.form_template_id = ft.id and ft.status not in (-1, 2) " +
                "order by fc.form_template_id, fc.ord "
        sql.eachRow(sqlQuery) {
            def type_id = it.type_id as Integer
            def version = it.version
            if (map[type_id][version].columns == null) {
                map[type_id][version].columns = [:]
            }
            // Колонки версии макета
            def column = new Expando()
            def aliases = ['id', 'name', 'ord', 'alias', 'type', 'width', 'precision', 'max_length','checking',
                    'attribute_id', 'format', 'filter', 'parent_column_id', 'attribute_id2', 'numeration_row']
            if (!isBefore1_0) {
                aliases.add('short_name')
            }
            aliases.each { alias ->
                column[alias] = it[alias]
            }
            map[type_id][version].columns.put(it.alias, column)
        }
        // определить для зависимых столбцов алиасы родительких столбцов
        map.each { type_id, versionMaps ->
            versionMaps.each { version, versionMap ->
                versionMap.columns.each { alias, column ->
                    if (column.parent_column_id) {
                        def parentColumn = versionMap.columns.find { tmpColumn -> tmpColumn.value.id == column.parent_column_id }
                        column.parentAlias = parentColumn?.key
                    }
                }
            }
        }
        println("Load DB form_column OK")

        return map
    }

    // DECLARATION_TYPE.ID → Версия макета
    def static getDeclarationDBVersions(def sql) {
        def map = [:]

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
        XMLUnit.setNormalizeWhitespace(true)

        sql.eachRow("select dt.id," +
                " dt.declaration_type_id as type_id," +
                " to_char(dt.version, 'RRRR') as version," +
                " dt.name," +
                " dt.create_script as script," +
                " dt.status, " +
                " (select data from blob_data where id = dt.xsd) As xsd, " +
                " (select data from blob_data where id = dt.jrxml) As jrxml " +
                "from declaration_template dt where dt.status not in (-1, 2)") {
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
            if (it.xsd) {
                try {
                    version.xsd = it.xsd ? XMLUnit.buildControlDocument(IOUtils.toString(it.xsd.binaryStream)) : null
                } catch (SAXException e) {
                    println("Ошибка при разборе XSD декларации id = ${it.id} \"${version.name}\"")
                }
            }
            if (it.jrxml) {
                try {
                    version.jrxml = it.jrxml ? XMLUnit.buildControlDocument(IOUtils.toString(it.jrxml.binaryStream, "UTF-8")) : null
                } catch (SAXException e) {
                    println("Ошибка при разборе JRXML декларации id = ${it.id} \"${version.name}\"")
                }
            }
            map[type_id].put(it.version, version)
        }
        println("Load DB declaration_template OK")
        return map
    }

    // REF_BOOK.ID → Скрипт справочника
    def static getRefBookScripts(def sql) {
        def refbooks = []
        sql.eachRow("select rb.id, rb.name, (select data from blob_data where id = rb.script_id) as script from ref_book rb where rb.script_id is not null") {
            def refbook = new Expando()
            refbook.id = it.id as Integer
            refbook.name = it.name
            if (it.script) {
                refbook.script = IOUtils.toString(it.script.binaryStream, "UTF-8")
            }
            refbooks.add(refbook)
        }
        println("Load DB ref_book OK")
        return refbooks
    }

    static void updateStyleAliases(Document document, def styleMap) {
        if (document == null) {
            return
        }
        def nodeList = document.getElementsByTagName("cell")
        for (int i = 0; i < nodeList.length; i++) {
            def cell = nodeList.item(i)
            def aliasNode = cell.attributes.getNamedItem("styleAlias")
            if (aliasNode != null) {
                def alias = aliasNode.getTextContent()
                def FormStyle formStyle = styleMap[alias]
                if (formStyle != null) {
                    aliasNode.setTextContent(formStyle.toString())
                }
            }
        }
    }
}

