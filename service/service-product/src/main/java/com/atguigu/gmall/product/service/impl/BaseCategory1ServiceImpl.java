package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 一级分类的实现类
 */
@Service
public class BaseCategory1ServiceImpl
        extends ServiceImpl<BaseCategory1Mapper, BaseCategory1>
        implements BaseCategory1Service {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    /**
     * 条件查询
     *
     * @param baseCategory1
     * @return
     */
    @Override
    public List<BaseCategory1> search(BaseCategory1 baseCategory1) {
        //参数校验
        if(baseCategory1 == null){
            //没有任何查询条件的时候查询全部的数据
            return baseCategory1Mapper.selectList(null);
        }
        //构建查询条件
        LambdaQueryWrapper wrapper = buildQueryParam(baseCategory1);
        //执行查询获取结果
        List<BaseCategory1> list = baseCategory1Mapper.selectList(wrapper);
        //返回
        return list;
    }

    /**
     * 分页查询
     *
     * @param pageNum : 页码
     * @param size    : 每页显示数量
     * @return
     */
    @Override
    public IPage<BaseCategory1> page(Integer pageNum, Integer size) {
        IPage<BaseCategory1> baseCategory1IPage = baseCategory1Mapper.selectPage(new Page<>(pageNum, size), null);
        return baseCategory1IPage;
    }

    /**
     * 分页条件查询
     *
     * @param baseCategory1
     * @param pageNum
     * @param size
     * @return
     */
    @Override
    public IPage<BaseCategory1> search(BaseCategory1 baseCategory1, Integer pageNum, Integer size) {
        //参数校验
        if(baseCategory1 == null){
            //没有任何查询条件的时候查询全部的数据
            return baseCategory1Mapper.selectPage(new Page<>(pageNum, size), null);
        }
        //构建条件
        LambdaQueryWrapper wrapper = buildQueryParam(baseCategory1);
        //执行查询
        IPage<BaseCategory1> baseCategory1IPage = baseCategory1Mapper.selectPage(new Page<>(pageNum, size), wrapper);
        //返回结果
        return baseCategory1IPage;
    }

    /**
     * 构建查询条件
     * @param baseCategory1
     * @return
     */
    private LambdaQueryWrapper buildQueryParam(BaseCategory1 baseCategory1){
        //声明条件构造器
        LambdaQueryWrapper<BaseCategory1> wrapper = new LambdaQueryWrapper<>();
        //拼接条件
        if(baseCategory1.getId() != null){
            wrapper.eq(BaseCategory1::getId, baseCategory1.getId());
        }
        //name
        if(!StringUtils.isEmpty(baseCategory1.getName())){
            wrapper.like(BaseCategory1::getName, baseCategory1.getName());
        }
        return wrapper;
    }
}
