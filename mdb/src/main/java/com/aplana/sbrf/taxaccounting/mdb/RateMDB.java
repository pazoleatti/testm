package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
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
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/rateQueue")})
@Interceptors(TransportInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RateMDB implements MessageListener {

    private static final Log logger = LogFactory.getLog(RateMDB.class);
    private static final String RATE_ENCODING = "UTF-8";
    // Максимальное число попыток загрузки одного сообщения
    private static final int MAX_DELIVERY_COUNT = 10;

    static final String ERROR_FORMAT = "Сообщение не соответствует заданному формату";
    static final String ERROR_RATE = "Сообщение не соответствует передаче данных по курсам валют / драгоценным металлам";
    static final String ERROR_PUBLIC = "Сообщение не содержит публичные курсы";
    static final String ERROR_VALUE = "Сообщение не содержит значений";
    static final String ERROR_CODE = "Значения сообщения установлены не по отношению к российскому рублю";
    static final String SUCCESS_IMPORT = "Успешный обмен данными с КСШ. Загружено %d курсов справочника «%s».";
    static final String FAIL_IMPORT = "Неуспешная попытка обмена данными с КСШ. %s.";
	static final String FAIL_IMPORT_DELIVERY_COUNT = "Неуспешная попытка обмена данными с КСШ. %s. Попытка № %s";
    static final String ERROR_AUDIT = "Ошибка записи в журнал аудита.";
    static final String ERROR_COUNT = "Превышено максимальное число попыток загрузки сообщения (" + MAX_DELIVERY_COUNT + ").";

    @Autowired
	private RefBookScriptingService refBookScriptingService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService taUserService;

    // Маппинг атрибута "OperName" из файла → Id справочника
    private static Map<String, Long> rateMapping = new HashMap() {{
        put("Currency", 22L);
        put("Metal", 90L);
    }};

    // Маппинг Id справочника → Название
    private static Map<Long, String> refBookNameMapping = new HashMap() {{
        put(22L, "Курсы Валют");
        put(90L, "Курсы драгоценных металлов");
    }};

    @Override
    public void onMessage(Message message) {
        TAUserInfo userInfo = getUser();

        if (message == null || !(message instanceof TextMessage)) {
            logger.error(ERROR_FORMAT);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT));
            return;
        }

        TextMessage tm = (TextMessage) message;

        try {
			int deliveryCount = tm.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount > MAX_DELIVERY_COUNT) {
                logger.error(ERROR_COUNT);
                return;
            }

            String fileText = tm.getText();
            if (fileText == null) {
                logger.error(ERROR_FORMAT);
                addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT));
                return;
            }
            importRate(fileText, userInfo, deliveryCount);
        } catch (Exception ex) {
            logger.error("Ошибка при получении сообщения: " + ex.getMessage(), ex);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT));
        }
    }

    private TAUserInfo getUser() {
        if (taUserService != null) {
            return taUserService.getSystemUserInfo();
        }
        return new TAUserInfo();
    }

    /**
     * Импорт курсов из сообщения
     */
    private void importRate(final String fileText, final TAUserInfo userInfo, int deliveryCount) throws Exception {
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
                logger.error(ERROR_RATE);
                addLog(userInfo, String.format(FAIL_IMPORT, ERROR_RATE));
                return;
            }
            runScript(refBookId[0], fileText, userInfo, deliveryCount);
        } catch (Exception ex) {
            logger.error(ERROR_FORMAT, ex);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT));
        }
    }

    /**
     * Запуск скрипта
     */
    private void runScript(Long refBookId, String fileText, TAUserInfo userInfo, int deliveryCount) {
        Logger logger = new Logger();
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        ScriptStatusHolder scriptStatusHolder = new ScriptStatusHolder();
        try {
            additionalParameters.put("inputStream", new ByteArrayInputStream(fileText.getBytes(RATE_ENCODING)));
            additionalParameters.put("scriptStatusHolder", scriptStatusHolder);
            refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, additionalParameters);
        } catch (ServiceLoggerException e) {
            logger.error(e);
            logger.info("uuid = " + e.getUuid());
            addLog(userInfo, String.format(FAIL_IMPORT_DELIVERY_COUNT, e.getMessage(), deliveryCount));
            return;
        } catch (Exception e) {
            logger.error(e);
            addLog(userInfo, String.format(FAIL_IMPORT_DELIVERY_COUNT, e.getMessage(), deliveryCount));
            return;
        }
        addLog(userInfo, String.format(SUCCESS_IMPORT, scriptStatusHolder.getSuccessCount(), refBookNameMapping.get(refBookId)));
    }

    /**
     * Запись в журнал аудита
     *
     * @param userInfo
     * @param msg
     */
    private void addLog(TAUserInfo userInfo, String msg) {
        // TODO Добавить признак ошибки, когда будет поддержка в ЖА
        try {
            // Ошибка записи в журнал аудита не должна откатывать импорт
            if (auditService != null) {
                auditService.add(FormDataEvent.IMPORT, userInfo, 0, null, null, null, null, msg);
            }
        } catch (Exception e) {
            logger.error(ERROR_AUDIT, e);
        }
    }
}
