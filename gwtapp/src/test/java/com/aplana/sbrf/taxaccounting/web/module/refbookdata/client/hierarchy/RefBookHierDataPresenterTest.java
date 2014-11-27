package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.google.inject.Inject;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class RefBookHierDataPresenterTest {
    @Inject
    private RefBookHierDataPresenter refBookHierDataPresenter;

    @Test
    public void testOnAddRowClicked(RefBookHierDataPresenter.MyView myView) {
        refBookHierDataPresenter.onAddRowClicked();
        verify(myView, never()).updateMode(FormMode.CREATE);
    }
}