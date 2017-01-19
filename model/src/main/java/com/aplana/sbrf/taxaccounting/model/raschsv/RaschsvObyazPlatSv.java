package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Сводные данные об обязательствах плательщика страховых взносов (ОбязПлатСВ)
 */
public class RaschsvObyazPlatSv extends IdentityObject<Long> {

    private Long declarationDataId;
    private String oktmo;

    // УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО
    private List<RaschsvUplPer> raschsvUplPerList;

    // УплПревОСС
    private RaschsvUplPrevOss raschsvUplPrevOss;

    // РасчСВ_ОПС_ОМС
    private List<RaschsvSvOpsOms> raschsvSvOpsOmsList;

    // РасчСВ_ОСС.ВНМ
    private RaschsvOssVnm raschsvOssVnm;

    // РасхОССЗак
    private RaschsvRashOssZak raschsvRashOssZak;

    // ВыплФинФБ
    private RaschsvVyplFinFb raschsvVyplFinFb;

    // ПравТариф3.1.427
    private RaschsvPravTarif31427 raschsvPravTarif31427;

    // ПравТариф5.1.427
    private RaschsvPravTarif51427 raschsvPravTarif51427;

    // ПравТариф7.1.427
    private RaschsvPravTarif71427 raschsvPravTarif71427;

    // СвПримТариф9.1.427
    private RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427;

    // СвПримТариф2.2.425
    private RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425;

    // СвПримТариф1.3.422
    private RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422;

    public RaschsvObyazPlatSv() {
        super();
        raschsvUplPerList = new ArrayList<RaschsvUplPer>();
        raschsvSvOpsOmsList = new ArrayList<RaschsvSvOpsOms>();
    }

    public static final String SEQ = "seq_raschsv_obyaz_plat_sv";
    public static final String TABLE_NAME = "raschsv_obyaz_plat_sv";
    public static final String COL_ID = "id";
    public static final String COL_DECLARATION_DATA_ID = "declaration_data_id";
    public static final String COL_OKTMO = "oktmo";

    public static final String[] COLUMNS = {COL_ID, COL_DECLARATION_DATA_ID, COL_OKTMO};

    public Long getDeclarationDataId() { return declarationDataId; }
    public void setDeclarationDataId(Long declarationDataId) { this.declarationDataId = declarationDataId; }

    public String getOktmo() { return oktmo; }
    public void setOktmo(String oktmo) { this.oktmo = oktmo; }

    public List<RaschsvUplPer> getRaschsvUplPerList() {
        return raschsvUplPerList;
    }
    public void setRaschsvUplPerList(List<RaschsvUplPer> raschsvUplPerList) {
        this.raschsvUplPerList = raschsvUplPerList;
    }

    public RaschsvUplPrevOss getRaschsvUplPrevOss() {
        return raschsvUplPrevOss;
    }
    public void setRaschsvUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss) {
        this.raschsvUplPrevOss = raschsvUplPrevOss;
    }

    public List<RaschsvSvOpsOms> getRaschsvSvOpsOmsList() {
        return raschsvSvOpsOmsList;
    }
    public void setRaschsvSvOpsOmsList(List<RaschsvSvOpsOms> raschsvSvOpsOmsList) {
        this.raschsvSvOpsOmsList = raschsvSvOpsOmsList;
    }

    public RaschsvOssVnm getRaschsvOssVnm() {
        return raschsvOssVnm;
    }
    public void setRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm) {
        this.raschsvOssVnm = raschsvOssVnm;
    }

    public RaschsvRashOssZak getRaschsvRashOssZak() {
        return raschsvRashOssZak;
    }
    public void setRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak) {
        this.raschsvRashOssZak = raschsvRashOssZak;
    }

    public RaschsvVyplFinFb getRaschsvVyplFinFb() {
        return raschsvVyplFinFb;
    }
    public void setRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb) {
        this.raschsvVyplFinFb = raschsvVyplFinFb;
    }

    public RaschsvPravTarif31427 getRaschsvPravTarif31427() {
        return raschsvPravTarif31427;
    }
    public void setRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427) {
        this.raschsvPravTarif31427 = raschsvPravTarif31427;
    }

    public RaschsvPravTarif51427 getRaschsvPravTarif51427() {
        return raschsvPravTarif51427;
    }
    public void setRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427) {
        this.raschsvPravTarif51427 = raschsvPravTarif51427;
    }

    public RaschsvPravTarif71427 getRaschsvPravTarif71427() {
        return raschsvPravTarif71427;
    }
    public void setRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427) {
        this.raschsvPravTarif71427 = raschsvPravTarif71427;
    }

    public RaschsvSvPrimTarif91427 getRaschsvSvPrimTarif91427() {
        return raschsvSvPrimTarif91427;
    }
    public void setRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427) {
        this.raschsvSvPrimTarif91427 = raschsvSvPrimTarif91427;
    }

    public RaschsvSvPrimTarif22425 getRaschsvSvPrimTarif22425() {
        return raschsvSvPrimTarif22425;
    }
    public void setRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425) {
        this.raschsvSvPrimTarif22425 = raschsvSvPrimTarif22425;
    }

    public RaschsvSvPrimTarif13422 getRaschsvSvPrimTarif13422() {
        return raschsvSvPrimTarif13422;
    }
    public void setRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422) {
        this.raschsvSvPrimTarif13422 = raschsvSvPrimTarif13422;
    }
}
