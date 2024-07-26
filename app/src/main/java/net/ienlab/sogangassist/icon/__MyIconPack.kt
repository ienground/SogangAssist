package net.ienlab.sogangassist.icon

import androidx.compose.ui.graphics.vector.ImageVector
import net.ienlab.sogangassist.icon.myiconpack.Assignment
import net.ienlab.sogangassist.icon.myiconpack.LiveClass
import net.ienlab.sogangassist.icon.myiconpack.Team
import net.ienlab.sogangassist.icon.myiconpack.Test
import net.ienlab.sogangassist.icon.myiconpack.Video
import net.ienlab.sogangassist.icon.myiconpack.VideoSup
import kotlin.collections.List as ____KtList

public object MyIconPack

private var __MyIcon: ____KtList<ImageVector>? = null

public val MyIconPack.MyIcon: ____KtList<ImageVector>
  get() {
    if (__MyIcon != null) {
      return __MyIcon!!
    }
    __MyIcon= listOf(VideoSup, LiveClass, Team, Assignment, Video, Test)
    return __MyIcon!!
  }
