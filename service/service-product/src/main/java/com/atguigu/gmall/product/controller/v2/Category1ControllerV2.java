package com.atguigu.gmall.product.controller.v2;

import com.atguigu.gmall.api.product.controller.ProductApiController;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 一级分类的控制层
 */
@RestController
@RequestMapping(value = "/v2/api/category1")
public class Category1ControllerV2 implements ProductApiController {

    @Autowired
    private BaseCategory1Service baseCategory1Service;


    /**
     * 根据id查询一级分类的信息
     * @param id
     * @return
     */
    @GetMapping(value = "/findById/{id}")
    public Result<BaseCategory1> findById(@PathVariable(value = "id") Long id){
        BaseCategory1 baseCategory1 = baseCategory1Service.getById(id);
        return Result.ok(baseCategory1);
    }

    /**
     * 查询全部的一级分类
     * @return
     */
    @GetMapping(value = "/findAll")
    public Result<List<BaseCategory1>> findAll(){
        List<BaseCategory1> list = baseCategory1Service.list(null);
        return Result.ok(list);
    }

    /**
     * 新增
     * @param baseCategory1
     * @return
     */
    @PostMapping(value = "/add")
    public Result add(@RequestBody BaseCategory1 baseCategory1){
        baseCategory1Service.save(baseCategory1);
        return Result.ok();
    }

    /**
     * 修改
     * @param baseCategory1
     * @return
     */
    @PutMapping(value = "/update")
    public Result update(@RequestBody BaseCategory1 baseCategory1){
        baseCategory1Service.updateById(baseCategory1);
        return Result.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping(value = "/del/{id}")
    public Result del(@PathVariable(value = "id") Long id){
        baseCategory1Service.removeById(id);
        return Result.ok();
    }

    /**
     * 条件查询
     * @param baseCategory1
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<BaseCategory1>> search(@RequestBody BaseCategory1 baseCategory1){
        List<BaseCategory1> baseCategory1List = baseCategory1Service.search(baseCategory1);
        return Result.ok(baseCategory1List);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/page/{page}/{size}")
    public Result page(@PathVariable(value = "page") Integer page,
                       @PathVariable(value = "size") Integer size){
        IPage<BaseCategory1> page1 = baseCategory1Service.page(page, size);
        return Result.ok(page1);
    }

    /**
     * 条件分页查询
     * @param baseCategory1
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result search(@RequestBody BaseCategory1 baseCategory1,
                         @PathVariable(value = "page") Integer page,
                         @PathVariable(value = "size") Integer size){
        IPage<BaseCategory1> search = baseCategory1Service.search(baseCategory1, page, size);
        return Result.ok(search);
    }
}
