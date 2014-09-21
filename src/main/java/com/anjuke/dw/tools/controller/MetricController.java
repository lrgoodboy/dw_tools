package com.anjuke.dw.tools.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anjuke.dw.tools.dao.MetricLogRepository;
import com.anjuke.dw.tools.model.MetricLog;
import com.anjuke.dw.tools.model.MetricLogPoint;

@Controller
@RequestMapping("/metric")
public class MetricController {
    @Autowired
    private MetricLogRepository metricLogRepository;

    @RequestMapping("")
    public String index(ModelMap model) throws Exception {

        Date begin = DateUtils.truncate(new Date(), Calendar.DATE);
        Date end = DateUtils.addSeconds(DateUtils.addDays(begin, 1), -1);

        List<MetricLogPoint> udData = log2point(6, metricLogRepository.findToday(6, true, 1440));

        model.addAttribute("beginTime", begin.getTime());
        model.addAttribute("endTime", end.getTime());
        model.addAttribute("udData", udData);

        return "metric";
    }

    @RequestMapping("get-latest")
    @ResponseBody
    public List<MetricLogPoint> getLatest(@RequestParam long metricId) {
        return log2point(metricId, metricLogRepository.findToday(metricId, false, 5));
    }

    private List<MetricLogPoint> log2point(long metricId, List<MetricLog> logs) {

        DateFormat dfId = new SimpleDateFormat("yyyyMMddHHmm");

        List<MetricLogPoint> points = new ArrayList<MetricLogPoint>();
        for (MetricLog log : logs) {
            MetricLogPoint point = new MetricLogPoint();
            point.setId(String.format("%d-%s", metricId, dfId.format(log.getCreated())));
            point.setX(log.getCreated().getTime());
            point.setY(log.getData());
            points.add(point);
        }

        return points;
    }

    @RequestMapping("get")
    @ResponseBody
    public Map<Long, Long> get(@RequestParam String metricIds) {

        Map<Long, Long> metricData = new HashMap<Long, Long>();

        for (String metricIdString : metricIds.split(",")) {
            long metricId;
            try {
                metricId = Long.parseLong(metricIdString);
            } catch (NumberFormatException e) {
                continue;
            }

            List<MetricLog> metricLogs = metricLogRepository.findByMetricIdOrderByCreatedDesc(metricId, new PageRequest(0, 1));
            if (metricLogs.isEmpty()) {
                continue;
            }
            MetricLog metricLog = metricLogs.get(0);
            metricData.put(metricLog.getMetricId(), metricLog.getData());
        }

        return metricData;
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "metric";
    }

}
