package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.gwtplatform.mvp.client.UiHandlers;

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

    void onClickOpenDeclarationDestinations();
}
