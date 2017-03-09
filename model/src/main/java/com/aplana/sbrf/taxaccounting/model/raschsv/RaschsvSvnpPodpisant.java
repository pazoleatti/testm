package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ (СвНП и Подписант)
 */
public class RaschsvSvnpPodpisant extends IdentityObject<Long> {

    private Long declarationDataId;
    private String svnpOkved;
    private String svnpTlph;
    private String svnpNaimOrg;
    private String svnpInnyl;
    private String svnpKpp;
    private String svnpSvReorgForm;
    private String svnpSvReorgInnyl;
    private String svnpSvReorgKpp;
    private String familia;
    private String imya;
    private String otchestvo;
    private String podpisantPrPodp;
    private String podpisantNaimDoc;
    private String podpisantNaimOrg;
    private Integer nomKorr;

    public static final String SEQ = "seq_raschsv_svnp_podpisant";
    public static final String TABLE_NAME = "raschsv_svnp_podpisant";
    public static final String COL_ID = "id";
    public static final String COL_DECLARATION_DATA_ID = "declaration_data_id";
    public static final String COL_SVNP_OKVED = "svnp_okved";
    public static final String COL_SVNP_TLPH = "svnp_tlph";
    public static final String COL_SVNP_NAIM_ORG = "svnp_naim_org";
    public static final String COL_SVNP_INNYL = "svnp_innyl";
    public static final String COL_SVNP_KPP = "svnp_kpp";
    public static final String COL_SVNP_SV_REORG_FORM = "svnp_sv_reorg_form";
    public static final String COL_SVNP_SV_REORG_INNYL = "svnp_sv_reorg_innyl";
    public static final String COL_SVNP_SV_REORG_KPP = "svnp_sv_reorg_kpp";
    public static final String COL_FAMILIA = "familia";
    public static final String COL_IMYA = "imya";
    public static final String COL_OTCHESTVO = "otchestvo";
    public static final String COL_PODPISANT_PR_PODP = "podpisant_pr_podp";
    public static final String COL_PODPISANT_NAIM_DOC = "podpisant_naim_doc";
    public static final String COL_PODPISANT_NAIM_ORG = "podpisant_naim_org";
    public static final String COL_NOM_KORR = "nom_korr";

    public static final String[] COLUMNS = {COL_ID, COL_DECLARATION_DATA_ID, COL_SVNP_OKVED, COL_SVNP_TLPH, COL_SVNP_NAIM_ORG,
            COL_SVNP_INNYL, COL_SVNP_KPP, COL_SVNP_SV_REORG_FORM, COL_SVNP_SV_REORG_INNYL, COL_SVNP_SV_REORG_KPP,
            COL_FAMILIA, COL_IMYA, COL_OTCHESTVO, COL_PODPISANT_PR_PODP, COL_PODPISANT_NAIM_DOC, COL_PODPISANT_NAIM_ORG, COL_NOM_KORR};

    public Long getDeclarationDataId() {
        return declarationDataId;
    }
    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getSvnpOkved() {
        return svnpOkved;
    }
    public void setSvnpOkved(String svnpOkved) {
        this.svnpOkved = svnpOkved;
    }

    public String getSvnpTlph() {
        return svnpTlph;
    }
    public void setSvnpTlph(String svnpTlph) {
        this.svnpTlph = svnpTlph;
    }

    public String getSvnpNaimOrg() {
        return svnpNaimOrg;
    }
    public void setSvnpNaimOrg(String svnpNaimOrg) {
        this.svnpNaimOrg = svnpNaimOrg;
    }

    public String getSvnpInnyl() {
        return svnpInnyl;
    }
    public void setSvnpInnyl(String svnpInnyl) {
        this.svnpInnyl = svnpInnyl;
    }

    public String getSvnpKpp() {
        return svnpKpp;
    }
    public void setSvnpKpp(String svnpKpp) {
        this.svnpKpp = svnpKpp;
    }

    public String getSvnpSvReorgForm() {
        return svnpSvReorgForm;
    }
    public void setSvnpSvReorgForm(String svnpSvReorgForm) {
        this.svnpSvReorgForm = svnpSvReorgForm;
    }

    public String getSvnpSvReorgInnyl() {
        return svnpSvReorgInnyl;
    }
    public void setSvnpSvReorgInnyl(String svnpSvReorgInnyl) {
        this.svnpSvReorgInnyl = svnpSvReorgInnyl;
    }

    public String getSvnpSvReorgKpp() {
        return svnpSvReorgKpp;
    }
    public void setSvnpSvReorgKpp(String svnpSvReorgKpp) {
        this.svnpSvReorgKpp = svnpSvReorgKpp;
    }

    public String getFamilia() {
        return familia;
    }
    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getImya() {
        return imya;
    }
    public void setImya(String imya) {
        this.imya = imya;
    }

    public String getOtchestvo() {
        return otchestvo;
    }
    public void setOtchestvo(String otchestvo) {
        this.otchestvo = otchestvo;
    }

    public String getPodpisantPrPodp() {
        return podpisantPrPodp;
    }
    public void setPodpisantPrPodp(String podpisantPrPodp) {
        this.podpisantPrPodp = podpisantPrPodp;
    }

    public String getPodpisantNaimDoc() {
        return podpisantNaimDoc;
    }
    public void setPodpisantNaimDoc(String podpisantNaimDoc) {
        this.podpisantNaimDoc = podpisantNaimDoc;
    }

    public String getPodpisantNaimOrg() {
        return podpisantNaimOrg;
    }
    public void setPodpisantNaimOrg(String podpisantNaimOrg) {
        this.podpisantNaimOrg = podpisantNaimOrg;
    }

    public Integer getNomKorr() {
        return nomKorr;
    }
    public void setNomKorr(Integer nomKorr) {
        this.nomKorr = nomKorr;
    }
}
