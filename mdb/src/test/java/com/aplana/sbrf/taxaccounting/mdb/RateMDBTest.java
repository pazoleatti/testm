package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;

import static org.mockito.Mockito.mock;

public class RateMDBTest {

    private RateMDB rmdb;

    @Before
    public void init() {
        rmdb = new RateMDB();
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        ReflectionTestUtils.setField(rmdb, "refBookScriptingService", refBookScriptingService);
    }

    private class TextMessageImpl implements TextMessage {
        private String text;

        public TextMessageImpl(String text) {
            this.text = text;
        }

        @Override
        public void setText(String string) throws JMSException {
        }

        @Override
        public String getText() throws JMSException {
            return text;
        }

        @Override
        public String getJMSMessageID() throws JMSException {
            return null;
        }

        @Override
        public void setJMSMessageID(String id) throws JMSException {
        }

        @Override
        public long getJMSTimestamp() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSTimestamp(long timestamp) throws JMSException {
        }

        @Override
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
            return new byte[0];
        }

        @Override
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        }

        @Override
        public void setJMSCorrelationID(String correlationID) throws JMSException {
        }

        @Override
        public String getJMSCorrelationID() throws JMSException {
            return null;
        }

        @Override
        public Destination getJMSReplyTo() throws JMSException {
            return null;
        }

        @Override
        public void setJMSReplyTo(Destination replyTo) throws JMSException {
        }

        @Override
        public Destination getJMSDestination() throws JMSException {
            return null;
        }

        @Override
        public void setJMSDestination(Destination destination) throws JMSException {
        }

        @Override
        public int getJMSDeliveryMode() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        }

        @Override
        public boolean getJMSRedelivered() throws JMSException {
            return false;
        }

        @Override
        public void setJMSRedelivered(boolean redelivered) throws JMSException {
        }

        @Override
        public String getJMSType() throws JMSException {
            return null;
        }

        @Override
        public void setJMSType(String type) throws JMSException {
        }

        @Override
        public long getJMSExpiration() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSExpiration(long expiration) throws JMSException {
        }

        @Override
        public int getJMSPriority() throws JMSException {
            return 0;
        }

        @Override
        public void setJMSPriority(int priority) throws JMSException {
        }

        @Override
        public void clearProperties() throws JMSException {
        }

        @Override
        public boolean propertyExists(String name) throws JMSException {
            return false;
        }

        @Override
        public boolean getBooleanProperty(String name) throws JMSException {
            return false;
        }

        @Override
        public byte getByteProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public short getShortProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public int getIntProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public long getLongProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public float getFloatProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public double getDoubleProperty(String name) throws JMSException {
            return 0;
        }

        @Override
        public String getStringProperty(String name) throws JMSException {
            return null;
        }

        @Override
        public Object getObjectProperty(String name) throws JMSException {
            return null;
        }

        @Override
        public Enumeration getPropertyNames() throws JMSException {
            return null;
        }

        @Override
        public void setBooleanProperty(String name, boolean value) throws JMSException {
        }

        @Override
        public void setByteProperty(String name, byte value) throws JMSException {
        }

        @Override
        public void setShortProperty(String name, short value) throws JMSException {
        }

        @Override
        public void setIntProperty(String name, int value) throws JMSException {
        }

        @Override
        public void setLongProperty(String name, long value) throws JMSException {
        }

        @Override
        public void setFloatProperty(String name, float value) throws JMSException {
        }

        @Override
        public void setDoubleProperty(String name, double value) throws JMSException {
        }

        @Override
        public void setStringProperty(String name, String value) throws JMSException {
        }

        @Override
        public void setObjectProperty(String name, Object value) throws JMSException {
        }

        @Override
        public void acknowledge() throws JMSException {
        }

        @Override
        public void clearBody() throws JMSException {
        }
    };

    @Test
    public void onMessageTest1() throws Exception {
        StringWriter output = new StringWriter();
        IOUtils.copy(getCurrencyRateStream(), output);
        rmdb.onMessage(new TextMessageImpl(output.toString()));
    }

    @Test
    public void onMessageTest2() throws Exception {
        StringWriter output = new StringWriter();
        IOUtils.copy(getMetalRateStream(), output);
        rmdb.onMessage(new TextMessageImpl(output.toString()));
    }

//    @Test
//    public void onMessageTest3() throws Exception {
//        rmdb.onMessage(new TextMessageImpl("Bad123"));
//    }

    private static InputStream getCurrencyRateStream() {
        return RateMDBTest.class.getResourceAsStream("public-currency_1.xml");
    }

    private static InputStream getMetalRateStream() {
        return RateMDBTest.class.getResourceAsStream("public-currency_5.xml");
    }
}
