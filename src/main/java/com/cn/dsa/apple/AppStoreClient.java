package com.cn.dsa.apple;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.ByteArrayInputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import javax.net.ssl.HttpsURLConnection;

import java.text.SimpleDateFormat;
import java.text.ParseException;

public class AppStoreClient {

    private static String LS = System.getProperty("line.separator");

    //Report Types
    public static String DAILY_REPORT = "appStoreDaily"; //default
    public static String WEEKLY_REPORT = "appStoreWeekly";

    //property names
    public static String URL_KEY = "apple.autoingestion.url";
    public static String USER_KEY = "apple.userId";
    public static String PWD_KEY = "apple.pwd";
    public static String VENDOR_ID_KEY = "apple.vendorId";
    public static String REPORT_TYPE_KEY = "apple.reportType";
    public static String REPORT_SUB_TYPE_KEY = "apple.reportSubType";
    public static String DAILY_TYPE_KEY = "apple.dateType.daily";
    public static String WEEKLY_TYPE_KEY = "apple.dateType.weekly";

    //request param keys
    public static String USER_PARAM = "USERNAME";
    public static String PWD_PARAM = "PASSWORD";
    public static String VENDOR_ID_PARAM = "VNDNUMBER";
    public static String REPORT_TYPE_PARAM = "TYPEOFREPORT";
    public static String REPORT_SUB_TYPE_PARAM = "REPORTTYPE";
    public static String DATE_TYPE_PARAM = "DATETYPE";
    public static String REPORT_DATE_PARAM = "REPORTDATE";

    //response
    public static String ERROR_HEADER = "ERRORMSG";

    private static final String APPLE_FORMAT = "yyyyMMdd"; //example: 20130915
    private static final String ISO_FORMAT = "yyyy-MM-dd";

    private static Logger log = Logger.getLogger(AppStoreClient.class.getName());

    private static Properties props = null;

    private Map<String,String> requestParams = new HashMap<String,String>();
    public void setRequestParams(Map<String,String> requestParams) {
        this.requestParams = requestParams;
    }
    public Map<String,String> getRequestParams() {
        return requestParams;
    }
    
    private String response = null;
    public void setResponse(String response) {
        this.response = response;
    }
    public String getResponse() {
        return response;
    }

    private String errorMsg = "";
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * make an API call to get a report
     * @param string requestType
     * @param Date reportDate
     * @return boolean
     */
    public boolean getReport(String requestType, Date reportDate) throws Exception {
        if(props == null) {
            loadProperties();
        }

        //api url
        String apiUrl = props.getProperty(URL_KEY, "");
        
        if(apiUrl.length() == 0) {
            log.warning("problems getting request properties!");
            return false;
        }

        setRequestParams(null);
        setResponse(null);
        
        log.info("API URL: "+apiUrl);

        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept-Encoding", "agzip");

        SimpleDateFormat appleFormatter = new SimpleDateFormat(APPLE_FORMAT);
        String dateStr = appleFormatter.format(reportDate);

        String postBody = createPostBody(requestType, dateStr);

        String response = doSecurePostRequest(apiUrl, postBody, headers); 
        
        if(response != null) {
            //log.info("API called successfully: "+path);
            //log.info("API response: "+response);
            setResponse(response);
            return true;
        }

        return false;
    }


    private String createPostBody(String requestType, String reportDate) throws Exception {
        StringBuilder body = new StringBuilder();

        String user = props.getProperty(USER_KEY, "");
        String pwd = props.getProperty(PWD_KEY, "");
        String vendorId = props.getProperty(VENDOR_ID_KEY, "");

        String reportType = props.getProperty(REPORT_TYPE_KEY, "");
        String reportSubType = props.getProperty(REPORT_SUB_TYPE_KEY, "");

        String dateType = props.getProperty(DAILY_TYPE_KEY,""); //default
        if(requestType.equals(WEEKLY_REPORT)) {
            dateType = props.getProperty(WEEKLY_TYPE_KEY,"");
        }

        Map<String,String> params = new HashMap<String,String>();

        body.append(USER_PARAM+"="+user);
        params.put(USER_PARAM,user);
        body.append("&"+PWD_PARAM+"="+pwd);
        params.put(PWD_PARAM,pwd);
        body.append("&"+VENDOR_ID_PARAM+"="+vendorId);
        params.put(VENDOR_ID_PARAM,vendorId);
        body.append("&"+REPORT_TYPE_PARAM+"="+reportType);
        params.put(REPORT_TYPE_PARAM,reportType);
        body.append("&"+REPORT_SUB_TYPE_PARAM+"="+reportSubType);
        params.put(REPORT_SUB_TYPE_PARAM,reportSubType);
        body.append("&"+DATE_TYPE_PARAM+"="+dateType);
        params.put(DATE_TYPE_PARAM,dateType);
        body.append("&"+REPORT_DATE_PARAM+"="+reportDate);
        params.put(REPORT_DATE_PARAM,reportDate);

        //save these request params for later use
        setRequestParams(params);

        String postBody = body.toString();
        log.info("postBody: "+postBody);
        return postBody;
    }

    private String doSecurePostRequest(String urlstr, String postBody, Map<String,String> headers) 
        throws MalformedURLException, IOException, ProtocolException {

        URL url = new URL(urlstr);
        HttpsURLConnection httpsCon = (HttpsURLConnection)url.openConnection();
        log.info("secure connection opened . . .");
        httpsCon.setDoOutput(true);
        httpsCon.setRequestMethod("POST");
        
        if(headers != null && headers.size() > 0) {
            for (Map.Entry<String,String> header : headers.entrySet()) {
                String name = header.getKey();
                String value = header.getValue();
                httpsCon.setRequestProperty(name,value);
            }
        }
                
        OutputStreamWriter wr = new OutputStreamWriter(httpsCon.getOutputStream());
        wr.write(postBody);
        wr.flush ();
        wr.close ();

        //check for error header
        String errorMsg = httpsCon.getHeaderField(ERROR_HEADER);

        if(errorMsg != null) {
            setErrorMsg(errorMsg);
            return null;
        }
        
        //Get Response
        InputStream is = null;

        try {
            log.info("getting input stream . . .");
            is = httpsCon.getInputStream();
            Map<String, List<String>> responseHeaders = httpsCon.getHeaderFields();
            List<String> contentEncodings = responseHeaders.get("Content-Encoding");

            boolean hasGzipHeader = false;
            if (contentEncodings != null) {
                for (String respHeader : contentEncodings) {
                    if (respHeader.equalsIgnoreCase("gzip") || respHeader.equalsIgnoreCase("agzip")) {
                        hasGzipHeader = true;
                        break;
                    }
                }
            }
            if (hasGzipHeader) {
                log.info("Response encoding is GZIP");
                is = new GZIPInputStream(is);
            }
        }
        catch(IOException ie) {
            log.info("error getting input stream: "+ie);
            throw ie;
            //is = httpsCon.getErrorStream();
        }

        //return is;
        String response = handleResponse(is);

        log.info("response handled . . .");  
        
        httpsCon.disconnect();
        return response;
        
    }

    private String handleResponse(InputStream is) {
        //capture complete Input stream 
        //to avoid server closing connection
        //String encoding = "ISO-8859-1";
        String encoding = "UTF-8";

        String response = "";
        try {
            Reader decoder = new InputStreamReader(is, encoding);
            BufferedReader inputReader = new BufferedReader(decoder);
            if(!inputReader.ready()) {
                //Pause for 1 second
                //log.info("waiting for response to complete . . .");
                Thread.sleep(1000);
            }
            StringBuilder sb = new StringBuilder();
            String inline = "";
            //log.info("reading response");
            while ((inline = inputReader.readLine()) != null) {
                //log.info(inline);
                sb.append(inline+LS);
            }
            //close the input stream
            is.close();
            response = sb.toString();

        }
        catch(IOException ie) {
            log.warning("bad HTTP response: "+ie.getMessage());
        }
        catch(InterruptedException iee) {
            log.warning("HTTP connection interrupted: "+iee.getMessage());
        }

        return response;

    }


    private void loadProperties() {
        log.info("AppStoreClient loading properties . . .");
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("cn.properties");
            props = new Properties();
            props.load(is);
            /*
            String prop = (String)props.getProperty(PWD_KEY,null);
            if(prop != null) {
                log.info(PWD_KEY+" = "+prop);
                this.pwd = prop;
            }
            */
        }
        catch(IOException ie) {
            log.log(Level.SEVERE, "Could not load AppStoreClient properties: ", ie);
        }
        catch(Exception e) {
            log.log(Level.SEVERE, "problems loading AppStoreClient properties: ", e);
        }
    }
    
    

    public static void main(String[] args) {
        AppStoreClient appStoreClient = new AppStoreClient();
        /*
        //get today's date
        Date today = new Date();
        
        //create date minus one day
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_YEAR,-1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR,-7);
        Date weekPrevious = cal.getTime();
        */

        //For weekly reports, this date must be a Sunday
        //String requestType = WEEKLY_REPORT;
        String requestType = DAILY_REPORT;
        String reportDateStr = "2014-11-02";
        try {
           SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
            Date reportDate = isoFormatter.parse(reportDateStr);

            boolean success = appStoreClient.getReport(requestType, reportDate);
            if(success) {
                String report = appStoreClient.getResponse();
                System.out.println("App Store Report "+requestType+" for "+reportDateStr);
                System.out.println(report);
            }
            else {
                String errorMsg = appStoreClient.getErrorMsg();
                System.err.println("FAILED! Report "+requestType+" for "+reportDateStr);
                System.err.println("errorMsg: "+errorMsg);

            }
        }
        catch(Exception e) {
            System.err.println("exception Msg: "+e.getMessage());
        }
        
    }
    

}