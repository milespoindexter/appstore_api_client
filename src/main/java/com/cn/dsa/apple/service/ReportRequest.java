package com.cn.dsa.apple.service;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import com.cn.dsa.apple.AppStoreClient;

@XmlRootElement(name="reportRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder={"requestType", "reportDate", "startRow", "limit"})
public class ReportRequest {

    private String requestType = AppStoreClient.DAILY_REPORT;
    private String reportDate = null;
    private int startRow = 0;
    private int limit = 20; //default

    //constructor
    public ReportRequest() {
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    @XmlElement(name="requestType", required=true)
    public String getRequestType() {
        return requestType;
    }   

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }
    @XmlElement(name="reportDate", required=true)
    public String getReportDate() {
        return reportDate;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    @XmlElement(name="startRow", required=false)
    public int getStartRow() {
        return startRow;
    }  

    public void setLimit(int limit) {
        this.limit = limit;
    }
    @XmlElement(name="limit", required=false)
    public int getLimit() {
        return limit;
    }  
}