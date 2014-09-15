package com.anjuke.dw.tools.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anjuke.dw.tools.dao.MetricLogRepository;
import com.anjuke.dw.tools.model.MetricLog;

@Controller
@RequestMapping("/metric")
public class MetricController {

    @Autowired
    private MetricLogRepository metricLogRepository;

    @RequestMapping("")
    public String index() {
        return "metric";
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
