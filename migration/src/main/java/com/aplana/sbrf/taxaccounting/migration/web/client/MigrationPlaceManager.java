package com.aplana.sbrf.taxaccounting.migration.web.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * @author Alexander Ivanov
 */
public class MigrationPlaceManager extends PlaceManagerImpl{
    @Inject
    public MigrationPlaceManager(
            EventBus eventBus,
            TokenFormatter tokenFormatter ) {
        super(eventBus, tokenFormatter);
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(new PlaceRequest("main"), false );
    }
}
