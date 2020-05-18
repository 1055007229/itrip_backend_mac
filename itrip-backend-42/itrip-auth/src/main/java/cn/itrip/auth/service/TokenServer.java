package cn.itrip.auth.service;

import cn.itrip.auth.exception.TokenException;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;

import java.io.IOException;

public interface TokenServer {

    //生成Token
    public ItripTokenVO generateToken(String agent, ItripUser itripUser) throws IOException;

    //把用户信息保存到redis中
    public boolean save(ItripTokenVO itripTokenVO, ItripUser itripUser) throws Exception;

    //验证token
    public boolean validateToken(String token, String agent) throws Exception;

    //删除退出token
    public void deleteToken(String token) throws Exception;

    //置换token
    public ItripTokenVO reFreshToken(String agent, String oldToken) throws TokenException, Exception;
}
