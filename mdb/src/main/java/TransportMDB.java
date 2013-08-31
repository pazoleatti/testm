import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {@ActivationConfigProperty(
        propertyName = "destinationType",
        propertyValue = "javax.jms.Queue"
), @ActivationConfigProperty(
        propertyName = "destination",
        propertyValue = "jms/transportQueue"
)})
//@Interceptors(SpringBeanAutowiringInterceptor.class)
public class TransportMDB implements MessageListener {

    //@Autowired
    //MappingService mappingService;

    public static final String FILENAME_PROPERTY_NAME = "FILENAME";
    public static final String DATA_PROPERTY_NAME = "DATA";

    private static final Log logger = LogFactory.getLog(TransportMDB.class);

    @Override
    public void onMessage(Message message) {
        logger.debug("onMessage: " + message);
        if (message == null || !(message instanceof MapMessage)) {
            return;
        }

        MapMessage mm = (MapMessage) message;

        try {
            String fileName = mm.getString(FILENAME_PROPERTY_NAME);
            byte[] bodyFile = mm.getBytes(DATA_PROPERTY_NAME);

            logger.debug("fileName = " + fileName);
            logger.debug("bodyFile.length = " + bodyFile.length);

            // mappingService.addFormData(fileName, bodyFile);

        } catch (JMSException e) {
            logger.error("Retrieving error message: " + e.getMessage(), e);
        }
    }
}
