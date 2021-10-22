package com.atguigu.gmall.list.service;

import java.util.Map;

public interface SearchService {

    /**
     * 搜索功能
     * @param searchData
     * @return
     */
    public Map<String,Object> search(Map<String, String> searchData);
}
