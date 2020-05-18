package cn.itrip.trade.service.impl;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.beans.pojo.ItripTradeEnds;
import cn.itrip.common.EmptyUtils;
import cn.itrip.mapper.itripHotelOrder.ItripHotelOrderMapper;
import cn.itrip.mapper.itripTradeEnds.ItripTradeEndsMapper;
import cn.itrip.trade.service.ItripOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @outhor Ricardo
 * @date 2020-04-01
 */
@Service
public class ItripOrderServiceImpl implements ItripOrderService {

    @Resource
    private ItripHotelOrderMapper itripHotelOrderMapper;

    @Resource
    private ItripTradeEndsMapper itripTradeEndsMapper;

    @Override
    public ItripHotelOrder getItripHotelOrderByOrderNo(String orderNo) throws Exception {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("orderNo", orderNo);
        List<ItripHotelOrder> itripHotelOrderList = itripHotelOrderMapper.getItripHotelOrderListByMap(param);
        if (EmptyUtils.isEmpty(itripHotelOrderList)) {
            return null;
        }
        return itripHotelOrderList.get(0);
    }

    @Override
    public void processOrderStatus(String orderNo, String tradeNo, int payType) throws Exception {
        ItripHotelOrder itripHotelOrder = this.getItripHotelOrderByOrderNo(orderNo);
        if (EmptyUtils.isEmpty(itripHotelOrder.getTradeNo())) {
            //修改订单信息
            itripHotelOrder.setTradeNo(tradeNo);
            itripHotelOrder.setPayType(payType);
            itripHotelOrder.setModifyDate(new Date());
            itripHotelOrder.setOrderStatus(2);
            itripHotelOrderMapper.updateItripHotelOrder(itripHotelOrder);
            //向itrip_trade_ends添加待处理记录
            ItripTradeEnds itripTradeEnds = new ItripTradeEnds();
            itripTradeEnds.setId(itripHotelOrder.getId());
            itripTradeEnds.setOrderNo(itripHotelOrder.getOrderNo());
            itripTradeEnds.setFlag(0);
            itripTradeEndsMapper.insertItripTradeEnds(itripTradeEnds);
        }
    }

}
