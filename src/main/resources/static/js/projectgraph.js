var svg_global = {};
var diameter_global;
var g_global = {};
var flag = {};

var jsonLinks_global;

var projectgraph = function () {
    var mainUrl = "/project";

    var projectToGraph = function(result,divId){
        // console.log(result);
        var projectdata = result[0].result;
        var clonedata = result[1].clone;
        var jsonLinks = result[2].links;

        for(var i = 0; i < jsonLinks.length; i++){
            if(jsonLinks[i].source_projectBelong !== projectdata.id || jsonLinks[i].target_projectBelong !== projectdata.id){
                jsonLinks.splice(i,1);
            }
        }

        if(typeof(jsonLinks) !== "undefined"){
            jsonLinks_global[projectdata.id] = jsonLinks;
        }

        var svg = d3.select("#" + divId)
                .attr("width", 1500)
                .attr("height", 1500),
            margin = 20,
            diameter = +svg.attr("width"),
            g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

        svg_global[projectdata.id] = svg;
        g_global[projectdata.id] = g;
        diameter_global = diameter;
        flag[projectdata.id] = true;

        var color = d3.scaleLinear()
            .domain([0, 1])
            .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
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

        var circle = g.selectAll("circle")
            .data(nodes)
            .enter().append("circle")
            .attr("class", function(d) {
                return d.parent ? d.children ? "circlepacking_node" : "circlepacking_node circlepacking_node--leaf" : "circlepacking_node circlepacking_node--root";
            })
            .style("fill", function(d) {
                return d.children ? color(d.depth/(d.depth+14)) : (getCloneBooleanByName(projectdata,d.data.name) ? "\t#FFB6C1" : null);
            })
            .attr("id", function (d) {
                return d.data.id;
            })
            .on("click", function(d) {
                if (focus !== d) zoom(d), d3.event.stopPropagation();
            })
            .call(text => text.append("title").text(function(d) {
                return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name + setCloneTitle(getCloneDataByName(clonedata,d.data.long_name)): d.data.name;
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
            .style("font-size", function(d) {
                return d.children ? color(d.depth) : (getCloneBooleanByName(projectdata,d.data.name) ? "\t#FFB6C1" : null);
            })
            .text(function(d) {
                return d.data.name;
            });

        var node = g.selectAll("circle,text");

        svg
            .style("background", "white")
            .on("click", function() {
                zoom(root);
            });

        zoomTo([root.x, root.y, root.r * 2 + margin]);

        function zoom(d) {
            g_global.selectAll("circle")
                .style("stroke","")
                .style("stroke-width","")
            var svg1 = d3.select(".packageLink") .remove();
            flag = true;

            if(!d.children){
                d = d.parent;
            }
            var focus0 = focus; focus = d;

            var transition = d3.transition()
                .duration(d3.event.altKey ? 7500 : 750)
                .tween("zoom", function(d) {
                    var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
                    return function(t) {
                        zoomTo(i(t));
                    };
                });

            transition.selectAll("text")
                .filter(function(d) {
                    return d.parent === focus || this.style.display === "inline";
                })
                .style("fill-opacity", function(d) {
                    return d.parent === focus ? 1 : 0;
                })
                .on("start", function(d) {
                    if (d.parent === focus) this.style.display = "inline";
                })
                .on("end", function(d) {
                    if (d.parent !== focus) this.style.display = "none";
                });
        }

        function zoomTo(v) {
            var k = diameter / v[2]; view = v;
            node.attr("transform", function(d) {
                return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")";
            });
            circle.attr("r", function(d) {
                return d.r * k;
            });
        }

        function getCloneBooleanByName(data,name){
            if(data.name === name){
                return data.clone;
            }else{
                if(data.children){
                    for(var i = 0; i < data.children.length; i++) {
                        // console.log(d.name)
                        if (data.children[i] === name) {
                            return data.children[i].clone;
                        } else {
                            var findResult = getCloneBooleanByName(data.children[i], name);
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

    var loaddata = function () {
        var resultjson = null;
        var projectlist = new Array();
        var projectId = null;

        $.ajax({
            type : "GET",
            url : mainUrl + "/all",
            success : function(result) {
                for(x in result){
                    projectlist.push(x);
                }

                var html = ""

                for(var i = 0; i < projectlist.length; i++){
                    html += "<svg id = projectToGraph_" + projectlist[i] + "></svg>";
                    html += "<button class = \"showLineButton\" type=\"button\" onclick= showLine(\"" + projectlist[i] + "\") id = showLineButton_" + projectlist[i] + ">显示包克隆关系</button>";
                    // console.log(html);
                    $("#projectToGraph").html(html);

                    // console.log(projectlist[i])
                }

                Loop_ajax(0, projectlist);
            }
        })

        function Loop_ajax(index, projectlist) {
            if (index < projectlist.length) {
                $.ajax({
                    type : "GET",
                    url : mainUrl + "/has?projectId=" + projectlist[index],
                    success : function(result) {
                        resultjson = result;
                        console.log(projectlist[index])
                        console.log("projectToGraph_" + projectlist[index])
                        projectToGraph(resultjson,"projectToGraph_" + projectlist[index]);
                        if (index < projectlist.length) {
                            Loop_ajax(index + 1, projectlist);
                        }
                    }
                })
            }
        }
    }

    var _graph = function(){
        loaddata();
        // projectToGraph(result,divId);
    }
    return {
        init : function() {
            _graph();
        }
    }
}

var showLine = function(projectId){
    var links;
    function drawLink() {
        links = svg_global["id_" + projectId].append('g')
            .style('stroke', '#aaa')
            .attr("class", "packageLink")
            .selectAll('line')
            .data(jsonLinks_global["id_" + projectId])
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
        g_global["id_" + projectId].selectAll("circle")
            .style("stroke","")
            .style("stroke-width","")
        var svg1 = d3.select(".packageLink") .remove();
    }

    if(typeof(jsonLinks_global["id_" + projectId]) !== "undefined"){
        if(flag){
            drawLink();
            document.getElementById("showLineButton_" + projectId).innerHTML = "显示包克隆关系"
            flag["id_" + projectId] = false;
        }else{
            clearLink();
            document.getElementById("showLineButton_" + projectId).innerHTML = "显示包克隆关系"
            flag["id_" + projectId] = true;
        }
    }
}


