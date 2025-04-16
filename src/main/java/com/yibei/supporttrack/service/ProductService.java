package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.dto.ProductQueryParam;
import com.yibei.supporttrack.entity.po.Product;

import java.util.List;

public interface ProductService {

    int addProduct(Product product);

    int updateProduct(Product product);

    int deleteProduct(Integer id);

    Product getProductById(Integer id);

    List<Product> getProductList(ProductQueryParam param);
}
