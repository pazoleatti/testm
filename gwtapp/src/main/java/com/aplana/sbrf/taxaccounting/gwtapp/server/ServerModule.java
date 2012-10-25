package com.aplana.sbrf.taxaccounting.gwtapp.server;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SendTextToServer;
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
	public SendTextToServerHandler getSendTextToServerHandler() {
		return new SendTextToServerHandler();
	}
	
	@Bean
	public GetFormDataListHandler getGetFormDataListHandler() {
		return new GetFormDataListHandler();
	}
	
	@Bean
	public GetFormDataHandler getGetFormDataHandler() {
		return new GetFormDataHandler();
	}	

	@Bean
	public ActionValidator getDefaultActionValidator() {
		return new DefaultActionValidator();
	}

    @Bean
    public SaveDataHandler getSaveDataHandler(){
        return new SaveDataHandler();
    }

	protected void configureHandlers() {
		bindHandler(SendTextToServer.class, SendTextToServerHandler.class);
		bindHandler(GetFormDataList.class, GetFormDataListHandler.class);
		bindHandler(GetFormData.class, GetFormDataHandler.class);
        bindHandler(SaveDataAction.class, SaveDataHandler.class);
	}
}
