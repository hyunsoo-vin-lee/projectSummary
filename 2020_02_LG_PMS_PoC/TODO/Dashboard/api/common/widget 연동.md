# widget 연동

```
// 호출하는 쪽
// PlatformAPI.publish("호출받는 쪽 id", 전달할 data(ex. json));
PlatformAPI.publish("actualChart", {"projectId" : $("#selectProject").val()});

// 호출받는 쪽
// PlatformAPI.subscribe("호출받을 자신의 id", 호출받은 데이터를 처리할 자신의 function);
PlatformAPI.subscribe("actualChart", myWidget.selectUserRow);
```

- 멀티테이블 호출
```
PlatformAPI.publish("Add_Ids", {
    "ids" : [this.id]
    , "doInit" : true
});
```