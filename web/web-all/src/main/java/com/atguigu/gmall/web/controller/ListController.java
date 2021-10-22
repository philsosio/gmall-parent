package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.list.feign.ListFeign;
import com.atguigu.gmall.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/page")
public class ListController {

    @Autowired
    private ListFeign listFeign;

    /**
     * 搜索页面
     * @return
     */
    @GetMapping(value = "/search")
    public String search(@RequestParam Map<String,String> searchData,
                         Model model){
        Map search = listFeign.search(searchData);
        //存放搜索的结果
        model.addAllAttributes(search);
        //存放搜索的参数
        model.addAttribute("searchData", searchData);
        //获取当前的url
        String url = getUrl(searchData);
        model.addAttribute("url", url);
        //获取分页信息
        Object totalHits = search.get("totalHits");
        Object pageSize = search.get("pageSize");
        Object page = search.get("page");
        Page pageInfo = new Page<>(Long.parseLong(totalHits.toString()),
                Integer.parseInt(page.toString()),
                Integer.parseInt(pageSize.toString()));
        model.addAttribute("pageInfo",pageInfo);
        return "list";
    }

    /**
     * 获取当前的url的完整内容
     * @param searchData
     * @return
     */
    private String getUrl(Map<String,String> searchData){
        String url = "/page/search?";
        //遍历参数,拼接url
        for (Map.Entry<String, String> entry : searchData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(!key.equals("soft") && !key.equals("page")){
                url = url + key + "=" + value + "&";
            }
        }
        return url.substring(0, url.length() - 1);
    }
}
