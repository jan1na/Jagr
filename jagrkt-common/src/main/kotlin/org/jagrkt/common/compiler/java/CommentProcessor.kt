package org.jagrkt.common.compiler.java

import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtComment

class CommentProcessor(): AbstractProcessor<CtComment>() {
  override fun process(element: CtComment) {
    if (element.commentType == CtComment.CommentType.INLINE) {
      element.delete()
    }
  }

}
