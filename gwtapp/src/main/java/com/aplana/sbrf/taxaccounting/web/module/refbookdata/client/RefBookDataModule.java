package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.TaPlaceManagerImpl;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionView;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class RefBookDataModule extends AbstractPresenterModule {
    /**Идентификаторы справочников, которые не версионируются*/
    public static final Long[] NOT_VERSIONED_REF_BOOK_IDS = new Long[]{30l};

    @Override
    protected void configure() {

        // создаем отдельный презентор что бы не смешивать логику загрузки и обработки данных при разных типах справочниках
        // для иерархических справочников
        bindPresenter(RefBookHierDataPresenter.class, RefBookHierDataPresenter.MyView.class,
                RefBookHierDataView.class, RefBookHierDataPresenter.MyProxy.class);
        // для линейных справочников
        bindPresenter(RefBookDataPresenter.class, RefBookDataPresenter.MyView.class,
                RefBookDataView.class, RefBookDataPresenter.MyProxy.class);

        // общие виджеты
        bindPresenter(RefBookVersionPresenter.class, RefBookVersionPresenter.MyView.class,
                RefBookVersionView.class, RefBookVersionPresenter.MyProxy.class);
        bindSingletonPresenterWidget(EditFormPresenter.class, EditFormPresenter.MyView.class, EditFormView.class);
        bindPresenterWidget(RenameDialogPresenter.class, RenameDialogPresenter.MyView.class, RenameDialogView.class);
        bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);

        // Для скрипта
        bindPresenter(RefBookScriptPresenter.class, RefBookScriptPresenter.MyView.class,
                RefBookScriptView.class, RefBookScriptPresenter.MyProxy.class);

        bind(TaPlaceManager.class).to(TaPlaceManagerImpl.class).in(Singleton.class);
    }
}
