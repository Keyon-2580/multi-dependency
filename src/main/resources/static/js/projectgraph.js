var y = 0;

var cloneLinks_global = [];
var dependsonLinks_global = [];
var cochangeLinks_global = [];

var diameter_global;
var svg_global;
var g_global;
var projectList_global;
var table_global;

var projectgraph = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

//加载数据
var loadPageData = function () {
    var projectlist = [];

    $.ajax({
        type : "GET",
        url : "/project/all/name",
        success : function(result) {
            for(var i = 0; i < result.length; i++){
                // projectlist.push(x);
                var name_temp = {};
                // console.log(x);
                name_temp["id"] = result[i].id;
                name_temp["name"] = result[i].name;
                projectlist.push(name_temp);
            }

            var html = ""
            html += "<div id = \"ProjectSelect\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
            for(var i = 0; i < projectlist.length; i++) {
                if (i === 0) {
                    html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                } else {
                    html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                }
            }
            html += "</select>";
            html += "<br><button id = \"multipleProjectsButton\" type=\"button\" onclick= showMultipleButton()>加载项目</button></div>";

            html += "<div id = \"AttributionSelect\">" +
                "<form role=\"form\">" +
                "<p><label class = \"AttributionSelectTitle\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOn\" onclick=\"CancelChildrenChecked('dependsOn')\">DependsOn：" +
                "</label>" +
                "<label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnTimes\" name = \"dependsOn_children\"> Times >= " +
                "<input  id=\"dependencyTimes\" class = \"AttributionSelectInput\" value=\"3\">" +
                "</label></p>";

            html += "<p><label class = \"AttributionSelectTitle\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"clone\" onclick=\"CancelChildrenChecked('clone')\">Clone：" +
                "</label>" +

                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cloneSimilarity\" name = \"clone_children\">" +
                "<input  class = \"AttributionSelectInput\" id=\"similaritybelow\" value=\"0.7\">" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectBelow\">" +
                "<option value=\"<=\" selected = \"selected\"><=</option>" +
                "<option value=\"<\"><</option></select>" +

                "<label class = \"AttributionSelectLabel\"> &nbsp;Similarity</label>" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectHigh\">" +
                "<option value=\"<=\"><=</option>" +
                "<option value=\"<\" selected = \"selected\"><</option></select>" +

                "<input  class = \"AttributionSelectInput\" id=\"similarityhigh\" value=\"1\">" +

                "<label class = \"AttributionSelectLabel\" style = \"margin-left: 80px\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cloneTimes\" name = \"clone_children\">CloneTimes >=</label>" +
                "<input  class = \"AttributionSelectInput\" id=\"clonetimes\" value=\"3\">" +
                "</p>";

            html += "<p><label class = \"AttributionSelectTitle\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"coChange\" onclick=\"CancelChildrenChecked('coChange')\">Co-change：" +
                "</label>" +
                "<label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cochangeTimes\" name = \"coChange_children\"> Times >= " +
                "<input  id=\"cochangeTimes\" class = \"AttributionSelectInput\" id=\"cochangetimes\" value=\"3\">" +
                "</label></p>";

            html += "<p><div style=\"margin-top: 10px;\">" +
                "<button class = \"showLineButton\" id = \"showLineId\" type=\"button\" onclick= showLineButton()>显示关系</button>" +
                "<button class = \"clearLineButton\" id = \"clearLineId\" type=\"button\" onclick= clearLink() style = \"margin-left: 30px\">隐藏关系</button>" +
                "</div></p>" +
                "</form>" +
                "</div>";

            // console.log(html)
            $("#projectToGraph_util").html(html);
            $('.selectpicker').selectpicker();

            var temp_array = [];
            temp_array.push(projectlist[0].id);
            projectList_global = temp_array.concat();
            projectGraphAjax(temp_array);

            // Loop_ajax(0, projectlist);
        }
    })
}

//调用接口请求数据
var projectGraphAjax = function(projectIds){
    d3.selectAll("svg > *").remove();
    var projectList = {};
    var projectIds_array = [];

    for(var i = 0; i < projectIds.length; i++){
        var tempId = {};
        tempId["id"] = projectIds[i];
        projectIds_array.push(tempId);
    }

    projectList["projectIds"] = projectIds_array;
    projectList["showType"] = "graph";

    $.ajax({
        type:"POST",
        url : "/project/has",
        contentType: "application/json", //必须这样写
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            projectToGraph(result,"projectToGraphSvg");
        }
    })
}

//绘制气泡图
var projectToGraph = function(result,divId){
    // console.log(result);
    var projectdata = result[0].result;
    cloneLinks_global = result[1].links.clone_links;
    dependsonLinks_global = result[1].links.dependson_links;
    table_global = result[2].table;

    var svg = d3.select("#" + divId)
            .attr("width", 1500)
            .attr("height", 1500),
        margin = 20,
        diameter = +svg.attr("width"),
        g_remove = svg.selectAll("g").remove();
        g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

    svg_global = svg;
    g_global = g;
    diameter_global = diameter;

    var color = d3.scaleLinear()
        .domain([0, 1])
        .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
        .interpolate(d3.interpolateHcl);

    var color_clone = d3.scaleLinear()
        .domain([0, 1])
        .range(["hsl(0,30%,90%)", "hsl(15,70%,70%)"])
        .interpolate(d3.interpolateHcl);

    var pack = d3.pack()
        .size([diameter - margin, diameter - margin])
        .padding(2);

    root = d3.hierarchy(projectdata)
        .sum(function(d) { return d.size; })
        .sort(function(a, b) { return b.value - a.value; });

    var focus = root,
        nodes = pack(root).descendants(),
        view;

    //文件节点颜色计算及克隆数据（暂不需要）
    // .style("fill", function(d) {
    //     return d.children ? color(d.depth/(d.depth+5)) : (getCloneBooleanByName(projectdata,d.data.name) ? "\t#FFB6C1" : null);
    // })
    // .call(text => text.append("title").text(function(d) {
    //     return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name + setCloneTitle(getCloneDataByName(clonedata,d.data.long_name)): d.data.name;
    // }));

    var circle = g.selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", function(d) {
            return d.parent ? d.children ? "circlepacking_node" : "circlepacking_node circlepacking_node--leaf" : "circlepacking_node circlepacking_node--root";
        })
        .style("fill", function(d) {
            if(d.children){
                return color(d.depth/(d.depth+10));
            }else{
                var ratio = getCloneRatioByName(projectdata,d.data.id)[1];
                var id = getCloneRatioByName(projectdata,d.data.id)[0];

                if(ratio === 0){
                    // console.log("white")
                    return null;
                }else{
                    return color_clone(ratio);
                }
            }
        })
        .attr("id", function (d) {
            return d.data.id;
        })
        .call(text => text.append("title").text(function(d) {
            return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name  + "\nID：" + d.data.id : d.data.name;
        }));

    var text = g.selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", "circlepacking_label")
        .style("fill-opacity", function(d) {
            return d.parent === root ? 1 : 0;
        })
        .style("display", function(d) {
            return d.parent === root ? "inline" : "none";
        })
        .text(function(d) {
            return d.data.name;
        });

    var node = g.selectAll("circle,text");

    zoomTo([root.x, root.y, root.r * 2 + margin]);

    function zoomTo(v) {
        var k = diameter / v[2]; view = v;
        node.attr("transform", function(d) {
            // console.log("translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")");
            return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")";
        });
        circle.attr("r", function(d) {
            return d.r * k;
        });
    }

    function getCloneRatioByName(data,id){
        var result = [];
        if(data.id === id){
            result.push(data.id,data.clone_ratio);
            return result;
        }else{
            if(data.children){
                for(var i = 0; i < data.children.length; i++) {
                    // console.log(d.name)
                    if (data.children[i].id === id) {
                        // return data.children[i].clone_ratio;
                        result.push(data.children[i].id,data.children[i].clone_ratio);
                        return result;
                    } else {
                        var findResult = getCloneRatioByName(data.children[i], id);
                        if(findResult) {
                            return findResult;
                        }
                    }
                }
                // console.log(typeof(projectdata.children[2].children) != "undefined")
            }
        }
    }

    drawCloneTableBelow(table_global, "project");
}

//根据筛选规则绘制气泡图连线
var showLine = function(links_local, type){
    if(type === "project"){
        if($("#dependsOn").prop("checked")){
            links_local = links_local.concat(dependsonLinks_global);
        }
        if($("#clone").prop("checked")){
            links_local = links_local.concat(cloneLinks_global);
        }
        if($("#coChange").prop("checked")){
            links_local = links_local.concat(cochangeLinks_global);
        }
    }

    // console.log(links_local);
    var temp_delete_number = 0;

    for(var i = links_local.length; i > 0; i--){
        var source_project = links_local[i - 1].source_projectBelong.split("_")[1];
        var target_project = links_local[i - 1].target_projectBelong.split("_")[1];
        var relation_type = links_local[i - 1].type;

        var flag_delete = false;
        var temp_flag_source = false;
        var temp_flag_target = false;

        for(var j = 0; j < projectList_global.length; j++){
            if(source_project === projectList_global[j]){
                temp_flag_source = true;
            }
        }

        for(var k = 0; k < projectList_global.length; k++){
            if(target_project === projectList_global[k] ){
                temp_flag_target = true;
            }
        }

        if(temp_flag_source === false || temp_flag_target === false){
            links_local.splice(i - 1, 1);
            temp_delete_number++;
            flag_delete = true;
        }

        if(relation_type === "clone" && flag_delete === false){
            var similarityValue = links_local[i - 1].similarityValue.toFixed(2);
            if($("#cloneSimilarity").prop("checked")){
                var temp_flag_clonesimilarity = false;

                if($("#similarityCompareSelectBelow").val() === "<=" &&
                    similarityValue >= $("#similaritybelow").val()){
                    if($("#similarityCompareSelectHigh").val() === "<=" &&
                        similarityValue <= $("#similarityhigh").val()){
                        temp_flag_clonesimilarity = true;
                    }else if($("#similarityCompareSelectHigh").val() === "<" &&
                        similarityValue < $("#similarityhigh").val()){
                        temp_flag_clonesimilarity = true;
                    }
                }else if($("#similarityCompareSelectBelow").val() === "<" &&
                    similarityValue > $("#similaritybelow").val()){
                    if($("#similarityCompareSelectHigh").val() === "<=" &&
                        similarityValue <= $("#similarityhigh").val()){
                        temp_flag_clonesimilarity = true;
                    }else if($("#similarityCompareSelectHigh").val() === "<" &&
                        similarityValue < $("#similarityhigh").val()){
                        temp_flag_clonesimilarity = true;
                    }
                }

                if(temp_flag_clonesimilarity === false ){
                    links_local.splice(i - 1, 1);
                    temp_delete_number++;
                    flag_delete = true;
                }
            }

            if($("#cloneTimes").prop("checked") && links_local[i - 1].cloneTimes >= $("#clonetimes").val()){
                links_local.splice(i - 1, 1);
                temp_delete_number++;
                flag_delete = true;
            }
        }

        if(relation_type === "dependson" && flag_delete === false){
            if($("#dependsOnTimes").prop("checked")){
                if($("#dependencyTimes").val() > links_local[i - 1].dependsOnTimes &&
                    $("#dependencyTimes").val() > links_local[i - 1].dependsByTimes){
                    links_local.splice(i - 1, 1);
                    temp_delete_number++;
                    flag_delete = true;
                }
            }
        }
    }
    drawLink(links_local);
}

    // .attr("class", "packageLink")
function drawLink(jsonLinks) {
    var svg1 = d3.select(".packageLink") .remove();
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var circleCoordinate = [];

    console.log(jsonLinks);

    var links = svg_global.append('g')
        .style('stroke', '#aaa')
        .attr("class", "packageLink")
        .selectAll('line')
        .data(jsonLinks)
        .enter().append('line')
        .attr("stroke-dasharray", function (d){
            // console.log(d);
            return d.bottom_package ? "20,2" : null;
        })
        .attr("stroke", function (d){
            return getTypeColor(d);
        })
        .attr("onclick", function(d){
            source_id = d.source_id.split("_")[1];
            target_id = d.target_id.split("_")[1];
            return "drawChildrenCloneLinks(\"" + source_id + "\", \"" + target_id + "\", \"" + d.type + "\")";
        })
        .call(text => text.append("title").text(function(d) {
            if(d.type === "clone"){
                return "Package1: " + d.source_name + "\nPackage2: " + d.target_name + "\nsimilarityValue: " + d.similarityValue.toFixed(2)
                    + "\npackageCochangeTimes: " + d.packageCochangeTimes
                    + "\npackageCloneCochangeTimes: " + d.packageCloneCochangeTimes
                    + "\nclonePairs: " + d.clonePairs;
            }else if(d.type === "dependson"){
                return "Package1: " + d.source_name + "\nPackage2: " + d.target_name + "\ndependsOnTypes: " + d.dependsOnTypes
                    + "\ndependsByTypes: " + d.dependsByTypes
                    + "\ndependsOnTimes: " + d.dependsOnTimes
                    + "\ndependsByTimes: " + d.dependsByTimes;
            }
        }));

    jsonLinks.forEach(function (d){
        d3.select("#" + d.source_id)
            .style("stroke",function (e){
                return getTypeColor(d);
            })
            .style("stroke-width","1.5px")

        d3.select("#" + d.target_id)
            .style("stroke",function (e){
                return getTypeColor(d);
            })
            .style("stroke-width","1.5px")

        //获取两个圆的transform属性（包含坐标信息）和半径
        var source_transform = d3.select("#" + d.source_id).attr("transform");
        var target_transform = d3.select("#" + d.target_id).attr("transform");
        var r1 = d3.select("#" + d.source_id).attr("r");
        var r2 = d3.select("#" + d.target_id).attr("r");

        //求初始情况下的两个圆心坐标
        var x1 = parseFloat(source_transform.slice(source_transform.indexOf("(") + 1, source_transform.indexOf(",")));
        var y1 = parseFloat(source_transform.slice(source_transform.indexOf(",") + 1, source_transform.indexOf(")")));
        var x2 = parseFloat(target_transform.slice(target_transform.indexOf("(") + 1, target_transform.indexOf(",")));
        var y2 = parseFloat(target_transform.slice(target_transform.indexOf(",") + 1, target_transform.indexOf(")")));

        //求斜率(考虑斜率正无穷问题)
        if(x1 !== x2){
            var k = (y2 - y1) / (x2 - x1);
        }else{
            var k;
        }

        if(typeof(k) !== "undefined"){
            //求偏移量
            var x1_offset = Math.sqrt((r1 * r1) / (k * k + 1));
            var y1_offset = Math.sqrt((r1 * r1) / (k * k + 1)) * k;
            var x2_offset = Math.sqrt((r2 * r2) / (k * k + 1));
            var y2_offset = Math.sqrt((r2 * r2) / (k * k + 1)) * k;
            if(x1 > x2){
                x1 -= x1_offset;
                y1 -= y1_offset;
                x2 += x2_offset;
                y2 += y2_offset;
            }else{
                x1 += x1_offset;
                y1 += y1_offset;
                x2 -= x2_offset;
                y2 -= y2_offset;
            }
        }else{
            if(y1 > y2){
                y1 -= r1;
                y2 += r2;
            }else if(y1 < y2){
                y1 += r1;
                y2 -= r2;
            }
        }

        var temp_coordinate = {};
        temp_coordinate["id"] = d.source_id + "_" + d.target_id;
        temp_coordinate["x1"] = x1;
        temp_coordinate["y1"] = y1;
        temp_coordinate["x2"] = x2;
        temp_coordinate["y2"] = y2;
        circleCoordinate.push(temp_coordinate);
    })

    function getTranslateX1(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        // console.log(link_id);
        // console.log(circleCoordinate.find((n) => n.id === link_id))
        return circleCoordinate.find((n) => n.id === link_id).x1;
    }

    function getTranslateY1(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).y1;
    }

    function getTranslateX2(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).x2;
    }

    function getTranslateY2(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).y2;
    }

    links.attr("x1", function (d) {
        return getTranslateX1(d.source_id, d.target_id) + diameter_global / 2;
    })
        .attr("y1", function (d) {
            return getTranslateY1(d.source_id, d.target_id) + diameter_global / 2;
        })
        .attr("x2", function (d) {
            return getTranslateX2(d.source_id, d.target_id) + diameter_global / 2;
        })
        .attr("y2", function (d) {
            return getTranslateY2(d.source_id, d.target_id) + diameter_global / 2;
        });

    // flag = false;
}

function clearLink(){
    drawCloneTableBelow(table_global, "project");
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var svg1 = d3.select(".packageLink") .remove();

    // flag = true;
}

//点击连线，获取子包关系,绘制图下方表格
function drawChildrenCloneLinks(package1Id, package2Id, type){
    if (type === "clone") {
        $.ajax({
            type : "GET",
            url : "/project/has/childrenlinks?package1Id=" + package1Id + "&package2Id=" + package2Id,
            success : function(result) {
                clearLink();
                // console.log(result);
                if(result.children_graphlinks.clone_links.length > 0){
                    showLine(result.children_graphlinks.clone_links, "package");
                }
                drawCloneTableBelow(result.table, "package");
            }
        });
    } else if (type === "dependson") {

    }
}

//多选下拉框，加载多项目
var showMultipleButton = function(){
    var value = $('#multipleProjectSelect').val();
    projectList_global = [];
    projectList_global = value;
    // console.log(projectList_global);
    var table_clear = d3.selectAll("table").remove();
    document.getElementById("showLineId").innerHTML = "显示关系";
    flag = true;
    projectGraphAjax(value);
}

function drawCloneTableBelow(tableData, type){
    // console.log(tableData);
    if(type === "project"){
        var temp_delete_number = 0;

        for(var i = tableData.clonefiles1.length; i > 0; i--){
            var source_project = tableData.clonefiles1[i - 1].projectBelong;
            var target_project = tableData.clonefiles2[i - 1].projectBelong;
            var temp_flag_source = false;
            var temp_flag_target = false;

            for(var j = 0; j < projectList_global.length; j++){
                if(source_project === projectList_global[j]){
                    temp_flag_source = true;
                }
            }

            for(var k = 0; k < projectList_global.length; k++){
                if(target_project === projectList_global[k] ){
                    temp_flag_target = true;
                }
            }

            if(temp_flag_source === false || temp_flag_target === false){
                tableData.clonefiles1.splice(i - 1, 1);
                tableData.clonefiles2.splice(i - 1, 1);
                temp_delete_number++;
            }
        }
    }

    var cleartable = d3.selectAll("table").remove();
    var clonefiles1 = tableData.clonefiles1;
    var clonefiles2 = tableData.clonefiles2;
    var nonclonefiles1 = tableData.nonclonefiles1;
    var nonclonefiles2 = tableData.nonclonefiles2;
    let html = "";
    html += "<table class = \"gridtable\">"
        + "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>总克隆占比</th><th>包克隆Cochange占比</th><th>克隆文件对数</th></tr>";
    if(type === "package"){
        var parentCochangeRate = "";
        if(tableData.packageCochangeTimes < 3){
            parentCochangeRate = tableData.packageCloneCochangeTimes + "/" + tableData.packageCochangeTimes + "=0.00";
        }else {
            parentCochangeRate = tableData.packageCloneCochangeTimes  + "/" + tableData.packageCochangeTimes  + "=" + ((tableData.packageCloneCochangeTimes  + 0.0) / tableData.packageCochangeTimes ).toFixed(2);
        }


        html += "<tr><th>" + tableData.parentpackage1 + "</th><th>"
            + tableData.relationNodes1 + "/" + tableData.allNodes1 + "=" + ((tableData.relationNodes1 + 0.0) / tableData.allNodes1).toFixed(2) + "</th><th>"
            + tableData.parentpackage2 + "</th><th>"
            + tableData.relationNodes2 + "/" + tableData.allNodes2 + "=" + ((tableData.relationNodes2 + 0.0) / tableData.allNodes2).toFixed(2) + "</th><th>"
            + "(" + tableData.relationNodes1 + "+" + tableData.relationNodes2 + ")/(" + tableData.allNodes1 + "+" + tableData.allNodes2 + ")=" + tableData.similarityValue.toFixed(2) + "</th><th>"
            + parentCochangeRate + "</th><th>"
            + tableData.clonePairs + "</th></tr>";
    }

    for(let i = 0; i < clonefiles1.length; i++){
        var packageCochangeTimes = clonefiles1[i].packageCochangeTimes;
        var packageCloneCochangeTimes = clonefiles1[i].packageCloneCochangeTimes;
        var clonePairs = clonefiles1[i].clonePairs;
        var path1CloneRate = clonefiles1[i].relationNodes1 + "/" + clonefiles1[i].allNodes1 + "=" + ((clonefiles1[i].relationNodes1 + 0.0) / clonefiles1[i].allNodes1).toFixed(2);
        var path2CloneRate = clonefiles2[i].relationNodes2 + "/" + clonefiles2[i].allNodes2 + "=" + ((clonefiles2[i].relationNodes2 + 0.0) / clonefiles2[i].allNodes2).toFixed(2);
        var cloneRate = "(" + clonefiles1[i].relationNodes1 + "+" + clonefiles2[i].relationNodes2 + ")/(" + clonefiles1[i].allNodes1 + "+" + clonefiles2[i].allNodes2 + ")=" + ((clonefiles1[i].relationNodes1 + clonefiles2[i].relationNodes2 + 0.0) / (clonefiles1[i].allNodes1 + clonefiles2[i].allNodes2)).toFixed(2);
        var cochangeRate = "";

        if(packageCochangeTimes < 3){
            cochangeRate = packageCloneCochangeTimes + "/" + packageCochangeTimes + "=0.00";
        }else {
            cochangeRate = packageCloneCochangeTimes  + "/" + packageCochangeTimes  + "=" + ((packageCloneCochangeTimes  + 0.0) / packageCochangeTimes ).toFixed(2);
        }

        if(type === "package"){
            html += "<tr><td>|---" + clonefiles1[i].name + "</td><td>" + path1CloneRate + "</td><td>|---" + clonefiles2[i].name + "</td>";
        }else{
            html += "<tr><td>" + clonefiles1[i].name + "</td><td>" + path1CloneRate + "</td><td>" + clonefiles2[i].name + "</td>";
        }

        html += "<td>" + path2CloneRate + "</td><td>" + cloneRate + "</td>";
        html += "<td>" + cochangeRate + "</td>";
        html += "<td>";
        if(clonePairs > 0) {
            html += "<a target='_blank' class='package' href='/cloneaggregation/details" +
                "?id1=" + clonefiles1[i].id +
                "&id2=" + clonefiles2[i].id +
                "&path1=" + clonefiles1[i].name +
                "&path2=" + clonefiles2[i].name +
                "&cloneNodes1=" + clonefiles1[i].relationNodes1 +
                "&allNodes1=" + clonefiles1[i].allNodes1 +
                "&cloneNodes2=" + clonefiles2[i].relationNodes2 +
                "&allNodes2=" + clonefiles2[i].allNodes2 +
                "&cloneCochangeTimes=" + packageCloneCochangeTimes +
                "&allCochangeTimes=" + packageCochangeTimes +
                "&clonePairs=" + clonePairs +
                "'>" + clonePairs + "</a>";
        }else {
            html += clonePairs;
        }
        html += "</td></tr>";
    }

    if(typeof(nonclonefiles1) !== "undefined"){
        if(nonclonefiles1.length > 0){
            for(let i = 0; i < nonclonefiles1.length; i++){
                html += "<tr><td>" + nonclonefiles1[i].name + "</td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td></tr>";
            }
        }

        if(nonclonefiles2.length > 0){
            for(let i = 0; i < nonclonefiles2.length; i++){
                html += "<tr><td> </td><td> </td><td>" + nonclonefiles2[i].name + "</td><td> </td><td> </td><td> </td><td> </td></tr>";
            }
        }
    }

    html += "</table>";
    $("#projectCloneTable").html(html);
}

var showLineButton = function(){
    var temp_links = [];
    // console.log(dependsonLinks_global);
    // console.log(cloneLinks_global);
    showLine(temp_links,"project");
}

var getTypeColor = function(d){
    if(d.type === "clone") {
        return d.similarityValue === 1 ? "#a52404" : d.similarityValue >= 0.9 ? "#e90c0c" : "#f16c6c";
    }else if(d.type === "dependson"){
        return "#34ace0";
    }
}

var CancelChildrenChecked = function(parent_name){
    console.log(parent_name);
    if(!$("#" + parent_name).is(":checked")){
        $("input[name = '" + parent_name + "_children" + "']").prop("checked", false);
    }
}



