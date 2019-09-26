package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
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
    public ActionResult exportExcel() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return transportMessageService.asyncExport(Collections.EMPTY_LIST, userInfo);
    }

    /**
     * Выгрузка списка транспортных сообщений по списку ид
     */
    @PostMapping(value = "/actions/transportMessages/exportExcelBySelected")
    public ActionResult exportExcelBySelected(@RequestBody List<Long> transportMessageIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return transportMessageService.asyncExport(transportMessageIds, userInfo);
    }

    /**
     * Выгрузка списка транспортных сообщений по фильтру
     */
    @PostMapping(value = "/actions/transportMessages/exportExcelByFilter")
    public ActionResult exportExcelByFilter(@RequestParam TransportMessageFilter filter) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return transportMessageService.asyncExport(filter, userInfo);
    }

}
