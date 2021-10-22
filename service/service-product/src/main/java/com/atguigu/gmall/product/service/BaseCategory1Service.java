package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 一级分类的接口类
 */
public interface BaseCategory1Service  extends IService<BaseCategory1> {

    /**
     * 条件查询
     * @param baseCategory1
     * @return
     */
    public List<BaseCategory1> search(BaseCategory1 baseCategory1);


    /**
     * 分页查询
     * @param pageNum: 页码
     * @param size: 每页显示数量
     * @return
     */
    public IPage<BaseCategory1> page(Integer pageNum, Integer size);

    /**
     * 分页条件查询
     * @param baseCategory1
     * @return
     */
    public IPage<BaseCategory1> search(BaseCategory1 baseCategory1,
                                      Integer pageNum,
                                      Integer size);
}
