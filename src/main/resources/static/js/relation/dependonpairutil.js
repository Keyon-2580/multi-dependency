var showDependOnDetails = function (id1, id2, dependsOnIntensity, dependsByIntensity) {
    showMetrics(id1, id2, dependsOnIntensity, dependsByIntensity);
    showMatrix(id1, id2);
}

var showMetrics = function (id1, id2, dependsOnIntensity, dependsByIntensity) {
    $.ajax({
        type: "GET",
        url: "/dependon/packagepair/metrics?id1=" + id1 + "&id2=" + id2,
        success: function (result) {
            var html = "";
            html += "<table class='table table-bordered'>";
            html += "<tr><th>包名</th><th>文件数</th><th>代码行数</th><th>依赖类型（次数）</th><th>依赖强度</th><th>CoChange次数</th></tr>";
            html += "<tr>";
            html += "<td>";
            html += "<a target='_blank' href='/relation/package/" + id1 + "'>" + result.path1;
            html += "</td>";
            html += "<td>";
            html += result.pck1num;
            html += "</td>";
            html += "<td>";
            html += result.loc1;
            html += "</td>";
            html += "<td>";
            html += result.dependon1types;
            html += "</td>";
            html += "<td>";
            html += dependsOnIntensity.toFixed(2);
            html += "</td>";
            html += "<td rowspan='2' style='vertical-align: middle'>";
            html += result.cochange;
            html += "</td>";
            html += "</tr>";
            html += "<tr>";
            html += "<td>";
            html += "<a target='_blank' href='/relation/package/" + id2 + "'>" + result.path2;
            html += "</td>";
            html += "<td>";
            html += result.pck2num;
            html += "</td>";
            html += "<td>";
            html += result.loc2;
            html += "</td>";
            html += "<td>";
            html += result.dependon2types;
            html += "</td>";
            html += "<td>";
            html += dependsByIntensity.toFixed(2);
            html += "</td>";
            html += "</tr>";
            html += "</table>";
            $("#package_dependon_detail").html(html);
        }
    })
}


var showMatrix = function (id1, id2) {
    $.ajax({
        type: "GET",
        url: "/dependon/packagepair/matrix?id1=" + id1 + "&id2=" + id2,
        success: function (result) {
            var html = "";
            html += "<div  style='overflow: auto;' width='100%' id='matrix_2'>";
            html += "<table  class = 'table table-bordered'>"
            html += "<tr>";
            html += "<th  style='background-color: #FFFFFF;'></th>";
            for(var i = 0; i < result.numofpck1; i++){
                html += "<th style='background-color: #FFFFFF;'>P1.F" + (i + 1) + "</th>";
            }
            for(var i = result.numofpck1; i < result.allfiles.length; i++){
                html += "<th style='background-color: #FFFFFF;'>P2.F" + (i+1-result.numofpck1) + "</th>";
            }
            html += "</tr>";
            for(var i = 0; i < result.allfiles.length; i++){
                html += "<tr>";
                html += "<td style='background-color: #FFFFFF;'>";
                if(i < result.numofpck1){
                    html += "P1.F" + (i+1) + ": ";
                }else{
                    html += "P2.F" + (i-result.numofpck1+1) + ": ";
                }
                html += "<a target='_blank' href='/relation/file/" + result.allfiles[i].id + "'>" + result.allfiles[i].name + "(" + result.allfiles[i].loc + ")" + "</a></td>";
                for(var j = 0; j < result.allfiles.length; j++){
                    html += "<td style='background-color: #FFFFFF;'>";
                    if(result.matrix[i][j] != null){
                        html +=  result.matrix[i][j];
                    }
                    html += "</td>";
                }
                html += "</tr>";
            }
            html += "</table>";
            html += "</div>";
            $("#package_files_dependon_matrix").html(html);
            $("#matrix_2").scroll(function(){
                $("#matrix_2 tr th").css({"position":"relative","top":$("#matrix_2").scrollTop(),"z-index":"2"});
                $("#matrix_2 tr td:nth-child(1)").css({"position":"relative","left":$("#matrix_2").scrollLeft(),"z-index":"1"});
                $("#matrix_2 tr th:nth-child(1)").css({"position":"relative","top":$("#matrix_2").scrollTop(),"left":$("#matrix_2").scrollLeft(),"z-index":"3"});
            });

            var html1 = "";
            html1 += "<div><ul>";
            for(var i = 0; i < result.numofpck1; i++){
                html1 += "<li>P1.F" + (i+1) + ": <a target='_blank' href='/relation/file/" + result.allfiles[i].id + "'>" + result.allfiles[i].path + "(" + result.allfiles[i].loc + ")" + "</a></li>";
            }
            html1 += "</ul></div>";
            $("#package1_files").html(html1);

            var html2 ="";
            html2 += "<div><ul>";
            for(var i = result.numofpck1; i < result.allfiles.length; i++){
                html2 += "<li>P2.F" + (i+1-result.numofpck1) + ": <a target='_blank' href='/relation/file/" + result.allfiles[i].id + "'>" + result.allfiles[i].path + "(" + result.allfiles[i].loc + ")" + "</a></li>";
            }
            html2 += "</ul></div>";
            $("#package2_files").html(html2);
        }
    })
}