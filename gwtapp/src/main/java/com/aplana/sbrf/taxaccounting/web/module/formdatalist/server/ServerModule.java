package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aplana.sbrf.taxaccounting.web.module.formdata.server.GetFormDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdata.server.SaveFormDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.widget.signin.server.GetUserInfoActionHandler;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.gwtplatform.dispatch.server.actionvalidator.ActionValidator;
import com.gwtplatform.dispatch.server.spring.HandlerModule;
import com.gwtplatform.dispatch.server.spring.actionvalidator.DefaultActionValidator;
import com.gwtplatform.dispatch.server.spring.configuration.DefaultModule;

@Configuration
@Import(DefaultModule.class)
public class ServerModule extends HandlerModule {

	public ServerModule() {
	}
	
	@Bean
	public GetFormDataListHandler getGetFormDataListHandler() {
		return new GetFormDataListHandler();
	}
	
	@Bean
	public ActionValidator getDefaultActionValidator() {
		return new DefaultActionValidator();
	}

	protected void configureHandlers() {
		bindHandler(GetFormDataList.class, GetFormDataListHandler.class);
		bindHandler(GetFormData.class, GetFormDataHandler.class);
        bindHandler(SaveFormDataAction.class, SaveFormDataHandler.class);
        // TODO: Убрать отсюда
        bindHandler(GetUserInfoAction.class, GetUserInfoActionHandler.class);
	}
}
