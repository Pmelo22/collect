package org.odk.collect.android.instancemanagement.autosend

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils.buildForm
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils.buildInstance
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles.createTempDir

@RunWith(AndroidJUnit4::class)
class InstanceAutoSendFetcherTest {

    private val instancesRepository = InMemInstancesRepository()
    private val formsRepository = InMemFormsRepository()

    private val formWithEnabledAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = "true").build()

    private val instanceOfFormWithEnabledAutoSendIncomplete = buildInstance("1", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendComplete = buildInstance("1", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmissionFailed = buildInstance("1", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmitted = buildInstance("1", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithoutSpecifiedAutoSend = buildForm("2", "1", createTempDir().absolutePath).build()

    private val instanceOfFormWithoutSpecifiedAutoSendIncomplete = buildInstance("2", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendComplete = buildInstance("2", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed = buildInstance("2", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmitted = buildInstance("2", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithDisabledAutoSend = buildForm("3", "1", createTempDir().absolutePath, autosend = "false").build()

    private val instanceOfFormWithDisabledAutoSendIncomplete = buildInstance("3", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendComplete = buildInstance("3", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmissionFailed = buildInstance("3", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmitted = buildInstance("3", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithCustomAutoSend = buildForm("4", "1", createTempDir().absolutePath, autosend = "anything").build()

    private val instanceOfFormWithCustomAutoSendIncomplete = buildInstance("4", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendComplete = buildInstance("4", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmissionFailed = buildInstance("4", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmitted = buildInstance("4", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `return all finalized instances of forms that do not have auto send disabled on a form level`() {
        formsRepository.save(formWithEnabledAutoSend)
        formsRepository.save(formWithoutSpecifiedAutoSend)
        formsRepository.save(formWithDisabledAutoSend)
        formsRepository.save(formWithCustomAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendIncomplete)
            save(instanceOfFormWithEnabledAutoSendComplete)
            save(instanceOfFormWithEnabledAutoSendSubmissionFailed)
            save(instanceOfFormWithEnabledAutoSendSubmitted)

            save(instanceOfFormWithoutSpecifiedAutoSendIncomplete)
            save(instanceOfFormWithoutSpecifiedAutoSendComplete)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmitted)

            save(instanceOfFormWithDisabledAutoSendIncomplete)
            save(instanceOfFormWithDisabledAutoSendComplete)
            save(instanceOfFormWithDisabledAutoSendSubmissionFailed)
            save(instanceOfFormWithDisabledAutoSendSubmitted)

            save(instanceOfFormWithCustomAutoSendIncomplete)
            save(instanceOfFormWithCustomAutoSendComplete)
            save(instanceOfFormWithCustomAutoSendSubmissionFailed)
            save(instanceOfFormWithCustomAutoSendSubmitted)
        }

        val settingsProvider = InMemSettingsProvider().also {
            it.getUnprotectedSettings()
                .save(ProjectKeys.KEY_AUTOSEND, AutoSend.WIFI_ONLY.getValue(application))
        }

        val instancesToSend = InstanceAutoSendFetcher.getInstancesToAutoSend(
            application,
            instancesRepository,
            formsRepository,
            settingsProvider
        )

        assertThat(
            instancesToSend.map { it.instanceFilePath },
            contains(
                instanceOfFormWithEnabledAutoSendComplete.instanceFilePath,
                instanceOfFormWithEnabledAutoSendSubmissionFailed.instanceFilePath,
                instanceOfFormWithoutSpecifiedAutoSendComplete.instanceFilePath,
                instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed.instanceFilePath,
                instanceOfFormWithCustomAutoSendComplete.instanceFilePath,
                instanceOfFormWithCustomAutoSendSubmissionFailed.instanceFilePath
            )
        )
    }

    @Test
    fun `if there are multiple versions of one form and only one has auto-send enabled take only instances of that form`() {
        val formWithEnabledAutoSendV1 = buildForm("1", "1", createTempDir().absolutePath, autosend = "false").build()
        val instanceOfFormWithEnabledAutoSendCompleteV1 = buildInstance("1", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        val formWithEnabledAutoSendV2 = buildForm("1", "2", createTempDir().absolutePath, autosend = "true").build()
        val instanceOfFormWithEnabledAutoSendCompleteV2 = buildInstance("1", "2", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        formsRepository.save(formWithEnabledAutoSendV1)
        formsRepository.save(formWithEnabledAutoSendV2)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendCompleteV1)
            save(instanceOfFormWithEnabledAutoSendCompleteV2)
        }

        val settingsProvider = InMemSettingsProvider().also {
            it.getUnprotectedSettings()
                .save(ProjectKeys.KEY_AUTOSEND, AutoSend.WIFI_ONLY.getValue(application))
        }

        val instancesToSend = InstanceAutoSendFetcher.getInstancesToAutoSend(
            application,
            instancesRepository,
            formsRepository,
            settingsProvider
        )
        assertThat(
            instancesToSend.map { it.instanceFilePath },
            contains(
                instanceOfFormWithEnabledAutoSendCompleteV2.instanceFilePath
            )
        )
    }
}
