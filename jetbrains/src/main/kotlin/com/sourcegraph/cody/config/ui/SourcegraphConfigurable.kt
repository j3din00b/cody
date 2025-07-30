package com.sourcegraph.cody.config.ui

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.sourcegraph.cody.config.CodyApplicationSettings
import com.sourcegraph.cody.config.ui.lang.UpdateMode
import com.sourcegraph.config.ConfigUtil

class SourcegraphConfigurable(val project: Project) :
    BoundConfigurable(ConfigUtil.SOURCEGRAPH_DISPLAY_NAME) {
  private lateinit var dialogPanel: DialogPanel
  private val codyApplicationSettings = CodyApplicationSettings.instance
  private var channel: UpdateChannel = findConfiguredChannel()
  private var updateMode = codyApplicationSettings.updateMode
  private var automaticallyDisableJcefOutOfProcess =
      codyApplicationSettings.automaticallyDisableJcefOutOfProcess

  override fun createPanel(): DialogPanel {
    dialogPanel = panel {
      group("Plugin") {
        row {
          label("Update channel:")
          comboBox(
                  UpdateChannel.values().toList(),
                  SimpleListCellRenderer.create("") { it.presentableText })
              .bindItem({ channel }, { channel = it!! })
        }
        row {
          label("Update mode:")
          comboBox(
                  UpdateMode.values().toList(),
                  SimpleListCellRenderer.create("") { it.presentableText })
              .bindItem({ updateMode }, { updateMode = it!! })
        }
      }
      group("Environment") {
        row {
          checkBox("Automatically disable JCEF out-of-process rendering")
              .bindSelected(
                  { automaticallyDisableJcefOutOfProcess },
                  { automaticallyDisableJcefOutOfProcess = it })
        }
      }
    }
    return dialogPanel
  }

  override fun reset() {
    dialogPanel.reset()
    automaticallyDisableJcefOutOfProcess =
        codyApplicationSettings.automaticallyDisableJcefOutOfProcess
    updateMode = codyApplicationSettings.updateMode
  }

  override fun apply() {
    super.apply()
    codyApplicationSettings.automaticallyDisableJcefOutOfProcess =
        automaticallyDisableJcefOutOfProcess
    codyApplicationSettings.updateMode = updateMode
    if (codyApplicationSettings.updateMode != UpdateMode.Never) {
      CheckUpdatesTask(project).queue()
    }

    applyChannelConfiguration()
  }

  private fun applyChannelConfiguration() {
    val configuredChannel = findConfiguredChannel()
    val newChannel = channel

    if (configuredChannel != newChannel) {
      if (UpdateChannel.Stable != configuredChannel) {
        UpdateSettings.getInstance().storedPluginHosts.remove(configuredChannel.channelUrl)
      }

      if (UpdateChannel.Stable != newChannel) {
        UpdateSettings.getInstance().storedPluginHosts.add(newChannel.channelUrl)
      }
    }
  }

  private fun findConfiguredChannel(): UpdateChannel {
    var currentChannel = UpdateChannel.Stable
    for (channel in UpdateChannel.values().toList()) {
      val url = channel.channelUrl
      if (url != null && UpdateSettings.getInstance().storedPluginHosts.contains(url)) {
        currentChannel = channel
        break
      }
    }
    return currentChannel
  }
}
