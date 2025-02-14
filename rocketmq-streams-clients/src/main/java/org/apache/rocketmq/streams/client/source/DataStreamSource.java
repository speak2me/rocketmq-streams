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
package org.apache.rocketmq.streams.client.source;

import com.alibaba.fastjson.JSONObject;

import com.google.common.collect.Sets;
import org.apache.rocketmq.streams.client.transform.DataStream;
import org.apache.rocketmq.streams.common.channel.impl.CollectionSource;
import org.apache.rocketmq.streams.common.channel.impl.file.FileSource;
import org.apache.rocketmq.streams.common.channel.impl.memory.MemoryCache;
import org.apache.rocketmq.streams.common.channel.impl.memory.MemorySource;
import org.apache.rocketmq.streams.common.channel.source.ISource;
import org.apache.rocketmq.streams.common.topology.ChainStage;
import org.apache.rocketmq.streams.common.topology.builder.PipelineBuilder;

import org.apache.rocketmq.streams.connectors.source.CycleDynamicMultipleDBScanSource;
import org.apache.rocketmq.streams.connectors.source.DynamicMultipleDBScanSource;
import org.apache.rocketmq.streams.connectors.source.filter.CycleSchedule;
import org.apache.rocketmq.streams.source.RocketMQSource;

import java.util.Set;

public class DataStreamSource {
    protected PipelineBuilder mainPipelineBuilder;
    protected Set<PipelineBuilder> otherPipelineBuilders;

    public DataStreamSource(String namespace, String pipelineName) {
        this.mainPipelineBuilder = new PipelineBuilder(namespace, pipelineName);
        this.otherPipelineBuilders = Sets.newHashSet();
    }

    public static DataStreamSource create(String namespace, String pipelineName) {
        return new DataStreamSource(namespace, pipelineName);
    }

    public static DataStreamSource create(String namespace, String pipelineName, String[] duplicateKeys, Long windowSize) {
        return new DataStreamSource(namespace, pipelineName);
    }

    public DataStream fromArray(Object[] o) {
        MemoryCache cache = new MemoryCache(o);
        return fromMemory(cache, o instanceof JSONObject[]);
    }

    public DataStream fromMemory(MemoryCache memoryCache, boolean isJson) {
        MemorySource memorySource = new MemorySource();
        this.mainPipelineBuilder.addConfigurables(memoryCache);
        memorySource.setMemoryCache(memoryCache);
        memorySource.setJsonData(isJson);
        this.mainPipelineBuilder.setSource(memorySource);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, null);
    }

    public DataStream fromFile(String filePath) {
        return fromFile(filePath, true);
    }

    public DataStream fromFile(String filePath, Boolean isJsonData) {
        FileSource fileChannel = new FileSource(filePath);
        fileChannel.setJsonData(isJsonData);
        this.mainPipelineBuilder.setSource(fileChannel);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, null);
    }

    public DataStream fromRocketmq(String topic, String groupName, String namesrvAddress) {
        return fromRocketmq(topic, groupName, false, namesrvAddress);
    }

    public DataStream fromRocketmq(String topic, String groupName, boolean isJson, String namesrvAddress) {
        return fromRocketmq(topic, groupName, "*", isJson, namesrvAddress);
    }

    public DataStream fromRocketmq(String topic, String groupName, String tags, boolean isJson, String namesrvAddress) {
        RocketMQSource rocketMQSource = new RocketMQSource();
        rocketMQSource.setTopic(topic);
        rocketMQSource.setTags(tags);
        rocketMQSource.setGroupName(groupName);
        rocketMQSource.setJsonData(isJson);
        rocketMQSource.setNamesrvAddr(namesrvAddress);
        this.mainPipelineBuilder.setSource(rocketMQSource);
        return new DataStream(this.mainPipelineBuilder, null);
    }

    public DataStream fromMultipleDB(String url, String userName, String password, String tablePattern) {
        DynamicMultipleDBScanSource source = new DynamicMultipleDBScanSource();
        source.setUrl(url);
        source.setUserName(userName);
        source.setPassword(password);
        source.setBatchSize(10);
        source.setLogicTableName(tablePattern);
        this.mainPipelineBuilder.setSource(source);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, (ChainStage)null);
    }

    public DataStream fromCycleSource(String url, String userName, String password, String tablePattern, CycleSchedule.Cycle cycle, int balanceSec) {
        CycleDynamicMultipleDBScanSource source = new CycleDynamicMultipleDBScanSource(cycle);
        source.setUrl(url);
        source.setUserName(userName);
        source.setPassword(password);
        source.setBatchSize(10);
        source.setLogicTableName(tablePattern);
        source.setBalanceTimeSecond(balanceSec);

        this.mainPipelineBuilder.setSource(source);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, (ChainStage)null);
    }

    public DataStream fromCollection(JSONObject... elements) {
        CollectionSource source = new CollectionSource();
        source.addAll(elements);
        this.mainPipelineBuilder.setSource(source);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, (ChainStage)null);
    }

    public DataStream from(ISource<?> source) {
        this.mainPipelineBuilder.setSource(source);
        return new DataStream(this.mainPipelineBuilder, this.otherPipelineBuilders, null);
    }

}
