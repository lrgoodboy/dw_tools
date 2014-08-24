package com.anjuke.dw.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.anjuke.dw.tools.dao.IssueRepository;

@Controller
@RequestMapping("/issue")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @RequestMapping({"", "list"})
    public String greeting(
            @RequestParam(value = "name", required = false, defaultValue = "World") String name,
            Model model) {

        model.addAttribute("count", issueRepository.count());
        model.addAttribute("name", name);
        return "issue";
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "issue";
    }

}
