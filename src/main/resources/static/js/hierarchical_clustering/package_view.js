let data;
const COLOR_LINK_NORMAL = '#9aa6d2';

const container = document.getElementById('package_view');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;
const outLayout = new G6.Layout['concentric']({
    // sortBy: 'id'
});
const inLayout = new G6.Layout['grid']({
    preventOverlap: true,
    condense: true
});

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
          </ul>`;
        }else if(e.item._cfg.type === "edge"){
            outDiv.innerHTML = `
                  <h4><b>tag</b>: ${e.item._cfg.model.source}_${e.item._cfg.model.target}</h4>
                  <ul>
                    <li><b>聚类距离</b>: ${e.item._cfg.model.clusterDistance}</li>
                  </ul>`;

        }
        return outDiv;
    },
});

const graph = new G6.Graph({
    container: 'package_view',
    width,
    height,
    fitView: true,
    fitViewPadding: 50,
    minZoom: 0.00000001,
    layout: {
        type: 'comboCombined',
        spacing: (d) => 2,
        outerLayout: outLayout,
        innerLayout: inLayout
        // preventOverlap: true,
    },
    defaultNode: {
        size: 14,
        color: '#5B8FF9',
        style: {
            lineWidth: 2,
            fill: '#C6E5FF',
        },
    },
    defaultEdge: {
        // type: 'quadratic',
        type: 'cubic-vertical',
        size: 1,
        labelCfg: {
            style: {
                fontSize: 5,
            },
        },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1.15,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    defaultCombo: {
        type: 'rect',
        labelCfg: {
            position: 'top',
        },
    },
    modes: {
        default: ['drag-combo', 'drag-node', 'drag-canvas', 'zoom-canvas'],
    },
    plugins: [tooltip],
});

function main(packageId) {
    return {
        init : function() {
            getPackageViewData(packageId);
        }
    }
}

function getPackageViewData(packageId){
    $.ajax({
        type: "GET",
        url: "/hierarchical_clustering/get_package_view?packageId=" + packageId,
        success: function (result) {
            data = result;
            console.log(result);
            initChart();
        }
    });
}

function initChart(){
    graph.data(data);
    graph.render();
    setOuterNodes();
    console.log(graph);
}

function setOuterNodes(){
    let outerModel = {
        color: '#ff3c5f',
        style: {
            lineWidth: 2,
            fill:'#f9abab',
        },
    };
    let nodes = graph.getNodes();

    nodes.forEach(node => {
        if(node._cfg.model.isOuter === true){
            node.update(outerModel);
            node.refresh();
        }
    })
    graph.refresh();
}

