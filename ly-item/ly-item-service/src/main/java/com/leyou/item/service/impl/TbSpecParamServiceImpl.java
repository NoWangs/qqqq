package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecParamMapper;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import com.leyou.utils.BeanHelper;
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.ws.Action;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 规格参数组下的参数名 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSpecParamServiceImpl extends ServiceImpl<TbSpecParamMapper, TbSpecParam> implements TbSpecParamService {

    @Override
    public List<SpecParamDTO> findSpecParamBygid(Long gid, Long cid, Boolean searching) {
//        gid和cid必须保持有一个值
//        判断gid和cid是否同时为空，如果是抛异常
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if(gid==null&&cid==null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        if(gid!=null){
//            构建gid的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getGroupId,gid);
        }
        if(cid!=null){
//            构建cid的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getCid,cid);
        }
        if(searching!=null){
//            构建searching的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getSearching,searching);  //true--->1  false --->0
        }

        List<TbSpecParam> tbSpecParamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSpecParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSpecParamList,SpecParamDTO.class);
    }
    @Autowired
    private TbSpecGroupService specGroupService;
    @Override
    public List<SpecGroupDTO> findSpecGroupWithSpecParamByCategoryId(Long id) {
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,id);
        //拿到所有参数组的集合
        List<TbSpecGroup> tbSpecGroupList = specGroupService.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSpecGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        List<SpecGroupDTO> specGroupDTOList = BeanHelper.copyWithCollection(tbSpecGroupList, SpecGroupDTO.class);
        //查出所有参数
        List<SpecParamDTO> paramDTOList = this.findSpecParamBygid(null, id, null);
        //按组id搜集
        Map<Long, List<SpecParamDTO>> paramMapByGroup = paramDTOList.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));

        specGroupDTOList = specGroupDTOList.stream().map(group -> {
            List<SpecParamDTO> paramDTOS = paramMapByGroup.get(group.getId());
            group.setParams(paramDTOS);
            return group;
        }).collect(Collectors.toList());
        return specGroupDTOList;
    }
}
