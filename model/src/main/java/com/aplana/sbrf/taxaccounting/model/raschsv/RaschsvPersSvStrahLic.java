package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Персонифицированные сведения о застрахованных лицах
 */
public class RaschsvPersSvStrahLic extends IdentityObject<Long> {

    private Long declarationDataId;
    private Integer nomKorr;
    private String period;
    private String otchetGod;
    private Integer nomer;
    private Date svData;
    private String innfl;
    private String snils;
    private Date dataRozd;
    private String grazd;
    private String pol;
    private String kodVidDoc;
    private String serNomDoc;
    private String prizOps;
    private String prizOms;
    private String prizOss;
    private String familia;
    private String imya;
    private String otchestvo;

    // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица
    private List<RaschsvSvVypl> raschsvSvVyplList;

    public RaschsvPersSvStrahLic() {
        super();
        raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
    }

    public static final String SEQ = "seq_raschsv_pers_sv_strah_lic";
    public static final String TABLE_NAME = "raschsv_pers_sv_strah_lic";
    public static final String COL_ID = "id";
    public static final String COL_DECLARATION_DATA_ID = "declaration_data_id";
    public static final String COL_NOM_KORR = "nom_korr";
    public static final String COL_PERIOD = "period";
    public static final String COL_OTCHET_GOD = "otchet_god";
    public static final String COL_NOMER = "nomer";
    public static final String COL_SV_DATA = "sv_data";
    public static final String COL_INNFL = "innfl";
    public static final String COL_SNILS = "snils";
    public static final String COL_DATA_ROZD = "data_rozd";
    public static final String COL_GRAZD = "grazd";
    public static final String COL_POL = "pol";
    public static final String COL_KOD_VID_DOC = "kod_vid_doc";
    public static final String COL_SER_NOM_DOC = "ser_nom_doc";
    public static final String COL_PRIZ_OPS = "priz_ops";
    public static final String COL_PRIZ_OMS = "priz_oms";
    public static final String COL_PRIZ_OSS = "priz_oss";
    public static final String COL_FAMILIA = "familia";
    public static final String COL_IMYA = "imya";
    public static final String COL_OTCHESTVO = "otchestvo";

    public Long getDeclarationDataId() { return declarationDataId; }
    public void setDeclarationDataId(Long declarationDataId) { this.declarationDataId = declarationDataId; }

    public Integer getNomKorr() { return nomKorr; }
    public void setNomKorr(Integer nomKorr) { this.nomKorr = nomKorr; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getOtchetGod() { return otchetGod; }
    public void setOtchetGod(String otchetGod) { this.otchetGod = otchetGod; }

    public Integer getNomer() { return nomer; }
    public void setNomer(Integer nomer) { this.nomer = nomer; }

    public Date getSvData() { return svData; }
    public void setSvData(Date svData) { this.svData = svData; }

    public String getInnfl() { return innfl; }
    public void setInnfl(String innfl) { this.innfl = innfl; }

    public String getSnils() { return snils; }
    public void setSnils(String snils) { this.snils = snils; }

    public Date getDataRozd() { return dataRozd; }
    public void setDataRozd(Date dataRozd) { this.dataRozd = dataRozd; }

    public String getGrazd() { return grazd; }
    public void setGrazd(String grazd) { this.grazd = grazd; }

    public String getPol() { return pol; }
    public void setPol(String pol) { this.pol = pol; }

    public String getKodVidDoc() { return kodVidDoc; }
    public void setKodVidDoc(String kodVidDoc) { this.kodVidDoc = kodVidDoc; }

    public String getSerNomDoc() { return serNomDoc; }
    public void setSerNomDoc(String serNomDoc) { this.serNomDoc = serNomDoc; }

    public String getPrizOps() { return prizOps; }
    public void setPrizOps(String prizOps) { this.prizOps = prizOps; }

    public String getPrizOms() { return prizOms; }
    public void setPrizOms(String prizOms) { this.prizOms = prizOms; }

    public String getPrizOss() { return prizOss; }
    public void setPrizOss(String prizOss) { this.prizOss = prizOss; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    public String getImya() { return imya; }
    public void setImya(String imya) { this.imya = imya; }

    public String getOtchestvo() { return otchestvo; }
    public void setOtchestvo(String otchestvo) { this.otchestvo = otchestvo; }

    public List<RaschsvSvVypl> getRaschsvSvVyplList() {
        return raschsvSvVyplList != null ? raschsvSvVyplList : Collections.<RaschsvSvVypl>emptyList();
    }
    public void setRaschsvSvVyplList(List<RaschsvSvVypl> raschsvSvVyplList) { this.raschsvSvVyplList = raschsvSvVyplList; }
}
