package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.testutils.DispatchAsyncStub;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(JukitoRunner.class)
public class EditFormPresenterTest {
    @Inject
    private EditFormPresenter presenter;
    @Inject
    private DepartmentEditPresenter departmentEditPresenter;

    private static final long REF_BOOK_DEPARTMENTS_ID = 30L;
    private static final long REF_BOOK_SOME_ID = 1L;

    @Test
    public void testOnCancelClickedWhenDepartments(EditFormPresenter.MyView myView) {
        presenter.init(REF_BOOK_SOME_ID, false);
        presenter.onCancelClicked();
        verify(myView, never()).updateMode(FormMode.EDIT);
    }

    @Test
    public void testOnCancelClickedWhenNotDepartments(EditFormPresenter.MyView myView) {
        presenter.init(REF_BOOK_SOME_ID, false);
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
        presenter.init(REF_BOOK_DEPARTMENTS_ID, false);
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(1000L);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(ids);
        result.setCheckRegion(true);

        DispatchAsyncStub.callSuccessWith(result)
                .when(dispatchAsync)
                .execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());

        when(myView.checkCorrectnessForSave()).thenReturn(true);
        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());
        //verify(myView).updateMode(FormMode.EDIT);
        /*assertNotNull(presenter.currentUniqueRecordId);
        assertTrue(presenter.currentUniqueRecordId == 1000);*/
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
        presenter.init(REF_BOOK_SOME_ID, false);
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(1000L);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(ids);
        result.setCheckRegion(true);

        DispatchAsyncStub.callSuccessWith(result)
                .when(dispatchAsync)
                .execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());

        when(myView.checkCorrectnessForSave()).thenReturn(true);
        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());
        verify(myView, never()).updateMode(FormMode.EDIT);
        /*assertNotNull(presenter.currentUniqueRecordId);
        assertTrue(presenter.currentUniqueRecordId == 1000);*/
    }

    /**
     * Обновление элемента справочника
     *
     * @param dispatchAsync
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnSaveClickedWhenUpdate(DispatchAsync dispatchAsync, EditFormPresenter.MyView myView) {
        presenter.init(REF_BOOK_SOME_ID, false);
        when(myView.checkCorrectnessForSave()).thenReturn(true);
        presenter.onSaveClicked(false);
        verify(dispatchAsync, atLeastOnce()).execute((SaveRefBookRowVersionAction) any(), (AsyncCallback<SaveRefBookRowVersionResult>) any());
    }

    @Test
    public void depEditPresenterWithoutRepeatedTest(DispatchAsync dispatchAsync){
        departmentEditPresenter.init(REF_BOOK_DEPARTMENTS_ID, false);
        departmentEditPresenter.setPreviousURId(1l);
        departmentEditPresenter.showRecord(1l);
        verify(dispatchAsync, atMost(1)).execute((SaveRefBookRowVersionAction) any(), Mockito.<AsyncCallback<SaveRefBookRowVersionResult>>any());
    }
}