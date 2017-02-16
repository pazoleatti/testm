package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeclarationTemplateContent {
    @XmlElement
    private List<DeclarationSubreportContent> subreports;

    public List<DeclarationSubreportContent> getSubreports() {
        return subreports;
    }

    public void setSubreports(List<DeclarationSubreportContent> subreports) {
        this.subreports = subreports;
    }

    public void fillDeclarationTemplateContent(DeclarationTemplate declarationTemplate) {
        this.subreports = new ArrayList<DeclarationSubreportContent>();
        for (DeclarationSubreport declarationSubreport: declarationTemplate.getSubreports()) {
            DeclarationSubreportContent declarationSubreportContent = new DeclarationSubreportContent();
            declarationSubreportContent.setAlias(declarationSubreport.getAlias());
            declarationSubreportContent.setName(declarationSubreport.getName());
            declarationSubreportContent.setOrder(declarationSubreport.getOrder());
            declarationSubreportContent.setBlobDataId(declarationSubreport.getBlobDataId());
            declarationSubreportContent.setSelectRecord(declarationSubreport.isSelectRecord());
            declarationSubreportContent.setSubreportParams(new ArrayList<DeclarationSubreportParamContent>());
            for(DeclarationSubreportParam declarationSubreportParam: declarationSubreport.getDeclarationSubreportParams()) {
                DeclarationSubreportParamContent declarationSubreportParamContent = new DeclarationSubreportParamContent();
                declarationSubreportParamContent.setAlias(declarationSubreportParam.getAlias());
                declarationSubreportParamContent.setName(declarationSubreportParam.getName());
                declarationSubreportParamContent.setFilter(declarationSubreportParam.getFilter());
                declarationSubreportParamContent.setRefBookAttributeId(declarationSubreportParam.getRefBookAttributeId());
                declarationSubreportParamContent.setOrder(declarationSubreportParam.getOrder());
                declarationSubreportParamContent.setType(declarationSubreportParam.getType());
                declarationSubreportParamContent.setRequired(declarationSubreportParam.isRequired());
                declarationSubreportContent.getSubreportParams().add(declarationSubreportParamContent);
            }

            this.subreports.add(declarationSubreportContent);
        }
    }

    public void fillDeclarationTemplate(DeclarationTemplate declarationTemplate) {
        declarationTemplate.getSubreports().clear();
        for (DeclarationSubreportContent declarationSubreportContent: this.subreports) {
            DeclarationSubreport declarationSubreport = new DeclarationSubreport();
            declarationSubreport.setAlias(declarationSubreportContent.getAlias());
            declarationSubreport.setName(declarationSubreportContent.getName());
            declarationSubreport.setOrder(declarationSubreportContent.getOrder());
            declarationSubreport.setBlobDataId(declarationSubreportContent.getBlobDataId());
            declarationSubreport.setSelectRecord(declarationSubreportContent.isSelectRecord());
            declarationSubreport.setDeclarationSubreportParams(new ArrayList<DeclarationSubreportParam>());
            for(DeclarationSubreportParamContent declarationSubreportParamContent: declarationSubreportContent.getSubreportParams()) {
                DeclarationSubreportParam declarationSubreportParam = new DeclarationSubreportParam();
                declarationSubreportParam.setAlias(declarationSubreportParamContent.getAlias());
                declarationSubreportParam.setName(declarationSubreportParamContent.getName());
                declarationSubreportParam.setFilter(declarationSubreportParamContent.getFilter());
                declarationSubreportParam.setRefBookAttributeId(declarationSubreportParamContent.getRefBookAttributeId());
                declarationSubreportParam.setOrder(declarationSubreportParamContent.getOrder());
                declarationSubreportParam.setType(declarationSubreportParamContent.getType());
                declarationSubreportParam.setRequired(declarationSubreportParamContent.isRequired());
                declarationSubreport.getDeclarationSubreportParams().add(declarationSubreportParam);
            }
            declarationTemplate.getSubreports().add(declarationSubreport);
        }
    }
}
