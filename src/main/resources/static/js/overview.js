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
        url: "/git/repoMetric",
        success: function(result) {
            let html = "<div class=\"common_list_box\">" +
                "<div  class=\"common_list_title\">" +
                "<h3 class=\"common_list_title_h3\">项目列表</h3>" +
                "</div>" +
                "<div  class=\"my_common_con\">" +
                "<div>" +
                "<ul  class=\"my_common_list\">" +
                "<li class=\"my_common_list_li\">" +
                "<label  class=\"my_common_list_th\" style='width: 6%'> index" +
                "</label>" +
                "<label  class=\"my_common_list_th\" style='width: 6%'> ID" +
                "</label>" +
                "<label  class=\"my_common_list_th\" style='width: 20%'> Project" +
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
                "<label  class=\"my_common_list_th\"> Commits" +
                "</label>" +
                "<label  class=\"my_common_list_th\"> Issues" +
                "</label>" +
                "</li>";

            for(let i = 0; i < result.length; i++) {
                let projectMetricsList = result[i].projectMetricsList[0];
                let combo_url = '/project/combo_chart?projectId=' + projectMetricsList.project.id;
                html += "<li class=\"my_common_list_li\">";
                html += "<label  class=\"my_common_list_td\" style='width: 6%'>" + (i + 1) + "</label>";
                html += "<label  class=\"my_common_list_td\" style='width: 6%'>" + projectMetricsList.project.id + "</label>";
                html += "<a class=\"my_common_list_a\" style='width: 20%' href='" + combo_url + "' target='_blank' >" + projectMetricsList.project.name + " (" + projectMetricsList.project.language + ") " + "</a>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.nop + "</label>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.nof + "</label>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.nom + "</label>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.loc + "</label>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.lines + "</label>";
                html += "<label  class=\"my_common_list_td\">" + projectMetricsList.commits + "</label>";
                html += "<label  class=\"my_common_list_td\">" + result[i].numOfIssues + "</label>";
                html += "</li>";
            }

            html += "</ul></div></div></div>";

            $("#project_list").html(html);

            // let loc_list = [];
            // let loc_name_list = [];
            // for(let j = 0; j < result.length; j++) {
            //     let temp_loc = {};
            //     temp_loc["name"] = result[j]["node"]["name"];
            //     temp_loc["value"] = result[j]["metric"]["metricValues"]["LOC"];
            //     loc_list.push(temp_loc);
            //     loc_name_list.push(result[j]["node"]["name"]);
            // }

            // loadLOCChart(loc_name_list,loc_list);
        }
    })
}