package cn.itrip.auth.controller;

import cn.itrip.auth.exception.ActivateException;
import cn.itrip.auth.exception.SmsException;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.userinfo.ItripUserVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Api(tags = "注册验证接口")
@Controller
@RequestMapping(value = "/api/register")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 手机注册测试
     */
    /*@RequestMapping(value = "/sendSms", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public String sendSms() {
        ItripUser itripUser = new ItripUser();
        itripUser.setUserName("张三");
        itripUser.setUserCode("13055769579");
        itripUser.setUserPassword("123456");
        try {
            userService.itriptxCreateUserByPhone(itripUser);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }*/

    /**
     * 手机注册
     */
    @ApiOperation(value = "手机注册", httpMethod = "POST", produces = "application/json", protocols = "HTTP", response = Dto.class, notes = "手机注册")
    @ApiImplicitParam(value = "用户对象", name = "itripUserVO",required = true)
    @RequestMapping(value = "/registerByPhone", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByPhone(@RequestBody ItripUserVO itripUserVO) {
        if (EmptyUtils.isEmpty(itripUserVO.getUserCode()) || !checkPhone(itripUserVO.getUserCode())) {
            return DtoUtil.returnFail("手机格式不正确！", ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        try {
            if (!userService.checkUserCode(itripUserVO.getUserCode())) {
                return DtoUtil.returnFail("该手机号码已被注册！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
            ItripUser itripUser = new ItripUser();
            // spring提供 对象拷贝 底层使用反射
            BeanUtils.copyProperties(itripUserVO, itripUser);
            userService.itriptxCreateUserByPhone(itripUser);
            return DtoUtil.returnSuccess();
        } catch (SmsException e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！", ErrorCode.AUTH_UNKNOWN);
        }
    }

    /**
     * 手机验证码激活
     *
     * @param userCode
     * @param code
     * @return
     */
    @RequestMapping(value = "/validateByPhone", produces = "application/json;charset=UTF-8", method = RequestMethod.PUT)
    @ResponseBody
    public Dto validateByPhone(@RequestParam(value = "userCode", required = false) String userCode,
                             @RequestParam(value = "code", required = false) String code) {
        if (EmptyUtils.isEmpty(userCode) || !checkPhone(userCode)) {
            return DtoUtil.returnFail("手机格式不正确！激活失败！", ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        if (EmptyUtils.isEmpty(code)) {
            return DtoUtil.returnFail("激活码不能为空！激活失败！", ErrorCode.AUTH_UNKNOWN);
        }
        //手机账户激活
        try {
            userService.activate(userCode, code);
            return DtoUtil.returnSuccess("激活成功！");
        } catch (ActivateException e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_ACTIVATE_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！请稍后重试！", ErrorCode.AUTH_ACTIVATE_FAILED);
        }
    }

    /**
     * 邮箱注册测试
     */
    @RequestMapping(value = "/sendMail", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public String sendMail() {
        ItripUser itripUser = new ItripUser();
        itripUser.setUserName("张三");
        itripUser.setUserCode("3005250961@qq.com");
        itripUser.setUserPassword("123456");
        try {
            userService.itriptxCreateUserByMail(itripUser);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    /**
     * 邮箱注册
     */
    @RequestMapping(value = "/registerByMail", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByMail(@RequestBody ItripUserVO itripUserVO) {
        if (EmptyUtils.isEmpty(itripUserVO.getUserCode()) || !checkEmail(itripUserVO.getUserCode())) {
            return DtoUtil.returnFail("邮箱格式不正确！", ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        try {
            if (!userService.checkUserCode(itripUserVO.getUserCode())) {
                return DtoUtil.returnFail("该邮箱已被注册！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
            ItripUser itripUser = new ItripUser();
            // spring提供 对象拷贝 底层使用反射
            BeanUtils.copyProperties(itripUserVO, itripUser);
            userService.itriptxCreateUserByMail(itripUser);
            return DtoUtil.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！", ErrorCode.AUTH_UNKNOWN);
        }
    }

    /**
     * 邮箱验证码激活
     *
     * @param userCode
     * @param code
     * @return
     */
    @RequestMapping(value = "/validateByMail", produces = "application/json;charset=UTF-8", method = RequestMethod.PUT)
    @ResponseBody
    public Dto validateByMail(@RequestParam(value = "userCode", required = false) String userCode,
                            @RequestParam(value = "code", required = false) String code) {
        if (EmptyUtils.isEmpty(userCode) || !checkEmail(userCode)) {
            return DtoUtil.returnFail("邮箱格式不正确！激活失败！", ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        if (EmptyUtils.isEmpty(code)) {
            return DtoUtil.returnFail("激活码不能为空！激活失败！", ErrorCode.AUTH_UNKNOWN);
        }
        //邮箱账户激活
        try {
            userService.activate(userCode, code);
            return DtoUtil.returnSuccess("激活成功！");
        } catch (ActivateException e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_ACTIVATE_FAILED);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！请稍后重试！", ErrorCode.AUTH_ACTIVATE_FAILED);
        }
    }

    /**
     * 普通注册
     *
     * @param userCode
     * @return
     */
    @RequestMapping(value = "/ckusr", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto ckusr(@RequestParam(value = "userCode") String userCode) {
        if (EmptyUtils.isEmpty(userCode)) {
            return DtoUtil.returnFail("用户名不能为空！", ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        try {
            //判断当前用户名是否被注册 如果被注册则返回false
            if (userService.checkUserCode(userCode)) {
                return DtoUtil.returnSuccess("用户名可以注册！");
            } else {
                return DtoUtil.returnFail("用户名已被注册！", ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常!", ErrorCode.AUTH_UNKNOWN);
        }
    }

    /**
     * 验证邮箱格式
     *
     * @param email
     * @return
     */
    public boolean checkEmail(String email) {
        String regex = "[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+";
        return Pattern.compile(regex).matcher(email).find();
    }

    /**
     * 手机号验证
     *
     * @param phone
     * @return
     */
    public boolean checkPhone(String phone) {
        String regex = "^1[3578]{1}\\d{9}$";
        return Pattern.compile(regex).matcher(phone).find();
    }

}
