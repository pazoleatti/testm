package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.service.MappingService;
import com.aplana.sbrf.taxaccounting.service.TransportInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * MDB-модуль для импорта справочников
 *
 * @author Dmitriy Levykin
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/transportQueue")})
@Interceptors(TransportInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // Rollback во внешних сервисах не будет отмечать сообщение как необработанное из-за ошибки
public class TransportMDB implements MessageListener {

    @Autowired
    MappingService mappingService;

    public static final String FILENAME_PROPERTY_NAME = "FILENAME";
    public static final String DATA_PROPERTY_NAME = "DATA";

    private static final Log LOG = LogFactory.getLog(TransportMDB.class);

    @Override
    // @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
        if (!(message instanceof MapMessage)) {
            return;
        }

        MapMessage mm = (MapMessage) message;

        try {
            String fileName = mm.getString(FILENAME_PROPERTY_NAME);
            byte[] bodyFile = mm.getBytes(DATA_PROPERTY_NAME);
            LOG.debug("fileName = " + fileName);
            LOG.debug("bodyFile.length = " + bodyFile.length);

            mappingService.addFormData(fileName, bodyFile);
        } catch (Exception e) {
            LOG.error("Ошибка при получении сообщения: " + e.getMessage(), e);
        }
    }
}
