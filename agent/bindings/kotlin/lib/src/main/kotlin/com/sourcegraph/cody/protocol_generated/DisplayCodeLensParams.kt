@file:Suppress("FunctionName", "ClassName", "unused", "EnumEntryName", "UnusedImport")
package com.sourcegraph.cody.protocol_generated;

data class DisplayCodeLensParams(
  val uri: String,
  val codeLenses: List<ProtocolCodeLens>,
)

