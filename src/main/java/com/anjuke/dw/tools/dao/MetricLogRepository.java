package com.anjuke.dw.tools.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.anjuke.dw.tools.model.MetricLog;


@Repository
public class MetricLogRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<MetricLog> findToday(long metricId, boolean asc, int limit) {

        Date begin = DateUtils.truncate(new Date(), Calendar.DATE);
        Date end = DateUtils.addSeconds(begin, 86400 - 1);
        String suffix = new SimpleDateFormat("yyyyMMdd").format(begin);

        return jdbcTemplate.query(
                "SELECT id, metric_id, data, created FROM metric_log_" + suffix
                + " WHERE metric_id = ? AND created BETWEEN ? AND ?"
                + " ORDER BY created" + (asc ? "" : " DESC") + " LIMIT " + limit,
                rowMapper, metricId, begin, end);
    }

    public List<MetricLog> findLatest(long metricId, int limit) {
        String suffix = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return jdbcTemplate.query(
                "SELECT id, metric_id, data, created FROM metric_log_" + suffix
                + " WHERE metric_id = ? ORDER BY created DESC LIMIT " + limit,
                rowMapper, metricId);
    }

    private RowMapper<MetricLog> rowMapper = new RowMapper<MetricLog>() {

        @Override
        public MetricLog mapRow(ResultSet rs, int numRow) throws SQLException {
            MetricLog row = new MetricLog();
            row.setId(rs.getLong("id"));
            row.setMetricId(rs.getLong("metric_id"));
            row.setData(rs.getLong("data"));
            row.setCreated(rs.getTimestamp("created"));
            return row;
        }

    };

}
