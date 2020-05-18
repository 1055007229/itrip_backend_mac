package cn.itrip.trade.controller;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.common.EmptyUtils;
import cn.itrip.trade.config.AlipayConfig;
import cn.itrip.trade.service.ItripOrderService;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @outhor Ricardo
 * @date 2020-04-01
 */
@Controller
@RequestMapping(value = "/api")
public class AliPaymentController {

    @Autowired
    private ItripOrderService itripOrderService;

    @Autowired
    private AlipayConfig alipayConfig;

    @RequestMapping(value = "/prePay/{orderNo}", method = RequestMethod.GET)
    public String prePay(@PathVariable String orderNo, Model model) throws Exception {
        try {
            ItripHotelOrder itripHotelOrder = itripOrderService.getItripHotelOrderByOrderNo(orderNo);
            if (EmptyUtils.isEmpty(itripHotelOrder)) {
                return "notfound";
            }
            model.addAttribute("orderNo", itripHotelOrder.getOrderNo());
            model.addAttribute("hotelName", itripHotelOrder.getHotelName());
            model.addAttribute("roomId", itripHotelOrder.getRoomId());
            model.addAttribute("count", itripHotelOrder.getCount());
            model.addAttribute("payAmount", itripHotelOrder.getPayAmount());
            return "pay";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public void pay(@RequestParam(value = "WIDout_trade_no") String WIDout_trade_no, @RequestParam(value = "WIDsubject") String WIDsubject, @RequestParam(value = "WIDtotal_amount") String WIDtotal_amount, HttpServletResponse response) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getAppID(), alipayConfig.getRsaPrivateKey(), "json", alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(), alipayConfig.getSignType());

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());

        //商户订单号，商户网站订单系统中唯一订单号，必填
        //String out_trade_no = new String(request.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"),"UTF-8");
        //付款金额，必填
        //String total_amount = new String(request.getParameter("WIDtotal_amount").getBytes("ISO-8859-1"),"UTF-8");
        //订单名称，必填
        //String subject = new String(request.getParameter("WIDsubject").getBytes("ISO-8859-1"),"UTF-8");
        //商品描述，可空
        //String body = new String(request.getParameter("WIDbody").getBytes("ISO-8859-1"),"UTF-8");

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + WIDout_trade_no + "\","
                + "\"total_amount\":\"" + WIDtotal_amount + "\","
                + "\"subject\":\"" + WIDsubject + "\","
                //+ "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        System.out.println(alipayRequest.getBizContent());

        /*AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(WIDout_trade_no);
        model.setTotalAmount(WIDtotal_amount);
        model.setSubject(WIDsubject);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizModel(model);*/

        //若想给BizContent增加其他可选请求参数，以增加自定义超时时间参数timeout_express来举例说明
        //alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
        //		+ "\"total_amount\":\""+ total_amount +"\","
        //		+ "\"subject\":\""+ subject +"\","
        //		+ "\"body\":\""+ body +"\","
        //		+ "\"timeout_express\":\"10m\","
        //		+ "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println(result);
        //输出
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        System.out.println("=============================================================");
        System.out.println("return：" + params);

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType()); //调用SDK验证签名

        //——请在这里编写您的程序（以下代码仅作参考）——
        if (signVerified) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");

            //String appId = params.get("app_id");

            //out.println("trade_no:"+trade_no+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+total_amount);


            /*ItripHotelOrder itripHotelOrder = itripOrderService.getItripHotelOrderByOrderNo(out_trade_no);

            if (EmptyUtils.isNotEmpty(itripHotelOrder)) {
                if (itripHotelOrder.getPayAmount() == BigDecimal.valueOf(Double.parseDouble(total_amount))) {
                    if (alipayConfig.getAppID().equals(appId)) {
                        //处理订单
                        itripHotelOrder.setPayType(1);
                        itripHotelOrder.setOrderStatus(2);
                        itripHotelOrder.setTradeNo(trade_no);
                        itripOrderService.processOrderStatus(itripHotelOrder);
                        return "success";
                    }
                }
            }*/

            try {
                ItripHotelOrder order = itripOrderService.getItripHotelOrderByOrderNo(out_trade_no);
                request.setAttribute("hotelName", order.getHotelName());
                request.setAttribute("roomId", order.getRoomId());
                request.setAttribute("checkInDate", order.getCheckInDate());
                request.setAttribute("checkOutDate", order.getCheckOutDate());
                request.setAttribute("trade_no", trade_no);
                request.setAttribute("total_amount", total_amount);
                request.setAttribute("index", "http://itrip.project.bdqn.cn");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("验签成功");
            return "success";
        } else {
            System.out.println("验签失败");
            return "failure";
        }
        //——请在这里编写您的程序（以上代码仅作参考）——
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public void trackPaymentStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /* *
         * 功能：支付宝服务器异步通知页面
         * 日期：2017-03-30
         * 说明：
         * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
         * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。


         *************************页面功能说明*************************
         * 创建该页面文件时，请留心该页面文件中无任何HTML代码及空格。
         * 该页面不能在本机电脑测试，请到服务器上做测试。请确保外部可以访问该页面。
         * 如果没有收到该页面返回的 success
         * 建议该页面只做支付成功的业务逻辑处理，退款的处理请以调用退款查询接口的结果为准。
         */

        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            //获得key
            String name = (String) iter.next();

            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";

            }

            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        System.out.println("=============================================================");
        System.out.println("notify：" + params);

        //验签
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType()); //调用SDK验证签名

        //——请在这里编写您的程序（以下代码仅作参考）——

	/* 实际验证过程建议商户务必添加以下校验：
	1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
	4、验证app_id是否为该商户本身。
	*/
        if (signVerified) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            if (trade_status.equals("TRADE_FINISHED")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知

                itripOrderService.processOrderStatus(out_trade_no, trade_no, 1);
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //付款完成后，支付宝系统发送该交易状态通知
                itripOrderService.processOrderStatus(out_trade_no, trade_no, 1);
            }
            System.out.println("异步请求成功！");
        } else {
            System.out.println("验签失败");
        }
        //——请在这里编写您的程序（以上代码仅作参考）——

    }

}
