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

package org.sourcegrade.jagr

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger

fun main(vararg args: String) {
    try {
        MainCommand().main(args)
    } catch (e: Throwable) {
        Jagr.logger.error("A fatal error occurred", e)
        throw e
    }
}

class MainCommand : CliktCommand() {

    /**
     * Command line option to indicate that this process will listen to (via std in) to a grading request
     */
    private val child by option("--child", "-c").flag()
    private val exportOnly by option("--export-only", "-e").flag()
    private val rainbow by option("--rainbow").flag()
    override fun run() {
        if (child) {
            Environment.initializeChildProcess()
            ChildProcGrading().grade()
        } else {
            Environment.initializeMainProcess()
            val startTime = System.currentTimeMillis()
            StandardGrading(rainbow).grade(exportOnly)
            Jagr.logger.info("Time taken: ${System.currentTimeMillis() - startTime}")
        }
    }
}
