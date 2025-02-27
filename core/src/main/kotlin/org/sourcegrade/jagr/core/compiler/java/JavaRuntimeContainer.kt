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

package org.sourcegrade.jagr.core.compiler.java

import org.sourcegrade.jagr.core.compiler.ProcessedContainer
import org.sourcegrade.jagr.core.compiler.ResourceCollector
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.launcher.io.ResourceContainerInfo

data class JavaRuntimeContainer(
    override val info: ResourceContainerInfo,
    override val resourceCollector: ResourceCollector,
    override val runtimeResources: RuntimeResources,
) : RuntimeContainer {
    override val source: ProcessedContainer? = null
}
