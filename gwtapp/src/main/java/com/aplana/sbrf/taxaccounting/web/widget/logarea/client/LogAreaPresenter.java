package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import java.util.ArrayList;
import java.util.List;



import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class LogAreaPresenter extends
		PresenterWidget<LogAreaPresenter.MyView> implements
		LogAddEvent.MyHandler, LogCleanEvent.MyHandler, LogAreaUiHandlers {


	public static interface MyView extends View, HasUiHandlers<LogAreaUiHandlers>{

		void setLogEntries(List<LogEntry> entries);
		void setLogSize(int full, int error, int warn, int info);

	}

	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

    private static final String etab =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	@Inject
	public LogAreaPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(LogAddEvent.getType(), this);
		addRegisteredHandler(LogCleanEvent.getType(), this);
	}

	@Override
	public void onLogAdd(LogAddEvent event) {
		if (event.getLogEntries()!=null){
			logEntries.addAll(event.getLogEntries());
		}
		updateView();

		if (!logEntries.isEmpty()){
			LogShowEvent.fire(this, true);
		}
	}

	@Override
	public void onLogClean(LogCleanEvent event) {
		logEntries.clear();
		updateView();
	}

	private void updateView(){
		getView().setLogEntries(logEntries);
		int error = 0, warn = 0, info = 0;
		for (LogEntry logEntry : logEntries) {
			switch (logEntry.getLevel()) {
			case ERROR:
				error++;
				break;
			case WARNING:
				warn++;
				break;
			case INFO:
				info++;
			}
		}
		getView().setLogSize(error + warn, error, warn, info);
	}

	@Override
	public String print() {
		
		JSONObject requestJSON = new JSONObject();
		JSONArray jArr = new JSONArray();

		for(int i = 0; i < logEntries.size() ; i++){
			JSONObject jObj = new JSONObject();
			jObj.put("errorCode", new JSONString(logEntries.get(i).getLevel().name().toString()));
			jObj.put("message", new JSONString(logEntries.get(i).getMessage()));
			jArr.set(i, jObj);
		}
		requestJSON.put("listLogEntries", jArr);

        //additional screening because we have two daserialization, first when write a string, second on server side
		return requestJSON.toString().replace("\\\"","\\\\\"");
		// TODO: SBRFACCTAX-2494
	}

    /*private static String encode(String data) {
        StringBuffer out = new StringBuffer();

        int i = 0;
        int r = data.length();
        while (r > 0) {
            byte d0, d1, d2;
            byte e0, e1, e2, e3;

            d0 = (byte) data.charAt(i++); --r;
            e0 = (byte) (d0 >>> 2);
            e1 = (byte) ((d0 & 0x03) << 4);

            if (r > 0) {
                d1 = (byte) data.charAt(i++); --r;
                e1 += (byte) (d1 >>> 4);
                e2 = (byte) ((d1 & 0x0f) << 2);
            }
            else {
                e2 = 64;
            }

            if (r > 0) {
                d2 = (byte) data.charAt(i++); --r;
                e2 += (byte) (d2 >>> 6);
                e3 = (byte) (d2 & 0x3f);
            }
            else {
                e3 = 64;
            }
            out.append(etab.charAt(e0));
            out.append(etab.charAt(e1));
            out.append(etab.charAt(e2));
            out.append(etab.charAt(e3));
        }

        return out.toString();
    }

    private String utf8EnCode(String str){
        str.replace("/\\r\\n/g","\\n");

        return null;

    } */

	@Override
	public void clean() {
		logEntries.clear();
		updateView();
	}

	@Override
	public void hide() {
		LogShowEvent.fire(this, false);
	}

}
