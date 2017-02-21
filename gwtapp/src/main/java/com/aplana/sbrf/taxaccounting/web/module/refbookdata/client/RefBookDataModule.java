package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.*;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear.RefBookLinearPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear.RefBookLinearView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.person.PersonPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.person.PersonView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.script.RefBookScriptView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload.UploadDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.upload.UploadDialogView;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class RefBookDataModule extends AbstractPresenterModule {

    @Override
    protected void configure() {

        // создаем отдельный презентор что бы не смешивать логику загрузки и обработки данных при разных типах справочниках
        // для иерархических справочников
        bindPresenter(RefBookHierPresenter.class, RefBookHierPresenter.MyView.class,
                RefBookHierView.class, RefBookHierPresenter.MyProxy.class);
        // для линейных справочников
        bindPresenter(RefBookDataPresenter.class, RefBookDataPresenter.MyView.class,
                RefBookDataView.class, RefBookDataPresenter.MyProxy.class);

        // общие виджеты
        bindSingletonPresenterWidget(RefBookVersionPresenter.class, RefBookVersionPresenter.MyView.class, RefBookVersionView.class);
        bindSingletonPresenterWidget(RefBookLinearPresenter.class, RefBookLinearPresenter.MyView.class, RefBookLinearView.class);
        bindPresenterWidget(RenameDialogPresenter.class, RenameDialogPresenter.MyView.class, RenameDialogView.class);
        bindPresenterWidget(UploadDialogPresenter.class, UploadDialogPresenter.MyView.class, UploadDialogView.class);
        bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);
        bindSingletonPresenterWidget(RefBookHierDataPresenter.class, RefBookHierDataPresenter.MyView.class, RefBookHierDataView.class);
        bindSingletonPresenterWidget(PersonPresenter.class, PersonPresenter.MyView.class, PersonView.class);

        bindSingletonPresenterWidget(EditFormPresenter.class, EditFormPresenter.MyView.class, EditFormView.class);
        bindSingletonPresenterWidget(DepartmentEditPresenter.class, DepartmentEditPresenter.MyView.class, DepartmentEditView.class);
        bindSingletonPresenterWidget(HierEditPresenter.class, HierEditPresenter.MyView.class, HierEditView.class);

        // Для скрипта
        bindPresenter(RefBookScriptPresenter.class, RefBookScriptPresenter.MyView.class,
                RefBookScriptView.class, RefBookScriptPresenter.MyProxy.class);

        //bind(TaPlaceManager.class).to(TaPlaceManagerImpl.class).in(Singleton.class);
    }
}
