package com.aplana.taxaccounting

import groovy.sql.Sql
import org.apache.commons.io.IOUtils
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.xml.sax.SAXException

/**
 * Отчет сравнения Git и БД
 */
class GitReport {
    // Загрузка из git в БД и отчет в html-файле
    def static void updateScripts(def versionsMap, def checkOnly = true) {
        println("DBMS connect: ${Main.DB_USER}")
        def sql = Sql.newInstance(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

        def writer = new FileWriter(new File(Main.REPORT_GIT_NAME))
        def builder = new groovy.xml.MarkupBuilder(writer)
        builder.html {
            head {
                meta(charset: 'windows-1251')
                title "Сравнение макетов git и БД ${Main.DB_USER}"
                style(type: "text/css", Main.HTML_STYLE)
            }
            body {
                p "Сравнение макетов в БД ${Main.DB_USER} и git:"
                table(class: 'rt') {
                    Main.TAX_FOLDERS.keySet().each { folderName ->
                        def scanResult = scanSrcFolderAndUpdateDb(versionsMap, folderName, checkOnly ? null : sql)
                        if (!scanResult.isEmpty()) {
                            tr {
                                td(colspan: 8, class: 'hdr', Main.TAX_FOLDERS[folderName])
                            }
                            tr {
                                th 'type_id'
                                th 'Папка'
                                th 'Название'
                                th 'Версия git'
                                th 'Версия БД'
                                th 'Заголовок'
                                th 'Сравнение скриптов'
                                th 'Сравнение стилей'
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
                                    td(class: (result.errorCode ? 'td_error' : 'td_ok'), result.checkCode)
                                    td(class: (result.error ? 'td_error' : 'td_ok'), result.check)
                                    td(class: (result.errorStyle ? 'td_error' : 'td_ok'), result.checkStyle)
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

    // Загрузка скриптов деклараций из git в БД и отчет в html-файле
    def static void updateDeclarationScripts(def versionsMap, def checkOnly = true) {
        println("DBMS connect: ${Main.DB_USER}")
        def sql = Sql.newInstance(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")

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
                        def scanResult
                        try {
                            scanResult = scanDeclarationSrcFolderAndUpdateDb(versionsMap, folderName, checkOnly ? null : sql)//TODO
                        } finally {
                            sql.close()
                        }
                        if (!scanResult.isEmpty()) {
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

    // Сравнение git-версии с версией в БД и загрузка в случае отличий
    def static scanSrcFolderAndUpdateDb(def versionsMap, def folderName, def sql) {
        def map = [:]
        def scanResult = []

        def folderFile = new File("${Main.SRC_FOLDER_PATH}/$folderName")
        map.put(folderFile, [:])
        // По видам НФ
        folderFile.eachDir { templateFolder ->
            // Id типа формы
            def Integer id = Main.TEMPLATE_NAME_TO_TYPE_ID[folderName][templateFolder.name]
            // Новый тип в результат
            map[folderFile].put(templateFolder.name, [:])

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
                                // Сравнение стилей
                                def contentFile = new File("$versionFolder/content.xml")
                                if (!contentFile.exists()) {
                                    result.checkStyle = "Файл со стилями не найден в «${contentFile.absolutePath}»"
                                    result.errorStyle = true
                                } else {
                                    def Map mapStylesXml = [:]
                                    def xml = new XmlSlurper().parseText(contentFile.getText())
                                    def codeXml = xml.header?.text() ?: xml.code?.text()
                                    if (codeDB != codeXml) {
                                        result.checkCode = "Значение в БД \"$codeDB\" отличается от значения в GIT \"$codeXml\""
                                        result.errorCode = true
                                    } else {
                                        result.checkCode = "Ok"
                                        result.errorCode = false
                                    }
                                    def stylesDb = versions[version]?.styles
                                    // Собираем все стили из xml
                                    for (def styleXml : xml.styles) {
                                        def style = new Expando()
                                        style.alias = styleXml.alias.text()
                                        style.back_color = StyleColor[styleXml.backColor.text()]
                                        style.font_color = StyleColor[styleXml.fontColor.text()]
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
                                            ['back_color', 'font_color', 'bold', 'italic'].each {
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

    // Сравнение git-версии декларации с версией в БД и загрузка в случае отличий
    def static scanDeclarationSrcFolderAndUpdateDb(def versionsMap, def folderName, def sql) {
        def map = [:]
        def scanResult = []

        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true)
        XMLUnit.setNormalizeWhitespace(true)

        def folderFile = new File("${Main.SRC_FOLDER_PATH}/$folderName")
        map.put(folderFile, [:])
        // По видам НФ
        folderFile.eachDir { templateFolder ->
            // Id типа формы
            def Integer temp_id = Main.TEMPLATE_NAME_TO_TYPE_ID[folderName][templateFolder.name]
            // Новый тип в результат
            map[folderFile].put(templateFolder.name, [:])

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
                                boolean jrxmlEqual = true
                                if (!jrxmlFile.exists()) {
                                    result.jrxmlCheck = "Макет Jasper не найден в «${jrxmlFile.absolutePath}»"
                                    result.jrxmlError = true
                                } else {
                                    def dbJrxml = versions[version]?.jrxml
                                    def gitJrxml = XMLUnit.buildControlDocument(jrxmlFile?.text)
                                    if (dbJrxml != null && gitJrxml != null) {
                                        Diff diff = XMLUnit.compareXML(dbJrxml, gitJrxml)
                                        jrxmlEqual = diff.similar()
                                    } else if (dbJrxml != null || gitJrxml != null) {
                                        jrxmlEqual = false
                                    }
                                    if (jrxmlEqual) {
                                        result.jrxmlCheck = "Ok"
                                    } else {
                                        result.jrxmlError = true
                                        result.jrxmlCheck = "Макеты Jasper отличаются"
                                    }
                                }
                                // Сравнение XSD
                                def xsdFiles = new File("$versionFolder").listFiles(new FilenameFilter() {
                                    @Override
                                    boolean accept(File dir, String name) {
                                        return name.toLowerCase().endsWith("xsd")
                                    }
                                })
                                boolean xsdEqual = false
                                if (xsdFiles.size() == 1) {
                                    def xsdFile = xsdFiles[0]
                                    def dbXsd = versions[version]?.xsd
                                    def gitXsd = XMLUnit.buildControlDocument(xsdFile?.text)
                                    if (dbXsd != null && gitXsd != null) {
                                        Diff diff = XMLUnit.compareXML(dbXsd, gitXsd)
                                        xsdEqual = diff.similar()
                                    } else if (dbXsd != null || gitXsd != null) {
                                        xsdEqual = false
                                    }
                                    if (xsdEqual) {
                                        result.xsdCheck = "Ok"
                                    } else {
                                        result.xsdError = true
                                        result.xsdCheck = "Файлы XSD отличаются"
                                    }
                                } else if (xsdFiles.size() > 1 ) {
                                    result.xsdCheck = "Найдено более одного файла XSD в «${versionFolder.absolutePath}»"
                                    result.xsdError = true
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

    // FORM_TYPE.ID → Версия макета
    def static getDBVersions() {
        println("DBMS connect: ${Main.DB_USER}")
        def sql = Sql.newInstance(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")
        def map = [:]

        sql.eachRow("select id, type_id, to_char(version, 'RRRR') as version, name, header, script, status from form_template where status not in (-1, 2)") {
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
            version.script = it.script?.characterStream?.text
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
            def style = new Expando()
            style.alias = it.alias
            style.font_color = StyleColor.getById(it.font_color as Integer)
            style.back_color = StyleColor.getById(it.back_color as Integer)
            style.italic = it.italic == 1
            style.bold = it.bold == 1
            map[type_id][version].styles.put(it.alias, style)
        }
        sql.close()
        println("Load DB form_style OK")
        return map
    }

    // DECLARATION_TYPE.ID → Версия макета
    def static getDeclarationDBVersions() {
        println("DBMS connect: ${Main.DB_USER}")
        def sql = Sql.newInstance(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")
        def map = [:]

        try {
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
                map[type_id].put(it.version, version)
            }

        } finally {
            sql.close()
        }
        println("Load DB declaration_template OK")
        return map
    }
}
