package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookHierPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.google.inject.Inject;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class RefBookHierDataPresenterTest {
    @Inject
    private RefBookHierPresenter refBookHierPresenter;
    @Inject
    private RefBookHierDataPresenter refBookHierDataPresenter;

    @Test
    public void testOnAddRowClicked(RefBookHierDataPresenter.MyView myView) {
        refBookHierPresenter.setInSlot(RefBookHierPresenter.TYPE_editFormPresenter, refBookHierDataPresenter);
        refBookHierPresenter.onAddRowClicked();
        verify(myView, atLeastOnce()).updateMode(FormMode.CREATE);
    }
}