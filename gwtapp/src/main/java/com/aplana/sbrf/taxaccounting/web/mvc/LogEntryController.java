package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.web.model.CustomMediaType;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Контроллер для работы с уведомлениями
 *
 * @author Dmitriy Levykin
 */
@RestController
public class LogEntryController {
    private static final Log LOG = LogFactory.getLog(LogEntryController.class);

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    private PrintingService printingService;
    private BlobDataService blobDataService;
    private LogEntryService logEntryService;

    public LogEntryController(PrintingService printingService, BlobDataService blobDataService, LogEntryService logEntryService) {
        this.printingService = printingService;
        this.blobDataService = blobDataService;
        this.logEntryService = logEntryService;
    }

    /**
     * Получает список сообщений для определенного события
     * @param uuid идентификатор события
     * @param pagingParams параметры пагинации
     * @return список сообщений
     */
    @GetMapping(value = "/rest/logEntry/{uuid}")
    public JqgridPagedList<LogEntry> fetchLogEntries(@PathVariable String uuid, @RequestParam PagingParams pagingParams) {
        PagingResult<LogEntry> logEntries = logEntryService.fetch(uuid, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(logEntries, logEntries.getTotalCount(), pagingParams);
    }

    /**
     * Получает количество сообщений на каждом уровне важности
     * @param uuid идентификатор события
     * @return Map с количеством ошибок на каждом уровне важности
     */
    @GetMapping(value = "/rest/logEntry/{uuid}", params = "projection=count")
    public Map<LogLevel, Integer> fetchLogEntriesCount(@PathVariable String uuid) {
        return logEntryService.getLogCount(uuid);
    }

    /**
     * Скачивание уведомлений в виде csv файла
     *
     * @param uuid идентификатор группы уведомлений
     * @param resp объект http ответа
     * @throws IOException в случае ошибки при работе с потоками/файлами
     */
    @GetMapping(value = "/actions/logEntry/{uuid}", produces = CustomMediaType.APPLICATION_VND_UTF8_VALUE)
    public void download(@PathVariable String uuid, HttpServletResponse resp) throws IOException {
        if (uuid == null || uuid.isEmpty()) {
            LOG.error("Ошибка получения уведомлений. Не задан параметр uuid.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Получение списка уведомлений
        List<LogEntry> logEntryList;
        try {
            logEntryList = logEntryService.getAll(uuid);
        } catch (Exception ex) {
            LOG.error("Ошибка получения уведомлений. " + ex.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Построение отчета
        String fileUuid = printingService.generateCsvLogEntries(logEntryList);
        // TODO snazin: заменить на использование ResponseUtils.createBlobResponse
        // Получение файла
        InputStream fis = blobDataService.get(fileUuid).getInputStream();
        // Копирование файла в http ответ
        resp.setHeader("Content-Disposition", createHeader());
        OutputStream out = resp.getOutputStream();
        IOUtils.copy(fis, out);
        // Закрываем потоки ввода/вывода
        fis.close();
        out.close();
    }

    /**
     * Формирует заголовок с именем файла для выгрузки уведомлений
     *
     * @return заголовок с именем файла для выгрузки уведомлений
     */
    private String createHeader() {
        // Messages_yyyy-MM-dd_HH-mm-ss.csv
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        return "attachment; filename=\"" +
                "Messages_" +
                formatter.format(new Date()) +
                ".csv" +
                "\"";
    }
}