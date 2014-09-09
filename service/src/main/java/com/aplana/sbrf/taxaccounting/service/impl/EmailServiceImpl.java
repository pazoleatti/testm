package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private ConfigurationDao configurationDao;

    @Override
    public void send(List<String> destinations, String subject, String text) {
        // TODO SBRFACCTAX-8784

        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        final String login = model.get(ConfigurationParam.EMAIL_LOGIN, 0).get(0);
        final String password = model.get(ConfigurationParam.EMAIL_PASSWORD, 0).get(0);
        String server = model.get(ConfigurationParam.EMAIL_SERVER, 0).get(0);

        Properties props = new Properties();
        props.put("mail.smtp.host", server);
        props.put("mail.smtp.port", "25");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sender"));
            for (String destination : destinations) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            }
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new ServiceException("Ошибка отправки сообщения. %s", e.getMessage());
        }
    }
}