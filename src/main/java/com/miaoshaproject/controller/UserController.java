package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.Model.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.Model.UserModel;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
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

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value="/getotp",method={RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOpt(@RequestParam("telphone") String tel){
        //1.生成随机验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 100000;
        String OptCode = String.valueOf(randomInt);
        //2.将OptCode和Tel关联
        httpServletRequest.getSession().setAttribute(tel, OptCode);
        //3. 通过短信通道发送给用户
        System.out.println("Telephone:"+ tel + " optCode:" + OptCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value="/register",method={RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam("name") String name, @RequestParam("gender") Byte gender,
                                     @RequestParam("age") Integer age, @RequestParam("telphone") String tel,
                                     @RequestParam("password") String encrptPassword,
                                     @RequestParam("otpCode") String optCode) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 1.验证用户的手机号与验证码是否匹配
        String optCodeInSession = (String) httpServletRequest.getSession().getAttribute(tel);
        if(!StringUtils.equals(optCodeInSession, optCode))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        // 2.用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setTelephone(tel);
        userModel.setEncrptPassword(this.EncodeByMd5(encrptPassword));

        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors()){
            String error = validationResult.getErrMsg();
            BusinessException ex = new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, error);
            throw ex;
        }
        //调用Service层方法进行注册
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    @Autowired
    ValidatorImpl validator;

    @RequestMapping(value="/login",method={RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam("telphone") String tel,
                                  @RequestParam("password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 1.判空
        if(org.apache.commons.lang3.StringUtils.isEmpty(tel)
        || org.apache.commons.lang3.StringUtils.isEmpty(password))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        // 2.验证
        UserModel userModel = userService.validateLogin(tel, this.EncodeByMd5(password));
        // 3.将用户加入到session中
        httpServletRequest.getSession().setAttribute("is_login", true);
        httpServletRequest.getSession().setAttribute("loginUser",userModel);

        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

}
