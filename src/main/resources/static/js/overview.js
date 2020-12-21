var overview = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

var loadLOCChart = function(loc_name_list, loc_list){
    var html = "<div  class=\"common_list_title\" style='height: 60px'>" +
        "<h3 class=\"common_list_title_h3\">LOC</h3>" +
        "</div><div id = \"loc_chart\" style='height: 500px'></div>";
    $("#loc_div").html(html);

    var dom = document.getElementById("loc_chart");
    var myChart = echarts.init(dom);
    option = null;
    option = {
        tooltip: {
            trigger: 'item',
            formatter: 'project: {b}<br/>loc: {c} ({d}%)'
        },
        legend: {
            orient: 'vertical',
            right: 5,
            top:5,
            data: loc_name_list
        },
        series: [
            {
                name: 'LOC',
                type: 'pie',
                radius: ['50%', '70%'],
                avoidLabelOverlap: false,
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '30',
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: loc_list
            }
        ]
    };
    if (option && typeof option === "object") {
        myChart.setOption(option, true);
    }
}

var loadPageData = function (){
    $.ajax({
        type: "get",
        url: "/metric/project",
        success: function(result) {
            var html = "<div class=\"common_list_box\">" +
                "<div  class=\"common_list_title\">" +
                "<h3 class=\"common_list_title_h3\">项目列表</h3>" +
                "</div>" +
                "<div  class=\"my_common_con\">" +
                "<div>" +
                "<ul  class=\"my_common_list\">" +
                "<li class=\"my_common_list_li\">" +
                "<label  class=\"my_common_list_th\"> index" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> ID" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> Project" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> NOP" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> NOF" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> NOM" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> LOC" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> lines" +
                "</label>" +
                "</li>";

            for(var i = 0; i < result.length; i++) {
                html += "<li class=\"my_common_list_li\">";
                html += "<label  class=\"my_common_list_td\">" + (i + 1) + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].project.id + "</label>";
                html += "<a class=\"my_common_list_a\" href=\"\" target=\"_blank\">" + result[i].project.name + " (" + result[i].project.language + ") " + "</a>";
                html += "<label  class=\"my_common_list_td\">" + result[i].nop + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].nof + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].nom + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].loc + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].lines + "</label>";
                html += "</li>";
            }

            html += "</ul></div></div></div>";

            $("#project_list").html(html);

            var loc_list = [];
            var loc_name_list = [];
            for(var j = 0; j < result.length; j++) {
                var temp_loc = {};
                temp_loc["name"] = result[j]["project"]["name"];
                temp_loc["value"] = result[j]["loc"];
                loc_list.push(temp_loc);
                loc_name_list.push(result[j]["project"]["name"]);
            }

            loadLOCChart(loc_name_list,loc_list)
        }
    })
}