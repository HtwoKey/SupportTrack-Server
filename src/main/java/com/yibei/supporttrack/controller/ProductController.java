package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.dto.ProductQueryParam;
import com.yibei.supporttrack.entity.po.Product;
import com.yibei.supporttrack.entity.vo.CommonPage;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/product")
@Slf4j
public class ProductController {

    private final ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    public CommonResult<CommonPage<Product>> list(ProductQueryParam param){
        List<Product> list = productService.getProductList(param);
        return CommonResult.success(CommonPage.restPage(list));
    }

    @PostMapping("/add")
    public CommonResult<?> create(Product product){
        int i = productService.addProduct(product);
        if (i > 0){
            return CommonResult.success(i);
        }
        return CommonResult.failed();
    }

    @PutMapping("/update")
    public CommonResult<?> update(Product product){
        if (product.getId() == null){
            return CommonResult.failed("请选择需要更新的产品");
        }
        int i = productService.updateProduct(product);
        if (i > 0){
            return CommonResult.success(i);
        }
        return CommonResult.failed();
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult<?> delete(@PathVariable Integer id){
        if (id == null){
            return CommonResult.failed("请选择需要删除的产品");
        }
        int i = productService.deleteProduct(id);
        if (i > 0){
            return CommonResult.success(i);
        }
        return CommonResult.failed();
    }
}
