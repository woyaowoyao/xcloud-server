package com.xxx.xcloud.rest.v1.component;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.module.component.service.IComponentService;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.rest.v1.component.model.ConfigDTO;
import com.xxx.xcloud.rest.v1.component.model.RedisCreateDTO;
import com.xxx.xcloud.rest.v1.component.model.RedisOperatorDTO;
import com.xxx.xcloud.rest.v1.component.model.UpdateResourceDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * @ClassName: RedisController
 * @Description: redis
 * @author lnn
 * @date 2019年11月18日
 *
 */
@Controller
@Api(value = "Redis接口")
@RequestMapping("/v1/component")
public class RedisController {

    @Autowired
    @Qualifier("componentServiceRedisImpl")
    IComponentService componentServiceRedisImpl;

    /**
     * 创建集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建集群", notes = "")
    public ApiResult createRedisCluster(@RequestBody RedisCreateDTO json) {

        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, String> map = null;

        // 解析参数
        try {
            map = componentServiceRedisImpl.checkClusterCreate(json.getTenantName(), jsonObject);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        // 创建集群
        StatefulService statefulService = null;
        try {
            statefulService = componentServiceRedisImpl.clusterCreate(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, statefulService, "创建redis服务成功");
    }

    /**
     * 启动,停止,新增实例
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "服务启动,服务停止,新增实例", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "redis服务ID", required = true, dataType = "String")
    public ApiResult operatorRedisCluster(@PathVariable("serviceId") String serviceId,
            @RequestBody RedisOperatorDTO json) {

        String tenantName = json.getTenantName();
        String operator = json.getOperator();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        try {
            switch (operator) {
            case "stop":
                componentServiceRedisImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceRedisImpl.clusterStop(tenantName, serviceId);
                break;
            case "start":
                componentServiceRedisImpl.checkClusterUpdate(tenantName, serviceId);
                componentServiceRedisImpl.clusterStart(tenantName, serviceId);
                break;
            case "expand":
                Map<String, String> map = componentServiceRedisImpl.checkClusterExpand(tenantName, serviceId,
                        jsonObject);
                componentServiceRedisImpl.clusterExpand(map);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作redis服务成功");
    }

    /**
     * 删除集群
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除集群", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "redis服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String") })
    public ApiResult deleteRedisCluster(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        try {
            componentServiceRedisImpl.checkClusterUpdate(tenantName, serviceId);
            componentServiceRedisImpl.clusterDelete(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除redis服务成功");
    }

    /**
     * 修改服务资源
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}/resource" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务资源", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "redis服务ID", required = true, dataType = "String")
    public ApiResult changeRedisResource(@PathVariable("serviceId") String serviceId,
            @RequestBody UpdateResourceDTO json) {

        String tenantName = json.getTenantName();
        String jsonString = JSONObject.toJSONString(json);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        try {
            Map<String, String> map = componentServiceRedisImpl.checkChangeResource(tenantName, serviceId, jsonObject);
            componentServiceRedisImpl.changeResource(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务资源成功");
    }

    /**
     * 修改服务配置
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}/config" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配置", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "redis服务ID", required = true, dataType = "String")
    public ApiResult changeRedisConfig(@PathVariable("serviceId") String serviceId, @RequestBody ConfigDTO json) {

        String tenantName = json.getTenantName();
        try {
            Map<String, String> map = componentServiceRedisImpl.checkChangeConfig(tenantName, serviceId, json.getInfo());
            componentServiceRedisImpl.changeConfig(map);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改服务配置成功");
    }

    /**
     * 查询服务列表(page)
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务列表(page)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceName", value = "服务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceState", value = "服务状态", required = false, dataType = "String") })
    public ApiResult getRedisPage(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "serviceState", required = false) String serviceState) {

        Pageable pageable = PageUtil.getPageable(page, size);
        Page<StatefulService> serviceList = null;
        try {
            serviceList = componentServiceRedisImpl.getServiceList(tenantName, projectId, serviceName, serviceState,
                    CommonConst.APPTYPE_REDIS, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceList, "查询服务列表(page)成功");
    }

    /**
     * 查询服务详情
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询服务详情", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult getRedisInfo(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName) {

        StatefulService service = null;

        try {
            service = componentServiceRedisImpl.getServiceInfo(tenantName, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "查询服务详情成功");
    }

    /**
     * 查询节点列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/{serviceId}/node" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询节点列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "instanceName", value = "节点名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "nodeState", value = "节点状态", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "role", value = "节点角色", required = false, dataType = "String") })
    public ApiResult getRedisNodeList(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "instanceName", required = false) String instanceName,
            @RequestParam(value = "nodeState", required = false) String nodeState,
            @RequestParam(value = "role", required = false) String role) {

        List<StatefulNode> nodeList = null;
        try {
            nodeList = componentServiceRedisImpl.getNodeList(tenantName, serviceId, instanceName, role, nodeState);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, nodeList, "查询节点列表成功");
    }

    /**
     * 查询集群的配置参数
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/config" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询集群的配置参数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID(创建查询为空)", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "version", value = "版本(创建后查询为空)", required = false, dataType = "String") })
    public ApiResult getRedisConfig(@RequestParam(value = "serviceId", required = false) String serviceId,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "version", required = false) String version) {

        Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> configMap = null;
        try {
            configMap = componentServiceRedisImpl.getServiceConfig(tenantName, serviceId, CommonConst.APPTYPE_REDIS,
                    version);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, configMap, "查询集群的配置参数");
    }

    /**
     * 查询版本列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/redis/version" }, method = RequestMethod.GET)
    @ApiOperation(value = "查询版本列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "extendedField", value = "扩展字段", required = false, dataType = "String")
    public ApiResult getRedisVersion(@RequestParam(value = "extendedField", required = false) String extendedField) {

        List<String> versionList = null;
        try {
            versionList = componentServiceRedisImpl.getUnitVersion(CommonConst.APPTYPE_REDIS, extendedField);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, versionList, "查询版本列表成功");
    }

}
