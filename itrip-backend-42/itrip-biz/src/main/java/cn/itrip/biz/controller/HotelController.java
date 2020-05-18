package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.biz.service.itripImage.ItripImageService;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.biz.service.itripAreaDic.ItripAreaDicService;
import cn.itrip.biz.service.itripHotel.ItripHotelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/api/hotel")
public class HotelController {

    @Autowired
    private ItripHotelService itripHotelService;

    @Autowired
    private ItripAreaDicService itripAreaDicService;

    @Autowired
    private ItripImageService itripImageService;

    @RequestMapping(value = "/queryHotCity/{type}", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripAreaDicVO> queryHotCity(@PathVariable Integer type) {
        if (type == null || type == 0) {
            return DtoUtil.returnFail("type不能为空", "10201");
        }
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("isHot", 1);
            param.put("isChina", type);
            List<ItripAreaDic> itripAreaDicList = itripAreaDicService.getItripAreaDicListByMap(param);
            List<ItripAreaDicVO> itripAreaDicVOList = new ArrayList<ItripAreaDicVO>();
            if (EmptyUtils.isNotEmpty(itripAreaDicList)) {
                for (ItripAreaDic itripAreaDic : itripAreaDicList) {
                    ItripAreaDicVO itripAreaDicVO = new ItripAreaDicVO();
                    BeanUtils.copyProperties(itripAreaDic, itripAreaDicVO);
                    itripAreaDicVOList.add(itripAreaDicVO);
                }
            }
            return DtoUtil.returnDataSuccess(itripAreaDicVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常获取失败", "10202");
        }
    }

    @RequestMapping(value = "/queryTradearea/{cityId}", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripAreaDicVO> queryTradearea(@PathVariable Integer cityId) {
        if (cityId == null || cityId == 0) {
            return DtoUtil.returnFail("cityId不能为空", "10203");
        }
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("isTradingArea", 1);
            param.put("parent", cityId);
            List<ItripAreaDic> itripAreaDicList = itripAreaDicService.getItripAreaDicListByMap(param);
            List<ItripAreaDicVO> itripAreaDicVOList = new ArrayList<ItripAreaDicVO>();
            if (EmptyUtils.isNotEmpty(itripAreaDicList)) {
                for (ItripAreaDic itripAreaDic : itripAreaDicList) {
                    ItripAreaDicVO itripAreaDicVO = new ItripAreaDicVO();
                    BeanUtils.copyProperties(itripAreaDic, itripAreaDicVO);
                    itripAreaDicVOList.add(itripAreaDicVO);
                }
            }
            return DtoUtil.returnDataSuccess(itripAreaDicVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常获取失败", "10204");
        }
    }

    @RequestMapping(value = "/getImg/{targetId}", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripImageVO> getImg(@PathVariable Integer targetId) {
        if (targetId == null || targetId == 0) {
            return DtoUtil.returnFail("targetId不能为空", "100213");
        }
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("type", "0");
            param.put("targetId", targetId);
            List<ItripImage> itripImageList = itripImageService.getItripImageListByMap(param);
            List<ItripImageVO> itripImageVOList = new ArrayList<ItripImageVO>();
            if (EmptyUtils.isNotEmpty(itripImageList)) {
                for (ItripImage itripImage : itripImageList) {
                    ItripImageVO itripImageVO = new ItripImageVO();
                    BeanUtils.copyProperties(itripImage, itripImageVO);
                    itripImageVOList.add(itripImageVO);
                }
            }
            return DtoUtil.returnDataSuccess(itripImageVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常获取失败", "100214");
        }
    }

    @RequestMapping(value = "/getVideoDesc/{hotelId}", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto getVideoDesc(@PathVariable String hotelId) {
        if (EmptyUtils.isEmpty(hotelId.trim())) {
            return DtoUtil.returnFail("酒店id不能为空", "100215");
        }
        //查询     酒店特色、商圈、酒店名称
        try {
            HotelVideoDescVO hotelVideoDescVO = itripHotelService.getHotelVideoDescVOById(hotelId);
            return DtoUtil.returnDataSuccess(hotelVideoDescVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常,获取失败", "10211");
        }
    }

}
