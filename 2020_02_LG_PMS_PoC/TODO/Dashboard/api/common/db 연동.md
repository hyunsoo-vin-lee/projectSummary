# db 연동

```
var restURL = myWidget._3dspaceURL + "/rest/project/getProgramList";
WAFData.authenticatedRequest(restURL, {
    method : "GET",
    type : "json",
    headers:{Accept:"application/json","Content-Type":"application/json"},
    data : {
        programId : "ALL"
    },
    onComplete: function(res){
        var jsonData = JSON.parse(JSON.stringify(res));
        var sel = new Select({
            placeholder: false,
            nativeSelect: true,                
            attributes: {
                id : 'selectProgram',
                disabled: false 
            }       
        });
        var array = new Array();
        for (var k = 0; k < jsonData.length; k++)
        {
            array.push({
                value: jsonData[k].objectId
                , label : jsonData[k].name
            });
        }
        sel.add(array);
        sel.addEvent('onChange', function(e, item) {
            myWidget.drawProjectFilter();
        });
        sel.getContent().setStyle("width", 270) ;  
        myWidget.selectProgramContainer.addContent(sel);
        
        // trigger
        myWidget.drawProjectFilter();
    },
    onFailure : function(error){
        console.log(error);
    }
});
```