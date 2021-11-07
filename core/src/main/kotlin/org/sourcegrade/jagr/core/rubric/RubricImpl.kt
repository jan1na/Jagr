/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.core.rubric

import com.google.common.base.MoreObjects
import org.sourcegrade.jagr.api.rubric.CriterionHolderPointCalculator
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Graded
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.api.rubric.Rubric
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.writeList

class RubricImpl(
  private val title: String,
  private val criteria: List<CriterionImpl>,
) : Rubric {

  init {
    for (criterion in criteria) {
      criterion.setParent(this)
    }
  }

  private val maxPointsKt: Int by lazy { CriterionHolderPointCalculator.maxOfChildren(0).getPoints(this) }
  private val minPointsKt: Int by lazy { CriterionHolderPointCalculator.minOfChildren(0).getPoints(this) }

  override fun getTitle(): String = title
  override fun getMaxPoints(): Int = maxPointsKt
  override fun getMinPoints(): Int = minPointsKt
  override fun getChildCriteria(): List<CriterionImpl> = criteria

  override fun grade(testCycle: TestCycle): GradedRubric {
    val childGraded = childCriteria.map { it.grade(testCycle) }
    val gradeResult = with(testCycle.submission.compileResult) {
      val comments = testCycle.notes.toMutableList()
      when {
        errorCount > 0 -> "Your submission did not compile. There were $errorCount errors and $warningCount warnings."
        warningCount > 0 -> "Your submission compiled, but there were $warningCount warnings."
        otherCount > 0 || messages.isNotEmpty() -> "Your submission compiled, but there were some messages from the compiler."
        else -> null
      }?.also(comments::add)
      comments += messages
      GradeResult.withComments(GradeResult.of(GradeResult.ofNone(), childGraded.map(Graded::getGrade)), comments)
    }.let {
      val netPoints = it.netPoints
      if (netPoints < 0) {
        GradeResult.withComments(GradeResult.of(it.correctPoints, it.incorrectPoints + netPoints), it.comments)
      } else it
    }
    return GradedRubricImpl(testCycle, gradeResult, this, childGraded)
  }

  private val stringRep: String by lazy {
    MoreObjects.toStringHelper(this)
      .add("title", title)
      .add("maxPoints", maxPointsKt)
      .add("minPoints", minPointsKt)
      .add("childCriteria", "[" + criteria.joinToString(", ") + "]")
      .toString()
  }

  override fun toString(): String = stringRep

  companion object Factory : SerializerFactory<RubricImpl> {
    override fun read(scope: SerializationScope.Input) = RubricImpl(scope.input.readUTF(), scope.readList())

    override fun write(obj: RubricImpl, scope: SerializationScope.Output) {
      scope.output.writeUTF(obj.title)
      scope.writeList(obj.criteria)
    }
  }
}
