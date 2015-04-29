package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: avanteev
 * Сервлет для обработки загрузки файла из налоговых форм на сервер
 */

@Controller
@RequestMapping("/uploadController")
public class UploadController {

    @Autowired
    BlobDataService blobDataService;

    /**
     * Метод исключительно для загрузки содержимого на страницу.
     * Сделан в следствии того, что нет возможности использовать html5 спецификацию.
     * Задача, в рамках которой делалось http://jira.aplana.com/browse/SBRFACCTAX-10779
     * @throws FileUploadException
     * @throws IOException
     */
    @RequestMapping(value = "/patterntemp", method = RequestMethod.POST)
    public void processUploadTemp(@RequestParam("uploader") MultipartFile file,
            HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        IOUtils.copy(file.getInputStream(), response.getWriter(), "UTF-8");
    }

    @RequestMapping(value = "/pattern", method = RequestMethod.POST)
    public void processUploadXls(@RequestParam("uploader") MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException, JSONException {
        request.setCharacterEncoding("UTF-8");
        String uuid = blobDataService.create(file.getInputStream(), file.getName());
        JSONObject result = new JSONObject();
        result.put(UuidEnum.UUID.toString(), uuid);
        response.getWriter().printf(result.toString());
    }
}
