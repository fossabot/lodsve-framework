/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.redis.timer;

import lodsve.redis.core.connection.LodsveRedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * redis timer MessageListenerContainer.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2017/12/17 上午12:34
 */
public class RedisTimerMessageListenerContainer extends RedisMessageListenerContainer {
    public RedisTimerMessageListenerContainer(LodsveRedisConnectionFactory connectionFactory, RedisTimerListener listener) {
        super.setConnectionFactory(connectionFactory);
        super.addMessageListener(listener, new PatternTopic("__keyevent@*:expired"));
    }
}
