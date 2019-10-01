package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

/**
 * Контроллер для объектов "Транспортное сообщение".
 */
@RestController
public class TransportMessageController {

    @Autowired
    private TransportMessageService transportMessageService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LogEntryService logEntryService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(TransportMessageFilter.class, new RequestParamEditor(TransportMessageFilter.class));
    }

    /**
     * Выгрузка Транспортных сообщений по фильтру и с пагинацией с учетом доступных пользователю подразделений.
     */
    @GetMapping("/rest/transportMessages")
    public JqgridPagedList<TransportMessage> getMessagesByFilter(@RequestParam(required = false) TransportMessageFilter filter,
                                                                 @RequestParam(required = false) PagingParams pagingParams) {
        PagingResult<TransportMessage> messages = transportMessageService.findByFilterWithUserDepartments(filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(messages, pagingParams);
    }

    /**
     * Выгрузка Транспортного сообщения по идентификатору.
     */
    @GetMapping("/rest/transportMessages/{messageId}")
    public TransportMessage getMessage(@PathVariable Long messageId) {
        return transportMessageService.findById(messageId);
    }

    /**
     * Выгрузка файла из Транспортного сообщения.
     */
    @GetMapping("rest/transportMessages/{messageId}/file")
    public void downloadTransportMessageFile(@PathVariable Long messageId,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws IOException {
        TransportMessage message = transportMessageService.findById(messageId);
        if (message == null || message.getBlob() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        BlobData file = blobDataService.get(message.getBlob().getUuid());
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ResponseUtils.createBlobResponse(request, response, file);
    }

    /**
     * Выгрузка тела Транспортного сообщения.
     */
    @GetMapping("/rest/transportMessages/{messageId}/body")
    public String getMessageBody(@PathVariable Long messageId) {
        return transportMessageService.findMessageBodyById(messageId);
    }

    /**
     * Выгрузка тела Транспортного сообщения в виде файла.
     */
    @GetMapping("rest/transportMessages/{messageId}/bodyFile")
    public void downloadTransportMessageBodyFile(@PathVariable Long messageId,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws IOException {
        TransportMessage message = transportMessageService.findById(messageId);
        if (message == null || !message.hasBody()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String messageBody = transportMessageService.findMessageBodyById(messageId);
        if (StringUtils.isEmpty(messageBody)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ResponseUtils.createBlobHeaders(request, response, message.getBodyFileName());
        ServletOutputStream out = response.getOutputStream();
        out.print(messageBody);
        out.flush();
        out.close();
    }


    /**
     * Выгрузка списка транспортных сообщений
     */
    @PostMapping(value = "/actions/transportMessages/exportExcel")
    public void exportExcel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        export(req, resp, null);
    }

    /**
     * Выгрузка списка транспортных сообщений по списку ид
     */
    @PostMapping(value = "/actions/transportMessages/exportExcelBySelected")
    public void exportExcelBySelected(@RequestBody List<Long> transportMessageIds,
                                      HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        TransportMessageFilter transportMessageFilter = new TransportMessageFilter();
        transportMessageFilter.setIds(transportMessageIds);

        export(req, resp, transportMessageFilter);
    }

    /**
     * Выгрузка списка транспортных сообщений по фильтру
     */
    @PostMapping(value = "/actions/transportMessages/exportExcelByFilter")
    public void exportExcelByFilter(@RequestParam TransportMessageFilter filter,
                                    HttpServletRequest req, HttpServletResponse resp) throws IOException {
        export(req, resp, filter);
    }

    private void export(HttpServletRequest req, HttpServletResponse resp, TransportMessageFilter transportMessageFilter)
            throws IOException {
        TAUserInfo userInfo = securityService.currentUserInfo();

        Calendar generationExcelDate = Calendar.getInstance();
        String headerDescription = "Список Транспортных сообщений (" +
                FastDateFormat.getInstance("dd.MM.yyyy HH.mm.ss").format(generationExcelDate) + ")";

        String fileName = "Список транспортных сообщений " +
                FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss").format(generationExcelDate) +
                ".xlsx";
        //ResponseUtils.createBlobHeaders(req, resp, fileName);

        String fileNameAttr = "filename=\"" + URLEncoder.encode(fileName, "UTF-8")
                .replaceAll("\\+", "%20") + "\"";
        resp.setHeader("Content-Disposition", "attachment;" + fileNameAttr);
        resp.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        ServletOutputStream out = null;
        InputStream in = null;
        try {
            out = resp.getOutputStream();
            in = transportMessageService.export(headerDescription, transportMessageFilter, userInfo);
            IOUtils.copy(in, out);
        } catch (ServiceException | IOException e) {
            Logger logger = logEntryService.createLogger();
            String messageError = "Не выполнена операция \"Выгрузка списка транспортных сообщений.\n" +
                    "Причина: " + e.getMessage();
            logger.log(LogLevel.ERROR, messageError);

            resp.setContentType(MediaType.TEXT_PLAIN_VALUE+";charset=UTF-8");
            out.print(messageError);
            resp.setStatus(500);
        } finally {
            // Закрываем потоки ввода/вывода
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }


    }


}
