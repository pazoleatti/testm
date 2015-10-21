package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageServiceBeanTest {

    private final String ERROR_MESSAGE = "Test error";
    private final String EXCEPTION_MESSAGE = "Test Exception";
    private final String LOGGER_EXCEPTION_MESSAGE = "Test ServiceLoggerException";
    private final String SUCCESS_IMPORT = String.format(MessageServiceBean.SUCCESS_IMPORT, StringUtils.getNumberString(1, "Загружен", "Загружено", "Загружено"),
            1, StringUtils.getNumberString(1, "курс", "курса", "курсов"), "%s");

    private MessageServiceBean rmdb;

    private List<String> logList;

    // Флаги для эмуляции ошибок в сервисе скриптов
    private boolean refBookServiceScriptException;
    private boolean refBookServiceScriptServiceLoggerException;

    @Before
    public void init() {
        rmdb = new MessageServiceBean();

        // Сброс флагов
        refBookServiceScriptException = false;
        refBookServiceScriptServiceLoggerException = false;

        // RefBookScriptingService
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        ReflectionTestUtils.setField(rmdb, "refBookScriptingService", refBookScriptingService);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (refBookServiceScriptException) {
                    throw new Exception(EXCEPTION_MESSAGE);
                }
                if (refBookServiceScriptServiceLoggerException) {
                    throw new ServiceLoggerException(LOGGER_EXCEPTION_MESSAGE, "test uuid");
                }
                Map<String, Object> map = (Map<String, Object>) invocation.getArguments()[4];
                ((ScriptStatusHolder) map.get("scriptStatusHolder")).setSuccessCount(1);
                return null;
            }
        }).when(refBookScriptingService).executeScript(any(TAUserInfo.class), anyLong(), any(FormDataEvent.class),
                any(Logger.class), anyMapOf(String.class, Object.class));

        // AuditService
        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(rmdb, "auditService", auditService);

        logList = new LinkedList<String>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String str = (String)invocation.getArguments()[7];
                logList.add(str);
                return null;
            }
        }).when(auditService).add(any(FormDataEvent.class), any(TAUserInfo.class), any(Integer.class),
                any(Integer.class), any(String.class), any(String.class), any(Integer.class), any(String.class), anyString());

        // LogEntryService
        LogEntryService logEntryService = mock(LogEntryService.class);
        when(logEntryService.save(anyListOf(LogEntry.class))).thenReturn("test-uuid");
        ReflectionTestUtils.setField(rmdb, "logEntryService", logEntryService);
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
    }

    // Успешный импорт к. валют
    @Test
    public void onMessage1Test() throws Exception {
        StringWriter output = new StringWriter();
        IOUtils.copy(getCurrencyRateStream(), output);
        rmdb.processRateMessage(new TextMessageImpl(output.toString()));
        Assert.assertEquals(1, logList.size());
        Assert.assertEquals(String.format(SUCCESS_IMPORT, "Курсы Валют"), logList.get(0));
    }

    // Успешный импорт к. драг. мет.
    @Test
    public void onMessage2Test() throws Exception {
        StringWriter output = new StringWriter();
        IOUtils.copy(getMetalRateStream(), output);
        rmdb.processRateMessage(new TextMessageImpl(output.toString()));
        Assert.assertEquals(1, logList.size());
        Assert.assertEquals(String.format(SUCCESS_IMPORT, "Курсы драгоценных металлов"), logList.get(0));
    }

    // Сообщение null — неправильный формат
    @Test
    public void nullMessageTest() {
        rmdb.processRateMessage(new TextMessageImpl(null));
        Assert.assertEquals(1, logList.size());
        Assert.assertTrue(logList.contains(String.format(MessageServiceBean.FAIL_IMPORT, MessageServiceBean.ERROR_FORMAT)));
    }

    // Неправильный формат
    @Test
    public void badFormatMessageTest() {
        rmdb.processRateMessage(new TextMessageImpl("Test ERROR_MESSAGE"));
        Assert.assertEquals(1, logList.size());
        Assert.assertTrue(logList.contains(String.format(MessageServiceBean.FAIL_IMPORT, MessageServiceBean.ERROR_FORMAT)));
    }

    // Исключение в скрипте
    @Test
    public void scriptExceptionTest() throws IOException {
        refBookServiceScriptException = true;
        StringWriter output = new StringWriter();
        IOUtils.copy(getCurrencyRateStream(), output);
        rmdb.processRateMessage(new TextMessageImpl(output.toString()));
        Assert.assertEquals(1, logList.size());
        Assert.assertTrue(logList.get(0).contains(EXCEPTION_MESSAGE));
    }

    // Исключение ServiceLoggerException в скрипте
    @Test
    public void scriptServiceLoggerExceptionTest() throws IOException {
        refBookServiceScriptServiceLoggerException = true;
        StringWriter output = new StringWriter();
        IOUtils.copy(getCurrencyRateStream(), output);
        rmdb.processRateMessage(new TextMessageImpl(output.toString()));
        Assert.assertEquals(1, logList.size());
        Assert.assertTrue(logList.get(0).contains(LOGGER_EXCEPTION_MESSAGE));
    }

    private static InputStream getCurrencyRateStream() {
        return MessageServiceBean.class.getResourceAsStream("public-currency_1.xml");
    }

    private static InputStream getMetalRateStream() {
        return MessageServiceBean.class.getResourceAsStream("public-currency_5.xml");
    }
}
