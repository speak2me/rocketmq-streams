/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.streams.filter.operator.var;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.streams.common.context.AbstractContext;
import org.apache.rocketmq.streams.common.context.IMessage;

@SuppressWarnings("rawtypes")
public class InnerVar extends Var {

    private static final long serialVersionUID = -166963014761276615L;
    public static final String ORIG_MESSAGE = "inner_message";

    @Override
    public Object doMessage(IMessage message, AbstractContext context) {
        String varName = getVarName();
        if (varName.equals(ORIG_MESSAGE)) {
            return message.getMessageBody().toJSONString();
        }
        return null;
    }

    @Override
    public boolean canLazyLoad() {
        return false;
    }



}
