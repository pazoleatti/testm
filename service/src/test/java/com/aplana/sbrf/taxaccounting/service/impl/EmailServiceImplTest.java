package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailServiceImplTest {

    private static EmailService service = new EmailServiceImpl();

    private final static ConfigurationParamModel model = new ConfigurationParamModel();

    static {
        model.setFullStringValue(ConfigurationParam.EMAIL_LOGIN, 0, "email_login_test");
        model.setFullStringValue(ConfigurationParam.EMAIL_PASSWORD, 0, "email_password_test");
        model.setFullStringValue(ConfigurationParam.EMAIL_SERVER, 0, "localhost");
    }

    @Before
    public void init() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        when(configurationDao.getByDepartment(eq(0))).thenReturn(model);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);
    }

    @Test
    public void sendTest() {
        SimpleSmtpServer server = SimpleSmtpServer.start();
        List<String> toList = new ArrayList<String>();
        toList.add("to1@to.ru");
        String subject = "subject";
        String body = "body";
        service.send(toList, subject, body);
        toList.add("to2@to.ru");
        toList.add("to3@to.ru");
        service.send(toList, subject, body);

        Iterator emailIterator = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIterator.next();
        Assert.assertEquals(2, server.getReceivedEmailSize());
        Assert.assertEquals(toList.get(0), email.getHeaderValue("To"));
        Assert.assertEquals(subject, email.getHeaderValue("Subject"));
        Assert.assertEquals(body, email.getBody());

        server.stop();
    }
}
