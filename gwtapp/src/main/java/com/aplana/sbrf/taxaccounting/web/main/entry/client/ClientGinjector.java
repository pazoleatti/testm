package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutGinjector;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminGinjector;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorGinjector;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataGinjector;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListGinjector;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuClientGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInClientGinjector;
import com.google.gwt.inject.client.GinModules;

@GinModules({ ClientModule.class })
public interface ClientGinjector extends ClientGinjectorBase,
		SignInClientGinjector, 
		FormDataListGinjector, 
		FormDataGinjector,
		HomeGinjector, 
		AboutGinjector,
		MainMenuClientGinjector,
        AdminGinjector,
        ErrorGinjector
{

}