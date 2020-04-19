package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.utils.BeanHelper;
import com.leyou.utils.JsonUtils;
import com.leyou.vo.PageResult;

import org.apache.commons.lang3.StringUtils;


import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class SearchService {
    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spuDTO){
        Goods goods = new Goods();
        goods.setId(spuDTO.getId());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setSubTitle(spuDTO.getSubTitle());
        String all=spuDTO.getName()+spuDTO.getBrandName()+spuDTO.getCategoryName();
        goods.setAll(all);//以上为简单参数以下为复杂参数

        //skus是一个集合内部为map[{},{}]
        //根据spu拿到sku的列表
        List<SkuDTO> skuDTOList = itemClient.findSku(spuDTO.getId());
        //创建一个集合用来收集map
        ArrayList<Map> skus = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map map=new HashMap();
            map.put("id", skuDTO.getId());
            map.put("title", skuDTO.getTitle());
            map.put("price", skuDTO.getPrice());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(),","));//取第一张图片
            //map.put("image",skuDTO.getImages().split(",")[0]);
            skus.add(map);
        }
        goods.setSkus(JsonUtils.toString(skus));
        //收集每一个sku的price存入set集合
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        goods.setPrice(priceSet);

        //specs需要dpudetail对象
        SpuDetailDTO spuDetail = itemClient.findSpuDetail(spuDTO.getId());
        //转成map
        String genericSpec = spuDetail.getGenericSpec();//普通参数
        Map<Long,String> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class,String.class);
        String specialSpec = spuDetail.getSpecialSpec();//特殊参数
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {});
        //将连个map整合到一个map中  并将其中的id改为名称
        HashMap<String, Object> specsMap = new HashMap<>();
        //根据分类id查询规格参数
        List<SpecParamDTO> specParamList = itemClient.findSpecParamByGid(null, spuDTO.getCid3(), true);
        for (SpecParamDTO specParam : specParamList) {
            String key=specParam.getName();
            Object value="";
            if (specParam.getGeneric()){
                value=genericSpecMap.get(specParam.getId());
            }else {
                value=specialSpecMap.get(specParam.getId());
            }
            if (specParam.getIsNumeric()){
                value=chooseSegment(value,specParam);
            }
            specsMap.put(key, value);
        }
        goods.setSpecs(specsMap);
        return goods;
    }

    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    @Autowired
    private ElasticsearchTemplate template;
    public PageResult<GoodsDTO> findGoodsByPage(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if (StringUtils.isEmpty(key)){
            return null;
        }
        //es和spring整合   SpringDataElasticSearch
        //使用java和原生查询方式结合查询
        //用match查询  只要 id subtitle skus
        //构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //设置显示的过滤字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subtitle","skus"}, null));
        //根据关键字match查询所有
        buildBasicQuery(searchRequest, queryBuilder);
        //设置分页
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1, size, Sort.Direction.DESC,"createTime"));
        //执行查询
        AggregatedPage<Goods> aggregatedPage = template.queryForPage(queryBuilder.build(), Goods.class);
        //拿到当前页数据
        List<Goods> goodsList = aggregatedPage.getContent();
        if (CollectionUtils.isEmpty(goodsList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //转为dto
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);
        //拿到总条数和总页数
        long total = aggregatedPage.getTotalElements();
        Integer pages= aggregatedPage.getTotalPages();

        return new PageResult<GoodsDTO>(total,pages.longValue(),goodsDTOS);
    }

    public Map<String, List<?>> filter(SearchRequest searchRequest) {
        HashMap<String, List<?>> resultMap = new HashMap<>();
        String key = searchRequest.getKey();
//        做一个判断是否有关键字
        if(StringUtils.isEmpty(key)){
            return null;
        }
        //构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //设置显示过滤字段啊
        queryBuilder.withSourceFilter(new FetchSourceFilter(null, null ));
        //根据关键字match查询
        buildBasicQuery(searchRequest, queryBuilder);
        //设置分页 起始位置不是当前页码
        Integer page = searchRequest.getPage();
        queryBuilder.withPageable(PageRequest.of(page-1, 1));
        //构建品牌聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId").size(20));
        //构建分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId").size(20));
        //执行查询
        AggregatedPage<Goods> aggregatedPage = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = aggregatedPage.getAggregations();
        //从聚合结果获取品牌
        Terms termsBrand = aggregations.get("brandAgg");
        List<? extends Terms.Bucket> brandBuckets = termsBrand.getBuckets();
        //收集品牌id成集合
        List<Long> brandIdList = brandBuckets.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());
        //调用接口根据品牌id集合查询品牌对象集合
        List<BrandDTO> brandList = itemClient.findBrandListByIds(brandIdList);
        resultMap.put("品牌", brandList);
        //从聚合结果中获取分类
        Terms termsCategory= aggregations.get("categoryAgg");
        List<? extends Terms.Bucket> categoryBuckets = termsCategory.getBuckets();
        //收集分类id成集合
        List<Long> categoryIdList = categoryBuckets.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());


        //掉接口查询分类对象集合
        List<CategoryDTO> categoryDTOS = itemClient.findCategoryByIds(categoryIdList);
        resultMap.put("分类", categoryDTOS);


        //开始构建规格参数的查询
        //规格参数需要显示当前分类的规格参数  所以通过分类查询当前分类下的参数

        if(categoryIdList!=null&&categoryIdList.size()>0){
            Long categoryId = categoryIdList.get(0);
            List<SpecParamDTO> paramList = itemClient.findSpecParamByGid(null, categoryId, true);
//           重新执行查询，重新构建条件
            NativeSearchQueryBuilder queryParamBuilder = new NativeSearchQueryBuilder();
            queryParamBuilder.withSourceFilter(new FetchSourceFilter(null,null)); //不显示goods的属性
//        根据关键字match查询
            buildBasicQuery(searchRequest, queryParamBuilder); //执行了基本查询
            queryParamBuilder.withPageable(PageRequest.of(0,1)) ;//封装了 "from" 和  "size"
//           真正的构建聚合条件
            for (SpecParamDTO specParam : paramList) {
                String paramName = specParam.getName();
                queryParamBuilder.addAggregation(AggregationBuilders.terms(paramName+"Agg").field("specs."+paramName).size(20));
            }
//           执行查询
            AggregatedPage<Goods> aggregatedParamPage = template.queryForPage(queryParamBuilder.build(), Goods.class);
            Aggregations paramPageAggregations = aggregatedParamPage.getAggregations();
//           获取聚合后的结果
            for (SpecParamDTO specParam : paramList) {
                String paramName = specParam.getName();
                Terms terms = paramPageAggregations.get(paramName + "Agg");
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> paramAggList = buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                //System.out.println(paramAggList);
                resultMap.put(paramName,paramAggList);
            }
        }


        return resultMap;
    }

    private void buildBasicQuery(SearchRequest searchRequest, NativeSearchQueryBuilder queryBuilder) {
       // queryBuilder.withQuery(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));

        Map<String, Object> filterMap = searchRequest.getFilterMap();
        if (filterMap!=null){
            for (String key : filterMap.keySet()) {
                if (key.equals("品牌")){
                    boolQuery.filter(QueryBuilders.termQuery("brandId", filterMap.get(key)));
                }else if (key.equals("分类")){
                    boolQuery.filter(QueryBuilders.termQuery("categoryId", filterMap.get(key)));
                }else {
                    boolQuery.filter(QueryBuilders.termQuery("specs."+key, filterMap.get(key)));
                }
            }
        }
        queryBuilder.withQuery(boolQuery);
    }
    @Autowired
    private GoodsRepository goodsRepository;
    public void createGoods(Long spuId) {
        //查询数据
        SpuDTO spu = itemClient.findSpuBySpuId(spuId);
        //添加品牌名称和分类名称
        Long brandId = spu.getBrandId();
        BrandDTO brandDTO = itemClient.findBrandByBrangId(brandId);
        spu.setBrandName(brandDTO.getName());
        List<Long> categoryIds = spu.getCategoryIds();
        List<CategoryDTO> category = itemClient.findCategoryByIds(categoryIds);
        String categoryName = category.stream().map(CategoryDTO::getName).collect(Collectors.joining("/"));
        spu.setCategoryName(categoryName);
        //转为goods
        Goods goods = this.buildGoods(spu);
        //存入es
        goodsRepository.save(goods);
        System.out.println("索引数据添加成功");
    }

    public void removeGoods(Long spuId) {
        goodsRepository.deleteById(spuId);
        System.out.println("索引数据删除成功");
    }
}



