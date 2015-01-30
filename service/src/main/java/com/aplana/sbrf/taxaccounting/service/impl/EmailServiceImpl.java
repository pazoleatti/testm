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
import com.sun.mail.util.MailSSLSocketFactory;
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
            try {
                MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
                socketFactory.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.socketFactory", socketFactory);
                Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(finalLogin, finalPassword);
                        }
                    });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(login));
                for (String destination : destinations) {
                    InternetAddress address = new InternetAddress(destination);
                    message.addRecipient(Message.RecipientType.TO, address);
                }
                message.setSubject(subject);
                message.setText(text);
                Transport.send(message);
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
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
            MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
            socketFactory.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.socketFactory", socketFactory);
            if (login == null || password == null || host == null || port == null) {
                logger.error("Не указан один из обязательных параметров!");
                logger.error("Авторизация с указанными параметрами не выполнена!");
                return false;
            } else {
                Session session = Session.getInstance(props);
                Transport transport = session.getTransport();
                transport.connect(host, Integer.parseInt(port), login, password);
                logger.info("Авторизация выполнена успешно");
            }
        } catch (Exception e) {
            Log.error("Авторизация с указанными параметрами не выполнена!", e);
            logger.error(e.getLocalizedMessage());
            logger.error("Авторизация с указанными параметрами не выполнена!");
			return false;
        }
		return true;
    }
}