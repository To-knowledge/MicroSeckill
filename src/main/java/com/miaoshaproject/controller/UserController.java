package com.miaoshaproject.controller;

import com.miaoshaproject.controller.Model.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.Model.UserModel;
import com.miaoshaproject.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //invoke service layer to get corresponding id, then return Object of user to front end
        UserModel userModel = userService.getUserById(id);
        //define exception handler to handle the exception that is not solved by Controller layer
        //一种spring的钩子设计思想
        if(userModel == null)
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        // int i = 1 / 0;
        UserVO userVO = convertFromModel(userModel);
        //将返回结果归一化为Status+Data的格式
        CommonReturnType commonReturnType = null;
        return commonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if(userModel == null)
            return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
