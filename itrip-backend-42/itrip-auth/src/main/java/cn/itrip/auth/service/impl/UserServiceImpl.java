package cn.itrip.auth.service.impl;

import cn.itrip.auth.exception.ActivateException;
import cn.itrip.auth.exception.SmsException;
import cn.itrip.auth.exception.UserLoginActivatedException;
import cn.itrip.auth.exception.UserLoginFailedException;
import cn.itrip.auth.service.MailService;
import cn.itrip.auth.service.SmsService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import cn.itrip.mapper.itripUser.ItripUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisAPI redisAPI;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ItripUserMapper itripUserMapper;

    @Override
    public boolean checkUserCode(String userCode) throws Exception {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userCode", userCode);
        List<ItripUser> itripUserList = itripUserMapper.getItripUserListByMap(param);
        if (EmptyUtils.isEmpty(itripUserList)) {
            return true;
        }
        return false;
    }

    @Override
    public void itriptxCreateUserByPhone(ItripUser itripUser) throws SmsException, Exception {
        //生成验证码
        Integer code = MD5.getRandomCode();
        //调用服务商sdk
        smsService.sendSms(itripUser.getUserCode(), "1", new String[]{String.valueOf(code), String.valueOf(Constants.PHONE_CODE_TIMEOUT / 60)});
        //保存到redis
        redisAPI.set(Constants.PHONE_CODE_PREFIX + itripUser.getUserCode(),
                Constants.PHONE_CODE_TIMEOUT,
                code.toString());
        //保存到数据库
        itripUser.setActivated(0); //默认不激活
        itripUser.setCreationDate(new Date());
        itripUser.setUserPassword(MD5.getMd5(itripUser.getUserPassword(), 32));
        itripUser.setUserType(0); //自注册用户
        itripUserMapper.insertItripUser(itripUser);
        System.out.println(itripUser.getUserCode() + ":" + code);
    }

    @Override
    public void itriptxCreateUserByMail(ItripUser itripUser) throws Exception {
        //生成加密验证码
        String code = MD5.getMd5(String.valueOf(Math.random() * 1000000), 16);
        //调用javamail
        mailService.sendMail(itripUser.getUserCode(), code);
        //保存到redis
        redisAPI.set(Constants.MAIL_CODE_PREFIX + itripUser.getUserCode(),
                Constants.MAIL_CODE_TIMEOUT,
                code);
        //保存到数据库
        itripUser.setActivated(0); //默认不激活
        itripUser.setCreationDate(new Date());
        itripUser.setUserPassword(MD5.getMd5(itripUser.getUserPassword(), 32));
        itripUser.setUserType(0); //自注册用户
        itripUserMapper.insertItripUser(itripUser);
        System.out.println(itripUser.getUserCode() + ":" + code);
    }

    @Override
    public void activate(String userCode, String code) throws ActivateException, Exception {
        String key = null;
        if (code.length() == 4) {
            key = Constants.PHONE_CODE_PREFIX + userCode;
        } else {
            key = Constants.MAIL_CODE_PREFIX + userCode;
        }
        if (redisAPI.exist(key)) {
            String rCode = redisAPI.get(key);
            if (!code.equals(rCode)) {
                throw new ActivateException("验证码不正确！");
            }
        } else {
            throw new ActivateException("验证码已失效！");
        }
        itripUserMapper.updateItripUserActivated(userCode);
    }

    @Override
    public ItripUser login(String userCode, String userPassword) throws UserLoginActivatedException, UserLoginFailedException, Exception {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userCode", userCode);
        param.put("userPassword", userPassword);
        List<ItripUser> itripUserList = itripUserMapper.getItripUserListByMap(param);
        ItripUser itripUser = itripUserList.get(0);
        if (itripUser.getActivated() == 0) {
            throw new UserLoginActivatedException("账号未激活！");
        }
        return itripUser;
    }

    @Override
    public ItripUser getItripUserByUserCode(String userCode) throws Exception {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userCode", userCode);
        List<ItripUser> itripUserList = itripUserMapper.getItripUserListByMap(param);
        if (EmptyUtils.isNotEmpty(itripUserList)) {
            return itripUserList.get(0);
        }
        return null;
    }

}
