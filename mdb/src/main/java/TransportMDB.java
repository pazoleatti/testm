import com.aplana.sbrf.taxaccounting.service.MappingService;
import com.aplana.sbrf.taxaccounting.service.TransportInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/transportQueue")})
@Interceptors(TransportInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TransportMDB implements MessageListener {

    @Autowired
    MappingService mappingService;

    public static final String FILENAME_PROPERTY_NAME = "FILENAME";
    public static final String DATA_PROPERTY_NAME = "DATA";

    private static final Log logger = LogFactory.getLog(TransportMDB.class);

    @Override
    public void onMessage(Message message) {
        if (message == null || !(message instanceof MapMessage)) {
            return;
        }

        MapMessage mm = (MapMessage) message;

        try {
            String fileName = mm.getString(FILENAME_PROPERTY_NAME);
            byte[] bodyFile = mm.getBytes(DATA_PROPERTY_NAME);
            logger.debug("fileName = " + fileName);
            logger.debug("bodyFile.length = " + bodyFile.length);

            mappingService.addFormData(fileName, bodyFile);
        } catch (Exception e) {
            logger.error("Ошибка при получении сообщения: " + e.getMessage(), e);
        }
    }
}
