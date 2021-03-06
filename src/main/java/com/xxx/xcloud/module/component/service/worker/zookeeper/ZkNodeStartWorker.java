package com.xxx.xcloud.module.component.service.worker.zookeeper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;

@Service
@Scope("prototype")
public class ZkNodeStartWorker extends BaseZkClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(ZkNodeStartWorker.class);

    @Override
    public void execute() {
        LOG.info("===============ZkNodeStartWorker====================");
        // 1、获取参数
        String tenantName = data.get("tenantName");
        String nodeId = data.get("nodeId");

        // 2、获取node
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            componentOptNodeBase(CommonConst.APPTYPE_ZK, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        // 3、获取service
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(node.getServiceId());
        if (null == service) {
            LOG.error("根据serviceId获取statefulService为空,serviceId:" + node.getServiceId());
            return;
        }

        // 4、拼接zk cluster
        ZkCluster newCluster = updateYamlForZkCluster(tenantName, service.getServiceName(),
                ZkClusterConst.OPERATOR_NODE_START, node.getNodeName(), 0);

        // 5、调用k8sclient启动节点并循环获取启动结果
        if (!updateAndRetry(tenantName, newCluster)) {
            LOG.error("zk节点" + node.getNodeName() + "启动失败！");
            componentOptNodeBase(CommonConst.APPTYPE_ZK, nodeId, CommonConst.STATE_NODE_FAILED);
            return;
        }

        boolean checkResult = checkNodeStartOrStopResult(tenantName, service.getServiceName(),
                ZkClusterConst.OPERATOR_NODE_START, CommonConst.STATE_NODE_RUNNING, node.getNodeName());
        if (checkResult) {
            LOG.info("Zk节点启动成功，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        } else {
            LOG.error("Zk节点启动失败，tenantName:" + tenantName + ",nodeName:" + node.getNodeName());
        }

        optNodeStart(tenantName, service.getId(), service.getServiceName(), node, checkResult);

    }

    /**
     * 处理节点启动
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param node
     * @param checkResult
     */
    private void optNodeStart(String tenantName, String serviceId, String serviceName, StatefulNode node,
            boolean checkResult) {
        // 获取zkCluster
        ZkCluster zkCluster = componentOperationsClientUtil.getZkCluster(tenantName, serviceName);

        // 获取集群running时集群的额外信息，包含连接串
        Map<String, String> serviceExtendedField = buildServiceExtendedField(zkCluster);
        // 获取集群running时节点的额外信息，包含ip，port
        Map<String, Map<String, String>> nodesExtendedField = buildNodesExtendedField(zkCluster);

        componentOperationsClientUtil.changeZkClusterAndNodesStateAndExtendedByYaml(tenantName, serviceId, serviceName,
                serviceExtendedField, nodesExtendedField);

        // 注册lvm，处理之前创建时failed或新增的节点（修改资源后）启动
        if (checkResult && null != nodesExtendedField && nodesExtendedField.containsKey(node.getNodeName())) {
            String exterHost = nodesExtendedField.get(node.getNodeName()).get("ip");
            componentOperationsClientUtil.registerLvm(tenantName, node.getLvmName(), exterHost,
                    ZkClusterConst.ZK_CAPACITY_DEFAULT);
        }
    }

}
