Java client for automated report retrieval from Apple App Store API.

2014-12-12<br>
Miles Poindexter<br>
selfpropelledcity@gmail.com<br>

This client is designed to run as a service inside Apache ServiceMix (SMX).<br>
http://servicemix.apache.org/

It has also been tested for Talend ESB, which is based in SMX.<br>
https://www.talend.com/products/esb

TEST:<br>
mvn exec:java -Dexec.mainClass="com.cn.dsa.apple.AppStoreMgr"

mvn exec:java -Dexec.mainClass="com.cn.dsa.apple.AppStoreClient"

REST Service:<br>
Web service for Apple appStore reports.
This "micro service" is designed so that report data can easily be pulled into an RDBMS, BI tool or Mobile App.

REQUESTS:<br> 
This service will accept 4 params, which can be sent as either GET params or in a POST body.
The report will be pulled from the AppStore API if no matching report is found in its MongoDB collection. All reports that are pulled from the AppStore API are stored in MongoDB for future use, so the API will only be hit once for each type of report requested.

1 param is required:<br>
reportDate: (in ISO format. example: 2014-11-22)

3 params are optional:<br>
requestType: (currently only "appStoreWeekly" or "appStoreDaily" are recognized. appStoreDaily is the default)<br>
startRow: the row to begin showing results for. Default is 0.<br>
limit: how many rows to show.  Default is 20.

The examples below show possible values for params.

URL:<br>
http://localhost:8040/services/appstore/report/

WADL:<br>
http://localhost:8040/services/appstore?_wadl

GET REQUEST:<br>
sample daily report:<br>
http://localhost:8040/services/appstore/report?requestType=appStoreDaily&reportDate=2014-11-22

sample weekly report:<br>
http://localhost:8040/services/appstore/report?requestType=appStoreWeekly&reportDate=2014-11-02

Set limit (default is 20 rows):<br>
http://localhost:8040/services/appstore/report?requestType=appStoreDaily&reportDate=2014-11-22&limit=200

Set startRow (default is row 0):<br>
http://localhost:8040/services/appstore/report?requestType=appStoreDaily&reportDate=2014-11-22&startRow=19


POST REQUEST:<br>
To see a sample POST Body:<br>
http://localhost:8040/services/appstore/report/test

XML:<br>
headers:<br>
Content-Type: application/xml<br>
Accept: application/xml

POST Body:<br>
<reportRequest><br>
<requestType>appStoreWeekly</requestType><br>
<reportDate>2014-11-02</reportDate><br>
</reportRequest>

JSON:<br>
Content-Type: application/json<br>
Accept: application/json

POST Body:<br>
{<br>
  "reportRequest":{<br>
    "requestType":"appStoreWeekly",<br>
    "reportDate":"2014-11-02"<br>
  }<br>
}


