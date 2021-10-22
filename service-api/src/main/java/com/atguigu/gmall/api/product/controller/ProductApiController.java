package com.atguigu.gmall.api.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 商品管理微服务的api的接口类
 */
@Api(value = "商品管理微服务的api的接口类1",
        description = "商品管理微服务的api的接口类2",
        tags = "商品管理微服务的api的接口类3")
public interface ProductApiController {

    /**
     * 根据id查询一级分类的信息
     * @param id
     * @return
     */
    @ApiOperation("根据id查询一级分类的信息")
    public Result<BaseCategory1> findById(@PathVariable(value = "id") Long id);

    /**
     * 查询全部的一级分类
     * @return
     */
    @ApiOperation("查询全部的一级分类")
    public Result<List<BaseCategory1>> findAll();

    /**
     * 新增
     * @param baseCategory1
     * @return
     */
    @ApiOperation("新增")
    public Result add(@RequestBody BaseCategory1 baseCategory1);

    /**
     * 修改
     * @param baseCategory1
     * @return
     */
    @ApiOperation("修改")
    public Result update(@RequestBody BaseCategory1 baseCategory1);

    /**
     * 删除
     * @param id
     * @return
     */
    @ApiOperation("删除")
    public Result del(@PathVariable(value = "id") Long id);

    /**
     * 条件查询
     * @param baseCategory1
     * @return
     */
    @ApiOperation("条件查询")
    public Result<List<BaseCategory1>> search(@RequestBody BaseCategory1 baseCategory1);

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @ApiOperation("分页查询")
    public Result page(@PathVariable(value = "page") Integer page,
                       @PathVariable(value = "size") Integer size);

    /**
     * 条件分页查询
     * @param baseCategory1
     * @param page
     * @param size
     * @return
     */
    @ApiOperation("条件分页查询")
    public Result search(@RequestBody BaseCategory1 baseCategory1,
                         @PathVariable(value = "page") Integer page,
                         @PathVariable(value = "size") Integer size);
}
