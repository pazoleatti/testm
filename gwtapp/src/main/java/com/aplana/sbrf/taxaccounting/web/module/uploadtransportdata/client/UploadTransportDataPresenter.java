package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload.FileUploadHandler;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.GetDepartmentsResult;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;
import java.util.Set;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public class UploadTransportDataPresenter extends Presenter<UploadTransportDataPresenter.MyView,
        UploadTransportDataPresenter.MyProxy> implements UploadTransportDataUiHandlers {

    private final DispatchAsync dispatcher;

    @ProxyCodeSplit
    @NameToken(UploadTransportDataTokens.uploadTransportData)
    public interface MyProxy extends ProxyPlace<UploadTransportDataPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<UploadTransportDataUiHandlers> {
        void setFileUploadHandler(FileUploadHandler fileUploadHandler);
        void setDepartments(List<Department> departments, Set<Integer> avalableDepartments, Integer defaultDepartmentId);
        void setCanChooseDepartment(boolean canChooseDepartment);
    }

    @Inject
    public UploadTransportDataPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                        DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        getView().setFileUploadHandler(new FileUploadHandler() {
            @Override
            public void onSuccess() {
                Dialog.infoMessage("Загрузка транспортных файлов в каталог загрузки", "Загрузка транспортных файлов в каталог загрузки завершена");
            }

            @Override
            public void onFailure() {
                Dialog.errorMessage("Загрузка транспортных файлов в каталог загрузки", "Транспортные файлы не загружены в каталог загрузки. Обратитесь к администратору!");
            }
        });
    }

    @Override
    public void onStartLoad(StartLoadFileEvent event) {
        LogCleanEvent.fire(this);
        LockInteractionEvent.fire(this, true);
    }

    @Override
    public void onEndLoad(EndLoadFileEvent event) {
        LockInteractionEvent.fire(this, false);
        if (event.getUuid() != null) {
            LogAddEvent.fire(UploadTransportDataPresenter.this, event.getUuid());
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        GetDepartmentsAction action = new GetDepartmentsAction();
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentsResult>() {
                    @Override
                    public void onSuccess(GetDepartmentsResult result) {
                        getView().setCanChooseDepartment(result.isCanChooseDepartment());
                        getView().setDepartments(result.getDepartments(), result.getAvailableDepartments(), result.getDefaultDepartmentId());
                    }
                }, this));
    }
}