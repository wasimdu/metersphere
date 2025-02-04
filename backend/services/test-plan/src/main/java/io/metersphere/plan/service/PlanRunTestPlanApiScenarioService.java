package io.metersphere.plan.service;

import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.mapper.ExtTestPlanApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.dto.api.task.*;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlanRunTestPlanApiScenarioService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private TestPlanApiScenarioBatchRunService testPlanApiScenarioBatchRunService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;

    /**
     * 串行批量执行
     *
     * @Return 是否执行完毕
     */
    public boolean serialExecute(TestPlanExecutionQueue testPlanExecutionQueue) {
        String userId = testPlanExecutionQueue.getCreateUser();
        String parentQueueId = testPlanExecutionQueue.getQueueId();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);

        List<String> ids = extTestPlanApiScenarioMapper.getIdsByReportIdAndCollectionId(testPlanExecutionQueue.getPrepareReportId(), collection.getId());

        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        // 先初始化集成报告，设置好报告ID，再初始化执行队列
        ExecutionQueue queue = apiBatchRunBaseService.initExecutionQueue(testPlanExecutionQueue.getTaskId(), runModeConfig, ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name(), parentQueueId, userId);

        SubListUtils.dealForSubList(ids, 100, subIds -> {
            List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.selectExecInfoByResourceIds(subIds);
            // 初始化队列项
            apiBatchRunBaseService.initExecutionQueueDetails(queue.getQueueId(), execTaskItems);
        });

        // 执行第一个任务
        ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(queue.getQueueId());
        executeNextTask(queue, nextDetail);

        return false;
    }

    /**
     * 并行批量执行
     *
     * @return 是否执行完毕
     */
    public boolean parallelExecute(TestPlanExecutionQueue testPlanExecutionQueue) {
        String parentQueueId = testPlanExecutionQueue.getQueueId() + "_" + IDGenerator.nextStr();
        String testPlanReportId = testPlanExecutionQueue.getPrepareReportId();
        String userId = testPlanExecutionQueue.getCreateUser();
        TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
        String testPlanId = collection.getTestPlanId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        ApiRunModeConfigDTO runModeConfig = testPlanApiBatchRunBaseService.getApiRunModeConfig(collection);
        TaskBatchRequestDTO taskRequest = testPlanApiScenarioBatchRunService.getTaskBatchRequestDTO(testPlan.getProjectId(), runModeConfig);
        taskRequest.getTaskInfo().setTaskId(testPlanExecutionQueue.getTaskId());
        taskRequest.getTaskInfo().setParentQueueId(parentQueueId);
        taskRequest.getTaskInfo().setUserId(userId);
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name());
        Map<String, String> resourceTaskItemMap = new HashMap<>();

        List<TestPlanReportApiScenario> testPlanReportApiScenarios = getTestPlanReportApiScenarios(testPlanReportId, collection);
        if (CollectionUtils.isEmpty(testPlanReportApiScenarios)) {
            return true;
        }

        SubListUtils.dealForSubList(testPlanReportApiScenarios, 100,
                subTestPlanReportApiCases-> {
                    List<String> subIds = subTestPlanReportApiCases.stream().map(TestPlanReportApiScenario::getId).toList();
                    extExecTaskItemMapper.selectExecInfoByResourceIds(subIds)
                            .forEach(execTaskItem -> resourceTaskItemMap.put(execTaskItem.getResourceId(), execTaskItem.getId()));
                });

        Map<String, String> scenarioReportMap = initReport(resourceTaskItemMap, testPlanReportApiScenarios, runModeConfig, userId);
        List<TaskItem> taskItems = testPlanReportApiScenarios.stream()
                .map(item -> {
                    TaskItem taskItem = apiExecuteService.getTaskItem(scenarioReportMap.get(item.getId()), item.getId());
                    taskItem.setId(resourceTaskItemMap.get(item.getId()));
                    return taskItem;
                }).toList();

        // 初始化执行集合，以便判断是否执行完毕
        apiExecutionSetService.initSet(parentQueueId, taskItems.stream().map(TaskItem::getId).toList());

        taskRequest.setTaskItems(taskItems);
        apiExecuteService.batchExecute(taskRequest);

        return false;
    }

    private Map<String, String> initReport(Map<String, String> resourceExecTaskItemMap,
                                           List<TestPlanReportApiScenario> testPlanReportApiScenarios,
                                           ApiRunModeConfigDTO runModeConfig, String userId) {

        List<ApiScenario> apiScenarios = new ArrayList<>(testPlanReportApiScenarios.size());

        testPlanReportApiScenarios.stream().sorted(Comparator.comparing(TestPlanReportApiScenario::getPos));

        List<String> caseIds = testPlanReportApiScenarios.stream().map(TestPlanReportApiScenario::getApiScenarioId).toList();
        SubListUtils.dealForSubList(caseIds, 100, subIds -> apiScenarios.addAll(extApiScenarioMapper.getScenarioExecuteInfoByIds(subIds)));

        Map<String, ApiScenario> apiScenarioMap = apiScenarios.stream()
                .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));

        // 初始化独立报告，执行时初始化步骤
        return initScenarioReport(resourceExecTaskItemMap, runModeConfig, testPlanReportApiScenarios, apiScenarioMap, userId);
    }

    public Map<String, String> initScenarioReport(Map<String, String> resourceExecTaskItemMap,
                                                  ApiRunModeConfigDTO runModeConfig,
                                                  List<TestPlanReportApiScenario> testPlanReportApiScenarios,
                                                  Map<String, ApiScenario> apiScenarioMap, String userId) {
        List<ApiScenarioReport> apiScenarioReports = new ArrayList<>(testPlanReportApiScenarios.size());
        List<ApiScenarioRecord> apiScenarioRecords = new ArrayList<>(testPlanReportApiScenarios.size());
        List<ApiReportRelateTask> apiReportRelateTasks = new ArrayList<>();

        Map<String, String> resourceReportMap = new HashMap<>();
        for (TestPlanReportApiScenario testPlanReportApiScenario : testPlanReportApiScenarios) {
            ApiScenario apiScenario = apiScenarioMap.get(testPlanReportApiScenario.getApiScenarioId());
            // 初始化报告
            ApiScenarioReport apiScenarioReport = getScenarioReport(runModeConfig, testPlanReportApiScenario, apiScenario.getProjectId(), userId);
            apiScenarioReport.setTestPlanScenarioId(testPlanReportApiScenario.getTestPlanApiScenarioId());
            // 报告预生成，方便停止测试计划时直接更新报告状态
            apiScenarioReport.setId(testPlanReportApiScenario.getApiScenarioExecuteReportId());
            apiScenarioReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanReportApiScenario.getEnvironmentId()));
            apiScenarioReport.setPlan(true);

            apiScenarioReports.add(apiScenarioReport);
            // 创建报告和用例的关联关系
            ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
            scenarioRecord.setApiScenarioId(apiScenario.getId());
            scenarioRecord.setApiScenarioReportId(apiScenarioReport.getId());
            apiScenarioRecords.add(scenarioRecord);
            resourceReportMap.put(testPlanReportApiScenario.getId(), apiScenarioReport.getId());

            // 创建报告和任务的关联关系
            ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
            apiReportRelateTask.setReportId(apiScenarioReport.getId());
            apiReportRelateTask.setTaskResourceId(resourceExecTaskItemMap.get(testPlanReportApiScenario.getId()));
            apiReportRelateTasks.add(apiReportRelateTask);
        }
        apiScenarioReportService.insertApiScenarioReport(apiScenarioReports, apiScenarioRecords, apiReportRelateTasks);
        return resourceReportMap;
    }

    private ApiScenarioReport getScenarioReport(ApiRunModeConfigDTO runModeConfig, TestPlanReportApiScenario testPlanReportApiScenario, String projectId, String userId) {
        ApiScenarioReport apiScenarioReport = apiScenarioRunService.getScenarioReport(userId);
        apiScenarioReport.setName(testPlanReportApiScenario.getApiScenarioName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        apiScenarioReport.setProjectId(projectId);
        apiScenarioReport.setEnvironmentId(runModeConfig.getEnvironmentId());
        apiScenarioReport.setRunMode(runModeConfig.getRunMode());
        apiScenarioReport.setPoolId(runModeConfig.getPoolId());
        apiScenarioReport.setTriggerMode(TaskTriggerMode.BATCH.name());
        apiScenarioReport.setTestPlanScenarioId(testPlanReportApiScenario.getId());
        apiScenarioReport.setEnvironmentId(apiBatchRunBaseService.getEnvId(runModeConfig, testPlanReportApiScenario.getEnvironmentId()));
        return apiScenarioReport;
    }

    private List<TestPlanReportApiScenario> getTestPlanReportApiScenarios(String testPlanReportId, TestPlanCollection collection) {
        TestPlanReportApiScenarioExample example = new TestPlanReportApiScenarioExample();
        example.createCriteria()
                .andTestPlanReportIdEqualTo(testPlanReportId)
                .andTestPlanCollectionIdEqualTo(collection.getId());
        example.setOrderByClause(" pos asc");
        return testPlanReportApiScenarioMapper.selectByExample(example);
    }


    /**
     * 执行串行的下一个任务
     *
     * @param queue
     * @param queueDetail
     */
    public void executeNextTask(ExecutionQueue queue, ExecutionQueueDetail queueDetail) {
        ApiRunModeConfigDTO runModeConfig = queue.getRunModeConfig();
        String resourceId = queueDetail.getResourceId();

        TestPlanReportApiScenario testPlanReportApiScenario = testPlanReportApiScenarioMapper.selectByPrimaryKey(resourceId);

        if (testPlanReportApiScenario == null) {
            LogUtils.info("当前执行任务的用例已删除 {}", resourceId);
            return;
        }

        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanReportApiScenario.getApiScenarioId());

        // 独立报告，执行到当前任务时初始化报告
        String reportId = initScenarioReport(Map.of(testPlanReportApiScenario.getId(), queueDetail.getTaskItemId()), runModeConfig, List.of(testPlanReportApiScenario), Map.of(apiScenario.getId(), apiScenario), queue.getUserId()).get(resourceId);
        TaskRequestDTO taskRequest = testPlanApiScenarioBatchRunService.getTaskRequestDTO(apiScenario.getProjectId(), queue.getRunModeConfig());
        TaskItem taskItem = apiExecuteService.getTaskItem(reportId, queueDetail.getResourceId());
        taskItem.setId(queueDetail.getTaskItemId());
        taskRequest.setTaskItem(taskItem);
        taskRequest.getTaskInfo().setTaskId(queue.getTaskId());
        taskRequest.getTaskInfo().setQueueId(queue.getQueueId());
        taskRequest.getTaskInfo().setUserId(queue.getUserId());
        taskRequest.getTaskInfo().setParentQueueId(queue.getParentQueueId());
        taskRequest.getTaskInfo().setResourceType(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name());

        apiExecuteService.execute(taskRequest);
    }

    public GetRunScriptResult getRunScript(GetRunScriptRequest request) {
        TaskItem taskItem = request.getTaskItem();
        TestPlanReportApiScenario testPlanReportApiScenario = testPlanReportApiScenarioMapper.selectByPrimaryKey(taskItem.getResourceId());
        ApiScenarioDetail apiScenarioDetail = apiScenarioRunService.getForRun(testPlanReportApiScenario.getApiScenarioId());
        apiScenarioDetail.setEnvironmentId(testPlanReportApiScenario.getEnvironmentId());
        apiScenarioDetail.setGrouped(testPlanReportApiScenario.getGrouped());
        return apiScenarioRunService.getRunScript(request, apiScenarioDetail);
    }
}
