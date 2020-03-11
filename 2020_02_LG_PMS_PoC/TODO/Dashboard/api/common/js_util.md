# js util

- url 가져오기
```
var widgetURL = widget.getUrl();
myWidget._3dspaceURL = widgetURL.substring(0, widgetURL.indexOf('/3dspace/')+8);
```

- preference 사용
```
// preference value 조회
var fileUrl = widget.getValue("configFile");

// preference value 설정
widget.setValue("nbItems", 5);

// preference 조회
widget.getPreference(name); // name이라는 preference의 모든 값들을 출력한다.

/*
<widget:preference name="limit" type="range" label="Number of items to display" defaultValue="5" step="1" min="1" max="25" />

{
   name: "limit",
   type: "range",
   label: "Number of items to display",
   defaultValue: "",
   step: "1",
   min: "1",
   max: "25",
   value: "5"
}
*/

```

- html 변경 : 동적으로 selectbox 변경 등...
```
// setHTML
var myContent = UWA.createElement('div', {
   html: '<p>Loading...<p/>'
});

myContent.setHTML('<p>Ready!<p/>'); // 기존 element 하위의 html을 변경한다.

// setContent
var sel = new Select({
    placeholder: false,
    nativeSelect: true,                
    attributes: {
        id : 'selectProject',
        disabled: false 
    }       
});
var array = new Array();
var defaultValue = null;
for (var k = 0; k < jsonData.length; k++)
{
    if ( k == 0 )
    {
        defaultValue = jsonData[k].id;
    }
    array.push({
        value: jsonData[k].objectId
        , label : jsonData[k].name
    });
}
sel.add(array);
sel.addEvent('onChange', function(e, item) {
    myWidget.onChangeProjectFilter();
});
sel.getContent().setStyle("width", 270) ;  
myWidget.selectProjectContainer.setContent(sel); // selectProjectContainer 라는 div 하위에 selectbox를 새로 만들어서 overwrite한다.
```

- stringlist(java) > json > string array(javascript)
```
$.ajax(
		{
			url: "http://localhost:9080/WebService/rest/stringlist",
			method: "GET",
			success: function(res) {
				res = res.replace(/[\[\] ]/g,"");
				var resArr = res.split(",");
				for (var k = 0; k < resArr.length; k++)
				{
					$("#a").append("<p>" + resArr[k] + "</p>");
				}
				
				//$("#a").html("<p>" + jsonData["attribute[Title]"] + "</p>");
				//$("#b").html("<p>" + jsonData["from[Subtask].to.name"] + "</p>");
				//$("#c").html("<p>" + jsonData["program[emxProjectSpace -method getIssueList ${OBJECTID}]"] + "</p>");
			},
			error: function(e) {
				alert(e);
			}
		}
	);
```