var projectList_global;

var TreeMap = function (data_list) {
    var result = data_list[0].result;
    var smell = data_list[1].smell;
    var HEADER, OFFSET, color, h, height, svg, treemap, vis, w, width, zoom, zoomable_layer;

    OFFSET = 1;

    HEADER = 8;

    svg = d3.select("#treemap");

    width = svg
        .node()
        .getBoundingClientRect().width;

    height = svg
        .node()
        .getBoundingClientRect().height;

    w = width - 40;

    h = height - 40;

    treemap = d3.layout.treemap()
        .size([w, h]).value(function(node) {
            return node.size;
        })
        .padding([OFFSET + HEADER, OFFSET, OFFSET, OFFSET])
        .sort(function(a, b) {
            h = d3.ascending(a.height, b.height);
            if (h === 0) {
                return d3.ascending(a.size, b.size);
            }
            return h;
        });

    svg.attr({
        viewBox: (-width / 2) + " " + (-height / 2) + " " + width + " " + height
    });

    zoomable_layer = svg.append('g');

    zoom = d3.behavior
        .zoom()
        .scaleExtent([-Infinity, Infinity])
        .on('zoom', function() {
        return zoomable_layer.attr({
            transform: "translate(" + (zoom.translate()) + ")scale(" + (zoom.scale()) + ")"
        });
    });

    svg.call(zoom);

    vis = zoomable_layer.append('g').attr({
        transform: "translate(" + (-w / 2) + "," + (-h / 2) + ")"
    });

    color = d3.scale
        .linear()
        .domain([0, 7])
        .range([d3.rgb("#b73779"), d3.rgb("#fcfdbf")])
        .interpolate(d3.interpolateHcl);

// .range([d3.hcl(320, 0, 20), d3.hcl(200, 70, 80)])

    var aggregate, cells, compute_height, compute_heights, data, labels;

    aggregate = function(node) {
        if (node.children != null) {
            node.children.forEach(aggregate);
            return node.size = d3.sum(node.children, function(d) {
                return d.size;
            });
        }
    };

    aggregate(result);

    compute_height = function(node) {
        if (node.children != null) {
            node.children.forEach(compute_height);
            return node.height = 1 + d3.max(node.children, function(d) {
                return d.height;
            });
        } else {
            return node.height = 0;
        }
    };

    compute_height(result);

    data = treemap.nodes(result);

    compute_heights = function(node) {
        var bchildren, bmax, rchildren, rmax;
        if (node.children != null) {
            node.children.forEach(compute_heights);

            rmax = d3.max(node.children, function(c) {
                return c.x + c.dx;
            });

            rchildren = node.children.filter(function(d) {
                return (d.x + d.dx) >= rmax;
            });

            node.height_r = 1 + d3.max(rchildren, function(d) {
                return d.height_r;
            });

            bmax = d3.max(node.children, function(c) {
                return c.y + c.dy;
            });

            bchildren = node.children.filter(function(d) {
                return (d.y + d.dy) >= bmax;
            });

            return node.height_b = 1 + d3.max(bchildren, function(d) {
                return d.height_b;
            });
        } else {
            node.height_r = 0;
            return node.height_b = 0;
        }
    };

    compute_heights(result);

    data.sort(function(a, b) {
        return d3.ascending(a.depth, b.depth);
    });

    cells = vis.selectAll('.cell').data(data);

    cells.enter().append('rect')
        .attr({
            "class": 'cell',
            x: function(d) {
                return d.x;
            },
            y: function(d) {
                return d.y;
            },
            width: function(d) {
                var width = d.dx - 2 * OFFSET * d.height_r;
                return width >= 0 ? width : 0.1;
            },
            height: function(d) {
                var height = d.dy - 2 * OFFSET * d.height_b;
                return height < 0 ? 0.1 : height;
            },
            fill: function(d) {
                // return d.hasOwnProperty("children") ? color(d.depth) : d.clone ? "#ea7d5f" : "#fcfdbf";
                return d.hasOwnProperty("children") ? color(d.depth) : "#fcfdbf";
            },
            stroke: function(d) {
                // console.log(d.children.length);
                // console.log(d.children);
                // return "#fcfdbf";
                return color(6.1);
            }
        })
        .attr("id", function (d) {
            return d.id;
        })
        .attr("onmouseover", "showSmellGroupOnMouseOver(-1)")
        .classed('leaf', function(d) {
        return (d.children == null) || d.children.length === 0;
        })
        .call(text => text.append("title").text(function(d) {
            return d.hasOwnProperty("children") ? "ID：" + d.id.split("_")[1] + "\nPath：" + d.name + "\nDepth：" + d.depth
                : "ID：" + d.id.split("_")[1] + "\nPath：" + d.long_name + "\nDepth：" + d.depth;
        }));

    labels = vis.selectAll('.label').data(data.filter(function(d) {
        return (d.children != null) && d.children.length > 0;
    }));

    smell.forEach(function (item){
        var nodes = item.nodes;
        nodes.forEach(function (node){
            var smell = d3.select("#" + node.id)
                .attr("fill", "#ea7d5f")
                .attr("smell", 1)
                .attr("smellId", item.id)
                .attr("onmouseover", "showSmellGroupOnMouseOver(" + item.id + ")")
                .attr("smellGroup", item.name);
        });
    });

    $('#multipleProjectsButton').css('background-color', '#efefef');

    return labels.enter().append('text').text(function(d) {
        return ((d.dx - 2 * OFFSET * d.height_r) >= 30 && (d.dy - 2 * OFFSET * d.height_b) >= 8) ? d.name.split("/")[d.name.split("/").length - 2] : null;
    }).attr({
        "class": 'label',
        x: function(d) {
            return d.x;
        },
        y: function(d) {
            return d.y;
        },
        dx: 2,
        dy: '1em'
    });
}

var main = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

//加载数据
var loadPageData = function () {
    var projectlist = [];
    var projectIds = [];

    $.ajax({
        type : "GET",
        url : "/project/all/name",
        success : function(result) {
            for(var i = 0; i < result.length; i++){
                var name_temp = {};
                // console.log(x);
                name_temp["id"] = result[i].id;
                name_temp["name"] = result[i].name;
                projectlist.push(name_temp);

                var html = ""
                html += "<div class = \"treemap_div\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
                for(var i = 0; i < projectlist.length; i++) {
                    if (i === 0) {
                        html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    } else {
                        html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    }
                }
                html += "</select>";
                html += "<br><button id = \"multipleProjectsButton\" type=\"button\" class='common_button' style='margin-top: 15px' onclick= showMultipleButton()>加载项目</button></div>";

                html += "<div class = \"treemap_div\">"+
                    "<form role=\"form\">" +

                    "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Smell ：</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_Clone\"> Clone " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_CyclicDependency\"> Cyclic Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_HublikeDependency\"> Hublike Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnstableDependency\"> Unstable Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnusedComponent\"> Unused Component " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_ImplicitCrossModuleDependency\"> Implicit Cross Module Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_GodComponent\"> God Component " +
                    "</label>" +

                    "</p>";

                html += "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Level ：</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input name=\"level_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Module\"> Module " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_\"> Package " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_\"> File " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_\"> Type " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_ratio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Snippet\"> Snippet " +
                    "</label>" +

                    "</p>";

                html += "</form>" +
                    "</div>";

                $("#treemap_util").html(html);
                $(".selectpicker").selectpicker({
                    actionsBox:true,
                    countSelectedText:"已选中{0}项",
                    selectedTextFormat:"count > 2"
                })
            }
        }
    })
}

//多选下拉框，加载多项目
var showMultipleButton = function(){
    var value = $('#multipleProjectSelect').val();
    $('#multipleProjectsButton').css('background-color', '#f84634');
    projectList_global = [];
    projectList_global = value;
    projectGraphAjax(value);
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

    $.ajax({
        type:"POST",
        url : "/project/has/treemap",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            TreeMap(result);
        }
    });
}

//鼠标悬停时，显示属于同一组的文件
var showSmellGroupOnMouseOver = function (smellId){
    $("rect[smell=1]").css("fill", "#ea7d5f");
    if(smellId > 0){
        $("rect[smellId=" + smellId + "]").css("fill", "#af5247");
    }
}