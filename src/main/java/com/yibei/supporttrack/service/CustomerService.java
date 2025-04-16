package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.dto.CustomerQueryParam;
import com.yibei.supporttrack.entity.po.Customer;

import java.util.List;

public interface CustomerService {

    int addCustomer(Customer customer);

    int updateCustomer(Customer customer);

    int deleteCustomer(Integer id);

    Customer getCustomerById(Integer id);

    List<Customer> getCustomerList(CustomerQueryParam param);
}
