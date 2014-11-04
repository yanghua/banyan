<div id="container" style="width: 100%; height: 600px;"></div>
<script src="/resources/component/d3.v2.2.9.6.js"></script>
<style type="text/css">
    .node circle {
        fill: #fff;
        stroke: steelblue;
        stroke-width: 3px;
    }

    .node text { font: 12px sans-serif; }

    .link {
        fill: none;
        stroke: #ccc;
        stroke-width: 2px;
    }
</style>
<script>
    var drawTopologyTree = function (data) {
        // *********** Convert flat data into a nice tree ***************
        // create a name: node map
        var dataMap = data.reduce(function(map, node) {
            map[node.name] = node;
            return map;
        }, {});

        // create the tree array
        var treeData = [];
        data.forEach(function(node) {
            // add to parent
            var parent = dataMap[node.parent];
            if (parent) {
                // create child array if it doesn't exist
                (parent.children || (parent.children = []))
                    // add node to child array
                        .push(node);
            } else {
                // parent is null or missing
                treeData.push(node);
            }
        });

        // ************** Generate the tree diagram	 *****************
        var margin = {top: 20, right: 120, bottom: 20, left: 120},
                width = 960 - margin.right - margin.left,
                height = 500 - margin.top - margin.bottom;

        var i = 0;

        var tree = d3.layout.tree()
                .size([height, width]);

        var diagonal = d3.svg.diagonal()
                .projection(function(d) { return [d.y, d.x]; });

        var svg = d3.select("#container").append("svg")
                .attr("width", width + margin.right + margin.left)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        root = treeData[0];

        update(root);

        function update(source) {

            // Compute the new tree layout.
            var nodes = tree.nodes(root).reverse(),
                    links = tree.links(nodes);

            // Normalize for fixed-depth.
            nodes.forEach(function(d) { d.y = d.depth * 180; });

            // Declare the nodes…
            var node = svg.selectAll("g.node")
                    .data(nodes, function(d) { return d.id || (d.id = ++i); });

            // Enter the nodes.
            var nodeEnter = node.enter().append("g")
                    .attr("class", "node")
                    .attr("transform", function(d) {
                        return "translate(" + d.y + "," + d.x + ")"; });

            nodeEnter.append("circle")
                    .attr("r", 10)
                    .style("fill", "#fff");

            nodeEnter.append("text")
                    .attr("x", function(d) {
                        return d.children || d._children ? -13 : 13; })
                    .attr("dy", ".35em")
                    .attr("text-anchor", function(d) {
                        return d.children || d._children ? "end" : "start"; })
                    .text(function(d) { return d.name; })
                    .style("fill-opacity", 1);

            // Declare the links…
            var link = svg.selectAll("path.link")
                    .data(links, function(d) { return d.target.id; });

            // Enter the links.
            link.enter().insert("path", "g")
                    .attr("class", "link")
                    .attr("d", diagonal);

        }
    };

    $(document).ready(function () {
        $.ajax({
            type : 'GET',
            url  : '/maintain/topology/generatedtreedata',
            success : function (data) {
                drawTopologyTree(data);
            },
            error : function (err) {
                alert(err);
            }
        });
    });

</script>
