# bar chart 그리기

1. [java] server에서 data 반환
```
@GET
@Path("/barChartData")
public Response getBarChartData() throws MatrixException {
    MapList mapList = new MapList();
    Map map = new HashMap();
    map.put("name", "Ross");
    map.put("age", "26");
    map.put("body-age", "36");
    mapList.add(map);
    map = new HashMap();
    map.put("name", "Chandler");
    map.put("age", "26");
    map.put("body-age", "50");
    mapList.add(map);
    map = new HashMap();
    map.put("name", "Joey");
    map.put("age", "25");
    map.put("body-age", "20");
    mapList.add(map);
    map = new HashMap();
    map.put("name", "Rachel");
    map.put("age", "24");
    map.put("body-age", "30");
    mapList.add(map);
    map = new HashMap();
    map.put("name", "Monica");
    map.put("age", "24");
    map.put("body-age", "18");
    mapList.add(map);
    map = new HashMap();
    map.put("name", "Phoeby");
    map.put("age", "25");
    map.put("body-age", "46");
    mapList.add(map);
    
    StringList slCategory = new StringList();
    MapList mlReturn = new MapList();
    mlReturn.add( extractBarChartDataFromMapList(mapList, "name", slCategory, "age", "Age") );
    mlReturn.add( extractBarChartDataFromMapList(mapList, "name", slCategory, "body-age", "Body Age") );
    
    JSONArray jsonArray = convertObject2JSONArray(mlReturn);
    
    return getResponse(jsonArray.toString());
}

public Map extractBarChartDataFromMapList(MapList mapList, String selectCategory, StringList slCategory, String selectData, String name) {
    try {
        if ( slCategory == null )
        {
            slCategory = new StringList();
        }
        
        Map map = null;
        String sCategory = null;
        String sFixedCategory = null;
        String sData = null;
        StringList slData = new StringList();
        
        if ( slCategory.size() > 0 )
        {
            for (int k = 0; k < slCategory.size(); k++)
            {
                sFixedCategory = (String) slCategory.get(k);
                
                for (int m = 0; m < mapList.size(); m++)
                {
                    map = (Map) mapList.get(m);
                    sCategory = (String) map.get(selectCategory);
                    
                    if ( sCategory.equals(sFixedCategory) )
                    {
                        sData = (String) map.get(selectData);
                        slData.add(sData);
                        
                        break;
                    }
                }
            }
        }
        else
        {
            for (int k = 0; k < mapList.size(); k++)
            {
                map = (Map) mapList.get(k);
                sCategory = (String) map.get(selectCategory);
                
                slCategory.add(sCategory);
                
                sData = (String) map.get(selectData);
                slData.add(sData);
            }
        }
        
        Map mReturn = new HashMap();
        mReturn.put("name", name);
        mReturn.put("data", slData);
        mReturn.put("category", slCategory);
        
        return mReturn;
    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
}
```
