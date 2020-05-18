package cn.itrip.search.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.hotel.SearchHotelVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.search.beans.ItripHotelVO;
import cn.itrip.search.service.SearchHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/api/hotelList")
public class HotelListController {

    @Autowired
    private SearchHotelService searchHotelService;

    /*@RequestMapping(value = "/searchItripHotelPageTest", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto searchItripHotelPageTest(@RequestBody HotelVO hotelVO){
        //solr查询
        try {
            Page<Hotel> itripHotelVOPage = searchHotelService.searchHotelPageTest(hotelVO);
            return DtoUtil.returnDataSuccess(itripHotelVOPage);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常,获取失败","20001");
        }
    }*/

    @RequestMapping(value = "/searchItripHotelPage", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto searchItripHotelPage(@RequestBody SearchHotelVO searchHotelVO) {
        if (EmptyUtils.isEmpty(searchHotelVO)
                || EmptyUtils.isEmpty(searchHotelVO.getDestination())) {
            return DtoUtil.returnFail("目的地不能为空！", "20002");
        }
        //solr查询
        try {
            Page<ItripHotelVO> itripHotelVOPage = searchHotelService.searchHotelPage(searchHotelVO);
            return DtoUtil.returnDataSuccess(itripHotelVOPage);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常,获取失败", "20001");
        }
    }

}
