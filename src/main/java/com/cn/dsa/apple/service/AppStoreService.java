package com.cn.dsa.apple.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue; 
import javax.ws.rs.core.MediaType;

import com.cn.dsa.apple.AppStoreClient;
import com.cn.dsa.apple.AppStoreMgr;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AppStoreService {

    private AppStoreMgr appStoreMgr = new AppStoreMgr();

    @POST
    @Path("/report")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces(MediaType.APPLICATION_JSON)
    public Response report(ReportRequest request) throws Exception {
        ReportResponse response = appStoreMgr.requestReport(request);

        //return response;
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/report")
    //@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces(MediaType.APPLICATION_JSON)
    public Response report( @QueryParam("requestType") String requestType, 
                            @QueryParam("reportDate") String reportDate,
                            @QueryParam("startRow") Integer startRow,
                            @QueryParam("limit") Integer limit
                            ) throws Exception {
        ReportRequest request = new ReportRequest();
        request.setRequestType(requestType);
        request.setReportDate(reportDate);
        if(startRow != null) {
            request.setStartRow(startRow.intValue());
        }
        if(limit != null) {
            request.setLimit(limit.intValue());
        }

        ReportResponse response = appStoreMgr.requestReport(request);

        //return response;
        return Response.ok().entity(response).build();
    }


    @GET
    //@Path("/test")
    @Path("/{a:test|report/test}")
    //@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	//@Produces(MediaType.APPLICATION_JSON)
    public Response testReport() throws Exception {
        ReportRequest request = new ReportRequest();
        request.setRequestType(AppStoreClient.DAILY_REPORT);
        request.setReportDate("2014-11-02");
        request.setStartRow(0);
        request.setLimit(30);

        //return request;
        return Response.ok().entity(request).build();
    }
}
