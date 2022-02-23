import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.context.IssueContext
import com.atlassian.jira.issue.context.IssueContextImpl
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager

// the project key under which the issue will get created
final projectKey = 'TEST'

// the issue type for the new issue
final issueTypeName = 'Bug'

// user with that user key will be the reporter of the issue
final reporterKey = 'auser'

// the summary of the new issue
final summary = 'Groovy Friday'

// the priority of the new issue
final priorityName = 'Major'

def issueService = ComponentAccessor.issueService
def constantsManager = ComponentAccessor.constantsManager
def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
def prioritySchemeManager = ComponentAccessor.getComponent(PrioritySchemeManager)

def project = ComponentAccessor.projectManager.getProjectObjByKey(projectKey)
assert project : "Could not find project with key $projectKey"

def issueType = constantsManager.allIssueTypeObjects.findByName(issueTypeName)
assert issueType : "Could not find issue type with name $issueTypeName"

// if we cannot find user with the specified key or this is null, then set as a  reporter the logged in user
def reporter = ComponentAccessor.userManager.getUserByKey(reporterKey) ?: loggedInUser

// if we cannot find the priority with the given name or if this is null, then set the default priority
def issueContext = new IssueContextImpl(project, issueType) as IssueContext
def priorityId = constantsManager.priorities.findByName(priorityName)?.id ?: prioritySchemeManager.getDefaultOption(issueContext)

def issueInputParameters = issueService.newIssueInputParameters().with {
    setProjectId(project.id)
    setIssueTypeId(issueType.id)
    setReporterId(reporter.name)
    setSummary(summary)
    setPriorityId(priorityId)
}

def validationResult = issueService.validateCreate(loggedInUser, issueInputParameters)
assert validationResult.valid : validationResult.errorCollection

def result = issueService.create(loggedInUser, validationResult)
assert result.valid : result.errorCollection
