package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.*;
import cn.itrip.beans.vo.order.ItripAddHotelOrderVO;
import cn.itrip.beans.vo.order.ItripModifyHotelOrderVO;
import cn.itrip.beans.vo.order.RoomStoreVO;
import cn.itrip.beans.vo.order.ValidateRoomStoreVO;
import cn.itrip.biz.service.itripHotel.ItripHotelService;
import cn.itrip.biz.service.itripHotelOrder.ItripHotelOrderService;
import cn.itrip.biz.service.itripHotelRoom.ItripHotelRoomService;
import cn.itrip.biz.service.itripTradeEnds.ItripTradeEndsService;
import cn.itrip.common.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping(value = "/api/hotelOrder")
public class HotelOrderController {

    @Autowired
    private ValidationToken validationToken;

    @Autowired
    private ItripHotelService itripHotelService;

    @Autowired
    private ItripHotelRoomService itripHotelRoomService;

    @Autowired
    private ItripHotelOrderService itripHotelOrderService;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private ItripTradeEndsService itripTradeEndsService;

    @RequestMapping(value = "/getPreOrderInfo", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto<RoomStoreVO> getPreOrderInfo(@RequestBody ValidateRoomStoreVO validateRoomStoreVO, HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        if (!validationToken.validateToken(token, agent)) {
            return DtoUtil.returnFail("token失效，请重新登录！", "100000");
        }
        if (validateRoomStoreVO.getHotelId() == null || validateRoomStoreVO.getHotelId() == 0) {
            return DtoUtil.returnFail("hotelId不能为空", "100510");
        }
        if (validateRoomStoreVO.getRoomId() == null || validateRoomStoreVO.getRoomId() == 0) {
            return DtoUtil.returnFail("roomId不能为空", "100511");
        }
        if (EmptyUtils.isEmpty(validateRoomStoreVO.getCheckInDate()) || EmptyUtils.isEmpty(validateRoomStoreVO.getCheckOutDate())) {
            return DtoUtil.returnFail("入住和退房时间不能为空！", "100514");
        }
        if (validateRoomStoreVO.getCheckInDate().getTime() > validateRoomStoreVO.getCheckOutDate().getTime()) {
            return DtoUtil.returnFail("入住时间不能晚于退房时间！", "100515");
        }
        try {
            //获取酒店
            ItripHotel itripHotel = itripHotelService.getItripHotelById(validateRoomStoreVO.getHotelId());
            //获取房间
            ItripHotelRoom itripHotelRoom = itripHotelRoomService.getItripHotelRoomById(validateRoomStoreVO.getRoomId());
            //返回对象
            RoomStoreVO roomStoreVO = new RoomStoreVO();
            roomStoreVO.setHotelName(itripHotel.getHotelName());
            roomStoreVO.setCount(1);
            roomStoreVO.setPrice(itripHotelRoom.getRoomPrice());
            roomStoreVO.setCheckInDate(validateRoomStoreVO.getCheckInDate());
            roomStoreVO.setCheckOutDate(validateRoomStoreVO.getCheckOutDate());
            roomStoreVO.setHotelId(validateRoomStoreVO.getHotelId());
            roomStoreVO.setRoomId(validateRoomStoreVO.getRoomId());

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("startTime", validateRoomStoreVO.getCheckInDate());
            param.put("endTime", validateRoomStoreVO.getCheckOutDate());
            param.put("roomId", validateRoomStoreVO.getRoomId());
            param.put("hotelId", validateRoomStoreVO.getHotelId());

            //当前房间库存
            List<ItripHotelTempStore> itripHotelTempStoreList = itripHotelOrderService.queryRoomStore(param);
            if (EmptyUtils.isNotEmpty(itripHotelTempStoreList)) {
                roomStoreVO.setStore(itripHotelTempStoreList.get(0).getStore());
            } else {
                return DtoUtil.returnFail("暂时无房！", "100512");
            }
            return DtoUtil.returnDataSuccess(roomStoreVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！", "100513");
        }
    }

    @RequestMapping(value = "/validateRoomStore", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto<Map<String, Boolean>> validateRoomStore(@RequestBody ValidateRoomStoreVO validateRoomStoreVO, HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        if (!validationToken.validateToken(token, agent)) {
            return DtoUtil.returnFail("token失效，请重新登录！", "100000");
        }
        if (validateRoomStoreVO.getHotelId() == null || validateRoomStoreVO.getHotelId() == 0) {
            return DtoUtil.returnFail("hotelId不能为空", "100515");
        }
        if (validateRoomStoreVO.getRoomId() == null || validateRoomStoreVO.getRoomId() == 0) {
            return DtoUtil.returnFail("roomId不能为空", "100516");
        }
        if (EmptyUtils.isEmpty(validateRoomStoreVO.getCheckInDate()) || EmptyUtils.isEmpty(validateRoomStoreVO.getCheckOutDate())) {
            return DtoUtil.returnFail("入住和退房时间不能为空！", "100514");
        }
        if (validateRoomStoreVO.getCheckInDate().getTime() > validateRoomStoreVO.getCheckOutDate().getTime()) {
            return DtoUtil.returnFail("入住时间不能晚于退房时间！", "100515");
        }
        if (validateRoomStoreVO.getCount() == null || validateRoomStoreVO.getCount() == 0) {
            return DtoUtil.returnFail("需要输入房间数！", "100518");
        }
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("startTime", validateRoomStoreVO.getCheckInDate());
            param.put("endTime", validateRoomStoreVO.getCheckOutDate());
            param.put("roomId", validateRoomStoreVO.getRoomId());
            param.put("hotelId", validateRoomStoreVO.getHotelId());
            param.put("count", validateRoomStoreVO.getCount());
            boolean result = itripHotelOrderService.validateRoomStore(param);
            Map<String, Boolean> output = new HashMap<String, Boolean>();
            output.put("flag", result);
            if (!result) {
                return DtoUtil.returnFail("暂时无房！", "100512");
            }
            return DtoUtil.returnDataSuccess(output);
            //Map<String, Object> result = itripHotelOrderService.validateRoomStores(param);
            //return DtoUtil.returnDataSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常！", "100517");
        }
    }


    @RequestMapping(value = "/addHotelOrder", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto<Map<String, String>> addHotelOrder(@RequestBody ItripAddHotelOrderVO itripAddHotelOrderVO, HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        //当前登录的用户对象 下单人
        ItripUser itripUser = validationToken.getCurrentUser(token);
        if (!validationToken.validateToken(token, agent)) {
            return DtoUtil.returnFail("token失效，请重新登录！", "100000");
        }
        if (EmptyUtils.isEmpty(itripAddHotelOrderVO)) {
            return DtoUtil.returnFail("不能提交空，请填写订单信息", "100506");
        }
        //判断时间是否正确
        List<Date> dates = DateUtil.getBetweenDates(itripAddHotelOrderVO.getCheckInDate(),
                itripAddHotelOrderVO.getCheckOutDate());
        if (dates.size() == 0) {
            return DtoUtil.returnFail("退房日期必须大于入住日期", "100505");
        }
        //获取酒店
        ItripHotel itripHotel = itripHotelService.getItripHotelById(itripAddHotelOrderVO.getHotelId());
        //判断库存是否充足
        Map<String, Object> param = new HashMap<>();
        param.put("startTime", itripAddHotelOrderVO.getCheckInDate());
        param.put("endTime", itripAddHotelOrderVO.getCheckOutDate());
        param.put("roomId", itripAddHotelOrderVO.getRoomId());
        param.put("hotelId", itripAddHotelOrderVO.getHotelId());
        param.put("count", itripAddHotelOrderVO.getCount());
        try {
            boolean flag = itripHotelOrderService.validateRoomStore(param);
            if (!flag) {
                return DtoUtil.returnFail("暂时无房！", "100512");
            }
            //封装订单对象
            ItripHotelOrder hotelOrder = new ItripHotelOrder();
            //复制相同字段数据
            BeanUtils.copyProperties(itripAddHotelOrderVO, hotelOrder);
            hotelOrder.setUserId(itripUser.getId());
            hotelOrder.setCreatedBy(itripUser.getId());
            hotelOrder.setCreationDate(new Date());
            hotelOrder.setHotelName(itripHotel.getHotelName());
            //封装入住信息----------------------
            if (EmptyUtils.isNotEmpty(itripAddHotelOrderVO.getLinkUser())) {
                StringBuffer linkUserName = new StringBuffer();
                int i = 0;
                for (ItripUserLinkUser itripUserLinkUser : itripAddHotelOrderVO.getLinkUser()) {
                    linkUserName.append(itripUserLinkUser.getLinkUserName());
                    if (i != itripAddHotelOrderVO.getLinkUser().size() - 1) {
                        linkUserName.append(",");
                    }
                    i++;
                }
                hotelOrder.setLinkUserName(linkUserName.toString());
            }
            //封装入住天数
            hotelOrder.setBookingDays(dates.size());
            //封装订单类型----------------------
            if (token.startsWith("token:PC-")) {
                hotelOrder.setBookType(0);
            } else if (token.startsWith("token:MOBILE-")) {
                hotelOrder.setBookType(1);
            } else {
                hotelOrder.setBookType(2);
            }
            //封装订单状态 默认0
            hotelOrder.setOrderStatus(0);
            //生成订单号      机器码 +日期+（MD5结尾）（商品IDs+毫秒数+1000000的随机数）

            //1.生成加密订单结尾
            StringBuffer md5Code = new StringBuffer();
            md5Code.append(itripAddHotelOrderVO.getHotelId());
            md5Code.append(itripAddHotelOrderVO.getRoomId());
            md5Code.append(itripUser.getId());
            md5Code.append(Calendar.getInstance().getTimeInMillis());

            //2.开始拼接订单
            StringBuffer orderNo = new StringBuffer();
            orderNo.append(systemConfig.getMachineCode());
            orderNo.append(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
            orderNo.append(MD5.getMd5(md5Code.toString(), 6));

            hotelOrder.setOrderNo(orderNo.toString());
            hotelOrder.setOrderType(1);

            //计算价格
            BigDecimal amout = itripHotelOrderService.getOrderAmount(itripAddHotelOrderVO.getRoomId(), dates.size() * itripAddHotelOrderVO.getCount());
            hotelOrder.setPayAmount(amout);


            //保存订单信息，保存入住人信息
            Map<String, String> result = itripHotelOrderService.itriptxAddItripHotelOrder(hotelOrder, itripAddHotelOrderVO.getLinkUser());

            return DtoUtil.returnDataSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("生成订单失败！", "100505");
        }
    }

    @RequestMapping(value = "/updateOrderStatusAndPayType", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto updateOrderStatusAndPayType(@RequestBody ItripModifyHotelOrderVO itripModifyHotelOrderVO, HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        if (!validationToken.validateToken(token, agent)) {
            return DtoUtil.returnFail("token失效，请重新登录！", "100000");
        }
        if (EmptyUtils.isEmpty(itripModifyHotelOrderVO) || itripModifyHotelOrderVO.getId() == null || itripModifyHotelOrderVO.getId() == 0 || itripModifyHotelOrderVO.getPayType() == null || itripModifyHotelOrderVO.getPayType() == 0) {
            return DtoUtil.returnFail("不能提交空，请填写订单信息！", "100523");
        }
        //是否支持线下支付 不支持线下支付
        if (!itripHotelOrderService.isOffLinePaymentSupported(itripModifyHotelOrderVO.getId())) {
            return DtoUtil.returnFail("对不起，此房间不支持线下支付！", "100521");
        }
        //支持线下支付
        try {
            //更新状态和支付类型
            ItripHotelOrder itripHotelOrder = new ItripHotelOrder();
            itripHotelOrder.setId(itripModifyHotelOrderVO.getId());
            itripHotelOrder.setPayType(itripModifyHotelOrderVO.getPayType());
            //订单状态，以支付
            itripHotelOrder.setOrderStatus(2);
            ItripUser itripUser = validationToken.getCurrentUser(token);
            itripHotelOrder.setModifiedBy(itripUser.getId());
            itripHotelOrder.setModifyDate(new Date());
            //更新
            itripHotelOrderService.itriptxModifyItripHotelOrder(itripHotelOrder);
            return DtoUtil.returnSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("修改订单失败！", "100522");
        }
    }

    @RequestMapping(value = "/querySuccessOrderInfo/{orderId}", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto<Map<String, Object>> querySuccessOrderInfo(@PathVariable Long orderId, HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        if (!validationToken.validateToken(token, agent)) {
            return DtoUtil.returnFail("token失效，请重新登录！", "100000");
        }
        if (orderId == null || orderId == 0) {
            return DtoUtil.returnFail("orderId不能为空！", "100519");
        }
        try {
            ItripHotelOrder itripHotelOrder = itripHotelOrderService.getItripHotelOrderById(orderId);
            if (EmptyUtils.isEmpty(itripHotelOrder)) {
                return DtoUtil.returnFail("获取数据失败！", "100520");
            }
            ItripHotelRoom itripHotelRoom = itripHotelRoomService.getItripHotelRoomById(itripHotelOrder.getRoomId());
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("id", itripHotelOrder.getId());
            result.put("orderNo", itripHotelOrder.getOrderNo());
            result.put("payType", itripHotelOrder.getPayType());
            result.put("payAmount", itripHotelOrder.getPayAmount());
            result.put("hotelName", itripHotelOrder.getHotelName());
            result.put("roomTitle", itripHotelRoom.getRoomTitle());
            return DtoUtil.returnDataSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取数据失败！", "100520");
        }
    }

    @RequestMapping(value = "/scanTradeEnd", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto scanTradeEnd() {
        //扫描中间表 未处理为0 更新实时库存表
        //将0改为1，表示正在处理
        //1.从trade_ends表查询出flag=1的记录，得到订单id和orderNo
        //2.根据订单的入住时间退房时间相对应的减少该房间的库存
        //3.将flag=1 => flag=2 表示已处理
        try {
            this.job();
            return DtoUtil.returnSuccess("成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100536");
        }
    }

    //秒 分 时 日 月 周 [年] * 0/2 * * * *
    //    //'*'表示不限
    //    //数字具体时间：0 * 1 5 * ?
    //    //','多个时间点：0 0 1,3,5 * * ?
    //    //'-'表示范围：0 0 1-5 * * ?
    //    //'/'表示每：0 0 0/2 * * ?
    //    //'?'只出现在日和周，二选一：* * * 2 * ?
    @Scheduled(cron = "0 0/30 * * * ?")
    public void job() throws Exception {
        Map<String, Object> param = new HashMap<>();
        //标注中间表订单状态为处理中
        param.put("oldFlag", 0);
        param.put("flag", 1);
        int count = itripTradeEndsService.itriptxModifyItripTradeEndsFlag(param);
        if (count == 0) {
            System.out.println("当前没有需要处理的订单");
            return;
        }
        //获取支付中间表集合(拿到处理中的订单)
        List<ItripTradeEnds> itripTradeEndsList = itripTradeEndsService.getItripTradeEndsListByMap(param);
        //减库存
        itripHotelOrderService.itriptxFlushTempStore(itripTradeEndsList);
        //修改中间表 状态改为 处理完成
        param.put("oldFlag", 1);
        param.put("flag", 2);
        itripTradeEndsService.itriptxModifyItripTradeEndsFlag(param);
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void flushCancelOrderStatus() {
        //调用service
        try {
            boolean flag = itripHotelOrderService.flushOrderStatus(1);
            System.out.println(flag ? "超时订单刷单成功" : "超时订单刷单失败！暂无超时订单！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("超时刷单异常！");
        }
    }

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void flushSuccessOrderStatus() {
        //调用service
        try {
            boolean flag = itripHotelOrderService.flushOrderStatus(2);
            System.out.println(flag ? "已消费刷单成功" : "已消费刷单失败！暂无超时订单！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("已消费刷单异常！");
        }
    }

}
