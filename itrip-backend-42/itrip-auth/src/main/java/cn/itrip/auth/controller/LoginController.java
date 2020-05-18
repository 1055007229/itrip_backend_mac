package cn.itrip.auth.controller;

import cn.itrip.auth.exception.UserLoginActivatedException;
import cn.itrip.auth.service.TokenServer;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "登录接口")
@Controller
@RequestMapping(value = "/api/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenServer tokenServer;

    @ApiOperation(value = "用户登录", httpMethod = "POST", produces = "application/json", protocols = "HTTP", response = Dto.class, notes = "根据用户名、密码进行统一认证")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "form", required = false, value = "用户名", name = "userCode", defaultValue = "XXX@139.com"), @ApiImplicitParam(paramType = "form", required = false, value = "密码", name = "userPassword", defaultValue = "******")})
    @RequestMapping(value = "/doLogin", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto doLogin(@RequestParam(value = "userCode", required = false) String userCode, @RequestParam(value = "userPassword", required = false) String userPassword, HttpServletRequest request) {
        if (EmptyUtils.isEmpty(userCode) || EmptyUtils.isEmpty(userPassword)) {
            return DtoUtil.returnFail("用户名或密码不能为空！", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        try {
            ItripUser itripUser = userService.login(userCode, MD5.getMd5(userPassword, 32));
            if (!EmptyUtils.isEmpty(itripUser)) {
                //获取请求头
                String agent = request.getHeader("User-Agent");
                System.out.println("doLogin：" + agent);
                //生成token
                ItripTokenVO token = tokenServer.generateToken(agent, itripUser);
                if (token.getToken().startsWith(Constants.TOKEN_PREFIX + "MOBILE-")) {
                    token.setExpTime(token.getExpTime() - Constants.TOKEN_CODE_TIMEOUT);
                }
                //将token保存到redis
                if (!tokenServer.save(token, itripUser)) {
                    return DtoUtil.returnFail("Redis异常，请联系管理员！", ErrorCode.AUTH_UNKNOWN);
                }
                //返回token到客户端
               /* Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(token.getToken().split("-")[3]);
                Integer timeout = 0;
                if (token.getToken().startsWith(Constants.TOKEN_PREFIX + "PC-")) {
                    timeout = Constants.TOKEN_CODE_TIMEOUT * 1000;
                }
                ItripTokenVO itripTokenVO = new ItripTokenVO(token.getToken(), date.getTime() + timeout, date.getTime());*/
                System.out.println(token.getToken() + "|" + token.getExpTime() + "|" + token.getGenTime());
                return DtoUtil.returnDataSuccess(token);
            } else {
                return DtoUtil.returnFail("用户名或密码错误！", ErrorCode.AUTH_PARAMETER_ERROR);
            }
        } catch (UserLoginActivatedException e) {
            e.printStackTrace();
            return DtoUtil.returnFail("用户未激活！", ErrorCode.AUTH_AUTHENTICATION_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，请联系管理员！", ErrorCode.AUTH_UNKNOWN);
        }
    }

    @ApiOperation(value = "用户注销", httpMethod = "GET", produces = "application/json", protocols = "HTTP", response = Dto.class, notes = "注销并销毁token")
    @RequestMapping(value = "/loginOut", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto loginOut(HttpServletRequest request) {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        System.out.println("loginOut：" + agent);
        try {
            if (tokenServer.validateToken(token, agent)) {
                tokenServer.deleteToken(token);
                //如有其它业务数据，也需要进行销毁
                //...
                return DtoUtil.returnSuccess("退出成功！");
            } else {
                return DtoUtil.returnFail("Token无效！", ErrorCode.AUTH_TOKEN_INVALID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，请联系管理员！", ErrorCode.AUTH_UNKNOWN);
        }
    }

}
