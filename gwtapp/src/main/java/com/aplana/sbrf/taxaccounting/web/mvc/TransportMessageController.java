package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Контроллер для объектов "Транспортное сообщение".
 */
@RestController
public class TransportMessageController {

    @Autowired
    private TransportMessageService transportMessageService;
    @Autowired
    private BlobDataService blobDataService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(TransportMessageFilter.class, new RequestParamEditor(TransportMessageFilter.class));
    }

    /**
     * Выгрузка Транспортных сообщений по фильтру и с пагинацией.
     */
    @GetMapping("/rest/transportMessages")
    public List<TransportMessage> getMessagesByFilter(@RequestParam(required = false) TransportMessageFilter filter) {
        return transportMessageService.findByFilter(filter);
    }

    /**
     * Выгрузка Транспортного сообщения по идентификатору.
     */
    @GetMapping("/rest/transportMessages/{messageId}")
    public TransportMessage getMessage(@PathVariable Long messageId) {
        return transportMessageService.findById(messageId);
    }

    /**
     * Выгрузка Транспортного сообщения по идентификатору.
     */
    @GetMapping("/rest/transportMessages/{messageId}/body")
    public String getMessageBody(@PathVariable Long messageId) {
        return transportMessageService.findMessageBodyById(messageId);
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
     * Выгрузка файла из Транспортного сообщения.
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
}
