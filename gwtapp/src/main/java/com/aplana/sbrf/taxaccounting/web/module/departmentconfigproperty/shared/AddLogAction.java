package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class AddLogAction extends UnsecuredActionImpl<AddLogResult> {
    String oldUUID;
    List<LogEntry> messages;

    public List<LogEntry> getMessages() {
        return messages;
    }

    public void setMessages(List<LogEntry> messages) {
        this.messages = messages;
    }

    public String getOldUUID() {
        return oldUUID;
    }

    public void setOldUUID(String oldUUID) {
        this.oldUUID = oldUUID;
    }
}
