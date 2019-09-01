
$(document).ready(function() {



$('#updateTable').click(function downloadTable() {
//    if (userHasAccess()) return
       $.ajax({url: "/getFilteredHTMLTable",
       data:{
            'tableName': 'tables',
            'columnName': 'description',
            'filter': $('#filterInput').val(),
            'tableStyle': ' class=\"table\"'
       },
       success: function(result){
       $('#mainTable').empty()
            $('#mainTable').append(result)
//            $('#mainTable').updateTable
          }});
    })

//$('#updateTable').click(function () {
//getData();

//    return
//        $.ajax({url: "/test",
//         success: function(result){
//              $('#mainTable').text(result)
//            }});
//})
//
//function getData() {
//  $.ajax({url: "/test", success: function(result){
//
//            var actual = result;
//      }});
//}


function userHasAccess(){
    return true;
}

})