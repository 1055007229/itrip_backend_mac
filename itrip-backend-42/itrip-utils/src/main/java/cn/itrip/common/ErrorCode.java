package cn.itrip.common;

/**
 * 系统错误编码，根据业务定义如下
 * <br/>
 * 酒店主业务biz：1开头（10000）<br/>
 * 评论：10001 ——10100<br/>
 * 酒店详情：10101 ——10200<br/>
 * 订单：10201 ——10400<br/>
 * 搜索search：2开头（20000）<br/>
 * 认证auth：3开头（30000）<br/>
 * 支付trade：4开头（40000）<br/>
 * @author hduser
 *
 */
public class ErrorCode {

	/*static enum AuthErrorCode{

		VILLIDATE_EMAIL("邮箱格式不正确","30000");

		AuthErrorCode(String msg,String code){

		}

	}*/

	/*认证模块错误码-start*/
	public final static String AUTH_UNKNOWN="30000";
	public final static String AUTH_USER_ALREADY_EXISTS="30001";//用户已存在
	public final static String AUTH_AUTHENTICATION_FAILED="30002";//认证失败
	public final static String AUTH_PARAMETER_ERROR="30003";//用户名密码参数错误，为空
	public final static String AUTH_ACTIVATE_FAILED="30004";//邮件注册，激活失败
	public final static String AUTH_REPLACEMENT_FAILED="30005";//置换token失败
	public final static String AUTH_TOKEN_INVALID="30006";//token无效
	public static final String AUTH_ILLEGAL_USERCODE = "30007";//非法的用户名
	
	interface Auth{
		String AUTH_UNKNOWN="30000";
		String AUTH_USER_ALREADY_EXISTS="30001";//用户已存在
		String AUTH_AUTHENTICATION_FAILED="30002";//认证失败
		String AUTH_PARAMETER_ERROR="30003";//用户名密码参数错误，为空
		String AUTH_ACTIVATE_FAILED="30004";//邮件注册，激活失败
		String AUTH_REPLACEMENT_FAILED="30005";//置换token失败
		String AUTH_TOKEN_INVALID="30006";//token无效
		String AUTH_ILLEGAL_USERCODE = "30007";//非法的用户名

	}


	enum AuthException{

		AUTH_LOGIN_EXCEPTION("30000","登录失败"),
		AUTH_ACTIVATE_EXCEPTION("30001","激活失败");

		private Double num = 100.21321;

		private String code;

		private String msg;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		AuthException(String code, String msg){
			this.code=code;
			this.msg=msg;
		}


	}
	/*认证模块错误码-end*/

	public static void main(String[] args) {
		System.out.println(AuthException.AUTH_LOGIN_EXCEPTION.getCode());
	}
}
