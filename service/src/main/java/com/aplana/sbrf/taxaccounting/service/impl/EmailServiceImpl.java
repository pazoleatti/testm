package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService {

	static final Log Log = LogFactory.getLog(EmailServiceImpl.class);

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public void send(List<String> destinations, String subject, String text) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.EMAIL_CONFIG);
        PagingResult<Map<String, RefBookValue>> params = provider.getRecords(new Date(), null, null, null);
        Properties props = new Properties();
        String login = null;
        String password = null;
        String host = null;
        String port = null;
        for (Map<String, RefBookValue> param : params) {
            String key = param.get("NAME").getStringValue();
            String value = param.get("VALUE").getStringValue();
            if (value != null && !value.isEmpty()) {
                if (key.equals("mail.smtp.user")) {
                    login = value;
                } else if (key.equals("mail.smtp.password")) {
                    password = value;
                } else if (key.equals("mail.smtp.host")) {
                    host = value;
                    props.setProperty(key, value);
                } else if (key.equals("mail.smtp.port")) {
                    port = value;
                    props.setProperty(key, value);
                } else {
                    props.setProperty(key, value);
                }
            }
        }
        if (login == null || password == null || host == null || port == null) {
            throw new IllegalArgumentException("Не указан один из обязательных параметров для отправки почты!");
        } else {
            final String finalLogin = login;
            final String finalPassword = password;
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(finalLogin, finalPassword);
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
    }

    @Override
    public boolean testAuth(Logger logger) {
        try {
            RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.EMAIL_CONFIG);
            PagingResult<Map<String, RefBookValue>> params = provider.getRecords(new Date(), null, null, null);
            Properties props = new Properties();
            String login = null;
            String password = null;
            String host = null;
            String port = null;
            for (Map<String, RefBookValue> param : params) {
                String key = param.get("NAME").getStringValue();
                String value = param.get("VALUE").getStringValue();
                if (value != null && !value.isEmpty()) {
                    if (key.equals("mail.smtp.user")) {
                        login = value;
                    } else if (key.equals("mail.smtp.password")) {
                        password = value;
                    } else if (key.equals("mail.smtp.host")) {
                        host = value;
                        props.setProperty(key, value);
                    } else if (key.equals("mail.smtp.port")) {
                        port = value;
                        props.setProperty(key, value);
                    } else {
                        props.setProperty(key, value);
                    }
                }
            }
            if (login == null || password == null || host == null || port == null) {
                logger.error("Не указан один из обязательных параметров!");
            } else {
                final String finalPassword = password;
                final String finalLogin = login;
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(finalLogin, finalPassword);
                            }
                        });
                session.getTransport().connect(host, Integer.parseInt(port), login, password);
                session.getTransport().close();
                logger.info("Проверка выполнена, ошибок не найдено");
            }
        } catch (Exception e) {
            logger.error("Авторизация с указанными параметрами не выполнена!", e);
			return false;
        }
		return true;
    }
}