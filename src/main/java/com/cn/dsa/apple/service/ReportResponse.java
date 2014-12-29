package com.cn.dsa.apple.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "reportResponse")
@XmlType(propOrder={"success", "responseMsg", "report"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ReportResponse extends ReportRequest {

    public ReportResponse() {
        super();
    }

    public ReportResponse(ReportRequest request) {
        super();
        //copy fields into this object
        setRequestType(request.getRequestType());
        setReportDate(request.getReportDate());
        setStartRow(request.getStartRow());
        setLimit(request.getLimit());
        
    }
    
    private boolean success;
    
    @XmlElement
    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    private String responseMsg;
    
    @XmlElement
    public String getResponseMsg() {
        return responseMsg;
    }
    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
    
    private List<ReportRow> report = null;
    //@XmlJavaTypeAdapter(IdListAdapter.class)
    @XmlElement(name="report", required=false)
    @XmlElementWrapper( name="reports" )
    public List<ReportRow> getReport() {
        return report;
    }
    public void setReport(List<ReportRow> report) {
        this.report = report;
    }


}