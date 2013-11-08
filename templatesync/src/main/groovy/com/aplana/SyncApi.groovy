package com.aplana

import groovyx.net.http.HTTPBuilder
import org.apache.commons.io.IOUtils

import java.util.zip.ZipInputStream

import static groovyx.net.http.ContentType.BINARY
import static groovyx.net.http.ContentType.URLENC

/**
 * @author Dmitriy Levykin
 */
public class SyncAPI {
    private static SyncAPI instance;

    private Map params;
    private HTTPBuilder httpBuilder;

    private SyncAPI(def paramsMap) {
        params = paramsMap
    }

    /**
     * Экземпляр
     *
     * @param paramsMap
     * @return
     */
    public static SyncAPI getInstance(Map paramsMap) {
        if (instance == null) {
            instance = new SyncAPI(paramsMap)
        }
        instance
    }

    /**
     * Авторизация
     */
    public void login() {
        println "Connect to " + params.serverAddress
        httpBuilder = new HTTPBuilder(params.serverAddress)
        def postBody = [j_username: params.login, j_password: params.pass]
        httpBuilder.post(path: params.rootPath + params.loginPath, body: postBody, requestContentType: URLENC) { resp ->
            println "AUTH $resp.statusLine"
        }
    }

    /**
     * Закрытие сессии
     */
    public void close() {
        httpBuilder.shutdown()
        httpBuilder = null
        instance = null
        params = null
    }

    /**
     * Получение шаблона НФ
     *
     * @param id Идентификатор шаблона
     * @param path Папка для сохранения
     */
    public void downloadTemplate(int id, String path) {
        httpBuilder.get(path: params.rootPath + params.downloadPath + '/' + id, contentType: BINARY) { resp, reader ->
            println "Get template (id=$id) $resp.statusLine"
            def zis = new ZipInputStream(reader)
            def entry = zis.nextEntry
            while (entry != null) {
                def file = new File(path + entry.name)
                file.getParentFile().mkdirs()
                file.createNewFile()
                println "DBMS->FileSystem: ${file.canonicalPath}"
                IOUtils.copy(zis, new FileOutputStream(file))
                entry = zis.nextEntry
            }
        }
    }
}