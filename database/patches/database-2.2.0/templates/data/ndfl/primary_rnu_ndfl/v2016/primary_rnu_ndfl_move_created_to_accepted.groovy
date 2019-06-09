package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new MoveCreatedToAccepted(this).run()

@TypeChecked
class MoveCreatedToAccepted extends AbstractScriptClass {

    DeclarationData declarationData
    NdflPersonService ndflPersonService

    private MoveCreatedToAccepted() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    MoveCreatedToAccepted(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
                checkAccept()
                declarationService.check(logger, declarationData.id, userInfo, null)
        }
    }

    /**
     * Проверки наличия данных при принятии
     * @return
     */
    def checkAccept() {
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        if (ndflPersonList.isEmpty()) {
            logger.error("Консолидированная форма не содержит данных, принятие формы невозможно")
        }
    }
}
