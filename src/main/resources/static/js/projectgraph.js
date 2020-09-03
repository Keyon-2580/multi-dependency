var projectgraph = function () {
    var mainUrl = "/project";

    var projectToGraph = function(result,divId){
        // console.log(result);
        var projectdata = result;
        var svg = d3.select("#" + divId)
                .attr("width", 1800)
                .attr("height", 1800),
            margin = 20,
            diameter = +svg.attr("width"),
            g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

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
            .attr("class", function(d) { return d.parent ? d.children ? "circlepacking_node" : "circlepacking_node circlepacking_node--leaf" : "circlepacking_node circlepacking_node--root"; })
            .style("fill", function(d) {return d.children ? color(d.depth/(d.depth+19)) : (getCloneByName(projectdata,d.data.name) ? "\t#FFB6C1" : null); })
            .on("click", function(d) { if (focus !== d) zoom(d), d3.event.stopPropagation(); })
            .call(text => text.append("title").text(function(d) { return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name : d.data.name; }));

        var text = g.selectAll("text")
            .data(nodes)
            .enter().append("text")
            .attr("class", "circlepacking_label")
            .style("fill-opacity", function(d) { return d.parent === root ? 1 : 0; })
            .style("display", function(d) { return d.parent === root ? "inline" : "none"; })
            .style("font-size", function(d) {
                return d.children ? color(d.depth) : (getCloneByName(projectdata,d.data.name) ? "\t#FFB6C1" : null); })
            .text(function(d) { return d.data.name; });

        var node = g.selectAll("circle,text");

        svg
            .style("background", "white")
            .on("click", function() { zoom(root); });

        zoomTo([root.x, root.y, root.r * 2 + margin]);

        function zoom(d) {
            if(!d.children){
                d = d.parent;
            }
            var focus0 = focus; focus = d;

            var transition = d3.transition()
                .duration(d3.event.altKey ? 7500 : 750)
                .tween("zoom", function(d) {
                    var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
                    return function(t) { zoomTo(i(t)); };
                });

            transition.selectAll("text")
                .filter(function(d) { return d.parent === focus || this.style.display === "inline"; })
                .style("fill-opacity", function(d) { return d.parent === focus ? 1 : 0; })
                .on("start", function(d) { if (d.parent === focus) this.style.display = "inline"; })
                .on("end", function(d) { if (d.parent !== focus) this.style.display = "none"; });
        }

        function zoomTo(v) {
            var k = diameter / v[2]; view = v;
            node.attr("transform", function(d) { return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")"; });
            circle.attr("r", function(d) { return d.r * k; });
        }

        function getCloneByName(data,name){
            if(data.name === name){
                return data.clone;
            }else{
                if(data.children){
                    for(var i = 0; i < data.children.length; i++) {
                        // console.log(d.name)
                        if (data.children[i] === name) {
                            return data.children[i].clone;
                        } else {
                            var findResult = getCloneByName(data.children[i], name);
                            if(findResult) {
                                return findResult;
                            }
                        }
                    }
                    // console.log(typeof(projectdata.children[2].children) != "undefined")
                }
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
                        resultjson = result[0].result;
                        // console.log(projectlist[index])
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


