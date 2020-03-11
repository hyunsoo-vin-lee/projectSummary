function executeWidgetCode() {
    require(
        [
            "UWA/Drivers/jQuery",
            "DS/PlatformAPI/PlatformAPI",
            "UWA/Core",
            "BTWWSemanticUI/SemanticUITable_ES5_UM5_v1/SemanticUITable",
            "UM5Modules/EnoTableUtils",
            "DS/UIKIT/Input/Select",
            "DS/WAFData/WAFData"
        ], // 해당 function을 구현하기 위해 필요한 library 선언
        function($,PlatformAPI,UWA, SemanticUITable, EnoTableUtils, Select, WAFData) { // 각 library를 사용하기 위해 변수로 지정
            var MyWidget = {
                onLoadWidget: function() {

                },
                buildSkeletion: function() {

                }
            }

            widget.addEvent("onLoad", myWidget.onLoadWidget);
        }

    );
}