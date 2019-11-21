package com.xxx.xcloud.module.component.service.worker.zookeeper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;

@Service
@Scope("prototype")
public class ZkClusterChangeResourceWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkClusterChangeResourceWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkClusterChangeResourceWorker====================");
        // 1、获取参数
        String serviceId = data.get("serviceId");
        String tenantName = data.get("tenantName");
        String cpu = data.get("cpu");
        String memory = data.get("memory") + CommonConst.UNIT_GI;

        // 2、获取service
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("集群扩展节点时获取service失败，error：", e);
            return;
        }
        if (null == service) {
            LOG.error("根据serviceId获取的service为null");
            return;
        }
        LOG.info("集群扩展节点时获取的service：" + JSON.toJSONString(service));

        // 3、循环获取修改资源结果
        long start = System.currentTimeMillis();

        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时： " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常");
                break;
            }

            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.info("=============循环获取操作结果超时==========");
                break;
            }

            ZkCluster newCluster = componentOperationsClientUtil.getZkCluster(tenantName, service.getServiceName());
            if (null == newCluster) {
                LOG.error("获取的newCluster为null");
                break;
            }
            LOG.info("newCluster:" + JSON.toJSONString(newCluster));
            if (cpu.equals(newCluster.getSpec().getResources().getLimits().getCpu())
                    && memory.equals(newCluster.getSpec().getResources().getLimits().getMemory())
                    && "true".equals(newCluster.getStatus().getResourceupdateneedrestart())) {
                try {
                    service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
                    if (null != service) {
                        LOG.info("集群修改资源时修改RESOURCE_EFFECTIVE为false");
                        Map<String, String> extendedField = componentOperationsDataBaseUtil
                                .getServiceExtendedField(service.getExtendedField());
                        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
                        service.setExtendedField(JSON.toJSONString(extendedField));
                        statefulServiceRepository.save(service);
                    }
                    break;
                } catch (ErrorMessageException e) {
                    LOG.error("集群修改资源失败:", e);
                    throw e;
                } catch (Exception e) {
                    LOG.error("集群修改资源时修改集群资源和状态失败:", e);
                    throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                            "集群修改资源时修改集群资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
                }
            }
        }
        componentOperationsClientUtil.changeZkClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());
    }
}
