package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.list.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/list")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索
     * @param searchData
     * @return
     */
    @GetMapping(value = "/search")
    public Map search(@RequestParam Map<String, String> searchData){
        return searchService.search(searchData);
    }
}
