package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.dto.CustomerQueryParam;
import com.yibei.supporttrack.entity.po.Customer;
import com.yibei.supporttrack.entity.vo.CommonPage;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/list")
    public CommonResult<CommonPage<Customer>> list(CustomerQueryParam param) {
        List<Customer> list = customerService.getCustomerList(param);
        return CommonResult.success(CommonPage.restPage(list));
    }

    @GetMapping("/{id}")
    public CommonResult<Customer> getItem(@PathVariable Integer id) {
        Customer customer = customerService.getCustomerById(id);
        return CommonResult.success(customer);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult<?> delete(@PathVariable Integer id) {
        int count = customerService.deleteCustomer(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @PostMapping("/add")
    public CommonResult<?> create(@RequestBody Customer customer) {
        int count = customerService.addCustomer(customer);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @PutMapping("/update")
    public CommonResult<?> update(@RequestBody Customer customer) {
        int count = customerService.updateCustomer(customer);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
