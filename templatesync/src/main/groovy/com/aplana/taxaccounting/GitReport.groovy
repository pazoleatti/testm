package com.aplana.taxaccounting

import groovy.sql.Sql

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
        sql.close()
        println("Load DB form_template OK")

        def styleSql = Sql.newInstance(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD, "oracle.jdbc.OracleDriver")
        styleSql.eachRow("select fs.alias, ft.type_id, to_char(ft.version, 'RRRR') as version, fs.font_color, fs.back_color, fs.italic, fs.bold from form_style fs, form_template ft where fs.form_template_id=ft.id and ft.status not in (-1, 2)") {
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
        styleSql.close()
        println("Load DB form_style OK")
        return map
    }
}
