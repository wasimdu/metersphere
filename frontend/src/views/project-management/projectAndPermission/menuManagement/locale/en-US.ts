export default {
  'project.menu.management': 'Application Settings',
  'project.menu.manageTip':
    'You can configure various feature switches based on usage scenarios. After closing them, the corresponding feature entry will be hidden and members will not be able to access the feature and its data. The rule does not affect existing data. When you reopen it, it will restore to the state before closing.',
  'project.menu.name': 'Menu Name',
  'project.menu.pleaseConfig': 'Please configure',
  'project.menu.count': 'Count',

  'project.menu.WORKSTATION_SYNC_RULE': 'Interface Test Update Synchronization Rule',
  'project.menu.TEST_PLAN_CLEAN_REPORT': 'Report Retention Time',
  'project.menu.TEST_PLAN_SHARE_REPORT': 'Report Link Validity Period',
  'project.menu.UI_CLEAN_REPORT': 'Report Retention Time',
  'project.menu.UI_SHARE_REPORT': 'Report Link Validity Period',
  'project.menu.UI_RESOURCE_POOL': 'Execution Resource Pool',
  'project.menu.UI_RESOURCE_POOL_TIP': 'The current resource pool in use is: {name}; you can switch resource pools',
  'project.menu.PERFORMANCE_TEST_CLEAN_REPORT': 'Report Retention Time',
  'project.menu.PERFORMANCE_TEST_SHARE_REPORT': 'Report Link Validity Period',
  'project.menu.PERFORMANCE_TEST_SCRIPT_REVIEWER': 'Script Reviewer',
  'project.menu.PERFORMANCE_TEST_SCRIPT_REVIEWER_TIP':
    'Specify a user to review script steps in interface test cases; you can change the reviewer',
  'project.menu.API_URL_REPEATABLE': 'API Definition URL Repeatability',
  'project.menu.API_CLEAN_REPORT': 'Report Retention Time',
  'project.menu.API_SHARE_REPORT': 'Report Link Validity Period',
  'project.menu.API_RESOURCE_POOL': 'Execution Resource Pool',
  'project.menu.API_RESOURCE_POOL_TIP': 'Execution machine for interface testing',
  'project.menu.API_SCRIPT_REVIEWER': 'Script Reviewer',
  'project.menu.API_SCRIPT_REVIEWER_TIP':
    'Specify a user to review script steps in interface test cases; you can change the reviewer',
  'project.menu.API_ERROR_REPORT_RULE': 'False Alert Rule',
  'project.menu.API_ERROR_REPORT_RULE_TIP':
    'When the interface returns a result that matches the false alert rule, the interface result will be treated as a false alert',
  'project.menu.API_SYNC_CASE': 'Change Synchronized CASE',
  'project.menu.TASK_CLEAN_REPORT': 'Result Retention Time',

  'project.menu.CASE_PUBLIC': 'Public Test Case Library',
  'project.menu.CASE_RE_REVIEW': 'Re-review',
  'project.menu.CASE_RELATED': 'Related Requirements',
  'project.menu.BUG_SYNC': 'Defect Synchronization',
  'project.menu.bugThirdIntegrationTip':
    'No integration of third-party management platform, please contact your administrator',
  'project.menu.SYNC_ENABLE': 'Status',
  'project.menu.MECHANISM': 'Interface Test Update Synchronization Rule',
  'project.menu.row1': 'The system displays data that meets the rules in My To-Do List - To Be Updated',
  'project.menu.row2': 'Bidirectional synchronization with defects of third-party project management platforms',
  'project.menu.row3': 'Add test cases to the public test case library for sharing',
  'project.menu.row4': 'Associate test cases with third-party project management platforms',
  'project.menu.row5':
    'When changes occur in test cases during the review process, the test case status automatically switches to re-review',
  'project.menu.reArraignment':
    'Name of the ”case“, "information on use case steps" change triggers, other changes not trigger',
  'project.menu.row6': 'When enabled, the interface definition module will not validate URL duplication',
  'project.menu.row7': 'Automatically synchronize interface CASE when the interface definition changes',
  'project.menu.notConfig': 'Third-party information not configured, click',
  'project.menu.configure': 'to configure',
  'project.menu.status': 'Status',
  'project.menu.updateSync': 'Update mechanism',
  'project.menu.updateSyncTip':
    'When turned on, the associated requirements will be updated according to the set frequency.',
  'project.menu.incrementalSync': 'Incremental Synchronization',
  'project.menu.incrementalSyncTip': 'Only make content changes to existing third-party defects in MS',
  'project.menu.fullSync': 'Full Synchronization',
  'project.menu.fullSyncTip': 'Fully synchronize defects from third-party platforms to MS',
  'project.menu.platformPlaceholder':
    'Third-party platform not integrated yet, please contact the organization administrator for integration',
  'project.menu.platformLabel': 'Third-party Project Management Platform',
  'project.menu.syncMechanism': 'Synchronization Mechanism',
  'project.menu.CRON_EXPRESSION': 'Synchronization Frequency',
  'project.menu.projectKey': 'Project Key',
  'project.menu.projectId': 'Project ID',
  'project.menu.organizationId': 'Organization ID',
  'project.menu.azureId': 'Azure Filter ID',
  'project.menu.defectType': 'Defect Type',
  'project.menu.demandType': 'Requirement Type',
  'project.menu.howGetJiraKey': 'How to Get JIRA Project Key',
  'project.menu.preview': 'Preview',
  'project.menu.show': 'Show',
  'project.menu.pleaseInputJiraKey': 'Please enter JIRA project key',
  'project.menu.addFalseAlertRules': 'Add False Alert Rule',
  'project.menu.updateFalseAlertRules': 'Update False Alert Rule',
  'project.menu.nameSearch': 'Search by Name',
  // Defect Synchronization
  'project.menu.defect.enableTip':
    'Enable: Synchronize defects created on the platform to third-party project management platforms',
  'project.menu.defect.closeTip':
    'Disable: Defects created on the platform will not be synchronized to third-party project management platforms',
  'project.menu.demand.enableTip':
    'Enable: Test cases created on the platform can be associated with third-party requirements',
  'project.menu.demand.closeTip':
    'Disable: Test cases created on the platform cannot be associated with third-party requirements',
  'project.menu.defect.customLabel': 'Custom Frequency',
  'project.menu.defect.enableAfterConfig': 'Enable after configuring third-party information',
  // False Alert Rule
  'project.menu.rule.name': 'Name',
  'project.menu.rule.enable': 'Status',
  'project.menu.rule.label': 'Label',
  'project.menu.rule.rule': 'Rule',
  'project.menu.rule.creator': 'Creator',
  'project.menu.rule.updateTime': 'Update Time',
  'project.menu.rule.operation': 'Operation',
  'project.menu.rule.ruleName': 'Rule Name',
  'project.menu.rule.ruleExpression': 'Rule condition',
  'project.menu.rule.ruleNameNotNull': 'Rule name cannot be empty',
  'project.menu.rule.ruleNameRepeat': 'Please modify the duplicate name',
  'project.menu.rule.expressionNotNull': 'Limiting condition cannot be empty',
  'project.menu.rule.addRule': 'Add Rule',
  'project.menu.rule.disableRule': 'Disable Rule',
  'project.menu.rule.disableRuleTip':
    'After disabling, the interface result will no longer match this false alert rule.',
  'project.menu.rule.enableRule': 'Enable Rule',
  'project.menu.rule.enableRuleTip':
    'After enabling, the interface result will be matched against the false alert rule first.',
  'project.menu.rule.deleteRule': 'Confirm to delete {size} false alert rules?',
  'project.menu.rule.deleteRuleTip':
    'After deletion, it only takes effect on newly executed test reports. Please be cautious!',
  'project.menu.rule.hasBeenEnabled': 'Have been enabled',
  'project.menu.rule.bar': 'number',
  'project.menu.rule.ruleAlertList': 'List of false alarm rules',
};
