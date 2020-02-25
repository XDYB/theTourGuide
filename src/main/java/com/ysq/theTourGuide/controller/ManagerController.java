package com.ysq.theTourGuide.controller;

import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.config.ErrorCode;
import com.ysq.theTourGuide.entity.*;
import com.ysq.theTourGuide.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    AdministratorService administratorService;

    @Autowired
    AdministratorTypeService administratorTypeService;

    @Autowired
    AdministratorAuthorityService administratorAuthorityService;

    @Autowired
    ScenicService scenicService;

    @Autowired
    GuideService guideService;

    /**
     * 管理员登录
     * @param account
     * @param psw
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public ResultDTO login(String account,String psw)throws Exception{
        List<Administrator> administrators = administratorService.findByParams(new Administrator(account));
        if(administrators.size()==0){
            return ResultUtil.Error(ErrorCode.ADMINISTRATOR_NOEXIST);
        }
        Administrator administrator = administrators.get(0);
        if(administrator.getPassword().equals(psw)){
            return ResultUtil.Success(administrator);
        }else{
            return ResultUtil.Error(ErrorCode.ERROR_PSW);
        }
    }

    /**
     * 添加管理员
     * @param administratorId
     * @param administratorTypeId
     * @param account
     * @param psw
     * @return
     * @throws Exception
     */
    @PostMapping("/addAdministrator")
    @ApiOperation("添加管理员")
    public ResultDTO addAdministrator(Integer administratorId,Integer administratorTypeId,String account,String psw)throws Exception{
        AdministratorType administratorType = administratorTypeService.get(
                administratorService.get(administratorId).getTypeId());
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(administratorType
                .getAuthorityId());
        if(administratorAuthority.getAddAdministrator() || (administratorAuthority.getAddChildAdmistrator() && ((administratorTypeId ==3 && administratorType.getId() == 2)|| (administratorTypeId == 5 &&  administratorType.getId() == 4)))) {
            if(administratorService.findByParams(new Administrator(account)).size()==0) {
                return ResultUtil.Success(administratorService.save(new Administrator(account, psw, administratorTypeId)));
            }else {
                return ResultUtil.Error(ErrorCode.ISEXIST);
            }
        }else {
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 获得管理类型
     * @return
     * @throws Exception
     */
    @GetMapping("/getAdministratorType")
    @ApiOperation("获得管理类型")
    public ResultDTO getAdministratorType()throws Exception{
        return ResultUtil.Success(administratorTypeService.findAll());
    }

    /**
     * 删除管理员
     * @param administratorId
     * @param deleteAdministratorId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteAdministrator")
    @ApiOperation("删除管理员")
    public ResultDTO deleteAdministrator(Integer administratorId,Integer deleteAdministratorId)throws Exception{
        AdministratorType administratorType = administratorTypeService.get(
                administratorService.get(administratorId).getTypeId());
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(administratorType
                .getAuthorityId());
        AdministratorType deleteAdministratorType = administratorTypeService.get(
                administratorService.get(administratorId).getTypeId());
        if(administratorAuthority.getAddAdministrator() || (administratorAuthority.getAddChildAdmistrator() && ((deleteAdministratorType.getId() ==3 && administratorType.getId() == 2)|| (deleteAdministratorType.getId() == 5 &&  administratorType.getId() == 4)))) {
            administratorService.deleteById(deleteAdministratorId);
            return ResultUtil.Success();
        }else {
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 获得我的景区
     * @param administratorId
     * @return
     * @throws Exception
     */
    @GetMapping("/getMyScenic")
    @ApiOperation("获得我的景区")
    public ResultDTO getMyScenic(Integer administratorId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageScenic()){
            if (administratorAuthority.getAddChildAdmistrator()||administratorAuthority.getAddAdministrator()){
                return ResultUtil.Success(scenicService.findAll());
            }
            return ResultUtil.Success(scenicService.findByParams(new Scenic(administratorId)));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 搜素景区
     * @param administratorId
     * @param attr
     * @return
     * @throws Exception
     */
    @GetMapping("/findMyScenic")
    @ApiOperation("搜素景区")
    public ResultDTO findMyScenic(Integer administratorId,String attr)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageScenic()){
            Scenic scenic = new Scenic();
            scenic.setName(attr);
            if (administratorAuthority.getAddChildAdmistrator()||administratorAuthority.getAddAdministrator()){
                return ResultUtil.Success(scenicService.findByParams(scenic));
            }
            scenic.setAdministratorId(administratorId);
            return ResultUtil.Success(scenicService.findByParams(scenic));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 添加景区
     * @param administratorId
     * @param scenic
     * @return
     * @throws Exception
     */
    @PostMapping("/addScenic")
    @ApiOperation("添加景区")
    public ResultDTO addScenic(Integer administratorId,Scenic scenic)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageScenic()){
            scenic.setAdministratorId(administratorId);
            return ResultUtil.Success(scenicService.save(scenic));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 删除景区
     * @param administratorId
     * @param scenicId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteScenic")
    @ApiOperation("删除景区")
    public ResultDTO deleteScenic(Integer administratorId,Long scenicId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageScenic()){
            if (administratorAuthority.getAddChildAdmistrator()||administratorAuthority.getAddAdministrator()){
                scenicService.deleteById(scenicId);
                return ResultUtil.Success();
            }
            if(scenicService.get(scenicId).getAdministratorId() == administratorId) {
                scenicService.deleteById(scenicId);
                return ResultUtil.Success();
            }
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 获取申请的导游信息
     * @param administratorId
     * @return
     * @throws Exception
     */
    @GetMapping("/getGuideing")
    @ApiOperation("获取申请的导游信息")
    public ResultDTO getGuideing(Integer administratorId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            return ResultUtil.Success(guideService.findByParams(new Guide(0)));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 搜索未审核的导游
     * @param administratorId
     * @param attr
     * @return
     * @throws Exception
     */
    @GetMapping("/findGuideing")
    @ApiOperation("搜索未审核的导游")
    public ResultDTO findGuideing(Integer administratorId,String attr)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            List<Guide> result = new ArrayList<>();
            Guide guide = new Guide();
            guide.setName(attr);
            guide.setState(0);
            result.addAll(guideService.findByParams(guide));
            Guide g = new Guide();
            g.setPhone(attr);
            g.setState(0);
            result.addAll(guideService.findByParams(g));
            return ResultUtil.Success(result);
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }


    /**
     * 获取所有通过导游信息
     * @param administratorId
     * @return
     * @throws Exception
     */
    @GetMapping("/getGuide")
    @ApiOperation("获取所有通过导游信息")
    public ResultDTO getGuide(Integer administratorId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            return ResultUtil.Success(guideService.findByParams(new Guide(1)));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 搜索审核通过的导游
     * @param administratorId
     * @param attr
     * @return
     * @throws Exception
     */
    @GetMapping("/findGuide")
    @ApiOperation("搜索导游")
    public ResultDTO findGuide(Integer administratorId,String attr)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            List<Guide> result = new ArrayList<>();
            Guide guide = new Guide();
            guide.setName(attr);
            result.addAll(guideService.findByParams(guide));
            Guide g = new Guide();
            g.setPhone(attr);
            result.addAll(guideService.findByParams(g));
            return ResultUtil.Success(result);
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }


    /**
     * 获取导游详情
     * @param administratorId
     * @param guideId
     * @return
     * @throws Exception
     */
    @GetMapping("/getGuideMsg")
    @ApiOperation("获取导游详情")
    public ResultDTO getGuideMsg(Integer administratorId,Long guideId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            return ResultUtil.Success(guideService.get(guideId));
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 通过认证
     * @param administratorId
     * @param guideId
     * @return
     * @throws Exception
     */
    @PostMapping("/passGuide")
    @ApiOperation("通过认证")
    public ResultDTO passGuide(Integer administratorId,Long guideId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            guideService.update(new Guide(guideId,1));
            return ResultUtil.Success();
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }

    /**
     * 不通过认证
     * @param administratorId
     * @param guideId
     * @return
     * @throws Exception
     */
    @PostMapping("/noPassGuide")
    @ApiOperation("不通过认证")
    public ResultDTO noPassGuide(Integer administratorId,Long guideId)throws Exception{
        AdministratorAuthority administratorAuthority = administratorAuthorityService.get(
                administratorTypeService.get(
                        administratorService.get(administratorId).getTypeId()).getAuthorityId());
        if(administratorAuthority.getManageGuide()){
            guideService.update(new Guide(guideId,2));
            return ResultUtil.Success();
        }else{
            return ResultUtil.Error(ErrorCode.LIMITED_AUTHORITY);
        }
    }



}
