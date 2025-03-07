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

package lodsve.rabbitmq.configuration;

import lodsve.core.properties.relaxedbind.annotations.ConfigurationProperties;
import lodsve.core.properties.relaxedbind.annotations.Required;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * rabbit mq base properties.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2016-01-15 12:00
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "lodsve.rabbit", locations = "${params.root}/framework/rabbit.properties")
public class RabbitProperties {
    @Required
    private String address;
    @Required
    private String username;
    @Required
    private String password;
    /**
     * false: 异常消息直接丢弃
     * true: 异常消息将被重新传递
     */
    private Boolean requeueRejected = true;

    private Map<String, QueueConfig> queues;
}
