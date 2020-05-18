package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenServer;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@ApiIgnore
@Controller
@RequestMapping(value = "/api/token")
public class TokenController {

    @Autowired
    private TokenServer tokenServer;

    @RequestMapping(value = "/reToken", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto reToken(HttpServletRequest request) {
        //获取到旧token、user-agent
        String oldToken = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        //生成新token
        ItripTokenVO itripTokenVO = null;
        try {
            itripTokenVO = tokenServer.reFreshToken(agent, oldToken);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("生成新token失败！", ErrorCode.AUTH_REPLACEMENT_FAILED);
        }
        return DtoUtil.returnDataSuccess(itripTokenVO);
    }

}
