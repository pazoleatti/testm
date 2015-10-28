package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.MigrationSendResult;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.*;
import javax.jms.Queue;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * EJB-модуль миграции
 *
 * @author Dmitriy Levykin
 */
@Stateless
@Local(MessageServiceLocal.class)
@Interceptors(MigrationInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MessageServiceBean implements MessageService {

	private static final Log LOG = LogFactory.getLog(MessageServiceBean.class);
    private static final String FILENAME_PROPERTY_NAME = "FILENAME";
    private static final String DATA_PROPERTY_NAME = "DATA";

    @Resource(name = "jms/transportConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(name = "jms/rateQueueConnectionFactory")
    private QueueConnectionFactory queueConnectionFactory;
    @Resource(name = "jms/transportQueue")
    private Queue queue;
    @Resource(name = "jms/rateQueue")
    private Queue rateQueue;

    // Формат РНУ
    private static final Set<Integer> RNU_SET = new HashSet<Integer>(Arrays.asList(25, 26, 27, 31));
    // Формат XML
    private static final Set<Integer> XML_SET = new HashSet<Integer>(Arrays.asList(51, 53, 54, 59, 60, 64));

    /** Параметры КСШ */
    private static final String EMPTY_QUERY = "Обмен данными с КСШ завершен. Сообщения в КСШ отсутствуют";
	static final String ERROR_FORMAT = "Сообщение не соответствует заданному формату";
	private static final String ERROR_RATE = "Сообщение не соответствует передаче данных по курсам валют / драгоценным металлам";
	private static final String ERROR_PUBLIC = "Сообщение не содержит публичные курсы";
	private static final String ERROR_VALUE = "Сообщение не содержит значений";
	private static final String ERROR_CODE = "Значения сообщения установлены не по отношению к российскому рублю";
    static final String EMPTY_IMPORT = "Обмен данными с КСШ завершен (справочник «%s»). Создано 0 записей справочника";
	static final String SUCCESS_IMPORT = "Обмен данными с КСШ завершен (справочник «%s»). Создано %d из %s записей справочника";
	static final String FAIL_IMPORT = "При обмене данными с КСШ произошла ошибка. %s.";
	private static final String FAIL_IMPORT_DELIVERY_COUNT = "При обмене данными с КСШ произошла ошибка. %s. Попытка № %s";
	private static final String ERROR_AUDIT = "Ошибка записи в журнал аудита.";
    // Максимальное число попыток загрузки одного сообщения
    private static final int MAX_DELIVERY_COUNT = 10;
	private static final String ERROR_COUNT = "Превышено максимальное число попыток загрузки сообщения (" + MAX_DELIVERY_COUNT + ").";
    private static final String RATE_ENCODING = "UTF-8";

	private Session session;
	private MessageProducer messageProducer;

	private RnuMigrationGenerator rnuGenerator = new RnuMigrationGenerator();
	private XmlMigrationGenerator xmlGenerator = new XmlMigrationGenerator();

	@Autowired
	private MigrationDao migrationDao;
	@Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private LogEntryService logEntryService;

    // Маппинг атрибута "OperName" из файла -> Id справочника
    private static final Map<String, Long> rateMapping = new HashMap<String, Long>() {{
        put("Currency", 22L);
        put("Metal", 90L);
    }};

    // Маппинг Id справочника → Название
    private static final Map<Long, String> refBookNameMapping = new HashMap<Long, String>() {{
        put(22L, "Курсы Валют");
        put(90L, "Курсы драгоценных металлов");
    }};

    private List<Exemplar> getExemplarsByRnuType(long rnuTypeId, String yearSeq) {
        return migrationDao.getExemplarByRnuType(rnuTypeId, yearSeq);
    }

    private List<Exemplar> getExemplarsByRnuTypes(long[] rnuIds, long[] years) {
        List<Exemplar> rnuList = new ArrayList<Exemplar>();
        String yearSeq = Arrays.toString(years).replace("[", "").replace("]", "");
        for (long rnu : rnuIds) {
            rnuList.addAll(getExemplarsByRnuType(rnu, yearSeq));
        }
        return rnuList;
    }

    /**
     * Отправка группы файлов
     *
     * @param rnuIds список видов РНУ
     * @param years  года
     * @return количество отосланныхфайлов
     */
    private int startSendFiles(long[] rnuIds, long[] years) {

        List<Exemplar> list = getExemplarsByRnuTypes(rnuIds, years);

		LOG.debug("Count of examples = " + list.size());
        int count = 0;

        for (Exemplar ex : list) {
			LOG.debug("Start forming file. ExemplarId = " + ex.getExemplarId());
            try {
                String filename = null;
                byte[] fileBytes = null;
                if (RNU_SET.contains(ex.getRnuTypeId())) {
                    filename = rnuGenerator.getRnuFileName(ex);
                    fileBytes = rnuGenerator.generateRnuFileToBytes(ex, getRnuList(ex));
                } else if (XML_SET.contains(ex.getRnuTypeId())) {
                    filename = xmlGenerator.getXmlFileName(ex);
                    fileBytes = xmlGenerator.generateXmlFileToBytes(ex, getRnuList(ex));
                }

                if (sendFile(filename, fileBytes)) {
                    count++;
                }

				LOG.debug("Stop forming file. ExemplarId = " + ex.getExemplarId() + ", Filename: " + filename);
            } catch (Exception e) {
				LOG.error("Ошибка подготовки файла (ExemplarId = " + ex.getExemplarId() + ", Ошибка: " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * Отправка файла JMS-сообщением
     */
    private boolean sendFile(String name, byte[] fileBytes) {
        try {
            MapMessage map = session.createMapMessage();
            map.setString(FILENAME_PROPERTY_NAME, name);
            map.setBytes(DATA_PROPERTY_NAME, fileBytes);
            messageProducer.send(map);
			LOG.debug("File [name=" + name + ", size=" + fileBytes.length + "] is sended.");
            return true;
        } catch (JMSException e) {
			LOG.error("Ошибка отправки файла JMS-сообщением. " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Список РНУ по экземпляру
     *
     * @param ex актуальный экземпляр формы РНУ
     * @return
     */
    private List<? extends AbstractRnuRow> getRnuList(Exemplar ex) {
        List<? extends AbstractRnuRow> list = new ArrayList<AbstractRnuRow>();
        switch (NalogFormType.getById(ex.getRnuTypeId())) {
            case RNU25:
                list = migrationDao.getRnu25RowList(ex);
                break;
            case RNU26:
                list = migrationDao.getRnu26RowList(ex);
                break;
            case RNU27:
                list = migrationDao.getRnu27RowList(ex);
                break;
            case RNU31:
                list = migrationDao.getRnu31RowList(ex);
                break;
            case RNU51:
                list = migrationDao.getRnu51RowList(ex);
                break;
            case RNU53:
                list = migrationDao.getRnu53RowList(ex);
                break;
            case RNU54:
                list = migrationDao.getRnu54RowList(ex);
                break;
            case RNU59:
                list = migrationDao.getRnu59RowList(ex);
                break;
            case RNU60:
                list = migrationDao.getRnu60RowList(ex);
                break;
            case RNU64:
                list = migrationDao.getRnu64RowList(ex);
                break;
        }
        return list;
    }

    @Override
    public MigrationSendResult sendFiles(long[] rnus, long[] year) {
        MigrationSendResult result = new MigrationSendResult();
        Connection connection;
        if (migrationDao == null) {
            // В dev-mode попадем сюда, это нормально
            throw new ServiceException("В Dev-mode не реализована отправка JMS-сообщений.");
        }
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(queue);
			try {
            	result.setExemplarList(getExemplarsByRnuTypes(rnus, year));
            	result.setSendFilesCount(startSendFiles(rnus, year));
			} finally {
				messageProducer.close();
				session.close();
				connection.close();
			}
        } catch (JMSException e) {
			LOG.error("Ошибка подготовки JMS. " + e.getMessage(), e);
            return result;
        }
        return result;
    }

    @Override
    public void getRateMessages() {
        QueueConnection queueConnection = null;
        TAUserInfo userInfo = getUser();
        try {
            /** Подключение к очереди */
			LOG.info("Подключение к КСШ");
            queueConnection = queueConnectionFactory.createQueueConnection();
            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueReceiver queueReceiver = queueSession.createReceiver(rateQueue);
            queueConnection.start();

            /** Обработка сообщений */
            boolean load = false;
            Message message;
            while ((message = queueReceiver.receive(1)) != null) {
                load = true;
                try {
                    processRateMessage(message);
                } catch (Exception e) {
					LOG.error("Произошла ошибка при обработке сообщения, оно будет пропущено", e);
                }
            }
            if (!load) {
                addLog(userInfo, EMPTY_QUERY, null);
            }
            queueReceiver.close();
            queueSession.close();
			LOG.info("Данные КСШ обработаны");
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            }
        }
    }

        public void processRateMessage(Message message){
        TAUserInfo userInfo = getUser();

        if (!(message instanceof TextMessage)) {
			LOG.error(ERROR_FORMAT);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT), null);
            return;
        }

        TextMessage tm = (TextMessage) message;

        try {
            int deliveryCount = tm.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount > MAX_DELIVERY_COUNT) {
				LOG.error(ERROR_COUNT);
                return;
            }

            String fileText = tm.getText();
            if (fileText == null) {
				LOG.error(ERROR_FORMAT);
                addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT), null);
                return;
            }
            importRate(fileText, userInfo, deliveryCount);
        } catch (Exception ex) {
			LOG.error("Ошибка при получении сообщения: " + ex.getMessage(), ex);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT), null);
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
     *
     * @param fileText
     * @param userInfo
     */
    private void importRate(final String fileText, final TAUserInfo userInfo, int deliveryCount) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        final Long[] refBookId = new Long[1];

        DefaultHandler handler = new DefaultHandler() {

            private boolean bSendRateRq;
            private boolean bOperName;
            private boolean bExRateBlock;
            private boolean bExRateType;
            private boolean bExRateDetails;
            private boolean bRateParamType;
            private boolean bCcy;
            private boolean bCode;
            private String operName;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
				LOG.error(ERROR_RATE);
                addLog(userInfo, String.format(FAIL_IMPORT, ERROR_RATE), null);
                return;
            }
            runScript(refBookId[0], fileText, userInfo, deliveryCount);
        } catch (ServiceException ex1) {
            LOG.error(ERROR_FORMAT, ex1);
            addLog(userInfo, String.format(FAIL_IMPORT, ex1.getMessage()), null);
        } catch (Exception ex2) {
			LOG.error(ERROR_FORMAT, ex2);
            addLog(userInfo, String.format(FAIL_IMPORT, ERROR_FORMAT), null);
        }
    }

    /**
     * Запуск скрипта
     */
    private void runScript(Long refBookId, String fileText, TAUserInfo userInfo, int deliveryCount) {
        Logger logger = new Logger();
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        ScriptStatusHolder scriptStatusHolder = new ScriptStatusHolder();
        Logger localLogger = new Logger();
        try {
            additionalParameters.put("inputStream", new ByteArrayInputStream(fileText.getBytes(RATE_ENCODING)));
            additionalParameters.put("scriptStatusHolder", scriptStatusHolder);
            refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, localLogger, additionalParameters);
        } catch (ServiceLoggerException e) {
            logger.error(e);
            logger.info("uuid = " + e.getUuid());
            addLog(userInfo, String.format(FAIL_IMPORT_DELIVERY_COUNT, e.getMessage(), deliveryCount), e.getUuid());
            return;
        } catch (Exception e) {
            logger.error(e);
            addLog(userInfo, String.format(FAIL_IMPORT_DELIVERY_COUNT, e.getMessage(), deliveryCount), logEntryService.save(logger.getEntries()));
            return;
        }
        int successCount = scriptStatusHolder.getSuccessCount();
        int totalCount = scriptStatusHolder.getTotalCount();
        if (totalCount == 0) {
            String msg = String.format(EMPTY_IMPORT,
                    refBookNameMapping.get(refBookId));
            logger.info(msg);
            logger.getEntries().addAll(localLogger.getEntries());
            addLog(userInfo, msg, logEntryService.save(logger.getEntries()));
        } else {
            String msg = String.format(EMPTY_IMPORT,
                    refBookNameMapping.get(refBookId));
            logger.info(msg);
            logger.getEntries().addAll(localLogger.getEntries());
            addLog(userInfo, String.format(SUCCESS_IMPORT,
                    refBookNameMapping.get(refBookId),
                    successCount,
                    totalCount), logEntryService.save(logger.getEntries()));
        }
    }

    /**
     * Запись в журнал аудита
     *
     * @param userInfo
     * @param msg
     */
    private void addLog(TAUserInfo userInfo, String msg, String uuid) {
        // TODO Добавить признак ошибки, когда будет поддержка в ЖА
        try {
            // Ошибка записи в журнал аудита не должна откатывать импорт
            if (auditService != null) {
                auditService.add(FormDataEvent.IMPORT, userInfo, 0, null, null, null, null, msg, uuid);
            }
        } catch (Exception e) {
            LOG.error(ERROR_AUDIT, e);
        }
    }
}