package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Log LOG = LogFactory.getLog(EmailServiceImpl.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LogEntryService logEntryService;

    @Override
    public void send(List<String> destinations, String subject, String text) {
        List<Configuration> params = configurationService.getEmailConfig();
        Properties props = new Properties();
        String login = null;
        String password = null;
        String host = null;
        String port = null;
        for (Configuration param : params) {
            String code = param.getCode();
            String value = param.getValue();
            if (value != null && !value.isEmpty()) {
                if ("mail.smtp.user".equals(code)) {
                    login = value;
                } else if ("mail.smtp.password".equals(code)) {
                    password = value;
                } else if ("mail.smtp.host".equals(code)) {
                    host = value;
                    props.setProperty(code, value);
                } else if ("mail.smtp.port".equals(code)) {
                    port = value;
                    props.setProperty(code, value);
                } else {
                    props.setProperty(code, value);
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
                LOG.error(e.getMessage(), e);
                throw new ServiceException("Ошибка отправки сообщения. %s", e.getMessage());
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_CONFIG)")
    public String checkAuthAccess(TAUserInfo userInfo) {
        Logger logger = new Logger();
        try {
            Properties props = new Properties();
            String login = null;
            String password = null;
            String host = null;
            String port = null;
            Map<String, String> authEmailParams = configurationService.fetchAuthEmailParamsMap();
            for (String key : authEmailParams.keySet()) {
                switch (key) {
                    case "mail.smtp.user": {
                        login = authEmailParams.get(key);
                        break;
                    }
                    case "mail.smtp.password": {
                        password = authEmailParams.get(key);
                        break;
                    }
                    case "mail.smtp.host": {
                        host = authEmailParams.get(key);
                        break;
                    }
                    case "mail.smtp.port": {
                        port = authEmailParams.get(key);
                        break;
                    }
                }
            }
            MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
            socketFactory.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.socketFactory", socketFactory);
            if (login == null || password == null || host == null || port == null) {
                logger.error("Не указан один из обязательных параметров!");
                logger.error("Авторизация с указанными параметрами не выполнена!");
                return logEntryService.save(logger.getEntries());
            } else {
                Session session = Session.getInstance(props);
                Transport transport = session.getTransport();
                transport.connect(host, Integer.parseInt(port), login, password);
                logger.info("Авторизация выполнена успешно");
                return logEntryService.save(logger.getEntries());
            }
        } catch (Exception e) {
            LOG.error("Авторизация с указанными параметрами не выполнена!", e);
            logger.error(e.getLocalizedMessage());
            logger.error("Авторизация с указанными параметрами не выполнена!");
            return logEntryService.save(logger.getEntries());
        }
    }
}