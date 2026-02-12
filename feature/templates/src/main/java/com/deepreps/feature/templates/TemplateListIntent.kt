package com.deepreps.feature.templates

/**
 * User intents for the template list screen.
 */
sealed interface TemplateListIntent {

    /** User tapped a template card to load it into workout setup. */
    data class LoadTemplate(val templateId: Long) : TemplateListIntent

    /** User initiated delete on a template (swipe or long-press). */
    data class RequestDelete(val templateId: Long, val templateName: String) : TemplateListIntent

    /** User confirmed deletion in the confirmation dialog. */
    data object ConfirmDelete : TemplateListIntent

    /** User dismissed the deletion dialog. */
    data object DismissDelete : TemplateListIntent

    /** User tapped create/new template. */
    data object CreateTemplate : TemplateListIntent

    /** User tapped edit on a template. */
    data class EditTemplate(val templateId: Long) : TemplateListIntent

    /** User requested a retry after an error. */
    data object Retry : TemplateListIntent
}
