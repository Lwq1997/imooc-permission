package com.lwq.service;

import com.google.common.base.Preconditions;
import com.lwq.dao.SysDeptMapper;
import com.lwq.exception.ParamException;
import com.lwq.model.SysDept;
import com.lwq.param.DeptParam;
import com.lwq.util.BeanValidator;
import com.lwq.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 22:05
 * @Version 1.0
 * @Describe
 */
@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    public void save(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept dept = SysDept.builder()
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .remark(param.getRemark()).build();

        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        dept.setOperator("system");//TODO
        dept.setOperateIp("127.0.0.1");
        dept.setOperateTime(new Date());

        sysDeptMapper.insertSelective(dept);
    }

    public void update(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }

        SysDept beforeDept = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(beforeDept,"待更新的部门不存在");

        SysDept afterDept = SysDept.builder()
                .id(param.getId())
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .remark(param.getRemark()).build();

        afterDept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        afterDept.setOperator("system-update");//TODO
        afterDept.setOperateIp("127.0.0.1");
        afterDept.setOperateTime(new Date());

        updateWithChild(beforeDept,afterDept);
    }

    @Transactional
    public void updateWithChild(SysDept before, SysDept after){

        String newLevelPerfix = after.getLevel();
        String oldLevelPerfix = before.getLevel();

        if(StringUtils.equals(oldLevelPerfix,newLevelPerfix)){
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(oldLevelPerfix);
            if(CollectionUtils.isNotEmpty(deptList)){
                for(SysDept dept:deptList){
                    String level = dept.getLevel();
                    if(level.indexOf(oldLevelPerfix)==0){
                        level = newLevelPerfix+level.substring(oldLevelPerfix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        sysDeptMapper.updateByPrimaryKey(after);
    }

    private boolean checkExist(Integer parentId,String deptName,Integer deptId){
        return sysDeptMapper.countByNameAndParentId(parentId,deptName,deptId) > 0;
    }

    private String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }


}
