package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.gwtplatform.mvp.client.proxy.PlaceManager;

public interface TaPlaceManager extends PlaceManager {

	/**
	 * Тихая навигация на предыдущий historyToken без вызова ValueChangeEvent
	 * (т.е. приложение будет игнорировать эту команду)
	 */
	void navigateBackQuietly();

}
