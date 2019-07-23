package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"jms", "development"})
public class EdoTestController {

    private static final Log LOG = LogFactory.getLog(EdoTestController.class);

    @Autowired
    private MessageSender messageSender;

    @PostMapping("/dev/edo/send")
    public void send(@RequestBody String message) {
        LOG.info("Send message: " + message);
        System.out.println("Send message: " + message);
        messageSender.sendMessage(message);
    }
}
