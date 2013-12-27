package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetLogEntriesAction extends UnsecuredActionImpl<GetLogEntriesResult> {
    private String uuid;
    private int start = 0;
    private int length = 50;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
