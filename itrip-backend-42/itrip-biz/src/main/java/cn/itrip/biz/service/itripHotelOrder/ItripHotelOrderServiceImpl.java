package cn.itrip.biz.service.itripHotelOrder;

import cn.itrip.beans.pojo.*;
import cn.itrip.common.BigDecimalUtil;
import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.mapper.itripHotelOrder.ItripHotelOrderMapper;
import cn.itrip.mapper.itripHotelRoom.ItripHotelRoomMapper;
import cn.itrip.mapper.itripOrderLinkUser.ItripOrderLinkUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItripHotelOrderServiceImpl implements ItripHotelOrderService {

    @Resource
    private ItripHotelOrderMapper itripHotelOrderMapper;

    @Resource
    private ItripHotelRoomMapper itripHotelRoomMapper;

    @Resource
    private ItripOrderLinkUserMapper itripOrderLinkUserMapper;

    public ItripHotelOrder getItripHotelOrderById(Long id) throws Exception {
        return itripHotelOrderMapper.getItripHotelOrderById(id);
    }

    public List<ItripHotelOrder> getItripHotelOrderListByMap(Map<String, Object> param) throws Exception {
        return itripHotelOrderMapper.getItripHotelOrderListByMap(param);
    }

    public Integer getItripHotelOrderCountByMap(Map<String, Object> param) throws Exception {
        return itripHotelOrderMapper.getItripHotelOrderCountByMap(param);
    }

    public Map<String, String> itriptxAddItripHotelOrder(ItripHotelOrder itripHotelOrder, List<ItripUserLinkUser> itripUserLinkUserList) throws Exception {
        Map<String, String> output = new HashMap<String, String>();
        if (EmptyUtils.isEmpty(itripHotelOrder.getId())) {
            itripHotelOrder.setCreationDate(new Date());
            itripHotelOrderMapper.insertItripHotelOrder(itripHotelOrder);
        } else {
            itripOrderLinkUserMapper.deleteItripOrderLinkUserByOrderId(itripHotelOrder.getId());
            itripHotelOrder.setModifyDate(new Date());
            itripHotelOrderMapper.updateItripHotelOrder(itripHotelOrder);
        }
        if (EmptyUtils.isNotEmpty(itripUserLinkUserList)) {
            for (ItripUserLinkUser linkUser : itripUserLinkUserList) {
                ItripOrderLinkUser orderLinkUser = new ItripOrderLinkUser();
                orderLinkUser.setLinkUserId(linkUser.getUserId());
                orderLinkUser.setLinkUserName(linkUser.getLinkUserName());
                orderLinkUser.setOrderId(itripHotelOrder.getId());
                orderLinkUser.setCreationDate(new Date());
                orderLinkUser.setCreatedBy(linkUser.getUserId());
                //添加入住人中间表
                itripOrderLinkUserMapper.insertItripOrderLinkUser(orderLinkUser);
            }
        }
        output.put("id", itripHotelOrder.getId().toString());
        output.put("orderNo", itripHotelOrder.getOrderNo());
        return output;
    }

    public Integer itriptxModifyItripHotelOrder(ItripHotelOrder itripHotelOrder) throws Exception {
        ItripHotelOrder orderToModify = itripHotelOrderMapper.getItripHotelOrderById(itripHotelOrder.getId());
        //变更实时库存表 减库存
        this.flushTempStore(orderToModify);
        //更新订单表
        return itripHotelOrderMapper.updateItripHotelOrder(itripHotelOrder);
    }

    public Integer flushTempStore(ItripHotelOrder itripHotelOrder) throws Exception {
        //变更实时库存表 减库存
        Map<String,Object> param = new HashMap<String, Object>();
        param.put("count",itripHotelOrder.getCount());
        param.put("roomId",itripHotelOrder.getRoomId());
        param.put("startDate",itripHotelOrder.getCheckInDate());
        param.put("endDate",itripHotelOrder.getCheckOutDate());
        //减库存
        return itripHotelOrderMapper.flushTempStore(param);
    }

    public Integer itriptxDeleteItripHotelOrderById(Long id) throws Exception {
        return itripHotelOrderMapper.deleteItripHotelOrderById(id);
    }

    public Page<ItripHotelOrder> queryItripHotelOrderPageByMap(Map<String, Object> param, Integer pageNo, Integer pageSize) throws Exception {
        Integer total = itripHotelOrderMapper.getItripHotelOrderCountByMap(param);
        pageNo = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        pageSize = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        Page page = new Page(pageNo, pageSize, total);
        param.put("beginPos", page.getBeginPos());
        param.put("pageSize", page.getPageSize());
        List<ItripHotelOrder> itripHotelOrderList = itripHotelOrderMapper.getItripHotelOrderListByMap(param);
        page.setRows(itripHotelOrderList);
        return page;
    }

    @Override
    public List<ItripHotelTempStore> queryRoomStore(Map<String, Object> param) throws Exception {
        //完善实时库存表，调用存储过程
        itripHotelOrderMapper.flushStore(param);
        //结合实时库存表和订单表的临时记录（为支付）计算每一天的真实库存
        return itripHotelOrderMapper.queryRoomStore(param);
    }

    @Override
    public boolean validateRoomStore(Map<String, Object> param) throws Exception {
        //用户输入count
        Integer count = (Integer) param.get("count");
        //查出实际最小库存
        List<ItripHotelTempStore> result = this.queryRoomStore(param);
        //最小库存 >= 用户输入count
        if (EmptyUtils.isEmpty(result)) {
            return false;
        }
        return result.get(0).getStore() >= count;
    }

    @Override
    public Map<String, Object> validateRoomStores(Map<String, Object> param) throws Exception {
        //用户输入count
        Integer count = (Integer) param.get("count");
        //查出实际最小库存
        List<ItripHotelTempStore> result = this.queryRoomStore(param);
        //最小库存 >= 用户输入count
        Map<String, Object> output = new HashMap<String, Object>();
        if (EmptyUtils.isEmpty(result)) {
            output.put("flag", false);
            output.put("store", 0);
            return output;
        }
        output.put("flag", result.get(0).getStore() >= count);
        output.put("store", result.get(0).getStore());
        return output;
    }

    @Override
    public BigDecimal getOrderAmount(Long roomId, Integer count) throws Exception {
        //获取房间价格
        BigDecimal roomPrice = itripHotelRoomMapper.getItripHotelRoomById(roomId).getRoomPrice();
        //声明总价格
        //BigDecimal payAmount=null;
        //round_down 1.33933 1.33 round_up 1.33333 1.34 1.3311 1.34 1.330 1.33
        return BigDecimalUtil.OperationASMD(roomPrice, count,
                BigDecimalUtil.BigDecimalOprations.multiply,
                2, BigDecimal.ROUND_DOWN);
    }

    @Override
    public void createOrder(ItripHotelOrder order, List<ItripUserLinkUser> linkUserList) throws Exception {
        //添加订单
        itripHotelOrderMapper.insertItripHotelOrder(order);
        for (ItripUserLinkUser linkUser : linkUserList) {
            ItripOrderLinkUser orderLinkUser = new ItripOrderLinkUser();
            orderLinkUser.setLinkUserId(linkUser.getUserId());
            orderLinkUser.setLinkUserName(linkUser.getLinkUserName());
            orderLinkUser.setOrderId(order.getId());
            orderLinkUser.setCreationDate(new Date());
            orderLinkUser.setCreatedBy(linkUser.getUserId());
            //添加入住人中间表
            itripOrderLinkUserMapper.insertItripOrderLinkUser(orderLinkUser);
        }
    }

    @Override
    public boolean isOffLinePaymentSupported(Long id) throws Exception {
        Integer payType = itripHotelOrderMapper.getPayTypeByHotelId(id);
        return ((2 & payType) != 0);
    }

    @Override
    public void itriptxFlushTempStore(List<ItripTradeEnds> itripTradeEndsList) throws Exception {
        for (ItripTradeEnds itripTradeEnds : itripTradeEndsList) {
            ItripHotelOrder itripHotelOrder = itripHotelOrderMapper.getItripHotelOrderById(itripTradeEnds.getId());
            this.flushTempStore(itripHotelOrder);
        }
    }

    @Override
    public boolean flushOrderStatus(Integer orderStatus) throws Exception {
        int flag = 0;
        if (orderStatus == 1) {//执行刷新过期订单
            flag = itripHotelOrderMapper.flushCancelOrderStatus();
        } else {//执行刷新成功消费的订单
            flag = itripHotelOrderMapper.flushSuccessOrderStatus();
        }
        return flag >=1 ? true : false;
    }

}
