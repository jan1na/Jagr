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

package org.sourcegrade.jagr.core.testing

import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.api.testing.RubricConfiguration
import java.util.Collections

class RubricConfigurationImpl : RubricConfiguration {
    private val transformers = mutableListOf<ClassTransformer>()
    private val fileNameSolutionOverrides = mutableListOf<String>()
    override fun getTransformers(): List<ClassTransformer> = Collections.unmodifiableList(transformers)
    override fun getFileNameSolutionOverrides(): MutableList<String> = Collections.unmodifiableList(fileNameSolutionOverrides)

    override fun addTransformer(transformer: ClassTransformer): RubricConfiguration {
        transformers += transformer
        return this
    }

    override fun addFileNameSolutionOverride(fileName: String): RubricConfiguration {
        fileNameSolutionOverrides += fileName
        return this
    }
}
