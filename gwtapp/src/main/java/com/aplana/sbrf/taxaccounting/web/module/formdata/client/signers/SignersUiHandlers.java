package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;


import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface SignersUiHandlers extends UiHandlers {
	void onSave(FormDataPerformer performer, List<FormDataSigner> signers);
}
