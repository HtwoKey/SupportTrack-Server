package com.yibei.supporttrack.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yibei.supporttrack.entity.dto.ProductQueryParam;
import com.yibei.supporttrack.entity.po.Product;
import com.yibei.supporttrack.mapper.ProductMapper;
import com.yibei.supporttrack.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public int addProduct(Product product) {
        product.setCreatedAt(new Date());
        return productMapper.insert(product);
    }

    @Override
    public int updateProduct(Product product) {
        if (product.getId() != null){
            return productMapper.updateById(product);
        }
        return 0;
    }

    @Override
    public int deleteProduct(Integer id) {
        return productMapper.deleteById(id);
    }

    @Override
    public Product getProductById(Integer id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> getProductList(ProductQueryParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (param.getProductName() != null){
            queryWrapper.like("product_name", param.getProductName());
        }
        return productMapper.selectList(queryWrapper);
    }
}
