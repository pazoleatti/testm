package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * Презентер для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationPresenter
        extends Presenter<TaxFormNominationPresenter.MyView, TaxFormNominationPresenter.MyProxy>
        implements TaxFormNominationUiHandlers {

    @ProxyCodeSplit
    @NameToken(TaxFormNominationToken.taxFormNomination)
    public interface MyProxy extends ProxyPlace<TaxFormNominationPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaxFormNominationUiHandlers> {
        void init(Boolean isForm);
        boolean isForm();
        void setTaxFormKind(List<FormType> formTypes);
        void setTableData(List<FormTypeKind> departmentFormTypes);
    }

    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        // Проверка прав доступа
        dispatcher.execute(
                new DetectUserRoleAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<DetectUserRoleResult>() {
                            @Override
                            public void onSuccess(DetectUserRoleResult result) {
                                if (!isControl(result.getUserRole())) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }
                                // С правами доступа всё окей
                                getView().init(Boolean.valueOf(request.getParameter("isForm", "")));
                                getProxy().manualReveal(TaxFormNominationPresenter.this);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                getProxy().manualRevealFailed();
                            }
                        }, this));

        // TODO реализовать анлоки (пока не надо)
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    /**
     * Контролер
     *
     * @param userRoles
     * @return Да/Нет
     */
    private boolean isControl(List<TARole> userRoles) {
        // TODO вопрос в аналитике (УВиСАС), нужно ли сюда добавить администратора
        if (userRoles != null) {
            for (TARole taRole : userRoles) {
                if (taRole.getAlias().equals(TARole.ROLE_CONTROL_UNP) || taRole.getAlias().equals(TARole.ROLE_CONTROL)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Перезагруска бокса "Вид налоговой формы"/"Вид декларации"
     * @param taxType
     */
    @Override
    public void getTaxFormKind(TaxType taxType) {
        GetTaxFormTypesAction action = new GetTaxFormTypesAction();
        action.setTaxType(taxType);
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxFormTypesResult>() {
                    @Override
                    public void onSuccess(GetTaxFormTypesResult result) {
                        getView().setTaxFormKind(result.getFormTypeList());
                    }
                }, this));
    }

    /**
     * Перезагрузка таблицы
     * @param depoId идентификатор подразделения
     * @param taxTypeCode код вида налога
     */
    @Override
    public void getTableData(Long depoId, char taxTypeCode) {
        GetTableDataAction action = new GetTableDataAction();
        action.setDepoId(depoId);
        action.setTaxType(taxTypeCode);
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setTableData(result.getTableData());
                    }
                }, this).addCallback(new ManualRevealCallback<GetTableDataResult>(TaxFormNominationPresenter.this)));
    }

}
