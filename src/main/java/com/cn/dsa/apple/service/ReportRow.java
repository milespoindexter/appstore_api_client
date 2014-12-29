package com.cn.dsa.apple.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import com.cn.dsa.common.PropMapAdapter;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "reportRow")
/*
@XmlType(propOrder={    "provider",
                        "providerCountry",
                        "sku",
                        "developer",
                        "title",
                        "version",
                        "productTypeId",
                        "units",
                        "developerProceeds",

                    })
*/
public class ReportRow {

    private static final String PROVIDER = "Provider";
    private static final String PROVIDER_COUNTRY = "Provider Country";
    private static final String SKU = "SKU";
    private static final String DEVELOPER = "Developer";
    private static final String TITLE = "Title";
    private static final String VERSION = "Version";
    private static final String PRODUCT_TYPE_ID = "Product Type Identifier";
    private static final String UNITS = "Units";
    private static final String DEVELOPER_PROCEEDS = "Developer Proceeds";
    private static final String CUSTOMER_CURRENCY = "Customer Currency";
    private static final String COUNTRY_CODE = "Country Code";
    private static final String CURRENCY_OF_PROCEEDS = "Currency of Proceeds";
    private static final String APPLE_ID = "Apple Identifier";
    private static final String CUSTOMER_PRICE = "Customer Price";
    private static final String PROMO_CODE = "Promo Code";
    private static final String PARENT_ID = "Parent Identifier";
    private static final String SUBSCRIPTION = "Subscription";
    private static final String PERIOD = "Period";
    private static final String DOWNLOAD_DATE_PST = "Download Date (PST)";
    private static final String CUSTOMER_IDENTIFIER = "Customer Identifier";
    private static final String REPORT_DATE_LOCAL = "Report Date (Local)";
    private static final String SALES_RETURN = "Sales/Return";
    private static final String CATEGORY = "Category";

    private static final String REPORT_TYPE = "reportType";
    private static final String REPORT_DATE = "reportDate";
    private static final String DATE_STR = "dateStr";

    private Map<String,String> props = new HashMap<String,String>();
    
    //constructors
    public ReportRow() {
        super();
    }

    public ReportRow(Map<String,Object> fields) {
        super();
        setProps(fields);
    }

    public void setProps(Map<String, Object> map) {
        //this.props = props;
        for (Map.Entry<String, Object> set : map.entrySet()) {
            String key = set.getKey();
            if(!key.equalsIgnoreCase("_id")) { //skip MongoDB IDs
                Object val = set.getValue();
                if(val instanceof java.lang.String) {
                    props.put(key, (String)val);
                    //System.out.println("adding prop: "+key+" = "+val);
                }
                else {
                    props.put(key, val.toString());
                }
                
                
            }
            
        }
    }
    @XmlJavaTypeAdapter(PropMapAdapter.class)
    @XmlElement(nillable=true, name="props")
    public Map getProps() {
        return props;
    }

   
    /******* Measures *******
    @XmlElement
    public String getTotalUniqueVisitors() {
        return (String)map.get(ParamMgr.MEAS_TUV.toLowerCase());
    }
    @XmlElement
    public String getAvgMinutesPerVisit() {
        return (String)map.get(ParamMgr.MEAS_AVG_MIN_VISITS.toLowerCase());
    }

    @XmlElement
    public String getAvgMinutesPerVisitor() {
        return (String)map.get(ParamMgr.MEAS_AVG_MIN_VISITOR.toLowerCase());
    }
    @XmlElement
    public String getAvgVisitsPerVisitor() {
        return (String)map.get(ParamMgr.MEAS_AVG_VISIT_VISITOR.toLowerCase());
    }
    @XmlElement
    public String getTotalVisits() {
        return (String)map.get(ParamMgr.MEAS_TV.toLowerCase());
    }
    
    @XmlTransient
    public Map<String,Object> getMap() {
        return map;
    }
    */
}