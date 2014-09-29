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

        List<MetricLogPoint> udData = log2point(26, metricLogRepository.findToday(26, true, 1440));
        String udWindow = getWindow(6);

        List<MetricLogPoint> vppvData = log2point(11, metricLogRepository.findToday(11, true, 1440));
        String vppvWindow = getWindow(1);

        String vppvEsfWindow = getWindow(2);
        String vppvNhWindow = getWindow(3);
        String vppvZfWindow = getWindow(4);
        String vppvSydcWindow = getWindow(5);

        model.addAttribute("beginTime", begin.getTime());
        model.addAttribute("endTime", end.getTime());
        model.addAttribute("udData", udData);
        model.addAttribute("udWindow", udWindow);
        model.addAttribute("vppvData", vppvData);
        model.addAttribute("vppvWindow", vppvWindow);
        model.addAttribute("vppvEsfWindow", vppvEsfWindow);
        model.addAttribute("vppvNhWindow", vppvNhWindow);
        model.addAttribute("vppvZfWindow", vppvZfWindow);
        model.addAttribute("vppvSydcWindow", vppvSydcWindow);

        return "metric";
    }

    private String getWindow(long metricId) {
        List<MetricLog> logs = metricLogRepository.findToday(metricId, false, 1);
        if (logs.isEmpty()) {
            return "-";
        }
        return logs.get(0).getData().toString();
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

    @RequestMapping("get-points")
    @ResponseBody
    public Map<Long, List<MetricLogPoint>> getPoints(@RequestParam String metricIds, @RequestParam String limits) {

        Map<Long, List<MetricLogPoint>> result = new HashMap<Long, List<MetricLogPoint>>();

        String[] metricIdArray = metricIds.split(",");
        String[] limitArray = limits.split(",");
        for (int i = 0; i < metricIdArray.length; ++i) {

            if (i > limitArray.length - 1) {
                break;
            }

            long metricId;
            int limit;
            try {
                metricId = Long.parseLong(metricIdArray[i]);
                limit = Integer.parseInt(limitArray[i]);
            } catch (NumberFormatException e) {
                continue;
            }

            List<MetricLog> logs = metricLogRepository.findLatest(metricId, limit);
            result.put(metricId, log2point(metricId, logs));

        }

        return result;
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "metric";
    }

}
