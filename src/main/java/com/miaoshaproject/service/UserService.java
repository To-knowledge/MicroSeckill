package com.miaoshaproject.service;

import com.miaoshaproject.service.Model.UserModel;

public interface UserService {
    //get Object of user by user id
    UserModel getUserById(Integer id);
}
