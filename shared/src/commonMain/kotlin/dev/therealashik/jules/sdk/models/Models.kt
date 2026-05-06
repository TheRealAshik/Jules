package dev.therealashik.jules.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SessionState {
    @SerialName("STATE_UNSPECIFIED") STATE_UNSPECIFIED,
    @SerialName("QUEUED") QUEUED,
    @SerialName("PLANNING") PLANNING,
    @SerialName("AWAITING_PLAN_APPROVAL") AWAITING_PLAN_APPROVAL,
    @SerialName("AWAITING_USER_FEEDBACK") AWAITING_USER_FEEDBACK,
    @SerialName("IN_PROGRESS") IN_PROGRESS,
    @SerialName("PAUSED") PAUSED,
    @SerialName("COMPLETED") COMPLETED,
    @SerialName("FAILED") FAILED
}

@Serializable
enum class AutomationMode {
    @SerialName("AUTOMATION_MODE_UNSPECIFIED") AUTOMATION_MODE_UNSPECIFIED,
    @SerialName("AUTO_CREATE_PR") AUTO_CREATE_PR
}

@Serializable
data class Session(
    val name: String = "",
    val id: String = "",
    val prompt: String = "",
    val title: String = "",
    val state: SessionState = SessionState.STATE_UNSPECIFIED,
    val url: String = "",
    val sourceContext: SourceContext? = null,
    val automationMode: AutomationMode = AutomationMode.AUTOMATION_MODE_UNSPECIFIED,
    val outputs: List<SessionOutput> = emptyList(),
    val createTime: String = "",
    val updateTime: String = ""
)

@Serializable
data class GitHubBranch(
    val displayName: String = ""
)

@Serializable
data class GitHubRepo(
    val owner: String = "",
    val repo: String = "",
    val isPrivate: Boolean = false,
    val defaultBranch: GitHubBranch? = null,
    val branches: List<GitHubBranch> = emptyList()
)

@Serializable
data class GitHubRepoContext(
    val startingBranch: String
)

@Serializable
data class SourceContext(
    val source: String,
    val githubRepoContext: GitHubRepoContext? = null
)

@Serializable
data class Source(
    val name: String = "",
    val id: String = "",
    val githubRepo: GitHubRepo? = null
)

@Serializable
data class PullRequest(
    val url: String = "",
    val title: String = "",
    val description: String = ""
)

@Serializable
data class SessionOutput(
    val pullRequest: PullRequest? = null
)

@Serializable
data class PlanStep(
    val id: String = "",
    val index: Int = 0,
    val title: String = "",
    val description: String = ""
)

@Serializable
data class Plan(
    val id: String = "",
    val steps: List<PlanStep> = emptyList(),
    val createTime: String = ""
)

@Serializable
data class GitPatch(
    val baseCommitId: String = "",
    val unidiffPatch: String = "",
    val suggestedCommitMessage: String = ""
)

@Serializable
data class ChangeSet(
    val source: String = "",
    val gitPatch: GitPatch? = null
)

@Serializable
data class BashOutput(
    val command: String = "",
    val output: String = "",
    val exitCode: Int = 0
)

@Serializable
data class Media(
    val mimeType: String = "",
    val data: String = ""
)

@Serializable
data class Artifact(
    val changeSet: ChangeSet? = null,
    val bashOutput: BashOutput? = null,
    val media: Media? = null
)

@Serializable
data class PlanGenerated(val plan: Plan? = null)

@Serializable
data class PlanApproved(val planId: String = "")

@Serializable
data class UserMessaged(val userMessage: String = "")

@Serializable
data class AgentMessaged(val agentMessage: String = "")

@Serializable
data class ProgressUpdated(val title: String = "", val description: String = "")

@Serializable
class SessionCompleted

@Serializable
data class SessionFailed(val reason: String = "")

@Serializable
data class Activity(
    val name: String = "",
    val id: String = "",
    val originator: String = "",
    val description: String = "",
    val createTime: String = "",
    val artifacts: List<Artifact> = emptyList(),
    @SerialName("planGenerated") val planGenerated: PlanGenerated? = null,
    @SerialName("planApproved") val planApproved: PlanApproved? = null,
    @SerialName("userMessaged") val userMessaged: UserMessaged? = null,
    @SerialName("agentMessaged") val agentMessaged: AgentMessaged? = null,
    @SerialName("progressUpdated") val progressUpdated: ProgressUpdated? = null,
    @SerialName("sessionCompleted") val sessionCompleted: SessionCompleted? = null,
    @SerialName("sessionFailed") val sessionFailed: SessionFailed? = null
)

@Serializable
data class CreateSessionRequest(
    val prompt: String,
    val title: String? = null,
    val sourceContext: SourceContext? = null,
    val requirePlanApproval: Boolean? = null,
    val automationMode: AutomationMode? = null
)

@Serializable
data class SendMessageRequest(
    val prompt: String
)

@Serializable
class SendMessageResponse

@Serializable
class ApprovePlanRequest

@Serializable
class ApprovePlanResponse

@Serializable
data class ListSessionsResponse(
    val sessions: List<Session> = emptyList(),
    val nextPageToken: String? = null
)

@Serializable
data class ListActivitiesResponse(
    val activities: List<Activity> = emptyList(),
    val nextPageToken: String? = null
)

@Serializable
data class ListSourcesResponse(
    val sources: List<Source> = emptyList(),
    val nextPageToken: String? = null
)
