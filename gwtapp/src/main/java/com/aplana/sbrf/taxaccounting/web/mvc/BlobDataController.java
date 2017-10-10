package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с файловым хранилищем
 */
@RestController
public class BlobDataController {
    /**
     * Максимальный размер файла (5 Мб)
     */
    private static final int MAX_FILE_SIZE = 5242880;

    private SecurityService securityService;
    private BlobDataService blobDataService;
    private LogEntryService logEntryService;
    private ReportService reportService;

    public BlobDataController(SecurityService securityService, BlobDataService blobDataService, LogEntryService logEntryService, ReportService reportService) {
        this.securityService = securityService;
        this.blobDataService = blobDataService;
        this.logEntryService = logEntryService;
        this.reportService = reportService;
    }
    /**
     * Выгрузка файла по uuid (работа настройщика)
     *
     * @param uuid уникальный идентификатор файла
     * @param req  запрос
     * @param resp ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/blobData/{uuid}/conf")
    public void processDownloadConf(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            BlobData blobData = blobDataService.get(uuid);
            if (blobData != null) {
                ResponseUtils.createBlobResponse(req, resp, blobData);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Получает архив журнала аудита
     *
     * @param uuid     идентификатор
     * @param request  запрос
     * @param response ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/blobData/{uuid}")
    public void processDownload(@PathVariable String uuid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobData blobData = blobDataService.get(uuid);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(request, response, blobData);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Загрузка файла
     *
     * @param file файл
     * @return строка с uuid
     * @throws IOException в случае исключения при работе с потоками/файлами
     */
    @PostMapping(value = "/actions/blobData/uploadFile", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
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
    @PostMapping(value = "/actions/blobData/uploadFiles", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
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

    /**
     * Проверка пользователя на наличие необходимых прав
     *
     * @param response ответ
     * @param userInfo данные пользователя
     * @return признак наличия прав
     * @throws IOException IOException
     */
    private boolean checkRole(HttpServletResponse response, TAUserInfo userInfo) throws IOException {
        if (!userInfo.getUser().hasRoles(TARole.N_ROLE_CONF, TARole.F_ROLE_CONF, TARole.N_ROLE_CONTROL_UNP)) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding(UTF_8);
            response.getWriter().printf("Ошибка доступа (недостаточно прав)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
