package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService {

	static final Log Log = LogFactory.getLog(EmailServiceImpl.class);

    @Autowired
    private ConfigurationDao configurationDao;

    @Override
    public void send(List<String> destinations, String subject, String text) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        final String login = model.get(ConfigurationParam.EMAIL_LOGIN, 0).get(0);
        final String password = model.get(ConfigurationParam.EMAIL_PASSWORD, 0).get(0);
        String server = model.get(ConfigurationParam.EMAIL_SERVER, 0).get(0);
        String port = model.get(ConfigurationParam.EMAIL_PORT, 0).get(0);

        Properties props = new Properties();
        props.put("mail.smtp.host", server);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol", "smtps");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(login));
            for (String destination : destinations) {
                InternetAddress address = new InternetAddress(destination);
                message.addRecipient(Message.RecipientType.TO, address);
            }
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
			Log.error(e);
            throw new ServiceException("Ошибка отправки сообщения. %s", e.getMessage());
        }
    }

    @Override
    public boolean testAuth(String server, String port, String login, String password, Logger logger) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            Session session = Session.getInstance(props);
            Transport transport = session.getTransport("smtp");
            transport.connect(server, Integer.parseInt(port), login, password);
            transport.close();
            logger.info("Проверка выполнена, ошибок не найдено");
        } catch (Exception e) {
            logger.error("Авторизация с указанными параметрами не выполнена!");
			Log.error(e);
			return false;
        }
		return true;
    }
}