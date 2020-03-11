# event 구현

```
// widget.createElement 이용
widget.createElement({ tag: 'span', id: jsonTemp.name + '_data', text: 0,
    events:{
        click:function(e){
            PlatformAPI.publish("actualChart", {"projectId" : $("#selectProject").val()});
            PlatformAPI.publish("planActualCompareChart", {"projectId" : $("#selectProject").val()});
            PlatformAPI.publish("costItemPlanActualChart", {"projectId" : $("#selectProject").val()});
        }
    }
});

// element.addEvent 이용
var myElement = UWA.createElement('div', {
    html: 'Event delegation is <strong>the best</strong>!'
});

// Bind the event on strong and span tags
myElement.addEvent('click strong,span', function () {
    UWA.log('You clicked on a strong or highlighted text!');
});
```