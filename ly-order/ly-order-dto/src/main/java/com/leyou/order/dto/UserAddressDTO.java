package com.leyou.order.dto;

import lombok.Data;

@Data
public class UserAddressDTO {
    private Long id;
    private Long userId;
    private String name;
    private String addressee;// 收件人姓名
    private String address;// 详细地址
    private String phone;// 电话
    private String province;// 省份
    private String city;// 城市
    private String district;// 区
    private String street;// 街道地址
    private String  postcode;// 邮编
    private Boolean isDefault;
}