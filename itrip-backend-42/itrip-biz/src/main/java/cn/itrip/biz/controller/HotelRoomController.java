package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripHotelRoom;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import cn.itrip.beans.vo.hotelroom.SearchHotelRoomVO;
import cn.itrip.biz.service.itripHotelRoom.ItripHotelRoomService;
import cn.itrip.biz.service.itripImage.ItripImageService;
import cn.itrip.biz.service.itripLabelDic.ItripLabelDicService;
import cn.itrip.common.DateUtil;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping(value = "/api/hotelRoom")
public class HotelRoomController {

    @Autowired
    private ItripLabelDicService itripLabelDicService;

    @Autowired
    private ItripHotelRoomService itripHotelRoomService;

    @Autowired
    private ItripImageService itripImageService;

    @RequestMapping(value = "/queryHotelRoomBed", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelRoomBed() {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("parentId", 1);
        try {
            List<ItripLabelDic> itripLabelDicList = itripLabelDicService.getItripLabelDicListByMap(param);
            List<ItripLabelDicVO> itripAreaDicVOList = new ArrayList<ItripLabelDicVO>();
            if (EmptyUtils.isNotEmpty(itripLabelDicList)) {
                for (ItripLabelDic itripLabelDic : itripLabelDicList) {
                    ItripLabelDicVO itripLabelDicVO = new ItripLabelDicVO();
                    BeanUtils.copyProperties(itripLabelDic, itripLabelDicVO);
                    itripAreaDicVOList.add(itripLabelDicVO);
                }
            }
            return DtoUtil.returnDataSuccess(itripAreaDicVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取九点房间床型失败！", "100305");
        }
    }

    @RequestMapping(value = "/queryHotelRoomByHotel", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public Dto queryHotelRoomByHotel(@RequestBody SearchHotelRoomVO searchHotelRoomVO) {
        if (searchHotelRoomVO.getHotelId() == null || searchHotelRoomVO.getHotelId() == 0) {
            return DtoUtil.returnFail("酒店ID不能为空！", "100303");
        }
        if (EmptyUtils.isEmpty(searchHotelRoomVO.getEndDate()) || EmptyUtils.isEmpty(searchHotelRoomVO.getStartDate())) {
            return DtoUtil.returnFail("入住时间或退房时间不能为空！", "100303");
        }
        if (searchHotelRoomVO.getEndDate().getTime() < searchHotelRoomVO.getStartDate().getTime()) {
            return DtoUtil.returnFail("酒店入住及退房时间不能为空,入住时间不能大于退房时间", "100303");
        }
        //获取时间集合
        List<Date> dates = DateUtil.getBetweenDates(searchHotelRoomVO.getStartDate(), searchHotelRoomVO.getEndDate());
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("hotelId", searchHotelRoomVO.getHotelId());
        param.put("isBook", searchHotelRoomVO.getIsBook());
        param.put("isHavingBreakfast", searchHotelRoomVO.getIsHavingBreakfast());
        param.put("isTimelyResponse", searchHotelRoomVO.getIsTimelyResponse());
        param.put("roomBedTypeId", searchHotelRoomVO.getRoomBedTypeId());
        param.put("isCancel", searchHotelRoomVO.getIsCancel());
        param.put("payType", searchHotelRoomVO.getPayType() == null ? 3 : searchHotelRoomVO.getPayType());
        param.put("dates", dates);
        try {
            List<ItripHotelRoom> itripHotelRoomList = itripHotelRoomService.getItripHotelRoomListByMap(param);
            List<List<ItripHotelRoomVO>> itripHotelRoomVOLists = new ArrayList<List<ItripHotelRoomVO>>();
            for (ItripHotelRoom itripHotelRoom : itripHotelRoomList) {
                //pojo转vo
                ItripHotelRoomVO itripHotelRoomVO = new ItripHotelRoomVO();
                BeanUtils.copyProperties(itripHotelRoom, itripHotelRoomVO);
                //创建床型 二级集合
                List<ItripHotelRoomVO> tempList = new ArrayList<ItripHotelRoomVO>();
                //把转换好的vo放入 二级集合
                tempList.add(itripHotelRoomVO);
                //把二级集合放入 一级集合
                itripHotelRoomVOLists.add(tempList);
            }
            return DtoUtil.returnDataSuccess(itripHotelRoomList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100304");
        }
    }

    @RequestMapping(value = "/getImg/{targetId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getImg(@PathVariable String targetId) {

        if (EmptyUtils.isEmpty(targetId)) {
            return DtoUtil.returnFail("酒店id不能为空", "100302");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("type", 1);
        param.put("targetId", targetId);
        try {
            List<ItripImage> itripImageList = itripImageService.getItripImageListByMap(param);
            List<ItripImageVO> itripImageVOList = new ArrayList<>();

            for (ItripImage image : itripImageList) {
                ItripImageVO imageVO = new ItripImageVO();
                BeanUtils.copyProperties(image, imageVO);
                itripImageVOList.add(imageVO);
            }
            return DtoUtil.returnDataSuccess(itripImageVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店图片失败", "100301");
        }
    }

}
