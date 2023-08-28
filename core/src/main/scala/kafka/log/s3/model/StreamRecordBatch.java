/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.log.s3.model;

import com.automq.elasticstream.client.api.RecordBatch;

public class StreamRecordBatch implements Comparable<StreamRecordBatch> {
    private final long streamId;
    private final long epoch;
    private final long baseOffset;
    private final RecordBatch recordBatch;

    public StreamRecordBatch(long streamId, long epoch, long baseOffset, RecordBatch recordBatch) {
        this.streamId = streamId;
        this.epoch = epoch;
        this.baseOffset = baseOffset;
        this.recordBatch = recordBatch;
    }

    public long getStreamId() {
        return streamId;
    }

    public long getEpoch() {
        return epoch;
    }

    public long getBaseOffset() {
        return baseOffset;
    }

    public long getLastOffset() {
        return baseOffset + recordBatch.count();
    }

    public RecordBatch getRecordBatch() {
        return recordBatch;
    }

    @Override
    public int compareTo(StreamRecordBatch o) {
        int rst = Long.compare(streamId, o.streamId);
        if (rst != 0) {
            return rst;
        }
        rst = Long.compare(epoch, o.epoch);
        if (rst != 0) {
            return rst;
        }
        return Long.compare(baseOffset, o.baseOffset);
    }
}
