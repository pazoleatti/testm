package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Контроллер для получения выгрузки пользовательских сообщений
 *
 * @author Dmitriy Levykin
 */
@Controller
public class LogEntryController {

    private static final Log LOG = LogFactory.getLog(LogEntryController.class);

    @Autowired
    private PrintingService printingService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LogEntryService logEntryService;

    @RequestMapping(value = "/logEntry/{uuid}", method = RequestMethod.GET)
    public void download(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (uuid == null || uuid.isEmpty()) {
            LOG.error("Ошибка получения сообщений. Не задан параметр uuid.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Получение сообщений
        List<LogEntry> logEntryList;
        try {
            logEntryList = logEntryService.getAll(uuid);
        } catch (Exception ex) {
            LOG.error("Ошибка получения сообщений. " + ex.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Построение отчета
        String fileUuid = printingService.generateExcelLogEntry(logEntryList);
        // Получение файла
        InputStream fis = blobDataService.get(fileUuid).getInputStream();
        // Выдача файла
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        //resp.setContentLength((int) file.length());

        resp.setHeader("Content-Disposition", createHeader(logEntryList != null && !logEntryList.isEmpty() ? logEntryList.get(0).getMessage() : ""));
        OutputStream out = resp.getOutputStream();
        IOUtils.copy(fis, out);

        fis.close();
        out.close();
    }


    /**
     * Формирует заголовок с именем файла для выгрузки журнала сообщений
     *
     * @param headerMsg заглавное сообщение
     * @return
     */
    public static String createHeader(String headerMsg) {
        //"20170317_112234_Журнал_сообщений"
        StringBuffer sb = new StringBuffer();
        sb.append("attachment; filename=\"");
        sb.append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        sb.append("_Messages.csv");
        sb.append("\"");
        return sb.toString();
    }


}
