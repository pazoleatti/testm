package com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch;

import com.gwtplatform.dispatch.shared.ActionException;

public class TaActionException extends ActionException {
	private static final long serialVersionUID = 2378347325524891374L;
	
	private String uuid;
	
	private String trace;
	
	public TaActionException() {
		super();
	}

	public TaActionException(String msg) {
		super(msg);
	}
	
	public TaActionException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public TaActionException(Throwable e) {
		super(e);
	}

	public TaActionException(String msg, String trace) {
		super(msg);
		this.trace = trace;
	}
	
	public TaActionException(String msg, String trace, Throwable e) {
		super(msg, e);
		this.trace = trace;
	}

    public String getUuid() {
        return uuid;
    }

    public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
