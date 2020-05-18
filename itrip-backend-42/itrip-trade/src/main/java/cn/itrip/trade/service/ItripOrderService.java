package cn.itrip.trade.service;

import cn.itrip.beans.pojo.ItripHotelOrder;

/**
 * @outhor Ricardo
 * @date 2020-04-01
 */
public interface ItripOrderService {

    public ItripHotelOrder getItripHotelOrderByOrderNo(String orderNo) throws Exception;

    public void processOrderStatus(String orderNo,String tradeNo,int payType) throws Exception;

}
