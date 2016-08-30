package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookHierPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
/*
@RunWith(JukitoRunner.class)
public class RefBookHierPresenterTest {
    @Inject
    private RefBookHierPresenter refBookHierPresenter;
    @Inject
    private RefBookHierDataPresenter refBookHierDataPresenter;

    @Test
    public void testOnAddRowClicked(RefBookHierDataPresenter.MyView myView) {
        refBookHierPresenter.prepareFromRequest(new PlaceRequest.Builder().nameToken(RefBookDataTokens.REFBOOK_HIER_DATA).with("id", "71").build());
        refBookHierPresenter.setInSlot(RefBookHierPresenter.TYPE_mainFormPresenter, refBookHierDataPresenter);
        refBookHierPresenter.onAddRowClicked();
        verify(myView, times(1)).updateMode(FormMode.CREATE);
    }

    @Test
    public void testOnDeleteClicked(RefBookHierDataPresenter.MyView myView, EditFormPresenter.MyView editView) {
        refBookHierPresenter.prepareFromRequest(new PlaceRequest.Builder().nameToken(RefBookDataTokens.REFBOOK_HIER_DATA).with("id", "71").build());
        refBookHierPresenter.setInSlot(RefBookHierPresenter.TYPE_mainFormPresenter, refBookHierDataPresenter);
        refBookHierPresenter.onDeleteRowClicked();
        verify(myView, never()).updateMode(FormMode.EDIT);
    }
}
*/
