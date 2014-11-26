package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.testutils.DispatchAsyncStub;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JukitoRunner.class)
public class EditFormPresenterTest {
    @Inject
    private EditFormPresenter presenter;

    private static final long REF_BOOK_DEPARTMENTS_ID = 30L;

    @Test
    public void testOnCancelClickedWhenDepartments(EditFormPresenter.MyView myView) {
        presenter.init(REF_BOOK_DEPARTMENTS_ID, new ArrayList<RefBookColumn>());
        presenter.onCancelClicked();
        verify(myView, never()).updateMode(FormMode.EDIT);
    }

    @Test
    public void testOnCancelClickedWhenNotDepartments(EditFormPresenter.MyView myView) {
        presenter.init(REF_BOOK_DEPARTMENTS_ID, new ArrayList<RefBookColumn>());
        presenter.onCancelClicked();
        verify(myView, never()).updateMode(FormMode.EDIT);
    }

    /**
     * Добавление элемента справочника подразделений
     *
     * @param dispatchAsync диспатчер
     * @throws ActionException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnSaveClickedWhenCreateAndDepartments(DispatchAsync dispatchAsync, EditFormPresenter.MyView myView) throws ActionException {
        presenter.init(REF_BOOK_DEPARTMENTS_ID, new ArrayList<RefBookColumn>());
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(1000L);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(ids);
        result.setCheckRegion(true);

        DispatchAsyncStub.callSuccessWith(result)
                .when(dispatchAsync)
                .execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());

        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());
        //verify(myView).updateMode(FormMode.EDIT);
        assertNotNull(presenter.currentUniqueRecordId);
        assertTrue(presenter.currentUniqueRecordId == 1000);
    }

    /**
     * Добавление элемента справочника подразделений
     *
     * @param dispatchAsync
     * @throws ActionException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnSaveClickedWhenCreateAndNotDepartments(DispatchAsync dispatchAsync, EditFormPresenter.MyView myView) throws ActionException {
        presenter.init(REF_BOOK_DEPARTMENTS_ID, new ArrayList<RefBookColumn>());
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(1000L);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(ids);
        result.setCheckRegion(true);

        DispatchAsyncStub.callSuccessWith(result)
                .when(dispatchAsync)
                .execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());

        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());
        verify(myView, never()).updateMode(FormMode.EDIT);
        assertNotNull(presenter.currentUniqueRecordId);
        assertTrue(presenter.currentUniqueRecordId == 1000);
    }

    /**
     * Обновление элемента справочника
     *
     * @param dispatchAsync
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnSaveClickedWhenUpdate(DispatchAsync dispatchAsync) {
        presenter.init(REF_BOOK_DEPARTMENTS_ID, new ArrayList<RefBookColumn>());
        presenter.setCurrentUniqueRecordId(null);
        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((SaveRefBookRowVersionAction) any(), (AsyncCallback<SaveRefBookRowVersionResult>) any());
    }
}