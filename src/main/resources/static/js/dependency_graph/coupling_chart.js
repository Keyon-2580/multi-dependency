let data = {};
let pckId = 265;

const INSTABILITY_COLOR1 = "#642924";
const INSTABILITY_COLOR2 = "#8B3830";
const INSTABILITY_COLOR3 = "#B2463C";
const INSTABILITY_COLOR4 = "#C86259";
const INSTABILITY_COLOR5 = "#D6857E";
const COLOR_LINK_NORMAL = '#9aa6d2';
const COLOR_LINK_SPECIAL = '#f25f3f';
const COLOR_LINK_EXTENDS_AND_IMPLEMENTS = '#4393ee';

const container = document.getElementById('coupling_chart');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;

function main() {
    return {
        init : function() {
            instabilityAjax();
        }
    }
}

function instabilityAjax(){
    $.ajax({
        type : "GET",
        url : "/coupling/group/one_step_child_packages?pckId=" + pckId,
        success : function(result) {
            data = result;
            // let nodes_data = json_data["nodes"];
            // let edges_data = json_data["edges"];
            //
            // data["nodes"] = json_data["nodes"];
            // data["edges"] = json_data["edges"];

            levelLayout();
            handleReverseEdgesAndExtends();

            graph.data(data);
            graph.render();
            console.log(graph)
        }
    })
}

const tooltip = new G6.Tooltip({
    offsetX: 10,
    offsetY: 10,
    itemTypes: ['node', 'edge'],
    getContent: (e) => {
        const outDiv = document.createElement('div');
        outDiv.style.width = 'fit-content';
        if(e.item._cfg.type === "node"){
            outDiv.innerHTML = `
              <h4>${e.item._cfg.id}</h4>
              <ul>
                <li>name: ${e.item.getModel().name}</li>
              </ul>
              <ul>
                <li>path: ${e.item.getModel().path}</li>
              </ul>
              <ul>
                <li>instability: ${e.item.getModel().instability}</li>
              </ul>`;
        }else if(e.item._cfg.type === "edge"){
            outDiv.innerHTML = `
              <h4>${e.item.getModel().source}_${e.item.getModel().target}</h4>
              <ul>
                <li>${e.item.getModel().dependsOnTypes}</li>
              </ul>`;
        }
        return outDiv;
    },
});

const graph = new G6.Graph({
    container: 'coupling_chart',
    width,
    height,
    fitView: true,
    modes: {
        default: ['drag-canvas', 'drag-node', 'zoom-canvas', 'click-select'],
    },
    // layout: {
    //     type: 'dagre',
    //     rankdir: 'BT',
    //     align: 'UL',
    //     controlPoints: true,
    //     nodesepFunc: () => 1,
    //     ranksepFunc: () => 1,
    // },
    defaultNode: {
        type: 'circle',
        size: 20,
        style: {
            cursor: "pointer",
            // fill: "#cce9f8",
            // stroke: "#a0d6f4",
            fill: "rgb(129,236,236)",
            stroke: "rgb(113,151,234)",

        },
        labelCfg: {
            style: {
                fill: '#1890ff',
                fontSize: 5,
                background: {
                    fill: '#ffffff',
                    stroke: '#9EC9FF',
                    padding: [2, 2, 2, 2],
                    radius: 2,
                },
            },
            position: 'bottom',
        }
    },
    defaultEdge: {
        type: 'quadratic',
        labelCfg: {
            autoRotate: true,
        },
        // type: 'cubic-vertical',
        // size: 1,
        // labelCfg: {
        //     style: {
        //         fontSize: 5,
        //     },
        // },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    edgeStateStyles: {
        highlight: {
            stroke: '#bc2704',
        },
    },
    plugins: [tooltip],
    minZoom: 0.05,
});

function levelLayout(){
    let nodelist = data["nodes"];

    let list1 = [], list2 = [], list3 = [], list4 = [], list5 = [];
    nodelist.forEach(node =>{
        if(node["instability"] >= 0.8){
            node["style"] = {"fill" : INSTABILITY_COLOR1};
            list1.push(node);
        }else if(node["instability"] >= 0.6){
            node["style"] = {"fill" : INSTABILITY_COLOR2};
            list2.push(node);
        }else if(node["instability"] >= 0.4){
            node["style"] = {"fill" : INSTABILITY_COLOR3};
            list3.push(node);
        }else if(node["instability"] >= 0.2){
            node["style"] = {"fill" : INSTABILITY_COLOR4};
            list4.push(node);
        }else{
            node["style"] = {"fill" : INSTABILITY_COLOR5};
            list5.push(node);
        }
    })

    list1.forEach((node, index) =>{
        node["x"] = (index - (list1.length / 2)) * 100;
        node["y"] = 0;
    })

    list2.forEach((node, index) =>{
        node["x"] = (index - (list2.length / 2)) * 100;
        node["y"] = 50;
    })

    list3.forEach((node, index) =>{
        node["x"] = (index - (list3.length / 2)) * 100;
        node["y"] = 100;
    })

    list4.forEach((node, index) =>{
        node["x"] = (index - (list4.length / 2)) * 100;
        node["y"] = 150;
    })

    list5.forEach((node, index) =>{
        node["x"] = (index - (list5.length / 2)) * 100;
        node["y"] = 200;
    })
}

function handleReverseEdgesAndExtends(){
    let edges = data["edges"];
    let nodes = data["nodes"];

    edges.forEach(edge =>{
        let startLevel = 0;
        let endLevel = 0;

        nodes.forEach(node =>{
            if(node["id"] === edge["source"]){
                startLevel = node["y"];
            }
            if(node["id"] === edge["target"]){
                endLevel = node["y"];
            }
        })

        if(startLevel > endLevel){
            if(edge.isExtendOrImplements){
                edge["style"] = {
                    endArrow: {
                        path: G6.Arrow.triangleRect(10, 10, 10, 2, 4),
                        fill: COLOR_LINK_EXTENDS_AND_IMPLEMENTS,
                    }
                }
            }else{
                edge["style"] = {
                    "stroke": COLOR_LINK_SPECIAL,
                }
            }
        }else{
            if(edge.isTwoWayDependsOn){
                let tmpedge = edges.find(edge2 => edge2.id === edge["target"] + "_" + edge["source"]);
                if(tmpedge.isExtendOrImplements){
                    edge["style"] = {
                        "stroke": COLOR_LINK_SPECIAL,
                    }
                }
            }
        }
    })
}

graph.on('node:dragend', evt => {
    let edges = evt.item.getEdges();
    let specialModel = {
        style: {
            stroke: COLOR_LINK_SPECIAL,
        },
    };
    let normalModel = {
        style: {
            stroke: COLOR_LINK_NORMAL,
        },
    };

    edges.forEach(edge =>{
        if(edge._cfg.source._cfg.model.y > edge._cfg.target._cfg.model.y){
            if(!edge._cfg.model.isExtendOrImplements)
                edge.update(specialModel);
        }else{
            if(edge._cfg.model.isTwoWayDependsOn){
                let edgeTmp = graph.findById(edge._cfg.model.target + "_" + edge._cfg.model.source);
                if(edgeTmp._cfg.model.isExtendOrImplements){
                    edge.update(specialModel);
                }
            }else{
                edge.update(normalModel);
            }
        }
    })
})

graph.on('nodeselectchange', (e) => {
    console.log(e.selectedItems);
});

if (typeof window !== 'undefined')
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };