var flag = true;
var y = 0;

var projectId;
var jsonLinks_global;
var diameter_global;
var svg_global;
var g_global;

var projectgraph = function () {
    return {
        init : function() {
            loaddata();
        }
    }
}

//加载数据
var loaddata = function () {
    var resultjson = null;
    var projectlist = [];

    $.ajax({
        type : "GET",
        url : "/project/all",
        success : function(result) {
            for(x in result){
                var name_temp = {};
                // projectlist.push(x);
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
            html += "<select onchange=\"gradeChange(this.options[this.options.selectedIndex].value)\" >";
            for(var i = 0; i < projectlist.length; i++){
                if(i === 0){
                    html += "<option id = \"project_graph_select\" selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                }else{
                    html += "<option id = \"project_graph_select\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                }
                // html += "<button class = \"showLineButton\" type=\"button\" onclick= showLine(\"" + projectlist[i] + "\") id = showLineButton_" + projectlist[i] + ">显示包克隆关系</button>";
            }
            html += "</select>";
            html += "";
            console.log(html)
            $("#projectToGraph_util").html(html);

            projectGraphAjax(projectlist[0].id);

            // Loop_ajax(0, projectlist);
        }
    })
    function Loop_ajax(index, projectlist) {
        if (index < projectlist.length) {
            $.ajax({
                type : "GET",
                url : mainUrl + "/has?projectId=" + projectlist[index] + "&showType=graph",
                success : function(result) {
                    resultjson = result;
                    // console.log(projectlist[index])
                    // console.log("projectToGraph_" + projectlist[index])
                    projectToGraph(resultjson,"projectToGraph_" + projectlist[index]);
                    if (index < projectlist.length) {
                        Loop_ajax(index + 1, projectlist);
                    }
                }
            })
        }
    }
}

//绘制气泡图
var projectToGraph = function(result,divId){
    // console.log(result);
    var projectdata = result[0].result;
    var clonedata = result[1].clone;
    var jsonLinks = result[2].links;

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

                console.log(ratio)
                console.log(id)
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

    function getCloneDataByName(data,name){
        var result = [];
        for(var i = 0; i < data.length; i++){
            var import_result = findNameInImports(data[i].imports,name);
            if(data[i].name === name){
                return data[i].imports;
            }else if(import_result){
                var temp = {};
                temp["name"] = data[i].name;
                temp["clone_type"] = import_result.clone_type;
                result.push(temp);
            }
        }
        return result;
    }

    function findNameInImports(data,name){
        for(var i = 0; i < data.length; i++){
            if(data[i].name === name){
                return data[i];
            }
        }
        return null;
    }

    function setCloneTitle(data){
        if(data.length > 0){
            var result = "\n克隆关系:";
            for(var i = 0; i < data.length; i++){
                result += "\n{\n克隆文件:";
                result += data[i].name;
                result += "\n克隆类型:";
                result += data[i].clone_type;
                result += "\n}";
            }
            return result;
        }else{
            return null;
        }
    }
}

//绘制气泡图连线
var showLine = function(){
    var links;
    function drawLink() {
        links = svg_global.append('g')
            .style('stroke', '#aaa')
            .attr("class", "packageLink")
            .selectAll('line')
            .data(jsonLinks_global)
            .enter().append('line');


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
    }

    function clearLink(){
        g_global.selectAll("circle")
            .style("stroke","")
            .style("stroke-width","")
        var svg1 = d3.select(".packageLink") .remove();
    }

    // console.log(projectId);
    // console.log(project_index);
    // console.log(project_index["id_" + projectId]);
    console.log(jsonLinks_global);

    if(typeof(jsonLinks_global) !== "undefined"){
        if(flag){
            drawLink();
            document.getElementById("showLineId").innerHTML = "显示包克隆关系";
            flag = false;
        }else{
            clearLink();
            document.getElementById("showLineId").innerHTML = "显示包克隆关系";
            flag = true;
        }
    }
}

//单选下拉框，切换项目
var gradeChange = function(projectId){
    projectGraphAjax(projectId);
}

//调用接口请求数据
var projectGraphAjax = function(projectId){
    $.ajax({
        type : "GET",
        url : "/project/has?projectId=" + projectId + "&showType=graph",
        success : function(result) {
            // resultjson = result;
            // console.log(projectlist[index])
            // console.log("projectToGraph_" + projectlist[index])
            projectToGraph(result,"projectToGraphSvg");
        }
    })
}




