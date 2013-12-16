package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransportInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * MDB-модуль для импорта курсов из КСШ
 *
 * @author Dmitriy Levykin
 */
// TODO: Levykin: закомментировал для поставки 0.3.3. После поставки раскомментировать.
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/rateQueue")})
@Interceptors(TransportInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RateMDB implements MessageListener {

    private static final Log logger = LogFactory.getLog(RateMDB.class);
    private static final String RATE_ENCODING = "UTF-8";
    private static String USER_APPENDER = "controlUnp";

    private static String ERROR_FORMAT = "Сообщение не соответствует заданному формату";
    private static String ERROR_RATE = "Сообщение не соответствует передаче данных по курсам валют / драгоценным металлам";
    private static String ERROR_PUBLIC = "Сообщение не содержит публичные курсы";
    private static String ERROR_VALUE = "Сообщение не содержит значений";
    private static String ERROR_CODE = "Значения сообщения установлены не по отношению к российскому рублю";
    private static String ERROR_IMPORT = "Произошли ошибки в скрипте импорта справочника id = %d";
    private static String SUCCESS_IMPORT = "Импорт файла из КСШ успешно произведен";
    private static String ERROR_AUDIT = "Ошибка записи в журнал аудита";

    @Autowired
    RefBookScriptingService refBookScriptingService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService taUserService;

    // Маппинг атрибута "OperName" из файла -> Id справочника
    private static Map<String, Long> rateMapping = new HashMap() {{
        put("Currency", 22L);
        put("Metal", 90L);
    }};

    @Override
    public void onMessage(Message message) {
        TAUserInfo userInfo = getUser();

        if (message == null || !(message instanceof TextMessage)) {
            logger.error(ERROR_FORMAT);
            addLog(userInfo, ERROR_FORMAT);
            return;
        }

        TextMessage tm = (TextMessage) message;
        try {
            String fileText = tm.getText();
            importRate(fileText, userInfo);
        } catch (Exception ex) {
            logger.error("Ошибка при получении сообщения: " + ex.getMessage(), ex);
            addLog(userInfo, ERROR_FORMAT);
        }
    }

    private TAUserInfo getUser() {
        TAUserInfo userInfo = new TAUserInfo();
        if (taUserService != null) {
            userInfo.setUser(taUserService.getUser(USER_APPENDER));
            //TODO (alivanov 3.09.13) подставить правильного пользователя
            // Пользователя брать с некого конфигурационного файла (его пока нет)
            // Это будет специальный пользователь для операции миграции (импорта "старых" данных)
            // Сейчас же пока подставлен controlUnp
            userInfo.setIp("127.0.0.1");
        }
        return userInfo;
    }

    /**
     * Импорт курсов из сообщения
     *
     * @param fileText
     * @param userInfo
     */
    private void importRate(final String fileText, final TAUserInfo userInfo) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        final Long[] refBookId = new Long[1];

        DefaultHandler handler = new DefaultHandler() {

            boolean bSendRateRq = false;
            boolean bOperName = false;
            boolean bExRateBlock = false;
            boolean bExRateType = false;
            boolean bExRateDetails = false;
            boolean bRateParamType = false;
            boolean bCcy = false;
            boolean bCode = false;

            String operName = null;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
                if (qName.equalsIgnoreCase("SendRateRq")) {
                    bSendRateRq = true;
                } else if (qName.equalsIgnoreCase("OperName")) {
                    bOperName = true;
                } else if (qName.equalsIgnoreCase("ExRateBlock")) {
                    bExRateBlock = true;
                } else if (qName.equalsIgnoreCase("ExRateType")) {
                    bExRateType = true;
                } else if (qName.equalsIgnoreCase("ExRateDetails")) {
                    bExRateDetails = true;
                } else if (qName.equalsIgnoreCase("RateParamType")) {
                    bRateParamType = true;
                } else if (qName.equalsIgnoreCase("Ccy")) {
                    bCcy = true;
                } else if (qName.equalsIgnoreCase("Code")) {
                    bCode = true;
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                if (bSendRateRq) {
                    if (bOperName) {
                        bOperName = false;
                        operName = new String(ch, start, length);
                        refBookId[0] = rateMapping.get(operName);
                    } else if (bExRateBlock) {
                        bExRateBlock = false;
                        String exRateBlock = new String(ch, start, length);
                        if (!"PUBLIC".equalsIgnoreCase(exRateBlock)) {
                            throw new ServiceException(ERROR_PUBLIC);
                        }
                    } else if (bExRateType) {
                        bExRateType = false;
                        String exRateType = new String(ch, start, length);
                        if (!"PUBLIC-1".equalsIgnoreCase(exRateType) && !"PUBLIC-5".equalsIgnoreCase(exRateType)) {
                            throw new ServiceException(ERROR_RATE);
                        }
                        if (operName != null && (
                                operName.equalsIgnoreCase("Currency")
                                        && !exRateType.equalsIgnoreCase("PUBLIC-1")
                                        || operName.equalsIgnoreCase("Metal")
                                        && !exRateType.equalsIgnoreCase("PUBLIC-5"))) {
                            throw new ServiceException(ERROR_FORMAT);
                        }
                    } else if (bExRateDetails && bRateParamType) {
                        bRateParamType = false;
                        String rateParamType = new String(ch, start, length);
                        if (!"Value".equalsIgnoreCase(rateParamType)) {
                            throw new ServiceException(ERROR_VALUE);
                        }
                    } else if (bExRateDetails && bCcy && bCode) {
                        bCode = false;
                        bCcy = false;
                        bExRateDetails = false;
                        String code = new String(ch, start, length);
                        if (!"643".equalsIgnoreCase(code) && !"810".equalsIgnoreCase(code)) {
                            throw new ServiceException(ERROR_CODE);
                        }
                    }
                }
            }
        };

        try {
            saxParser.parse(new ByteArrayInputStream(fileText.getBytes(RATE_ENCODING)), handler);
            if (refBookId[0] == null) {
                throw new ServiceException(ERROR_RATE);
            }
            runScript(refBookId[0], fileText, userInfo);
        } catch (ServiceException ex) {
            logger.error(ex.getMessage(), ex);
            addLog(userInfo, ex.getMessage());
        } catch (Exception ex) {
            logger.error(ERROR_FORMAT, ex);
            addLog(userInfo, ERROR_FORMAT);
        }
    }

    /**
     * Запуск скрипта
     *
     * @param refBookId
     * @param fileText
     */
    private void runScript(Long refBookId, String fileText, TAUserInfo userInfo) {
        Logger logger = new Logger();
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        try {
            additionalParameters.put("inputStream", new ByteArrayInputStream(fileText.getBytes(RATE_ENCODING)));
            refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, additionalParameters);
        }
        catch (ServiceException e) {
            addLog(userInfo, e.getMessage());
            return;
        }
        catch (Exception e) {
            logger.error(e);
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            String msg = String.format(ERROR_IMPORT, refBookId);
            addLog(userInfo, msg);
            throw new ServiceLoggerException(msg, logger.getEntries());
        }
        addLog(userInfo, SUCCESS_IMPORT);
    }

    /**
     * Запись в журнал аудита
     *
     * @param userInfo
     * @param msg
     */
    private void addLog(TAUserInfo userInfo, String msg) {
        try {
            // Ошибка записи в журнал аудита не должна откатывать импорт
            if (auditService != null) {
                auditService.add(FormDataEvent.IMPORT, userInfo, 1, null, null, null, null, msg);
            }
        } catch (Exception e) {
            logger.error(ERROR_AUDIT, e);
        }
    }
}
