package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.testutils.DispatchAsyncStubber;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.shared.ActionException;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(JukitoRunner.class)
public class EditFormPresenterTest {
    @Inject
    EditFormPresenter presenter;

    /**
     * Добавление элемента справочника
     *
     * @param dispatchAsync
     * @throws ActionException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testOnSaveClickedWhenCreate(DispatchAsync dispatchAsync) throws ActionException {
        RefBookValueSerializable refBookValueSerializable = new RefBookValueSerializable();
        refBookValueSerializable.setDateValue(new Date());

        Map<String, RefBookValueSerializable> map = new HashMap<String, RefBookValueSerializable>();
        map.put("code", refBookValueSerializable);

        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(1000L);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(ids);

        presenter.init(1L, true);
        DispatchAsyncStubber.callSuccessWith(result)
                .when(dispatchAsync)
                .execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());

        presenter.onSaveClicked();
        verify(dispatchAsync, atLeastOnce()).execute((AddRefBookRowVersionAction) any(), (AsyncCallback<AddRefBookRowVersionResult>) any());
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
        presenter.init(1L, true);
        presenter.setCurrentUniqueRecordId(1000L);
        presenter.onSaveClicked();
        verify(dispatchAsync, atLeastOnce()).execute((SaveRefBookRowVersionAction) any(), (AsyncCallback<SaveRefBookRowVersionResult>) any());
    }
}