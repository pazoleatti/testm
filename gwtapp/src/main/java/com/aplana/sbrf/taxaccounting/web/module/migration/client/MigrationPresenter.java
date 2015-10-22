package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationAction;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Presenter для формы "Миграция исторических данных"
 *
 * @author Dmitriy Levykin
 */
public class MigrationPresenter extends Presenter<MigrationPresenter.MyView,
        MigrationPresenter.MyProxy> implements MigrationUiHandlers {

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    @ProxyCodeSplit
    @NameToken(MigrationTokens.migration)
    public interface MyProxy extends ProxyPlace<MigrationPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<MigrationUiHandlers> {
        void setResult(String result);

        List<Long> getRnus();

        List<Long> getYears();
    }

    @Inject
    public MigrationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                              DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
    }

    @Override
    public void runImport() {
        final List<Long> rnus = getView().getRnus();
        final List<Long> years = getView().getYears();

        final String start = dateTimeFormat.format(new Date());

        if (!rnus.isEmpty() && !years.isEmpty()) {
            dispatcher.execute(new MigrationAction(toLongs(rnus), toLongs(years)), CallbackUtils
                    .defaultCallback(new AbstractCallback<MigrationResult>() {
                        @Override
                        public void onSuccess(MigrationResult result) {
                            String msg = start + " - время начала операции." + "\n";
                            msg += "Выбранные годы РНУ: " + Arrays.toString(years.toArray()).replace("[", "").replace("]", "") + "\n";
                            msg += "Выбранные виды РНУ: " + Arrays.toString(rnus.toArray()).replace("[", "").replace("]", "") + "\n";
                            if (result.getExemplarList() != null) {
                                msg += "Актуальных экземпляров найдено: " + result.getExemplarList().size() + "\n";
                                msg += "Отправлено экземпляров: " + result.getSendFilesCount() + "\n";
                            } else {
                                msg += "Ошибка списка экземпляров\n";
                            }
                            msg += dateTimeFormat.format(new Date()) + " - время окончания операции.";
                            getView().setResult(msg);
                        }
                    }, this));
        } else {
            getView().setResult("Должно быть выбрано хотя бы один год и один вид РНУ.");
        }
    }

    private long[] toLongs(List<Long> longs) {
        long[] longs1 = new long[longs.size()];
        int i = 0;
        for (Long aLong : longs) {
            longs1[i] = aLong;
            i++;
        }
        return longs1;

    }
}