package com.anjuke.dw.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping("/greeting")
    public String greeting(
            @RequestParam(value = "name", required = false, defaultValue = "World") String name,
            Model model) {

        Date now = jdbcTemplate.queryForObject("SELECT NOW()", Date.class);
        DateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("now", dfTime.format(now));
        model.addAttribute("name", name);
        return "greeting";
    }

}
