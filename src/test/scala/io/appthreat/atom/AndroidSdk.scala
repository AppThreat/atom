package io.appthreat.atom

import better.files.File

/** Whether a dex input can be analysed here: the dex frontend needs an Android SDK's `android.jar`,
  * from `ANDROID_HOME` or the default SDK location on macOS. CI's Windows runners have neither.
  */
object AndroidSdk:
  lazy val missing: Option[String] =
    val found = Seq(
      sys.env.get("ANDROID_HOME"),
      Some(s"${sys.props("user.home")}/Library/Android/sdk")
    ).flatten.map(File(_)).exists(d => d.isDirectory && d.glob("**/android.jar").hasNext)
    Option.when(!found)("dex analysis needs an Android SDK (ANDROID_HOME)")
