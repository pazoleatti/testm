package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * Контроллер для получения выгрузки пользовательских сообщений
 *
 * @author Dmitriy Levykin
 */
@Controller
public class LogEntryController {
    private static final Log logger = LogFactory.getLog(LogEntryController.class);

    @Autowired
    private PrintingService printingService;
    @Autowired
    private LogEntryService logEntryService;

    @RequestMapping(value = "/logEntry/{uuid}", method = RequestMethod.GET)
    public void download(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (uuid == null || StringUtils.isEmpty(uuid)) {
            logger.error("Ошибка получения сообщений. Не задан параметр uuid.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Получение сообщений
        List<LogEntry> logEntryList;
        try {
            logEntryList = logEntryService.getAll(uuid);
        } catch (Exception ex) {
            logger.error("Ошибка получения сообщений. " + ex.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Построение отчета
        String filePath = printingService.generateExcelLogEntry(logEntryList);
        // Получение файла
        File file = new File(filePath);
        InputStream fis = new FileInputStream(file);
        // Выдача файла
        ServletContext context = req.getSession().getServletContext();
        String mimeType = context.getMimeType(filePath);

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType(mimeType == null ?
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8" : mimeType);
        resp.setContentLength((int) file.length());
        resp.setHeader("Content-Disposition", "attachment; filename=\"messages.xlsx\"");

        OutputStream out = resp.getOutputStream();
        IOUtils.copy(fis, out);

        fis.close();
        out.close();
    }
}
