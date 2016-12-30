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
            declarationSubreport.setDeclarationSubreportParams(new ArrayList<DeclarationSubreportParam>());
            declarationTemplate.getSubreports().add(declarationSubreport);
        }
    }
}
