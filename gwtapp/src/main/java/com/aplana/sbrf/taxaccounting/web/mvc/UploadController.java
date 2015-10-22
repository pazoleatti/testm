package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
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
import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 * Сервлет для обработки загрузки файла из налоговых форм на сервер
 */

@Controller
@RequestMapping("/uploadController")
public class UploadController {

    private static final int MAX_FILE_SIZE = 5242880; // 5 МБайт

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

    @RequestMapping(value = "/formDataFiles", method = RequestMethod.POST)
    public void processUploadFormDataFiles(@RequestParam("uploader") List<MultipartFile> files,
                                          HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException, JSONException {
        request.setCharacterEncoding("UTF-8");
        List<String> uuidList = new ArrayList<String>();
        for(MultipartFile file: files) {
            if (file.getSize() <= MAX_FILE_SIZE) {
                uuidList.add(blobDataService.create(file.getInputStream(), file.getOriginalFilename()));
            }
        }
        if (uuidList.size() == files.size()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.UUID.toString(), StringUtils.join(uuidList.toArray(), ','));
            response.getWriter().printf(jsonObject.toString());
        } else if (!uuidList.isEmpty()) {
            Logger log = new Logger();
            log.error("Часть выбранных файлов имеет размер более 5 МБайт, данные файлы не добавлены. Для добавления доступны файлы размером меньшим или равным 5 МБайт.");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.UUID.toString(), StringUtils.join(uuidList.toArray(), ','));
            jsonObject.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(log.getEntries()));
            response.getWriter().printf(jsonObject.toString());
        } else {
            Logger log = new Logger();
            log.error("Выбранные файлы (файл) имеют размер более 5 МБайт. Для добавления доступны файлы размером менее 5 МБайт.");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
            response.getWriter().printf(jsonObject.toString());
        }

    }
}
