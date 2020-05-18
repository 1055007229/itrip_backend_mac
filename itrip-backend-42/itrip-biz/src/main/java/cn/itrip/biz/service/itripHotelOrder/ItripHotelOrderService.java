package cn.itrip.biz.service.itripHotelOrder;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.beans.pojo.ItripHotelTempStore;
import cn.itrip.beans.pojo.ItripTradeEnds;
import cn.itrip.beans.pojo.ItripUserLinkUser;
import cn.itrip.common.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by shang-pc on 2015/11/7.
 */
public interface ItripHotelOrderService {

    public ItripHotelOrder getItripHotelOrderById(Long id) throws Exception;

    public List<ItripHotelOrder> getItripHotelOrderListByMap(Map<String, Object> param) throws Exception;

    public Integer getItripHotelOrderCountByMap(Map<String, Object> param) throws Exception;

    public Map<String, String> itriptxAddItripHotelOrder(ItripHotelOrder itripHotelOrder, List<ItripUserLinkUser> itripUserLinkUserList) throws Exception;

    public Integer itriptxModifyItripHotelOrder(ItripHotelOrder itripHotelOrder) throws Exception;

    public Integer itriptxDeleteItripHotelOrderById(Long id) throws Exception;

    public Page<ItripHotelOrder> queryItripHotelOrderPageByMap(Map<String, Object> param, Integer pageNo, Integer pageSize) throws Exception;

    public List<ItripHotelTempStore> queryRoomStore(Map<String, Object> param) throws Exception;

    public boolean validateRoomStore(Map<String, Object> param) throws Exception;

    public Map<String, Object> validateRoomStores(Map<String, Object> param) throws Exception;

    public BigDecimal getOrderAmount(Long roomId, Integer count) throws Exception;

    public void createOrder(ItripHotelOrder order, List<ItripUserLinkUser> linkUserList) throws Exception;

    public boolean isOffLinePaymentSupported(Long id) throws Exception;

    public void itriptxFlushTempStore(List<ItripTradeEnds> itripTradeEndsList) throws Exception;

    public boolean flushOrderStatus(Integer orderStatus) throws Exception;
}
