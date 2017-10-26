package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new MoveCreatedToAccepted(this).run()

@TypeChecked
class MoveCreatedToAccepted extends AbstractScriptClass {

    DeclarationData declarationData

    MoveCreatedToAccepted() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    MoveCreatedToAccepted(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
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
