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

        setInterval(function() {
            self.refresh();
        }, 4500);
    },

    refresh: function() {
        var self = this;

        var data = {
            metricIds: '1,2,3,4,5,6,11,16',
            limits: '1,1,1,1,1,1,5,5'
        };

        $.getJSON(self.contextPath + '/metric/get-points', data, function(result) {

            // update table
            $.each(result[1], function() {
                $('#tdVppv').text(this.y);
            });
            $.each(result[2], function() {
                $('#tdEsfVppv').text(this.y);
            });
            $.each(result[3], function() {
                $('#tdNhVppv').text(this.y);
            });
            $.each(result[4], function() {
                $('#tdZfVppv').text(this.y);
            });
            $.each(result[5], function() {
                $('#tdSydcVppv').text(this.y);
            });
            $.each(result[6], function() {
                $('#tdUd').text(this.y);
            });

            // update charts
            function updateChart(chartId, minMetric, winMetric) {

                var chart = $('#' + chartId).highcharts();

                $.each(result[minMetric], function() {

                    var point = chart.get(this.id);

                    if (point) {
                        point.update(this.y);
                    } else {
                        chart.series[0].addPoint(this);
                    }

                });

                $.each(result[winMetric], function() {
                    self.drawText(chart, chartId, this.y);
                });

            }

            updateChart('udChart', 16, 6);
            updateChart('vppvChart', 11, 1);
        });
    },

    initChart: function() {
        var self = this;

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        var template = {
            credits: {
                enabled: false
            },
            chart: {
                zoomType: 'x'
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

        };

        $('#udChart').highcharts($.extend({

            title: {
                text: '移动App实时UD'
            },
            series: [{
                name: 'UD',
                data: self.opts.udData
            }]

        }, template), function(chart) {
            self.drawText(chart, 'udChart', self.opts.udWindow);
        });

        $('#vppvChart').highcharts($.extend({

            title: {
                text: '移动App实时VPPV'
            },
            series: [{
                name: 'VPPV',
                turboThreshold: 1440,
                data: self.opts.vppvData
            }]

        }, template), function(chart) {
            self.drawText(chart, 'vppvChart', self.opts.vppvWindow);
        });

    },

    texts: {},
    drawText: function(chart, name, data) {
        var self = this;

        if (self.texts[name]) {
            self.texts[name].destroy();
        }

        self.texts[name] = chart.renderer.text(
                '<span style="font-size: 16px; font-weight: bold;">' + data + '/min</span>',
                chart.chartWidth - 120, 25);

        self.texts[name].add();
    },

    _theEnd: undefined
};
