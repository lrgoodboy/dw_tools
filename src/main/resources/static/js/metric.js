var Metric = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;

    self.init();
};

Metric.prototype = {
    constructor: Metric,

    init: function() {
        var self = this;

        self.refresh();
        setInterval(function() {
            self.refresh();
        }, 4500);
    },

    refresh: function() {
        var self = this;

        var data = {
            metricIds: '1,2,3,4,5,6'
        };

        $.getJSON(self.contextPath + '/metric/get', data, function(result) {
            $('#tdVppv').text(result[1]);
            $('#tdEsfVppv').text(result[2]);
            $('#tdNhVppv').text(result[3]);
            $('#tdZfVppv').text(result[4]);
            $('#tdSydcVppv').text(result[5]);
            $('#tdUd').text(result[6]);
        });
    },

    _theEnd: undefined
};
