package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog.RenameDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog.RenameDialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm.RefBookVersionView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class RefBookDataModule extends AbstractPresenterModule {
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

        // Для скрипта
        bindPresenter(RefBookScriptPresenter.class, RefBookScriptPresenter.MyView.class,
                RefBookScriptView.class, RefBookScriptPresenter.MyProxy.class);
    }
}
