/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasset.wallet.core.random;


import com.dasset.wallet.core.contant.Constant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class URandom {

    public static synchronized byte[] nextBytes(int length) {
        File file = new File(Constant.FILE_URANDOM);
        byte[] bytes = new byte[length];
        if (!file.exists()) {
            throw new RuntimeException("Unable to generate URandom bytes on this Android device");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            dataInputStream.readFully(bytes);
            dataInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate URandom bytes on this Android device", e);
        }
        return bytes;
    }

}
