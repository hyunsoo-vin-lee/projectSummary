# JPO.invoke

'''
package com.lhs.rest;

iimport javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import com.matrixone.servlet.Framework;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

@Path("/rest")
@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
public class RestHandler{
	
	@javax.ws.rs.core.Context
	private HttpServletResponse _response;
	 
	@javax.ws.rs.core.Context
	private HttpServletRequest _request;
	
	private Context getContext(HttpServletRequest request) throws Exception{
		try {
        	Context context = Framework.getContext(request); 
            return context;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@GET
	@Path("/jpo")
	public Response invokeGET(@QueryParam("program") String programExpr, @QueryParam("returnType") String returnType) {
		return invokeCommon(programExpr, returnType);
	}
	
	@POST
	@Path("/jpo")
	public Response invokePOST(@FormParam("program") String program, @FormParam("returnType") String returnType) throws MatrixException {
		return invokeCommon(program, returnType);
	}
	
	private Response invokeCommon(String programExpr, String returnType) {
		String outStr = "";
		try {
			Context context = getContext(_request);
//			Context context = null;
			
			if ( StringUtils.isEmpty(programExpr) )
			{
				outStr = "Check program parameter. program parameter is empty.";
			}
			else
			{
				returnType = StringUtils.isEmpty(returnType) ? "MapList" : returnType;
				
				Map paramMap = _request.getParameterMap();
				paramMap.remove("program");
				paramMap.remove("returnType");
				
				StringList slProgramExpr = FrameworkUtil.splitString(programExpr, ",");
				String[] sProgramExprArr = null;
				String sTempProgramExpr = null;
				
				JSONArray jsonArr = new JSONArray();
				for (int k = 0; k < slProgramExpr.size(); k++)
				{
					sTempProgramExpr = (String) slProgramExpr.get(k);
					sProgramExprArr = sTempProgramExpr.split(":");
					
					if ( sProgramExprArr != null && sProgramExprArr.length >= 2 )
					{
						Object result = JPO.invoke(context, sProgramExprArr[0], null, sProgramExprArr[0], JPO.packArgs(paramMap), Class.forName(returnType));
						jsonArr.put(convertObject2JSONArray(result));
					}
					else
					{
						outStr = "Check program parameter. program parameter is " + sTempProgramExpr;
						break;
					}
					
				}
				
				outStr = jsonArr.toString();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			outStr = e.getMessage();
		}
		return getResponse(outStr);
	}
	
	private Response getResponse(String outStr) {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
				.header("Access-Control-Max-Age", "3600")
				.header("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization")
				.entity(outStr)
				.build();
	}
	
	private JSONArray convertObject2JSONArray(Object obj) throws MatrixException {
		JSONArray jsonArr = new JSONArray();
		JSONObject json = null;
		if ( obj instanceof MapList )
		{
			MapList mapList = (MapList) obj;
			Map map = null;
			for (int k = 0; k < mapList.size(); k++)
			{
				map = (Map) mapList.get(k);
				jsonArr.put( new JSONObject(map) );
			}
		}
		else if ( obj instanceof Map )
		{
			json = new JSONObject((Map) obj);
			jsonArr.put(json);
		}
		else
		{
			json = new JSONObject();
			json.put("value", obj);
			jsonArr.put(json);
		}
		return jsonArr;
	}
	
}
'''