package com.lwq.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.lwq.dao.SysAclMapper;
import com.lwq.dao.SysAclModuleMapper;
import com.lwq.dao.SysDeptMapper;
import com.lwq.dto.AclDto;
import com.lwq.dto.AclModuleLevelDto;
import com.lwq.dto.DeptLevelDto;
import com.lwq.model.SysAcl;
import com.lwq.model.SysAclModule;
import com.lwq.model.SysDept;
import com.lwq.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 22:28
 * @Version 1.0
 * @Describe
 */
@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysCoreService sysCoreService;

    @Resource
    private SysAclMapper sysAclMapper;

    public List<AclModuleLevelDto> roleTree(int roleId){
        //当前用户已经被分配的权限点
        List<SysAcl> userAclList = sysCoreService.getCurrentUserAclList();
        //当前角色已经被分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        //系统所有的权限点
        List<SysAcl> aclList = sysAclMapper.getAll();

        //对所有的权限点进行封装
        List<AclDto> aclDtoList = Lists.newArrayList();

        Set<Integer> userAclIdList = userAclList.stream()
                .map(sysAcl -> sysAcl.getId())
                .collect(Collectors.toSet());
        Set<Integer> roleAclIdList = roleAclList.stream()
                .map(sysAcl -> sysAcl.getId())
                .collect(Collectors.toSet());

        for(SysAcl acl:aclList){
            AclDto dto = AclDto.adapt(acl);
            if(userAclIdList.contains(acl.getId())){
                dto.setHasAcl(true);
            }
            if(roleAclIdList.contains(acl.getId())){
                dto.setChecked(true);
            }
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }

    private List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList) {
        if(CollectionUtils.isEmpty(aclDtoList)){
            return Lists.newArrayList();
        }
        //系统所有的权限模块树
        List<AclModuleLevelDto> aclModuleLevelList = aclModuleTree();

        Multimap<Integer,AclDto> moduleIdAclMap = ArrayListMultimap.create();
        for(AclDto acl:aclDtoList){
            if(acl.getStatus()==1){
                moduleIdAclMap.put(acl.getAclModuleId(),acl);
            }
        }
        bindAclsWithOrder(aclModuleLevelList,moduleIdAclMap);
        return aclModuleLevelList;
    }

    private void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelList, Multimap<Integer, AclDto> moduleIdAclMap) {
        if(CollectionUtils.isEmpty(aclModuleLevelList)){
            return;
        }
        for(AclModuleLevelDto dto:aclModuleLevelList){
            List<AclDto> aclDtoList = (List<AclDto>) moduleIdAclMap.get(dto.getId());
            if(CollectionUtils.isNotEmpty(aclDtoList)){
                Collections.sort(aclDtoList,aclSeqComparator);
                dto.setAclList(aclDtoList);
            }
            bindAclsWithOrder(dto.getAclModuleList(),moduleIdAclMap);
        }
    }


    public List<AclModuleLevelDto> aclModuleTree(){
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();

        for(SysAclModule aclModule:aclModuleList){
            AclModuleLevelDto dto = AclModuleLevelDto.adapt(aclModule);
            dtoList.add(dto);
        }
        return aclModuleListToTree(dtoList);
    }


    public List<DeptLevelDto> deptTree(){
        List<SysDept> deptList = sysDeptMapper.getAllDept();
        List<DeptLevelDto> dtoList = Lists.newArrayList();

        for(SysDept dept:deptList){
            DeptLevelDto dto = DeptLevelDto.adapt(dept);
            dtoList.add(dto);
        }
        return deptListToTree(dtoList);
    }

    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        if(CollectionUtils.isEmpty(deptLevelList)){
            return Lists.newArrayList();
        }
//        level -> [dept1,dept2....]
//        Map<String,List<Object>>
        Multimap<String,DeptLevelDto> levelDtoMultiMap = ArrayListMultimap.create();
        List<DeptLevelDto> rootList = Lists.newArrayList();

        for(DeptLevelDto dto:deptLevelList){
            levelDtoMultiMap.put(dto.getLevel(),dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        //按照seq排序,处理第一层
        Collections.sort(rootList,deptSeqComparator);

        //递归生成树（从第二层开始）
        transformDeptTree(rootList,LevelUtil.ROOT,levelDtoMultiMap);
        return rootList;
    }

    //level:0 0,all
    public void transformDeptTree(List<DeptLevelDto> deptLevelList,String level,Multimap<String,DeptLevelDto> levelDtoMultiMap){
        for(int  i = 0; i < deptLevelList.size(); i++){
            //遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level,deptLevelDto.getId());
            //处理下一层
            List<DeptLevelDto> temoDeptList = (List<DeptLevelDto>) levelDtoMultiMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(temoDeptList)){
                //排序
                Collections.sort(temoDeptList,deptSeqComparator);

                //设置下一层
                deptLevelDto.setDeptList(temoDeptList);

                //处理下一层
                transformDeptTree(temoDeptList,nextLevel,levelDtoMultiMap);
            }
        }
    }

    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };

    private List<AclModuleLevelDto> aclModuleListToTree( List<AclModuleLevelDto> aclModuleLevelList) {
        if(CollectionUtils.isEmpty(aclModuleLevelList)){
            return Lists.newArrayList();
        }
//        level -> [aclModule1,aclModule2....]
//        Map<String,List<Object>>
        Multimap<String,AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for(AclModuleLevelDto dto:aclModuleLevelList){
            levelAclModuleMap.put(dto.getLevel(),dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        //按照seq排序,处理第一层
        Collections.sort(rootList, aclModuleSeqComparator);

        //递归生成树（从第二层开始）
        transformAclModuleTree(rootList,LevelUtil.ROOT,levelAclModuleMap);
        return rootList;
    }

    private void transformAclModuleTree(List<AclModuleLevelDto> aclModuleLevelList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for(int  i = 0; i < aclModuleLevelList.size(); i++){
            //遍历该层的每个元素
            AclModuleLevelDto aclModuleLevelDto = aclModuleLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level,aclModuleLevelDto.getId());
            //处理下一层
            List<AclModuleLevelDto> tempAclModuleList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(tempAclModuleList)){
                //排序
                Collections.sort(tempAclModuleList,aclModuleSeqComparator);

                //设置下一层
                aclModuleLevelDto.setAclModuleList(tempAclModuleList);

                //处理下一层
                transformAclModuleTree(tempAclModuleList,nextLevel,levelAclModuleMap);
            }
        }
    }

    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        @Override
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };

    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        @Override
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq()-o2.getSeq();
        }
    };
}
