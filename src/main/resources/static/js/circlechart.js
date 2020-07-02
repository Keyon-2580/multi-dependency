// Avoid `console` errors in browsers that lack a console.
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeline', 'timelineEnd', 'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

var diameter = 1800,
    radius = diameter / 2,
    innerRadius = radius - 300;

var cluster = d3.layout.cluster()
    .size([360, innerRadius])
    .sort(null)
    .value(function(d) { return d.size; });

var bundle = d3.layout.bundle();

var line = d3.svg.line.radial()
    .interpolate("bundle")
    .tension(.85)
    .radius(function(d) { return d.y; })
    .angle(function(d) { return d.x / 180 * Math.PI; });


// var svg2 = d3.select("#svg").append("svg")
//     .attr("width", diameter)
//     .attr("height", diameter)
//     .attr("id", "svg2")
//     .append("g")
//     .attr("transform", "translate(" + radius + "," + radius + ")");

var svg = d3.select("#groupCytoscape2").append("svg")
    .attr("width", diameter)
    .attr("height", diameter)
    .attr("id", "svg1")
    .append("g")
    .attr("transform", "translate(" + radius + "," + radius + ")");


var link = svg.append("g").selectAll(".link"),
    node = svg.append("g").selectAll(".node");

// var changes,
//     old_nodes,
//     old_links,
//     old_node =  svg2.append("g").selectAll(".node"),
//     old_link = svg2.append("g").selectAll(".link");

d3.json("../static/data/node.json", function(error, root) {
    changes = root.children
    console.log(changes)
});

d3.json("../static/data/link.json", function(error, classes) {
    if (error) throw error;

    var nodes = cluster.nodes(packageHierarchy(classes)),
        links = packageImports(nodes);

    link = link
        .data(bundle(links))
        .enter().append("path")
        .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
        .attr("class", "link")
        .attr("d", line);

    node = node
        .data(nodes.filter(function(n) { return !n.children; }))
        .enter().append("text")
        // .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
        //                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
        .attr("class", "node")
        .attr("dy", ".31em")
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
        .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
        .text(function(d) { return d.key; })
        .on("mouseover", mouseovered)
        .on("mouseout", mouseouted);
});

//      d3.json("data/z_g530_5.1_acdc_clustered.json", function(error, classes) {
//     d3.json("data/s6_6.0_acdc_clustered.json", function(error, classes) {
//         if (error) throw error;
//         old_nodes = cluster.nodes(packageHierarchy(classes)),
//             old_links = packageImports(old_nodes);
//
//         old_link = old_link
//             .data(bundle(old_links))
//             .enter().append("path")
//             .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
//             .attr("class", "link")
//             .attr("d", line);
//
//         old_node = old_node
//             .data(old_nodes.filter(function(n) { return !n.children; }))
//             .enter().append("text")
//             // .style("fill", function (d) {
//                 // if (checkChangeType(d.key, changes)== 5) { return '#b40017';}})
//             .attr("class", "node")
//             .attr("dy", ".31em")
//             .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
//             .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
//             .text(function(d) { return d.key; })
//             .on("mouseover", mouseovered)
//             .on("mouseout", mouseouted);
//     });

String.prototype.replaceAt=function(index, replacement) {
    return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
}

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};


var width = 360;
var height = 360;
var radius = Math.min(width, height) / 2;
var donutWidth = 75;
var legendRectSize = 18;                                  // NEW
var legendSpacing = 4;

// var color = d3.scale.ordinal()
//     .domain(["Removed Component", "Changed Component", "New Component", "Unchanged Component"])
//     .range(["#b40017", "#b47500", "#00b40a", "#D3D3D3"]);

var legend = d3.select('svg')
    .append("g")
    .selectAll("g")
    // .data(color.domain())
    //.enter()
    .append('g')
    .attr('class', 'legend')
    .attr('transform', function(d, i) {
        var height = legendRectSize;
        var x = 0;
        var y = (i+1) * height;
        return 'translate(' + x + ',' + y + ')';
    });

d3.select('svg')
    .select("g:nth-child(0)").append('text').text("Component Colors:");
//.attr('transform', 'translate(0,0)');


legend.append('rect')
    .attr('width', legendRectSize)
    .attr('height', legendRectSize)
// .style('fill', color)
// .style('stroke', color);

legend.append('text')
    .attr('x', legendRectSize + legendSpacing)
    .attr('y', legendRectSize - legendSpacing)
    .text(function(d) { return d; });

// function checkChangeType(value, array) {
//     console.log(array)
//     for (var i = 0; i < array.length; i++){
//         //console.log(array[i])
//         var child_name = array[i].name,
//             child_type = array[i].type,
//             new_name = child_name.replace(/\./g,"_");
//         //console.log(new_name)
//         if(value == new_name)
//         {
//             return child_type
//         }
//
//     }
// }


// function  checkOldLink(link, old_links) {
//     for (var i = 0; i < old_links.length; i++){
//         if ((link.target.key == old_links[i].target.key) && (link.source.key == old_links[i].source.key))
//             return false
//     }
//     console.log(link)
//     return true
// }


function mouseovered(d) {
    node
        .each(function(n) { n.target = n.source = false; });

    link
        .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
        .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
        .filter(function(l) { return l.target === d || l.source === d; })
        // .style("stroke", function (l) { if (checkOldLink(l, old_links)) { return '#b400ad';}})
        .style("stroke", "#e0230a")
        .each(function() { this.parentNode.appendChild(this); });

    node
        .classed("node--target", function(n) { return n.target; })
        .classed("node--source", function(n) { return n.source; });

    // old_node
    //     .each(function(n) { n.target = n.source = false; });
    //
    // old_link
    //     .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
    //     .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
    //     .filter(function(l) { return l.target === d || l.source === d; })
    //     .style("stroke", function (l) { if (checkOldLink(l, old_links)) { return '#b400ad';}})
    //     .each(function() { this.parentNode.appendChild(this); });
    //
    // old_node
    //     .classed("node--target", function(n) { return n.target; })
    //     .classed("node--source", function(n) { return n.source; });
}

function mouseouted(d) {
    link
        .classed("link--target", false)
        .classed("link--source", false)
        .style("stroke", 'DarkGray');

    node
        .classed("node--target", false)
        .classed("node--source", false);

    // old_link
    //     .classed("link--target", false)
    //     .classed("link--source", false)
    //     .style("stroke", 'DarkGray');
    //
    // old_node
    //     .classed("node--target", false)
    //     .classed("node--source", false);
}

d3.select(self.frameElement).style("height", diameter + "px");

// Lazily construct the package hierarchy from class names.
function packageHierarchy(classes) {
    var map = {};

    function find(name, data) {
        var node = map[name], i;
        if (!node) {
            node = map[name] = data || {name: name, children: []};
            if (name.length) {
                node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
                node.parent.children.push(node);
                node.key = name.substring(i + 1);
            }
        }
        return node;
    }

    classes.forEach(function(d) {
        find(d.name, d);
    });

    return map[""];
}

// Return a list of imports for the given array of nodes.
function packageImports(nodes) {
    var map = {},
        imports = [];

    // Compute a map from name to node.
    nodes.forEach(function(d) {
        map[d.name] = d;
    });

    // For each import, construct a link from the source to target node.
    nodes.forEach(function(d) {
        if (d.imports) d.imports.forEach(function(i) {
            imports.push({source: map[d.name], target: map[i]});
        });
    });

    return imports;
}
// Place any jQuery/helper plugins in here.
