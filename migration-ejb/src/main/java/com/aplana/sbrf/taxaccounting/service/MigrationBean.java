package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.MigrationSendResult;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.*;
import javax.jms.Queue;
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
public class MigrationBean implements MessageService {

    private final Log logger = LogFactory.getLog(getClass());
    private static final String FILENAME_PROPERTY_NAME = "FILENAME";
    private static final String DATA_PROPERTY_NAME = "DATA";

    @Resource(name = "jms/transportConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/transportQueue")
    private Queue queue;

    // РНУ для миграции
//    private static long[] rnus = {25L, 26L, 27L, 31L, 51L, 53L, 54L, 59L, 60L, 64L};

    // Формат РНУ
    private static Set<Integer> RNU_SET = new HashSet<Integer>(Arrays.asList(25, 26, 27, 31));

    // Формат XML
    private static Set<Integer> XML_SET = new HashSet<Integer>(Arrays.asList(51, 53, 54, 59, 60, 64));

    @Autowired
    private MigrationDao migrationDao;

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;

    private RnuMigrationGenerator rnuGenerator = new RnuMigrationGenerator();
    private XmlMigrationGenerator xmlGenerator = new XmlMigrationGenerator();

    private List<Exemplar> getExemplarsByRnuType(long rnuTypeId, String yearSeq) {
        return migrationDao.getExemplarByRnuType(rnuTypeId, yearSeq);
    }

    private List<Exemplar> getExemplarsByRnuTypes(long[] rnuIds, long[] years) {
        List<Exemplar> rnuList = new ArrayList<Exemplar>();
        String yearSeq = Arrays.toString(years);
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

        logger.debug("Count of examples = " + list.size());
        int count = 0;

        for (Exemplar ex : list) {
            logger.debug("Start forming file. ExemplarId = " + ex.getExemplarId());
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

                logger.debug("Stop forming file. ExemplarId = " + ex.getExemplarId() + ", Filename: " + filename);
            } catch (Exception e) {
                logger.error("Ошибка подготовки файла (ExemplarId = " + ex.getExemplarId() + ", Ошибка: " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * Отправка файла JMS-сообщением
     */
    private boolean sendFile(String name, byte[] fileBytes) {
        try {
            for (int i = 0; i < 60; i++) {
                MapMessage map = session.createMapMessage();
                map.setString(FILENAME_PROPERTY_NAME, name);
                map.setBytes(DATA_PROPERTY_NAME, fileBytes);
                messageProducer.send(map);
                logger.debug("File [name=" + name + ", size=" + fileBytes.length + "] is sended.");
            }
            return true;
        } catch (JMSException e) {
            logger.error("Ошибка отправки файла JMS-сообщением. " + e.getMessage(), e);
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
        if (migrationDao == null) {
            // В dev-mode попадем сюда, это нормально
            throw new ServiceException("В Dev-mode не реализована отправка JMS-сообщений.");
        }
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(queue);

            result.setExemplarList(getExemplarsByRnuTypes(rnus, year));
            result.setSendFilesCount(startSendFiles(rnus, year));

            messageProducer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            logger.error("Ошибка подготовки JMS. " + e.getMessage(), e);
            return result;
        }
        return result;
    }
}