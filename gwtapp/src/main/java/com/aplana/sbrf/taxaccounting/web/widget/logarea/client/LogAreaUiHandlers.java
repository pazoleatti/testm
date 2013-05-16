package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import com.google.gwt.json.client.JSONObject;
import com.gwtplatform.mvp.client.UiHandlers;

public interface LogAreaUiHandlers extends UiHandlers {
	
	JSONObject print();
	
	void clean();
	
	void hide();

}
