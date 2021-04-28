let drawSmellGraph = function (json_data) {
    const CYCLIC_DEPENDENCY = "CyclicDependency";
    const UNUSED_INCLUDE = "UnusedInclude";
    const data = {};
    let smellType = json_data["smellType"];
    const nodeTip = new G6.Tooltip({
        offsetX: 10,
        offsetY: 0,
        fixToNode: [1, 0],
        trigger: 'click',
        itemTypes: ['node'],
        getContent: (e) => {
            const outDiv = document.createElement('div');
            outDiv.style.width = 'fit-content';
            outDiv.innerHTML = `
            <h5>name: ${e.item.getModel().name}</h5>
            <h5>path: ${e.item.getModel().path}</h5>`;
            return outDiv;
        },
    });
    const edgeTip = new G6.Tooltip({
        offsetX: 10,
        offsetY: 0,
        fixToNode: [1, 0],
        trigger: 'click',
        itemTypes: ['edge'],
        getContent: (e) => {
            const outDiv = document.createElement('div');
            switch (smellType) {
                case CYCLIC_DEPENDENCY:
                    let dependsOnTypes = e.item.getModel().dependsOnTypes;
                    let str = ``;
                    for(let key in dependsOnTypes){
                        str += `<ul><li>${key}: ${dependsOnTypes[key]}</li></ul>`;
                    }
                    outDiv.style.width = 'fit-content';
                    outDiv.innerHTML = `
                        <h5>Source: (${e.item.getModel().source_label})${e.item.getModel().source_name}</h5>
                        <h5>Target: (${e.item.getModel().target_label})${e.item.getModel().target_name}</h5>
                        <h5>Relation: DependsOn(${e.item.getModel().times})</h5>` + str;
                    break;
                case UNUSED_INCLUDE:
                    outDiv.style.width = 'fit-content';
                    outDiv.innerHTML = `
                        <h5>Source: (${e.item.getModel().source_label})${e.item.getModel().source_name}</h5>
                        <h5>Target: (${e.item.getModel().target_label})${e.item.getModel().target_name}</h5>
                        <h5>Relation: Include</h5>`;
                    break;
                default:
                    break;
            }
            return outDiv;
        },
    });
    const container = document.getElementById('smellGraph');
    const width = container.scrollWidth;
    const height = container.scrollHeight || 500;
    const graph = new G6.Graph({
        container: 'smellGraph',
        width,
        height,
        modes: {
            default: ['drag-canvas', 'drag-node', 'zoom-canvas'],
        },
        plugins: [nodeTip, edgeTip],
        animate: true,
        defaultNode: {
            size: 30,
            style: {
                lineWidth: 2,
                stroke: '#5B8FF9',
                fill: '#C6E5FF',
            },
        },
        defaultEdge: {
            size: 1,
            color: '#A9A9A9',
            style: {
                endArrow: {
                    path: 'M 0,0 L 8,4 L 8,-4 Z',
                    fill: '#A9A9A9',
                },
            },
            labelCfg: {
                autoRotate: true,
            },
        },
        layout: {
            type: 'fruchterman',
            gpuEnabled: true,
            maxIteration: 300,
            preventOverlap: true,
        },
        nodeStateStyles: {
            coreNode: {
                lineWidth: 2,
                stroke: '#DC143C',
                fill: '#FFC0CB',
            },
        },
    });
    let coreNodeId = json_data["coreNode"];
    let links = json_data["edges"];
    let edges = [];
    links.forEach(function (link){
        let edge = link;
        let reverseLink = links.find((l) => (l.source === link.target && l.target === link.source));
        if (reverseLink != null) {
            edge["type"] = "quadratic";
        }
        edges.push(edge);
    })
    data["nodes"] = json_data["nodes"];
    data["edges"] = edges;

    graph.data(data);
    graph.render();

    const node_list = graph.getNodes();
    node_list.forEach(function (item){
        if(item._cfg.model.id === coreNodeId){
            graph.setItemState(item, 'coreNode', true);
        }
    });

    showSmellDetail(json_data["smells"]);

    graph.on('node:mouseenter', (e) => {
        graph.setItemState(e.item, 'active', true);
    });
    graph.on('node:mouseleave', (e) => {
        graph.setItemState(e.item, 'active', false);
    });
    graph.on('edge:mouseenter', (e) => {
        graph.setItemState(e.item, 'active', true);
    });
    graph.on('edge:mouseleave', (e) => {
        graph.setItemState(e.item, 'active', false);
    });

    if (typeof window !== 'undefined') {
        window.onresize = () => {
            if (!graph || graph.get('destroyed')) return;
            if (!container || !container.scrollWidth || !container.scrollHeight) return;
            graph.changeSize(container.scrollWidth, container.scrollHeight);
        };
    }
}

let showSmellDetail = function (smells) {
    let html = "";
    html += "<table class='table table-bordered'>";
    html += "<tr>";
    html += "<th style='text-align: center; vertical-align: middle'>SmellIndex</th>";
    html += "<th style='text-align: center; vertical-align: middle'>SmellName</th>";
    html += "<th style='text-align: center; vertical-align: middle'>NodeNumber</th>";
    html += "<th style='text-align: center; vertical-align: middle'>NodeIndex</th>";
    html += "<th style='vertical-align: middle'>NodePath</th>";
    html += "</tr>";
    let index = 1;
    smells.forEach(function (smell){
        let nodes = smell.nodes;
        let len = nodes.length;
        html += "<tr>";
        html += "<td rowspan='" + len + "' style='text-align: center; vertical-align: middle'>" + index + "</td>";
        html += "<td rowspan='" + len + "' style='text-align: center; vertical-align: middle'>" + smell.name + "</td>";
        html += "<td rowspan='" + len + "' style='text-align: center; vertical-align: middle'>" + nodes.length + "</td>";
        for (let nodeIndex in nodes) {
            if (nodes.hasOwnProperty(nodeIndex)) {
                let node = nodes[nodeIndex];
                if (nodeIndex > 0) {
                    html += "<tr>";
                }
                html += "<td style='text-align: center; vertical-align: middle'>" + node.index + "</td>";
                html += "<td style='vertical-align: middle'>" + node.path + "</td>";
                html += "</tr>";
            }
        }
        index ++;
    });
    html += "</table>";
    $("#smellDetail").html(html);
}