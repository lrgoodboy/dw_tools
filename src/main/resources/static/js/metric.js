var Metric = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;
    self.opts = opts;

    self.init();
    self.initChart();
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

    initChart: function() {
        var self = this;

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        $('#udChart').highcharts({
            credits: {
                enabled: false
            },
            chart: {
                zoomType: 'x'
            },
            title: {
                text: '移动App实时UD'
            },
            tooltip: {
                xDateFormat: '%Y-%m-%d %H:%M'
            },
            xAxis: {
                type: 'datetime',
                min: self.opts.beginTime,
                max: self.opts.endTime,
                dateTimeLabelFormats: {
                    second: '%H:%M:%S',
                    minute: '%H:%M',
                    hour: '%H:%M',
                    day: '%m-%d',
                    week: '%m-%d',
                    month: '%Y-%m',
                    year: '%Y'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: null
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                line: {
                    marker: {
                        enabled: false,
                    }
                }
            },
            series: [{
                name: 'UD',
                data: self.opts.udData
            }]

        }, function(chart) {

            setInterval(function() {

                $.getJSON(self.contextPath + '/metric/get-latest', {metricId: 6}, function(result) {

                    $.each(result, function() {
                        var p = chart.get(this.id);
                        if (p) {
                            p.update(this.y);
                        } else {
                            chart.series[0].addPoint(this);
                        }
                    });

                });

            }, 5000);

            var text = chart.renderer.text(
                    '<span style="font-size: 16px; font-weight: bold;">123/min</span>',
                    chart.chartWidth - 100, 80);
            text.add();
        });

    },

    _theEnd: undefined
};
