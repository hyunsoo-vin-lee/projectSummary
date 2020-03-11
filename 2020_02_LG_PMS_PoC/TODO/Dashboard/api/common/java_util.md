# java util

- [java] JPO.invoke로 추출한 데이터 JSONArray로 변환
```
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
```

- [java] response 생성
'''
private Response getResponse(String outStr) {
    return Response.ok().header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization")
            .entity(outStr)
            .build();
}
'''

- [java] Context 생성
```
private Context getContext(HttpServletRequest request) throws Exception{
    try {
        Context context = Framework.getContext(request); 
        return context;
    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
}
```