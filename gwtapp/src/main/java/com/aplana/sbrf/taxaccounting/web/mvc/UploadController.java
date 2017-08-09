package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для загрузки файлов
 */
@RestController
public class UploadController {
    /**
     * Максимальный размер файла (5 Мб)
     */
    private static final int MAX_FILE_SIZE = 5242880;

    private BlobDataService blobDataService;
    private LogEntryService logEntryService;

    public UploadController(BlobDataService blobDataService, LogEntryService logEntryService) {
        this.blobDataService = blobDataService;
        this.logEntryService = logEntryService;
    }

    /**
     * Загрузка файла
     *
     * @param file файл
     * @return строка с uuid
     * @throws IOException в случае исключения при работе с потоками/файлами
     */
    @PostMapping(value = "/actions/upload/file", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String uploadFile(@RequestParam("uploader") MultipartFile file) throws IOException {
        String uuid = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
        return "{uuid : \"" + uuid + "\"}";
    }

    /**
     * Загрузка нескольких файлов налоговых форм
     *
     * @param files   файлы
     * @param request запрос
     * @throws IOException в случае исключения при работе с потоками/файлами
     * @throws JSONException JSONException
     * @return строка с данными о загрузке файлов
     */
    @PostMapping(value = "/actions/upload/files", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String uploadFiles(@RequestParam("uploader") List<MultipartFile> files,
                              HttpServletRequest request)
            throws IOException, JSONException {
        request.setCharacterEncoding(UTF_8);
        List<String> uuidList = new ArrayList<String>();
        for (MultipartFile file : files) {
            if (file.getSize() <= MAX_FILE_SIZE) {
                uuidList.add(blobDataService.create(file.getInputStream(), file.getOriginalFilename()));
            }
        }
        if (uuidList.size() == files.size()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.UUID.toString(), StringUtils.join(uuidList.toArray(), ','));
            return jsonObject.toString();
        } else if (!uuidList.isEmpty()) {
            Logger log = new Logger();
            log.error("Часть выбранных файлов имеет размер более 5 МБайт, данные файлы не добавлены. Для добавления доступны файлы размером меньшим или равным 5 МБайт.");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.UUID.toString(), StringUtils.join(uuidList.toArray(), ','));
            jsonObject.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(log.getEntries()));
            return jsonObject.toString();
        } else {
            Logger log = new Logger();
            log.error("Выбранные файлы (файл) имеют размер более 5 МБайт. Для добавления доступны файлы размером менее 5 МБайт.");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
            return jsonObject.toString();
        }
    }
}