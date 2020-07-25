package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.Model.UserModel;

public interface UserService {
    //get Object of user by user id
    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;

    UserModel validateLogin(String tel, String encrptPassword) throws BusinessException;
}
