package cn.itrip.auth.service.impl;

import cn.itrip.auth.exception.TokenException;
import cn.itrip.auth.service.TokenServer;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cz.mallat.uasparser.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenServer {

    @Autowired
    private RedisAPI redisAPI;

    @Autowired
    private ValidationToken validationToken;

    @Override
    public ItripTokenVO generateToken(String agent, ItripUser itripUser) throws IOException {
        // 客户端识别工具类
        UserAgentInfo info = UserAgentUtil.getUasParser().parse(agent);
        String deviceType = info.getDeviceType();
        // token对象声明且拼接前缀
        StringBuilder builder = new StringBuilder("token:");
        // 判断当前客户端是属于PC还是移动端
        if (deviceType.equals("Personal computer")) {
            builder.append("PC-");
        } else if (deviceType.equals(UserAgentInfo.UNKNOWN)) {
            if (UserAgentUtil.CheckAgent(agent)) {
                builder.append("MOBILE-");
            } else {
                builder.append("PC-");
            }
        } else {
            builder.append("MOBILE-");
        }
        builder.append(MD5.getMd5(itripUser.getUserCode(), 32));
        builder.append("-");
        builder.append(itripUser.getId());
        builder.append("-");
        Date date = new Date();
        builder.append(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        builder.append("-");
        builder.append(MD5.getMd5(agent, 6));
        return new ItripTokenVO(builder.toString(), date.getTime() + Constants.TOKEN_CODE_TIMEOUT, date.getTime());
    }

    @Override
    public boolean save(ItripTokenVO itripTokenVO, ItripUser itripUser) throws Exception {
        if (itripTokenVO.getToken().startsWith(Constants.TOKEN_PREFIX + "PC-")) {
            return redisAPI.set(itripTokenVO.getToken(), Constants.TOKEN_CODE_TIMEOUT, JSONObject.toJSONString(itripUser));
        } else {
            return redisAPI.set(itripTokenVO.getToken(), JSONObject.toJSONString(itripUser));
        }
    }

    @Override
    public boolean validateToken(String token, String agent) throws Exception {
        return validationToken.validateToken(token,agent);
    }

    @Override
    public void deleteToken(String token) throws Exception {
        redisAPI.delete(token);
    }

    @Override
    public ItripTokenVO reFreshToken(String agent, String oldToken) throws TokenException, Exception {
        System.out.println(oldToken);
        //验证token是否有效
        if (!redisAPI.exist(oldToken)) {
            throw new TokenException("无效token");
        }
        //判断是否达到保护期，一小时保护期
        Date data = new SimpleDateFormat("yyyyMMddHHmmss").parse(oldToken.split("-")[3]);
        if ((Calendar.getInstance().getTimeInMillis() - data.getTime()) < Constants.TOKEN_REPLACEMENT_PROTECTED_TIME) {
            throw new TokenException("token还在保护期，不允许置换");
        }
        //生成新token
        String jsonUser = redisAPI.get(oldToken);
        ItripUser itripUser = JSON.parseObject(jsonUser, ItripUser.class);
        ItripTokenVO newToken = this.generateToken(agent, itripUser);
        //新旧token数据移交 value
        try {
            this.save(newToken, itripUser);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TokenException("token保存失败！");
        }
        //旧token续命2分钟
        redisAPI.set(oldToken, Constants.TOKEN_DELAY_TIME, jsonUser);
        return newToken;
    }

}
