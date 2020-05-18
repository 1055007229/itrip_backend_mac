package cn.itrip.auth.service;

import cn.itrip.auth.exception.ActivateException;
import cn.itrip.auth.exception.SmsException;
import cn.itrip.auth.exception.UserLoginActivatedException;
import cn.itrip.auth.exception.UserLoginFailedException;
import cn.itrip.beans.pojo.ItripUser;

public interface UserService {

    //判断账号是否被注册
    public boolean checkUserCode(String userCode) throws Exception;

    // 手机注册账号
    public void itriptxCreateUserByPhone(ItripUser itripUser) throws SmsException,Exception;

    // 邮箱注册账号
    public void itriptxCreateUserByMail(ItripUser itripUser) throws Exception;

    // 激活账号
    public void activate(String userCode,String code) throws ActivateException,Exception;

    // 登录
    public ItripUser login(String userCode,String userPassword) throws UserLoginActivatedException, UserLoginFailedException,Exception;

    //根据用户帐号获取用户对象
    public ItripUser getItripUserByUserCode(String userCode) throws Exception;
}
