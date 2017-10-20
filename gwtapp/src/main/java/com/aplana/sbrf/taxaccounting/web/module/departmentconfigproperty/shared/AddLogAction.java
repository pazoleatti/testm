package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class AddLogAction extends UnsecuredActionImpl<AddLogResult> {
    String oldUUID;
    List<GWTLogEntry> messages;

    public List<GWTLogEntry> getMessages() {
        return messages;
    }

    public void setMessages(List<GWTLogEntry> messages) {
        this.messages = messages;
    }

    public String getOldUUID() {
        return oldUUID;
    }

    public void setOldUUID(String oldUUID) {
        this.oldUUID = oldUUID;
    }
}
