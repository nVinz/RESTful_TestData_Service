



$('#updateTable').click(function downloadTable() {
    if (userHasAccess()) return
      $.ajax({url: "/getFilteredHTMLTable",
       data:{
            'tableName': 'tables',
            'columnName': 'name',
            'filter': ${'#filterInput'}.getContext(),
            'tableStyle': ' class=\"table\"'
       },
       success: function(result){
            $('#mainTable').text(result)
          }});
    })

function userHasAccess(){
    return true;
}