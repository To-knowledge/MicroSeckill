package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.Model.UserModel;
import com.miaoshaproject.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl  implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        // invoke userMOMapper's method to return userDo Object
        // userDO must be not seen for front end, (透传)
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null)  return null;

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        if(StringUtils.isEmpty(userModel.getName())||
        userModel.getAge() == null ||
        userModel.getGender() == null ||
        StringUtils.isEmpty(userModel.getTelephone()))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);

        //将UserModel转化为UserDO
        UserDO userDO = convertFromUserModel(userModel);

        //注入到数据库,insertSelective在字段为空时，拒绝insert
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号已被注册");
        }
        userModel.setId(userDO.getId());
        //还有密码的处理
        UserPasswordDO userPasswordDO = convertPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

    }

    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel) {
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        //BeanUtils.copyProperties(userModel, userPasswordDO);
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertFromUserModel(UserModel userModel) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null)  return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if(userPasswordDO != null)
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());

        return userModel;
    }
}
