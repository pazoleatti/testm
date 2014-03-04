package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;
import java.util.Set;

/**
 * Хэндлеры для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public interface TaxFormNominationUiHandlers extends UiHandlers {
    void getTaxFormKind();

    void reloadFormTableData();
    void reloadDeclarationTableData();

    void save(Set<Long> ids);

    void onClickOpenFormDestinations();
    void onClickEditFormDestinations(List<FormTypeKind> formTypeKinds);

    void onClickOpenDeclarationDestinations();

	void onClickDeclarationCancelAnchor();

    /**
     * Удалить назначенные формы
     */
	void onClickFormCancelAnchor();

    void onFormRangeChange(int start, int length);

    void onDeclarationRangeChange(int start, int length);
}
