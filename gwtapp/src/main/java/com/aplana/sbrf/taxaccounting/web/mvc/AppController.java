package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.ScriptExecutionService;
import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.ConfigModel;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Properties;

/**
 * Контроллер для работы с данными приложения
 */
@RestController
public class AppController {

    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ScriptExecutionService scriptExecutionService;

    private Properties versionInfoProperties;

    private ServerInfo serverInfo;

    public AppController(Properties versionInfoProperties, ServerInfo serverInfo) {
        this.versionInfoProperties = versionInfoProperties;
        this.serverInfo = serverInfo;
    }

    /**
     * Получения конфигурационных данных приложения
     *
     * @return конфигурационные данные
     */
    @GetMapping(value = "/rest/config")
    public ConfigModel fetchConfig() {
        String gwtMode = applicationInfo.isProductionMode() ? "" : "?gwt.codesvr=127.0.0.1:9997";

        return new ConfigModel(gwtMode, versionInfoProperties, serverInfo);
    }

    /**
     * Выполнение groovy-скрипта
     *
     * @param script содержимое groovy-скрипт
     * @return uuid группы сообщений
     * @throws IOException IOException
     */
    @PostMapping(value = "/rest/executeScript")
    public ActionResult executeScript(@RequestBody TextNode script) {
        if (script.asText().isEmpty()) {
            throw new ServiceException("Скрипт не может быть пустым");
        }
        return scriptExecutionService.executeScript(securityService.currentUserInfo(), script.asText());
    }

    /**
     * Извлечение содержимого groovy-скрипта из архива
     *
     * @param file архив
     * @return uuid группы сообщений
     * @throws IOException IOException
     */
    @PostMapping(value = "/rest/extractScript")
    public String extractScript(@RequestParam(value = "uploader") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Не указан файл для загрузки!");
        }
        if (!file.getOriginalFilename().endsWith("zip")) {
            throw new ServiceException("Скрипт должен быть упакован в zip-архив");
        }
        return scriptExecutionService.extractScript(file.getInputStream());
    }

}