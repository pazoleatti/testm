package com.aplana.sbrf.taxaccounting.migration.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

/**
 * @author Alexander Ivanov
 */
public class MigrationModule implements EntryPoint {
    public final MyGinjector ginjector = GWT.create(MyGinjector.class);

    @Override
    public void onModuleLoad() {
        DelayedBindRegistry.bind(ginjector);
        ginjector.getPlaceManager().revealCurrentPlace();
    }

}
