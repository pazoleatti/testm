package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
/*
@RunWith(JukitoRunner.class)
public class RefBookLinearPresenterTest {
    @Inject
    private RefBookLinearPresenter refBookLinearPresenter;
    @Inject
    private RefBookDataPresenter refBookDataPresenter;
    @Inject
    private EditFormPresenter editFormPresenter;

    @Test
    public void testOnAddRowClicked(RefBookLinearPresenter.MyView myView) {
        refBookDataPresenter.prepareFromRequest(new PlaceRequest.Builder().nameToken(RefBookDataTokens.REFBOOK_HIER_DATA).with("id", "71").build());
        refBookDataPresenter.setInSlot(RefBookDataPresenter.TYPE_mainFormPresenter, refBookLinearPresenter);
        refBookDataPresenter.onAddRowClicked();
        verify(myView, times(1)).updateMode(FormMode.CREATE);
    }


    @Test
    public void testOnAllVersionsClickNonVersioned(EditFormPresenter.MyView editView) {
        refBookDataPresenter.prepareFromRequest(new PlaceRequest.Builder().nameToken(RefBookDataTokens.REFBOOK_HIER_DATA).with("id", "71").build());
        refBookDataPresenter.setInSlot(RefBookDataPresenter.TYPE_mainFormPresenter, refBookLinearPresenter);
        refBookDataPresenter.setInSlot(RefBookDataPresenter.TYPE_editFormPresenter, editFormPresenter);
        editFormPresenter.setVersionMode(true);
        verify(editView, times(0)).getClickAllVersion();
    }
}
*/