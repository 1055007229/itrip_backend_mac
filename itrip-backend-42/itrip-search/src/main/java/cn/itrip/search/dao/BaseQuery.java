package cn.itrip.search.dao;

import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.common.PropertiesUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class BaseQuery<T> {
    //solr客户端对象
    private SolrClient solrClient;

    //读取database.properties
    private String baseUrl = PropertiesUtils.get("database.properties", "baseUrl");
    private String connectionTimeout = PropertiesUtils.get("database.properties", "connectionTimeout");
    private String socketTimeout = PropertiesUtils.get("database.properties", "socketTimeout");

    //创建日志
    Logger logger = Logger.getLogger(BaseQuery.class);

    //构造方法
    public BaseQuery() {
        solrClient = new HttpSolrClient.Builder(baseUrl).withConnectionTimeout(Integer.parseInt(connectionTimeout)).withSocketTimeout(Integer.parseInt(socketTimeout)).build();
    }

    public Page<T> queryPage(SolrQuery query, Integer pageNo, Integer pageSize, Class calzz) throws IOException, SolrServerException {
        //设置开始页数
        Integer currNo = EmptyUtils.isNotEmpty(pageNo) ? pageNo - 1 : Constants.DEFAULT_PAGE_NO - 1;
        //返回条数
        Integer rows = EmptyUtils.isNotEmpty(pageSize) ? pageSize : Constants.DEFAULT_PAGE_SIZE;
        //求出开始索引
        Integer start = currNo * rows;
        //查询对象中设置分页
        query.setStart(start);
        query.setRows(rows);
        //执行查询 获取solr结果集
        QueryResponse queryResponse = solrClient.query(query);
        //获取solr文档集合（为了求出 总数据条数）
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        //创建并封装分页对象（数据对象）
        Page page = new Page(currNo + 1, rows, (int) solrDocumentList.getNumFound());
        //创建数据对象 并获取solr结果集中到数据
        List<T> dataList = queryResponse.getBeans(calzz);
        page.setRows(dataList);
        return page;
    }

}
