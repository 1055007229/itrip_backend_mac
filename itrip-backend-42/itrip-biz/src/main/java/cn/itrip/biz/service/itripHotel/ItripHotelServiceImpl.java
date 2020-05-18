package cn.itrip.biz.service.itripHotel;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.mapper.itripHotel.ItripHotelMapper;
import cn.itrip.mapper.itripHotelFeature.ItripHotelFeatureMapper;
import cn.itrip.mapper.itripHotelTradingArea.ItripHotelTradingAreaMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ItripHotelServiceImpl implements ItripHotelService {

    @Resource
    private ItripHotelMapper itripHotelMapper;

    @Resource
    private ItripHotelFeatureMapper itripHotelFeatureMapper;

    @Resource
    private ItripHotelTradingAreaMapper itripHotelTradingAreaMapper;

    public ItripHotel getItripHotelById(Long id)throws Exception{
        return itripHotelMapper.getItripHotelById(id);
    }

    public List<ItripHotel>	getItripHotelListByMap(Map<String,Object> param)throws Exception{
        return itripHotelMapper.getItripHotelListByMap(param);
    }

    public Integer getItripHotelCountByMap(Map<String,Object> param)throws Exception{
        return itripHotelMapper.getItripHotelCountByMap(param);
    }

    public Integer itriptxAddItripHotel(ItripHotel itripHotel)throws Exception{
            itripHotel.setCreationDate(new Date());
            return itripHotelMapper.insertItripHotel(itripHotel);
    }

    public Integer itriptxModifyItripHotel(ItripHotel itripHotel)throws Exception{
        itripHotel.setModifyDate(new Date());
        return itripHotelMapper.updateItripHotel(itripHotel);
    }

    public Integer itriptxDeleteItripHotelById(Long id)throws Exception{
        return itripHotelMapper.deleteItripHotelById(id);
    }

    public Page<ItripHotel> queryItripHotelPageByMap(Map<String,Object> param, Integer pageNo, Integer pageSize)throws Exception{
        Integer total = itripHotelMapper.getItripHotelCountByMap(param);
        pageNo = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        pageSize = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        Page page = new Page(pageNo, pageSize, total);
        param.put("beginPos", page.getBeginPos());
        param.put("pageSize", page.getPageSize());
        List<ItripHotel> itripHotelList = itripHotelMapper.getItripHotelListByMap(param);
        page.setRows(itripHotelList);
        return page;
    }

    @Override
    public HotelVideoDescVO getHotelVideoDescVOById(String hotelId) throws Exception {
        HotelVideoDescVO hotelVideoDescVO= new HotelVideoDescVO();
        //获取酒店名称
        ItripHotel itripHotel = itripHotelMapper.getItripHotelById(Long.parseLong(hotelId));
        hotelVideoDescVO.setHotelName(itripHotel.getHotelName());
        //获取特色集合
        hotelVideoDescVO.setHotelFeatureList(itripHotelFeatureMapper.getHotelFeatureById(hotelId));
        //获取商圈集合
        hotelVideoDescVO.setTradingAreaNameList(itripHotelTradingAreaMapper.getHotelTradingAreaById(hotelId));
        return hotelVideoDescVO;
    }

}
