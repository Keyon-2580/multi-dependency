var projecttree = function () {
    var ShowTree = function(data, divId){
        var width = 1700,
            height = 1700;

        var force = d3.layout.force()
            .size([width, height])
            .on("tick", tick);

        var svg = d3.select("#" + divId).append("svg")
            .attr("width", width)
            .attr("height", height);

        var link = svg.selectAll(".projecttree_link"),
            node = svg.selectAll(".projecttree_node");

        update();

        function update() {
            var nodes = flatten(data),
                links = d3.layout.tree().links(nodes);

            // Restart the force layout.
            force
                .nodes(nodes)
                .links(links)
                .start();

            // Update the links…
            link = link.data(links, function(d) { return d.target.id; });

            console.log(link)

            // Exit any old links.
            link.exit().remove();

            // Enter any new links.
            link.enter().insert("line", ".projecttree_node")
                .attr("class", "projecttree_link")
                .attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            // Update the nodes…
            node = node.data(nodes, function(d) { return d.id; }).style("fill", color);

            // Exit any old nodes.
            node.exit().remove();

            // Enter any new nodes.
            node.enter().append("circle")
                .attr("class", "projecttree_node")
                .attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; })
                .attr("r", function(d) { return Math.sqrt(d.size) / 10 || 4.5; })
                .style("fill", color)
                .on("click", click)
                .call(force.drag);
        }

        function tick() {
            link.attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            node.attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
        }

// Color leaf nodes orange, and packages white or blue.
        function color(d) {
            return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
        }

// Toggle children on click.
        function click(d) {
            if (!d3.event.defaultPrevented) {
                if (d.children) {
                    d._children = d.children;
                    d.children = null;
                } else {
                    d.children = d._children;
                    d._children = null;
                }
                update();
            }
        }

// Returns a list of all nodes under the root.
        function flatten(root) {
            var nodes = [], i = 0;

            function recurse(node) {
                if (node.children) node.children.forEach(recurse);
                if (!node.id) node.id = ++i;
                nodes.push(node);
            }

            recurse(root);
            return nodes;
        }
    }

    var LoadDataOfTree = function(){
        $.ajax({
            type : "GET",
            url : "/project/has?projectId=4",
            success : function(result) {
                resultjson = result;
                // console.log(projectlist[index])
                // console.log("projectToGraph_" + projectlist[index])
                ShowTree(resultjson[0].result,"ProjectToTree");
            }
        })
    }

    return {
        init : function() {
            LoadDataOfTree();
        }
    }
}

