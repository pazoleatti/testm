package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.io.Serializable;
import java.util.Date;

public class NotificationTableRow implements Serializable {
	private static final long serialVersionUID = 4077680439794472365L;

    private Long id;
    private Date date;
    private String msg;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
