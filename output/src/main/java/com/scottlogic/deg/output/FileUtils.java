/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface FileUtils {
    boolean containsInvalidChars(File file);

    boolean isFileEmpty(File file);

    boolean exists(Path path);

    boolean isDirectory(Path path);

    boolean isDirectoryEmpty(Path filepath, int fileCount);

    boolean createDirectories(Path dir) throws IOException;
}