package com.aplana.taxaccounting

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.io.IOUtils
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.FileBody

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
     * @param folderPath Папка для сохранения
     */
    public void downloadTemplate(int id, String folderPath) {
        httpBuilder.get(path: params.rootPath + params.downloadPath + '/' + id, contentType: BINARY) { resp, reader ->
            println "Download template (id=$id) $resp.statusLine"
            def zis = new ZipInputStream(reader)
            def entry = zis.nextEntry
            while (entry != null) {
                def file = new File(folderPath + entry.name)
                file.getParentFile().mkdirs()
                file.createNewFile()
                println "Application->FileSystem: ${file.canonicalPath}"
                IOUtils.copy(zis, new FileOutputStream(file))
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Загрузка шаблона НФ
     *
     * @param id Идентификатор шаблона
     * @param folderPath Папка для сохранения
     */
    public void uploadTemplate(int id, String folderPath) {
        def folder = new File(folderPath)
        if (!folder.isDirectory() || !folder.exists()) {
            return
        }
        def files = folder.listFiles()
        if (files.size() == 0) {
            return
        }
        def url =  params.rootPath + params.uploadPath + '/' + id
        def zipFile = File.createTempFile("formTemplate_${id}_", '.zip')

        def zos = new ZipOutputStream(new FileOutputStream(zipFile))

        files.each { file ->
            zos.putNextEntry(new ZipEntry(file.name))
            IOUtils.copy(new FileInputStream(file), zos)
            zos.closeEntry()
        }
        zos.close()
        // println "file = "+zipFile.canonicalPath
        println "FileSystem->Application: ${folder.canonicalPath} -> ${params.serverAddress + url}"

        httpBuilder.request(Method.POST) { request ->
            uri.path = url
            requestContentType = 'multipart/form-data'
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
            entity.addPart("uploadFormFile", new FileBody(zipFile, 'application/zip'))
            request.entity = entity
        }
        zipFile.delete()
    }
}
