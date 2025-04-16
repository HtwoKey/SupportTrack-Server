package com.yibei.supporttrack.service.impl;

import com.yibei.supporttrack.entity.dto.CustomerQueryParam;
import com.yibei.supporttrack.entity.po.Customer;
import com.yibei.supporttrack.mapper.CustomerMapper;
import com.yibei.supporttrack.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addCustomer(Customer customer) {
        customer.setCreatedAt(new Date());
        return customerMapper.insert(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCustomer(Customer customer) {
        customer.setUpdatedAt(new Date());
        return customerMapper.updateById(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCustomer(Integer id) {
        return customerMapper.deleteById(id);
    }

    @Override
    public Customer getCustomerById(Integer id) {
        return customerMapper.selectById(id);
    }

    @Override
    public List<Customer> getCustomerList(CustomerQueryParam param) {
        return List.of();
    }
}
