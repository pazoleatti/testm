package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.UploadTransportDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Контроллер для работы с ТФ
 */
@RestController
public class TransportDataController {

    private final SecurityService securityService;
    private final UploadTransportDataService uploadTransportDataService;

    public TransportDataController(SecurityService securityService, UploadTransportDataService uploadTransportDataService) {
        this.securityService = securityService;
        this.uploadTransportDataService = uploadTransportDataService;
    }

    /**
     * Загрузка ТФ
     *
     * @param file ТФ
     * @return uuid группы сообщений
     * @throws IOException IOException
     */
    @PostMapping(value = "/actions/transportData/upload")
    public ActionResult upload(@RequestParam(value = "uploader") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Не указан файл для загрузки!");
        }
        return uploadTransportDataService.uploadFile(securityService.currentUserInfo(),
                file.getOriginalFilename(), file.getInputStream(), file.getSize());
    }

    /**
     * Загрузка всех файлов из каталога загрузки
     *
     * @return uuid группы сообщений
     */
    @PostMapping(value = "/actions/transportData/loadAll")
    public ActionResult loadAll() {
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        return uploadTransportDataService.uploadAll(userInfo, logger);
    }
}
