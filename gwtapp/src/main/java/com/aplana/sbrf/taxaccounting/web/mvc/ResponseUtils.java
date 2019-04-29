package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Класс для создания заголовка ответа
 */
class ResponseUtils {
    private static final String ENCODING = "UTF-8";

    /**
     * Создание заголовка ответа
     *
     * @param req      запрос
     * @param response ответ
     * @param blobData файловое хранилище
     * @throws IOException IOException
     */
    static void createBlobResponse(final HttpServletRequest req, final HttpServletResponse response, final BlobData blobData) throws IOException {
        String fileName = blobData.getName();
        createBlobHeaders(req, response, fileName);

        DataInputStream in = new DataInputStream(blobData.getInputStream());
        OutputStream out = response.getOutputStream();
        int count;
        try {
            count = IOUtils.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
        response.setContentLength(count);
    }

    /**
     * Приведение названия файла к корректному виду
     *
     * @param request          запрос
     * @param response         ответ
     * @param originalFileName название файла
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    static void createBlobHeaders(HttpServletRequest request, HttpServletResponse response, String originalFileName) throws UnsupportedEncodingException {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String fileName = URLEncoder.encode(originalFileName, ENCODING).replaceAll("\\+", "%20");
        String fileNameAttr = "filename=";
        if (userAgent.contains("msie") || userAgent.contains("webkit")) {
            fileName = "\"" + fileName + "\"";
        } else {
            fileNameAttr = fileNameAttr.replace("=", "*=") + ENCODING + "''";
        }
        response.setHeader("Content-Disposition", "attachment;" + fileNameAttr + fileName);
    }
}
