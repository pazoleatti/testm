package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;
import java.util.List;

public class GetLogAction extends UnsecuredActionImpl<GetLogResult> {

    private List<GetLogAction.PairLogLevelMessage> messages;

    public List<PairLogLevelMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<PairLogLevelMessage> messages) {
        this.messages = messages;
    }

    public enum LogLevel {
        INFO, WARN, ERROR
    }

    public static class PairLogLevelMessage implements Serializable {

        private LogLevel logLevel;

        private String message;

        public PairLogLevelMessage() {
        }

        public PairLogLevelMessage(LogLevel logLevel, String message) {
            this.logLevel = logLevel;
            this.message = message;
        }

        public LogLevel getLogLevel() {
            return logLevel;
        }

        public String getMessage() {
            return message;
        }
    }
}
