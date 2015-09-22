package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.apache.commons.fileupload.FileUploadException;
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

    @Autowired
    LogEntryService logEntryService;

    @RequestMapping(value = "/pattern", method = RequestMethod.POST)
    public void processUploadXls(@RequestParam("uploader") MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        request.setCharacterEncoding("UTF-8");
        String uuid = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
        response.getWriter().printf("{uuid : \"%s\"}", uuid);
    }

    @RequestMapping(value = "/formDataFile", method = RequestMethod.POST)
    public void processUploadFormDataFile(@RequestParam("uploader") MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException, JSONException {
        request.setCharacterEncoding("UTF-8");
        if (file.getSize() > 5*1024*1024) {
            JSONObject errors = new JSONObject();
            Logger log = new Logger();
            log.error("Выбранные файлы (файл) имеют размер более 5 МБайт. Для добавления доступны файлы размером менее 5 МБайт.");
            errors.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
            response.getWriter().printf(errors.toString());
        } else {
            String uuid = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
            response.getWriter().printf("{uuid : \"%s\"}", uuid);
        }
    }
}
