package cn.itrip.search.service.impl;

import cn.itrip.beans.vo.hotel.SearchHotelVO;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.common.PropertiesUtils;
import cn.itrip.search.beans.Hotel;
import cn.itrip.search.beans.HotelVO;
import cn.itrip.search.beans.ItripHotelVO;
import cn.itrip.search.dao.BaseQuery;
import cn.itrip.search.service.SearchHotelService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchHotelServiceImpl implements SearchHotelService {

    //声明solr查询工具类
    /*private BaseQuery<Hotel> baseQuery = null;*/
    @Autowired
    private BaseQuery<ItripHotelVO> baseQuery;

    /*@Override
    public Page<Hotel> searchHotelPageTest(HotelVO hotelVO) throws IOException, SolrServerException {
        //创建查询对象
        SolrQuery solrQuery = new SolrQuery("*:*");
        //拼接query主查询语句
        StringBuilder queryBuilder = new StringBuilder();
        //默认拼接地址
        if (EmptyUtils.isNotEmpty(hotelVO.getKeyword())) {
            String[] keywords = hotelVO.getKeyword().split(" ");
            int tempFlag = 0;
            for (String keyword : keywords) {
                if (EmptyUtils.isEmpty(keyword)) {
                    continue;
                }
                if (tempFlag == 0) {
                    queryBuilder.append(" AND keyword:" + keyword);
                } else {
                    queryBuilder.append(" OR keyword:" + keyword);
                }
                tempFlag = 1;
            }
        }
        //往查询对象中添加 主查询
        solrQuery.setQuery(queryBuilder.toString());
        return baseQuery.queryPage(solrQuery,
                hotelVO.getPageNo(),
                hotelVO.getPageSize(),
                Hotel.class);
    }*/

    @Override
    public Page<ItripHotelVO> searchHotelPage(SearchHotelVO searchHotelVO) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery("*:*"); // where 1=1
        //构建q查询
        StringBuilder queryBuilder = new StringBuilder();
        //设置目的地
        queryBuilder.append(" destination:" + searchHotelVO.getDestination());
        //判断关键字是否为空
        if (EmptyUtils.isNotEmpty(searchHotelVO.getKeywords())) {
            //xxx ccc => 'xxx' 'ccc' 拆分关键字字符串 北京饭点 北京 and 饭店
            String[] keywords = searchHotelVO.getKeywords().split(" ");
            int tempFlag = 0;
            for (String keyword : keywords) {
                if (EmptyUtils.isEmpty(keyword)) {
                    continue;
                }
                if (tempFlag == 0) {
                    queryBuilder.append(" AND keyword:" + keyword);
                } else {
                    queryBuilder.append(" OR keyword:" + keyword);
                }
                tempFlag = 1;
            }

        }
        //往查询对象中添加q查询
        solrQuery.setQuery(queryBuilder.toString());
        //构建fq条件 商圈
        if (EmptyUtils.isNotEmpty(searchHotelVO.getTradeAreaIds())) {
            //拆分商圈字符串
            String[] tradeAreaIds = searchHotelVO.getTradeAreaIds().split(",");
            StringBuilder tradeAreaIdsBuilder = new StringBuilder("(");
            int tempFlag = 0;
            for (String tranAreaId : tradeAreaIds) {
                if (tempFlag == 0)
                    tradeAreaIdsBuilder.append(" tradingAreaIds:*," + tranAreaId + ",*");
                else
                    tradeAreaIdsBuilder.append(" OR tradingAreaIds:*," + tranAreaId + ",*");
                tempFlag = 1;
            }
            tradeAreaIdsBuilder.append(")");
            solrQuery.addFilterQuery(tradeAreaIdsBuilder.toString());
        }
        //最低价格
        if (EmptyUtils.isNotEmpty(searchHotelVO.getMinPrice())) {
            solrQuery.addFilterQuery(" minPrice:[" + searchHotelVO.getMinPrice() + " TO *]");
        }
        //最高价格
        if (EmptyUtils.isNotEmpty(searchHotelVO.getMaxPrice())) {
            solrQuery.addFilterQuery(" maxPrice:[* TO " + searchHotelVO.getMaxPrice() + "]");
        }
        //酒店星级
        if (EmptyUtils.isNotEmpty(searchHotelVO.getHotelLevel())) {
            solrQuery.addFilterQuery(" hotelLevel:" + searchHotelVO.getHotelLevel());
        }
        //酒店特色
        if (EmptyUtils.isNotEmpty(searchHotelVO.getFeatureIds())) {
            String[] featureIds = searchHotelVO.getFeatureIds().split(",");
            StringBuilder featureIdBuilder = new StringBuilder("(");
            int tempFlag = 0;
            for (String featureId : featureIds) {
                if (tempFlag == 0)
                    featureIdBuilder.append(" featureIds:*," + featureId + ",*");
                else
                    featureIdBuilder.append(" OR featureIds:*," + featureId + ",*");
                tempFlag = 1;
            }
            featureIdBuilder.append(")");
            solrQuery.addFilterQuery(featureIdBuilder.toString());
        }
        //欢迎度 升序 降序
        if (EmptyUtils.isNotEmpty(searchHotelVO.getAscSort())) {
            solrQuery.addSort(searchHotelVO.getAscSort(), SolrQuery.ORDER.asc);
        }
        if (EmptyUtils.isNotEmpty(searchHotelVO.getDescSort())) {
            solrQuery.addSort(searchHotelVO.getDescSort(), SolrQuery.ORDER.desc);
        }
        return baseQuery.queryPage(solrQuery,
                searchHotelVO.getPageNo(),
                searchHotelVO.getPageSize(),
                ItripHotelVO.class);
    }

}
