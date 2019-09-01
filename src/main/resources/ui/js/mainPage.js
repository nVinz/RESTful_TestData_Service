




function downloadTable() {
if (userHasAccess()) return
  $.ajax({url: "/graphInfo", success: function(result){
        for (var key in result.charts) {
            var name = result.charts[key].name;
            var labels = result.charts[key].labels;
            var actual = result.charts[key].actual;
            var ctx = document.getElementById("chart " + name).getContext('2d');
            var  myChart = new Chart(ctx, copyObject(config));
            addData(myChart, labels, actual);
        }
        $('.loadingmessage').hide();
        graphIsLoaded = true
      }});
}