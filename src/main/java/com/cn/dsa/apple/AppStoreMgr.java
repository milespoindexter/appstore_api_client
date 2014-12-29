package com.cn.dsa.apple;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import java.net.URLEncoder;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.BufferedWriter;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import it.sauronsoftware.cron4j.Scheduler;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.WriteResult;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;

import org.json.XML;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import com.cn.dsa.common.DateUtils;
import com.cn.dsa.db.MongoMgr;

import com.cn.dsa.apple.service.ReportRequest;
import com.cn.dsa.apple.service.ReportResponse;
import com.cn.dsa.apple.service.ReportRow;

public class AppStoreMgr {
    private static String LS = System.getProperty("line.separator");
    private static Logger log = Logger.getLogger(AppStoreMgr.class.getName());

    private static final String DELIMITER = "|"; //commas don't work, since some values have commas

    private static final String REPORT_PATH = "/app/appstore/reports";
    
    private static final String ISO_FORMAT = "yyyy-MM-dd";
    public static final String FILE_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    
    private static String MONGO_DB = "apple";
    private static String MONGO_COLLECTION = "appstore";

    private AppStoreClient appStoreClient = new AppStoreClient();
    private static DateUtils dateUtils = new DateUtils();

    private Scheduler scheduler = null;
    private static int PAUSE = 10; //seconds to wait between each report request.
    
    //mongoDB objects
    private MongoMgr mongoMgr = MongoMgr.getInstance();
    private MongoClient mongoClient;
    private DB mongoDb;
    private DBCollection dbCollection;

    private String chronStr = "30 6 * * *"; //once-a-day at 6:30am

    public void setChronStr(String chronStr) {
        this.chronStr = chronStr;
    }
    public String getChronStr() {
        return chronStr;
    }

    public AppStoreMgr() {
        super();
        //trackReports();
    }

    public AppStoreMgr(String chron) {
        super();
        setChronStr(chron);
        trackReports();
    }

    public void trackReports() {
        log.info("kicking off continuous tracking of AppStore Programmatic Reports: "+getChronStr());
        // Creates a Scheduler instance.
        scheduler = new Scheduler();
        // Schedule a task.
        scheduler.schedule(getChronStr(), new Runnable() {
            public void run() {
                getAllReports();
            }
        });
        // Starts the scheduler.
        scheduler.start();

    }

    public void stop() {
        if(scheduler != null) {
            scheduler.stop();
        }
    }

    public boolean getAllReports() {
        //get today's date
        //Date reportDate = new Date();
        String reportDateStr = "2014-11-02";
        try {
            SimpleDateFormat appleFormatter = new SimpleDateFormat(ISO_FORMAT);
            Date reportDate = appleFormatter.parse(reportDateStr);

            return getAllReports(reportDate);

        }
        catch(Exception e) {
            System.err.println("exception Msg: "+e.getMessage());
        }
        
        /*
        //create date minus one day
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_YEAR,-1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR,-1);
        Date dayBeforeYesterday = cal.getTime();
        */
        //get all reports for this date range
        return false;
        
    }

    public boolean getAllReports(Date reportDate) {
        boolean totalSuccess = true;
        SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
        String dateStr = isoFormatter.format(reportDate);
        
        try {
            //for(String requestType : AppStoreClient.REPORT_TYPES) {
                //String requestType = AppStoreClient.WEEKLY_REPORT;
                String requestType = AppStoreClient.DAILY_REPORT;
                
                //first check if report is already there
                if(checkForReport(requestType, dateStr)) {
                    log.info("report found. Cancelling request for identical report.");
                    //continue;
                }

                if(!appStoreClient.getReport(requestType, reportDate)) {
                    totalSuccess = false;
                }
                else {
                    //save to file
                    String response = appStoreClient.getResponse();

                    //write report to file
                    List<String> csvReport = writeReport(response, reportDate, requestType);
                    
                    if(csvReport != null && csvReport.size() > 0) {
                        List<ReportRow> savedReport = saveReportToDB(requestType, reportDate, csvReport);
                        if(savedReport != null && savedReport.size() > 0) {
                            log.info(dateStr+" report saved to MongoDB.");
                        }
                        else {
                            log.warning("could not save "+dateStr+" report to MongoDB");
                            totalSuccess = false;
                        }
                    }
                    
                }
                log.info("Waiting "+PAUSE+" seconds before next AppStore request . . . ");
                Thread.sleep(PAUSE * 1000);
            //}
        } catch (Exception e) {
            log.warning("Error getting AppStore reports: \n"+e.getMessage());
            totalSuccess = false;
        }

        return totalSuccess;
    }



    public ReportResponse requestReport(ReportRequest request) {
        ReportResponse response = new ReportResponse(request);
        response.setSuccess(false);
        response.setResponseMsg("Report was null");

        //get request parameters
        String requestType = request.getRequestType();
        String reportDate = request.getReportDate();
        int startRow = request.getStartRow();
        int limit = request.getLimit();

        if(reportDate == null || reportDate.length() == 0) {
            response.setResponseMsg("reportDate is a required param.");
        }
        else {
            try {
                //check if report exists in MongoDB
                List<ReportRow> report = getReportFromDb(requestType, reportDate);
                boolean saveToMongo = true;
                List<String> csvReport = null;
                SimpleDateFormat appleFormatter = new SimpleDateFormat(ISO_FORMAT);
                Date reportDateObj = appleFormatter.parse(reportDate);  

                //if not there, trigger new report request to API
                if(report == null) {
                    boolean retrieved = appStoreClient.getReport(requestType, reportDateObj);
                    if(retrieved) {
                        String resp = appStoreClient.getResponse();
                        csvReport = writeReport(resp, reportDateObj, requestType);
                        if(csvReport != null) {
                            report = saveReportToDB(requestType, reportDateObj, csvReport);
                        }
                    }
                }
                else {
                    log.info("report found in MongoDB. Skipping API call.");
                    saveToMongo = false;
                }
                if(report != null) {
                    response.setSuccess(true);
                    response.setResponseMsg("Report successfully retrieved.");
                    response = addReportToResponse(response, report, startRow, limit);
                }
                else {
                    String errMsg = appStoreClient.getErrorMsg();
                    if(errMsg != null) {
                        response.setResponseMsg(errMsg);
                    }
                    else {
                        response.setResponseMsg("Report failed. No error msg.");
                    }
                }    
            }
            catch(Exception e) {
                response.setResponseMsg("Could not retrieve report: "+e.getMessage());

            }
        }
        
        return response;
    }


    private ReportResponse addReportToResponse(ReportResponse response, List<ReportRow> report, int startRow, int limit) {
        //trim report
        try {
            List<ReportRow> trimmedReport = report.subList(startRow, startRow + limit);
            response.setReport(trimmedReport);
        }
        catch(Exception e) {
            log.warning(    "Original Report size: "+report.size()+
                            ". startRow: "+startRow+
                            ", limit: "+limit+
                            ". Could not trim report rows: "+e.getMessage()
                        );
            response.setReport(report);
        }
        
        return response;

    }

    private boolean checkForReport(String requestType, String reportDate) {
        List<ReportRow> report = getReportFromDb(requestType, reportDate);
        if(report != null) {
            return true;
        }
        return false;
    } 

    private List<ReportRow> getReportFromDb(String requestType, String reportDate) {
        if(dbCollection == null) {
            loadMongoCollection();
        }

        List<ReportRow> csvReport = null;

        DBObject query = new BasicDBObject("requestType",requestType).
                                    append("dateStr",reportDate);

        List<DBObject> docs = dbCollection.find(query).toArray();
        
        return convertDbResults(docs);

    }

    private List<ReportRow> convertDbResults(List<DBObject> docs) {
        List<ReportRow> results = null;
        if(docs != null && docs.size() > 0) {
            results = new ArrayList<ReportRow>();
            for(DBObject doc : docs) {
                results.add( new ReportRow(doc.toMap()) );
            }
        }
        return results;
    }


    private List<String> writeReport(String report, Date reportDate, String requestType) {
        String filePath = "";
        List<String> csvReport = new ArrayList<String>();
        
        try {  
            SimpleDateFormat fileFormatter = new SimpleDateFormat(FILE_DATE_FORMAT);
            String fileDate = fileFormatter.format(reportDate);
            File reportFile = File.createTempFile(requestType+"_"+fileDate+"-", ".txt", new File(REPORT_PATH));

            filePath = reportFile.toString();
            Path path = Paths.get(filePath);
            

            //write file
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            String lines[] = report.split("\\r?\\n");

            for(String line : lines){
                //replace tabs with pipe delimiters
                line = line.replace("\t",DELIMITER);
                //remove last delimiter from line
                line = line.substring(0, line.length()-1);
                //log.info("writing line: "+line);
                writer.write(line);
                writer.newLine();
                csvReport.add(line);
            }
            writer.flush();
            writer.close();

        }
        catch(Exception ioe) {
            log.info("could not write report: "+filePath+": "+ioe.getMessage());
        }

        return csvReport;

    }


    private List<ReportRow> saveReportToDB(String requestType, Date reportDate, List<String> csvReport) throws Exception {
        List<ReportRow> report = new ArrayList<ReportRow>();
        boolean headerRow = true;
        String[] columns = {};
        SimpleDateFormat format = new SimpleDateFormat(ISO_FORMAT);
        String reportDateStr = format.format(reportDate);

        if(dbCollection == null) {
            loadMongoCollection();
        }

        for(String line : csvReport) {
            if(headerRow) { //get the column headers
                columns = line.split(DELIMITER);
                headerRow = false;
                continue;
            }
            
            BasicDBObject dbObj = new BasicDBObject()
                    .append("requestType",requestType)
                    .append("reportDate",reportDate)
                    .append("dateStr",reportDateStr);

            String[] vals = line.split(DELIMITER);
            int colIndex = 0;
            for(String val : vals) {
                dbObj.append(columns[colIndex++].trim(),val);
            }
            //add the request parameters also
            Map<String,String> params = appStoreClient.getRequestParams();
            for (Map.Entry<String, String> param : params.entrySet()) {
                String key = param.getKey();
                if( !key.equals("USERNAME") && 
                    !key.equals("VNDNUMBER") && 
                    !key.equals("PASSWORD")
                    ) {
                    dbObj.append(param.getKey(), param.getValue());
                }
                
            }

            WriteResult result = dbCollection.insert(dbObj);
            ObjectId id = (ObjectId)dbObj.get( "_id" );
            log.info("line saved to mongoDB: "+id.toHexString());
            report.add( new ReportRow(dbObj.toMap()) );
            
        }
        
        return report; 
    }

    private void loadMongoCollection() {
        mongoClient = mongoMgr.getClient();
        mongoDb = mongoClient.getDB(MONGO_DB);
        dbCollection = mongoDb.getCollection(MONGO_COLLECTION);
    }
    

    public static void main(String[] args) {
        AppStoreMgr appStoreMgr = new AppStoreMgr();
        boolean success = appStoreMgr.getAllReports();
        if(success) {
            System.out.println("App Store Report retrieved");
        }
        else {
            System.err.println("App Store Report FAILED!");
        }
    }
    

}