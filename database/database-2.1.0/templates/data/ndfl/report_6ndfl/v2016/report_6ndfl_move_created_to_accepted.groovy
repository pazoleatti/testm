package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new MoveCreatedToAccepted(this).run()

@TypeChecked
class MoveCreatedToAccepted extends AbstractScriptClass {

    DeclarationData declarationData
    DeclarationService declarationService

    MoveCreatedToAccepted() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    MoveCreatedToAccepted(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
                declarationService.check(logger, declarationData.id, userInfo, null)
        }
    }
}

