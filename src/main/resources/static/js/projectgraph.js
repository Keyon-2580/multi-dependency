var flag = true;
var y = 0;

var projectId;
var jsonLinks_global;
var diameter_global;
var svg_global;
var g_global;
var projectList_global;

var projectgraph = function () {
    return {
        init : function() {
            loaddata();
        }
    }
}

//加载数据
var loaddata = function () {
    var projectlist = [];

    $.ajax({
        type : "GET",
        url : "/project/all",
        success : function(result) {
            for(x in result){
                // projectlist.push(x);
                var name_temp = {};
                // console.log(x);
                name_temp["id"] = x;
                name_temp["name"] = result[x].name;
                projectlist.push(name_temp);
            }

            // projectlist_guava.map((item,index) => {
            //     if(item.name === "guava"){
            //         guava_id = item.id;
            //     }
            // })

            var html = ""

            html += "<select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
            for(var i = 0; i < projectlist.length; i++) {
                if (i === 0) {
                    html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                } else {
                    html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                }
            }
            html += "</select>";
            html += "<button id = \"multipleProjectsButton\" type=\"button\" onclick= showMultipleButton()>加载项目</button>\n";

            // console.log(html)
            $("#projectToGraph_util").html(html);

            var temp_array = [];
            temp_array.push(projectlist[0].id);
            projectList_global = temp_array.concat();
            projectGraphAjax(temp_array);

            // Loop_ajax(0, projectlist);
        }
    })

    //多项目加载（暂不需要）
    // function Loop_ajax(index, projectlist) {
    //     if (index < projectlist.length) {
    //         $.ajax({
    //             type : "GET",
    //             url : mainUrl + "/has?projectId=" + projectlist[index] + "&showType=graph",
    //             success : function(result) {
    //                 resultjson = result;
    //                 // console.log(projectlist[index])
    //                 // console.log("projectToGraph_" + projectlist[index])
    //                 projectToGraph(resultjson,"projectToGraph_" + projectlist[index]);
    //                 if (index < projectlist.length) {
    //                     Loop_ajax(index + 1, projectlist);
    //                 }
    //             }
    //         })
    //     }
    // }
}

//绘制气泡图
var projectToGraph = function(result,divId){
    // console.log(result);
    var projectdata = result[0].result;
    var jsonLinks = result[1].links;

    // console.log(jsonLinks);

    jsonLinks_global = jsonLinks;

    // console.log(delete_index);
    //
    // if(typeof(jsonLinks) !== "undefined"){
    //     // console.log(typeof(jsonLinks));
    //     // console.log(jsonLinks);
    //
    // }

    var svg = d3.select("#" + divId)
            .attr("width", 1500)
            .attr("height", 1500),
            // .call(d3.zoom().on("zoom", function () {
            //     svg.attr("transform", d3.event.transform)
            // })),
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

                // console.log(ratio)
                // console.log(id)
                if(ratio === 0){
                    // console.log("white")
                    return null;
                }else{
                    return color_clone(ratio);
                }
            }
            // return d.children ? color(d.depth/(d.depth+10))  : color_clone(getCloneRatioByName(projectdata,d.data.name));
        })
        .attr("id", function (d) {
            return d.data.id;
        })
        .call(text => text.append("title").text(function(d) {
            return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name  + "\nID：" + d.data.id : d.data.name;
        }));

    //点击后缩放（暂不需要）
    // .on("click", function(d) {
    //         if (focus !== d) zoom(d), d3.event.stopPropagation();
    //     })

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

    // svg
    //     .style("background", "#88f1ce")
    //     .on("click", function() {
    //         zoom(root);
    //     });

    // console.log(root);
    zoomTo([root.x, root.y, root.r * 2 + margin]);

    //缩放函数（暂不需要）
    // function zoom(d) {
    //     g.selectAll("circle")
    //         .style("stroke","")
    //         .style("stroke-width","")
    //     var svg1 = d3.select(".packageLink") .remove();
    //     flag = true;
    //
    //     if(!d.children){
    //         d = d.parent;
    //     }
    //     var focus0 = focus; focus = d;
    //
    //     var transition = d3.transition()
    //         .duration(d3.event.altKey ? 7500 : 750)
    //         .tween("zoom", function(d) {
    //             var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
    //             return function(t) {
    //                 zoomTo(i(t));
    //             };
    //         });
    //
    //     transition.selectAll("text")
    //         .filter(function(d) {
    //             return d.parent === focus || this.style.display === "inline";
    //         })
    //         .style("fill-opacity", function(d) {
    //             return d.parent === focus ? 1 : 0;
    //         })
    //         .on("start", function(d) {
    //             if (d.parent === focus) this.style.display = "inline";
    //         })
    //         .on("end", function(d) {
    //             if (d.parent !== focus) this.style.display = "none";
    //         });
    // }
    //
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

    // function getCloneDataByName(data,name){
    //     var result = [];
    //     for(var i = 0; i < data.length; i++){
    //         var import_result = findNameInImports(data[i].imports,name);
    //         if(data[i].name === name){
    //             return data[i].imports;
    //         }else if(import_result){
    //             var temp = {};
    //             temp["name"] = data[i].name;
    //             temp["clone_type"] = import_result.clone_type;
    //             result.push(temp);
    //         }
    //     }
    //     return result;
    // }

    // function findNameInImports(data,name){
    //     for(var i = 0; i < data.length; i++){
    //         if(data[i].name === name){
    //             return data[i];
    //         }
    //     }
    //     return null;
    // }
    //
    // function setCloneTitle(data){
    //     if(data.length > 0){
    //         var result = "\n克隆关系:";
    //         for(var i = 0; i < data.length; i++){
    //             result += "\n{\n克隆文件:";
    //             result += data[i].name;
    //             result += "\n克隆类型:";
    //             result += data[i].clone_type;
    //             result += "\n}";
    //         }
    //         return result;
    //     }else{
    //         return null;
    //     }
    // }
}

//绘制气泡图连线
var showLine = function(){
    console.log(jsonLinks_global.length);
    var jsonLinks_local = jsonLinks_global.concat();
    var temp_delete_number = 0;

    for(var i = jsonLinks_local.length; i > 0; i--){
        var source_project = jsonLinks_local[i - 1].source_projectBelong.split("_")[1];
        var target_project = jsonLinks_local[i - 1].target_projectBelong.split("_")[1];
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
            jsonLinks_local.splice(i - 1, 1);
            temp_delete_number++;
        }
    }
    console.log(jsonLinks_local.length);

    if(typeof(jsonLinks_local) !== "undefined" && (temp_delete_number !== jsonLinks_global.length)){
        if(flag){
            drawLink(jsonLinks_local);
            document.getElementById("showLineId").innerHTML = "显示包克隆关系";
        }else{
            clearLink();
            document.getElementById("showLineId").innerHTML = "显示包克隆关系";
        }
    }
}

function drawLink(jsonLinks) {
    var links = svg_global.append('g')
        .style('stroke', '#aaa')
        .attr("class", "packageLink")
        .selectAll('line')
        .data(jsonLinks)
        .enter().append('line')
        .attr("onclick", function(d){
            source_id = d.source_id.split("_")[1];
            target_id = d.target_id.split("_")[1];
            return "drawChildrenLinks(\"" + source_id + "\", \"" + target_id + "\")";
        });


    function getTranslateX(translateText) {
        var start = translateText.indexOf("(");
        var comma = translateText.indexOf(",");
        return parseFloat(translateText.slice(start + 1, comma));
    }

    function getTranslateY(translateText) {
        var comma = translateText.indexOf(",");
        var end = translateText.indexOf(")");
        return parseFloat(translateText.slice(comma + 1, end));
    }

    function getCircleTransform(id) {
        // return d3.select("#" + id.replace(/\./g, '\\.')).attr("transform");
        // console.log(d3.select("#L1\\.M0\\.L1\\.M0").attr("transform"));
        d3.select("#" + id)
            .style("stroke","#d62728")
            .style("stroke-width","1.5px")
        return d3.select("#" + id).attr("transform");
    }
    links.attr("x1", function (d) {
        var test = getCircleTransform(d.source_id);
        return getTranslateX(getCircleTransform(d.source_id)) + diameter_global / 2;
    })
        .attr("y1", function (d) {
            return getTranslateY(getCircleTransform(d.source_id)) + diameter_global / 2;
        })
        .attr("x2", function (d) {
            return getTranslateX(getCircleTransform(d.target_id)) + diameter_global / 2;
        })
        .attr("y2", function (d) {
            return getTranslateY(getCircleTransform(d.target_id)) + diameter_global / 2;
        });

    flag = false;
}

function clearLink(){
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","")
    var svg1 = d3.select(".packageLink") .remove();

    flag = true;
}

function drawChildrenLinks(package1Id, package2Id){
    clearLink();
    $.ajax({
        type : "GET",
        url : "/project/has/childrenlinks?package1Id=" + package1Id + "&package2Id=" + package2Id,
        success : function(result) {
            drawLink(result);
        }
    });
}

//多选下拉框，加载多项目
var showMultipleButton = function(){
    var value = $('#multipleProjectSelect').val();
    projectList_global = [];
    projectList_global = value;
    console.log(projectList_global);
    projectGraphAjax(value);
}

//调用接口请求数据
var projectGraphAjax = function(projectIds){
    var projectList = {};
    var projectIds_array = [];

    for(var i = 0; i < projectIds.length; i++){
        var tempId = {};
        tempId["id"] = projectIds[i];
        projectIds_array.push(tempId);
    }

    projectList["projectIds"] = projectIds_array;
    projectList["showType"] = "graph";

    // console.log(projectList);

    // var projectList={
    //     "projectIds": [
    //         {
    //             "id" : projectId
    //         }
    //     ],
    //     "showType": "graph"
    // }
    $.ajax({
        type:"POST",
        url : "/project/has",
        contentType: "application/json", //必须这样写
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            console.log(result);
            // resultjson = result;
            // console.log(projectlist[index])
            // console.log("projectToGraph_" + projectlist[index])
            projectToGraph(result,"projectToGraphSvg");
        }
    })
}




