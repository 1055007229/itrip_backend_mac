package cn.itrip.search.service;

import cn.itrip.beans.vo.hotel.SearchHotelVO;
import cn.itrip.common.Page;
import cn.itrip.search.beans.Hotel;
import cn.itrip.search.beans.HotelVO;
import cn.itrip.search.beans.ItripHotelVO;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public interface SearchHotelService {

    /*public Page<Hotel> searchHotelPageTest(HotelVO HotelVO) throws IOException, SolrServerException;*/

    public Page<ItripHotelVO> searchHotelPage(SearchHotelVO searchHotelVO) throws IOException, SolrServerException;
}
